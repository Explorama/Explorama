(ns de.explorama.shared.common.unification.time
  (:require #?(:clj [taoensso.timbre :refer [error]]
               :cljs [taoensso.timbre :refer-macros [error]])
            #?@(:cljs [[goog.date :as gdate]])
            [clojure.string :as st])
  #?(:clj (:import [java.time Instant LocalDate LocalDateTime LocalTime YearMonth ZoneOffset]
                   [java.time.format DateTimeFormatter DateTimeFormatterBuilder]
                   [java.time.temporal ChronoField Temporal TemporalAdjusters WeekFields]
                   [java.util Date Locale])
     :cljs (:import [goog.date Interval UtcDateTime]
                    [goog.i18n DateTimeFormat DateTimeParse])))

(def date-format "yyyy-MM-dd")
(def year-month-format "yyyy-MM")
(def year-format "yyyy")

(def date-format-placeholder (st/lower-case date-format))

#?(:cljs
   (defrecord TimeFormatter [pattern format-obj parse-obj]))

#?(:clj
   (defn formatter [fmt-str]
     (-> (DateTimeFormatterBuilder.)
         (.parseCaseInsensitive)
         (.parseLenient)
         (.appendPattern fmt-str)
         (.parseDefaulting ChronoField/MONTH_OF_YEAR 1)
         (.parseDefaulting ChronoField/DAY_OF_MONTH 1)
         (.toFormatter Locale/ENGLISH)))
   :cljs
   (defn formatter [fmt-str]
     (->TimeFormatter fmt-str
                      (DateTimeFormat. fmt-str)
                      (DateTimeParse. fmt-str))))

#?(:clj (defn unparse [fmt obj]
          (.format ^DateTimeFormatter fmt obj))
   :cljs (defn unparse [fmt obj]
           (.format (:format-obj fmt) obj)))

#?(:clj (defn parse [fmt s]
          (let [ta (.parse ^DateTimeFormatter fmt ^String s)
                date (LocalDate/from ta)]
            (if (.isSupported ta ChronoField/HOUR_OF_DAY)
              (.atTime date (LocalTime/from ta))
              (.atStartOfDay date))))
   :cljs (defn parse [fmt s]
           (let [scratch (UtcDateTime. 1970 0 1 0 0 0 0)
                 consumed (.parse (:parse-obj fmt) s scratch)]
             (when-not (pos? consumed)
               (throw (ex-info "Unparseable date" {:value s :pattern (:pattern fmt)})))
             (UtcDateTime. (.getFullYear scratch) (.getMonth scratch) (.getDate scratch)
                           (.getHours scratch) (.getMinutes scratch) (.getSeconds scratch)
                           (.getMilliseconds scratch)))))

(def formatters
  {:date-hour-minute-second (formatter "yyyy-MM-dd'T'HH:mm:ss")
   :date-hour-minute-second-fraction (formatter "yyyy-MM-dd'T'HH:mm:ss.SSS")
   :year-month-day (formatter "yyyy-MM-dd")
   :basic-date-time-no-ms (formatter "yyyyMMdd'T'HHmmss'Z'")})

(def day-formatter (formatter date-format))
(def year-month-formatter (formatter year-month-format))
(def year-formatter (formatter year-format))

(defn choose-formatter [precision]
  (case precision
    (:day "day") day-formatter
    (:month "month") year-month-formatter
    (:year "year") year-formatter
    :else nil))

#?(:clj (defn now [] (LocalDateTime/now ZoneOffset/UTC))
   :cljs (defn now [] (UtcDateTime.)))

#?(:clj (defn date-time
          ([y] (LocalDateTime/of (int y) 1 1 0 0 0))
          ([y m] (LocalDateTime/of (int y) (int m) 1 0 0 0))
          ([y m d] (LocalDateTime/of (int y) (int m) (int d) 0 0 0))
          ([y m d h] (LocalDateTime/of (int y) (int m) (int d) (int h) 0 0))
          ([y m d h mi] (LocalDateTime/of (int y) (int m) (int d) (int h) (int mi) 0))
          ([y m d h mi s] (LocalDateTime/of (int y) (int m) (int d) (int h) (int mi) (int s))))
   :cljs (defn date-time
           ([y] (UtcDateTime. y 0 1 0 0 0 0))
           ([y m] (UtcDateTime. y (dec m) 1 0 0 0 0))
           ([y m d] (UtcDateTime. y (dec m) d 0 0 0 0))
           ([y m d h] (UtcDateTime. y (dec m) d h 0 0 0))
           ([y m d h mi] (UtcDateTime. y (dec m) d h mi 0 0))
           ([y m d h mi s] (UtcDateTime. y (dec m) d h mi s 0))))

#?(:clj (defn month [obj] (.getMonthValue ^LocalDateTime obj))
   :cljs (defn month [obj] (inc (.getMonth obj))))

#?(:clj (defn year [obj] (.getYear ^LocalDateTime obj))
   :cljs (defn year [obj] (.getFullYear obj)))

#?(:clj (defn get-day [obj] (.getDayOfMonth ^LocalDateTime obj))
   :cljs (defn get-day [obj] (.getDate obj)))

#?(:clj (defn get-hour [obj] (.getHour ^LocalDateTime obj))
   :cljs (defn get-hour [obj] (.getHours obj)))

#?(:clj (defn get-minute [obj] (.getMinute ^LocalDateTime obj))
   :cljs (defn get-minute [obj] (.getMinutes obj)))

#?(:clj (defn get-second [obj] (.getSecond ^LocalDateTime obj))
   :cljs (defn get-second [obj] (.getSeconds obj)))

#?(:clj (def ^:private iso-week-field (.weekOfWeekBasedYear WeekFields/ISO)))

#?(:clj (defn week-number-of-year [obj] (.get ^LocalDateTime obj iso-week-field))
   :cljs (defn week-number-of-year [obj]
           (gdate/getWeekNumber (.getFullYear obj) (.getMonth obj) (.getDate obj) 3 0)))

#?(:clj (defn day-of-week [obj] (.getValue (.getDayOfWeek ^LocalDateTime obj)))
   :cljs (defn day-of-week [obj] (inc (.getIsoWeekday obj))))

#?(:clj (defn minus-days [dt n] (.minusDays ^LocalDateTime dt (long n)))
   :cljs (defn minus-days [dt n] (doto (.clone dt) (.add (Interval. 0 0 (- n))))))

#?(:clj (defn minus-months [dt n] (.minusMonths ^LocalDateTime dt (long n)))
   :cljs (defn minus-months [dt n] (doto (.clone dt) (.add (Interval. 0 (- n) 0)))))

#?(:clj (defn minus-years [dt n] (.minusYears ^LocalDateTime dt (long n)))
   :cljs (defn minus-years [dt n] (doto (.clone dt) (.add (Interval. (- n) 0 0)))))

#?(:clj (defn first-day-of-the-month [dt] (.with ^LocalDateTime dt (TemporalAdjusters/firstDayOfMonth)))
   :cljs (defn first-day-of-the-month [dt] (doto (.clone dt) (.setDate 1))))

#?(:clj (defn last-day-of-the-month [dt] (.with ^LocalDateTime dt (TemporalAdjusters/lastDayOfMonth)))
   :cljs (defn last-day-of-the-month [dt]
           (let [c (.clone dt)]
             (.setDate c (gdate/getNumberOfDaysInMonth (.getFullYear c) (.getMonth c)))
             c)))

#?(:clj (defn today-at-midnight [] (.atStartOfDay (LocalDate/now ZoneOffset/UTC)))
   :cljs (defn today-at-midnight []
           (let [n (UtcDateTime.)]
             (UtcDateTime. (.getFullYear n) (.getMonth n) (.getDate n) 0 0 0 0))))

#?(:clj (defn to-date [obj]
          (Date/from (.toInstant ^LocalDateTime obj ZoneOffset/UTC)))
   :cljs (defn to-date [obj] (js/Date. (.getTime obj))))

#?(:clj (defn from-date [^Date d]
          (LocalDateTime/ofInstant (.toInstant d) ZoneOffset/UTC))
   :cljs (defn from-date [d] (UtcDateTime. d)))

#?(:clj (defn from-long [l]
          (LocalDateTime/ofInstant (Instant/ofEpochMilli (long l)) ZoneOffset/UTC))
   :cljs (defn from-long [l] (UtcDateTime. (js/Date. (long l)))))

#?(:clj (defn number-of-days-in-the-month [obj]
          (.lengthOfMonth (YearMonth/from ^LocalDateTime obj)))
   :cljs (defn number-of-days-in-the-month [obj]
           (gdate/getNumberOfDaysInMonth (.getFullYear obj) (.getMonth obj))))

(defn date-protocol? [obj]
  #?(:clj (instance? Temporal obj)
     :cljs (instance? gdate/Date obj)))

(defn- convert-and-apply
  "Converts obj to a platform date object when necessary before applying f"
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

#?(:clj (defn- before?* [a b] (.isBefore ^LocalDateTime a b))
   :cljs (defn- before?* [a b] (< (.getTime a) (.getTime b))))
#?(:clj (defn- after?* [a b] (.isAfter ^LocalDateTime a b))
   :cljs (defn- after?* [a b] (> (.getTime a) (.getTime b))))
#?(:clj (defn- equal?* [a b] (.isEqual ^LocalDateTime a b))
   :cljs (defn- equal?* [a b] (== (.getTime a) (.getTime b))))

(def before? (partial convert-and-apply before?*))
(def after? (partial convert-and-apply after?*))
(def equal? (partial convert-and-apply equal?*))

(defn within? [start end x]
  (let [start (convert-and-apply nil start)
        end (convert-and-apply nil end)
        x (convert-and-apply nil x)]
    (and (not (before?* x start))
         (before?* x end))))

(defn earliest
  ([dts] (reduce (fn [a b] (if (before?* b a) b a)) dts))
  ([dt1 dt2] (if (before?* dt2 dt1) dt2 dt1)))

(defn latest
  ([dts] (reduce (fn [a b] (if (after?* b a) b a)) dts))
  ([dt1 dt2] (if (after?* dt2 dt1) dt2 dt1)))

#?(:clj (defn to-long [obj]
          (cond
            (nil? obj) nil
            (number? obj) (long obj)
            (string? obj) (.toEpochMilli (.toInstant (parse day-formatter obj) ZoneOffset/UTC))
            (instance? Date obj) (.getTime ^Date obj)
            :else (.toEpochMilli (.toInstant ^LocalDateTime obj ZoneOffset/UTC))))
   :cljs (defn to-long [obj]
           (cond
             (nil? obj) nil
             (number? obj) (long obj)
             (string? obj) (.getTime (parse day-formatter obj))
             :else (.getTime obj))))

(def current-ms #(to-long (now)))

(defn obj->date-str
  ([obj]
   (obj->date-str :day obj))
  ([precision obj]
   (when obj
     (try
       (let [obj (cond-> obj
                   (not (date-protocol? obj))
                   (from-date))
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
           (to-date)))
       (catch #?(:clj Throwable :cljs :default) e
         (error e "Date-str is not valid" date-string precision native?))))))

(defn filter-date-ranges [start-date end-date possible-dates equal?]
  (let [start-date (date-str->obj false :day start-date)
        end-date (date-str->obj false :day end-date)
        check-fn (fn [d]
                   (= equal? (within? start-date end-date (date-str->obj false :day d))))]
    (filter (fn [d]
              (= equal? (check-fn d)))
            possible-dates)))

(defn filter-months [month-val year-months]
  (reduce (fn [res ym-str]
            (if (= month-val (-> (date-str->obj false :month ym-str)
                                 (month)))
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
   (number-of-days-in-the-month (date-time year month)))
  ([d]
   (number-of-days-in-the-month d)))
