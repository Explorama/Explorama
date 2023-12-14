(ns de.explorama.backend.indicator.backend
  (:require [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.indicator.attribute-characteristics :as acs]
            [de.explorama.backend.indicator.calculate :as calc]
            [de.explorama.backend.indicator.config :as config-indicator]
            [de.explorama.backend.indicator.data.core :as data]
            [de.explorama.backend.indicator.persistence.api :as persistence]
            [de.explorama.backend.indicator.persistence.store.core :as store]
            [de.explorama.shared.indicator.config :as config-shared-indicator]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [taoensso.timbre :refer [debug warn]]))

(defn- default-fn [name & _]
  (warn "Not yet implemented" name))

(defn indicator-ui-descriptions [{:keys [client-callback]}]
  (client-callback config-indicator/explorama-indicator-ui))

(defn init []
  (frontend-api/register-routes {ws-api/update-user-info (partial default-fn ws-api/update-user-info)
                                 ws-api/connect-to-di data/connect-to-di
                                 ws-api/create-new-indicator persistence/create-new-indicator
                                 ws-api/share-indicator persistence/share-indicator
                                 ws-api/update-indicator-infos persistence/update-indicator
                                 ws-api/all-indicators persistence/all-user-indicators
                                 ws-api/delete-indicator persistence/delete-indicator
                                 ws-api/load-indicator-ui-descs indicator-ui-descriptions
                                 ws-api/data-sample data/data-sample
                                 ws-api/create-and-publish-di calc/create-di-and-acs})
  (store/new-instance)
  (cache-invalidate/register-invalidate config-shared-indicator/plugin-string
                                        {#{"ac"} (fn [_]
                                                   (acs/create-options-ui))})
  (acs/create-options-ui)
  (debug "Indicator backend started"))