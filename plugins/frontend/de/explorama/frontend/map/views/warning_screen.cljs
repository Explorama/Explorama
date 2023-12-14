(ns de.explorama.frontend.map.views.warning-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.map.paths :as geop]))

(re-frame/reg-event-db
 ::warning-view-display
 (fn [db [_ frame-id callback-event]]
   (-> db
       (assoc-in (geop/frame-warn-view? frame-id) true)
       (assoc-in (geop/frame-warn-callback frame-id) callback-event))))

(re-frame/reg-sub
 ::warning-view-display
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-warn-view? frame-id))))

(re-frame/reg-event-fx
 ::warning-proceed
 (fn [{db :db} [_ frame-id]]
   {:db (geop/dissoc-in db
                        (geop/frame-warn-screen frame-id))
    :dispatch (get-in db (geop/frame-warn-callback frame-id))}))

(re-frame/reg-event-db
 ::warning-stop
 (fn [db [_ frame-id]]
   (geop/dissoc-in db 
                   (geop/frame-warn-screen frame-id))))