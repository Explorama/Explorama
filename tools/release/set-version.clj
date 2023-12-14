#!/usr/bin/env bb
(require '[clojure.string :as st])

(def file-sep (java.io.File/separator))
(defn- add-to-path [path & add]
  (st/join file-sep
           (into [path] add)))

(def root-path (add-to-path ".." ".."))
(def project-files [(add-to-path root-path "bundles" "browser" "project.clj")
                    (add-to-path root-path "bundles" "browser" "package.json")
                    (add-to-path root-path "bundles" "electron" "project.clj")
                    (add-to-path root-path "bundles" "electron" "package.json")
                    (add-to-path root-path "bundles" "server" "project.clj")
                    (add-to-path root-path "frontend-integrations" "woco" "frontend" "de" "explorama" "frontend" "woco" "config.cljs")])

(def clj-regex #"\(def\sapp-version\s\"(.+)\"\)")
(def cljs-regex #"\(def\sapp-version\s\"(.+)\"\)")
(def json-regex #"\"version\"\:\s\"(.+)\"\,")

(defn- args->map [args]
  (if (and (seq args)
           (= 1 (count args)))
    {:new-version (first args)}
    (println "Error: Failed to parse args
              \nUsage:   bb set-version.clj <version>
              \nExample: bb set-version.clj 1.0.1")))

(let [{:keys [new-version]} (args->map *command-line-args*)]
  (when new-version
    (doseq [f project-files]
      (when-let [content (slurp f)]
        (when-let [pattern (cond
                             (st/ends-with? f ".clj") clj-regex
                             (st/ends-with? f ".cljs") cljs-regex
                             (st/ends-with? f ".json") json-regex)]
          (if-let [matches (re-find pattern content)]
            (do
              (println " - set version for file " f)
              (let [[full-match old-version] matches
                    content (-> content
                                (st/replace full-match
                                            (st/replace full-match
                                                        old-version
                                                        new-version)))]
                (spit f content)))

            (println " - nothing found for setting version (file " f ")")))))
    (println "Done.")))
         