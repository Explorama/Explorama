(ns de.explorama.frontend.woco.components.dialog
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [input-field]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.woco.path :as path]))

(re-frame/reg-event-db
 ::open
 (fn [db [_ dialog-desc]]
   (assoc-in db path/generic-dialog-desc (assoc dialog-desc
                                                :show? true))))

(re-frame/reg-event-db
 ::hide
 (fn [db]
   (path/dissoc-in db path/generic-dialog-desc)))

(re-frame/reg-sub
 ::dialog-infos
 (fn [db]
   (get-in db path/generic-dialog-desc)))

(defn view []
  (let [{:keys [show? ok-label cancel-label proceed-event message input-params]}
        @(re-frame/subscribe [::dialog-infos])
        state (when input-params
                (reagent/atom nil))]
    [dialog {:show? (boolean show?)
             :hide-fn #(reset! show? false)
             :ok {:label ok-label :on-click (fn [e]
                                              (re-frame/dispatch (conj proceed-event e @state)))}
             :cancel {:label cancel-label :on-click (fn [e]
                                                      (re-frame/dispatch [::hide]))}
             :message (cond message
                            message
                            input-params
                            [input-field (merge {:on-change #(reset! state %)}
                                                input-params)]
                            :else "")}]))