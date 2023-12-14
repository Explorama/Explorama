(ns de.explorama.backend.rights-roles.authorization.adapter-ldap
  (:require [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.backend.rights-roles.attribute-infos.api :as attrs]
            [de.explorama.backend.rights-roles.authorization.interface :as interface]
            [de.explorama.backend.rights-roles.config :as config]
            [de.explorama.backend.rights-roles.ldap.ldap-client :as ldap-client]
            [de.explorama.backend.rights-roles.ldap.ldap-util :as ldap-util]
            [taoensso.timbre :refer [error]]))

(defn- logout-user [{:keys [headers]}]
  (let [referer (get headers "referer")]
    {:body referer}))

(defn- login-page [login-target login-class
                   {:keys [message]}
                   {:keys [username password remember-me]}]
  [:div.absolute.center.flex.flex-col.align-items-center.w-320
   {:class login-class}
   [:img.login-logo
    {:alt (str config/system-name " logo")
     :src (interface/login-header-image)}]
   [:div.animation-fade-in.w-full
    [:form.flex.flex-column.gap-8
     {:action "/login"
      :method "post"}
     [:div.input
      [:div.text-input
       [:input {:type "text"
                :name "username"
                :placeholder "Username"
                :autocomplete "username"
                :id "login-user"
                :value username}
        [:button.btn-clear
         {:type "button"
          :aria-label "clear username"
          :onClick "document.getElementById('login-user').value = '';"}
         [:span.icon-close]]]]]
     [:div.input
      [:div.text-input
       [:input {:type "password"
                :name "password"
                :placeholder "Password"
                :autocomplete "current-password"
                :id "login-pw"
                :value password}
        [:button.btn-clear
         {:type "button"
          :aria-label "clear password"
          :onClick "document.getElementById('login-pw').value = '';"}
         [:span.icon-close]]]]]
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
      {:type "submit"}
      "Sign in"]]]])

(defn- login-user [auth-config {:keys [username password]}]
  (let [{:keys [login-valid role ldap-available message]} (ldap-client/login-valid? auth-config
                                                                                    username
                                                                                    password)
        {:keys [role] :as user-info} (when login-valid
                                       (attrs/username->user-info username role))
        token (when login-valid (jwt/user-token user-info))
        response (cond (and login-valid
                            (not (seq role))) {:ldap-available true
                                               :message "You have no login rights, please contact your Admin."
                                               :valid? false}
                       (not login-valid) {:message message
                                          :ldap-available ldap-available})]
    {:valid? (nil? response)
     :response response
     :user-info user-info
     :token token
     :expires-in config/access-token-lifetime}))

(defn- validate-user-token [cookies]
  (let [token (get-in cookies ["nginxauth" :value])
        user-info (when (:valid? (jwt/token-valid? token))
                    (jwt/token-payload token))
        new-token (when (seq user-info)
                    (jwt/user-token user-info))]
    (when (seq new-token)
      {:user-info (assoc user-info :token new-token)
       :token new-token
       :expires-in config/access-token-lifetime})))

(defn- admin-token [auth-config {:keys [username password]}]
  (let [{:keys [login-valid role]} (ldap-client/login-valid? auth-config
                                                             username
                                                             password)
        user-info (when login-valid
                    (attrs/username->user-info username role))]
    (jwt/admin-token user-info)))

(defn- list-roles [{{name-key :displayname} :group-config
                    :as auth-config}]
  (when-let [ldap-con (ldap-util/ldap-open-connection auth-config)]
    (try
      (let [all-groups (ldap-util/groups-map ldap-con auth-config)]
        (reduce (fn [acc [_ group]]
                  (conj acc (get group name-key)))
                []
                all-groups))
      (catch Exception e
        (error e "Error while fetching available roles.")
        [])
      (finally
        (ldap-util/ldap-close-connection ldap-con)))))

(deftype Auth-LDAP [auth-config]
  interface/Authorization
  (login-page [_ login-class login-target response input]
    (login-page login-target login-class response input))
  (login-user-form [_ login-info]
    (login-user auth-config login-info))
  (logout-user [_ req]
    (logout-user req))
  (admin-token [_ req]
    (admin-token auth-config req))
  (list-roles [_]
    (list-roles auth-config))
  (validate-user-token [_ cookies]
    (validate-user-token cookies)))

(defn new-instance [auth-config]
  (->Auth-LDAP auth-config))

(comment
  (def auth-config {:dn-bind "ou=Users,dc=explorama,dc=de",
                    :group-bind "ou=Groups,dc=explorama,dc=de",
                    :group-config {:displayname :cn, :id-key :gidNumber},
                    :group-filter "(objectclass=posixGroup)",
                    :ldap-config {:host "192.168.206.128:389"},
                    :type :ldap,
                    :user-config {:displayname [:givenName :sn], :loginname :uid, :mail :mail},
                    :user-filter "(objectclass=inetOrgPerson)",
                    :user-group-config {:found :user, :key :gidNumber, :ref-key :gidNumber}})

  (login-user auth-config {:username "PAdmin"
                           :password "PAdmin18"})
  (admin-token auth-config {:username "PAdmin"
                            :password "PAdmin18"}))