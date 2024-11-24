(ns de.explorama.backend.charts.util
  (:require [clojure.string :as string]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.shared.data-format.filter]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.time :refer [to-long]]
            [de.explorama.shared.common.unification.misc :refer [cljc-max-int
                                                                 cljc-min-int
                                                                 cljc-parse-int]]))

(def relevant-aggregation-attributes (reduce (fn [acc [_ {:keys [attribute result-type need-attribute?] :as agg-attr-desc}]]
                                               (cond-> acc
                                                 (and (not need-attribute?)
                                                      (= result-type "number"))
                                                 (assoc attribute (assoc agg-attr-desc :value attribute))))
                                             {}
                                             dfl-agg/descs))

(def relevant-aggregation-methods (reduce (fn [acc [_ {:keys [attribute result-type need-attribute?] :as agg-attr-desc}]]
                                            (cond-> acc
                                              (and need-attribute?
                                                   (= result-type "number"))
                                              (assoc attribute (assoc agg-attr-desc :value attribute))))
                                          {}
                                          dfl-agg/descs))

(def relevant-agg-methods-options (mapv #(select-keys % [:label :value])
                                        (vals relevant-aggregation-methods)))

(def relevant-agg-attribute-options (mapv #(select-keys % [:label :value])
                                          (vals relevant-aggregation-attributes)))

(defn safe-split [s p]
  (when s
    (string/split s p)))

(defn attr-value
  ([attribute-key point {:keys [year-month-pair?]}]
   (cond
     (or (nil? attribute-key)
         (nil? point)) nil
     (= attribute-key
        (attrs/access-key "year"))
     (-> (attrs/value point "date")
         (safe-split #"-")
         first)
     (= attribute-key
        (attrs/access-key "month"))
     (let [[year month] (-> (attrs/value point "date")
                            (safe-split #"-"))]
       (if year-month-pair?
         (str year "-" month)
         month))
     (= attribute-key "day") (attrs/value point "date")
     (= attribute-key "datasource") (attrs/value point
                                                 (attrs/access-key attrs/datasource-attr))
     :else (attrs/value point attribute-key)))
  ([attribute-key point]
   (attr-value attribute-key point {})))

(defn month-range [[min-year min-month] [max-year max-month]]
  (when (and min-year min-month max-year max-month)
    (let [min-year (cljc-parse-int min-year)
          min-month (cljc-parse-int min-month)
          max-year (cljc-parse-int max-year)
          max-month (cljc-parse-int max-month)]
      (for [year (range min-year (inc max-year))
            month (range 1 13)
            :when (and (not (and (= year min-year)
                                 (< month min-month)))
                       (not (and (= year max-year)
                                 (< max-month month))))]
        [year month]))))

(defn data-min-max-date [data]
  (reduce (fn [{:keys [min max]} d]
            (let [date-val (attr-value "date" d)
                  min-timestamp (if (string? min)
                                  (to-long min)
                                  min)
                  max-timestamp (if (string? max)
                                  (to-long max)
                                  max)
                  timestamp (to-long date-val)]
              {:min (if (< timestamp min-timestamp)
                      date-val
                      min)
               :max (if (> timestamp max-timestamp)
                      date-val
                      max)}))
          {:min cljc-max-int
           :max cljc-min-int}
          data))
