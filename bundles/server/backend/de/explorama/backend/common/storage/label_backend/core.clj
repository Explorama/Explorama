(ns de.explorama.backend.common.storage.label-backend.core
  "This will be the main point to access the backend where the labels will be saved.
   There is no rights checking if a user is allowed to access/change labels.
   This should be done before calling the persistance functions."
  (:require [de.explorama.backend.labels.label-protocol :as lp]
            [de.explorama.backend.storage.label-backend.file :as file-backend]
            [de.explorama.backend.storage.label-backend.redis :as redis-backend]
            [de.explorama.backend.storage.label-backend.memory :as memory-backend]
            [taoensso.timbre :refer [error warn]]))

(defonce instance (atom nil))

(defn write-labels
  "Returns the labels as a hash-map from attribute to label."
  [labels]
  (lp/write-labels @instance labels))

(defn overwrite-labels
  "Returns the labels as a hash-map from attribute to label."
  [labels]
  (lp/overwrite-labels @instance labels))

(defn read-labels
  "Returns all labels as a hash-map from attribute to label."
  []
  (lp/read-labels @instance))

(defn new-instance [{:keys [backend label-store-dir]}  watch-fn]
  (reset! instance
          (case backend
            :redis (redis-backend/new-instance watch-fn)
            :file (file-backend/new-instance label-store-dir watch-fn)
            :in-memory (memory-backend/new-instance watch-fn)
            (throw (ex-info "Unknown backend type." backend)))))