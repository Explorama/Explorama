(ns de.explorama.backend.rights-roles.keycloak.util
  (:require [de.explorama.backend.rights-roles.http-util :refer [safe-http-get safe-http-post]]
            [de.explorama.backend.rights-roles.oidc.util :refer [token-endpoint]]
            [taoensso.timbre :refer [debug]]))

(defn realm-url [{:keys [base-url url-prefix realm]
                  :or {url-prefix ""}}]
  (format "%s%s/admin/realms/%s"
          base-url
          url-prefix
          realm))

(defn users-route [config]
  (str (realm-url config)
       "/users"))

(defn clients-route [config]
  (str (realm-url config)
       "/clients"))

(defn groups-route [config]
  (str (realm-url config)
       "/groups"))

(defn group-members-route [config group-id]
  (str (groups-route config)
       "/" group-id "/members"))

(defn roles-route [config]
  (str (realm-url config)
       "/roles"))

(defn realm-roles-users [config role-name]
  (str (roles-route config)
       "/" role-name
       "/users"))

(defn client-roles-route [config id-client]
  (str (clients-route config)
       "/" id-client
       "/roles"))

(defn client-roles-users [config id-client role-name]
  (str (client-roles-route config id-client)
       "/" role-name
       "/users"))

(defn admin-cli-token [{:keys [client-id client-secret] :as config}]
  (let [form-params {:scope "openid"
                     :client_id client-id
                     :client_secret client-secret
                     :grant_type "client_credentials"}
        parsed-body (safe-http-post (token-endpoint config)
                                    {:form-params form-params})]
    (get parsed-body :access_token)))

(defn client-id->id-client [{:keys [client-id] :as config}]
  (debug "Get client uuid" client-id)
  (let [token (admin-cli-token config)
        parsed-body (when (seq token)
                      (safe-http-get (clients-route config)
                                     {:query-params {:clientId client-id}
                                      :headers {"authorization" (str "bearer " token)}}))]
    (:id (first parsed-body))))

(comment
  (def auth-conf {:base-url "http://localhost:8085"
                  :url-prefix "/auth"
                  :realm "explorama"
                  :client-id "explorama"
                  :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                  :client-roles-only? false})

  (client-id->id-client auth-conf)
  (admin-cli-token auth-conf))