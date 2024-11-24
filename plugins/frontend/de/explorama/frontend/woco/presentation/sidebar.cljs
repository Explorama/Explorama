(ns de.explorama.frontend.woco.presentation.sidebar
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core
             :refer [button input-field]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.timeout :refer [handle-timeout]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.woco.api.overlay :as overlay-api]
            [de.explorama.frontend.woco.presentation.confirmation-dialog :as dialog]
            [de.explorama.frontend.woco.presentation.core :as pres-core]
            [de.explorama.frontend.woco.sidebar]))

(re-frame/reg-event-fx
 ::open-window
 (fn [{db :db} _]
   {:dispatch (fi/call-api :sidebar-create-event-vec
                           {:module "presentation-sidebar-window"
                            :title (i18n/translate db :presentation-mode)
                            :id "presentation"
                            :position :right
                            :close-event [::pres-core/toggle-modes :standard :standard]
                            :width 100
                            :header-items-fn (fn [])})}))

(re-frame/reg-event-fx
 ::hide-window
 (fn [{db :db} [_ prevent-sync?]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (when-not prevent-sync?
       (sync-event-fn [::hide-window true]))
     {:dispatch [:de.explorama.frontend.woco.sidebar/hide-sidebar "presentation"]})))

(defn list-entry [slide index max-index read-only? overlayer-active?]
  (let [editing? (reagent/atom false)
        backup (reagent/atom nil)
        click-timeout (reagent/atom nil)
        reset-state #(do (reset! editing? false)
                         (reset! backup nil))]
    (fn [slide index max-index read-only? overlayer-active?]
      (let [first-slide? (= index 0)
            last-slide? (= index max-index)]
        [:div.card__element.clickable
         [:div.order__controls
          [button {:start-icon :arrow-up
                   :extra-class "order__up"
                   :aria-label :aria-label-slide-up
                   :on-click #(re-frame/dispatch [::pres-core/change-slide-order index (dec index)])
                   :disabled? (or read-only? first-slide?)}
           [icon {:icon :arrow-up}]]
          [button {:start-icon :arrow-down
                   :extra-class "order__down"
                   :aria-label :aria-label-slide-down
                   :on-click #(re-frame/dispatch [::pres-core/change-slide-order index (inc index)])
                   :disabled? (or read-only? last-slide?)}]]
         [:div.card__content {:on-double-click #(when-not read-only?
                                                  (when-let [t @click-timeout]
                                                    (reset! click-timeout nil)
                                                    (js/clearTimeout t))
                                                  (reset! editing? true))
                              :on-click (when-not overlayer-active?
                                          (fn []
                                            (handle-timeout click-timeout
                                                            200
                                                            #(do
                                                               (reset! click-timeout nil)
                                                               (re-frame/dispatch [::pres-core/move-to-slide slide])))))}
          [:div.title__bar
           [:h3
            (if @editing?
              [input-field {:default-value (:name slide)
                            :extra-class "input--w100"
                            :on-change #(do (re-frame/dispatch [::pres-core/update-slide (:uid slide) {:name %}])
                                            (swap! backup (fn [b] (or b (:name slide)))))
                            :aria-label :aria-label-edit-slide-title
                            :autofocus? true
                            :on-key-up  #(case (aget % "key")
                                           "Escape" (do (re-frame/dispatch [::pres-core/update-slide (:uid slide) {:name @backup}])
                                                        (reset-state))
                                           "Enter" (reset-state)
                                           nil)
                            :on-blur reset-state}]
              (:name slide))]
           [button {:start-icon :close
                    :aria-label :aria-label-slide-remove
                    :on-click #(do (.stopPropagation %)
                                   (re-frame/dispatch [::pres-core/remove-slide-by-uid (:uid slide)]))
                    :disabled? read-only?}]]]]))))

(defn slidelist [read-only? overlayer-active?]
  (let [slides @(re-frame/subscribe [::pres-core/slides])
        add-slide @(re-frame/subscribe [::i18n/translate :presentation-add-slide])
        max-index (dec @(re-frame/subscribe [::pres-core/max-slide-sub]))]
    [:div.card__list__ordered
     (map-indexed (fn [idx slide]
                    ^{:key (:uid slide)}
                    [list-entry slide idx max-index read-only? overlayer-active?])
                  slides)
     [:div.card__button.card__element {:on-click (when-not (or read-only? overlayer-active?)
                                                   #(re-frame/dispatch [::pres-core/spawn-new-slide]))
                                       :class (when read-only? "disabled")}
      [icon {:icon :plus}]
      add-slide]]))

(defn sidebar-view [frame-id]
  (let [{:keys [presentation-play-button
                presentation-surround-button
                presentation-remove-all-button]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :presentation-play-button
                              :presentation-surround-button
                              :presentation-remove-all-button])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id
                                  :component :presentation-mode
                                  :additional-info :sidebar-edit})
        no-slides? @(re-frame/subscribe [::pres-core/no-slides?])
        no-frames? (not @(re-frame/subscribe [::pres-core/frames?]))
        overlayer-active? @(re-frame/subscribe [::overlay-api/overlayer-active?])]
    [:<>
     [dialog/confirmation-dialog]
     [:div.content
      [:div.presentation__settings
       [button {:label presentation-play-button
                :on-click #(re-frame/dispatch [::pres-core/start-presentation])
                :size :big
                :start-icon :play
                :disabled? (or no-slides? overlayer-active?)}]
       [:div.title__bar
        [:h2 "Slides"]
        [:div.flex.gap-6
         [button {:label presentation-surround-button
                  :on-click #(re-frame/dispatch [::pres-core/add-slides-to-all-frames])
                  :disabled? (boolean (or read-only? no-frames? overlayer-active?))
                  :variant :secondary}]
         [button {:label presentation-remove-all-button
                  :on-click #(re-frame/dispatch [::dialog/ask-for-confirmation
                                                 [::pres-core/remove-all-slides]
                                                 :title-remove-all-slides
                                                 :message-remove-all-slides])
                  :disabled? (boolean (or read-only? no-slides? overlayer-active?))
                  :variant :secondary}]]]
       [slidelist read-only? overlayer-active?]]]]))

