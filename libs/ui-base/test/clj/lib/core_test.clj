(ns lib.core-test
  (:require [clojure.test :refer :all]
            [de.explorama.frontend.ui-base.core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 1))
    (is true)))
