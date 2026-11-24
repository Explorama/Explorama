(ns de.explorama.shared.common.unification.time-test
  (:require [clojure.test :as t :include-macros true]
            [de.explorama.shared.common.unification.time :as sut]))

(t/deftest parse-unparse-roundtrips
  (t/testing "protocol objects at day, month and year precision"
    (t/is (= "2023-06-15" (sut/obj->date-str :day (sut/date-str->obj false :day "2023-06-15"))))
    (t/is (= "2023-06" (sut/obj->date-str :month (sut/date-str->obj false :month "2023-06"))))
    (t/is (= "2023" (sut/obj->date-str :year (sut/date-str->obj false :year "2023")))))
  (t/testing "native date objects roundtrip through from-date"
    (t/is (= "2023-06-15" (sut/obj->date-str :day (sut/date-str->obj :day "2023-06-15"))))))

(t/deftest date-construction
  (t/is (= "2023-06-15" (sut/obj->date-str :day (sut/date-time 2023 6 15))))
  (t/is (= 6 (sut/get-month (sut/date-time 2023 6 15))))
  (t/is (= 2023 (sut/get-year (sut/date-time 2023 6 15)))))

(t/deftest comparisons
  (let [a (sut/date-str->obj false :day "2023-01-01")
        b (sut/date-str->obj false :day "2023-01-02")]
    (t/is (sut/before? a b))
    (t/is (sut/after? b a))
    (t/is (sut/equal? a (sut/date-str->obj false :day "2023-01-01")))
    (t/is (not (sut/before? b a)))))

(t/deftest native-comparisons
  (t/is (sut/before? (sut/date-str->obj :day "2023-01-01")
                     (sut/date-str->obj :day "2023-01-02"))))

(t/deftest days-in-month
  (t/is (= 29 (sut/get-days-in-month 2 2024)))
  (t/is (= 28 (sut/get-days-in-month 2 2023)))
  (t/is (= 31 (sut/get-days-in-month 1 2023))))

(t/deftest long-roundtrip
  (t/is (= 1700000000000 (sut/to-long (sut/from-long 1700000000000)))))

(t/deftest same-day
  (t/is (true? (sut/is-same-day? (sut/date-str->obj :day "2023-06-15")
                                 (sut/date-str->obj :day "2023-06-15"))))
  (t/is (false? (sut/is-same-day? (sut/date-str->obj :day "2023-06-15")
                                  (sut/date-str->obj :day "2023-06-16")))))

(t/deftest earliest-latest-over-collections
  (let [dates (mapv #(sut/date-str->obj false :day %)
                    ["2023-03-01" "2023-01-15" "2023-12-31"])]
    (t/is (= "2023-01-15" (sut/obj->date-str :day (sut/earliest dates))))
    (t/is (= "2023-12-31" (sut/obj->date-str :day (sut/latest dates))))))

(t/deftest range-filtering
  (t/is (= ["2023-06-15"]
           (vec (sut/filter-date-ranges "2023-06-01" "2023-06-30"
                                        ["2023-05-31" "2023-06-15" "2023-07-01"] true)))))

(t/deftest formatters-table
  (t/is (some? (sut/formatters :basic-date-time-no-ms)))
  (t/is (some? (sut/formatters :date-hour-minute-second)))
  (t/is (some? (sut/formatters :year-month-day))))

(t/deftest to-long-coercions
  (t/is (nil? (sut/to-long nil)))
  (t/is (= 1700000000000 (sut/to-long 1700000000000)))
  (t/is (= 1686787200000 (sut/to-long "2023-06-15"))))

(t/deftest lenient-numeric-widths
  (t/is (= "2018-09-04"
           (sut/unparse sut/day-formatter
                        (sut/parse (sut/formatter "dd.MM.yyyy") "4.9.2018")))))

(t/deftest field-accessors
  (let [dt (sut/date-time 2023 6 15 14 30 45)]
    (t/is (= 15 (sut/get-day dt)))
    (t/is (= 14 (sut/get-hour dt)))
    (t/is (= 30 (sut/get-minute dt)))
    (t/is (= 45 (sut/get-second dt)))))

(t/deftest week-and-weekday
  (t/is (= 24 (sut/week-number-of-year (sut/date-time 2023 6 15))))
  (t/is (= 52 (sut/week-number-of-year (sut/date-time 2023 1 1))))
  (t/is (= 4 (sut/day-of-week (sut/date-time 2023 6 15)))))

(t/deftest date-arithmetic
  (let [dt (sut/date-time 2023 3 31)]
    (t/is (= "2023-03-01" (sut/obj->date-str :day (sut/minus-days dt 30))))
    (t/is (= "2023-02-28" (sut/obj->date-str :day (sut/minus-months dt 1))))
    (t/is (= "2022-03-31" (sut/obj->date-str :day (sut/minus-years dt 1))))))

(t/deftest month-boundaries
  (t/is (= "2024-02-01" (sut/obj->date-str :day (sut/first-day-of-the-month (sut/date-time 2024 2 15)))))
  (t/is (= "2024-02-29" (sut/obj->date-str :day (sut/last-day-of-the-month (sut/date-time 2024 2 15)))))
  (t/is (= "2023-02-28" (sut/obj->date-str :day (sut/last-day-of-the-month (sut/date-time 2023 2 15))))))

(t/deftest today-midnight
  (let [dt (sut/today-at-midnight)]
    (t/is (= 0 (sut/get-hour dt)))
    (t/is (= 0 (sut/get-minute dt)))
    (t/is (= (sut/get-year (sut/now)) (sut/get-year dt)))))

(t/deftest within-range
  (let [start (sut/date-str->obj false :day "2023-06-01")
        end (sut/date-str->obj false :day "2023-06-30")]
    (t/is (true? (sut/within? start end (sut/date-str->obj false :day "2023-06-15"))))
    (t/is (true? (sut/within? start end start)))
    (t/is (false? (sut/within? start end end)))
    (t/is (false? (sut/within? start end (sut/date-str->obj false :day "2023-07-01"))))))

(t/deftest fraction-formatter
  (t/is (some? (sut/formatters :date-hour-minute-second-fraction)))
  (t/is (= "2023-06-15T14:30:45.123"
           (sut/unparse (sut/formatters :date-hour-minute-second-fraction)
                        (sut/from-long 1686839445123)))))

(t/deftest date-time-partial-arities
  (t/is (= "2023-06-01" (sut/obj->date-str :day (sut/date-time 2023 6))))
  (t/is (= "2023-01-01" (sut/obj->date-str :day (sut/date-time 2023)))))

(t/deftest parse-time-carrying-schemas
  (let [dt (sut/parse (sut/formatter "dd/MM/yyyy hh:mm a") "15/06/2023 10:20 PM")]
    (t/is (= "2023-06-15" (sut/obj->date-str :day dt)))
    (t/is (= 22 (sut/get-hour dt)))))
