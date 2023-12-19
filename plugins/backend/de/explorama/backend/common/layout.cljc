(ns de.explorama.backend.common.layout
  (:require [clojure.set :as set]
            [de.explorama.backend.common.scales :as scale]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]
            [de.explorama.shared.configuration.defaults :as defaults]))

(defn layout-ranges [dim-info datasource-set]
  (let [ranges (if (empty? (:buckets dim-info))
                 []
                 (ac-api/attribute-ranges (->> (map (fn [[key val]]
                                                      (if (= key :years)
                                                        [key (set (map str val))]
                                                        [key (set val)]))
                                                    dim-info)
                                               (into {}))))]
    (cond (seq ranges)
          (let [[attribute {:keys [min max]}]
                (first (reverse (sort-by (fn [[_ {:keys [min max]}]]
                                           (- max min))
                                         ranges)))
                step (scale/round-value (/ (- max min) 4))
                min (scale/round-value min)
                attribute-range [[##-Inf (+ min (* step 1))]
                                 [(+ min (* step 1)) (+ min (* step 2))]
                                 [(+ min (* step 2)) (+ min (* step 3))]
                                 [(+ min (* step 3)) (+ min (* step 4))]
                                 [(+ min (* step 4)) ##Inf]]]
            [:num attribute attribute-range])
          (< 1 (count datasource-set))
          [:datasource (conj (mapv (fn [ds] (if (nil? ds)
                                              []
                                              [ds]))
                                   (take 4 datasource-set))
                             ["*"])]
          :else nil)))

(defn- create-layout [_ds-acs dim-info datasource-set]
  (let [ranges (layout-ranges dim-info datasource-set)]
    [(case (first ranges)
       :num
       (let [[_ attribute attribute-range] ranges]
         {:id (str "generated-default-layout-" (cljc-uuid))
          :name "Default Layout"
          :timestamp 1627662367930
          :default? true
          :color-scheme (defaults/default-colors "colorscale1")
          :attributes [attribute]
          :attribute-type "number"
          :value-assigned attribute-range
          :card-scheme "scheme-2"
          :field-assignments [["else" "date"]
                              ["else" "datasource"]
                              ["else" attribute]
                              ["location" "country"]]})
       :datasource
       {:id (str "country-default-layout-" (cljc-uuid))
        :name "Datasource Layout"
        :timestamp 1627662367930
        :default? true
        :color-scheme (defaults/default-colors "access-colorscale2")
        :attributes ["datasource"]
        :attribute-type "string"
        :value-assigned (second ranges)
        :card-scheme "scheme-2"
        :field-assignments [["else" "date"]
                            ["else" "datasource"]
                            ["notes" "notes"]
                            ["location" "country"]]}
       {:id (str "country-default-layout-" (cljc-uuid))
        :name "Country Layout"
        :timestamp 1627662367930
        :default? true
        :color-scheme (defaults/default-colors "access-colorscale2")
        :attributes ["country"]
        :attribute-type "string"
        :value-assigned [[] ["*"] [] [] []]
        :card-scheme "scheme-2"
        :field-assignments [["else" "date"]
                            ["else" "datasource"]
                            ["notes" "notes"]
                            ["location" "country"]]})]))

(defn- find-layout-based-on-attrs [ds-acs dim-info layouts]
  (let [layout-attributes (reduce (fn [accum [_ {:keys [attributes attribute-type]}]]
                                    (apply conj
                                           accum
                                           (map (fn [attribute]
                                                  [attribute
                                                   (get {"integer" "number"
                                                         "string" "string"
                                                         "decimal" "number"}
                                                        attribute-type)])
                                                attributes)))
                                  #{}
                                  layouts)
        best-attribute #{(->> (select-keys ds-acs (:datasources dim-info))
                              vals
                              (map #(map (fn [{:keys [key type]}] [key type]) %))
                              (map set)
                              (apply set/intersection layout-attributes)
                              (sort-by (fn [[_ type]]
                                         (condp = type
                                           "string" 0.5
                                           "number" 1
                                           0.1))
                                       #(compare %2 %1))
                              ffirst)}
        best-layout (some (fn [[_ {:keys [attributes] :as layout}]]
                            (when (set/subset? best-attribute (set attributes))
                              layout))
                          layouts)]
    (if best-layout [best-layout] [])))

(defn find-layout [ds-acs dim-info layouts create-layout-fn]
  (let [datasource-set (set (:datasources dim-info))
        found-layouts (find-layout-based-on-attrs ds-acs dim-info layouts)]
    (if (empty? found-layouts)
      ((or create-layout-fn create-layout) ds-acs dim-info datasource-set)
      found-layouts)))

(defn check-layouts [ds-acs dim-info layouts raw-layouts & [create-layout-fn]]
  (let [datasource-attributes (->> (select-keys ds-acs (:datasources dim-info))
                                   vals
                                   (map #(map (fn [{:keys [key]}] key) %))
                                   (map set)
                                   (apply set/union))
        usable-layouts (reduce (fn [accum {:keys [attributes] :as layout}]
                                 (if (seq (set/intersection (set attributes)
                                                            datasource-attributes))
                                   (conj accum layout)
                                   accum))
                               []
                               layouts)]
    (if (empty? usable-layouts)
      (find-layout ds-acs dim-info raw-layouts create-layout-fn)
      usable-layouts)))
