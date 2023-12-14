(ns de.explorama.frontend.projects.tracks
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :refer [log debug trace info]]
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
