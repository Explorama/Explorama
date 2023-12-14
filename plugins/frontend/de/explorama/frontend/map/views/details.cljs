(ns de.explorama.frontend.map.views.details
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]))

(re-frame/reg-event-fx
 ws-api/update-ref-entry
 (fn [{db :db} [_ content]]
   {:db (reduce (fn [db [event-id attr content]]
                  (fi/call-api [:details-view :update-event-data-db-update]
                               db event-id {(name attr) content} true))
                db
                content)}))

(defn- handle-external-refs [db frame-id event-id event-data]
  (let [external-refs (get-in db (geop/frame-external-refs frame-id))
        external-refs (reduce merge {} external-refs)
        external-refs-keys (set (mapv name (keys external-refs)))
        {:keys [event-data loading]}
        (reduce (fn [acc attr]
                  (let [value (get event-data attr)]
                    (if (external-refs-keys attr)
                      (-> acc
                          (update :event-data (fn [val]
                                                (assoc val attr "loading")))
                          (update :loading conj (if value
                                                  [event-id attr value]
                                                  [event-id attr event-id])))
                      (update acc :event-data (fn [val]
                                                (assoc val attr value))))))
                {:event-data {}
                 :loading []}
                (keys event-data))]
    {:event-data event-data
     :tubes-dispatch (when (and external-refs (not-empty loading))
                       [ws-api/load-external-ref
                        {:client-callback [ws-api/update-ref-entry]}
                        external-refs
                        loading])}))

;;Prepare event-trigger/logging and fix position due to pageX and pageY from event is wrong
(re-frame/reg-event-fx
 ::prepare-add-to-details-view
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id di event-id mouse-event]]
   (let [read-only? (fi/call-api [:interaction-mode :read-only-db-get?]
                                 db
                                 {:frame-id frame-id})
         [bucket event-id] event-id]
     (when-not read-only?
       (let [page-x (aget mouse-event "pageX")
             page-y (aget mouse-event "pageY")
             add? (fi/call-api [:details-view :can-add?-db-get] db)
             limit-message (i18n/translate db :details-view-limit-message)]
         (if add?
           {:dispatch [:de.explorama.frontend.map.event-logging/ui-wrapper
                       frame-id
                       "add-to-details-view"
                       {:event-id event-id
                        :bucket bucket
                        :di di
                        :context :page
                        :left page-x
                        :top page-y}]}
           {:dispatch-n [(fi/call-api [:notify-event-vec]
                                      {:type :warning
                                       :category {:misc :details-view}
                                       :message limit-message})]}))))))

(re-frame/reg-event-fx
 ::add-to-details-view
 (fn [{db :db} [_ frame-id {:keys [event-id bucket left top context di while-project-loading?] :as params} task-id]]
   (when (map-api/instances-exist? frame-id)
     (let [bucket-event-id [bucket event-id]]
       (if-let [event-data (map-api/get-event-data frame-id bucket-event-id)]
         (let [{:keys [event-data tubes-dispatch]} (handle-external-refs db frame-id event-id event-data)
               remove-event [::prepare-remove-event-from-details-view]
               icon :map
               removed-events (get-in db (geop/removed-detail-view-events frame-id))
               add-event? (not (removed-events event-id))]
           (cond-> {}
             add-event?
             (assoc
              :db
              (fi/call-api [:details-view :add-to-details-view-db-update]
                           db frame-id di event-id event-data icon remove-event)
              :dispatch-n [(fi/call-api [:product-tour :next-event-vec] :map :details-view)
                           (when-not while-project-loading?
                             (fi/call-api [:details-view :add-event-compare-event-vec]
                                          {:frame-id frame-id
                                           :event-id event-id
                                           :context context
                                           :left left
                                           :top top}))])
             (and tubes-dispatch add-event?)
             (assoc :backend-tube tubes-dispatch)
             (not add-event?)
             (assoc :db
                    (update-in db
                               (geop/removed-detail-view-events frame-id)
                               #(-> (or % #{})
                                    (disj event-id))))))
         {:backend-tube [ws-api/retrieve-event-data
                         {:client-callback [ws-api/retrieved-event-data
                                            frame-id
                                            bucket-event-id
                                            [::add-to-details-view frame-id params task-id]]}
                         bucket-event-id]})))))

(re-frame/reg-event-fx
 ::prepare-remove-event-from-details-view
 (fn [{db :db} [_ frame-id di event-id]]
   {:dispatch [:de.explorama.frontend.map.event-logging/log-event
               frame-id
               "remove-event-from-details-view"
               {:event-id event-id
                :di di}]}))

(re-frame/reg-event-fx
 ::remove-event-from-details-view
 (fn [{db :db} [_ frame-id {:keys [di event-id]}]]
   {:db (fi/call-api [:details-view :remove-event-from-details-view-db-update]
                     db event-id)}))

(re-frame/reg-event-fx
 ::prepare-remove-from-details-view
 (fn [{db :db} [_ frame-id di event-id]]
   {:dispatch [:de.explorama.frontend.map.event-logging/log-event
               frame-id
               "remove-from-details-view"
               {:event-id event-id
                :di di}]}))

(re-frame/reg-event-fx
 ::remove-from-details-view
 (fn [{db :db} [_ frame-id {:keys [di event-id]} task-id]]
   (cond-> {:db (if event-id
                  (fi/call-api [:details-view :remove-event-from-details-view-db-update]
                               db event-id)
                  (fi/call-api [:details-view :remove-frame-events-from-details-view-db-update]
                               db frame-id))}
     task-id
     (update-in [:dispatch-n]
                conj
                [::ddq/finish-task frame-id task-id ::remove-from-details-view]))))