(ns de.explorama.backend.charts.data.base-charts
  (:require [de.explorama.shared.data-format.filter]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :refer [debug warn]]
            [de.explorama.backend.charts.data.colors :refer [opacity-value
                                                             rgb->add-opacity]]
            [de.explorama.backend.charts.data.helper :as helper]
            [de.explorama.shared.common.unification.misc :refer [cljc-parse-int]]))

(defn- chart-index-type->style [chart-type chart-index]
  (when (and chart-index (> chart-index 0))
    (case chart-type
      :scatter {:pointStyle "rectRot"}
      :bubble {:pointStyle "rectRot"}
      :line {:pointStyle "rectRot"}
      :bar {:borderWidth 2
            :borderRadius 25}
      (do
        (warn (str "No style for chart type " type))
        {}))))

(defn- month-label [datapoint result-access]
  (cond->> (get datapoint result-access)
    (and (= result-access :month)
         (get datapoint :year))
    (str (get datapoint :year) "-")))

(defn- data->x-labels [grouped-data x-key]
  (->> grouped-data
       (vals)
       (flatten)
       (map #(month-label % x-key))
       (filter identity)
       (set)
       (sort)
       (into [])))

(defn- basic-datasets [data {:keys [x-axis y-axis dataset-fn datasets-fn org-y-range-fn] :as desc}]
  (let [{:keys [x-access sum-by-key sum-by-access]
         :as desc}
        (helper/gen-grouping-desc desc)
        grouped-data (helper/chart-grouping data desc)
        x-axis-time (helper/x-axis-time x-axis)
        labels (cond x-access
                     (data->x-labels grouped-data x-access)
                     (helper/sum-all? sum-by-key)
                     ["All"]
                     :else
                     (data->x-labels grouped-data sum-by-access))
        datasets-fn (when (fn? datasets-fn)
                      (partial datasets-fn desc))
        dataset-fn (when (fn? dataset-fn)
                     (partial dataset-fn desc))]
    (cond
      (empty? labels)
      {:labels [] :x-axis-time nil :datasets []}

      (helper/sum-all? sum-by-key)
      {:y-axis-attr y-axis
       :labels labels
       :x-axis-time x-axis-time
       :datasets (if (fn? datasets-fn)
                   (datasets-fn grouped-data)
                   [(dataset-fn (first grouped-data))])
       :org-y-range (when (fn? org-y-range-fn)
                      (org-y-range-fn grouped-data))}

      :else
      {:y-axis-attr y-axis
       :labels labels
       :x-axis-time x-axis-time
       :org-y-range (when (fn? org-y-range-fn)
                      (org-y-range-fn grouped-data))
       :datasets (cond->> grouped-data
                              ;;  (group-by #(get % sum-by-access)))
                  ;;  :always (helper/sum-by-grouping)
                  ;;  :always (helper/sort-datasets (or x-access sum-by-access))
                   (fn? dataset-fn) (mapv dataset-fn)
                   (fn? datasets-fn) (datasets-fn)
                   :always (into []))})))

(defn- grouped-data->y-range [y-target-access grouped-data]
  (let [unique-y-values (->> grouped-data
                             (mapcat second)
                             (map #(get % y-target-access))
                             (filter identity)
                             set)
        min-y (when (seq unique-y-values) (apply min unique-y-values))
        max-y (when (seq unique-y-values) (apply max unique-y-values))]
    (when (seq unique-y-values)
      {:org-min-y (min min-y 0)
       :org-max-y max-y})))

(defn scatter-datasets [data color-provider {:keys [y-target-access agg-target-access
                                                    y-axis type chart-index changed-y-range]
                                             :as desc}]
  (debug "Generate datasets for scatter-plot" desc)
  (let [transparency-attr :number-of-events
        datapoint-func (fn [result-access {transp-val agg-target-access y-val y-target-access :as d}]
                         {:x (month-label d result-access)
                          :y y-val
                          :freq transp-val})]
    (basic-datasets
     data
     (-> desc
         (assoc :additional-aggregation-attr transparency-attr)
         (assoc :org-y-range-fn (partial grouped-data->y-range y-target-access))
         (assoc :dataset-fn (fn [{:keys [sum-by-access x-access]} [{group-key sum-by-access} datapoints]]
                              (let [datapoints (filterv #(get % y-target-access)
                                                        datapoints)
                                    rgb-color (color-provider sum-by-access group-key)
                                    freq-set (into #{}
                                                   (comp
                                                    (map #(get % agg-target-access))
                                                    (filter identity))
                                                   datapoints)

                                    min-freq (apply min freq-set)
                                    max-freq (apply max freq-set)
                                    opacity-value (partial opacity-value min-freq max-freq)
                                    bg-colors (mapv (fn [{transp-val agg-target-access}]
                                                      (when transp-val
                                                        (rgb->add-opacity rgb-color
                                                                          (opacity-value transp-val))))
                                                    datapoints)]
                                (merge
                                 (chart-index-type->style type chart-index)
                                 {:type "scatter"
                                  :label group-key
                                  :chartIndex chart-index
                                  :yAxisID (str "y" y-axis chart-index)
                                  :y-range changed-y-range
                                  :backgroundColor bg-colors
                                  :data (sort-by :x (mapv (partial datapoint-func x-access)
                                                          datapoints))
                                  :legend {:shape :circle, :color rgb-color}}))))))))

(def ^:number min-radius 3) ;px
(def ^:number max-radius 30) ;px

(defn- calc-radius [data data-min data-max attr]
  (let [;; simple linear function
        m (when (not= data-min data-max)
            (/
             (- max-radius min-radius)
             (- data-max data-min)))
        b (when m
            (- min-radius (* m data-min)))
        radius-fn (fn [val]
                    (cond
                      (and val m)
                      (float (+ (* m val) b)) ; f(x) = mx + b
                      (and val (not m))
                      min-radius
                      :else 0))]
    (map (fn [d]
           (-> d
               (assoc :r-label (get d attr 0))
               (update attr radius-fn)))
         data)))

(defn bubble-datasets [data color-provider {:keys [y-target-access agg-target-access
                                                   y-axis type chart-index changed-y-range r-attr]
                                            :as desc}]
  (debug "Generate datasets for bubble-chart" desc)
  (let [datapoint-func (fn [result-access {r-label :r-label r-val agg-target-access y-val y-target-access :as d}]
                         {:x (month-label d result-access)
                          :y y-val
                          :r r-val
                          :r-label r-label})]
    (basic-datasets
     data
     (-> desc
         (assoc :additional-aggregation-attr r-attr)
         (assoc :org-y-range-fn (partial grouped-data->y-range y-target-access))
         (assoc :datasets-fn
                (fn [{:keys [sum-by-access x-access]} grouped-data]
                  (let [r-values (->> grouped-data
                                      (map #(map (fn [d] (get d agg-target-access))
                                                 (second %)))
                                      (flatten)
                                      (filter identity)
                                      (set))

                        global-max (if (empty? r-values) 0 (apply max r-values))
                        global-min (if (empty? r-values) 0 (apply min r-values))]
                    (mapv (fn [[{group-key sum-by-access} datapoints]]
                            (let [color (color-provider sum-by-access group-key)
                                  datapoints (-> datapoints
                                                 (calc-radius global-min global-max agg-target-access)
                                                 (vec))]
                              (merge
                               (chart-index-type->style type chart-index)
                               {:type "bubble"
                                :label group-key
                                :backgroundColor color
                                :chartIndex chart-index
                                :yAxisID (str "y" y-axis chart-index)
                                :y-range changed-y-range
                                :options {:hoverRadius 0
                                          :hoverBorderWidth 10}
                                :data (vec (sort-by :r (mapv (partial datapoint-func x-access)
                                                             datapoints)))
                                :legend {:shape :circle, :color color}})))
                          grouped-data))))))))

(def base-line-settings {;:barPercentage 0.95
                        ;; :barThickness "flex"
                            ;; :barThickness 6
                            ;; :maxBarThickness 8
                         :hoverBackgroundColor "rgba(0,0,0,0.2)"
                        ;;  :minBarLength 4
                         :spanGaps true})

(defn line-datasets [data color-provider {:keys [y-target-access y-axis type chart-index changed-y-range] :as desc}]
  (debug "Generate datasets for line-chart" desc)
  (let [datapoint-func (fn [result-access {y-val y-target-access :as d}]
                         {:x (month-label d result-access)
                          :y y-val})]
    (basic-datasets
     data
     (-> desc
         (assoc :org-y-range-fn (partial grouped-data->y-range y-target-access))
         (assoc :dataset-fn (fn [{:keys [sum-by-access x-access]} [{group-key sum-by-access} datapoints]]
                              (let [color (color-provider sum-by-access group-key)]
                                (merge
                                 base-line-settings
                                 (chart-index-type->style type chart-index)
                                 {:type "line"
                                  :label group-key
                                  :borderColor color
                                  :data (sort-by :x (mapv (partial datapoint-func x-access)
                                                          datapoints))
                                  :legend {:shape :line, :color color}
                                  :chartIndex chart-index
                                  :yAxisID (str "y" y-axis chart-index)
                                  :y-range changed-y-range
                                  :options {:showLines true
                                            :fill true
                                            :elements {:line {:tension 0
                                                              :fill false}}}}))))))))

(def base-bar-settings {:barPercentage 0.95
                        ;; :barThickness "flex"
                            ;; :barThickness 6
                            ;; :maxBarThickness 8
                        ;; :hoverBackgroundColor "rgba(0,0,0,0.2)"
                        :minBarLength 4
                        :borderWidth 0})

;; base-bar-settings

(defn bar-datasets [data color-provider {:keys [y-target-access y-axis type chart-index x-axis changed-y-range] :as desc}]
  (debug "Generate datasets for bar-chart" desc)
  (let [x-time? (helper/time-attr? (attrs/access-key x-axis))
        datapoint-func (fn [result-access {y-val y-target-access :as d}]
                         (if x-time?
                           {:x (month-label d result-access)
                            :y y-val}
                           y-val))]
    (basic-datasets
     data
     (-> desc
         (assoc :org-y-range-fn (partial grouped-data->y-range y-target-access))
         (assoc :dataset-fn (fn [{:keys [sum-by-access x-access]} [{group-key sum-by-access} datapoints]]
                              (when (seq datapoints)
                                (let [color (color-provider sum-by-access group-key)]
                                  (merge
                                   base-bar-settings
                                   (chart-index-type->style type chart-index)
                                   {:type "bar"
                                    :label group-key
                                    :backgroundColor (vec (repeat (count datapoints)
                                                                  color))
                                    :data (vec (sort-by :x (mapv (partial datapoint-func x-access)
                                                                 datapoints)))
                                    :chartIndex chart-index
                                    :yAxisID (str "y" y-axis chart-index)
                                    :y-range changed-y-range
                                    :legend {:color color}})))))))))

(defn pie-datasets [data color-provider {:keys [y-target-access y-axis x-axis sum-remaining?] :as desc}]
  (debug "Generate datasets for pie-chart"
         desc)
  (basic-datasets
   data
   (-> desc
       (assoc
        :calc-sum-by-others? sum-remaining?
        :datasets-fn (fn [{:keys [sum-by-access sum-by]} grouped-data]
                       (let [grouped-data (->> grouped-data
                                               (map second)
                                               (flatten))
                             sum-all? (helper/sum-all? sum-by-access)
                             colors (mapv (fn [group]
                                            (color-provider sum-by-access (if sum-all?
                                                                            {sum-by-access "All"}
                                                                            group)))
                                          grouped-data)]
                         [{:type "pie"
                           :label y-axis
                           :backgroundColor colors
                           :data (mapv (fn [{y-val y-target-access}]
                                         (cond-> y-val
                                           false (cljc-parse-int)))
                                       grouped-data)
                           :legend (mapv (fn [{label sum-by-access} color]
                                           {:label (if sum-all?
                                                     "All"
                                                     label)
                                            :color color})
                                         grouped-data colors)}]))))))