(ns de.explorama.frontend.mosaic.render.cache
  (:require [de.explorama.frontend.mosaic.data-access-layer :as gdal]))

(defonce ^:private event-cache (atom (. cljs.core/PersistentHashMap -EMPTY)))

(defn get-event [frame-id bucket event-id]
  (get-in @event-cache [frame-id [bucket event-id]]))

(defn get-frame-events [frame-id]
  (get @event-cache frame-id))

(defn set-events [frame-id new-events]
  (swap! event-cache
         update
         frame-id
         merge new-events))

(defn delete-frame-events [frame-id]
  (swap! event-cache dissoc frame-id))

(def get-color first)
(defn get-id [data]
  (gdal/get data 1))
(defn get-bucket [data]
  (gdal/get data 2))
(defn get-layout-id [data]
  (gdal/get data 3))

(defn update-annotation [frame-id data-key annotation]
  (swap! event-cache
         update-in
         [frame-id
          [(get-bucket data-key)
           (get-id data-key)]]
         (fn [event]
           (gdal/assoc event "annotation"
                       (gdal/get annotation "annotation")))))
