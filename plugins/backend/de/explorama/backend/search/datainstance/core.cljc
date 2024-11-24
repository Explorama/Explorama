(ns de.explorama.backend.search.datainstance.core
  (:require [de.explorama.shared.data-format.core :as dfl-core]
            [de.explorama.shared.data-format.data-instance :as di]
            [de.explorama.backend.common.middleware.cache :as idb-cache]
            [de.explorama.backend.expdb.middleware.ac :as dt-ref-api]
            [de.explorama.backend.search.config :as config-search]
            [de.explorama.backend.search.datainstance.filter :as sf]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :refer [error]]
            [taoensso.tufte :as tufte]))
(defn acs->ac-map
  "Returns a map where the key is the attribute and the value a Vector of Values."
  [acs]
  (let [possible-acs (:nodes acs)
        grouped-acs (group-by (fn [[_ [attribute]]]
                                attribute)
                              possible-acs)
        ac-map-result (into {}
                            (map (fn [[attr attribute-descs]]
                                   [attr
                                    (mapv (fn [[_ [_ attribute-val]]]
                                            attribute-val)
                                          attribute-descs)])
                                 grouped-acs))]
    ac-map-result))

(def ^:private formdata-blacklist #{attrs/country-attr attrs/datasource-attr attrs/datasource-node attrs/year-attr})

(defn reduce-formdata [formdata]
  (tufte/p ::calc-reduce-formdata
           (reduce (fn [acc [[attr node-type] attr-d]]
                     (if (or (formdata-blacklist attr)
                             (formdata-blacklist node-type))
                       acc
                       (assoc acc [attr node-type] attr-d)))
                   {}
                   formdata)))

(defn gen-di [formdata]
  (let [reduced-formdata (reduce-formdata formdata)
        filter-desc (filterv identity (sf/formdata->filter reduced-formdata))
        filter-desc-id (di/ctn->sha256-id filter-desc)
        data-tile-ref {:di/identifier config-search/plugin-string
                       :formdata (str formdata)}
        data-tile-def-id (di/ctn->sha256-id data-tile-ref)]
    (cond-> {:di/data-tile-ref {data-tile-def-id data-tile-ref}}
      (seq filter-desc) (assoc :di/operations [:filter filter-desc-id data-tile-def-id]
                               :di/filter {filter-desc-id filter-desc}))))

(defn- get-data [di & opts]
  (apply dfl-core/transform
         di
         dt-ref-api/data-tiles-ref-api
         idb-cache/lookup
         opts))

(defn max-formdata-size [formdata]
  (let [data-instance (gen-di formdata)]
    (try
      {:size (count (get-data data-instance
                              :abort-early
                              {:data-tile-limit config-search/explorama-threshold-count-events-data-tiles
                               :result-limit config-search/explorama-threshold-count-events-filter
                               :result-chunk-size config-search/explorama-threshold-count-events-chunk-size}))
       :success true}
      (catch #?(:clj Throwable :cljs :default) e
        (let [{:keys [reason]} (ex-data e)]
          (case reason
            :result-limit
            {:size config-search/explorama-threshold-count-events-filter
             :reason reason
             :success false}
            :data-tile-limit
            {:size config-search/explorama-threshold-count-events-data-tiles
             :reason reason
             :success false}
            (do (error e "Counting data-failed, due to a unknow reason")
                {:size 0
                 :reason :unknown
                 :success false})))))))

(defn traffic-light-status [formdata]
  (let [{:keys [size] :as result} (max-formdata-size formdata)
        status
        #_{:clj-kondo/ignore [:type-mismatch]}
        (cond (>= size config-search/explorama-traffic-lights-threshold-red) :red
              (>= size config-search/explorama-traffic-lights-threshold-yellow) :yellow
              :else :green)]
    (assoc result :status status)))
