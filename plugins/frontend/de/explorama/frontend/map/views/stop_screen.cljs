(ns de.explorama.frontend.map.views.stop-screen
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.map.paths :as path]))

(re-frame/reg-event-db
 ::stop-view-display
 (fn [db [_ frame-id display details]]
   (cond-> db
     :always (assoc-in (path/stop-view-display frame-id) display)
     display (assoc-in (path/stop-view-details frame-id) details)
     (not display) (update-in (path/frame-desc frame-id) dissoc :stop-view-details))))

(re-frame/reg-sub
 ::stop-view-display
 (fn [db [_ frame-id]]
   (get-in db (path/stop-view-display frame-id))))