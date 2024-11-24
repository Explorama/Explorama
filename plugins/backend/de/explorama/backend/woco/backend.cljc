(ns de.explorama.backend.woco.backend
  (:require [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.backend.woco.websocket :as websocket]
            [taoensso.timbre :refer [debug]]))

(defn init []
  (frontend-api/register-routes websocket/endpoints)
  (debug "Woco backend started"))