(ns de.explorama.backend.common.environment.probe
  (:require [clojure.set :as set]
            [de.explorama.backend.woco.server-config :as config-server]
            [de.explorama.backend.common.environment.critical-exceptions :refer [exceptions]]))

(defn readiness []
  ;TOTO r1/server readiness
  true)

(defonce ^:private liveness-state (atom {:current true}))
(defn rate-exception [e]
  (cond ((set/union
          exceptions
          #{java.lang.OutOfMemoryError}) (type e))
        (swap! liveness-state assoc :current false)
        :else
        (swap! liveness-state (fn [{start-time :start-time :as state}]
                                (let [ct (System/currentTimeMillis)
                                      new-state (cond (not start-time)
                                                      (assoc state
                                                             :start-time ct
                                                             :count 1)
                                                      (<= (- ct start-time)
                                                          (.toMillis java.util.concurrent.TimeUnit/MINUTES 1))
                                                      (update state :count inc)
                                                      :else
                                                      (assoc state
                                                             :start-time ct
                                                             :count 1))]
                                  #_{:clj-kondo/ignore [:type-mismatch]}
                                  (if (<= config-server/explorama-liveness-exceptions-per-min
                                          (:count new-state))
                                    (assoc new-state :current false)
                                    new-state)))))
  nil)

(defn liveness []
  (:current @liveness-state))