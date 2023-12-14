(ns de.explorama.backend.algorithms.data.range
  (:require  [clojure.edn :as edn]
             [clojure.string :as str]))

(defn date-convert [value {granularity :granularity}]
  (let [[year month day] (str/split value #"-0|-")
        day (int (edn/read-string day))
        month (int (edn/read-string month))
        year (int (edn/read-string year))]
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
                      4)])))

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

(defn numeric-range [min-value-data max-value-data config step-function]
  (let [[min max step] (step-function min-value-data max-value-data config)]
    (range min (+ max step) step)))

(defn training-data-step-function [min-value-data
                                   max-value-data
                                   {{{max-type :type max-value :value} :max
                                     {min-type :type min-value :value} :min
                                     method-continues-value :method
                                     step-continues-value :step
                                     :as continues-value}
                                    :continues-value}]
  (cond
    (and continues-value
         (= method-continues-value :range))
    (let [max (case max-type
                :value max-value
                :max max-value-data)
          min (case min-type
                :value min-value
                :min min-value-data)]
      [min max step-continues-value])
    :else
    [min-value-data max-value-data (or step-continues-value 1)]))

(defn future-data-step-function [min-value-data max-value-data
                                 {{step-future-value :step}
                                  :future-values}]
  [min-value-data max-value-data (or step-future-value 1)])

;Step-function only for numbers and length only for date FIXME
(defn custom-range [min-value max-value {:keys [type value] :as config} length step-function]
  (case type
    :numeric
    (vec (numeric-range (get min-value value) (get max-value value) config step-function))
    :date
    (if (zero? length)
      (date-range (get min-value value) (get max-value value) config)
      (vec (take length (rest (date-range (get min-value value) (get max-value value) config)))))
    :categoric
    []))