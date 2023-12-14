(ns de.explorama.backend.expdb.persistence.backend-indexed
  (:require [clojure.set :as set]
            [de.explorama.backend.expdb.legacy.compatibility :refer [create-formdata-for-ds]]
            [de.explorama.backend.expdb.legacy.search.data-tile :refer [get-data-tiles-for-schema]]
            [de.explorama.backend.expdb.persistence.common
             :refer [db->explorama db-event->explorama merge-data-tiles-data
                     merge-data-tiles-meta]]
            [de.explorama.backend.expdb.persistence.indexed :as itf]
            [de.explorama.backend.expdb.query.graph :refer [dts-full]]
            [de.explorama.backend.expdb.utils :refer [expdb-hash]]))

(defonce ^:private store (atom {}))

(def ^:private root-key "/de.explorama.backend.expdb/")

(defn- base-key [schema]
  (str root-key
       "dt/"
       schema
       "/"))

(def ^:private data-key-value "data/")
(defn- data-key [{schema :schema} data-tile-key]
  (str (base-key schema)
       data-key-value
       (expdb-hash data-tile-key)))

(defn- data-key-hash [{schema :schema} data-tile-key-hash]
  (str (base-key schema)
       data-key-value
       data-tile-key-hash))

(def ^:private meta-key-value "meta/")
(defn- meta-key [{schema :schema} data-tile-key]
  (str (base-key schema)
       meta-key-value
       (expdb-hash data-tile-key)))

(defn- index-key [{schema :schema}]
  (str root-key
       "index/"
       (or "default" schema)))

(deftype Backend [config internal-data-store]
  itf/Indexed
  (schema [_]
    (:schema config))

  (dump [_]
    @internal-data-store)
  (set-dump [_ data]
    (reset! internal-data-store data)
    {:success true
     :pairs (count data)})

  (data-tiles [_ data-tiles]
    (into {}
          (comp (map (fn [data-tile]
                       [data-tile (if (number? data-tile)
                                    (get @internal-data-store (data-key-hash config data-tile))
                                    (get @internal-data-store (data-key config data-tile)))]))
                (map db->explorama))
          data-tiles))

  (event [_ data-tile event-id]
    (db-event->explorama [event-id
                          (if (number? data-tile)
                            (get-in @internal-data-store [(data-key-hash config data-tile) :data event-id])
                            (get-in @internal-data-store [(data-key config data-tile) :data event-id]))]))

  (set-index [_ new-indexes]
    (swap! internal-data-store assoc (index-key config) new-indexes)
    {:success true
     :pairs 1})
  (get-index [_]
    (get @internal-data-store (index-key config) []))

  (get-meta-data [_ data-tiles]
    (into {}
          (map (fn [data-tile]
                 [data-tile (get @internal-data-store
                                 (meta-key config data-tile))]))
          data-tiles))

  (import-data [instance new-datatiles]
    (itf/import-data instance new-datatiles {}))
  (import-data [_ new-datatiles _]
    (let [new-datatiles-keys (keys new-datatiles)
          merged-data-tiles-data
          (merge-data-tiles-data (mapv (fn [data-tile-key]
                                         (get @internal-data-store (data-key config data-tile-key)))
                                       new-datatiles-keys)
                                 new-datatiles
                                 new-datatiles-keys)
          merged-data-tiles-meta
          (merge-data-tiles-meta (mapv (fn [data-tile-key]
                                         (get @internal-data-store (meta-key config data-tile-key)))
                                       new-datatiles-keys)
                                 new-datatiles
                                 new-datatiles-keys
                                 merged-data-tiles-data)]
      (doseq [[data-tile-key new-value] merged-data-tiles-data]
        (swap! internal-data-store assoc (data-key config data-tile-key) new-value))

      (doseq  [[data-tile-key new-value] merged-data-tiles-meta]
        (swap! internal-data-store assoc (meta-key config data-tile-key) new-value))
      {:success true
       :data (reduce (fn [acc [_ {count :count}]]
                       (+ acc count))
                     0
                     merged-data-tiles-meta)}))

  (delete [instance data-source]
    (itf/delete instance data-source {}))
  (delete [_ data-source _options]
    (let [matching-data-tiles (get-data-tiles-for-schema [] (create-formdata-for-ds data-source) config)
          matching-data-tiles-key (mapv (fn [matching-data-tile]
                                          [(meta-key config matching-data-tile)
                                           (data-key config matching-data-tile)])
                                        matching-data-tiles)]
      (mapv (fn [[meta-key data-key]]
              (swap! internal-data-store dissoc meta-key)
              (swap! internal-data-store dissoc data-key))
            matching-data-tiles-key)
      {:success true
       :data-tiles matching-data-tiles}))

  (delete-all [instance]
    (itf/delete-all instance {}))
  (delete-all [_instance _options]
    (reset! internal-data-store {})
    {:success true
     :dropped-bucket? true})

  (patch [_instance _bucket _field _content])

  (data-sources [_instance _opts]
    (let [dts-by-ds (group-by #(get % "datasource") (dts-full (:schema config)))]
      (reduce (fn [acc [ds dts]]
                (let [rr (mapv (fn [key]
                                 (get @internal-data-store (meta-key (:schema config) key)))
                               dts)]
                  (assoc acc
                         ds
                         {:features
                          (reduce (fn [acc meta-data]
                                    (+ acc (:count meta-data))) ; this should not happen
                                  0
                                  rr)
                          :attributes
                          (reduce (fn [acc meta-data]
                                    (set/union acc (set (:attributes meta-data))))
                                  #{}
                                  rr)})))
              {}
              dts-by-ds))))

(defn new-instance [config]
  (if-let [instance (get @store (:bucket config))]
    instance
    (let [instance (Backend. config (atom {}))]
      (swap! store assoc (:bucket config) instance)
      instance)))

(defn instances []
  @store)
