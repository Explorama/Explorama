(ns de.explorama.backend.visualization.components.chart-types.bubble-chart-test
  (:require [clojure.test :refer :all]
            [de.explorama.shared.common.test-data :as td]
            [de.explorama.backend.visualization.components.charts.core :as charts]))

(def y-axis-fact-1
  td/fact-1)
(def x-axis-day
  "day")
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
(def r
  :same-position-metric-label)

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3", "datasource" td/datasource-a, td/org [(td/org-val 2) (td/org-val 4)], "location" [[15 15]], "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-02-19", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]], "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0}])

(def bubble-day
  {:labels ["1997-10-10" "1997-12-02" "1998-02-19" "1998-06-21" "1998-08-01" "1998-08-07"]
   :x-axis-time {:unit "day"
                 :displayFormats {:year "yyyy"
                                  :month "yyyy-MM"
                                  :quarter "yyyy-MM"
                                  :day "yyyy-MM-dd"}}
   :datasets  [{:label "All"
                :backgroundColor "#0067b27f"
                :data
                [{:x "1997-10-10", :y 0, :r 3.0901936161855166, :r-label 1}
                 {:x "1997-12-02", :y 0, :r 3.0901936161855166, :r-label 1}
                 {:x "1998-02-19", :y 0, :r 3.0901936161855166, :r-label 1}
                 {:x "1998-06-21", :y 0, :r 3.0901936161855166, :r-label 1}
                 {:x "1998-08-01", :y 1, :r 3.0901936161855166, :r-label 1}
                 {:x "1998-08-07", :y 0, :r 3.0901936161855166, :r-label 1}]
                :legend {:shape :circle, :color "#0067b2"}}]})

(def bubble-month
  {:labels ["1997-10" "1997-12" "1998-02" "1998-06" "1998-08"]
   :x-axis-time {:unit "month"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}
   :datasets [{:label "All"
               :backgroundColor "#0067b27f"
               :data [{:x "1997-10", :y 0, :r 3.0901936161855166, :r-label 1}
                      {:x "1997-12", :y 0, :r 3.0901936161855166, :r-label 1}
                      {:x "1998-02", :y 0, :r 3.0901936161855166, :r-label 1}
                      {:x "1998-06", :y 0, :r 3.0901936161855166, :r-label 1}
                      {:x "1998-08", :y 1, :r 3.0901936161855166, :r-label 1}
                      {:x "1998-08", :y 0, :r 3.0901936161855166, :r-label 1}]
               :legend {:shape :circle
                        :color "#0067b2"}}]})

(def bubble-year
  {:labels ["1997" "1998"]
   :x-axis-time {:unit "year"
                 :displayFormats {:year "yyyy", :month "yyyy-MM", :quarter "yyyy-MM", :day "yyyy-MM-dd"}}
   :datasets [{:label "All"
               :backgroundColor "#0067b27f"
               :data [{:x "1998", :y 0, :r 3.989422804014327, :r-label 3}
                      {:x "1997", :y 0, :r 3.5682482323055424, :r-label 2}
                      {:x "1998", :y 1, :r 3.0901936161855166, :r-label 1}]
               :legend {:shape :circle
                        :color "#0067b2"}}]})

(def bubble-country
  {:labels [td/country-a]
   :x-axis-time nil
   :datasets [{:label "All"
               :backgroundColor "#0067b27f"
               :data [{:x td/country-a, :y 0, :r 4.720348719413148, :r-label 5}
                      {:x td/country-a, :y 1, :r 3.0901936161855166, :r-label 1}]
               :legend {:shape :circle
                        :color "#0067b2"}}]})

(def bubble-org
  {:labels [(td/org-val 2) (td/org-val 4) (td/org-val 6)]
   :x-axis-time nil
   :datasets [{:label "All"
               :backgroundColor "#0067b27f"
               :data [{:x (td/org-val 6), :y 0, :r 4.720348719413148, :r-label 5}
                      {:x (td/org-val 2), :y 1, :r 3.0901936161855166, :r-label 1}
                      {:x (td/org-val 4), :y 1, :r 3.0901936161855166, :r-label 1}]
               :legend {:shape :circle
                        :color "#0067b2"}}]})

(def org-show-by-event-type
  {:labels [(td/org-val 2) (td/org-val 4) (td/org-val 6)]
   :x-axis-time nil
   :datasets '({:label (td/category-val "A" 2)
                :backgroundColor "#0067b27f"
                :data [{:x (td/org-val 6), :y 0, :r 4.720348719413148, :r-label 5}]
                :legend {:shape :circle, :color "#0067b2"}}
               {:label (td/category-val "A" 1)
                :backgroundColor "#f498197f"
                :data [{:x (td/org-val 2), :y 1, :r 3.0901936161855166, :r-label 1}
                       {:x (td/org-val 4), :y 1, :r 3.0901936161855166, :r-label 1}]
                :legend {:shape :circle
                         :color "#f49819"}})})

;;TODO r1/charts fix this test
#_(deftest bubble
    (testing "bubble-day"
      (is (= bubble-day
             (charts/bubble-datasets y-axis-fact-1 x-axis-day r sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "bubble-month"
      (is (= bubble-month
             (charts/bubble-datasets y-axis-fact-1 x-axis-month r sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "bubble-year"
      (is (= bubble-year
             (charts/bubble-datasets y-axis-fact-1 x-axis-year r sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "bubble-country"
      (is (= bubble-country
             (charts/bubble-datasets y-axis-fact-1 x-axis-country r sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "bubble-org"
      (is (= bubble-org
             (charts/bubble-datasets y-axis-fact-1 x-axis-org r sum-by-all sum-filter-empty data (partial charts/color {})))))
    (testing "org-show-by-event-type"
      (is (= org-show-by-event-type
             (charts/bubble-datasets y-axis-fact-1 x-axis-org r show-by characteristics data (partial charts/color {})))))
    (testing "negative-tests"
      (is (= {:labels [], :x-axis-time nil, :datasets ()}
             (charts/bubble-datasets "" "" nil "" sum-filter-empty data (partial charts/color {}))))
      (is (= {:labels [], :x-axis-time nil, :datasets ()}
             (charts/bubble-datasets "" "" nil "" sum-filter-empty [] (partial charts/color {}))))
      (is (= {:labels [], :x-axis-time nil, :datasets ()}
             (charts/bubble-datasets nil nil nil nil sum-filter-empty [] (partial charts/color {}))))
      (is (= {:labels [], :x-axis-time nil, :datasets ()}
             (charts/bubble-datasets "T-Rex" "seconds" nil "Dino" sum-filter-empty data (partial charts/color {}))))))