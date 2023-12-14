(ns de.explorama.frontend.charts.components.stop-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.charts.path :as path]))

(re-frame/reg-event-db
 ::stop-view-display
 (fn [db [_ frame-id display details]]
   (-> db
       (assoc-in (path/stop-view-display frame-id)
                 display)
       (assoc-in (path/stop-view-details frame-id)
                 details))))

(re-frame/reg-sub
 ::stop-view-display
 (fn [db [_ frame-id]]
   (get-in db (path/stop-view-display frame-id))))