(ns de.explorama.backend.rights-roles.authorization.api
  (:require [de.explorama.backend.rights-roles.authorization.adapter-keycloak :as keycloak]
            [de.explorama.backend.rights-roles.authorization.adapter-ldap :as ldap]
            [de.explorama.backend.rights-roles.authorization.adapter-no-credentials :as no-cred]
            [de.explorama.backend.rights-roles.authorization.interface :as interface]
            [taoensso.timbre :refer [warn]]))

(defonce ^:private instance (atom nil))

(defn init-instance [{:keys [type] :as auth-config}]
  (reset! instance
          (case type
            :ldap (ldap/new-instance auth-config)
            :keycloak (keycloak/new-instance auth-config)
            (do
              (warn "No credential mode configured." auth-config)
              (no-cred/new-instance)))))

(defn login-user-form [login-infos]
  (interface/login-user-form @instance login-infos))

(defn logout-user [request]
  (interface/logout-user @instance request))

(defn validate-user-token [cookies]
  (interface/validate-user-token @instance cookies))

(defn admin-token [request]
  (interface/admin-token @instance request))

(defn login-page [login-class login-target response input]
  (interface/login-page @instance login-class login-target response input))

(defn list-roles []
  (interface/list-roles @instance))

(comment
  (init-instance {:type :keycloak
                  :base-url "http://localhost:8080"
                  :realm "explorama"
                  :client-id "explorama"}))
