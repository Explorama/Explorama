(ns de.explorama.backend.data-atlas.descriptions-test
  (:require [de.explorama.backend.common.data.descriptions :as desc]
            [clojure.test :as test :refer [deftest is]]))

(deftest empty-hierarchy
  (is (= {} (@#'desc/hierarchy nil))))

(deftest singleton-hierarchy
  (is (= {:a 1} (@#'desc/hierarchy [[[:a] 1]]))))

(deftest nested-hierarchy
  (is (= {:a {:b 1}} (@#'desc/hierarchy [[[:a :b] 1]]))))

(deftest nested-multi-hierarchy
  (is (= {:a {:b 1, :c 2}} (@#'desc/hierarchy [[[:a :b] 1] [[:a :c] 2]]))))
