(ns de.explorama.frontend.configuration.i18n.labels
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(def entry-type :labels)

(re-frame/reg-event-fx
 ::request-labels
 (fn [{db :db} [_ user-info]]
   {:backend-tube [ws-api/load-labels-route
                   {:client-callback [ws-api/load-labels-result]
                    :failed-callback [ws-api/handle-errors "Failed to request labels!"]}
                   user-info (i18n/current-language db)]}))

(re-frame/reg-event-db
 ws-api/load-labels-result
 (fn [db [_ labels]]
   (assoc-in db (path/i18n-entry entry-type) labels)))

(re-frame/reg-event-fx
 ws-api/update-labels
 (fn [{db :db} [_ _]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:dispatch [::request-labels user-info]})))

(defn get-labels
  [db]
  (get-in db (path/i18n-entry entry-type) {}))

(re-frame/reg-sub
 ::get-labels
 (fn [db]
   (get-labels db)))
