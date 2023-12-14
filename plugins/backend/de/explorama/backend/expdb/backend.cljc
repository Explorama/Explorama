(ns de.explorama.backend.expdb.backend
  (:require [de.explorama.backend.common.middleware.cache :as idb-cache]
            [de.explorama.backend.common.middleware.data-provider :as data-provider]
            [de.explorama.backend.expdb.data-loader :as data-loader]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.cache :as cache]
            [de.explorama.backend.expdb.legacy.search.data-tile-ref :as dt-api]
            [de.explorama.backend.expdb.loader :as loader]
            [de.explorama.backend.expdb.middleware.indexed :as indexed]
            [de.explorama.backend.expdb.persistence.db-api :as db-api]
            [de.explorama.backend.expdb.temp-import.api :as import-api]
            [de.explorama.backend.frontend-api :as frontend-api]
            [de.explorama.shared.expdb.ws-api :as ws-api]
            [taoensso.timbre :refer [debug]]))

(defn init []
  (frontend-api/register-routes {ws-api/load-buckets db-api/load-buckets
                                 ws-api/download-bucket db-api/download-bucket
                                 ws-api/download-expdb db-api/download-expdb
                                 ws-api/upload-bucket db-api/upload-bucket
                                 ws-api/upload-expdb db-api/upload-expdb
                                 ws-api/upload-file import-api/upload-file
                                 ws-api/import-file import-api/import-file
                                 ws-api/delete-file import-api/delete-file
                                 ws-api/update-options import-api/update-options
                                 ws-api/commit-import import-api/commit-import
                                 ws-api/cancel-import import-api/cancel-import})
  (data-provider/register-provider "search" {:data-tiles indexed/get+
                                             :data-tile-ref dt-api/get-data-tiles-api})
  (dt-api/reset-cache)
  (cache/new-ac-cache)
  (idb-cache/reset-states)
  (loader/index-init)
  (data-loader/load-data)
  (debug "expdb backend started"))
