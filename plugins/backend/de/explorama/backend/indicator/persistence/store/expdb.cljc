(ns de.explorama.backend.indicator.persistence.store.expdb
  (:require [de.explorama.backend.expdb.middleware.db :as expdb]
            [de.explorama.backend.indicator.persistence.store.adapter :as adapter]
            [de.explorama.shared.common.configs.platform-specific :as platform-specific]
            [taoensso.timbre :refer [warn error]]))

(defonce ^:private bucket "/indicator/indicators/")

(defn write-indicator [indicator]
  (expdb/set bucket (:id indicator) indicator))

(defn read-indicator [id]
  (expdb/get bucket id))

(defn list-indicators []
  (vals (expdb/get+ bucket)))

(defn list-all-user-indicators [user]
  (let [result (filterv (fn [{creator :creator}]
                          (= creator (:username user)))
                        (vals (expdb/get+ bucket)))]
    (if platform-specific/explorama-multi-user
      (do
        (warn "list-all-user-indicators is not implemented yet - works only for single user")
        result)
      result)))

(defn short-indicator-desc [id]
  (if platform-specific/explorama-multi-user
    (do
      (warn "list-all-user-indicators is not implemented yet - works only for single user")
      (select-keys (expdb/get bucket id)
                   [:id :creator :name :description :shared-by]))
    (select-keys (expdb/get bucket id)
                 [:id :creator :name :description :shared-by])))

(defn user-for-indicator-id [id]
  (let [result (->> (filter (fn [{iid :id}]
                              (= id iid))
                            (vals (expdb/get+ bucket)))
                    first
                    :creator)]
    (if platform-specific/explorama-multi-user
      (do
        (warn "list-all-user-indicators is not implemented yet - works only for single user")
        result)
      result)))

(defn delete-indicator [id]
  (expdb/del bucket id))

(deftype ExpDBBackend []
  adapter/Backend
  (write-indicator [_instance indicator]
    (write-indicator indicator))
  (read-indicator [_instance id]
    (read-indicator id))
  (list-indicators [_instance]
    (list-indicators))
  (short-indicator-desc [_instance id]
    (short-indicator-desc id))
  (list-all-user-indicators [_instance user]
    (list-all-user-indicators user))
  (user-for-indicator-id [_instance id]
    (user-for-indicator-id  id))
  (delete-indicator [_instance id]
    (delete-indicator id)))

(defn new-instance []
  (ExpDBBackend.))