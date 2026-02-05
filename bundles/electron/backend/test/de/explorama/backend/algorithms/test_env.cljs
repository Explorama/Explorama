(ns de.explorama.backend.algorithms.test-env
  (:require [de.explorama.backend.algorithms.algorithm :as alg]
            [de.explorama.backend.algorithms.registry :as registry]
            [de.explorama.backend.algorithms.regression.lr :as lr]))

(let [lr-instance (lr/create-instance)]
  (registry/register-algorithm (alg/algorithm-key lr-instance) lr-instance))