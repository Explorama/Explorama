(ns de.explorama.backend.mosaic.data.core
  (:require [de.explorama.backend.common.middleware.cache :as cache]
            [de.explorama.backend.mosaic.attribute-characteristics :as acs]
            [taoensso.timbre :as log]))

(defn attribute-tree [{:keys [client-callback]} [_event]]
  (client-callback (acs/datasource-tree)))

(defn possible-attribute-values [{:keys [client-callback]} [attribute]]
  (client-callback
   attribute
   (acs/possible-attribute-values attribute)))

(defn on-create [tube _]
  (log/debug "Opening a new WebSocket channel " {:client-id (:client-id tube)
                                                 :tube tube})
  tube)

(defn get-events [{:keys [client-callback]} [event-keys]]
  (client-callback
   (into {}
         (map (fn [event-key]
                [event-key (cache/lookup-event event-key)])
              event-keys))))
