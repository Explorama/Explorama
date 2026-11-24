(ns de.explorama.frontend.common.i18n-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.frontend.common.i18n :as sut]))

(def ^:private labels {"temperature" "Temperature"})

(deftest attribute-label-with-unit-test
  (testing "exactly one unit is appended"
    (is (= "Temperature (°C)"
           (sut/attribute-label-with-unit labels {"temperature" #{"°C"}} "temperature"))))
  (testing "no unit leaves the label bare"
    (is (= "Temperature"
           (sut/attribute-label-with-unit labels {} "temperature"))))
  (testing "several units leave the label bare"
    (is (= "Temperature"
           (sut/attribute-label-with-unit labels {"temperature" #{"°C" "°F"}} "temperature"))))
  (testing "an unlabelled attribute falls back to its name"
    (is (= "population (people)"
           (sut/attribute-label-with-unit labels {"population" #{"people"}} "population")))))

(deftest unit-suffix-test
  (testing "exactly one unit gives the suffix"
    (is (= " (°C)"
           (sut/unit-suffix {"temperature" #{"°C"}} "temperature"))))
  (testing "no unit gives nil"
    (is (nil? (sut/unit-suffix {} "temperature"))))
  (testing "several units give nil"
    (is (nil? (sut/unit-suffix {"temperature" #{"°C" "°F"}} "temperature")))))
