(ns de.explorama.frontend.reporting.plugin.core
  (:require [re-frame.core :as re-frame :refer [reg-event-fx]]
            [de.explorama.frontend.reporting.plugin.sidebar :as sidebar]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.data.dashboards :as dashboards]
            [de.explorama.frontend.reporting.data.reports :as reports]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.tubes :as tubes]
            [de.explorama.frontend.reporting.flags :as reporting-flags]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.paths.discovery-base :as disc-path]
            [de.explorama.frontend.reporting.views.context-menu :as menu]
            [taoensso.timbre :as log :refer [debug error]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event "reporting")
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ [_ user-info]]
   (let [{info :info-event-vec
          tools-register :tools-register-event-vec
          overlay-register :overlay-register-event-vec
          init-done :init-done-event-vec}
         (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id "tool-reporting"
                                       :icon "icon-reporting"
                                       :action-key :reporting
                                       :component :reporting
                                       :action [::reporting-create]
                                       :tooltip-text [::i18n/translate :vertical-label-reporting]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :tool-group :header
                                       :header-group :middle
                                       :sort-order 2})]
           [:dispatch (overlay-register :reporting-context-menu menu/view)]
           [:dispatch [::dashboards/request-dashboards]]
           [:dispatch [::reports/request-reports]]
           [:dispatch (init-done "reporting")]
           [:dispatch (info "reporting arriving!")]]})))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get]
          service-register :service-register-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:fx [[:dispatch [::tubes/init-tube user-info]]
           [:dispatch (service-register :update-user-info-event-vec
                                        ::tubes/update-user-info)]

           [:dispatch (service-register
                       :clean-workspace
                       ::clean-workspace
                       [::clean-workspace])]
           [:dispatch (service-register :modules "reporting-sidebar" sidebar/content)]
           [:dispatch (service-register :provider config/default-namespace reporting-flags/provide-flags)]
           [:dispatch (service-register :logout-events :reporting-logout [::logout])]
           [:dispatch [::arrive user-info]]]})))

(def create-sidebar
  {:id "tool-reporting"
   :width 600
   :module "reporting-sidebar"
   :title [::i18n/translate :vertical-label-reporting]
   :event ::reporting-view-event
   :close-event [::clean-up-services]
   :position :right
   :vertical "reporting"})


(re-frame/reg-event-fx
 ::clean-up-services
 (fn [{db :db} [_ clear-db?]]
   (debug "%% CLEANUP %% REPORTING %%")
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::clean-up-services-sync clear-db?])
     (cond-> {}
       clear-db? (assoc :db (update-in db disc-path/root dissoc dr-path/dashboards-reports-root-key))))))

(re-frame/reg-event-fx
 ::clean-up-services-sync
 (fn [{db :db} [_ clear-db?]]
   (debug "%% CLEANUP %% REPORTING %%")
   (cond-> {:dispatch (fi/call-api :hide-sidebar-event-vec "tool-reporting")}
     clear-db? (assoc :db (update-in db disc-path/root dissoc dr-path/dashboards-reports-root-key)))))

(re-frame/reg-event-fx
 ::reporting-create
 (fn [{db :db} [_ source-frame-id source-connection-id create-position ignore-scroll-position?]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::reporting-create-sync])
     {:dispatch (fi/call-api :sidebar-create-event-vec
                             create-sidebar)})))

(re-frame/reg-event-fx
 ::reporting-create-sync
 (fn [_ _]
   {:dispatch (fi/call-api :sidebar-create-event-vec
                           create-sidebar)}))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db}
      [_ follow-event reason]]
   {:fx [[:dispatch [::clean-up-services true]]
         (when (not= reason :logout)
           [:dispatch [::dashboards/request-dashboards]])
         (when (not= reason :logout)
           [:dispatch [::reports/request-reports]])
         [:dispatch (conj follow-event ::clean-workspace)]]}))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {:fx []}))

(re-frame/reg-event-fx
 ::reporting-view-event
 (fn [{db :db}
      [_ action params]]))

(defn init []
  (register-init 0))