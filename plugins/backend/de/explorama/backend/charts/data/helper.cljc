(ns de.explorama.backend.charts.data.helper
  (:require [clojure.string :as string :refer [lower-case]]
            [data-format-lib.dates :as df-dates]
            [data-format-lib.filter :as dfl-filter]
            [data-format-lib.operations :as dfl-op]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.backend.charts.attribute-characteristics :as ac]
            [de.explorama.shared.common.unification.misc :refer [cljc-parse-int]]
            [de.explorama.backend.charts.util :refer [attr-value
                                                      relevant-aggregation-attributes relevant-aggregation-methods]]))

(def remaining-group-name
  "Other")

(defn time-attr? [attr]
  (#{"year" "month" "day"} attr))

(defn x-axis-time [x-axis]
  (when (time-attr? x-axis)
    {:unit x-axis
     :displayFormats {:year "yyyy"
                      :month "yyyy-MM"
                      :quarter "yyyy-MM"
                      :day "yyyy-MM-dd"}}))

(def date-attributes #{"month" "year"})

(defn access-key [k]
  (if (date-attributes k)
    "date"
    k))

(defn sum-by-access-key [k]
  (if (date-attributes k)
    (keyword k)
    k))

(defn sum-all? [sum-by]
  (and
   (string? sum-by)
   (= "all" (lower-case sum-by))))

(defn normalize-attr [attr]
  (cond
    (= "year" attr) ::df-dates/year
    (= "month" attr) ::df-dates/month
    :else (attrs/access-key attr)))

(defn chart-options []
  (ac/get-client-options))

(defn attribute-value [attr-key point]
  (attr-value attr-key point {:year-month-pair? true}))

(defn sum-by->filter [{:keys [sum-by sum-by-filter]} handle-data-as-str?]
  (when (and (not (sum-all? sum-by))
             (seq sum-by-filter))
    (let [attr (if handle-data-as-str?
                 (sum-by-access-key sum-by)
                 (normalize-attr sum-by))]
      (reduce (fn [acc sum-by-value]
                (conj acc
                      {::dfl-filter/op :=
                       ::dfl-filter/prop attr
                       ::dfl-filter/value (cond (and (not handle-data-as-str?)
                                                     (#{"year" "month"} sum-by))
                                                (cljc-parse-int sum-by-value)
                                                (= sum-by "undefined")
                                                nil
                                                :else
                                                sum-by-value)}))
              [:or]
              sum-by-filter))))

(defn sort-group-datasets [group-datasets x-key]
  (->> group-datasets
       (sort-by #(get % x-key))
       (into [])))

(defn sort-datasets
  "Sort datasets based on the label.
   The sorting-function is based on the sum-by key."
  [grouped-data {:keys [x-access sum-by-access]}]
  (let [comp-function (if (#{"month" "year"} sum-by-access)
                        #(< (cljc-parse-int %1) (cljc-parse-int %2))
                        (fn [v1 v2]
                          (compare (str v1)
                                   (str v2))))]
    (->> grouped-data
         ;;sort group-data (= x-axis)
         (reduce (fn [acc [k datasets]]
                   (assoc acc
                          k
                          (vec (sort-group-datasets datasets x-access))))
                 {})
         ;;sort groups (= sum-by)
         (sort-by (fn [[{v sum-by-access} _]]
                    v)
                  comp-function))))

(defn- gather-min-max [grouped-data {:keys [sum-by-access access-fn min-fn max-fn val-fn]}]
  (reduce (fn [{:keys [min-val max-val] :as acc} {sum-by-val sum-by-access :as d}]
            (let [val (cond-> d
                        (fn? val-fn) (val-fn))]
              (cond-> acc
                (not min-val) (assoc :min-val val)
                (not max-val) (assoc :max-val val)
                max-val (update :min-val min-fn val)
                max-val (update :max-val max-fn val)
                :always (update :existing assoc (access-fn val sum-by-val) d))))
          {:existing {}}
          grouped-data))

(defmulti gen-missing-groups (fn [_ {:keys [x-axis]}] x-axis))

(defmethod gen-missing-groups "year" [grouped-data {:keys [y-access x-axis x-access y-target-access sum-by-access sum-filter sum-by] :as desc}]
  (let [sum-all? (sum-all? sum-by-access)
        {agg-default-value :default-value} (get relevant-aggregation-attributes y-access)
        sum-by-month? (and (= sum-by "month")
                           (not (keyword sum-by-access)))
        access-fn (fn [year sum-by]
                    (cond-> [year]
                      (and (not= sum-by-access x-access)
                           (not sum-all?)
                           sum-by)
                      (conj sum-by)))
        {min-year :min-val max-year :max-val :keys [existing] :as d}
        (gather-min-max grouped-data (assoc desc
                                            :min-fn min
                                            :max-fn max
                                            :val-fn (fn [d]
                                                      (let [val (get d x-access)]
                                                        (cond-> val
                                                          sum-by-month? (-> (string/split #"-")
                                                                            (first))
                                                          (or (keyword? sum-by-access)
                                                              (string? val))
                                                          (cljc-parse-int))))
                                            :access-fn access-fn))]

    (if (and min-year max-year)
      (mapv (fn [[year sum-by-val]]
              (let [access (access-fn year sum-by-val)]
                (get existing
                     access
                     (cond-> {x-access (str year)
                              y-target-access agg-default-value}
                       (and sum-by-val
                            (not= x-access sum-by-access))
                       (assoc sum-by-access sum-by-val)))))
            (reduce (fn [acc year]
                      (if (and (not sum-all?)
                               (seq sum-filter)
                               (not= x-axis sum-by))
                        (apply conj acc (mapv #(cond-> [year]
                                                 sum-by-month? (conj (str year "-" %))
                                                 (not sum-by-month?) (conj %))
                                              sum-filter))
                        (conj acc [year])))
                    []
                    (range min-year (inc max-year))))
      grouped-data)))

(defn- calc-year-month-combinations [min-year max-year min-month max-month {:keys [x-access sum-by-access sum-filter]}]
  (let [sum-all? (sum-all? sum-by-access)
        month-v-fn (fn [year]
                     (mapv (fn [month]
                             {:year year
                              :month (if (< month 10)
                                       (str "0" month)
                                       (str month))})
                           (cond (= min-year max-year year)
                                 (range min-month (inc max-month))
                                 (= year min-year)
                                 (range min-month 13)
                                 (= year max-year)
                                 (range 1 (inc max-month))
                                 :else (range 1 13))))]
    (reduce (fn [acc year]
              (let [year-month-vals (month-v-fn year)]
                (if (and (not sum-all?)
                         (seq sum-filter)
                         (not= sum-by-access x-access))
                  (reduce (fn [acc year-month]
                            (apply conj acc (map #(assoc year-month sum-by-access %)
                                                 sum-filter)))
                          acc
                          year-month-vals)
                  (apply conj acc year-month-vals))))
            []
            (range min-year (inc max-year)))))

(defmethod gen-missing-groups "month" [grouped-data {:keys [x-access y-access y-target-access year-month-workaround? sum-by-access sum-filter] :as desc}]
  (let [sum-all? (sum-all? sum-by-access)
        {agg-default-value :default-value} (get relevant-aggregation-attributes y-access)
        access-fn (fn [month-year sum-by]
                    (cond-> [month-year]
                      (and (not= sum-by-access x-access)
                           (not sum-all?)
                           sum-by)
                      (conj sum-by)))
        compare-fn (fn [f v1 v2]
                     (if (f (compare (str v1)
                                     (str v2)))
                       v1
                       v2))
        val-fn (fn [{val x-access :as d}]
                 (if year-month-workaround?
                   (str (:year d) "-" (:month d))
                   val))
        {min-year-month :min-val max-year-month :max-val :keys [existing] :as d}
        (gather-min-max grouped-data (assoc desc
                                            :val-fn val-fn
                                            :min-fn (partial compare-fn neg?)
                                            :max-fn (partial compare-fn pos?)
                                            :access-fn access-fn))
        [min-year min-month] (string/split min-year-month #"-")
        min-year (cljc-parse-int min-year)
        min-month (cljc-parse-int min-month)
        [max-year max-month] (string/split max-year-month #"-")
        max-year (cljc-parse-int max-year)
        max-month (cljc-parse-int max-month)]
    (if (and min-year-month max-year-month)
      (mapv (fn [{:keys [year month] sum-by-val sum-by-access}]
              (let [year-month (str year "-" month)
                    access (access-fn year-month sum-by-val)]
                (get existing
                     access
                     (cond-> {y-target-access agg-default-value}

                       year-month-workaround?
                       (assoc :month month :year year)

                       (and (not year-month-workaround?)
                            x-access)
                       (assoc x-access year-month)

                       (and sum-by-val
                            (not= sum-by-access x-access))
                       (assoc sum-by-access sum-by-val)))))
            (calc-year-month-combinations min-year max-year min-month max-month desc))

      grouped-data)))

(defn cart [colls]
  (if (empty? colls)
    '(())
    (for [more (cart (rest colls))
          x (first colls)]
      (cons x more))))

(defmethod gen-missing-groups :default [grouped-data {:keys [sum-by x-access year-month-workaround? sum-by-access sum-filter] :as desc}]
  (if (sum-all? sum-by)
    grouped-data
    (let [x-vals (sort (set (map #(get % x-access) grouped-data)))
          existing (persistent!
                    (reduce (fn [acc {x-val x-access sum-by-val sum-by-access :as d}]
                              (assoc! acc [x-val sum-by-val] d))
                            (transient {})
                            grouped-data))]
      (-> (reduce (fn [acc [x-val sum-by-val]]
                    (conj! acc
                           (get existing
                                [x-val sum-by-val]
                                {x-access x-val
                                 sum-by-access sum-by-val})))
                  (transient [])
                  (vec (cart [x-vals (if (seq sum-filter)
                                       sum-filter
                                       #{nil})])))
          (persistent!)))))

(defn- apply-calc-sum-by-others [grouped-result filtered-result {:keys [sum-by y-target-access sum-by-access y-axis year-month-workaround? attr group-attributes aggregation-desc]}]
  (let [{agg-default-value :default-value agg-need-attribute? :need-attribute?} (get relevant-aggregation-attributes y-axis)
        existing-sum-by-vals (set (map #(get % sum-by-access) (keys filtered-result)))
        other-groups (reduce (fn [acc {attr-val y-target-access sum-by-val sum-by-access}]
                               (cond-> acc
                                 (not (existing-sum-by-vals sum-by-val))
                                 (conj {sum-by-access remaining-group-name
                                        y-target-access attr-val})))
                             []
                             grouped-result)
        group-attributes (if year-month-workaround?
                           (mapv sum-by-access-key group-attributes)
                           group-attributes)]
    (if (and (seq other-groups)
             attr sum-by)
      (assoc filtered-result
             {sum-by-access remaining-group-name}
             (dfl-op/perform-operation {"di1" other-groups}
                                       nil
                                       (cond->> "di1"
                                         :always (conj [:group-by {:attributes group-attributes}])
                                         agg-need-attribute? (conj aggregation-desc)
                                                                       ;; handle for example number of events where we want to sum the number of events from other-groups
                                         (not agg-need-attribute?) (conj [:sum {:attribute y-target-access}])
                                         :always (conj [:heal-event
                                                        {:policy :merge
                                                         :descs [{:attribute y-target-access}]}]))))
      filtered-result)))

(defn- gen-sum-by-op-desc [group-attributes date-keyword-attr?]
  [:group-by (cond-> {:attributes (cond->> group-attributes
                                    date-keyword-attr? (mapv sum-by-access-key))}
               (not date-keyword-attr?) (assoc :ignore-hierarchy? true
                                               :granularity-attr? true))
   "di1"])

(defn- gen-op-desc [group-attributes aggregation-desc {:keys [add-agg-desc group-by-args]
                                                       :or {group-by-args {}}
                                                       {:keys [y-target-access agg-target-access]} :desc}]
  ;; add-agg-desc is needed for exampe for add size attribute for bubblecharts
  (cond-> [:heal-event
           {:policy :merge
            :descs (cond-> []
                     add-agg-desc
                     (conj {:attribute agg-target-access})
                     aggregation-desc
                     (conj {:attribute y-target-access}))}]
    add-agg-desc
    (conj
     (conj add-agg-desc
           [:group-by (assoc group-by-args :attributes group-attributes)
            "di1"]))
    aggregation-desc
    (conj
     (conj aggregation-desc
           [:group-by (assoc group-by-args :attributes group-attributes)
            "di1"]))))

(defn gen-grouping-desc [{:keys [y-axis x-axis sum-by sum-filter] :as desc}]
  (let [y-key (cond-> y-axis
                (string? y-axis) (attrs/access-key))
        x-key (attrs/access-key x-axis)
        sum-by-key (attrs/access-key sum-by)
        year-month-workaround? (or (and
                                    (date-attributes x-axis)
                                    (date-attributes sum-by))
                                   (and
                                    (not x-axis)
                                    (date-attributes sum-by)))
        sum-by-access (sum-by-access-key sum-by-key)
        x-access (if year-month-workaround?
                   (sum-by-access-key x-key)
                   (access-key x-key))
        y-access (access-key y-key)]
    (assoc desc
           :year-month-workaround? year-month-workaround?
           :sum-by-access sum-by-access
           :sum-by-key sum-by-key
           :sum-by sum-by-key
           :sum-by-filter sum-filter
           :x-access x-access
           :y-access y-access
           :x-axis x-key
           :y-axis y-key)))

(defn chart-grouping [data {:keys [x-axis y-axis sum-by year-month-workaround?
                                   sum-by-access
                                   aggregation-method additional-aggregation-attr calc-sum-by-others?]
                            :as desc}]
  (let [{dfl-agg-attr :attribute agg-attr-op :dfl-op} (get relevant-aggregation-attributes y-axis)
        {agg-op :dfl-op} (get relevant-aggregation-methods aggregation-method)
        {dfl-add-attr :attribute agg-add-op :dfl-op} (when additional-aggregation-attr
                                                       (or (get relevant-aggregation-attributes additional-aggregation-attr)
                                                           (get relevant-aggregation-methods additional-aggregation-attr)
                                                           (get relevant-aggregation-methods :sum)))
        sum-all? (sum-all? sum-by)
        attr (if (and agg-attr-op dfl-agg-attr)
               dfl-agg-attr
               y-axis)
        year-month-workaround (cond
                                (and year-month-workaround? (not x-axis))
                                [sum-by]
                                year-month-workaround?
                                ["year" "month"])
        group-by-sum-by? (and x-axis
                              (not sum-all?)
                              (not (and
                                    (date-attributes x-axis)
                                    (date-attributes sum-by))))
        group-attributes (cond
                           group-by-sum-by?
                           [x-axis sum-by]
                           year-month-workaround
                           year-month-workaround
                           x-axis
                           [x-axis]
                           :else
                           [sum-by])
        aggregation-desc (conj (or agg-attr-op agg-op [:sum])
                               {:attribute attr})
        additional-aggregation-desc (when agg-add-op
                                      (conj agg-add-op
                                            {:attribute additional-aggregation-attr}))
        group-year-month-workaround? (and (or year-month-workaround group-by-sum-by?)
                                          (date-attributes sum-by))
        group-by-args (cond-> {}
                        group-year-month-workaround?
                        (assoc :ignore-hierarchy? true
                               :granularity-attr? true))
        sum-by-filter (sum-by->filter desc (boolean (seq group-by-args)))
        op-desc (if (and agg-add-op dfl-add-attr)
                  (gen-op-desc group-attributes aggregation-desc (cond-> {:add-agg-desc additional-aggregation-desc
                                                                          :add-agg-attr additional-aggregation-attr
                                                                          :desc desc}
                                                                   group-year-month-workaround? (assoc :group-by-args group-by-args)))
                  (gen-op-desc group-attributes aggregation-desc (cond-> {:desc desc}
                                                                   group-year-month-workaround?
                                                                   (assoc :group-by-args group-by-args))))
        sum-by-op-desc (when-not sum-all?
                         (gen-sum-by-op-desc [sum-by] (boolean (seq group-by-args))))]

    (if (or (and sum-by (seq sum-by-filter))
            sum-all?)
      (let [grouped-result (dfl-op/perform-operation
                            {"di1" (if (set? data)
                                     (vec data)
                                     data)}
                            nil
                            op-desc)
            filtered-result (cond-> grouped-result
                              (seq grouped-result)
                              (gen-missing-groups (assoc desc :y-access attr))
                              :always (-> (set)
                                          (vec)))
            filtered-result (if sum-by-filter
                              (dfl-op/perform-operation
                               {"di1" filtered-result}
                               {"f1" sum-by-filter}
                               [:filter "f1" "di1"])
                              filtered-result)
            filtered-result (cond
                              sum-all?
                              {{sum-by-access "All"} filtered-result}

                              sum-by-op-desc
                              (dfl-op/perform-operation
                               {"di1" filtered-result}
                               nil
                               sum-by-op-desc)

                              :else filtered-result)
            ;; filtered-result (if (= sum-by "month")
            ;;                   (handle-month-groups filtered-result desc)
            ;;                   filtered-result)]
            filtered-result (cond
                              (and calc-sum-by-others? (not sum-all?))
                              (apply-calc-sum-by-others grouped-result filtered-result (assoc desc
                                                                                              :attr attr
                                                                                              :group-attributes group-attributes
                                                                                              :aggregation-desc aggregation-desc))
                              :else filtered-result)]
                              ;; (= x-access sum-by-access :month))]
        (sort-datasets filtered-result desc))
      [])))
