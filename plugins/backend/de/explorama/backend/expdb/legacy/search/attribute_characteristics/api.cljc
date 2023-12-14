(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.api
  (:require [clojure.string :as string]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.buckets :as buckets]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.core :as core]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.graph :as graph]
            [de.explorama.backend.expdb.persistence.indexed :as persistence]
            [de.explorama.backend.expdb.query.graph :as ngraph]
            [de.explorama.shared.common.data.attributes :as attrs]))

(defn- string-bucket [bucket]
  (if (keyword? bucket)
    (name bucket)
    bucket))

(defn attribute-types [{:keys [blocklist attribute]
                        :or {blocklist #{}}}]
  (let [all-types (graph/get-attribute-types)]
    (if attribute
      (select-keys all-types [attribute])
      (apply dissoc all-types blocklist))))

(defn- possible-attributes [blacklist formdata]
  (filterv (fn [node]
             (not (blacklist node)))
           (if
            (empty? formdata) (core/all-attributes)
            (core/attributes-formdata formdata))))

(defn- search-attributes [blacklist formdata search-term]
  (let [possible-attrs (possible-attributes blacklist formdata)
        search-query (string/lower-case search-term)
        selected-attributes (set (map #(get-in % [0 0]) formdata))]
    (filterv (fn [[attribute]]
               (and (string/includes? (string/lower-case attribute)
                                      search-query)
                    (not (selected-attributes attribute))))
             possible-attrs)))

(defn- sort-by-fn [{:keys [limit order by] :as desc}]
  (if (nil? desc)
    identity
    (let [comparer (if (= order :asc)
                     #(compare %1 %2)
                     #(compare %2 %1))
          sort-fn (case by
                    :value (fn [[_ _ value]]
                             value)
                    :name (fn [[_ name]]
                            name)
                    :label (fn [[label]]
                             label)
                    identity)]
      (fn [data]
        (->> data
             (sort-by sort-fn comparer)
             (take limit)
             vec)))))

(defn attributes [{:keys [formdata blocklist
                          allowed-types search-term]
                   sort-by-desc :sort-by
                   :or {blocklist #{}}}]
  (let [sort-by-fn (sort-by-fn sort-by-desc)]
    (cond
      (seq allowed-types) (->> (core/reduced-acs formdata)
                               (filterv (fn [[node-label attr]]
                                          (and (allowed-types node-label)
                                               (not (blocklist [attr node-label])))))
                             ;=> directsearch-attributes for the config (allowed-types) 
                               sort-by-fn)
      (seq search-term) (search-attributes blocklist formdata search-term)
      :else (possible-attributes blocklist formdata))))

(defn- sort-data [data {:keys [limit order by]}]
  (->> data
       (sort-by (case by
                  :alpha identity
                  identity)
                (if (= order :asc)
                  #(compare %1 %2)
                  #(compare %2 %1)))
       (take limit)
       vec))

(defn- merge-and-sort [data {:keys [mode]
                             :as desc
                             :or {mode :each}}]
  (case mode
    :merge {["sort-by" "Aggregation"]
            (sort-data (reduce (fn [acc [key values]]
                                 (into acc
                                       (mapv (fn [value]
                                               (conj key value))
                                             values)))
                               []
                               data)
                       desc)}
    :each (reduce (fn [acc [key data]]
                    (assoc acc key (sort-data data desc)))
                  {}
                  data)))

(defn attribute-values
  [{:keys [formdata attributes search-term sort-by bucket]}]
  (let [bucket (string-bucket bucket)
        all-attribute-options (:result-options (if bucket
                                                 (core/attribute-options attributes formdata bucket)
                                                 (core/attribute-options attributes formdata)))]
    (cond-> (if (seq search-term)
              (let [lower-search-term (string/lower-case search-term)]
                (into {}
                      (map (fn [[attr vals]]
                             [attr (filterv #(and (string? %)
                                                  (seq %)
                                                  (string/includes?
                                                   (string/lower-case %)
                                                   lower-search-term))
                                            vals)]))
                      all-attribute-options))
              all-attribute-options)
      sort-by
      (merge-and-sort sort-by))))

(defn- x-list [var ignore-str ignore-fn default-fn]
  (cond (vector? var)
        (set var)
        (and (string? var)
             (= ignore-str var))
        ignore-fn
        (string? var)
        (let [var (string/lower-case var)]
          (fn [node-value]
            (when node-value
              (string/includes? (string/lower-case node-value)
                                var))))
        :else
        default-fn))

(defn- allow-list-fn [[label? attr-name? value?]]
  (let [label-fn (x-list label? "*" (constantly true) (constantly true))
        attr-name? (x-list attr-name? "*" (constantly true) (constantly true))
        value? (x-list value? "*" (constantly true) (constantly true))]
    (fn [[label attr-name attr-value]]
      (and (label-fn label)
           (attr-name? attr-name)
           (value? attr-value)))))

(defn- block-list-fn [[label? attr-name? value?]]
  (let [label-fn (x-list label? "_" (constantly false) (constantly false))
        attr-name? (x-list attr-name? "_" (constantly false) (constantly false))
        value? (x-list value? "_" (constantly false) (constantly false))]
    (fn [[label attr-name attr-value]]
      (or (label-fn label)
          (attr-name? attr-name)
          (value? attr-value)))))

(defn- sort-by-fn-nei [{:keys [limit order by] :as desc}]
  (if (nil? desc)
    identity
    (let [comparer (if (= order :asc)
                     #(compare %1 %2)
                     #(compare %2 %1))
          sort-fn (case by
                    :value (fn [[_ _ value]]
                             value)
                    :name (fn [[_ name]]
                            name)
                    :label (fn [[label]]
                             label)
                    identity)]
      (fn [data]
        (cond->> data
          :always
          (sort-by sort-fn comparer)
          limit
          (take limit)
          :always
          vec)))))

(defn neighborhood [{:keys [nodes bucket operation]
                     pattern :pattern
                     allow-list :allow-list
                     block-list :block-list
                     sort-by :sort-by
                     :or {bucket "default"}}]
  (core/neighborhood (get-in config-expdb/explorama-bucket-config
                             [bucket :schema])
                     nodes
                     pattern
                     (or operation :intersection)
                     (if allow-list
                       (allow-list-fn allow-list)
                       (constantly true))
                     (if block-list
                       (block-list-fn block-list)
                       (constantly false))
                     (if sort-by
                       (sort-by-fn-nei sort-by)
                       identity)))

(defn- update-aggregates [[min1-value max1-value]
                          [min2-value max2-value]]
  [(cond (and min1-value min2-value)
         (min min1-value min2-value)
         min1-value min1-value
         min2-value min2-value)
   (cond (and max1-value max2-value)
         (max max1-value max2-value)
         max1-value max1-value
         max2-value max2-value)])

(defn ranges [{:keys [buckets attributes countries datasources years]}]
  (->> (reduce (fn [acc bucket]
                 (->> (filter (fn [{country attrs/country-attr
                                    datasource attrs/datasource-attr
                                    year attrs/year-attr}]
                                (and (or (not countries)
                                         (and countries
                                              (countries country)))
                                     (or (not datasources)
                                         (and datasources
                                              (datasources datasource)))
                                     (or (not years)
                                         (and years
                                              (years year)))))
                              (ngraph/dts-full bucket))
                      (persistence/get-meta-data (buckets/new-instance bucket :indexed))
                      vals
                      (into []
                            (comp (filter (fn [{ranges :ranges}]
                                            (or (not attributes)
                                                (and attributes
                                                     (seq (select-keys ranges attributes))))))
                                  (map (fn [{ranges :ranges}]
                                         ranges))))
                      (reduce (fn [acc ranges]
                                (reduce-kv (fn [acc key value]
                                             (update acc key update-aggregates value))
                                           acc
                                           ranges))
                              acc)))
               {}
               (if (seq buckets)
                 buckets
                 (keys config-expdb/explorama-bucket-config)))
       (into {}
             (map (fn [[key [min max]]]
                    [key {:min min
                          :max max}])))))
