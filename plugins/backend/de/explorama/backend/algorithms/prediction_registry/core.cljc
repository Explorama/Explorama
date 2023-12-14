(ns de.explorama.backend.algorithms.prediction-registry.core
  (:require [de.explorama.backend.algorithms.prediction-registry.adapter :as adapter]
            [de.explorama.backend.algorithms.prediction-registry.expdb-backend :as expdb-impl]))

(defonce instance (atom nil))

(defn new-instance []
  (reset! instance (expdb-impl/new-instance)))

(defn store-prediction [username prediction-task]
  (adapter/store-prediction @instance username prediction-task))

(defn retrive-prediction [username prediction-id]
  (adapter/retrive-prediction @instance username prediction-id))

(defn list-predictions [username include-hidden?]
  (adapter/list-predictions @instance username include-hidden?))

(defn all-predictions []
  (adapter/all-predictions @instance))

(defn hide-prediction [username prediction-id]
  (adapter/hide-prediction @instance username prediction-id))

(defn remove-prediction [username prediction-id]
  (adapter/remove-prediction @instance username prediction-id))

(defn remove-predictions [username prediction-ids]
  (adapter/remove-predictions @instance username prediction-ids))
