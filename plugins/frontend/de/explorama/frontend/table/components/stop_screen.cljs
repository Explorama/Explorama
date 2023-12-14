(ns de.explorama.frontend.table.components.stop-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.table.path :as paths]))

(re-frame/reg-event-db
 ::stop-view-display
 (fn [db [_ frame-id display details]]
   (-> db
       (assoc-in (paths/stop-view-display frame-id)
                 display)
       (assoc-in (paths/stop-view-details frame-id)
                 details))))

(re-frame/reg-sub
 ::stop-view-display
 (fn [db [_ frame-id]]
   (get-in db (paths/stop-view-display frame-id))))