(ns de.explorama.backend.expdb.legacy.search.data-tile
  (:require [data-format-lib.dates :as dfl-dates]
            [data-format-lib.filter-functions :refer [default-impl]]
            [de.explorama.backend.expdb.config :as config-expdb]
            [de.explorama.backend.expdb.query.graph :as ngraph]
            [de.explorama.shared.common.unification.misc :refer [cljc-parse-int]]))
(defn- number-val [val]
  (cond
    (string? val) (cljc-parse-int val)
    (number? val) val
    :else nil))

(defn- dlf-date-workaround
  "Makes sure the val key is in the date-map."
  [date-map]
  (assoc date-map
         ::dfl-dates/val
         (-> date-map ::dfl-dates/full-date ::dfl-dates/val)))

(defn- last-x-fn [type last-x value]
  (let [ac-val-date (dlf-date-workaround (dfl-dates/parse value))
        target-date (dlf-date-workaround (dfl-dates/parse "today"))]
    (case type
      "year" (dfl-dates/last-x-years default-impl ac-val-date target-date last-x)
      "month" (dfl-dates/last-x-months default-impl ac-val-date target-date last-x)
      "day" (dfl-dates/last-x-days default-impl ac-val-date target-date last-x))))

(defn- current-fn [type value]
  (let [ac-val-date (dlf-date-workaround (dfl-dates/parse value))
        target-date (dlf-date-workaround (dfl-dates/parse "today"))]
    (case type
      "year" (dfl-dates/year-equal? default-impl ac-val-date target-date)
      "month" (dfl-dates/month-equal? default-impl ac-val-date target-date)
      "day" (dfl-dates/month-equal? default-impl ac-val-date target-date))))


(defn- filter-ac-by-formdata [formdata-row]
  (let [[[type label] {:keys [values advanced all-values? empty-values? last-x]
                       {value :value} :value
                       {from :value} :from
                       {to :value} :to
                       {cond-val :value} :cond}]
        formdata-row
        cond-func
        (case cond-val
          "<" <
          "<=" <=
          ">=" >=
          ">" >
          "=" =
          "last-x" (partial last-x-fn type last-x)
          "current" (partial current-fn type)
          nil)]
    (cond
      (and advanced all-values?)
      (constantly true)

      (and advanced empty-values?)
      (constantly false) ; this does not work

      (or (and from to (not advanced))
          ;thats the same but idk
          (and advanced
               (= cond-val "in range")
               from to))
      (fn [dt]
        (<= from (number-val (get dt type))
            to))

         ; not= []
      (and advanced (= cond-val "not=") values)
      (fn [dt]
        (not ((set (mapv str values))
              (get dt type))))

      (and advanced (= cond-val "not in range") from to)
      (fn [dt]
        (not (<= from (number-val (get dt type)) to)))

         ; not= x
      (and advanced (= cond-val "not=") value)
      (fn [dt]
        (not (= (str value)
                (str (get dt type)))))

      ; last-x/current date something like one of year/month/day
      (and advanced (or (and (= cond-val "last-x") last-x)
                        (= cond-val "current")))
      (fn [dt]
        (cond-func (get dt type)))

      (and advanced cond-func value)
      (fn [dt]
        (cond-func (number-val (get dt type))
                   value))

      ; = x
      value
      (fn [dt]
        (= value (get dt type)))

      :else
      (let [values (set values)]
        (fn [dt]
          (values (get dt type)))))))

(defn get-data-tiles-for-schema [res formdata {schema :schema
                                               dims :data-tile-keys}]
  (let [dims (set (keys dims))
        formdata-rows (filter (fn [[[dim]]]
                                (dims dim))
                              formdata)
        func-filters (mapv filter-ac-by-formdata
                           formdata-rows)]
    (reduce (fn [res dt]
              (if (every? (fn [func-filter]
                            (func-filter dt))
                          func-filters)
                (conj res dt)
                res))
            res
            (ngraph/dts-full schema))))

(defn get-data-tiles [formdata]
  (reduce (fn [res [_ config]]
            (get-data-tiles-for-schema res formdata config))
          []
          config-expdb/explorama-bucket-config))
