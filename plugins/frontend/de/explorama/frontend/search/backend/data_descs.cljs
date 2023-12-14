(ns de.explorama.frontend.search.backend.data-descs
  (:require [de.explorama.shared.search.ws-api :as ws-api]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.path :as path]
            [re-frame.core :refer [reg-event-fx]]))

(reg-event-fx
 ws-api/data-descs
 (fn [{db :db} [_ frame-id]]
   {:backend-tube [ws-api/data-descs
                   {:client-callback [ws-api/data-descs-result frame-id]
                    :failed-callback [ws-api/failed-handler :data-descs]}
                   {:language (i18n/current-language db)}]}))

(reg-event-fx
 ws-api/data-descs-result
 (fn [{db :db} [_ frame-id data-descs]]
   {:db (assoc-in db
                  (path/data-descs frame-id)
                  data-descs)}))
