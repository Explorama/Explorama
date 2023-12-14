(ns de.explorama.backend.table.backend
  (:require [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.table.config]
            [de.explorama.backend.table.websocket :as websocket]
            [taoensso.timbre :refer [debug]]))

(defn init []
  (frontend-api/register-routes websocket/endpoints)
  (debug "table backend started"))