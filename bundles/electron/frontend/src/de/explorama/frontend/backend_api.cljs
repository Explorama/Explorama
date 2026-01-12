(ns de.explorama.frontend.backend-api
  (:require [de.explorama.frontend.woco.app.backend :as backend]))

;;TODO r1/electron do we need a client-id?
(def tube-spec {})

(defn dispatch [event]
  (backend/send event)
  {})

(defn dispatch-n [events]
  (doseq [event events :when event]
    (dispatch event))
  {})

(defn init-tube [{db :db} [_ user-info after-fxs]]
  (when after-fxs
    {:fx after-fxs}))

(defn close-tube [_ [_ tube-events]]
  (doseq [event tube-events
          :when event]
    (dispatch event))
  {})
