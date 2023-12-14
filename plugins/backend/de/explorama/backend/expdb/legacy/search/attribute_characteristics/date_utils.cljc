(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.date-utils
  (:require [de.explorama.shared.common.unification.time :as t]
            [clojure.string :as st]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.options-utils :as opts-utils]
            [taoensso.timbre :refer [error]]))

(def date-format "YYYY-MM-dd") ;Different formats for date-fns and clj-time
(def year-month-format "YYYY-MM")
(def year-format "YYYY")

(def date-format-placeholder (st/lower-case date-format))

(defn create-obj [y m d]
  (t/date-time y m d))

(def d-before? t/before?)

(def d-after? t/after?)

(def d-equal? t/equal?)

(def day-formatter (t/formatter date-format))
(def year-month-formatter (t/formatter year-month-format))
(def year-formatter (t/formatter year-format))

(defn choose-formatter [precision]
  (case precision
    (:day "day") day-formatter
    (:month "month") year-month-formatter
    (:year "year") year-formatter
    :else nil))

(defn obj->date-str
  ([obj]
   (obj->date-str :day obj))
  ([precision obj]
   (when obj
     (try
       (let [formatter (choose-formatter precision)]
         (t/unparse formatter obj))
       (catch #?(:clj Throwable :cljs :default) e
         (error "Date-Obj is not valid" (str (type obj)) obj precision e))))))

(defn date-str->obj
  "The Precision defines how accurate the date-string is, eg. year, month, day.
   Format of the date-string is the IS0-8601 definition. Example: 2018-01-31
   Returns a Date/Moment-object."
  ([date-string]
   (date-str->obj :day date-string))
  ([precision date-string]
   (when-let [date-string (opts-utils/normalize date-string)]
     (try
       (let [formatter (choose-formatter precision)]
         (t/parse formatter date-string))
       (catch #?(:clj Throwable :cljs :default) e
         (error "Date-str is not valid" date-string precision e))))))

(defn filter-date-ranges [start-date end-date possible-dates equal?]
  (let [start-date (date-str->obj :day start-date)
        end-date (date-str->obj :day end-date)
        check-fn (fn [d]
                   (= equal? (t/within? start-date end-date (date-str->obj d))))]
    (filter (fn [d]
              (= equal? (check-fn d)))
            possible-dates)))

(defn filter-months [month year-months]
  (reduce (fn [res ym-str]
            (if (= month (-> (date-str->obj :month ym-str)
                             (t/month)))
              (conj res ym-str)
              res))
          #{}
          year-months))

(defn is-same-day? [date1 date2]
  (try
    (= (obj->date-str date1)
       (obj->date-str date2)) ;!TODO There are corner cases where it might not work 
    (catch #?(:clj Throwable :cljs :default) e
      (error "Dates are not comparable" date1 date2 e))))

(defn initialize []
  nil ;Currently not needed, later maybe for langloc
  )

(defn get-month [date-obj]
  nil ;Currently not needed
  )

(defn get-year [date-obj]
  nil)

(defn get-month-year [date-obj]
  (str (get-year date-obj)
       (get-month date-obj)))
;Get the number of days in month - Currently only needed in client for performance optimizing
(defn get-days-in-month
  ([month year]
   nil)
  ([d]
   nil))
