(ns de.explorama.frontend.table.components.warning-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.table.path :as paths]))

(re-frame/reg-sub
 ::warning-view-display
 (fn [db [_ frame-id]]
   (get-in db (paths/warn-view-display frame-id))))

(re-frame/reg-sub
 ::warning-view-cancel
 (fn [db [_ frame-id]]
   (get-in db (paths/warn-view-cancel-event frame-id))))

(re-frame/reg-event-fx
 ::warning-proceed
 (fn [{db :db} [_ frame-id]]
   {:db (paths/dissoc-in db
                         (paths/frame-warn frame-id))
    :dispatch (get-in db (paths/warn-view-callback frame-id))}))

(re-frame/reg-event-fx
 ::warning-stop
 (fn [{db :db} [_ frame-id]]
   {:db (paths/dissoc-in db
                         (paths/frame-warn frame-id))}))