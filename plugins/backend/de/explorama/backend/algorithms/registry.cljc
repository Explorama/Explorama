(ns de.explorama.backend.algorithms.registry
  (:require [de.explorama.backend.algorithms.algorithm :as algorithm]))

(def ^:private algorithms (atom {}))

(defn all-algorithms []
  (set (keys @algorithms)))

(defn register-algorithm [algorithm-name algorithm]
  (swap! algorithms assoc algorithm-name algorithm))

(defn make-model [algorithm desc]
  (algorithm/create-model (get @algorithms algorithm) desc))

(defn execute-model [algorithm desc]
  (algorithm/execute-model (get @algorithms algorithm) desc))
