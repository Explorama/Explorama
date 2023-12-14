(ns de.explorama.backend.rights-roles.core
  (:require [de.explorama.backend.abac.policy-repository :as pr]
            [de.explorama.backend.environment.common.core :as discovery]
            [integrant.core :as ig]
            [de.explorama.backend.rights-roles.auth-attrs-api :as auth-attrs-management]
            [de.explorama.shared.rights-roles.config :as config]
            [de.explorama.backend.rights-roles.legal-helper :as legal-helper]
            [de.explorama.backend.rights-roles.env :as env]
            [de.explorama.backend.rights-roles.services :as services]
            [de.explorama.backend.rights-roles.tubes]
            [taoensso.timbre :refer [debug]]))

(defmethod ig/init-key :environment.discovery.init.lupta/sub [_ _]
  (services/sub))

(defmethod ig/halt-key! :environment.discovery.init.lupta/sub [_ _]
  (services/unsub))

(defmethod ig/init-key :environment.discovery.init.k8/simple [_ _])

(defmethod ig/halt-key! :environment.discovery.init.k8/simple [_ _])

(defmethod ig/init-key :environment.discovery.init.static/file [_ _])

(defmethod ig/halt-key! :environment.discovery.init.static/file [_ _])

(defn- start-abac []
  (debug "Start abac backend")
  (pr/set-backend config/abac-backend-config))

(defn init-server []
  (auth-attrs-management/init-instance config/authorization-config
                                       config/attributes-info-config)
  (start-abac)
  (env/init)
  (legal-helper/init-legal-dict))

(defn stop-server []
  (discovery/stop))
