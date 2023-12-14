(ns de.explorama.backend.algorithms.config
  (:require [de.explorama.shared.algorithms.config :as config-shared-algorithms]
            [de.explorama.backend.algorithms.resources :as resources]
            [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-plugin-key
  (defconfig
    {:env :explorama-plugin-key
     :default :algorithms
     :doc "An explorama-wide unique key for this plugin."
     :type :keyword}))

;; Cache for predictions (task, di) -> forecast data
(def explorama-prediction-cache-size
  (defconfig
    {:env :explorama-prediction-cache-size
     :type :integer
     :default 1000
     :doc "How big the cache for the preticted data should be.
         If the size is reached older and not used keys will be removed to make space based on the explorama-prediction-cache rule."}))

(def explorama-prediction-cache
  (defconfig
    {:env :explorama-prediction-cache
     :type :keyword
     :default :lru
     :possible-values #{:lru :fifo :lu :lirs}
     :doc "Defines the cache type that is used for the prediction data cache."}))

;; Cache for training data (task, di) -> training data
(def explorama-algorithm-work-cache-size
  (defconfig
    {:env :explorama-algorithm-work-cache-size
     :type :integer
     :default 100
     :doc "How big the cache for the training data should be.
         If the size is reached older and not used keys will be removed to make space based on the explorama-work-cache rule."}))

(def explorama-algorithm-work-cache
  (defconfig
    {:env :explorama-algorithm-work-cache
     :type :keyword
     :default :lru
     :possible-values #{:lru :fifo :lu :lirs}
     :doc "Defines the cache type that is used."}))

(def explorama-default-date
  (defconfig
    {:env :explorama-default-date
     :default "0000-01-01"
     :type :string
     :doc "Fallback date when the predicted data has no date."}))

(def explorama-default-rounding-decimal-place
  (defconfig
    {:env :explorama-default-rounding-decimal-place
     :default 6
     :type :string
     :doc "Decimal precision when rounding the predicted value."}))

(def explorama-algorithms-algorithms-config
  (defconfig
    {:name :explorama-algorithms-algorithms-config
     :fallback resources/algorithms-config
     :default [:config-dir (name config-shared-algorithms/plugin-key) "algorithms.edn"]
     :type :edn-file
     :doc "TODO"}))

(def explorama-algorithms-problem-types-config
  (defconfig
    {:name :explorama-algorithms-problem-types-config
     :fallback resources/problem-types-config
     :default [:config-dir (name config-shared-algorithms/plugin-key) "problem-types.edn"]
     :type :edn-file
     :doc "TODO"}))

(def explorama-algorithms-algorithm-mapping-config
  (defconfig
    {:name :explorama-algorithms-algorithm-mapping-config
     :fallback resources/algorithm-mapping-config
     :default [:config-dir (name config-shared-algorithms/plugin-key) "algorithm-mapping.edn"]
     :type :edn-file
     :doc "TODO"}))

(def explorama-algorithms-quality-measures-config
  (defconfig
    {:name :explorama-algorithms-quality-measures-config
     :fallback resources/quality-measures-config
     :default [:config-dir (name config-shared-algorithms/plugin-key) "quality-measures.edn"]
     :type :edn-file
     :doc "TODO"}))

(def explorama-enforce-merge-policy-defaults
  (defconfig
    {:env :explorama-enforce-merge-policy-defaults
     :type :keyword
     :possible-values #{:enforce :ui}
     :default :enforce
     :doc "Decide what the merge policy for defaults should be."}))

(def explorama-algorithms-max-data-amount
  (defconfig
    {:env :explorama-algorithms-max-data-amount
     :type :integer
     :default 400000
     :doc "Defines the maximum amount of data that can be used for algorithms."}))
