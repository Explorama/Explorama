(ns de.explorama.frontend.algorithms.event-logging
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.algorithms.components.goal :as goal]
            [de.explorama.frontend.algorithms.components.helper :as helper]
            [de.explorama.frontend.algorithms.config :as config]
            [de.explorama.frontend.algorithms.path.core :as paths]
            [de.explorama.frontend.common.event-logging.util :as log-util :refer [base-desc]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.algorithms.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [info debug error]]))

(def event-version 1.0)

(defn is-ki? [frame-id]
  (= config/default-vertical
     (:vertical frame-id)))

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ resp]
   (info "Event-logging success" resp)
   {}))

(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ resp]
   (error "Event-logging failed" resp)
   {}))

(re-frame/reg-event-fx
 ::log-event
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id event-name description]]
   (when-let [log-fn (fi/call-api :service-target-db-get db :project-fns :event-log)]
     (log-fn db
             config/default-vertical-str
             frame-id
             event-name
             description
             event-version))))

(re-frame/reg-event-fx
 ::log-pseudo-init
 (fn [{db :db} _]
   (let [workspace-id (fi/call-api :workspace-id-db-get db)
         pseudo-frame-id {:workspace-id workspace-id
                          :vertical "algorithms"}]
     {:dispatch [::log-event pseudo-frame-id "init-ki" nil]})))

(defn check-frame-id [frame-id callback-vec body]
  (if (is-ki? frame-id)
    body
    (do (debug "ignore event " frame-id)
        {:fx [(when callback-vec
                [:dispatch callback-vec])]})))

(defn connect-event [db _ callback-vec {{:keys [di]} :connection-data
                                        :keys [frame-target-id]
                                        :as data}]
  (debug :connect-event di frame-target-id data)
  (let [task-id (str (random-uuid))]
    (check-frame-id
     frame-target-id
     callback-vec
     (if di
       {:db (-> db
                (assoc-in (paths/data-instance-consuming frame-target-id) di)
                (assoc-in (paths/loading frame-target-id) true)
                (assoc-in (paths/connect-task-id frame-target-id) task-id))
        :backend-tube [ws-api/data-options
                       {:client-callback [ws-api/data-options-result frame-target-id true callback-vec task-id]}
                       di]}
       {:fx [(when callback-vec
               [:dispatch callback-vec])]}))))

(re-frame/reg-event-fx
 ::merge-callbacks
 (fn [{db :db} [_ identifier callback-vec]]
   (let [new-replay-frames-hit-callback-tracker (disj (get-in db paths/replay-frames-hit-callback-tracker) identifier)]
     (if (empty? new-replay-frames-hit-callback-tracker)
       {:db (paths/dissoc-in db paths/replay-frames-hit-callback-tracker)
        :fx [(when callback-vec
               [:dispatch callback-vec])]}
       {:db (assoc-in db paths/replay-frames-hit-callback-tracker
                      new-replay-frames-hit-callback-tracker)}))))

(defn close-event [db frame-id callback-vec _]
  (check-frame-id
   frame-id
   callback-vec
   {:db (paths/clean-frames db [frame-id])
    :fx [(when callback-vec
           [:dispatch callback-vec])]}))

(defn create-frame [db frame-id callback-vec params]
  (check-frame-id
   frame-id
   callback-vec
   {:db (-> (assoc-in db (paths/frame frame-id) {}))
    :dispatch [:de.explorama.frontend.algorithms.view/view-event :frame/init (assoc params
                                                                                    :callback-event callback-vec
                                                                                    :frame-id frame-id
                                                                                    :replay? true)]}))

(defn load-prediction-event [db frame-id callback-vec {:keys [pred-id]}]
  (check-frame-id
   frame-id
   callback-vec
   {:dispatch [:de.explorama.frontend.algorithms.components.main/load-prediction frame-id pred-id true callback-vec]}))

(defn ui-value-changed-event [db frame-id callback-vec {:keys [path state-key value map? multi-select?]}]
  (let [path (into (case state-key
                     :goal (paths/goal-state frame-id)
                     :settings (paths/settings-state frame-id)
                     :parameter (paths/parameter-state frame-id)
                     :simple-parameter (paths/simple-parameter-state frame-id)
                     :future-data (paths/future-data-state frame-id))
                   path)
        translate-function (partial i18n/translate db)]
    (check-frame-id
     frame-id
     callback-vec
     {:db (assoc-in db path (cond (and map?
                                       (= [:choose-algorithm] path)
                                       (= state-key :goal))
                                  (goal/icon-option [value {:requirements true}] translate-function identity)
                                  map?
                                  (helper/keyword-translate-option value translate-function)
                                  :else value))
      :fx [(when callback-vec
             [:dispatch callback-vec])]})))

(defn submit-task-event [_ frame-id callback-vec {di :di}]
  (check-frame-id
   frame-id
   callback-vec
   {:backend-tube [ws-api/predict
                   {:client-callback [ws-api/predict-result frame-id true callback-vec]
                    :custom {:predicting-event [:de.explorama.frontend.algorithms.components.main/predicting frame-id]}}
                   {:pred-di di}]}))

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(def event-vector-funcs
  "[event-name event-version]"
  {["create-frame" 1] create-frame
   ["connect" 1] connect-event
   ["connect-code" 1] connect-event
   ["load-prediction" 1] load-prediction-event
   ["ui-value-changed" 1] ui-value-changed-event
   ["submit-task" 1] submit-task-event
   ["move-frame" 1] no-event
   ["resize-stop" 1] no-event
   ["maximize" 1] no-event
   ["minimize" 1] no-event
   ["normalize" 1] no-event
   ["close-frame" 1] close-event})

(defn event-func
  ([event-name]
   (event-func event-name 1))
  ([event-name event-version]
   (get event-vector-funcs [event-name event-version])))

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "replay-event ki" {:frame-id frame-id
                               :event-name event-name
                               :callback-vec callback-vec
                               :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id callback-vec parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
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
     {:db (assoc-in db paths/replay-progress (if (> max-events 0)
                                               (/ events-done
                                                  max-events)
                                               0))
      :dispatch (if current-event
                  [::replay-event current-event
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
                                 is-ki?
                                 (constantly false)
                                 (constantly false)))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "sync-event ki" {:frame-id frame-id
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
     {:dispatch [::next-replay-event
                 (first events)
                 (rest events)
                 0
                 (count events)
                 done-event
                 ignored-frames
                 plogs-id
                 (when test-and-profile? test-and-profile?)]})))

(defn action-desc [base-op action both only-old only-new]
  (case action
    "create-frame" (base-desc :search-protocol-action-create-window)
    "connect" (base-desc :algorithms-protocol-action-connect)
    "submit-task" (base-desc :algorithms-protocol-action-submit-task)
    nil))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))

(re-frame/reg-event-fx
 ::ui-wrapper
 (fn [_ [_ frame-id event-name event-params]]
   (if (event-vector-funcs [event-name event-version])
     {:dispatch-n [[::log-event frame-id event-name event-params]]}
     (do
       (debug "no event-function found for " [event-name event-version])
       nil))))
