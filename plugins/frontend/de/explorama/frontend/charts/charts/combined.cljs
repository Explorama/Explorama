(ns de.explorama.frontend.charts.charts.combined
  (:require [cljsjs.chartjs]
            [cljsjs.chartjs-adapter-date-fns]
            [cljsjs.date-fns]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.utils.interop :refer [safe-aget]]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.config :as config]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(defonce chart-id-prefix "vis_combined-")

(defn- generate-labels [use-dataset-label-only? selected-y-options chart]
  (let [datasets (js->clj (safe-aget chart "data" "datasets"))]
    (clj->js
     (vec
      (map-indexed
       (fn [index dataset]
         (let [dataset-type (get dataset "type")
               chart-index (get dataset "chartIndex")
               {y-label :label} (get selected-y-options chart-index)
               dataset-label (get dataset "label")]
           {:datasetIndex index
            :text (cond
                    use-dataset-label-only? dataset-label
                    (= dataset-label "All") y-label
                    :else (str dataset-label " - " y-label))
            :fillStyle (get-in dataset ["legend" "color"])
            :strokeStyle (get dataset "borderColor")
            :pointStyle (case dataset-type
                          "line" "line"
                          "bar" "rect"
                          "scatter" "circle"
                          "bubble" "circle")
            :lineWidth (if (= dataset-type "line")
                         5
                         1)
            :hidden (not (.isDatasetVisible chart index))}))
       datasets)))))

(defn show-chart [frame-id props]
  (let [context (cutils/get-element-context chart-id-prefix frame-id)
        cur-lang (re-frame/subscribe [::i18n/current-language])
        {:keys [y x data height width render-done active-charts theme]} @props
        use-dataset-label-only? (= active-charts 1)
        {:keys [x-axis-time]} (first data)
        {:keys [labels datasets]} (reduce (fn [acc
                                               {chart-labels :labels
                                                chart-datasets :datasets}]
                                            (-> acc
                                                (update :labels into chart-labels)
                                                (update :datasets
                                                        into
                                                        chart-datasets)))
                                          {:labels #{}
                                           :datasets []}
                                          data)
        y-ranges (reduce (fn [acc {:keys [yAxisID y-range]}]
                           (assoc acc yAxisID y-range))
                         {}
                         datasets)
        labels (vec (sort labels))
        [text-color border-color]
        (cutils/theme-colors theme)
        chart-data (cutils/disable-legend-interaction
                    {:data {:datasets datasets}
                     :animation false
                     :options {:responsive true
                               :animation {:duration 0}
                               :maintainAspectRatio false
                               :legend {:position :top
                                        :labels {:fontSize 12
                                                 :boxWidth 10}}
                               :plugins {:legend {:labels {:usePointStyle true
                                                           :generateLabels (partial generate-labels use-dataset-label-only? y)}}
                                         :tooltip {:callbacks
                                                   {:title (cutils/tooltip-title-fn)
                                                    :label (cutils/tooltip-label-fn cur-lang)}}}
                               :scales (reduce (fn [acc [chart-index
                                                         {y :label
                                                          y-value :value}]]
                                                 (let [axis-id (str "y" y-value chart-index)
                                                       {y-min :min
                                                        y-max :max} (get y-ranges axis-id)]
                                                   (assoc acc
                                                          axis-id
                                                          (cond-> {:title {:display true
                                                                           :color text-color
                                                                           :text y}
                                                                   :beginAtZero true
                                                                   :ticks {:callback (cutils/y-axis-label-fn cur-lang)
                                                                           :color text-color}
                                                                   :grid {:color border-color
                                                                          :borderColor border-color}
                                                                   :position (if (= (mod chart-index 2) 0)
                                                                               "left"
                                                                               "right")}
                                                            y-min (assoc :min y-min)
                                                            y-max (assoc :max y-max)))))
                                               {:x (merge (cutils/x-axis x-axis-time labels x theme)
                                                          {:barPercentage 0.9
                                                           :maxBarThickness 8
                                                           :stacked false
                                                           :minBarLength 2
                                                           :gridLines {:offsetGridLines true}})}
                                               (map-indexed vector y))}})]
    (when chart-data
      (cutils/save-instance frame-id (js/Chart. context (clj->js chart-data)))
      (cutils/update-chart frame-id)
      (cutils/resize-chart frame-id height width))
    (when render-done
      (render-done frame-id))))

(defn chart-component [frame-id props]
  (reagent/create-class
   {:display-name chart-id-prefix
    :component-did-mount #(show-chart frame-id props)
    :should-component-update cutils/should-update?
    :component-did-update cutils/component-update
    :component-will-unmount cutils/component-unmount
    :component-did-catch cutils/component-did-catch
    :reagent-render (fn [frame-id _]
                      [:canvas {:id (str chart-id-prefix frame-id)}])}))

(defn render-done [frame-id]
  (re-frame/dispatch
   (fi/call-api :render-done-event-vec frame-id (str config/default-namespace " - line chart"))))

(defn content [frame-id vis-desc]
  (reagent/create-class
   {:display-name frame-id
    :component-did-mount (fn []
                           (let [data @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                                 loading? @(re-frame/subscribe [::queue-util/loading? frame-id])]
                             (when (and vis-desc (not loading?) (empty? data))
                               (re-frame/dispatch [:de.explorama.frontend.charts.vis-state/restore-vis-desc frame-id vis-desc]))
                             (when (empty? data)
                               (render-done frame-id))))
    :reagent-render (fn [frame-id {:keys [size]}]
                      (let [data @(re-frame/subscribe [:de.explorama.frontend.charts.charts.core/datasets frame-id])
                            active-charts @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/num-of-charts frame-id])
                            y-option @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/y-option frame-id])
                            ;y-ranges @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/changed-y-range frame-id])
                            x-option @(re-frame/subscribe [:de.explorama.frontend.charts.charts.settings/x-option frame-id])
                            scale-info @(fi/call-api :scale-info-sub)
                            {[width height] :size} @(fi/call-api :frame-sub frame-id)
                            theme @(fi/call-api :config-theme-sub)
                            props (reagent/atom {:data data
                                                 :height (if size (second size) height)
                                                 :width (if size (first size) width)
                                                 :type path/line-id-key
                                                 :theme theme
                                                 :y y-option
                                                 :x (:label x-option)
                                                 :active-charts active-charts
                                                 :render-done render-done
                                                 :scale-info scale-info})]
                        (when (not-empty data)
                          [:div #_{:style {:width "100%"}}
                           [chart-component frame-id props]])))}))