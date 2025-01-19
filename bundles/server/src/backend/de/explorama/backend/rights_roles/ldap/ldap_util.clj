(ns de.explorama.backend.rights-roles.ldap.ldap-util
  (:require [clj-ldap.client :as ldap]
            [taoensso.timbre :refer [error]]))

(defn config-available? [{:keys [ldap-config dn-bind
                                 group-bind user-filter group-filter]}]
  (boolean (and (seq ldap-config)
                (seq dn-bind)
                (seq group-bind)
                (seq user-filter)
                (seq group-filter))))

(defn ldap-accessible [config]
  (config-available? config))

(defn groups-list [ldap-server {:keys [group-bind group-filter]}]
  (ldap/search ldap-server group-bind {:filter group-filter}))

(defn filter-users [ldap-server-con {:keys [user-filter dn-bind]}]
  (ldap/search ldap-server-con dn-bind {:filter user-filter}))

(defn groups-map [ldap-server {:keys [group-config] :as config}]
  (let [group-id-key (get group-config :id-key :gidNumber)]
    (into {} (map #(vector (get % group-id-key) %)
                  (groups-list ldap-server config)))))

(defn user-role [user groups {:keys [found key ref-key]} {group-name :displayname}]
  (cond
    (= :user
       found)
    (let [user-groups (get user key)
          group-filter-func (fn [g]
                              (first (filterv #(= g
                                                  (get % ref-key))
                                              (vals groups))))]
      (if (vector? user-groups)
        (mapv #(get (group-filter-func %)
                    group-name)
              user-groups)
        (get (group-filter-func user-groups)
             group-name)))
    (= :group
       found)
    (let [user-ref-val #{(get user ref-key)}
          user-groups (filterv #(some user-ref-val
                                      (get % key))
                               (vals groups))]
      (mapv #(get % group-name)
            user-groups))
    :else nil))

(defn- try-to-connect [{:keys [ldap-config] :as config}]
  (when (ldap-accessible config)
    (try
      (ldap/connect ldap-config)
      (catch Exception e
        (error e)))))

(defn ldap-open-connection [config]
  (try-to-connect config))

(defn ldap-close-connection [con]
  (ldap/close con))
