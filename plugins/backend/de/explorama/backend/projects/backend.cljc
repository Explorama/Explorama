(ns de.explorama.backend.projects.backend
  (:require [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.projects.config]
            [de.explorama.backend.projects.notifications :as notification]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [de.explorama.backend.projects.api :as projects-api]
            [taoensso.timbre :refer [debug warn]]))

(defn- default-fn [name & _]
  (warn "Not yet implemented" name))

(defn init []
  (frontend-api/register-routes {ws-api/update-user-info (partial default-fn ws-api/update-user-info)
                                 ws-api/create-project-route projects-api/create-project
                                 ws-api/log-event-route projects-api/log-event
                                 ws-api/load-project-after-create-route projects-api/load-project-after-create
                                 ws-api/request-projects-route projects-api/request-projects
                                 ws-api/load-project-infos-route projects-api/load-project-infos
                                 ws-api/update-project-detail-route projects-api/update-project-detail
                                 ws-api/unloaded-project-route projects-api/unloaded-project
                                 ws-api/copy-project-route projects-api/copy-project
                                 ws-api/based-events-route projects-api/based-events
                                 ws-api/load-head-route projects-api/load-head
                                 ws-api/delete-project-route projects-api/delete-project

                                 ws-api/project-sync-event-route (partial default-fn ws-api/project-sync-event-route)
                                 ws-api/re-connect-route (partial default-fn ws-api/re-connect-route)
                                 ws-api/search-projects-route (partial default-fn ws-api/search-projects-route)
                                 ws-api/create-snapshot-route (partial default-fn ws-api/create-snapshot-route)
                                 ws-api/delete-snapshot-route (partial default-fn ws-api/delete-snapshot-route)
                                 ws-api/server-loaded-project-route (partial default-fn ws-api/server-loaded-project-route)
                                 ws-api/update-snapshot-desc-route (partial default-fn ws-api/update-snapshot-desc-route)
                                 ws-api/exportable-route (partial default-fn ws-api/exportable-route)
                                 ws-api/share-project-route (partial default-fn ws-api/share-project-route)
                                 ws-api/show-project-in-overview-route (partial default-fn ws-api/show-project-in-overview-route)
                                 ws-api/create-session-route (partial default-fn ws-api/create-session-route)
                                 ws-api/save-as-file-route (partial default-fn ws-api/save-as-file-route)
                                 ws-api/search-route (partial default-fn ws-api/search-route)
                                 ws-api/mouse-position-update-route (partial default-fn ws-api/mouse-position-update-route)
                                 ws-api/automated-tests-results-route (partial default-fn ws-api/automated-tests-results-route)})
  (notification/init!)
  (debug "Projects backend started"))