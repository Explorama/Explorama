(ns de.explorama.shared.common.configs.provider-impl)

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

(defn load-logging-config [_])
