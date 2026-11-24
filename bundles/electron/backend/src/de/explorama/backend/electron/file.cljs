(ns de.explorama.backend.electron.file
  (:require [cljs.reader :refer [read-string]]
            [taoensso.timbre :refer-macros [error]]))

;; Node builtins are required at runtime: under figwheel's :auto-bundle
;; (:target :bundle) compile, namespace requires only resolve against
;; node_modules, and fs/path are builtins — js/require defers resolution to
;; the Node/electron runtime (webpack target 'node' leaves it untouched).
(def ^:private node-fs (js/require "fs"))
(def ^:private node-path (js/require "path"))

(defn add-to-path [base-path add]
  (.normalize node-path
              (.format node-path (clj->js {:dir base-path
                                           :base add}))))

(defn json-parse
  [string]
  (.parse js/JSON string))

(defn file-exists? [path]
  (.existsSync node-fs path))

(defn create-folder [path]
  (when (not (.existsSync node-fs path))
    (.mkdirSync node-fs path #js{:recursive true})))

(defn read-edn [path]
  (try
    (-> (.readFileSync node-fs path)
        (.toString)
        (read-string))
    (catch js/Object e
      (error "Failed to read file" (str e)))))

(defn delete-file [path]
  (when (file-exists? path)
    (.rmSync node-fs path (clj->js {:maxRetries 20
                                    :retryDelay 500}))))

(defn write-file-sync
  ([path data as-string?]
   (let [dirname (.dirname node-path path)]
     (when (not (file-exists? dirname))
       (.mkdirSync node-fs dirname #js{:recursive true}))
     (.writeFileSync node-fs path (cond-> data
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
