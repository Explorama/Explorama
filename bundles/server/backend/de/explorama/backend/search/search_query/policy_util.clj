(ns de.explorama.backend.search.search-query.policy-util
  (:require [de.explorama.backend.abac.policy-repository :as pr]
            [de.explorama.backend.abac.pep :as pep]
            [clojure.string :as string]
            [taoensso.timbre :refer [debug]]))

(def ^:private group-id "saved-search-queries")

(defn- policy-id [query-id]
  (keyword (str "search-query-" query-id)))

(defn create-policy [{:keys [username]} title query-id]
  (let [pol-id (policy-id query-id)]
    (pr/create-policy pol-id
                      {:user-attributes {:attributes {:username [username]}
                                         :required [:username]}
                       :data-attributes {:id [query-id]}
                       :id              pol-id
                       :group-id group-id
                       :name            title
                       :creator username})))

(defn delete-policy [query-id]
  (let [pol-id (policy-id query-id)]
    (pr/delete-policy pol-id)))

(defn useable? [user query-id]
  (let [pol-id (policy-id query-id)]
    (pep/checked-function-call pol-id
                               (select-keys user [:username])
                               {:id query-id}
                               (fn []
                                 true)
                               (fn [failreason]
                                 (debug failreason)
                                 false))))

(defn filter-policy [[pol-id policy]]
  (and (not= group-id (get policy :group-id))
       (not (string/starts-with? (name pol-id)
                                 "search-query-"))))

(defn share-with [user query-id share-with]
  (throw (ex-info "Share with not implemented" {:user user
                                                :query-id query-id
                                                :share-with share-with})))