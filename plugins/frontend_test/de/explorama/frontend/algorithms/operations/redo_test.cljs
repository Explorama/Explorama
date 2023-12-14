(ns de.explorama.frontend.algorithms.operations.redo-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.algorithms.operations.redo :as redo]
            [de.explorama.frontend.algorithms.path.core :as path]))

(def test-frame-id {:frame-id "test" :vertical "test" :workspace "test"})

(defn- problem-def [problem-type algorithm]
  (cond-> {}
    problem-type (assoc :problem-type problem-type)
    algorithm (assoc :algorithm algorithm)))

(def problem-type1 :pt1)
(def pt1-algorithms (list :a :b :c :d))
(def problem-type2 :pt2)
(def pt2-algorithms (list :e :f :g))

;; When user selects an problem-type (no specific algorithm)
(def problem-def-pt1 (problem-def problem-type1 nil))
(def problem-def-pt2 (problem-def problem-type2 nil))
;; When user selects an specific algorithm
(def problem-def-pt1-algo (problem-def nil (first pt1-algorithms)))
(def problem-def-pt2-algo (problem-def nil (last pt2-algorithms)))

(def test-problem-types {problem-type1 {:algorithms pt1-algorithms}
                         problem-type2 {:algorithms pt2-algorithms}})

(def attr1 "attr1")
(def attr2 "attr2")
(def attr3 "attr3")

(def empty-header nil)
(def header1 (list attr1))
(def header2 (list attr1 attr2 attr3))

(def empty-future-header nil)
(def future-header1 (list attr1))
(def future-header2 (list attr1 attr2 attr3))

(def base-db (assoc-in {} path/problem-types test-problem-types))

(defn- add-training-data [db problem-def header future-header]
  (let [db (or db {})
        training-data (cond-> {}
                        header (assoc :header header)
                        future-header (assoc :future-header future-header))]
    (assoc-in db
              (conj (path/training-data test-frame-id)
                    problem-def)
              training-data)))

(defn- do-for-problem-types [header future-header result-operations-state]
  (let [build-operations-state (fn [problem-def]
                                 (redo/build-operations-state
                                  (add-training-data base-db problem-def header future-header)
                                  test-frame-id))]
    (is (= (build-operations-state problem-def-pt1)
           (assoc result-operations-state :problem-type problem-type1)))
    (is (= (build-operations-state problem-def-pt1-algo)
           (assoc result-operations-state :problem-type problem-type1)))
    (is (= (build-operations-state problem-def-pt2)
           (assoc result-operations-state :problem-type problem-type2)))
    (is (= (build-operations-state problem-def-pt2-algo)
           (assoc result-operations-state :problem-type problem-type2)))))

(deftest empty-headers-redo-test
  (testing "testing empty-headers for redo functionality"
    (do-for-problem-types empty-header
                          empty-future-header
                          {})))

(deftest headers-redo-test
  (testing "testing headers redo functionality"
    (do-for-problem-types header1 empty-future-header
                          {:header (set header1)})
    (do-for-problem-types header2 empty-future-header
                          {:header (set header2)}))

  (testing "testing future-headers redo functionality"
    (do-for-problem-types empty-header future-header1
                          {:future-header (set future-header1)})
    (do-for-problem-types empty-header future-header2
                          {:future-header (set future-header2)}))

  (testing "testing mixed-headers redo functionality"
    (do-for-problem-types header1 future-header1
                          {:header (set header1)
                           :future-header (set future-header1)})
    (do-for-problem-types header1 future-header2
                          {:header (set header1)
                           :future-header (set future-header2)})
    (do-for-problem-types header2 future-header1
                          {:header (set header2)
                           :future-header (set future-header1)})
    (do-for-problem-types header2 future-header2
                          {:header (set header2)
                           :future-header (set future-header2)})))

(deftest show-notification-test
  (testing "testing if show-notification check works"
    (is (not (redo/show-notification? nil)))
    (is (not (redo/show-notification? #{})))
    (is (redo/show-notification? #{{:op :header}}))
    (is (redo/show-notification? #{{:op :future-header}}))
    (is (redo/show-notification? #{{:op :header} {:op :future-header}}))))

