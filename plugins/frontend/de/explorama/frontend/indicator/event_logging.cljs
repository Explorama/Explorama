(ns de.explorama.frontend.indicator.event-logging
  (:require [de.explorama.frontend.common.event-logging.util :as log-util]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.indicator.config :as config]
            [de.explorama.frontend.indicator.event-replay :as e-replay]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(def event-version 1.0)

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ _]
   #_{:dispatch [:woco.log/info (str "Event-logging success" resp)]}
   {}))

(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ [_ resp]]
   {:dispatch (fi/call-api :error-event-vec (str "Event-logging failed" resp))}))

(defn- fake-frame-id [db]
  {:workspace-id (fi/call-api :workspace-id-db-get db)
   :vertical config/default-vertical-str
   :frame-id (str config/default-vertical-str "-" (random-uuid))})

(re-frame/reg-event-fx
 ::log-event
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ event-name description]]
   (when-let [log-fn (fi/call-api :service-target-db-get db :project-fns :event-log)]
     (log-fn db
             config/default-vertical-str
             (fake-frame-id db)
             event-name
             description
             event-version))))

(re-frame/reg-event-fx
 ::ui-wrapper
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id event-name event-params]]
   (if-let [event-func (e-replay/event-func event-name)]
     (merge-with into
                 (event-func db frame-id nil event-params)
                 {:dispatch-n [[:de.explorama.frontend.map.event-logging/log-event frame-id event-name event-params]]})
     (do
       (debug "no event-function found for " [event-name event-version])
       nil))))

(defn- action-desc [_base-op action _both _only-old _only-new]
  (case action
    nil))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))