(ns de.explorama.backend.expdb.middleware.db
  (:require [de.explorama.backend.expdb.buckets :as buckets]
            [de.explorama.backend.expdb.persistence.simple :as persistence])
  (:refer-clojure :exclude [get set]))

(def ^:private access-type :simple)

(defn del-bucket [bucket]
  (persistence/del-bucket (buckets/new-instance bucket access-type)))

(defn del [bucket key]
  (persistence/del (buckets/new-instance bucket access-type) key))

(defn get [bucket key]
  (persistence/get (buckets/new-instance bucket access-type) key))

(defn get+
  ([bucket]
   (persistence/get+ (buckets/new-instance bucket access-type)))
  ([bucket keys]
   (persistence/get+ (buckets/new-instance bucket access-type) keys)))

(defn set [bucket key value]
  (persistence/set (buckets/new-instance bucket access-type) key value))

(defn set+ [bucket data]
  (persistence/set+ (buckets/new-instance bucket access-type) data))

(defn dump [bucket]
  (persistence/dump (buckets/new-instance bucket access-type)))

(defn set-dump [bucket data]
  (persistence/set-dump (buckets/new-instance bucket access-type) data))
