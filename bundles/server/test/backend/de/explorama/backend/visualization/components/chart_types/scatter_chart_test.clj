(ns de.explorama.backend.visualization.components.chart-types.scatter-chart-test
  (:require [clojure.test :refer :all]
            [de.explorama.shared.common.test-data :as td]
            [de.explorama.backend.visualization.components.charts.core :as charts]))

(def y-axis-fact-1
  td/fact-1)
(def x-axis-month
  "month")
(def x-axis-year
  "year")
(def x-axis-org
  td/org)
(def x-axis-country
  td/country)
(def sum-by-all
  "all")
(def sum-filter-empty
  #{})
(def show-by
  td/category-1)

(def characteristics
  #{(td/category-val "A" 1) (td/category-val "A" 2)})

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3", "datasource" td/datasource-a, td/org [(td/org-val 2) (td/org-val 4)], "location" [[15 15]], "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-02-19", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0}])

(def scatter-month
  {:labels ["1997-10" "1997-12" "1998-02" "1998-06" "1998-08"]
   :x-axis-time {:unit "month"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}
   :datasets [{:label "All"
               :backgroundColor ["rgb(0,103,178,1.0)" "rgb(0,103,178,1.0)" "rgb(0,103,178,1.0)"
                                 "rgb(0,103,178,1.0)" "rgb(0,103,178,1.0)" "rgb(0,103,178,1.0)"]
               :data [{:x "1997-10", :y 0, :freq 1}
                      {:x "1997-12", :y 0, :freq 1}
                      {:x "1998-02", :y 0, :freq 1}
                      {:x "1998-06", :y 0, :freq 1}
                      {:x "1998-08", :y 1, :freq 1}
                      {:x "1998-08", :y 0, :freq 1}]
               :legend {:shape :circle, :color "#0067b2"}}]})

(def scatter-year
  {:labels ["1997" "1998"]
   :x-axis-time {:unit "year"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}
   :datasets [{:label "All"
               :backgroundColor ["rgb(0,103,178,0.75)" "rgb(0,103,178,0.5)" "rgb(0,103,178,1.0)"]
               :data [{:x "1997", :y 0, :freq 2} {:x "1998", :y 1, :freq 1} {:x "1998", :y 0, :freq 3}]
               :legend {:shape :circle, :color "#0067b2"}}]})

(def scatter-country
  {:labels [td/country-a]
   :x-axis-time nil
   :datasets [{:label "All"
               :backgroundColor ["rgb(0,103,178,1.0)" "rgb(0,103,178,0.5)"]
               :data [{:x td/country-a, :y 0, :freq 5}
                      {:x td/country-a, :y 1, :freq 1}]
               :legend {:shape :circle, :color "#0067b2"}}]})

(def org-show-by-event-type
  {:labels [(td/org-val 2) (td/org-val 4) (td/org-val 6)]
   :x-axis-time nil
   :datasets '({:label (td/category-val "A" 2)
                :backgroundColor ["rgb(0,103,178,1.0)"]
                :data [{:x (td/org-val 6), :y 0, :freq 5}]
                :legend {:shape :circle, :color "#0067b2"}}
               {:label (td/category-val "A" 1)
                :backgroundColor ["rgb(244,152,25,1.0)" "rgb(244,152,25,1.0)"]
                :data [{:x (td/org-val 2) :y 1, :freq 1}
                       {:x (td/org-val 4), :y 1, :freq 1}]
                :legend {:shape :circle, :color "#f49819"}})})

(def empty-data-result
  {:datasets [{:backgroundColor ["rgb(0,103,178,1.0)"]
               :data [{:freq 1, :x nil, :y nil}]
               :label "All"
               :legend {:color "#0067b2", :shape :circle}}]
   :labels [nil]
   :x-axis-time {:displayFormats {:day "yyyy-MM-dd", :month "yyyy-MM", :quarter "yyyy-MM", :year "yyyy"}
                 :unit "month"}})

;;TODO r1/charts fix this test
#_(deftest scatter-chart
    (testing "scatter-month"
      (is (= scatter-month
             (charts/scatter-datasets y-axis-fact-1 x-axis-month sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "scatter-chart-year"
      (is (= scatter-year
             (charts/scatter-datasets y-axis-fact-1 x-axis-year sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "scatter-chart-country"
      (is (= scatter-country
             (charts/scatter-datasets y-axis-fact-1 x-axis-country sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "org-show-by-event-type"
      (is (= org-show-by-event-type
             (charts/scatter-datasets y-axis-fact-1 x-axis-org show-by characteristics data (partial charts/color {})))))
    (testing "negative-tests"
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/scatter-datasets "no-fact-1" "xy-org" "no-show-by" #{"Monsters, Inc."} data (partial charts/color {}))))
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/scatter-datasets "" "" "" #{""} data (partial charts/color {}))))
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/scatter-datasets "" "" "" #{""} [] (partial charts/color {}))))
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/scatter-datasets nil nil nil #{nil} [] (partial charts/color {}))))
      (is (= empty-data-result
             (charts/scatter-datasets y-axis-fact-1 x-axis-month sum-by-all sum-filter-empty [nil] (partial charts/color {}))))))