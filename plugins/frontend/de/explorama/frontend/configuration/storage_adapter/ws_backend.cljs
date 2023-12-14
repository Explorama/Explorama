(ns de.explorama.frontend.configuration.storage-adapter.ws-backend
  (:require [de.explorama.shared.configuration.storage-protocol :as sp]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            [de.explorama.frontend.configuration.path :as path]
            [re-frame.core :refer [reg-event-fx]]
            [de.explorama.frontend.backend-api :as backend-api]
            [taoensso.timbre :refer-macros [debug warn]]))

(defonce callbacks-store (atom {}))

(defn add-callbacks [action config-type config-id callbacks]
  (when (map? callbacks)
    (swap! callbacks-store assoc-in [action config-type config-id] callbacks)))

(defn rm-callbacks [action config-type config-id]
  (swap! callbacks-store update-in [action config-type] dissoc config-id))


(defn execute-callback [action config-type config-id status]
  (debug "callback to execute" action config-type config-id status)
  (when-let [callbacks (get-in @callbacks-store [action config-type config-id])]
    (let [{:keys [failed-callback success-callback]} callbacks]
      (cond
        (and (fn? success-callback)
             (= status :success))
        (success-callback)
        (and (fn? failed-callback)
             (= status :failed))
        (failed-callback)))
    (rm-callbacks action config-type config-id)))

(defn callback-config-ids-for-type [action config-types]
  (reduce (fn [acc config-type]
            (if-let [config-ids (-> (get-in @callbacks-store [action config-type])
                                    keys
                                    vec)]
              (apply conj acc (map #(vector config-type %)
                                   config-ids))
              acc))
          []
          config-types))

(reg-event-fx
 ws-api/load-defaults-result
 (fn [{db :db} [_ config-types entries]]
   (debug "Defaults arrived from server" config-types entries)
   {:db  (reduce (fn [db config-type]
                   (assoc-in db
                             (path/config-type config-type)
                             (get entries config-type)))
                 db
                 config-types)}))

(reg-event-fx
 ws-api/list-entries-result
 (fn [{db :db} [_ status config-types entries]]
   (debug "entries arrived from server" status config-types entries)
   (doseq [[config-type config-id] (callback-config-ids-for-type :list-entries config-types)]
     (execute-callback :list-entries config-type config-id status))
   (when (= status :success)
     {:dispatch-later {:ms 100
                       :dispatch [:de.explorama.frontend.configuration.configs.persistence/propagate-changes config-types]}
      :db  (reduce (fn [db config-type]
                     (assoc-in db
                               (path/config-type config-type)
                               (get entries config-type)))
                   db
                   config-types)})))

(reg-event-fx
 ws-api/update-entry-result
 (fn [{db :db} [_ status config-type config-id]]
   (debug "entry updated from server" status config-type config-id)
   (execute-callback :update config-type config-id status)
   (when (= status :success)
     {:dispatch [:de.explorama.frontend.configuration.configs.persistence/load-user-configs #{config-type}]})))

(reg-event-fx
 ws-api/get-entry-result
 (fn [{db :db} [_ config-type config-id entry]]
   (debug "entry arrived from server" config-type config-id entry)))

(reg-event-fx
 ws-api/delete-entry-result
 (fn [{db :db} [_ status config-type config-id]]
   (debug "entry deleted from server" status config-type config-id)
   (execute-callback :delete config-type config-id status)
   (when (= status :success)
     {:dispatch [:de.explorama.frontend.configuration.configs.persistence/load-user-configs #{config-type}]})))

(defn- generate-broadcast-filter [client-id target-user-infos]
  [:and
   #:data-format-lib.filter{:op :not=, :prop :client-id, :value client-id}
   (reduce (fn [acc {:keys [value]}]
             (conj acc
                   #:data-format-lib.filter{:op :=, :prop :username, :value value}))
           [:or]
           target-user-infos)])

(defn copy-config [source-user-info client-id target-user-infos config-type config-id]
  (backend-api/dispatch [ws-api/copy-entry-route
                         {:client-callback [ws-api/update-entry-result config-type config-id]
                          :broadcast-callback [:de.explorama.frontend.configuration.configs.persistence/load-user-configs #{config-type}]
                          :broadcast-filter (generate-broadcast-filter client-id target-user-infos)}
                         source-user-info
                         target-user-infos
                         config-type
                         config-id]))

(reg-event-fx
 ws-api/copy-entry-result
 (fn [{db :db} [_ config-type config-id]]
   (debug "entry copied on server" config-type config-id)))

(deftype WsBackendStorage []
  sp/StorageProtocol
  (sp/load-defaults [_ user-info config-types]
    (backend-api/dispatch [ws-api/load-defaults-route
                           {:client-callback [ws-api/load-defaults-result config-types]}
                           user-info
                           config-types]))

  (sp/list-entries [_ user-info config-types]
    (backend-api/dispatch [ws-api/list-entries-route
                           {:client-callback [ws-api/list-entries-result :success config-types]
                            :failed-callback [ws-api/list-entries-result :failed config-types]}
                           user-info
                           config-types]))

  (sp/update-entry [_ user-info config-type config-id entry]
    (backend-api/dispatch [ws-api/update-entry-route
                           {:client-callback [ws-api/update-entry-result :success config-type config-id]
                            :failed-callback [ws-api/update-entry-result :failed config-type config-id]}
                           user-info
                           config-type
                           config-id
                           entry]))

  (sp/get-entry [_ user-info config-type config-id]
    (backend-api/dispatch [ws-api/get-entry-route
                           {:client-callback [ws-api/get-entry-result config-type config-id]}
                           user-info
                           config-type
                           config-id]))

  (sp/delete-entry [_ user-info config-type config-id]
    (backend-api/dispatch [ws-api/delete-entry-route
                           {:client-callback [ws-api/delete-entry-result :success config-type config-id]
                            :failed-callback [ws-api/delete-entry-result :failed config-type config-id]}
                           user-info
                           config-type
                           config-id]))
  (sp/delete-all [_] (warn "Not implemented" {}))
  (sp/download-all [_] (warn "Not implemented" {}))
  (sp/upload-all [_] (warn "Not implemented" {})))

(defn init []
  (WsBackendStorage.))