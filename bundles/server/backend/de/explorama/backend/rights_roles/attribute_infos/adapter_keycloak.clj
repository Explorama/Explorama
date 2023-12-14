(ns de.explorama.backend.rights-roles.attribute-infos.adapter-keycloak
  (:require [clojure.string :as str]
            [de.explorama.backend.rights-roles.attribute-infos.interface :as interface]
            [de.explorama.backend.rights-roles.http-util :refer [safe-http-get]]
            [de.explorama.backend.rights-roles.keycloak.util :refer [admin-cli-token
                                                                     client-id->id-client client-roles-route
                                                                     client-roles-users group-members-route groups-route realm-roles-users roles-route
                                                                     users-route]]
            [taoensso.timbre :refer [error trace]]))

(def next-token-ms (.toMillis java.util.concurrent.TimeUnit/MINUTES 3))

(defn- request-all-from-stream [attrs-conf req-url]
  (let [current-time (System/currentTimeMillis)]
    (loop [result []
           token (admin-cli-token attrs-conf)
           new-token-at (+ current-time next-token-ms)
           offset 500
           cur-result (safe-http-get req-url
                                     {:query-params {:briefRepresentation false
                                                     :first 0
                                                     :max 500}
                                      :headers {"authorization" (str "bearer " token)}})]
      (let [res (into result cur-result)]
        (if (or (empty? cur-result)
                (< 500 (count cur-result)))
          res
          (recur res
                 (if (>= (System/currentTimeMillis) new-token-at)
                   (admin-cli-token attrs-conf)
                   token)
                 (if (>= (System/currentTimeMillis) new-token-at)
                   (+ (System/currentTimeMillis) next-token-ms)
                   new-token-at)
                 (+ offset 500)
                 (safe-http-get req-url
                                {:query-params {:briefRepresentation false
                                                :first offset
                                                :max 500}
                                 :headers {"authorization" (str "bearer " token)}})))))))

(defn- request-all-users [attrs-conf]
  (request-all-from-stream attrs-conf (users-route attrs-conf)))

(defn- filter-groups [{:keys [client-roles-only? client-id]}
                      all-groups]
  (let [client-id (keyword client-id)
        filter-fn (fn [{:keys [clientRoles realmRoles]}]
                    (let [has-client-roles? (seq (get clientRoles client-id))
                          has-realm-roles (seq realmRoles)]
                      (or (and client-roles-only?
                               has-client-roles?)
                          (and (not client-roles-only?)
                               (or has-client-roles?
                                   has-realm-roles)))))]
    (loop [groups (rest all-groups)
           cur-group (first all-groups)
           result []]
      (let [is-valid? (filter-fn cur-group)
            subgroups (:subGroups cur-group)
            n-groups (vec (into groups subgroups))
            n-result (if is-valid?
                       (conj result (:id cur-group))
                       result)]
        (if (nil? cur-group)
          result
          (recur (rest n-groups)
                 (first n-groups)
                 n-result))))))

(defn- get-group-members [attrs-conf group-id]
  (let [req-url (group-members-route attrs-conf group-id)]
    (request-all-from-stream attrs-conf req-url)))

(defn- get-users-by-group [attrs-conf]
  (let [all-groups (request-all-from-stream attrs-conf (groups-route attrs-conf))
        filtered-groups (filter-groups attrs-conf all-groups)]
    (->> filtered-groups
         (mapcat (partial get-group-members attrs-conf))
         set
         vec)))

(defn- get-realm-role-members [attrs-conf role-name]
  (let [req-url (realm-roles-users attrs-conf role-name)]
    (request-all-from-stream attrs-conf req-url)))

(defn- get-client-role-members [attrs-conf id-client role-name]
  (let [req-url (client-roles-users attrs-conf id-client role-name)]
    (request-all-from-stream attrs-conf req-url)))

(defn- get-users-by-role [id-client {:keys [client-roles-only?] :as attrs-conf}]
  (let [token (admin-cli-token attrs-conf)
        realm-roles (mapv :name
                          (when (not client-roles-only?)
                            (safe-http-get (roles-route attrs-conf)
                                           {:headers {"authorization" (str "bearer " token)}})))

        client-roles (mapv :name
                           (safe-http-get (client-roles-route attrs-conf id-client)
                                          {:headers {"authorization" (str "bearer " token)}}))]
    (-> (mapv (partial get-realm-role-members attrs-conf)
              realm-roles)
        (into (map (partial get-client-role-members attrs-conf id-client))
              client-roles)
        flatten
        set
        vec)))

(defn- retrive-users [id-client {:keys [filter-method] :as attrs-conf}]
  (case filter-method
    :none (request-all-users attrs-conf)
    :group (get-users-by-group attrs-conf)
    :role (get-users-by-role id-client attrs-conf)
    :group-roles (-> (get-users-by-group attrs-conf)
                     (into (get-users-by-role id-client attrs-conf))
                     set
                     vec)
    (do
      (error "Given filter-method not valid" {:given-filter-method filter-method
                                              :valid #{:none :group :role}})
      nil)))

(defn- username->user-info [db username]
  (get-in db [:users username]))

(defn- list-users [db]
  (mapv (fn [[_ user-info]]
          (select-keys user-info interface/list-user-keys))
        (:users db)))

(defn- possible-user-attribute-vals [db attrs]
  (let [all-attrs {:mail (->> (list-users db)
                              (map :mail)
                              (filterv identity))}]
    (if (seq attrs)
      (select-keys all-attrs attrs)
      all-attrs)))

(defn- refresh-loaded-user-infos [id-client
                                  {:keys [displayname] :as config
                                   :or {displayname [:firstName :lastName]}}]
  (trace "Refresh user-infos with keycloak.")
  (let [all-users (retrive-users id-client config)]
    {:users (reduce (fn [acc {:keys [username email] :as user}]
                      (let [user-name (if (vector? displayname)
                                        (str/join " " (map #(% user) displayname))
                                        (get user (keyword displayname)))]
                        (assoc acc username (select-keys (-> user
                                                             (dissoc :email)
                                                             (assoc :mail email)
                                                             (assoc :name user-name))
                                                         interface/list-user-keys))))
                    {}
                    all-users)}))

(deftype Attr-Keycloak [config
                        id-client
                        ^:unsynchronized-mutable db]
  interface/Attributes
  (refresh-loaded-user-infos [_]
    (set! db (refresh-loaded-user-infos id-client config)))
  (username->user-info [_ username]
    (username->user-info db username))
  (list-users [_]
    (list-users db))
  (possible-user-attribute-vals [_ attrs]
    (possible-user-attribute-vals db attrs)))

(defn new-instance [attrs-config]
  (let [id-client (client-id->id-client attrs-config)]
    (->Attr-Keycloak attrs-config
                     id-client
                     {})))

(comment
  (def id-client (client-id->id-client {:type :keycloak
                                        :base-url "http://localhost:8085"
                                        :url-prefix "/auth"
                                        :realm "explorama"
                                        :client-id "explorama"
                                        :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                                        :filter-method :group
                                        :client-roles-only? true}))

  ;;Client roles only
  (def attrs-group {:type :keycloak
                    :base-url "http://localhost:8085"
                    :url-prefix "/auth"
                    :realm "explorama"
                    :client-id "explorama"
                    :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                    :filter-method :group
                    :client-roles-only? true})
  (refresh-loaded-user-infos id-client attrs-group)

  (def attrs-role {:type :keycloak
                   :base-url "http://localhost:8085"
                   :url-prefix "/auth"
                   :realm "explorama"
                   :client-id "explorama"
                   :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                   :filter-method :role
                   :client-roles-only? true})
  (refresh-loaded-user-infos id-client attrs-role)

  (def attrs-both {:type :keycloak
                   :base-url "http://localhost:8085"
                   :url-prefix "/auth"
                   :realm "explorama"
                   :client-id "explorama"
                   :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                   :filter-method :group-roles
                   :client-roles-only? true})
  (refresh-loaded-user-infos id-client attrs-both)

  (def attrs-none {:type :keycloak
                   :base-url "http://localhost:8085"
                   :url-prefix "/auth"
                   :realm "explorama"
                   :client-id "explorama"
                   :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                   :filter-method :none
                   :client-roles-only? true})
  (refresh-loaded-user-infos id-client attrs-none)

  ;;All roles
  (def attrs-group-all {:type :keycloak
                        :base-url "http://localhost:8085"
                        :url-prefix "/auth"
                        :realm "explorama"
                        :client-id "explorama"
                        :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                        :filter-method :group
                        :client-roles-only? false})
  (refresh-loaded-user-infos id-client attrs-group-all)

  (def attrs-role-all {:type :keycloak
                       :base-url "http://localhost:8085"
                       :url-prefix "/auth"
                       :realm "explorama"
                       :client-id "explorama"
                       :client-secret "ZjQRI4hDcup8sh735SebAmWQVHsngK5W"
                       :filter-method :role
                       :client-roles-only? false})
  (refresh-loaded-user-infos id-client attrs-role-all)

  (def attrs-conf {:base-url "http://localhost:8080"
                   :realm "explorama"
                   :client-id "explorama"
                   :client-secret "2lIBsQBG4TKvk0zkEh5nlFgONq3xhX5K"
                   :client-roles-only? true})
  (def id-client-attrs (client-id->id-client attrs-conf))

  (admin-cli-token attrs-conf)

  (retrive-users id-client-attrs attrs-conf)

  (refresh-loaded-user-infos id-client-attrs attrs-conf)

  (possible-user-attribute-vals (refresh-loaded-user-infos id-client-attrs attrs-conf)
                                nil)
  )
