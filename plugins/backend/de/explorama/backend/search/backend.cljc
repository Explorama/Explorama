(ns de.explorama.backend.search.backend
  (:require [de.explorama.backend.common.middleware.cache-invalidate :refer [register-invalidate]]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.search.attribute-characteristics.api :as saca]
            [de.explorama.backend.search.client-api
             :as client-api
             :refer [create-di-handler data-descs direct-search-handler
                     initialize recalc-traffic-lights request-attributes
                     search-bar-find-elements search-options]]
            [de.explorama.shared.search.config :as config]
            [de.explorama.backend.search.search-query.core :as sqc]
            [de.explorama.shared.search.ws-api :as ws-api]
            [taoensso.timbre :refer [debug info]]))

(defn init []
  (register-invalidate config/plugin-string
                       {#{"ac"}
                        (fn [_]
                          (info "Search: Invalidating caches")
                          (saca/reset-cache)
                          (client-api/initialize {:client-callback
                                                  (fn [res]
                                                    (frontend-api/broadcast [ws-api/init-client-result res]))}
                                                 nil))})
  (frontend-api/register-routes {ws-api/init-client               initialize

                                 ws-api/direct-search             direct-search-handler

                                 ws-api/create-di                 create-di-handler
                                 ws-api/request-attributes        request-attributes
                                 ws-api/search-options            search-options
                                 ws-api/search-bar-find-elements  search-bar-find-elements
                                 ws-api/recalc-traffic-lights     recalc-traffic-lights
                                 ws-api/data-descs                data-descs

                                 ws-api/search-query-save         sqc/save-query
                                 ws-api/search-query-update-usage sqc/update-query-usage
                                 ws-api/search-query-list         sqc/list-queries
                                 ws-api/search-query-share        sqc/share-query
                                 ws-api/search-query-delete       sqc/delete-query})
  (sqc/new-instance)
  (saca/reset-cache)
  (debug "Search backend started"))