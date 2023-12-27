(ns de.explorama.backend.projects.event-log
  (:require [de.explorama.backend.frontend-api :as fapi]
            [de.explorama.backend.projects.core :as pcore]
            [de.explorama.backend.projects.persistence.event-log :as persist]
            [de.explorama.backend.projects.persistence.project :as project-backend]
            [de.explorama.shared.common.unification.time :refer [current-ms]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [taoensso.timbre :refer [error]]))

(defonce counter (atom 0))

(defn eventlog [{:keys [user-info]}
                {:keys [frame-id event-id event-name origin version]
                 desc :description
                 :as event-desc}]
  (let [project-id (:workspace-id frame-id)
        counter (swap! counter (fn [val]
                                 (mod (inc val)
                                      100000)))
        event-id (if event-id
                   event-id
                   {:c counter
                    :t (current-ms)})]
    (when (pcore/user-allowed? user-info project-id)
      (try
        (let [instance (persist/new-instance project-id)
              project-instance (project-backend/new-instance project-id)
              new-pdesc (-> (project-backend/read project-instance)
                            (assoc :last-modified (current-ms)))]
          (try
            (persist/append-lines instance [[event-id [origin frame-id event-name desc version]]])
            (project-backend/update project-instance new-pdesc)
            ;;TODO r1/projects whats this?
            ;; (when (and inform-after-log client-id)
              ;;   (after-log-inform client-id project-id))
              ;; (when (and sync? client-id (not not-broadcast?))
              ;;   (broadcast-log client-id project-id event)))
            (fapi/dispatch
             ;(partial de.explorama.backend.projects.session/to-client? client-id)
             [ws-api/request-projects-route])
            (catch #?(:clj Throwable :cljs :default) e
              (error e "Could not save event:" event-desc))))
              ;; (swap! event-queue conj (assoc event :event-id event-id)))))
        (catch #?(:clj Throwable :cljs :default) e
          (error e "Could not save event:" event-desc))))))

; TODO r1/projects make this a queue again
;; (defn worker [event-id {:keys [client-id frame-id event-name origin user-info project-id inform-after-log version
;;                                not-broadcast?]
;;                         desc :description
;;                         :as event}]
;;   (fn [

;;               (catch Exception e
;;                 (error e "Could not save event:" event)
;;                 (swap! event-queue conj (assoc event :event-id event-id)))
;;           (catch Exception e
;;             (error e "Could not save event:" event))]))

;; (defn spawn-worker [pool id event]
;;   (.execute pool (worker id event)))
