(ns de.explorama.frontend.woco.frame.filter.util
  (:require ["date-fns" :refer [parse format isBefore isAfter min max getYear]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.data-format.date-filter :as df]))

;char for .. when pruning text
(def prune-char \u2026) ;".." \u2025

(def date-format "yyyy-MM-dd")

(defn date->moment [dobj]
  (when dobj
    (if (string? dobj)
      (js/Date. dobj)
      dobj)))

(defn moment->date [^js mobj]
  (when mobj
    (if (instance? js/Date mobj)
      mobj
      (js/Date. mobj))))

(defn date<-
  "Parse date-str as a Date object, using date format \"yyyy-MM-dd\".
   Used for dates as needed by UI description."
  [date-str]
  (cond (string? date-str)
        (parse date-str date-format (js/Date.))
        (instance? js/Date date-str)
        (date->moment date-str)
        :else date-str))

(defn date->
  "Format date-obj as a Date string, using date format \"yyyy-MM-dd\".
   Used for dates as needed by Filter description."
  [date-obj]
  (if (string? date-obj)
    date-obj
    (format date-obj date-format)))

(defn is-before? [^js mobj1 mobj2]
  (try
    (isBefore mobj1 mobj2)
    (catch :default _e
      false)))

(defn is-after? [^js mobj1 mobj2]
  (try
    (isAfter mobj1 mobj2)
    (catch :default _e
      false)))

(defn date-min [& dates]
  (min (clj->js (mapv date<- dates))))

(defn date-max [& dates]
  (max (clj->js (mapv date<- dates))))

(defn year-min [& dates]
  (apply min (mapv #(if (number? %)
                      %
                      (-> % date<- getYear))
                   dates)))

(defn year-max [& dates]
  (apply max (mapv #(if (number? %)
                      %
                      (-> % date<- getYear))
                   dates)))

;#######################
;    UI description to Filter description
;#######################

(def date-attrs #{"year" "month" "date"})

(defn gen-filter-string
  "Returns a filter map for a property based on an operation attribute and a value."
  [op attr value]
  {:de.explorama.shared.data-format.filter/op    op
   :de.explorama.shared.data-format.filter/prop  attr
   :de.explorama.shared.data-format.filter/value value})

(defn gen-filter
  "Returns a filter map based on Operation, Attribute and Value"
  [op attr value]
  {:de.explorama.shared.data-format.filter/op    op
   :de.explorama.shared.data-format.filter/prop  (attrs/access-key attr)
   :de.explorama.shared.data-format.filter/value value})

(defn- gen-filter-multi
  [attr-key value]
  (map (fn [val] (gen-filter := attr-key (val :value))) value))

(defn- gen-textsearch-filter
  [attr-key value]
  (gen-filter :includes
              attr-key
              value))

(defn ui-selection-type
  " Associate attribute name and constraint with attribute type,
  resulting to the following data format:

  {[datasource :std] :string,
  [date :std] :date,
  [fact-1 :std] :number,
  [country :std] :string,
  [date :year] :year,
  [org :std] :string"
  [data-acs]
  (reduce (fn [result [key contraints]]
            (reduce (fn [result [constraint-key {ftype :type}]]
                      (assoc-in result
                                {key constraint-key}
                                ftype))
                    result
                    contraints))
          {}
          data-acs))

(defn desc-to-filter
  "Transform attribute to a filter condition.
   Types can be :year :string :number :date"
  [attribute type value textsearch?]
  (let [attr (first attribute)
        filter-fn (if (date-attrs attr)
                    gen-filter-string
                    gen-filter)
        attr-key (case attribute
                   ["date" :year] :de.explorama.shared.data-format.dates/year
                   ["date" :month] :de.explorama.shared.data-format.dates/month
                   ["date" :std] :de.explorama.shared.data-format.dates/full-date
                   attr)]
    (cond
      (and (= type :string)
           textsearch?)
      [:and (gen-textsearch-filter attr-key value)]
      (= type  :string)
      (into [:or] (gen-filter-multi attr-key value))
      (= type :date)
      (into [:and] [(filter-fn :>= attr-key (date-> (value :start-date))) (filter-fn :<= attr-key (date-> (value :end-date)))])
      (#{:year :number} type)
      (into [:and] [(filter-fn :>= attr-key (first value)) (filter-fn :<= attr-key (second value))])
      :else
      [:and (filter-fn :>= attr-key (first value)) (filter-fn :<= attr-key (second value))])))

(defn ui-app-state->filter-desc
  "Transform the UI App description to a Filter description.

  UI App description format:
  {:selected-ui   {\"country\"      {:std [{:value \"Country A\"
                                                  :label \"Country A\"}]}
                   \"org\" {:std [{:value \"Foo\"
                                                   :label \"Foo\"}
                                           {:value \"Bar\"
                                                   :label \"Bar\"}]}
                  \"fact-1\"   {:std [1 4]}
                  \"date\"         {:std  {:start-date (date<- \"2004-01-31\")
                                           :end-date   (date<- \"2015-01-31\")}
                                    :year [2004 2016]}}
   :selected-ui-attributes [[\"date\" :year]
                            [\"fact-1\" :std]
                            [\"country\" :std]
                            [\"date\" :std]]}

  Filter description format:
  [:and
    [:and
     {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/year, :de.explorama.shared.data-format.filter/value 2004}
     {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/year, :de.explorama.shared.data-format.filter/value 2016}]
    [:and
     {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop \"fact-1\", :de.explorama.shared.data-format.filter/value 2}
     {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop \"fact-1\", :de.explorama.shared.data-format.filter/value 4}]
    [:or
     {:de.explorama.shared.data-format.filter/op :=, :de.explorama.shared.data-format.filter/prop \"country\", :de.explorama.shared.data-format.filter/value \"Country A\"}]
    [:and
     {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value \"2004-01-31\"}
     {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value \"2015-01-31\"}]]"
  [{:keys [selected-ui-attributes selected-ui]} data-acs]
  (let [ui-selection-types (ui-selection-type data-acs)]
    (when (and (seq selected-ui-attributes) (seq ui-selection-types))
      (into [:and] (map (fn [attribute]
                          (let [value (get-in selected-ui attribute)
                                textsearch? (get-in data-acs (conj attribute :text-search?))]
                            (desc-to-filter attribute (ui-selection-types attribute) value textsearch?)))
                        selected-ui-attributes)))))

;#######################
;    Filter description to UI description
;#######################

(defn- to-property
  "Parse arguments as
  {\"property\" {:std [:value \"val\" :label \"val\"]}}
  or as
  {\"property\" {:std [from to]}}"
  ([prop value]
   {prop {:std [{:value value :label value}]}})
  ([value]
   {:value value :label value})
  ([prop from-val to-val]
   (if (and from-val to-val)
     {prop {:std (vec [from-val to-val])}}
     {prop {:std from-val}}))) ;;Handle :includes filter, little bit dirty here

(defn- to-date
  ([prop start end]
   (case prop
     :de.explorama.shared.data-format.dates/full-date {"date" {:std {:start-date (date<- start) :end-date (date<- end)}}}
     :de.explorama.shared.data-format.dates/year {"date" {:year (vec [start end])}}
     {"date" {(keyword prop) (vec [start end])}}))
  ([prop value]
   (case prop
     :de.explorama.shared.data-format.dates/month {"date" {:month [:value value :label value]}}
     {"date" {(keyword prop) [{:value value :label value}]}})))

(defn- parse-date
  " Parse date filter condition to a map:

    [:and
      #:de.explorama.shared.data-format.filter{:op :>=,
                               :prop :de.explorama.shared.data-format.dates/full-date,
                               :value 2004-01-31}
      #:de.explorama.shared.data-format.filter{:op :<=,
                               :prop :de.explorama.shared.data-format.dates/full-date,
                               :value 2015-01-31}]
      to

    {\"date\"  {:std  {:start-date \"2004-01-31\" :end-date   \"2015-01-31\"}}"
  [item]
  (let [op (first item)
        filters (rest item)]
    (case op
      :and (let [[from to] filters
                 prop (get from :de.explorama.shared.data-format.filter/prop)
                 from (get from :de.explorama.shared.data-format.filter/value)
                 to (get to :de.explorama.shared.data-format.filter/value)]
             (to-date prop from to))
      :or (mapv (fn [filter-map] (let [prop (get filter-map :de.explorama.shared.data-format.filter/prop)
                                       value (get filter-map :de.explorama.shared.data-format.filter/value)]
                                   (to-date prop value)))
                filters))))

(defn- parse-filter
  " Parse filter conditions to a map:

    [:and
        {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop \"fact-1\", :de.explorama.shared.data-format.filter/value 2}
        {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop \"fact-1\", :de.explorama.shared.data-format.filter/value 4}]
    [:or
        {:de.explorama.shared.data-format.filter/op :=, :de.explorama.shared.data-format.filter/prop \"country\", :de.explorama.shared.data-format.filter/value \"Country A\"}
        {:de.explorama.shared.data-format.filter/op :=, :de.explorama.shared.data-format.filter/prop \"country\", :de.explorama.shared.data-format.filter/value \"Albania\"}]

        to

    {\"country\"
        {:std [{:value \"Country A\" :label \"Country A\"} {:value \"Albania\"\n   :label \"Albania\"}]}
    {\"fact-1\" {:std [2 4]}"
  [item]
  (case (first item)
    :and (let [[from to] (rest item)
               prop (get from :de.explorama.shared.data-format.filter/prop)
               from (get from :de.explorama.shared.data-format.filter/value)
               to (get to :de.explorama.shared.data-format.filter/value)]
           (to-property prop from to))
    :or (let [attributes (mapv (fn [filter-map] (let [value (get filter-map :de.explorama.shared.data-format.filter/value)]
                                                  (to-property value)))
                               (rest item))
              property (get (second item) :de.explorama.shared.data-format.filter/prop)]
          {property {:std attributes}})))

(defn- filter-to-desc
  "Parse filter conditions to UI description."
  [filter]
  (if (df/filter-contains-date? filter)
    (parse-date filter)
    (parse-filter filter)))

(defn filter-desc->ui-desc
  "Transform the Filter description to UI App description.

  Filter description format:
  [:and
    [:and
     {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/year, :de.explorama.shared.data-format.filter/value 2004}
     {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/year, :de.explorama.shared.data-format.filter/value 2016}]
    [:and
     {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop \"fact-1\", :de.explorama.shared.data-format.filter/value 2}
     {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop \"fact-1\", :de.explorama.shared.data-format.filter/value 4}]
    [:or
     {:de.explorama.shared.data-format.filter/op :=, :de.explorama.shared.data-format.filter/prop \"country\", :de.explorama.shared.data-format.filter/value \"Country A\"}]
    [:and
     {:de.explorama.shared.data-format.filter/op :>=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value \"2004-01-31\"}
     {:de.explorama.shared.data-format.filter/op :<=, :de.explorama.shared.data-format.filter/prop :de.explorama.shared.data-format.dates/full-date, :de.explorama.shared.data-format.filter/value \"2015-01-31\"}]]

  UI App description format:
  {:selected-ui   {\"country\"      {:std [{:value \"Country A\"
                                                   :label \"Country A\"}]}
                            \"org\" {:std [{:value \"Foo\"
                                                            :label \"Foo\"}
                                                    {:value \"Bar\"
                                                            :label \"Bar\"}]}
                   \"fact-1\"   {:std [1 4]}
                   \"date\"         {:std  {:start-date (date<- \"2004-01-31\")
                                            :end-date   (date<- \"2015-01-31\")}
                                     :year [2004 2016]}}
   :selected-ui-attributes [[\"date\" :year]
                            [\"fact-1\" :std]
                            [\"country\" :std]
                            [\"date\" :std]]}"
  [filter-desc]
  (if (or (nil? filter-desc) (= filter-desc [:and]))
    {:selected-ui {}
     :selected-ui-attributes []}
    (let [filter-to-ui-desc (mapcat (fn [item]
                                      (when (coll? item)
                                        (filter-to-desc item)))
                                    filter-desc)
          sel-ui-attrs (mapv (fn [[k v]] (vec [k (key (first v))])) filter-to-ui-desc)
          selected-ui (reduce (fn [acc desc] (merge-with merge acc (apply hash-map desc))) {} filter-to-ui-desc)
          result (hash-map :selected-ui selected-ui :selected-ui-attributes sel-ui-attrs)]
      result)))

(defn ensure-dates
  "Ensure that start-date and end-date is set currectly on selected-ui to prevent wrong filtering when year is selected as filter"
  [selected-ui data-acs]
  (let [[start-ac-date end-ac-date] (get-in data-acs ["date" :std :vals])
        {:keys [year std] :as date-entry} (get selected-ui "date")]
    (if (and year (not std))
      (assoc selected-ui
             "date"
             (assoc date-entry
                    :std {:start-date (date<- start-ac-date)
                          :end-date (date<- end-ac-date)}))
      selected-ui)))
