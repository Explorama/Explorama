(ns de.explorama.shared.projects.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def plugin-key :projects)
(def plugin-string (name plugin-key))

(def explorama-direct-search-open
  (defconfig
    {:env :explorama-direct-search-open
     :default true
     :type :boolean
     :doc "Defines if the project should also be opened or only added to the overview."}))

(def explorama-projects-unlock-grace-period
  (defconfig
    {:env :explorama-projects-unlock-grace-period
     :type :integer
     :default 60000
     :use-default-on-fail? true
     :doc "Grace period in ms for the unlocking in case of a connection loss"}))


(def explorama-update-steps-tick-rate-minutes
  (defconfig
    {:env :explorama-update-steps-tick-rate-minutes
     :type :integer
     :default 5
     :doc "Defines how often in minutes the steps count should be updated for all projects."}))

; For information have a look projects-test-runner/README.md

; automate tests ->
(def explorama-automate-tests-enabled
  (defconfig
    {:env :explorama-automate-tests-enabled
     :default false
     :type :boolean
     :doc "Enables automated testing based on projects"}))

(def explorama-automate-tests-opt-release
  (defconfig
    {:env :explorama-automate-tests-opt-release
     :type :string
     :doc "explorama release version"}))

(def explorama-automate-tests-opt-test-name
  (defconfig
    {:env :explorama-automate-tests-opt-test-name
     :type :string
     :doc "Optional autmate test name"}))

(def client-configs
  [:explorama-automate-tests-enabled])

(def explorama-projects-workspace-cleanup-days
  (defconfig
    {:env :explorama-projects-workspace-cleanup-days
     :default 5
     :type :integer
     :doc "The days after which unused workspace keys from redis should be cleaned up."}))

(def explorama-projects-workspace-cleanup-frequency
  (defconfig
    {:env :explorama-projects-workspace-cleanup-frequency
     :default 1
     :type :integer
     :doc "The frequency (in hours) at which the unused workspaces should be cleaned up."}))
