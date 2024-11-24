(ns lib.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.ui-base.core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 1))
    (is true)))
