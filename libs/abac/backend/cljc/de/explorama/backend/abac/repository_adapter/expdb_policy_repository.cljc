(ns de.explorama.backend.abac.repository-adapter.expdb-policy-repository
  (:require [de.explorama.backend.abac.repository-adapter.adapter :as adapter]
            [de.explorama.backend.expdb.middleware.db :as db]))

(def ^:private bucket "/abac/repository_adapter/")

(defn- expdb-key [id]
  (str id))

(defn add-policies-update-index [policies]
  (mapv (fn [[id val]]
          (db/set bucket (expdb-key id) val))
        policies))

(defn init* [data should-overwrite?]
  (when should-overwrite?
    (db/del-bucket bucket))
  (add-policies-update-index data))

(defn fetch* [id]
  (db/get bucket (expdb-key id)))

(defn fetch-multiple* [ids]
  (let [ids (if (empty? ids)
              (db/get+ bucket)
              (mapv (partial expdb-key) ids))]
    (into {}
          (map (fn [id]
                 (let [{pol-id :id
                        :as pol} (db/get bucket id)]
                   [pol-id pol])))
          ids)))

(defn edit* [id data]
  (db/set bucket (expdb-key id) data)
  data)

(defn edit-all* [data]
  (mapv (fn [[id val]]
          (db/set bucket (expdb-key id) val))
        data)
  data)

(defn create* [id data]
  (add-policies-update-index [[id data]])
  data)

(defn delete* [id]
  (let [result (db/del bucket (expdb-key id))]
    (when result
      id)))

(deftype ExpDB []
  adapter/PolicyAdapter
  (init [_ data should-overwrite?]
    (init* data should-overwrite?))
  (fetch [_ id]
    (fetch* id))
  (fetch-multiple [_ ids]
    (fetch-multiple* ids))
  (edit [_ id data]
    (edit* id data))
  (edit-all [_ data]
    (edit-all* data))
  (create [_ id data]
    (create* id data))
  (delete [_ id]
    (delete* id)))

(defn new-adapter []
  (ExpDB.))
