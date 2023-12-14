(ns de.explorama.backend.expdb.temp-import.api
  (:require [clojure.string :as str]
            [de.explorama.backend.expdb.persistence.shared :as imp]
            [de.explorama.backend.expdb.temp-import.csv-parser :as csv-parser]
            [de.explorama.shared.data-transformer.generator :as generator]
            [de.explorama.shared.data-transformer.generator.edn-json :as edn-json-gen]
            [de.explorama.shared.data-transformer.mapping :as mapping]
            [de.explorama.shared.data-transformer.schema :as schema]
            [de.explorama.shared.data-transformer.suggestions :as suggestions]
            [taoensso.timbre :refer [error warn]]))

(defonce ^:private files (atom {}))

(defn upload-file [{:keys [client-callback failed-callback]} [meta-data result]]
  (try
    (let [meta-data-mapping (cond (= (str/lower-case (:extention meta-data))
                                     "csv")
                                  {:suggestions {:check-lines 200}
                                   :meta-data {:file-format :csv
                                               :csv {:separator ","
                                                     :quote "\""}}})
          data (cond :else
                     (csv-parser/parse meta-data-mapping
                                       result))
          fname (:name meta-data)
          _ (swap! files assoc fname result)
          mapping (suggestions/create meta-data-mapping data)]
      (client-callback mapping (vec (take 50 data))))
    (catch #?(:clj Throwable :cljs :default) e
      (warn e (ex-data e))
      (failed-callback {:success false
                        :error (ex-message e)
                        :error-data (ex-data e)}))))

(defn update-options [{:keys [client-callback failed-callback]} [meta-data options-type options]]
  (let [data (get @files (:name meta-data))
        meta-data-mapping {:suggestions {:check-lines 200}
                           :meta-data (case options-type
                                        :csv
                                        {:file-format :csv
                                         :csv options})}
        data (cond :else
                   (csv-parser/parse meta-data-mapping
                                     data))
        mapping (suggestions/create meta-data-mapping data)]
    (client-callback mapping (vec (take 50 data)))))

(defn import-file [{:keys [client-callback]} [meta-data desc]]
  (try
    (let [result (get @files (:name meta-data))
          _ (when-let [result (schema/explain desc)]
              (throw (ex-info "Schema not valid" result)))
          data (cond :else
                     (csv-parser/parse desc
                                       result))
          instance (edn-json-gen/new-instance)
          data (mapping/mapping instance
                                desc
                                data)
          result (generator/finalize instance
                                     data
                                     :edn)
          state @(generator/state instance)
          {success? :success :as trans} (imp/begin-transaction "default")
          _ (when-not success?
              (throw (ex-info "Transaction could not be started" {:trans trans})))
          result (imp/transform->import result {} "default")]
      (client-callback (assoc result
                              :mapping-errors (:errors state))))
    (catch #?(:clj Throwable :cljs :default) e
      (error e "Import failed" (ex-message e) (ex-data e))
      (client-callback {:success false
                        :error (ex-message e)
                        :error-data (ex-data e)}))))

(defn commit-import [{:keys [client-callback]} _]
  (try
    (let [result (imp/commit-transaction "default")]
      (client-callback result))
    (catch #?(:clj Throwable :cljs :default) e
      (error e (ex-data e))
      (client-callback {:success false
                        :error (ex-message e)
                        :error-data (ex-data e)}))))

(defn cancel-import [{:keys [client-callback]} _]
  (try
    (let [result (imp/cancel-transaction "default")]
      (client-callback result))
    (catch #?(:clj Throwable :cljs :default) e
      (error e (ex-data e))
      (client-callback {:success false
                        :error (ex-message e)
                        :error-data (ex-data e)}))))

(defn delete-file [{:keys [client-callback]} [meta-data]]
  (try
    (imp/cancel-transaction "default")
    (swap! files dissoc (:name meta-data))
    (client-callback {:success true})
    (catch #?(:clj Throwable :cljs :default) e
      (error e)
      (client-callback {:error (ex-message e)
                        :error-data (ex-data e)}))))
