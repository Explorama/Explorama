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

;; ---------------------------------------------------------------------
;; Tailwind migration of styles/src/scss/components/_card.scss's
;; `.card__list__ordered`/`.card__element`/`.order__controls`/`.title__bar`
;; family. No ui_base component renders this markup -- only this file and
;; configuration/views/data_management/geographic_attributes.cljs do
;; (verified via grep across plugins/, bundles/*/frontend) -- duplicated
;; per Task 7's direct-builder precedent (2 sites, well under the ≤5
;; threshold), not hoisted into a new shared ns.
(def ^:private card-list-ordered-class "flex flex-col gap-1.5")

;; Properties `.card__element`'s base rule sets that neither the
;; `.card__list__ordered .card__element` override nor `&.card__button`
;; touch -- these survive unconditionally for both the plain (`.clickable`)
;; and `.card__button` variants below.
(def ^:private card-element-shared-class
  (str "relative flex border border-(--border) rounded-xl overflow-hidden shadow-xs "
       "text-(--text) bg-(--bg) "
       "[transition:background-color_.12s_ease,box-shadow_.12s_ease,color_.12s_ease,transform_.12s_ease]"))

;; `.card__list__ordered .card__element` (flex-row/gap-0/padding-0) always
;; wins over the base `.card__element` rule (higher specificity: 2 classes
;; vs 1) for every real instance -- both are always nested inside
;; `.card__list__ordered` in production, so the base column/gap-4/padding-8
;; variant never actually renders. Translated as the cascade-resolved
;; effective style, not the two competing selectors (same approach as
;; Task 6/7's cascade findings).
(def ^:private card-element-class
  (str card-element-shared-class " flex-row gap-0 p-0"))
(def ^:private card-element-clickable-class "cursor-pointer hover:shadow-sm active:scale-97")

;; `.card__element.card__button` -- its own row/gap/height/padding/
;; justify/align/cursor win over the ordered-list override (declared later
;; in the old sheet, same specificity). No `:not(.disabled)` guard needed:
;; unlike `.btn-card`, `.card__button.disabled` sets `pointer-events:none`,
;; which already blocks `:hover` from ever triggering via the mouse in a
;; browser -- the old sheet relies on that instead of an explicit guard,
;; reproduced identically here.
(def ^:private card-button-class
  (str card-element-shared-class
       " flex-row justify-center items-center gap-1 h-[53px] p-2 cursor-pointer "
       "hover:bg-(--bg-hover) hover:shadow-sm"))
;; `!` on bg/text (not on `pointer-events-none`, which has no competing
;; class): old CSS's `.card__element.card__button.disabled` has real
;; (0,3,0) specificity, unconditionally beating the base rules' `bg-(--bg)`/
;; `text-(--text)` (from `card-element-shared-class`) regardless of source
;; order. Flat Tailwind utility classes of equal specificity are instead
;; tie-broken by Tailwind's own internal generation order -- see card.cljs's
;; `btn-card-disabled-class` comment for the harness-caught version of this
;; same bug.
(def ^:private card-button-disabled-class
  "disabled bg-(--bg-hover)! pointer-events-none text-(--text-disabled)!")
(def ^:private card-button-icon-class "m-0 bg-(--icon) [transition:background-color_.12s_ease]")
(def ^:private card-button-icon-disabled-class
  "m-0 bg-(--icon-disabled) [transition:background-color_.12s_ease]")

(def ^:private card-content-class "flex flex-col justify-center grow gap-1 p-1.5 pl-3 overflow-hidden")

;; The close button rendered inside `.title__bar` inside `.card__content`
;; (`.card__content button` in the old sheet, a descendant selector that
;; also matches through `.title__bar`). The warning tint triggers on BUTTON
;; hover (`.card__content button:enabled:hover span[icon]`), not icon hover
;; -- the button has a padding ring around the icon, so a plain `hover:` on
;; the icon span would silently narrow the trigger region. `group` on the
;; button + `group-hover:` on the icon reproduces the old trigger area
;; (same pattern as `order-controls-icon-class` below). `:enabled` needs no
;; explicit guard: the `button` component sets the real HTML `disabled`
;; attribute, which blocks hover on the whole button.
(def ^:private card-content-button-class "group bg-transparent shadow-none")
(def ^:private card-content-icon-class "bg-(--icon-secondary) group-hover:bg-(--icon-warning)")

(def ^:private order-controls-class "flex flex-col bg-(--bg-hover)")

;; `border-width: 0 size('1') size('1') 0` on top of a `border: size('1')
;; solid var(--border-secondary)` base -- net effect: no top/left border,
;; 1px solid border-secondary on right/bottom.
(def ^:private order-controls-button-class
  (str "group grow p-1.5 pl-2 bg-(--bg) border-t-0 border-r border-b border-l-0 "
       "border-(--border-secondary) rounded-none last:border-b-0 "
       "transition-[background-color] duration-[120ms] ease-[ease] "
       "disabled:bg-(--bg-hover)"))
(def ^:private order-controls-icon-class
  (str "m-0 bg-(--icon) [transition:background-color_.12s_ease] "
       "group-hover:bg-(--primary) group-disabled:bg-(--icon-disabled)"))

(def ^:private title-bar-class "flex flex-row justify-between items-center gap-2")
;; `margin:0!important;padding:0!important;` in the old sheet -- kept
;; `!important` via Tailwind's `!` modifier for exact fidelity, though no
;; current title__bar heading carries a competing margin/padding class.
;; `overflow-hidden text-ellipsis` (not `truncate`): the old rule never set
;; `white-space:nowrap`, so `text-overflow:ellipsis` was already a visual
;; no-op there -- `truncate` would add nowrap and change behaviour.
(def ^:private title-bar-heading-class "m-0! p-0! grow overflow-hidden text-ellipsis")

(defn list-entry [slide index max-index read-only? overlayer-active?]
  (let [editing? (reagent/atom false)
        backup (reagent/atom nil)
        click-timeout (reagent/atom nil)
        reset-state #(do (reset! editing? false)
                         (reset! backup nil))]
    (fn [slide index max-index read-only? overlayer-active?]
      (let [first-slide? (= index 0)
            last-slide? (= index max-index)]
        [:div.card__element.clickable {:class [card-element-class card-element-clickable-class]}
         [:div.order__controls {:class order-controls-class}
          [button {:start-icon :arrow-up
                   :extra-class (str "order__up " order-controls-button-class)
                   :icon-params {:extra-class order-controls-icon-class}
                   :aria-label :aria-label-slide-up
                   :on-click #(re-frame/dispatch [::pres-core/change-slide-order index (dec index)])
                   :disabled? (or read-only? first-slide?)}
           [icon {:icon :arrow-up}]]
          [button {:start-icon :arrow-down
                   :extra-class (str "order__down " order-controls-button-class)
                   :icon-params {:extra-class order-controls-icon-class}
                   :aria-label :aria-label-slide-down
                   :on-click #(re-frame/dispatch [::pres-core/change-slide-order index (inc index)])
                   :disabled? (or read-only? last-slide?)}]]
         [:div.card__content {:class card-content-class
                              :on-double-click #(when-not read-only?
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
          [:div.title__bar {:class title-bar-class}
           [:h3 {:class title-bar-heading-class}
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
                    :extra-class card-content-button-class
                    :icon-params {:extra-class card-content-icon-class}
                    :aria-label :aria-label-slide-remove
                    :on-click #(do (.stopPropagation %)
                                   (re-frame/dispatch [::pres-core/remove-slide-by-uid (:uid slide)]))
                    :disabled? read-only?}]]]]))))

(defn slidelist [read-only? overlayer-active?]
  (let [slides @(re-frame/subscribe [::pres-core/slides])
        add-slide @(re-frame/subscribe [::i18n/translate :presentation-add-slide])
        max-index (dec @(re-frame/subscribe [::pres-core/max-slide-sub]))]
    [:div.card__list__ordered {:class card-list-ordered-class}
     (map-indexed (fn [idx slide]
                    ^{:key (:uid slide)}
                    [list-entry slide idx max-index read-only? overlayer-active?])
                  slides)
     [:div.card__button.card__element {:on-click (when-not (or read-only? overlayer-active?)
                                                   #(re-frame/dispatch [::pres-core/spawn-new-slide]))
                                       :class (cond-> [card-button-class]
                                                read-only? (conj card-button-disabled-class))}
      [icon {:icon :plus :extra-class (if read-only? card-button-icon-disabled-class card-button-icon-class)}]
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
       [:div.title__bar {:class title-bar-class}
        [:h2 {:class title-bar-heading-class} "Slides"]
        [:div.flex {:class "gap-1.5"}
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

