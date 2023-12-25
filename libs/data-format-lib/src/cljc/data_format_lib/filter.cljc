(ns data-format-lib.filter
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

(def ^:private pred? (m/validator (filter-spec ::pred)))

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

(defn- number-op [op]
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

(def ^:private filter-leaf? map?)

(defn- filter-op?
  [node]
  (and (sequential? node)
       (#{:and :or} (first node))))

(defn- filter-func [instance {prop ::prop op ::op val ::value extra ::extra-val}]
  (let [date-filter? (= prop ::dates/full-date)
        number-filter? (number? val)
        op-func (cond
                  date-filter? (date-op instance op)
                  number-filter? (number-op op)
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
                 date-filter?)
           (op-func data-val pred-val extra)
           (coll-fn instance
                    #(op-func % pred-val extra)
                    data-val)))))))

(defn- merge-operation-function
  ([_op function]
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
  [instance filter-definition]
  (walk/postwalk (partial compile* instance) filter-definition))

(defn group-by-data
  [instance f ms]
  (as-> f $
    (compile-filter instance $)
    (ff/group-by instance $ ms)
    (ff/update instance $ true #(or % (ff/->ds instance [])))
    (ff/update instance $ false #(or % (ff/->ds instance [])))))

(defn filter-data
  [instance f ms]
  (as-> f $
    (compile-filter instance $)
    (ff/filterv instance $ ms)))

(defn check-datapoint
  [instance f datapoint]
  (as-> f $
    (compile-filter instance $)
    ($ datapoint)))

(def ^:private antagonist {:=      :not=
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
