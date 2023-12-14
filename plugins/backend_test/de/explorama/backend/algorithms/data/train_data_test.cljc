(ns de.explorama.backend.algorithms.data.train-data-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.algorithms.config :as config-algortihms]
            [de.explorama.backend.algorithms.data.algorithm :as algo]
            [de.explorama.backend.algorithms.data.features :as feat]
            [de.explorama.backend.algorithms.data.range :as ranges]
            [de.explorama.backend.algorithms.data.test-common :refer [data
                                                                      data-2
                                                                      data-3]]
            [de.explorama.shared.common.test-data :as td]))

(def multiple-values-per-event-test-data
  [{"date" "1999-10-01", td/fact-1 [3 4 5]}
   {"date" "1999-11-01", td/fact-1 [1 6]}
   {"date" "1999-12-01", td/fact-1 3}])

(def multiple-values-per-event-test-result
  [{"date" "1999-10-01", td/fact-1 3}
   {"date" "1999-10-01", td/fact-1 4}
   {"date" "1999-10-01", td/fact-1 5}
   {"date" "1999-11-01", td/fact-1 1}
   {"date" "1999-11-01", td/fact-1 6}
   {"date" "1999-12-01", td/fact-1 3}])

(deftest multiple-values-per-event-test
  (testing "test multiply of events"
    (is (= multiple-values-per-event-test-result
           (feat/multiple-values-per-event multiple-values-per-event-test-data {})))))

(def attributes-structure-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0.6}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :month}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-result-1
  [{"date" [1999 10], td/fact-1 3}
   {"date" [1999 11], td/fact-1 1}
   {"date" [1999 12], td/fact-1 3}
   {"date" [2000 1], td/fact-1 5}
   {"date" [2000 2], td/fact-1 0.6}
   {"date" [2000 3], td/fact-1 0.6}
   {"date" [2000 4], td/fact-1 0.6}
   {"date" [2000 5], td/fact-1 8}
   {"date" [2000 6], td/fact-1 0.6}
   {"date" [2000 7], td/fact-1 0.6}
   {"date" [2000 8], td/fact-1 0.6}
   {"date" [2000 9], td/fact-1 0.6}
   {"date" [2000 10], td/fact-1 0.6}
   {"date" [2000 11], td/fact-1 0.6}
   {"date" [2000 12], td/fact-1 0.6}
   {"date" [2001 1], td/fact-1 0.6}
   {"date" [2001 2], td/fact-1 6}
   {"date" [2001 3], td/fact-1 0.6}
   {"date" [2001 4], td/fact-1 0.6}
   {"date" [2001 5], td/fact-1 0.6}
   {"date" [2001 6], td/fact-1 0.6}
   {"date" [2001 7], td/fact-1 0.6}
   {"date" [2001 8], td/fact-1 0.6}
   {"date" [2001 9], td/fact-1 0.6}
   {"date" [2001 10], td/fact-1 0.6}
   {"date" [2001 11], td/fact-1 0.6}
   {"date" [2001 12], td/fact-1 0.6}
   {"date" [2002 1], td/fact-1 2}
   {"date" [2002 2], td/fact-1 13}])

(def attributes-structure-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :ignore
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-2-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0.6}
                                      :shared {:aggregation :average
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-replace-avg-month
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :average}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :month}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-replace-avg-year
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :average}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-2-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :min
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-2-3
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :max
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-result-2
  [{"date" [1999], td/fact-1 7}
   {"date" [2000], td/fact-1 13}
   {"date" [2001], td/fact-1 6}
   {"date" [2002], td/fact-1 15}])

(def attributes-structure-result-2-1
  [{"date" [1999], td/fact-1 (double (/ 7 4))}
   {"date" [2000], td/fact-1 (double (/ 13 2))}
   {"date" [2001], td/fact-1 6.0}
   {"date" [2002], td/fact-1 (double (/ 15 4))}])

(def attributes-result-replace-avg-month
  (let [avg-replacement (double (/ (+ 3 1 3 8 5 6 2 13)
                                   8))]
    [{"date" [1999 10], td/fact-1 3}
     {"date" [1999 11], td/fact-1 1}
     {"date" [1999 12], td/fact-1 3}
     {"date" [2000 1], td/fact-1 5}
     {"date" [2000 2], td/fact-1 avg-replacement}
     {"date" [2000 3], td/fact-1 avg-replacement}
     {"date" [2000 4], td/fact-1 avg-replacement}
     {"date" [2000 5], td/fact-1 8}
     {"date" [2000 6], td/fact-1 avg-replacement}
     {"date" [2000 7], td/fact-1 avg-replacement}
     {"date" [2000 8], td/fact-1 avg-replacement}
     {"date" [2000 9], td/fact-1 avg-replacement}
     {"date" [2000 10], td/fact-1 avg-replacement}
     {"date" [2000 11], td/fact-1 avg-replacement}
     {"date" [2000 12], td/fact-1 avg-replacement}
     {"date" [2001 1], td/fact-1 avg-replacement}
     {"date" [2001 2], td/fact-1 6}
     {"date" [2001 3], td/fact-1 avg-replacement}
     {"date" [2001 4], td/fact-1 avg-replacement}
     {"date" [2001 5], td/fact-1 avg-replacement}
     {"date" [2001 6], td/fact-1 avg-replacement}
     {"date" [2001 7], td/fact-1 avg-replacement}
     {"date" [2001 8], td/fact-1 avg-replacement}
     {"date" [2001 9], td/fact-1 avg-replacement}
     {"date" [2001 10], td/fact-1 avg-replacement}
     {"date" [2001 11], td/fact-1 avg-replacement}
     {"date" [2001 12], td/fact-1 avg-replacement}
     {"date" [2002 1], td/fact-1 2}
     {"date" [2002 2], td/fact-1 13}]))

(def attributes-structure-result-2-2
  [{"date" [1999], td/fact-1 0}
   {"date" [2000], td/fact-1 5}
   {"date" [2001], td/fact-1 6}
   {"date" [2002], td/fact-1 2}])

(def attributes-structure-result-2-3
  [{"date" [1999], td/fact-1 3}
   {"date" [2000], td/fact-1 8}
   {"date" [2001], td/fact-1 6}
   {"date" [2002], td/fact-1 6}])

(def attributes-structure-2-4
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :average
                                                      :value 0}
                                      :shared {:aggregation :multiple
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :month}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-2-5
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :median
                                                      :value 0}
                                      :shared {:aggregation :multiple
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :month}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-2-6
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :most-frequent
                                                      :value 0}
                                      :shared {:aggregation :multiple
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :month}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(defn attributes-structure-result-2-4to6 [replacement]
  [{td/fact-1 0, "date" [1999 10]}
   {td/fact-1 3, "date" [1999 10]}
   {td/fact-1 1, "date" [1999 11]}
   {td/fact-1 3, "date" [1999 12]}
   {td/fact-1 5, "date" [2000 1]}
   {td/fact-1 replacement, "date" [2000 2]}
   {td/fact-1 replacement, "date" [2000 3]}
   {td/fact-1 replacement, "date" [2000 4]}
   {td/fact-1 8, "date" [2000 5]}
   {td/fact-1 replacement, "date" [2000 6]}
   {td/fact-1 replacement, "date" [2000 7]}
   {td/fact-1 replacement, "date" [2000 8]}
   {td/fact-1 replacement, "date" [2000 9]}
   {td/fact-1 replacement, "date" [2000 10]}
   {td/fact-1 replacement, "date" [2000 11]}
   {td/fact-1 replacement, "date" [2000 12]}
   {td/fact-1 replacement, "date" [2001 1]}
   {td/fact-1 6, "date" [2001 2]}
   {td/fact-1 replacement, "date" [2001 3]}
   {td/fact-1 replacement, "date" [2001 4]}
   {td/fact-1 replacement, "date" [2001 5]}
   {td/fact-1 replacement, "date" [2001 6]}
   {td/fact-1 replacement, "date" [2001 7]}
   {td/fact-1 replacement, "date" [2001 8]}
   {td/fact-1 replacement, "date" [2001 9]}
   {td/fact-1 replacement, "date" [2001 10]}
   {td/fact-1 replacement, "date" [2001 11]}
   {td/fact-1 replacement, "date" [2001 12]}
   {td/fact-1 2, "date" [2002 1]}
   {td/fact-1 6, "date" [2002 2]}
   {td/fact-1 3, "date" [2002 2]}
   {td/fact-1 4, "date" [2002 2]}])

{"date" [2000 5], td/fact-1 8}
{"date" [2000 1], td/fact-1 5}
{"date" [1999 12], td/fact-1 3}
{"date" [1999 10], td/fact-1 3}
{"date" [2002 2], td/fact-1 3}
{"date" [2002 2], td/fact-1 4}
{"date" [1999 10], td/fact-1 0}
{"date" [1999 11], td/fact-1 1}
{"date" [2002 1], td/fact-1 2}
{"date" [2002 2], td/fact-1 6}
{"date" [2001 2], td/fact-1 6}

(def attributes-structure-result-2-4
  (attributes-structure-result-2-4to6 (double (/ 41 11))))

(def attributes-structure-result-2-5
  (attributes-structure-result-2-4to6 3))

(def attributes-structure-result-2-6 (attributes-structure-result-2-4to6 3))

(def attributes-structure-3
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :ignore
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :multiple
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}}]}
   :parameter {:aggregate-by-attribute ["some-number"]}})

(def attributes-structure-result-3
  [{"some-number" 0, td/fact-1 0}
   {"some-number" 2, td/fact-1 1}
   {"some-number" 3, td/fact-1 4}
   {"some-number" 3, td/fact-1 5}
   {"some-number" 4, td/fact-1 6}
   {"some-number" 4, td/fact-1 3}
   {"some-number" 7, td/fact-1 6}
   {"some-number" 8, td/fact-1 3}])

(def attributes-structure-3-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :ignore
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}}]}
   :parameter {:aggregate-by-attribute ["some-number"]}})

(def attributes-structure-result-3-1
  [{"some-number" 0, td/fact-1 0}
   {"some-number" 2, td/fact-1 1}
   {"some-number" 3, td/fact-1 9}
   {"some-number" 4, td/fact-1 9}
   {"some-number" 7, td/fact-1 6}
   {"some-number" 8, td/fact-1 3}])

(def attributes-structure-3-2-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}}]}
   :parameter {:aggregate-by-attribute ["some-number"]}})

(def attributes-structure-result-3-2-1
  [{"some-number" 0, td/fact-1 0}
   {"some-number" 1, td/fact-1 0}
   {"some-number" 2, td/fact-1 1}
   {"some-number" 3, td/fact-1 9}
   {"some-number" 4, td/fact-1 9}
   {"some-number" 5, td/fact-1 0}
   {"some-number" 6, td/fact-1 0}
   {"some-number" 7, td/fact-1 6}
   {"some-number" 8, td/fact-1 3}])

(def attributes-structure-3-2-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :replace
                                                        :replacement :number
                                                        :value 0}
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}}]}
   :parameter {:aggregate-by-attribute ["some-number"]}})

(def attributes-structure-result-3-2-2
  [{"some-number" 0, td/fact-1 0}
   {"some-number" 1, td/fact-1 0}
   {"some-number" 2, td/fact-1 1}
   {"some-number" 3, td/fact-1 9}
   {"some-number" 4, td/fact-1 9}
   {"some-number" 5, td/fact-1 0}
   {"some-number" 6, td/fact-1 0}
   {"some-number" 7, td/fact-1 6}
   {"some-number" 8, td/fact-1 3}])

(def attributes-structure-3-3
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :value
                                                                :value 5}
                                                          :min {:type :value
                                                                :value 0}}}]}
   :parameter {:aggregate-by-attribute ["some-number"]}})

(def attributes-structure-result-3-3
  [{"some-number" 0, td/fact-1 0}
   {"some-number" 1, td/fact-1 0}
   {"some-number" 2, td/fact-1 1}
   {"some-number" 3, td/fact-1 9}
   {"some-number" 4, td/fact-1 9}
   {"some-number" 5, td/fact-1 0}
   {"some-number" 7, td/fact-1 6}
   {"some-number" 8, td/fact-1 3}])

(def attributes-structure-4-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true

                                        :date-config {:granularity :year}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-result-4-1
  [{"date" [1999], "some-number" 8, td/fact-1 4}
   {"date" [2001], "some-number" 7, td/fact-1 6}
   {"date" [2002], "some-number" 20, td/fact-1 18}])

(def attributes-structure-4-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true

                                        :date-config {:granularity :month}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-result-4-2
  [{"date" [1999 10], "some-number" 4, td/fact-1 3}
   {"date" [1999 11], "some-number" 2, td/fact-1 1}
   {"date" [1999 12], "some-number" 2, td/fact-1 0}
   {"date" [2001 2], "some-number" 7, td/fact-1 6}
   {"date" [2002 1], "some-number" 2, td/fact-1 0}
   {"date" [2002 2], "some-number" 18, td/fact-1 18}])

(def attributes-structure-5-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true

                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-result-5-1
  [{"date" [1999], "some-number" 0, td/fact-1 0}
   {"date" [1999], "some-number" 1, td/fact-1 0}
   {"date" [1999], "some-number" 2, td/fact-1 1}
   {"date" [1999], "some-number" 3, td/fact-1 0}
   {"date" [1999], "some-number" 4, td/fact-1 3}
   {"date" [1999], "some-number" 5, td/fact-1 0}
   {"date" [1999], "some-number" 6, td/fact-1 0}
   {"date" [1999], "some-number" 7, td/fact-1 0}
   {"date" [1999], "some-number" 8, td/fact-1 0}
   {"date" [2000], "some-number" 0, td/fact-1 0}
   {"date" [2000], "some-number" 1, td/fact-1 0}
   {"date" [2000], "some-number" 2, td/fact-1 0}
   {"date" [2000], "some-number" 3, td/fact-1 0}
   {"date" [2000], "some-number" 4, td/fact-1 0}
   {"date" [2000], "some-number" 5, td/fact-1 0}
   {"date" [2000], "some-number" 6, td/fact-1 0}
   {"date" [2000], "some-number" 7, td/fact-1 0}
   {"date" [2000], "some-number" 8, td/fact-1 0}
   {"date" [2001], "some-number" 0, td/fact-1 0}
   {"date" [2001], "some-number" 1, td/fact-1 0}
   {"date" [2001], "some-number" 2, td/fact-1 0}
   {"date" [2001], "some-number" 3, td/fact-1 0}
   {"date" [2001], "some-number" 4, td/fact-1 0}
   {"date" [2001], "some-number" 5, td/fact-1 0}
   {"date" [2001], "some-number" 6, td/fact-1 0}
   {"date" [2001], "some-number" 7, td/fact-1 6}
   {"date" [2001], "some-number" 8, td/fact-1 0}
   {"date" [2002], "some-number" 0, td/fact-1 0}
   {"date" [2002], "some-number" 1, td/fact-1 0}
   {"date" [2002], "some-number" 2, td/fact-1 0}
   {"date" [2002], "some-number" 3, td/fact-1 9}
   {"date" [2002], "some-number" 4, td/fact-1 6}
   {"date" [2002], "some-number" 5, td/fact-1 0}
   {"date" [2002], "some-number" 6, td/fact-1 0}
   {"date" [2002], "some-number" 7, td/fact-1 0}
   {"date" [2002], "some-number" 8, td/fact-1 3}])

(def attributes-structure-5-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :value
                                                                :value 9}
                                                          :min {:type :value
                                                                :value 0}}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-result-5-2
  [{"date" [1999], "some-number" 0, td/fact-1 0}
   {"date" [1999], "some-number" 1, td/fact-1 0}
   {"date" [1999], "some-number" 2, td/fact-1 1}
   {"date" [1999], "some-number" 3, td/fact-1 0}
   {"date" [1999], "some-number" 4, td/fact-1 3}
   {"date" [1999], "some-number" 5, td/fact-1 0}
   {"date" [1999], "some-number" 6, td/fact-1 0}
   {"date" [1999], "some-number" 7, td/fact-1 0}
   {"date" [1999], "some-number" 8, td/fact-1 0}
   {"date" [1999], "some-number" 9, td/fact-1 0}
   {"date" [2000], "some-number" 0, td/fact-1 0}
   {"date" [2000], "some-number" 1, td/fact-1 0}
   {"date" [2000], "some-number" 2, td/fact-1 0}
   {"date" [2000], "some-number" 3, td/fact-1 0}
   {"date" [2000], "some-number" 4, td/fact-1 0}
   {"date" [2000], "some-number" 5, td/fact-1 0}
   {"date" [2000], "some-number" 6, td/fact-1 0}
   {"date" [2000], "some-number" 7, td/fact-1 0}
   {"date" [2000], "some-number" 8, td/fact-1 0}
   {"date" [2000], "some-number" 9, td/fact-1 0}
   {"date" [2001], "some-number" 0, td/fact-1 0}
   {"date" [2001], "some-number" 1, td/fact-1 0}
   {"date" [2001], "some-number" 2, td/fact-1 0}
   {"date" [2001], "some-number" 3, td/fact-1 0}
   {"date" [2001], "some-number" 4, td/fact-1 0}
   {"date" [2001], "some-number" 5, td/fact-1 0}
   {"date" [2001], "some-number" 6, td/fact-1 0}
   {"date" [2001], "some-number" 7, td/fact-1 6}
   {"date" [2001], "some-number" 8, td/fact-1 0}
   {"date" [2001], "some-number" 9, td/fact-1 0}
   {"date" [2002], "some-number" 0, td/fact-1 0}
   {"date" [2002], "some-number" 1, td/fact-1 0}
   {"date" [2002], "some-number" 2, td/fact-1 0}
   {"date" [2002], "some-number" 3, td/fact-1 9}
   {"date" [2002], "some-number" 4, td/fact-1 6}
   {"date" [2002], "some-number" 5, td/fact-1 0}
   {"date" [2002], "some-number" 6, td/fact-1 0}
   {"date" [2002], "some-number" 7, td/fact-1 0}
   {"date" [2002], "some-number" 8, td/fact-1 3}
   {"date" [2002], "some-number" 9, td/fact-1 0}])

(def attributes-structure-6-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}}
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-result-6-1
  [{"date" [1999], "some-number" 0, "some-number-2" 0, td/fact-1 0}
   {"date" [1999], "some-number" 2, "some-number-2" 5, td/fact-1 1}
   {"date" [1999], "some-number" 4, "some-number-2" 0, td/fact-1 3}
   {"date" [2001], "some-number" 7, "some-number-2" 1, td/fact-1 6}
   {"date" [2002], "some-number" 2, "some-number-2" 9, td/fact-1 0}
   {"date" [2002], "some-number" 3, "some-number-2" 3, td/fact-1 9}
   {"date" [2002], "some-number" 8, "some-number-2" 0, td/fact-1 3}])

(def attributes-structure-6-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :date-config {:granularity :year}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}}
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :replace
                                                        :replacement :number
                                                        :value 0.9}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-result-6-2
  [{"some-number-2" 0, "date" [1999], "some-number" 0, td/fact-1 0}
   {"some-number-2" 0.9, "date" [1999], "some-number" 1, td/fact-1 0}
   {"some-number-2" 5, "date" [1999], "some-number" 2, td/fact-1 1}
   {"some-number-2" 0.9, "date" [1999], "some-number" 3, td/fact-1 0}
   {"some-number-2" 0, "date" [1999], "some-number" 4, td/fact-1 3}
   {"some-number-2" 0.9, "date" [1999], "some-number" 5, td/fact-1 0}
   {"some-number-2" 0.9, "date" [1999], "some-number" 6, td/fact-1 0}
   {"some-number-2" 0.9, "date" [1999], "some-number" 7, td/fact-1 0}
   {"some-number-2" 0.9, "date" [1999], "some-number" 8, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 0, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 1, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 2, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 3, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 4, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 5, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 6, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 7, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2000], "some-number" 8, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 0, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 1, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 2, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 3, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 4, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 5, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2001], "some-number" 6, td/fact-1 0}
   {"some-number-2" 1, "date" [2001], "some-number" 7, td/fact-1 6}
   {"some-number-2" 0.9, "date" [2001], "some-number" 8, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2002], "some-number" 0, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2002], "some-number" 1, td/fact-1 0}
   {"some-number-2" 9, "date" [2002], "some-number" 2, td/fact-1 0}
   {"some-number-2" 3, "date" [2002], "some-number" 3, td/fact-1 9}
   {"some-number-2" 0.9, "date" [2002], "some-number" 4, td/fact-1 6}
   {"some-number-2" 0.9, "date" [2002], "some-number" 5, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2002], "some-number" 6, td/fact-1 0}
   {"some-number-2" 0.9, "date" [2002], "some-number" 7, td/fact-1 0}
   {"some-number-2" 0, "date" [2002], "some-number" 8, td/fact-1 3}])

(def attributes-structure-7-1
  {:algorithm :k-means
   :attributes {:feature [{:value td/fact-1
                           :type :numeric
                           :given? true
                           :shared {:aggregation :sum
                                    :multiple-values-per-event :multiply
                                    :merge-policy :ignore-incomplete}
                           :missing-value {:method :ignore
                                           :replacement :number
                                           :value 0}}
                          {:value "date"
                           :type :date
                           :given? true
                           :date-config {:granularity :year}}
                          {:value "some-number"
                           :type :numeric
                           :given? true
                           :missing-value {:method :ignore
                                           :replacement :number
                                           :value 0}}
                          {:value "some-number-2"
                           :type :numeric
                           :given? true
                           :missing-value {:method :ignore
                                           :replacement :number
                                           :value 0}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-result-7-1
  [{"date" [1999], td/fact-1 4, "some-number" 6, "some-number-2" 3}
   {"date" [2001], td/fact-1 6, "some-number" 7, "some-number-2" 1}
   {"date" [2002], td/fact-1 8, "some-number" 11, "some-number-2" 3}])

(deftest training-data-test
  (testing "date independent attribute"
    (is (= (set (feat/transform attributes-structure-1 data))
           (set attributes-structure-result-1)))
    (is (= (feat/transform attributes-structure-2 data)
           attributes-structure-result-2))
    (is (= (set (feat/transform attributes-structure-2-1 data))
           (set attributes-structure-result-2-1)))
    (is (= (set (feat/transform attributes-structure-replace-avg-month data))
           (set attributes-result-replace-avg-month)))
    (is (= (set (feat/transform attributes-structure-2-2 data))
           (set attributes-structure-result-2-2)))
    (is (= (set (feat/transform attributes-structure-2-3 data))
           (set attributes-structure-result-2-3)))
    (is (= (set (feat/transform attributes-structure-2-4 data))
           (set attributes-structure-result-2-4)))
    (is (= (set (feat/transform attributes-structure-2-5 data))
           (set attributes-structure-result-2-5)))
    (is (= (set (feat/transform attributes-structure-2-6 data))
           (set attributes-structure-result-2-6))))
  (testing "numeric independent attribute"
    (is (= (feat/transform attributes-structure-3 data-2)
           attributes-structure-result-3))
    (is (= (feat/transform attributes-structure-3-1 data-2)
           attributes-structure-result-3-1))
    (is (= (feat/transform attributes-structure-3-2-1 data-2)
           attributes-structure-result-3-2-1))
    (is (= (feat/transform attributes-structure-3-2-2 data-2)
           attributes-structure-result-3-2-2))

    ; This will test letting the range only proceed to a max will but other existing values that exceed max are still collected
    (is (= (feat/transform attributes-structure-3-3 data-2)
           attributes-structure-result-3-3)))
  (testing "numeric and date independent attribute; event is ignored if missing"
    (is (= (feat/transform attributes-structure-4-1 data-2)
           attributes-structure-result-4-1))
    (is (= (feat/transform attributes-structure-4-2 data-2)
           attributes-structure-result-4-2)))
  (testing "numeric and date independent attribute; combining two ranges"
    (is (= (feat/transform attributes-structure-5-1 data-2)
           attributes-structure-result-5-1))
    (is (= (feat/transform attributes-structure-5-2 data-2)
           attributes-structure-result-5-2)))
  (testing "two numeric and date independent attribute; combining two ranges and one not range attribute"
    (is (= (feat/transform attributes-structure-6-1 data-3)
           attributes-structure-result-6-1))
    (is (= (feat/transform attributes-structure-6-2 data-3)
           attributes-structure-result-6-2)))
  (with-redefs [config-algortihms/explorama-enforce-merge-policy-defaults :ui]
    (testing "4 features"
      (is (= (feat/transform attributes-structure-7-1 data-3)
             attributes-structure-result-7-1)))))

(deftest fill-date-data-test
  (testing "tests date range"
    (is (= (ranges/date-range [2000] [2015] {:date-config {:granularity :year}})
           (mapv vector (range 2000 2016))))
    (is (= (ranges/date-range [2000 2] [2002 3] {:date-config {:granularity :quarter}})
           [[2000 2] [2000 3] [2000 4] [2001 1] [2001 2] [2001 3] [2001 4] [2002 1] [2002 2] [2002 3]]))
    (is (= (ranges/date-range [2000 3] [2002 2] {:date-config {:granularity :month}})
           (concat (mapv (fn [i] [2000 i]) (range 3 13))
                   (mapv (fn [i] [2001 i]) (range 1 13))
                   (mapv (fn [i] [2002 i]) (range 1 3)))))
    (is (= (ranges/date-range [2000 2 1] [2000 2 4] {:date-config {:granularity :day}})
           [[2000 2 1] [2000 2 2] [2000 2 3] [2000 2 4]])))
  (testing "tests numeric range"
    (is (= (ranges/numeric-range 2 6 {:continues-value {:method :range
                                                        :step 1
                                                        :max {:type :value
                                                              :value 0}
                                                        :min {:type :value
                                                              :value 0}}}
                                 ranges/training-data-step-function)
           [0]))
    (is (= (ranges/numeric-range 2 6 {:continues-value {:method :range
                                                        :step 0.1
                                                        :max {:type :value
                                                              :value 0.5}
                                                        :min {:type :value
                                                              :value 0}}}
                                 ranges/training-data-step-function)
           [0 0.1 0.2 0.30000000000000004 0.4 0.5])) ;? not sure about this
    (is (= (ranges/numeric-range 2 6 {:continues-value {:method :range
                                                        :step 0.1
                                                        :max {:type :value
                                                              :value 0}
                                                        :min {:type :value
                                                              :value 0}}}
                                 ranges/training-data-step-function)
           [0]))
    (is (= (ranges/numeric-range 2 6 {:continues-value {:method :range
                                                        :step 1
                                                        :max {:type :max}
                                                        :min {:type :min}}}
                                 ranges/training-data-step-function)
           [2 3 4 5 6]))
    (is (= (ranges/numeric-range 1 2 {:continues-value {:method :range
                                                        :step 0.2
                                                        :max {:type :max}
                                                        :min {:type :min}}}
                                 ranges/training-data-step-function)
           [1 1.2 1.4 1.5999999999999999 1.7999999999999998 1.9999999999999998 2.1999999999999997]))))  ;? not sure about this

(deftest algo-data-test
  (testing "basic test"
    (let [test-result
          {:data
           [{"date" 0, td/fact-1 3} {"date" 1, td/fact-1 1} {"date" 2, td/fact-1 3}
            {"date" 3, td/fact-1 5} {"date" 4, td/fact-1 0.6} {"date" 5, td/fact-1 0.6}
            {"date" 6, td/fact-1 0.6} {"date" 7, td/fact-1 8} {"date" 8, td/fact-1 0.6}
            {"date" 9, td/fact-1 0.6} {"date" 10, td/fact-1 0.6} {"date" 11, td/fact-1 0.6}
            {"date" 12, td/fact-1 0.6} {"date" 13, td/fact-1 0.6} {"date" 14, td/fact-1 0.6}
            {"date" 15, td/fact-1 0.6} {"date" 16, td/fact-1 6} {"date" 17, td/fact-1 0.6}
            {"date" 18, td/fact-1 0.6} {"date" 19, td/fact-1 0.6} {"date" 20, td/fact-1 0.6}
            {"date" 21, td/fact-1 0.6} {"date" 22, td/fact-1 0.6} {"date" 23, td/fact-1 0.6}
            {"date" 24, td/fact-1 0.6} {"date" 25, td/fact-1 0.6} {"date" 26, td/fact-1 0.6}
            {"date" 27, td/fact-1 2} {"date" 28, td/fact-1 13}]
           :mapping
           {["date" [2002 1]] 27
            ["date" [2001 8]] 22
            ["date" [2000 8]] 10
            ["date" [2000 10]] 12
            ["date" [2000 9]] 11
            ["date" [2000 5]] 7
            ["date" [2001 10]] 24
            ["date" [2001 1]] 15
            ["date" [1999 11]] 1
            ["date" [2000 3]] 5
            ["date" [2000 6]] 8
            ["date" [1999 12]] 2
            ["date" [2000 7]] 9
            ["date" [2000 1]] 3
            ["date" [2001 3]] 17
            ["date" [2001 6]] 20
            ["date" [2000 11]] 13
            ["date" [2001 11]] 25
            ["date" [2001 5]] 19
            ["date" [2001 7]] 21
            ["date" [1999 10]] 0
            ["date" [2001 4]] 18
            ["date" [2001 9]] 23
            ["date" [2000 4]] 6
            ["date" [2001 12]] 26
            ["date" [2000 2]] 4
            ["date" [2001 2]] 16
            ["date" [2000 12]] 14
            ["date" [2002 2]] 28}}
          result (->> (feat/transform attributes-structure-1 data)
                      (algo/transform attributes-structure-1))]
      (is (=  (:data test-result)
              (:data result)))
      (is (= (:mapping test-result)
             (:mapping result))))))

(deftest aggregation-date-test
  (testing "min"
    (is (= (feat/aggregate-function-date :min [[2002 4 5] [2001 2 18]
                                               [2003 12 31]
                                               [2002 4 30] [2002 5 1]
                                               [2001 3 1] [2001 3 4]])
           [2001 2 18])))
  (testing "max"
    (is (= (feat/aggregate-function-date :max [[2002 4 5] [2001 2 18]
                                               [2003 12 31]
                                               [2002 4 30] [2002 5 1]
                                               [2001 3 1] [2001 3 4]])
           [2003 12 31]))))