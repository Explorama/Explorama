(ns de.explorama.shared.woco.config
  (:require [de.explorama.shared.common.configs.provider
             :refer [defconfig load-logging-config]
             :as configp]
            [taoensso.timbre :as timbre]))

(def plugin-key :woco)
(def plugin-string (name plugin-key))

#?(:cljs (def debug? ^boolean goog.DEBUG))
#?(:cljs (goog-define ^string DEFAULT_LOG_LEVEL "info"))
#?(:cljs (goog-define ^string RUNTIME_MODE "dev"))

#?(:cljs (def dev-mode? (= "dev" RUNTIME_MODE)))

(def explorama-log-level
  (defconfig
    {:env :explorama-log-level
     :default :info
     :type :keyword
     :values [:debug :info :warn :error]
     :doc "Defines the lowest log output."}))

(def explorama-log-config
  (defconfig
    {:name :explorama-log-config
     :fallback (fn []
                 {:min-level    explorama-log-level
                  :ns-filter    {:deny #{"org.apache.http.*"
                                         "io.grpc.netty.shaded.io.*"}
                                 :allow #{"*"}}
                  :appenders    {:println {:min-level :trace}
                                 :spit    {:output-fn (partial timbre/default-output-fn
                                                               {:stacktrace-fonts {}})}}})
     :default [:config-dir "/" (name plugin-key) "/log.edn"]
     :type :edn-file
     :doc "Defines how the logging should work.
         See http://ptaoussanis.github.io/timbre/taoensso.timbre.html#var-*config* for more infos."}))

(def explorama-search-workaround-data-classification
  (defconfig
    {:env :explorama-search-workaround-data-classification
     :default {:default :small}
     :type :edn-string
     :doc "Defines which data-tiles are mapped to specific query-partition sizes.
         Examples: {:classification [{:match {\"identifier\" \"search\"} :regex {\"datasource\" \"ICEWS.*\"}:=> :big}] :default :small}
         This matches everything that starts with ICEWS in the datasource to :big and anything else to :small (:default)."}))

(def explorama-query-partition
  (defconfig
    {:env :explorama-query-partition
     :type :edn-string
     :default {"search" {:big {:partition 25
                               :keys [:datasource :year :identifier]}
                         :medium {:partition 250
                                  :keys [:datasource :identifier]}
                         :small {:partition 1000
                                 :keys [:datasource :identifier]}}}
     :doc "Defines how big the diffrent data-tile query-partitions should be.
         This means with partition 25, the search will retrieve 25 Data-Tiles from the Database with one request."}))

(def explorama-experimental-features-enabled
  (defconfig
    {:env :explorama-experimental-features-enabled
     :type :boolean
     :default false
     :scope :all
     :overwritable? true
     :doc "Defines if possible experimental-features should be enabled.
         Those can be instable and might bring problems."}))

(def explorama-activate-devtools
  (defconfig
    {:env :explorama-activate-devtools
     :type :boolean
     :default false
     :scope :all
     :overwritable? true
     :doc "Enables some debug feauters, like reseting the policies or configs."}))

(def explorama-enabled-notifications
  (defconfig
    {:env :explorama-enabled-notifications
     :type :edn-string
     ;to enable all set to "*"; to disable all set to false/nil
     :default {:projects "*"     ; (possible: ["*" false/nil :rights])
               :operations "*"   ; (possible: ["*" false/nil :redo-on-reconnect])))
               :network nil      ; (possible: ["*" false/nil :config :i18n :ws])
               :misc "*"}        ; (possible: ["*" false/nil :product-tour])
     :scope :all
     :overwritable? true
     :doc "Enables some notifications."}))

(def explorama-direct-search-max-showall
  (defconfig
    {:env :explorama-direct-search-max-showall
     :type :boolean
     :default true
     :doc "Activates the devtools for the frontend."}))

(load-logging-config explorama-log-config)
