(ns de.explorama.backend.reporting.core
  (:require [de.explorama.backend.abac.policy-repository :as pr]
            [de.explorama.backend.environment.common.core :as discovery]
            [de.explorama.backend.environment.common.discovery :refer [VERTICALS]]
            [de.explorama.backend.environment.fi.assets :refer [resolve-assets]]
            [de.explorama.backend.reporting.env :as env]
            [de.explorama.backend.reporting.policies :as pol]
            [de.explorama.backend.reporting.services :as services]
            [integrant.core :as ig]))

(defmethod ig/init-key :environment.discovery.init.lupta/sub [_ _]
  (services/sub))

(defmethod ig/halt-key! :environment.discovery.init.lupta/sub [_ _]
  (services/unsub))

(defmethod ig/init-key :environment.discovery.init.k8/simple [_ {discovery-client :discovery}]
  (resolve-assets services/supported-vertical-types @(.retrieve-space-value discovery-client VERTICALS)))

(defmethod ig/halt-key! :environment.discovery.init.k8/simple [_ _])

(defmethod ig/init-key :environment.discovery.init.static/file [_ {discovery-client :discovery}]
  (resolve-assets services/supported-vertical-types @(.retrieve-space-value discovery-client VERTICALS)))

(defmethod ig/halt-key! :environment.discovery.init.static/file [_ _])

(defn- start-abac []
  (pr/set-backend)
  (pr/init-repository pol/standard-rights false))

(defn init-server []
  (start-abac)
  (env/init)
  "done")

(defn stop-server []
  (discovery/stop))
