(ns de.explorama.frontend.projects.views.confirm-dialog
  (:require [clojure.string :as string]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.projects.path :as path]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get-in db path/dialog-active false)))

(re-frame/reg-sub
 ::dialog-infos
 (fn [db]
   (get-in db path/dialog-infos)))

(re-frame/reg-event-fx
 ::show-dialog
 (fn [{db :db} [_ show? dialog-infos]]
   {:db (cond-> db
          show? (assoc-in path/dialog-active show?)
          dialog-infos (assoc-in path/dialog-infos dialog-infos)
          (not show?) (update path/root dissoc path/confirm-dialog-root))}))

(re-frame/reg-event-fx
 ::delete-project
 (fn [{db :db} [_ project-infos]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:backend-tube [ws-api/delete-project-route
                     {:client-callback [ws-api/delete-project-result]}
                     project-infos user-info]})))

(re-frame/reg-event-fx
 ws-api/delete-project-result
 (fn [_ [_ result]]
   (debug "Project delete result" result)
   {:dispatch [ws-api/request-projects-route]}))

(defn panel []
  (let [is-active? @(re-frame/subscribe [::is-active?])
        confirm-delete-dialog-title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-delete-dialog-title-project])
        confirm-delete-dialog-question @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-delete-dialog-question-project])
        confirm-dialog-ok @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :ok])
        {:keys [delete-label cancel-label
                confirm-dialog-yes confirm-dialog-no
                warning-read-only-project warning-unauthorized-delete warning-deletable-project-is-shared
                show-dialog-option
                confirm-delete-dialog-infos
                confirm-remove-dialog-title confirm-remove-dialog-question confirm-remove-dialog-infos
                confirm-title-dialog-title confirm-title-dialog-question confirm-title-dialog-infos
                confirm-description-dialog-title confirm-description-dialog-question confirm-description-dialog-infos]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi
                              :delete-label :cancel-label
                              :confirm-dialog-yes :confirm-dialog-no
                              :warning-unauthorized-delete :warning-read-only-project :warning-deletable-project-is-shared
                              :show-dialog-option
                              :confirm-delete-dialog-infos
                              :confirm-remove-dialog-title :confirm-remove-dialog-question :confirm-remove-dialog-infos
                              :confirm-title-dialog-title :confirm-title-dialog-question :confirm-title-dialog-infos
                              :confirm-description-dialog-title :confirm-description-dialog-question :confirm-description-dialog-infos])
        show-confirm-dialog  @(re-frame/subscribe [:de.explorama.frontend.projects.views.project-card/show-confirm-dialog])
        {:keys [project tag shared-with]} @(re-frame/subscribe [:de.explorama.frontend.projects.views.project-card/confirm-dialog])
        {:keys [title question infos confirm-event confirm-label confirm-icon discard-event discard-label dialog-type] :as dialog-infos}
        @(re-frame/subscribe [::dialog-infos])
        [title question infos confirm-event confirm-label discard-label confirm-icon]
        (if dialog-infos
          [title question infos confirm-event confirm-label discard-label confirm-icon]
          (case tag
            "delete" [(str confirm-delete-dialog-title " \"" (:title project) "\"")
                      confirm-delete-dialog-question
                      (str (when (seq shared-with) (str warning-deletable-project-is-shared (string/join ", " shared-with) ".\n"))
                           confirm-delete-dialog-infos)
                      (fn [] (re-frame/dispatch [::delete-project project]))
                      delete-label
                      cancel-label
                      :trash]
            "remove" [(str confirm-remove-dialog-title " \"" (:title project) "\"")
                      confirm-remove-dialog-question
                      confirm-remove-dialog-infos
                      (fn [] (re-frame/dispatch [:de.explorama.frontend.projects.core/show-project-for-overview project false]))
                      nil
                      nil
                      nil]
            "title"  [(str confirm-title-dialog-title " \"" (:title project) "\"")
                      confirm-title-dialog-question
                      confirm-title-dialog-infos
                      nil
                      nil
                      nil
                      nil]
            "description"  [(str confirm-description-dialog-title " \"" (:title project) "\"")
                            confirm-description-dialog-question
                            confirm-description-dialog-infos
                            nil
                            nil
                            nil
                            nil]
            "read-only" [(str confirm-description-dialog-title " \"" (:title project) "\"")
                         nil
                         warning-read-only-project
                         nil
                         nil
                         nil]
            "delete-not-allowed" [(str confirm-delete-dialog-title " \"" (:title project) "\"")
                                  nil
                                  warning-unauthorized-delete
                                  nil
                                  nil
                                  nil]
            "default"))
        dialog-type (cond
                      dialog-type dialog-type
                      (#{"delete"} tag) :warning
                      :else :prompt)]
    [dialog (merge {:title title
                    :type dialog-type
                    :message
                    [:div
                     [:h3 question]
                     [:p (or infos "")]]
                    :show? (and is-active?
                                (or show-confirm-dialog
                                    (= tag "delete")))
                    :hide-fn #(do
                                (when (and dialog-infos discard-event)
                                  (re-frame/dispatch discard-event))
                                (re-frame/dispatch [:de.explorama.frontend.projects.views.project-card/reset-confirm-dialog])
                                (re-frame/dispatch [::show-dialog false]))}
                   (if (#{"title" "description" "read-only" "delete-not-allowed"} tag)
                     {:ok {:label (or confirm-label confirm-dialog-ok)
                           :on-click #(do
                                        (cond
                                          (vector? confirm-event) (re-frame/dispatch confirm-event)
                                          (fn? confirm-event) (confirm-event))
                                        (re-frame/dispatch [::show-dialog false]))}}
                     {:yes (cond-> {:label (or confirm-label confirm-dialog-yes)
                                    :type (if (= :warning dialog-type) :warning :normal)
                                    :on-click #(do
                                                 (cond
                                                   (vector? confirm-event) (re-frame/dispatch confirm-event)
                                                   (fn? confirm-event) (confirm-event))
                                                 (re-frame/dispatch [::show-dialog false]))}
                             confirm-icon (assoc :start-icon confirm-icon))
                      :no {:label (or discard-label confirm-dialog-no)
                           :variant :secondary}}))]))
