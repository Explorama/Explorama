(ns data-format-lib.filter-test
  (:require
   [data-format-lib.filter :as filter]
   [data-format-lib.filter-functions :as ff]
   #?(:clj  [clojure.test :as t]
      :cljs [cljs.test :as t :include-macros true])))





;; Make sure we can test the private function here
(def compile-filter #'filter/compile-filter)

(t/deftest compile-filter-tests
  (t/testing "date filter"
    (t/is (fn?
           (compile-filter ff/default-impl [:and
                                            {:data-format-lib.filter/op :>=, :data-format-lib.filter/prop :data-format-lib.dates/full-date, :data-format-lib.filter/value "1997-07-27"}
                                            {:data-format-lib.filter/op :<=, :data-format-lib.filter/prop :data-format-lib.dates/full-date, :data-format-lib.filter/value "1997-07-27"}]))))
  (t/testing "fulltext attribute filter"
    (t/is (fn?
           (compile-filter ff/default-impl [:and
                                            {:data-format-lib.filter/op :in, :data-format-lib.filter/prop :fulltext, :data-format-lib.filter/value
                                             #{"33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"
                                               "9281063a53d401d75ddf4e0003c72a642f061d907480f768daa5fb88161a3875"
                                               "44b98b02d264c98164d4a629adc946a9b22305f25b1f823909a8afbe28bfd846"
                                               "ac9f6dd080352c274687e482ce8ee114fdd96084a7f58559e2aae7a154dd815e"
                                               "deca73b70efaa4629b121d9b54eff2b7203148a6ea582afb6e4826364914d26"
                                               "511acd880f8c8f08b5a3550a6cd4038e2a6aa1347ae956ed9b2589d25eba1a92"
                                               "483c7ca0655a94804fdb027fe8282cda81c761b602f34d14fa7478357538736b"}}]))))

  (t/testing "mixed filter"
    (t/is (fn?
           (compile-filter ff/default-impl [:and [:and
                                                  {:data-format-lib.filter/op :>=, :data-format-lib.filter/prop :data-format-lib.dates/full-date, :data-format-lib.filter/value "1997-07-27"}
                                                  {:data-format-lib.filter/op :<=, :data-format-lib.filter/prop :data-format-lib.dates/full-date, :data-format-lib.filter/value "1997-07-27"}]
                                            {:data-format-lib.filter/op :in, :data-format-lib.filter/prop :fulltext, :data-format-lib.filter/value
                                             #{"33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"
                                               "9281063a53d401d75ddf4e0003c72a642f061d907480f768daa5fb88161a3875"
                                               "44b98b02d264c98164d4a629adc946a9b22305f25b1f823909a8afbe28bfd846"
                                               "ac9f6dd080352c274687e482ce8ee114fdd96084a7f58559e2aae7a154dd815e"
                                               "deca73b70efaa4629b121d9b54eff2b7203148a6ea582afb6e4826364914d26"
                                               "511acd880f8c8f08b5a3550a6cd4038e2a6aa1347ae956ed9b2589d25eba1a92"
                                               "483c7ca0655a94804fdb027fe8282cda81c761b602f34d14fa7478357538736b"}}]))))

  (t/testing
   "compile-filter handles any type of sequence, not only vectors."
    (t/is (fn? (compile-filter ff/default-impl
                               [:and (list :or #:data-format-lib.filter{:op :includes, :prop "notes", :value "BoMB"})])))))

(t/deftest empty-val?-test
  (t/testing "empty values"
    (t/are [x] (= (filter/empty-val? x nil)
                  true)
      nil
      " "
      "\n"
      ""))
  (t/testing "non-empty values"
    (t/are [x] (= (filter/empty-val? x nil)
                  false)
      1
      1.1
      "a"
      [])))
