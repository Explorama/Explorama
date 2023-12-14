(ns de.explorama.frontend.projects.views.warning-dialog
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.projects.path :as ppath]))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get db ::is-active? false)))

(re-frame/reg-sub
 ::loaded-project
 (fn [db _]
   (get-in db [:projects :loaded-project])))

(re-frame/reg-event-fx
 ::show-dialog
 (fn [{db :db} [_ show?]]
   {:db (assoc db ::is-active? show?)}))

(re-frame/reg-event-fx
 ::remove-warning-screen
 (fn [{db :db} [_]]
   {:db (dissoc db ::is-active?)}))

(re-frame/reg-event-fx
 ::handle-clean-workspace
 (fn [{db :db}]
   {:db (assoc-in db [:projects :dont-cleanall-veto] true)
    :fx [[:dispatch (fi/call-api [:interaction-mode :set-pending-normal-event])]
         [:dispatch (fi/call-api :clean-workspace-event-vec
                                 [:de.explorama.frontend.projects.core/clean-workspace-done])]]}))

(re-frame/reg-sub
 ::to-be-loaded-project
 (fn [db _]
   (get-in db ppath/to-be-loaded)))

(re-frame/reg-event-db
 ::remove-to-be-loaded
 (fn [db _]
   (ppath/remove-to-be-loaded db)))

(re-frame/reg-event-fx
 ::handle-load-project-with-warning
 (fn [{db :db} [_ project head force-read-only?]]
   (let [frames (fi/call-api :list-frames-get-db db [:frame/content-type :frame/custom-type])]
     (if (or (> (count frames) 0)
             (ppath/project-id db))
       {:db (assoc-in db ppath/force-read-only? force-read-only?)
        :dispatch [::show-dialog true]}
       {:db (assoc-in db ppath/force-read-only? force-read-only?)
        :dispatch-n [[:de.explorama.frontend.projects.core/start-loading-project project head]
                     [::remove-to-be-loaded]]}))))

(re-frame/reg-event-fx
 ::cancel-load
 (fn [{db :db} _]
   {:db (update db ppath/root
                dissoc ppath/welcome-callback-fx-key)
    :fx [[:dispatch [::remove-warning-screen]]
         [:dispatch [::remove-to-be-loaded]]]}))

(defn warning-screen []
  (let [is-active? @(re-frame/subscribe [::is-active?])
        warning-title @(re-frame/subscribe [::i18n/translate :warning-header-title])
        warning-open-project-message @(re-frame/subscribe [::i18n/translate :warning-open-project-message])
        warning-clean-message @(re-frame/subscribe [::i18n/translate :warning-clean-message])
        loaded-project @(re-frame/subscribe [::loaded-project])
        stop @(re-frame/subscribe [::i18n/translate :stop-warning-dialog])
        proceed @(re-frame/subscribe [::i18n/translate :proceed-warning-dialog])
        proceed-project @(re-frame/subscribe [::i18n/translate :proceed-warning-project-dialog])
        {:keys [project head]} @(re-frame/subscribe [::to-be-loaded-project])]
    [dialog {:title warning-title
             :message
             [:p (if loaded-project
                   warning-open-project-message
                   warning-clean-message)]
             :show? is-active?
             :type (if loaded-project
                     :message
                     :warning)
             :hide-fn #()
             :cancel {:label stop
                      :variant :secondary
                      :on-click #(re-frame/dispatch [::cancel-load])}
             :ok {:label (if loaded-project
                           proceed-project
                           proceed)
                  :type (if loaded-project
                          :normal
                          :warning)
                  :on-click  #(do
                                (re-frame/dispatch [::handle-clean-workspace])
                                (re-frame/dispatch [::remove-warning-screen])
                                (when project
                                  (re-frame/dispatch [:de.explorama.frontend.projects.core/start-loading-project project head])
                                  (re-frame/dispatch [::remove-to-be-loaded])))}}]))