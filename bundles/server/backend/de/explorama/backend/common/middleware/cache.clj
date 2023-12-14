(ns de.explorama.backend.common.middleware.cache
  (:require [de.explorama.backend.expdb.middleware.indexed :as idb]))

(def ^:private event->dt (atom {})) ;TODO r1/expdb is that a thing for the expdb??
(def ^:private dt-stored? (atom #{}))

(def ^:private aggregated-di->events (atom {}))
(def ^:private events (atom {}))

(defn lookup [data-tiles & _]
  (let [dts (idb/get+ data-tiles)]
    (swap! event->dt merge (reduce (fn [acc [dt events]]
                                     (if (@dt-stored? dt)
                                       acc
                                       (do
                                         (swap! dt-stored? conj dt)
                                         (into acc (mapv #(vector [(get dt "bucket") (get % "id")] dt) events)))))
                                   {}
                                   dts))
    dts))

(defn reset-states []
  (reset! event->dt {})
  (reset! dt-stored? #{})
  (let [_old-dis (keys @aggregated-di->events)])
    ;TODO r1/indicator reheat the caches
    
  (reset! aggregated-di->events {})
  (reset! events {}))

(defn lookup-event [event-id]
  (when (second event-id)
    (cond (get @event->dt event-id)
          (idb/get-event (get @event->dt event-id)
                         (second event-id))
          (get @events (second event-id))
          (get @events (second event-id))
          :else nil)))

(defn index-aggregation-result [di result]
  (let [result (into {} (map (fn [event]
                               [(get event "id") event])
                             result))]
    (swap! aggregated-di->events assoc di (vec (keys result)))
    (swap! events merge result)
    nil))
  
