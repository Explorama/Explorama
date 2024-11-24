(ns de.explorama.shared.data-format.aggregations
  "Namespace containing the definitions of available aggregations for generalized showing and usage of them without touching all vertical every time")

;;TODO r1/aggregations Create other aggregation descs here maybe in the same way as number-of-events

(def sum {:label :sum-label
          :need-attribute? true
          :result-type "number"
          :default-value nil
          :attribute :sum
          :dfl-op [:sum]})

(def min-op {:label :min-label
             :need-attribute? true
             :result-type "number"
             :default-value nil
             :attribute :min
             :dfl-op [:min]})

(def max-op {:label :max-label
             :need-attribute? true
             :result-type "number"
             :default-value nil
             :attribute :max
             :dfl-op [:max]})

(def average {:label :average-label
              :need-attribute? true
              :result-type "number"
              :default-value nil
              :attribute :average
              :dfl-op [:average]})

(def median {:label :median-label
             :need-attribute? true
             :result-type "number"
             :default-value nil
             :attribute :median
             :dfl-op [:median]})

(def number-of-events {:label :number-of-events-label
                       :need-attribute? false
                       :result-type "number"
                       :default-value 0
                       :attribute :number-of-events
                       :dfl-op [:count-events]})

(def sort-by-frequencies {:label :sort-by-frequencies-label
                          :need-attribute? true
                          :result-type "string"
                          :dfl-op-fn (fn [attr parent-op]
                                       [:sort-by-frequencies {}
                                        [:select {:attribute attr}
                                         parent-op]])})
(def most-occurring-value {:label :most-occurring-value-label
                           :need-attribute? true
                           :result-type "string"
                           :dfl-op-fn (fn [attr parent-op]
                                        [:take-first {}
                                         [:sort-by-frequencies {}
                                          [:select {:attribute attr}
                                           parent-op]]])})

(def least-occurring-value {:label :least-occurring-value-label
                            :need-attribute? true
                            :result-type "string"
                            :dfl-op-fn (fn [attr parent-op]
                                         [:take-last {}
                                          [:sort-by-frequencies {}
                                           [:select {:attribute attr}
                                            parent-op]]])})

;; all available aggregations
;; <attribute> <desc>
(def descs
  {:number-of-events number-of-events
   :sum sum
   :min min-op
   :max max-op
   :average average
   :median median
   ;not used right now
   :sort-by-frequencies sort-by-frequencies
   :most-occurring-value most-occurring-value
   :least-occurring-value least-occurring-value})
