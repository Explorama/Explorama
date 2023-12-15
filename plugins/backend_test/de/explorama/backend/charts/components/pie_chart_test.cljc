(ns de.explorama.backend.charts.components.pie-chart-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.charts.data.base-charts :as charts]
            [de.explorama.backend.charts.data.colors :as colors]
            [de.explorama.backend.charts.data.helper :refer [remaining-group-name]]
            [de.explorama.shared.charts.util :refer [remaining-group-color]]))

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
  {:labels ["All"]
   :datasets [{:label "All"
               :data [1]
               :backgroundColor ["#0067b2"]
               :legend {:color "#0067b2"}}]})

(def show-by-organisation
  {:labels #{"org-2" "org-3" "org-1"}
   :datasets [{:label "fact-1"
               :backgroundColor ["#0067b2" "#f49819" "#41ab34"]
               :data [1 1 0]
               :legend [{:label "org-2"
                         :color "#0067b2"}
                        {:label "org-3"
                         :color "#f49819"}
                        {:label "org-1"
                         :color "#41ab34"}]}]})

(def sum-remaining-AB
  {:labels ["B" "A" remaining-group-name]
   :datasets
   [{:label "fact-1"
     :backgroundColor ["#000000" "#000000" remaining-group-color]
     :data [7 3 11]
     :legend
     [{:label "B", :color "#000000"}
      {:label "A", :color "#000000"}
      {:label remaining-group-name, :color remaining-group-color}]}]})

(def sum-remaining-C
  {:labels ["C" remaining-group-name]
   :datasets
   [{:label "fact-1"
     :backgroundColor ["#000000" remaining-group-color]
     :data [11 10]
     :legend
     [{:label "C", :color "#000000"}
      {:label remaining-group-name, :color remaining-group-color}]}]})

#_;TODO r1/tests fix this test
(deftest pie-chart-test
  (testing "fact-1-all"
    (is (= fact-1-all
           (charts/pie-datasets data (partial colors/color {}) {:y-axis y-axis-fact-1
                                                                :sum-by sum-by-all
                                                                :sum-filter sum-filter-empty
                                                                :sum-remaining? sum-remaining?}))))
  (testing "show-by-organisation"
    (is (= show-by-organisation
           (charts/pie-datasets data (partial colors/color {})
                                {:y-axis y-axis-fact-1
                                 :sum-by sum-by-organisation
                                 :sum-filter characteristics
                                 :sum-remaining? sum-remaining?}))))
  (testing "negative-tests"
    (is (= {:datasets [{:backgroundColor [], :data [], :label "", :legend []}], :labels #{}}
           (charts/pie-datasets data (partial colors/color {})
                                {:y-axis ""
                                 :sum-by ""
                                 :sum-filter sum-filter-empty
                                 :sum-remaining? sum-remaining?})))
    (is (= {:datasets [{:backgroundColor [], :data [], :label "", :legend []}], :labels #{}}
           (charts/pie-datasets [] (partial colors/color {})
                                {:y-axis ""
                                 :sum-by ""
                                 :sum-filter sum-filter-empty
                                 :sum-remaining? sum-remaining?})))
    (is (= {:labels nil
            :datasets [{:label nil, :backgroundColor [], :data [], :legend []}]}
           (charts/pie-datasets data (partial colors/color {})
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