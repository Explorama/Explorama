(ns de.explorama.backend.projects.session
  (:require [de.explorama.backend.frontend-api :as fapi]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Tube-predicates
;;
;; These predicates can be used to identify WebSocket channels (tubes)
;; based upon associated information
;;
;; Can be combined using (every-pred p1 p2 ... )

(defn service-tube? [{type :type}]
  (= type :service-tube))

(defn to-client?
  "Returns true, if the client-id equals the maps
  :client-id, false otherwise."
  [client-id {t-client-id :client-id}]
  (= client-id t-client-id))

(defn- project-id-but-not-client-id?
  "Returns true, if project-id and t-project-id match,
   but client-id and t-client-id differ.

   Used to send messages to all tubes connected to a project
   but the one for a specific client-id."
  [project-id client-id
   {; "t-" prefix, as these are from the tube
    t-project-id :project-id
    t-client-id  :client-id}]
  (and (= project-id t-project-id)
       (not= client-id t-client-id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn after-log-inform [client-id project-id]
  ;; This might not be the optimal solution if we have a high number
  ;; of tubes connected to projects, as it filters the list of tubes
  ;; every time.
  ;;
  ;; Instead, creating a map from client-id > tube(s) might be helpful,
  ;; but that would involve adding new tubes on re-connect and especially
  ;; disposing disconnected tubes. This is prone to memory leaks and should
  ;; be last resort to optimize stuff.
  (fapi/dispatch [:de.explorama.backend.projects.core/inform project-id]))

(defn broadcast-specific [user-specific-event-fn]
  (let [user-info {}]
    (fapi/broadcast (user-specific-event-fn user-info))))

(defn broadcast [event]
  (fapi/broadcast event))

(defn dispatch-to [_ _])

(defn broadcast-log [client-id project-id log])
  ;;TODO r1/projects whats this?
  ;; broadcast to every tube with the same project-id
  ;; but the one with the given client-id
  ;; (fapi/dispatch 
  ;;  (partial project-id-but-not-client-id? project-id client-id)
  ;;  [:de.explorama.backend.projects.core/sync log]))

(defn filter-tube-by-project [client-id project-id
                              {tube-project-id :project-id
                               tubes-client-id :client-id}]
  (and (not= tubes-client-id client-id)
       (= project-id tube-project-id)))

(defn dispatch-user-watching [client-id project-id user-info])
  ;;TODO r1/projects whats this?
  ;; (dispatch-to (partial filter-tube-by-project client-id project-id)
  ;;              [:de.explorama.backend.projects.core/user-watching project-id user-info]))

(defn dispatch-user-left [client-id project-id user-info])
  ;;TODO r1/projects whats this? 
  ;; (dispatch-to (partial filter-tube-by-project client-id project-id)
  ;;              [:de.explorama.backend.projects.core/user-left project-id user-info]))

(defn users-by-project-id [client-id project-id])
  ;;TODO r1/projects whats this?
  ;; (->> (find-tubes (partial filter-tube-by-project client-id project-id))
  ;;      (mapv #(select-keys % [:username]))))

(defn broadcast-event [client-id project-id event-dispatch bypass-re-frame? & params])
  ;;TODO r1/projects whats this?
;; (let [event-dispatch (if (vector? event-dispatch)
  ;;                        event-dispatch
  ;;                        [event-dispatch])]
  ;;   (dispatch-to (partial project-id-but-not-client-id? project-id client-id)
  ;;                (if bypass-re-frame?
  ;;                  [:bypass-re-frame
  ;;                   event-dispatch
  ;;                   params]
  ;;                  event-dispatch))))
