(ns de.explorama.backend.projects.notifications
  (:require [de.explorama.backend.projects.session :refer [broadcast-specific]]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.projects.ws-api :as ws-api]))


(defonce state (atom {}))

(defn init! [])

(defn add-notification [info project-id content]
  (swap! state assoc-in [info project-id (cljc-uuid)] content))

(defn remove-notification
  ([info project-id]
   (swap! state update info dissoc project-id))
  ([info project-id notification-ids]
   (swap! state update-in [info project-id] (fn [v]
                                              (apply dissoc v notification-ids)))))

(defn remove-notifications-for-users
  ([users project-id]
   (remove-notifications-for-users "" users project-id))
  ([group users project-id]
   (doseq [user users]
     (remove-notification [(if (map? user)
                             (:username user)
                             user) group]
                          project-id))))

(defn remove-group-notifications [groups project-id right-users-mapping]
  (doseq [group groups]
    (when-not (= "" group)
      (let [users-for-role (get right-users-mapping group)]
        (remove-notifications-for-users group
                                        users-for-role
                                        project-id)))))

(defn add-notifications-for-users
  ([users project-id content]
   (add-notifications-for-users "" users project-id content))
  ([group users project-id content]
   (doseq [user users]
     (add-notification [(if (map? user)
                          (:username user)
                          user) group]
                       project-id
                       content))))

(defn add-group-notifications [groups project-creator project-id right-users-mapping content]
  (doseq [group groups]
    (when-not (= "" group)
      (let [users-for-role (->> group
                                (get right-users-mapping)
                                (map :username)
                                (filterv #(not= % project-creator)))]
        (when-not (empty? users-for-role)
          (add-notifications-for-users group
                                       users-for-role
                                       project-id
                                       content))))))

(defn get-notifications
  ([]
   @state)
  ([{user-info :username}]
   (into {} (filterv (fn [[key _]]
                       (= (first key)
                          user-info))
                     (get-notifications)))))

(defn broadcast []
  (broadcast-specific
   (let [notifications (get-notifications)]
     (fn [user-name]
       [ws-api/notify-client notifications (get-notifications user-name)]))))

(defn delete-notifications! [{:keys [project-id
                                     allowed-user allowed-groups
                                     read-only-user read-only-groups]}
                             right-users-mapping]
  (remove-group-notifications (into allowed-groups read-only-groups)
                              project-id
                              right-users-mapping)
  (remove-notifications-for-users (into allowed-user read-only-user)
                                  project-id))
