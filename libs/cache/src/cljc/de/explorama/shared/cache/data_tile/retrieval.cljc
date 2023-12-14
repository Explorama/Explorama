(ns de.explorama.shared.cache.data-tile.retrieval
  (:require [de.explorama.shared.common.data.data-tiles :as explorama-tiles]
            [de.explorama.shared.cache.util :as util]
            [taoensso.timbre :refer [debug trace warn]]))

(defn- simple-match-fn [data-tile simple-match]
  (or (nil? simple-match)
      (= (select-keys data-tile
                      (keys simple-match))
         simple-match)))

(defn- regex-match-fn [data-tile regex-matches]
  (every? (fn [[key regex-match]]
            (try
              (re-find (re-pattern regex-match)
                       (explorama-tiles/value data-tile key))
              (catch #?(:clj Throwable :cljs :default) e
                (trace e)
                (debug "Can not find pattern for" key
                       "with pattern"
                       (str regex-match)
                       "for data-tile"
                       data-tile)
                false)))
          regex-matches))

(def ^:private regex-match-fn-mem (memoize regex-match-fn))

(defn- check-rules [data-tile {simple-match :match regex-match :regex}]
  (and (simple-match-fn data-tile simple-match)
       (regex-match-fn-mem (select-keys data-tile (keys regex-match))
                           regex-match)))

(defn- classification [data-tile data-tile-classification]
  (first
   (for [{result :=> :as classification} data-tile-classification
         :when (check-rules data-tile classification)]
     result)))

(defn- partition-type [workaround-data-tile-classification data-tile]
  (if-let [result (classification data-tile (:classification workaround-data-tile-classification))]
    result
    (:default workaround-data-tile-classification)))

(defn- partition-data-tile-class [data-tiles-grp partition-size group-by-keys]
  (reduce (fn [acc [_ data-tiles]]
            (into acc (partition-all partition-size data-tiles)))
          []
          (group-by #(select-keys % group-by-keys)
                    data-tiles-grp)))

(defn- partition-data-tile-classes [query-partition acc [[identifer classification] data-tiles-grp]]
  (let [{partition-size :partition
         group-by-keys :keys}
        (get-in query-partition [identifer classification])]
    (if (and partition-size group-by-keys)
      (into acc
            (partition-data-tile-class data-tiles-grp partition-size group-by-keys))
      (do
        (warn "Invalid tile configuration for" identifer classification " using defaults: :partition 1000 :keys [\"identifier\"]")
        (into acc
              (partition-data-tile-class data-tiles-grp 1000 [(explorama-tiles/access-key "identifier")]))))))

(defn- tile-data-tiles [{:keys [query-partition workaround-data-tile-classification]} tile-specs]
  (reduce (partial partition-data-tile-classes query-partition)
          []
          (group-by (fn [tile]
                      [(explorama-tiles/value tile "identifier")
                       (partition-type workaround-data-tile-classification tile)])
                    tile-specs)))

(defn request-data-tiles
  [config miss- tile-specs {abort-early :abort-early :as opts}]
  (if abort-early
    (let [{data-tile-limit :data-tile-limit} abort-early]
      (reduce (fn [acc tiles]
                (let [new-data (miss- tiles opts)
                      {events :events
                       :or {events 0}}
                      (meta acc)
                      events (+ (transduce (map (fn [[_ events]]
                                                  (count events)))
                                           +
                                           new-data)
                                events)]
                  (if (<= data-tile-limit events)
                    (util/data-tile-limit-exceeded events)
                    (vary-meta (merge acc
                                      (into {} new-data))
                               assoc
                               :events
                               events))))
              {}
              (tile-data-tiles config tile-specs)))
    (reduce (fn [acc tiles]
              (merge acc
                     (into {} (miss- tiles opts))))
            {}
            (tile-data-tiles config tile-specs))))
