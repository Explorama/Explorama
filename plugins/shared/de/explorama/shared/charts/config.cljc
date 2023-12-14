(ns de.explorama.shared.charts.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :charts)
(def plugin-string (name plugin-key))

(def explorama-charts-chartjs-selectlimit
  (defconfig
    {:env :explorama-charts-chartjs-selectlimit
     :default 10
     :type :integer
     :doc "Defines how many diffrent characteristisc the user can select."}))

(def explorama-charts-max-multiple
  (defconfig
    {:env :explorama-charts-max-multiple
     :type :integer
     :default 2
     :doc "How many charts can be displayed in one window"}))