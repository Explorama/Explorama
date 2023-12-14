(ns de.explorama.backend.expdb.ac-api-test
  (:require [clojure.test :as t :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.api :as sac-api]
            [de.explorama.backend.expdb.legacy.search.data-tile :as data-tile]
            [de.explorama.backend.expdb.middleware.indexed-db-test :refer [test-setup]]))

(def ^:private dummy-data-1 {:contexts [{:name "country1"
                                         :global-id "c1"
                                         :type "country"}
                                        {:name "country2"
                                         :global-id "c2"
                                         :type "country"}
                                        {:name "org1"
                                         :global-id "o1"
                                         :type "org"}
                                        {:name "org2"
                                         :global-id "o2"
                                         :type "org"}
                                        {:name "org3"
                                         :global-id "o3"
                                         :type "org"}
                                        {:name "org4"
                                         :global-id "o4"
                                         :type "org"}
                                        {:name "type1"
                                         :global-id "t1"
                                         :type "type"}]
                             :datasource {:name "dsn-1"
                                          :global-id "ds1"}
                             :items [{:global-id "i1"
                                      :features [{:global-id "if1"
                                                  :facts [{:name "fact1"
                                                           :type "integer"
                                                           :value 1}
                                                          {:name "fact2"
                                                           :type "decimal"
                                                           :value 2.0}]
                                                  :locations [{:lat 15
                                                               :lon 15}]
                                                  :context-refs [{:global-id "c1"}
                                                                 {:global-id "o1"}
                                                                 {:global-id "o2"}
                                                                 {:global-id "t1"}]
                                                  :dates [{:type "occured-at"
                                                           :value "1997-01-02"}]
                                                  :texts ["test text"]}]}
                                     {:global-id "i2"
                                      :features [{:global-id "if2"
                                                  :facts [{:name "fact1"
                                                           :type "integer"
                                                           :value 3}
                                                          {:name "fact2"
                                                           :type "decimal"
                                                           :value 4.0}]
                                                  :locations [{:lat 15
                                                               :lon 15}]
                                                  :context-refs [{:global-id "c2"}
                                                                 {:global-id "o3"}
                                                                 {:global-id "o4"}
                                                                 {:global-id "t1"}]
                                                  :dates [{:type "occured-at"
                                                           :value "1998-01-05"}]
                                                  :texts ["test text"]}]}]})

(def ^:private dummy-data-2 {:contexts [{:name "country2"
                                         :global-id "c2"
                                         :type "country"}
                                        {:name "country3"
                                         :global-id "c3"
                                         :type "country"}
                                        {:name "org5"
                                         :global-id "o5"
                                         :type "org"}
                                        {:name "org6"
                                         :global-id "o6"
                                         :type "org"}
                                        {:name "org7"
                                         :global-id "o7"
                                         :type "org"}
                                        {:name "org8"
                                         :global-id "o8"
                                         :type "org"}
                                        {:name "type2"
                                         :global-id "t2"
                                         :type "type"}]
                             :datasource {:name "dsn-2"
                                          :global-id "ds2"}
                             :items [{:global-id "i3"
                                      :features [{:global-id "if3"
                                                  :facts [{:name "fact3"
                                                           :type "integer"
                                                           :value 1}
                                                          {:name "fact2"
                                                           :type "decimal"
                                                           :value 2.0}]
                                                  :locations [{:lat 15
                                                               :lon 15}]
                                                  :context-refs [{:global-id "c3"}
                                                                 {:global-id "o5"}
                                                                 {:global-id "o6"}
                                                                 {:global-id "t2"}]
                                                  :dates [{:type "occured-at"
                                                           :value "1996-01-02"}]
                                                  :texts ["test text"]}]}
                                     {:global-id "i4"
                                      :features [{:global-id "if4"
                                                  :facts [{:name "fact3"
                                                           :type "integer"
                                                           :value 3}
                                                          {:name "fact2"
                                                           :type "decimal"
                                                           :value 4.0}]
                                                  :locations [{:lat 15
                                                               :lon 15}]
                                                  :context-refs [{:global-id "c2"}
                                                                 {:global-id "o7"}
                                                                 {:global-id "o8"}
                                                                 {:global-id "t2"}]
                                                  :dates [{:type "occured-at"
                                                           :value "1998-01-05"}]
                                                  :texts ["test text"]}]}]})

(use-fixtures :each (partial test-setup [dummy-data-1 dummy-data-2]))

(def attributes-dsn-1-c1
  [[["datasource" "Datasource"]
    {:values ["dsn-1"], :timestamp 1634653963745, :valid? true}]
   [["country" "Context"] {:values ["country1"], :timestamp 1634653965131, :valid? true}]
   [["year" "Date"] {:values ["1997"]}]])

(def attributes-dsn-1-c-1-2-years
  [[["datasource" "Datasource"]
    {:values ["dsn-1"], :timestamp 1634653963745, :valid? true}]
   [["country" "Context"] {:values ["country1" "country2"], :timestamp 1634653965131, :valid? true}]
   [["year" "Date"] {:from {:value 1997 :lable "1997"} :to {:value 1998 :lable "1998"}}]])

(def attributes-dsn-1-2-c2
  [[["datasource" "Datasource"]
    {:values ["dsn-1" "dsn-2"], :timestamp 1634653963745, :valid? true}]
   [["country" "Context"] {:values ["country2"], :timestamp 1634653965131, :valid? true}]])

(def attributes-dsn-1-2
  [[["datasource" "Datasource"]
    {:values ["dsn-1" "dsn-2"], :timestamp 1634653963745, :valid? true}]])

(def attributes-dsn-2
  [[["datasource" "Datasource"]
    {:values ["dsn-2"], :timestamp 1634653963745, :valid? true}]])

(def datasource-dsn-1
  [[["datasource" "Datasource"]
    {:values ["dsn-1"], :timestamp 1634653963745, :valid? true}]])

(def attributes-empty-r #{["month" "Date"]
                          ["notes" "Notes"]
                          ["datasource" "Datasource"]
                          ["type" "Context"]
                          ["day" "Date"]
                          ["fact1" "Fact"]
                          ["fact2" "Fact"]
                          ["fact3" "Fact"]
                          ["org" "Context"]
                          ["year" "Date"]
                          ["location" "Context"]
                          ["country" "Context"]})
(def attributes-dsn-1-r #{["month" "Date"]
                          ["notes" "Notes"]
                          ["datasource" "Datasource"]
                          ["type" "Context"]
                          ["day" "Date"]
                          ["fact1" "Fact"]
                          ["fact2" "Fact"]
                          ["org" "Context"]
                          ["year" "Date"]
                          ["location" "Context"]
                          ["country" "Context"]})
(def attributes-dsn-2-r #{["month" "Date"]
                          ["notes" "Notes"]
                          ["datasource" "Datasource"]
                          ["type" "Context"]
                          ["day" "Date"]
                          ["fact3" "Fact"]
                          ["fact2" "Fact"]
                          ["org" "Context"]
                          ["year" "Date"]
                          ["location" "Context"]
                          ["country" "Context"]})
(def attributes-dsn-1-2-c2-r #{["month" "Date"]
                               ["notes" "Notes"]
                               ["datasource" "Datasource"]
                               ["type" "Context"]
                               ["day" "Date"]
                               ["fact1" "Fact"]
                               ["fact2" "Fact"]
                               ["fact3" "Fact"]
                               ["org" "Context"]
                               ["year" "Date"]
                               ["location" "Context"]
                               ["country" "Context"]})

(def attributes-dsn-1-allowed
  #{["Context" "org" "org4"]
    ["Context" "org" "org3"]
    ["Context" "type" "type1"]
    ["Context" "country" "country1"]
    ["Context" "location" "location"]
    ["Context" "org" "org2"]
    ["Context" "org" "org1"]
    ["Context" "country" "country2"]})

(def attributes-dsn-1-context-sort-by
  [["Context" "country" "country1"]
   ["Context" "country" "country2"]
   ["Context" "location" "location"]
   ["Context" "org" "org1"]])

(deftest attributes-test
  (testing "Attributes test for empty formdata"
    (is (= attributes-empty-r
           (set (sac-api/attributes {:formdata []})))))
  (testing "Attributes test for datasource = dsn-1 + dsn-2"
    (is (= attributes-empty-r
           (set (sac-api/attributes {:formdata attributes-dsn-1-2})))))
  (testing "Attributes test for dsn-1 with or countries"
    (is (= attributes-dsn-1-r
           (set (sac-api/attributes {:formdata attributes-dsn-1-c1})))))
  (testing "Attributes test for dsn-1 with one country"
    (is (= attributes-dsn-1-r
           (set (sac-api/attributes {:formdata attributes-dsn-1-c-1-2-years})))))
  (testing "Attributes test for dsn-1 + dsn-2 and fact-2"
    (is (= attributes-dsn-1-2-c2-r
           (set (sac-api/attributes {:formdata attributes-dsn-1-2-c2})))))
  (testing "Attributes test for dsn-2 with only datasource"
    (is (= attributes-dsn-2-r
           (set (sac-api/attributes {:formdata attributes-dsn-2})))))
  (testing "Attributes test for dsn-1 with only datasource and allowed-types"
    (is (= attributes-dsn-1-allowed
           (set (sac-api/attributes {:formdata datasource-dsn-1
                                     :allowed-types #{"Context"}})))))
  (testing "Attributes test for dsn-1 with only datasource, allowed-type and sort-by"
    (is (= attributes-dsn-1-context-sort-by
           (sac-api/attributes {:formdata datasource-dsn-1
                                :allowed-types #{"Context"}
                                :sort-by {:limit 4
                                          :order :asc
                                          :by :value}})))))

(deftest attribute-values-test
  (testing "Attributes values for datasource with empty formdata"
    (is (= #{"dsn-1" "dsn-2"}
           (-> (sac-api/attribute-values {:formdata []
                                          :attributes [["datasource" "Datasource"]]})
               (get ["datasource" "Datasource"])
               set))))
  (testing "Attributes values for datasource with empty formdata"
    (is (= #{"dsn-1" "dsn-2"}
           (-> (sac-api/attribute-values {:formdata datasource-dsn-1
                                          :attributes [["datasource" "Datasource"]]})
               (get ["datasource" "Datasource"])
               set))))
  (testing "Attributes values for country with datasource dsn-1 + dsn-2"
    (is (= #{"country2" "country1" "country3"}
           (-> (sac-api/attribute-values {:formdata attributes-dsn-1-2
                                          :attributes [["country" "Context"]]})
               (get ["country" "Context"])
               set))))
  (testing "Attributes values for org with datasource dsn-1 + dsn-2, country country1"
    (is (= #{"org3" "org4" "org7" "org8"}
           (-> (sac-api/attribute-values {:formdata attributes-dsn-1-2-c2
                                          :attributes [["org" "Context"]]})
               (get ["org" "Context"])
               set)))
    (testing "Attributes values for org with empty formdata"
      (is (= #{"org1" "org2" "org3" "org4" "org5" "org6" "org7" "org8"}
             (-> (sac-api/attribute-values {:formdata []
                                            :attributes [["org" "Context"]]})
                 (get ["org" "Context"])
                 set))))
    (testing "Attributes values for org with empty formdata and sort-by merge"
      (is (= [["org" "Context" "org1"]
              ["org" "Context" "org2"]
              ["org" "Context" "org3"]
              ["org" "Context" "org4"]
              ["org" "Context" "org5"]]
             (-> (sac-api/attribute-values {:formdata []
                                            :attributes [["org" "Context"]]
                                            :sort-by {:limit 5
                                                      :order :asc
                                                      :by :alpha
                                                      :mode :merge}})
                 (get ["sort-by" "Aggregation"])))))
    (testing "Attributes values for org with empty formdata and sort-by each"
      (is (= ["org1" "org2" "org3" "org4" "org5"]
             (-> (sac-api/attribute-values {:formdata []
                                            :attributes [["org" "Context"]]
                                            :sort-by {:limit 5
                                                      :order :asc
                                                      :by :alpha}})
                 (get ["org" "Context"])))))))

(def data-tiles-all
  #{{"country" "country2", "year" "1998", "datasource" "dsn-2", "bucket" "default", "identifier" "search"}
    {"country" "country3", "year" "1996", "datasource" "dsn-2", "bucket" "default", "identifier" "search"}
    {"country" "country1", "year" "1997", "datasource" "dsn-1", "bucket" "default", "identifier" "search"}
    {"country" "country2", "year" "1998", "datasource" "dsn-1", "bucket" "default", "identifier" "search"}})

(def data-tiles-dsn-2
  #{{"country" "country2", "year" "1998", "datasource" "dsn-2", "bucket" "default", "identifier" "search"}
    {"country" "country3", "year" "1996", "datasource" "dsn-2", "bucket" "default", "identifier" "search"}})

(deftest data-tiles-test
  (testing "Data tile test for empty formdata"
    (is (= data-tiles-all
           (set (data-tile/get-data-tiles [])))))
  (testing "Data tile test for datasource = dsn-1 + dsn-2"
    (is (=  data-tiles-all
            (set (data-tile/get-data-tiles attributes-dsn-1-2)))))
  (testing "Data tile test for dsn-1 with or countries"
    (is (=  #{{"country" "country1", "year" "1997", "datasource" "dsn-1", "bucket" "default", "identifier" "search"}}
            (set (data-tile/get-data-tiles attributes-dsn-1-c1)))))
  (testing "Data tile test for dsn-1 with one country"
    (is (=  #{{"country" "country2", "year" "1998", "datasource" "dsn-1", "bucket" "default", "identifier" "search"}
              {"country" "country1", "year" "1997", "datasource" "dsn-1", "bucket" "default", "identifier" "search"}}
            (set (data-tile/get-data-tiles attributes-dsn-1-c-1-2-years)))))
  (testing "Data tile test for dsn-1 + dsn-2 and fact-2"
    (is (=  #{{"country" "country2", "year" "1998", "datasource" "dsn-2", "bucket" "default", "identifier" "search"}
              {"country" "country2", "year" "1998", "datasource" "dsn-1", "bucket" "default", "identifier" "search"}}
            (set (data-tile/get-data-tiles attributes-dsn-1-2-c2)))))
  (testing "Data tile test for dsn-2 with only datasource"
    (is (= data-tiles-dsn-2
           (set (data-tile/get-data-tiles attributes-dsn-2))))))

(def query-all-attr-name {:nodes []
                          :pattern [true true]})

(def query-all-attr-name-result
  [["Fact" "fact2"]
   ["Fact" "fact3"]
   ["Context" "type"]
   ["Notes" "notes"]
   ["Context" "country"]
   ["Date" "day"]
   ["Fact" "fact1"]
   ["Date" "month"]
   ["Date" "year"]
   ["Context" "org"]
   ["Context" "location"]
   ["Datasource" "datasource"]])

(deftest neighborhood-test
  (testing "Testing neighborhood with two nodes"
    (is (= #{["Fact" "fact2"]
             ["Context" "type"]
             ["Notes" "notes"]
             ["Context" "country"]
             ["Date" "day"]
             ["Date" "month"]
             ["Date" "year"]
             ["Context" "org"]
             ["Context" "location"]}
           (set (sac-api/neighborhood {:nodes [["Datasource" "datasource" "dsn-1"]
                                               ["Datasource" "datasource" "dsn-2"]]
                                       :pattern [true true]})))))
  (testing "Testing neighborhood with one nodes"
    (is (= #{["Fact" "fact2"]
             ["Context" "type"]
             ["Notes" "notes"]
             ["Context" "country"]
             ["Date" "day"]
             ["Fact" "fact1"]
             ["Date" "month"]
             ["Date" "year"]
             ["Context" "org"]
             ["Context" "location"]}
           (set (sac-api/neighborhood {:nodes [["Datasource" "datasource" "dsn-1"]]
                                       :pattern [true true]})))))
  (testing "Testing neighborhood with one nodes and blocklist"
    (is (= #{["Fact" "fact2"]
             ["Context" "type"]
             ["Notes" "notes"]
             ["Context" "country"]
             ["Fact" "fact1"]
             ["Context" "org"]
             ["Context" "location"]}
           (set (sac-api/neighborhood {:nodes [["Datasource" "datasource" "dsn-1"]]
                                       :pattern [true true]
                                       :block-list [["Date" "Datasource"] "_" "_"]})))))
  (testing "Testing neighborhood with one nodes and allowlist"
    (is (= #{["Fact" "fact2"]
             ["Context" "type"]
             ["Context" "country"]
             ["Fact" "fact1"]
             ["Context" "org"]
             ["Context" "location"]}
           (set (sac-api/neighborhood {:nodes [["Datasource" "datasource" "dsn-1"]]
                                       :pattern [true true]
                                       :allow-list [["Context" "Fact"] "*" "*"]})))))
  (testing "Testing neighborhood with no nodes and sort-by"
    (is (= [["Context" "org" "org1"]
            ["Context" "org" "org2"]
            ["Context" "org" "org3"]
            ["Context" "org" "org4"]
            ["Context" "org" "org5"]]
           (sac-api/neighborhood {:nodes []
                                  :pattern [true true true]
                                  :allow-list [["Context"] ["org"] "*"]
                                  :sort-by {:limit 5
                                            :order :asc
                                            :by :value}}))))
  (testing "Testing neighborhood with no nodes and sort-by matching strings"
    (is (= [["Context" "org" "org1"]
            ["Context" "org" "org2"]
            ["Context" "org" "org3"]
            ["Context" "org" "org4"]
            ["Context" "org" "org5"]]
           (sac-api/neighborhood {:nodes []
                                  :pattern [true true true]
                                  :allow-list [["Context"] ["org"] "org"]
                                  :sort-by {:limit 5
                                            :order :asc
                                            :by :value}}))))
  (testing "Testing neighborhood with no nodes and all attr-name"
    (is (= (set query-all-attr-name-result)
           (set (sac-api/neighborhood query-all-attr-name))))))

(deftest ranges-test
  (testing "Testing ranges for one data-tile"
    (is (= {"fact1" {:min 1 :max 3}
            "fact2" {:min 2 :max 4}
            "fact3" {:min 1, :max 3}}
           (sac-api/ranges {})))))
