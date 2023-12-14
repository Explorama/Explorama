(ns de.explorama.shared.map.ws-api)

(def load-layer-config :map/load-layer-config)
(def set-layer-config :map/set-layer-config)

(def load-acs :map/load-acs)
(def set-acs :map/set-acs)
(def update-user-info :map/update-user-infos)

(def load-external-ref :map/external-ref-load)
(def update-ref-entry :map/update-external-ref-entry)

;; Kind of Legacy not sure if still needed
(def remove-data :map/remove-data)
(def cancel-loading :map/cancel-loading)

(def operation :map/operation)

(def operation-result :map/operation-result)
(def operation-failed :map/operation-failed)
(def operation-async-acs :map/operation-async-acs)

(def retrieve-event-data :map/event-data)
(def retrieved-event-data :map/retrieved-event-data)

(def update-usable-config-ids :map/usable-config-ids)
(def update-usable-config-result :map/usable-config-reuslt)
(def update-usable-config-failed :map/usable-config-failed)