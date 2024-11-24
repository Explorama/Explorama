(ns de.explorama.shared.data-format.standard-filter-test
  (:require [clojure.test :as t]
            [de.explorama.shared.data-format.date-filter :as sut]
            [de.explorama.shared.data-format.filter :as f]
            [de.explorama.shared.data-format.filter-functions :as ff]
            [de.explorama.shared.common.test-data :as td]))

(def test-input-map [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1
                      "d" 1.0}
                     {"a" "ehlo"
                      "b" "world"
                      "c" nil
                      "d" -1.0}])

(def test-input-vector [{"a" ["hello" "world"]
                         "b" "foo"
                         "c" [2.1 nil]}
                        {"a" ["foo" "bar"]
                         "b" "hello"}])

(def test-input-vector-2 [{td/org [(td/org-val 2), (td/org-val 3)], "a" 1}
                          {td/org [(td/org-val 2)], "a" 1}
                          {td/org [(td/org-val 3)]}
                          {td/org ["Test", "Testorg", (td/org-val 2)], "a" 1}
                          {td/org ["Testorg", (td/org-val 3)], "a" 1}
                          {td/org ["Test"], "a" 2}
                          {td/org [], "a" 2}])

(t/deftest standard-filter-tests
  (t/testing "string = filter"
    (t/is (= (list {"a" "hello"
                    "b" "hello-world"
                    "c" 2.1
                    "d" 1.0})
             (sut/filter-data
              ff/default-impl
              #::f{:op :=, :prop "a", :value "hello"}
              test-input-map))))
  (t/testing "number = filter"
    (t/is (= (list {"a" "hello"
                    "b" "hello-world"
                    "c" 2.1
                    "d" 1.0})
             (sut/filter-data
              ff/default-impl
              #::f{:op :=, :prop "d", :value 1}
              test-input-map))))
  (t/testing "number not= filter"
    (t/is (= (list {"a" "ehlo"
                    "b" "world"
                    "c" nil
                    "d" -1.0})
             (sut/filter-data
              ff/default-impl
              #::f{:op :not=, :prop "d", :value 1}
              test-input-map))))
  (t/testing "includes filter"
    (t/is (= (list {"a" "hello"
                    "b" "hello-world"
                    "c" 2.1
                    "d" 1.0})
             (sut/filter-data
              ff/default-impl
              #::f{:op :includes, :prop "a", :value "hello"}
              test-input-map))))
  (t/testing "in filter"
    (t/is (= (list {"a" "hello"
                    "b" "hello-world"
                    "c" 2.1
                    "d" 1.0})
             (sut/filter-data
              ff/default-impl
              #::f{:op :in, :prop "c", :value #{2.1}}
              test-input-map))))
  (t/testing "group-by in filter"
    (t/is (= {true  [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1
                      "d" 1.0}]
              false [{"a" "ehlo"
                      "b" "world"
                      "c" nil
                      "d" -1.0}]}
             (sut/group-by-data
              ff/default-impl
              #::f{:op :in, :prop "a", :value #{"hello"}}
              test-input-map))))
  (t/testing "has filter"
    (t/is (= (list {"a" ["hello" "world"]
                    "b" "foo"
                    "c" [2.1 nil]})
             (sut/filter-data
              ff/default-impl
              #::f{:op :has, :prop "a", :value "hello"}
              test-input-vector))))
  (t/testing "group-by has filter"
    (t/is (= {true  [{"a" ["foo" "bar"]
                      "b" "hello"}]
              false [{"a" ["hello" "world"]
                      "b" "foo"
                      "c" [2.1 nil]}]}
             (sut/group-by-data
              ff/default-impl
              #::f{:op :has, :prop "a", :value "foo"}
              test-input-vector))))
  (t/testing "excludes filter"
    (t/is (= (list {"a" "ehlo"
                    "b" "world"
                    "c" nil
                    "d" -1.0})
             (sut/filter-data
              ff/default-impl
              #::f{:op :excludes, :prop "a", :value "hello"}
              test-input-map))))
  (t/testing "group-by string = filter"
    (t/is (= {true  [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1
                      "d" 1.0}]
              false [{"a" "ehlo"
                      "b" "world"
                      "c" nil
                      "d" -1.0}]}
             (sut/group-by-data
              ff/default-impl
              #::f{:op :=, :prop "a", :value "hello"}
              test-input-map))))
  (t/testing "not= filter on vector-values"
    (t/is (= (list {td/org [(td/org-val 3)]}
                   {td/org ["Testorg", (td/org-val 3)], "a" 1}
                   {td/org [], "a" 2})
             (sut/filter-data
              ff/default-impl
              [:and [:and
                     #::f{:op :not=, :prop td/org, :value (td/org-val 2)}
                     #::f{:op :not=, :prop td/org, :value "Test"}]]
              test-input-vector-2))))

  (t/testing "group-by includes filter"
    (t/is (= {true  [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1
                      "d" 1.0}]
              false [{"a" "ehlo"
                      "b" "world"
                      "c" nil
                      "d" -1.0}]}
             (sut/group-by-data
              ff/default-impl
              #::f{:op :includes, :prop "a", :value "hello"}
              test-input-map))))
  (t/testing "group-by excludes filter"
    (t/is (= {true  [{"a" "ehlo"
                      "b" "world"
                      "c" nil
                      "d" -1.0}]
              false [{"a" "hello"
                      "b" "hello-world"
                      "c" 2.1
                      "d" 1.0}]}
             (sut/group-by-data
              ff/default-impl
              #::f{:op :excludes, :prop "a", :value "hello"}
              test-input-map))))
  (t/testing "vector values = filter"
    (t/is (= [{"a" ["hello" "world"]
               "b" "foo"
               "c" [2.1 nil]}]
             (sut/filter-data
              ff/default-impl
              #::f{:op := :prop "a" :value "hello"}
              test-input-vector))))
  (t/testing "nil values >= filter"
    (t/is (= [{"a" "hello"
               "b" "hello-world"
               "c" 2.1
               "d" 1.0}]
             (sut/filter-data
              ff/default-impl
              #::f{:op :>= :prop "c" :value 1.3}
              test-input-map))))
  (t/testing "nil values >= filter in vector"
    (t/is (= [{"a" "hello"
               "b" "hello-world"
               "c" 2.1
               "d" 1.0}]
             (sut/filter-data
              ff/default-impl
              #::f{:op :>= :prop "c" :value 1.3}
              test-input-map))))
  (t/testing "group-by vector values = filter"
    (t/is (= {true  [{"a" ["hello" "world"]
                      "b" "foo"
                      "c" [2.1 nil]}]
              false [{"a" ["foo" "bar"]
                      "b" "hello"}]}
             (sut/group-by-data
              ff/default-impl
              #::f{:op := :prop "a" :value "hello"}
              test-input-vector)))))

(defn filter-by [query data]
  (sut/filter-data ff/default-impl query data))

(t/deftest empty-non-empty-operators-test
  (let [ev-some {"a" 1}
        ev-nil {"a" nil}
        ev-empty {"a" " "}
        ev-missing {"b" 1}
        ev-vec-empty {"a" []}
        ev-vec-empty2 {"a" [nil "" " "]}
        ev-vec-some {"a" [nil "x"]}
        all-events [ev-some ev-nil ev-empty ev-missing ev-vec-empty ev-vec-empty2 ev-vec-some]]
    (t/testing "empty operator"
      (t/is (= [ev-nil ev-empty ev-missing ev-vec-empty ev-vec-empty2]
               (filter-by #::f{:op :empty :prop "a" :value nil}
                          all-events))))
    (t/testing "non-empty-operator"
      (t/is (= [ev-some ev-vec-some]
               (filter-by #::f{:op :non-empty :prop "a" :value nil}
                          all-events))))))

(t/deftest safe-arguments-test
  (t/testing "nil attribute yields nil result of comparison"
    (t/are [op v1 v2 result] (= result
                                (filter-by #::f{:prop "a" :op op :value v2} [{"a" v1}]))
      := nil nil [{"a" nil}]
      := 1 nil []
      := nil 1 []
      :< nil 1 []
      :< 1 nil []
      :> nil 1 []
      :> 1 nil []
      :<= nil 1 []
      :<= 1 nil []
      :>= nil 1 []
      :>= 1 nil [])))

(t/deftest filter-negate-test
  (t/testing "negate >= filter with string prop"
    (t/is (= #::f{:op :< :prop "c" :value 1.2}
             (f/negate #::f{:op :>= :prop "c" :value 1.2}))))
  (t/testing "negate and >= <= filter with string prop"
    (t/is (= [:or
              #::f{:op :< :prop "c" :value 1.2}
              #::f{:op :> :prop "c" :value 50}]
             (f/negate [:and
                        #::f{:op :>= :prop "c" :value 1.2}
                        #::f{:op :<= :prop "c" :value 50}]))))
  (t/testing "negate = filter with string prop"
    (t/is (= #::f{:op :not= :prop "c" :value "test1"}
             (f/negate #::f{:op := :prop "c" :value "test1"}))))
  (t/testing "negate multiple = filter with string prop"
    (t/is (= [:and
              #::f{:op :not= :prop "c" :value "test1"}
              #::f{:op :not= :prop "c" :value "test2"}]
             (f/negate [:or
                        #::f{:op := :prop "c" :value "test1"}
                        #::f{:op := :prop "c" :value "test2"}]))))
  (t/testing "negate >= filter with keyword prop"
    (t/is (= #::f{:op :< :prop "c" :value 1.2}
             (f/negate #::f{:op :>= :prop "c" :value 1.2}))))
  (t/testing "negate and >= <= filter with keyword prop"
    (t/is (= [:or
              #::f{:op :< :prop "c" :value 1.2}
              #::f{:op :> :prop "c" :value 50}]
             (f/negate [:and
                        #::f{:op :>= :prop "c" :value 1.2}
                        #::f{:op :<= :prop "c" :value 50}]))))
  (t/testing "negate = filter with keyword prop"
    (t/is (= #::f{:op :not= :prop "c" :value "test1"}
             (f/negate #::f{:op := :prop "c" :value "test1"}))))
  (t/testing "negate multiple = filter with keyword prop"
    (t/is (= [:and
              #::f{:op :not= :prop "c" :value "test1"}
              #::f{:op :not= :prop "c" :value "test2"}]
             (f/negate [:or
                        #::f{:op := :prop "c" :value "test1"}
                        #::f{:op := :prop "c" :value "test2"}])))))
