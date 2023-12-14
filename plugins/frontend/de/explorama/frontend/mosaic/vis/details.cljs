(ns de.explorama.frontend.mosaic.vis.details
  (:require [clojure.walk :as cw]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.interaction-mode :refer [read-only?]]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::update-entry
 (fn [{db :db} [_ content]]
   {:db (reduce (fn [db [event-id attr content]]
                  (fi/call-api [:details-view :update-event-data-db-update]
                               db
                               event-id
                               {(name attr) content}
                               true))
                db
                content)}))

;;Prepare event-trigger/logging and fix position due to pageX and pageY from event is wrong
(re-frame/reg-event-fx
 ::prepare-add-to-details-view
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id data-key mouse-event]]
   (let [read-only? ((read-only?) db {:frame-id frame-id
                                      :component :mosaic
                                      :additional-info :details-view})
         event-id (gdal/get data-key 1)
         bucket (gdal/get data-key 2)]
     (when-not read-only?
       (let [page-x (aget mouse-event "pageX")
             page-y (aget mouse-event "pageY")
             add? (fi/call-api [:details-view :can-add?-db-get] db)
             limit-message (i18n/translate db :details-view-limit-message)]
         (if add?
           {:dispatch [:de.explorama.frontend.mosaic.event-logging/ui-wrapper
                       frame-id
                       "add-to-details-view"
                       {:path (gp/top-level frame-id)
                        :event-id event-id
                        :context :page
                        :di (get-in db (gp/data-instance frame-id))
                        :bucket bucket
                        :left page-x
                        :top page-y}]}
           {:dispatch-n [(fi/call-api [:notify-event-vec]
                                      {:type :warning
                                       :category {:misc :details-view}
                                       :message limit-message})]}))))))

(re-frame/reg-event-fx
 ::add-to-details-view
 (fn [{db :db} [_ path {:keys [event-id left top context di bucket while-project-loading?]} task-id]]
   (let [frame-id (gp/frame-id path)
         event-data (grc/get-event (gp/frame-id path)
                                   bucket event-id)
         event-data (gdal/g-> event-data)
         event-data (cw/stringify-keys event-data)
         remove-event [::prepare-remove-from-details-view]
         icon :mosaic2]
     (cond-> {:db (fi/call-api [:details-view :add-to-details-view-db-update]
                               db frame-id di event-id event-data icon remove-event)
              :dispatch-n [(when task-id
                             [::ddq/finish-task frame-id task-id ::add-to-details-view])
                           (fi/call-api [:product-tour :next-event-vec] :mosaic :details-view)
                           (when-not while-project-loading?
                             (fi/call-api [:details-view  :add-event-compare-event-vec]
                                          {:frame-id frame-id
                                           :event-id event-id
                                           :context context
                                           :left left
                                           :top top}))]}))))


(re-frame/reg-event-fx
 ::prepare-remove-from-details-view
 (fn [_ [_ frame-id di event-id]]
   {:dispatch [:de.explorama.frontend.mosaic.event-logging/log-event
               (gp/frame-id frame-id)
               "remove-from-details-view"
               {:event-id event-id
                :di di}]}))

(re-frame/reg-event-fx
 ::remove-from-details-view
 (fn [{db :db} [_ frame-id _ task-id]]
   (cond-> (fi/call-api [:details-view :remove-frame-events-from-details-view-db-update]
                        db frame-id)
     task-id
     (update-in [:dispatch-n]
                conj
                [::ddq/finish-task frame-id task-id ::remove-from-details-view]))))