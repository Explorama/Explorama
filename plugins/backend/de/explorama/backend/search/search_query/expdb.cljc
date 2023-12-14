(ns de.explorama.backend.search.search-query.expdb
  "Implements the backend for saving search queries in redis.
   This implementation does not use policies to restrict access but uses the key-path definition"
  (:require [de.explorama.backend.expdb.middleware.db :as db]
            [de.explorama.backend.search.search-query.backend :as backend]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.common.unification.time :refer [current-ms]]
            [taoensso.timbre :refer [error warn]]))

(def ^:private bucket "/search/search-queries/")

(defn- db-key [& [user query-id]]
  (cond-> ""
    (map? user) (str "/" (:username user))
    (string? user) (str "/" user)
    (seq query-id) (str "/" query-id)))

(defn- save-query [user title size query]
  (let [query-id (cljc-uuid)
        query-desc {:id query-id
                    :creator (:username user)
                    :last-used (current-ms)
                    :title title
                    :estimated-size size
                    :query query}
        query-redis-key (db-key user query-id)
        set-result (db/set bucket
                           query-redis-key
                           query-desc)]
    (if set-result ;; TODO (= set-result "OK")
      query-desc
      (do
        (error "Could not save query" {:user user
                                       :query-redis-key query-redis-key
                                       :query-desc query-desc
                                       :save-response set-result})
        nil))))

(defn- update-query-usage [user query-id]
  (let [query-redis-key (db-key user query-id)
        current-val (db/get bucket query-redis-key)
        new-val (assoc current-val
                       :last-used (current-ms))]
    (if (nil? current-val)
      (do (warn "Could not update usage. Query doesn't exist anymore" {:user user
                                                                       :query-id query-id
                                                                       :redis-key query-redis-key})
          nil)
      (do (db/set bucket query-redis-key new-val)
          new-val))))

(defn- list-queries [user]
  (let [queries (db/get+ bucket)]
    (into {}
          (comp (filter (fn [{creator :creator}]
                          (= creator (:username user))))
                (map (fn [{:keys [id] :as q}]
                       [id q])))
          (vals queries))))

(defn- share-query [user share-with query-id]
  (throw (ex-info "share-query not implemented for Redis backend" {})))

(defn- delete-query [user query-id]
  (let [query-redis-key (db-key user query-id)
        {:keys [success] :as delete-result} (db/del bucket query-redis-key)]
    (if success
      true
      (warn "Couldn't delete query" {:query-id query-id
                                     :user user
                                     :redis-key query-redis-key
                                     :delete-result delete-result}))))

(defn- delete-all-queries []
  (db/del-bucket bucket))

(deftype ExpDB []
  backend/Adapter
  (save-query [_instance user title size query]
    (save-query user title size query))
  (update-query-usage [_instance user query-id]
    (update-query-usage user query-id))
  (list-queries [_instance user]
    (list-queries user))
  (share-query [_instance user share-with query-id]
    (share-query user share-with query-id))
  (delete-query [_instance user query-id]
    (delete-query user query-id))
  (debug-delete-all-queries [_instance]
    (delete-all-queries)))

(defn new-instance []
  (ExpDB.))
