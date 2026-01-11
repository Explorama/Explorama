(ns de.explorama.backend.electron.file
  (:require [fs :refer [writeFileSync rmSync readFileSync existsSync mkdirSync]]
            [cljs.reader :refer [read-string]]
            [taoensso.timbre :refer-macros [error]]
            [path :refer [dirname normalize format]]))

(defn add-to-path [base-path add]
  (normalize
   (format (clj->js {:dir base-path
                     :base add}))))

(defn json-parse
  [string]
  (.parse js/JSON string))

(defn file-exists? [path]
  (existsSync path))

(defn create-folder [path]
  (when (not (existsSync path))
    (mkdirSync path #js{:recursive true})))

(defn read-edn [path]
  (try
    (-> path
        (readFileSync)
        (.toString)
        (read-string))
    (catch js/Object e
      (error "Failed to read file" (str e)))))

(defn delete-file [path]
  (when (file-exists? path)
    (rmSync path (clj->js {:maxRetries 20
                           :retryDelay 500}))))

(defn write-file-sync
  ([path data as-string?]
   (let [dirname (dirname path)]
     (when (not (file-exists? dirname))
       (mkdirSync dirname #js{:recursive true}))
     (writeFileSync path (cond-> data
                           as-string? (str)))))
  ([path data]
   (write-file-sync path data true)))

(defn write-edn
  ([path data merge?]
   (write-file-sync
    path
    (if merge?
      (-> (read-edn path)
          (or {})
          (merge data))
      data)))
  ([path data]
   (write-edn path data true)))

