(ns de.explorama.backend.data-atlas.backend
  (:require [de.explorama.backend.data-atlas.api :as api]
            [de.explorama.backend.data-atlas.attribute-characteristics :as daac]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.data-atlas.ws-api :as ws-api]
            [taoensso.timbre :refer [debug]]))

(defn init []
  (frontend-api/register-routes {ws-api/get-data-elements-route api/get-data-elements})
  (daac/reset-cache)
  (daac/get-all-types)
  (debug "Data-atlas backend started"))