(ns de.explorama.shared.data-format.operations
  (:require [clojure.math.combinatorics :as combo]
            [clojure.set :as set]
            [clojure.string :as string]
            [de.explorama.shared.data-format.date-filter :as f]
            [de.explorama.shared.data-format.dates :refer [safe-subs transform-week
                                                           transform-weekday]]
            [de.explorama.shared.data-format.filter-functions :as ff]
            [de.explorama.shared.data-format.vpl :as vpl]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :refer [error warn]]
            [taoensso.tufte :refer [p]]))

(declare functions)

(defn prepare-data [data]
  (as-> data $
    (p ::merge-data
       (vec (flatten (vals (into {} $)))))
    (p ::deduplicate
       (->>
        (map (fn [e] [(attrs/value e "id") e]) $)
        (into {})
        vals
        vec))))

(defn extract-fun [instance key data]
  (->> (ff/reduce instance
                  (fn [acc element]
                    (let [value (ff/get instance element key)]
                      (if (ff/coll? instance value)
                        (ff/concat instance acc value)
                        (ff/conj instance acc value))))
                  (ff/->ds instance [])
                  data)
       (ff/ds-> instance)
       set))

(defn filter-fun [instance intersected-keys datakey data]
  (->> (ff/filter instance
                  (fn [element]
                    (let [value (ff/get instance element datakey)]
                      (if (ff/coll? instance value)
                        (not-empty (ff/filter instance intersected-keys value))
                        (intersected-keys value))))
                  data)
       set))

(defn intersect-by [instance datakey data]
  (let [set_data (map (partial extract-fun instance datakey) data)
        intersected-keys (apply set/intersection set_data)
        events (map (partial filter-fun instance intersected-keys datakey) data)
        op-res (apply ff/union instance events)]
    op-res))

(def error-msg "more than 2 parameters are currently not implemented")

(defn parameter-count-wrapper [num sets & params]
  (when (and (< num (count sets))
             (some nil? params))
    (error error-msg sets)
    (throw (ex-info error-msg {}))))

(defonce ^:private time-attributes #{"week" "weekday" "year" "month" "day" "hour" "minute" "seconds"})

(def ^String date-access-key (attrs/access-key "date"))

(defn- time-value [datapoint time-attribute ^Boolean ignore-hierarchy?]
  (let [date (attrs/value datapoint date-access-key)]
    (cond
      (not (seq date)) (do (warn "Datapoint has no date" datapoint) nil)
      ignore-hierarchy?
      (case time-attribute
        "year" (safe-subs date 0 4)
        "month" (safe-subs date 5 7)
        "day" (safe-subs date 8 10)
        "hour" (safe-subs date 11 13)
        "minute" (safe-subs date 14 16)
        "seconds" (safe-subs date 17)
        "week" (transform-week date)
        "weekday" (transform-weekday date))
      :else (case time-attribute
              "year" (safe-subs date 0 4)
              "month" (safe-subs date 0 7)
              "day" (safe-subs date 0 10)
              "hour" (safe-subs date 0 13)
              "minute" (safe-subs date 0 16)
              "seconds" date
              "week" (str (safe-subs date 0 4)
                          "/"
                          (transform-week date))
              "weekday" (str (safe-subs date 0 10)
                             "/"
                             (transform-weekday date))))))

(defn- date->attr [attribute ^Boolean granularity-attr?]
  (if (and attribute granularity-attr?)
    (keyword (attrs/access-key attribute))
    date-access-key))

(defn- cartesian-product [data date-attribute attributes-set ^Boolean reduce-date? ^Boolean ignore-hierarchy? ^Boolean granularity-attr?]
  (flatten (map (fn [event]
                  (let [vector-attrs (reduce (fn [acc [attr value]]
                                               (if (and (vector? value)
                                                        (attributes-set attr))
                                                 (conj acc attr)
                                                 acc))
                                             #{}
                                             event)
                        event (cond
                                (and reduce-date? (seq date-attribute))
                                (assoc event
                                       (date->attr (first date-attribute) granularity-attr?)
                                       (time-value event (first date-attribute) ignore-hierarchy?))
                                (and granularity-attr? (seq date-attribute))
                                (reduce (fn [event date-attr]
                                          (assoc event
                                                 (date->attr date-attr granularity-attr?)
                                                 (time-value event date-attr ignore-hierarchy?)))

                                        event
                                        date-attribute)
                                :else event)]
                    (if (empty? vector-attrs)
                      event
                      (->> vector-attrs
                           (map #(set (map (fn [value]
                                             {% value})
                                           (attrs/value event %))))
                           (apply combo/cartesian-product)
                           (map #(apply merge event %))))))
                data)))

(defn- catesian-product-group-by [attributes data ^Boolean ignore-hierarchy? ^Boolean granularity-attr?]
  (group-by (fn [datapoint]
              (into {}
                    (map (fn [attribute]
                           [(if (time-attributes attribute)
                              (date->attr attribute granularity-attr?)
                              attribute)
                            (if (time-attributes attribute)
                              (time-value datapoint attribute ignore-hierarchy?)
                              (attrs/value datapoint attribute))]))
                    attributes))
            data))

(defn- keep-group-by [attributes data ^Boolean ignore-hierarchy?]
  (reduce (fn [result datapoint]
            (let [keys-to-add
                  (reduce (fn [keys-to-add attribute]
                            ;TODO remarks do we need reduce-date? with :keep - or at all?
                            (let [attribute-value (if (time-attributes attribute)
                                                    (time-value datapoint attribute ignore-hierarchy?)
                                                    (attrs/value datapoint attribute))]
                              (if (vector? attribute-value)
                                (into #{}
                                      (for [attribute-value-element attribute-value
                                            prev-key keys-to-add]
                                        (assoc prev-key attribute attribute-value-element)))
                                (into #{} (map (fn [prev-key]
                                                 (assoc prev-key attribute attribute-value))
                                               keys-to-add)))))
                          #{{}}
                          attributes)]
              (merge-with into result (into {}
                                            (map (fn [key]
                                                   [key [datapoint]]))
                                            keys-to-add))))
          {}
          data))

(defn group-data [{:keys [attributes forced-groups mode ^Boolean reduce-date? ^Boolean ignore-hierarchy? ^Boolean granularity-attr?]}
                  data]
  (assert (seq attributes) "Group-by attributes can not be nil or empty.")
  (let [attributes-set (set attributes)
        date-attribute (set/intersection #{"week" "weekday" "year" "month" "day" "hour" "minute" "seconds"} (set attributes))
        data (cond (= mode :cartesian)
                   (cartesian-product data date-attribute attributes-set reduce-date? ignore-hierarchy? granularity-attr?)
                   (= mode :keep)
                   data
                   :else
                   (cartesian-product data date-attribute attributes-set reduce-date? ignore-hierarchy? granularity-attr?))
        grouped-data (cond (= mode :cartesian)
                           (catesian-product-group-by attributes data ignore-hierarchy? granularity-attr?)
                           (= mode :keep)
                           (keep-group-by attributes data ignore-hierarchy?)
                           :else
                           (catesian-product-group-by attributes data ignore-hierarchy? granularity-attr?))]
    (if (and forced-groups (not-empty forced-groups))
      (reduce
       (fn [result group]
         (assoc result group (get grouped-data group [])))
       {}
       forced-groups)
      grouped-data)))

(defn meta-primitive-value
  "Meta data attributes for primitive values e.g. 5"
  [data]
  (vary-meta
   data
   assoc
   :structure :value
   :element :value))

(defn meta-list-values
  "Meta data attributes for a list of primitive values e.g. [1 2 3 4 5]"
  [data]
  (vary-meta
   data
   assoc
   :structure :list
   :element :value))

(defn meta-list-events
  "Meta data attributes for a list of maps e.g. [{:a 1} {:b 2}]"
  [data]
  (vary-meta
   data
   assoc
   :structure :list
   :element :event))

(defn meta-group-list-events
  "Meta data attributes for a map of a lists of maps e.g. {{:a 1 :b 2} [{:a 1 :c 3} {:b 2 :c 4}]}"
  [data]
  (vary-meta
   data
   assoc
   :structure :group
   :group :list
   :element :event))

;TODO remarks this allows only 2 times nested maps
(defn meta-sub-group-list-events
  "Meta data attributes for a map of maps of lists of maps e.g. {{:a 1 :b 2} {{:c 4} [{:b 2 :c 4}]}}"
  [data]
  (vary-meta
   data
   assoc
   :structure :sub-group
   :group :list
   :element :event))

(defn meta-group-values
  "Meta data attributes for a map of primitive values e.g. {{:a 1 :b 2} 3}"
  [data]
  (vary-meta
   data
   assoc
   :structure :group
   :group :value
   :element :value))

(defn meta-group-list-values
  "Meta data attributes for a map of lists of primitive values e.g. {{:a 1 :b 2} [1 2 3]}"
  [data]
  (vary-meta
   data
   assoc
   :structure :group
   :group :list
   :element :value))

(defn- value-fn [attribute]
  (cond (time-attributes attribute)
        (fn [e]
          (if (map? e)
            (time-value e attribute true)
            e))
        attribute
        (fn [e]
          (if (map? e)
            (attrs/value e attribute)
            e))
        :else
        identity))

(defn- operation-on-group-values
  "Perform one operation on each group.
   The operation-fn gets all values inside
   the group for a specified attribute or
   the element."
  [groups attribute operation-fn]
  (reduce (fn [acc [group-key values]]
            (assoc acc
                   group-key
                   (->> values
                        (map (value-fn attribute))
                        (filter identity)
                        (operation-fn))))
          {}
          groups))

(defn- operation-on-group-values-join
  "Perform one operation on each group.
   The operation-fn gets all values inside
   the group for a specified attribute or
   the element. The results will be
   joined at the end."
  [groups attribute operation-fn]
  (operation-fn
   (map (fn [[_ values]]
          (->> values
               (map (value-fn attribute))
               (filter identity)
               (operation-fn)))
        groups)))

(defn- operation-on-sub-group-values-join-fully
  "Perform one operation on each group.
   The operation-fn gets all values inside
   the group for a specified attribute or
   the element. The results will be
   joined at the end."
  [groups attribute operation-fn join-fn]
  (reduce (fn [acc [_ values]]
            (reduce (fn [acc [_ values]]
                      (if acc
                        (join-fn
                         acc
                         (->> values
                              (map (value-fn attribute))
                              (filter identity)
                              (operation-fn)))
                        (->> values
                             (map (value-fn attribute))
                             (filter identity)
                             (operation-fn))))

                    acc
                    values))
          nil
          groups))

(defn- operation-on-sub-group-values-join-partially
  "Perform one operation on each group.
   The operation-fn gets all values inside
   the group for a specified attribute or
   the element. The results will be
   joined partially at the end - keeps the outer group-by."
  [groups attribute operation-fn join-fn]
  (reduce (fn [acc [key values]]
            (assoc acc key
                   (reduce (fn [acc [_ values]]
                             (if acc
                               (join-fn
                                acc
                                (->> values
                                     (map (value-fn attribute))
                                     (filter identity)
                                     (operation-fn)))
                               (->> values
                                    (map (value-fn attribute))
                                    (filter identity)
                                    (operation-fn))))
                           nil
                           values)))
          {}
          groups))

(defn- operation-on-group-value
  "Perform one operation on each group.
   The operation-fn gets all values inside
   the groups value."
  [groups operation-fn]
  (reduce (fn [acc [group-key value]]
            (assoc acc group-key (operation-fn value)))
          {}
          groups))

(defn- operation-on-group-value-join
  "Perform one operation on each group.
   The operation-fn gets all values inside
   the groups value. The results will be
   joined at the end."
  [groups operation-fn]
  (operation-fn
   (map (fn [[_ value]]
          (operation-fn value))
        groups)))

(defn- operation-on-list-values
  "Perform one operation on a list.
   The operation-fn gets all values inside
   the list for a specified attribute or
   the element."
  [list attribute operation-fn]
  (->> list
       (map (value-fn attribute))
       (filter identity)
       (operation-fn)))

(defn- operation-on-groups-with-merge
  "Perform one operation on each group over all dis.
   Example di-groups:
   {\"di-1\" {\"a\" {}}
    \"di-2\" {\"a\" {}}}
   
   The operation-fn gets a list of all values over all dis."
  [di-groups attribute operation-fn]
  (let [group-keys (keys (first di-groups))]
    (meta-group-values
     (into {}
           (map (fn [group-key]
                  [group-key
                   (->> di-groups
                        (map #(get % group-key))
                        (filter identity)
                        flatten
                        (map (value-fn attribute))
                        (operation-fn group-key))]))
           group-keys))))

(defn- operation-on-lists-with-merge
  "Perform one operation on each group over all dis.
   Example di-groups:
   {\"di-1\" {\"a\" {}}
    \"di-2\" {\"a\" {}}}
   
   The operation-fn gets a list of all values over all dis."
  [di-lists attribute operation-fn]
  (meta-list-values
   (operation-fn
    (map (fn [di-list]
           (if (sequential? di-list)
             (->> di-list
                  (filter identity)
                  (map (value-fn attribute)))
             di-list))
         di-lists))))

(defn group-by-meta [di-mixed]

  {:primitive-value
   (filter (fn [data]
             (let [{:keys [structure]} (meta data)]
               (or (= structure :value)
                   (number? data)
                   (string? data))))
           di-mixed)
   :list-values
   (filter (fn [data]
             (let [{:keys [element structure]} (meta data)]
               (and (= structure :list)
                    (= element :value))))
           di-mixed)
   :list-events
   (filter (fn [data]
             (let [{:keys [element structure]} (meta data)]
               (and (= structure :list)
                    (= element :event))))
           di-mixed)
   :group-list-events
   (filter (fn [data]
             (let [{:keys [group structure element]} (meta data)]
               (and (= structure :group)
                    (= group :list)
                    (= element :event))))
           di-mixed)
   :sub-group-list-events
   (filter (fn [data]
             (let [{:keys [group structure element]} (meta data)]
               (and (= structure :sub-group)
                    (= group :list)
                    (= element :event))))
           di-mixed)
   :group-values
   (filter (fn [data]
             (let [{:keys [group structure element]} (meta data)]
               (and (= structure :group)
                    (= group :value)
                    (= element :value))))
           di-mixed)
   :group-list-values
   (filter (fn [data]
             (let [{:keys [group structure element]} (meta data)]
               (and (= structure :group)
                    (= group :list)
                    (= element :value))))
           di-mixed)})

(defn- operation-on-mixed-with-merge [di-mixed attribute operation-fn operation-fn-default-value]
  (let [{:keys [primitive-value list-values list-events group-list-events group-values group-list-values]}
        (group-by-meta di-mixed)
        group-factor (reduce (fn [acc key]
                               (let [group-value (map #(get % key) group-values)
                                     group-list-value (map #(get % key) group-list-values)]
                                 (assoc acc key
                                        (operation-fn
                                         (cond-> []
                                           group-value
                                           (into group-value)
                                           group-list-value
                                           (into group-list-value))))))
                             {}
                             (set/union (into #{} (mapcat keys) group-values)
                                        (into #{} (mapcat keys) group-values)))
        factor (operation-fn
                (cond-> []
                  (seq primitive-value) (into (flatten primitive-value))
                  (seq list-events) (conj (operation-on-lists-with-merge list-events attribute operation-fn))
                  (seq list-values) (into list-values)))]
    (cond (seq group-list-events)
          (operation-on-groups-with-merge group-list-events attribute #(operation-fn (conj %2 factor (get group-factor %1 operation-fn-default-value))))

          (seq group-factor)
          (meta-group-values
           (into {}
                 (map (fn [[key value]]
                        [key (operation-fn [value factor])]))
                 group-factor))
          factor
          (meta-primitive-value [factor]))))

(defn check-meta [groups meta-func]
  (every? (fn [groups]
            (meta-func (meta groups)))
          groups))

(defn- operation-on-*-with-merge [groups attribute operation-fn operation-fn-default-value]
  (cond (check-meta groups
                    (fn [{:keys [structure]}]
                      (= structure :group)))
        (operation-on-groups-with-merge groups
                                        attribute
                                        #(operation-fn %2))

        (check-meta groups
                    (fn [{:keys [structure element]}]
                      (and (= structure :list)
                           (= element :event))))
        (operation-on-lists-with-merge groups
                                       attribute
                                       operation-fn)

        :else
        (operation-on-mixed-with-merge groups
                                       attribute
                                       operation-fn
                                       operation-fn-default-value)))

(defn- operation-on-*-values [[groups] attribute operation-fn op-desc]
  (let [{:keys [structure group element]} (meta groups)]
    (cond (and (= :group structure)
               (= :list group)
               (= :event element))
          (meta-group-values
           (operation-on-group-values groups
                                      attribute
                                      operation-fn))
          (and (= :group structure)
               (= :value group)
               (= :value element))
          (meta-group-values
           (operation-on-group-value groups
                                     operation-fn))
          (and (= :list structure)
               (= :event element))
          (meta-primitive-value
           (operation-on-list-values groups
                                     attribute
                                     operation-fn))
          :else
          (throw (ex-info "Not supported data structure"
                          {:op op-desc
                           :structure structure
                           :group group
                           :element element})))))

(defn- operation-on-*-values-join [[groups] join-fully? attribute operation-fn join-fn op-desc]
  (let [{:keys [structure group element]} (meta groups)]
    (cond (and (= :sub-group structure)
               (= :list group)
               (= :event element))
          (if join-fully?
            (meta-primitive-value
             (operation-on-sub-group-values-join-fully groups
                                                       attribute
                                                       operation-fn
                                                       join-fn))
            (meta-group-values
             (operation-on-sub-group-values-join-partially groups
                                                           attribute
                                                           operation-fn
                                                           join-fn)))
          (and (= :group structure)
               (= :list group)
               (= :event element))
          (meta-primitive-value
           (operation-on-group-values-join groups
                                           attribute
                                           operation-fn))
          (and (= :group structure)
               (= :value group)
               (= :value element))
          (meta-primitive-value
           (operation-on-group-value-join groups
                                          operation-fn))
          (and (= :list structure)
               (= :event element))
          (meta-primitive-value
           (operation-on-list-values groups
                                     attribute
                                     operation-fn))
          :else
          (throw (ex-info "Not supported data structure"
                          {:op op-desc
                           :structure structure
                           :group group
                           :element element})))))

(def ^:private m-seq-intersection
  (memoize (fn [^clojure.lang.IPersistentSet case-set
                ^clojure.lang.IPersistentSet evtvs]
             (seq (set/intersection case-set evtvs)))))

(def ^:private layout-key "layout")
(def ^:private layout-map (fn [color id]
                            {"color" color
                             "id" id}))

(defn- colorscheme-as-function
  [reverse-color? {:keys [attribute-type value-assigned id attributes] {colors :colors} :color-scheme}]
  (let [colors (mapv (fn [r]
                       (get colors (keyword (str r))))
                     (range (count colors)))
        colors (if reverse-color?
                 (reverse colors)
                 colors)]
    (if (= "string" attribute-type)
      (let [{:keys [cases else?]}
            (p ::cases-string
               (reduce (fn [acc [idx values]]
                         (cond (empty? values)
                               acc
                               (seq (filter #(= % "*") values))
                               (assoc acc :else? idx)
                               :else
                               (update acc :cases conj [idx (set values)])))
                       {:cases []
                        :else? nil}
                       (map-indexed vector value-assigned)))]
        (fn [^clojure.lang.IPersistentMap evt]
          (p ::string
             (loop [current-attributes attributes]
               (cond (and (empty? current-attributes)
                          (not else?))
                     nil
                     (and (empty? current-attributes)
                          else?)
                     (if (some #(get evt %) attributes)
                       (assoc evt layout-key (layout-map (get colors else?) id))
                       nil)
                     :else
                     (let [attribute (first current-attributes)
                           av (get evt attribute)]
                       (cond (nil? av)
                             (recur (rest current-attributes))
                             (vector? av)
                             (if-some [idx (some (fn [[idx case-set]]
                                                   (when (m-seq-intersection case-set (set av))
                                                     idx))
                                                 cases)]
                               (assoc evt layout-key (layout-map (get colors idx) id))
                               (recur (rest current-attributes)))
                             :else
                             (if-some [idx (some (fn [[idx case-set]]
                                                   (when (case-set av)
                                                     idx))
                                                 cases)]
                               (assoc evt layout-key (layout-map (get colors idx) id))
                               (recur (rest current-attributes))))))))))
      (let [cases (p ::cases-number
                     (->> (map-indexed (fn [idx [minv maxv]]
                                         (if (and (nil? minv)
                                                  (nil? maxv))
                                           nil
                                           [idx (fn [v]
                                                  (and (<= minv v)
                                                       (< v maxv)))]))
                                       value-assigned)
                          (filter identity)
                          vec))]
        (fn [^clojure.lang.IPersistentMap evt]
          (p ::number
             (loop [attributes attributes]
               (if (empty? attributes) nil
                   (let [attribute (first attributes)
                         av (get evt attribute)]
                     (cond (nil? av)
                           (recur (rest attributes))
                           (vector? av)
                           (if-some [idx (some (fn [[idx ^clojure.lang.Fn case-fn]]
                                                 (when (some case-fn av)
                                                   idx))
                                               cases)]
                             (assoc evt layout-key (layout-map (get colors idx) id))
                             (recur (rest attributes)))
                           :else
                           (if-some [idx (some (fn [[idx ^clojure.lang.Fn case-fn]]
                                                 (when (case-fn av)
                                                   idx))
                                               cases)]
                             (assoc evt layout-key (layout-map (get colors idx) id))
                             (recur (rest attributes)))))))))))))

(def ^:private empty-layout {"color" nil
                             "id" nil})

(defn colorize-events [layouts reverse-color? events]
  (let [color-fns (p ::layout-fns (mapv (partial colorscheme-as-function reverse-color?) layouts))]
    (if (empty? layouts)
      events
      (p ::iteration
         (mapv (fn [event]
                 (if-some [evt (some (fn [color-fn]
                                       (color-fn event))
                                     color-fns)]
                   evt
                   (assoc event layout-key empty-layout)))
               events)))))

(defn- apply-layout-operation [layouts reverse-color? data]
  (let [{:keys [structure group element] :as meta-data} (meta data)]
    (-> (cond
          (and (= structure :list)
               (= element :event)) (colorize-events layouts reverse-color? data)
          (and (= structure :group)
               (= group :list)
               (= element :event)) (reduce (fn [accum [grp evts]]
                                             (assoc accum grp (colorize-events layouts reverse-color? evts)))
                                           {}
                                           data)
          :else
          (throw (ex-info "Only lists of events or grouped events are supported"
                          {:structure structure
                           :group group
                           :element element})))
        (with-meta meta-data))))

(defn- sort-event-list [groups attribute-types attribute direction take-value]
  (let [sortfn (case (get attribute-types attribute)
                 :string (fn [value]
                           (if (vector? value)
                             (string/join "" (sort value))
                             value))
                 :number (fn [value]
                           (if (vector? value)
                             (apply + value)
                             value))
                 :date identity)]
    (cond->> groups
      :always
      (sort-by #(sortfn (attrs/value % attribute))
               (if (= direction :asc)
                 #(compare %1 %2)
                 #(compare %2 %1)))
      take-value
      (take take-value)
      :always
      vec)))

(defn- sort-group-keys [instance groups {:keys [attribute direction aggregate custom-compare]
                                         {take-value :value} :take}]
  (if aggregate
    (let [groups-aggregated ((functions (first aggregate))
                             instance
                             {}
                             (second aggregate)
                             [groups])
          groups-keys-sorted (cond->> groups-aggregated
                               :always
                               (sort-by (fn [[_ group-value]]
                                          group-value)
                                        (if (= direction :asc)
                                          #(compare %1 %2)
                                          #(compare %2 %1)))
                               take-value
                               (take take-value))]
      groups-keys-sorted)
    (->> (sort-by (fn [group-key]
                    (get group-key attribute))
                  (let [compare (if custom-compare
                                  custom-compare
                                  compare)]
                    (if (= direction :asc)
                      #(compare %1 %2)
                      #(compare %2 %1)))
                  (keys groups))
         (map (fn [group-key]
                [group-key nil])))))

(defn- return-map [return-map?]
  (if return-map?
    (fn [data]
      (into {} data))
    vec))

(defn- sort-groups [instance groups {:keys [return-map?]
                                     {take-value :value} :take
                                     :or {return-map? true}
                                     :as params}]
  (let [groups (if (map? groups)
                 groups
                 (with-meta
                   (into {} groups)
                   (meta groups)))
        group-keys (sort-group-keys instance groups params)
        return-map-fn (return-map return-map?)]
    (cond->> (map (fn [[key aggregated-value]]
                    (if aggregated-value
                      [(assoc key "aggregated-value" aggregated-value)
                       (get groups key)]
                      [key (get groups key)]))
                  group-keys)
      take-value
      (take take-value)
      :always
      return-map-fn)))

(defn- sort-by-operation [instance
                          [groups]
                          {:keys [attribute direction attribute-types apply-to level return-map?]
                           {take-value :value} :take
                           :as params}]
  (let [{:keys [structure group element]} (meta groups)
        apply-to (cond apply-to
                       apply-to
                       (and (nil? apply-to)
                            (= structure :group))
                       :group
                       :else
                       :events)
        return-map-func (return-map return-map?)]
    (cond (and (= :list structure)
               (= :event element))
          (meta-list-events
           (sort-event-list groups attribute-types attribute direction take-value))
          (and (= :sub-group structure)
               (= :group apply-to)
               (= level 1))
          (-> (map (fn [[key val]]
                     [key (sort-groups instance
                                       (meta-group-list-events val)
                                       params)])
                   groups)
              return-map-func
              meta-sub-group-list-events)
          (and (= :sub-group structure)
               (= :events apply-to))
          (-> (map (fn [[sub-key sub-value]]
                     [sub-key
                      (return-map-func
                       (map (fn [[key value]]
                              [key (sort-event-list (meta-list-events value)
                                                    attribute-types
                                                    attribute
                                                    direction
                                                    take-value)])
                            sub-value))])
                   groups)
              return-map-func
              meta-sub-group-list-events)
          (and (= :group structure)
               (= :list group)
               (= :event element)
               (= :events apply-to))
          (-> (map (fn [[key value]]
                     [key (sort-event-list (meta-list-events value)
                                           attribute-types
                                           attribute
                                           direction
                                           take-value)])
                   groups)
              return-map-func
              meta-group-list-events)
          (and (or (= :group structure)
                   (and (= :sub-group structure)
                        (= 0 level)))
               (= :group apply-to))
          ((cond (and (= :group structure)
                      (= :list group)
                      (= :event element))
                 meta-group-list-events
                 (and (= :group structure)
                      (= :list group)
                      (= :value element))
                 meta-group-list-values
                 (and (= :group structure)
                      (= :value group)
                      (= :value element))
                 meta-group-values
                 (and (= :sub-group structure)
                      (= :list group)
                      (= :event element))
                 meta-sub-group-list-events)
           (sort-groups instance groups params))
          :else
          (throw (ex-info "Data structure is not allowed for this operation."
                          {:op :sort-by
                           :structure structure
                           :group group
                           :element element})))))

(defn normalize- [range-min range-max min-val max-val original-val]
  (if (= min-val max-val)
    range-max
    (+ (* (- range-max
             range-min)
          (/ (- original-val min-val)
             (- max-val min-val)))
       range-min)))

(defn normalize [{:keys [attribute
                         range-min
                         range-max
                         all-data?
                         result-name]
                  :or {all-data? true
                       result-name attribute}} di-groups]
  (assert (= 1 (count di-groups)) "normalize only accepts on data-instance")
  (let [{:keys [primitive-value list-values list-events group-list-events group-values group-list-values]}
        (group-by-meta di-groups)]
    (cond (and (empty? list-events)
               (empty? group-list-events))
          (let [all-values (cond-> []
                             (seq group-values)
                             (into (flatten (map vals group-values)))
                             (seq group-list-values)
                             (into (flatten (vals group-list-values)))
                             (seq primitive-value)
                             (into primitive-value)
                             (seq list-values)
                             (into list-values))
                min-val (apply min all-values)
                max-val (apply max all-values)
                map-fn (fn [value]
                         (normalize- range-min range-max
                                     min-val max-val
                                     value))]
            (operation-on-*-values di-groups
                                   attribute
                                   map-fn
                                   :normalize))
          (seq group-list-events)
          (let [_ (assert attribute ":attribute not defined")
                group-list-events (first group-list-events)
                [min-val max-val] (if all-data?
                                    (let [all-values
                                          (->> (vals group-list-events)
                                               flatten
                                               (map #(get % attribute)))]
                                      [(apply min all-values)
                                       (apply max all-values)])
                                    (reduce (fn [acc [group-key events]]
                                              (let [group-events (map #(get % attribute) events)]
                                                (-> (assoc-in acc [0 group-key] (apply min group-events))
                                                    (assoc-in [1 group-key] (apply max group-events)))))
                                            []
                                            group-list-events))]
            (meta-group-list-events
             (into {}
                   (map (fn [[key events]]
                          [key
                           (map (fn [event]
                                  (assoc event
                                         result-name
                                         (if all-data?
                                           (normalize- range-min range-max
                                                       min-val max-val
                                                       (get event attribute))
                                           (normalize- range-min
                                                       range-max
                                                       (get min-val key)
                                                       (get max-val key)
                                                       (get event attribute)))))
                                events)]))
                   group-list-events)))
          (seq list-events)
          (let [_ (assert attribute ":attribute not defined")
                all-values (map #(get % attribute) (first list-events))
                min-val (apply min all-values)
                max-val (apply max all-values)]
            (map (fn [event]
                   (assoc event result-name
                          (normalize- range-min range-max
                                      min-val max-val
                                      (get event attribute))))
                 (first list-events))))))

(defn heal-event-addon-attributes [addons]
  (->> addons
       (mapv (fn [{:keys [attribute value]}]
               [attribute value]))
       flatten))

(defn- heal-event [{:keys [policy descs addons workaround force-type generate-ids]
                    :or {policy :merge}} data]
  (let [{:keys [primitive-value list-values list-events group-list-events group-values group-list-values]}
        (group-by-meta data)
        generate-ids-fn
        (when generate-ids
          (case (:policy generate-ids)
            :uuid
            (fn [_]
              #?(:clj (str (java.util.UUID/randomUUID))
                 :cljs (str (random-uuid))))
            :select-vals
            (fn [event]
              (->>
               (map #(get event %) (:keys generate-ids))
               (string/join "-")))))]
    (cond->> (case policy
               :merge
               (let [_ (assert (and (or (seq group-values)
                                        (seq group-list-values))
                                    (empty? primitive-value)
                                    (empty? list-values)
                                    (empty? list-events)
                                    (empty? group-list-events))
                               "Merging is only allowed for multiple datasets of group-values ({{:a 1} 1 {:a 2} 1})")
                     _ (assert descs "descs ist not defined")
                     groups (if (seq group-list-values)
                              (into #{} (mapcat keys) group-list-values)
                              (into #{} (mapcat keys) group-values))
                     data (apply assoc {} (interleave descs (if (seq group-values) group-values group-list-values)))
                     additional-attributes (heal-event-addon-attributes addons)]
                 (reduce (fn [acc key]
                           (conj acc
                                 (reduce (fn [acc [{:keys [attribute]} data]]
                                           (if-let [val (get data key)]
                                             (assoc acc attribute val)
                                             acc))
                                         (if (seq additional-attributes)
                                           (apply assoc key additional-attributes)
                                           key)
                                         data)))
                         []
                         groups))
               :vals
               (let [additional-attributes (heal-event-addon-attributes addons)]
                 (assert (and (seq group-list-events)
                              (empty? primitive-value)
                              (empty? list-values)
                              (empty? list-events)
                              (empty? group-values)
                              (empty? group-list-values))
                         "Vals is only allowed for multiple datasets of group-list-events ({{:a 1} [{:a 1 :b 2} ...] {:a 2} [{:a 2 :c 1} ...]})")
                 (cond->> (mapcat vals group-list-events)
                   :always
                   flatten
                   :always
                   distinct
                   (seq additional-attributes)
                   (map (fn [event]
                          (apply assoc event additional-attributes))))))
      workaround
      (mapv (fn [event]
              (reduce (fn [event [key workaround-desc]]
                        (case key
                          "date" (update event "date" (fn [date]
                                                        (cond (string/blank? date)
                                                              (str (:year workaround-desc) "-" (:month workaround-desc) "-" (:day workaround-desc))
                                                              (= 4 (count date))
                                                              (str date "-" (:month workaround-desc) "-" (:day workaround-desc))
                                                              (= 7 (count date))
                                                              (str date "-" (:day workaround-desc))
                                                              :else
                                                              date)))))
                      event
                      workaround)))
      generate-ids-fn
      (map (fn [event]
             (assoc event "id" (generate-ids-fn event))))
      force-type
      (map (fn [event]
             (reduce (fn [event {:keys [attribute new-type]}]
                       (let [change-fn (case new-type
                                         (case new-type
                                           :string str
                                           :double (fn [val]
                                                     (if (number? val)
                                                       (double val)
                                                       #?(:clj (Double/parseDouble (str val))
                                                          :cljs (js/parseFloat (str val)))))
                                           :integer (fn [val]
                                                      (if (number? val)
                                                        (int val)
                                                        #?(:clj (Integer/parseInt (str val))
                                                           :cljs (js/parseInteger (str val)))))
                                           :long (fn [val]
                                                   (if (number? val)
                                                     (long val)
                                                     #?(:clj (Long/parseLong (str val))
                                                        :cljs (js/parseInteger (str val)))))
                                           identity))]
                         (update event
                                 attribute
                                 (fn [val]
                                   (cond
                                     (vector? val) (mapv change-fn val)
                                     val (change-fn val)
                                     :else (do
                                             (warn "val empty for force type" {:event event
                                                                               :attribute attribute})
                                             val))))))
                     event
                     force-type)))
      :always
      vec
      :always
      meta-list-events)))

(defn median [attribute-values]
  (when (seq attribute-values)
    (let [attribute-values (sort attribute-values)
          c (count attribute-values)
          mid (int (/ c 2))]
      (cond
        (= c 0) nil
        (odd? c) (-> (nth attribute-values mid)
                     (float))
        :else
        (-> (+  (nth attribute-values (dec mid))
                (nth attribute-values mid))
            (/ 2)
            (float))))))

(defn average [attribute-values]
  (when (seq attribute-values)
    (float
     (/ (reduce + attribute-values)
        (count attribute-values)))))

(defn select-attribute-values [attribute events]
  (reduce (fn [acc ev]
            (let [val (get ev attribute)]
              (cond
                (vector? val) (into acc val)
                val (conj acc val)
                :else acc)))
          []
          events))

(defn sort-by-frequencies [direction values]
  (let [order-fn (if (= direction :asc)
                   <
                   >)]
    (->> values
         frequencies
         (sort-by second order-fn)
         (mapv first))))

(defn empty-check-apply [op coll]
  (when-not (empty? coll)
    (apply op coll)))

(def functions
  (vpl/function-metadata
   {[] {:steering {:disable-inputs [:meta-sub-group-list-events]}}
    [[:category] :aggregation] {:steering {:arguments 1
                                           :attributes {:attribute {:type :select
                                                                    :values :ac-numbers}
                                                        :join? {:type :boolean
                                                                :default false}
                                                        :join-fully? {:hidden true
                                                                      :type :boolean
                                                                      :default false}}
                                           :input->output {:dependent [[[:join? true
                                                                         :join-fully? false]]
                                                                       {:meta-sub-group-list-events :meta-group-values
                                                                        :meta-group-list-events :meta-primitive-value
                                                                        :meta-group-values :meta-primitive-value
                                                                        :meta-list-events :meta-primitive-value}
                                                                       [[:join? true]
                                                                        [:join-fully? true]]
                                                                       {:meta-sub-group-list-events :meta-primitive-value
                                                                        :meta-group-list-events :meta-primitive-value
                                                                        :meta-group-values :meta-primitive-value
                                                                        :meta-list-events :meta-primitive-value}]
                                                           :default {:meta-group-list-events :meta-group-values
                                                                     :meta-group-values :meta-group-values
                                                                     :meta-list-events :meta-primitive-value}}}}
    [[:category] :set]
    {:steering {:arguments 0
                :attributes {}
                :input->output {:default {:meta-group-list-events :meta-sub-group-list-events
                                          :meta-list-events :meta-group-list-events}}}}
    [[:category] :nummerical]
    {:steering {:arguments 0
                :attributes {:attribute {:type :select
                                         :values :ac-numbers}
                             :input {:type :number
                                     :optional true}}
                :input->output {:default {:meta-sub-group-list-events :meta-sub-group-list-events
                                          :meta-group-list-events :meta-group-values
                                          :meta-group-values :meta-group-values
                                          :meta-list-events :meta-list-values}}}}}
   :heal-event {:category :special
                :internal true
                :description "Creates a list of events from groups."
                :steering {:arguments 0
                           :attributes {:policy {:type :select
                                                 :values [:merge :vals]
                                                 :default :merge}
                                        ;TODO r1/vpl create a component for this
                                        :generate-ids {:type :custom
                                                       :hidden true}
                                        :descs {:type :custom}
                                        :force-type {:type :custom}
                                        :addons {:type :custom}
                                        :workaround {:type :custom}}
                           :input->output {:default {:meta-group-list-events :meta-list-events
                                                     :meta-group-list-values :meta-list-events
                                                     :meta-group-values :meta-list-events}}}}
   (fn [_instance _ descs data]
     (heal-event descs data))
   :group-by {:category :by
              :description "Groups events by a provided attributes."
              :steering {:arguments 1
                         :attributes {:attributes {:type :multi-select
                                                   :values :ac-contexts}
                                      :forced-groups {:hidden true
                                                      :type :custom}
                                      :mode {:type :select
                                             :values [:keep :cartesian]
                                             :default :keep}
                                      :ignore-hierarchy? {:type :boolean
                                                          :default false}
                                      :reduce-date? {:type :boolean
                                                     :default false}}
                         :input->output {:default {:meta-group-list-events :meta-sub-group-list-events
                                                   :meta-list-events :meta-group-list-events}}}}
   (fn [_instance _ desc [di-data]]
     (let [{:keys [structure group element]} (meta di-data)]
       (cond (and (= :group structure)
                  (= :list group)
                  (= :event element))
             (meta-sub-group-list-events
              (into {}
                    (map (fn [[key value]]
                           [key (group-data desc value)]))
                    di-data))
             (and (= :list structure)
                  (= :event element))
             (meta-group-list-events
              (group-data desc di-data))
             :else
             (throw (ex-info "Group-by works only on meta-sub-group-list-events and meta-group-list-events"
                             {:op :group-by
                              :structure structure
                              :group group
                              :element element})))))
   :sort-by {:category :by
             :description "Sorts a data set."
             :steering {:arguments 1
                        :attributes {:attribute {:type :multi-select
                                                 :values :ac-contexts}
                                     :direction {:type :select
                                                 :values [:asc :desc]
                                                 :default :asc}
                                     :attribute-types {:hidden true
                                                       :type :custom}
                                     :apply-to {:type :select
                                                :values [:events :group]
                                                :default :events}
                                     :level {:type :select
                                             :values [:sub-group :group]
                                             :default :group}
                                     :return-map? {:type :boolean
                                                   :default true}}
                        :input->output {:default {:meta-sub-group-list-events :meta-sub-group-list-events
                                                  :meta-group-list-events :meta-group-list-events
                                                  :meta-list-events :meta-list-events}}}}
   (fn [instance _ desc di-data]
     (sort-by-operation instance di-data desc))
   :count-events {:category :aggregation
                  :description "Counts events"}
   (fn [_instance _ {:keys [join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? nil #(count %) #(+ %1 %2) :count-event)
       (operation-on-*-values di-groups nil #(count %) :count-event)))
   :distinct {:interal true}
   (fn [_instance _ {:keys [attribute join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? attribute #(-> % flatten distinct) #(distinct (into %1 %2)) :distinct)
       (operation-on-*-values di-groups attribute #(-> % flatten distinct) :distinct)))
   :sum {:category :aggregation
         :description "Sum"}
   (fn [_instance _ {:keys [attribute join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? attribute #(empty-check-apply + %) #(+ %1 %2) :sum)
       (operation-on-*-values di-groups attribute #(empty-check-apply + %) :sum)))
   :min {:category :aggregation
         :description "Min"}
   (fn [_instance _ {:keys [attribute join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? attribute #(apply min %) #(min %1 %2) :min)
       (operation-on-*-values di-groups attribute #(apply min %) :min)))
   :max {:category :aggregation
         :description "Max"}
   (fn [_instance _ {:keys [attribute join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? attribute #(apply max %) #(max %1 %2) :max)
       (operation-on-*-values di-groups attribute #(apply max %) :max)))
   :normalize {:interal true}
   (fn [_instance _ descs di-groups]
     (normalize descs di-groups))
   :median {:category :aggregation
            :description "allows you to create events from grouped events sets. Just"}
   (fn [_instance _ {:keys [attribute join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? attribute #(median %) #(median [%1 %2]) :median)
       (operation-on-*-values di-groups attribute #(median %) :median)))
   :average {:category :aggregation
             :description "allows you to create events from grouped events sets. Just"}
   (fn [_instance _ {:keys [attribute join? join-fully?]} di-groups]
     (if join?
       (operation-on-*-values-join di-groups join-fully? attribute #(average %) #(average [%1 %2]) :average)
       (operation-on-*-values di-groups attribute #(average %) :average)))
   :+ {:category :nummerical
       :description "Adds values"}
   (fn [_instance _ {:keys [attribute]} di-groups]
     (operation-on-*-with-merge di-groups attribute #(apply + %) 0))
   :- {:category :nummerical
       :description "Adds values"}
   (fn [_instance _ {:keys [attribute]} di-groups]
     (operation-on-*-with-merge di-groups attribute #(apply - %) 0))
   :* {:category :nummerical
       :description "Adds values"}
   (fn [_instance _ {:keys [attribute]} di-groups]
     (operation-on-*-with-merge di-groups attribute #(apply * %) 1))
   :/ {:category :nummerical
       :description "Adds values"}
   (fn [_instance _ {:keys [attribute]} di-groups]
     (operation-on-*-with-merge di-groups attribute #(apply / %) 1))
   :select {:internal true}
   (fn [_instance _ {:keys [attribute]} [di-data]]
     (let [{:keys [structure group element]} (meta di-data)]
       (cond (and (= :group structure)
                  (= :list group)
                  (= :event element))
             (meta-group-list-values
              (into {}
                    (map (fn [[k events]]
                           [k (select-attribute-values attribute events)]))
                    di-data))
             (and (= :list structure)
                  (= :event element))
             (meta-list-values
              (select-attribute-values attribute di-data))
             :else
             (throw (ex-info "Select works only on meta-group-list-events and meta-list-events"
                             {:op :select
                              :structure structure
                              :group group
                              :element element})))))
   :sort-by-frequencies {:internal true}
   (fn [_instance _ {:keys [direction]} [di-data]]
     (let [{:keys [structure group element]} (meta di-data)]
       (cond (and (= :group structure)
                  (= :list group)
                  (= :value element))
             (meta-group-list-values
              (into {}
                    (map (fn [[k values]]
                           [k (sort-by-frequencies direction values)]))
                    di-data))
             (and (= :list structure)
                  (= :value element))
             (meta-list-values
              (sort-by-frequencies direction di-data))
             :else
             (throw (ex-info "Sort-by-frequencies works only on meta-group-list-values and meta-list-value"
                             {:op :sort-by-frequencies
                              :structure structure
                              :group group
                              :element element})))))
   :take-first {:internal true}
   (fn [_instance _ {} [di-data]]
     (let [{:keys [structure group element]} (meta di-data)]
       (cond (and (= :group structure)
                  (= :list group)
                  (= :value element))
             (meta-group-values (into {}
                                      (map (fn [[k [v]]]
                                             [k v]))
                                      di-data))
             (and (= :list structure)
                  (= :value element))
             (meta-primitive-value
              [(first di-data)])
             :else
             (throw (ex-info "take-first works only on meta-group-list-values and meta-list-value"
                             {:op :sort-by-frequencies
                              :structure structure
                              :group group
                              :element element})))))
   :take-last {:internal true}
   (fn [_instance _ {} [di-data]]
     (let [{:keys [structure group element]} (meta di-data)]
       (cond (and (= :group structure)
                  (= :list group)
                  (= :value element))
             (meta-group-values (into {}
                                      (map (fn [[k values]]
                                             [k (peek values)]))
                                      di-data))
             (and (= :list structure)
                  (= :value element))
             (meta-primitive-value
              [(peek di-data)])
             :else
             (throw (ex-info "take-last works only on meta-group-list-values and meta-list-value"
                             {:op :sort-by-frequencies
                              :structure structure
                              :group group
                              :element element})))))
   :union {:category :set
           :description "Applies union two multiple datasets"}
   (fn  [instance _ _ sets]
     (parameter-count-wrapper 2 (vec sets))
     (meta-list-events
      (apply ff/union instance (map set sets))))
   :intersection {:category :set
                  :description "Applies intersection two multiple datasets"}
   (fn [instance _ _ sets]
     (parameter-count-wrapper 2 (vec sets))
     (meta-list-events
      (apply ff/intersection instance (map set sets))))
   :intersection-by {:internal true}
   (fn [instance _ by sets]
     (parameter-count-wrapper 2 sets by)
     (meta-list-events
      (intersect-by instance by sets)))
   :difference {:category :set
                :description "Applies difference two multiple datasets"}
   (fn [instance _ _ sets]
     (parameter-count-wrapper 2 sets)
     (meta-list-events
      (apply ff/difference instance (map set sets))))
   :sym-difference {:category :set
                    :description "Applies sym-difference two multiple datasets"}
   (fn [instance _ _ sets]
     (parameter-count-wrapper 2 sets)
     (meta-list-events
      (ff/difference instance
                     (apply ff/union instance (map set sets))
                     (apply ff/intersection instance (map set sets)))))
   :filter {:internal true}
   (fn [instance filters filter-id [data & too-many?]]
     (when (seq too-many?)
       (error error-msg filter-id data too-many?)
       (throw (ex-info error-msg {})))
     (meta-list-events
      (f/filter-data-api (get filters filter-id)
                         data
                         instance)))
   :apply-layout {:internal true}
   (fn [_instance _ {layouts :layouts
                     reverse-color? :reverse-color?}
        [data]]
     (apply-layout-operation layouts reverse-color? data))))

(defn prepare-data-perform-op [data-tile-sets]
  (into {} (map (fn [[key value]]
                  [key (prepare-data value)])
                data-tile-sets)))

(defn execute [results result-frequency-atom data-tile-sets filters instance [op param & data-sets :as operation]]
  (assert (functions op) (str "function for " op " does not exists"))
  (apply dissoc
         (assoc results
                operation
                ((functions op)
                 instance
                 filters
                 param
                 (map (fn [data-set]
                        (cond (contains? results data-set)
                              (get results data-set)
                              (contains? data-tile-sets data-set)
                              (meta-list-events
                               (get data-tile-sets data-set))
                              :else
                              data-set))
                      data-sets)))
         (let [{:keys [removed]}
               (swap! result-frequency-atom
                      (fn [current-state]
                        (reduce (fn [{state :state :as result} data-set]
                                  (if (= 1 (get state data-set))
                                    (-> (update result :state dissoc data-set)
                                        (update :removed conj data-set))
                                    (update-in result [:state data-set] dec)))
                                current-state
                                (filter #(contains? results %) data-sets))))]
           removed)))

(comment
  [[:union [:filter "f1" "d1"] [:filter "f2" "d2"]]] ;stack
  [:union [:filter "f1" "d1"] [:filter "f2" "d2"]] ; peek -> no result and not executeable
  ; push

  [[:union [:filter "f1" "d1"] [:filter "f2" "d2"]] [:filter "f2" "d2"] [:filter "f1" "d1"]] ; stack 
  [:filter "f1" "d1"] ; peek  -> no result but executeable
  ; pop

  [[:union [:filter "f1" "d1"] [:filter "f2" "d2"]] [:filter "f2" "d2"]] ; stack
  [:filter "f2" "d2"] ; peek  -> no result but executeable
  ; pop

  [[:union [:filter "f1" "d1"] [:filter "f2" "d2"]] [:filter "f2" "d2"]] ; stack
  [:union [:filter "f1" "d1"] [:filter "f2" "d2"]] ; peek -> no result but executeable
  ; pop

  []) ; stack
  ; -> done

(defn pre-perform-operation* [operations]
  (loop [stack [(vec operations)]
         results {}]
    (if (empty? stack)
      results
      (let [current-operation (peek stack)]
        (recur (into (pop stack)
                     (filter #(not (or (string? %)
                                       (number? %)))
                             (drop 2 current-operation)))
               (update results current-operation (fnil inc 0)))))))

(defn perform-operation* [operations action]
  (loop [stack [(vec operations)]
         results {}]
    (if (empty? stack)
      (get results operations)
      (let [current-operation (peek stack)
            [stack results]
            (if (every? #(or (string? %)
                             (number? %)
                             (contains? results %))
                        (drop 2 current-operation))
              [(pop stack)
               (action results current-operation)]
              [(into stack (filter #(not (or (string? %)
                                             (number? %)
                                             (contains? results %)))
                                   (drop 2 current-operation)))
               results])]
        (recur stack
               results)))))

(defn perform-operation
  ([data-tile-sets
    filters
    operations]

   (perform-operation data-tile-sets filters operations ff/default-impl))
  ([data-tile-sets
    filters
    operations
    instance]
   (let [every-grouped? (every? #(map? (second %))
                                data-tile-sets)
         every-not-grouped? (every? #(or (vector? (second %))
                                         (set? (second %)))
                                    data-tile-sets)
         input-valid? (or every-grouped?
                          every-not-grouped?)]
     (when-not input-valid?
       (throw (ex-info "Input-Data not valid for operation. Every di needs to be grouped or a list of data." {}))))
   (let [result-frequency (pre-perform-operation* operations)
         result-frequency-atom (atom {:state result-frequency
                                      :removed #{}})]
     (perform-operation* operations
                         (fn [results current-operation]
                           (execute results
                                    result-frequency-atom
                                    data-tile-sets
                                    filters
                                    instance
                                    current-operation))))))

(defn- attach-bucket [data-event-map attach-buckets?]
  (if attach-buckets?
    (into {} (map (fn [[data-tile-key events]]
                    [data-tile-key
                     (let [bucket (attrs/value data-tile-key "bucket")]
                       (mapv #(assoc % (attrs/access-key "bucket") bucket)
                             events))]))
          data-event-map)
    data-event-map))

(defn retreive-data-tiles [data-tile-refs data-tile-lookup data-tile-retrieval data-tile-limit {:keys [attach-buckets?]}]
  (letfn [(retreive [desc]
            (if data-tile-limit
              (->  desc
                   data-tile-lookup
                   (data-tile-retrieval {:abort-early {:data-tile-limit data-tile-limit}})
                   (attach-bucket attach-buckets?)
                   prepare-data)
              (->  desc
                   data-tile-lookup
                   data-tile-retrieval
                   (attach-bucket attach-buckets?)
                   prepare-data)))]
    (reduce (fn [acc [data-tile-key data-tile-desc]]
              (if data-tile-limit
                (let [new-data (retreive data-tile-desc)
                      {events :events
                       :or {events 0}}
                      (meta acc)
                      events (+ (count new-data) events)]
                  (if (<= data-tile-limit events)
                    (throw (ex-info "Data-tile limited exceeded"
                                    {:events events
                                     :reason :data-tile-limit}))
                    (vary-meta (assoc acc
                                      data-tile-key
                                      new-data)
                               assoc
                               :events
                               events)))
                (assoc acc
                       data-tile-key
                       (retreive data-tile-desc))))
            {}
            data-tile-refs)))

(defn ensure-simple-operations [data-instance]
  (and (loop [[op filter-key data-key] (:di/operations data-instance)]
         (cond (and op
                    filter-key
                    data-key
                    (= :filter op)
                    (vector? data-key)
                    (string? filter-key))
               (recur data-key)
               (and op
                    filter-key
                    data-key
                    (= :filter op)
                    (string? filter-key)
                    (string? data-key))
               true
               :else
               false))
       (= 1 (count (:di/data-tile-ref data-instance)))))

(defn transform [data-instance data-tile-lookup data-tile-retrieval & opts]
  (if-not data-instance
    (do (warn "Data-instance is nil")
        [])
    (let [{:keys [instance]
           {data-tile-limit :data-tile-limit
            result-limit :result-limit
            result-chunk-size :result-chunk-size}
           :abort-early
           post-fn :post-fn
           :or {instance ff/default-impl
                result-chunk-size 1000000
                post-fn identity}}
          (when opts (apply hash-map opts))
          _ (when result-limit
              (assert (ensure-simple-operations data-instance)
                      "Result-limit is only allowed for simple operations [:filter \"some-filter\" \"some-data-tile-set\"]"))
          data-tile-set (retreive-data-tiles (:di/data-tile-ref data-instance)
                                             data-tile-lookup
                                             data-tile-retrieval
                                             data-tile-limit
                                             opts)]
      (post-fn
       (if result-limit
         (let [data-set-key (first (keys data-tile-set))
               result
               (reduce (fn [acc data-tile-set]
                         (if (<= result-limit (count acc))
                           (throw (ex-info "Result limited exceeded"
                                           {:events (count acc)
                                            :reason :result-limit}))
                           (into acc
                                 (perform-operation data-tile-set
                                                    (:di/filter data-instance)
                                                    (:di/operations data-instance)
                                                    instance))))
                       []
                       (map (fn [partial-data-tile-set]
                              {data-set-key (vec partial-data-tile-set)})
                            (partition-all result-chunk-size (first (vals data-tile-set)))))]
           result)
         (perform-operation data-tile-set
                            (:di/filter data-instance)
                            (:di/operations data-instance)
                            instance))))))
