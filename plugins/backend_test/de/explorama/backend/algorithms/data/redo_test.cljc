(ns de.explorama.backend.algorithms.data.redo-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.algorithms.data.redo :as redo]))

(def problem-type1 :pt1)

(def attr1 "attr1")
(def attr2 "attr2")
(def attr3 "attr3")

(def header1 (list attr1))
(def header2 (list attr1 attr2 attr3))
(def future-header1 (list attr1))
(def future-header2 (list attr1 attr2 attr3))

(def empty-operations-state {})
(def operations-state-header-1 {:problem-type problem-type1
                                :header (set header1)})
(def operations-state-header-2 {:problem-type problem-type1
                                :header (set header2)})

(def operations-state-future-header-1 {:problem-type problem-type1
                                       :future-header (set future-header1)})
(def operations-state-future-header-2 {:problem-type problem-type1
                                       :future-header (set future-header2)})

(def invalid-operation-header-attr1 {:op :header :attribute attr1})
(def invalid-operation-header-attr3 {:op :header :attribute attr3})
(def invalid-operation-future-header-attr1 {:op :future-header :attribute attr1})
(def invalid-operation-future-header-attr3 {:op :future-header :attribute attr3})

(def attribute-set-empty #{})
(def attribute-set-attr1 #{attr1})
(def attribute-set-att1-attr2 #{attr1 attr2})

(deftest check-redo-test-valid
  (testing "tests the check-redo function of redo for valid-constellations (= all valid)"
    ;; with attribute-set-empty
    (is (= (redo/check-redo attribute-set-empty empty-operations-state)
           {:valid-operations {}
            :invalid-operations #{}
            :valid-operations-state {}}))
    ;; with attribute-set-attr1 and attribute-set-att1-attr2
    (is (= (redo/check-redo attribute-set-attr1 operations-state-header-1)
           (redo/check-redo attribute-set-att1-attr2 operations-state-header-1)
           {:valid-operations {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}
                               attr1 {:op #{:header} :opst-paths #{[:header attr1]}}}
            :invalid-operations #{}
            :valid-operations-state operations-state-header-1}))
    (is (= (redo/check-redo attribute-set-attr1 operations-state-future-header-1)
           (redo/check-redo attribute-set-att1-attr2 operations-state-future-header-1)
           {:valid-operations {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}
                               attr1 {:op #{:future-header} :opst-paths #{[:future-header attr1]}}}
            :invalid-operations #{}
            :valid-operations-state operations-state-future-header-1}))

    (is (= (redo/check-redo attribute-set-attr1 (merge operations-state-header-1
                                                       operations-state-future-header-1))
           (redo/check-redo attribute-set-att1-attr2 (merge operations-state-header-1
                                                            operations-state-future-header-1))
           {:valid-operations {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}
                               attr1 {:op #{:header :future-header} :opst-paths #{[:header attr1]
                                                                                  [:future-header attr1]}}}
            :invalid-operations #{}
            :valid-operations-state (merge operations-state-header-1
                                           operations-state-future-header-1)}))))

(deftest check-redo-test-attr1-invalid
  (let [valid-operations-result {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}}]
    (testing "tests the check-redo function of redo for invalid-constellations of attr1 (attr1 not available)"
      (is (= (redo/check-redo attribute-set-empty operations-state-header-1)
             {:valid-operations valid-operations-result
              :invalid-operations #{invalid-operation-header-attr1}
              :valid-operations-state (dissoc operations-state-header-1 :header)}))
      (is (= (redo/check-redo attribute-set-empty operations-state-future-header-1)
             {:valid-operations valid-operations-result
              :invalid-operations #{invalid-operation-future-header-attr1}
              :valid-operations-state (dissoc operations-state-future-header-1 :future-header)}))

      (is (= (redo/check-redo attribute-set-empty (merge operations-state-header-1
                                                         operations-state-future-header-1))
             {:valid-operations valid-operations-result
              :invalid-operations #{invalid-operation-header-attr1
                                    invalid-operation-future-header-attr1}
              :valid-operations-state (dissoc (merge operations-state-header-1
                                                     operations-state-future-header-1)
                                              :header :future-header)})))))

(deftest check-redo-test-attr3-invalid
  (testing "tests the check-redo function of redo for invalid-constellations of attr3 (attr3 not available) and attr1 valid"
    (is (= (redo/check-redo attribute-set-att1-attr2 operations-state-header-2)
           {:valid-operations {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}
                               attr1 {:op #{:header} :opst-paths #{[:header attr1]}}
                               attr2 {:op #{:header} :opst-paths #{[:header attr2]}}}
            :invalid-operations #{invalid-operation-header-attr3}
            :valid-operations-state (update operations-state-header-2 :header disj attr3)}))
    (is (= (redo/check-redo attribute-set-att1-attr2 operations-state-future-header-2)
           {:valid-operations {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}
                               attr1 {:op #{:future-header} :opst-paths #{[:future-header attr1]}}
                               attr2 {:op #{:future-header} :opst-paths #{[:future-header attr2]}}}
            :invalid-operations #{invalid-operation-future-header-attr3}
            :valid-operations-state (update operations-state-future-header-2 :future-header disj attr3)}))
    (is (= (redo/check-redo attribute-set-att1-attr2 (merge operations-state-header-2
                                                            operations-state-future-header-2))
           {:valid-operations {problem-type1 {:op #{:problem-type} :opst-paths #{[:problem-type]}}
                               attr1 {:op #{:header :future-header} :opst-paths #{[:header attr1]
                                                                                  [:future-header attr1]}}
                               attr2 {:op #{:header :future-header} :opst-paths #{[:header attr2]
                                                                                  [:future-header attr2]}}}
            :invalid-operations #{invalid-operation-header-attr3
                                  invalid-operation-future-header-attr3}
            :valid-operations-state (-> (merge operations-state-header-2
                                               operations-state-future-header-2)
                                        (update :header disj attr3)
                                        (update :future-header disj attr3))}))))

