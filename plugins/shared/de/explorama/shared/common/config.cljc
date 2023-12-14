(ns de.explorama.shared.common.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-profile-time
  (defconfig
    {:env :explorama-profile-time
     :default false
     :type :boolean
     :doc "Deactivate/Activate profiling."}))

(def explorama-project-sync?
  (defconfig
    {:name :explorama-project-sync?
     :type :boolean
     :default false
     :scope :all
     :doc "Defines if the application is running in multi user mode."}))

(def explorama-normalize-username
  (defconfig
    {:env :explorama-normalize-username
     :default true
     :doc "If true the username gets trimmed (whitespaces from start and end removed) and transformed to lower-case.
         This is needed when the returned usernames from the auth-server are always completly in lower-case."
     :type :boolean}))
