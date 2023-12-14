(ns de.explorama.profiling-tool.env
  (:require [de.explorama.backend.frontend-api :as frontend-api]))

(def ^{:doc "Maximum number of attempts to run finalisers and gc."
       :dynamic true
       :private true}
  *max-gc-attempts* 100)

(defn force-gc
  "Force garbage collection and finalisers so that execution time associated
   with this is not incurred later. Up to max-attempts are made."
  ([] (force-gc *max-gc-attempts*))
  ([_]))

(defn thread-wait [_])

(defn get-software-version []
  "R1") ;TODO

(def bench-bucket :default)

(defn wait-for-result [state & [key]]
  (if key
    (let [result (get @state key)]
      (swap! state dissoc key)
      result)
    (let [result (:result @state)]
      (swap! state dissoc :result)
      result)))

(defn send-fn [state]
  (fn [& params]
    (with-redefs [frontend-api/dispatch (fn [result]
                                          (swap! state
                                                 assoc
                                                 (first result)
                                                 (rest result)))]
      (apply frontend-api/request-listener params))))

(defn create-ws-with-user [vertical state]
  {:send-fn (send-fn state)
   :result-atom state
   :close-fn (fn [])})
