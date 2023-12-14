(ns de.explorama.frontend.mosaic.operations.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug info]]
            [vimsical.re-frame.fx.track :as track]))


(defn register-fx
  [track-or-tracks]
  (try
    (track/register-fx track-or-tracks)
    (catch js/Error e
      (info e track-or-tracks)
      {})))

(defn dispose-fx
  [track-or-tracks]
  (try
    (track/dispose-fx track-or-tracks)
    (catch js/Error e
      (info e track-or-tracks)
      {})))

(re-frame/reg-fx ::register register-fx)
(re-frame/reg-fx ::dispose dispose-fx)

;! TODO move this to better namespace
(re-frame/reg-sub
 ::canvas
 (fn [db [_ path]]
   (get-in db (gp/canvas path))))

(re-frame/reg-event-fx
 ::track-register-canvas-rendering
 (fn [_ [_ path]]
   {::register
    {:id [::canvas (gp/frame-id path)]
     :subscription [::canvas path]
     :event-fn (fn [_]
                 [:de.explorama.frontend.mosaic.render.core/init-headless path])}}))

(re-frame/reg-event-fx
 ::track-dispose-canvas-rendering
 (fn [_ [_ path]]
   {::dispose
    {:id [::canvas (gp/frame-id path)]}}))

(re-frame/reg-event-db
 ::on-canvas-exit
 (fn [db [_ path dispatch-n]]
   (assoc-in db
             (gp/on-frame-exit path)
             dispatch-n)))

(re-frame/reg-sub
 ::exit-frame
 (fn [db [_ path]]
   (get-in db
           (gp/on-frame-exit path))))

(re-frame/reg-event-fx
 ::canvas-exit
 (fn [{db :db} [_ path]]
   (let [dispatch (or (get-in db (gp/on-frame-exit path))
                      [(fi/call-api :error-event-vec (str "canvas-exit execution failed dispatch-n is " path))])]
     (debug "canvas exit - " (gp/on-frame-exit path))
     {:db (gp/dissoc-in db (gp/on-frame-exit path))
      :dispatch-n (conj dispatch
                        [:de.explorama.frontend.mosaic.event-logging/execute-on-exit-callback-vec (gp/frame-id path)])})))
