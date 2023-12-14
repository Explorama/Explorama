(ns de.explorama.backend.rights-roles.authorization.adapter-keycloak
  (:require [de.explorama.backend.abac.jwt :as jwt]
            [buddy.core.keys :as keys]
            [buddy.sign.jwt :as buddy-jwt]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [de.explorama.backend.rights-roles.crypto-util :refer [aes-decrypt aes-encrypt decrypt
                                                                   encrypt]]
            [muuntaja.core]
            [muuntaja.format.json]
            [de.explorama.backend.rights-roles.attribute-infos.api :as attrs]
            [de.explorama.backend.rights-roles.authorization.interface :as interface]
            [de.explorama.backend.rights-roles.config :as config]
            [de.explorama.backend.rights-roles.http-util :refer [safe-http-get safe-http-post]]
            [de.explorama.backend.rights-roles.keycloak.util :refer [admin-cli-token
                                                                     client-id->id-client client-roles-route
                                                                     roles-route]]
            [de.explorama.backend.rights-roles.oidc.util :refer [auth-endpoint certs-endpoint
                                                                 logout-endpoint oidc-authorization
                                                                 oidc-refresh-token token-endpoint]]
            [ring.util.codec :as ring-codec]
            [taoensso.timbre :refer [debug]]))

(defn- retrieve-jwks [auth-config]
  (let [url (certs-endpoint auth-config)]
    (:keys (safe-http-get url {}))))

(defn- retrieve-claims-core [auth-token jwk]
  (try
    (buddy-jwt/unsign auth-token
                      (keys/jwk->public-key jwk)
                      {:alg (-> jwk :alg str/lower-case keyword)})
    (catch Exception _e
      nil)))

(defn- sso-logout-url
  ([auth-config]
   (logout-endpoint auth-config))
  ([{:keys [client-id] :as auth-config} redirect-uri i]
   (str (sso-logout-url auth-config)
        "?post_logout_redirect_uri=" redirect-uri
        "&client_id=" client-id
        "&id_token_hint=" i)))

(defn parse-sso-token [sso-query-string]
  (ring-codec/form-decode sso-query-string "UTF-8"))

(defn- logout-user [{:keys [logout-session?] :as auth-config}
                    {:keys [headers cookies]}]
  (let [referer (get headers "referer")
        token (get-in cookies ["nginxauth" :value])
        {:keys [i]} (jwt/token-payload token)]
    {:body (when logout-session?
             (sso-logout-url auth-config referer i))}))

(defn retrieve-claims [auth-token jwks]
  (some (partial retrieve-claims-core auth-token) jwks))

(defonce keycloak-sso-inline-js (slurp (io/resource "static/explorama_keycloak.js")))

(defn- login-page [{:keys [client-id] :as auth-config}
                   login-target login-class
                   {:keys [message]}
                   {:keys [remember-me]}]
  [:div.absolute.center.flex.flex-col.align-items-center.w-320
   {:class login-class}
   [:script {:type "text/javascript"}
    keycloak-sso-inline-js]
   [:img.login-logo
    {:alt (str config/system-name " logo")
     :src (interface/login-header-image)}]
   [:div.animation-fade-in.w-full
    [:form {:action "/login"
            :method "post"
            :id "sso-login-form"}
     [:input {:type "hidden"
              :name "sso-query-string"
              :id "sso-query-string"}]
     [:input {:type "hidden"
              :name "redirect-url"
              :id "redirect-url"}]
     [:input {:type "hidden"
              :name "target"
              :value login-target}]]
    [:form.flex.flex-column.gap-8
     {:action "/login"
      :method "post"}
     (when (seq message)
       [:div.flex.align-items-center.gap-6.px-12.py-8.rounded-xs.bg-black-alpha-50.text-red
        [:span.icon-error.icon-red]
        message])
     [:div.checkbox
      [:input (let [base {:type "checkbox"
                          :tabindex 0
                          :name "remember-me"
                          :id "remember-me"}]
                (if remember-me
                  (assoc base :checked true)
                  base))]
      [:label {:for "remember-me"}
       "Remember me"]]
     [:input {:name "target"
              :type "hidden"
              :value login-target}]
     [:button.btn-primary.btn-large
      {:type "button"
       :onclick
       (str "explorama_keycloak_auth('"
            (auth-endpoint auth-config)
            "','" client-id "')")}
      "Sign in"]]]])

(defn- claim->roles [{:keys [client-roles-only? client-role-path client-id]} retrieved-claim]
  (let [client-role-path (or client-role-path
                             [:resource_access (keyword client-id) :roles])]
    (cond-> []
      (not client-roles-only?) (into (get-in retrieved-claim [:realm_access :roles]))
      :always (into (get-in retrieved-claim client-role-path)))))

(defn- access-token->user-info [jwks auth-config access-token]
  (let [{username :preferred_username
         :as res} (retrieve-claims access-token jwks)
        roles (claim->roles auth-config res)
        user-info (when username (attrs/username->user-info username roles))]
    user-info))

(defn- enc-refresh-token [refresh-token]
  (str (-> refresh-token
           (aes-encrypt)
           (update :secret encrypt)
           (update :initv encrypt))))

(defn- dec-refresh-token [enc-refresh-token]
  (let [{:keys [content secret initv]} (when (seq enc-refresh-token)
                                         (edn/read-string enc-refresh-token))
        dec-refresh-token (when (and content secret initv)
                            (aes-decrypt content
                                         (decrypt secret)
                                         (decrypt initv)))]
    dec-refresh-token))

(defn- login-user [jwks auth-config sso-query-string redirect-url]
  (let [{:keys [id_token access_token refresh_token expires_in]}
        (oidc-authorization auth-config sso-query-string redirect-url)
        session-state (get sso-query-string "session_state")
        {:keys [role] :as user-info} (access-token->user-info jwks auth-config access_token)
        response (if (not (seq role))
                   (do
                     (debug "User login failed, possible no rights" {:user-info-from-attr-provider user-info})
                     {:ldap-available true
                      :message "You have no login rights, please contact your Admin."
                      :valid? false})
                   {:valid? true})]
    {:valid? (and (boolean user-info) (:valid? response))
     :response response
     :session-state session-state
     :refresh-token (enc-refresh-token refresh_token)
     :expires-in (if (= 0 expires_in)
                   config/access-token-lifetime
                   expires_in)
     :user-info user-info
     :token (jwt/user-token (assoc user-info
                                   :i id_token))}))

(defn- validate-user-token [jwks auth-config cookies]
  (let [token (get-in cookies ["nginxauth" :value])
        session-token (get-in cookies ["session" :value])
        user-info (when (:valid? (jwt/token-valid? token))
                    (jwt/token-payload token))
        dec-refresh-token (dec-refresh-token session-token)
        {:keys [access_token expires_in refresh_token]} (oidc-refresh-token auth-config dec-refresh-token)
        access-user-info (access-token->user-info jwks auth-config access_token)
        user-info-equal? (= (select-keys user-info [:username :role])
                            (select-keys access-user-info [:username :role]))
        n-token (when user-info-equal?
                  (jwt/user-token user-info))]
    (when (and user-info-equal? (seq refresh_token))
      {:user-info (assoc user-info :token n-token)
       :token n-token
       :session (enc-refresh-token refresh_token)
       :expires-in expires_in})))

(defn- admin-token [jwks {:keys [client-id client-secret] :as auth-config} {:keys [username password]}]
  (let [{access-token :access_token} (safe-http-post (token-endpoint auth-config)
                                                     {:form-params {:client_id client-id
                                                                    :client_secret client-secret
                                                                    :password password
                                                                    :username username
                                                                    :grant_type "password"}})
        roles (claim->roles auth-config (retrieve-claims access-token jwks))
        user-info (when (seq access-token)
                    (attrs/username->user-info username roles))]
    (jwt/admin-token user-info)))

(defn- list-roles [{:keys [client-roles-only?] :as auth-config} id-client]
  (let [token (admin-cli-token auth-config)
        parsed-body-realm (if-not client-roles-only?
                            (safe-http-get (roles-route auth-config)
                                           {:headers {"authorization" (str "bearer " token)}})
                            [])

        parsed-body-client (safe-http-get (client-roles-route auth-config id-client)
                                          {:headers {"authorization" (str "bearer " token)}})

        all-roles (reduce (fn [acc {:keys [name]}]
                            (conj acc name))
                          #{}
                          (into parsed-body-realm
                                parsed-body-client))]
    all-roles))

(deftype Auth-Keycloak [auth-config id-client jwks]
  interface/Authorization
  (login-page [_ login-class login-target response input]
    (login-page auth-config login-target login-class response input))
  (login-user-form [_ {:keys [sso-query-string redirect-url]}]
    (login-user jwks auth-config (parse-sso-token sso-query-string) redirect-url))
  (logout-user [_ req]
    (logout-user auth-config req))
  (admin-token [_ req]
    (admin-token jwks auth-config req))
  (list-roles [_]
    (list-roles auth-config id-client))
  (validate-user-token [_ cookies]
    (validate-user-token jwks auth-config cookies)))

(defn new-instance [auth-config]
  (let [jwks (retrieve-jwks auth-config)
        id-client (client-id->id-client auth-config)]
    (->Auth-Keycloak auth-config id-client jwks)))

(comment
  (def auth-conf {:type :keycloak
                  :base-url "http://localhost:8085"
                  :url-prefix "/auth"
                  :realm "explorama"
                  :confidential? true
                  :client-id "explorama"
                  :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                  :client-roles-only? true})

  (client-id->id-client auth-conf)

  (def demo-conf {:base-url "http://acc-demo.dev-explorama.com/keycloak"
                  :realm "explorama"
                  :client-id "explorama"
                  :client-secret "n8mI7d37nCtDXO4Aw6devND2mNsoQ2Aw"
                  :client-roles-only? true
                  :displayname [:firstName :lastName]})

  (let [id-client (client-id->id-client demo-conf)]
    (list-roles demo-conf id-client))

  (def jwks (retrieve-jwks auth-conf))
  (token-endpoint auth-conf)
  (admin-token jwks auth-conf {:username "PAdmin"
                               :password "PAdmin18"}))

