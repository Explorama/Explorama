(ns de.explorama.shared.cache.data-tile.transparent
  (:require [de.explorama.shared.cache.data-tile.invalidate :as invalidate]
            [de.explorama.shared.cache.data-tile.storage :as storage]
            [de.explorama.shared.cache.data-tile.transparent.retrieval :as retrieval]
            [de.explorama.shared.cache.api :as exploramacache]
            [de.explorama.shared.cache.interfaces.invalidate :as exploramainvalidate]
            [de.explorama.shared.cache.interfaces.storage :as exploramastorage])
  #?(:clj
     (:import [de.explorama.shared.cache.api Cache])))

(deftype DataTileCache [storage
                        retrieval
                        invalidate]
  #?(:clj Cache
     :cljs exploramacache/Cache)

  (exploramacache/lookup [_ data-tiles]
    (exploramastorage/access storage retrieval data-tiles nil))
  (exploramacache/lookup [_ data-tiles opts]
    (exploramastorage/access storage retrieval data-tiles opts))
  (exploramacache/has? [_ data-tile]
    (exploramastorage/has? storage data-tile))
  (exploramacache/evict [_ data-tile]
    (exploramastorage/evict storage data-tile))

  (exploramacache/evict-by-query [_ params]
    (exploramainvalidate/evict-by-query invalidate storage params))
  (exploramacache/evict-by-query [_ params propagate? before-propagation-fn]
    (exploramainvalidate/evict-by-query invalidate storage params propagate? before-propagation-fn)))

(defn init [retrieval-config
            storage-config

            cache-services
            data-tile-services
            miss-http-req
            delete-propagation-req]
  (let [retrieval (retrieval/init retrieval-config data-tile-services miss-http-req)
        invalidate (invalidate/init cache-services delete-propagation-req)
        storage (storage/init storage-config identity)]
    (DataTileCache. storage
                    retrieval
                    invalidate)))