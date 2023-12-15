(ns de.explorama.backend.common.data.descriptions-test
  (:require [clojure.test :as test :refer [deftest is]]
            [de.explorama.backend.common.data.descriptions :as desc]))

(deftest empty-hierarchy
  (is (= {} (@#'desc/hierarchy nil))))

(deftest singleton-hierarchy
  (is (= {:a 1} (@#'desc/hierarchy [[[:a] 1]]))))

(deftest nested-hierarchy
  (is (= {:a {:b 1}} (@#'desc/hierarchy [[[:a :b] 1]]))))

(deftest nested-multi-hierarchy
  (is (= {:a {:b 1, :c 2}} (@#'desc/hierarchy [[[:a :b] 1] [[:a :c] 2]]))))
