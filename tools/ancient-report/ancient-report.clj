#!/usr/bin/env bb
(require '[clojure.string :as str])

(def browser-ancient "bundles/browser/browser_ancient.txt")
(def electron-ancient "bundles/electron/electron_ancient.txt")


(defn- clean-up-new-version [new-version]
  (-> new-version
      (str/replace "\"" "")
      (str/replace "]" "")))

(defn- clean-up-old-version [old-version]
  (-> old-version
      str/trim
      (str/split #" ")
      first
      (str/replace "\"" "")))

(defn- read-ancient-report [report-path]
  (let [report-lines (str/split (slurp report-path)
                                #"\r\n|\n")]
    (->> report-lines
         (filter #(not (str/includes? % "/root/.lein/self-installs/leiningen")))
         (map (fn [report-line]
                (let [[new-version-vector old-version] (str/split report-line #"is available but we use")
                      [lib new-version] (str/split new-version-vector #" ")]

                  [(str/replace lib "[" "")
                   (clean-up-old-version old-version)
                   (clean-up-new-version new-version)]))))))

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
