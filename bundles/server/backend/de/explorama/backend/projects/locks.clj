(ns de.explorama.backend.projects.locks)

(def ^:private locks-state (atom {}))

(defn locks [] @locks-state)

(defn locked? [project-id]
  (get @locks-state project-id))

(defn lock [project-id client-id user-info]
  (locking locks-state
    (let [lock (get @locks-state project-id)]
      (if lock
        (throw (ex-info "Trying to lock already locked project" {:client-id client-id
                                                                 :project-id project-id}))
        (do (swap! locks-state assoc project-id {:client-id client-id
                                                 :user-info user-info})
            true)))))

(defn unlock [project-id client-id]
  (locking locks-state
    (let [lock (get @locks-state project-id)]
      (if (and lock
               (= (:client-id lock) client-id))
        (do (swap! locks-state dissoc project-id)
            true)
        (throw (ex-info "Trying to unlock not locked project" {:client-id client-id
                                                               :project-id project-id}))))))
