(ns de.explorama.backend.abac.util
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [de.explorama.backend.abac.config :as config-abac]
            [de.explorama.shared.common.config :as config-shared]))

(defn normalize-username [username]
  (if (and config-shared/explorama-normalize-username username)
    (-> username
        str/trim
        str/lower-case)
    username))

(defn create-keyword
  [action-id]
  (if action-id
    (cond
      (keyword? action-id) action-id
      (str/starts-with? action-id ":") (keyword (subs action-id 1))
      :else (keyword action-id))
    action-id))

(defn stringify-keyword
  [action-id]
  (if action-id
    (subs (str action-id)
          1)
    ""))

(def role-mapping-available?
  (not-empty (get config-abac/explorama-roles-mapping-config :role)))

(defn role-lookup [role-name]
  (get-in config-abac/explorama-roles-mapping-config
          [:role role-name]
          role-name))

(defn- update-pol [pol-desc update-path]
  (if (seq (get-in pol-desc update-path))
    (update-in pol-desc
               update-path
               (fn [current-roles]
                 (mapv
                  (fn [role]
                    (role-lookup role))
                  current-roles)))
    pol-desc))

(defn role-lookup-in-policies [data]
  (cond
    (and role-mapping-available?
         (not (nil? (get data :user-attributes)))) ;single policy
    (let [update-path (if (get-in data [:user-attributes :attributes :role])
                        [:user-attributes :attributes :role]
                        [:user-attributes :role])]
      (update-pol data update-path))
    role-mapping-available? ;multiple policies
    (into {}
          (map
           (fn [[pol-k pol-desc]]
             (let [update-path (if (get-in pol-desc [:attributes :role])
                                 [:attributes :role]
                                 [:user-attributes :role])]
               [pol-k (update-pol pol-desc update-path)]))
           data))
    :else data))

(defn user-info-role-fix [use-info]
  (update use-info
          :role
          (fn [role]
            (if (and (string? role) (str/includes? role "["))
              (edn/read-string role)
              role))))