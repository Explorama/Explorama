
(ns de.explorama.backend.rights-roles.env
  (:require [de.explorama.backend.environment.common.core :as discovery]
            [de.explorama.backend.environment.common.discovery :refer [VERTICALS]]
            [de.explorama.shared.rights-roles.config :as config]
            [de.explorama.backend.rights-roles.service-states :as servs]))

(defn init []
  (discovery/init
   config/plugin-key
   {:states {VERTICALS @#'servs/current-verticals}}))