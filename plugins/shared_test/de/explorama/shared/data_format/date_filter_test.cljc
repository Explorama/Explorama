(ns de.explorama.shared.data-format.date-filter-test
  (:require [de.explorama.shared.data-format.date-filter :as sut]
            [de.explorama.shared.data-format.filter :as f]
            [de.explorama.shared.data-format.filter-functions :as ff]
            [de.explorama.shared.data-format.core :as fc]
            #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            #?(:clj [clj-time.core :as time]
               :cljs [cljs-time.core :as time])
            #?(:clj [clj-time.format :as date-format]
               :cljs [cljs-time.format :as date-format])))

(def date-test-input [{"date" "2012", "a" 1}
                      {"date" "2012-01", "a" 1}
                      {"date" "2013-02"}
                      {"date" "2014-01-23", "a" 1}
                      {"date" "2014-02-03", "a" 1}
                      {"date" "2015", "a" 2}])

(defn filter-by [query data]
  (sut/filter-data ff/default-impl query data))

(defn unparse [d]
  (date-format/unparse (date-format/formatters :year-month-day) d))

(t/deftest date-filter-tests

  (t/testing "month = filter"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1}
                   {"date" "2014-01-23", "a" 1}
                   {"date" "2015", "a" 2})
             (filter-by
              [:and [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/month, :value 1}]]
              date-test-input))))

  (t/testing "date >= filter"
    (t/is (= (list {"date" "2014-02-03", "a" 1}
                   {"date" "2015", "a" 2})
             (filter-by
              [:and [:and #:de.explorama.shared.data-format.filter{:op :>=, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]]
              date-test-input))))

  (t/testing "date <= filter"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1}
                   {"date" "2013-02"}
                   {"date" "2014-01-23", "a" 1}
                   {"date" "2014-02-03", "a" 1})
             (filter-by
              [:and [:and #:de.explorama.shared.data-format.filter{:op :<=, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]]
              date-test-input))))

  (t/testing "year > filter"
    (t/is (= (list {"date" "2013-02"}
                   {"date" "2014-01-23", "a" 1}
                   {"date" "2014-02-03", "a" 1}
                   {"date" "2015", "a" 2})
             (filter-by
              [:and [:and #:de.explorama.shared.data-format.filter{:op :>, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]]
              date-test-input))))

  (t/testing "year < filter"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1})
             (filter-by
              [:and [:and #:de.explorama.shared.data-format.filter{:op :<, :prop :de.explorama.shared.data-format.dates/year, :value 2013}]]
              date-test-input))))

  (t/testing "year = filter"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1})
             (filter-by
              [:and [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]]
              date-test-input))))

  (t/testing "filter deep 0"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1})
             (filter-by
              #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}
              date-test-input))))

  (t/testing "filter deep 1"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1})
             (filter-by
              [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]
              date-test-input))))

  (t/testing "filter deep 3"
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1})
             (filter-by
              [:and [:and [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]]]
              date-test-input))))

  (t/testing "filter not="
    (t/is (= (list {"date" "2012", "a" 1}
                   {"date" "2012-01", "a" 1}
                   {"date" "2013-02"}
                   {"date" "2014-01-23", "a" 1}
                   {"date" "2015", "a" 2})
             (filter-by
              [:and [:or #:de.explorama.shared.data-format.filter{:op :not=, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]]
              date-test-input))))

  (t/testing "applying empty / non-empty operator to date events using"
    (let [nil-date-event {"a" 1 "date" nil}
          some-date-event {"a" 2 "date" "2014-01-01"}]
      (t/testing "year"
        (t/is (= [nil-date-event]
                 (filter-by #:de.explorama.shared.data-format.filter{:op :empty :prop :de.explorama.shared.data-format.dates/year :value nil}
                            [some-date-event nil-date-event])))
        (t/is (= [some-date-event]
                 (filter-by #:de.explorama.shared.data-format.filter{:op :non-empty :prop :de.explorama.shared.data-format.dates/year :value nil}
                            [some-date-event nil-date-event]))))
      (t/testing "month"
        (t/is (= [nil-date-event]
                 (filter-by #:de.explorama.shared.data-format.filter{:op :empty :prop :de.explorama.shared.data-format.dates/month :value nil}
                            [some-date-event nil-date-event])))
        (t/is (= [some-date-event]
                 (filter-by #:de.explorama.shared.data-format.filter{:op :non-empty :prop :de.explorama.shared.data-format.dates/month :value nil}
                            [some-date-event nil-date-event]))))
      (t/testing "day"
        (t/is (= [nil-date-event]
                 (filter-by #:de.explorama.shared.data-format.filter{:op :empty :prop :de.explorama.shared.data-format.dates/full-date :value nil}
                            [some-date-event nil-date-event])))
        (t/is (= [some-date-event]
                 (filter-by #:de.explorama.shared.data-format.filter{:op :non-empty :prop :de.explorama.shared.data-format.dates/full-date :value nil}
                            [some-date-event nil-date-event]))))))

  (t/testing "group-by grpmonth = filter"
    (let [result-true [{"date" "2012", "a" 1}
                       {"date" "2012-01", "a" 1}
                       {"date" "2014-01-23", "a" 1}
                       {"date" "2015", "a" 2}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/month, :value 1}]]
                date-test-input)))))

  (t/testing "group-by empty filter"
    (let [result-true date-test-input
          result-false []]
      (t/is (= {true  result-true
                false result-false}
               (fc/group-by-data
                [:and]
                date-test-input
                ff/default-impl)))))

  (t/testing "group-by date >= filter"
    (let [result-true [{"date" "2014-02-03", "a" 1}
                       {"date" "2015", "a" 2}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and [:and #:de.explorama.shared.data-format.filter{:op :>=, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]]
                date-test-input)))))

  (t/testing "group-by date <= filter"
    (let [result-true [{"date" "2012", "a" 1}
                       {"date" "2012-01", "a" 1}
                       {"date" "2013-02"}
                       {"date" "2014-01-23", "a" 1}
                       {"date" "2014-02-03", "a" 1}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and [:and #:de.explorama.shared.data-format.filter{:op :<=, :prop :de.explorama.shared.data-format.dates/full-date, :value "2014-02-03"}]]
                date-test-input)))))

  (t/testing "group-by year > filter"
    (let [result-true [{"date" "2013-02"}
                       {"date" "2014-01-23", "a" 1}
                       {"date" "2014-02-03", "a" 1}
                       {"date" "2015", "a" 2}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and [:and #:de.explorama.shared.data-format.filter{:op :>, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]]
                date-test-input))))

    (t/testing "group-by year < filter"
      (let [result-true [{"date" "2012", "a" 1}
                         {"date" "2012-01", "a" 1}
                         {"date" "2013-02"}]
            result-false (vec (remove (set result-true) date-test-input))]
        (t/is (= {true  result-true
                  false result-false}
                 (sut/group-by-data
                  ff/default-impl
                  [:and [:and #:de.explorama.shared.data-format.filter{:op :<, :prop :de.explorama.shared.data-format.dates/year, :value 2014}]]
                  date-test-input))))))

  (t/testing "group-by year = filter"
    (let [result-true [{"date" "2012", "a" 1}
                       {"date" "2012-01", "a" 1}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]]
                date-test-input)))))

  (t/testing "group-by filter deep 0"
    (let [result-true [{"date" "2012", "a" 1}
                       {"date" "2012-01", "a" 1}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}
                date-test-input)))))

  (t/testing "group-by filter deep 1"
    (let [result-true [{"date" "2012", "a" 1}
                       {"date" "2012-01", "a" 1}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]
                date-test-input)))))

  (t/testing "group-by filter deep 3"
    (let [result-true [{"date" "2012", "a" 1}
                       {"date" "2012-01", "a" 1}]
          result-false (vec (remove (set result-true) date-test-input))]
      (t/is (= {true  result-true
                false result-false}
               (sut/group-by-data
                ff/default-impl
                [:and [:and [:and #:de.explorama.shared.data-format.filter{:op :=, :prop :de.explorama.shared.data-format.dates/year, :value 2012}]]]
                date-test-input))))))

(t/deftest filter-contains-date?-tests
  (t/is (= :de.explorama.shared.data-format.dates/full-date
           (sut/filter-contains-date? [:and [:and
                                             #:de.explorama.shared.data-format.filter{:op :>=, :prop :de.explorama.shared.data-format.dates/full-date, :value "1997-07-27"}
                                             #:de.explorama.shared.data-format.filter{:op :<=, :prop :de.explorama.shared.data-format.dates/full-date, :value "1997-07-27"}]
                                       #:de.explorama.shared.data-format.filter{:op :in
                                                                :prop :fulltext
                                                                :value #{"33b023077c5964ed91e20b1bac2887276aa964f4d109ba5bb5611d27a18afa1e"
                                                                         "9281063a53d401d75ddf4e0003c72a642f061d907480f768daa5fb88161a3875"
                                                                         "44b98b02d264c98164d4a629adc946a9b22305f25b1f823909a8afbe28bfd846"
                                                                         "ac9f6dd080352c274687e482ce8ee114fdd96084a7f58559e2aae7a154dd815e"
                                                                         "deca73b70efaa4629b121d9b54eff2b7203148a6ea582afb6e4826364914d26"
                                                                         "511acd880f8c8f08b5a3550a6cd4038e2a6aa1347ae956ed9b2589d25eba1a92"
                                                                         "483c7ca0655a94804fdb027fe8282cda81c761b602f34d14fa7478357538736b"}}]))))

(t/deftest date-special-filter-tests
  (t/is (= [{"a" 1, "date" "2014-02-03"}]
           (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :current-month
                                                           :prop :de.explorama.shared.data-format.dates/full-date
                                                           :value "2014-02"}]]
                      date-test-input)))
  (t/is (= [{"a" 1, "date" "2012"}
            {"a" 1, "date" "2012-01"}]
           (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :current-year
                                                           :prop :de.explorama.shared.data-format.dates/full-date
                                                           :value "2012"}]]
                      date-test-input)))
  (t/is (= [{"date" "2014-02-03", "a" 1}]
           (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :current-day
                                                           :prop :de.explorama.shared.data-format.dates/full-date
                                                           :value "2014-02-03"}]]
                      date-test-input)))
  (t/is (= [{"a" 1, "date" "2014-01-23"}
            {"a" 1, "date" "2014-02-03"}]
           (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :last-x-days
                                                           :prop :de.explorama.shared.data-format.dates/full-date
                                                           :value "2014-02-06"
                                                           :extra-val 30}]]
                      date-test-input)))
  (let [earlier (time/minus (time/now) (time/days 20))
        date-string (unparse earlier)
        event {"a" 1, "date" date-string}]
    (t/is (= [event]
             (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :last-x-days
                                                             :prop :de.explorama.shared.data-format.dates/full-date
                                                             :value "today"
                                                             :extra-val 30}]]
                        (conj date-test-input event)))))
  (let [earlier (time/minus (time/now) (time/months 3))
        date-string (unparse earlier)
        event {"a" 1, "date" date-string}]
    (t/is (= [event]
             (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :last-x-months
                                                             :prop :de.explorama.shared.data-format.dates/full-date
                                                             :value "today"
                                                             :extra-val 3}]]
                        (conj date-test-input event)))))
  (let [earlier (time/minus (time/now) (time/years 3))
        date-string (unparse earlier)
        event {"a" 1, "date" date-string}]
    (t/is (= [event]
             (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :last-x-years
                                                             :prop :de.explorama.shared.data-format.dates/full-date
                                                             :value "today"
                                                             :extra-val 3}]]
                        (conj date-test-input event)))))
  (let [earlier (time/now)
        date-string (unparse earlier)
        event {"a" 1, "date" date-string}]
    (t/is (= [event]
             (filter-by [:and [:and #:de.explorama.shared.data-format.filter{:op :current-year
                                                             :prop :de.explorama.shared.data-format.dates/full-date
                                                             :value "today"}]]
                        (conj date-test-input event))))))