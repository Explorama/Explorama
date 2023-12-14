(ns de.explorama.profiling-tool.cli-helper
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [de.explorama.profiling-tool.config :as config]
            [taoensso.timbre :refer [error info]]))

(defn exit [status msg]
  (if (= status 0)
    (info msg)
    (error msg))
  (when-not config/repl?
    (System/exit status)))

(def valid-commands #{"version"})

(def cli-options
  ;; An option with a required argument
  [["-v" "--verbose" "Verbosity level"
    :id :debug?]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [""
        "Usage: builder.sh [options] [command] [arguments]"
        ""
        "Options:"
        options-summary
        ""
        "Commands:"
        "  version   Shows the XML-Builder version Eg. builder.sh version"
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        [command & arguments] arguments]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (valid-commands command)
      {:command command :options options :arguments arguments}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))