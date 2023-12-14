(ns de.explorama.backend.map.data.core
  (:require [data-format-lib.core :as dfl]
            [de.explorama.backend.common.calculations.data-acs :as acs]
            [de.explorama.backend.common.environment.probe :as probe]
            [de.explorama.backend.common.middleware.cache :as cache]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [taoensso.timbre :refer [error]]
            [taoensso.tufte :as tufte]))

(defonce store (atom {}))

(defn get-data [data-instance]
  (try
    (tufte/p ::merge-data
             (dfl/transform data-instance
                            ac-api/data-tiles-ref-api
                            cache/lookup
                            :attach-buckets? true
                            :post-fn (fn [result]
                                       ;TODO r1/caching handle this when we have caching and this does not matter
                                       (when (:di/aggregation-caching? data-instance)
                                         (cache/index-aggregation-result data-instance result))
                                       result)))
    (catch #?(:clj Throwable :cljs :default) e
      (probe/rate-exception e)
      (error e
             "Unable to retrieve your data."
             {:data-instance data-instance})
      [])))

(defn get-data-acs [di]
  (when di
    (let [data (get-data di)]
      (acs/data-acs data))))

(defn update-filtered-data [data filter]
  (let [filtered-data (dfl/filter-data filter data)]
    filtered-data))
