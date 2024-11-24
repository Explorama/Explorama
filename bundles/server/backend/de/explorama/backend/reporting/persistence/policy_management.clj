(ns de.explorama.backend.reporting.persistence.policy-management
  (:require [de.explorama.shared.abac.policy-repository :as pr]
            [de.explorama.shared.abac.pep :as pep]
            [taoensso.timbre :refer [warn]]))

(defn- policy-id
  "Suffix: write|use"
  [dr-id suffix]
  (keyword (str dr-id "-" suffix)))

(defn- has-access? [dr-id user prefix]
  (pep/checked-function-call (policy-id dr-id prefix)
                             user
                             {:id dr-id}
                             (fn []
                               true)
                             (fn [failreason]
                               false)))

(defn- get-dr-policies [dr-id]
  (let [use-pol-id (policy-id dr-id "use")
        write-pol-id (policy-id dr-id "write")
        {write-pol write-pol-id
         use-pol use-pol-id} (pr/fetch-multiple-policies [write-pol-id use-pol-id])]
    {:use use-pol
     :write write-pol}))

(defn- dr-policies->policy-map
  "Converts the shallow-policies in the selection into a map.
   Map contains three keys (read, write, use) for the user-attributes."
  [dr-ui-policies user creator public-read?]
  (let [user (if (map? user)
               (:username user)
               user)
        dr-ui-policies (reduce
                        (fn [new-pols [k v]]
                          (assoc new-pols k (map :value v)))
                        {}
                        dr-ui-policies)
        {groups-full-access :groups-full-access
         groups-read-only   :groups-read-only
         user-full-access   :user-full-access
         user-read-only     :user-read-only} dr-ui-policies
        user-full-access (vec (cond-> (set user-full-access)
                                (not (some #{user} user-full-access)) (conj user)
                                (not (some #{creator} user-full-access)) (conj creator)))
        use-pol-group (vec (concat groups-full-access groups-read-only))
        write-pol-group groups-full-access
        use-pol-user (vec (concat user-full-access user-read-only))
        use-pol-user (if public-read?
                       (conj use-pol-user "*")
                       (vec (remove #(= "*" %) use-pol-user)))
        write-pol-user (or user-full-access [])]
    {:use   {:attributes {:username (vec use-pol-user)
                          :role     use-pol-group}
             :optional   [:username :role]}
     :write {:attributes {:username (vec write-pol-user)
                          :role     write-pol-group}
             :optional   [:username :role]}}))


(defn- create-dr-policies! [user creator dr-policies dr-id dr-name public-read?]
  (let [write-pol-id (policy-id dr-id "write")
        use-pol-id   (policy-id dr-id "use")
        {use-users-attrs :use
         write-users-attrs :write} (dr-policies->policy-map dr-policies user creator public-read?)
        base-pol {:data-attributes {:id [dr-id]}
                  :creator creator
                  :group-conf dr-id
                  :group-id dr-id
                  :group-name dr-name}]
    [(pr/create-policy use-pol-id
                       (assoc base-pol
                              :user-attributes use-users-attrs
                              :id use-pol-id
                              :name (str dr-name " read")))
     (pr/create-policy write-pol-id
                       (assoc base-pol
                              :user-attributes write-users-attrs
                              :id write-pol-id
                              :name (str dr-name " write")))]))

(defn write-access? [{:keys [id creator]} {:keys [username] :as user}]
  (or (= creator username)
      (has-access? id user "write")))

(defn read-access? [{:keys [id creator]} {:keys [username] :as user}]
  (or (= creator username)
      (has-access? id user "use")))

(defn policies-user-attributes [dr-id]
  (let [{use-pol   :use
         write-pol :write} (get-dr-policies dr-id)
        groups-full-access (get-in write-pol [:user-attributes :attributes :role])
        groups-read-only (remove #(some #{%} groups-full-access)
                                 (get-in use-pol [:user-attributes :attributes :role]))
        user-full-access (get-in write-pol [:user-attributes :attributes :username])
        user-read-only (remove #(some #{%} user-full-access)
                               (get-in use-pol [:user-attributes :attributes :username]))
        public? (some #(= "*" %)
                      (get-in use-pol [:user-attributes :attributes :username]))]
    {:groups-full-access groups-full-access
     :groups-read-only groups-read-only
     :user-full-access user-full-access
     :user-read-only user-read-only
     :public? public?}))

(defn create-init-dr-policies [creator dr-id dr-name dr-policies public-read?]
  (let [use-pol-id (policy-id dr-id "use")
        write-pol-id (policy-id dr-id "write")
        user-attrs {:attribtues {:username [creator]}
                    :optional [:username]}
        base-pol {:data-attributes {:id [dr-id]}
                  :creator creator
                  :group-conf dr-id
                  :group-id dr-id
                  :group-name dr-name}]
    (create-dr-policies! creator creator dr-policies dr-id dr-name public-read?)))

(defn delete-dr-policies [dr-id]
  (let [use-pol-id (policy-id dr-id "use")
        write-pol-id (policy-id dr-id "write")]
    [(pr/delete-policy use-pol-id)
     (pr/delete-policy write-pol-id)]))

(defn policies-exist? [dr-id]
  (not-empty (pr/fetch-policy (policy-id dr-id "use"))))

(defn update-dr-policies [user creator dr-policies dr-id dr-name public-read?]
  (if (write-access? dr-id user)
    (do (delete-dr-policies dr-id)
        (create-dr-policies! user creator dr-policies dr-id dr-name public-read?))
    (do
      (warn "The user tried to change the policies but doesn't have any rights to do so." {:user user
                                                                                           :id dr-id
                                                                                           :name dr-name})
      nil)))