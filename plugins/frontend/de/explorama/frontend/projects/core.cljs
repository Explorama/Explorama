(ns de.explorama.frontend.projects.core
  (:require [clojure.set :as cl-set]
            [clojure.string :as st]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.tubes :as tubes]
            [de.explorama.frontend.projects.config :as config]
            [de.explorama.frontend.projects.direct-search]
            [de.explorama.frontend.projects.event-logging :as event-logging]
            [de.explorama.frontend.projects.monitoring :as monitoring]
            [de.explorama.frontend.projects.mouse-position :as mouse-position]
            [de.explorama.frontend.projects.path :as pp]
            [de.explorama.frontend.projects.project.post-processing :as project-post-processing]
            [de.explorama.frontend.projects.protocol.core :as protocol]
            [de.explorama.frontend.projects.protocol.dialog :as protocol-dialog]
            [de.explorama.frontend.projects.protocol.path :as protocol-path]
            [de.explorama.frontend.projects.re-events] ;; require to have re-frame event handlers in place.
            [de.explorama.frontend.projects.subs :as psubs :refer [project-by-id]] ;; require to have subs in place.
            [de.explorama.frontend.projects.utils.projects :as p-utils]
            [de.explorama.frontend.projects.views.confirm-dialog :as conf-dialog]
            [de.explorama.frontend.projects.views.create-project :as create-project]
            [de.explorama.frontend.projects.views.overview :as overview]
            [de.explorama.frontend.projects.views.project-loading-screen :as loading-screen]
            [de.explorama.frontend.projects.views.share-project :as share-project]
            [de.explorama.frontend.projects.views.warning-dialog :as warning-dialog]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]
            [vimsical.re-frame.cofx.inject :as inject]))

(when (get-in config/configs [:automate-tests :enabled?])
  (defn startTestCase [project-id]
    (re-frame/dispatch [::start-test-case project-id]))

  (re-frame/reg-event-fx
   ::start-test-case
   [(re-frame/inject-cofx ::inject/sub (with-meta [:de.explorama.frontend.projects.views.project-loading-screen/is-active?]
                                         {:ignore-dispose true}))]
   (fn [{loading? :de.explorama.frontend.projects.views.project-loading-screen/is-active?} [_ project-id]]
     (overview/load-project-from-code loading? project-id {:plogs-id {:project-id project-id}}))))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event "projects")
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(defn is-read-only-for-user? [project {:keys [username role]}]
  (let [is-creator? (= username (:creator project))
        in-r-user? (boolean (some #{username} (:read-only-user project)))
        in-r-group? (boolean (some #{role} (:read-only-groups project)))
        in-rw-user? (boolean (some #{username} (:allowed-user project)))
        in-rw-group? (boolean (some #{role} (:allowed-groups project)))]
    {:user-read-only? (or in-r-user? in-r-group?)
     :user-write?     (or is-creator? in-rw-user? in-rw-group?)}))

(defn is-project-user-read-only? [db project user-info]
  (let [{:keys [project-id]} project
        {:keys [user-read-only? user-write?]} (is-read-only-for-user? project user-info)
        is-project? (seq project-id)
        read-only? (and is-project?
                        (or (and user-read-only? (not user-write?))
                            (get-in db pp/force-read-only?)
                            (get-in db protocol-path/step-read-only)
                            (and
                             (not user-write?)
                             (get project :public-read-only? false))))]
    read-only?))

(defn- receive-sync-events? [db]
  (let [user-info (fi/call-api :user-info-db-get db)
        loaded-project-id (get-in db (pp/project-id))
        project (project-by-id db loaded-project-id)
        project-lock (get-in db (conj (pp/locks) loaded-project-id))
        read-only? (is-project-user-read-only? db project user-info)
        users-watching-count (count (get-in db pp/joined-users #{}))]
    (and config-shared/explorama-project-sync?
         (seq loaded-project-id)
         (> users-watching-count 0)
         read-only?
         (boolean project-lock))))

(re-frame/reg-sub
 ::project-unsaved?
 :<- (fi/call-api :statusbar-info-sub-vec)
 (fn [status-info _]
   (-> status-info :unsaved boolean)))

(re-frame/reg-sub
 ::close-project-tooltip
 :<- [::loaded-project-id]
 :<- [:de.explorama.frontend.common.i18n/translate :menusection-snapshots-close-project]
 :<- [:de.explorama.frontend.common.i18n/translate :menusection-snapshots-clean-workspace]
 (fn [[loaded-project-id close-tooltip clean-tooltip] _]
   (if loaded-project-id
     close-tooltip
     clean-tooltip)))

(re-frame/reg-event-fx
 ::init-client
 (fn [_ [_ user-info]]
   {:fx [[:backend-tube [ws-api/create-session-route
                         {}
                         user-info]]]}))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get]
          info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          overlay-register :overlay-register-event-vec
          welcome-interceptor-register :welcome-register-interceptor-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:fx [[:dispatch [::init-client user-info]]
           [:dispatch (service-register :db-get
                                        :project-loading?
                                        psubs/project-loading?)]
           [:dispatch (service-register :db-get
                                        :loaded-project
                                        (fn [db]
                                          (pp/project-id db))
                                        psubs/project-loading?)]
           [:dispatch (service-register :sub-vector
                                        :project-loading?
                                        [::psubs/project-loading?])]
           [:dispatch (service-register :sub-vector
                                        :loaded-project
                                        [::loaded-project-id])]
           [:dispatch (service-register :sub-vector
                                        :receive-sync-events?
                                        [::receive-sync-events?])]
           [:dispatch (service-register :db-get
                                        :receive-sync-events?
                                        receive-sync-events?)]
           [:dispatch (service-register :db-get
                                        :current-project-step
                                        protocol/current-step)]
           [:dispatch (service-register :event-vec
                                        :load-step
                                        [::protocol/start-loading-step])]
           [:dispatch (tools-register {:id "projects-open-projects"
                                       :icon :folder-open
                                       :action-key :projects
                                       :component :projects
                                       :action [::overview/toggle-project-overview]
                                       :notification-sub [::notifications-count]
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :menusection-projects]
                                       :vertical config/default-vertical-str
                                       :tool-group :header
                                       :header-group :left
                                       :sort-order 1})]
           [:dispatch (tools-register {:id "projects-protocol"
                                       :icon :protokoll
                                       :action-key :protocol
                                       :component :projects
                                       :action [::protocol/open-window]
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :menusection-protocol]
                                       :vertical config/default-vertical-protocol-str
                                       :tool-group :header
                                       :header-group :left
                                       :sort-order 2})]
           [:dispatch (tools-register {:id "projects-save"
                                       :action-key :save
                                       :component :projects
                                       :tool-group :project
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :menusection-snapshots-save-project]
                                       :action [::create-project/show-dialog true]
                                       :vertical config/default-vertical-str
                                       :sort-order 100
                                       :icon :save
                                       :enabled-sub [::project-unsaved?]})]
           [:dispatch (tools-register {:id "projects-clear"
                                       :action-key :clear
                                       :component :projects
                                       :tool-group :project
                                       :tooltip-text [::close-project-tooltip]
                                       :action [::handle-clean-workspace-with-warning]
                                       :vertical config/default-vertical-str
                                       :sort-order 300
                                       :icon :close})]
           [:dispatch (tools-register {:id "projects-userlist"
                                       :action-key :userlist
                                       :component :projects
                                       :tool-group :sync-project
                                       :tooltip-text [::userlist-tooltip]
                                       :notification-sub [::users-watching-count true]
                                       :action [::display-userlist]
                                       :visible-sub [::show-sync-tools?]
                                       :enabled-sub [::userlist-enabled-sub]
                                       :vertical config/default-vertical-str
                                       :sort-order 300
                                       :icon :users})]
           [:dispatch (tools-register {:id "projects-cursortoggle"
                                       :action-key :cursortoggle
                                       :component :projects
                                       :tool-group :sync-project
                                       :tooltip-text [::i18n/translate :project-sync-cursortoggle]
                                       :action [::mouse-position/toggle-cursors]
                                       :active-sub [::mouse-position/show-cursors?]
                                       :visible-sub [::show-sync-tools?]
                                       :vertical config/default-vertical-str
                                       :sort-order 300
                                       :icon :user-cursor})]
           [:dispatch (overlay-register :projects-overview overview/project-overview)]
           [:dispatch (overlay-register :projects-mouse-position mouse-position/view)]
           [:dispatch (overlay-register :projects-coulndt-open-protocol protocol-dialog/couldnt-open-panel)]
           [:dispatch (overlay-register :projects-confirm-overview conf-dialog/panel)]
           [:dispatch (overlay-register :projects-project-loading-screen loading-screen/panel)]
           [:dispatch (overlay-register :projects-create-project create-project/create-panel)]
           [:dispatch (overlay-register :projects-share-project share-project/share-panel)]
           [:dispatch (overlay-register :projects-warning-screen warning-dialog/warning-screen)]
           [:dispatch (service-register :event-replay "projects" {:event-replay :de.explorama.frontend.projects.event-logging/replay-events
                                                                  :replay-progress pp/replay-progress})]
           [:dispatch (service-register :project-fns :event-log event-logging/log-event-fn)]
           [:dispatch (service-register :project-fns :event-sync tubes/sync-event-vec)]
           [:dispatch (service-register
                       :clean-workspace
                       ::clean-workspace
                       [::clean-workspace])]
           [:dispatch (service-register
                       :direct-search
                       "projects-projects"
                       [:de.explorama.frontend.projects.direct-search/search])]
           [:dispatch (service-register :modules "projects-protocol-window" protocol/view)]
           [:dispatch (service-register :welcome-section
                                        ::overview/projects
                                        {:order 20
                                         :render-fn overview/welcome-section})]
           [:dispatch (welcome-interceptor-register ::intercept-welcome)]
           [:dispatch (service-register :logout-events :projects-logout [::logout])]
           [:dispatch (init-done "projects")]
           [:dispatch (info "projects arriving!")]]})))

(re-frame/reg-event-fx
 ::handle-clean-workspace-with-warning
 (fn [{db :db} _]
   (let [frames (fi/call-api :list-frames-get-db db [:frame/content-type :frame/custom-type])]
     (if (or (> (count frames) 0)
             (pp/project-id db))
       {:dispatch [:de.explorama.frontend.projects.views.warning-dialog/show-dialog true]}
       {:dispatch [:de.explorama.frontend.projects.views.warning-dialog/handle-clean-workspace]}))))

(re-frame/reg-event-fx
 ::intercept-welcome
 (fn [{db :db} [_ callback-fx]]
   {:db       (assoc-in db pp/welcome-callback-fx callback-fx)
    :dispatch [::handle-clean-workspace-with-warning]}))

(re-frame/reg-sub
 ::active-projects
 (fn [db]
   (apply merge
          (vals
           (get-in db pp/projects)))))

(re-frame/reg-sub
 ::notifications
 (fn [db]
   (get-in db [:projects :notifications] {})))

(re-frame/reg-sub
 ::relevant-notifications
 (fn [_]
   [(fi/call-api :user-info-sub)
    (re-frame/subscribe [::active-projects])
    (re-frame/subscribe [::notifications])])
 (fn [[user-info active-projects notifications]]
   (let [relevant-projects (filterv (fn [[_ {:keys [not-visible-in-overview
                                                    creator]}]]
                                      (not ((set (conj (mapv :username not-visible-in-overview)
                                                       (:username creator)))
                                            (:username user-info))))
                                    active-projects)
         relevent-project-ids (set (mapv first relevant-projects))
         relevent-notifications (filterv (fn [[key _]]
                                           (and (= (:username user-info)
                                                   (first key))))
                                         notifications)
         noti-project-ids (set (mapcat (fn [pro]
                                         (mapv #(first %)
                                               pro))
                                       (vals relevent-notifications)))]

     (vec (cl-set/intersection relevent-project-ids
                               noti-project-ids)))))

(re-frame/reg-sub
 ::notifications-count
 :<- [::relevant-notifications]
 (fn [notifications]
   (if notifications
     (count notifications)
     0)))

(re-frame/reg-event-fx
 ::show-project-for-overview
 (fn [{db :db} [_ project-infos show?]]
   (let [user-info (fi/call-api :user-info-db-get db)
         query (get-in db pp/current-search-query)]
     {:backend-tube [ws-api/show-project-in-overview-route
                     {}
                     project-infos user-info show? query]})))

(re-frame/reg-event-fx
 ::frame-created
 (fn [{db :db} [_ id]]
   (let [follow-event (get-in db [:projects :frame-created-event])]
     {:db       (update db :projects dissoc :frame-created-event)
      :dispatch (conj follow-event id)})))

(re-frame/reg-sub
 ::to-execute-event-count
 (fn [db [_ project-id]]
   (let [execute-events (get-in db [:projects :execute-events project-id])]
     (if execute-events
       (count execute-events)
       -1))))

(re-frame/reg-sub
 ::logs-to-load
 (fn [db [_ project-id]]
   (get-in db [:projects :logs-to-load project-id] -1)))

(re-frame/reg-sub
 ::writable-project?
 (fn [db [_ project-id]]
   (boolean (or (get-in db [:projects :projects :created-projects project-id])
                (get-in db [:projects :projects :allowed-projects project-id])))))

(re-frame/reg-sub
 ::done-loading?
 (fn [db _]
   (or (get-in db (pp/project-id))
       (and (not (get-in db protocol-path/step-loading))
            (get-in db protocol-path/step-loaded-desc)))))

(re-frame/reg-sub
 ::current-project-title
 (fn [db]
   (let [project-id (get-in db (pp/project-id))
         project-title (:title (project-by-id db project-id))
         {:keys [snapshot-name read-only?]} (get-in db pp/loaded-snapshot)]
     (cond-> ""
       :always (str project-title)
       (and snapshot-name read-only?) (str " | Snapshot: " snapshot-name)))))

(defn clean-workspace [db project-id user-info frames follow-event reason]
  (let [step-loading? (get-in db protocol-path/step-loading)
        dont-clean-all-veto (get-in db [:projects :dont-cleanall-veto] false)
        newdb (cond-> (update db
                              :projects
                              dissoc
                              :chat-open?
                              :loaded-project
                              :execute-events
                              :dont-cleanall-veto
                              :create-dialog
                              :snapshot-creating?
                              :show-snap-form
                              :loaded-snapshot
                              :loading-snapshot
                              :snapshot-read-only?
                              :sync
                              protocol-path/protocol-window-root
                              protocol-path/step-read-only-key
                              protocol-path/step-loaded-desc-key
                              pp/snapshot-window-root
                              pp/origins-loaded
                              pp/origins-to-load
                              pp/replay-progress-key
                              pp/replay-progress-paths-key
                              pp/overlayer-active-key)
                (not dont-clean-all-veto) (update :projects dissoc :projects))
        res-map {:db         newdb
                 :dispatch-n (concat [(when follow-event
                                        (conj follow-event ::clean-workspace))
                                      (fi/call-api :statusbar-deregister-vec
                                                   :de.explorama.frontend.projects.core/project)]
                                     (mapv #(fi/call-api :frame-delete-quietly-event-vec %)
                                           frames))}]
    (cond
      (and project-id
           (not step-loading?))
      (assoc res-map :backend-tube [ws-api/unloaded-project-route
                                    {}
                                    user-info project-id])
      :else res-map)))

(re-frame/reg-event-db
 ::dont-cleanall-veto
 (fn [db]
   (assoc-in db [:projects :dont-cleanall-veto] true)))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (let [user-info (fi/call-api :user-info-db-get db)
         frames (concat (fi/call-api :list-frames-vertical-db-get db config/default-vertical-str)
                        (fi/call-api :list-frames-vertical-db-get db config/default-vertical-protocol-str))
         project-id (pp/project-id db)]
     (clean-workspace db project-id user-info frames follow-event reason))))

(re-frame/reg-event-fx
 ::logout
 (fn [{db :db} _]
   (let [user-info (fi/call-api :user-info-db-get db)
         project-id (pp/project-id db)]
     {:fx [[:backend-tube [ws-api/unloaded-project-route
                           {}
                           user-info project-id]]]})))

(re-frame/reg-event-db
 ::dont-care
 (fn [db _]
   db))

(re-frame/reg-event-fx
 ::clean-workspace-done
 (fn [{db :db} _]
   (debug "clean-workspace done")
   (when config-shared/explorama-project-sync?
     (mouse-position/reset-mouse-positions)
     (mouse-position/remove-mouse-handler))
   (let [callback-fx (get-in db pp/welcome-callback-fx)]
     {:db (-> db
              (assoc-in [:projects :cleaned?] true)
              (update :projects dissoc :welcome-callback))
      :fx [[:dispatch [::check-ready-replay]]
           (when callback-fx
             [:dispatch callback-fx])]})))

(re-frame/reg-event-fx
 ws-api/load-project-infos-result
 [monitoring/reset-log]
 (fn [{db :db} [_ project]]
   (let [user-info (fi/call-api :user-info-db-get db)
         {:keys [logs]
          {:keys [project-id] :as project-desc} :description}
         project
         plogs-id {:project-id project-id}
         grouped-logs (p-utils/group-logs-by-origin logs)
         origins (set (keys grouped-logs))
         load-read-only? (get-in project [:snapshot :read-only?])
         read-only? (or load-read-only?
                        (is-project-user-read-only? db project-desc user-info))]
     (when config-shared/explorama-project-sync?
       (mouse-position/add-mouse-handler))
     {:db (-> db
              (assoc-in protocol-path/step-read-only load-read-only?)
              (assoc-in (pp/load-counter plogs-id) 0)
              (assoc-in (pp/execute-events plogs-id) grouped-logs)
              (assoc-in (pp/logs-to-load plogs-id) (count logs))
              (assoc-in (pp/loading-project-id) plogs-id)
              (assoc-in pp/origins-to-load origins))
      :fx [(when-let [counter (get-in project [:snapshot :head])]
             [:dispatch [:de.explorama.frontend.projects.protocol.core/opened-step counter]])
           [:dispatch [::check-ready-replay]]
           [:dispatch (fi/call-api [:interaction-mode :set-no-render-event])]
           [:dispatch [ws-api/request-projects-route]]
           [:dispatch [::add-title-to-woco project-desc]]
           [:dispatch (if read-only?
                        (fi/call-api [:interaction-mode :set-pending-read-only-event])
                        (fi/call-api [:interaction-mode :set-pending-normal-event]))]]})))

(re-frame/reg-event-fx
 ::check-ready-replay
 (fn [{db :db} _]
   (let [plogs-id (get-in db pp/project-loading)
         grouped-logs (get-in db (pp/execute-events plogs-id))
         origins (get-in db pp/origins-to-load)]
     (when (and grouped-logs
                (get-in db [:projects :cleaned?]))
       {:db         (update db :projects dissoc :cleaned?)
        :dispatch-n (if (empty? origins)
                      [(fi/call-api [:interaction-mode :set-render-event]
                                    nil ::check-ready-replay)
                       [::project-loaded plogs-id]]
                      (mapv (fn [[origin events]]
                              [::replay-vertical-events origin plogs-id events])
                            grouped-logs))}))))

(re-frame/reg-event-fx
 ::replay-vertical-events
 monitoring/log-time
 (fn [{db :db}
      [_ event-origin plogs-id events :as event-vec]]
   (let [{:keys [event-replay replay-progress]} (fi/call-api :service-target-db-get db :event-replay event-origin)]
     (if event-replay
       (do
         (debug "dispatching " events " for " event-origin)
         {:db       (update-in db
                               pp/replay-progress-paths
                               #(conj (or % #{}) replay-progress))
          :dispatch [event-replay
                     events
                     [::replay-vertical-done plogs-id event-origin]
                     plogs-id
                     (when (get-in config/configs [:automate-tests :enabled?])
                       {:profiling-start (.now js/Date.)})]})
       (do
         (debug "could not find " event-origin " - retry")
         {:dispatch-later {:ms       400
                           :dispatch event-vec}})))))

(re-frame/reg-event-fx
 ::replay-vertical-done
 (fn [{db :db} [_ {:keys [project-id] :as plogs-id} origin {profiling-start :profiling-start :as profiling-state}]]
   (let [old-status (get-in db pp/origins-to-load)
         n-db (-> db
                  (update-in pp/origins-to-load
                             disj
                             origin)
                  (update-in pp/origins-loaded
                             #(conj (or % #{}) origin)))
         status (get-in n-db pp/origins-to-load)
         all-done? (empty? status)
         ignore? (boolean (empty? old-status))
         user-info (fi/call-api :user-info-db-get db)
         project (project-by-id db project-id)
         read-only? (is-project-user-read-only? db project user-info)]
     (debug "replay-vertical-done" origin plogs-id status "ignored?" ignore?)
     (when (aget js/window "sendHealthPing")
       (.sendHealthPing js/window))
     (when-not ignore?
       (cond-> {:db         n-db
                :dispatch-n [(when all-done?
                               [::project-post-processing/check-and-execute
                                {:project-id plogs-id
                                 :read-only? read-only?
                                 :finish-callback (fi/call-api [:interaction-mode :set-render-event]
                                                               [::project-loading-done plogs-id
                                                                (when profiling-state {["woco" nil "rendering" "{}"] {:start (.now js/Date.)}
                                                                                       :profiling-start profiling-start})]
                                                               ::replay-vertical-done)}])]}

         profiling-state
         (assoc :backend-tube [ws-api/automated-tests-results-route
                               {}
                               (:project-id plogs-id)
                               profiling-start
                               origin
                               profiling-state]))))))

(re-frame/reg-event-fx
 ::project-loading-done
 [monitoring/compute-and-persist-time-deltas
  #_monitoring/print-time-log]
 (fn [{db :db} [_ {:keys [project-id workspace-id] :as plogs-id} {profiling-start :profiling-start
                                                                  :as profiling-state}]]
   (let [user-info (fi/call-api :user-info-db-get db)
         project (project-by-id db project-id)
         read-only? (is-project-user-read-only? db project user-info)]
     (debug "Project - Loaded - " plogs-id read-only?)
     (when (aget js/window "sendHealthPing")
       (.sendHealthPing js/window))
     {:db (-> db
              (assoc-in [:projects :loaded-project] project-id)
              (assoc-in protocol-path/step-loading false)
              (update :projects dissoc :loading-project))
      :fx (conj [[:dispatch (fi/call-api :workspace-id-vec (or project-id workspace-id))]
                 [:dispatch [::protocol/update-based-events]]]
                [:dispatch (if read-only?
                             (fi/call-api [:interaction-mode :set-read-only-event])
                             (fi/call-api [:interaction-mode :set-normal-event]))])

      :backend-tube-n [[ws-api/server-loaded-project-route
                        {}
                        user-info project-id]
                       (when profiling-state
                         [ws-api/automated-tests-results-route
                          {}
                          project-id profiling-start "rendering"
                          (-> (assoc-in profiling-state
                                        [["woco" nil "rendering" "{}"] :end]
                                        (.now js/Date.))
                              (assoc :profiling-end (.now js/Date.)))])]})))

(re-frame/reg-sub
 ::status-message
 :<- [::current-project-title]
 :<- (fi/call-api [:interaction-mode :current-sub-vec])
 :<- [::i18n/translate :status-message-read-only-suffix]
 (fn [[title interaction-mode ro-suffix] _]
   (if (= interaction-mode :read-only)
     (str title " " ro-suffix)
     title)))

(re-frame/reg-event-fx
 ::add-title-to-woco
 (fn [_ [_ _]]
   {:dispatch (fi/call-api :statusbar-register-vec
                           :de.explorama.frontend.projects.core/project
                           {:status-name-sub    [::i18n/translate :status-bar-project]
                            :status-message-sub [::status-message]})}))

(re-frame/reg-event-fx
 ::inform
 (fn [_]
   {:dispatch [:de.explorama.frontend.projects.event-logging/execute-callback]}))

(re-frame/reg-event-fx
 ::fake-progress
 (fn [_ _]
   (js/console.info "FAKE EVENT PROGRESS")
   {}))

(re-frame/reg-event-fx
 ::sync
 (fn [{db :db} [_ {log      :description
                   frame-id :frame-id
                   event-origin :origin
                   event-name :event-name
                   event-version :version
                   :as      log-event}]]
   (let [origins (get-in db pp/origins-loaded)]
     {:fx (if (= event-origin "woco")
            (mapv (fn [origin]
                    (let [event-sync (fi/call-api :service-target-db-get db :event-sync origin)]
                      (when event-sync
                        [:dispatch
                         [event-sync
                          [frame-id event-name log event-version]]])))
                  origins)
            (let [event-sync (fi/call-api :service-target-db-get db :event-sync event-origin)]
              [(when event-sync
                 [:dispatch
                  [event-sync
                   [frame-id event-name log event-version]]])]))})))

(re-frame/reg-event-fx
 ::failure-project-locked
 (fn [_ [_ project-id]]
   (debug "ERROR LOADING PROJECT " project-id " PROJECT ALREADY LOADED")
   {}))

(re-frame/reg-sub
 ::locks
 (fn [db _]
   (pp/locks db)))

(re-frame/reg-event-fx
 ::loading-project-clean-done
 (fn [{db :db} [_ project step]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:backend-tube [ws-api/load-project-infos-route
                     {:client-callback [ws-api/load-project-infos-result]}
                     project
                     {:head-desc step}
                     (is-project-user-read-only? db project user-info)]
      :fx [[:dispatch [::clean-workspace-done]]
           [:dispatch [:de.explorama.frontend.projects.views.project-loading-screen/show-dialog {:project-id (:project-id project)}]]]})))

(re-frame/reg-event-fx
 ::start-loading-project
 (fn [_ [_ project step]]
   {:fx [[:dispatch [:de.explorama.frontend.projects.core/dont-cleanall-veto]]
         [:dispatch (fi/call-api :clean-workspace-event-vec
                                 [::loading-project-clean-done project step])]]}))

(re-frame/reg-sub
 ::receive-sync-events?
 (fn [db _]
   (receive-sync-events? db)))

(re-frame/reg-sub
 ::userlist-tooltip
 (fn [db]
   (let [{:keys [username]} (fi/call-api :user-info-db-get db)]
     (st/join ", "
              (map #(fi/call-api :name-for-user-db-get db %)
                   (conj (sort (get-in db pp/joined-users #{}))
                         username))))))

(re-frame/reg-sub
 ::userlist-enabled-sub
 (fn [_]
   false))

(re-frame/reg-sub
 ::users-watching-count
 (fn [db [_ add-self?]]
   (let [users-count (count (get-in db pp/joined-users #{}))]
     (if add-self?
       (inc users-count)
       users-count))))

(re-frame/reg-sub
 ::show-sync-tools?
 :<- [::users-watching-count]
 (fn [users-counts]
   (> users-counts 0)))

(re-frame/reg-event-fx
 ::display-userlist
 (fn [{db :db}]
   nil))

(re-frame/reg-event-fx
 ::user-watching
 (fn [{db :db} [_ project-id {:keys [username]}]]
   (let [name (fi/call-api :name-for-user-db-get db username)]
     {:db (update-in db pp/joined-users (fnil conj #{}) username)
      :fx [[:dispatch (fi/call-api :notify-event-vec {:type :i
                                                      :vertical :projects
                                                      :category {:misc :project-loaded}
                                                      :message (format (i18n/translate db
                                                                                       :user-joined-project)
                                                                       name)})]]})))

(re-frame/reg-event-fx
 ::user-left
 (fn [{db :db} [_ project-id {:keys [username]}]]
   (let [name (fi/call-api :name-for-user-db-get db username)]
     (mouse-position/remove-mouse-position [nil username])
     {:db (update-in db pp/joined-users disj username)
      :fx [[:dispatch (fi/call-api :notify-event-vec {:type :i
                                                      :vertical :projects
                                                      :category {:misc :project-loaded}
                                                      :message (format (i18n/translate db
                                                                                       :user-left-project)
                                                                       name)})]]})))

(re-frame/reg-event-fx
 ::set-watching-users
 (fn [{db :db} [_ users]]
   {:db (assoc-in db pp/joined-users (set (map :username users)))}))

(defn init []
  (register-init 0))
