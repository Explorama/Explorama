(ns de.explorama.backend.charts.core-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.charts.config :as charts-config]
            [de.explorama.backend.charts.data.fetch :as data-fetch]
            [de.explorama.shared.common.test-data :as td]
            [taoensso.timbre :refer [error]]))

(def data
  [{td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "1"
    "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-12-02", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "2"
    "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1997-10-10", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 1), "id" "3"
    "datasource" td/datasource-a, td/org [(td/org-val 4) (td/org-val 2)], "location" [[15 15]]
    "annotation" "", "date" "1998-08-01", "notes" "Text", td/fact-1 1}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "4","datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-06-21", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "5"
    "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-02-19", "notes" "Text", td/fact-1 0}
   {td/country td/country-a, td/category-1 (td/category-val "A" 2), "id" "6", "datasource" td/datasource-a, td/org (td/org-val 6), "location" [[15 15]]
    "annotation" "", "date" "1998-08-07", "notes" "Text", td/fact-1 0}])

(def data-count (count data))

(use-fixtures :each (fn [tf]
                      (with-redefs [charts-config/explorama-charts-max-data-amount (inc data-count)]
                        (tf))))

#_;TODO r1/tests fix this test
(deftest too-much-data-test
  (testing "testing exception when data amount is too much"

    (try
      (data-fetch/di-data {})
      (error "Data fetch was successful, but but it shouldn't be")
      (is false)
      (catch #?(:cljs :default :clj Throwable) e
        (is (= {:error :too-much-data
                :data-count data-count
                :max-data-amount (dec data-count)}
               (ex-data e)))))))
