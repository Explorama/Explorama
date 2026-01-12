(ns de.explorama.backend.search.search-query.policy-util)

(defn create-policy [_ _ _] {})

(defn delete-policy [_] true)

(defn useable? [_ _] true)

(defn filter-policy [_]
  true)

(defn share-with [user query-id share-with]
  (throw (ex-info "Share with not implemented" {:user user
                                                :query-id query-id
                                                :share-with share-with})))