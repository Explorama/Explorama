(ns de.explorama.backend.indicator.persistence-test
  (:require [clojure.test :as test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.indicator.persistence.core :as persistence]
            [de.explorama.backend.indicator.persistence.store.core :as store]))

(use-fixtures :once (fn [f]
                      (store/new-instance)
                      (f)))

(def PAdmin {:username "PAdmin"
             :role "admin"})
(def mmeier {:username "MMeier"
             :role "data-expert"})

(def indicator-1-id "1uia")
(def indicator-2-id "1uiaua23")
(def indicator-1 {:id indicator-1-id
                  :creator "PAdmin"
                  :name "Indicator 1"
                  :dis {"di-1" "foo"}
                  :calculation-desc []
                  :group-attributes ["country" "year"]})
(def indicator-2 {:id indicator-2-id
                  :creator "PAdmin"
                  :name "Indicator 1"
                  :dis {"di-1" "foo"}
                  :calculation-desc []
                  :group-attributes ["country" "year"]})
(def indicator-3-id "2hfd")
(def indicator-3 {:id indicator-3-id
                  :creator "MMeier"
                  :name "Indicator 2"
                  :dis {"di-1" "foo"}
                  :calculation-desc []
                  :group-attributes ["country" "year"]})

(deftest simple-test
  (testing "create indicators"
    (is (= :success
           (:status (persistence/create-new-indicator PAdmin indicator-1))))
    (is (= :success
           (:status (persistence/create-new-indicator PAdmin indicator-2))))
    (is (= :success
           (:status (persistence/create-new-indicator mmeier indicator-3)))))
  (testing "listing indicators"
    (is (= 2
           (count (persistence/all-user-indicators PAdmin))))
    (is (= 1
           (count (persistence/all-user-indicators mmeier)))))
  (testing "list all indicator"
    (is (= (set (store/list-indicators))
           #{indicator-1 indicator-2 indicator-3})))
  (testing "delete indicator"
    (is (= :success
           (:status (persistence/delete-indicator PAdmin indicator-1))))
    (is (= :success
           (:status (persistence/delete-indicator mmeier indicator-3)))))
  (testing "listing indicators"
    (is (= 1
           (count (persistence/all-user-indicators PAdmin))))
    (is (= 0
           (count (persistence/all-user-indicators mmeier)))))
  (testing "list all indicator"
    (is (= (set (store/list-indicators))
           #{indicator-2})))
  (testing "User for indicator id"
    (is (= (store/user-for-indicator-id indicator-2-id)
           (:username PAdmin)))))
