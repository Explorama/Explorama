(ns de.explorama.backend.data-atlas.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :data-atlas)
(def plugin-string (name plugin-key))

(def explorama-top-characteristics-max
  (defconfig
    {:env :explorama-top-characteristics-max
     :type :integer
     :default 100000
     :doc "Set the maximum visible characteristics in the ui."}))


(def explorama-top-characteristics
  (defconfig
    {:env :explorama-top-characteristics
     :type :integer
     :default 1000
     :doc "Limit when querying the neighborhood from the ac-graph."}))


(def explorama-data-atlas-cache-size
  (defconfig
    {:env :explorama-data-atlas-cache-size
     :type :integer
     :default 10000
     :doc "How big the cache for the data should be.
         If the size is reached older and not used keys will be removed to make space."}))
