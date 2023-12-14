(ns de.explorama.backend.algorithms.data.features
  (:require [clojure.math.combinatorics :as combo]
            [clojure.pprint :as pprint]
            [clojure.set :as set]
            [clojure.string :as str]
            [de.explorama.backend.algorithms.config :as config]
            [de.explorama.backend.algorithms.data.aggregations :refer [aggregate]]
            [de.explorama.backend.algorithms.data.algorithm :as algo]
            [de.explorama.backend.algorithms.data.range :as feat-range :refer [complete-date custom-range date-convert date-range day-granularity]]
            [de.explorama.backend.algorithms.registry :as registry]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid
                                                                 double-negativ-infinity double-positiv-infinity]]))

(def date-attribute-fn :date)

(defn reduced-data [attributes data]
  (let [attrs (reduce (fn [acc [_ values]]
                        (set/union acc (set (map :value values))))
                      #{}
                      attributes)
        number-of-events? (attrs :number-of-events)
        attrs (disj attrs :number-of-events)]
    (if number-of-events?
      (map (fn [event]
             (assoc (select-keys event attrs)
                    :number-of-events 1))
           data)
      (map (fn [event]
             (select-keys event attrs))
           data))))

(defn sort-fn [type value]
  (get
   {:date #(let [val (get % value)
                 [year month day] (map complete-date val)]
             (str year
                  (or month "00")
                  (or day "00")))
    :numeric #(get % value)}
   type
   #(get % value)))

(defn aggregate-feature-type [[_ config] event]
  (reduce (fn [acc {:keys [date-config type value]}]
            (case type
              :numeric acc
              :date (update acc value date-convert date-config)))
          event
          config))

(defn find-date-features [leading-features]
  (filter (fn [{:keys [value]}] (= value "date")) leading-features))

(defn find-continues-features [leading-features]
  (filter (fn [{:keys [given?] {continues-method :method} :continues-value}]
            (and given?
                 (or (= continues-method :range)
                     (= :date type))))
          leading-features))

(defn find-leading-feature [leading-features]
  (if (= 1 (count leading-features))
    (first leading-features)
    (first (or
            (find-date-features leading-features)
            (find-continues-features leading-features)))))

(defn fill-blanks [data replacement-aggregate]
  (cond->> data
    replacement-aggregate
    (map #(merge replacement-aggregate %))))

(defn finalize-sort-fn [sort-fns]
  (fn [value]
    (str/join "-"
              (map (fn [sort-fn]
                     (sort-fn value))
                   sort-fns))))

(defn numeric-replacement [value fun replacement-aggregate attr]
  (update value attr (fn [val]
                       (case type
                         :numeric
                         (if-let [replacement (get replacement-aggregate attr)]
                           (fun val replacement)
                           val)
                         val))))

(defn min-max-from-data [data _ type value]
  (let [data (vec (sort-by (sort-fn type value) data))
        max-value (peek data)
        min-value (some #(when (get % value) %) data)]
    [min-value max-value]))

(defn feature-range [min-max-func config leading-replacement-aggregate type value length step-function]
  (let [min-max-values (min-max-func config type value)]
    (if (vector? min-max-values)
      (let [[min-value max-value] min-max-values
            max-value (numeric-replacement max-value max leading-replacement-aggregate value)
            min-value (numeric-replacement min-value min leading-replacement-aggregate value)
            feature-range (custom-range min-value max-value config length step-function)]
        feature-range)
      (:range min-max-values))))

(defn leading-feature-ranges [min-max-func transformed-data leading-feature-configs empty-return-fn leading-replacement-aggregate length step-function]
  (let [leading-range-feature-configs (concat (find-date-features leading-feature-configs)
                                              (find-continues-features leading-feature-configs))]
    (if (empty? leading-range-feature-configs)
      (empty-return-fn transformed-data)
      (let [min-max-func (partial min-max-func transformed-data)]
        (loop [result-features '()
               leading-features leading-range-feature-configs]
          (let [{:keys [value type] :as config} (find-leading-feature leading-features)
                feature-range (feature-range min-max-func config leading-replacement-aggregate type value length step-function)
                remaining-features (remove #{config} leading-features)
                inital-feature
                (map (fn [range-value]
                       {value range-value})
                     feature-range)
                result-features (if (empty? result-features)
                                  inital-feature
                                  (map #(apply merge %)
                                       (combo/cartesian-product result-features inital-feature)))]
            (if (empty? remaining-features)
              result-features
              (recur result-features
                     remaining-features))))))))

(defn compile-sort-function [leading-features]
  (finalize-sort-fn (mapv (fn [{:keys [type value]}]
                            (sort-fn type value))
                          (let [date-feature (first (filter (fn [{type :type}] (= type :date)) leading-features))]
                            (cond-> []
                              date-feature
                              (conj date-feature)
                              :always
                              (into (remove #{date-feature} leading-features)))))))

(defn aggregate-function [op data]
  (cond (= op :sum)
        (reduce + 0 data)
        (= op :min)
        (reduce min double-positiv-infinity data)
        (= op :max)
        (reduce max double-negativ-infinity data)
        (= op :average)
        (double (/ (reduce + 0 data)
                   (count data)))
        (= op :number-of-events)
        (count data)
        :else
        data))

(defn aggregate-function-date [op data]
  (cond (= op :min)
        (first (vec (sort-by (fn [[year month day]]
                               (str year
                                    (complete-date month)
                                    (complete-date day)))
                             data)))
        (= op :max)
        (peek (vec (sort-by (fn [[year month day]]
                              (str year
                                   (complete-date month)
                                   (complete-date day)))
                            data)))
        :else
        data))

(defn feature-names [features-configs]
  (mapv :value features-configs))

(defn missing-attribute-error-case [attributes]
  (not (every? (fn [[_ v]]
                 (seq v))
               attributes)))

(defn same-attribute-error-case [attributes]
  (->> (mapcat val attributes)
       (map :value)
       frequencies
       (every? (fn [[_ num]] (= 1 num)))
       not))

(defn aggregate-by-attribute-error-case [aggregate-by-attribute]
  (empty? aggregate-by-attribute))

(defn multiply-number-of-events [{:keys [multiple-values-per-event]} flat-config]
  (and (= :multiply multiple-values-per-event)
       (some (fn [{:keys [value]}]
               (= :number-of-events value))
             flat-config)))

(defn not-enough-data [data]
  (<= (count data) 1))

(defn merge-with-multiple-values [merge-policy aggregation]
  (and (= aggregation :multiple)
       (= merge-policy :merge-incomplete)))

(defn result-validation [data]
  (let [error-case
        (cond (not-enough-data data)
              :not-enough-data
              :else nil)]
    {:valid? (nil? error-case)
     :reason error-case}))

(defn determine-merge-policy [merge-policy aggregation]
  (if (= config/explorama-enforce-merge-policy-defaults :enforce)
    (case aggregation
      (:sum :min :max :average) :merge-incomplete
      :multiple :ignore-incomplete
      :ignore-incomplete)
    (or merge-policy :ignore-incomplete)))

(defn aggregate-dates [rest-features aggregation]
  (and (#{:sum :average} aggregation)
       (not-empty (find-date-features rest-features))))

(defn features [attributes {:keys [aggregate-by-attribute]}]
  (let [flat-config (mapcat val attributes)
        number-of-events? (some (fn [{type :type}]
                                  (when (= type :number-of-events)
                                    type))
                                flat-config)
        flat-config (if number-of-events?
                      (map (fn [config]
                             (if (get-in config [:shared :multiple-values-per-event])
                               (assoc-in config [:shared :multiple-values-per-event] :keep)
                               config))
                           flat-config)
                      flat-config)
        aggregate-by-attribute-set (set aggregate-by-attribute)
        leading-features (filter (fn [{:keys [value]}]
                                   (aggregate-by-attribute-set value))
                                 flat-config)
        given-features (filter :given? flat-config)
        rest-features (remove (set leading-features) flat-config)
        {{aggregation :aggregation
          merge-policy :merge-policy
          :as shared}
         :shared}
        (first (filter :shared flat-config))
        merge-policy (determine-merge-policy merge-policy aggregation)
        error-case (cond (multiply-number-of-events shared flat-config)
                         :number-of-events-multiply
                         (missing-attribute-error-case attributes)
                         :missing-attribute
                         (same-attribute-error-case attributes)
                         :same-attribute
                         (aggregate-by-attribute-error-case aggregate-by-attribute)
                         :aggregate-by-attribute
                         (merge-with-multiple-values merge-policy aggregation)
                         :merge-with-multiple-values
                         (aggregate-dates rest-features aggregation)
                         :aggregate-dates
                         :else nil)
        valid? {:valid? (nil? error-case)
                :reason error-case}]
    [leading-features (feature-names leading-features)
     given-features (feature-names given-features)
     rest-features (feature-names rest-features)
     flat-config (feature-names flat-config)
     valid?]))

(defn merge-dimensions [leading-features grouped-data]
  (if (= (count leading-features)
         (count grouped-data))
    grouped-data
    (reduce (fn [result leading-feature]
              (if-let [value (get grouped-data leading-feature)]
                (assoc result leading-feature value)
                (assoc result leading-feature :<-replace-me)))
            {}
            (set/union (set leading-features)
                       (-> (dissoc grouped-data {}) keys set)))))

(defn multiple-values-per-event [reduced-data leading-range-feature-configs]
  (if (= :multiply (get-in (first leading-range-feature-configs)
                           [:shared :multiple-values-per-event]
                           :multiply))
    (flatten (map (fn [event]
                    (let [vector-attrs (reduce (fn [acc [attr value]]
                                                 (if (vector? value)
                                                   (conj acc attr)
                                                   acc))
                                               #{}
                                               event)]
                      (if (empty? vector-attrs)
                        event
                        (->> vector-attrs
                             (map #(map (fn [value]
                                          {% value})
                                        (attrs/value event %)))
                             (apply combo/cartesian-product)
                             (map #(apply merge event %))))))
                  reduced-data))
    reduced-data))

(defonce debug-atom (atom []))

(defn exceptions [{valid? :valid? reason :reason}]
  (when-not valid?
    (case reason
      :missing-attribute
      (throw (ex-info "Empty attribute configuration for at lease one attribute type"
                      {:error-key :missing-attribute
                       :error-label :invalid-selection-or-missing-attribute-error}))
      :same-attribute
      (throw (ex-info "Using one variable as independent and dependent is not valid"
                      {:error-key :same-attribute
                       :error-label :same-attribute-for-multiple-attribute-kinds-error}))
      :aggregate-by-attribute
      (throw (ex-info "No aggregation base is defined"
                      {:error-key :aggregate-by-attribute
                       :error-label :no-or-wrong-aggregation-base-defined-error}))
      :merge-with-multiple-values
      (throw (ex-info "Merge and multiple values is not a valid combination"
                      {:error-key :merge-with-multiple-values
                       :error-label :merge-and-multiple-values-error}))
      :number-of-events-multiply
      (throw (ex-info "Multiply and number of events will result not comprehensible results"
                      {:error-key :number-of-events-multiply
                       :error-label :number-of-events-multiply-error}))
      :aggregate-dates
      (throw (ex-info "Date aggregation is not valid"
                      {:error-key :aggregate-dates
                       :error-label :aggregate-dates-error}))
      (throw (ex-info "Unknown error"
                      {:error-key :unknown-error})))))

(defn transform [{:keys [attributes] parameter :parameter
                  :as transform-config}
                 data]
  (let [debug? false
        _ (when debug? (reset! debug-atom [transform-config data]))
        [leading-feature-configs leading-feature-names
         given-features leading-featuren-names
         rest-feature-configs rest-feature-names
         flat-configs flat-config-names
         validation]
        (features attributes parameter)]
    (exceptions validation)
    (let [_ (when debug? (println "=============================="))
          _ (when debug? (println "ATTRIBUTES"))
          _ (when debug? (println "=============================="))
          _ (when debug? (println "transform-config" transform-config))
          _ (when debug? (println "=============================="))
          _ (when debug? (println "TRANSFORM"))
          _ (when debug? (println "=============================="))
          _ (when debug? (println "leading-feature-configs" leading-feature-configs))
          _ (when debug? (println "given-features" given-features))
          _ (when debug? (println "rest-feature-configs" rest-feature-configs))
          _ (when debug? (println "rest-feature-configs" (map :value flat-configs)))
          _ (when debug? (println "=============================="))

          _ (when debug? (println "data" (reduced-data attributes data)))
          _ (when debug? (println "=============================="))
          _ (when debug? (println "data-count" (count data)))
          _ (when debug? (println "=============================="))

         ;Reduce and unification of features
          reduced-data (reduced-data attributes data)

          reduced-data (multiple-values-per-event reduced-data leading-feature-configs)

          transformed-data
          (reduce (fn [_ config]
                    (map (partial aggregate-feature-type config)
                         reduced-data))
                  '()
                  attributes)

          _ (when debug? (println "transformed-data" transformed-data))
          _ (when debug? (println "=============================="))
          _ (when debug? (println "transformed-data count" (count transformed-data)))
          _ (when debug? (println "=============================="))

         ; Define leading dimensions
          leading-features
          (leading-feature-ranges min-max-from-data
                                  transformed-data
                                  leading-feature-configs
                                  (fn [transformed-data]
                                    (let [not-range-keys (mapv :value leading-feature-configs)]
                                      (mapv (fn [event]
                                              (select-keys event not-range-keys))
                                            transformed-data)))
                                  {} #_rest-replacement-aggregate ;! fix this
                                  0
                                  feat-range/training-data-step-function)

          _ (when debug? (println "leading-features"))
          _ (when debug? (pprint/pprint leading-features))
          _ (when debug? (println "=============================="))

          grouped-data
          (group-by (fn [event]
                      (select-keys event leading-feature-names))
                    transformed-data)

          _ (when debug? (println "keys grouped-data"))
          _ (when debug? (pprint/pprint (keys grouped-data)))
          _ (when debug? (println "=============================="))

          merged-data (merge-dimensions leading-features grouped-data)

          _ (when debug? (println "merged-data" merged-data))
          _ (when debug? (println "=============================="))
          _ (when debug? (println "keys merged-data"))
          _ (when debug? (pprint/pprint (keys merged-data)))
          _ (when debug? (println "=============================="))

          rest-feature-names-set (set rest-feature-names)

          aggregated-data (reduce (fn [acc [aggregation-key values]]
                                    (let [{{aggregation :aggregation
                                            merge-policy :merge-policy}
                                           :shared}
                                          (first (filter :shared rest-feature-configs)) ;? are multiple nested aggregations possible? - Currently there is one
                                          merge-policy (determine-merge-policy merge-policy aggregation)]
                                      (cond (= :<-replace-me values)
                                            (assoc acc aggregation-key [aggregation-key])
                                            (or (not aggregation)
                                                (= aggregation :multiple))
                                            (assoc acc aggregation-key values)
                                            :else
                                            (assoc acc
                                                   aggregation-key
                                                   (let [values (map #(select-keys % rest-feature-names) values)
                                                         values (case merge-policy
                                                                  :merge-incomplete
                                                                  (reduce (fn [acc key]
                                                                            (let [value (->> (mapv #(get % key)
                                                                                                   values)
                                                                                             (filter identity))]
                                                                              (if (not-empty value)
                                                                                (assoc acc key (cond (= "date" key)
                                                                                                     (aggregate-function-date aggregation value)
                                                                                                     (= :number-of-events key)
                                                                                                     (aggregate-function :sum value)
                                                                                                     :else
                                                                                                     (aggregate-function aggregation value)))
                                                                                acc)))
                                                                          aggregation-key
                                                                          rest-feature-names)
                                                                  :ignore-incomplete
                                                                  (reduce (fn [acc key]
                                                                            (let [value (->> (mapv #(when (set/subset? rest-feature-names-set (set (keys %)))
                                                                                                      (get % key))
                                                                                                   values)
                                                                                             (filter identity))]
                                                                              (if (not-empty value)
                                                                                (assoc acc key (cond (= "date" key)
                                                                                                     (aggregate-function-date aggregation value)
                                                                                                     (= :number-of-events key)
                                                                                                     (aggregate-function :sum value)
                                                                                                     :else
                                                                                                     (aggregate-function aggregation value)))
                                                                                acc)))
                                                                          aggregation-key
                                                                          rest-feature-names))]
                                                     [values])))))
                                  {}
                                  merged-data)

          aggregated-data (flatten (vals aggregated-data))

          _ (when debug? (println "aggregated-data" aggregated-data))
          _ (when debug? (println "=============================="))

          rest-replacement-aggregate (aggregate rest-feature-configs aggregated-data)

          _ (when debug? (println "rest-replacement-aggregate" rest-replacement-aggregate))
          _ (when debug? (println "=============================="))

          aggregated-data (fill-blanks aggregated-data rest-replacement-aggregate)

          _ (when debug? (println "fill-blanks" aggregated-data))
          _ (when debug? (println "=============================="))

          feature-sort-function (compile-sort-function given-features)

          result
          (sort-by feature-sort-function
                   aggregated-data)
          result
          (filterv (fn [event]
                     (every? #(get event %) flat-config-names))
                   result)
          _ (when debug? (println "result" result))
          validation-result (result-validation result)]

      (when-not (:valid? validation-result)
        (case (:reason validation-result)
          :not-enough-data
          (throw (ex-info "Not enough data"
                          {:error-key :not-enough-data
                           :error-label :not-enough-data-error}))
          (throw (ex-info "Unknown error" {:validation-result validation-result}))))
      result)))

(defn future-data-date [acc mapping length date-config]
  (let [date-mapping
        (into {} (filter (fn [[[key]]]
                           (= "date" key))
                         mapping))
        date-mapping (set/map-invert date-mapping)
        max-value (reduce max 0 (keys date-mapping))
        max-date (second (get date-mapping max-value))
        new-max-date (update max-date 0 #(+ % length))
        max-date (update max-date 0 #(inc %))
        date-range-mapping (date-range max-date new-max-date date-config)
        new-mapping
        (map-indexed (fn [idx new-date]
                       [["date" new-date] (+ max-value idx)])
                     date-range-mapping)]
    {:data (update acc :data #(map-indexed (fn [idx [[key] v]]
                                             (assoc
                                              (or (get % idx) {})
                                              key v))
                                           new-mapping))
     :mapping (update acc :mapping merge (into {} new-mapping))}))

(defn future-min-max-date [mapping length value]
  (let [date-mapping
        (into {} (filter (fn [[[key]]]
                           (= value key))
                         mapping))
        date-mapping (set/map-invert date-mapping)
        max-value (reduce max 0 (keys date-mapping))
        max-date (second (get date-mapping max-value))
        new-max-date (update max-date 0 #(+ % length))]
    [{value max-date}
     {value new-max-date}]))

(defn future-min-max-numeric-new-max [min-value length step]
  (+ min-value (* (+ 1 length) step)))

(defn future-min-max-numeric-range-max [data value step length]
  (let [min-value
        (reduce max double-negativ-infinity (map #(get % value) data))
        min-value (+ step min-value)
        max-value (future-min-max-numeric-new-max min-value length step)]
    [min-value max-value]))

(defn future-min-max-numeric-range-start-value [start-value step length]
  [start-value (future-min-max-numeric-new-max start-value length step)])

(defn future-min-max-numeric [data
                              {{:keys [method step]
                                {start-value-type :type start-value :value}
                                :start-value
                                manual-values
                                :manual-values}
                               :future-values}
                              length value]

  (if (and (= method :manual)
           manual-values)
    {:range
     (mapv (fn [idx]
             (or (get manual-values idx)
                 :?))
           (range length))}
    (let [[min-value max-value]
          (cond (and (= method :range)
                     (= start-value-type :max))
                (future-min-max-numeric-range-max data value step length)
                (and (= method :range)
                     (= start-value-type :start-value))
                [(+ step start-value) (future-min-max-numeric-new-max start-value length step)])]
      [{value min-value}
       {value max-value}])))

(defn future-min-max [mapping length data config type value]
  (case type
    :date (future-min-max-date mapping length value)
    :numeric (future-min-max-numeric data config length value)
    #_#_:categoric (future-min-max-date mapping length)))

(defn missing-value-range-values- [data mapping length config type value]
  (let [[min-value max-value]
        (future-min-max mapping length data config type value)]
    (custom-range min-value max-value config length feat-range/future-data-step-function)))

(defn reverse-mapping  [mapping]
  (->> (filter (fn [[[key]]] (= "date" key)) mapping)
       (map (fn [[[_ value] mapping-value]] [mapping-value value]))
       (into {})))

(defn reverse-mapping-date [mapping data]
  (mapv (fn [event]
          (assoc event "date" (get mapping
                                   (get event "date"))))
        data))

(defn predict-auto-features [auto-feature-configs
                             {date-config :date-config}
                             shared
                             {group-by-country :group-by-country}
                             {data :data
                              mapping :mapping}
                             options
                             country
                             length
                             aggregate-by-attribute]
  (let [data (if ((set aggregate-by-attribute) "date")
               (mapv #(assoc % "_tech-counter" (get % "date")) data)
               (vec (map-indexed (fn [idx event]
                                   (assoc event "_tech-counter" idx))
                                 data)))]
    (mapv (fn [{value :value}]
            (let [desc {:algorithm :linear-regression
                        :attributes {:dependent-variable [{:value value
                                                           :type :numeric
                                                           :missing-value {:method :ignore}
                                                           :shared {:aggregation :multiple
                                                                    :multiple-values-per-event :multiply}}]
                                     :independent-variable [{:value "_tech-counter"
                                                             :type :numeric
                                                             :given? true
                                                             :missing-value {:method :ignore}
                                                             :shared {:aggregation :multiple
                                                                      :multiple-values-per-event :multiply}
                                                             :future-values {:method :range
                                                                             :start-value {:type :max}
                                                                             :step 1}}]}
                        :parameter (cond-> {:aggregate-by-attribute ["_tech-counter"]
                                            :length length
                                            :ignore-backdated? true}
                                     group-by-country
                                     (assoc :group-by-country group-by-country))}

                  data (mapv #(select-keys % ["_tech-counter" value]) data)
                  training-data (algo/transform desc data)
                  training (registry/make-model (:algorithm desc)
                                                (assoc desc
                                                       :task-id (str "auto-feature-" (cljc-uuid))
                                                       :datainstance-id nil
                                                       :training-data training-data
                                                       :options (assoc options
                                                                       "_tech-counter"
                                                                       :integer)
                                                       :country country))
                  {result-data :prediction-data} (registry/execute-model (:algorithm desc)
                                                                         training)
                  result (mapv #(get % value) result-data)]
              [value result]))
          auto-feature-configs)))

(defn create-partial-event [feature idx & [fallback]]
  (into {} (mapv (fn [[value values]]
                   [value (get values idx fallback)])
                 feature)))

(defn future-data-
  ([{:keys [attributes parameter training-data options country]}]
   (future-data- parameter attributes training-data options country))
  ([{:keys [attributes parameter options country]} data]
   (future-data- parameter attributes data options country))
  ([{:keys [length aggregate-by-attribute]
     :or {length 3}
     :as parameter}
    attributes
    {mapping :mapping
     data :data
     :as data-raw}
    options
    country]
   (let [debug? false
         [leading-feature-configs leading-feature-names
          given-features given-features-names
          rest-feature-configs rest-feature-names
          flat-configs flat-config-names]
         (features attributes parameter)

         _ (when debug? (println "=============================="))
         _ (when debug? (println "FUTURE DATA"))
         _ (when debug? (println "=============================="))
         _ (when debug? (println "leading-feature-configs" leading-feature-configs))
         _ (when debug? (println "rest-feature-configs" rest-feature-configs))
         _ (when debug? (println "given-features" given-features))
         _ (when debug? (println "=============================="))
         _ (when debug? (println "mapping" mapping))
         _ (when debug? (println "=============================="))
         manual-features
         (filter (fn [{{method :method} :future-values}]
                   (= method :manual))
                 flat-configs)

         date-feature-configs
         (filter (fn [{attr-type :type}]
                   (= attr-type :date))
                 flat-configs)

         _ (when debug? (println "date-feature-configs" date-feature-configs))

         auto-feature-configs
         (filter (fn [{{method :method} :future-values}]
                   (= method :auto))
                 flat-configs)
         _ (when debug? (println "auto-feature-configs" auto-feature-configs))
         auto-features (predict-auto-features auto-feature-configs
                                              (first date-feature-configs)
                                              (some :shared flat-configs)
                                              parameter
                                              data-raw
                                              options
                                              country
                                              length
                                              aggregate-by-attribute)

         _ (when debug? (println "auto-features" auto-features))
         _ (when debug? (println "=============================="))

         missing-value-range-values-configs
         (remove (set/union (set date-feature-configs)
                            (set auto-feature-configs)
                            (set manual-features))
                 given-features)
         missing-value-range-values (map (fn [{value :value type :type :as config}]
                                           [value (missing-value-range-values- data mapping length config type value)])
                                         missing-value-range-values-configs)
         manual-values (map (fn [{value :value
                                  {values :manual-values} :future-values}]
                              [value values])
                            manual-features)
         _ (when debug? (println "manual-features" manual-features))
         _ (when debug? (println "manual-values" manual-values))
         _ (when debug? (println "=============================="))
         _ (when debug? (println "missing-value-range-values-configs" missing-value-range-values-configs))
         _ (when debug? (println "missing-value-range-values" missing-value-range-values))

         date-feature
         (when (seq date-feature-configs)
           (leading-feature-ranges (partial future-min-max mapping length)
                                   data
                                   date-feature-configs
                                   (fn [_]
                                     [])
                                   {}
                                   length
                                   feat-range/future-data-step-function))

         date-feature (mapv #(get % "date") date-feature)
         _ (when debug? (println "date-feature" date-feature))
         _ (when debug? (println "=============================="))
         provided-features (map (fn [idx]
                                  (merge (if-let [date-val (get date-feature idx)]
                                           {"date" date-val}
                                           {})
                                         {}
                                         (create-partial-event manual-values idx :?)
                                         (create-partial-event missing-value-range-values idx)
                                         (create-partial-event auto-features idx)))
                                (range length))

         _ (when debug? (println "provided-features" provided-features))
         _ (when debug? (println "=============================="))]
     (vec provided-features))))

(defn future-data [{training-data :training-data :as task}]
  (algo/transform task
                  (future-data- task)
                  (algo/offsets task training-data)))

(defn restore-event-config [attributes future-data-input]
  (let [{{granularity :granularity} :date-config} (first (find-date-features (:independent-variable attributes)))
        future-data-input-data (:data future-data-input)
        future-data-input-mapping (:mapping future-data-input)
        future-data-input-reverse-mapping (set/map-invert future-data-input-mapping)]
    {:future-data-input-data future-data-input-data
     :granularity granularity
     :future-data-input-reverse-mapping future-data-input-reverse-mapping}))

(defn restore-event [{:keys [future-data-input-reverse-mapping granularity]} input-event]
  (let [result-event
        (reduce (fn [acc [key value]]
                  (assoc acc
                         key
                         (if (= "date" key)
                           (let [[_ value]
                                 (get future-data-input-reverse-mapping (int value))
                                 [year month day] value
                                 month (if (= granularity :quarter)
                                         (case month
                                           1 3
                                           2 6
                                           3 9
                                           4 12)
                                         month)
                                 [min-day max-day] (if (or (= granularity :quarter)
                                                           (nil? day))
                                                     [1 1]
                                                     [day day])
                                 [year month day]
                                 (peek (vec (day-granularity [year (or month 1) min-day]
                                                             [year (or month 1) max-day])))]
                             (str year
                                  "-"
                                  (complete-date month)
                                  "-"
                                  (complete-date day)))
                           value)))
                {}
                input-event)
        result-event (if (get result-event "date")
                       result-event
                       (assoc result-event "date" config/explorama-default-date))]
    (attrs/ensure-structure result-event)))

(defn adjust-data-for-plot [attributes raw-data prediction-input-data forecast-values?]
  (let [{:keys [future-data-input-data] :as restore-event-config} (restore-event-config attributes raw-data)
        col-name-mapping
        (into {}
              (map (fn [{:keys [value]}]
                     ;TODO r1/algorithms is this still necessary?
                     [value
                      value])
                   (mapcat val attributes)))]
    (map-indexed (fn [idx event]
                   (let [input-event (get future-data-input-data idx)]
                     (if forecast-values?
                       (restore-event restore-event-config
                                      (into (set/rename-keys event col-name-mapping)
                                            input-event))
                       (restore-event restore-event-config (set/rename-keys event col-name-mapping)))))
                 prediction-input-data)))