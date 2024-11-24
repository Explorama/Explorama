(ns de.explorama.frontend.woco.util.api
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :refer [warn]]))

(defonce ^:private errors (atom #{}))

(defn event-error-boundary [event-vec & vec-params]
  (if (vector? event-vec)
    (apply conj event-vec vec-params)
    (when-not (@errors [:event-error event-vec])
      (warn "Event-vec not available" event-vec)
      (swap! errors conj [:event-error event-vec])
      [::no-op])))

(defn sub-error-boundary [subscription-vec & vec-params]
  (if-let [sub-vector @(re-frame/subscribe subscription-vec)]
    (re-frame/subscribe (apply conj sub-vector vec-params))
    (when-not (@errors [:sub-error subscription-vec])
      (warn "subscription not available" subscription-vec)
      (swap! errors conj [:sub-error subscription-vec])
      (atom nil))))

(defn db-get-error-boundary [db db-get-fn warn-identifier & fn-params]
  (if (fn? db-get-fn)
    (apply db-get-fn db fn-params)
    (when-not (@errors [:warn warn-identifier])
      (warn "db-get not available" warn-identifier)
      (swap! errors conj [:warn warn-identifier])
      nil)))