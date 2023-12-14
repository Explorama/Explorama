(ns de.explorama.backend.algorithms.data-tiles
  (:require [data-format-lib.core :as dfl-core]
            [de.explorama.backend.common.middleware.cache :as cache]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [taoensso.timbre :refer [error]]
            [taoensso.tufte :as tufte]))

(defn data-tiles-lookup [data-instance]
  (try
    (tufte/p ::merge-data
             (dfl-core/transform data-instance
                                 ac-api/data-tiles-ref-api
                                 cache/lookup))
    (catch #?(:clj Throwable :cljs :default) e
      (error e "Unable to retrieve your data."
             {:data-instance data-instance})
      [])))
