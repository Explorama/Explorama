(ns de.explorama.shared.cache.put.no-tiling
  (:require [de.explorama.shared.cache.data-tile.invalidate :as invalidate]
            [de.explorama.shared.cache.api :as exploramacache]
            [de.explorama.shared.cache.interfaces.invalidate :as exploramainvalidate]
            [de.explorama.shared.cache.interfaces.storage :as exploramastorage]
            [de.explorama.shared.cache.put.storage :as storage])
  #?(:clj
     (:import [de.explorama.shared.cache.api Cache])))

(deftype DataTileCache [storage
                        invalidate]
  #?(:clj Cache
     :cljs exploramacache/Cache)
  (exploramacache/lookup [_ _]
    (throw (ex-info "Do not you the 2 arity function - please provide a miss function"
                    {:reason :not-implemented})))
  (exploramacache/lookup [_ data-tiles {:keys [miss] :as opts}]
    (when (nil? miss)
      (throw (ex-info "Provide a miss function"
                      {:reason :missing-parameter})))
    (exploramastorage/access storage miss data-tiles (dissoc opts miss)))
  (exploramacache/has? [_ data-tile]
    (exploramastorage/has? storage data-tile))
  (exploramacache/evict [_ data-tile]
    (exploramastorage/evict storage data-tile))

  (exploramacache/evict-by-query [_ params]
    (exploramainvalidate/evict-by-query invalidate storage params))
  (exploramacache/evict-by-query [_ params propagate? before-propagation-fn]
    (exploramainvalidate/evict-by-query invalidate storage params propagate? before-propagation-fn)))


(defn init [storage-config

            cache-services
            delete-propagation-req]
  (let [invalidate (invalidate/init cache-services delete-propagation-req)
        storage (storage/init storage-config vec)]
    (DataTileCache. storage
                    invalidate)))