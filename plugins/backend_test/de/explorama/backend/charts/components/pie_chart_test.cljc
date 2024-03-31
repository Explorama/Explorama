(ns de.explorama.backend.charts.components.pie-chart-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.charts.data.base-charts :as charts]
            [de.explorama.backend.charts.data.colors :as colors]
            [de.explorama.backend.charts.data.helper :refer [remaining-group-name]]))

(def y-axis-fact-1
  "fact-1")
(def sum-by-all
  "all")
(def sum-by-organisation
  "organisation")
(def sum-filter-empty
  #{})

(def sum-remaining?
  false)
(def characteristics
  #{"org-2" "org-3" "org-1"})

(def data
  [{"country" "country-1", "category" "category-1", "id" "1", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1997-12-02", "notes" "notes-1", "fact-1" 0}
   {"country" "country-1", "category" "category-1", "id" "2", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1997-10-10", "notes" "notes-1", "fact-1" 0}
   {"country" "country-1", "category" "category-2", "id" "3", "datasource" "DS-A", "organisation" ["org-2" "org-3"], "location" [[6.35 2.4333]], "annotation" "", "date" "1998-08-01", "notes" "notes-2", "fact-1" 1}
   {"country" "country-1", "category" "category-1", "id" "4", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1998-06-21", "notes" "notes-3", "fact-1" 0}
   {"country" "country-1", "category" "category-1", "id" "5", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1998-02-19", "notes" "notes-4", "fact-1" 0}
   {"country" "country-1", "category" "category-1", "id" "6", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1998-08-07", "notes" "notes-5", "fact-1" 0}])

(def data2
  [{"country" "A", "category" "category-1", "id" "1", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1997-12-02", "notes" "notes-1", "fact-1" 1}
   {"country" "A", "category" "category-1", "id" "2", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1997-10-10", "notes" "notes-1", "fact-1" 2}
   {"country" "B", "category" "category-2", "id" "3", "datasource" "DS-A", "organisation" ["org-2" "org-3"], "location" [[6.35 2.4333]], "annotation" "", "date" "1998-08-01", "notes" "notes-2", "fact-1" 3}
   {"country" "B", "category" "category-1", "id" "4", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1998-06-21", "notes" "notes-3", "fact-1" 4}
   {"country" "C", "category" "category-1", "id" "5", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1998-02-19", "notes" "notes-4", "fact-1" 5}
   {"country" "C", "category" "category-1", "id" "6", "datasource" "DS-A", "organisation" "org-1", "location" [[6.35 2.4333]], "annotation" "", "date" "1998-08-07", "notes" "notes-5", "fact-1" 6}])

(def fact-1-all
  {:y-axis-attr "fact-1",
   :labels ["All"],
   :x-axis-time nil,
   :datasets [{:type "pie",
               :label "fact-1",
               :backgroundColor ["rgba(238,102,119,0.8)"],
               :data [1],
               :legend [{:label "All", :color "rgba(238,102,119,0.8)"}]}],
   :org-y-range nil})

(def show-by-organisation
  {:y-axis-attr "fact-1",
   :labels ["org-1" "org-2" "org-3"],
   :x-axis-time nil,
   :org-y-range nil,
   :datasets [{:type "pie",
               :label "fact-1",
               :backgroundColor ["rgba(238,102,119,0.8)"
                                 "rgba(238,102,119,0.8)"
                                 "rgba(200,45,136,0.8)"
                                 "rgba(200,45,136,0.8)"
                                 "rgba(188,207,0,0.8)"
                                 "rgba(188,207,0,0.8)"],
               :data [0 1 0 1 0 1],
               :legend [{:label "org-1", :color "rgba(238,102,119,0.8)"}
                        {:label "org-1", :color "rgba(238,102,119,0.8)"}
                        {:label "org-2", :color "rgba(200,45,136,0.8)"}
                        {:label "org-2", :color "rgba(200,45,136,0.8)"}
                        {:label "org-3", :color "rgba(188,207,0,0.8)"}
                        {:label "org-3", :color "rgba(188,207,0,0.8)"}]}]})

(def sum-remaining-AB
  {:y-axis-attr "fact-1",
   :labels ["A" "B" remaining-group-name],
   :x-axis-time nil,
   :org-y-range nil,
   :datasets [{:type "pie",
               :label "fact-1",
               :backgroundColor ["#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"],
               :data [3 7 11 3 7 11 nil],
               :legend [{:label "A", :color "#000000"}
                        {:label "A", :color "#000000"}
                        {:label "A", :color "#000000"}
                        {:label "B", :color "#000000"}
                        {:label "B", :color "#000000"}
                        {:label "B", :color "#000000"}
                        {:label remaining-group-name, :color "#000000"}]}]})

(def sum-remaining-C
  {:y-axis-attr "fact-1",
   :labels ["A" "B" remaining-group-name],
   :x-axis-time nil,
   :org-y-range nil,
   :datasets [{:type "pie",
               :label "fact-1",
               :backgroundColor ["#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"
                                 "#000000"],
               :data [3 7 11 3 7 11 nil],
               :legend [{:label "A", :color "#000000"}
                        {:label "A", :color "#000000"}
                        {:label "A", :color "#000000"}
                        {:label "B", :color "#000000"}
                        {:label "B", :color "#000000"}
                        {:label "B", :color "#000000"}
                        {:label remaining-group-name, :color "#000000"}]}]})

(deftest pie-chart-test
  (testing "fact-1-all"
    (is (= fact-1-all
           (charts/pie-datasets data (partial colors/color {} "pie-test") {:y-axis y-axis-fact-1
                                                                :sum-by sum-by-all
                                                                :sum-filter sum-filter-empty
                                                                :sum-remaining? sum-remaining?}))))
  (testing "show-by-organisation"
    (is (= show-by-organisation
           (charts/pie-datasets data (partial colors/color {} "pie-test")
                                {:y-axis y-axis-fact-1
                                 :sum-by sum-by-organisation
                                 :sum-filter characteristics
                                 :sum-remaining? sum-remaining?}))))
  (testing "negative-tests"
    (is (= {:labels [], :x-axis-time nil, :datasets []}
           (charts/pie-datasets data (partial colors/color {} "pie-test")
                                {:y-axis ""
                                 :sum-by ""
                                 :sum-filter sum-filter-empty
                                 :sum-remaining? sum-remaining?})))
    (is (= {:labels [], :x-axis-time nil, :datasets []}
           (charts/pie-datasets [] (partial colors/color {} "pie-test")
                                {:y-axis ""
                                 :sum-by ""
                                 :sum-filter sum-filter-empty
                                 :sum-remaining? sum-remaining?})))
    (is (= {:labels [], :x-axis-time nil, :datasets []}
           (charts/pie-datasets data (partial colors/color {} "pie-test")
                                {:y-axis nil
                                 :sum-by nil
                                 :sum-filter nil
                                 :sum-remaining? sum-remaining?}))))
  (testing "summarize remaining groups"
    (is (= sum-remaining-AB
           (charts/pie-datasets data2 (constantly "#000000")
                                {:y-axis y-axis-fact-1
                                 :sum-by "country"
                                 :sum-filter #{"A" "B"}
                                 :sum-remaining? true})))
    (is (= sum-remaining-C
           (charts/pie-datasets
            data2 (constantly "#000000")
            {:y-axis y-axis-fact-1
             :sum-by "country"
             :sum-filter #{"A" "B"}
             :sum-remaining? true})))))