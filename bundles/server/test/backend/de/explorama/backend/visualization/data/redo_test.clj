(ns de.explorama.backend.visualization.data.redo-test
  (:require [clojure.test :refer :all]
            [de.explorama.backend.visualization.data.redo :as redo]))

(def example-x-option "x-opt")
(def example-y-option "y-opt")
(def example-r-option "r-opt")
(def example-sum-by-option "sub-by-opt")
(def sum-by-value-1 "a")
(def sum-by-value-2 "b")
(def sum-by-value-3 "c")
(def example-sum-by-values #{sum-by-value-1 sum-by-value-2 sum-by-value-3})

(def attributes-value-1 "a")
(def attributes-value-2 "b")
(def attributes-value-3 "c")

(def test-ui-ops {:y-options {:groups [{:options [{:value example-y-option}]}]}
                  :x-options [{:value example-x-option}]
                  :sum-options [{:value example-sum-by-option}]
                  :wordcloud-attrs [{:value attributes-value-3}]
                  example-sum-by-option [{:value sum-by-value-1}]})

(def test-operations-state {:x-option example-x-option
                            :chart-operations [{:y-option example-y-option
                                                :r-option example-r-option
                                                :sum-by-option example-sum-by-option
                                                :sum-by-values example-sum-by-values}]})

(def test-wordcloud-op-state {:chart-operations [{:use-nlp #{attributes-value-2}
                                                  :attributes #{attributes-value-1
                                                                attributes-value-2
                                                                attributes-value-3}}]})

(def valid-operations-wordcloud [{attributes-value-3 {:op #{:attributes}
                                                      :opst-paths #{[:attributes attributes-value-3]}}}])

(def invalid-wordcloud-operations #{{:op :use-nlp :attribute attributes-value-2}
                                    {:op :attributes :attribute attributes-value-2}
                                    {:op :attributes :attribute attributes-value-1}})

(def valid-wordcloud-op-state {:attributes #{attributes-value-3}})

(def valid-operations-state {:y-option example-y-option
                             :x-option example-x-option
                             :sum-by-option example-sum-by-option
                             :sum-by-values #{sum-by-value-1}})

(def valid-operations [{example-y-option {:op #{:y-option} :opst-paths #{[0 :y-option]}}
                        example-x-option {:op #{:x-option} :opst-paths #{[0 :x-option]}}
                        example-sum-by-option {:op #{:sum-by-option}
                                               :opst-paths #{[0 :sum-by-option]
                                                             [0 :sum-by-values sum-by-value-1]}}}])

(def invalid-operations #{{:op :r-option, :attribute example-r-option, :chart-index 0}
                          {:op :sum-by-values :attribute example-sum-by-option :value sum-by-value-2}
                          {:op :sum-by-values :attribute example-sum-by-option :value sum-by-value-3}})

(deftest check-redo-test
  (testing "tests the check-redo function of redo"
    (is (= {:valid-operations valid-operations
            :invalid-operations invalid-operations
            :valid-operations-state valid-operations-state}
           (redo/charts-check-redo test-ui-ops test-operations-state))))
  (testing "tests the check-redo function for wordcloud"
    (is (= {:valid-operations valid-operations-wordcloud
            :invalid-operations invalid-wordcloud-operations
            :valid-operations-state valid-wordcloud-op-state}
           (redo/charts-check-redo test-ui-ops test-wordcloud-op-state)))))