(ns de.explorama.backend.rights-roles.authorization.adapter-no-credentials
  (:require [de.explorama.backend.abac.jwt :as jwt]
            [de.explorama.backend.rights-roles.authorization.interface :as interface]
            [de.explorama.backend.rights-roles.config :as config]
            [ring.util.response :refer [redirect]]))

(def ^:private dummy-user {:username "admin"
                           :role "admin"
                           :name "Admin"})

(defn- login-page [login-target
                   login-class
                   {:keys [remember-me]}]
  [:div.absolute.center.flex.flex-col.align-items-center.w-320
   {:class login-class}
   [:img.login-logo
    {:alt (str config/system-name " logo")
     :src (interface/login-header-image)}]
   [:div.animation-fade-in.w-full
    [:form.flex.flex-column.gap-8
     {:action "/login"
      :method "post"}
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
     [:input {:type "hidden"
              :name "target"
              :value login-target}]
     [:button.btn-primary.btn-large
      {:type "submit"}
      "Sign in"]]]])

(defn- logout-user []
  (redirect "/"))

(defn- login-user []
  {:valid? true
   :token (jwt/user-token dummy-user)
   :user-info dummy-user
   :expires-in config/access-token-lifetime})

(defn- validate-user-token []
  (let [n-token (jwt/user-token dummy-user)]
    {:user-info (assoc dummy-user
                       :token n-token)
     :token n-token
     :expires-in config/access-token-lifetime}))

(defn- admin-token []
  (jwt/admin-token dummy-user))

(defn- list-roles []
  ["admin"])

(deftype Auth-No-Credentials []
  interface/Authorization
  (login-page [_ login-class login-target _ input]
    (login-page login-target login-class input))
  (login-user-form [_ _]
    (login-user))
  (logout-user [_ _]
    (logout-user))
  (admin-token [_ _]
    (admin-token))
  (list-roles [_]
    (list-roles))
  (validate-user-token [_ _]
    (validate-user-token)))

(defn new-instance []
  (->Auth-No-Credentials))