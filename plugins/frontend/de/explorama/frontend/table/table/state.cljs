(ns de.explorama.frontend.table.table.state
  (:require [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.table.interaction.selection :as selection]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.shared.table.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-event-fx
 ::init-frame
 (fn [{db :db} [_ frame-id source-title source-frame-id source-di source-table-state]]
   (let [source-selections (get-in db (path/current-selection source-frame-id))]
     (when source-table-state
       (table-data/merge-frame-table-config frame-id (select-keys source-table-state ws-api/logging-keys)))
     {:db (cond-> db
            frame-id (update-in (path/table-frame frame-id) #(or % {}))
            source-di (assoc-in (path/frame-di frame-id) source-di)
            source-selections (assoc-in (path/current-selection frame-id)
                                        source-selections))})))

(re-frame/reg-sub
 ::selected-ids
 (fn [db [_ frame-id]]
   (set (map #(attrs/value % (attrs/access-key "id"))
             (:current (get-in db (path/current-selection frame-id)))))))

(re-frame/reg-event-fx
 ::update-selection
 (fn [{db :db} [_ frame-id selections ignore-request?]]
   (when selections
     (debug "Update selection" {:frame-id frame-id
                                :selections selections
                                :ignore-request? ignore-request?})

     (let [from-this-frame? (= (get-in selections [:source-infos :frame-id])
                               frame-id)
           scroll-to-row? (and (not from-this-frame?)
                               (= :select (:last-action selections)))
           project-loading? (fi/call-api :project-loading-db-get db)]
       (table-data/set-frame-table-config frame-id
                                          ws-api/focus-event-id-key
                                          (if scroll-to-row?
                                            (-> (peek (:current selections))
                                                (attrs/value (attrs/access-key "id")))
                                            nil))
       {:db (assoc-in db
                      (path/current-selection frame-id)
                      selections)
        :fx [(when (and scroll-to-row?
                        (not ignore-request?)
                        (not project-loading?))
               [:dispatch (table-data/request-data-event-vec frame-id)])]}))))


(re-frame/reg-event-fx
 ::click-select-handler
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id data-event]]
   (let [id-key (attrs/access-key "id")
         location-key (attrs/access-key "location")
         event-id (attrs/value data-event id-key)
         event-location (attrs/value data-event location-key)
         selected {id-key event-id
                   location-key event-location}
         current-selection (get-in db (path/current-selection frame-id) {})
         already-selected? (some (fn [s]
                                   (= event-id (attrs/value s id-key)))
                                 (:current current-selection))]
     {:dispatch (if already-selected?
                  [::selection/deselect frame-id selected]
                  [::selection/select frame-id selected])})))

(re-frame/reg-sub
 ::replay-events
 (fn [db [_ frame-id]]
   (get-in db (path/replay-queue frame-id))))

(re-frame/reg-sub
 ::loaded-page
 (fn [db [_ frame-id]]
   (get-in db (path/loaded-page frame-id) 0)))

(re-frame/reg-sub
 ::base-columns
 (fn [db [_ frame-id]]
   (get-in db (path/infos-columns frame-id) [])))

(re-frame/reg-sub
 ::flag-render
 (fn [db [_ frame-id]]
   (get-in db (path/flag-render frame-id))))

(re-frame/reg-sub
 ::selection
 (fn [db [_ frame-id]]
   (get-in db (path/current-selection frame-id))))

(re-frame/reg-sub
 ::focus
 (fn [db [_ frame-id]]
   (get-in db (path/current-focus frame-id))))
