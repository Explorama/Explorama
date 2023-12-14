(ns de.explorama.backend.visualization.cancel-test
  (:require [clojure.test :refer :all]
            [de.explorama.backend.visualization.cancel :refer :all]))

(defn wait [n]
  (Thread/sleep (* n 100)))

(use-fixtures
  :once
  (fn [test]
    (with-redefs [de.explorama.backend.visualization.cancel/cancellations (atom {})]
      (test))))

(deftest cancel-scenarios
  (testing "cancel signal discards action result"
    (is (= [nil :cancel]
           (do
             (future (wait 1)
                     (cancel :a))
             (restart-listening :a)
             (run-cancellable :a (fn []
                                   (wait 2)
                                   :result))))))

  (testing "a canceling signal before starting to listen is ignored"
    (is (= [30 false]
           (do
             (future (wait 0) (cancel :b))
             (run-cancellable :b
                              (fn [x y]
                                (wait 1)
                                (+ x y))
                              10
                              20)))))

  (testing "cancel event cancels all task after it is successfully received"
    (restart-listening :c)
    (future (wait 2)
            (cancel :c))
    (is (= [:result false]
           (run-cancellable :c (fn [] (wait 1) :result)))
        "blocks for 1 tick and delivers result")
    (is (= false
           (boolean (cancelled? :c)))
        "cancel is not in effect yet")
    (is (= [nil :cancel]
           (run-cancellable :c (fn [] (wait 2) :result)))
        "blocks for 1 tick after which cancel is in effect")
    (is (= true
           (cancelled? :c))
        "cancel is in effect now")
    (is (= [nil :cancel]
           (run-cancellable :c (fn [] (wait 1) :result)))
        "cancel is in effect, returns immediately")))







