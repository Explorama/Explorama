(ns de.explorama.shared.cache.interfaces.retrieval)

(defprotocol Retrieval
  (miss [instance keys opts]))