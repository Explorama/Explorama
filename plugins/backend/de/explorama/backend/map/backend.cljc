(ns de.explorama.backend.map.backend
  (:require [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.map.attribute-characteristics :as acs]
            [de.explorama.backend.map.client-api :as client-api]
            [de.explorama.shared.map.config :as config]
            [de.explorama.shared.map.ws-api :as ws-api]
            [taoensso.timbre :refer [debug warn]]))

(defn- default-fn [name & _]
  (warn "Not yet implemented" name))

(defn init []
  (frontend-api/register-routes {ws-api/load-layer-config client-api/load-layer-config
                                 ws-api/load-acs client-api/load-acs
                                 ws-api/load-external-ref (partial default-fn ws-api/load-external-ref)
                                 ws-api/remove-data client-api/retrieve-event-data
                                 ws-api/operation client-api/operation-tasks
                                 ws-api/retrieve-event-data client-api/retrieve-event-data
                                 ws-api/update-usable-config-ids client-api/update-usable-configs})
  (cache-invalidate/register-invalidate config/plugin-string
                                        {#{"ac"} (fn [_]
                                                   (acs/populate-ui-options!))})
  (acs/populate-ui-options!)
  (debug "Map backend started"))