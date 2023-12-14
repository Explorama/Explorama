(ns de.explorama.shared.cache.interfaces.storage)

(defprotocol Storage
  (access [instance retrieval keys opts])
  (has? [instance key])
  (evict [instance key])
  (all [instance])
  (all-keys [instance]))