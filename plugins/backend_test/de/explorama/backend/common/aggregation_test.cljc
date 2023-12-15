(ns de.explorama.backend.common.aggregation-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.common.aggregation :as agg]
            [de.explorama.shared.common.test-data :as td]))

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1"
    "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0 "bucket" "default"}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2"
    "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0 "bucket" "default"}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3"
    "datasource" td/datasource-a, td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
    "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1 "bucket" "default"}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4","datasource"
    td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0 "bucket" "default"}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5"
    "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-02-19", "notes" "Text", td/fact-1 0 "bucket" "default"}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource"
    td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0 "bucket" "default"}])

(def ^:private stringify-years #'agg/stringify-years)

(deftest stringify-years-test
  (testing "stringify-years"
    (is (= "1997-1998, 2000, 2010"
           (stringify-years [1997 1998 2000 2010])))
    (is (= "1997-2001"
           (stringify-years [1997 1998 1999 2000 2001])))
    (is (= "1996, 1998, 2000, 2010"
           (stringify-years [1996 1998 2000 2010])))
    (is (= "2022"
           (stringify-years [2022])))
    (is (= ""
           (stringify-years [])))
    (is (= ""
           (stringify-years [nil])))
    #_(is (= "0010-0011" ;got "8-9"
             (stringify-years [0010 0011])))
    (is (= "10-11"
           (stringify-years [10 11])))
    #_(is (= "0-5, 11, 2022" ;got "1-5, 11, 2022"
             (stringify-years [0 1 2 3 4 5 11 2022])))))

(def ^:private stringify-countries #'agg/stringify-countries)

(deftest stringify-countries-test
  (testing "stringify-countries"
    (is (= "Country A, Country C, Country D"
           (stringify-countries [td/country-a td/country-c td/country-d])))
    (is (= td/country-a
           (stringify-countries [td/country-a])))
    (is (= ""
           (stringify-countries [])))
    (is (= ""
           (stringify-countries [nil])))))

(def ^:private stringify-datasource #'agg/stringify-datasource)

(deftest stringify-datasource-test
  (testing "stringify-datasource"
    (is (= "Datasource A, Datasource B, Datasource D, Datasource E"
           (stringify-datasource [td/datasource-a td/datasource-b td/datasource-d td/datasource-e])))
    (is (= td/datasource-d
           (stringify-datasource [td/datasource-d])))
    (is (= ""
           (stringify-datasource [])))
    (is (= ""
           (stringify-datasource [nil])))))

(def ^:private calculate-dimensions #'agg/calculate-dimensions)

(deftest calculate-dimensions-test
  (testing "calculate-dimensions"
    (is (= {:datasources td/datasource-a,
            :years "1997-1998",
            :countries td/country-a,
            :buckets ["default"]
            :dim-info {:datasources [td/datasource-a],
                       :buckets ["default"]
                       :years [1997 1998],
                       :countries [td/country-a]}}
           (calculate-dimensions data)))))