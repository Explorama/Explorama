(ns de.explorama.backend.expdb.legacy.service-states)

; PLEASE DO NOT USE THE SERVICE ATOM - use the discovery interface instead
(defonce ^:private current-cache-services (atom nil))

(defonce ^:private current-verticals (atom nil))
