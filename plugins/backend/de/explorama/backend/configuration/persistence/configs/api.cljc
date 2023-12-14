(ns de.explorama.backend.configuration.persistence.configs.api
  (:require [de.explorama.backend.configuration.persistence.configs.expdb :as expdb-storage]
            [de.explorama.shared.common.configs.layouts :refer [is-layout-valid?
                                                                reduce-layout-desc]]
            [de.explorama.shared.common.configs.overlayers :refer [is-overlayer-valid?
                                                                   reduce-overlayer-desc]]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.common.unification.time :refer [current-ms]]
            [de.explorama.shared.configuration.data-management.geographic-attributes-config :as ga-config]
            [de.explorama.shared.configuration.defaults :as default-configs]
            [de.explorama.shared.configuration.storage-protocol :as sp]))

(defn- new-instance []
  (expdb-storage/init {:geographic-attributes-key ga-config/geographic-attributes-key
                       :geographic-attributes-id ga-config/geographic-attributes-id
                       :geographic-attributes ga-config/initial-geographic-attributes
                       :space-config {:global-space?-fn (fn [_] true)}}))

(def ^:private storage (atom nil))

(defn- normalize [entry config-type]
  (case config-type
    :layouts (reduce-layout-desc entry)
    ;; :i18n 
    :overlayers (reduce-overlayer-desc entry)
    entry))

(defn- check [config-type entry]
  (case config-type
    :layouts (is-layout-valid? entry true)
    :i18n true
    :overlayers (is-overlayer-valid? entry true)
    entry))

(defn space-config [{:keys [username]}]
  (let [global? #{:topics :geographic-attributes :export-settings}]
    {:username username
     :global-space?-fn (fn [config-type]
                         (boolean (global? config-type)))}))

;;; ----------------------------- regular api -------------------------------

(defn load-defaults
  [{:keys [client-callback failed-callback user-validation]} [user-info config-types]]
  (if (and @storage (user-validation user-info))
    (client-callback (default-configs/load-defaults (space-config user-info) config-types))
    (failed-callback :user-invalid)))

(defn list-entries
  [{:keys [client-callback failed-callback user-validation]} [user-info config-types]]
  (if (and @storage (user-validation user-info))
    (client-callback (sp/list-entries @storage (space-config user-info) config-types))
    (failed-callback :user-invalid)))

(defn update-entry
  [{:keys [client-callback failed-callback user-validation]} [user-info config-type config-id entry]]
  (if (and @storage (user-validation user-info))
    (let [entry (cond-> entry
                  (map? entry)
                  (assoc :timestamp (current-ms))
                  :always
                  (normalize config-type))]
      (if (check config-type entry)
        (client-callback (sp/update-entry @storage (space-config user-info) config-type config-id entry))
        (failed-callback :entry-invalid)))
    (failed-callback :user-invalid)))

(defn get-entry
  [{:keys [client-callback failed-callback user-validation]} [user-info config-type config-id]]
  (if (and @storage (user-validation user-info))
    (client-callback (sp/get-entry @storage (space-config user-info) config-type config-id))
    (failed-callback :user-invalid)))

(defn delete-entry
  [{:keys [client-callback failed-callback user-validation]} [user-info config-type config-id]]
  (if (and @storage (user-validation user-info))
    (client-callback (sp/delete-entry @storage (space-config user-info) config-type config-id))
    (failed-callback :user-invalid)))

(defn copy-entry
  [{:keys [client-callback broadcast-callback failed-callback user-validation]} [source-user-info target-user-infos config-type config-id]]
  (if (and @storage (user-validation source-user-info)) ;TODO test if target user exists!
    (let [entry (-> (sp/get-entry @storage (space-config source-user-info) config-type config-id)
                    (assoc :timestamp (current-ms)))]
      (client-callback
       (doseq [{:keys [value]} target-user-infos]
         (when (string? value)
           (let [new-id (str (cljc-uuid))]
             (sp/update-entry @storage (space-config {:username value}) config-type new-id (assoc entry :id new-id))))))
      (broadcast-callback))
    (failed-callback :user-invalid)))

;;; ----------------------------- debug api -------------------------------

(defn upload-all [user-info configs]
  (when @storage
    (doseq [[config-type type-configs] configs]
      (doseq [[config-id entry] type-configs]
        (sp/update-entry @storage (space-config user-info) config-type (name config-id) entry)))))

(defn download-all [user-info]
  (when @storage
    (sp/list-entries @storage (space-config user-info) #{:layouts :overlayers})))

(defn delete-all [_user-info]
  (when @storage
    (sp/delete-all @storage)))

(defn init []
  (reset! storage (new-instance)))