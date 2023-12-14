(ns de.explorama.backend.algorithms.prediction-registry.expdb-backend
  (:require [de.explorama.backend.algorithms.prediction-registry.adapter :as adapter]
            [de.explorama.backend.expdb.middleware.db :as db]
            [de.explorama.shared.common.unification.time :refer [current-ms]]))

(def ^:private index-store-key "prediction-index")
(def ^:private bucket "predictions")

(defn- prediction-key [username prediction-id]
  (str username "/" prediction-id))

(defn- write-prediction-task [username {:keys [prediction-id] :as prediction-task}]
  (let [prediction-key (prediction-key username prediction-id)]
    (db/set bucket prediction-key prediction-task)
    prediction-key))

(defn- delete-prediction [index-store username prediction-id]
  (let [prediction-path (get-in index-store [username prediction-id :path])
        new-index (update index-store username dissoc prediction-id)]
    (when prediction-path
      (db/del bucket prediction-path))
    new-index))

(deftype Expdb-Registry [^:unsynchronized-mutable index-store]
  adapter/Registry
  (store-prediction [_ username prediction-task]
    (let [{:keys [prediction-id
                  prediction-name
                  last-used]} prediction-task
          prediction-path (write-prediction-task username prediction-task)
          new-index (assoc-in index-store
                              [username prediction-id] {:path prediction-path
                                                        :prediction-name prediction-name
                                                        :last-used last-used
                                                        :prediction-id prediction-id})]
      (set! index-store new-index)
      (db/set bucket index-store-key new-index)
      true))
  (retrive-prediction [_ username prediction-id]
    (let [prediction-path (get-in index-store [username prediction-id :path])
          prediction (db/get bucket prediction-path)
          new-index (assoc-in index-store [username prediction-id :last-used]  (current-ms))]
      (when prediction
        (set! index-store new-index)
        (db/set bucket index-store-key new-index))
      prediction))
  (list-predictions [_ username include-hidden?]
    (filterv seq
             (map (fn [[_ {hidden? :hidden? :as task}]]
                    (when (or (and include-hidden?
                                   hidden?)
                              (not hidden?))
                      (select-keys task [:prediction-name
                                         :prediction-id
                                         :last-used])))
                  (get index-store username))))
  (all-predictions [_]
    (into {}
          (mapv (fn [[user predictions]]
                  [user (mapv (fn [[_ task]]
                                (select-keys task [:prediction-name
                                                   :prediction-id
                                                   :last-used]))
                              predictions)])
                index-store)))
  (remove-prediction [_ username prediction-id]
    (let [new-index (delete-prediction index-store username prediction-id)]
      (set! index-store new-index)
      (db/set bucket index-store-key new-index)
      true))
  (remove-predictions [_ username prediction-ids]
    (let [new-index (reduce (fn [n-index pred-id]
                              (delete-prediction n-index username pred-id))
                            index-store
                            prediction-ids)]
      (set! index-store new-index)
      (db/set bucket index-store-key new-index)
      true)))

(defn new-instance []
  (let [index (db/get bucket index-store-key)]
    (Expdb-Registry. index)))
