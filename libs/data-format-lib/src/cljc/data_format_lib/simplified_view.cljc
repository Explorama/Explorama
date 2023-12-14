(ns data-format-lib.simplified-view
  (:require [data-format-lib.filter :as f]
            [malli.core :as m]))

(def simple-view-registry
  (assoc f/filter-registry
         ::op
         (into (::f/op f/filter-registry) [:in-range :not-in-range])
         ::simple-pred
         [:map
          [::op [:ref ::op]]
          [::f/prop [:ref ::f/prop]]
          [::f/value :any]]
         ::composite-pred
         [:cat [:enum :or] [:repeat [:or [:ref ::f/pred] [:ref ::f/preds]]]]
         ::view
         [:repeat [:or [:ref ::simple-pred] [:ref ::composite-pred]]]))

(defn- in-range-constraint [op args]
  (when (and (= :and op) (= 2 (count args)) (every? map? args))
    (let [[upper lower] (sort-by ::f/op args)]
      (when (and (= :<= (::f/op upper))
                 (= :>= (::f/op lower))
                 (= (::f/prop lower)
                    (::f/prop upper)))
        {::op      :in-range
         ::f/prop  (::f/prop lower)
         ::f/value [(::f/value lower)
                    (::f/value upper)]}))))

(defn- not-in-range-constraint [op args]
  (when (and (= :or op) (= 2 (count args)) (every? map? args))
    (let [[lower upper] (sort-by ::f/op args)]
      (when (and (= :< (::f/op lower))
                 (= :> (::f/op upper))
                 (= (::f/prop lower)
                    (::f/prop upper)))
        {::op      :not-in-range
         ::f/prop  (::f/prop lower)
         ::f/value [(::f/value lower)
                    (::f/value upper)]}))))

(defn- in-constraint [op args]
  (when (and (= :or op)
             (seq args)
             (every? map? args)
             (every? #(= := (::f/op %)) args)
             (apply = (map ::f/prop args)))
    {::op      :in
     ::f/prop  (::f/prop (first args))
     ::f/value (mapv ::f/value args)}))

(defn- primitive-constraint [op args]
  (some #(% op args)
        [in-constraint in-range-constraint not-in-range-constraint]))

(let [not-found-guard #?(:clj (Object.)
                         :cljs (js/Object.))]
  (defn- replace-key [m k0 k1]
    (let [v (get m k0 not-found-guard)]
      (if (= not-found-guard v)
        m
        (assoc (dissoc m k0) k1 v)))))

(def =>flatten-filter
  [:function {:registry simple-view-registry}
   [:=>
    [:cat [:or [:ref ::f/pred] [:ref ::f/preds]]]
    ::view]])

(defn flatten-filter
  "Flatten the data-format library filter `filter` into a vector of conjunctive
  constraints. In the process all :and operations, single argument :or
  operations and primitive constraints get flattened. Any other constraints are
  included unchanged. The filter is walked depth-first, so the order of the
  constrains corresponds to that of the written from.

  Also see: `primitive-constraint`."
  [filter]
  (loop [s [filter], flattened []]
    (if (seq s)
      (let [top (peek s), s' (pop s)]
        (if-let [[op & args] (when (sequential? top) top)]
          (if (and (= :or op) (= 1 (count args)))
            (recur (conj s' (first args)) flattened)
            (if-let [primitive (primitive-constraint op args)]
              (recur s' (conj flattened primitive))
              (if (= :and op)
                (recur (into s' (reverse args)) flattened)
                (recur s' (conj flattened top)))))
          (recur s' (conj flattened (replace-key top ::f/op ::op)))))
      flattened)))
(m/=> flatten-filter =>flatten-filter)


(defn- get-filter-ids [operations]
  (loop [ops operations, filter-ids []]
    (if (vector? ops)
      (let [[op filter-id di] ops]
        (if (= :filter op)
          (recur di (conj filter-ids filter-id))
          [:complex filter-ids ops]))
      [:simple filter-ids ops])))

(defn- get-di-filter [{:di/keys [operations filter]}]
  (let [[ops-type filter-ids] (get-filter-ids operations)]
    [ops-type (into [:and] (map filter) filter-ids)]))

(defn simplified-filter [di-filter local-filter]
  (let [[di-filter-type di-filter] (get-di-filter di-filter)
        {di-primitives true, di-complex false}
        (->> [di-filter]
             (into [:and] (comp (filter some?)))
             flatten-filter
             (group-by map?))

        {primitives true, complex false}
        (->> [local-filter]
             (into [:and] (comp (filter some?)))
             flatten-filter
             (group-by map?))]
    {:incomplete? (or (= :complex di-filter-type)
                      (some? complex)
                      (some? di-complex))
     :di-filter-primitives di-primitives
     :local-filter-primitives primitives}))
