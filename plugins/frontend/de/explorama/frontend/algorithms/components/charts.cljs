(ns de.explorama.frontend.algorithms.components.charts
  (:require ["chart.js/auto"]
            ["chartjs-adapter-date-fns"]
            ["date-fns"]
            [de.explorama.frontend.ui-base.utils.interop :refer [format safe-aget]]
            [goog.string :as gstring]
            [goog.string.format]
            [de.explorama.frontend.algorithms.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [reagent.core :as reagent]))

(defn tooltip-label-fn [language-function x-y?]
  (fn [context]
    (let [dataset-label (safe-aget context "dataset" "label")
          x-val (safe-aget context "raw" "x")
          y-label (safe-aget context "raw" "y")
          freq-val (safe-aget context "raw" "freq")
          label (if x-y?
                  (format "(%s, %s)"
                          x-val
                          (.toLocaleString y-label (language-function)))
                  (str dataset-label ": " (.toLocaleString y-label (language-function))))]
      (if freq-val
        (str label ", Events " (.toLocaleString freq-val (language-function)))
        label))))

(defn tooltip-title-fn []
  (fn [context]
    (safe-aget context 0 "raw" "x")))

(defn y-axis-label-fn [language-function]
  (fn [value _ _]
    (.toLocaleString value (language-function))))

(defn axes-number [label language-function]
  {:beginAtZero false
   :ticks {:callback (y-axis-label-fn language-function)}
   :title
   {:display true
    :text label}})

(defn axes-description [label labels x-axis-time attr-type]
  {:title
   {:display true
    :text label}
   :type (case attr-type
           :date (if x-axis-time
                   "time"
                   "category")
           :categoric "category"
           :numeric "linear")
   :ticks {:autoSkip  (when x-axis-time false)}
   :time x-axis-time
   :labels (when-not x-axis-time labels)})

(defn- rgb-background-color [[r g b] opacity]
  (str "rgb(" r "," g "," b "," opacity ")"))

(defonce ^:private max-opacity 1.0)

(defn- opacity-value [min-val max-val original-val]
  (let [chart-min-oppacity 0.5]
    (if (= min-val max-val)
      max-opacity
      (+ (* (- max-opacity
               chart-min-oppacity)
            (/ (- original-val min-val)
               (- max-val min-val)))
         chart-min-oppacity))))

(defn- scatter-dataset [rgb-color datapoints]
  (let [data-points (as-> datapoints d
                      (frequencies d)
                      (map (fn [[point freq]]
                             (assoc point
                                    :freq freq))
                           d)
                      (sort-by :x d)
                      (vec d))
        freq-set (set (map :freq data-points))
        min-freq (apply min freq-set)
        max-freq (apply max freq-set)
        opacity-value (partial opacity-value min-freq max-freq)
        bg-colors (mapv (fn [{:keys [freq]}]
                          (rgb-background-color rgb-color
                                                (opacity-value freq)))
                        data-points)]
    {:backgroundColor bg-colors
     :data data-points}))


(defn line-chart-config [datasets yAxes xAxes language-function input-datasets backdated-datasets]
  (let [scatter-color [31 40 74]
        scatter-input-datasets
        (scatter-dataset scatter-color (:data (first input-datasets)))
        too-much? (if (> (count (get scatter-input-datasets :data)) 1000)
                    true
                    false)]
    {:type "line"
     :data {:labels []
            :datasets
            [{:label (get-in input-datasets [0 :label])
              :data (get scatter-input-datasets :data)
              :type "scatter"
              :hidden too-much?
              :fill false
              :borderColor (get scatter-input-datasets :backgroundColor)
              :backgroundColor (get scatter-input-datasets :backgroundColor)
              :legend {:color scatter-color, :shape :circle}}
             (let [color (get-in datasets [0 :line-color])]
               {:label (get-in datasets [0 :label])
                :data  (get-in datasets [0 :data])
                :borderColor (get-in datasets [0 :borderColor])
                :backgroundColor (apply gstring/format "rgb(%d,%d,%d)" color)
                :legend {:color color, :shape :circle-line}})
             (let [color [105 128 221]]
               {:label (get-in backdated-datasets [0 :label])
                :data  (-> backdated-datasets first :data distinct)
                :borderColor "rgb(105, 128, 221)"
                :backgroundColor (apply gstring/format "rgb(%d,%d,%d)" color)
                :legend {:color color, :shape :circle-line}})]}
     :options {:responsive true
               :maintainAspectRatio false
               :legend {:position :top
                        :labels {:fontSize 12
                                 :boxWidth 10}}
               :showLines true
               :fill true
               :elements {:line {:tension 0
                                 :fill false}}
               :plugins {:tooltip {:callbacks {:label (tooltip-label-fn language-function false)
                                               :title (tooltip-title-fn)}}
                         :legend {:labels {:boxHeight 1}}}
               :scales {:y yAxes
                        :x xAxes}}}))

(defn pie-chart-data-set [prediction-data attr]
  (->>
   (map #(get % attr) prediction-data)
   frequencies
   vec))

(def color-letters "0123456789ABCDEF")
(defn random-color [& _]
  (apply str
         "#"
         (mapv (fn [s]
                 (rand-nth color-letters))
               (range 0 6))))

(defn- pie-chart-data-config [data labels]
  (let [colors (mapv random-color data)]
    [{:data (mapv val data)
      :backgroundColor colors
      :legend (mapv (fn [l c] {:label l, :color c}) labels colors)}]))

(defn pie-chart-config [datasets attr]
  (let [datasets (pie-chart-data-set datasets attr)
        labels (mapv (fn [[cluster]] (str "cluster " cluster)) datasets)]
    {:type "doughnut"
     :data {:datasets (pie-chart-data-config datasets labels)
            :labels labels}
     :options {:responsive true
               :maintainAspectRatio false
               :legend {:display false}
               :cutoutPercentage 50}}))

(defn datapoint->line-point [x y datapoint]
  {:x (get datapoint x)
   :y (get datapoint y)})

(defn labels [prediction-data x]
  (->> prediction-data
       (map #(get % x))
       set
       sort
       vec))

(defn line-chart-data-set [x y label-text prediction-data]
  [{:label label-text
    :borderColor "#ff6347"
    :line-color [255 63 47]
    :data (vec (sort-by :x
                        (mapv #(datapoint->line-point x y %)
                              prediction-data)))}])

(defonce chart-instances (atom {}))

(defn instance-available? [frame-id]
  (boolean (get @chart-instances frame-id)))

(defn- update-chart-size [chart-id new-height new-width]
  (let [{padding-width :width
         padding-height :height} (fi/call-api :frame-content-padding-raw)
        new-height (- new-height
                      padding-height)
        new-width (- new-width
                     padding-width) ;;frame-width - padding
        chart-instance (get @chart-instances chart-id)
        canvas-node (when (instance-available? chart-id)
                      (.-canvas chart-instance))
        parent-node (when canvas-node
                      (.-parentNode canvas-node))
        parent-node-style (when parent-node
                            (.-style parent-node))]
    (when parent-node-style
      (set! (.-height parent-node-style)
            (str new-height "px"))
      (.resize chart-instance new-width new-height)
      (.update chart-instance))))

(defn- show-line-chart [frame-id chart-id chart-data size]
  (let [context (.getContext (.getElementById js/document
                                              (str "line-chart-" chart-id))
                             "2d")
        chart-instance (js/Chart. context (clj->js chart-data))
        {[width] :size} @(fi/call-api :frame-sub frame-id)]
    (swap! chart-instances assoc chart-id chart-instance)
    (update-chart-size chart-id (if (coll? size) (second size) config/default-chart-height) width)))

(defn view [frame-id chart-id chart-data size]
  (reagent/create-class
   {:component-did-mount #(show-line-chart frame-id chart-id chart-data size)
    :component-will-unmount (fn [this]
                              (let [[_ chart-id chart-data] (reagent/argv this)
                                    instance (get @chart-instances chart-id)]
                                (when instance
                                  (swap! chart-instances dissoc chart-id)
                                  (.destroy instance))))
    :reagent-render (fn [frame-id chart-id chart-data size]
                      [:div {:style {:height (if (coll? size)
                                               (second size)
                                               (str config/default-chart-height "px"))}}
                       [:canvas.canvas__container {:id (str "line-chart-" chart-id)}]])}))

(defn config->legend [config]
  (into []
        (mapcat (fn [ds]
                  (let [defaults (select-keys ds [:label])
                        legend (:legend ds)]
                    (if (map? legend)
                      [(merge defaults legend)]
                      (map (partial merge defaults) legend)))))
        (-> config :data :datasets)))
