(ns de.explorama.frontend.table.components.details
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.shared.table.ws-api :as ws-api]
            [de.explorama.frontend.common.i18n :as i18n]))

(re-frame/reg-event-fx
 ws-api/retrieve-external-ref-result
 (fn [{db :db} [_ content]]
   {:db (reduce (fn [db [event-id attr content]]
                  (fi/call-api [:details-view :update-event-data-db-update]
                               db event-id {(name attr) content} true))
                db
                content)}))

(defn- handle-external-refs [db frame-id event-id event-data]
  (let [external-refs (get-in db (path/frame-external-refs frame-id))
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
                       [ws-api/retrieve-external-ref {:client-callback [ws-api/retrieve-external-ref-result]}
                        external-refs
                        loading])}))

;;Prepare event-trigger/logging and fix position due to pageX and pageY from event is wrong
(re-frame/reg-event-fx
 ::prepare-add-to-details-view
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id event-id mouse-event]]
   (let [read-only? (fi/call-api [:interaction-mode :read-only-db-get?]
                                 db
                                 {:frame-id frame-id})]
     (when-not read-only?
       (let [di (get-in db (path/table-datasource frame-id))
             page-x (aget mouse-event "pageX")
             page-y (aget mouse-event "pageY")
             add? (fi/call-api [:details-view :can-add?-db-get] db)
             limit-message (i18n/translate db :details-view-limit-message)]
         (if add?
           {:dispatch [:de.explorama.frontend.table.event-logging/ui-wrapper
                       frame-id
                       "add-to-details-view"
                       {:event-id event-id
                        :di di
                        :context :page
                        :left page-x
                        :top page-y}]}
           {:dispatch-n [(fi/call-api [:notify-event-vec]
                                      {:type :warning
                                       :category {:misc :details-view}
                                       :message limit-message})]}))))))

(re-frame/reg-event-fx
 ws-api/load-event-details-result
 (fn [{db :db} [_ frame-id event-data {:keys [event-id left top context di while-project-loading?]}]]
   (let [{:keys [event-data tubes-dispatch]} (handle-external-refs db frame-id event-id event-data)
         remove-event [::prepare-remove-from-details-view]
         icon :table
         comp-key (keyword (:vertical frame-id))
         removed-events (get-in db (path/removed-detail-view-events frame-id))
         add-event? (not (removed-events event-id))]
     (cond-> {}
       add-event?
       (assoc
        :db
        (fi/call-api [:details-view :add-to-details-view-db-update]
                     db frame-id di event-id event-data icon remove-event)
        :dispatch-n [(fi/call-api [:product-tour :next-event-vec] comp-key :details-view)
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
                         (path/removed-detail-view-events frame-id)
                         #(-> (or % #{})
                              (disj event-id))))))))

(re-frame/reg-event-fx
 ::prepare-remove-from-details-view
 (fn [{db :db} [_ frame-id di event-id]]
   {:dispatch [:de.explorama.frontend.table.event-logging/ui-wrapper
               frame-id
               "remove-from-details-view"
               {:event-id event-id
                :di di}]}))

(re-frame/reg-event-fx
 ::remove-from-details-view
 (fn [{db :db} [_ frame-id {:keys [di event-id]} task-id]]
   {:db (if event-id
          (fi/call-api [:details-view :remove-event-from-details-view-db-update]
                       db event-id)
          (fi/call-api [:details-view :remove-frame-events-from-details-view-db-update]
                       db frame-id))}))
