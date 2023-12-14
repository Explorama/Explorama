(ns de.explorama.backend.configuration.persistence.labels.label-protocol)

(defprotocol
 Label-Backend
  (write-labels
    [instance labels])
  (overwrite-labels
    [instance labels])
  (read-labels
    [instance]))