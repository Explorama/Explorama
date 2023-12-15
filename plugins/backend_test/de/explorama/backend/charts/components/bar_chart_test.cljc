(ns de.explorama.backend.charts.components.bar-chart-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.charts.data.base-charts :refer [base-bar-settings]]
            [de.explorama.backend.charts.data.core :as charts]
            [de.explorama.backend.charts.data.colors :as colors]
            [de.explorama.shared.common.test-data :as td]))

(def month "month")
(def year "year")
(def country td/country)
(def fact-1 td/fact-1)
(def category-1 td/category-1)
(def org td/org)

(def number-of-events
  :number-of-events)

(def sum-by-all
  "all")
(def sum-filter-empty
  #{})
(def category-1-characteristics
  #{(td/category-val "A" 1) (td/category-val "A" 2)})

(def month-characteristics
  #{"08" "03" "01" "02"})

(def year-characteristics
  #{"1998" "1997" "1999"})

(defn color [idx]
  (@#'colors/next-color ["test"] idx))

(defn- color-vec [take-num idx]
  (vec (repeat take-num (color idx))))

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3", "datasource" td/datasource-a, td/org [(td/org-val 2) (td/org-val 4)], "location" [[15 15]], "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1999-01-03", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0}])

(def month-base
  {:labels ["1997-10" "1997-11" "1997-12"
            "1998-01" "1998-02" "1998-03" "1998-04" "1998-05" "1998-06" "1998-07" "1998-08" "1998-09" "1998-10" "1998-11" "1998-12"
            "1999-01"]
   :x-axis-time {:unit "month"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}})

(def bar-chart-month
  (assoc month-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 16 0)
                           :data [{:x "1997-10", :y 0} {:x "1997-11", :y nil} {:x "1997-12", :y 0}
                                  {:x "1998-01", :y nil} {:x "1998-02", :y nil} {:x "1998-03", :y nil}
                                  {:x "1998-04", :y nil} {:x "1998-05", :y nil} {:x "1998-06", :y 0}
                                  {:x "1998-07", :y nil} {:x "1998-08", :y 1} {:x "1998-09", :y nil}
                                  {:x "1998-10", :y nil} {:x "1998-11", :y nil} {:x "1998-12", :y nil}
                                  {:x "1999-01", :y 0}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" td/fact-1 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 1}
         :y-axis-attr td/fact-1))
(def bar-chart-month-number-of-events
  (assoc month-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 16 0)
                           :data [{:x "1997-10", :y 1} {:x "1997-11", :y 0} {:x "1997-12", :y 1}
                                  {:x "1998-01", :y 0} {:x "1998-02", :y 0} {:x "1998-03", :y 0}
                                  {:x "1998-04", :y 0} {:x "1998-05", :y 0} {:x "1998-06", :y 1}
                                  {:x "1998-07", :y 0} {:x "1998-08", :y 2} {:x "1998-09", :y 0}
                                  {:x "1998-10", :y 0} {:x "1998-11", :y 0} {:x "1998-12", :y 0}
                                  {:x "1999-01", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 2}
         :y-axis-attr :number-of-events))
(def bar-chart-month-number-of-events-show-by-month
  (assoc month-base
         :labels ["01" "02" "03" "08"]
         :datasets [(assoc base-bar-settings
                           :label "01"
                           :backgroundColor (color-vec 2 0)
                           :data [{:x "1997-10", :y 1} {:x "1997-11", :y 0} {:x "1997-12", :y 1}
                                  {:x "1998-01", :y 0} {:x "1998-02", :y 0} {:x "1998-03", :y 0}
                                  {:x "1998-04", :y 0} {:x "1998-05", :y 0} {:x "1998-06", :y 1}
                                  {:x "1998-07", :y 0} {:x "1998-08", :y 2} {:x "1998-09", :y 0}
                                  {:x "1998-10", :y 0} {:x "1998-11", :y 0} {:x "1998-12", :y 0}
                                  {:x "1999-01", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 16 0)
                           :data [{:x "1997-10", :y 1} {:x "1997-11", :y 0} {:x "1997-12", :y 1}
                                  {:x "1998-01", :y 0} {:x "1998-02", :y 0} {:x "1998-03", :y 0}
                                  {:x "1998-04", :y 0} {:x "1998-05", :y 0} {:x "1998-06", :y 1}
                                  {:x "1998-07", :y 0} {:x "1998-08", :y 2} {:x "1998-09", :y 0}
                                  {:x "1998-10", :y 0} {:x "1998-11", :y 0} {:x "1998-12", :y 0}
                                  {:x "1999-01", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 16 0)
                           :data [{:x "1997-10", :y 1} {:x "1997-11", :y 0} {:x "1997-12", :y 1}
                                  {:x "1998-01", :y 0} {:x "1998-02", :y 0} {:x "1998-03", :y 0}
                                  {:x "1998-04", :y 0} {:x "1998-05", :y 0} {:x "1998-06", :y 1}
                                  {:x "1998-07", :y 0} {:x "1998-08", :y 2} {:x "1998-09", :y 0}
                                  {:x "1998-10", :y 0} {:x "1998-11", :y 0} {:x "1998-12", :y 0}
                                  {:x "1999-01", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 16 0)
                           :data [{:x "1997-10", :y 1} {:x "1997-11", :y 0} {:x "1997-12", :y 1}
                                  {:x "1998-01", :y 0} {:x "1998-02", :y 0} {:x "1998-03", :y 0}
                                  {:x "1998-04", :y 0} {:x "1998-05", :y 0} {:x "1998-06", :y 1}
                                  {:x "1998-07", :y 0} {:x "1998-08", :y 2} {:x "1998-09", :y 0}
                                  {:x "1998-10", :y 0} {:x "1998-11", :y 0} {:x "1998-12", :y 0}
                                  {:x "1999-01", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 2}))

(def month-number-of-events
  {"1997-10" {:number-of-events 1 :sum 0}})

(def year-base
  {:labels ["1997" "1998" "1999"]
   :x-axis-time {:unit "year"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}})

(def bar-chart-year
  (assoc year-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 3 0)
                           :data [{:x "1997", :y 0} {:x "1998", :y 1} {:x "1999", :y 0}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" td/fact-1 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 1}
         :y-axis-attr td/fact-1))
(def bar-chart-year-number-of-events
  (assoc year-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 3 0)
                           :data [{:x "1997", :y 2} {:x "1998", :y 3} {:x "1999", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 3}
         :y-axis-attr :number-of-events))

(def bar-chart-year-number-of-events-show-by-year
  (assoc year-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 3 0)
                           :data [{:x "1997", :y 2} {:x "1998", :y 3} {:x "1999", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 3}
         :y-axis-attr :number-of-events))

(def bar-chart-year-number-of-events-show-by-month
  (assoc year-base
         :datasets [(assoc base-bar-settings
                           :label "01"
                           :backgroundColor (color-vec 3 0)
                           :data [{:x "1997", :y 0} {:x "1998", :y 0} {:x "1999", :y 1}]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label "02"
                           :backgroundColor (color-vec 3 1)
                           :data [{:x "1997", :y 0} {:x "1998", :y 0} {:x "1999", :y 0}]
                           :legend {:color (color 1)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label "03"
                           :backgroundColor (color-vec 3 2)
                           :data [{:x "1997", :y 0} {:x "1998", :y 0} {:x "1999", :y 0}]
                           :legend {:color (color 2)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label "08"
                           :backgroundColor (color-vec 3 3)
                           :data [{:x "1997", :y 0} {:x "1998", :y 2} {:x "1999", :y 0}]
                           :legend {:color (color 3)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 2}
         :y-axis-attr :number-of-events))

(def country-base
  {:labels [td/country-a]
   :x-axis-time nil})
(def bar-chart-country
  (assoc country-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor [(color 0)]
                           :data [1]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" td/fact-1 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 1}
         :y-axis-attr td/fact-1))
(def bar-chart-country-number-of-events
  (assoc country-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor [(color 0)]
                           :data [6]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 6}
         :y-axis-attr :number-of-events))

(def org-base
  {:labels [(td/org-val 2) (td/org-val 4) (td/org-val 6)]
   :x-axis-time nil})
(def bar-chart-org
  (assoc org-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 3 0)
                           :data [1 1 0]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" td/fact-1 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 1}
         :y-axis-attr td/fact-1))
(def bar-chart-org-number-of-events
  (assoc org-base
         :datasets [(assoc base-bar-settings
                           :label "All"
                           :backgroundColor (color-vec 3 0)
                           :data [1 1 5]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 5}
         :y-axis-attr :number-of-events))

(def org-show-by-category-1
  (assoc org-base
         :datasets [(assoc base-bar-settings
                           :label (td/category-val "A" 2)
                           :backgroundColor (color-vec 3 0)
                           :data [nil nil 0]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" td/fact-1 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label (td/category-val "A" 1)
                           :backgroundColor (color-vec 3 1)
                           :data [1 1 nil]
                           :legend {:color (color 1)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" td/fact-1 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 1}
         :y-axis-attr td/fact-1))

(def org-show-by-category-1-number-of-events
  (assoc org-base
         :datasets [(assoc base-bar-settings
                           :label (td/category-val "A" 2)
                           :backgroundColor (color-vec 3 0)
                           :data [nil nil 5]
                           :legend {:color (color 0)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)
                    (assoc base-bar-settings
                           :label (td/category-val "A" 1)
                           :backgroundColor (color-vec 3 1)
                           :data [1 1 nil]
                           :legend {:color (color 1)}
                           :type "bar"
                           :y-range nil
                           :yAxisID (str "y" ":number-of-events" 0)
                           :chartIndex 0)]
         :org-y-range {:org-min-y 0
                       :org-max-y 5}
         :y-axis-attr :number-of-events))

(def empty-data-result
  {:labels []
   :x-axis-time {:unit "month"
                 :displayFormats {:year "yyyy"
                                  :month "yyyy-MM"
                                  :quarter "yyyy-MM"
                                  :day "yyyy-MM-dd"}}
   :datasets [{:barPercentage 0.5
               :barThickness 6
               :maxBarThickness 8
               :minBarLength 2
               :label "All"
               :backgroundColor ["#0067b2"]
               :data []
               :legend {:color "#0067b2"}}]
   :y-axis-attr nil})

(defn- simple-x-axis [x-axis y-axis]
  (-> (charts/datasets data {}
                       #{}
                       0
                       {:di {}
                        :type :bar
                        :x-axis x-axis
                        :y-axis y-axis
                        :y-target-access y-axis
                        :sum-by sum-by-all
                        :sum-filter sum-filter-empty})
      (second)))

(defn- simple-show-by
  ([data x-axis y-axis show-by characteristics]
   (-> (charts/datasets data {}
                        #{}
                        0
                        {:di {}
                         :type :bar
                         :x-axis x-axis
                         :y-axis y-axis
                         :y-target-access y-axis
                         :sum-by show-by
                         :sum-filter characteristics})
       (second)))
  ([x-axis y-axis show-by characteristics]
   (simple-show-by data x-axis y-axis show-by characteristics)))

(def empty-result {:datasets [] :labels [] :x-axis-time nil})

(deftest bar-chart
  (testing "bar-chart x month, y fact-1"
    (is (= bar-chart-month
           (simple-x-axis month fact-1))))
  (testing "bar-chart x month, y number-of-events"
    (is (= bar-chart-month-number-of-events
           (simple-x-axis month number-of-events))))

  (testing "bar-chart x year y fact-1"
    (is (= bar-chart-year
           (simple-x-axis year fact-1))))
  (testing "bar-chart x year, y number-of-events"
    (is (= bar-chart-year-number-of-events
           (simple-x-axis year number-of-events))))
  #_;TODO r1/tests fix this test
    (testing "bar-chart x year, y number-of-events, show-by month"
      (is (= bar-chart-year-number-of-events-show-by-month
             (simple-show-by year number-of-events month month-characteristics))))

  (testing "bar-chart x country, y fact-1"
    (is (= bar-chart-country
           (simple-x-axis country fact-1))))
  (testing "bar-chart x country, y number-of-events"
    (is (= bar-chart-country-number-of-events
           (simple-x-axis country number-of-events))))

  (testing "bar-chart x org, y fact-1"
    (is (= bar-chart-org
           (simple-x-axis org fact-1))))
  (testing "bar-chart x org, y number-of-events"
    (is (= bar-chart-org-number-of-events
           (simple-x-axis org number-of-events))))

  #_;TODO r1/tests fix this test
    (testing "org x org, y fact-1, show-by category-1"
      (is (= org-show-by-category-1
             (simple-show-by org fact-1 category-1 category-1-characteristics))))

  #_;TODO r1/tests fix this test
    (testing "org x org, y number-of-events, show-by category-1"
      (is (= org-show-by-category-1-number-of-events
             (simple-show-by org number-of-events category-1 category-1-characteristics))))

  (testing "negative-tests"
    (is (= empty-result
           (simple-show-by "xy-org" "no-fact-1" "no-show-by" #{"Monsters, Inc."})))
    (is (= empty-result
           (simple-show-by nil nil nil #{nil})))
    (is (= empty-result
           (simple-show-by [] "" "" "" #{""})))
    (is (= empty-result
           (simple-show-by [] month fact-1 sum-by-all sum-filter-empty)))))