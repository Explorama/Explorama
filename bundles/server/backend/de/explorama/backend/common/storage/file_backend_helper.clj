(ns de.explorama.backend.common.storage.file-backend-helper
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pp]
            [clojure.java.io :as io]
            [taoensso.timbre :refer [error]]))

(defn save-value-to-file [path new-val pretty?]
  (try (if pretty?
         (pp/pprint new-val (io/writer (path)))
         (spit (path) new-val))
       (catch java.io.IOException e
         (error "Could not persist" (.getMessage e)))))

(defn load-file-to-atom [path atom-store default]
  (let [file (io/file (path))]
    (if (.exists file)
      (reset! atom-store (edn/read-string (slurp file)))
      (do
        (io/make-parents (path))
        (spit file default)
        (reset! atom-store default)))))

(defn add-save-watcher
  ([path atom-store watcher-key]
   (add-save-watcher path atom-store watcher-key false))
  ([path atom-store watcher-key pretty?]
   (add-watch atom-store
              watcher-key
              (fn [_ _ _ new-val]
                (save-value-to-file path new-val pretty?)))))
