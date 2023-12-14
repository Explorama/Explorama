(ns de.explorama.shared.cache.api)

(defprotocol Cache

  (lookup [instance keys] [instance keys opts])
  (has? [instance key])
  (evict [instance key])

  (evict-by-query [instance params] [instance params propagate? before-propagation-fn]))
