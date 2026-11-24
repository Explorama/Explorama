(ns de.explorama.frontend.woco.util.api
  (:require [de.explorama.frontend.woco.api.registry :as registry]
            [re-frame.core :as re-frame]
            [re-frame.db :as rf-db]
            [reagent.ratom :as ratom]
            [taoensso.timbre :refer [warn]]))

(defonce ^:private errors (atom #{}))

(defn event-error-boundary [event-vec & vec-params]
  (if (vector? event-vec)
    (apply conj event-vec vec-params)
    (when-not (@errors [:event-error event-vec])
      (warn "Event-vec not available" event-vec)
      (swap! errors conj [:event-error event-vec])
      [::no-op])))

(defn sub-error-boundary [category service & vec-params]
  (if-let [sub-vector (registry/lookup-target @rf-db/app-db category service)]
    (re-frame/subscribe (apply conj sub-vector vec-params))
    (when-not (@errors [:sub-error category service])
      (warn "subscription not available" category service)
      (swap! errors conj [:sub-error category service])
      (atom nil))))

(defn read-derefable
  "One-shot imperative read of a plugin-api fn that returns a derefable.
  The fn runs inside a short-lived reaction, so subscriptions it creates are
  made in a reactive context; the reaction is disposed right after."
  [derefable-fn & args]
  (when (fn? derefable-fn)
    (let [value (volatile! nil)
          rx (ratom/run! (vreset! value (some-> (apply derefable-fn args) deref)))]
      (ratom/dispose! rx)
      @value)))

(defn db-get-error-boundary [db db-get-fn warn-identifier & fn-params]
  (if (fn? db-get-fn)
    (apply db-get-fn db fn-params)
    (when-not (@errors [:warn warn-identifier])
      (warn "db-get not available" warn-identifier)
      (swap! errors conj [:warn warn-identifier])
      nil)))