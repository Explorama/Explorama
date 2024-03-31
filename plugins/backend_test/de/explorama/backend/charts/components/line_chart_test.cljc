(ns de.explorama.backend.charts.components.line-chart-test
  (:require #_[clojure.test :refer [deftest is testing]]
            [de.explorama.shared.common.test-data :as td]))

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
(def x-axis-datasource
  "datasource")
(def sum-by-all
  "all")
(def sum-by-country
  td/country)
(def sum-filter-empty
  #{})
(def sum-filter-country
  #{td/country-a})

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3", "datasource" td/datasource-a, td/org [(td/org-val 2) (td/org-val 4)], "location" [[15 15]], "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-02-19", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0}])

(def line-chart-year
  {:labels ["1997" "1998"]
   :x-axis-time {:unit "year"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}
   :datasets [{:label "All"
               :borderColor "#0067b2"
               :data '({:x "1997", :y 0} {:x "1998", :y 1})
               :legend {:shape :line, :color "#0067b2"}}]})

(def line-chart-month
  {:labels ["1997-10" "1997-12" "1998-02" "1998-06" "1998-08"]
   :x-axis-time {:unit "month"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}
   :datasets [{:label "All"
               :borderColor "#0067b2"
               :data '({:x "1997-10", :y 0} {:x "1997-12", :y 0} {:x "1998-02", :y 0}
                                            {:x "1998-06", :y 0} {:x "1998-08", :y 1})
               :legend {:shape :line, :color "#0067b2"}}]})

(def line-chart-country
  {:labels [td/country-a]
   :x-axis-time nil
   :datasets [{:label "All"
               :borderColor "#0067b2"
               :data '({:x td/country-a, :y 1})
               :legend {:shape :line
                        :color "#0067b2"}}]})

(def line-chart-org
  {:labels [(td/org-val 2) (td/org-val 4) (td/org-val 6)]
   :x-axis-time nil
   :datasets [{:label "All"
               :borderColor "#0067b2"
               :data '({:x (td/org-val 2), :y 1}
                       {:x (td/org-val 4), :y 1}
                       {:x (td/org-val 6), :y 0})
               :legend {:shape :line
                        :color "#0067b2"}}]})

(def line-chart-datasource
  {:labels [td/datasource-a]
   :x-axis-time nil
   :datasets [{:label "All"
               :borderColor "#0067b2"
               :data '({:x td/datasource-a, :y 1})
               :legend {:shape :line, :color "#0067b2"}}]})

(def org-show-by-country
  {:labels [(td/org-val 2) (td/org-val 4) (td/org-val 6)]
   :x-axis-time nil
   :datasets '({:label td/country-a, :borderColor "#0067b2"
                :data ({:x (td/org-val 2), :y 1}
                       {:x (td/org-val 4), :y 1}
                       {:x (td/org-val 6), :y 0})
                :legend {:shape :line, :color "#0067b2"}})})

(def empty-data-result
  {:labels []
   :x-axis-time
   {:unit "month"
    :displayFormats
    {:year "yyyy"
     :month "yyyy-MM"
     :quarter "yyyy-MM"
     :day "yyyy-MM-dd"}}
   :datasets
   [{:label "All"
     :borderColor "#0067b2"
     :data '()
     :legend {:shape :line, :color "#0067b2"}}]})

;;TODO r1/tests fix this test
#_(deftest line-chart
    (testing "monthly-line-chart"
      (is (= line-chart-month
             (charts/line-datasets y-axis-fact-1 x-axis-month sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "year"
      (is (= line-chart-year
             (charts/line-datasets y-axis-fact-1 x-axis-year sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing td/country
      (is (= line-chart-country
             (charts/line-datasets y-axis-fact-1 x-axis-country sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing td/org
      (is (= line-chart-org
             (charts/line-datasets y-axis-fact-1 x-axis-org sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "datasource"
      (is (= line-chart-datasource
             (charts/line-datasets y-axis-fact-1 x-axis-datasource sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "org-show-by-country"
      (is (= org-show-by-country
             (charts/line-datasets y-axis-fact-1 x-axis-org sum-by-country sum-filter-country data (partial charts/color {})))))

    (testing "negative-tests"
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/line-datasets "no-fact-1" "xy-org" "no-show-by" #{"Monsters, Inc."} data (partial charts/color {}))))
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/line-datasets "" "" "" #{""} data (partial charts/color {}))))
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/line-datasets "" "" "" #{""} [] (partial charts/color {}))))
      (is (= {:datasets (), :labels [], :x-axis-time nil}
             (charts/line-datasets nil nil nil #{nil} [] (partial charts/color {}))))
      (is (= empty-data-result
             (charts/line-datasets y-axis-fact-1 x-axis-month sum-by-all sum-filter-empty [] (partial charts/color {}))))))