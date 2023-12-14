(ns de.explorama.shared.common.date.utils
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

(defn parse-date [value]
  (mapv #(int (edn/read-string %)) (str/split value #"-0|-")))

(defn date-convert [value & [{granularity :granularity}]]
  (let [[year month day] (parse-date value)]
    (if granularity
      (case granularity
        :day [year month day]
        :year [year]
        :month [year month]
        :quarter [year
                  (cond (< month 4)
                        1
                        (< month 7)
                        2
                        (< month 10)
                        3
                        :else
                        4)])
      {:day [year month day]
       :year [year]
       :month [year month]
       :quarter [year
                 (cond (< month 4)
                       1
                       (< month 7)
                       2
                       (< month 10)
                       3
                       :else
                       4)]})))

(defn complete-date [num]
  (let [num (str num)]
    (if (< (count num) 2)
      (str "0" num)
      num)))

(defn year-granularity [[min-value] [max-value]]
  (map vector (range min-value (inc max-value))))

(defn quarter-granularity [[min-year min-quarter] [max-year max-quarter]]
  (for [year (range min-year (inc max-year))
        quarter (range 1 5)
        :when (and (not (and (= year min-year)
                             (< quarter min-quarter)))
                   (not (and (= year max-year)
                             (< max-quarter quarter))))]
    [year quarter]))

(defn month-granularity [[min-year min-month] [max-year max-month]]
  (for [year (range min-year (inc max-year))
        month (range 1 13)
        :when (and (not (and (= year min-year)
                             (< month min-month)))
                   (not (and (= year max-year)
                             (< max-month month))))]
    [year month]))

(defn months [month day tm td]
  (and (= tm month)
       (<= day td)))

(defn day-granularity [[min-year min-month min-day] [max-year max-month max-day]]
  (for [year (range min-year (inc max-year))
        month (range 1 13)
        day (range 1 32)
        :let [months (partial months month day)]
        :when (and  (not (and (= year min-year)
                              (< month min-month)))
                    (not (and (= year max-year)
                              (< max-month month)))
                    (not (and (= year min-year)
                              (= month min-month)
                              (< day min-day)))
                    (not (and (= year max-year)
                              (= max-month month)
                              (< max-day day)))
                    (or (and (not= (mod year 4) 0)
                             (= month 2)
                             (<= day 28))
                        (and (= (mod year 4) 0)
                             (= month 2)
                             (<= day 29))
                        (and (not= month 2)
                             (or (months 1 31)
                                 (months 3 31)
                                 (months 4 30)
                                 (months 5 31)
                                 (months 6 30)
                                 (months 7 31)
                                 (months 8 31)
                                 (months 9 30)
                                 (months 10 31)
                                 (months 11 30)
                                 (months 12 31)))))]
    [year month day]))

(defn date-range [min-value max-value {{:keys [granularity]} :date-config}]
  (case granularity
    :year (year-granularity min-value max-value)
    :quarter (quarter-granularity min-value max-value)
    :month (month-granularity min-value max-value)
    :day (day-granularity min-value max-value)))
