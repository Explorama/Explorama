(ns de.explorama.backend.search.filter-test
  (:require [clojure.test :refer [deftest testing is]]
            [de.explorama.backend.search.datainstance.filter :refer [fulltext->filter]]
            [data-format-lib.date-filter :as sut]
            [data-format-lib.filter-functions :as ff]))

(deftest fulltext-filter-gen
  (testing "basic search"
    (is (= [:or
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "foo"}
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "bar"}
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "lorem"}
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "ippsum"}]
           (fulltext->filter "notes" false "includes" "foo bar lorem ippsum")))
    (is (= [:or #:data-format-lib.filter{:op :includes, :prop "notes", :value "benin"}]
           (fulltext->filter "notes" false "includes" "benin"))))
  (testing "advanced or search"
    (is (= [:or
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "foo"}
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "bar"}
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "lorem"}
            #:data-format-lib.filter{:op :includes, :prop "notes", :value "ippsum"}]
           (fulltext->filter "notes" true "includes" "foo bar lorem ippsum")))
    (is (= [:or #:data-format-lib.filter{:op :includes, :prop "notes", :value "benin"}]
           (fulltext->filter "notes" true "includes" "benin"))))
  (testing "advandced and search"
    (is (= #:data-format-lib.filter{:op :includes, :prop "notes", :value "foo bar lorem ippsum"}
           (fulltext->filter "notes" true "exact term" "foo bar lorem ippsum")))
    (is (=  #:data-format-lib.filter{:op :includes, :prop "notes", :value "benin"}
            (fulltext->filter "notes" true "exact term" "benin"))))
  (testing "advandced not search"
    (is (= #:data-format-lib.filter{:op :excludes, :prop "notes", :value "foo bar lorem ippsum"}
           (fulltext->filter "notes" true "excludes" "foo bar lorem ippsum")))
    (is (=  #:data-format-lib.filter{:op :excludes, :prop "notes", :value "benin"}
            (fulltext->filter "notes" true "excludes" "benin")))))

(def test-data-input
  [{"id" 1
    "notes" ""
    "country" "a"}
   {"id" 2
    "notes" "benin"
    "country" "a"}
   {"id" 3
    "notes" "benin"
    "country" "b"}
   {"id" 4
    "notes" "foo lorem"
    "country" "b"}
   {"id" 5
    "notes" "foo bar lorem ippsum"
    "country" "b"}])

(defn filter-by [query data]
  (sut/filter-data ff/default-impl query data))

(deftest fulltext-filtering
  (testing "basic search"
    (is (= [{"id" 4
             "notes" "foo lorem"
             "country" "b"}
            {"id" 5
             "notes" "foo bar lorem ippsum"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" false "includes" "foo bar lorem ippsum")
                      test-data-input)))
    (is (= [{"id" 2
             "notes" "benin"
             "country" "a"}
            {"id" 3
             "notes" "benin"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" false "includes" "benin")
                      test-data-input))))
  (testing "advanced and search"
    (is (= [{"id" 5
             "notes" "foo bar lorem ippsum"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "exact term" "foo bar lorem ippsum")
                      test-data-input)))
    (is (= [{"id" 4
             "notes" "foo lorem"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "exact term" "foo lorem")
                      test-data-input)))
    (is (= [{"id" 2
             "notes" "benin"
             "country" "a"}
            {"id" 3
             "notes" "benin"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "exact term" "benin")
                      test-data-input)))
    (is (= [{"id" 2
             "notes" "benin"
             "country" "a"}
            {"id" 3
             "notes" "benin"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "exact term" "benin")
                      test-data-input))))
  (testing "advanced not search"
    (is (= [{"id" 1
             "notes" ""
             "country" "a"}
            {"id" 2
             "notes" "benin"
             "country" "a"}
            {"id" 3
             "notes" "benin"
             "country" "b"}
            {"id" 5
             "notes" "foo bar lorem ippsum"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "excludes" "foo lorem")
                      test-data-input)))
    (is (= [{"id" 1
             "notes" ""
             "country" "a"}
            {"id" 4
             "notes" "foo lorem"
             "country" "b"}
            {"id" 5
             "notes" "foo bar lorem ippsum"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "excludes" "benin")
                      test-data-input)))
    (is (= [{"id" 1
             "notes" ""
             "country" "a"}
            {"id" 2
             "notes" "benin"
             "country" "a"}
            {"id" 3
             "notes" "benin"
             "country" "b"}
            {"id" 4
             "notes" "foo lorem"
             "country" "b"}]
           (filter-by (fulltext->filter "notes" true "excludes" "foo bar lorem ippsum")
                      test-data-input)))))