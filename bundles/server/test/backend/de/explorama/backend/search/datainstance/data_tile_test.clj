(ns de.explorama.backend.search.datainstance.data-tile-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.search.datainstance.data-tile :refer [data-tile-reduce-formdata reduce-formdata]]))

(def test-formdata [[["datasource" "Datasource"] {:values ["Datasource A"], :timestamp 1643111305594, :valid? true}]
                    [["country" "Context"] {:values ["Country A"], :timestamp 1643111312358, :valid? true}]
                    [["year" "Date"] {:from {:value 2016, :label "2016", :i 0, :type :value}, :to {:value 2016, :label "2016", :i 0, :type :value}, :timestamp 1643114698974, :valid? true}]
                    [["city" "Context"] {:values ["City A"], :timestamp 1643114811064, :valid? true}]])

(def test-attributes {["Fact1" "Fact"] "string"
                      ["year" "Date"] "year"
                      ["month" "Date"] "month"
                      ["country" "Context"] "string"
                      ["Fact2" "Fact"] "decimal"
                      ["Fact3" "Fact"] "decimal"
                      ["notes" "Notes"] "notes"
                      ["datasource" "Datasource"] "string"
                      ["Fact4" "Fact"] "decimal"
                      ["tag" "Context"] "string"
                      ["Fact5" "Fact"] "decimal"
                      ["Fact6" "Fact"] "decimal"
                      ["org" "Context"] "string"
                      ["day" "Date"] "day"
                      ["city" "Context"] "string"
                      ["Fact7" "Fact"] "integer"
                      ["Fact8" "Fact"] "integer"})

(deftest test-reduce-formdata
  (testing "reduce formdata"
    (is (=
         {["city" "Context"] {:values ["City A"], :timestamp 1643114811064, :valid? true}}
         (reduce-formdata test-formdata)))))

(deftest test-data-tile-reduce-formdata
  (testing "data-tile reduce-formdata"
    (is (=
         {["datasource" "Datasource"] {:values ["Datasource A"]},
          ["country" "Context"] {:values ["Country A"]},
          ["year" "Date"] {:from {:value 2016}, :to {:value 2016}},
          ["city" "Context"] {:values ["City A"]}}
         (data-tile-reduce-formdata
          test-attributes
          test-formdata)))))
