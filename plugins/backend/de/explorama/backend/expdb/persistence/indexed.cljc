(ns de.explorama.backend.expdb.persistence.indexed)

(defprotocol Indexed
  (schema [instance])

  (dump [instance]) ;TODO r1/db make sure to not implement this for the server bundle
  (set-dump [instance data]) ;TODO r1/db make sure to not implement this for the server bundle

  (data-tiles
    [instance data-tiles])
  (event
    [instance data-tile event-id])

  (set-index [instance new-indexes])
  (get-index [instance])

  (get-meta-data [instance data-tiles])

  (import-data
    [instance xml-spec]
    [instance xml-spec options])

  (delete
    [instance data-source-id]
    [instance data-source-id options])

  (delete-all
    [instance]
    [instance options])

  (patch [instance bucket field content])

  (data-sources [instance opts]))
