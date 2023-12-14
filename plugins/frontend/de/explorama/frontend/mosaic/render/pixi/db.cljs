(ns de.explorama.frontend.mosaic.render.pixi.db
  (:require [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.engine :as gre]))

(defonce instances (atom {}))
(defonce instances-headless (atom {}))

(defn get-viewport [path]
  (when-let [instance (get @instances (gp/top-level path))]
    (select-keys (gre/state instance)
                 [:x :y :z :next-zoom])))

(defn update-couple-state [path by with max-levels]
  (when-let [instance (get @instances (gp/top-level path))] 
    (gre/merge-state! instance 
                      {:coupled (cond-> {}
                                  with (assoc :with with)
                                  by (assoc :by by)
                                  max-levels (assoc :max-levels max-levels))})))

(defn decouple-instance [path]
  (when-let [instance (get @instances (gp/top-level path))] 
    (gre/dissoc-state! instance [:coupled])))

(defn couple-state [path]
  (when-let [instance (get @instances (gp/top-level path))]
    (:coupled (gre/state instance))))