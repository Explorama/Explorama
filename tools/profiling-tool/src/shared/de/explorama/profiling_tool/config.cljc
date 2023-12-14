(ns de.explorama.profiling-tool.config
  (:require [de.explorama.shared.common.configs.util :refer [defconfig
                                                             load-logging-config]]
            [taoensso.timbre :as timbre]))

(def repl?
  (defconfig
    {:env :dev-repl
     :default false
     :type :boolean
     :doc "Helper env to decide if current process is a repl"}))

(def log-level
  (defconfig
    {:env :explorama-log-level
     :default :info
     :type :keyword
     :values [:debug :info :warn :error]
     :doc "Defines the lowest log output."}))

(def log-config
  (defconfig
    {:default {:min-level    log-level
               :ns-filter    {:deny #{"org.apache.http.*"
                                      "io.grpc.netty.shaded.io.*"}
                              :allow #{"*"}}
               :appenders    {:println {:min-level :trace}
                              :spit    {:output-fn (partial timbre/default-output-fn
                                                            {:stacktrace-fonts {}})}}}
     :doc "Defines how the logging should work. 
         See http://ptaoussanis.github.io/timbre/taoensso.timbre.html#var-*config* for more infos."}))

(load-logging-config log-config)

(def scheme
  (defconfig
    {:env :explorama-scheme
     :default "http"
     :type :string
     :doc "The scheme (protocol) part of the base URL as which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def host
  (defconfig
    {:env :explorama-host
     :default "localhost"
     :type :string
     :doc "The host (domain) part of the base URL as which we are running.
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def proxy-scheme
  (defconfig
    {:env :explorama-proxy-scheme
     :default scheme
     :type :string
     :doc "The scheme (protocol) part of the base URL as which we are running.  
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def proxy-host
  (defconfig
    {:env :explorama-proxy-host
     :default "dev-tls.explorama.local"
     :type :string
     :doc "The host (domain) part of the base URL as which we are running.  
         Note that we assume that this is the same for both the frontend server and for the clients."}))

(def base-url
  (defconfig
    {:name :profiling-tool-base-url
     :default (str scheme "://" host)
     :doc "The base URL, generated from the configured scheme, host and port."}))

(def proxy-base-url
  (defconfig
    {:name :profiling-tool-proxy-base-url
     :default (str proxy-scheme "://" proxy-host)
     :doc "The base URL, generated from the configured scheme, host and port."}))

(def testuser-login
  (defconfig
    {:default {:username "PAdmin" :password "PAdmin18"}
     :env :explorama-testuser-login
     :type :edn-string
     :doc "User to be used for login and benchmarking the explorama software."}))

(def bench-report-wait
  (defconfig
    {:default 2000
     :env :explorama-benchmark-report-wait-ms
     :type :integer
     :doc "Thread sleep before the report gets retrieved from tufte."}))

(def static-service-file
  (defconfig
    {:env :explorama-env-static-file
     :default [:config-dir "/services.edn"]
     :type :string
     :doc "Path to the static services.edn."}))

(def test-frame-id {:frame-id "benchmark-frame"
                    :workspace-id "7298a7fb-0838-49b2-8435-b3101a549b20"})
