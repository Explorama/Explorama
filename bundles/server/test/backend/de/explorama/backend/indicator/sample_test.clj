(ns de.explorama.backend.indicator.sample-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [de.explorama.backend.indicator.data.core :as data]))

(deftest random-data-sample-test
  (testing "Lower dataset then threshold"
    (is (= 9 (count (@#'data/random-data-sample 10 (range 9))))))
  (testing "Testing higher amounts of data"
    (is (every? #(< 450 %) (map (fn [_]
                                  (count (@#'data/random-data-sample 500 (range 100000))))
                                (range 20))))))

(def test-data
  {"di-1" (mapv (fn [idx]
                  {"id" idx
                   "attra" (rand-int 100)
                   "classa" (first (shuffle ["A" "B" "C" "D" "E"]))})
                (range 300))
   "di-2" (mapv (fn [idx]
                  {"id" idx
                   "attrb" (rand-int 100)
                   "classb" (first (shuffle ["A" "B" "C" "D" "E"]))})
                (range 300))})

(def test-desc
  [:heal-event {:policy :merge
                :descs [{:attribute "indicator"}]}
   [:+ nil
    [:sum {:attribute "attra"}
     [:group-by {:attributes ["classa"]}
      "di-1"]]
    [:sum {:attribute "attrb"}
     [:group-by {:attributes ["classb"]}
      "di-2"]]]])

(deftest data-sample-test
  (testing "Testing higher amounts of data"
    (is (= 5 (count (data/data-sample 5
                                      100
                                      test-data
                                      test-desc))))))