(ns de.explorama.frontend.knowledge-editor.canvas.tracks
  (:require [de.explorama.frontend.knowledge-editor.path :as path]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [vimsical.re-frame.fx.track :as track]))

(defn register-fx
  [track-or-tracks]
  (try
    (track/register-fx track-or-tracks)
    (catch js/Error
           e
      (debug e)
      {})))

(defn dispose-fx
  [track-or-tracks]
  (try
    (track/dispose-fx track-or-tracks)
    (catch js/Error
           e
      (debug e)
      {})))

(re-frame/reg-fx ::register register-fx)
(re-frame/reg-fx ::dispose dispose-fx)

(re-frame/reg-sub
 ::canvas
 (fn [db [_ frame-id]]
   (get-in db (path/canvas-content frame-id))))

(re-frame/reg-event-fx
 ::reg-track
 (fn [_ [_ frame-id]]
   {::register
    {:id [::canvas frame-id]
     :subscription [::canvas frame-id]
     :event-fn (fn [_]
                 [:goose.render.core/update frame-id])}}))

(re-frame/reg-event-fx
 ::dispose-track
 (fn [_ [_ frame-id]]
   {::dispose
    {:id [::canvas frame-id]}}))
