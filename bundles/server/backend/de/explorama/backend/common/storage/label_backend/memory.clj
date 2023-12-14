(ns de.explorama.backend.common.storage.label-backend.memory
  (:require [de.explorama.backend.labels.label-protocol :as lp]))

(defonce labels-store (atom {}))

(defn- init! [watch-fn]
  (add-watch labels-store :label-propagation watch-fn))

(defn- write-labels [labels]
  (swap! labels-store #(merge-with merge % labels)))

(defn- overwrite-labels [labels]
  (reset! labels-store labels))

(defn- read-labels []
  @labels-store)

(deftype
 Redis-Backend []
  lp/Label-Backend
  (write-labels [instance labels]
    (write-labels labels))
  (overwrite-labels [instance labels]
    (overwrite-labels labels))
  (read-labels [instance]
    (read-labels)))

(defn new-instance [watch-fn]
  (init! watch-fn)
  (Redis-Backend.))
