(ns de.explorama.frontend.projects.views.project-loading-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.projects.path :as pp]
            [de.explorama.frontend.projects.config :as config]
            [reagent.core :as reagent]))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get db ::is-active? false)))

(re-frame/reg-event-fx
 ::show-dialog
 (fn [{db :db} [_ project-id]]
   {:db (-> db
            (assoc-in (pp/loading-project-id) project-id)
            (assoc ::is-active? true))}))

(re-frame/reg-event-fx
 ::close-dialog
 (fn [{db :db} _]
   {:db (-> db
            (update-in [:projects] dissoc :project-loading-screen)
            (update-in [:projects] dissoc :logs-to-load)
            (dissoc ::is-active?))}))

(def test-close-delay 1000)

(re-frame/reg-event-fx
 ::delayed-close
 (fn [{db :db} _]
   (cond (get-in config/configs [:automate-tests :enabled?])
         {:dispatch-later {:ms test-close-delay
                           :dispatch [::close-dialog]}}
         :else
         {:dispatch [::close-dialog]})))

(re-frame/reg-sub
 ::project-id
 (fn [db _]
   (get-in db (pp/loading-project-id))))

(re-frame/reg-sub
 ::loading-progress
 (fn [db _]
   (let [num-verticals (+ (count (get-in db pp/origins-to-load))
                          (count (get-in db pp/origins-loaded)))
         verticals-progress (map #(get-in db % 0)
                                 (get-in db pp/replay-progress-paths))]
     (when (> num-verticals 0)
       (* (/ (apply + verticals-progress)
             num-verticals)
          100)))))

(defn panel []
  (reagent/create-class
   {:reagent-render
    (fn []
      (let [is-active? @(re-frame/subscribe [::is-active?])
            message @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :project-loading-message])]
        (when (and (not config/disable-project-loadingscreen) is-active?)
          (let [loading-progress @(re-frame/subscribe [::loading-progress])
                loaded-finished? @(re-frame/subscribe [:de.explorama.frontend.projects.core/done-loading?])
                render? @(fi/call-api [:interaction-mode :render-sub?])]
            (when (and render?
                       loaded-finished?) ;! not sure if this is a got idea (seq loaded-finished?)
              (re-frame/dispatch [::delayed-close]))
            [loading-screen {:show? true
                             :message message
                             :progress (or loading-progress 0)}]))))
    :component-did-update
    (fn [this]
      (let [is-active? @(re-frame/subscribe [::is-active?])
            loaded-finished? @(re-frame/subscribe [:de.explorama.frontend.projects.core/loaded-project-id])
            render? @(fi/call-api [:interaction-mode :render-sub?])]
        (when (and render?
                   loaded-finished?
                   (not is-active?)
                   (aget js/window "screenshot"))
          (js/setTimeout (fn []
                           (.screenshot js/window))
                         3000))
        (when (and render?
                   loaded-finished?
                   (not is-active?)
                   (aget js/window "finishedTestCase"))
          (js/setTimeout (fn []
                           (.finishedTestCase js/window))
                         3000))))}))
