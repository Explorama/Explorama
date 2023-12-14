(ns de.explorama.backend.algorithms.algorithm)

(defprotocol Algorithm
  (algorithm-key [this])
  (create-model [this params])
  (execute-model [this params]))
