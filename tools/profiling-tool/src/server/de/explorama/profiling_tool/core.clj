(ns de.explorama.profiling-tool.core
  (:require [de.explorama.profiling-tool.cli-helper :refer [validate-args exit]]
            [de.explorama.profiling-tool.verticals.ac-service]
            [de.explorama.profiling-tool.verticals.suche]
            [de.explorama.profiling-tool.config :as config]
            #_[de.explorama.profiling-tool.version :as v]
            [taoensso.timbre :refer [info]])
  (:gen-class))

#_(defn- execute-version []
    (info "Version:" v/version))

(defn- execute-command [{:keys [command arguments options]}]
  (let [{:keys [debug?]} options]
    (case command
      ;"version" (execute-version)
      (println "what?"))))

(defn -main [& args]
  (let [{:keys [exit-message ok?]
         :as parsed-opts} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (execute-command parsed-opts))))