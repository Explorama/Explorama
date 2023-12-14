(ns de.explorama.backend.configuration.persistence.configs.expdb
  (:require [de.explorama.backend.expdb.middleware.db :as expdb]
            [de.explorama.shared.configuration.storage-protocol :as sp]
            [de.explorama.shared.configuration.defaults :as defaults]))

(def ^:private bucket "/configuration/")

(defn- list-config-type-entries
  ([_config-type configs]
   (reduce (fn [result [k {:keys [id key value] :as data}]]
             (if id
               (assoc result id data)
               (assoc result key value)))
           {}
           configs))
  ([config-type]
   (list-config-type-entries config-type (expdb/get bucket config-type))))

(defn- list-entries [space-config config-types]
  (reduce (fn [acc config-type]
            (assoc acc config-type (list-config-type-entries config-type)))
          {}
          config-types))

(defn- get-entry [space-config config-type config-id]
  (-> (expdb/get bucket config-type)
      (get config-id)))

(defn- update-entry [space-config config-type config-id entry]
  (let [old (expdb/get bucket config-type)
        new (assoc old config-id entry)]
    (expdb/set bucket config-type new)))


(defn- delete-entry [space-config config-type config-id]
  (let [old (expdb/get bucket config-type)]
    (expdb/set bucket config-type (dissoc old config-id))))

(defn- delete-all* []
  (expdb/del-bucket bucket))

(deftype ExpDB []
  sp/StorageProtocol
  (sp/load-defaults [_ user-info config-types]
    (defaults/load-defaults user-info config-types))
      ;;Not used currently for this adapter
  (sp/list-entries [_ space-config config-types]
    (list-entries space-config config-types))

  (sp/update-entry [_ space-config config-type config-id entry]
    (update-entry space-config config-type config-id entry))

  (sp/get-entry [_ space-config config-type config-id]
    (get-entry space-config config-type config-id))

  (sp/delete-entry [_ space-config config-type config-id]
    (delete-entry space-config config-type config-id))

  (sp/delete-all [_]
    (delete-all*))

  (sp/download-all [_] (throw  (ex-info "Not implemented" {})))
  (sp/upload-all [_] (throw (ex-info "Not implemented" {}))))

(defn initalize-geographic-attributes [space-config
                                       geographic-attributes-key
                                       geographic-attributes-id
                                       geographic-attributes]
  (when-not (get-entry space-config
                       geographic-attributes-key
                       geographic-attributes-id)
    (update-entry space-config
                  geographic-attributes-key
                  geographic-attributes-id
                  {:id geographic-attributes-id
                   geographic-attributes-key geographic-attributes})))

(defn init [{:keys [geographic-attributes-key space-config geographic-attributes-id geographic-attributes]}]
  (when (and geographic-attributes-key space-config geographic-attributes-id geographic-attributes)
    (initalize-geographic-attributes space-config
                                     geographic-attributes-key
                                     geographic-attributes-id
                                     geographic-attributes))
  (ExpDB.))
