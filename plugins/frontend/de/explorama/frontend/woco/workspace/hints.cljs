(ns de.explorama.frontend.woco.workspace.hints
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button card checkbox]]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(def ^:private options [["img/whtour-pan.png"
                         :hint-text-pan
                         :hint-img-alt-pan]
                        ["img/whtour-drag-and-drop.png"
                         :hint-text-drag
                         :hint-img-alt-drag]
                        ["img/whtour-notes.png"
                         :hint-text-notes
                         :hint-img-alt-notes]
                        ["img/whtour-customize.png"
                         :hint-text-custom
                         :hint-img-alt-custom]])

(defn close [old-value checked? show?]
  (when show?
    (reset! show? false))
  (let [new-val (not checked?)]
    (when (not= new-val old-value)
      (re-frame/dispatch (fi/call-api [:user-preferences :save-event-vec]
                                      "show-hints"
                                      new-val)))))

(defn- view-impl [show-hints?]
  (let [state (r/atom 0)
        show? (r/atom true)
        checked? (r/atom show-hints?)
        max-num (dec (count options))]
    (fn [_]
      (when @show?
        [:div.flex.align-items-end
         (let [[img-url label alt-label] (get options @state)]
           [:div.window-handling-tour
            [card {:type :childs}
             [button {:start-icon :close
                      :extra-class "absolute right-12"
                      :aria-label :close
                      :variant :tertiary
                      :on-click (fn []
                                  (close show-hints? @checked? show?))}]

             [:img {:src img-url
                    :alt @(re-frame/subscribe [::i18n/translate alt-label])
                    :width 360 :height 240}]
             [:span.image-hint (str (inc @state) "/" (inc max-num))]
             [:h3 @(re-frame/subscribe [::i18n/translate label])]
             [:div.flex.w-full.justify-between.align-items-center
              [checkbox
               {:checked? @checked?,
                :label @(re-frame/subscribe [::i18n/translate :hint-text-checkbox])
                :on-change (fn [new-state]
                             (reset! checked? new-state))}]
              [:div.flex.gap-8.align-items-center
               (when (< 0 @state)
                 [button {:start-icon :chevron-left,
                          :aria-label :aria-carousel-previous
                          :variant :tertiary
                          :on-click (fn []
                                      (swap! state (fn [num]
                                                     (max 0 (dec num)))))}])
               (if (= @state max-num)
                 [button {:start-icon :close,
                          :label @(re-frame/subscribe [::i18n/translate :hint-text-close])
                          :on-click (fn []
                                      (close show-hints? @checked? show?))}]
                 [button {:start-icon :chevron-right,
                          :aria-label :aria-carousel-next
                          :variant :tertiary
                          :on-click (fn []
                                      (swap! state (fn [num]
                                                     (min max-num (inc num)))))}])]]]])]))))

(defn view []
  (when (and @(fi/call-api [:user-preferences :preferences-loaded-sub])
             (not @(re-frame/subscribe [:de.explorama.frontend.woco.welcome/welcome-active?])))
    (when-let [show-hints? @(fi/call-api [:user-preferences :preference-sub]
                                         "show-hints"
                                         true)]
      [view-impl show-hints?])))