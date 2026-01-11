(ns de.explorama.main.wrapper.file
  (:require [path :refer [dirname normalize format]]
            [cljs.reader :refer [read-string]]
            [fs :refer [writeFileSync rmSync readFileSync existsSync mkdirSync]]
            [taoensso.timbre :refer-macros [error]]))

(defn add-to-path [base-path add]
  (normalize
   (format (clj->js {:dir base-path
                     :base add}))))

(defn file-exists? [path]
  (existsSync path))

(defn delete-file [path]
  (when (file-exists? path)
    (rmSync path (clj->js {:force true
                           :maxRetries 20
                           :retryDelay 500}))))

(defn delete-folder [path]
  (when (file-exists? path)
    (rmSync path (clj->js {:recursive true
                           :force true
                           :maxRetries 20
                           :retryDelay 500}))))

(defn read-edn [path]
  (try
    (when (file-exists? path)
      (-> path
          (readFileSync)
          (.toString)
          (read-string)))
    (catch js/Object e
      (error e "Failed to read file"))))

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

