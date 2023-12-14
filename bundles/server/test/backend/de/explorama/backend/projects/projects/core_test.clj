(ns de.explorama.backend.projects.projects.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [de.explorama.backend.projects.projects.core :as core]))

(defn get-relative-date [date offset]
  (java.util.Date. (+ (.getTime date) offset)))

(deftest test-compare-snapshots
  (let [yesterday (get-relative-date (java.util.Date.) (* -1000 3600 24))
        snapshot-1 {:snapshot-id 1 :snapshot-desc {:date-obj (get-relative-date yesterday 1000)} :head {:c 10
                                                                                                        :t (.getTime (get-relative-date yesterday 1000))}}
        snapshot-2 {:snapshot-id 2 :snapshot-desc {:date-obj (get-relative-date yesterday 2000)} :head {:c 20
                                                                                                        :t (.getTime (get-relative-date yesterday 2000))}}
        snapshot-3 {:snapshot-id 3 :snapshot-desc {:date-obj (get-relative-date yesterday 3000)} :head {:c 20
                                                                                                        :t (.getTime (get-relative-date yesterday 3000))}}
        snapshot-4 {:snapshot-id 4 :snapshot-desc {:date-obj (get-relative-date yesterday 4000)} :head {:c 20
                                                                                                        :t (.getTime (get-relative-date yesterday 4000))}}
        snapshot-5 {:snapshot-id 5 :snapshot-desc {:date-obj (get-relative-date yesterday 5000)} :head {:c 30
                                                                                                        :t (.getTime (get-relative-date yesterday 5000))}}]
    (testing "newer snapshot is recognized as newer"
      (is (< (core/compare-snapshots snapshot-1 snapshot-2) 0))
      (is (< (core/compare-snapshots snapshot-1 snapshot-3) 0))
      (is (< (core/compare-snapshots snapshot-1 snapshot-4) 0))
      (is (< (core/compare-snapshots snapshot-1 snapshot-5) 0))
      (is (< (core/compare-snapshots snapshot-2 snapshot-3) 0))
      (is (< (core/compare-snapshots snapshot-2 snapshot-4) 0)))
    (testing "older snapshot is not recognized as older"
      (is (> (core/compare-snapshots snapshot-5 snapshot-1) 0))
      (is (> (core/compare-snapshots snapshot-5 snapshot-2) 0))
      (is (> (core/compare-snapshots snapshot-5 snapshot-3) 0))
      (is (> (core/compare-snapshots snapshot-5 snapshot-4) 0))
      (is (> (core/compare-snapshots snapshot-3 snapshot-1) 0))
      (is (> (core/compare-snapshots snapshot-3 snapshot-2) 0)))
    (testing "same snapshot is not recognized as same"
      (is (= (core/compare-snapshots snapshot-1 snapshot-1) 0))
      (is (= (core/compare-snapshots snapshot-2 snapshot-2) 0))
      (is (= (core/compare-snapshots snapshot-3 snapshot-3) 0))
      (is (= (core/compare-snapshots snapshot-4 snapshot-4) 0))
      (is (= (core/compare-snapshots snapshot-5 snapshot-5) 0)))))

