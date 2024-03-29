(ns de.explorama.backend.indicator.persistence.store.adapter)

(defprotocol
 Backend
  (write-indicator
    [instance indicator]
    "Persist the given de.explorama.backend.indicator.")
  (read-indicator
    [instance id]
    "Read a specific indicator version.")
  (short-indicator-desc
    [instance id]
    "Returns a small subset of keys without the calculation-desc.")
  (list-indicators
    [instance]
    "List all indicators.")
  (list-all-user-indicators
    [instance user]
    "List all user indicators.")
  (user-for-indicator-id
    [instance id]
    "Return the user for a given indicator id.")
  (delete-indicator
    [instance id]
    "Completly delete a specific indicator for a user."))