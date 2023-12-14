(ns de.explorama.backend.rights-roles.oidc.util
  (:require [de.explorama.backend.rights-roles.http-util :refer [safe-http-get safe-http-post]]))

(defonce api-routes (atom {}))

(defn- base-url->well-known-url [{:keys [base-url url-prefix realm]
                                  :or {url-prefix ""}}]
  (str base-url
       url-prefix
       "/realms/"
       realm
       "/.well-known/openid-configuration"))

(defn- config->id [config]
  (select-keys config [:base-url :url-prefix :realm]))

(defn- fetch-routes [config]
  (let [config (config->id config)]
    (when-not (get @api-routes config)
      (swap! api-routes
             assoc
             config
             (safe-http-get (base-url->well-known-url config)
                            {})))))

(defn- api-route
  [config identifier]
  (if-let [routes (get @api-routes (config->id config))]
    (get routes identifier)
    (get-in (fetch-routes config)
            [(config->id config)
             identifier])))

(defn token-endpoint [config]
  (api-route config :token_endpoint))

(defn certs-endpoint [config]
  (api-route config :jwks_uri))

(defn logout-endpoint [config]
  (api-route config :end_session_endpoint))

(defn auth-endpoint [config]
  (api-route config :authorization_endpoint))

(defn oidc-authorization [{:keys [client-id client-secret] :as auth-config}
                          {code "code" session-state "session_state" state "state"}
                          redirect-url]
  (when (and (seq code)
             (seq session-state)
             (seq state))
    (safe-http-post (token-endpoint auth-config)
                    {:content-type "application/x-www-form-urlencoded"
                     :form-params {:grant_type "authorization_code"
                                   :client_id client-id
                                   :client_secret client-secret
                                   :code code
                                   :redirect_uri redirect-url}})))

(defn oidc-refresh-token [{:keys [client-id client-secret] :as auth-config}
                          refresh-token]
  (when (seq refresh-token)
    (safe-http-post (token-endpoint auth-config)
                    {:content-type "application/x-www-form-urlencoded"
                     :form-params {:client_id client-id
                                   :client_secret client-secret
                                   :refresh_token refresh-token
                                   :grant_type "refresh_token"}})))

(comment
  (def config {:realm "explorama"
               :base-url "http://localhost:8085"
               :url-prefix "/auth"})
  (reset! api-routes {})
  (get @api-routes (config->id config))
  (fetch-routes config)
  (token-endpoint config)
  (certs-endpoint config)
  (logout-endpoint config)
  (auth-endpoint config))


