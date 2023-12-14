(ns de.explorama.shared.configuration.storage-protocol)

(defprotocol StorageProtocol
  "configuration storage protocol"
  (load-defaults [_ user-info config-types] "list all defaults for given user and config types")
  (list-entries [_ user-info config-types] "list all entries for given user and config types")
  (update-entry [_ user-info config-type config-id entry] "creates or updates entry for given user and config type")
  (get-entry [_ user-info config-type config-id] "returns entry for given user and config type")
  (delete-entry [_ user-info config-type config-id] "delete entry for given user and config type")
  (delete-all [_] "delete all configs. (Admin function)")
  (download-all [_] "download all configs. (Admin function)")
  (upload-all [_] "upload all configs. (Admin function)"))
