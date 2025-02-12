(require '[cheshire.core :as json]
         '[babashka.fs :as fs]
         '[clojure.string :as str])

(def target-fix-path "../gen_src/browser/de/explorama/profiling_tool/resource/")
(def target-gen-path "../gen_src/browser/de/explorama/profiling_tool/resource/")

(fs/create-dirs target-fix-path)
(fs/create-dirs target-gen-path)

(defn escape-string [s]
  (println s)
  (str "\""
       (-> (str/join "\n" s)
           (str/replace "\"" "\\\""))
       "\""))

(defn resource->code [root path extention]
  (let [files (fs/match root (str "regex:(.*/|.*)([a-zA-Z0-9-_]+)-(1k|5k|10k)." extention) {:recursive true})]
    (println files)
    (doseq [file files
            :let [fname (-> (fs/file-name file)
                            (str/replace (re-pattern (str "." extention "$")) "")
                            (str/replace #"\_" "-"))
                  new-path (str path (str/replace fname #"\-" "_") "_" extention ".cljs")]]
      (spit new-path (str "(ns de.explorama.profiling-tool.resource." fname "_" extention ")\n\n(def content\n"))
      (spit new-path (cond-> (fs/read-all-lines file)
                       (= extention "json")
                       (-> first (json/parse-string true))
                       (= extention "edn")
                       (-> first (read-string))
                       (= extention "csv")
                       (-> escape-string))
            :append true)
      (spit new-path ")" :append true))))

;; (resource->code "../gen_resources" target-fix-path "edn")
;; (resource->code "../gen_resources" target-fix-path "edn")
;; (resource->code "../gen_resources" target-gen-path "json")
;; (resource->code "../gen_resources" target-gen-path "json")
(resource->code "../gen_raw_data" target-gen-path "csv")
(resource->code "../gen_raw_data" target-gen-path "csv")