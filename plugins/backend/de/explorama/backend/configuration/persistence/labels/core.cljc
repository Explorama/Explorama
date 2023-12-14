(ns de.explorama.backend.configuration.persistence.labels.core
  (:require [de.explorama.backend.configuration.persistence.labels.expdb :as expdb-backend]
            [de.explorama.backend.configuration.persistence.labels.label-protocol :as lp]))

(defonce instance (atom nil))

(defn write-labels
  [labels]
  (lp/write-labels @instance labels))

(defn overwrite-labels
  [labels]
  (lp/overwrite-labels @instance labels))

(defn read-labels
  []
  (lp/read-labels @instance))

(defn new-instance [watch-fn]
  (reset! instance
          (expdb-backend/new-instance watch-fn)))
