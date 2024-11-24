(ns de.explorama.backend.indicator.data.core
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [de.explorama.shared.data-format.core :as dfl]
            [de.explorama.shared.data-format.data-instance :as dfl-di]
            [de.explorama.shared.data-format.filter-functions :as ff]
            [de.explorama.shared.data-format.operations :as dfl-op]
            [de.explorama.backend.indicator.config :as config]
            [de.explorama.backend.common.middleware.cache :as cache]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [de.explorama.backend.indicator.attribute-characteristics :as acs]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.data.data-tiles :as explorama-tiles]
            [taoensso.timbre :refer [error]]))

(defonce store (atom {}))

(defn data-tile-desc [indicator-desc]
  {(explorama-tiles/access-key "identifier") "indicator"
   (explorama-tiles/access-key "description") indicator-desc})

(def empty-filter [:and])
(def empty-filter-id (dfl-di/ctn->sha256-id empty-filter))

(defn generate-di [{:keys [calculation-desc dis]}]
  (let [merged-dis (reduce (fn [acc [_ di]]
                             (-> (update acc :di/data-tile-ref (fnil merge {}) (get di :di/data-tile-ref))
                                 (update :di/filter (fnil merge {}) (get di :di/filter))))
                           {}
                           dis)]
    (assoc merged-dis
           :di/operations
           (walk/postwalk (fn [value]
                            (if (and (string? value)
                                     (get dis value))
                              (get-in dis [value :di/operations])
                              value))
                          calculation-desc)
           :di/aggregation-caching? true)))

(defn- load-data [di]
  (try
    (let [filtered-data (dfl/transform di
                                       ac-api/data-tiles-ref-api
                                       cache/lookup
                                       :post-fn (fn [result]
                                                  ;TODO r1/caching handle this when we have caching and this does not matter
                                                  (when (:di/aggregation-caching? di)
                                                    (cache/index-aggregation-result di result))
                                                  result))]
      (when (seq filtered-data)
        (swap! store assoc di filtered-data))
      [filtered-data di])
    (catch #?(:clj Throwable :cljs :default) e
      (error e "Unable to retrieve your data."
             {:data-instance di})
      [[] di])))

(defn get-data [di]
  (when di
    (if-let [data (get @store di)]
      [data di]
      (load-data di))))

(defn load-di-data [dis local-filters]
  (as-> dis $
    (into {}
          (map (fn [[di-num di]]  ;di-num identifies the data inside the calculation-desc
                 [di-num (first (get-data di))]))
          $)
    (reduce (fn [acc [di-key local-filter]]
              (update acc
                      di-key
                      (fn [di-data]
                        (if local-filter
                          (dfl/filter-data local-filter di-data)
                          di-data))))
            $
            local-filters)))

(defn- group-contiguous-intervals
  "Returns the numbers in the seqable `numbers` as a seq of intervals.
  If `interval` is provided, the intervals from `numbers` are appended
  to it. `numbers` should contain a strictly increasing sequence of integers.
  The intervals returned are maximal in the sense that they cannot be extended
  by adding a number from `numbers` to them. The intervals are returned in
  increasing order."
  ([numbers]
   (when-let [[n & ns] (seq numbers)]
     (group-contiguous-intervals ns [n])))
  ([numbers interval]
   (lazy-seq
    (if-let [[n & ns] (seq numbers)]
      (if (= (dec n) (last interval))
        (group-contiguous-intervals ns [(first interval) n])
        (cons interval (group-contiguous-intervals ns [n])))
      [interval]))))

(defn- calculate-years [data]
  (let [years (->> data
                   (into #{} (map #(-> (get % (attrs/access-key "date"))
                                       (subs 0 4)
                                       edn/read-string)))
                   sort)]
    [(str/join ", " (map (fn [[l u]] (if u (str l \- u) (str l)))
                         (group-contiguous-intervals years)))
     years]))

(defn- calculate-countries [data]
  (let [countries (->> data
                       (reduce (fn [acc {country (attrs/access-key "country")}]
                                 (if (vector? country)
                                   (into acc country)
                                   (conj acc country)))
                               #{})
                       sort)]
    [(str/join ", " countries) countries]))

(defn- event-datasource [event]
  (attrs/value event attrs/datasource-attr))

(defn- calculate-datasource [data]
  (let [datasources (sort (into #{} (map event-datasource) data))]
    [(str/join ", " datasources) datasources]))

(defn connect-to-di
  "Retriving the data defined by the di.
   Returns attributes that can be used for indicator creation."
  [{:keys [client-callback]} [{:keys [di local-filter volatile-acs indicator-id frame-id]}]]
  (let [[filtered-data _] (get-data di)
        filtered-data (if local-filter
                        (dfl/filter-data local-filter filtered-data)
                        filtered-data)
        data-count (count filtered-data)
        calc-years (calculate-years filtered-data)
        calc-countries (calculate-countries filtered-data)
        calc-datasources (calculate-datasource filtered-data)
        filtered-data-keys (into #{} (mapcat keys) filtered-data)
        ui-options (acs/ui-options-for-data filtered-data-keys
                                            volatile-acs)]
    (client-callback {:di di
                      :frame-id frame-id
                      :local-filter local-filter
                      :data-count data-count
                      :di-desc {:years calc-years
                                :countries calc-countries
                                :datasources calc-datasources}
                      :ui-options ui-options
                      :indicator-id indicator-id})))

(defn- random-data-sample [sample-size data]
  (if (< (count data) sample-size)
    data
    (let [prob (/ (+ sample-size 100) ; this makes it probly more smooth
                  (count data))]
      (into []
            (comp
             (random-sample prob)
             (take sample-size))
            data))))

(defn- create-data-samples [sample-size data]
  (if sample-size
    (into {}
          (map (fn [[di data]]
                 [di
                  (random-data-sample sample-size data)]))
          data)
    data))

(defn data-sample
  ([{:keys [client-callback]} [{:keys [calculation-desc dis local-filters]}]]
   (let [data (load-di-data (map (fn [[di-num di]]
                                   [di-num di])
                                 dis)
                            local-filters)]
     (client-callback (data-sample config/explorama-indicator-data-sample-size
                                   config/explorama-indicator-data-sample-size-each-di
                                   data
                                   calculation-desc))))
  ([data-sample-size data-sample-size-each-di data calculation-desc]
   (->> (dfl-op/perform-operation (create-data-samples data-sample-size-each-di data)
                                  nil
                                  calculation-desc
                                  ff/default-impl)
        (take data-sample-size)
        vec)))