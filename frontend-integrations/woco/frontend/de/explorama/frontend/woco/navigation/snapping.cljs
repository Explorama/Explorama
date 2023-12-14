(ns de.explorama.frontend.woco.navigation.snapping
  (:require [cljs.reader :refer [read-string]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.components.misc.core :refer [context-menu]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [checkbox]]
            [de.explorama.frontend.common.i18n :as i18n]
            [reagent.core :as r]
            [taoensso.timbre :refer [error]]
            [de.explorama.frontend.woco.config :as config]
            [re-frame.core :as re-frame]))

(def ^:private pref-key "snapping?")
(def ^:private toggle-snapping-state (r/atom config/enable-snapping?))

(re-frame/reg-event-fx
 ::init-snapping-state
 (fn [{db :db}]
   (reset! toggle-snapping-state
           (try
             (let [pref (fi/call-api [:user-preferences :preference-db-get]
                                     db pref-key (str config/enable-snapping?))]
               (cond-> pref
                 (string? pref)
                 (read-string)))
             (catch :default e
               (error "Failed to read snapping-preference" e)
               config/enable-snapping?)))
   nil))

(re-frame/reg-event-fx
 ::init
 (fn [_ _]
   {:dispatch (fi/call-api [:user-preferences :add-preference-watcher-event-vec]
                           pref-key
                           [::init-snapping-state]
                           (str config/enable-snapping?))}))

(defn snapping?
  ([]
   (some true? (vals @toggle-snapping-state)))
  ([snap-type]
   (get @toggle-snapping-state snap-type)))

(defn toggle-snapping [snap-type]
  (swap! toggle-snapping-state update snap-type not)
  (re-frame/dispatch (fi/call-api [:user-preferences :save-event-vec]
                                  pref-key
                                  @toggle-snapping-state)))

(defn snap-menu [pos]
  (let [{:keys [window-snapping grid-snapping]}
        @(re-frame/subscribe [::i18n/translate-multi :window-snapping :grid-snapping])]
    [context-menu
     {:show? true
      :on-close #(reset! pos nil)
      :close-on-select? false
      :position @pos
      :items [{:label [:<>
                       window-snapping
                       [checkbox
                        {:aria-label window-snapping
                         :as-toggle? true
                         :extra-class "ml-auto"
                         :checked? (snapping? :frame)}]]
               :on-click #(toggle-snapping :frame)
               :icon :window}
              {:label [:<>
                       grid-snapping
                       [checkbox
                        {:aria-label grid-snapping
                         :as-toggle? true
                         :extra-class "ml-auto"
                         :checked? (snapping? :grid)}]]
               :on-click #(toggle-snapping :grid)
               :icon :grid-lines}]}]))