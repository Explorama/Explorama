(ns de.explorama.shared.mosaic.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :mosaic)
(def plugin-string (name plugin-key))

(def explorama-fallback-layout-color
  (defconfig
    {:env :explorama-fallback-layout-color
     :type :string
     :default "#ffffff"
     :doc "Fallback color which will be used when a data-point has no matching color for any selected layout."}))
