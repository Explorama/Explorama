(ns de.explorama.backend.expdb.persistence.shared
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [de.explorama.backend.common.middleware.cache-invalidate :as cache-invalidate]
            [de.explorama.backend.expdb.buckets :as buckets]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.core :as legacy-core]
            [de.explorama.backend.expdb.persistence.indexed :as pi]
            [de.explorama.backend.expdb.query.index :as idx]
            [de.explorama.backend.expdb.spec :as spec]
            [de.explorama.backend.expdb.utils :refer [expdb-hash]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.date.utils :refer [complete-date
                                                           date-convert]]
            [de.explorama.shared.common.unification.misc
             :refer [double-negativ-infinity double-positiv-infinity]]
            [de.explorama.shared.common.unification.time :refer [formatters
                                                                 now unparse]]
            [taoensso.timbre :refer [error]]))

(def ^:private transactions (atom {}))

(def ^:private formatter (formatters :basic-date-time-no-ms))

(defn- facts-> [feat-facts]
  (when-not (empty? feat-facts)
    (mapv (fn [{:keys [type value name]}]
            [name type value])
          feat-facts)))

(defn- contexts-> [contexts-nodes context-refs]
  (mapv (fn [{:keys [global-id rel-type rel-name]}]
          (if-let [context (get contexts-nodes global-id)]
            (into context
                  [rel-type rel-name])
            (throw (ex-info "Context is missing"
                            {:global-id global-id}))))
        context-refs))

(defn- locations-> [locations]
  (mapv (fn [{:keys [lat lon]}]
          [lat lon])
        locations))

(defn- dates-> [dates]
  (mapv (fn [{:keys [type value]}]
          [type value])
        dates))

(defn- notes-> [text]
  text)

#_datasource-gid
(defn- datasource-> [datasource]
  [(:name datasource)])

(defn- date-acs [year & [month day hour minute second]]
  (cond-> {"year" #{(str year)}}
    month
    (assoc "month"
           #{(str year "-" (complete-date month))})
    day
    (assoc "day"
           #{(str year "-" (complete-date month) "-" (complete-date day))})
    hour
    (assoc "hour"
           #{(str year "-" (complete-date month) "-" (complete-date day)
                  "T" (complete-date hour))})
    minute
    (assoc "day"
           #{(str year "-" (complete-date month) "-" (complete-date day)
                  "T" (complete-date hour) ":" (complete-date minute))})
    second
    (assoc "day"
           #{(str year "-" (complete-date month) "-" (complete-date day)
                  "T" (complete-date hour) ":" (complete-date minute) ":" (complete-date second))})))

(def date-acs-memo (memoize date-acs))

(defn transform->table [data bucket]
  ;TODO r1/db use a context graph and not insert contexts hard
  (let [contexts-nodes (into {}
                             (map (fn [{:keys [type global-id name]}]
                                    [global-id [type name]]))
                             (:contexts data))
        datasource (:datasource data)
        created-date ["created-at" (unparse formatter (now))]
        min-max-val [double-positiv-infinity double-negativ-infinity]]
    (persistent!
     (reduce (fn [acc {:keys [global-id features]}]
               (reduce (fn [acc {:keys [facts locations context-refs dates texts]}]
                         (when (empty? contexts-nodes)
                           (throw (ex-info "Input is not valid a contexts are missing" {})))
                         (let [ds (datasource-> datasource)
                               contexts (contexts-> contexts-nodes context-refs)
                               locations (locations-> locations)
                               facts (facts-> facts)
                               notes (notes-> texts)
                               dates (dates-> dates)
                               dates (conj dates created-date)
                               event [global-id
                                      dates
                                      ds
                                      facts
                                      contexts
                                      locations
                                      notes]
                               {day :day} ;TODO r1/db handle dates correctly
                               (date-convert (get-in (filterv (fn [[type]] (= type "occured-at")) dates)
                                                     [0 1]))
                               year (first day)
                               data-tile-keys (->>
                                               (filter (fn [[type]]
                                                         (= type "country"))
                                                       contexts)
                                               (map (fn [[_ country]]
                                                      {"year" (str year)
                                                       "country" country
                                                       "datasource" (first ds)
                                                       "bucket" (name bucket) ;TODO r1/db name on keyword? feels bad
                                                       "identifier" "search"}))) ;TODO r1/db this hsould be either schema or a separate value
                               acs (merge (cond-> {"datasource" #{(first ds)}}
                                            notes
                                            (assoc "notes" #{"notes"}))
                                          (reduce (fn [acc [type name]]
                                                    (update acc type conj name))
                                                  (into {} (map (fn [[type]] [type #{}]) contexts))
                                                  contexts)
                                          (reduce (fn [acc [name type]]
                                                    (update acc name conj type))
                                                  (into {} (map (fn [[name]] [name #{}]) facts))
                                                  facts)
                                          (apply date-acs-memo day)
                                          (if (seq locations)
                                            {attrs/location-attr #{"location"}}
                                            {}))
                               attributes (merge (cond-> {"datasource" "Datasource"
                                                          "year" "Date"
                                                          "month" "Date"
                                                          "day" "Date"}
                                                   (not (str/blank? notes))
                                                   (assoc "notes" "Notes"))
                                                 (reduce (fn [acc [type]]
                                                           (assoc acc type "Context"))
                                                         {}
                                                         contexts)
                                                 (reduce (fn [acc [name]]
                                                           (assoc acc name "Fact"))
                                                         {}
                                                         facts)
                                                 (if (seq locations)
                                                   {attrs/location-attr "Context"}
                                                   {}))
                               ranges (->> (filter (fn [[_ type]]
                                                     (#{"integer" "decimal"} type))
                                                   facts)
                                           (reduce (fn [acc [name _ value]]
                                                     (let [val (get acc name min-max-val)]
                                                       (assoc acc
                                                              name
                                                              (-> val
                                                                  (update 0 min value)
                                                                  (update 1 max value)))))
                                                   {}))]
                           (reduce (fn [acc data-tile-key]
                                     (let [data-tile (get acc data-tile-key)]
                                       (assoc! acc
                                               data-tile-key
                                               (-> (update data-tile :data (fnil assoc {}) global-id event)
                                                   (update :acs (fn [old-acs]
                                                                  (if old-acs
                                                                    (merge-with set/union
                                                                                old-acs
                                                                                acs)
                                                                    acs)))
                                                   (update :attributes (fn [old-attributes]
                                                                         (if old-attributes
                                                                           ;TODO r1/db only one type per attribute per data-tile allowed
                                                                           (merge old-attributes
                                                                                  attributes)
                                                                           attributes)))
                                                   (update :ranges (fn [old-ranges]
                                                                     (merge-with (fn [old new]
                                                                                   (-> (update old 0 min (first new))
                                                                                       (update 1 max (second new))))
                                                                                 old-ranges
                                                                                 ranges)))
                                                   (assoc :key data-tile-key)
                                                   (assoc :hash (expdb-hash data-tile-key))))))
                                   acc
                                   data-tile-keys)))
                       acc
                       features))
             (transient {})
             (:items data)))))

(defn begin-transaction [bucket]
  (locking transactions
    (if (get @transactions bucket)
      {:success false
       :message "There is already a transaction running"}
      (do
        (swap! transactions assoc bucket {})
        {:success true
         :bucket bucket}))))

(defn commit-transaction [bucket]
  (locking transactions
    (if (or (not (get @transactions bucket))
            (and (get @transactions bucket)
                 (not (get-in @transactions [bucket :data-tiles]))))
      {:success false
       :message "No active transaction"}
      (let [instance (buckets/new-instance bucket :indexed)
            {new-data-tiles :data-tiles
             options :options}
            (get @transactions bucket)
            result (pi/import-data instance
                                   new-data-tiles
                                   options)]
        (idx/add-index! instance new-data-tiles)
        (legacy-core/update-acs!)
        (cache-invalidate/send-invalidate #{"ac" "data"}
                                          (reduce (fn [acc dt-key]
                                                    (reduce-kv (fn [acc k v]
                                                                 (update acc k (fnil conj #{}) v))
                                                               acc
                                                               dt-key))
                                                  {}
                                                  (keys new-data-tiles)))
        (swap! transactions dissoc bucket)
        result))))

(defn cancel-transaction [bucket]
  (locking transactions
    (if (get @transactions bucket)
      (do (swap! transactions dissoc bucket)
          {:success true})
      {:success false
       :message "No active transaction"})))

(defn transform->import [data options bucket]
  (let [instance (buckets/new-instance bucket :indexed)
        data (spec/decode data)]
    (if (spec/validate data)
      (try
        (let [new-data-tiles (transform->table data bucket)
              events (reduce (fn [acc data-tile]
                               (+ acc (count (:data data-tile))))
                             0
                             (vals new-data-tiles))]
          (if (get @transactions bucket)
            (locking transactions
              (when (get-in @transactions [bucket :data-tiles])
                (throw (ex-info "There is only a transaction running"
                                {:bucket bucket})))
              (swap! transactions
                     assoc-in
                     [bucket :data-tiles]
                     new-data-tiles)
              (swap! transactions
                     assoc-in
                     [bucket :options]
                     options)
              {:success true
               :events events
               :data-tiles (count new-data-tiles)})
            (let [result (pi/import-data instance
                                         new-data-tiles
                                         options)]
              (idx/add-index! instance new-data-tiles)
              (legacy-core/update-acs!)
              (cache-invalidate/send-invalidate #{"ac" "data"}
                                                (reduce (fn [acc dt-key]
                                                          (reduce-kv (fn [acc k v]
                                                                       (update acc k (fnil conj #{}) v))
                                                                     acc
                                                                     dt-key))
                                                        {}
                                                        (keys new-data-tiles)))
              result)))
        (catch #?(:clj Throwable :cljs :default) e
          (error e "Import failed")
          {:success false
           :message (ex-message e)
           :data {:error-data (ex-data e)}}))
      {:success false
       :result (spec/explain data)})))

(defn delete-datasource [bucket datasource]
  (try
    (let [instance (buckets/new-instance bucket :indexed)
          data-tiles (:data-tiles (pi/delete instance datasource))]
      (idx/rm-index! instance data-tiles)
      (legacy-core/update-acs!)
      (cache-invalidate/send-invalidate #{"ac" "data"}
                                        (reduce (fn [acc dt-key]
                                                  (reduce-kv (fn [acc k v]
                                                               (update acc k (fnil conj #{}) v))
                                                             acc
                                                             dt-key))
                                                {}
                                                data-tiles))
      {:success false
       :data-tiles (count data-tiles)})
    (catch #?(:clj Throwable :cljs :default) e
      (error "Delete failed" e)
      {:success false
       :message (ex-message e)
       :data {:error-data (ex-data e)}})))
