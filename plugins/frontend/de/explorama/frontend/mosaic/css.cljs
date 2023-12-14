(ns de.explorama.frontend.mosaic.css
  (:require [de.explorama.frontend.mosaic.path :as gp]
            [re-frame.core :as re-frame]))

(defn create-choose-func [color-value attributes color-values]
  (if (vector? color-value)
    (cond
      (or (empty? color-value) (= "" (first color-value) (second color-value)))
      {:fn #(not true) :attr attributes}
      (and (number? (first color-value))
           (number? (second color-value))) {:fn #(and (<= (first color-value) %1) (> (second color-value) %1) %1) :attr attributes}
      :else (let [filter-set (set color-value)]
              (if (filter-set "*")
                (let [cvs (set (mapv #(second %) color-values))]
                  {:fn (fn [value] (not (cvs value))) :attr attributes})
                {:fn #(filter-set %) :attr attributes})))
    (if (= "" color-value)
      {:fn #(not true) :attr attributes}
      (if (= color-value "*")
        (let [cvs (set (mapv #(second %) color-values))]
          {:fn (fn [value] (not (cvs value))) :attr attributes})
        {:fn #(= color-value %1) :attr attributes})
      #_(if (= color-value "*") ;this would handle string as a containts of vectors
          (let [cvs (set (mapv #(second %) color-values))]
            {:fn (fn [value]
                   (not (if (vector? value)
                          (some cvs value)
                          (cvs value))))
             :attr [attr-key]})
          {:fn #(if (vector? %1)
                  (some #{color-value} %1)
                  (#{color-value} %1))
           :attr [attr-key]}))))

(defn create-layout-desc [layout-names]
  {:names layout-names
   :first (keyword (str (first layout-names) "-1"))})




(re-frame/reg-sub
 ::usable-layouts
 (fn [db [_ frame-id]]
   (get-in db (gp/usable-layouts frame-id))))

(re-frame/reg-sub
 ::raw-layouts
 (fn [db _]
   (get-in db gp/raw-layouts)))

(re-frame/reg-sub
 ::fallback-layout?
 (fn [db [_ frame-id]]
   (get-in db (gp/fallback-layout frame-id))))

(defn- prepared-layouts [layouts]
  (map (fn [[_ {:keys [color-scheme value-assigned field-assignments name] :as card-layout}]]
         (let [layout-name (or name
                               (get card-layout :id))
               layout-display-name (get card-layout :name)
               fields (mapv (fn [[idx v]]
                              [(keyword (str idx))
                               v])
                            (map-indexed vector field-assignments))
               color-map (->> (map-indexed vector value-assigned)
                              (map (fn [[idx v]]
                                     [(keyword (str idx))
                                      v]))
                              (sort-by (fn [[k _]] k))
                              vec)]
           (assoc card-layout
                  :name layout-name
                  :name-desc {:name layout-display-name}
                  :color-scheme color-scheme
                  :fields fields
                  :colors color-map)))
       layouts))

(defn transform-style [layouts]
  (let [layouts (prepared-layouts layouts)
        layout-details (into {} (map (fn [layout]
                                       [(:name layout)
                                        layout])
                                     layouts))]
    layout-details))

(re-frame/reg-sub
 ::card-margin
 (fn [db [_ path]]
   (get-in db (gp/card-margin path))))

(defn current-layout-sub [db path]
  (create-layout-desc (get-in db (gp/selected-layouts path))))

(re-frame/reg-sub
 ::current-layout
 (fn [db [_ path]]
   (current-layout-sub db path)))

