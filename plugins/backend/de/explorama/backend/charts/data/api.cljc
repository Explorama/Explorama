(ns de.explorama.backend.charts.data.api
  (:require [de.explorama.backend.charts.data.core :as charts-ops]
            [de.explorama.backend.common.middleware.cache :as data-cache]
            [taoensso.timbre :refer [debug error]]))

(defn retrieve-event-data
  "Loading details for single event e.g. for details-view"
  [{:keys [client-callback]}
   [frame-id {:keys [di event-id] :as desc}]]
  (let [event-data (data-cache/lookup-event event-id)]
    (debug "retrieved event-data" {:event-id event-id
                                   :data event-data})
    (client-callback frame-id event-data desc)))

(defn- error-handler [{:keys [client-callback failed-callback]} request-params processing-fn]
  (try
    (client-callback (processing-fn))
    (catch #?(:clj Throwable :cljs :default) e
      (let [data (ex-data e)]
        (when-not data
          (error e "Error during data preparation"))
        (failed-callback
         {;:request-params request-params
          :error-desc (if (seq data)
                        data
                        {:error :unknown})})))))

(defn chart-data [callbacks [request-params]]
  (debug "Getting chart data for: " request-params)
  (error-handler callbacks request-params (partial charts-ops/chart-data request-params)))
