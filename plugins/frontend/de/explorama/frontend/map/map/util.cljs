(ns de.explorama.frontend.map.map.util
  (:require [de.explorama.frontend.map.config :as config]))


(defn map-canvas-id [frame-id]
  (config/frame-body-dom-id frame-id)
  #_(str "map-" frame-id "-" workspace-id))