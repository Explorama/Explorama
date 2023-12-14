(ns de.explorama.frontend.configuration.configs.persistence
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.configuration.configs.config-types.language :as language-configs]
            [de.explorama.frontend.configuration.configs.config-types.layout :as layout-configs]
            [de.explorama.frontend.configuration.config :as config]
            [de.explorama.shared.configuration.storage-protocol :as sp]
            [de.explorama.frontend.configuration.storage-adapter.ws-backend :as ws-backend]
            [taoensso.timbre :refer-macros [error]]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(def backend-storage (ws-backend/init))
(defn- init-configs [user-info]
  (sp/load-defaults backend-storage user-info #{:color-scales :default-layouts :default-overlayers})
  (sp/list-entries backend-storage user-info #{:i18n :layouts :theme :overlayers :topics :geographic-attributes :export-settings}))

(re-frame/reg-event-fx
 ::init-configs
 (fn [{db :db}]
   (let [user-info (fi/call-api :user-info-db-get db)]
     (init-configs user-info)
     {})))

(re-frame/reg-event-fx
 ::propagate-changes
 (fn [{db :db} [_ config-types]]
   (let [propagate-descs (vals (fi/call-api :service-category-db-get db :config-changed-action))]
     {:dispatch-n (mapv (fn [{:keys [on-change-event configs]}]
                          (when (and (some config-types configs)
                                     (vector? on-change-event))
                            (conj on-change-event config-types)))
                        propagate-descs)})))

(re-frame/reg-event-fx
 ::load-user-configs
 (fn [{db :db} [_ config-keys]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     (sp/list-entries backend-storage user-info (or config-keys #{:i18n :layouts :overlayers}))
     {})))

(re-frame/reg-event-fx
 ::save-and-commit
 (fn [{db :db} [_ config-type config-id config-desc {:keys [trigger-action] :as callbacks}]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     (ws-backend/add-callbacks (or trigger-action :update)
                               config-type config-id callbacks)
     (sp/update-entry backend-storage user-info config-type config-id config-desc)
     {})))

(re-frame/reg-event-fx
 ::delete-config
 (fn [{db :db} [_ config-type config-id callbacks]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     (ws-backend/add-callbacks :delete config-type config-id callbacks)
     (sp/delete-entry backend-storage user-info config-type config-id)
     {})))

(re-frame/reg-event-fx
 ::copy-config
 (fn [{db :db} [_ config-type config-id target-users]]
   (let [source-user-info (fi/call-api :user-info-db-get db)
         client-id (fi/call-api :client-id-db-get db)]
     (ws-backend/copy-config source-user-info client-id target-users config-type config-id)
     {})))