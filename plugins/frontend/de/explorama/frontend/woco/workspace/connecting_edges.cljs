(ns de.explorama.frontend.woco.workspace.connecting-edges
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.path :as path]))

(def ^:private pref-key "show-connecting-edges?")

(re-frame/reg-event-fx
 ::init-connecting-edges-state
 (fn [{db :db}]
   {:db (assoc-in db path/show-connecting-edges?
                  (:status (fi/call-api [:user-preferences :preference-db-get]
                                        db pref-key config/enable-connecting-edges?)))}))

;TODO r1/window-handling remove this and the associated path in favor of using the preferences api.
(re-frame/reg-sub
 ::show-connecting-edges?
 (fn [db]
   (get-in db path/show-connecting-edges?)))

(re-frame/reg-event-fx
 ::toggle-show-connecting-edges
 (fn [{db :db}]
   (let [new-show-status (not (get-in db path/show-connecting-edges?))]
     {:db (assoc-in db path/show-connecting-edges? new-show-status)
      :fx [[:dispatch (fi/call-api [:user-preferences :save-event-vec]
                                   pref-key
                                   {:status new-show-status})]
           [:dispatch [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]]})))

(defn toggle []
  (let [workspace-connecting-edges @(re-frame/subscribe [::i18n/translate :workspace-connecting-edges])
        active? @(re-frame/subscribe [::show-connecting-edges?])]
    {:id "toggle-connections"
     :title workspace-connecting-edges
     :icon :window-link
     :on-click #(re-frame/dispatch [::toggle-show-connecting-edges])
     :active? (boolean active?)}))

(re-frame/reg-event-fx
 ::init
 (fn [_ _]
   {:dispatch (fi/call-api [:user-preferences :add-preference-watcher-event-vec]
                           pref-key
                           [::init-connecting-edges-state]
                           config/enable-connecting-edges?)}))
