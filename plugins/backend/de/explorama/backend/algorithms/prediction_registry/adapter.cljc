(ns de.explorama.backend.algorithms.prediction-registry.adapter)

(defprotocol
 Registry
  (store-prediction [instance username prediction-task])
  (retrive-prediction [instance username prediction-id])
  (list-predictions [instance username include-hidden?] "List all predictions based.")
  (hide-prediction [instance username prediction-id])
  (remove-prediction [instance username prediction-id] "Removes a Prediction from the store.")
  (remove-predictions [instance username prediction-ids] "Removes a Prediction from the store.")
  (all-predictions [instance] "List all Predictions regardless of user."))