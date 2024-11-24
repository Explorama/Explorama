(ns de.explorama.shared.cache.data-tile.multi-layer.storage
  (:require [de.explorama.shared.cache.interfaces.cache :as cache]
            [de.explorama.shared.cache.interfaces.retrieval :as exploramaretrieval]
            [de.explorama.shared.cache.interfaces.storage :as exploramastore]
            [taoensso.tufte :as tufte])
  #?(:clj
     (:import [de.explorama.shared.cache.interfaces.storage Storage])))

(defn- split-and-hit [cache tile-specs]
  (reduce (fn [{:keys [present-tiles missing-specs new-cache]}
               tile-spec]
            (let [key tile-spec]
              (if (cache/has? new-cache key)
                {:present-tiles (conj present-tiles
                                      {key (cache/lookup new-cache key)})
                 :missing-specs missing-specs
                 :new-cache (cache/hit new-cache key)}
                {:present-tiles present-tiles
                 :missing-specs (conj missing-specs tile-spec)
                 :new-cache new-cache}))) ; cache/miss will be done later
          {:present-tiles []
           :missing-specs []
           :new-cache cache}
          tile-specs))

(defn update-event-entry [evt old-event]
  (if old-event
    (update old-event :dt-index-counter inc)
    {:dt-index-counter 1 :event evt}))

(defn add-tile-to-event-index  [evt-index tilename tile]
  (let [bucket (tilename "bucket")]
    (reduce (fn [accum evt]
              (update accum [bucket (evt "id")] #(update-event-entry evt %)))
            evt-index tile)))
(defn- retrieve-into-cache! [storage event-index retrieval locks missing-specs opts]
  ;; locks
  ;; Retrieval status promises for specs; a map from tile-spec to a promise of a
  ;; boolean (which is always true if delivered).
  ;; We use a exploramaic lock here so that we can avoid duplicating the
  ;; group-by operation inside and outside the swap.
  (when (seq missing-specs)
    (let [;; "in-progress" are all tiles that are currently already being
          ;; requested, i. e. "in flight".  This can only be due to another
          ;; thread running through this function invocation at the same time.
          ;; All other tiles ("not-progress") are either already known and
          ;; delivered or have not already been requested.
          in-progress? (fn [promises tilename]
                         (let [pr (get promises tilename)]
                           (and (boolean pr)
                                (not (realized? pr)))))

          [old-locks _]
          (swap-vals! locks
                      (fn [old]
                        (let [{not-progress false}
                              (group-by (partial in-progress? old) missing-specs)]
                          (reduce (fn [acc nspec]
                                    (assoc acc nspec #?(:clj (promise)
                                                        :cljs false)))
                                  old
                                  not-progress))))
         ;; Repeat the grouping outside the swap operation
          {in-progress-tiles true
           not-progress-tiles false}
          (group-by (partial in-progress? old-locks) missing-specs)]
      (try
        (let [tiles-map (when not-progress-tiles
                          (exploramaretrieval/miss retrieval not-progress-tiles opts))]
          (tufte/p ::store-in-cache
                   (doseq [[tile-name tile] tiles-map]
            ;; Data tiles requested, block until response arrived.
            ;; Then storage in cache.
                     (when (some? tile)
                       (swap! storage #(cache/miss % tile-name
                                                   (map (fn [evt]
                                                          [(tile-name "bucket")
                                                           (evt "id")])
                                                        tile)))
                       (swap! event-index add-tile-to-event-index tile-name tile))
                     #?(:clj
                        (when-let [prom (get @locks tile-name)]
                          (deliver prom true)))))
         ;; Block until all promises required in this function invocation that were
         ;; already in flight before this function invocation have also been
         ;; fulfilled.
          (doseq [tile-name in-progress-tiles]
            #?(:clj @(get @locks tile-name))))
        (catch #?(:clj Throwable :cljs :default) e
         ;; We assume thats an exception means that the request failed.
          #?(:clj
             (doseq [tile-name not-progress-tiles]
               (deliver (get @locks tile-name) false))
             :cljs
             (doseq [tile-name not-progress-tiles]
               (swap! locks assoc tile-name false)))
          (throw e))))))

(defn- retrieve-data [storage event-index retrieval locks tile-specs opts]
  (let [current-cache @storage
        {:keys [present-tiles
                missing-specs]}
        (tufte/p ::split-and-hit-result
                 (split-and-hit current-cache tile-specs))]
    (tufte/p ::retrieve-into-cache-result
             (retrieve-into-cache! storage event-index retrieval locks missing-specs opts))
    (let [cache @storage]
      (into (vec present-tiles)
            (mapv #(hash-map % (cache/lookup cache %)) missing-specs)))))

(defn- get-data-tiles [storage event-index retrieval locks return-type data-tiles opts]
  (if (:single-events opts)
    (get-in @event-index [data-tiles :event])
    (let [result
          (cond (empty? data-tiles)
                (return-type {})
                (sequential? data-tiles)
                (tufte/p ::get-data-tiles-result
                         (let [retr-data (retrieve-data storage event-index retrieval locks data-tiles opts)]
                           (return-type (into {}
                                              (for [[k v] (reduce merge retr-data)]
                                                [k (mapv #(get-in @event-index [% :event]) v)]))))))]
      result)))

(defn update-index-on-eviction [event-index _ val]
  (swap! event-index #(reduce (fn [accum evt]
                                (let [{dt-index-counter :dt-index-counter} (get accum evt)]
                                  (if (> dt-index-counter 1)
                                    (update-in accum [evt :dt-index-counter] dec)
                                    (dissoc accum evt)))) % val)))

(deftype DataTileStorage [storage event-index locks return-type]
  #?(:clj Storage
     :cljs exploramastore/Storage)
  (exploramastore/access [_ retrieval data-tiles opts]
    (get-data-tiles storage event-index retrieval locks return-type data-tiles opts))
  (exploramastore/has? [_ tile-name]
    (cache/has? @storage tile-name))
  (exploramastore/evict [_ tile-name]
    (swap! storage #(cache/evict % tile-name)))
  (exploramastore/all [_]
    @storage)
  (exploramastore/all-keys [_]
    (cache/keys @storage)))

(defn- new-cache
  [{:keys [strategy size]} event-index]
  (case strategy
    :lru (cache/lru-multi-cache size (partial update-index-on-eviction event-index))
    (cache/lru-multi-cache size (partial update-index-on-eviction event-index))))

(defn init [cache-config return-type]
  (let [event-index (atom {})]
    (DataTileStorage. (atom (new-cache cache-config event-index))
                      event-index
                      (atom {})
                      return-type)))