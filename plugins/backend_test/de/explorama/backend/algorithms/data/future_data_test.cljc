(ns de.explorama.backend.algorithms.data.future-data-test
  (:require #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cljs.test :refer-macros [deftest is testing]])
            [de.explorama.backend.algorithms.data.algorithm :as algo]
            [de.explorama.backend.algorithms.data.features :as feat]
            [de.explorama.backend.algorithms.data.test-common :refer [data
                                                                      data-3]]
            [de.explorama.backend.algorithms.test-env]
            [de.explorama.shared.common.test-data :as td]))

(def some-number-1 #?(:cljs 22.857142857142854
                      :clj 22.857142857142858))
(def some-number-2 #?(:cljs 13.428571428571427
                      :clj 13.428571428571429))
(def some-number-3 #?(:cljs 10.844814241486066
                      :clj 10.844814241486068))
(def some-number-4 #?(:cljs 11.128482972136222
                      :clj 11.128482972136224))

(def attributes-structure-future-1
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0.6}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :shared {:aggregation :sum
                                                 :multiple-values-per-event :multiply}
                                        :date-config {:granularity :year}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-future-2
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}}]
                :independent-variable [{:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :shared {:aggregation :sum
                                                 :multiple-values-per-event :multiply}
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}]}
   :parameter {:aggregate-by-attribute ["some-number"]
               :length 5}})

(def attributes-structure-future-3
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
                                                          :min {:type :min}}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-future-4
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}}]
                :independent-variable [{:value "date"
                                        :type :date
                                        :given? true
                                        :shared {:aggregation :sum
                                                 :multiple-values-per-event :multiply}
                                        :date-config {:granularity :year}}
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :manual
                                                        :manual-values []}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-future-5
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :manual
                                                        :manual-values []}}]}
   :parameter {:aggregate-by-attribute ["some-number-2"]}})

(def attributes-structure-future-6
  {:algorithm :lmmr
   :attributes {:dependent-variable [{:value td/fact-1
                                      :type :numeric
                                      :missing-value {:method :replace
                                                      :replacement :number
                                                      :value 0}
                                      :shared {:aggregation :sum
                                               :multiple-values-per-event :multiply}}]
                :independent-variable [{:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :manual
                                                        :manual-values [1 2]}}]}
   :parameter {:aggregate-by-attribute ["some-number-2"]}})

(def attributes-structure-future-7
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :manual
                                                        :manual-values [1 2]}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-future-8
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :manual
                                                        :manual-values [1 2 3 4 5]}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :continues-value {:method :range
                                                          :step 1
                                                          :max {:type :max}
                                                          :min {:type :min}}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :start-value
                                                                      :value 5}}}]}
   :parameter {:aggregate-by-attribute ["date" "some-number"]}})

(def attributes-structure-future-9
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-future-10
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}]}
   :parameter {:aggregate-by-attribute ["date"]
               :length 8}})

(def attributes-structure-future-11
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-future-12
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-future-12-month
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
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-future-12-quarter
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
                                        :date-config {:granularity :quarter}}
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(def attributes-structure-future-12-day
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
                                        :date-config {:granularity :day}}
                                       {:value "some-number-2"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :range
                                                        :step 1
                                                        :start-value {:type :max}}}
                                       {:value "some-number"
                                        :type :numeric
                                        :given? true
                                        :missing-value {:method :ignore
                                                        :replacement :number
                                                        :value 0}
                                        :future-values {:method :auto}}]}
   :parameter {:aggregate-by-attribute ["date"]}})

(defn testing-future-data [desc structure data result-offset result-raw-data result-result]
  (testing desc
    (let [task (feat/transform structure data)
          raw-data (algo/transform structure task)
          future-data-raw (feat/future-data- structure raw-data)
          offsets (algo/offsets structure
                                raw-data)
          result (algo/transform structure
                                 future-data-raw
                                 (algo/offsets structure
                                               raw-data))]
      (is (= result-offset
             offsets))
      (is (= result-raw-data
             future-data-raw))
      (is (= result-result
             result)))))

(deftest future-data
  (testing-future-data "date independent variable"
                       attributes-structure-future-1
                       data
                       {"date" 3}
                       [{"date" [2003]}
                        {"date" [2004]}
                        {"date" [2005]}]
                       {:data [{"date" 4}
                               {"date" 5}
                               {"date" 6}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "numeric independent variable"
                       attributes-structure-future-2
                       data-3
                       {}
                       [{"some-number" 9}
                        {"some-number" 10}
                        {"some-number" 11}
                        {"some-number" 12}
                        {"some-number" 13}]
                       {:data [{"some-number" 9}
                               {"some-number" 10}
                               {"some-number" 11}
                               {"some-number" 12}
                               {"some-number" 13}]
                        :mapping {}})
  (testing-future-data "numeric and date independent variable"
                       attributes-structure-future-3
                       data-3
                       {"date" 3}
                       [{"date" [2003], "some-number" 9}
                        {"date" [2004], "some-number" 10}
                        {"date" [2005], "some-number" 11}]
                       {:data [{"date" 4, "some-number" 9}
                               {"date" 5, "some-number" 10}
                               {"date" 6, "some-number" 11}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "two independent variables and one dependent variable with future manual values - empty"
                       attributes-structure-future-4
                       data-3
                       {"date" 3}
                       [{"some-number-2" :?, "date" [2003], "some-number" 9}
                        {"some-number-2" :?, "date" [2004], "some-number" 10}
                        {"some-number-2" :?, "date" [2005], "some-number" 11}]
                       {:data [{"some-number-2" :?, "date" 4, "some-number" 9}
                               {"some-number-2" :?, "date" 5, "some-number" 10}
                               {"some-number-2" :?, "date" 6, "some-number" 11}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "one independet variable with future manual values - empty"
                       attributes-structure-future-5
                       data-3
                       {}
                       [{"some-number-2" :?}
                        {"some-number-2" :?}
                        {"some-number-2" :?}]
                       {:data [{"some-number-2" :?}
                               {"some-number-2" :?}
                               {"some-number-2" :?}]
                        :mapping {}})
  (testing-future-data "one independet variable with future manual values - partially filled"
                       attributes-structure-future-6
                       data-3
                       {}
                       [{"some-number-2" 1}
                        {"some-number-2" 2}
                        {"some-number-2" :?}]
                       {:data [{"some-number-2" 1}
                               {"some-number-2" 2}
                               {"some-number-2" :?}]
                        :mapping {}})
  (testing-future-data "Three independent variables and one dependent variable with future manual values - partially filled"
                       attributes-structure-future-7
                       data-3
                       {"date" 3}
                       [{"some-number-2" 1, "date" [2003], "some-number" 9}
                        {"some-number-2" 2, "date" [2004], "some-number" 10}
                        {"some-number-2" :?, "date" [2005], "some-number" 11}]
                       {:data [{"some-number-2" 1, "date" 4, "some-number" 9}
                               {"some-number-2" 2, "date" 5, "some-number" 10}
                               {"some-number-2" :?, "date" 6, "some-number" 11}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "two independent variables and one dependent variable"
                       attributes-structure-future-9
                       data-3
                       {"date" 3}
                       [{"some-number-2" 13, "date" [2003]}
                        {"some-number-2" 14, "date" [2004]}
                        {"some-number-2" 15, "date" [2005]}]
                       {:data [{"some-number-2" 13, "date" 4}
                               {"some-number-2" 14, "date" 5}
                               {"some-number-2" 15, "date" 6}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "two independent variables with one auto feature and one dependent variable"
                       attributes-structure-future-10
                       data-3
                       {"date" 3}
                       [{"date" [2003], "some-number-2" 10.0}
                        {"date" [2004], "some-number-2" 11.5}
                        {"date" [2005], "some-number-2" 13.0}
                        {"date" [2006], "some-number-2" 14.5}
                        {"date" [2007], "some-number-2" 16.0}
                        {"date" [2008], "some-number-2" 17.5}
                        {"date" [2009], "some-number-2" 19.0}
                        {"date" [2010], "some-number-2" 20.5}]
                       {:data [{"date" 4, "some-number-2" 10.0}
                               {"date" 5, "some-number-2" 11.5}
                               {"date" 6, "some-number-2" 13.0}
                               {"date" 7, "some-number-2" 14.5}
                               {"date" 8, "some-number-2" 16.0}
                               {"date" 9, "some-number-2" 17.5}
                               {"date" 10, "some-number-2" 19.0}
                               {"date" 11, "some-number-2" 20.5}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6
                                  ["date" [2006]] 7
                                  ["date" [2007]] 8
                                  ["date" [2008]] 9
                                  ["date" [2009]] 10
                                  ["date" [2010]] 11}})
  (testing-future-data "three independent variables with two auto feature and one dependent variable"
                       attributes-structure-future-11
                       data-3
                       {"date" 3}
                       [{"some-number-2" 10.0, "some-number" 19.5, "date" [2003]}
                        {"some-number-2" 11.714285714285715, "some-number" some-number-1, "date" [2004]}
                        {"some-number-2" some-number-2, "some-number" 26.214285714285715, "date" [2005]}]
                       {:data [{"date" 4, "some-number-2" 10.0, "some-number" 19.5}
                               {"date" 5, "some-number-2" 11.714285714285715, "some-number" some-number-1}
                               {"date" 6, "some-number-2" some-number-2, "some-number" 26.214285714285715}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "three independent variables with one auto feature and one dependent variable"
                       attributes-structure-future-12
                       data-3
                       {"date" 3}
                       [{"some-number-2" 13, "some-number" 19.5, "date" [2003]}
                        {"some-number-2" 14, "some-number" some-number-1, "date" [2004]}
                        {"some-number-2" 15, "some-number" 26.214285714285715, "date" [2005]}]
                       {:data [{"date" 4, "some-number-2" 13, "some-number" 19.5}
                               {"date" 5, "some-number-2" 14, "some-number" some-number-1}
                               {"date" 6, "some-number-2" 15, "some-number" 26.214285714285715}]
                        :mapping {["date" [2003]] 4
                                  ["date" [2004]] 5
                                  ["date" [2005]] 6}})
  (testing-future-data "three independent variables with one auto feature and one dependent variable - month"
                       attributes-structure-future-12-month
                       data-3
                       {"date" 28}
                       [{"some-number-2" 10, "some-number" 10.561145510835914, "date" [2002 3]}
                        {"some-number-2" 11, "some-number" some-number-3, "date" [2002 4]}
                        {"some-number-2" 12, "some-number" some-number-4, "date" [2002 5]}]
                       {:data [{"date" 29, "some-number-2" 10, "some-number" 10.561145510835914}
                               {"date" 30, "some-number-2" 11, "some-number" some-number-3}
                               {"date" 31, "some-number-2" 12, "some-number" some-number-4}]
                        :mapping {["date" [2002 3]] 29
                                  ["date" [2002 4]] 30
                                  ["date" [2002 5]] 31}})
  (testing-future-data "three independent variables with one auto feature and one dependent variable - quarter"
                       attributes-structure-future-12-quarter
                       data-3
                       {"date" 9}
                       [{"some-number-2" 13, "some-number" 18.442622950819672, "date" [2002 2]}
                        {"some-number-2" 14, "some-number" 19.71311475409836, "date" [2002 3]}
                        {"some-number-2" 15, "some-number" 20.983606557377048, "date" [2002 4]}]
                       {:data [{"date" 10, "some-number-2" 13, "some-number" 18.442622950819672}
                               {"date" 11, "some-number-2" 14, "some-number" 19.71311475409836}
                               {"date" 12, "some-number-2" 15, "some-number" 20.983606557377048}]
                        :mapping {["date" [2002 2]] 10
                                  ["date" [2002 3]] 11
                                  ["date" [2002 4]] 12}})
  (testing-future-data "three independent variables with one auto feature and one dependent variable - day"
                       attributes-structure-future-12-day
                       data-3
                       {"date" 855}
                       [{"some-number-2" 10, "some-number" 5.929175162179209, "date" [2002 2 15]}
                        {"some-number-2" 11, "some-number" 5.93359987981449, "date" [2002 2 16]}
                        {"some-number-2" 12, "some-number" 5.93802459744977, "date" [2002 2 17]}]
                       {:data [{"date" 856, "some-number-2" 10, "some-number" 5.929175162179209}
                               {"date" 857, "some-number-2" 11, "some-number" 5.93359987981449}
                               {"date" 858, "some-number-2" 12, "some-number" 5.93802459744977}]
                        :mapping {["date" [2002 2 15]] 856
                                  ["date" [2002 2 16]] 857
                                  ["date" [2002 2 17]] 858}}))

(deftest adjust-data-for-plot-test
  (testing "Granularity Year"
    (is (= (vec (feat/adjust-data-for-plot
                 {:dependent-variable
                  [{:value td/fact-1
                    :type :numeric
                    :given? nil
                    :missing-value {:method :ignore, :replacement :average, :value 0}
                    :shared
                    {:aggregation :sum
                     :multiple-values-per-event :multiply
                     :merge-policy :merge-incomplete}}]
                  :independent-variable
                  [{:value "date"
                    :type :date
                    :given? true
                    :date-config {:granularity :year}}]}
                 {:data [{"date" 2} {"date" 3} {"date" 4}]
                  :mapping {["date" [1999]] 2, ["date" [2000]] 3, ["date" [2001]] 4}}
                 '({:id "0f1f1331-8d89-4fa1-9001-9fa8efa4edb5"
                    :fact-1 -929.0000000000018}
                   {:id "12fd905b-31ea-4f19-b32a-0e33bed31be2"
                    :fact-1 -3127.0000000000027}
                   {:id "df3b69ba-d914-4535-a4e3-0217de7b4573"
                    :fact-1 -5325.000000000003})
                 true))
           [{"id" "0f1f1331-8d89-4fa1-9001-9fa8efa4edb5"
             td/fact-1 -929.0000000000018
             "date" "1999-01-01"}
            {"id" "12fd905b-31ea-4f19-b32a-0e33bed31be2"
             td/fact-1 -3127.0000000000027
             "date" "2000-01-01"}
            {"id" "df3b69ba-d914-4535-a4e3-0217de7b4573"
             td/fact-1 -5325.000000000003
             "date" "2001-01-01"}])))
  (testing "Granularity Month"
    (is (= (vec (feat/adjust-data-for-plot
                 {:dependent-variable
                  [{:value td/fact-1
                    :type :numeric
                    :given? nil
                    :missing-value {:method :ignore, :replacement :average, :value 0}
                    :shared
                    {:aggregation :sum
                     :multiple-values-per-event :multiply
                     :merge-policy :merge-incomplete}}]
                  :independent-variable
                  [{:value "date"
                    :type :date
                    :given? true
                    :date-config {:granularity :month}}]}
                 {:data [{"date" 24} {"date" 25} {"date" 26}]
                  :mapping
                  {["date" [1999 1]] 24, ["date" [1999 2]] 25, ["date" [1999 3]] 26}}
                 '({:id "66bc4196-9916-40ca-abf1-2b8cbc2abe62"
                    :fact-1 45.05612077453236}
                   {:id "6163e2fa-d23b-46a2-ae1e-b3ac7555ef62"
                    :fact-1 31.438792254676798}
                   {:id "2f7fbe29-03f1-4cda-a65c-1114094100ec"
                    :fact-1 17.82146373482118})
                 true))
           [{"id" "66bc4196-9916-40ca-abf1-2b8cbc2abe62"
             td/fact-1 45.05612077453236
             "date" "1999-01-01"}
            {"id" "6163e2fa-d23b-46a2-ae1e-b3ac7555ef62"
             td/fact-1 31.438792254676798
             "date" "1999-02-01"}
            {"id" "2f7fbe29-03f1-4cda-a65c-1114094100ec"
             td/fact-1 17.82146373482118
             "date" "1999-03-01"}])))
  (testing "Granularity Quarter"
    (is (= (vec (feat/adjust-data-for-plot
                 {:dependent-variable
                  [{:value td/fact-1
                    :type :numeric
                    :given? nil
                    :missing-value {:method :ignore, :replacement :average, :value 0}
                    :shared
                    {:aggregation :sum
                     :multiple-values-per-event :multiply
                     :merge-policy :merge-incomplete}}]
                  :independent-variable
                  [{:value "date"
                    :type :date
                    :given? true
                    :date-config {:granularity :quarter}}]}
                 {:data [{"date" 8} {"date" 9} {"date" 10}]
                  :mapping
                  {["date" [1999 1]] 8, ["date" [1999 2]] 9, ["date" [1999 3]] 10}}
                 '({:id "6ca6fa7d-1d09-421d-a8b8-3e009beaf105", :fact-1 149.5}
                   {:id "5a6736c9-83e4-40f0-b6c9-e293f9c30316"
                    :fact-1 51.16666666666663}
                   {:id "fbc287a3-1307-4c30-8135-380c8c414580"
                    :fact-1 -47.16666666666674})
                 true))
           [{"id" "6ca6fa7d-1d09-421d-a8b8-3e009beaf105"
             td/fact-1 149.5
             "date" "1999-03-01"}
            {"id" "5a6736c9-83e4-40f0-b6c9-e293f9c30316"
             td/fact-1 51.16666666666663
             "date" "1999-06-01"}
            {"id" "fbc287a3-1307-4c30-8135-380c8c414580"
             td/fact-1 -47.16666666666674
             "date" "1999-09-01"}])))
  (testing "Granularity Day"
    (is (= (vec (feat/adjust-data-for-plot
                 {:dependent-variable
                  [{:value td/fact-1
                    :type :numeric
                    :given? nil
                    :missing-value {:method :ignore, :replacement :average, :value 0}
                    :shared
                    {:aggregation :sum
                     :multiple-values-per-event :multiply
                     :merge-policy :merge-incomplete}}]
                  :independent-variable
                  [{:value "date"
                    :type :date
                    :given? true
                    :date-config {:granularity :day}}]}
                 {:data [{"date" 724} {"date" 725} {"date" 726}]
                  :mapping
                  {["date" [1998 12 29]] 724
                   ["date" [1998 12 30]] 725
                   ["date" [1998 12 31]] 726}}
                 '({:id "c5895d2e-4c35-438f-9a61-c4ea441e5490"
                    :fact-1 33.13822660429772}
                   {:id "5779cc85-4d7c-4e49-8231-862a06b21914"
                    :fact-1 33.13667651166904}
                   {:id "e672d5ad-fb8a-44b7-9bc4-23fd751a4d2d"
                    :fact-1 33.135126419040354})
                 true))
           [{"id" "c5895d2e-4c35-438f-9a61-c4ea441e5490"
             td/fact-1 33.13822660429772
             "date" "1998-12-29"}
            {"id" "5779cc85-4d7c-4e49-8231-862a06b21914"
             td/fact-1 33.13667651166904
             "date" "1998-12-30"}
            {"id" "e672d5ad-fb8a-44b7-9bc4-23fd751a4d2d"
             td/fact-1 33.135126419040354
             "date" "1998-12-31"}]))))
