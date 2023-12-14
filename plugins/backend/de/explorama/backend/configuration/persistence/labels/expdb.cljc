(ns de.explorama.backend.configuration.persistence.labels.expdb
  (:require [de.explorama.backend.configuration.persistence.labels.label-protocol :as lp]
            [de.explorama.backend.expdb.middleware.db :as expdb]))

(defonce ^:private bucket "/configuration/labels")

(defn- read-labels []
  (expdb/get+ bucket))

(defn- overwrite-labels [labels]
  (expdb/set+ bucket labels))

(defn- write-labels [labels]
  (overwrite-labels (merge-with merge (read-labels) labels)))

(deftype
 Exp-Backend [watch-fn]
  lp/Label-Backend
  (write-labels [_ labels]
    (write-labels labels)
    (watch-fn (read-labels)))
  (overwrite-labels [_ labels]
    (watch-fn labels)
    (overwrite-labels labels))
  (read-labels [_]
    (read-labels)))

(defn new-instance [watch-fn]
  (Exp-Backend. watch-fn))
