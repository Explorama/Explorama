(ns de.explorama.shared.mosaic.group-by-layout
  (:require [clojure.string :as string]))

(defn transform-single-layout [layout layout-idx]
  (let [color-map (get-in layout [:color-scheme :colors])
        value-assigned (:value-assigned layout)
        color-vals (filter (fn [[_ attr-vals]] (seq attr-vals))
                           (for [n (range (count value-assigned))]
                             [(get color-map  (keyword (str n)))
                              (get value-assigned n)]))
        colors  (->> (map-indexed vector color-vals)
                     (group-by (fn [[_ [color _]]]
                                 color))
                     (map (fn [[k details]]
                            [k (map (fn [[color-idx [_ characteristics]]]
                                      [color-idx characteristics])
                                    details)]))
                     (into {}))
        {:keys [name attribute-type attributes]} layout]
    [layout-idx
     {:colors colors
      :name name
      :attribute-type attribute-type
      :attributes attributes}]))

(defn build-layout-lookup-table [layouts]
  (reduce (fn [acc [layout layout-idx]]
            (assoc acc (:id layout)
                   (transform-single-layout layout layout-idx)))
          {}
          (partition 2 (interleave layouts (range)))))

(defn get-layout-and-color-idx [lookup-table layout-id color-code]
  (let [{[layout-idx layout-lookup] layout-id} lookup-table
        color-idx (ffirst (get-in layout-lookup [:colors color-code]))]
    [layout-idx  color-idx]))

(defn get-group-text [lookup-table layout-id color-code attr->display-name labels format-number-fn]
  (let [{[_ {:keys [attribute-type colors attributes]}] layout-id}
        lookup-table
        associated-values (map second (get colors color-code))
        ending (str "("
                    (clojure.string/join ", " (map #(attr->display-name % labels)
                                                   attributes))
                    ")")
        prefix (if (= attribute-type "number")
                 (clojure.string/join ", "
                                      (mapv (fn [l]
                                              (clojure.string/join " - " (map format-number-fn l)))
                                            associated-values))
                 (clojure.string/join ", "
                                      (map (fn [l]
                                             (clojure.string/join ", " l))
                                           associated-values)))]
    (if (nil? layout-id)
      "Unmatched"
      (str prefix " " ending))))

(defn generate-compare-by-group-text [layouts attr->display-name labels format-number-fn]
  (let [lookup-table (build-layout-lookup-table layouts)]
    (fn [l1 l2]
      (compare (get-group-text lookup-table
                               (get l1 "id")
                               (get l1 "color")
                               attr->display-name
                               labels
                               format-number-fn)
               (get-group-text lookup-table
                               (get l2 "id")
                               (get l2 "color")
                               attr->display-name
                               labels
                               format-number-fn)))))

(defn generate-layout-compare [layouts]
  (fn [l1 l2]
    (if (nil? (get l1 "id"))
      1
      (if (nil? (get l2 "id"))
        -1
        (let [lookup-table (build-layout-lookup-table layouts)
              [l1-ordinal l1-color-no] (get-layout-and-color-idx lookup-table
                                                                 (get l1 "id")
                                                                 (get l1 "color"))
              [l2-ordinal l2-color-no] (get-layout-and-color-idx lookup-table
                                                                 (get l2 "id")
                                                                 (get l2 "color"))
              c (compare l1-ordinal l2-ordinal)]
          (if (not= c 0)
            c
            (compare l1-color-no l2-color-no)))))))