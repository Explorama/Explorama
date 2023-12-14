(ns de.explorama.backend.expdb.persistence.backend-indexed
  (:require [clojure.set :as set]
            [de.explorama.backend.electron.config :refer [app-data-path]]
            [de.explorama.backend.electron.file :refer [add-to-path]]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.legacy.compatibility :refer [create-formdata-for-ds]]
            [de.explorama.backend.expdb.legacy.search.data-tile :refer [get-data-tiles-for-schema]]
            [de.explorama.backend.expdb.persistence.common
             :refer [db->explorama db-event->explorama merge-data-tiles-data
                     merge-data-tiles-meta]]
            [de.explorama.backend.expdb.persistence.common-sqlite :as sqlite
             :refer [db-del+ db-drop-table db-get+ db-set+]]
            [de.explorama.backend.expdb.persistence.indexed :as itf]
            [de.explorama.backend.expdb.query.graph :refer [dts-full]]
            [de.explorama.backend.expdb.utils :refer [expdb-hash]]
            [taoensso.timbre :refer [error]]))

(def ^:private db-key (add-to-path app-data-path
                                   "de.explorama.backend.expdb.indexed.sqlite3"))

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

(deftype Backend [bucket config]
  itf/Indexed
  (schema [_]
    bucket)

  (dump [_]
    (sqlite/dump db-key bucket))
  (set-dump [_ data]
    (sqlite/set-dump db-key bucket data)
    {:success true
     :pairs (count data)})

  (data-tiles [_ data-tiles]
    (let [data-tile-keys (into {}
                               (map (fn [data-tile]
                                      [data-tile (if (number? data-tile)
                                                   (data-key-hash config data-tile)
                                                   (data-key config data-tile))])
                                    data-tiles))
          result (db-get+ db-key bucket (vec (vals data-tile-keys)))]
      (into {}
            (comp (map (fn [[data-tile query-key]]
                         [data-tile (get result query-key)]))
                  (map db->explorama))
            data-tile-keys)))

  (event [_ data-tile event-id]
    (let [dt-key (if (number? data-tile)
                   (data-key-hash config data-tile)
                   (data-key config data-tile))
          result (db-get+ db-key bucket [dt-key])]
      (db-event->explorama [event-id
                            (get-in result [dt-key :data event-id])])))

  (set-index [_ new-indexes]
    (db-set+ db-key bucket {(index-key config) new-indexes})
    {:success true
     :pairs 1})
  (get-index [_]
    (get (db-get+ db-key bucket [(index-key config)])
         (index-key config)))

  (get-meta-data [_ data-tiles]
    (into {}
          (map (fn [{key :key :as dt}]
                 [key dt]))
          (vals (db-get+ db-key bucket (map (fn [data-tile-key]
                                              (meta-key config data-tile-key))
                                            data-tiles)))))

  (import-data [instance xml-spec]
    (itf/import-data instance xml-spec {}))
  (import-data [_ new-datatiles _]
    ;TODO r1/expdb make this a transaction and wrap all functions around it
    (try
      (let [new-datatiles-keys (keys new-datatiles)

            current-merged-data-tiles-data (db-get+ db-key
                                                    bucket
                                                    (mapv (fn [data-tile-key]
                                                            (data-key config data-tile-key))
                                                          new-datatiles-keys))

            merged-data-tiles-data
            (merge-data-tiles-data (mapv #(get current-merged-data-tiles-data (data-key config %))
                                         new-datatiles-keys)
                                   new-datatiles
                                   new-datatiles-keys)

            current-merged-data-tiles-meta (db-get+ db-key
                                                    bucket
                                                    (mapv (fn [data-tile-key]
                                                            (meta-key config data-tile-key))
                                                          new-datatiles-keys))

            merged-data-tiles-meta
            (merge-data-tiles-meta (mapv #(get current-merged-data-tiles-meta (meta-key config %))
                                         new-datatiles-keys)
                                   new-datatiles
                                   new-datatiles-keys
                                   merged-data-tiles-data)]
        (db-set+ db-key bucket (map (fn [[data-tile-key new-value]]
                                      [(data-key config data-tile-key) new-value])
                                    merged-data-tiles-data))
        (db-set+ db-key bucket (map (fn [[data-tile-key new-value]]
                                      [(meta-key config data-tile-key) new-value])
                                    merged-data-tiles-meta))
        {:success true
         :data (reduce (fn [acc [_ {count :count}]]
                         (+ acc count))
                       0
                       merged-data-tiles-meta)})
      (catch :default e
        (error "Import failed" e)
        {:success false
         :message "Import failed - see logs for details"
         :error-reason (ex-message e)})))

  (delete [instance data-source]
    (itf/delete instance data-source {}))
  (delete [_ data-source _options]
    (let [matching-data-tiles (get-data-tiles-for-schema [] (create-formdata-for-ds data-source) config)
          matching-data-tiles-keys (mapcat (fn [matching-data-tile]
                                             [(meta-key config matching-data-tile)
                                              (data-key config matching-data-tile)])
                                           matching-data-tiles)]
      (db-del+ db-key bucket matching-data-tiles-keys)
      {:success true
       :data-tiles matching-data-tiles}))
  (delete-all [instance]
    (itf/delete-all instance {}))
  (delete-all [_instance _options]
    (db-drop-table db-key bucket)
    {:success true
     :dropped-bucket? true})

  (patch [_instance _bucket _field _content])

  (data-sources [_instance _opts]
    (let [dts-by-ds (group-by #(get % "datasource") (dts-full (:schema config)))]
      (reduce (fn [acc [ds dts]]
                (let [rr (db-get+ db-key bucket (mapv (fn [key]
                                                        (meta-key config key))
                                                      dts))]
                  (assoc acc
                         ds
                         {:features
                          (reduce (fn [acc [_ meta-data]]
                                    (+ acc (:count meta-data))) ; this should not happen
                                  0
                                  rr)
                          :attributes
                          (reduce (fn [acc [_ meta-data]]
                                    (set/union acc (set (:attributes meta-data))))
                                  #{}
                                  rr)})))
              {}
              dts-by-ds))))

(defn new-instance [config]
  (Backend. (get config :schema) config))

(defn instances []
  (keys config-expdb/explorama-bucket-config))
