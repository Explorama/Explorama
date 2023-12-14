(ns de.explorama.backend.charts.backend
  (:require [de.explorama.backend.charts.attribute-characteristics :as acs]
            [de.explorama.backend.charts.data.api :as data-api]
            [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.charts.config :as config-charts]
            [de.explorama.shared.charts.ws-api :as ws-api]
            [taoensso.timbre :refer [debug warn]]))

(defn- default-fn [name & _]
  (warn "Not yet implemented" name))

(defn init []
  (frontend-api/register-routes {ws-api/update-user-info (partial default-fn ws-api/update-user-info)
                                 ws-api/set-backend-canceled (partial default-fn ws-api/set-backend-canceled)
                                 ws-api/retrieve-external-ref (partial default-fn ws-api/retrieve-external-ref)
                                 ws-api/chart-datasets data-api/chart-data})
  (cache-invalidate/register-invalidate config-charts/plugin-string
                                        {#{"ac"} (fn [_]
                                                   (acs/populate-ui-options!))})
  (acs/populate-ui-options!)
  (debug "charts backend started"))