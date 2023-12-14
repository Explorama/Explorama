(ns de.explorama.frontend.algorithms.components.parameter-test
  (:require [de.explorama.frontend.algorithms.components.parameter :as p]
            [cljs.test :refer-macros [deftest testing is]]))

(deftest get-power-test
  (testing "Testing get power function"
    (is (= (p/get-power 1.8)
           "0.1"))
    (is (= (p/get-power 10.08)
           "0.01"))
    (is (= (p/get-power 0.00000000006)
           "0.00000000001"))
    (is (= (p/get-power 1)
           "0.1"))))

(deftest number-boundaries-test
  (testing "Testing get power function"
    (is (p/min-number-boundaries :zero)
        0)
    (is (p/min-number-boundaries :greater-zero)
        0)
    (is (p/min-number-boundaries [:zero [:<= 1]])
        0)
    (is (p/min-number-boundaries [:greater-zero [:<= 1]])
        0)
    (is (p/min-number-boundaries [[:< 1] [:< 2]])
        2)
    (is (p/max-number-boundaries [:greater-zero [:< 2]])
        2)
    (is (p/max-number-boundaries [[:< -1] :zero])
        0)))