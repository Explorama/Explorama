(ns de.explorama.backend.common.data.descriptions
  (:require [de.explorama.backend.common.middleware.data :as data]))

(defn- data-source-desc [ds desc]
  [[[:data-source ds]]
   (get-in desc [ds :desc])])

(defn- temp-data-source-desc [ds desc]
  [[[:temp-data-source ds]]
   (get-in desc [ds :desc])])

(defn- attribute-desc [data-sources temp-data-sources a desc type-info]
  (into (for [data-source data-sources
              :let [ds (get data-source 2)]]
          [[[:data-source ds] [:attribute a type-info]]
           (get-in desc [ds :values a :desc])])
        (for [data-source temp-data-sources
              :let [ds (get data-source 2)]]
          [[[:temp-data-source ds] [:attribute a type-info]]
           (get-in desc [ds :values a :desc])])))

(defn- characteristic-desc [data-sources temp-data-sources a c desc type-info]
  (into (for [data-source data-sources
              :let [ds (get data-source 2)]]
          [[[:data-source ds] [:attribute a type-info] [:characteristic c]]
           (get-in desc [ds :values a :values c])])
        (for [data-source temp-data-sources
              :let [ds (get data-source 2)]]
          [[[:temp-data-source ds] [:attribute a type-info] [:characteristic c]]
           (get-in desc [ds :values a :values c])])))

(defn- hierarchy [path-value-pairs]
  (reduce (fn [m [p v]] (assoc-in m p v)) {} path-value-pairs))

(defn- attribute-type [types [name label]]
  [label (get types [name label])])

(defn datasource-desc [{:keys [datasource language]}]
  (let [lang (keyword language)]
    (get-in data/descriptions [lang datasource :desc])))

(defn simple-attribute-desc [{:keys [datasource attribute language]}]
  attribute)

(defn describe-selection [{:keys [data-source temp-data-source attribute characteristic]
                           :as selection}
                          {:keys [data-sources temp-data-sources]}
                          attribute-types
                          language]
  (let [language (keyword language)
        descs (get data/descriptions language)
        level (first (filter (or selection {})
                             [:characteristic :attribute :data-source :temp-data-source]))]
    (when level
      (->> (cond-> {}
             (= level :data-source)
             (conj (data-source-desc (get data-source 2) descs))
             (= level :temp-data-source)
             (conj (temp-data-source-desc (get temp-data-source 2) descs))
             (= level :attribute)
             (into (attribute-desc data-sources
                                   temp-data-sources
                                   (get attribute 2)
                                   descs
                                   (attribute-type attribute-types attribute)))
             (= level :characteristic)
             (into (characteristic-desc data-sources
                                        temp-data-sources
                                        (get characteristic 0)
                                        (get characteristic 2)
                                        descs
                                        (attribute-type attribute-types characteristic))))
           hierarchy))))
