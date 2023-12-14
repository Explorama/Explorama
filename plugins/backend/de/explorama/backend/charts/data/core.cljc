(ns de.explorama.backend.charts.data.core
  (:require [data-format-lib.aggregations :as dfl-agg]
            [data-format-lib.filter]
            [de.explorama.backend.charts.data.base-charts :as base-charts]
            [de.explorama.backend.charts.data.colors :refer [color]]
            [de.explorama.backend.charts.data.wordcloud :as wordcloud]
            [de.explorama.shared.charts.ws-api :as ws-api]
            [de.explorama.backend.charts.data.fetch :refer [di-data]]
            [de.explorama.backend.charts.data.redo :as redo]
            [taoensso.timbre :refer [warn]]))

(defn- empty-case [warn-message]
  (warn warn-message)
  [])

(def multiple-axis-chart-types #{:bar :line :scatter :bubble})
(def non-axis-chart-types #{:pie :wordcloud})

(defn- base-validation [{:keys [type x-axis y-axis sum-by sum-filter aggregation-method]}]
  (cond
    (and (multiple-axis-chart-types type)
         (= x-axis sum-by))
    (throw (ex-info "Same x-axis and sum-by" {:error :same-attributes
                                              :selections {:x-axis-attribute-label x-axis
                                                           :sum-by-label x-axis}}))
    (and (multiple-axis-chart-types type)
         (= x-axis y-axis))
    (throw (ex-info "Same x-axis and y-axis" {:error :same-attributes
                                              :selections {:x-axis-attribute-label x-axis
                                                           :y-axis-attribute-label x-axis}}))
    (and (#{:average :median} aggregation-method)
         (seq sum-filter))
    ;;Calculation is wrong there because its tricky to handle it through the multiple grouping and aggregation
    (throw (ex-info "Not supported aggregation sum-by combination"
                    {:error :invalid-aggregation
                     :selections {:aggregation-method-label (get-in dfl-agg/descs [aggregation-method :label])
                                  :sum-by-label sum-by}}))
    (and (= "month" x-axis)
         (= "year" sum-by))
    ;;Currently tricky to handle within the dataformat lib
    (throw (ex-info "Invalid time selection" {:error :same-time-selection
                                              :selections {:x-axis-attribute-label x-axis
                                                           :sum-by-label sum-by}}))))
;; These are target keys for operations e.g. to prevent overwriting each other when attr is the same
(def y-target-key "y")
(def agg-target-key "a")

(defn- handle-invalid [desc chart-index options invalid-operations]
  (let [apply-new-fn (fn [desc {:keys [op attribute]}]
                       (case op
                         :y-option
                         (assoc desc :y-axis
                                (cond
                                   ;Valid y attribute in the data
                                  (and (map? (:y-options options))
                                       (seq (get-in options [:y-options :groups 1 :options])))
                                  (get-in options [:y-options :groups 1 :options 0 :value])

                             ;No valid y attribute in data, choose something else from general (num-of-events)
                                  (map? (:y-options options))
                                  (get-in options [:y-options :groups 0 :options 0 :value])

                                  :else (get-in options [:y-options 0 :value])))
                         :x-option (assoc desc :x-axis ws-api/default-x-attribute)
                         :r-option (assoc desc :r-attr :number-of-events)
                         :sum-by-option (assoc desc
                                               :sum-by ws-api/default-sum-by
                                               :sum-filter #{})
                         :sum-by-values (let [new-sum-by-values (disj (:sum-filter desc))]
                                          (if (seq new-sum-by-values)
                                            (assoc desc :sum-filter new-sum-by-values)
                                            (assoc desc
                                                   :sum-by ws-api/default-sum-by
                                                   :sum-filter #{})))
                         :attributes (let [new-attributes (filterv #(not= attribute %)
                                                                   (:attributes desc))]
                                       (if (seq new-attributes)
                                         (assoc desc :attributes new-attributes)
                                         (assoc desc :attributes [{:value :characteristics}])))))]

    (reduce (fn [desc {idx :chart-idx :as invalid-op}]
              (cond-> desc
                (or (not idx)
                    (= idx chart-index))
                (apply-new-fn invalid-op)))
            desc
            invalid-operations)))

(defn datasets [data
                options
                invalid-operations
                chart-index
                desc]
  (let [relevant-desc (cond-> desc
                        (seq invalid-operations)
                        (handle-invalid chart-index options invalid-operations))
        {:keys [di type y-axis r-attr
                attributes stopping-attributes stemming-attributes
                min-occurence]
         :or {stemming-attributes #{}
              stopping-attributes #{}}}
        relevant-desc
        desc (cond-> (-> relevant-desc
                         (dissoc :di :local-filter :calc :type :frame-id :vis-type :task-id :operations-state :client-ui-options :volatile-acs)
                         (assoc :y-axis y-axis
                                :chart-index chart-index
                                :y-target-access y-target-key
                                :agg-target-access agg-target-key))
               r-attr (assoc :r-attr r-attr))
        type-key (keyword type)]
    (if data
      [relevant-desc
       (case type-key
         :scatter (base-charts/scatter-datasets data (partial color di chart-index) desc)
         :bubble (base-charts/bubble-datasets data (partial color di chart-index) desc)
         :line (base-charts/line-datasets data (partial color di chart-index) desc)
         :bar (base-charts/bar-datasets data (partial color di chart-index) desc)
         :pie (base-charts/pie-datasets data (partial color di chart-index) desc)
         :wordcloud (wordcloud/wordcloud-dataset data attributes stopping-attributes stemming-attributes min-occurence)
         (empty-case (str "No dataset-definition for chart type " type)))]
      (empty-case (str "No data found for datainstance " di)))))

(defn chart-data [{:keys [frame-id task-id di operations-state]
                   {:keys [datasets? options?]} :calc
                   :as params}]
  (base-validation params)
  (let [{:keys [data options] :as data-infos} (di-data params)
        {:keys [invalid-operations]} (when (and operations-state options)
                                       (redo/charts-check-redo options operations-state))
        resulting-datasets (reduce (fn [acc [chart-index desc]]
                                     (let [[applied-params datasets] (datasets data options invalid-operations chart-index desc)]
                                       (-> acc
                                           (update :datasets conj datasets)
                                           (update :applied-params conj applied-params))))
                                   {:datasets []
                                    :applied-params []}
                                   (map-indexed vector (:charts params)))]
    (cond-> (dissoc data-infos :data)
      (not options?) (dissoc :options)
      datasets? (merge resulting-datasets)
      invalid-operations (assoc :invalid-operations (set (filter #(not (and (contains? % :attribute)
                                                                            (nil? (:attribute %))))
                                                                 invalid-operations)))
      :always (assoc :frame-id frame-id
                     :task-id task-id
                     :di di))))