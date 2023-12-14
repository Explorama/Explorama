(ns de.explorama.shared.search.date-utils-test
  (:require  #?(:clj  [clojure.test :refer [deftest testing is]]
                :cljs [cljs.test :refer [deftest testing is] :include-macros true])
             [de.explorama.shared.common.unification.time :as t]))

(def test-year "1999")
(def test-month "02")
(def test-day "01")
(def test-year-month-str (str test-year "-" test-month))
(def test-date-str (str test-year "-" test-month "-" test-day))

(deftest date-str->obj->date-str-testing
  (testing "Testing conversion of date-strings to date-objects and back"
    (is (= test-date-str
           (t/obj->date-str (t/date-str->obj test-date-str))))
    (is (= test-year
           (t/obj->date-str :year (t/date-str->obj :year test-year))))
    (is (= test-year-month-str
           (t/obj->date-str :month (t/date-str->obj :month test-year-month-str))))
    (is (not= test-year-month-str
              (t/obj->date-str (t/date-str->obj test-date-str))))))
