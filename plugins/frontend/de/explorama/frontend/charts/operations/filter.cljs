(ns de.explorama.frontend.charts.operations.filter
  (:require ["moment"]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.charts.path :as path]))

(re-frame/reg-event-fx
 ::check-before-show
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event]]
   (let [show-warn? (get-in db (path/filter-warn-limit-reached frame-id))
         show-stop? (get-in db (path/filter-stop-limit-reached frame-id))]
     (cond-> {:db (cond
                    show-stop?
                    (assoc-in db (path/stop-view-display frame-id) :stop-view-display)
                    show-warn?
                    (-> (assoc-in db (path/warn-view-display frame-id) :filter-warn-view)
                        (assoc-in (path/warn-view-callback frame-id) event))
                    :else db)}
       (not (or show-warn? show-stop?))
       (assoc :dispatch event)))))

