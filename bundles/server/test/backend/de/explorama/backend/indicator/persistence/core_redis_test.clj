(ns de.explorama.backend.indicator.persistence.core-redis-test
  (:require [clojure.test :as test :refer [compose-fixtures deftest is testing
                                           use-fixtures]]
            [de.explorama.backend.mocks.redis :as redis-mock]
            [de.explorama.backend.indicator.persistence.backend.core :as backend-core]
            [de.explorama.backend.indicator.persistence.core :as persistence]
            [de.explorama.backend.indicator.persistence.util-test
             :refer [indicator-1 indicator-2 indicator-3 mmeier PAdmin]]))

(defn test-setup [test-fn]
  (backend-core/new-instance)
  (test-fn))

(use-fixtures :each (compose-fixtures redis-mock/fixture test-setup))

(deftest simple-test
  (testing "create indicators"
    (is (= :success
           (:status (persistence/create-new-indicator PAdmin indicator-1))))
    (is (= :success
           (:status (persistence/create-new-indicator PAdmin indicator-2))))
    (is (= :success
           (:status (persistence/create-new-indicator mmeier indicator-3)))))
  (testing "share with user"
    (is (= :failed
           (:status (persistence/share-with-user mmeier mmeier indicator-1))))
    (let [{:keys [status data]} (persistence/share-with-user PAdmin mmeier indicator-1)]
      (is (= :success status))
      (is (= "MMeier" (:creator data)))
      (is (= "PAdmin" (:shared-by data)))))
  (testing "delete indicator"
    (is (= :success
           (:status (persistence/delete-indicator PAdmin indicator-1))))
    (is (= :success
           (:status (persistence/delete-indicator mmeier indicator-3)))))
  (testing "listing indicators"
    (is (= 1
           (count (persistence/all-user-indicators PAdmin))))
    (is (= 1
           (count (persistence/all-user-indicators mmeier))))))
