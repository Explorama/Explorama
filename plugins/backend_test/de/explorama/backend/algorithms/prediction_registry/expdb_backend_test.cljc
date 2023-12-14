(ns de.explorama.backend.algorithms.prediction-registry.expdb-backend-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [de.explorama.backend.expdb.middleware.simple-db-test :refer [test-setup]]
            [de.explorama.backend.algorithms.prediction-registry.adapter :as adapter]
            [de.explorama.backend.algorithms.prediction-registry.expdb-backend :as expdb-backend]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.common.unification.time :refer [current-ms
                                                                 formatters
                                                                 from-long unparse]]))

(def date-formater (formatters :date-hour-minute-second))

(defn date-string-from-timestamp [timestamp]
  (unparse date-formater (from-long timestamp)))

(defn list-pred-desc [prediction]
  (select-keys prediction [:prediction-id
                           :prediction-name]))

(def username "PAdmin")
(def username-2 "Ahrrrhab")

;;; First User Predictions
(def pred-time-1 (current-ms))
(def pred-data-name-1 (str "Test - " (date-string-from-timestamp pred-time-1)))
(def pred-id-1 (cljc-uuid))
(def prediction-task-1 {:task-id pred-id-1
                        :prediction-name pred-data-name-1
                        :prediction-id pred-id-1
                        :foo "bar"
                        :content "iunar"})

(def pred-time-2 (+ (current-ms) 200000))
(def pred-data-name-2 (str "Test - " (date-string-from-timestamp pred-time-2)))
(def pred-id-2 (cljc-uuid))
(def prediction-task-2 {:task-id pred-id-2
                        :prediction-name pred-data-name-2
                        :prediction-id pred-id-2
                        :foo "bar"
                        :content "iunar"})

(def pred-time-3 (+ (current-ms) 205000))
(def pred-data-name-3 (str "Test - " (date-string-from-timestamp pred-time-3)))
(def pred-id-3 (cljc-uuid))
(def prediction-task-3 {:task-id pred-id-3
                        :prediction-name pred-data-name-3
                        :prediction-id pred-id-3
                        :foo "bar"
                        :content "iunar"})

;;; Second User predictions
(def pred-time-4 (+ (current-ms) 405000))
(def pred-data-name-4 (str "Test - " (date-string-from-timestamp pred-time-4)))
(def pred-id-4 (cljc-uuid))
(def prediction-task-4 {:task-id pred-id-4
                        :prediction-name pred-data-name-4
                        :prediction-id pred-id-4
                        :foo "bar"
                        :content "iunar"})

;;; Tests
(deftest test-expdb-backend
  (let [reg-instance (expdb-backend/new-instance)]
    (testing
     "Retrive with nil."
      (is (nil? (adapter/retrive-prediction reg-instance username pred-id-1)))
      (is (nil? (adapter/retrive-prediction reg-instance username pred-id-2)))
      (is (nil? (adapter/retrive-prediction reg-instance username pred-id-3)))
      (is (nil? (adapter/retrive-prediction reg-instance username-2 pred-id-4))))

    (println "Store Test-Prediction-Tasks")
    (adapter/store-prediction reg-instance username prediction-task-1)
    (adapter/store-prediction reg-instance username prediction-task-2)
    (adapter/store-prediction reg-instance username prediction-task-3)
    (adapter/store-prediction reg-instance username-2 prediction-task-4)
    (println "Retrive Test-Prediction-Tasks")
    (testing
     "Retrive Stored Tasks"
      (is (= prediction-task-1 (adapter/retrive-prediction reg-instance username pred-id-1)))
      (is (= prediction-task-2 (adapter/retrive-prediction reg-instance username pred-id-2)))
      (is (= prediction-task-3 (adapter/retrive-prediction reg-instance username pred-id-3)))
      (is (= prediction-task-4 (adapter/retrive-prediction reg-instance username-2 pred-id-4))))
    (println "List Test-Tasks")
    (let [predictions (adapter/all-predictions reg-instance)
          u1 (get predictions username)
          u2 (get predictions username-2)
          timestamps1 (filter identity (map #(get % :last-used) u1))
          timestamps2 (filter identity (map #(get % :last-used) u2))]
      (testing
       "last used timestamps"
        (is (= (count u1) (count timestamps1)))
        (is (not (some #{false} (map integer? timestamps1))))
        (is (= (count u2) (count timestamps2)))
        (is (not (some #{false} (map integer? timestamps2)))))
      (testing
       "List stored tasks"
        (is (= {username [(list-pred-desc prediction-task-1)
                          (list-pred-desc prediction-task-2)
                          (list-pred-desc prediction-task-3)]
                username-2 [(list-pred-desc prediction-task-4)]}
               (-> predictions
                   (update-in [username] #(map (fn [pred] (dissoc pred :last-used)) %))
                   (update-in [username-2] #(map (fn [pred] (dissoc pred :last-used)) %)))))))
    (println "Remove prediction by wrong user")
    (adapter/remove-prediction reg-instance username-2 pred-id-1)
    (adapter/remove-prediction reg-instance username pred-id-4)
    (testing
     "Prediction still there"
      (is (= prediction-task-1 (adapter/retrive-prediction reg-instance username pred-id-1)))
      (is (= prediction-task-4 (adapter/retrive-prediction reg-instance username-2 pred-id-4))))
    (println "Remove predictions by correct user")
    (adapter/remove-prediction reg-instance username pred-id-1)
    (adapter/remove-prediction reg-instance username-2 pred-id-4)
    (testing
     "Predictions removed"
      (is (nil? (adapter/retrive-prediction reg-instance username pred-id-1)))
      (is (nil? (adapter/retrive-prediction reg-instance username-2 pred-id-4)))
      (is (= {username [(list-pred-desc prediction-task-2)
                        (list-pred-desc prediction-task-3)]
              username-2 []}
             (-> (adapter/all-predictions reg-instance)
                 (update-in [username] #(map (fn [pred] (dissoc pred :last-used)) %))))))
    (println "Remove multiple predictions and then list all")
    (adapter/remove-predictions reg-instance username [pred-id-2 pred-id-3])
    (testing
     "List all predictions"
      (is (= {username []
              username-2 []}
             (adapter/all-predictions reg-instance))))))

(use-fixtures :each test-setup)