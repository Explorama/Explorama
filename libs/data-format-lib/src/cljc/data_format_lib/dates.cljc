(ns data-format-lib.dates
  (:require #?(:clj [clj-time.core :as t]
               :cljs [cljs-time.core :as t])
            [clojure.string :as s]
            [data-format-lib.filter-functions :as ff]
            #?(:clj [clj-time.format :as f]
               :cljs [cljs-time.format :as f])))

(def date-keys [::type ::full-date
                ::week
                ::weekday
                ::year ::month ::day
                ::hours ::minutes ::seconds])

(defn- to-int [s]
  (try
    (cond
      (string? s)
      #?(:clj (Integer/parseInt s)
         :cljs (js/parseInt s))
      (number? s) (int s))
    (catch #? (:clj Exception
               :cljs js/Error)
           e nil)))

(defn safe-subs [^String s start & [end]]
  (when (and (string? s)
             (>= (count s) start)
             (or (nil? end)
                 (and (>= (count s) end)
                      (< start end))))
    (if end
      (subs s start end)
      (subs s start))))

(defn- year-month-day->date [year month day]
  (let [int-year (to-int year)
        int-month (to-int month)
        int-day (to-int day)]
    (t/date-time int-year int-month int-day)))

(defn transform-week
  ([^String date]
   (let [year (safe-subs date 0 4)
         month (safe-subs date 5 7)
         day (safe-subs date 8 10)]
     (transform-week year month day)))
  ([year month day]
   (when (and year month day)
     (-> (year-month-day->date year month day)
         t/week-number-of-year
         str))))

(defn transform-weekday
  ([^String date]
   (let [year (safe-subs date 0 4)
         month (safe-subs date 5 7)
         day (safe-subs date 8 10)]
     (transform-weekday year month day)))
  ([year month day]
   (when (and year month day)
     (-> (year-month-day->date year month day)
         t/day-of-week
         str))))

(defn parse
  "Parses a string of the format YYYY-MM-DDThh:mm:ss with each of the element being optional"
  [s]
  (let [dt (if (= s "today")
             (t/today-at-midnight)
             (let [year (safe-subs s 0 4)
                   month (safe-subs s 5 7)
                   day (safe-subs s 8 10)
                   hour (safe-subs s 11 13)
                   minute (safe-subs s 14 16)
                   sec (safe-subs s 17)
                   ordinals (take-while some? [year month day hour minute sec])]
               (apply t/date-time (map to-int ordinals))))]
    {::type ::date
     ::full-date {::val dt}
     ::year (t/year dt)
     ::month (t/month dt)
     ::week (t/week-number-of-year dt)
     ::weekday (t/day-of-week dt)
     ::day (t/day dt)
     ::hours (t/hour dt)
     ::minutes (t/minute dt)
     ::seconds (t/second dt)}))

(defn unparse
  "unparses a date object into :date-hour-minute-second format"
  [d]
  (f/unparse (f/formatters :date-hour-minute-second) (::full-date d)))

;; These are only used for full-date comparison
(defn equal? [instance d1 d2 & _]
  (t/equal? (ff/get instance d1 ::val)
            (-> d2
                ::full-date
                ::val)))

(defn before? [instance d1 d2 & _]
  (t/before? (ff/get instance d1 ::val)
             (-> d2
                 ::full-date
                 ::val)))

(defn after? [instance d1 d2 & _]
  (t/after? (ff/get instance d1 ::val)
            (-> d2
                ::full-date
                ::val)))

(defn year-equal? [instance d1 d2 & _]
  (= (t/year (ff/get instance d1 ::val))
     (t/year (-> d2
                 ::full-date
                 ::val))))

(defn month-equal? [instance d1 d2 & _]
  (and (year-equal? instance d1 d2)
       (= (t/month (ff/get instance d1 ::val))
          (t/month (-> d2
                       ::full-date
                       ::val)))))

(defn week-equal? [instance d1 d2 & _]
  (= (t/week-number-of-year (ff/get instance d1 ::val))
     (t/week-number-of-year (-> d2
                                ::full-date
                                ::val))))

(defn weekday-equal? [instance d1 d2 & _]
  (= (t/day-of-week (ff/get instance d1 ::val))
     (t/day-of-week (-> d2
                        ::full-date
                        ::val))))

(defn day-equal? [instance d1 d2 & _]
  (and (month-equal? instance d1 d2)
       (= (t/day (ff/get instance d1 ::val))
          (t/day (-> d2
                     ::full-date
                     ::val)))))

(defn last-x-days [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val)
        start (t/minus end (t/days extra))
        data-val (ff/get instance d1 ::val)]
    (t/within? start end data-val)))

(defn last-x-weeks [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val)
        start (t/minus end (t/days extra))
        data-val (ff/get instance d1 ::val)]
    (t/within? start end data-val)))

(defn last-x-months [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val
                t/last-day-of-the-month)
        start (-> end
                  (t/minus (t/months extra))
                  t/first-day-of-the-month)
        data-val (ff/get instance d1 ::val)]
    (t/within? start end data-val)))

(defn last-x-years [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val)
        start (t/minus end (t/years extra))
        data-val (t/year (ff/get instance d1 ::val))
        end-y (t/year end)
        start-y (t/year start)]
    (<= start-y data-val end-y)))