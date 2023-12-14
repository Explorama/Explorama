(ns data-format-lib.filter
  "Namespace containing the specifications of filters on data and the specifications for filter."
  (:require [clojure.walk :as walk]
            [cuerdas.core :as str]
            [data-format-lib.dates :as dates]
            [data-format-lib.filter-functions :as ff]
            [malli.core :as m]))

(def filter-registry
  {::op    [:enum := :not= :< :> :>= :<= :in :not-in
            :includes :excludes :has :has-not :empty :non-empty
            :in-geo-rect :not-in-geo-rect]
   ::prop  [:or :string :keyword]
   ::pred  [:map
            [::op [:ref ::op]]
            [::prop [:ref ::prop]]
            [::value :any]]
   ::preds [:cat
            [:enum :and :or]
            [:repeat [:or [:ref ::preds] [:ref ::pred]]]]})

(defn filter-spec [return]
  [:schema {:registry filter-registry}
   return])

(def filter? (m/validator (filter-spec ::preds)))
(def pred? (m/validator (filter-spec ::pred)))

(defn- in [v coll & _]
  (contains? (set coll) v))

(defn- has [coll v & _]
  ;! do not know about this - is there a case that uses has as a filter?
  (if (string? coll)
    (= coll v)
    (contains? (set coll) v)))

(defn- includes? [v1 v2 & _]
  (str/includes? (str/lower v1) (str/lower v2)))

(defn empty-val? [val & _]
  (if (string? val)
    (str/blank? val)
    (nil? val)))

(defn safe [f]
  (fn [x1 x2 & _]
    (and x1 x2 (f x1 x2))))

(defn- wrapper [f]
  (fn [x1 x2 & _]
    (f x1 x2)))

(defn- in-geo-rect [[p-lat p-long] [bot-left-lat bot-left-long top-right-lat top-right-long] & _]
  (and (<= bot-left-lat p-lat)
       (<= p-lat top-right-lat)
       (if (< top-right-long bot-left-long)
         (or (<= bot-left-long p-long)
             (<= p-long top-right-long))
         (and (<= bot-left-long p-long)
              (<= p-long top-right-long)))))

(defn- standard-op
  "map of operator keywords to functions:
  property value, predicate value -> boolean
  pred-value might be a collection in conjunction with operator 'in'."
  [op]
  (get
   {:not=      (wrapper not=)
    :=         (wrapper =)
    :empty     empty-val?
    :non-empty (complement empty-val?)
    :<         (safe <)
    :<=        (safe <=)
    :>         (safe >)
    :>=        (safe >=)
    :includes  includes?
    :excludes  (complement includes?)
    :in        in
    :not-in    (complement in)
    :has       has
    :has-not   (complement has)
    :in-geo-rect in-geo-rect
    :not-in-geo-rect (complement in-geo-rect)}
   op))

(defn- standard-op-annotations
  [instance op]
  (let [op-fn (standard-op op)]
    (fn [data-val pred-val extra]
      (op-fn (ff/get instance data-val "content")
             pred-val
             extra))))

(defn- number-op
  "only contains to special cases
   := and :not=.
   Reasoning behind this is when the filter is for example = 1
   and the data-value is 1.0 then the filter will return false.
   With the == and the complement this will also work."
  [op]
  (get {:= (safe ==)
        :not= (complement (safe ==))}
       op
       (standard-op op)))

(defn- date-op [instance op]
  (get
   {:=         (partial dates/equal? instance)
    :not=      (complement (partial dates/equal? instance))
    :empty     empty-val?
    :non-empty (complement empty-val?)
    :<         (partial dates/before? instance)
    :<=        (complement (partial dates/after? instance))
    :>         (partial dates/after? instance)
    :>=        (complement (partial dates/before? instance))
    :current-day (partial dates/day-equal? instance)
    :current-month (partial dates/month-equal? instance)
    :current-year (partial dates/year-equal? instance)
    :last-x-days (partial dates/last-x-days instance)
    :last-x-months (partial dates/last-x-months instance)
    :last-x-years (partial dates/last-x-years instance)}
   op))

(def ^:private get-conj
  {:and (partial every? true?)
   :or  (partial some true?)})

(defn all-filter-maps
  "Returns all filter-maps with they respective path."
  [acc f-path f]
  (if (map? f)
    (conj acc [f-path [f]])
    (mapcat #(all-filter-maps
              acc
              (conj f-path (first f))
              %)
            (rest f))))

;------------------------------------------------------------------
; Functions to help with compiling a single function from a filter definition

(def filter-leaf? map?)

(defn- filter-op?
  "Returns true iff _node_ is a filter operation,
  which is [:and|:or fn_1 fn_2 ... fn_n]"
  ;; Beware: Intermediate nodes in clojure.walk are also vectors, so testing for vector is not enough here.
  [node]
  (and (sequential? node)
       (#{:and :or} (first node))))

(defn- filter-func [instance {prop ::prop op ::op val ::value extra ::extra-val}]
  (let [date-filter? (= prop ::dates/full-date)
        number-filter? (number? val)
        annotation? (= "annotation" prop)
        op-func (cond
                  date-filter? (date-op instance op)
                  number-filter? (number-op op)
                  annotation? (standard-op-annotations instance op)
                  :else (standard-op op))
        pred-val (if (and val date-filter?)
                   (dates/parse val)
                   val)
        coll-fn (if (#{:= :in :has :non-empty} op)
                  ff/some
                  ff/every?)]
    (fn [data]
      (let [data-val (ff/get instance data prop)]
        (boolean
         (if (or (not (ff/coll? instance data-val))
                 date-filter?
                 annotation?)
           (op-func data-val pred-val extra)
           (coll-fn instance
                    #(op-func % pred-val extra)
                    data-val)))))))

(defn- merge-operation-function
  "Merges one (trivial) or more functions in one and/or operation."
  ([op function]
   function)
  ([op function & more-functions]
   (let [op-fn (comp boolean ({:or  some
                               :and every?} op))]
     (fn [data]
       (op-fn #(% data) (into [function] more-functions))))))

(defn- compile* [instance filter-node]
  (cond (filter-leaf? filter-node) (filter-func instance filter-node)
        (filter-op? filter-node) (apply merge-operation-function filter-node)
        :default filter-node))

(defn- compile-filter
  "Create a single function executing the filter description, i.e. a predicate returning true or false when called on a
  data element."
  [instance filter-definition]
  (walk/postwalk (partial compile* instance) filter-definition))

;------------------------------------------------------------------

(defn group-by-data
  "Applies a filter to a collection of maps"
  [instance f ms]
  (as-> f $
    (compile-filter instance $)
    (ff/group-by instance $ ms)
    (ff/update instance $ true #(or % (ff/->ds instance [])))
    (ff/update instance $ false #(or % (ff/->ds instance [])))))

(defn filter-data
  "Applies a filter to a collection of maps"
  [instance f ms]
  (as-> f $
    (compile-filter instance $)
    (ff/filterv instance $ ms)))

(defn check-datapoint
  "Test if one single datapoint conforms to the given filter-definition.
   Returns true/false."
  [instance f datapoint]
  (as-> f $
    (compile-filter instance $)
    ($ datapoint)))

;; ==== predicate logic =============================
(def antagonist {:=      :not=
                 :not=   :=

                 :<=     :>
                 :>      :<=

                 :or     :and
                 :and    :or

                 :<      :>=
                 :>=     :<

                 :in     :not-in
                 :not-in :in})

(defn negate [f]
  (cond
    (pred? f) (update f ::op antagonist)
    :else (cons (antagonist (first f))
                (map negate (rest f)))))
