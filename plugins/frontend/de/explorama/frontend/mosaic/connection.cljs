(ns de.explorama.frontend.mosaic.connection
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.config :as gconfig]
            [de.explorama.frontend.mosaic.css :as css]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.interaction-mode :refer [render?]]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [taoensso.tufte :as tufte]))

(re-frame/reg-event-db
 ::dont-care
 (fn [db _]
   db))

(re-frame/reg-event-fx
 ::initialize
 (fn [_ _]
   {:backend-tube [ws-api/initialize-route
                   {:client-callback [ws-api/update-acs]}]}))


(defn unset-content [primary-canvas]
  (gdb/unset-events! primary-canvas)
  (gdb/unset-scale! primary-canvas))

(re-frame/reg-event-fx
 ws-api/operations-result
 (fn [{db :db}
      [_ frame-id task-id {:keys [invalid-options
                                  error-desc
                                  data-count]
                           :as transfer-description}]]
   (let [is-in-render-mode? ((render?) db)]
     (if error-desc
       (cond-> {:fx [[:dispatch [::ddq/finish-task frame-id task-id ::too-much-data?]]]
                :db db}
         is-in-render-mode?
         (-> (update :db assoc-in
                     (gp/stop-view frame-id)
                     (case (:error error-desc)
                       :too-much-data :stop-view-too-much-data
                       :axis-type-not-set :stop-view-scatter-no-values-for-axis
                       :scatter-plot-empty :stop-view-scatter-empty
                       :unknown :stop-view-unknown))
             (update :db assoc-in
                     (gp/stop-view-details frame-id)
                     error-desc))
         (and is-in-render-mode?
              (get-in db (gp/operation-desc-last-logged frame-id)))
         (update :fx
                 conj
                 [:dispatch [:de.explorama.frontend.mosaic.event-logging/log-event frame-id "operation" (get-in db (gp/operation-desc-last-logged frame-id))]])
         :always
         (update :db assoc-in
                 (gp/operation-desc frame-id)
                 (get-in db (gp/operation-desc-prev frame-id))))
       {:db (assoc-in db (gp/ignore-redo-ops frame-id) false)
        :dispatch-n [(when (and (seq invalid-options)
                                is-in-render-mode?)
                       (if (= 0 data-count)
                         (fi/call-api :frame-notifications-not-supported-redo-ops-event-vec
                                      frame-id
                                      #{{:op :no-data}})
                         (fi/call-api :frame-notifications-not-supported-redo-ops-event-vec
                                      frame-id
                                      (into #{}
                                            (map (fn [[k]]
                                                   {:op k})
                                                 invalid-options)))))
                     [::data-instance-init frame-id task-id transfer-description]]}))))

(re-frame/reg-event-fx
 ::data-instance-init
 (fn [{db :db}
      [_ frame-id task-id
       {:keys [di scale data-count filtered-data-count
               years countries datasources filtered-data-info
               local-filter new-di? generated-layout
               best-layout-ids usable-layout-ids annotations
               fallback-layout? new-operations-desc scatter-axis-fallback?
               data]
        {datasource-set :datasources :as dim-info} :dim-info}]]
   (when gconfig/timelog-tubes? (debug "Time di init: " (.getTime (js/Date.))))
   (debug "DATA INSTANCE INIT " {:new-di? new-di?
                                 :frame-id frame-id
                                 :task-id task-id})
   (tufte/profile
    {:when gconfig/timelog-tubes?}
    (tufte/p
     ::data-instance-init
     (let [top-path (gp/top-level frame-id)
           primary-canvas (gp/canvas top-path)]
       (unset-content primary-canvas)
       (let [new-data (tufte/p ::json-parse (js/JSON.parse data))
             raw-layouts (cond-> (get-in db gp/raw-layouts)
                           generated-layout
                           (assoc (:id (first generated-layout))
                                  (first generated-layout)))]
         (gdb/set-scale! top-path scale)
         (gdb/merge-annotations! frame-id annotations)
         ;Ensures that frame is not closed while data retrieval
         (if (get-in db top-path)
           {:db (-> (cond-> db
                      usable-layout-ids
                      (assoc-in (gp/layout-details frame-id)
                                (css/transform-style (select-keys raw-layouts
                                                                  usable-layout-ids)))
                      usable-layout-ids
                      (assoc-in (gp/usable-layouts frame-id) usable-layout-ids)
                      (seq best-layout-ids)
                      (assoc-in (gp/selected-layouts frame-id)
                                (mapv raw-layouts
                                      best-layout-ids))

                      (seq new-operations-desc)
                      (assoc-in (gp/operation-desc frame-id) new-operations-desc)
                      scatter-axis-fallback?
                      (update-in (gp/operation-desc frame-id)
                                 (fn [operation-desc]
                                   (-> operation-desc
                                       (assoc gcp/scatter-x (:x-label scale))
                                       (assoc gcp/scatter-y (:y-label scale))))))
                    (assoc-in (gp/applied-filter top-path) local-filter)
                    (assoc-in (gp/selected-years frame-id) years)
                    (assoc-in (gp/selected-countries frame-id) countries)
                    (assoc-in (gp/selected-datasources frame-id) datasources)
                    (assoc-in (gp/filtered-data-info frame-id) filtered-data-info)
                    (assoc-in (gp/instance-datasources frame-id) datasource-set)
                    (assoc-in (gp/fallback-layout frame-id) (if fallback-layout? true false))
                    (assoc-in (gp/dim-info frame-id) dim-info)
                    (update-in (gp/operation-desc frame-id)
                               (fn [operation-desc]
                                 (cond-> operation-desc
                                   (nil? (get operation-desc gcp/scatter-x))
                                   (assoc gcp/scatter-x (:x-label scale))
                                   (nil? (get operation-desc gcp/scatter-y))
                                   (assoc gcp/scatter-y (:y-label scale))))))
            :dispatch-n [[:de.explorama.frontend.mosaic.mosaic/connected-to-di frame-id di new-data data-count filtered-data-count task-id]]}
           (do
             (debug "data not saved, because frame is not there anymore")
             {}))))))))
