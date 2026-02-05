(ns de.explorama.frontend.woco.app.backend
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error info trace]]))

(defn- add-meta-data [[event-name metas & params :as event-vec]]
  (let [{:keys [client-callback failed-callback broadcast-callback]} (when (map? metas) metas)
        has-metas? (and (map? metas)
                        (or client-callback failed-callback broadcast-callback))
        ;; TODO no subs here
        user-info (val-or-deref (fi/call-api :user-info-sub))
        client-id (val-or-deref (fi/call-api :client-id-sub))
        params (if has-metas?
                 params
                 (rest event-vec))
        metas (-> (if has-metas?
                    metas
                    {})
                  (assoc :user-info user-info
                         :client-id client-id))]
    (apply conj
           [event-name metas]
           params)))

(defn listen [e]
  (let [event (edn/read-string e)]
    (trace "Result from backend" {:event event})
    (when (vector? event)
      (re-frame/dispatch event))))

(defn send [event]
  (let [event (add-meta-data event)]
    (trace "Send to worker" {:event event})
    (try
      ((aget js/window "electronAPI" "sendToWorker")
       (str event))
      (catch :default e
        (error e "Failed to send to worker" event))))) ;(select-keys event [:request-api :api]))))))

(defn- init-backend []
  ((aget js/window "electronAPI" "workerRequestHandler")
   listen))

(defn init []
  (info "Starting backend..")
  (init-backend))
