(ns de.explorama.shared.cache.data-tile.transparent.retrieval
  (:require [de.explorama.shared.common.data.data-tiles :as explorama-tiles]
            [de.explorama.shared.cache.data-tile.retrieval :as dt-retrieval]
            [de.explorama.shared.cache.interfaces.retrieval :as exploramaretrieval]
            [de.explorama.shared.cache.api :as exploramacache]
            [taoensso.tufte :as tufte])
  #?(:clj
     (:import [de.explorama.shared.cache.interfaces.retrieval Retrieval]
              [de.explorama.shared.cache.api Cache])))

(defn- miss [data-tile-services miss-http-req tiles _]
  (tufte/p ::request-and-decode-data-tiles-resp
           (miss-http-req (get-in @data-tile-services [(-> tiles
                                                           first
                                                           (explorama-tiles/value "identifier")
                                                           keyword)
                                                       :data-tiles])
                          {:data-tiles tiles})))

(deftype TransparentDataTileRetrieval [config data-tile-services miss-http-req]
  #?(:clj Retrieval
     :cljs exploramaretrieval/Retrieval)
  (exploramaretrieval/miss [_ data-tiles opts]
    (dt-retrieval/request-data-tiles config (partial miss data-tile-services miss-http-req) data-tiles opts)))

(defn init [config data-tile-services miss-http-req]
  (TransparentDataTileRetrieval. config
                                 data-tile-services
                                 miss-http-req))

(deftype TransparentDataTileClient [retrieval]
  #?(:clj Cache
     :cljs exploramacache/Cache)
  (exploramacache/lookup [_ data-tiles]
    (exploramaretrieval/miss retrieval data-tiles {}))
  (exploramacache/lookup [_ data-tiles opts]
    (exploramaretrieval/miss retrieval data-tiles opts))
  (exploramacache/has? [_ _]
    (ex-info "The client is for retrieval only" {}))
  (exploramacache/evict [_ _]
    (ex-info "The client is for retrieval only" {}))
  (exploramacache/evict-by-query [_ _]
    (ex-info "The client is for retrieval only" {}))
  (exploramacache/evict-by-query [_ _ _ _]
    (ex-info "The client is for retrieval only" {})))

(defn init-client [config
                   data-tile-services
                   miss-http-req]
  (let [retrieval (init config data-tile-services miss-http-req)]
    (TransparentDataTileClient. retrieval)))
