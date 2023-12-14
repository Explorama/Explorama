(ns de.explorama.frontend.woco.presentation.confirmation-dialog
  (:require [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :refer [subscribe reg-sub reg-event-db reg-event-fx dispatch]]))

(reg-sub
 ::dialog-data
 (fn [db]
   (get-in db path/presentation-dialog-data)))

(reg-event-db
 ::hide-dialog
 (fn [db]
   (path/dissoc-in db path/presentation-dialog-data)))

(defn confirmation-dialog []
  (let [{:keys [show? type title message on-success details yes-label no-label]}
        @(subscribe [::dialog-data])]
    [dialog
     (cond-> {:show? (boolean show?)
              :hide-fn #(dispatch [::hide-dialog])
              :title @(subscribe [::i18n/translate title])
              :message @(subscribe [::i18n/translate message])
              :yes (cond-> {:label @(subscribe [::i18n/translate (or yes-label :confirm-dialog-yes)])
                            :on-click #(dispatch on-success)}
                     (= type :warning) (assoc :type :warning :start-icon :trash))
              :no {:label @(subscribe [::i18n/translate (or no-label :confirm-dialog-no)])
                   :variant :secondary}}
       details (assoc :details @(subscribe [::i18n/translate details]))
       type (assoc :type type))]))

(reg-event-db
 ::ask-for-confirmation
 (re-frame.core/path path/presentation-dialog-data)
 (fn [dialog-data [_ on-success-evt title message details]]
   (assoc  dialog-data
           :show? true
           :type :warning
           :yes-label :delete-label
           :no-label :cancel-label
           :title title
           :message message
           :details details
           :on-success on-success-evt)))


