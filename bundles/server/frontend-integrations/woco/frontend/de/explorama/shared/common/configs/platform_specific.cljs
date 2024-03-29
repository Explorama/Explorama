(ns de.explorama.shared.common.configs.platform-specific
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-multi-user
  (defconfig
    {:name :explorama-multi-user
     :type :boolean
     :default false
     :scope :all
     :doc "Defines if the application is running in multi user mode."}))

(def explorama-origin
  (defconfig ;TODO r1/config make this proxy url?
    {:name :explorama-origin
     :type :string
     :default "127.0.0.1:4001"
     :scope :client
     :overwritable? true
     :doc "Defines if the application is running in multi user mode."}))

(def explorama-asset-origin
  (defconfig ;TODO r1/config make this proxy url?
    {:name :explorama-asset-origin
     :type :string
     :default ""
     :scope :client
     :overwritable? true
     :doc "Defines if the application is running in multi user mode."}))
