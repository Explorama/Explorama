(ns de.explorama.backend.table.table-test
  (:require [clojure.test :refer [deftest is]]
            [de.explorama.backend.table.data.table :as table]))

(deftest test-filter-search-data
  (let [filter-spec [{:field "field1" :value "val1"}
                     {:field "field2" :value "val2"}
                     {:field "missing"}]
        matching-record {"field1" "Xval1Y", "field2" "vaL2|"
                         "x" "er66bg6"}
        non-matching-record1 {"field1" "Xval1Y", "field2" "val3"
                              "x" "er66bg6"}
        non-matching-record2 {"field2" "val2|"
                              "y" "e!!r66bg6"}]
    (is (empty?
         (table/filter-search-data filter-spec
                                   [])))
    (is (= [matching-record]
           (table/filter-search-data filter-spec
                                     [non-matching-record1
                                      matching-record
                                      non-matching-record2])))))

(deftest test-sort-pure
  (is (= [{"not" "sorted2"}
          {"not" "sorted1"}
          {"field1" "v21", "field11" "v111", "field2" "v22"}
          {"field1" "v11", "field11" "v111", "field2" "v12"}
          {"field11" "v311", "field2" "v32"}
          {"field1" "v41", "field11" "v411", "field2" "v42"}]
         (table/sort-pure [{"field1" "v11", "field2" "v12", "field11" "v111"}
                           {"not" "sorted2"}
                           {"not" "sorted1"}
                           {"field1" "v21", "field2" "v22", "field11" "v111"}
                           {"field2" "v32", "field11" "v311"}
                           {"field1" "v41", "field2" "v42", "field11" "v411"}]
                          [{:attr "field11" :direction "asc"}
                           {:attr "field2" :direction "desc"}
                           {:attr "field1" :direction "asc"}]))))