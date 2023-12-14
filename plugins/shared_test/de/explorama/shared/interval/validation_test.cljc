(ns de.explorama.shared.interval.validation-test
  (:require [de.explorama.shared.common.interval.validation :as iv]
            #?(:clj [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer-macros [deftest testing is]])))

(def ^:private find-interval-overlaps #'iv/find-interval-overlaps)

(def well-defined-intervals-1 [[0 1] [1 100] [100 111]])
(def well-defined-intervals-2 [[##-Inf 1] [1 100] [100 ##Inf]])
(def well-defined-interval-with-gaps-1 [[0 1] [2 100] [100 111]])
(def well-defined-interval-with-gaps-2 [[2 100] [##-Inf 1] [111 ##Inf]])

(def ill-defined-intervals-1 [[0 2] [1 10] [0 1] [11 100]])
(def ill-defined-intervals-2 [[0 2] [##-Inf 10] [0 1] [11 ##Inf] [99 100] [11 ##Inf] [99 100] [101 200]])
(def ill-defined-intervals-3 [[##-Inf ##Inf] [##-Inf ##Inf]])

(deftest test-well-defined
  (testing "test well-defined intervals"
    (is (and (= [] (iv/interval-overlaps 0 well-defined-intervals-1))
             (= [] (iv/interval-overlaps 1 well-defined-intervals-1))
             (= [] (iv/interval-overlaps 2 well-defined-intervals-1))))
    (is (and (= [] (iv/interval-overlaps 0 well-defined-intervals-2))
             (= [] (iv/interval-overlaps 1 well-defined-intervals-2))
             (= [] (iv/interval-overlaps 2 well-defined-intervals-2))))))

(deftest test-ill-defined
  (testing "test well-defined intervals"
    (is (and (= [[:end-overlaps-with [1 10]]
                 [:contains-interval [0 1]]]
                (iv/interval-overlaps 0 ill-defined-intervals-1))
             (= [[:start-overlaps-with [0 2]]]
                (iv/interval-overlaps 1 ill-defined-intervals-1))
             (= [[:contained-in [0 2]]]
                (iv/interval-overlaps 2 ill-defined-intervals-1))
             (= []
                (iv/interval-overlaps 3 ill-defined-intervals-1)))
        (and (= [[:contained-in  [##-Inf 10]]
                 [:contains-interval [0 1]]]
                (iv/interval-overlaps 0 ill-defined-intervals-2))
             (= [[:contains-interval [0 2]]
                 [:contains-interval [0 1]]]
                (iv/interval-overlaps 1 ill-defined-intervals-2))
             (= [[:contained-in  [0 2]]
                 [:contained-in  [##-Inf 10]]]
                (iv/interval-overlaps 2 ill-defined-intervals-2))
             (= [[:contains-interval [99 100]]
                 [:duplicate [11 ##Inf]]
                 [:contains-interval [99 100]]
                 [:contains-interval [101 200]]]
                (iv/interval-overlaps 3 ill-defined-intervals-2))
             (= [[:contained-in [11 ##Inf]]
                 [:contained-in [11 ##Inf]]
                 [:duplicate [99 100]]]
                (iv/interval-overlaps 4 ill-defined-intervals-2))
             (= [[:duplicate [11 ##Inf]]
                 [:contains-interval [99 100]]
                 [:contains-interval [99 100]]
                 [:contains-interval [101 200]]]
                (iv/interval-overlaps 5 ill-defined-intervals-2))
             (= [[:contained-in [11 ##Inf]]
                 [:duplicate [99 100]]
                 [:contained-in [11 ##Inf]]]
                (iv/interval-overlaps 6 ill-defined-intervals-2))
             (= [[:contained-in [11 ##Inf]]
                 [:contained-in [11 ##Inf]]]
                (iv/interval-overlaps 7 ill-defined-intervals-2))
             (= [[:duplicate [##-Inf ##Inf]]]
                (iv/interval-overlaps 0 ill-defined-intervals-3))
             (= [[:duplicate [##-Inf ##Inf]]]
                (iv/interval-overlaps 1 ill-defined-intervals-3))))))

(deftest test-all-with-any
  (testing "check each interval against all the others"
    (is (= (into [] (repeat (count well-defined-intervals-1) []))
           (find-interval-overlaps well-defined-intervals-1)))
    (is (= (into [] (repeat (count well-defined-intervals-2) []))
           (find-interval-overlaps well-defined-intervals-2)))
    (is (= [[[:end-overlaps-with [1 10]]
             [:contains-interval [0 1]]]
            [[:start-overlaps-with [0 2]]]
            [[:contained-in [0 2]]]
            []]
           (find-interval-overlaps ill-defined-intervals-1)))
    (is (= [[[:contained-in  [##-Inf 10]]
             [:contains-interval [0 1]]]
            [[:contains-interval [0 2]]
             [:contains-interval [0 1]]]
            [[:contained-in  [0 2]]
             [:contained-in  [##-Inf 10]]]
            [[:contains-interval [99 100]]
             [:duplicate [11 ##Inf]]
             [:contains-interval [99 100]]
             [:contains-interval [101 200]]]
            [[:contained-in [11 ##Inf]]
             [:contained-in [11 ##Inf]]
             [:duplicate [99 100]]]
            [[:duplicate [11 ##Inf]]
             [:contains-interval [99 100]]
             [:contains-interval [99 100]]
             [:contains-interval [101 200]]]
            [[:contained-in [11 ##Inf]]
             [:duplicate [99 100]]
             [:contained-in [11 ##Inf]]]
            [[:contained-in [11 ##Inf]]
             [:contained-in [11 ##Inf]]]]
           (find-interval-overlaps ill-defined-intervals-2)))
    (is (= [[[:duplicate [##-Inf ##Inf]]]
            [[:duplicate [##-Inf ##Inf]]]]
           (find-interval-overlaps ill-defined-intervals-3)))))

(deftest test-overlapping-intervals?
  (testing "test the overlapping-intervals? function"
    (is (not (iv/interval-overlaps? well-defined-intervals-1)))
    (is (not (iv/interval-overlaps? well-defined-intervals-2)))
    (is (iv/interval-overlaps? ill-defined-intervals-1))
    (is (iv/interval-overlaps? ill-defined-intervals-2))
    (is (iv/interval-overlaps? ill-defined-intervals-3))))

(deftest test-check-for-gaps
  (testing "test the check-for-gaps function"
    (is (= [[1 2]] (iv/check-for-gaps well-defined-interval-with-gaps-1)))
    (is (= [[1 2] [100 111]] (iv/check-for-gaps well-defined-interval-with-gaps-2)))))
