(ns de.explorama.frontend.common.tracks
  "Dispatch events when a subscription's value changes.

  `::register` takes a map (or vector of maps) with
  `:id`           any value, used to dispose the track again
  `:subscription` a subscription vector
  `:event-fn`     fn of the subscription value returning an event vector to
                  dispatch, or nil for a no-op
  `:dispatch-first?` when false, skip the dispatch for the current value and
                  only dispatch on subsequent changes (defaults to true).

  `::dispose` takes a map (or vector of maps) with the `:id` to dispose."
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :as ratom]
            [taoensso.timbre :refer [debug]]))

(defonce ^:private tracks (atom {}))

(defn- reagent-track [{:keys [subscription event-fn dispatch-first?]
                       :or {dispatch-first? true}}]
  (let [dispatched-first? (atom false)]
    (ratom/track!
     (fn []
       (let [sub-value @(re-frame/subscribe subscription)]
         (when-some [event-vector (event-fn sub-value)]
           (when (or dispatch-first?
                     @dispatched-first?
                     (do (reset! dispatched-first? true) nil))
             (re-frame/dispatch event-vector))))))))

(defn- ensure-vec [x] (if (sequential? x) x [x]))

(defn- register-fx [track-or-tracks]
  (doseq [{:keys [id] :as track} (ensure-vec track-or-tracks)]
    (if (contains? @tracks id)
      (debug "Track already registered" id)
      (swap! tracks assoc id (reagent-track track)))))

(defn- dispose-fx [track-or-tracks]
  (doseq [{:keys [id]} (ensure-vec track-or-tracks)]
    (if-some [track (get @tracks id)]
      (do (ratom/dispose! track)
          (swap! tracks dissoc id))
      (debug "Track isn't registered" id))))

(re-frame/reg-fx ::register register-fx)
(re-frame/reg-fx ::dispose dispose-fx)
