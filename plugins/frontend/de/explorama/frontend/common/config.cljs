(ns de.explorama.frontend.common.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-retry-update-user-info-timeout
  (defconfig
    {:name :explorama-retry-update-user-info-timeout
     :type :integer
     :default 5000
     :scope :client
     :doc "Defines if the application is running in multi user mode."}))

(def explorama-max-user-info-updates-tries
  (defconfig
    {:name :explorama-max-user-info-updates-tries
     :type :integer
     :default 60
     :scope :client
     :doc "Defines if the application is running in multi user mode."}))
