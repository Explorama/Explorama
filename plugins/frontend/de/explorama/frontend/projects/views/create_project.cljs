(ns de.explorama.frontend.projects.views.create-project
  (:require [clojure.string :as string]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.projects.config :as config]
            [de.explorama.frontend.projects.path :as path]
            [de.explorama.frontend.projects.views.product-tour :refer [product-tour-step]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field textarea]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-id]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get db ::is-active? false)))

(re-frame/reg-event-fx
 ::show-dialog
 (fn [{db :db} [_ show?]]
   {:db (assoc db ::is-active? show?)
    :dispatch [:de.explorama.frontend.projects.views.overview/close-overview]}))

(re-frame/reg-event-db
 ::title-changed
 (fn [db [_ val]]
   (assoc-in db path/new-project-title val)))

(re-frame/reg-sub
 ::title
 (fn [db [_]]
   (get-in db path/new-project-title "")))

(re-frame/reg-event-db
 ::description-changed
 (fn [db [_ val]]
   (assoc-in db path/new-project-desc val)))

(re-frame/reg-event-db
 ::cancel-create
 (fn [db]
   (update db path/root dissoc path/new-project-key)))

(re-frame/reg-event-fx
 ::create
 (fn [{db :db} _]
   (let [user-info (fi/call-api :user-info-db-get db)
         workspace-id (fi/call-api :workspace-id-db-get db)
         project-infos (assoc (get-in db path/new-project)
                              :project-id workspace-id)]
     {:db (-> db
              (update path/root dissoc path/new-project-key)
              (update-in path/created-project-titles conj (:title project-infos)))
      :backend-tube [ws-api/create-project-route
                     {:client-callback [ws-api/create-project-result]}
                     project-infos user-info]})))

(re-frame/reg-event-fx
 ws-api/create-project-result
 (fn [_ [_ {:keys [project-id]}]]
   {:dispatch-n [[::show-dialog false]
                 [:de.explorama.frontend.projects.core/add-title-to-woco]
                 [ws-api/request-projects-route]
                 [:de.explorama.frontend.projects.core/project-loading-done {:project-id project-id}]]
    :backend-tube [ws-api/load-project-after-create-route
                   {:client-callback [ws-api/load-project-after-create-result]}
                   project-id]}))

(re-frame/reg-sub
 ::created-project-names
 (fn [db _]
   (get-in db path/created-project-titles #{})))

(re-frame/reg-sub
 ::project-already-exists?
 (fn [_]
   (re-frame/subscribe [::created-project-names]))
 (fn [project-names [_ title]]
   (if (set? project-names)
     (boolean (project-names title))
     false)))

(defn- product-tour-comp [id]
  (let [parent-bounding-rect (r/atom nil)]
    (r/create-class
     {:component-did-mount #(reset! parent-bounding-rect (bounding-rect-id id))
      :component-did-update #(reset! parent-bounding-rect (bounding-rect-id id))
      :reagent-render (fn [_]
                        (let [_ @(fi/call-api :scale-info-sub)]
                          [product-tour-step {:component :projects
                                              :additional-info :save-project
                                              :parent-bounding-rect parent-bounding-rect
                                              :offset-top -225
                                              :offset-left 15}]))})))

(defn create-panel []
  (let [title-input (r/atom "")]
    (fn []
      (let [is-active? @(re-frame/subscribe [::is-active?])
            create-project-new-project @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-new-project])
            create-project-dialog-title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-dialog-title])
            create-project-title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-title])
            create-project-title-placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-title-placeholder])
            create-project-create-button @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-create-button])
            create-project-cancel-button @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-cancel-button])
            info-at-least-chars @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :info-at-least-chars])
            info-at-least-chars (format info-at-least-chars 4)
            warning-project-title-already-exists @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :warning-project-title-already-exists])
            title @(re-frame/subscribe [::title])
            project-already-exists? @(re-frame/subscribe [::project-already-exists? title])
            title-chars-condition (>= (count title) config/min-project-title-length)
            enable-button (and (not (string/blank? title))
                               (not project-already-exists?)
                               title-chars-condition)
            form-error (when (or (not title-chars-condition)
                                 project-already-exists?)
                         "explorama__form--error")
            product-tour? (seq @(fi/call-api [:product-tour :current-sub] :product-tour-current-step-sub))
            caption (cond
                      project-already-exists? warning-project-title-already-exists
                      (not title-chars-condition) info-at-least-chars)
            id "create-project-dialog"
            create-and-close (fn []
                               (reset! title-input "")
                               (re-frame/dispatch [::show-dialog false])
                               (re-frame/dispatch [::create])
                               (re-frame/dispatch (fi/call-api [:product-tour :next-event-vec]
                                                               :projects
                                                               :save-project)))]
        [dialog {:title create-project-new-project
                 :id id
                 :type :prompt
                 :message
                 [:div.explorama__form__input.explorama__form--info
                  {:class form-error}
                  [product-tour-comp id]
                  [input-field {:label create-project-title
                                :caption caption
                                :invalid? (boolean caption)
                                :value title-input
                                :autofocus? true
                                :max-length config/max-project-title-length
                                :placeholder create-project-title-placeholder
                                :on-change #(do
                                              (reset! title-input %)
                                              (re-frame/dispatch [::title-changed (string/trim %)]))
                                :on-clear #(do
                                             (reset! title-input "")
                                             (re-frame/dispatch [::title-changed ""]))
                                :on-key-press (fn [ev]
                                                (when (and enable-button
                                                           (= 13 (aget ev "which")))
                                                  (create-and-close)))}]
                  [textarea {:label (re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :create-project-description])
                             :extra-class "input--w18"
                             :name "input-text"
                             :max-length config/max-project-desc-length
                             :on-key-press (fn [ev]
                                             (when (and enable-button
                                                        (aget ev "ctrlKey")
                                                        (= 13 (aget ev "which")))
                                               (create-and-close)))
                             :on-change #(re-frame/dispatch [::description-changed %])}]]

                 :show? is-active?
                 :hide-fn #(do
                             (reset! title-input "")
                             (re-frame/dispatch [::show-dialog false]))
                 :cancel {:label create-project-cancel-button
                          :variant :secondary
                          :disabled? (boolean product-tour?)
                          :on-click #(re-frame/dispatch [::cancel-create])}
                 :ok {:label create-project-create-button
                      :disabled? (not enable-button)
                      :on-click #(do (re-frame/dispatch [::create])
                                     (re-frame/dispatch (fi/call-api [:product-tour :next-event-vec]
                                                                     :projects
                                                                     :save-project)))}}]))))