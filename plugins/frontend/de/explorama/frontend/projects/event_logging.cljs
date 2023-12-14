(ns de.explorama.frontend.projects.event-logging
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.projects.config :as config]
            [de.explorama.frontend.projects.path :as ppath]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]))

(def event-version 1.0)

(re-frame/reg-event-fx
 ::execute-callback
 (fn [{db :db}]
   (let [callback (get-in db ppath/log-callback)]
     (if callback
       {:db (update db ppath/root dissoc ppath/log-callback-key)
        :dispatch-n [callback]}
       {}))))

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ [_ resp]]
   {:dispatch (fi/call-api :debug-event-vec (str "Event-logging success" resp))}
   {}))

(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ [_ resp]]
   {:dispatch-n [(fi/call-api :error-event-vec (str "Event-logging failed" resp))
                 [::execute-callback]]}))

(defn log-event-fn [db origin frame-id event-name description version & [not-broadcast?]]
  (let [activate-logging? (fi/call-api :flags-db-get db frame-id :activate-logging?)
        project-loading? (get-in db ppath/project-loading)
        description (str description)]
    (when (and activate-logging?
               (not project-loading?))
      (if (and frame-id
               event-name)
        {:backend-tube [ws-api/log-event-route
                        {:origin origin
                         :frame-id frame-id
                         :event-name event-name
                         :description description
                         :version version
                         :not-broadcast? not-broadcast?}]}
        (do
          (error "Event logging call failed" {:frame-id frame-id
                                              :description description
                                              :event-name event-name})
          {})))))

(re-frame/reg-event-fx
 ::log-event
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id event-name description]]
   (log-event-fn db
                 config/default-vertical-str
                 frame-id
                 event-name
                 description
                 event-version)))

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(def event-vector-funcs
  "[event-name event-version]"
  {["create-frame" 1] no-event
   ["move-frame" 1] no-event
   ["resize-stop" 1] no-event
   ["maximize" 1] no-event
   ["minimize" 1] no-event
   ["normalize" 1] no-event
   ["close-frame" 1] no-event
   ["connect" 1] no-event
   ["create-connection" 1] no-event})

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (get event-vector-funcs [event-name event-version])]
     (debug "REPLAY" event-name " - " frame-id " -> " callback-vec)
     (if event-func
       (event-func db frame-id callback-vec parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         {:dispatch callback-vec})))))

(re-frame/reg-event-fx
 ::next-replay-event
 (fn [{db :db} [_ current-event rest-events events-done max-events done-event plogs-id]]
   {:db (assoc-in db ppath/replay-progress (/ events-done
                                              max-events))
    :dispatch (if current-event
                [::replay-event current-event
                 [::next-replay-event (first rest-events) (rest rest-events) (inc events-done) max-events done-event plogs-id]]
                done-event)}))

(re-frame/reg-event-fx
 ::replay-events
 (fn [{db :db} [_ events done-event plogs-id]]
   {:dispatch [::next-replay-event (first events) (rest events) 0 (count events) done-event plogs-id]}))

(re-frame/reg-event-fx
 ::ui-wrapper
 (fn [_ [_ frame-id event-name event event-params]]
   {:dispatch-n [event
                 [::log-event frame-id event-name event-params]]}))