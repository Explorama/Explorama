(ns de.explorama.shared.cache.interfaces.invalidate)

(defprotocol Invalidation
  (evict-by-query
    [interface storage params]
    [interface storage params propagate? before-propagation-fn]))