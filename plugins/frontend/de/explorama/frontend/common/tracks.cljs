(ns de.explorama.frontend.common.tracks
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :refer [info]]
            [vimsical.re-frame.fx.track :as track]))


(defn- register-fx
  [track-or-tracks]
  (try
    (track/register-fx track-or-tracks)
    (catch js/Error
           e
      (info e)
      {})))

(defn- dispose-fx
  [track-or-tracks]
  (try
    (track/dispose-fx track-or-tracks)
    (catch js/Error
           e
      (info e)
      {})))

(re-frame/reg-fx ::register register-fx)
(re-frame/reg-fx ::dispose dispose-fx)