(ns de.explorama.backend.table.websocket
  (:require [de.explorama.backend.table.data.api :as data-api]
            [de.explorama.shared.table.ws-api :as ws-api]
            [taoensso.timbre :refer [warn]]))

(defn- default-fn [& _]
  (warn "Not yet implemented"))

(def endpoints
  {ws-api/update-user-info default-fn
   ws-api/set-backend-canceled default-fn
   ws-api/load-event-details data-api/retrieve-event-data
   ws-api/retrieve-external-ref default-fn
   ws-api/table-data data-api/table-data})