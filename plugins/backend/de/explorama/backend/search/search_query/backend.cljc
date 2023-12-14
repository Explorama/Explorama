(ns de.explorama.backend.search.search-query.backend)

(defprotocol Adapter
  (save-query [instance user title size query])
  (update-query-usage [instance user query-id])
  (list-queries [instance user])
  (share-query [instance user share-with query-id])
  (delete-query [instance user query-id])
  (debug-delete-all-queries [instance]))