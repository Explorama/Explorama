(ns de.explorama.backend.data-atlas.attribute-characteristics
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [de.explorama.backend.common.config :as config-backend]
            [de.explorama.backend.data-atlas.config :as config]
            [de.explorama.backend.data-atlas.match :as match]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :refer [put-cache]]
            [de.explorama.shared.cache.util :refer [single-destructuring
                                                    single-return-type]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.time :refer [current-ms]]
            [taoensso.timbre :refer [debug]]))

(def ^:private default-bucket :default)
(def ^:private temp-bucket :temp)

(defn new-cache []
  (put-cache
   {:strategy :lru
    :size config/explorama-data-atlas-cache-size}

   (atom {})
   (fn [_ _])))

(defonce ^:private request-cache (atom nil))

(defn reset-cache []
  (reset! request-cache (new-cache)))

(defonce ^:private types (atom nil))

(defn get-all-types []
  (reset! types (ac-api/attribute-types {})))

(defn- characteristics-by [formdata all-sources data-source temp-data-source unlimited? attribute query]
  (single-destructuring
   (cache-api/lookup
    @request-cache
    [{:type :characteristics-by
      :formdata formdata
      :unlimited? unlimited?
      :attribute attribute
      :query query}]
    {:miss
     (fn [key _]
       (single-return-type
        key
        (if unlimited?
          (-> (ac-api/attribute-values
               (cond-> {:formdata formdata
                        :attributes [[(first attribute)
                                      (second attribute)]]
                        :sort-by {:limit config/explorama-top-characteristics-max
                                  :order :asc
                                  :by :alpha
                                  :mode :merge}}
                 query
                 (assoc :search-term query)))
              (get ["sort-by" "Aggregation"])
              vec)
          (let [base-neighborhood {:operation :union
                                   :pattern [true true true]
                                   :allow-list [[attrs/feature-node attrs/context-node] "*" query]
                                   :sort-by {:limit config/explorama-top-characteristics
                                             :order :asc
                                             :by :value}}]
            (->> (cond-> []
                         ; Datasource selected, only characteristics from default-bucket
                   (and (nil? temp-data-source)
                        (seq data-source))
                   (into (ac-api/neighborhood
                          (assoc base-neighborhood
                                 :nodes [[attrs/datasource-node
                                          attrs/datasource-attr
                                          (get data-source 2)]])))

                         ; Temp Datasource selected, only characteristics from temp-bucket
                   (and (nil? data-source)
                        (seq temp-data-source)
                        attrs/datasource-attr)
                   (into (ac-api/neighborhood
                          (assoc base-neighborhood
                                 :nodes [[attrs/datasource-node
                                          attrs/datasource-attr
                                          (get temp-data-source 2)]]
                                 :bucket temp-bucket)))

                         ;Nothing selected in the ui, retrieve every characteristic
                   (and (nil? data-source)
                        (nil? temp-data-source))
                   (-> (into (ac-api/neighborhood
                              (assoc base-neighborhood
                                     :nodes (mapv (fn [source]
                                                    [attrs/datasource-node
                                                     attrs/datasource-attr
                                                     source])
                                                  all-sources))))
                       (into (ac-api/neighborhood
                              (assoc base-neighborhood
                                     :nodes (mapv (fn [source]
                                                    [attrs/datasource-node
                                                     attrs/datasource-attr
                                                     source])
                                                  all-sources)
                                     :bucket temp-bucket)))))
                 (map (fn [[label type name]] [type label name]))
                 (filter (fn [[_ _ name]] name))
                 (sort-by (fn [[_ _ name]] (str/lower-case name)))
                 vec)))))})))

(defn- attributes-by [formdata]
  (single-destructuring
   (cache-api/lookup
    @request-cache
    [{:type :attributes-by
      :formdata formdata}]
    {:miss
     (fn [key _]
       (single-return-type
        key
        (->>
         (ac-api/attributes {:formdata formdata
                             :blocklist #{["month" attrs/date-node]
                                          ["day" attrs/date-node]
                                          [attrs/year-attr
                                           attrs/date-node]
                                          [attrs/datasource-attr
                                           attrs/datasource-node]}})
         vec)))})))

(defn- datasources-by [formdata]
  (single-destructuring
   (cache-api/lookup
    @request-cache
    [{:type :datasources-by
      :formdata formdata}]
    {:miss
     (fn [key _]
       (single-return-type
        key
        (->> (ac-api/attribute-values {:formdata formdata
                                       :bucket default-bucket
                                       :attributes [[attrs/datasource-attr
                                                     attrs/datasource-node]]})
             (map (fn [[key values]]
                    (map (fn [value]
                           (conj key value))
                         values)))
             (apply concat)
             (sort-by (fn [[_ _ value]] (str/lower-case value)))
             vec)))})))

(defn- temp-datasources-by [formdata]
  (single-destructuring
   (cache-api/lookup
    @request-cache
    [{:type :temp-datasources-by
      :formdata formdata}]
    {:miss
     (fn [key _]
       (single-return-type
        key
        (when attrs/datasource-attr
          (->>
           (ac-api/attribute-values
            {:formdata formdata
             :bucket temp-bucket
             :attributes [[attrs/datasource-attr attrs/datasource-node]]})
           (map (fn [[key values]]
                  (map (fn [value]
                         (conj key value))
                       values)))
           (apply concat)
           (sort-by (fn [[_ _ value]] (str/lower-case value)))
           vec))))})))

(defn- formdata-datasource [attribute characteristic]
  (cond-> []
    (vector? attribute)
    (conj [(vec (take 2 attribute))
           {:advanced true
            :timestamp 1
            :all-values? true}])
    (vector? characteristic)
    (conj [(vec (take 2 characteristic))
           {:values [(get characteristic 2)]}])))

(defn- formdata-attribute [data-source characteristic]
  (cond-> []
    (vector? data-source)
    (conj [(vec (take 2 data-source))
           {:values [(get data-source 2)]}])
    (vector? characteristic)
    (conj [(vec (take 2 characteristic))
           {:values [(get characteristic 2)]}])))

(defn- formdata-characteristic [data-source attribute]
  (cond-> []
    (vector? data-source)
    (conj [(vec (take 2 data-source))
           {:values [(get data-source 2)]}])
    (vector? attribute)
    (conj [(vec (take 2 attribute))
           {:advanced true
            :timestamp 1
            :all-values? true}])))

(defn- group-by-query [query coll]
  (filterv (fn [[_ _ value]]
             (if (map? value)
               (or (boolean (match/match? (:label value) query))
                   (boolean (match/match? (:value value) query)))
               (boolean (match/match? value query))))
           coll))

(defn enabled-sources-vec [user-info]
  (let [temp-sources (set (map #(get % 2) (temp-datasources-by [])))
        all-sources (into temp-sources
                          (set (map #(get % 2) (datasources-by []))))]
    (if config-backend/explorama-datasource-access-control-enabled
      (let [name-enabled-sources (set (get config-backend/explorama-enabled-datasources-by-name (:username user-info)))
            role-enabled-sources (set (get config-backend/explorama-enabled-datasources-by-role (:role user-info)))]
        (vec (set/intersection all-sources (set/union name-enabled-sources role-enabled-sources))))
      (vec all-sources))))

(defn formdata-with-enabled-sources [formdata user-info]
  (let [enabled-sources (enabled-sources-vec user-info)
        used-sources (if (= 0 (count enabled-sources))
                       ["_"] ;; attributes-by needs an invalid datasource to exclude all nodes
                       enabled-sources)
        formdata-with-removed-sources (filter (fn [[[_attr-name attr-type] _]] (not= attr-type attrs/datasource-node)) formdata)
        timestamp (current-ms)]
    (cond
      (nil? enabled-sources) formdata
      (and (< 0 (count formdata)) (not= (count formdata) (count formdata-with-removed-sources))) formdata
      :else (vec (conj formdata-with-removed-sources
                       [[attrs/datasource-attr attrs/datasource-node]
                        {:values used-sources :timestamp timestamp :valid? true}])))))

(defn- request-range [datasource attributes]
  (ac-api/attribute-ranges {:attributes attributes
                            :datasources #{datasource}}))

(defn- request-ranges [attributes datasources]
  (reduce (fn [acc [_ _ ds-name]]
            (let [attribute-names (->> attributes
                                       (filter (fn [[_ node-type]] (= node-type "Fact")))
                                       (mapv (fn [[attr-name]]
                                               (if (map? attr-name)
                                                 (:value attr-name)
                                                 attr-name))))]
              (assoc acc ds-name (request-range ds-name attribute-names))))
          {}
          datasources))

(defn get-current-values [user-info {:keys [data-source temp-data-source attribute characteristic] :as selection} query]
  (debug "Start - get-current-values" (assoc selection
                                             :query query))
  (let [remove-labels-fn (fn [thing] (if (vector? thing)
                                       (mapv #(get % :value %) thing)
                                       (get thing :value)))

        attribute-values (remove-labels-fn attribute)
        characteristic-values (remove-labels-fn characteristic)

        enabled-sources-set (set (enabled-sources-vec user-info))
        datasources-unfiltered (if (vector? data-source)
                                 [data-source]
                                 (-> (formdata-datasource attribute-values characteristic-values)
                                     (datasources-by)))
        temp-datasources (if (vector? temp-data-source)
                           [temp-data-source]
                           (-> (formdata-datasource attribute-values characteristic-values)
                               (temp-datasources-by)))

        datasources (cond->> datasources-unfiltered
                      (seq enabled-sources-set)
                      (filter (fn [[_name _type value]] (contains? enabled-sources-set value)))
                      :always vec)
        _ (debug "found datasources datasources" datasources "temp-datasources" temp-datasources)
        formdata-ds (or data-source temp-data-source)
        attributes
        (cond (vector? attribute)
              [attribute]
              (vector? characteristic)
              [[(first characteristic)
                (second characteristic)
                (first characteristic)]]
              :else
              (->> (-> (formdata-attribute formdata-ds characteristic-values)
                       (formdata-with-enabled-sources user-info)
                       (attributes-by))
                   (map (fn [[name type]]
                          (let [attr {:value name :label name}]
                            (vector attr type attr))))
                   (sort-by (fn [[_ _ attr]] (str/lower-case (get attr :label))))
                   vec))

        characteristics
        (if (vector? characteristic)
          [characteristic]
          (->> (-> (formdata-characteristic formdata-ds attribute-values)
                   (formdata-with-enabled-sources user-info)
                   (characteristics-by enabled-sources-set data-source temp-data-source (vector? attribute) attribute-values query))
               (mapv (fn [[attr type char]]
                       (vector {:value attr :label attr} type char)))))]

    (debug "Done - get current-values" (assoc selection
                                              :query query))
    {:attribute-types @types
     :data-sources (cond
                     temp-data-source []
                     (and query
                          (nil? data-source)) (group-by-query query datasources)
                     :else datasources)

     :temp-data-sources (cond
                          data-source []
                          (and query
                               (nil? temp-data-source)) (group-by-query query temp-datasources)
                          :else temp-datasources)

     :attributes (if (and query
                          (nil? attribute))
                   (group-by-query query attributes)
                   attributes)
     :characteristics (if (and query
                               (nil? characteristic))
                        (group-by-query query characteristics)
                        characteristics)
     :ranges (request-ranges attributes
                             (cond
                               (seq data-source) [data-source]
                               (seq temp-data-source) [temp-data-source]
                               :else (into datasources temp-datasources)))}))
