(ns de.explorama.backend.indicator.persistence-test
  (:require [clojure.test :as test :refer [deftest is testing]]
            [de.explorama.backend.indicator.persistence.core :as persistence]))

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

#_;TODO r1/tests fix this test
(deftest simple-test
  (testing "create indicators"
    (is (= :success
           (:status (persistence/create-new-indicator PAdmin indicator-1))))
    (is (= :success
           (:status (persistence/create-new-indicator PAdmin indicator-2))))
    (is (= :success
           (:status (persistence/create-new-indicator mmeier indicator-3)))))
  #_
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
