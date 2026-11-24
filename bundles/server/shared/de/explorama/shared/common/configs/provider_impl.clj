(ns de.explorama.shared.common.configs.provider-impl
  (:require [clojure.string :as str]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.tools.logging :as timbre-tools]))

(def ^:private config {:explorama-bucket-config
                       {"default" {:backend "browser" :indexed? true
                                   :schema "default"
                                   :data-tile-keys {"year" {:field ["Date" "date" "value"]
                                                            :date-part :year
                                                            :type :string}
                                                    "country" {:field ["Context" "country" "name"]
                                                               :type :string}
                                                    "datasource" {:field ["Datasource" "datasource" "name"]}
                                                    "bucket" {:field :bucket
                                                              :type :string}
                                                    "identifier" {:value "search"}}}}})

(defn- env-var-name
  "Config key to environment variable name, e.g. :explorama-bind-address ->
   EXPLORAMA_BIND_ADDRESS. Values are strings; type coercion happens in
   defconfig's eval-type."
  [key]
  (-> (name key)
      (str/upper-case)
      (str/replace "-" "_")))

(defn lookup [key default]
  (or (System/getenv (env-var-name key))
      (get config key default)))

(def config-dir "")

(defn load-logging-config [log-config]
  (let [log-config (log-config)
        log-config (timbre/spy log-config)]
    (timbre/handle-uncaught-jvm-exceptions!)
    (timbre/merge-config! log-config)
    (timbre-tools/use-timbre)
    (timbre/info "Logging with new configuration.")))
