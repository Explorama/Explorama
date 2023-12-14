(ns de.explorama.backend.expdb.middleware.indexed
  (:require [de.explorama.backend.expdb.buckets :as buckets]
            [de.explorama.backend.expdb.persistence.indexed :as persistence]
            [de.explorama.backend.expdb.persistence.shared :as imp]))

(def import-data imp/transform->import)

(defn get-datasources [bucket & [opts]]
  (persistence/data-sources (buckets/new-instance bucket :indexed)
                            opts))

(defn get+ [data-tiles]
  (let [grouped-tiles (group-by #(get % "bucket") data-tiles)]
    (reduce (fn [acc [bucket tiles]]
              (into acc
                    (persistence/data-tiles (buckets/new-instance bucket :indexed) tiles)))
            {}
            grouped-tiles)))

(defn get-event [data-tile event-id]
  (persistence/event (buckets/new-instance (get data-tile "bucket") :indexed) data-tile event-id))

(defn delete-data-source [bucket datasource]
  (imp/delete-datasource bucket datasource))

(defn delete-all [bucket]
  (persistence/delete-all (buckets/new-instance bucket :indexed)))
