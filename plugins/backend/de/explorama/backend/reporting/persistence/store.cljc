
(ns de.explorama.backend.reporting.persistence.store)

(defprotocol Store
  (init [_])
  (read-files [_ force-reload?])
  (read-user-files [_ user-info])
  (read-file [_ user-info id])
  (write-file [_ user-info id content])
  (delete-file [_ user-info id]))