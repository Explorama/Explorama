(ns de.explorama.shared.mosaic.common-paths)

(def sort-key :sort)
(def sort-grp-key :sort-grp)
(def sort-sub-grp-key :sort-sub-grp)
(def grp-by-key :grp-key)
(def sub-grp-by-key :sub-grp-key)
(def couple-key :couple-groups)
(def coupled-key :coupled)
(def render-mode-key :type)
(def render-mode-key-raster :raster)
(def render-mode-key-scatter :scatter)
(def render-mode-key-treemap :treemap)
(def treemap-algorithm :treemap-algorithm)
(def scatter-x :x)
(def scatter-y :y)
(def filter-key :filter)
(def layouts :layouts)

(def start-order :asc)

(defn sort-desc [by order]
  {:by by
   :direction order})

(defn sort-grp-desc [by direction attr method]
  {:by by
   :direction direction
   :attr attr
   :method method})

(def sort-equal-vec [:by :type])
(def sort-grp-equal-vec [:by :type :attr :method])

(defn is-same-sort-desc? [old-desc new-desc equal-vec]
  (= (select-keys old-desc equal-vec)
     (select-keys new-desc equal-vec)))

(defn change-direction [{d :direction :as desc}]
  (assoc desc
         :direction
         (cond (= d :desc)
               :asc
               (= d :asc)
               :desc
               :else
               start-order)))

(def start-desc-sort (sort-desc "date" start-order))
(def start-desc-sort-groups (sort-grp-desc :name start-order nil nil))

;TODO r1/scatterplot not sure about this / server needs client specific informations in order to create scatterplot

(def scatter-client-dims :client-dims)
