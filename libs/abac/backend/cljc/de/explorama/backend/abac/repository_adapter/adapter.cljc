(ns de.explorama.backend.abac.repository-adapter.adapter)

(defprotocol PolicyAdapter
  (init [this data should-overwrite?])
  (fetch [this id])
  (fetch-multiple [this ids])
  (edit [this id data])
  (edit-all [this data])
  (create [this id data])
  (delete [this id]))