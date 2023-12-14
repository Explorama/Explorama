(ns de.explorama.shared.reporting.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :reporting)
(def plugin-string (name plugin-key))

(def explorama-system-name
  (defconfig
    {:env :explorama-system-name
     :default "Explorama"
     :type :string
     :possible-values #{"Explorama"}
     :doc "What system it should be."}))
