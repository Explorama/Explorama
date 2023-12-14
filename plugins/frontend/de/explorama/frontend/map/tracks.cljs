(ns de.explorama.frontend.map.tracks
  "This namespace is a clone of functionality in `mosaic.render.tracks`, and it implements a workaround to catch
  problems with `vimsical.re-frame.fx.track` throwing an exception."
  ;; TODO: Move to some generic explorama.re-frame.utils namespace in some generic explorama/frontend project.
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