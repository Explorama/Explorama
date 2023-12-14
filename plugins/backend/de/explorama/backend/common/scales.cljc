(ns de.explorama.backend.common.scales
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.date.utils :as dutil]
            [de.explorama.shared.common.unification.misc :refer [cljc-bigdec]]))

(def ^:private max-length 3000)

(defn date-axis [data-acs counter length-other-axis]
  (let [[start-day-obj end-day-obj] (get-in data-acs [:std :vals])
        start (dutil/parse-date start-day-obj)
        end (dutil/parse-date end-day-obj)
        months (dutil/month-granularity start end)
        month-num (count months)
        simple-case (inc (int (/ month-num 12)))
        coarse-days (* (/ month-num
                          (inc simple-case))
                       30)
        skip-days (max (int (/ (+ (if length-other-axis
                                    (* simple-case
                                       (/ coarse-days
                                          length-other-axis))
                                    1)
                                  (if counter
                                    (* simple-case (/ coarse-days
                                                      counter))
                                    1))
                               2))
                       (max (inc (int (/ (* month-num 30)
                                         max-length)))
                            1))
        all-days (map-indexed (fn [idx [year month day]]
                                [idx (str year
                                          "-"
                                          (dutil/complete-date month)
                                          "-"
                                          (dutil/complete-date day))])
                              (dutil/day-granularity start
                                                     end))
        scale (reduce (fn [acc [k v]]
                        (if (= 0 (mod k skip-days))
                          (assoc acc (int (/ k skip-days)) v)
                          acc))
                      {}
                      all-days)]
    {:scale scale
     :mapping-desc (reduce (fn [acc [k v]]
                             (assoc acc v (/ k skip-days)))
                           {}
                           all-days)
     :length (count scale)}))

(defn string-axis [data-acs]
  (let [scale
        (into {}
              (map-indexed vector
                           (sort (fn [a b]
                                   (compare
                                    (str/lower-case a)
                                    (str/lower-case b)))
                                 (get-in data-acs [:std :vals]))))]
    {:scale scale
     :mapping-desc (set/map-invert scale)
     :length (count scale)}))

(defn- below-zero-handling [in]
  (if (= "0" in)
    0
    (let [[i value]
          (loop [current in
                 i 0]
            (cond (= \0 (first current))
                  (recur (rest current)
                         (inc i))
                  (#{\9} (first current))
                  [(dec i) 1]
                  :else
                  [i
                   (get {\1 1
                         \2 2
                         \3 2.5
                         \4 5
                         \5 5
                         \6 5
                         \7 7.5
                         \8 7.5}
                        (first current)
                        0)]))]
      (if (= -1 i)
        1
        (* value (Math/pow 10 (- (inc i))))))))

(defn- above-zero-handling [in]
  (cond (= 1 (count in))
        (edn/read-string in)
        (= 2 (count in))
        (let [num (edn/read-string in)]
          (cond (< num 13)
                10
                (< num 17)
                15
                (< num 23)
                20
                (< num 35)
                25
                (< num 65)
                50
                (< num 90)
                75
                :else 100))
        (<= 3 (count in))
        (let [num (subs in 0 3)
              num (edn/read-string num)
              num (cond (< num 110)
                        100
                        (< num 130)
                        125
                        (< num 160)
                        150
                        (< num 190)
                        175
                        (< num 220)
                        200
                        (< num 300)
                        300
                        (< num 600)
                        500
                        (< num 900)
                        750
                        :else 1000)]
          (int (* num (Math/pow 10 (- (count in) 3)))))))

(defn- below-zero-handling-start [in sign]
  (let [[i value]
        (loop [current in
               i 0]
          (if (= i 2)
            0
            (cond (empty? current)
                  [0 0]
                  (= \0 (first current))
                  (recur (rest current)
                         (inc i))
                  (#{\9 \8} (first current))
                  (if (= - sign)
                    [(dec i) 1]
                    [i 7.5])
                  :else
                  [i
                   (if (= - sign)
                     (get {\1 1
                           \2 2.5
                           \3 5
                           \4 5
                           \5 5
                           \6 7.5
                           \7 7.5}
                          (first current))
                     (get {\1 0
                           \2 0
                           \3 2.5
                           \4 2.5
                           \5 5
                           \6 5
                           \7 5}
                          (first current)))])))]
    (if (= -1 i)
      1
      (* value (Math/pow 10 (- (inc i)))))))

(defn- above-zero-handling-start [in sign]
  (cond (= 1 (count in))
        (let [num (edn/read-string in)]
          (if (= sign -)
            (inc num)
            (min 0 (dec num))))
        (= 2 (count in))
        (let [num (edn/read-string in)]
          (if (= sign -)
            (cond (< num 25)
                  25
                  (< num 50)
                  50
                  (< num 75)
                  75
                  :else 100)
            (cond (< num 25)
                  0
                  (< num 50)
                  25
                  (< num 75)
                  50
                  :else 75)))
        (<= 3 (count in))
        (let [num (subs in 0 3)
              num (edn/read-string num)
              num (if (= sign -)
                    (cond (< num 250)
                          250
                          (< num 500)
                          500
                          (< num 750)
                          750
                          :else 1000)
                    (cond (< num 250)
                          0
                          (< num 500)
                          250
                          (< num 750)
                          500
                          :else 750))]
          (int (* num (Math/pow 10 (- (count in) 3)))))))

(defn- stringify-number [step]
  (str (cljc-bigdec step)))

(defn round-value [step]
  (if (= 0.0 (double step))
    0
    (let [step (double step)
          sign (if (< step 0) - +)
          step (Math/abs step)
          [above-zero below-zero] (str/split (stringify-number step) #"\.")
          step (cond (= above-zero "0")
                     (below-zero-handling below-zero)
                     (= (count above-zero) 1)
                     (let [above (above-zero-handling above-zero)]
                       (if (< above 3)
                         (+ above (below-zero-handling below-zero))
                         above))
                     :else
                     (above-zero-handling above-zero))]
      (sign step))))

(defn- round-value-integer [step]
  (if (= 0.0 (double step))
    0
    (let [step (double step)
          sign (if (< step 0) - +)
          step (Math/abs step)
          [above-zero] (str/split (str step) #"\.")
          step (if (= above-zero "0")
                 1
                 (above-zero-handling above-zero))]
      (sign step))))

(defn- start-value [start]
  (let [sign (if (< start 0) - +)
        start (Math/abs start)
        [above-zero below-zero] (str/split (str start) #"\.")
        step (cond (= above-zero "0")
                   (below-zero-handling-start below-zero sign)
                   (= (count above-zero) 1)
                   (let [above (above-zero-handling-start above-zero sign)]
                     (if (and (= + sign)
                              (< above 3))
                       (+ above (below-zero-handling-start below-zero sign))
                       above))
                   :else
                   (above-zero-handling-start above-zero sign))]
    (sign step)))

(defn- start-value-integer [start]
  (let [sign (if (< start 0) - +)
        start (Math/abs start)
        [above-zero] (str/split (str start) #"\.")
        step (if (= above-zero "0")
               0
               (above-zero-handling-start above-zero sign))]
    (sign step)))

(defn number-axis-decimal [data-acs axis-length x-axis?]
  (let [[start end] (get-in data-acs [:std :vals])
        current-range (- end start)
        step (/ current-range (min axis-length max-length))
        step (round-value step)]
    (if (= step 0)
      {:scale [[0 start]]
       :mapping-desc nil
       :length 1}
      (let [scale (loop [scale (lazy-seq)
                         value (start-value start)]
                    (if (< (+ end step) value)
                      (cond->> (conj scale value)
                        (and x-axis?
                             (< (last scale)
                                (first scale)))
                        reverse
                        :always (map-indexed vector)
                        :always vec)
                      (recur (conj scale value)
                             (+ value step))))]
        {:scale scale
         :mapping-desc nil
         :length (count scale)}))))

(defn number-axis-integer [data-acs axis-length x-axis?]
  (let [[start end] (get-in data-acs [:std :vals])
        current-range (- end start)
        step (/ current-range (min axis-length max-length))
        step (round-value-integer step)]
    (if (= step 0)
      {:scale [[0 start]]
       :mapping-desc nil
       :length 1}
      (let [scale (loop [scale (lazy-seq)
                         value (start-value-integer start)]
                    (if (<= end value)
                      (cond->> (conj scale value)
                        (and x-axis?
                             (< (last scale)
                                (first scale)))
                        reverse
                        :always (map-indexed vector)
                        :always vec)
                      (recur (conj scale value)
                             (+ value step))))]
        {:scale scale
         :mapping-desc nil
         :length (count scale)}))))

(defn- coord [axis-type {:keys [scale mapping-desc]} value card-dim]
  (case axis-type
    :string [(get mapping-desc value)
             (* card-dim 0.5)]
    :date (let [idx (get mapping-desc value)
                idx-int (int idx)
                idx-rest (- idx idx-int)]
            [idx-int
             (* (+ idx-rest 0.5)
                card-dim)])
    (:integer :decimal) (let [[highest-idx highest-value] (first scale)
                              [lowest-idx lowest-value] (peek scale)
                              distance (- highest-value lowest-value)
                              value-distance (- value lowest-value)
                              idx (if (= (count scale) 1)
                                    lowest-idx
                                    (* (/ value-distance distance)
                                       (dec (count scale))))
                              idx (if (< highest-idx lowest-idx)
                                    (- lowest-idx idx)
                                    idx)
                              idx-int (int idx)
                              idx-rest (- idx idx-int)]
                          [idx-int
                           (double (* (+ 0.5 idx-rest)
                                      card-dim))])))

(defn calculate-relative-positions [data x y
                                     x-axis-type y-axis-type
                                     {cpl :length :as x-mapping}
                                     y-mapping
                                     card-width card-height _]
  (reduce (fn [{:keys [counter mapping] :as acc} event]
            (let [x-value (attrs/value event x)
                  y-value (attrs/value event y)
                  {:keys [ignore-current mapping]}
                  (reduce (fn [acc event]
                            (let [x-value (attrs/value event x)
                                  y-value (attrs/value event y)]
                              (if (and x-value y-value)
                                (let [[x-coord
                                       x-relative]
                                      (coord x-axis-type
                                             x-mapping
                                             x-value
                                             card-width)
                                      [y-coord
                                       y-relative]
                                      (coord y-axis-type
                                             y-mapping
                                             y-value
                                             card-height)
                                      idx (+ (* cpl y-coord)
                                             x-coord)]
                                  (update-in acc
                                             [:mapping idx]
                                             (fnil conj [])
                                             [counter x-relative y-relative]))
                                (assoc acc :ignore-current true))))
                          {:ignore-current false
                           :mapping mapping}
                          (cond (and (vector? x-value)
                                     (vector? y-value))
                                (into
                                 (mapv #(assoc event x %) x-value)
                                 (mapv #(assoc event y %) y-value))
                                (vector? x-value)
                                (mapv #(assoc event x %) x-value)
                                (vector? y-value)
                                (mapv #(assoc event y %) y-value)
                                :else
                                [event]))]
              (if-not ignore-current
                {:mapping mapping
                 :counter (inc counter)}
                (update acc :counter inc))))
          {:counter 0
           :mapping {}}
          data))

(defn axis-length-adjust [_ window-width window-height _ _ x-axis-fn y-axis-fn]
  (let [count-x (int (* window-width 0.25))
        count-y (int (* window-height 0.25))]
    [(x-axis-fn count-x count-x)
     (y-axis-fn count-y count-y)]))

(defn attribute-counting [data x y]
  (reduce (fn [acc event]
            (let [x-value (attrs/value event x)
                  y-value (attrs/value event y)
                  {:keys [ignore-current] :as result}
                  (reduce (fn [acc event]
                            (let [x-value (attrs/value event x)
                                  y-value (attrs/value event y)]
                              (if-not (and x-value y-value)
                                (assoc acc :ignore-current true)
                                acc)))
                          (assoc acc :ignore-current false)
                          (cond (and (vector? x-value)
                                     (vector? y-value))
                                (into
                                 (mapv #(assoc event x %) x-value)
                                 (mapv #(assoc event y %) y-value))
                                (vector? x-value)
                                (mapv #(assoc event x %) x-value)
                                (vector? y-value)
                                (mapv #(assoc event y %) y-value)
                                :else
                                [event]))]
              (if ignore-current
                (->
                 (update acc :ignore inc)
                 (dissoc result :ignore-current))
                (update acc :counter inc))))
          {:counter 0
           :ignore 0}
          data))

(defn get-x-axis [x]
  (if (nil? x)
    "date"
    x))

(defn get-y-axis [data-acs y]
  (if (nil? y)
    (->> (map (fn [[name {{type :type} :std}]]
                [name type])
              data-acs)
         (filter (fn [[_ type]] (= type :number)))
         (sort-by (fn [[name]] name))
         ffirst)
    y))