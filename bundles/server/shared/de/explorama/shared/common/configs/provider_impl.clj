(ns de.explorama.shared.common.configs.provider-impl
  (:require [taoensso.timbre :as timbre]
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

(defn lookup [key default]
  (get config key default))

(def config-dir "")

(defn load-logging-config [log-config]
  (let [log-config (log-config)
        log-config (timbre/spy log-config)]
    (timbre/handle-uncaught-jvm-exceptions!)
    (timbre/merge-config! log-config)
    (timbre-tools/use-timbre)
    (timbre/info "Logging with new configuration.")))
