(ns de.explorama.backend.expdb.indexed-db-test
  (:require [clojure.test :as t :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.expdb.middleware.indexed-db-test :refer [db test-setup]]
            [de.explorama.backend.expdb.persistence.indexed :as sut]
            [de.explorama.backend.expdb.persistence.shared :as imp]))

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
                             :items [{:global-id "i1-0"
                                      :features [{:global-id "if1-0"
                                                  :facts [{:name "fact1"
                                                           :type "integer"
                                                           :value 1230812312}
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
                                                           :value "1997"}]
                                                  :texts ["test text"]}]}
                                     {:global-id "i1-1"
                                      :features [{:global-id "if1-1"
                                                  :facts [{:name "fact1"
                                                           :type "integer"
                                                           :value 2}
                                                          {:name "fact2"
                                                           :type "decimal"
                                                           :value 1.0}]
                                                  :locations [{:lat 30
                                                               :lon 30}]
                                                  :context-refs [{:global-id "c1"}
                                                                 {:global-id "o1"}
                                                                 {:global-id "o2"}
                                                                 {:global-id "t1"}]
                                                  :dates [{:type "occured-at"
                                                           :value "1997-01"}]
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

(use-fixtures :each (partial test-setup []))

(def ^:private dt-key-1 {"year" "1997"
                         "country" "country1"
                         "datasource" "dsn-1"
                         "bucket" "default"
                         "identifier" "search"})

(def ^:private dt-key-2 {"year" "1998"
                         "country" "country2"
                         "datasource" "dsn-1"
                         "bucket" "default"
                         "identifier" "search"})

(def ^:private dt-key-3 {"year" "1996"
                         "country" "country3"
                         "datasource" "dsn-2"
                         "bucket" "default"
                         "identifier" "search"})

(def ^:private dt-key-4 {"year" "1998"
                         "country" "country2"
                         "datasource" "dsn-2"
                         "bucket" "default"
                         "identifier" "search"})

(def ^:private data-tiles-result
  {dt-key-1 [{"org" ["org1" "org2"],
              "fact1" 1230812312,
              "country" "country1",
              "id" "i1-0",
              "datasource" "dsn-1",
              "location" [[15 15]],
              "type" "type1",
              "date" "1997",
              "notes" "test text",
              "fact2" #?(:cljs 2
                         :clj 2.0)}
             {"org" ["org1" "org2"],
              "fact1" 2,
              "country" "country1",
              "id" "i1-1",
              "datasource" "dsn-1",
              "location" [[30 30]],
              "type" "type1",
              "date" "1997-01",
              "notes" "test text",
              "fact2" #?(:cljs 1
                         :clj 1.0)}],
   dt-key-2 [{"org" ["org3" "org4"],
              "fact1" 3,
              "country" "country2",
              "id" "i2",
              "datasource" "dsn-1",
              "location" [[15 15]],
              "type" "type1",
              "date" "1998-01-05",
              "notes" "test text",
              "fact2" #?(:cljs 4
                         :clj 4.0)}],
   dt-key-3 [{"org" ["org5" "org6"],
              "country" "country3",
              "id" "i3",
              "datasource" "dsn-2",
              "location" [[15 15]],
              "type" "type2",
              "date" "1996-01-02",
              "notes" "test text",
              "fact2" #?(:cljs 2
                         :clj 2.0),
              "fact3" 1}],
   dt-key-4 [{"org" ["org7" "org8"],
              "country" "country2",
              "id" "i4",
              "datasource" "dsn-2",
              "location" [[15 15]],
              "type" "type2",
              "date" "1998-01-05",
              "notes" "test text",
              "fact2" #?(:cljs 4
                         :clj 4.0),
              "fact3" 3}]})

(deftest import-and-delete-tests
  (testing "testing core interface functions"
    (is (=  (imp/transform->import dummy-data-1 {} "default")
            {:success true
             :data 3}))
    (is (=  (imp/transform->import dummy-data-2 {} "default")
            {:success true
             :data 2}))
    (is (= (sut/data-tiles @db [dt-key-1 dt-key-2 dt-key-3 dt-key-4])
           data-tiles-result))
    (is (= (sut/event @db dt-key-1 "i1-0")
           {"id" "i1-0",
            "date" "1997",
            "datasource" "dsn-1",
            "location" [[15 15]],
            "fact1" 1230812312,
            "fact2" 2.0,
            "org" ["org1" "org2"]
            "type" "type1"
            "country" "country1",
            "notes" "test text"}))
    (is (= (get (sut/get-meta-data @db [dt-key-1]) dt-key-1)
           {:hash -25597803,
            :key {"year" "1997",
                  "country" "country1",
                  "datasource" "dsn-1",
                  "bucket" "default",
                  "identifier" "search"},
            :acs {"org" #{"org1" "org2"},
                  "fact1" #{"integer"},
                  "country" #{"country1"},
                  "datasource" #{"dsn-1"},
                  "year" #{"1997"},
                  "type" #{"type1"},
                  "month" #{"1997-01"},
                  "notes" #{"notes"},
                  "fact2" #{"decimal"},
                  "location" #{"location"}},
            :attributes {"org" "Context",
                         "fact1" "Fact",
                         "country" "Context",
                         "datasource" "Datasource",
                         "year" "Date",
                         "type" "Context",
                         "month" "Date",
                         "notes" "Notes",
                         "fact2" "Fact",
                         "day" "Date"
                         "location" "Context"},
            :ranges {"fact1" [2 1230812312], "fact2" [1 2]},
            :count 2}))
    (is (= (sut/delete @db "dsn-1")
           {:success true
            :data-tiles [{"year" "1997",
                          "country" "country1",
                          "datasource" "dsn-1",
                          "bucket" "default",
                          "identifier" "search"}
                         {"year" "1998",
                          "country" "country2",
                          "datasource" "dsn-1",
                          "bucket" "default",
                          "identifier" "search"}]}))
    (is (= (sut/data-tiles @db [dt-key-1 dt-key-2 dt-key-3 dt-key-4])
           (assoc data-tiles-result
                  dt-key-1 []
                  dt-key-2 [])))
    (is (= (sut/delete-all @db)
           {:success true
            :dropped-bucket? true}))
    (is (= (sut/data-tiles @db [dt-key-1 dt-key-2 dt-key-3 dt-key-4])
           (assoc data-tiles-result
                  dt-key-1 []
                  dt-key-2 []
                  dt-key-3 []
                  dt-key-4 [])))))

(deftest index-tests
  (testing "testing index functions"
    ;This is not the index structures - just validating that we can store complex data
    (is (= (sut/set-index @db [dummy-data-2 dummy-data-2 dummy-data-2])
           {:success true
            :pairs 1}))
    (is (= (sut/get-index @db)
           [dummy-data-2 dummy-data-2 dummy-data-2]))))

(deftest dump-test
  (testing "testing dump functions"
    (is (= (sut/dump @db)
           {}))
    (is (= (sut/set-dump @db
                         {"foo" "bar"
                          "bar" "foo"})
           {:success true
            :pairs 2}))
    (is (= (sut/dump @db)
           {"foo" "bar"
            "bar" "foo"}))))