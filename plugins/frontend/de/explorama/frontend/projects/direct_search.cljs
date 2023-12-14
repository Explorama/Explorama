(ns de.explorama.frontend.projects.direct-search
  (:require [de.explorama.frontend.projects.path :as path]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-event-fx
 ::search
 (fn [_ [_ query]]
   (debug query)
   {:backend-tube [ws-api/search-route
                   {}
                   query]}))

(re-frame/reg-event-fx
 ::add-and-open-project
 (fn [_ [_ project]]
   {:dispatch-n [[:de.explorama.frontend.projects.core/show-project-for-overview project true]
                 [:de.explorama.frontend.projects.core/start-loading-project project]]}))

(re-frame/reg-event-fx
 ::only-add-project
 (fn [_ [_ project]]
   {:dispatch-n [[:de.explorama.frontend.projects.core/show-project-for-overview project true]]}))

(re-frame/reg-event-fx
 ::project-locked
 (fn [_ [_ project]]
   (debug "Project is locked - Title:" (:title project) " - ID:" (:project-id project))
   {}))

(re-frame/reg-event-fx
 ::show-all
 (fn [{db :db} [_ projects]]
   {:db (-> db
            (assoc-in path/overview-overlayer-active? true)
            (assoc-in path/overview-show-all? true))
    :dispatch [:de.explorama.frontend.projects.views.overview/set-result-projects projects]}))
