(ns de.explorama.backend.algorithms.explorama-adapter
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str]
            [de.explorama.shared.data-format.data-instance :as dfl-di]
            [de.explorama.shared.algorithms.config :as config]
            [de.explorama.backend.configuration.middleware.i18n :as i18n]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.data.data-tiles :as explorama-tiles]))

(defn- type-mapping [key value]
  (cond (= key "date")
        [:date :date]
        (= key "location")
        [:location :location]
        (= key "notes")
        [:fulltext :fulltext]
        (integer? value)
        [:numeric :integer]
        (double? value)
        [:numeric :double]
        (float? value)
        [:numeric :double]
        (boolean? value)
        [:categoric :boolean]
        (string? value)
        [:categoric :string]
        (keyword? value)
        [:categoric :keyword]))

(defn- prediction-name [task-id]
  (str "Prediction-" (name task-id)))

(defn enhance-events [predict all-countries]
  (let [dict (i18n/get-translations :en-GB)]
    (mapv (fn [{:keys [country task-id algorithm problem-type] :as predict}]
            (cond-> (update predict :prediction-data #(mapv (fn [event]
                                                              (cond-> (assoc event "country" (name country))
                                                                task-id
                                                                (assoc "datasource" (prediction-name task-id))
                                                                algorithm
                                                                (assoc "algorithm" (get dict algorithm algorithm))
                                                                problem-type
                                                                (assoc "problem-type" (get dict problem-type problem-type))))
                                                            %))
              :always (assoc :algorithm-name (get dict algorithm algorithm))
              (= country :ignore)
              (assoc :countries all-countries)))
          predict)))

(defn make-di [di task]
  (let [task (assoc task :di di)
        id (dfl-di/ctn->sha256-id (assoc task :di di))]
    {:di/filter {"empty" [:and]}
     :di/operations [:filter "empty" id]
     :di/data-tile-ref {id {:di/identifier config/plugin-string
                            :task task}}}))

(defn data-tile-ref [di]
  (-> di
      :di/data-tile-ref
      vals
      first))

(defn di-ref [task]
  {(explorama-tiles/access-key "identifier") "algorithms"
   (explorama-tiles/access-key "task") task})

(defn- group-contiguous-intervals
  "Returns the numbers in the seqable `numbers` as a seq of intervals.
  If `interval` is provided, the intervals from `numbers` are appended
  to it. `numbers` should contain a strictly increasing sequence of integers.
  The intervals returned are maximal in the sense that they cannot be extended
  by adding a number from `numbers` to them. The intervals are returned in
  increasing order."
  ([numbers]
   (when-let [[n & ns] (seq numbers)]
     (group-contiguous-intervals ns [n])))
  ([numbers interval]
   (lazy-seq
    (if-let [[n & ns] (seq numbers)]
      (if (= (dec n) (last interval))
        (group-contiguous-intervals ns [(first interval) n])
        (cons interval (group-contiguous-intervals ns [n])))
      [interval]))))

(defn- calculate-years [data]
  (let [years (->> data
                   (into #{} (map #(-> (get % (attrs/access-key "date"))
                                       (subs 0 4)
                                       edn/read-string)))
                   sort)]
    [(str/join ", " (map (fn [[l u]] (if u (str l \- u) (str l)))
                         (group-contiguous-intervals years)))
     years]))

(defn- calculate-countries [data]
  (let [countries (->> data
                       (reduce (fn [acc {country (attrs/access-key "country")}]
                                 (if (vector? country)
                                   (into acc country)
                                   (conj acc country)))
                               #{})
                       sort)]
    [(str/join ", " countries) countries]))

(defn- event-datasource [event]
  (attrs/value event attrs/datasource-attr))

(defn- calculate-datasource [data]
  (let [datasources (sort (into #{} (map event-datasource) data))]
    [(str/join ", " datasources) datasources]))

(defn calculate-di-desc [data]
  (let [calc-years (calculate-years data)
        calc-countries (calculate-countries data)
        calc-datasources (calculate-datasource data)]
    {:event-count (count data)
     :years calc-years
     :countries calc-countries
     :datasources calc-datasources}))

(defn calculate-options [data]
  (reduce (fn [acc event]
            (reduce (fn [acc [key value]]
                      (let [value (if (vector? value)
                                    (first value)
                                    value)]
                        (assoc acc key (type-mapping key value))))
                    acc
                    event))
          {:number-of-events [:number-of-events :number-of-events]}
          data))

(defn- event-acs [event]
  (->> (map (fn [[attr-key attr-value]]
              (let [[_ attr-data-type] (type-mapping attr-key attr-value)
                    [name type label key]
                    (cond (= attr-key "cluster")
                          [attr-key "integer" "Context" attr-key]
                          (= attr-key "location")
                          [attr-key "string" "Context" attr-key]
                          (= attr-key "notes")
                          [attr-key "fulltext" "Notes" attr-key]
                          (= attr-data-type :integer)
                          [attr-key "integer" "Fact" attr-key]
                          (= attr-data-type :double)
                          [attr-key "decimal" "Fact" attr-key]
                          (= attr-data-type :boolean)
                          [attr-key "boolean" "Fact" attr-key]
                          (= attr-data-type :string)
                          [attr-key "string" "Context" attr-key]
                          (= attr-data-type :keyword)
                          [attr-key "string" "Context" attr-key]
                          :else
                          [attr-key "string" "Context" attr-key])]
                (when (and name type label key)
                  {:name  name
                   :type  type
                   :label label
                   :key   key})))
            event)
       (filterv identity)
       set))

(def ^:private base-acs
  #{{:name  "date"
     :type  "date"
     :label "Date"
     :key   "date"}
    {:name  "year"
     :type  "date"
     :label "Date"
     :key   "year"}
    {:name  "month"
     :type  "date"
     :label "Date"
     :key   "month"}
    {:name  "day"
     :type  "date"
     :label "Date"
     :key   "day"}
    {:name  "datasource"
     :type  "string"
     :label "Datasource"
     :key   "datasource"}})

(defn- color-acs [ac-options task-id ignore-values]
  (->> ac-options
       (remove (comp ignore-values :key))
       (mapv (fn [{:keys [name type]}]
               {:name         name
                :display-name (if (#{"integer" "decimal" "float" "double"} type)
                                (str name " (number)")
                                name)
                :info         (list task-id)
                :type         (if (#{"integer" "decimal" "float" "double"} type)
                                "number"
                                type)}))))

(defn volatile-acs [predictions]
  (let [{task-id :task-id} (first predictions)
        data (mapcat :prediction-data predictions)
        ac-options (into [] (reduce (fn [acc event]
                                      (set/union acc
                                                 (event-acs (dissoc event
                                                                    "date"
                                                                    "datasource"
                                                                    "id"))))
                                    base-acs
                                    data))]
    {:ac       {(prediction-name task-id) ac-options}
     :color-ac (color-acs ac-options task-id #{"notes" "location" "year" "month" "day"})
     :obj-ac   (color-acs ac-options task-id #{"year" "month" "day"})}))
