(ns de.explorama.backend.common.storage.agent.api)

(defprotocol Storage!
  (start [instance agent])
  (ready? [instance])
  (ready! [instance]))