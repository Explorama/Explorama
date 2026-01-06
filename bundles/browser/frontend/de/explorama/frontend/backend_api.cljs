(ns de.explorama.frontend.backend-api
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [taoensso.timbre :refer [debug info warn]]))

(def failed-send-timeout 1000)

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

(defn send [event]
  (let [event (add-meta-data event)]
    (if-let [send-to-frontend (-> (aget js/self "frontendAPI")
                                  :listener)]
      (send-to-frontend event)
      (do
        (js/setTimeout (fn []
                         (info "[frontend] Try to send again" {:event event})
                         (send event))
                       failed-send-timeout)
        (warn "[frontend] Send failed - Backend is not available" {:event event})))))

(defn listen [e]
  (let [event e]
    (when (vector? event)
      (re-frame/dispatch event))))

(defn dispatch [event]
  (js/setTimeout (fn []
                   (send event))
                 5)
  {})

(defn dispatch-n [events]
  (doseq [event events :when event]
    (js/setTimeout (fn []
                     (send event))
                   5))
  {})

(defn init-tube [_ [_ _user-info after-fxs]]
  (when after-fxs
    {:fx after-fxs}))

(defn close-tube [_ [_ tube-events]]
  (doseq [event tube-events
          :when event]
    (dispatch event))
  {})

(defn init []
  (debug "[frontend] Init backend API")
  (aset js/self "backendAPI"
        {:send send
         :listener listen}))
