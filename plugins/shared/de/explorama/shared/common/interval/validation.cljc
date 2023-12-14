(ns de.explorama.shared.common.interval.validation)

(defn interval-overlaps [idx intervals]
  (let [other-intervals (vec (keep-indexed (fn [i v] (when (not= i idx) v)) intervals))
        [from to] (get intervals idx [])
        from (or from ##-Inf)
        to (or to ##Inf)]
    (reduce (fn [acc checked-interval]
              (let
               [[from2 to2] checked-interval
                from2 (or from2 ##-Inf)
                to2 (or to2 ##Inf)
                start-overlap? (and (>= from from2) (< from to2))
                end-overlap? (and (> to from2) (< to to2))
                interval-contained? (<= from2 from to to2)
                contains-interval? (<= from from2 to2 to)]
                (cond
                  (and contains-interval? interval-contained?)
                  (conj acc [:duplicate checked-interval])
                  contains-interval?
                  (conj acc [:contains-interval checked-interval])
                  interval-contained?
                  (conj acc [:contained-in checked-interval])
                  start-overlap?
                  (conj acc [:start-overlaps-with checked-interval])
                  end-overlap?
                  (conj acc [:end-overlaps-with checked-interval])
                  :else acc)))
            []
            other-intervals)))

(defn- find-interval-overlaps [intervals]
  (map #(interval-overlaps % intervals) (take (count intervals) (range))))

(defn interval-overlaps? [intervals]
  (boolean (some not-empty (find-interval-overlaps intervals))))

(defn illegal-interval? [[lower-boundary upper-boundary]]
  (>= lower-boundary upper-boundary))

(defn check-for-gaps
  "It checks whether there any gaps between the interval. It expect well defined intervals, i.e. no overlaps and upper-boundary > lower-boundary has to true."
  [intervals]
  (let [sorted-intervals (into [] (sort-by first intervals))
        nr-intervals (count sorted-intervals)
        indices (range (dec nr-intervals))]
    (reduce (fn [acc idx]
              (let [[_ lower-to] (get sorted-intervals idx)
                    [higher-from _] (get sorted-intervals (inc idx))]
                (if (= lower-to higher-from)
                  acc
                  (conj acc [lower-to higher-from]))))
            [] indices)))