(ns de.explorama.cli.data-transformer.core
  (:require [de.explorama.cli.data-transformer.cli-helper :refer [validate-args exit]]
            [de.explorama.cli.data-transformer.config :as config]
            [de.explorama.cli.data-transformer.transformer-helper :as t-helper]
            [de.explorama.cli.data-transformer.version :as v]
            [taoensso.timbre :refer [info]])
  (:gen-class))

(defn- execute-check [[xml-file]]
  (if-not (seq xml-file)
    (exit 1 "No file given to check.")
    (t-helper/check xml-file)))

#_
(defn- execute-demo [[mapping source] {:keys [lines extra-files] :as opts}]
  (if-not (and (seq mapping)
               (seq source))
    (exit 1 "No mapping/source given.")
    (t-helper/demo source mapping lines extra-files)))

(defn- execute-gen [[mapping source target] {:keys [extra-files]}]
  (if-not (and (seq mapping)
               (seq source)
               (seq target))
    (exit 1 "No mapping/source/target given.")
    (t-helper/gen source mapping target extra-files)))

(defn- execute-operation [[mapping source] {:keys [lines extra-files] :as opts}]
  (if-not (and (seq mapping)
               (seq source))
    (exit 1 "No mapping/source given.")
    (t-helper/operation source mapping lines extra-files)))

(defn- execute-gen-mapping [[mapping source target] {:keys [extra-files]}]
  (if-not (and (seq mapping)
               (seq source)
               (seq target))
    (exit 1 "No mapping/source/target given.")
    (t-helper/gen-mapping source mapping target extra-files)))

(defn- execute-version []
  (info "Version:" v/version))

(defn- execute-command [{:keys [command arguments options]}]
  (let [{:keys [debug?]} options]
    (if debug?
      (config/debug-output)
      (config/default-output))
    (case command
      "version" (execute-version)
      "check" (execute-check arguments)
      #_#_"demo" (execute-demo arguments options)
      "gen-mapping" (execute-gen-mapping arguments options)
      "gen" (execute-gen arguments options)
      "operation" (execute-operation arguments options)
      (println "what?"))))

(defn -main [& args]
  (let [{:keys [exit-message ok?]
         :as parsed-opts} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (execute-command parsed-opts))))