(ns de.explorama.backend.algorithms.data.algorithm
  (:require [de.explorama.backend.algorithms.data.range :refer [date-range]]))

(defn filter-relevant-attributes [attributes]
  (->> (filter (fn [{attr-type :type
                     encoding :encoding}]
                 (or (= attr-type :date)
                     encoding))
               attributes)
       (map (fn [{value :value :as config}]
              [value config]))
       (into {})))

(defn encoding-key [{attr-type :type
                     encoding :encoding}]
  (cond encoding
        encoding
        (= attr-type :date)
        :ordinal-date
        :else nil))

(defn ordinal-mapping [{:keys [mapping] :as acc} event attr-key offsets]
  (let [event-val (get event attr-key)
        mapping-val (get mapping [attr-key event-val]
                         (+ (if-let [offset-val (get offsets attr-key)]
                              (inc offset-val)
                              0)
                            (count mapping)))]
    (-> acc
        (assoc-in [:event attr-key] mapping-val)
        (assoc-in [:mapping [attr-key event-val]] mapping-val))))

(defn ordinal-mapping-date [{:keys [mapping state] :as acc} event attr-key offsets attribute-config]
  (let [event-val (get event attr-key)
        prev-date (get-in state [attr-key :prev-value])
        increase (get-in state [attr-key :increase] 1)
        offset (if-let [offset-val (get offsets attr-key)]
                 (inc offset-val)
                 0)
        [next-index increase]
        (if prev-date
          [(+ offset
              (dec (count mapping))
              increase
              (max 0 (- (count (date-range prev-date event-val attribute-config)) 2)))
           (+ (max 0 (- (count (date-range prev-date event-val attribute-config)) 2))
              increase)]
          [offset 1])
        mapping-val (get mapping
                         [attr-key event-val]
                         next-index)]
    (-> acc
        (assoc-in [:event attr-key] mapping-val)
        (assoc-in [:state attr-key :prev-value] event-val)
        (assoc-in [:state attr-key :increase] increase)
        (assoc-in [:mapping [attr-key event-val]] mapping-val))))

(defn encoding-wrapper [data relevant-attributes offsets]
  (dissoc
   (reduce (fn [{mapping :mapping state :state :as acc} event]
             (let [{:keys [event mapping state]}
                   (reduce (fn [acc [_ {attr-key :value :as attribute-config}]]
                             (let [encoding (encoding-key attribute-config)]
                               (case encoding
                                 :ordinal
                                 (ordinal-mapping acc event attr-key offsets)
                                 :ordinal-date
                                 (ordinal-mapping-date acc event attr-key offsets attribute-config)
                                 acc)))
                           {:event event
                            :state state
                            :mapping mapping}
                           relevant-attributes)]
               (-> (update acc :data conj event)
                   (assoc :mapping mapping)
                   (assoc :state state))))
           {:data []
            :state {}
            :mapping {}}
           data)
   :state))

(defn ordinal-offset [value in]
  (max (or in 0) value))

(defn offsets [{:keys [attributes]} {mapping :mapping}]
  (let [attributes-map (filter-relevant-attributes (flatten (map val attributes)))]
    (reduce (fn [acc [[key] value]]
              (let [update-function
                    (case (encoding-key (get attributes-map key))
                      :ordinal (partial ordinal-offset value)
                      :ordinal-date (partial ordinal-offset value))]
                (update acc key update-function)))
            {}
            mapping)))

(defn transform [{:keys [attributes]} data & [offsets]]
  (let [relevant-attributes (filter-relevant-attributes (flatten (map val attributes)))]
    (encoding-wrapper data relevant-attributes offsets)))
