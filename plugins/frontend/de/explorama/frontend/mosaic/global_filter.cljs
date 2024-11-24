(ns de.explorama.frontend.mosaic.global-filter
  "Currently unused. Leaved here as data-format-lib protocol implementation for the internal mosaic data-structure"
  (:require [de.explorama.shared.data-format.filter-functions :as ff]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]))

(def impl
  (reify ff/FilterFunctions
    (ff/->ds [_ c]
      (gdal/->g c))
    (ff/ds-> [_ c]
      (gdal/g-> c))
    (ff/assoc [_ m k v]
      (gdal/assoc m k v))
    (ff/coll? [_ c]
      (gdal/coll? c))
    (ff/concat [_ c1 c2]
      (gdal/concat c1 c2))
    (ff/conj [_ c v]
      (gdal/conj c v))
    (ff/contains? [_ c k]
      (boolean
       (gdal/get c (if (keyword? k)
                     (name k)
                     k))))
    (ff/difference [_ c1 c2]
      (gdal/difference-vec c1 c2))
    (ff/dissoc [_ m ks]
      (gdal/dissoc m ks))
    (ff/every? [_ f c]
      (gdal/every? f c))
    (ff/filter [_ f c]
      (gdal/filter f c))
    (ff/filterv [_ f c]
      (gdal/filter f c))
    (ff/get [_ c k]
      (gdal/get c (if (keyword? k)
                    (name k)
                    k)))
    (ff/group-by [_ f c]
      (gdal/group-by f c))
    (ff/intersection [_ c1 c2]
      (gdal/intersection c1 c2))
    (ff/map [_ f c]
      (gdal/mapv f c))
    (ff/mapv [_ f c]
      (gdal/mapv f c))
    (ff/merge [_ m1 m2]
      (gdal/merge m1 m2))
    (ff/reduce [_ f init c]
      (gdal/reduce f init c))
    (ff/some [_ f c]
      (gdal/some f c))
    (ff/union [_ c1 c2]
      (gdal/union-vec c1 c2))
    (ff/update [_ c k f]
      (gdal/update c k f))))
