(ns de.explorama.frontend.indicator.components.dialog
  (:require [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [select]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.indicator.path :as ip]
            [de.explorama.frontend.indicator.views.management :as management]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [reagent.core :as r]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::is-active?
 (fn [db [_ dialog-key]]
   (get-in db (ip/dialog-is-active? dialog-key))))

(re-frame/reg-sub
 ::dialog-tag
 (fn [db [_ dialog-key]]
   (get-in db (ip/dialog-tag dialog-key))))

(re-frame/reg-sub
 ::dialog-data
 (fn [db [_ dialog-key]]
   (get-in db (ip/dialog-data dialog-key))))

(re-frame/reg-event-db
 ::set-data
 (fn [db [_ dialog-key data]]
   (assoc-in db (ip/dialog-data dialog-key) data)))

(re-frame/reg-event-db
 ::show-dialog
 (fn [db [_ dialog-key show? tag]]
   (-> db
       (assoc-in (ip/dialog-is-active? dialog-key) show?)
       (assoc-in (ip/dialog-tag dialog-key) tag))))

(re-frame/reg-event-db
 ::set-show
 (fn [db [_ dialog-type indicator-id show?]]
   (assoc-in db ip/show? {:dialog-type dialog-type
                          :indicator-id indicator-id
                          :show? show?})))

(re-frame/reg-sub
 ::is-show?
 (fn [db _]
   (get-in db ip/show? false)))

(defn- delete-dialog [id] ; overview
  [dialog
   {:show? ::is-show?
    :type :warning
    :hide-fn #(re-frame/dispatch [::set-show nil nil false])
    :title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-delete-dialog-title-indicator])
    :message @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-delete-dialog-question-indicator])
    :yes {:on-click #(re-frame/dispatch [::management/delete-indicator id])
          :label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :delete-label])
          :start-icon :trash
          :type :warning}
    :no {:label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :cancel-label])
         :variant :secondary}}])

(defn- back-confirm-dialog [id]
  [dialog
   {:show? ::is-show?
    :type :warning
    :hide-fn #(re-frame/dispatch [::set-show nil nil false])
    :title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-back-title])
    :message @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-back-message])
    :yes {:label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :confirm-back-yes])
          :type :warning
          :on-click #(re-frame/dispatch [::management/delete-indicator id])}
    :no {:label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :cancel-label])
         :variant :secondary}}])

(defn- send-copy [user-info selected-users]
  (let [options (vec
                 (sort-by (comp clojure.string/lower-case :label)
                          (remove #(= (:value %)
                                      (:username user-info))
                                  @(fi/call-api :users-sub))))]
    [:div.send__copy
     [select
      {:is-multi? true
       :is-clearable? true
       :on-change #(reset! selected-users %)
       :options options
       :values @selected-users
       :menu-row-height 35
       :extra-class "input--w100"}]]))

(defn- send-copy-dialog [_]
  (let [selected-users (r/atom nil)]
    (r/create-class
     {:display-name "indicator-share-dialog"
      :reagent-render
      (fn [id]
        (let [share-title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :send-copy-label])
              user-info @(fi/call-api :user-info-sub)]
          [dialog
           {:show? ::is-show?
            :hide-fn #(re-frame/dispatch [::set-show nil nil false])
            :title share-title
            :message [send-copy user-info selected-users]
            :yes {:label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :send-label])
                  :on-click #(re-frame/dispatch [::management/send-copy
                                             user-info
                                             @selected-users
                                             id])}
            :cancel {:label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :cancel-label])
                     :variant :secondary}}]))})))

(defn view []
  (let [{dialog-type :dialog-type
         indicator-id :indicator-id
         show? :show?} @(re-frame/subscribe [::is-show?])]
    (when show?
      [:<>
       [:div
        (case dialog-type
          "back-confirm" [back-confirm-dialog indicator-id]
          "send-copy" [send-copy-dialog indicator-id]
          "delete" [delete-dialog indicator-id])]])))