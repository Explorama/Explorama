(ns de.explorama.shared.data-format.aggregations-test
  (:require #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])
            [de.explorama.shared.data-format.aggregations :as agg]
            [de.explorama.shared.data-format.operations :as dfl-op]
            [de.explorama.shared.common.test-data :as td]))

(def test-data
  (flatten (concat (vals td/country-a-datasource-a-data)
                   (vals td/country-b-datasource-a-data))))

(t/deftest number-of-events-agg
  (let [number-of-events-dfl-op (get-in agg/descs [:number-of-events :dfl-op])
        number-of-events-attribute (get-in agg/descs [:number-of-events :attribute])
        numbers-only-fn (fn [data grouping-attributes]
                          (dfl-op/perform-operation
                           {"di1" data} {}
                           (conj (conj number-of-events-dfl-op nil)
                                 [:group-by {:attributes grouping-attributes}
                                  "di1"])))
        with-data-fn (fn [data grouping-attributes]
                       (dfl-op/perform-operation
                        {"di1" data} {}
                        [:heal-event
                         {:policy :merge
                          :descs [{:attribute number-of-events-attribute}]}
                         (conj (conj number-of-events-dfl-op nil)
                               [:group-by {:attributes grouping-attributes}
                                "di1"])]))]

    (t/testing "Number of events with number only"
      (t/is (= (numbers-only-fn test-data ["year" td/country])
               {{"date" "1997", td/country td/country-a} 2
                {"date" "1998", td/country td/country-a} 4
                {"date" "1999", td/country td/country-a} 2
                {"date" "2000", td/country td/country-a} 3
                {"date" "1997", td/country td/country-b} 1
                {"date" "1998", td/country td/country-b} 2
                {"date" "2000", td/country td/country-b} 3}))
      (t/is (= (numbers-only-fn test-data ["year"])
               {{"date" "1997"} 3
                {"date" "1998"} 6
                {"date" "1999"} 2
                {"date" "2000"} 4}))
      (t/is (= (numbers-only-fn test-data [td/country])
               {{td/country td/country-a} 11
                {td/country td/country-b} 6})))

    (t/testing "Number of events with data"
      (t/is (= (set (with-data-fn test-data ["year" td/country]))
               #{{"date" "1997", td/country td/country-a number-of-events-attribute 2}
                 {"date" "1998", td/country td/country-a number-of-events-attribute 4}
                 {"date" "1999", td/country td/country-a number-of-events-attribute 2}
                 {"date" "2000", td/country td/country-a number-of-events-attribute 3}
                 {"date" "1997", td/country td/country-b number-of-events-attribute 1}
                 {"date" "1998", td/country td/country-b number-of-events-attribute 2}
                 {"date" "2000", td/country td/country-b number-of-events-attribute 3}}))
      (t/is (= (set (with-data-fn test-data ["year"]))
               #{{"date" "1997" number-of-events-attribute 3}
                 {"date" "1998" number-of-events-attribute 6}
                 {"date" "1999" number-of-events-attribute 2}
                 {"date" "2000" number-of-events-attribute 4}}))
      (t/is (= (set (with-data-fn test-data [td/country]))
               #{{td/country td/country-a number-of-events-attribute 11}
                 {td/country td/country-b number-of-events-attribute 6}})))))

(t/deftest median-calc-test
  (t/testing "Median calc test on basic function"
    (t/is (= (float 3)
             (dfl-op/median [0 1 2 3 4 5 6])))
    (t/is (= (float 3)
             (dfl-op/median [3 6 2 0 4 5 1])))
    (t/is (= (float 3)
             (dfl-op/median (reverse [0 1 2 3 4 5 6]))))
    (t/is (= (float 3.5)
             (dfl-op/median [0 1 2 3 4 5 6 7])))
    (t/is (= (float 3.5)
             (dfl-op/median [7 1 5 3 6 0 2 4])))
    (t/is (= (float 3.5)
             (dfl-op/median (reverse [0 1 2 3 4 5 6 7]))))
    (t/is (= (float 5)
             (dfl-op/median [2 3 3 5 8 10 11])))
    (t/is (= (float 5)
             (dfl-op/median (reverse [2 3 3 5 8 10 11]))))
    (t/is (= (float 4)
             (dfl-op/median [2 2 3 3 5 7 8 130])))
    (t/is (= (float 4)
             (dfl-op/median (reverse [2 2 3 3 5 7 8 130]))))))

(t/deftest average-calc-test
  (t/testing "Median calc test on basic function"
    (t/is (= (float 3)
             (dfl-op/average [0 1 2 3 4 5 6])))
    (t/is (= (float 3)
             (dfl-op/average [3 6 2 0 4 5 1])))
    (t/is (= (float 3)
             (dfl-op/average (reverse [0 1 2 3 4 5 6]))))
    (t/is (= (float 3.5)
             (dfl-op/average [0 1 2 3 4 5 6 7])))
    (t/is (= (float 3.5)
             (dfl-op/average [7 1 5 3 6 0 2 4])))
    (t/is (= (float 3.5)
             (dfl-op/average (reverse [0 1 2 3 4 5 6 7]))))
    (t/is (= (float 6)
             (dfl-op/average [2 3 3 5 8 10 11])))
    (t/is (= (float 6)
             (dfl-op/average (reverse [2 3 3 5 8 10 11]))))
    (t/is (= (float 20)
             (dfl-op/average [2 2 3 3 5 7 8 130])))
    (t/is (= (float 20)
             (dfl-op/average (reverse [2 2 3 3 5 7 8 130]))))))

