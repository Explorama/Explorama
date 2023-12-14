(ns de.explorama.profiling-tool.verticals.projects
  (:require [de.explorama.backend.projects.middleware :refer [log-event]]
            [de.explorama.backend.projects.persistence.core :as store]
            [de.explorama.profiling-tool.benchmark :refer [bench bench-report
                                                           benchmark-all
                                                           report->save]]
            [de.explorama.profiling-tool.env :refer [create-ws-with-user
                                                     wait-for-result]]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [taoensso.tufte :as tufte]))

(def ^:private client-id (str "projects-benchmarking-client-" (cljc-uuid)))

(defn- project-line->log-event-body [event-log user-info new-workspace-id]
  (let [[_ [origin frame-id event-name description]] event-log]
    {:frame-id    (assoc frame-id :workspace-id new-workspace-id)
     :event-name  event-name
     :origin      origin
     :description description
     :version 1
     :user-info   user-info
     :client-id client-id}))

(defn- load-project-steps [file-name user-info new-workspace-id]
  (let [loaded-steps (store/read-lines-force
                      (store/new-instance file-name))]
    (mapv (fn [event-log]
            (project-line->log-event-body event-log user-info new-workspace-id))
          loaded-steps)))

(defn- log-event [log-event-body]
  (tufte/p
   :log-event
   (log-event log-event-body)))

(defn- create-project-request [{:keys [result-atom send-fn user-info]}
                               project-infos]
  (tufte/p
   :create-project
   (send-fn [ws-api/create-project-route
             project-infos user-info])
   (let [[_ created-project-infos] (wait-for-result result-atom)]
     created-project-infos)))

(defn- load-project-list [{:keys [result-atom send-fn user-info]}]
  (tufte/p
   :request-projects
   (send-fn [ws-api/request-projects-route user-info])
   (let [[eventname1 result1] (wait-for-result result-atom)
         [_ result2] (wait-for-result result-atom)]
     (if (= eventname1 :projects.views.overview/update-projects)
       result1
       result2))))

(defn- load-project [{:keys [result-atom send-fn]}
                     project-infos]
  (tufte/p
   :load-project-infos
   (send-fn [ws-api/load-project-infos-route project-infos])
   (let [_ (wait-for-result result-atom)
         [_ loaded-project-infos] (wait-for-result result-atom)]
     loaded-project-infos)))

(defn- project-unload [{:keys [result-atom send-fn]}
                       project-id]
  (tufte/p
   :project-unload
   (send-fn [ws-api/unloaded-project-route
             nil project-id])
   (let [[_ new-locks] (wait-for-result result-atom)]
     new-locks)))

#_{:workspace-id <id>
   :project-id <id>}
(defn- based-events [{:keys [result-atom send-fn user-info]}
                     plogs-id]
  (tufte/p
   :based-events
   (send-fn [ws-api/based-events-route
             plogs-id user-info])
   (let [[_ events] (wait-for-result result-atom)]
     events)))

(defn- delete-project [{:keys [result-atom send-fn user-info]}
                       project-infos]
  (tufte/p
   :delete-project
   (send-fn [ws-api/delete-project-route
             project-infos user-info])
   (let [notification-broadcast (wait-for-result result-atom)
         reload-project-broadcast (wait-for-result result-atom)]
     [notification-broadcast
      reload-project-broadcast])))

(defn- run-project-bench [ws-con log-requests-body project-info new-workspace-id]
  (tufte/p
   :log-all-events
   (doseq [request-body log-requests-body]
     (log-event request-body)))

  (based-events ws-con
                {:workspace-id new-workspace-id})

  (create-project-request ws-con project-info)

  (let [project-infos (get-in (load-project-list ws-con)
                              [:created-projects new-workspace-id])]
    (load-project ws-con project-infos)
    (project-unload ws-con new-workspace-id)
    (delete-project ws-con project-infos)))

(defn benchmark-project []
  (let [bench-project-100 "Log 100 Events, Create Project, Load, Unload, Delete"
        bench-project-500 "Log 500 Events, Create Project, Load, Unload, Delete"
        bench-project-1000 "Log 1000 Events, Create Project, Load, Unload, Delete"

        {:keys [user-info]
         :as ws-con} (create-ws-with-user "projects" (atom nil))

        project-100-steps-id (str (cljc-uuid))
        project-100-project-infos {:title "benchmark project 1",
                                   :description "100 Steps benchmark project",
                                   :project-id project-100-steps-id}
        log-events-requests-100 (load-project-steps "projects/100steps.edn"
                                                    user-info
                                                    project-100-steps-id)

        project-500-steps-id (str (cljc-uuid))
        project-500-project-infos {:title "benchmark project 1",
                                   :description "500 Steps benchmark project",
                                   :project-id project-500-steps-id}
        log-events-requests-500 (load-project-steps "projects/500steps.edn"
                                                    user-info
                                                    project-500-steps-id)

        project-1000-steps-id (str (cljc-uuid))
        project-1000-project-infos {:title "benchmark project 3",
                                    :description "1000 Steps benchmark project",
                                    :project-id project-1000-steps-id}
        log-events-requests-1000 (load-project-steps "projects/1000steps.edn"
                                                     user-info
                                                     project-1000-steps-id)]

    (bench bench-project-100
           []
           (partial run-project-bench
                    ws-con
                    log-events-requests-100
                    project-100-project-infos
                    project-100-steps-id)
           {:num-of-executions 15})

    (bench bench-project-500
           []
           (partial run-project-bench
                    ws-con
                    log-events-requests-500
                    project-500-project-infos
                    project-500-steps-id)
           {:num-of-executions 15})

    (bench bench-project-1000
           []
           (partial run-project-bench
                    ws-con
                    log-events-requests-1000
                    project-1000-project-infos
                    project-1000-steps-id)
           {:num-of-executions 15})

    (assoc (bench-report)
           :name "projects"
           :service "projects")))

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (report->save (benchmark-project))
     (report->save (benchmark-all benchmark-project)))))
