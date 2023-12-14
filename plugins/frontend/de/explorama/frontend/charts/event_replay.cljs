(ns de.explorama.frontend.charts.event-replay
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.event-logging.util :as log-util]
            [de.explorama.frontend.common.queue :as ddq]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.charts.charts.core :as charts]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.shared.charts.util :refer [is-charts?]]
            [de.explorama.shared.charts.ws-api :as ws-api]))

(defn check-frame-id [frame-id callback-vec chart-body]
  (cond
    (is-charts? frame-id)
    chart-body
    :else
    (do (debug "ignore event " frame-id)
        {:dispatch callback-vec})))

(defn create-frame-event [_ frame-id callback-vec _]
  (check-frame-id
   frame-id
   callback-vec
   {:dispatch callback-vec}))

(defn recreate-frame [db _ callback-vec {:keys [frame-id]
                                         :as params}]
  {:db (ddq/set-event-callback db frame-id callback-vec)
   :dispatch [::charts/charts-view-event
              :frame/recreate
              params]})

(defn override-frame [db _ callback-vec {:keys [frame-id]
                                         :as params}]
  {:db (ddq/set-event-callback db frame-id callback-vec)
   :dispatch [::charts/charts-view-event
              :frame/override
              params]})

(defn close-event [db frame-id callback-vec _]
  (check-frame-id
   frame-id
   callback-vec
   {:db (path/dissoc-in db (path/frame-desc frame-id))
    :dispatch-n [callback-vec]}))

(defn replay-filter [db frame-id callback-vec local-filter _ _]
  {:db (assoc-in db (path/applied-filter frame-id) local-filter)
   :fx [[:dispatch callback-vec]]})

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(defn update-chart [db frame-id callback-vec {:keys [selection di local-filter]}]
  {:db (cond-> db
         :always (charts/set-chart-selection frame-id selection)
         callback-vec (ddq/set-event-callback frame-id callback-vec)
         di (assoc-in (path/frame-di frame-id) di))
   :dispatch [::ddq/queue frame-id
              [:de.explorama.frontend.charts.charts.backend-interface/connect-to-datainstance
               {:frame-target-id frame-id
                :di di
                :keep-selection? true
                ;; force-operations-state? needed from release 10->11, because there sometime nil values can exist due to logging bug
                :force-operations-state? true}
               [local-filter]]]})

(def events-vektor-funcs
  "[event-name event-version]"
  {["update-chart" 1] update-chart
   ["create-frame" 1] create-frame-event
   ["apply-constraints" 1] replay-filter
   ["close-frame" 1] close-event
   ["move-frame" 1] no-event
   ["resize-stop" 1] no-event
   ["maximize" 1] no-event
   ["minimize" 1] no-event
   ["normalize" 1] no-event
   ["z-index" 1] no-event
   ["create-di" 1] no-event})

(defn event-func
  ([event-name]
   (event-func event-name 1))
  ([event-name event-version]
   (get events-vektor-funcs [event-name event-version])))

(defn- sync-update-chart [db frame-id callback-vec {:keys [selection di local-filter] :as d}]
  (let [current-chart-types (mapv #(get-in % [:type-desc :cid])
                                  (get-in db (path/charts frame-id)))
        new-chart-types (mapv #(get-in % [:type-desc :cid])
                              (get-in selection [:chart-desc :charts]))
        type-changed? (not= current-chart-types new-chart-types)]
    {:db (cond-> db
           :always (charts/set-chart-selection frame-id selection)
           callback-vec (ddq/set-event-callback frame-id callback-vec)
           di (assoc-in (path/frame-di frame-id) di)
           type-changed? (assoc-in (path/chart-data frame-id) []))
     :dispatch [::ddq/queue frame-id
                [:de.explorama.frontend.charts.charts.backend-interface/connect-to-datainstance
                 {:frame-target-id frame-id
                  :di di
                  :keep-selection? true
                ;; force-operations-state? needed from release 10->11, because there sometime nil values can exist due to logging bug
                  :force-operations-state? true}
                 [local-filter]]]}))

(def sync-event-funcs
  (merge events-vektor-funcs
         {["update-chart" 1] sync-update-chart}))

(defn sync-event-func
  ([event-name]
   (sync-event-func event-name 1))
  ([event-name event-version]
   (get sync-event-funcs [event-name event-version])))

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] ignored-frames callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "replay-event charts" {:frame-id frame-id
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
                                 is-charts?
                                 (constantly false)
                                 (constantly false)))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (sync-event-func event-name event-version)]
     (debug "sync-event charts" {:frame-id frame-id
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
   (debug "-------------->" events)
   (let [{events :result
          :keys [ignored-frames]} (pre-process-events events)]
     (debug "charts replay-events" events)
     {:dispatch [::next-replay-event
                 (first events)
                 (rest events)
                 0
                 (count events)
                 done-event
                 ignored-frames
                 plogs-id
                 (when test-and-profile? test-and-profile?)]})))
