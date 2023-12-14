(ns de.explorama.cli.data-transformer.config
  (:require [config.core :refer [env]]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.tools.logging :as timbre-tools]
            [clojure.edn :as edn]))

(def repl?
  (or (env :dev-repl)
      false))

(def log-config
  {:min-level    :info
   :ns-filter    {:deny #{"org.apache.http.*"} :allow #{"*"}}
   :appenders    {:println {:min-level :trace}
                  :spit    {:output-fn (partial timbre/default-output-fn
                                                {:stacktrace-fonts {}})}}})

(defn- setup-timbre [log-config]
  (timbre/handle-uncaught-jvm-exceptions!)
  (timbre/merge-config! log-config)
  (timbre-tools/use-timbre))

(defn debug-output []
  (setup-timbre (assoc log-config
                       :min-level :debug)))

(defn default-output []
  (setup-timbre log-config))

(def countries-path
  (or (env :explorama-country-path)
      nil))

(def countries
  (some-> (try (slurp countries-path)
               (catch Exception _ nil))
          (edn/read-string)))
