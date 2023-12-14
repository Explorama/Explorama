(ns de.explorama.cli.data-transformer.cli-helper
  (:require [clojure.string :as string]
            [clojure.tools.cli :refer [parse-opts]]
            [de.explorama.cli.data-transformer.config :as config]
            [taoensso.timbre :refer [error info]]))

(defn exit [status msg]
  (if (= status 0)
    (info msg)
    (error msg))
  (when-not config/repl?
    (System/exit status)))

(def valid-commands #{"gen" "gen-mapping" "check" "demo" "operation" "version"})

(def cli-options
  ;; An option with a required argument
  [["-n" "--number ROWS" "Number of rows (only for demo)"
    :id :lines
    :default 5
    :parse-fn #(Integer/parseInt %)
    :validate [#(pos? %) "Must be a positive number."]]
   ;; A non-idempotent option (:default is applied first)
   ["-f" "--file FILE" "CLJ file to be loaded before mapping."
    :id :extra-files
    :multi true ; use :update-fn to combine multiple instance of -f/--file
    :default []
    :update-fn conj]
   ["-v" "--verbose" "Verbosity level"
    :id :debug?]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [""
        "Usage: exploramaxml.sh [options] [command] [arguments]"
        ""
        "Options:"
        options-summary
        ""
        "Commands:"
        "  gen                      Generate a json from the source file based on a mapping. Eg. exploramaxml.sh gen mapping.clj source.csv target.xml"
        "  gen-mapping              Generate a mapping from the source file. Eg. exploramaxml.sh gen-mapping source.csv mapping.clj"
        "  check                    Checks the given xml against the input-datastructure xsd. Eg. exploramaxml.sh check target.xml"
        "  demo (not implementend)  Converts the first -n lines. Eg. exploramaxml.sh -n 5 demo mapping.clj source.csv"
        "  operation                Shows a set of possible operations after the import for a set of attributes. Eg. exploramaxml.sh operation mapping.clj source.csv"
        "  version                  Shows the XML-Builder version Eg. exploramaxml.sh version"
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