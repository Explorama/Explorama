(ns de.explorama.backend.reporting.backend
  (:require [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.reporting.config]
            [de.explorama.backend.reporting.persistence.api :as persistence-api]
            [de.explorama.backend.reporting.websocket :as websocket]
            [taoensso.timbre :refer [debug]]))

(defn init []
  (frontend-api/register-routes websocket/endpoints)
  (persistence-api/new-instances)
  (debug "Reporting backend started"))