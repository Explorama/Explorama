(ns de.explorama.backend.mosaic.backend
  (:require [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.mosaic.attribute-characteristics :as acs]
            [de.explorama.shared.mosaic.config :as config]
            [de.explorama.backend.mosaic.data.aggregation :as aggregation]
            [de.explorama.backend.mosaic.data.core :as data-core]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [taoensso.timbre :refer [debug warn]]))

(defn- default-fn [name & _]
  (warn "Not yet implemented" name))

(defn init []
  (frontend-api/register-routes {ws-api/user-info-update-route (partial default-fn ws-api/user-info-update-route)
                                 ws-api/initialize-route aggregation/initialize
                                 ws-api/attribute-tree-route data-core/attribute-tree
                                 ws-api/possible-values-route data-core/possible-attribute-values
                                 ws-api/get-events-route data-core/get-events
                                 ws-api/operations-route aggregation/operations
                                 ws-api/on-destroy-route (partial default-fn ws-api/on-destroy-route)
                                 ws-api/on-create-route (partial default-fn ws-api/on-create-route)})
  (cache-invalidate/register-invalidate config/plugin-string
                                        {#{"ac"} (fn [_]
                                                   (acs/populate-ui-options!))})
  (acs/populate-ui-options!)
  (debug "Mosaic backend started"))