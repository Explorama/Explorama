#!/usr/bin/env bb
(require '[clojure.edn :as edn]
         '[clojure.string :as str])

(def browser-check (edn/read-string (slurp "browser_check.edn")))
(def electron-check (edn/read-string (slurp "electron_check.edn")))
(def server-check (edn/read-string (slurp "server_check.edn")))
(def plugins-check (edn/read-string (slurp "plugins_check.edn")))

(def table-header "| file | type | level | row/end row | col/end col | message |
|------|------|-------|-------------|-------------|---------|")

(def row-template "| %s | %s | %s | %s | %s | %s |")

(defn- finding->table-row [{:keys [row end-row
                                   type
                                   level
                                   filename
                                   col end-col
                                   message]}]
  (format row-template
          filename
          (name type)
          (case level
            :warning ":warning:"
            :error ":x:"
            level)
          (str row " / " end-row)
          (str col "/" end-col)
          message))

(defn- check-result->report [report-title
                             {findings :findings
                              {:keys [error warning info files]} :summary}]
  (let [table-md (str/join "\n"
                           (into [table-header]
                                 (map finding->table-row)
                                 findings))]
    (str "### " report-title
         "\n\n"
         table-md
         "\n\n"
         "Summary \n files: " files " error(s): " error " warning(s): " warning " info(s): " info)))

(spit "report.md"
      (str/join "\n\n\n"
                [(check-result->report "Browser" browser-check)
                 (check-result->report "Electron" electron-check)
                 (check-result->report "Server" server-check)
                 (check-result->report "Plugins" plugins-check)]))
