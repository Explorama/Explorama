(ns de.explorama.frontend.mosaic.data-structure.data-format-test
  (:require [data-format-lib.core :as dflc]
            [de.explorama.frontend.mosaic.global-filter :as ggf]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data-structure.cljs-impl :as gdsci]
            [data-format-lib.filter-functions :as ff]
            [cljs.test :as t :include-macros true]))

(def date-test-input-org [{"date" "2012", "a" 1}
                          {"date" "2012-01", "a" 1}
                          {"date" "2013-02"}
                          {"date" "2014-02-03", "a" 1}
                          {"date" "2015", "a" 2}])

(defn impl-tests-date [test-impl-dfl text]
  (let [date-test-input (gdal/->g date-test-input-org)]
    (t/testing (str "month = filter" " " text)
      (t/is (= (->>
                [{"date" "2012", "a" 1}
                 {"date" "2012-01", "a" 1}
                 {"date" "2015", "a" 2}]
                (gdal/->g)
                (gdal/g->))
               (gdal/g->
                (dflc/filter-data
                 [:and [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/month, :value 1}]]
                 date-test-input
                 test-impl-dfl)))))

    (t/testing (str "date >= filter" " " text)
      (t/is (= (->>
                [{"date" "2014-02-03", "a" 1}
                 {"date" "2015", "a" 2}]
                (gdal/->g)
                (gdal/g->))
               (gdal/g->

                (dflc/filter-data
                 [:and [:and #:data-format-lib.filter{:op :>=, :prop :data-format-lib.dates/full-date, :value "2014-02-03"}]]
                 date-test-input
                 test-impl-dfl)))))

    (t/testing (str "year > filter" " " text)
      (t/is (= (->>
                [{"date" "2013-02"}
                 {"date" "2014-02-03", "a" 1}
                 {"date" "2015", "a" 2}]
                (gdal/->g)
                (gdal/g->))
               (gdal/g->
                (dflc/filter-data
                 [:and [:and #:data-format-lib.filter{:op :>, :prop :data-format-lib.dates/year, :value 2012}]]
                 date-test-input
                 test-impl-dfl)))))

    (t/testing (str "year = filter" " " text)
      (t/is (=
             (->>
              [{"date" "2012", "a" 1}
               {"date" "2012-01", "a" 1}]
              (gdal/->g)
              (gdal/g->))
             (gdal/g->
              (dflc/filter-data
               [:and [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}]]
               date-test-input
               test-impl-dfl)))))

    (t/testing (str "filter deep 0" " " text)
      (t/is (= (->>
                [{"date" "2012", "a" 1}
                 {"date" "2012-01", "a" 1}]
                (gdal/->g)
                (gdal/g->))
               (gdal/g->
                (dflc/filter-data
                 #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}
                 date-test-input
                 test-impl-dfl)))))

    (t/testing (str "filter deep 1" " " text)
      (t/is (=
             (->>
              [{"date" "2012", "a" 1}
               {"date" "2012-01", "a" 1}]
              (gdal/->g)
              (gdal/g->))
             (gdal/g->
              (dflc/filter-data
               [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}]
               date-test-input
               test-impl-dfl)))))

    (t/testing (str "filter deep 3" " " text)
      (t/is (= (->>
                [{"date" "2012", "a" 1}
                 {"date" "2012-01", "a" 1}]
                (gdal/->g)
                (gdal/g->))
               (gdal/g->
                (dflc/filter-data
                 [:and [:and [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}]]]
                 date-test-input
                 test-impl-dfl)))))

    (t/testing (str "group-by grpmonth = filter" " " text)
      (let [result-true [{"date" "2012", "a" 1}
                         {"date" "2012-01", "a" 1}
                         {"date" "2015", "a" 2}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   [:and [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/month, :value 1}]]
                   date-test-input
                   test-impl-dfl))))))

    (t/testing (str "group-by date >= filter" " " text)
      (let [result-true [{"date" "2014-02-03", "a" 1}
                         {"date" "2015", "a" 2}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   [:and [:and #:data-format-lib.filter{:op :>=, :prop :data-format-lib.dates/full-date, :value "2014-02-03"}]]
                   date-test-input
                   test-impl-dfl))))))

    (t/testing (str "group-by year > filter" " " text)
      (let [result-true [{"date" "2013-02"}
                         {"date" "2014-02-03", "a" 1}
                         {"date" "2015", "a" 2}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   [:and [:and #:data-format-lib.filter{:op :>, :prop :data-format-lib.dates/year, :value 2012}]]
                   date-test-input
                   test-impl-dfl))))))

    (t/testing (str "group-by year = filter" " " text)
      (let [result-true [{"date" "2012", "a" 1}
                         {"date" "2012-01", "a" 1}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   [:and [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}]]
                   date-test-input
                   test-impl-dfl))))))

    (t/testing "group-by empty filter"
      (let [result-true date-test-input
            result
            (dflc/group-by-data
             [:and]
             date-test-input
             test-impl-dfl)]
        (t/is (and (= 0
                      (gdal/count
                       (gdal/get
                        result
                        false)))
                   (= (gdal/->g
                       result-true)
                      (gdal/get
                       result
                       true))))))

    (t/testing (str "group-by filter deep 0" " " text)
      (let [result-true [{"date" "2012", "a" 1}
                         {"date" "2012-01", "a" 1}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}
                   date-test-input
                   test-impl-dfl))))))

    (t/testing (str "group-by filter deep 1" " " text)
      (let [result-true [{"date" "2012", "a" 1}
                         {"date" "2012-01", "a" 1}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}]
                   date-test-input
                   test-impl-dfl))))))

    (t/testing (str "group-by filter deep 3" " " text)
      (let [result-true [{"date" "2012", "a" 1}
                         {"date" "2012-01", "a" 1}]
            result-false (vec (remove (set result-true) date-test-input-org))]
        (t/is (= (->>
                  {true result-true
                   false result-false}
                  (gdal/->g)
                  (gdal/g->))
                 (gdal/g->
                  (dflc/group-by-data
                   [:and [:and [:and #:data-format-lib.filter{:op :=, :prop :data-format-lib.dates/year, :value 2012}]]]
                   date-test-input
                   test-impl-dfl))))))))

(t/deftest date-filter-tests
  (with-redefs [gdal/->g gdsci/->g
                gdal/g-> gdsci/g->
                gdal/g? gdsci/g?
                gdal/copy gdsci/copy
                gdal/coll? gdsci/coll?
                gdal/count gdsci/count
                gdal/keys gdsci/keys
                gdal/get gdsci/get
                gdal/mapv gdsci/mapv
                gdal/filter gdsci/filter
                gdal/filter-index gdsci/filter-index
                gdal/remove gdsci/remove
                gdal/concat gdsci/concat
                gdal/merge gdsci/merge
                gdal/conj gdsci/conj
                gdal/update gdsci/update
                gdal/assoc gdsci/assoc
                gdal/dissoc gdsci/dissoc
                gdal/join-strings gdsci/join-strings
                gdal/select-keys gdsci/select-keys
                gdal/some gdsci/some
                gdal/reduce gdsci/reduce
                gdal/group-by gdsci/group-by
                gdal/group-by-expand-vectors gdsci/group-by-expand-vectors
                gdal/union gdsci/union
                gdal/intersection gdsci/intersection
                gdal/union-vec gdsci/union-vec
                gdal/intersection-vec gdsci/intersection-vec
                gdal/sort-by-asc gdsci/sort-by-asc
                gdal/sort-by-dsc gdsci/sort-by-dsc
                gdal/contains? gdsci/contains?]
    (impl-tests-date ff/default-impl "normal impl")))

(def test-input-map [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1}
                     {"a" "ehlo"
                      "b" "world"
                      "c" nil}])

(def test-input-vector [{"a" ["hello" "world"]
                         "b" "foo"
                         "c" [2.1 nil]}
                        {"a" ["foo" "bar"]
                         "b" "hello"}])

(defn impl-tests-std [test-impl-dfl text]
  (let [test-input-map (gdal/->g test-input-map)
        test-input-vector (gdal/->g test-input-vector)]
    (t/testing (str "string = filter" " " text)
      (t/is (= (->> [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1}]
                    (gdal/->g)
                    (gdal/g->))
               (gdal/g->
                (dflc/filter-data
                 #:data-format-lib.filter{:op :=, :prop "a", :value "hello"}
                 test-input-map
                 test-impl-dfl)))))
    (t/testing (str "includes filter" " " text)
      (t/is (= (->> [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1}]
                    (gdal/->g)
                    (gdal/g->))
               (gdal/g-> (dflc/filter-data
                          #:data-format-lib.filter{:op :includes, :prop "a", :value "hello"}
                          test-input-map
                          test-impl-dfl)))))
    (t/testing (str "excludes filter" " " text)
      (t/is (= (->> [{"a" "ehlo"
                      "b" "world"
                      "c" nil}]
                    (gdal/->g)
                    (gdal/g->))
               (gdal/g-> (dflc/filter-data
                          #:data-format-lib.filter{:op :excludes, :prop "a", :value "hello"}
                          test-input-map
                          test-impl-dfl)))))
    (t/testing (str "group-by string = filter" " " text)
      (t/is (=  (->> {true (gdal/->g [{"a" "hello"
                                       "b" "hello-world"
                                       "c" 2.1}])
                      false (gdal/->g [{"a" "ehlo"
                                        "b" "world"
                                        "c" nil}])}
                     (gdal/->g)
                     (gdal/g->))
                (gdal/g-> (dflc/group-by-data
                           #:data-format-lib.filter{:op :=, :prop "a", :value "hello"}
                           test-input-map
                           test-impl-dfl)))))
    (t/testing (str "group-by includes filter" " " text)
      (t/is (= (->> {true (gdal/->g [{"a" "hello"
                                      "b" "hello-world"
                                      "c" 2.1}])
                     false (gdal/->g [{"a" "ehlo"
                                       "b" "world"
                                       "c" nil}])}
                    (gdal/->g)
                    (gdal/g->))
               (gdal/g-> (dflc/group-by-data
                          #:data-format-lib.filter{:op :includes, :prop "a", :value "hello"}
                          test-input-map
                          test-impl-dfl)))))
    (t/testing (str "group-by excludes filter" " " text)
      (t/is (= (->>  {true (gdal/->g [{"a" "ehlo"
                                       "b" "world"
                                       "c" nil}])
                      false (gdal/->g [{"a" "hello"
                                        "b" "hello-world"
                                        "c" 2.1}])}
                     (gdal/->g)
                     (gdal/g->))
               (gdal/g-> (dflc/group-by-data
                          #:data-format-lib.filter{:op :excludes, :prop "a", :value "hello"}
                          test-input-map
                          test-impl-dfl)))))
    (t/testing (str "vector values = filter" " " text)
      (t/is (= (->>  [{"a" ["hello" "world"]
                       "b" "foo"
                       "c" [2.1 nil]}]
                     (gdal/->g)
                     (gdal/g->))
               (gdal/g-> (dflc/filter-data
                          #:data-format-lib.filter{:op := :prop "a" :value "hello"}
                          test-input-vector
                          test-impl-dfl)))))
    (t/testing (str "nil values >= filter" " " text)
      (t/is (= (->> [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1}]
                    (gdal/->g)
                    (gdal/g->)))
            (gdal/g-> (dflc/filter-data
                       #:data-format-lib.filter{:op :>= :prop "c" :value 1.3}
                       test-input-map
                       test-impl-dfl))))
    (t/testing (str "nil values >= filter in vector" " " text)
      (t/is (= (gdal/g-> (gdal/->g [{"a" "hello"
                                     "b" "hello-world"
                                     "c" 2.1}]))
               (gdal/g-> (dflc/filter-data
                          #:data-format-lib.filter{:op :>= :prop "c" :value 1.3}
                          test-input-map
                          test-impl-dfl)))))
    (t/testing (str "group-by vector values = filter" " " text)
      (t/is (= (->> {true [{"a" ["hello" "world"]
                            "b" "foo"
                            "c" [2.1 nil]}]
                     false [{"a" ["foo" "bar"]
                             "b" "hello"}]}
                    (gdal/->g)
                    (gdal/g->))
               (gdal/g-> (dflc/group-by-data
                          #:data-format-lib.filter{:op := :prop "a" :value "hello"}
                          test-input-vector
                          test-impl-dfl)))))))

(t/deftest standard-filter-tests
  (with-redefs [gdal/->g gdsci/->g
                gdal/g-> gdsci/g->
                gdal/g? gdsci/g?
                gdal/copy gdsci/copy
                gdal/coll? gdsci/coll?
                gdal/count gdsci/count
                gdal/keys gdsci/keys
                gdal/get gdsci/get
                gdal/mapv gdsci/mapv
                gdal/filter gdsci/filter
                gdal/filter-index gdsci/filter-index
                gdal/remove gdsci/remove
                gdal/concat gdsci/concat
                gdal/merge gdsci/merge
                gdal/conj gdsci/conj
                gdal/update gdsci/update
                gdal/assoc gdsci/assoc
                gdal/dissoc gdsci/dissoc
                gdal/join-strings gdsci/join-strings
                gdal/select-keys gdsci/select-keys
                gdal/some gdsci/some
                gdal/reduce gdsci/reduce
                gdal/group-by gdsci/group-by
                gdal/group-by-expand-vectors gdsci/group-by-expand-vectors
                gdal/union gdsci/union
                gdal/intersection gdsci/intersection
                gdal/union-vec gdsci/union-vec
                gdal/intersection-vec gdsci/intersection-vec
                gdal/sort-by-asc gdsci/sort-by-asc
                gdal/sort-by-dsc gdsci/sort-by-dsc
                gdal/contains? gdsci/contains?]
    (impl-tests-std ff/default-impl "normal impl"))
  (impl-tests-std ggf/impl "js impl"))
