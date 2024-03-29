#!/usr/bin/env bb
(require '[babashka.deps :as deps])

(deps/add-deps '{:deps {lambdaisland/ansi {:mvn/version "0.2.37"}}})

(require '[clojure.string :as str]
         '[lambdaisland.ansi :refer [text->hiccup]])

(def browser-ancient "bundles/browser/browser_ancient.txt")
(def electron-ancient "bundles/electron/electron_ancient.txt")

(defn clean-ansi
  "Remove ansi code by converting to hiccup and only return the last bit (clean text)"
  [text]
  (first (map last (text->hiccup text))))

(defn- clean-up-new-version [new-version]
  (-> new-version
      (str/replace "\"" "")
      (str/replace "]" "")
      clean-ansi))

(defn- clean-up-old-version [old-version]
  (-> old-version
      str/trim
      (str/split #" ")
      first
      (str/replace "\"" "")
      clean-ansi))

(defn- read-ancient-report [report-path]
  (let [report-lines (str/split (slurp report-path)
                                #"\r\n|\n")]
    (->> report-lines
         (filter #(not (str/includes? % "/root/.lein/self-installs/leiningen")))
         (map (fn [report-line]
                (let [[new-version-vector old-version] (str/split report-line #"is available but we use")
                      [lib new-version] (str/split new-version-vector #" ")]
                  (when (and (not (str/blank? new-version))
                             (not (str/blank? lib))
                             (not (str/blank? old-version)))
                    [(str/replace lib "[" "")
                     (clean-up-old-version old-version)
                     (clean-up-new-version new-version)]))))
         (filter #(identity %)))))

(defn- report-row [[lib old-verion new-version]]
  (format "|     %s     |       %s        |      %s     |"
          lib
          old-verion
          new-version))

(defn- generate-report-table []
  (let [ancient-versions (distinct (concat (read-ancient-report browser-ancient)
                                           (read-ancient-report electron-ancient)))
        table-header (str "| Dependency | Current Version | New Version |
|----------|:---------------:|:-----------:|")]
    (spit "ancient-report.md" (str/join "\n" (into [table-header]
                                                   (map report-row
                                                        ancient-versions))))))

(generate-report-table)
