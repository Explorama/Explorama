(ns de.explorama.shared.data-format.dates
  (:require [de.explorama.shared.common.unification.time :as time]
            [de.explorama.shared.data-format.filter-functions :as ff]))

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

(defn- date-time-defaulted [[y m d h mi sec]]
  (time/date-time (to-int y) (or (to-int m) 1) (or (to-int d) 1)
                   (or (to-int h) 0) (or (to-int mi) 0) (or (to-int sec) 0)))

(defn- year-month-day->date [year month day]
  (let [int-year (to-int year)
        int-month (to-int month)
        int-day (to-int day)]
    (time/date-time int-year (or int-month 1) (or int-day 1))))

(defn transform-week
  ([^String date]
   (let [year (safe-subs date 0 4)
         month (safe-subs date 5 7)
         day (safe-subs date 8 10)]
     (transform-week year month day)))
  ([year month day]
   (when (and year month day)
     (-> (year-month-day->date year month day)
         time/week-number-of-year
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
         time/day-of-week
         str))))

(defn parse
  "Parses a string of the format yyyy-MM-ddThh:mm:ss with each of the element being optional"
  [s]
  (let [dt (if (= s "today")
             (time/today-at-midnight)
             (let [year (safe-subs s 0 4)
                   month (safe-subs s 5 7)
                   day (safe-subs s 8 10)
                   hour (safe-subs s 11 13)
                   minute (safe-subs s 14 16)
                   sec (safe-subs s 17)
                   ordinals (take-while some? [year month day hour minute sec])]
               (date-time-defaulted ordinals)))]
    {::type ::date
     ::full-date {::val dt}
     ::year (time/get-year dt)
     ::month (time/get-month dt)
     ::week (time/week-number-of-year dt)
     ::weekday (time/day-of-week dt)
     ::day (time/get-day dt)
     ::hours (time/get-hour dt)
     ::minutes (time/get-minute dt)
     ::seconds (time/get-second dt)}))

(defn unparse
  "unparses a date object into :date-hour-minute-second format"
  [d]
  (time/unparse (time/formatters :date-hour-minute-second) (::full-date d)))

;; These are only used for full-date comparison
(defn equal? [instance d1 d2 & _]
  (time/equal? (ff/get instance d1 ::val)
               (-> d2
                   ::full-date
                   ::val)))

(defn before? [instance d1 d2 & _]
  (time/before? (ff/get instance d1 ::val)
                (-> d2
                    ::full-date
                    ::val)))

(defn after? [instance d1 d2 & _]
  (time/after? (ff/get instance d1 ::val)
               (-> d2
                   ::full-date
                   ::val)))

(defn year-equal? [instance d1 d2 & _]
  (= (time/get-year (ff/get instance d1 ::val))
     (time/get-year (-> d2
                        ::full-date
                        ::val))))

(defn month-equal? [instance d1 d2 & _]
  (and (year-equal? instance d1 d2)
       (= (time/get-month (ff/get instance d1 ::val))
          (time/get-month (-> d2
                              ::full-date
                              ::val)))))

(defn week-equal? [instance d1 d2 & _]
  (= (time/week-number-of-year (ff/get instance d1 ::val))
     (time/week-number-of-year (-> d2
                                   ::full-date
                                   ::val))))

(defn weekday-equal? [instance d1 d2 & _]
  (= (time/day-of-week (ff/get instance d1 ::val))
     (time/day-of-week (-> d2
                           ::full-date
                           ::val))))

(defn day-equal? [instance d1 d2 & _]
  (and (month-equal? instance d1 d2)
       (= (time/get-day (ff/get instance d1 ::val))
          (time/get-day (-> d2
                            ::full-date
                            ::val)))))

(defn last-x-days [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val)
        start (time/minus-days end extra)
        data-val (ff/get instance d1 ::val)]
    (time/within? start end data-val)))

(defn last-x-weeks [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val)
        start (time/minus-days end extra)
        data-val (ff/get instance d1 ::val)]
    (time/within? start end data-val)))

(defn last-x-months [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val
                time/last-day-of-the-month)
        start (-> end
                  (time/minus-months extra)
                  time/first-day-of-the-month)
        data-val (ff/get instance d1 ::val)]
    (time/within? start end data-val)))

(defn last-x-years [instance d1 d2 extra]
  (let [end (-> d2
                ::full-date
                ::val)
        start (time/minus-years end extra)
        data-val (time/get-year (ff/get instance d1 ::val))
        end-y (time/get-year end)
        start-y (time/get-year start)]
    (<= start-y data-val end-y)))
