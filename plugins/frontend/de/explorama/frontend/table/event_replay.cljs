(ns de.explorama.frontend.table.event-replay
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.event-logging.util :as log-util]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.shared.table.util :refer [is-table?]]
            [de.explorama.frontend.table.table.core :as table]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.frontend.table.table.state :as table-state]
            [de.explorama.shared.table.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(defn check-frame-id [frame-id callback-vec table-body]
  (cond
    (is-table? frame-id)
    table-body
    :else
    (do (debug "ignore event " frame-id)
        {:dispatch callback-vec})))

(defn create-frame-event [_ frame-id callback-vec {{:keys [source-title source-frame]} :opts}]
  (check-frame-id
   frame-id
   callback-vec
   {:fx [[:dispatch [::table-state/init-frame frame-id nil source-title source-frame]]
         [:dispatch callback-vec]]}))

(defn recreate-frame [db _ callback-vec {:keys [frame-id]
                                         :as params}]
  {:db (ddq/set-event-callback db frame-id callback-vec)
   :dispatch [::table/table-view-event
              :frame/recreate
              params]})

(defn override-frame [db _ callback-vec {:keys [frame-id]
                                         :as params}]
  {:db (ddq/set-event-callback db frame-id callback-vec)
   :dispatch [::table/table-view-event
              :frame/override
              params]})

(defn selection-event [db frame-id callback-vec selections]
  (check-frame-id
   frame-id
   callback-vec
   {:fx [(when selections
           [:dispatch [::table-state/update-selection frame-id selections true]])
         (when callback-vec
           [:dispatch callback-vec])]}))

(defn close-event [db frame-id callback-vec _]
  (check-frame-id
   frame-id
   callback-vec
   {:db (path/dissoc-in db (path/frame-desc frame-id))
    :dispatch-n [callback-vec]}))

(defn update-connection [db frame-id
                         callback-vec
                         {:keys [targets]
                          {:keys [di selections]} :connection-data}
                         ignored-frames]
  (let [targets (conj targets [frame-id])
        table-frames (filterv #(and (is-table? %)
                                    (not (ignored-frames %)))
                              (map first targets))
        vis-frames? (not-empty table-frames)]
    (cond-> {:dispatch-n []}
      (and vis-frames? selections)
      (update :dispatch-n (fn [o]
                            (reduce (fn [r frame-id]
                                      (conj r [::table-state/update-selection frame-id selections]))
                                    (-> (or o [])
                                        (conj callback-vec))
                                    table-frames)))

      (or (not table-frames)
          (not selections))
      (assoc :dispatch callback-vec))))

(defn replay-filter [db frame-id callback-vec local-filter _ _]
  {:db (assoc-in db (path/applied-filter frame-id) local-filter)
   :fx [[:dispatch callback-vec]]})

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(defn- update-table [db frame-id callback-vec {:keys [di] :as table-state}]
  (table-data/merge-frame-table-config frame-id (select-keys table-state ws-api/logging-keys))
  {:db (cond-> (ddq/set-event-callback db frame-id callback-vec)
         di (assoc-in (path/table-datasource frame-id) di))
   :dispatch (table-data/request-data-event-vec frame-id {:calc {:data-keys? true
                                                                 :di-desc? true
                                                                 :data-acs? true}})})

(defn add-to-details-view-event
  [db frame-id callback-vec desc]
  (check-frame-id
   frame-id
   callback-vec
   {:backend-tube [ws-api/load-event-details {:client-callback [ws-api/load-event-details-result]}
                   frame-id
                   (assoc desc
                          :while-project-loading?
                          (boolean (fi/call-api :project-loading-db-get db)))]
    :db (update-in db
                   (path/removed-detail-view-events frame-id)
                   #(-> (or % #{})
                        (disj (:event-id desc))))
    :fx [(when callback-vec
           [:dispatch callback-vec])]}))

(defn remove-from-details-view
  [db frame-id callback-vec desc]
  (check-frame-id
   frame-id
   callback-vec
   {:db (update-in db
                   (path/removed-detail-view-events frame-id)
                   #(-> (or % #{})
                        (conj (:event-id desc))))
    :fx [[:dispatch [:de.explorama.frontend.table.components.details/remove-from-details-view frame-id desc]]
         (when callback-vec
           [:dispatch callback-vec])]}))

(def events-vektor-funcs
  "[event-name event-version]"
  {["update-table" 1] update-table
   ["create-frame" 1] create-frame-event
   ["close-frame" 1] close-event
   ["apply-constraints" 1] replay-filter
   ["move-frame" 1] no-event
   ["resize-stop" 1] no-event
   ["maximize" 1] no-event
   ["minimize" 1] no-event
   ["normalize" 1] no-event
   ["z-index" 1] no-event
   ["create-di" 1] no-event
   ["add-to-details-view" 1] add-to-details-view-event
   ["remove-from-details-view" 1] remove-from-details-view})

(defn event-func
  ([event-name]
   (event-func event-name 1))
  ([event-name event-version]
   (get events-vektor-funcs [event-name event-version])))

(defn sync-event-func
  ([event-name]
   (sync-event-func event-name 1))
  ([event-name event-version]
   (get events-vektor-funcs [event-name event-version])))

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] ignored-frames callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "replay-event table" {:frame-id frame-id
                                  :event-name event-name
                                  :callback-vec callback-vec
                                  :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id callback-vec parsed-event-params ignored-frames)
       (do
         (debug "no event-function found for " [event-name event-version] ", dispatching callback-vec.")
         {:dispatch callback-vec})))))

(re-frame/reg-event-fx
 ::next-replay-event
 (fn [{db :db} [_ current-event rest-events events-done max-events done-event ignored-frames plogs-id profiling-state]]
   (let [profiling-state (when profiling-state
                           (-> (assoc-in profiling-state
                                         [(get profiling-state :last-event)
                                          :end]
                                         (.now js/Date.))
                               (assoc current-event {:start (.now js/Date.)
                                                     :num (inc events-done)}
                                      :last-event current-event)))]

     (when (aget js/window "sendHealthPing")
       (.sendHealthPing js/window))
     {:db (assoc-in db path/replay-progress (/ events-done
                                               max-events))
      :dispatch (if current-event
                  [::replay-event current-event
                   ignored-frames
                   [::next-replay-event
                    (first rest-events)
                    (rest rest-events)
                    (inc events-done)
                    max-events
                    done-event
                    ignored-frames
                    plogs-id
                    (when profiling-state
                      profiling-state)]]
                  (cond-> done-event
                    profiling-state
                    (conj profiling-state)))})))

(def pre-process-events (partial log-util/pre-process-events
                                 is-table?
                                 (constantly false)
                                 (constantly false)
                                 #{"add-to-details-view" "remove-from-details-view"}))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (sync-event-func event-name event-version)]
     (debug "sync-event table" {:frame-id frame-id
                                :event-name event-name
                                :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id nil parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(re-frame/reg-event-fx
 ::replay-events
 (fn [{db :db} [_ events done-event plogs-id test-and-profile?]]
   (let [{events :result
          :keys [ignored-frames]} (pre-process-events events)]
     (debug "table replay-events" events)
     {:dispatch [::next-replay-event
                 (first events)
                 (rest events)
                 0
                 (count events)
                 done-event
                 ignored-frames
                 plogs-id
                 (when test-and-profile? test-and-profile?)]})))
