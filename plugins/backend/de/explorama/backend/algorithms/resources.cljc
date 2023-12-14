(ns de.explorama.backend.algorithms.resources
  (:require [de.explorama.backend.algorithms.resources.algorithm-mapping :as algorithm-mapping]
            [de.explorama.backend.algorithms.resources.algorithms :as algorithms]
            [de.explorama.backend.algorithms.resources.problem-types :as problem-types]
            [de.explorama.backend.algorithms.resources.quality-measures :as quality-measures]))

(def algorithm-mapping-config algorithm-mapping/value)

(def problem-types-config problem-types/value)

(def quality-measures-config quality-measures/value)

(def algorithms-config algorithms/value)
