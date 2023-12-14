(ns de.explorama.backend.rights-roles.attribute-infos.adapter-ldap
  (:require [de.explorama.backend.abac.util :as abac-util]
            [clojure.string :as str]
            [de.explorama.backend.rights-roles.attribute-infos.interface :as interface]
            [de.explorama.backend.rights-roles.ldap.ldap-util :as ldap-util]
            [taoensso.timbre :refer [error trace]]))

(defn- username->user-info [db username]
  (get-in db [:users username]))

(defn- list-users [db]
  (mapv (fn [[_ user-info]]
          (select-keys user-info interface/list-user-keys))
        (:users db)))

(defn user->user-info [{:keys [loginname displayname]} acc user]
  (let [username (abac-util/normalize-username (get user loginname))
        fullname (str/join " " (map #(get user %) displayname))
        user-info {:username username
                   :name fullname
                   :mail (:mail user)
                   :dn (:dn user)}]
    (update acc :users assoc username user-info)))

(defn- refresh-loaded-user-infos [{:keys [user-config]
                                   :as config}]
  (trace "Refresh user-infos with ldap")
  (when-let [ldap-con (ldap-util/ldap-open-connection config)]
    (try
      (let [all-users (ldap-util/filter-users ldap-con config)]
        (reduce (partial user->user-info user-config)
                {}
                all-users))

      (catch Exception e
        (error e "Error while refreshing cached user-infos.")
        {})
      (finally
        (ldap-util/ldap-close-connection ldap-con)))))

(defn- possible-user-attribute-vals [db attrs]
  (let [all-attrs {:mail (->> (list-users db)
                              (map :mail)
                              (filterv identity))}]
    (if (seq attrs)
      (select-keys all-attrs attrs)
      all-attrs)))

(deftype Attr-LDAP [config
                    ^:unsynchronized-mutable db]
  interface/Attributes
  (refresh-loaded-user-infos [_]
    (set! db (refresh-loaded-user-infos config)))
  (username->user-info [_ username]
    (username->user-info db username))
  (list-users [_]
    (list-users db))
  (possible-user-attribute-vals [_ attrs]
    (possible-user-attribute-vals db attrs)))

(defn new-instance [config]
  (->Attr-LDAP config {}))

(comment
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

  (def temp-db (refresh-loaded-user-infos l-config))

  (list-users temp-db)
  (possible-user-attribute-vals temp-db nil)
  (possible-user-attribute-vals temp-db '(:role)))