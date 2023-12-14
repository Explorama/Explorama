(ns de.explorama.backend.configuration.datasource-api
  (:require [de.explorama.backend.expdb.middleware.indexed :as idb]
            [taoensso.timbre :refer [error]]))

(defn delete-datasource [{:keys [client-callback]} [[bucket datasource]]]
  (try
    (idb/delete-data-source (name bucket) datasource)
    (client-callback {:success true})
    (catch #?(:cljs :default :clj Throwable) e
      (error "Could not delete datasource" e)
      (client-callback {:success false
                        :error e}))))
