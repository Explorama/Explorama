(ns de.explorama.shared.configuration.storage-memory
  (:require [de.explorama.shared.configuration.storage-protocol :as sp]))

(def db (atom {}))

(deftype MemoryStorage []
  sp/StorageProtocol

  (sp/load-defaults [_ user-info config-types])
    ;;Not used currently for this adapter

  (sp/list-entries [_ {:keys [username] :as user-info} config-types]
    (select-keys (get @db username {})
                 config-types))

  (sp/update-entry [_ {:keys [username] :as user-info} config-type config-id entry]
    (swap! db assoc-in [username config-type config-id] entry))

  (sp/get-entry [_ {:keys [username] :as user-info} config-type config-id]
    (get-in @db [username config-type config-id]))

  (sp/delete-entry [_ {:keys [username] :as user-info} config-type config-id]
    (swap! db update-in [username config-type] dissoc config-id))

  (sp/delete-all [_] (throw (ex-info "Not implemented" {})))
  (sp/download-all [_] (throw (ex-info "Not implemented" {})))
  (sp/upload-all [_] (throw (ex-info "Not implemented" {}))))

(defn init []
  (MemoryStorage.))
