(ns de.explorama.backend.indicator.persistence.redis-test
  (:require [clojure.test :as test :refer [compose-fixtures deftest is testing
                                           use-fixtures]]
            [de.explorama.backend.mocks.redis :as redis-mock]
            [de.explorama.backend.indicator.persistence.backend.adapter :as adapter]
            [de.explorama.backend.indicator.persistence.backend.core :as core]
            [de.explorama.backend.indicator.persistence.backend.redis :as redis-backend]
            [de.explorama.backend.indicator.persistence.util-test
             :refer [indicator-1 indicator-1-id indicator-2 indicator-2-id
                     indicator-3 indicator-3-id indicator-redis-exist mmeier
                     PAdmin]]))

(defn test-setup [test-fn]
  (redis-backend/new-instance)
  (test-fn))

(use-fixtures :each (compose-fixtures redis-mock/fixture test-setup))

(def PAdmin-name (:username PAdmin))
(def mmeier-name (:username mmeier))

(defn- get-index []
  @(.-index @core/instance))

(deftest simple-test
  (testing "write indicators"
    (adapter/write-indicator @core/instance indicator-1)
    (is (and (get-in (get-index) [PAdmin-name indicator-1-id])
             (indicator-redis-exist indicator-1)))
    (adapter/write-indicator @core/instance indicator-2)
    (is (and (get-in (get-index) [PAdmin-name indicator-2-id])
             (indicator-redis-exist indicator-2)))
    (adapter/write-indicator @core/instance indicator-3)
    (is (and (get-in (get-index) [mmeier-name indicator-3-id])
             (indicator-redis-exist indicator-3))))
  (testing "read indicators"
    (is (= indicator-1
           (adapter/read-indicator @core/instance PAdmin indicator-1-id)))
    (is (= indicator-2
           (adapter/read-indicator @core/instance PAdmin indicator-2-id)))
    (is (= indicator-3
           (adapter/read-indicator @core/instance mmeier indicator-3-id))))
  (testing "creator for id"
    (is (= {:username PAdmin-name}
           (adapter/user-for-indicator-id @core/instance indicator-1-id)))
    (is (= {:username mmeier-name}
           (adapter/user-for-indicator-id @core/instance indicator-3-id)))
    (is (nil? (adapter/user-for-indicator-id @core/instance "foo"))))
  (testing "delete indicator"
    (adapter/delete-indicator @core/instance PAdmin indicator-1-id)
    (is (and (nil? (get-in (get-index) [PAdmin-name indicator-1-id]))
             (not (indicator-redis-exist indicator-1))))))