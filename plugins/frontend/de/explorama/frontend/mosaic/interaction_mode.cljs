(ns de.explorama.frontend.mosaic.interaction-mode
  (:require [de.explorama.frontend.common.frontend-interface :as fi]))

(defn render? []
  (fi/api-definition [:interaction-mode :render-db-get?]))

(defn read-only? []
  (fi/api-definition [:interaction-mode :read-only-db-get?]))

(defn normal-sub? [frame-id]
  (fi/call-api [:interaction-mode :normal-sub?]
               {:frame-id frame-id}))
