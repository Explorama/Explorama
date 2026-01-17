(ns de.explorama.shared.abac.config
  (:require [de.explorama.shared.common.configs.provider :refer [defconfig]]))

(def explorama-filter-config-policies
  (defconfig
    {:env :explorama-filter-config-policies
     :default true
     :doc "Defines if the config-policies should be filtered based on a group-policy blacklist."
     :type :boolean}))

(def explorama-disable-policy-cache
  (defconfig
    {:env :explorama-disable-policy-cache
     :default false
     :doc "Activate/Deactivate the policy cache (true => deactivated)."
     :type :boolean}))

(def explorama-shared-secret-key
  (defconfig
    {:env :explorama-shared-secret-key
     :default "6NSAks+V*g9NL:,F,?61]yvrW&@gpK>i!reyI]+V:m_#XjsPgG:eI]OXU2ZhIp-y"
     :doc "Shared secret-key to encrypt/decrpyt diffrent data before send somewhere else."
     :type :string}))

(def explorama-token-experation-hours
  (defconfig
    {:env :explorama-token-experation-hours
     :default 48
     :doc "Defines how long a explorama token is valid in hours."
     :type :integer}))

(def explorama-admin-token-experation-hours
  (defconfig
    {:env :explorama-admin-token-experation-hours
     :default 2
     :doc "Defines how long a admin token is valid in hours."
     :type :integer}))

(def explorama-roles-mapping-config
  (defconfig
    {:env :explorama-roles-mapping-config
     :fallback {}
     :default [:config-dir "roles-mapping.edn"]
     :doc "Defines how long a admin token is valid in hours."
     :type :edn}))

(def explorama-abac-config
  (defconfig
    {:env :explorama-abac-config
     :default nil
     :post-read-fn (fn [v]
                     (assoc v
                            :key-path (str "/policies")))
     :type :edn-string
     :doc "Sets the backend for the rights-and-roles management where the policies are saved.
         See the system manual for more infos."}))
