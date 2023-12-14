(ns de.explorama.shared.mosaic.layout
  (:require [clojure.set :as set]))

(defn usable-layouts [ds-acs dim-info layouts best-layout]
  (let [datasource-attributes (->> (select-keys ds-acs (:datasources dim-info))
                                   vals
                                   (map #(map (fn [{:keys [key]}] key) %))
                                   (map set)
                                   (apply set/union))]
    (->> (filter (fn [[_ {attributes :attributes}]]
                   (seq (set/intersection (set attributes) datasource-attributes)))
                 (if (:default? best-layout)
                   (assoc layouts (:id best-layout) best-layout)
                   layouts))
         (map first))))
