(ns de.explorama.backend.search.datainstance.filter
  (:require [clojure.string :as str]
            [de.explorama.shared.data-format.filter :as f]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.backend.search.attribute-characteristics.api :as acs]
            [de.explorama.shared.search.conditions-utils :as cond-utils]
            [taoensso.timbre :refer [warn] :as log]
            #?(:cljs ["date-fns" :refer [format]])))

(defn condition->pred-op [advanced condition]
  (if (and advanced condition)
    (keyword condition)
    :=))

(defn is-range? [advanced condition value values from to]
  (cond
    ;Kann auch hier passieren, wenn man den Modus wechselt, da wir die Werte erhalten
    (and advanced (not (cond-utils/range-condition? condition)))
    false
    :else
    (and from
         to)))

(defn is-single? [advanced condition value values from to]
  (cond
    ;Kann auch hier passieren, wenn man den Modus wechselt, da wir die Werte erhalten
    (and advanced (cond-utils/range-condition? condition))
    false
    :else
    (and value
         (empty? values))))

(defn is-multi? [value values from to]
  (and (not-empty values)
       (not value)
       (not from)
       (not to)))

(defn is-date? [selected-date start-date end-date]
  (or selected-date
      (and start-date
           end-date)))

(defn is-current? [advanced condition]
  (and advanced
       (cond-utils/current-condition? condition)))

(defn is-last-x? [advanced condition last-x-value]
  (and advanced
       (cond-utils/last-x? condition)
       last-x-value))

(defn gen-filter
  "Returns a filter map based on Operation, Attribute and Value"
  [op attr value]
  {::f/op    op
   ::f/prop  (attrs/access-key attr)
   ::f/value value})

(defn gen-date-int-filter
  "Returns a filter map for date based on operation attribute and value"
  [op attr value]
  {::f/op    op
   ::f/prop  attr
   ::f/value value})

(defn gen-date-filter
  "Returns a filter map for date based on operation attribute and value"
  ([op attr value extra-val]
   {::f/op    op
    ::f/prop  attr
    ::f/value value
    ::f/extra-val extra-val})
  ([op attr value]
   {::f/op    op
    ::f/prop  attr
    ::f/value value}))

(defn location->filter [values]
  {::f/op    :in-geo-rect
   ::f/prop  attrs/location-attr
   ::f/value values})

(defn single-value->filter [attribute advanced condition value]
  (let [op (condition->pred-op advanced condition)
        attr attribute]
    (gen-filter op attr value)))

(defn multi-value->filter [attribute advanced condition values]
  (let [op (condition->pred-op advanced condition)
        attr attribute
        conj-ele (case op
                   :not= :and
                   :or)]
    (vec (cons conj-ele
               (mapv (fn [val]
                       (gen-filter op attr val))
                     values)))))

(def date-attrs #{"year" "month" "day"})

(defn empty-non-empty->filter [attr all-values? empty-values?]
  (let [filter-fn (if (date-attrs attr)
                    gen-date-filter
                    gen-filter)
        attr (case attr
               "year" :de.explorama.shared.data-format.dates/year
               "month" :de.explorama.shared.data-format.dates/month
               "day" :de.explorama.shared.data-format.dates/full-date
               attr)]
    (cond-> [:or]
      all-values?  (conj (filter-fn :non-empty attr nil))
      empty-values? (conj (filter-fn :empty attr nil)))))

(defn range->filter [attribute advanced condition from to]
  (let [attr attribute
        from-val from
        to-val to
        r-filter [:and (gen-filter :>= attr from-val)
                  (gen-filter :<= attr to-val)]]
    (cond
      (and advanced (= "in range" condition))
      r-filter
      (not (and advanced condition)) r-filter
      (and advanced (= "not in range" condition))
      (f/negate r-filter)
      :else nil)))

(defn moment->date-str [date]
  (if date
    #?(:cljs (format date "yyyy-MM-dd")
       :clj (.format date "yyyy-MM-dd"))
    nil))

(defn date->filter [attribute advanced condition selected-date start-date end-date]
  (let [attr attrs/date-attr
        selected-str (moment->date-str selected-date)
        start-str (moment->date-str start-date)
        end-str (moment->date-str end-date)
        op (when selected-str (condition->pred-op advanced condition))
        sel-filter (when selected-str (gen-date-filter op attr selected-str))
        r-filter [:and (when start-str (gen-date-filter :>= attr start-str))
                  (when end-str (gen-date-filter :<= attr end-str))]]
    (if (and advanced condition)
      (case condition
        "in range" r-filter
        "not in range" (f/negate r-filter)
        sel-filter)
      r-filter)))

(defn month->filter [attribute advanced values condition last-x-vals all-values?]
  (let [op (condition->pred-op advanced condition)
        attr (if (#{"last-x" "current"} condition)
               :de.explorama.shared.data-format.dates/full-date
               :de.explorama.shared.data-format.dates/month)
        conj-ele (case op
                   :not= :and
                   :or)]
    (cond all-values?
          [:and (gen-date-int-filter :non-empty attr nil)]
          (and advanced (#{"empty" "non-empty" "last-x" "current"} condition))
          (case condition
            "empty" [:and (gen-date-int-filter :empty attr nil)]
            "non-empty" [:and (gen-date-int-filter :non-empty attr nil)]
            "last-x" [:and (gen-date-filter :last-x-months attr "today" last-x-vals)]
            "current" [:and (gen-date-filter :current-month attr "today")])

          :else
          (into [conj-ele]
                (map (fn [val]
                       (gen-date-int-filter op attr val))
                     values)))))

(defn year->filter [attribute advanced condition value from to last-x-vals all-values?]
  (let [attr :de.explorama.shared.data-format.dates/year
        op (when value (condition->pred-op advanced condition))
        val-filter (when value (gen-date-int-filter op attr value))
        from-val from
        to-val to]
    (cond
      all-values?
      [:and (gen-date-int-filter :non-empty attr nil)]

      (and advanced condition)
      (case condition
        "empty" [:and (gen-date-int-filter :empty attr nil)]
        "non-empty" [:and (gen-date-int-filter :non-empty attr nil)]
        "in range" [:and (gen-date-int-filter :>= attr from-val)
                    (gen-date-int-filter :<= attr to-val)]
        "not in range" [:or (gen-date-int-filter :< attr from-val)
                        (gen-date-int-filter :> attr to-val)]
        "last-x" [:and (gen-date-filter :last-x-years attr "today" last-x-vals)]
        "current" [:and (gen-date-filter :current-year attr "today")]
        val-filter)
      :else
      [:and (gen-date-int-filter :>= attr from-val)
       (gen-date-int-filter :<= attr to-val)])))

(defn day->filter
  [attribute advanced condition value from to last-x-vals all-values?]
  (let [;; selection by day as month day (1 to 31) not in UI yet.
        attr :de.explorama.shared.data-format.dates/full-date
        op (when value (condition->pred-op advanced condition))
        val-filter (when value (gen-date-filter op attr value))
        [value from-val to-val] [value from to]]
    (cond
      all-values?
      [:and (gen-date-int-filter :non-empty attr nil)]

      (and advanced condition)
      (case condition
        "empty" [:and (gen-date-filter :empty attr nil)]
        "non-empty" [:and (gen-date-filter :non-empty attr nil)]
        "in range" [:and (gen-date-filter :>= attr from-val)
                    (gen-date-filter :<= attr to-val)]
        "not in range" (f/negate [:and (gen-date-filter :>= attr from-val)
                                  (gen-date-filter :<= attr to-val)])
        "last-x" [:and (gen-date-filter :last-x-days attr "today" last-x-vals)]
        "current" [:and (gen-date-filter :current-day attr "today")]
        val-filter)
      :else
      [:and (gen-date-filter :>= attr from-val)
       (gen-date-filter :<= attr to-val)])))

(defn fulltext->filter [attribute advanced condition value]
  (let [searched-value (or value "")
        splitted-text (str/split searched-value #" ")]
    (cond
      (and advanced
           (= condition "exact term")) (gen-filter :includes attribute searched-value)
      (and advanced
           (= condition "excludes")) (gen-filter :excludes attribute searched-value)
      ;default is or, and advanced or is the same as default
      :else (reduce (fn [res val]
                      (conj res
                            (gen-filter :includes attribute val)))
                    [:or]
                    splitted-text))))

(defn data->filter [[[attribute label-attribute]
                     {:keys [value values from to advanced
                             selected-date start-date end-date
                             all-values? empty-values? last-x]
                      condition     :cond
                      :as           data}]]
  (let [condition (get condition :value condition)
        from (get from :value from)
        to (get to :value to)
        value (get value :value value)
        type (acs/attribute-type [attribute label-attribute])]
    (log/trace "data->filter" {:attribute       attribute
                               :label-attribute label-attribute
                               :data            data
                               :condition       condition
                               :type            type})
    (cond
      (and (= attrs/context-node label-attribute)
           (= attrs/location-attr attribute))
      (location->filter values)
      (and advanced (or all-values? empty-values?))
      (empty-non-empty->filter attribute all-values? empty-values?)
      (= attribute "month")
      (month->filter attribute advanced values condition last-x all-values?)
      (= attribute attrs/year-attr)
      (year->filter attribute advanced condition value from to last-x all-values?)
      (= attribute "day")
      (day->filter attribute advanced condition selected-date start-date end-date last-x all-values?)
      (= type attrs/notes-attr)
      (fulltext->filter attribute advanced condition value)
      (is-date? selected-date start-date end-date)
      (date->filter attribute advanced condition value start-date end-date)
      (is-single? advanced condition value values from to)
      (single-value->filter attribute advanced condition value)
      (is-multi? value values from to)
      (multi-value->filter attribute advanced condition values)
      (is-range? advanced condition value values from to)
      (range->filter attribute advanced condition from to)
      :else (do
              (warn "Ooops something went wrong here for attribute" attribute)
              (warn "Following data was found in formdata:" data)
              nil))))

(defn formdata->filter
  "Returns a list of predicates from the formdata.
  The format is defined by de.explorama.shared.data-format.filter/pred."
  [formdata]
  (log/trace "formdata->filter" {:formdata formdata})
  (cons :and (->> (map data->filter
                       (seq formdata))
                  (filterv identity))))
