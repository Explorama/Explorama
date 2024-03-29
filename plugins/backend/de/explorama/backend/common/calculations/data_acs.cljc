(ns de.explorama.backend.common.calculations.data-acs
  (:require [clojure.set :as set]
            [clojure.string :refer [blank?] :as string]
            [de.explorama.shared.common.unification.misc :refer [cljc-json->edn
                                                                 cljc-parse-int]]
            [de.explorama.shared.common.unification.time :as cljc-time]
            [taoensso.timbre :refer [debug]]
            [taoensso.tufte :as tufte]))

(def formatter (cljc-time/formatter "yyyy-MM-dd"))

(defn date<- [date-str]
  (if (string? date-str)
    (->> (string/split date-str #"T|-|:")
         (mapv #(cljc-parse-int %))
         (apply cljc-time/date-time))
    date-str))

(def blacklist #{:id :location :fulltext :bucket
                 "id" "location" "fulltext" "bucket"})

(def textfield-attributes #{:notes :annotation
                            "notes" "annotation"})

(defn handle-string [elem-v val]
  (tufte/p ::handle-string
           (if (set? val)
             (if (vector? elem-v)
               (set/union val
                          (set elem-v))
               (conj val elem-v))
             (if (vector? elem-v)
               (set elem-v)
               #{elem-v}))))

(defn date-min [& dates]
  (tufte/p ::date-min
           (cljc-time/earliest (mapv date<- dates))))

(defn date-max [& dates]
  (tufte/p ::date-max
           (cljc-time/latest (mapv date<- dates))))

(defn year-min [& dates]
  (tufte/p ::year-min
           (apply min (mapv #(if (number? %)
                               %
                               (cljc-parse-int (subs % 0 4)))
                            dates))))

(defn year-max [& dates]
  (tufte/p ::year-max
           (apply max (mapv #(if (number? %)
                               %
                               (cljc-parse-int (subs % 0 4)))
                            dates))))

(defn handle-min-max [[min max] elem-v val]
  (tufte/p ::handle-min-max
           (if (not elem-v)
             [(apply min val)
              (apply max val)]
             (if (vector? val)
               (if (vector? elem-v)
                 [(apply min (concat elem-v val))
                  (apply max (concat elem-v val))]
                 [(apply min (conj val elem-v))
                  (apply max (conj val elem-v))])
               (if (vector? elem-v)
                 [(apply min elem-v)
                  (apply max elem-v)]
                 [(apply min [elem-v])
                  (apply max [elem-v])])))))

(defn- double-vs-integer [value]
  (if (integer? value)
    :integer
    :decimal))

(defn- specific-number [prev-value value]
  (cond (and (nil? prev-value)
             (number? value))
        (double-vs-integer value)
        (= prev-value :decimal) :decimal
        (and (= prev-value :integer)
             (= :integer (double-vs-integer value)))
        :integer
        :else
        :decimal))

(defn- specific-type-number [elem-v cur-val]
  (tufte/p ::specific-type-number
           (cond (vector? elem-v)
                 (reduce specific-number nil (if cur-val
                                               (conj elem-v cur-val)
                                               elem-v))
                 (not cur-val)
                 (double-vs-integer elem-v)
                 :else
                 (specific-number cur-val elem-v))))

(defn conditions-handler [result key val]
  (tufte/p
   ::conditions-handler
   (cond
     (#{"date" :date} key) (conj result [key {:std {:func (partial handle-min-max
                                                                   [date-min date-max])
                                                    :type :date
                                                    :specific-type-fn (constantly :date)}
                                              :year {:func (partial handle-min-max
                                                                    [year-min year-max])
                                                     :type :year
                                                     :name :contraints-year-filter
                                                     :specific-type-fn (constantly :year)}}])
     (string? val) (conj result [key {:std {:func handle-string
                                            :type :string
                                            :specific-type-fn (constantly :string)}}])
     (number? val) (conj result [key {:std {:func (partial handle-min-max
                                                           [min max])
                                            :type :number
                                            :specific-type-fn specific-type-number}}])
     :else (conj result [key {:std {:func handle-string
                                    :type :string
                                    :specific-type-fn (constantly :string)}}]))))

(defn handler-map-builder [event]
  (tufte/p ::handler-map-builder
           (reduce (fn [result [key val]]
                     (if (vector? val)
                       (conditions-handler result key (first val))
                       (conditions-handler result key val)))
                   []
                   event)))

(defn update-func-specific-type [handler-map key fkey val cval]
  (tufte/p ::update-func
           ((get-in handler-map [key fkey :specific-type-fn]) val cval)))

(defn update-func [handler-map key fkey val cval]
  (tufte/p ::update-func
           ((get-in handler-map [key fkey :func]) val cval)))

(defn empty-val? [val]
  (if (string? val)
    (blank? val)
    (nil? val)))

(defn remove-empty
  "Removes any empty values from vectors and for empty single values returns nil"
  [val]
  (if (vector? val)
    (into [] (remove empty-val? val))
    (when-not (empty-val? val) val)))

(defn data-acs
  ([small-data key-list]
   (debug "start data-ac calculation")
   (tufte/p
    ::unzip
    (data-acs
     (tufte/p
      ::zipmap
      (mapv (fn [event]
              (zipmap key-list event))
            (if (string? small-data)
              (cljc-json->edn small-data)
              small-data))))))
  ([data-points]
   (tufte/p
    ::data-acs
    (let [event (apply merge-with
                       (fn [v1 v2]
                         (let [v1-vec? (vector? v1)
                               cleaned-v1 (when v1-vec? (remove nil? v1))
                               v2-vec? (vector? v2)
                               cleaned-v2 (when v2-vec? (remove nil? v2))]
                           (cond
                             (and v1-vec?
                                  (not-empty cleaned-v1)) (vec cleaned-v1)
                             (and (not v1-vec?)
                                  v1) v1
                             (and v2-vec?
                                  (not-empty cleaned-v2)) (vec cleaned-v2)
                             (and (not v2-vec?)
                                  v2) v2
                             v1 v1
                             v2 v2
                             :else nil)))
                       data-points)
          handler-vec (handler-map-builder (as-> event $
                                             (apply dissoc $ blacklist)))
          handler-map (into {} handler-vec)
          init-res-map (tufte/p
                        ::init-res-map
                        (reduce (fn [result [key val]]
                                  (reduce (fn [result [fkey val]]
                                            (assoc-in result [key fkey] (select-keys val [:type :name])))
                                          result
                                          val))
                                {}
                                handler-vec))
          all-vals-map (tufte/p
                        ::all-vals-map
                        (apply dissoc
                               (apply merge-with
                                      (fn [v1 v2]
                                        (let [v1-vec? (vector? v1)
                                              v2-vec? (vector? v2)]
                                          (cond
                                            (and v1-vec?
                                                 v2-vec?) (into v1 v2)
                                            (and v1-vec?
                                                 (not v2-vec?)) (conj v1 v2)
                                            (and (not v1-vec?)
                                                 v2-vec?) (conj v2 v1)
                                            (and v1 (nil? v2)) [v1]
                                            (and v2 (nil? v1)) [v2]
                                            (and v1 v2) (vec (set [v1 v2]))
                                            :else nil)))
                                      data-points)
                               blacklist))
          res-map (tufte/p
                   ::res-map
                   (reduce (fn [result [key val]]
                             (let [filtered-val (remove-empty val)]
                               (cond
                                 (textfield-attributes key)
                                 (assoc-in result [key :std :text-search?] true)

                                 filtered-val
                                 (reduce (fn [result [fkey _]]
                                           (-> result
                                               (assoc-in [key fkey :vals]
                                                         (update-func handler-map key fkey filtered-val nil))
                                               (assoc-in [key fkey :specific-type]
                                                         (update-func-specific-type handler-map key fkey filtered-val nil))))
                                         result
                                         (get result key))

                                 :else
                                 result)))
                           init-res-map
                           all-vals-map))
          empty-keys (remove (fn [k]
                               (or (get-in res-map [k :std :text-search?])
                                   (seq (get-in res-map [k :std :vals]))))
                             (keys res-map))
          res-map (update-in res-map
                             ["date" :std :vals]
                             #(mapv (partial cljc-time/unparse formatter) %))]
      (reduce dissoc res-map empty-keys)))))

(defn post-process [data-acs]
  (if (empty? data-acs)
    data-acs
    (update-in data-acs ["date" :std :vals] #(mapv date<- %))))
