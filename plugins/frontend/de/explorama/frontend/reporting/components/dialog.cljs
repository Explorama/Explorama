(ns de.explorama.frontend.reporting.components.dialog
  (:require [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db reg-event-fx]]
            [de.explorama.frontend.reporting.configs.paths :as cpath]
            [taoensso.timbre :refer-macros [warn]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]))

(reg-sub
 ::is-active?
 (fn [db [_ dialog-key]]
   (get-in db (cpath/config-dialog-is-active? dialog-key))))

(reg-sub
 ::dialog-tag
 (fn [db [_ dialog-key]]
   (get-in db (cpath/config-dialog-tag dialog-key))))

(reg-event-fx
 ::delete-data
 (fn [{db :db} [_ dialog-key]]
   (let [{:keys [success is-new? delete-event] :as dialog-data} (get-in db (cpath/config-dialog-data dialog-key))
         delete-event (when (vector? delete-event)
                        delete-event)]
     (when (and (not is-new?)
                (not delete-event))
       (warn "No delete event defined" {:dialog-key dialog-key
                                        :dialog-data dialog-data}))
     {:dispatch-n [(if is-new?
                     success
                     delete-event)]})))

(reg-sub
 ::dialog-data
 (fn [db [_ dialog-key]]
   (get-in db (cpath/config-dialog-data dialog-key))))

(defn confirm-dialog [dialog-key]
  (let [is-active? @(subscribe [::is-active? dialog-key])
        tag @(subscribe [::dialog-tag dialog-key])
        {:keys [type]} @(subscribe [::dialog-data dialog-key])
        title (case type
                :dashboard @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-dashboard-title])
                :report @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-report-title])
                "")
        question (case [type tag]
                   [:dashboard :delete] @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-dashboard-text])
                   [:dashboard :info] @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-dashboard-info-text])
                   [:report :delete] @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-report-text])
                   [:report :info] @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-report-info-text])
                   "")
        warning? (= :delete tag)
        confirm-dialog-yes @(subscribe [:de.explorama.frontend.common.i18n/translate :delete-label])
        confirm-dialog-no @(subscribe [:de.explorama.frontend.common.i18n/translate :cancel-label])
        confirm-dialog-okay "OK"]
    [dialog (cond-> {:title title
                     :message question
                     :type (if warning? :warning :message)
                     :show? is-active?
                     :hide-fn #(dispatch [::show-dialog dialog-key false])
                     :no {:label (case tag
                                   :delete confirm-dialog-no
                                   (:copy-info :info) confirm-dialog-okay
                                   "")
                          :variant :secondary}}
              warning?
              (assoc :yes {:label confirm-dialog-yes
                           :type :warning
                           :start-icon :trash
                           :on-click #(do
                                        (dispatch [::delete-data dialog-key])
                                        (dispatch [::show-dialog dialog-key false]))}))]))

(reg-event-db
 ::set-data
 (fn [db [_ dialog-key data]]
   (assoc-in db (cpath/config-dialog-data dialog-key) data)))

(reg-event-db
 ::show-dialog
 (fn [db [_ dialog-key show? tag]]
   (-> db
       (assoc-in (cpath/config-dialog-is-active? dialog-key) show?)
       (assoc-in (cpath/config-dialog-tag dialog-key) tag))))