(ns de.explorama.shared.search.conditions-utils
  (:require [clojure.string :as clj-str]
            [de.explorama.shared.search.options-utils :as opts]
            [de.explorama.shared.common.unification.time :as t]
            #?(:clj [taoensso.timbre :refer [error]]
               :cljs [taoensso.timbre :refer-macros [error]])))

(def general-ops-ordered ["=" "not="])
(def compare-ops-ordered ["<" "<=" ">=" ">"])
(def range-ops-ordered ["in range" "not in range"])
(def contains-ops-ordered ["includes" "exact term" "excludes"])
(def date-ops-ordered ["current" "last-x"])

(def general-ops (set general-ops-ordered))
(def compare-ops (set compare-ops-ordered))
(def range-ops (set range-ops-ordered))
(def contains-ops (set contains-ops-ordered))
(def date-ops (set date-ops-ordered))

(defn exact? [condition]
  (= (opts/normalize condition)
     "="))

(defn not=? [condition]
  (= (opts/normalize condition)
     "not="))

(defn in-range? [condition]
  (= (opts/normalize condition)
     "in range"))

(defn not-in-range? [condition]
  (= (opts/normalize condition)
     "not in range"))

(defn range-condition? [condi]
  (clj-str/includes? (or (opts/normalize condi :value true)
                         condi "")
                     "range"))

(defn current-condition? [condi]
  (= "current" (or (opts/normalize condi :value true)
                   condi "")))

(defn last-x? [condi]
  (= "last-x" (or (opts/normalize condi :value true)
                  condi "")))

(defn select-options [ops-identifier]
  (case ops-identifier
    :general (mapv opts/to-option general-ops)
    :compare (mapv opts/to-option compare-ops)
    :range (mapv opts/to-option range-ops)
    :contains (mapv opts/to-option contains-ops)
    :date (mapv opts/to-option date-ops)))

(def number-conditions-ops (vec (concat []
                                        (select-options :general)
                                        (select-options :compare)
                                        (select-options :range))))

(def text-conditions-ops (select-options :general))
(def contains-conditions-ops (select-options :contains))

(def day-year-conditions-ops (vec (concat number-conditions-ops
                                          (select-options :date))))
(def month-conditions-ops (vec (concat text-conditions-ops
                                       (select-options :date))))

; -------- Defaults ------
(def number-conditions-default (opts/to-option (first general-ops-ordered)))
(def range-number-conditions-default (opts/to-option (first general-ops-ordered)))
(def text-conditions-default (opts/to-option (first general-ops-ordered)))
(def contains-conditions-default (opts/to-option (first contains-ops-ordered)
                                                 (first contains-ops-ordered) true))

(defn condition-func [condition]
  (let [condition (if (keyword? condition)
                    (name condition)
                    condition)]
    (case condition
      "="  =
      "not=" not=
      "<" <
      "<=" <=
      ">" >
      ">=" >=
      "in range" (fn [v from to] (<= from v to))
      "not in range" (fn [v from to] (not (<= from v to)))
      (error "No Condition Function for" condition))))

(defn compare-dates [v1 v2 condition]
  (let [condition (if (keyword? condition)
                    (name condition)
                    condition)

        [y1 m1 d1] (mapv #(opts/to-number %) (clj-str/split v1 #"-"))
        [y2 m2 d2] (mapv #(opts/to-number %) (clj-str/split v2 #"-"))
        date1 (t/date-time y1 m1 d1)
        date2 (t/date-time y2 m2 d2)]
    (case condition
      ">" (t/before? date1 date2)
      ">=" (or
            (t/before? date1 date2)
            (t/equal? date1 date2))
      "<" (t/after? date1 date2)
      "<=" (or
            (t/after? date1 date2)
            (t/equal? date1 date2))
      "not=" (not (t/equal? date1 date2))
      false)))

(defn condfn
  ([condition v1 v2]
   (condfn condition v1 v2 nil))
  ([condition v1 v2 dates?]
   (condfn condition v1 v2 nil dates?))
  ([condition v1 v2 v2-fn dates?]
   (let [condition (opts/normalize condition)
         v2 (cond-> (opts/normalize v2)
              (and (compare-ops condition)
                   (not dates?))
              (opts/to-number)
              v2-fn (v2-fn))
         v1 (cond-> (opts/normalize v1)
              (or
               (number? v2)
               (and (compare-ops condition)
                    (not dates?)))
              (opts/to-number))]
     (if dates?
       (compare-dates v2 v1 condition)
       ((condition-func condition)
        v1
        v2)))))

(defn prepare-value-options [search-row cond-v options value min?]
  (let [v-fn (if min? first peek)
        lab (v-fn (filterv (fn [v] (condfn cond-v v value))
                           options))
        val (opts/to-number lab)]
    (assoc search-row :value (opts/to-option val lab))))

(defn prepare-range-options [search-row options from-s to-s]
  (let [from (peek (filterv (fn [v] (condfn "<" v from-s))
                            options))
        to (first (filterv (fn [v] (condfn ">" v to-s))
                           options))
        from (or from to)
        to (or to from)
        from (or from (opts/normalize from-s))
        to (or to (opts/normalize to-s))
        from-val (opts/to-number from)
        to-val (opts/to-number to)]
    (-> search-row
        (assoc :from (opts/to-option from-val from))
        (assoc :to (opts/to-option to-val to)))))

(defn prepare-date-options [search-row cond-v options value min?]
  (let [v-fn (if min? first peek)
        lab (v-fn (filterv (fn [v] (condfn cond-v v value nil true))
                           options))]
    (assoc search-row :selected-date (opts/to-option lab lab))))

(defn prepare-date-range-options [search-row options from-s to-s]
  (let [from (peek (filterv (fn [v] (condfn "<" v from-s nil true))
                            options))
        to (first (filterv (fn [v] (condfn ">" v to-s nil true))
                           options))
        from (or from to)
        to (or to from)
        from (or from (opts/normalize from-s))
        to (or to (opts/normalize to-s))
        from-val (opts/to-number from)
        to-val (opts/to-number to)]
    (-> search-row
        (assoc :start-date (opts/to-option from-val from))
        (assoc :end-date (opts/to-option to-val to)))))

(defn prepare-filter-row [search-row]
  (let [{:keys [from to options advanced value start-date end-date selected-date] cond-v :cond} search-row
        cond-v (opts/normalize cond-v)]
    (cond-> search-row
      (and advanced value (#{"<" ">"} cond-v) options)
      (prepare-value-options cond-v options value (= ">" cond-v))
      (and advanced selected-date (#{"<" ">"} cond-v) options)
      (prepare-date-options cond-v options selected-date (= ">" cond-v))
      (and advanced from to (not-in-range? cond-v) options)
      (prepare-range-options options from to)
      (and advanced start-date end-date (not-in-range? cond-v) options)
      (prepare-date-range-options options start-date end-date))))

