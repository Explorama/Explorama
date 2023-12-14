(ns de.explorama.backend.search.search-query.core
  (:require [de.explorama.backend.search.search-query.backend :as backend]
            [de.explorama.backend.search.search-query.expdb :as expdb]))

(defonce ^:private instance (atom nil))

(defn new-instance []
  (reset! instance (expdb/new-instance)))

(defn save-query [{:keys [client-callback user-validation]} [user-info title query]]
  (when (user-validation user-info)
    (let [saved-query (backend/save-query @instance user-info title 0 ;0 = size - Ignore for now due to performance and its not visible currently
                                          query)]
      (client-callback saved-query))))

(defn update-query-usage [{:keys [client-callback user-validation]} [user-info query-id]]
  (when (user-validation user-info)
    (let [updated-query (backend/update-query-usage @instance user-info query-id)]
      (client-callback updated-query))))

(defn list-queries [{:keys [client-callback user-validation]} [user-info]]
  (when (user-validation user-info)
    (let [queries (backend/list-queries @instance user-info)]
      (client-callback queries))))

(defn share-query [{:keys [client-callback user-validation]} [user-info share-with query-id]]
  (when (user-validation user-info)
    (let [shared-query (backend/share-query @instance user-info share-with query-id)]
      (client-callback shared-query))))

(defn delete-query [{:keys [client-callback user-validation]} [user-info query-id]]
  (when (user-validation user-info)
    (let [deleted? (backend/delete-query @instance user-info query-id)]
      (client-callback query-id deleted?))))

(defn debug-delete-all-queries []
  (backend/debug-delete-all-queries @instance))
