(ns de.explorama.frontend.charts.components.warning-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.charts.path :as path]))

(re-frame/reg-sub
 ::warning-view-display
 (fn [db [_ frame-id]]
   (get-in db (path/warn-view-display frame-id))))

(re-frame/reg-sub
 ::warning-view-cancel
 (fn [db [_ frame-id]]
   (get-in db (path/warn-view-cancel-event frame-id))))

(re-frame/reg-event-fx
 ::warning-proceed
 (fn [{db :db} [_ frame-id]]
   {:db (path/dissoc-in db
                        (path/frame-warn frame-id))
    :dispatch (get-in db (path/warn-view-callback frame-id))}))

(re-frame/reg-event-fx
 ::warning-stop
 (fn [{db :db} [_ frame-id]]
   {:db (path/dissoc-in db
                        (path/frame-warn frame-id))}))