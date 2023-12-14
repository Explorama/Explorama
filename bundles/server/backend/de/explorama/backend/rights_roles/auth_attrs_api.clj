(ns de.explorama.backend.rights-roles.auth-attrs-api
  (:require [de.explorama.backend.rights-roles.attribute-infos.api :as attrs]
            [de.explorama.backend.rights-roles.attribute-infos.persistence.api :as persistence]
            [de.explorama.backend.rights-roles.authorization.api :as auth]
            [taoensso.timbre :refer [debug]]))

(defn init-instance [auth-config attrs-config]
  (debug "Init attribute-infos provider")
  (attrs/init-instance attrs-config)
  (debug "Init authorization provider")
  (auth/init-instance auth-config))

(defn admin-token [req]
  (auth/admin-token req))

(defn logout-user [req]
  (auth/logout-user req))

(defn login-user-form [org-input]
  (auth/login-user-form org-input))

(defn login-page [login-class login-target resp input]
  (auth/login-page login-class login-target resp input))

(defn validate-user-token [cookies]
  (auth/validate-user-token cookies))

(defn list-users []
  (attrs/list-users))

(defn blacklist-roles []
  (persistence/blacklist-attribute-values :role))

(defn list-roles []
  (let [blacklist-roles (blacklist-roles)
        whitelist-roles (persistence/whitelist-attribute-values :role)]
    (->> (auth/list-roles)
         (into whitelist-roles)
         (filterv #(not (blacklist-roles %))))))

(defn possible-user-attribute-vals [& attrs]
  (let [possible-attrs (attrs/possible-user-attribute-vals)
        roles (list-roles)
        all-attrs (assoc possible-attrs
                         :role roles)]
    (if (seq attrs)
      (select-keys all-attrs attrs)
      all-attrs)))

(defn username->user-info
  ([username]
   (attrs/username->user-info username))
  ([username role]
   (attrs/username->user-info username role)))

(defn whitelist-roles [attr-values]
  (attrs/whitelist-roles attr-values))

(defn refresh-loaded-user-infos []
  (attrs/refresh-loaded-user-infos))

(defn blacklist-role [role]
  (attrs/blacklist-role role))

(comment
  (def auth-conf {:type :keycloak
                  :base-url "http://localhost:8080"
                  :realm "explorama"
                  :client-id "explorama"
                  :admin-username "admin"
                  :admin-password "admin"
                  :client-roles-only? false})

  (def l-config {:type :ldap
                 :ldap-config {:host "192.168.206.128:389"}
                 :user-config {:displayname [:givenName :sn]
                               :loginname :uid
                               :mail :mail}
                 :group-config {:id-key :gidNumber
                                :displayname :cn}
                 :user-group-config {:found :user
                                     :key :gidNumber
                                     :ref-key :gidNumber}
                 :dn-bind "ou=Users,dc=explorama,dc=de"
                 :group-bind "ou=Groups,dc=explorama,dc=de"
                 :group-filter "(objectclass=posixGroup)"
                 :user-filter "(objectclass=inetOrgPerson)"})

  (init-instance auth-conf l-config)

  (list-roles)

  (possible-user-attribute-vals))
