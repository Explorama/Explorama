(ns de.explorama.frontend.rights-roles.warning-dialog
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.rights-roles.login :as login]
            [de.explorama.frontend.rights-roles.path :as path]))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get-in db path/try-logout?)))

(re-frame/reg-event-fx
 ::remove-warning-screen
 (fn [{db :db} [_]]
   {:db (assoc-in db path/try-logout? false)}))

(re-frame/reg-sub
 ::to-be-loaded-project
 (fn [db _]
   (fi/call-api :project-loading-db-get db)))

(re-frame/reg-event-db
 ::remove-to-be-loaded
 (fn [db _]
   (fi/call-api :project-loading-db-get db)))

(re-frame/reg-event-fx
 ::cancel-load
 (fn [{db :db} _]
   {:db db
    :fx [[:dispatch [::remove-warning-screen]]
         [:dispatch [::remove-to-be-loaded]]]}))

(defn warning-screen []
  (let [is-active? @(re-frame/subscribe [::is-active?])
        warning-title @(re-frame/subscribe [::i18n/translate :warning-header-title])
        warning-open-project-message @(re-frame/subscribe [::i18n/translate :warning-logout-open-project-message])
        warning-clean-message @(re-frame/subscribe [::i18n/translate :warning-logout-message])
        loaded-project @(fi/call-api :loaded-project-sub)
        stop @(re-frame/subscribe [::i18n/translate :stop-warning-dialog])
        logout-label @(re-frame/subscribe [::i18n/translate :log-out-tooltip])]
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
             :ok {:label logout-label
                  :type (if loaded-project
                          :normal
                          :warning)
                  :on-click  #(do
                                (re-frame/dispatch [::login/logout true])
                                (re-frame/dispatch [::remove-warning-screen]))}}]))
