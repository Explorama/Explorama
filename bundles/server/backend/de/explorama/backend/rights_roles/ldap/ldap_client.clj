(ns de.explorama.backend.rights-roles.ldap.ldap-client
  (:require [de.explorama.shared.abac.util :as abac-util]
            [clj-ldap.client :as ldap]
            [clojure.string :as s]
            [de.explorama.backend.rights-roles.ldap.ldap-util :as util]))

(defn- filter-users [ldap-server-con {:keys [dn-bind]} user-filter]
  (ldap/search ldap-server-con dn-bind {:filter user-filter}))

(defn- user-dn
  [ldap-server-con {:keys [user-config user-filter] :as auth-config} username]
  (let [username (abac-util/normalize-username username)
        uid-name (name (get user-config
                            :loginname
                            :uid))
        ldap-user-filter (format "(&%s(%s=%s))"
                                 user-filter
                                 uid-name
                                 username)
        ldap-user (first (filter-users ldap-server-con auth-config ldap-user-filter))]
    ldap-user))

(defn login-valid?
  [{:keys [user-group-config group-config] :as auth-config}
   username password]
  (if-let [ldap-server-con (util/ldap-open-connection auth-config)]
    (let [all-groups (util/groups-map ldap-server-con auth-config)
          {user-dn-result :dn
           :as user} (when-not (s/blank? username)
                       (user-dn ldap-server-con auth-config username))
          role (util/user-role user all-groups
                               user-group-config group-config)
          result
          (if (and (not (s/blank? password))
                   user-dn-result
                   (ldap/bind? ldap-server-con
                               user-dn-result
                               password))
            {:login-valid true
             :role role}
            {:login-valid false
             :ldap-available true
             :message "Wrong username/password."})]
      (util/ldap-close-connection ldap-server-con)
      result)
    {:login-valid false
     :ldap-available false
     :message "LDAP Server not available."}))

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

  (login-valid? auth-config
                "PAdmin"
                "PAdmin18"))