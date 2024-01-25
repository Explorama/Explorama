(ns de.explorama.shared.common.unification.time
  (:require #?(:clj [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
            #?(:clj [clj-time.format :as f]
               :cljs [cljs-time.format :as f])
            #?(:clj [clj-time.coerce :as ctco]
               :cljs [cljs-time.coerce :as ctco])
            #?(:clj [taoensso.timbre :refer [error]]
               :cljs [taoensso.timbre :refer-macros [error]])
            [clojure.string :as st]))

(def date-format "YYYY-MM-dd")
(def year-month-format "YYYY-MM")
(def year-format "YYYY")

(def date-format-placeholder (st/lower-case date-format))

(def formatter f/formatter)
(def unparse f/unparse)
(def parse f/parse)
(def formatters f/formatters)

(def day-formatter (formatter date-format))
(def year-month-formatter (formatter year-month-format))
(def year-formatter (formatter year-format))

(defn choose-formatter [precision]
  (case precision
    (:day "day") day-formatter
    (:month "month") year-month-formatter
    (:year "year") year-formatter
    :else nil))

(def now t/now)
(def date-time t/date-time)

(def month t/month)
(def year t/year)

(def to-date ctco/to-date)
(def from-date ctco/from-date)
(def from-long ctco/from-long)

(def number-of-days-in-the-month t/number-of-days-in-the-month)

(defn date-protocol? [obj]
  (satisfies? t/DateTimeProtocol obj))

(defn- convert-and-apply
  "Checks if obj is from date-protocol type which is needed to apply functions from clj/cljs-time"
  ([f obj]
   (cond-> obj
     (not (date-protocol? obj))
     (from-date)
     (fn? f)
     (f)))

  ([f obj1 obj2]
   (when (fn? f)
     (f (convert-and-apply nil obj1)
        (convert-and-apply nil obj2)))))


(def before? (partial convert-and-apply t/before?))
(def after? (partial convert-and-apply t/after?))
(def equal? (partial convert-and-apply t/equal?))
(def within? (partial convert-and-apply t/within?))

(def earliest t/earliest)
(def latest t/latest)

(def to-long ctco/to-long)

(def current-ms #(to-long (now)))

(defn obj->date-str
  ([obj]
   (obj->date-str :day obj))
  ([precision obj]
   (when obj
     (try
       (let [obj (cond-> obj
                   (not (date-protocol? obj))
                   (ctco/from-date))
             formatter (choose-formatter precision)]
         (unparse formatter obj))
       (catch #?(:clj Throwable :cljs :default) e
         (error "Date-Obj is not valid" (str (type obj)) obj precision e))))))

(defn date-str->obj
  "The Precision defines how accurate the date-string is, eg. year, month, day.
   Format of the date-string is the IS0-8601 definition. Example: 2018-01-31
   Returns a Date/Moment-object."
  ([date-string]
   (date-str->obj :day date-string))
  ([precision date-string]
   (date-str->obj true precision date-string))
  ([native? precision date-string]
   (when-not (st/blank? date-string)
     (try
       (let [formatter (choose-formatter precision)]
         (cond-> (parse formatter date-string)
           native?
           (ctco/to-date)))
       (catch #?(:clj Throwable :cljs :default) e
         (error e "Date-str is not valid" date-string precision native?))))))

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

(defn get-month [date-obj]
  (month date-obj))

(defn get-year [date-obj]
  (year date-obj))

(defn get-month-year [date-obj]
  (str (get-year date-obj)
       (get-month date-obj)))

;Get the number of days in month - Currently only needed in client for performance optimizing
(defn get-days-in-month
  ([month year]
   (t/number-of-days-in-the-month (t/date-time year month)))
  ([d]
   (t/number-of-days-in-the-month d)))