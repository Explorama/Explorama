(ns de.explorama.backend.configuration.backend
  (:require [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.configuration.ac-api :as ac-api]
            [de.explorama.backend.configuration.datasource-api :as datasource-api]
            [de.explorama.backend.configuration.persistence.configs.api :as configs-api]
            [de.explorama.backend.configuration.persistence.i18n.api :as i18n-api]
            [de.explorama.backend.configuration.persistence.i18n.core :as i18n]
            [de.explorama.backend.configuration.user-configs :as user-config]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.configuration.config :as config]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            [taoensso.timbre :refer [debug]]))

(defn init []
  (frontend-api/register-routes {ws-api/load-defaults-route configs-api/load-defaults
                                 ws-api/list-entries-route configs-api/list-entries
                                 ws-api/get-entry-route configs-api/get-entry
                                 ws-api/update-entry-route configs-api/update-entry
                                 ws-api/delete-entry-route configs-api/delete-entry
                                 ws-api/copy-entry-route configs-api/copy-entry

                                 ws-api/load-translations-route i18n-api/get-translations
                                 ws-api/load-labels-route i18n-api/get-labels

                                 ws-api/delete-datasource-route datasource-api/delete-datasource

                                 ws-api/get-acs ac-api/initialize
                                 ws-api/get-attr-characteristics ac-api/attr-characteristics
                                 ws-api/get-available-datasources ac-api/available-datasources
                                 ws-api/get-search-config user-config/initialize})
  (cache-invalidate/register-invalidate config/plugin-string
                                        {#{"ac"} (fn [_]
                                                   (ac-api/init-acs)
                                                   (frontend-api/broadcast
                                                    [ws-api/set-acs {:types (ac-api/attribute-types)}]))})
  (configs-api/init)
  (i18n/init)
  (debug "Configuration backend started"))