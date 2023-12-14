(ns de.explorama.backend.rights-roles.handler
  (:require [clojure.string :as string]
            [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.backend.rights-roles.auth-attrs-api :as auth-attrs-management]
            [de.explorama.backend.rights-roles.config :as config]
            [de.explorama.backend.rights-roles.legal-helper :as legal-helper]
            [hiccup.page :refer [html5]]
            [jsonista.core :as json]
            [ring.util.codec :as ring-codec]
            [ring.util.response :refer [redirect set-cookie]]
            [taoensso.timbre :refer [info]]))

(def ^:private legal-inline-js "re")

(defn page
  "Creates a login page used with nginx auth_request login"
  [login-target
   req
   {:keys [ldap-available message valid?]
    :or {valid? true}
    :as resp}
   input
   local-assets]
  (let [login-class (cond
                      valid? ""
                      (and (seq message)
                           (not ldap-available)) "ldap__error"
                      (seq message) "login_eror")]
    (html5
     {:lang (name (legal-helper/req->language-key req))}
     (into [:head
            [:title (str config/system-name " login")]
            [:meta {:charset "utf-8"}]
            [:meta {:http-equiv "X-UA-Compatible"
                    :content "IE=edge,chrome=1"}]
            ;; (when config/keycloak-auth?
            ;;   [:script {:type "text/javascript"}
            ;;    keycloak-sso-inline-js])
            [:script {:type "text/javascript"}
             legal-inline-js]
            [:link {:rel "shortcut icon"
                    :href "images/favicon.ico"}]]
           local-assets)
     [:body.initial.login
      [:div#app
       (auth-attrs-management/login-page login-class login-target resp input)
       (legal-helper/request->hiccup-desc req)]])))

(defn- parse-login-body
  "String is a <key>=<value> where each pair is combined with &"
  [body]
  (when (seq body)
    (into {}
          (map (fn [[k v]]
                 [(keyword k) v]))
          (ring-codec/form-decode body "UTF-8"))))

;; Same as in explorama_keycloak.js (resources)
(def ^:private params-blacklist ["code" "state" "session_state"])

(defn- reduce-login-params [target]
  (let [[target-path target-params] (if (string/includes? target "?")
                                      (string/split target #"\?")
                                      [target])

        target-params (when (seq target-params)
                        (ring-codec/form-decode (string/replace target-params #"\?" "")
                                                "UTF-8"))
        target-params (cond-> (apply dissoc target-params params-blacklist)
                        :always (ring-codec/form-encode "UTF-8"))
        new-target (cond-> ""
                     (seq target-path)
                     (str target-path)
                     (seq target-params)
                     (str "?" target-params))]
    new-target))

(defn routes [local-assets]
  [["/login" {:get {:handler (fn [req]
                               (page (get-in req [:headers "x-target"]) req nil nil local-assets))}
              :post {:handler (fn [req]
                                (let [body (slurp (:body req))
                                      {:keys [remember-me target]
                                       :as org-input} (parse-login-body body)
                                      {:keys [valid? response token refresh-token]}
                                      (auth-attrs-management/login-user-form org-input)
                                      target (reduce-login-params target)]
                                  (if (and valid? (seq token))
                                    (cond-> (redirect target)
                                      :always (set-cookie "session" (or refresh-token "") {:http-only true
                                                                                           :path "/"})
                                      (not remember-me) (set-cookie "nginxauth" token {:http-only true
                                                                                       :path "/"})
                                      remember-me (set-cookie "nginxauth" token {:http-only true
                                                                                 :path "/"
                                                                                 :max-age config/token-experation}))
                                    (page target req response org-input local-assets))))}}]
   ["/logout" {:post (fn [{:keys [cookies] :as req}]
                       (let [token (get-in cookies ["nginxauth" :value])]
                         (if (seq token)
                           (-> (auth-attrs-management/logout-user req)
                               (set-cookie "nginxauth" "" {:http-only true
                                                           :path "/"}))
                           {:status 400})))}]
   ["/validate" {:get {:handler (fn [{:keys [cookies]}]
                                  (let [{:keys [user-info token session expires-in]}
                                        (auth-attrs-management/validate-user-token cookies)]
                                    (if user-info
                                      (-> {:status 200
                                           :body {:user-info user-info
                                                  :expires-in expires-in}}
                                          (set-cookie "nginxauth" token
                                                      {:http-only true
                                                       :path "/"
                                                       :max-age config/token-experation})
                                          (set-cookie "session" (or session "")
                                                      {:http-only true
                                                       :path "/"
                                                       :max-age config/token-experation}))
                                      (-> {:status 403}
                                          (set-cookie "nginxauth" "" {:http-only true
                                                                      :path "/"})
                                          (set-cookie "session" "" {:http-only true
                                                                    :path "/"})))))}}]
   ["/ui/auth" {:get {:handler (fn [{:keys [cookies]}]
                                 (let [token (get-in cookies ["nginxauth" :value])
                                       user-info (when (:valid? (jwt/token-valid? token))
                                                   (jwt/token-payload token))]
                                   (if user-info
                                     {:status 200
                                      :cookies {"nginxauth" {:value (jwt/user-token user-info)
                                                             :http-only true
                                                             :path "/"
                                                             :max-age config/token-experation}}
                                      :headers {}}
                                     {:status 401})))}}]
   ["/admin-token" {:get {:handler (fn [body]
                                     (let [req (json/read-value body json/keyword-keys-object-mapper)
                                           admin-token (auth-attrs-management/admin-token req)]
                                       (if (seq admin-token)
                                         {:status 200
                                          :headers {"Content-Type" "text/plain"}
                                          :body admin-token}
                                         {:status 403
                                          :body {}})))}}]
   ["/user-infos/refresh" {:post {:handler (fn [req]
                                             (let [ip-addr (or (get-in req [:headers "x-forwarded-for"])
                                                               (:remote-addr req))]
                                               (info "Received user-infos refresh request from" ip-addr)
                                               (auth-attrs-management/refresh-loaded-user-infos)
                                               {:status 200}))}}]])
