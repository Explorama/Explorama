(ns de.explorama.frontend.configuration.components.dialog
  (:require [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db reg-event-fx]]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]))

(reg-sub
 ::is-active?
 (fn [db [_ dialog-key]]
   (get-in db (path/dialog-is-active? dialog-key))))

(reg-sub
 ::dialog-tag
 (fn [db [_ dialog-key]]
   (get-in db (path/dialog-tag dialog-key))))

(reg-event-fx
 ::delete-data
 (fn [{db :db} [_ dialog-key tag]]
   (let [{:keys [delete-event overwrite-event]} (get-in db (path/dialog-data dialog-key))
         delete-event (when (vector? delete-event)
                        delete-event)]
     {:dispatch-n [(case tag
                     :delete-layout delete-event
                     :delete-overlayer delete-event
                     :delete-topic delete-event
                     :overwrite-layout overwrite-event
                     :overwrite-overlayer overwrite-event)]})))

(reg-sub
 ::dialog-data
 (fn [db [_ dialog-key]]
   (get-in db (path/dialog-data dialog-key))))

(defn confirm-dialog [dialog-key]
  (let [is-active? @(subscribe [::is-active? dialog-key])
        {:keys [close-fn]} @(subscribe [::dialog-data dialog-key])
        tag @(subscribe [::dialog-tag dialog-key])
        {:keys [overwrite-label
                delete-label
                cancel-label
                confirm-dialog-no
                delete-layout-title
                delete-layout-question
                delete-topic-title
                delete-topic-question
                delete-overlayer-title
                delete-overlayer-question
                overwrite-layout-title
                overwrite-layout-question
                overwrite-overlayer-title
                overwrite-overlayer-question]}
        @(subscribe [::i18n/translate-multi
                     :overwrite-label
                     :delete-label
                     :cancel-label
                     :confirm-dialog-no
                     :delete-layout-title
                     :delete-layout-question
                     :delete-topic-title
                     :delete-topic-question
                     :delete-overlayer-title
                     :delete-overlayer-question
                     :overwrite-layout-title
                     :overwrite-layout-question
                     :overwrite-overlayer-title
                     :overwrite-overlayer-question])
        title (case tag
                :delete-layout delete-layout-title
                :delete-overlayer delete-overlayer-title
                :delete-topic delete-topic-title
                :overwrite-layout overwrite-layout-title
                :overwrite-overlayer overwrite-overlayer-title
                "")
        question (case tag
                   :delete-layout delete-layout-question
                   :delete-overlayer delete-overlayer-question
                   :delete-topic delete-topic-question
                   :overwrite-layout overwrite-layout-question
                   :overwrite-overlayer overwrite-overlayer-question
                   "")
        dialog-type (cond
                      (#{:delete-layout :delete-overlayer :delete-topic} tag) :delete
                      (#{:overwrite-layout :overwrite-overlayer} tag) :overwrite
                      :else nil)
        confirm-dialog-yes (case dialog-type
                             :delete delete-label
                             :overwrite overwrite-label
                             "")
        confirm-dialog-no (case dialog-type
                            :delete cancel-label
                            :overwrite confirm-dialog-no
                            "")
        confirm-icon (case dialog-type
                       :delete :trash
                       nil)]
    [dialog {:title title
             :message question
             :show? (boolean is-active?)
             :type :warning
             :hide-fn (fn []
                        (dispatch [::show-dialog dialog-key false])
                        (when (fn? close-fn)
                          (close-fn)))
             :yes {:label confirm-dialog-yes
                   :type :warning
                   :start-icon confirm-icon
                   :on-click #(do
                                (dispatch [::delete-data dialog-key tag])
                                (dispatch [::show-dialog dialog-key false]))}
             :no {:label (if (#{:delete-layout :delete-overlayer :delete-topic :overwrite-layout :overwrite-overlayer}
                              tag)
                           confirm-dialog-no
                           "")
                  :variant :secondary}}]))

(reg-event-db
 ::set-data
 (fn [db [_ dialog-key data]]
   (assoc-in db (path/dialog-data dialog-key) data)))

(reg-event-db
 ::show-dialog
 (fn [db [_ dialog-key show? tag]]
   (-> db
       (assoc-in (path/dialog-is-active? dialog-key) show?)
       (assoc-in (path/dialog-tag dialog-key) tag))))