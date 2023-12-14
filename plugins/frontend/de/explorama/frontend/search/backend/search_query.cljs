(ns de.explorama.frontend.search.backend.search-query
  (:require [de.explorama.shared.search.ws-api :as ws-api]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.search.path :as spath]
            [clojure.string :refer [trim]]
            [taoensso.timbre :refer-macros [debug]]
            [re-frame.core :refer [reg-event-fx]]))

(defn- remove-options [formdata]
  (reduce (fn [acc [attr-desc search-row-desc]]
            (assoc acc attr-desc (dissoc search-row-desc :options)))
          {}
          formdata))

(reg-event-fx
 ws-api/search-query-save
 (fn [{db :db} [_ title frame-id]]
   (let [user-info (fi/call-api :user-info-db-get db)
         formdata (-> (get-in db (spath/frame-search-rows frame-id))
                      (remove-options))]
     {:backend-tube [ws-api/search-query-save
                     {:client-callback [ws-api/search-query-save-result]
                      :failed-callback [ws-api/failed-handler :search-query-save]}
                     user-info (trim title) formdata]})))

(reg-event-fx
 ws-api/search-query-save-result
 (fn [{db :db} [_ {query-id :id :as saved-query}]]
   (debug "save result" saved-query)
   {:db (if (seq query-id)
          (assoc-in db (spath/search-query-desc query-id) saved-query)
          db)}))

(reg-event-fx
 ws-api/search-query-update-usage
 (fn [{db :db} [_ query-id]]
   {:backend-tube [ws-api/search-query-update-usage
                   {:client-callback [ws-api/search-query-update-usage-result]
                    :failed-callback [ws-api/failed-handler :search-query-update-usage]}
                   (fi/call-api :user-info-db-get db)
                   query-id]}))

(reg-event-fx
 ws-api/search-query-update-usage-result
 (fn [{db :db} [_ {query-id :id :as saved-query}]]
   (debug "update result" saved-query)
   {:db (if (seq query-id)
          (assoc-in db (spath/search-query-desc query-id) saved-query)
          db)}))

(reg-event-fx
 ws-api/search-query-list
 (fn [{db :db}]
   {:backend-tube [ws-api/search-query-list
                   {:client-callback [ws-api/search-query-list-result]
                    :failed-callback [ws-api/failed-handler :search-query-list]}
                   (fi/call-api :user-info-db-get db)]}))

(reg-event-fx
 ws-api/search-query-list-result
 (fn [{db :db} [_ queries]]
   (debug "list result" queries)
   {:db (assoc-in db (spath/search-queries) queries)}))

(reg-event-fx
 ws-api/search-query-share
 (fn [{db :db} [_ share-with query-id]]
   {:backend-tube [ws-api/search-query-share
                   {:client-callback [ws-api/search-query-share-result]
                    :failed-callback [ws-api/failed-handler :search-query-share]}
                   (fi/call-api :user-info-db-get db)
                   share-with query-id]}))

(reg-event-fx
 ws-api/search-query-share-result
 (fn [{db :db} [_ {query-id :id :as saved-query}]]
   (debug "share result" saved-query)
   {:db (if (seq query-id)
          (assoc-in db (spath/search-query-desc query-id) saved-query)
          db)}))

(reg-event-fx
 ws-api/search-query-delete
 (fn [{db :db} [_ query-id]]
   {:backend-tube [ws-api/search-query-delete
                   {:client-callback [ws-api/search-query-delete-result]
                    :failed-callback [ws-api/failed-handler :search-query-delete]}
                   (fi/call-api :user-info-db-get db)
                   query-id]}))

(reg-event-fx
 ws-api/search-query-delete-result
 (fn [{db :db} [_ query-id deleted?]]
   (debug "Delete result" query-id deleted?)
   {:db (if deleted?
          (update-in db (spath/search-queries) dissoc query-id)
          db)}))


