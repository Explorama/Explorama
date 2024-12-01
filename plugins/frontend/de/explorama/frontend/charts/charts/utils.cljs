(ns de.explorama.frontend.charts.charts.utils
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.ui-base.utils.interop :refer [format safe-aget]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :as timbre
             :refer-macros [debug warn trace]]
            [de.explorama.frontend.charts.config :as config]
            [de.explorama.frontend.charts.path :as path]))

(defonce instances (atom {}))

(defn instance-available? [frame-id]
  (boolean (get @instances frame-id)))

(defn clean-instance [frame-id]
  (let [^js instance (get @instances frame-id)]
    (when instance
      (swap! instances dissoc frame-id)
      (.destroy instance)
      (debug "chart-instance destroyed for frame id " frame-id))))

(defn save-instance [frame-id instance]
  (let [instance (get @instances frame-id)]
    (when instance
      (clean-instance frame-id)))
  (swap! instances assoc frame-id instance))

(defn- raw-context-obj [context cur-lang]
  (let [chart-type (safe-aget context "dataset" "type")
        x-y? (#{"scatter" "bubble"} chart-type)
        dataset-label (safe-aget context "dataset" "label")
        x-val (safe-aget context "raw" "x")
        y-value (safe-aget context "raw" "y")
        y-label (if (number? y-value)
                  (i18n/localized-number y-value @cur-lang)
                  y-value)
        freq-val (safe-aget context "raw" "freq")
        r-val (safe-aget context "raw" "r-label")
        label (cond
                (and x-y? r-val) (format "(%s, %s, %s)"
                                         x-val
                                         y-label
                                         (i18n/localized-number r-val @cur-lang))
                x-y? (format "(%s, %s)"
                             x-val
                             y-label)
                :else (str dataset-label ": " y-label))]
    (if freq-val
      (str label ", Events " (i18n/localized-number freq-val @cur-lang))
      label)))

(defn- raw-context-num-string
  "This seems only to happen when a catecory attribute is used for the x-axis
   and also only for bar-chart.
   Otherwise raw should be always an object."
  [context cur-lang]
  (let [x-label (safe-aget context "label")
        y-value (safe-aget context "raw")
        y-label (if (number? y-value)
                  (i18n/localized-number y-value @cur-lang)
                  y-value)
        label (format "(%s, %s)"
                      x-label
                      y-label)]
    label))

(defn tooltip-label-fn [cur-lang]
  (fn tooltip [context]
    (let [raw-content (safe-aget context "raw")
          context-raw-num? (number? raw-content)
          context-raw-string? (string? raw-content)]
      (if (or context-raw-num? context-raw-string?)
        (raw-context-num-string context cur-lang)
        (raw-context-obj context cur-lang)))))

(defn tooltip-title-fn []
  (fn [context]
    (let [chart-type (safe-aget context 0 "dataset" "type")]
      (when-not (#{"scatter" "bubble"} chart-type)
        (safe-aget context 0 "raw" "x")))))

(defn y-axis-label-fn [cur-lang]
  (fn [value _ _]
    (when value
      (-> (js/Intl.NumberFormat. (name @cur-lang)) (.format value)))))

(defn theme-colors [theme]
  (case theme
    :light [config/light-mode-text-color config/light-mode-grid-color]
    :dark [config/dark-mode-text-color config/dark-mode-grid-color]
    [config/light-mode-text-color config/light-mode-grid-color]))

(defn x-axis [x-axis-time labels x theme]
  (let [[text-color border-color]
        (theme-colors theme)]
    {:type   (if x-axis-time
               "time"
               "category")
     :title  {:display true
              :color   text-color
              :text    x}
     :ticks  {:major       {:enabled   true
                            :fontStyle "bold"}
              :minor       {:enabled true}
              :color       text-color
              :autoSkip    true
              :maxRotation 75}
     :grid {:color       border-color
            :borderColor border-color}
     :time   x-axis-time
     :labels (when-not x-axis-time labels)}))

(defn- chart-desc-selection [chart-descs]
  (mapv (fn [chart-desc]
          (-> chart-desc
              (select-keys [path/r-option-key
                            path/y-option-key
                            path/sum-remaining-key
                            path/y-range-change?-key
                            path/changed-y-range-key
                            path/aggregate-method-key
                            path/sum-by-option-key
                            path/sum-by-values-key
                            path/chart-type-desc-key
                            path/attributes-key
                            path/stopping-attrs-key
                            path/stemming-attrs-key
                            path/min-occurence-key])
              (update path/chart-type-desc-key
                      select-keys
                      [path/chart-desc-id-key
                       path/chart-desc-label-key])))
        chart-descs))

(defn frame-selection [db frame-id]
  (-> (get-in db (path/chart-frame frame-id))
      (select-keys [path/chart-desc-key])
      (update path/chart-desc-key select-keys [path/charts-key path/x-option-key])
      (update-in [path/chart-desc-key path/charts-key] chart-desc-selection)))

(defn log-chart-update [db frame-id]
  [:de.explorama.frontend.charts.event-logging/log-event
   frame-id
   "update-chart"
   {:selection (frame-selection db frame-id)
    :di (get-in db (path/frame-di frame-id))
    :local-filter (get-in db (path/applied-filter frame-id))}])

(defn y-range-change-valid? [chart-desc]
  (let [y-changed? (get chart-desc path/y-range-change?-key)
        {min-change-valid? :min
         max-change-valid? :max} (get chart-desc path/changed-y-range-key)]
    (or (not y-changed?)
        (and y-changed?
             min-change-valid?
             max-change-valid?))))

(defonce timeout-requests (atom {}))

(defn clear-timeout-request [frame-id]
  (when-let [old-timeout (get-in @timeout-requests frame-id)]
    (js/clearTimeout old-timeout)
    (swap! timeout-requests dissoc frame-id)))

(defn start-timeout-request [frame-id]
  (clear-timeout-request frame-id)
  (swap! timeout-requests
         assoc frame-id
         (js/setTimeout (fn []
                          (swap! timeout-requests dissoc frame-id)
                          (re-frame/dispatch [::ddq/queue frame-id
                                              [:de.explorama.frontend.charts.charts.backend-interface/request-datasets frame-id]]))
                        config/change-request-delay)))

(defn req-datasets
  ([_db frame-id req-fun]
   (when (fn? req-fun) (req-fun))
   (clear-timeout-request frame-id)
   [[::ddq/queue frame-id
     [:de.explorama.frontend.charts.charts.backend-interface/request-datasets frame-id]]])
  ([db frame-id]
   (req-datasets db frame-id nil)))

(defn row-data-selection-id [frame-id]
  (str frame-id "-data-selection"))

(defn row-advanced-selection-id [frame-id]
  (str frame-id "-advanced-selection"))

(defn row-chart-options-id [frame-id]
  (str frame-id "-chart-options"))

(defn row-chart-selection-id [frame-id]
  (str frame-id "-chart-select"))

(defn component-height [dom-id]
  (if-let [comp (js/document.getElementById dom-id)]
    (aget (.getBoundingClientRect comp) "height")
    0))

(defn component-width [dom-id]
  (if-let [comp (js/document.getElementById dom-id)]
    (aget (.getBoundingClientRect comp) "width")
    0))

(defn- update-chart-size [frame-id new-height new-width]
  (let [{padding-width :width
         padding-height :height} (fi/call-api :frame-content-padding-raw)
        new-height (- new-height
                      padding-height)
        new-width (- new-width
                     padding-width) ;;frame-width - padding
        chart-instance (get @instances frame-id)
        canvas-node (when (instance-available? frame-id)
                      (.-canvas chart-instance))]
    (when canvas-node
      (.resize chart-instance new-width new-height)
      (.update chart-instance))))

(defn resize-chart
  ([frame-id new-height new-width]
   (update-chart-size frame-id new-height new-width))
  ([frame-id]
   (let [{[width height] :size} @(fi/call-api :frame-sub frame-id)]
     (resize-chart frame-id height width))))

;; Todo R10 handle multiple chart-descs
(defn update-chart
  ([frame-id data x y height width theme]
   (trace (str "Update chart") {:frame-id frame-id :data data :x x :y y :height height})
   (let [chart-instance (get @instances frame-id)
         ^js chart-data (.-data chart-instance)
         data (if (map? data)
                [data]
                data)]
        ;;  x (config/attribute->display x)
        ;;  y (config/attribute->display y)]
     (when chart-instance
       (let [{:keys [labels datasets]}
             (reduce (fn [acc
                          {chart-labels :labels
                           chart-datasets :datasets}]
                       (-> acc
                           (update :labels into chart-labels)
                           (update :datasets into chart-datasets)))
                     {:labels #{}
                      :datasets []}
                     data)
             labels (vec (sort labels))]
         (set! (.-datasets chart-data)
               (clj->js datasets))
         (set! (.-labels chart-data)
               (clj->js labels))
         (doseq [chart-index (range (count data))]
           (let [{:keys [x-axis-time]
                  [{selected-chart :type}] :datasets}
                 (get data chart-index)
                 {y :label
                  y-value :value} (get y chart-index)
                 y-scale-id (str "y" y-value chart-index)]
             (when (and selected-chart
                        (not= (name selected-chart)
                              (name path/pie-id-key)))
               (let [scales (aget chart-instance "options" "scales")
                     [text-color border-color]
                     (theme-colors theme)]
                 (aset scales "x" (clj->js (x-axis x-axis-time labels x theme)))
                 (try
                   (aset scales y-scale-id "title" "text" y)
                   (aset scales y-scale-id "title" "color" text-color)
                   (aset scales y-scale-id "ticks" "color" text-color)
                   (aset scales y-scale-id "grid" "color" border-color)
                   (aset scales y-scale-id "grid" "borderColor" border-color)
                   (catch :default e
                     (warn e "Cannot set y-axis attributes" {:scales        scales
                                                             :should-become y}))))))))
       (resize-chart frame-id height width))))
  ([frame-id]
   (let [data @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
         y-values @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/y-option frame-id])
         x-value (:label @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/x-option frame-id]))
         theme @(fi/call-api :config-theme-sub)
         {[width height] :size} @(fi/call-api :frame-sub frame-id)]
     (when (not-empty data) (update-chart frame-id data x-value y-values height width theme)))))

(defn get-element
  ([dom-id]
   (.getElementById js/document
                    dom-id))
  ([prefix frame-id]
   (get-element (str prefix frame-id))))

(defn get-element-context [prefix frame-id]
  (.getContext (get-element prefix frame-id)
               "2d"))

(defn disable-legend-interaction [chart-data]
  ;; (let [read-only? @(fi/call-api [:interaction-mode :pending-read-only-sub?])]
  (assoc-in chart-data [:options :plugins :legend :display] false))
    ;; (if read-only?
    ;;   (assoc-in chart-data [:options :plugins :legend :onClick] (fn [_ _ _]))
    ;;                                                               ;do nothing

    ;;   chart-data)))

;; Component Functions
(defn should-update? [_ [_ _ old-props] [_ _ new-props]]
  (not= (val-or-deref old-props)
        (val-or-deref new-props)))

(defn component-update [this]
  (let [[_ frame-id props] (reagent/argv this)
        {:keys [data x y height width theme]} (val-or-deref props)]
    (update-chart frame-id data x y height width theme)))

(defn component-unmount [this]
  (let [[_ frame-id _props] (reagent/argv this)]
    (clean-instance frame-id)))

(defn component-did-catch [_this error info]
  (error "Chart component crashed: " error info))

(defn dataset-visible? [frame-id idx]
  (when-let [^js instance (get @instances frame-id)]
    (.isDatasetVisible instance idx)))

(defn show-dataset [frame-id idx flag]
  (when-let [^js instance (get @instances frame-id)]
    (if flag
      (.show instance idx)
      (.hide instance idx))))

(defn toggle-dataset [frame-id idx]
  (when-let [^js instance (get @instances frame-id)]
    (if (.isDatasetVisible instance idx)
      (.hide instance idx)
      (.show instance idx))))

(defn pie-show-dataset [frame-id idx flag]
  (when-let [instance (get @instances frame-id)]
    (when-let [metadata (.getDatasetMeta ^js instance 0)]
      (-> (aget metadata "data" idx)
          (aset "hidden" (not (boolean flag))))
      (.update instance))))