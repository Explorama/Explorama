(ns de.explorama.backend.expdb.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :expdb)
(def plugin-string (name plugin-key))

(def explorama-expdb-cache-size
  (defconfig
    {:env :explorama-expdb-cache-size
     :type :integer
     :default 150000
     :doc "How big the cache for the data should be.
         If the size is reached older and not used keys will be removed to make space."}))

(def explorama-expdb-cache
  (defconfig
    {:env :explorama-expdb-cache
     :type :keyword
     :default :lru
     :possible-values #{:lru :fifo :lu :lirs}
     :doc "Defines the cache type that is used."}))

(def explorama-bucket-config
  (defconfig
    {:env :explorama-bucket-config
     :default {}
     :type :edn-string
     :doc "Bucket configuration"}))
