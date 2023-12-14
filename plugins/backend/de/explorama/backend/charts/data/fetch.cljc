(ns de.explorama.backend.charts.data.fetch
  (:require [data-format-lib.core :as dfl]
            [de.explorama.backend.charts.attribute-characteristics :as acs]
            [de.explorama.backend.charts.config :as config]
            [de.explorama.backend.common.aggregation :as common-aggregation]
            [de.explorama.backend.common.calculations.data-acs :as calc-acs]
            [de.explorama.backend.common.middleware.cache :as cache]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [taoensso.timbre :refer [debug error]]
            [taoensso.tufte :as tufte]))

(defn- get-full-data [data-instance]
  (try
    (tufte/p ::merge-data
             (dfl/transform data-instance
                            ac-api/data-tiles-ref-api
                            cache/lookup
                            :attach-buckets? true
                            :post-fn (fn [result]
                                       ;TODO r1/charts handle this when we have caching and this does not matter
                                       (when (:di/aggregation-caching? data-instance)
                                         (cache/index-aggregation-result data-instance result))
                                       result)))
    (catch #?(:clj Throwable :cljs :default) e
      (error e
             "Unable to retrieve your data."
             {:data-instance data-instance})
      [])))

(defn- get-data-acs [data]
  (when (seq data)
    (calc-acs/data-acs data)))

(defn- di-desc [data filtered-data local-filter]
  (-> (common-aggregation/calculate-dimensions data)
      (assoc :filtered-data-info
             (when local-filter
               (-> (common-aggregation/calculate-dimensions filtered-data)
                   (select-keys [:countries :years :datasources]))))))

(defn- data-keys [data]
  (-> (into #{} (mapcat keys) data)
      (disj "bucket")))

(defn- ui-options [data volatile-acs]
  (acs/ui-options-for-data data
                           (data-keys data)
                           volatile-acs))

(defn di-data [{:keys [di
                       client-ui-options
                       local-filter]
                {:keys [data-keys? options?
                        di-desc? data-acs?
                        external-refs?]} :calc
                {di-acs :di/acs} :di}]
  (debug "Fetch datainstance" {:di di
                               :client-ui-options client-ui-options
                               :local-filter local-filter})
  (let [data (get-full-data di)
        data-count (count data)
        too-much-data? (> data-count
                          config/explorama-charts-max-data-amount)
        _ (when too-much-data?
            (throw (ex-info "Too much data" {:error :too-much-data
                                             :data-count data-count
                                             :max-data-amount config/explorama-charts-max-data-amount})))
        filtered-data (if local-filter
                        (dfl/filter-data local-filter data)
                        data)
        filtered-count (if local-filter
                         (count filtered-data)
                         data-count)
        stop-filterview? (and (number? config/explorama-charts-stop-filterview-amount)
                              (> data-count config/explorama-charts-stop-filterview-amount))
        warn-filterview? (and (number? config/explorama-charts-warn-filterview-amount)
                              (> data-count config/explorama-charts-warn-filterview-amount))]
    (cond-> {:data filtered-data
             :data-count data-count
             :filtered-count filtered-count
             :stop-filterview? stop-filterview?
             :warn-filterview? warn-filterview?}
      local-filter (assoc :local-filter local-filter)
      di-desc? (assoc :di-desc (di-desc data filtered-data local-filter))
      data-acs? (assoc :data-acs (get-data-acs data))
      external-refs? (assoc :external-refs (:di/external-ref di))
      data-keys? (assoc :filtered-data-keys (data-keys data))
      options? (assoc :options (ui-options data di-acs)))))


