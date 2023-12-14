(ns data-format-lib.date-filter
  (:require
   [data-format-lib.filter :as filter]
   [data-format-lib.dates :as dates]
   [data-format-lib.filter-functions :as ff]))

(defn- transparent?
  "Returns true iff the filter-definition is empty, i.e. obviously not filtering anything"
  [filter-def]
  (or (nil? filter-def)
      (#{[:and] [:or]} filter-def)))

(defn expand
  [instance m]
  (if-let [d-str (ff/get instance m "date")]
    (ff/merge instance m (dates/parse d-str))
    m))

(defn compress
  [instance m]
  (ff/dissoc instance m dates/date-keys))

(def date-keys #{::dates/week ::dates/weekday ::dates/year ::dates/month ::dates/full-date})

(defn filter-contains-date? [f]
  (if (map? f)
    (date-keys (get f ::filter/prop))
    (some filter-contains-date?
          (rest f))))

(defn group-by-data
  "Expanding data and then filter"
  [instance f ms]
  (if (filter-contains-date? f)
    ;; expand and compress only when there are dates
    (as-> ms $
      (ff/map instance (partial expand instance) $)
      (filter/group-by-data instance f $)
      ((fn [data]
         (as-> {} $
           (ff/assoc instance $ true (ff/mapv instance
                                              (partial compress instance)
                                              (ff/get instance data true)))
           (ff/assoc instance $ false (ff/mapv instance
                                               (partial compress instance)
                                               (ff/get instance data false)))))
       $))
    (filter/group-by-data instance f ms)))

(defn filter-data
  "Expanding data and then filter"
  [instance f ms]
  (if (filter-contains-date? f)
    ;; expand and compress only when there are dates
    (as-> ms $
      (ff/map instance (partial expand instance) $)
      (filter/filter-data instance f $)
      (ff/mapv instance (partial compress instance) $))
    (filter/filter-data instance f ms)))

(defn group-by-data-api
  ([filter-def coll instance]
   (if (transparent? filter-def)
     (as-> (ff/->ds instance {false []})
           $
       (ff/assoc instance $ true coll))
     (group-by-data instance filter-def coll)))
  ([f ms]
   (group-by-data f ms ff/default-impl)))

(defn filter-data-api [filter-def coll & [instance]]
  (if (transparent? filter-def)
    coll
    (filter-data (or instance ff/default-impl) filter-def coll)))

(defn check-datapoint
  "Test if one single datapoint conforms to the given filter-definition.
   Returns true/false."
  [filter-def datapoint & [instance]]
  (let [instance (or instance ff/default-impl)]
    (cond
      (transparent? filter-def) true
      (filter-contains-date? filter-def) (->> datapoint
                                              (expand instance)
                                              (filter/check-datapoint instance filter-def))
      :else (filter/check-datapoint instance filter-def datapoint))))