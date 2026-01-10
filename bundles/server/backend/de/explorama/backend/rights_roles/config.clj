(ns de.explorama.backend.rights-roles.config
  (:require [clojure.edn :as edn]
            [de.explorama.backend.rights-roles.crypto-util :as cryp-util]
            [de.explorama.shared.common.configs.provider :refer [defconfig]]
            [de.explorama.shared.common.configs.provider-impl :refer [config-dir]])
  (:import java.util.concurrent.TimeUnit))

(def system-name
  (defconfig
    {:env :explorama-system-name
     :default "explorama"
     :type :string
     :possible-values #{"explorama"}
     :doc "What system it should be."}))

(def environment
  (defconfig
    {:env :explorama-environment
     :default "PRODUCTION"
     :type :string
     :possible-values #{"PRODUCTION" "TRIAL"}
     :doc "In what environment the system is running."}))

(def show-legal? (= environment "TRIAL"))
(def show-accessibility? (= environment "PRODUCTION"))

(def run-dir
  (defconfig
    {:env :explorama-rundir
     :type :string
     :default "."
     :doc "Where the release is running from"}))

(def assets-folder
  (defconfig
    {:env :explorama-assets
     :type :string
     :default (str run-dir "/assets")
     :doc "Where the assets are."}))

(def asset-folder-css-path
  (defconfig
    {:env :explorama-css-assets
     :type :string
     :default (str assets-folder "/style")
     :doc "Where the css files are."}))

(def legal-folder
  (defconfig
    {:env :explorama-legal-assets
     :type :string
     :default (str assets-folder "/legal")
     :doc "Where the legal files are."}))

(def token-experation
  (defconfig
    {:env :explorama-token-experation-hours
     :default 48
     :type :integer
     :post-read-fn (fn [v]
                     (.toSeconds TimeUnit/HOURS v))
     :doc "Defines how long a explorama token is valid in hours."}))

(defn- decrypt-conf [conf]
  (let [{:keys [content initv secret]} conf]
    (if (and content initv secret conf)
      (-> (cryp-util/aes-decrypt content
                                 (cryp-util/decrypt secret)
                                 (cryp-util/decrypt initv))
          read-string)
      conf)))

(def authorization-config
  (defconfig
    {:name :explorama-authorization-config
     :default (or (some-> (try
                            (slurp (str config-dir
                                        "/rights-roles/auth.edn"))
                            (catch Exception _ nil))
                          (edn/read-string))
                  (some-> (try
                            (slurp (str config-dir
                                        "/auth.edn"))
                            (catch Exception _ nil))
                          (edn/read-string))
                  (some-> (try
                            (slurp "auth.edn")
                            (catch Exception _ nil))
                          (edn/read-string)))
     :post-read-fn decrypt-conf
     :doc "Configuration for the user authorization against some service (keycloak, ldap)."}))

(def attributes-info-config
  (defconfig
    {:name :explorama-attributes-info-config
     :default (or (some-> (try
                            (slurp (str config-dir
                                        "/rights-roles/user_infos.edn"))
                            (catch Exception _ nil))
                          (edn/read-string))
                  (some-> (try
                            (slurp (str config-dir
                                        "/user-infos.edn"))
                            (catch Exception _ nil))
                          (edn/read-string))
                  (some-> (try
                            (slurp "user_infos.edn")
                            (catch Exception _ nil))
                          (edn/read-string))
                  {})
     :post-read-fn decrypt-conf
     :doc "Configuration where the user information are coming from (keycloak, ldap)."}))

(def access-token-lifetime
  (defconfig
    {:env :explorama-access-token-lifetime
     :default (* 60000 30)
     :type :integer
     :post-read-fn (fn [v]
                     (.toSeconds TimeUnit/MILLISECONDS v))
     :doc "Defines how long in ms a access-token is valid until it needs refreshing."}))
