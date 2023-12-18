#!/usr/bin/env bb
(require '[clojure.edn :as edn]
         '[clojure.string :as str])


(def browser-check (edn/read-string (slurp "browser_check.edn")))
(def electron-check (edn/read-string (slurp "electron_check.edn")))
(def abac-check (edn/read-string (slurp "abac_check.edn")))
(def cache-check (edn/read-string (slurp "cache_check.edn")))
(def data-format-lib-check (edn/read-string (slurp "data-format-lib_check.edn")))
(def data-transformer-check (edn/read-string (slurp "data-transformer_check.edn")))
(def ui-base-check (edn/read-string (slurp "ui-base_check.edn")))
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

(let [browser-report (check-result->report "Browser" browser-check)
      electron-report (check-result->report "Electron" electron-check)
      abac-report (check-result->report "ABAC" abac-check)
      cache-report (check-result->report "Cache" cache-check)
      data-format-lib-report (check-result->report "Data-format-lib" data-format-lib-check)
      data-transformer-report (check-result->report "Data-transformer" data-transformer-check)
      ui-base-report (check-result->report "UI-Base" ui-base-check)
      plugins-report (check-result->report "Plugins" plugins-check)]
  (spit "report.md"
        (str browser-report
             "\n\n\n"
             electron-report
             "\n\n\n"
             abac-report
             "\n\n\n"
             cache-report
             "\n\n\n"
             data-format-lib-report
             "\n\n\n"
             data-transformer-report
             "\n\n\n"
             ui-base-report
             "\n\n\n"
             plugins-report)))