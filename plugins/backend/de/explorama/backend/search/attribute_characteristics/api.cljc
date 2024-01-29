(ns de.explorama.backend.search.attribute-characteristics.api
  (:require [clojure.string :as str]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :refer [put-cache]]
            [de.explorama.shared.cache.util :refer [single-destructuring
                                                    single-return-type]]
            [de.explorama.shared.common.data.attributes :as attrs]))

(defn new-cache []
  (put-cache
   {:strategy :lru
    :size 20}

   (atom {})
   (fn [_ _])))

(defonce ^:private request-cache (atom nil))

(defn reset-cache []
  (reset! request-cache (new-cache)))

(defn- datasource-values [bucket]
  (let [bucket (cond-> bucket
                 (string? bucket) (keyword))]
    (single-destructuring
     (cache-api/lookup @request-cache
                       [[::datasource-values bucket]]
                       {:miss
                        (fn [key _]
                          (single-return-type
                           key
                           (->> (ac-api/attribute-values {:attributes [[attrs/datasource-attr
                                                                        attrs/datasource-node]]
                                                          :formdata []
                                                          :bucket bucket})
                                (reduce (fn [_ [_ values]]
                                          (vec #?(:clj (sort String/CASE_INSENSITIVE_ORDER (set values))
                                                  :cljs (sort-by str/lower-case (set values)))))
                                        nil))))}))))

(defn bucket-datasources
  "Get available datasources per bucket"
  []
  (reduce (fn [acc bucket]
            (assoc acc bucket (datasource-values bucket)))
          {}
          (keys config-expdb/explorama-bucket-config)))

(defn search-options [attributes formdata]
  (->> (ac-api/attribute-values {:formdata formdata
                                 :attributes attributes})
       (map (fn [[attr values]]
              [attr (if values
                      (->> values
                           (filter identity)
                           #?(:clj (sort String/CASE_INSENSITIVE_ORDER)
                              :cljs (sort-by str/lower-case))
                           (vec))
                      [])]))
       (into {})))

(def attribute-blocklist
  "Blocklist to define which ACs should not send to Clients. This collection
  contains [<nodetype> <attribute>] tuples."
  #{})

(defn- add-attribute-entries [attrs]
  (let [annotation-node ["annotation" "Annotation"]
        attrs (filterv #(not= % annotation-node) attrs)] ;Ensures that annotation is there
    (if (and attrs (not-empty attrs))
      (conj attrs annotation-node)
      attrs)))

(defn possible-attributes [row-attrs formdata]
  (let [attrs (ac-api/attributes {:formdata formdata
                                  :attribute-blocklist attribute-blocklist})
                                        ; Filter other Datasources when a datasource row already exists, but no value is selected
                                        ; Datasource is here a special case, because its attr-name is different in everey Bucket
        existing-data-source-node (when row-attrs
                                    (some (fn [[attr nodetype]]
                                            (when (= nodetype attrs/datasource-node)
                                              #{[attr nodetype]}))
                                          row-attrs))]
    (cond-> (add-attribute-entries attrs)
      existing-data-source-node
      (->> (filterv (fn [[attr nodetype]]
                      (or (not= nodetype attrs/datasource-node)
                          (existing-data-source-node [attr nodetype]))))))))

(defn attribute-types []
  (single-destructuring
   (cache-api/lookup @request-cache
                     [::attribute-types]
                     {:miss
                      (fn [key _]
                        (single-return-type
                         key
                         (ac-api/attribute-types {:blocklist attribute-blocklist})))})))

(defn attribute-type [attr]
  (single-destructuring
   (cache-api/lookup @request-cache
                     [[::attributes-type attr]]
                     {:miss
                      (fn [key _]
                        (single-return-type
                         key
                         (-> (ac-api/attribute-types {:blocklist attribute-blocklist
                                                      :attribute attr})
                             (get attr))))})))

(defn search-attributes [formdata search-term & [lang]]
  (ac-api/attributes {:formdata formdata
                      :blacklist attribute-blocklist
                      :search-term search-term
                      :lang lang}))

(defn search-values [formdata attribute search-term]
  (ac-api/attribute-values {:attributes [attribute]
                            :formdata formdata
                            :search-term search-term}))

(def data-tiles ac-api/data-tiles)

(defn directsearch-attributes [formdata]
  (set (ac-api/attributes {:formdata formdata
                           :blacklist attribute-blocklist})))

(def datasource-search ac-api/datasource-search)

(def grouped-search ac-api/grouped-search)