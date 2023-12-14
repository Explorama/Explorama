(ns de.explorama.backend.indicator.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]
            [de.explorama.backend.indicator.defaults :as defaults]
            [de.explorama.shared.indicator.config :as config-shared]))

(def explorama-indicator-desc-validation
  (defconfig
    {:env :explorama-indicator-desc-validation
     :type :boolean
     :default false
     :doc "Sets if malli checks should be used or not.
         Should be false in production-mode."}))


(def explorama-indicator-data-sample-size-each-di
  (defconfig
    {:env :explorama-indicator-data-sample-size-each-di
     :type :integer
     :default 500
     :doc "Sets the amount of data for the sample generation during
         indicator development for each-di"}))


(def explorama-indicator-data-sample-size
  (defconfig
    {:env :explorama-indicator-data-sample-size
     :type :integer
     :default 30
     :doc "Sets the amount of data for the sample generation during
         indicator development for the whole dataset."}))

(def explorama-indicator-cache-size
  (defconfig
    {:env :explorama-indicator-cache-size
     :type :integer
     :default 150000
     :doc "How big the cache for the data should be.
         If the size is reached older and not used keys will be removed to make space."}))


(def explorama-indicator-cache
  (defconfig
    {:env :explorama-indicator-cache
     :type :keyword
     :default :lru
     :possible-values #{:lru :fifo :lu :lirs}
     :doc "Defines the cache type that is used."}))


(def explorama-indicator-ui
  (defconfig
    {:fallback defaults/indicator-ui-descriptions
     :env :explorama-indicator-ui
     :type :edn-file
     :default [:config-dir "/" (name config-shared/plugin-key) "/indicator-types.edn"]
     :doc "Configures available indicator types."}))
