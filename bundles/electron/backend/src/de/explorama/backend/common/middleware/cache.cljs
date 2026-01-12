(ns de.explorama.backend.common.middleware.cache
  (:require [de.explorama.backend.common.environment.core :refer [discovery-client]]
            [de.explorama.backend.common.environment.discovery :refer [CACHE_SERVICES DATA_SERVICES]]
            [de.explorama.backend.expdb.config :as config]
            [de.explorama.backend.expdb.middleware.indexed :as idb]
            [de.explorama.shared.cache.api :as cache]
            [de.explorama.shared.cache.core :refer [multi-layer-cache]]
            [taoensso.timbre :refer [error]]))

(def ^:private events (atom {}))

(defonce ^:private data-tile-cache
  (atom nil))

(defn new-cache []
  (multi-layer-cache
   {:query-partition
    {}
    :workaround-data-tile-classification
    {}}

   {:size config/explorama-expdb-cache-size}

   (discovery-client CACHE_SERVICES)

   (discovery-client DATA_SERVICES)

   (fn [_ {data-tiles :data-tiles}]
     (idb/get+ data-tiles))
   (fn [& _])))

(defn index-aggregation-result [_ result]
  (swap! events
         merge
         (into {} (map (fn [event]
                         [(get event "id") event])
                       result)))
  nil)

(defn remove-data-tiles [data-tiles]
  (doseq [data-tile data-tiles]
    (cache/evict @data-tile-cache data-tile)))

(defn lookup [data-tiles & [opts]]
  (cache/lookup @data-tile-cache data-tiles opts))

(defn lookup-event [event-id]
  (if-let [event (cache/lookup @data-tile-cache event-id {:single-events true})]
    event
    (if-let [event (get @events (second event-id))]
      event
      (do
        (error "event not found in cache" event-id)
        nil))))

(defn delete-by-query [params]
  (cache/evict-by-query @data-tile-cache
                        params
                        nil
                        (fn [evicted-tiles]
                          (lookup evicted-tiles))))

(defn reset-states []
  (reset! data-tile-cache (new-cache)))
