(ns de.explorama.shared.cache.core
  (:require [de.explorama.shared.cache.data-tile.local :as local-data-tile-cache]
            [de.explorama.shared.cache.data-tile.multi-layer-cache :as multi-layer-cache]
            [de.explorama.shared.cache.data-tile.no-tiling :as no-tiling]
            [de.explorama.shared.cache.data-tile.transparent :as transparent-data-tile-cache]
            [de.explorama.shared.cache.data-tile.transparent.retrieval :as data-tile-retrieval]
            [de.explorama.shared.cache.put.no-tiling :as put-cache]))

(defn transparent-data-tile-cache
  "This function will create and return a new transparent data-tile cache.
   This caches provides caching capabilites while retrieving missing data-tiles
   by quering them from its source.
   
   `retrieval-config` configures the batching of data-tile requests to its sources.
   `workaround-data-tile-classification` allows to classify data-tiles by simple tests.
   
   `storage-config` configures the org.clojure/core.cache policy and the size if the cache.
   
   `cache-services` contains the information for all other caches because the order for evicting
   and rerequesting data-tiles matters.
   
   `data-tile-services` contains the map of all data-tile sources."
  [retrieval-config
   storage-config

   cache-services
   data-tile-services
   miss-http-req
   delete-propagation-http-req]
  (transparent-data-tile-cache/init retrieval-config
                                    storage-config

                                    cache-services
                                    data-tile-services
                                    miss-http-req
                                    delete-propagation-http-req))

(comment
  (transparent-data-tile-cache
   {:query-partition
    {"search" {:big    {:partition 25
                        :keys      [:datasource :year :identifier]}
               :medium {:partition 250
                        :keys      [:datasource :identifier]}
               :small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:classification [{:match {"identifier" "algorithms"}
                       :=> :small}
                      {:match {"identifier" "search"}
                       :regex {"datasource" "GTD[0-9]+"}
                       :=> :medium}]
     :default :small}}

   {:size 150000}

   (atom {"mosaic" {:identifier "mosaic"
                    :sub    ["ac"]
                    :invalidate "http://mosaic:port/cache/invalidate"
                    :delete-by-query "http://mosaic:port/cache/delete-by-query"}})

   (atom {"search" {:identifier "search"
                    :datatiles  "http://suche:port/data-tiles"}})
   (fn [url body])
   (fn [url body])))

(defn local-data-tile-cache
  "This function will create and return a new local data-tile cache.
   This caches provides caching capabilites while requsting missing data-tiles through the
   provided miss function.
   
   `retrieval-config` configures the batching of data-tile requests to its sources.
   `workaround-data-tile-classification` allows to classify data-tiles by simple tests.
   
   `storage-config` configures the org.clojure/core.cache policy and the size if the cache.
   
   `cache-services` contains the information for all other caches because the order for evicting
   and rerequesting data-tiles matters.
   
   `miss` gets called when a set of data-tiles is missing. There will be multiple chunked calls
   according to the retrieval-configuration (miss tiles)."
  [retrieval-config
   storage-config

   cache-services

   miss
   delete-propagation-http-req]
  (local-data-tile-cache/init retrieval-config
                              storage-config

                              cache-services

                              miss
                              delete-propagation-http-req))

(comment
  (local-data-tile-cache
   {:query-partition
    {"search" {:big    {:partition 25
                        :keys      [:datasource :year :identifier]}
               :medium {:partition 250
                        :keys      [:datasource :identifier]}
               :small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:classification [{:match {"identifier" "algorithms"}
                       :=> :small}
                      {:match {"identifier" "search"}
                       :regex {"datasource" "GTD[0-9]+"}
                       :=> :medium}]
     :default :small}}

   {:size 150000}

   (atom {"mosaic" {:identifier "mosaic"
                    :depends    "local"
                    :invalidate "http://mosaic:port/cache/invalidate"
                    :delete-by-query "http://mosaic:port/cache/delete-by-query"}})

   (fn [tiles opts])
   (fn [url body])))

(defn cache-retrieval-client
  "This function will create and return a new transparent cache retrieval strategy.
   It allows to query data-tiles by using the given partitioning and and use the
   provided data-tile-services to resolve the the apis for each data-tile.
   
   `retrieval-config` configures the batching of data-tile requests to its sources.
   `workaround-data-tile-classification` allows to classify data-tiles by simple tests.
   
   `data-tile-services` contains the map of all data-tile sources."
  [retrieval-config
   data-tile-services
   miss-http-req]
  (data-tile-retrieval/init-client retrieval-config
                                   data-tile-services
                                   miss-http-req))

(comment
  (cache-retrieval-client
   {:query-partition
    {"search" {:big    {:partition 25
                        :keys      [:datasource :year :identifier]}
               :medium {:partition 250
                        :keys      [:datasource :identifier]}
               :small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:classification [{:match {"identifier" "algorithms"}
                       :=> :small}
                      {:match {"identifier" "search"}
                       :regex {"datasource" "GTD[0-9]+"}
                       :=> :medium}]
     :default :small}}

   (atom {"search" {:identifier "search"
                    :datatiles  "http://suche:port/data-tiles"}})

   (fn [url body])))

(defn no-tiling-cache
  "This function will create and return a new local data-tile cache.
   This caches provides caching capabilites while requsting missing data-tiles through the
   provided miss function.
   
   `storage-config` configures the org.clojure/core.cache policy and the size if the cache.
   
   `cache-services` contains the information for all other caches because the order for evicting
   and rerequesting data-tiles matters.
   
   `miss` gets called when a set of data-tiles is missing. There will be multiple chunked calls
   according to the retrieval-configuration (miss tiles)."
  [storage-config

   cache-services
   miss
   delete-propagation-http-req]
  (no-tiling/init storage-config

                  cache-services
                  miss
                  delete-propagation-http-req))

(comment
  (no-tiling-cache
   {:size 150000}

   (atom {"search" {:identifier "search"
                    :datatiles  "http://suche:port/data-tiles"}})

   (fn [missing-key opts])
   (fn [url body])))

(defn put-cache
  "This function will create and return a new local cache.
   This caches provides caching capabilites while directing misses at the provided
   provided miss function.
   
   `storage-config` configures the org.clojure/core.cache policy and the size if the cache.
   
   `cache-services` contains the information for all other caches because the order for evicting
   and rerequesting data-tiles matters."
  [storage-config

   cache-services
   delete-propagation-http-req]
  (put-cache/init storage-config

                  cache-services
                  delete-propagation-http-req))

(comment
  (put-cache
   {:size 150000}

   (atom {"search" {:identifier "search"
                    :datatiles  "http://suche:port/data-tiles"}})
   (fn [url body])))

(defn multi-layer-cache
  "This function will create and return a new local cache.
   This caches provides caching capabilites while directing misses at the provided
   provided miss function.

   `retrieval-config` configures the batching of data-tile requests to its sources.
   workaround-data-tile-classification` allows to classify data-tiles by simple tests.
   
   `storage-config` configures the org.clojure/core.cache policy and the size if the cache.
   There is no eviction policy choice possible - is is always LRU.
   
   `cache-services` contains the information for all other caches because the order for evicting
   and rerequesting data-tiles matters.

   `data-tile-services` contains the map of all data-tile sources."
  [retrieval-config
   storage-config

   cache-services
   data-tile-services
   miss-http-req
   delete-propagation-http-req]
  (multi-layer-cache/init retrieval-config
                          storage-config

                          cache-services
                          data-tile-services
                          miss-http-req
                          delete-propagation-http-req))

(comment
  (multi-layer-cache
   {:query-partition
    {"search" {:big    {:partition 25
                        :keys      [:datasource :year :identifier]}
               :medium {:partition 250
                        :keys      [:datasource :identifier]}
               :small  {:partition 1000
                        :keys      [:datasource :identifier]}}}
    :workaround-data-tile-classification
    {:classification [{:match {"identifier" "algorithms"}
                       :=> :small}
                      {:match {"identifier" "search"}
                       :regex {"datasource" "GTD[0-9]+"}
                       :=> :medium}]
     :default :small}}
   {:size 150000}

   (atom {"mosaic" {:identifier "mosaic"
                    :sub    ["ac"]
                    :invalidate "http://mosaic:port/cache/invalidate"
                    :delete-by-query "http://mosaic:port/cache/delete-by-query"}})

   (atom {"search" {:identifier "search"
                    :datatiles  "http://suche:port/data-tiles"}})
   (fn [url body])
   (fn [url body])))
