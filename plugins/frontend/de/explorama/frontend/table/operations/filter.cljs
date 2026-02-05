(ns de.explorama.frontend.table.operations.filter
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.table.path :as paths]))

(re-frame/reg-event-fx
 ::check-before-show
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event]]
   (let [show-warn? (get-in db (paths/filter-warn-limit-reached frame-id))
         show-stop? (get-in db (paths/filter-stop-limit-reached frame-id))]
     (cond-> {:db (cond
                    show-stop?
                    (assoc-in db (paths/stop-view-display frame-id) :stop-view-display)
                    show-warn?
                    (-> (assoc-in db (paths/warn-view-display frame-id) :filter-warn-view)
                        (assoc-in (paths/warn-view-callback frame-id) event))
                    :else db)}
       (not (or show-warn? show-stop?))
       (assoc :dispatch event)))))

