(ns de.explorama.backend.common.storage.label-backend.redis
  (:require [de.explorama.backend.labels.label-protocol :as lp]
            [de.explorama.backend.storage.redis-backend-helper :as redis-helper]
            [de.explorama.backend.redis.utils :refer [config->conspec]]))

(def  labels-redis-key "/i18n/attribute-labels")

(defonce labels-store (atom {}))

(defn- init! [server-conn watch-fn]
  (redis-helper/load-redis-to-atom server-conn labels-redis-key labels-store {})
  (redis-helper/add-save-watcher server-conn labels-redis-key labels-store :label-store)
  (add-watch labels-store :label-propagation watch-fn))

(defn- write-labels [labels]
  (swap! labels-store #(merge-with merge % labels)))

(defn- overwrite-labels [labels]
  (reset! labels-store labels))

(defn- read-labels []
  @labels-store)

(deftype
 Redis-Backend [server-conn]
  lp/Label-Backend
  (write-labels [instance labels]
    (write-labels labels))
  (overwrite-labels [instance labels]
    (overwrite-labels labels))
  (read-labels [instance]
    (read-labels)))

(defn new-instance [watch-fn]
  (let [server-conn (config->conspec)]
    (init! server-conn watch-fn)
    (Redis-Backend. server-conn)))
