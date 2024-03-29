(ns de.explorama.frontend.indicator.event-replay
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.event-logging.util :as log-util]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.indicator.path :as ip]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug]]))

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(defn restore-indicator-desc [db _ callback-event {{:keys [id]
                                                    :as indicator} :indicator} _]
  {:db (assoc-in db
                 (ip/project-indicator-desc id)
                 (assoc indicator :write-access? false))
   :dispatch callback-event})

(def event-vector-funcs
  "[event-name event-version]"
  {["restore-indicator-desc" 1] restore-indicator-desc})

(defn event-func
  ([event-name]
   (event-func event-name 1))
  ([event-name event-version]
   (get event-vector-funcs
        [event-name event-version])))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "sync-event indicator" {:frame-id frame-id
                                    :event-name event-name
                                    :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id nil parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ {:keys [ignored-frames callback-event]
                   [_ frame-id event-name event-params event-version] :event}]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "replay-event indicator" {:frame-id frame-id
                                      :event-name event-name
                                      :parsed-event-params parsed-event-params
                                      :callback-event callback-event})
     (if event-func
       (event-func db frame-id callback-event parsed-event-params ignored-frames)
       (do
         (debug "no event-function found for " [event-name event-version] ", use no-event fn.")
         {:dispatch callback-event})))))

(re-frame/reg-event-fx
 ::next-replay-event
 (fn [{db :db} [_ {:keys [next-event rest-events events-done
                          max-events done-event-callback ignored-frames
                          plogs-id profiling-state]}]]
   (let [profiling-state (when profiling-state
                           (-> (assoc-in profiling-state
                                         [(get profiling-state :last-event)
                                          :end]
                                         (.now js/Date.))
                               (assoc next-event {:start (.now js/Date.)
                                                  :num (inc events-done)}
                                      :last-event next-event)))]
     (when (aget js/window "sendHealthPing")
       (.sendHealthPing js/window))
     {:db (assoc-in db ip/replay-progress (/ events-done
                                             max-events))
      :dispatch (if next-event
                  [::replay-event
                   {:event next-event
                    :ignored-frames ignored-frames
                    :callback-event [::next-replay-event
                                     {:next-event (first rest-events)
                                      :rest-events (rest rest-events)
                                      :events-done (inc events-done)
                                      :max-events max-events
                                      :done-event-callback done-event-callback
                                      :ignored-frames ignored-frames
                                      :plogs-id plogs-id
                                      :profiling-state profiling-state}]}]
                  (cond-> done-event-callback
                    profiling-state (conj profiling-state)))})))

(def pre-process-events (partial log-util/pre-process-events
                                 (fn [{v :vertical}]
                                   (= v "indicator"))
                                 (constantly false)
                                 (constantly false)))

(re-frame/reg-event-fx
 ::replay-events
 (fn [_ [_ events done-event plogs-id test-and-profile?]]
   (let [{events :result}
         (pre-process-events events)]
     {:dispatch [::next-replay-event
                 {:next-event (first events)
                  :rest-events (rest events)
                  :events-done 0
                  :max-events (count events)
                  :done-event-callback done-event
                  :ignored-frames #{}
                  :plogs-id plogs-id
                  :test-and-profile? (when test-and-profile? test-and-profile?)}]})))