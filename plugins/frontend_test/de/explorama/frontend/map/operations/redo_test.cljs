(ns de.explorama.frontend.map.operations.redo-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.map.operations.redo :as redo]
            [clojure.set :refer [union]]
            [de.explorama.frontend.map.paths :as geop]))

(def test-frame-id {:frame-id "test" :vertical "test" :workspace "test"})

(defn create-layer [id name attr]
  (cond-> {}
    id (assoc :id id)
    name (assoc :name name)
    attr (assoc :attribute attr)))

(def default-layer-id "default-marker")
(def default-layer (create-layer default-layer-id "Events" nil))

(def attr1 "attr1")
(def attr1-layer-id "l1")
(def attr1-layer (create-layer attr1-layer-id attr1-layer-id attr1))

(def attr2 "attr2")
(def attr2-layer-id "l2")
(def attr2-layer (create-layer attr2-layer-id attr2-layer-id attr2))

(def base-db (-> {}
                 (assoc-in geop/layers-path (list default-layer attr1-layer attr2-layer))
                 (assoc-in (geop/frame-active-layers test-frame-id)
                           {default-layer-id false
                            attr1-layer-id false
                            attr2-layer-id false})))

(def default-active-db (assoc-in base-db (geop/frame-active-layer test-frame-id default-layer-id) true))
(def attr1-active-db (assoc-in base-db (geop/frame-active-layer test-frame-id attr1-layer-id) true))
(def attr2-active-db (assoc-in base-db (geop/frame-active-layer test-frame-id attr2-layer-id) true))
(def two-active-db (-> base-db
                       (assoc-in (geop/frame-active-layer test-frame-id default-layer-id) true)
                       (assoc-in (geop/frame-active-layer test-frame-id attr2-layer-id) true)))
(def all-active-db (-> base-db
                       (assoc-in (geop/frame-active-layer test-frame-id default-layer-id) true)
                       (assoc-in (geop/frame-active-layer test-frame-id attr1-layer-id) true)
                       (assoc-in (geop/frame-active-layer test-frame-id attr2-layer-id) true)))

(def invalid-operations-example0 #{{:op :active-layer-attributes :layer-id default-layer-id}})
(def invalid-operations-example1 #{{:op :active-layer-attributes :attribute attr1 :layer-id attr1-layer-id}})
(def invalid-operations-example2 #{{:op :active-layer-attributes :attribute attr2 :layer-id attr2-layer-id}})

(deftest default-layer-redo-test
  (testing "testing default-layer redo functionality"
    (is (= (redo/build-operations-state default-active-db test-frame-id)
           {:active-layer-attributes {}}))
    (is (= (redo/remove-invalid-operations default-active-db test-frame-id invalid-operations-example0)
           default-active-db))
    (is (= (redo/remove-invalid-operations two-active-db test-frame-id invalid-operations-example0)
           attr2-active-db))))

(deftest attribute-layer-redo-test
  (testing "testing active-layer redo functionality"
    (is (= (redo/build-operations-state attr1-active-db test-frame-id)
           {:active-layer-attributes {attr1 #{attr1-layer-id}}}))
    (is (= (redo/build-operations-state attr2-active-db test-frame-id)
           (redo/build-operations-state two-active-db test-frame-id)
           {:active-layer-attributes {attr2 #{attr2-layer-id}}}))
    (is (= (redo/build-operations-state all-active-db test-frame-id)
           {:active-layer-attributes {attr1 #{attr1-layer-id}
                                      attr2 #{attr2-layer-id}}}))

    (is (= (redo/remove-invalid-operations attr1-active-db test-frame-id invalid-operations-example0)
           (redo/remove-invalid-operations attr1-active-db test-frame-id invalid-operations-example2)
           attr1-active-db))
    (is (= (redo/remove-invalid-operations attr1-active-db test-frame-id invalid-operations-example1)
           default-active-db))
    (is (= (redo/remove-invalid-operations attr2-active-db test-frame-id invalid-operations-example0)
           (redo/remove-invalid-operations attr2-active-db test-frame-id invalid-operations-example1)
           attr2-active-db))
    (is (= (redo/remove-invalid-operations attr2-active-db test-frame-id invalid-operations-example2)
           default-active-db))

    (is (= (redo/remove-invalid-operations all-active-db test-frame-id invalid-operations-example1)
           two-active-db))))

(deftest show-notification-test
  (testing "testing if show-notification check works"
    (is (not (redo/show-notification? nil)))
    (is (not (redo/show-notification? #{})))
    (is (redo/show-notification? invalid-operations-example0))
    (is (redo/show-notification? invalid-operations-example1))
    (is (redo/show-notification? invalid-operations-example2))
    (is (redo/show-notification? (union invalid-operations-example0
                                        invalid-operations-example1
                                        invalid-operations-example2)))))

