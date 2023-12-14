(ns de.explorama.backend.common.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-datasource-access-control-enabled
  (defconfig
    {:env :explorama-datasource-access-control-enabled
     :type :boolean
     :default false
     :doc "Defines if datasource access-control is active/deactivated."}))

(def explorama-enabled-datasources-by-name
  (defconfig
    {:env :explorama-enabled-datasources-by-name
     :type :edn-string
     :default {}
     :doc "Give a specific user based on username access to a list of datasources."}))

(def explorama-enabled-datasources-by-role
  (defconfig
    {:env :explorama-enabled-datasources-by-role
     :type :edn-string
     :default {}
     :doc "Give a specific role access to a list of datasources."}))