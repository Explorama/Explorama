(ns de.explorama.frontend.mosaic.data-access-layer
  (:refer-clojure :exclude [filter filterv merge map
                            mapv group-by get assoc
                            dissoc update coll? some every?
                            contains? count keys reduce
                            remove concat conj select-keys
                            map? first second get-in])
  (:require [clojure.core :as cc]))
            ;? [clojure.core.async :refer [chan go put! take! close!]]
            ;? [re-frame.core :as re-frame]))

#_(defprotocol AsyncDataStrucureAbstraction
    (|->g| [impl data])
    (|g->| [impl data])
    (|gget| [impl data attr]
      [impl data attr default])
    (|gmapv| [impl fnc data])
    (|ggroup-by| [impl f data])
    (|gsort-by| [impl f data])
    (|| [x fn1]))

#_(defn async-helper [f]
    (let [c (chan)]
      (go (put! c f))
      c))

#_(def CoreAync
    (reify AsyncDataStrucureAbstraction
      (|->g| [impl data]
        (async-helper (gds/->g impl data)))
      (|g->| [impl data]
        (async-helper (gds/g-> impl data)))
      (|gmapv| [impl fnc data]
        (async-helper (gds/gmapv impl fnc data)))
      (|ggroup-by| [impl f data]
        (async-helper (gds/ggroup-by impl f data)))
      (|gsort-by| [impl f data]
        (async-helper (gds/gsort-by impl f data)))
      (|| [x fn1]
        (take! x (fn [d]
                   (fn1 d)
                   (close! x))))))

#_(re-frame/reg-fx
   ::async
   (fn [{d :dispatch f :fn}]
     (|| CoreAync (f) (fn [data]
                        (re-frame/dispatch (conj d data))))))

#_(re-frame/reg-fx
   ::async-n
   (fn [values]
     (doseq [{d :dispatch f :fn} values]
       (|| CoreAync (f) (fn [data]
                          (re-frame/dispatch (conj d data)))))))

(declare ->g)                     ;? [c]
(declare g->)                     ;? [c]
(declare g->keywords)             ;? [c]
(declare g?)                      ;? [v]
(declare vec?)                    ;? [v]
(declare map?)                    ;? [c]
(declare copy)                    ;? [c]
(declare coll?)                   ;? [c]
(declare count)                   ;? [c]
(declare keys)                    ;? [c]
(declare first)                   ;? [c]
(declare second)                  ;? [c]
(declare get)                     ;? [c k] [c k default]
(declare get-in)                  ;? [c ks] [c ks default]
(declare mapv)                    ;? [f c]
(declare filter)                  ;? [f c]
(declare filter-index)            ;? [f c]
(declare remove)                  ;? [f c]
(declare concat)                  ;? [c1 c2]
(declare merge)                   ;? [c1 c2]
(declare conj)                    ;? [c v]
(declare update)                  ;? [c k f]
(declare assoc)                   ;? [c k v]
(declare dissoc)                  ;? [c ks]
(declare join-strings)            ;? [s c]
(declare select-keys)             ;? [c ks]
(declare some)                    ;? [f c]
(declare every?)                  ;? [f c]
(declare reduce)                  ;? [f init c]
(declare group-by)                ;? [f c]
(declare group-by-expand-vectors) ;? [f c]
(declare union)                   ;? [c1 c2]
(declare intersection)            ;? [c1 c2]
(declare union-vec)               ;? [c1 c2]
(declare intersection-vec)        ;? [c1 c2]
(declare difference-vec)          ;? [c1 c2]
(declare symmetric-difference-vec);? [c1 c2]
(declare sort-by-asc)             ;? [f c]
(declare sort-by-dsc)             ;? [f c]
(declare contains?)               ;? [c k]

(defn ->g [c]
  (clj->js c))
(defn g-> [c]
  (js->clj c))
(defn g->keywords [c]
  (js->clj c :keywordize-keys true))
(defn g? [c]
  (.isArray js/Array c))
(defn vec? [c]
  (.isArray js/Array c))
(defn map? [c]
  (object? c))
(defn copy [c]
  (if (.isArray js/Array c)
    (.slice c 0)
    (.assign js/Object #js {} c)))
(defn coll? [c]
  (or (.isArray js/Array c)
      (object? c)))
(defn count [c]
  (assert (.isArray js/Array c))
  (.-length c))
(defn keys [c]
  (js-keys c))
(defn first [c]
  (aget c 0))
(defn second [c]
  (aget c 1))
(defn get
  ([c v]
   (when c
     (aget c v)))
  ([c v default]
   (when c
     (or (aget c v) default))))
(defn get-in
  ([m ks]
   (when m
     (loop [ks ks
            m m]
       (if (empty? ks)
         m
         (recur (rest ks)
                (aget m (cc/first ks)))))))
  ([m ks default]
   (or (get-in m ks)
       default)))
(defn mapv [f c]
  (assert (.isArray js/Array c))
  (.map (.slice c 0) f))
(defn filter [f c]
  (assert (.isArray js/Array c))
  (let [r #js []
        ct (.-length c)]
    (loop [i 0]
      (when (< i ct)
        (when (f (aget c i))
          (.push r (aget c i)))
        (recur (inc i))))
    r))
(defn filter-index [f c]
  (assert (.isArray js/Array c))
  (let [r #js []
        ct (.-length c)]
    (loop [i 0]
      (when (< i ct)
        (when (f (aget c i))
          (.push r i))
        (recur (inc i))))
    r))
(defn remove [f c]
  (assert (.isArray js/Array c))
  (let [r (.slice c 0)]
    (loop [i 0]
      (when (< i (.-length r))
        (if (f (aget r i))
          (do (.splice r i 1)
              (recur i))
          (recur (inc i)))))
    r))
(defn concat [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (.concat c1
           c2))
(defn merge [c1 c2]
  (let [c1 (if (map? c1) (clj->js c1) c1)
        c2 (if (map? c2) (clj->js c2) c2)
        ks (js-keys c2)
        ct (.-length ks)
        r (.assign js/Object #js {} c1)]
    (loop [i 0]
      (when (< i ct)
        (let [v (aget ks i)]
          (aset r v (aget c2 v)))
        (recur (inc i))))
    r))
(defn conj [c v]
  (assert (.isArray js/Array c))
  (let [r (.slice c 0)]
    (.push r v)
    r))
(defn update [c k f]
  (if (.isArray js/Array c)
    (let [r (.slice c 0)
          nv (f (aget r
                      k))]
      (.splice r k 1 nv)
      r)
    (let [r (.assign js/Object #js {} c)
          nv (f (aget r
                      k))]
      (aset r k nv)
      r)))
(defn assoc [c k v]
  (if (or (.isArray js/Array c)
          (vector? c))
    (let [c (if (vector? c) (clj->js c) c)
          r (.slice c 0)]
      (.splice r k 0 v)
      r)
    (let [k (if (keyword? k) (name k) k)
          c (if (map? c) (clj->js c) c)
          r (.assign js/Object #js {} c)]
      (aset r k v)
      r)))
(defn dissoc [c ks]
  (let [ks (clj->js ks)
        ct (.-length ks)
        r (.assign js/Object c)]
    (loop [i 0]
      (when (< i ct)
        (js-delete r (aget ks i))
        (recur (inc i))))
    r))
(defn join-strings [s c]
  (assert (.isArray js/Array c))
  (let [ct (.-length c)
        r (aget c 0)]
    (loop [i 1
           r r]
      (if (< i ct)
        (recur (inc i)
               (if (aget c i)
                 (.concat r s (aget c i))
                 (.concat r s))) ;Add Seperator, same as cljs implementation (https://cljs.github.io/api/clojure.string/join)
        r))))
(defn select-keys [c ks]
  (let [ks (clj->js ks)
        r #js {}
        ct (.-length ks)]
    (loop [i 0]
      (when (< i ct)
        (let [prop-name (aget ks i)]
          (when (aget c prop-name)
            (aset r prop-name (aget c prop-name)))
          (recur (inc i)))))
    r))
(defn some [f c]
  (assert (.isArray js/Array c))
  (let [ct (.-length c)]
    (loop [i 0]
      (when (< i ct)
        (if-let [r (f (aget c i))]
          r
          (recur (inc i)))))))
(defn every? [f c]
  (assert (.isArray js/Array c))
  (let [ct (.-length c)]
    (loop [i 0]
      (if (< i ct)
        (if (f (aget c i))
          (recur (inc i))
          false)
        true))))
(defn reduce [f init c]
  (assert (.isArray js/Array c))
  (let [r init
        ct (.-length c)]
    (loop [i 0
           r r]
      (if (< i ct)
        (recur (inc i)
               (f r (aget c i)))
        r))))
(defn group-by [f c]
  (assert (.isArray js/Array c))
  (let [r #js {}
        ct (.-length c)]
    (loop [i 0]
      (when (< i ct)
        (let [x (aget c i)
              val (f x)]
          (if-let [d (aget r val)]
            (.push d x)
            (aset r val (js/Array. x)))
          (recur (inc i)))))
    r))
(defn group-by-expand-vectors [f c]
  (assert (.isArray js/Array c))
  (let [ct (.-length c)
        r #js {}]
    (loop [i 0]
      (when (< i ct)
        (let [x (aget c i)
              val (f x)]
          (if (.isArray js/Array val)
            (let [t #js {}
                  cv (.-length val)]
              (loop [ii 0]
                (when (< ii cv)
                  (let [valv (aget val ii)]
                    (aset t valv valv)
                    (recur (inc ii)))))
              (let [val (.values js/Object t)
                    cc (.-length val)]
                (loop [iii 0]
                  (let [valval (aget val iii)]
                    (when (< iii cc)
                      (if-let [d (aget r valval)]
                        (.push d x)
                        (aset r valval (js/Array. x)))
                      (recur (inc iii)))))))
            (if-let [d (aget r val)]
              (.push d x)
              (aset r val (js/Array. x))))
          (recur (inc i)))))
    r))
(defn union-vec [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (let [ct1 (.-length c1)
        ct2 (.-length c2)
        r #js {}]
    (loop [i 0]
      (when (< i ct1)
        (let [val (aget c1 i)]
          (aset r (aget val "id") val)
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)
              id (aget val "id")]
          (when-not (aget r id)
            (aset r id val))
          (recur (inc i)))))
    (.values js/Object r)))
(defn union-vec-start [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (let [ct1 (.-length c1)
        ct2 (.-length c2)
        r #js {}]
    (loop [i 0]
      (when (< i ct1)
        (let [val (aget c1 i)]
          (aset r (aget val "id") val)
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)
              id (aget val "id")]
          (when-not (aget r id)
            (aset r id val))
          (recur (inc i)))))
    r))
(defn union-vec-iteration [r c2]
  (assert (.isArray js/Array c2))
  (let [ct2 (.-length c2)]
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)
              id (aget val "id")]
          (when-not (aget r id)
            (aset r id val))
          (recur (inc i)))))
    r))
(defn union-vec-end [r]
  (.values js/Object r))
(defn intersection-vec [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (let [ct1 (.-length c1)
        ct2 (.-length c2)
        t #js {}
        r #js []]
    (loop [i 0]
      (when (< i ct1)
        (let [val (aget c1 i)]
          (aset t (aget val "id") i)
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)
              f? (aget t (aget val "id"))]
          (when f?
            (.push r val))
          (recur (inc i)))))
    r))
(defn difference-vec [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (let [ct1 (.-length c1)
        ct2 (.-length c2)
        t #js {}
        r #js []]
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)]
          (aset t (aget val "id") i)
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct1)
        (let [val (aget c1 i)
              f? (aget t (aget val "id"))]
          (when-not f?
            (.push r val))
          (recur (inc i)))))
    r))
(defn symmetric-difference-vec [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (let [ct1 (.-length c1)
        ct2 (.-length c2)
        t1 #js {}
        t2 #js {}
        r #js []]
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)]
          (aset t2 (aget val "id") i)
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct1)
        (let [val (aget c1 i)]
          (aset t1 (aget val "id") i)
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct1)
        (let [val (aget c1 i)
              f2? (aget t2 (aget val "id"))]
          (when-not f2?
            (.push r val))
          (recur (inc i)))))
    (loop [i 0]
      (when (< i ct2)
        (let [val (aget c2 i)
              f1? (aget t1 (aget val "id"))]
          (when-not f1?
            (.push r val))
          (recur (inc i)))))
    r))
(defn union [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (->> (union-vec c1 c2)
       g->
       set))
(defn intersection [c1 c2]
  (assert (.isArray js/Array c1))
  (assert (.isArray js/Array c2))
  (->> (intersection-vec c1 c2)
       g->
       set))

(defn- is-string-attribute? [f c]
  (let [ct (count c)]
    (loop [i 0]
      (cond (> i ct)
            false
            (-> (get c i)
                f
                string?)
            true
            :else
            (recur (inc i))))))

(defn handle-nil-compare [e1 e2]
  (cond
    (and (nil? e1)
         (nil? e2))
    0
    (nil? e1)
    (aget js/Number "MIN_SAFE_INTEGER")
    (nil? e2)
    (aget js/Number "MAX_SAFE_INTEGER")))

(defn- handle-str-compare [e1 e2]
  (let [nil-check (handle-nil-compare e1 e2)]
    (cond
      nil-check nil-check
      (and (.isArray js/Array e2)
           (.isArray js/Array e1))
      (.localeCompare (aget e1 0) (aget e2 0))
      (.isArray js/Array e2)
      (.localeCompare e1 (aget e2 0))
      (.isArray js/Array e1)
      (.localeCompare (aget e1 0) e2)
      :else
      (.localeCompare e1 e2))))

(defn sort-by-asc [f c]
  (assert (.isArray js/Array c))
  (if (is-string-attribute? f c)
    (.sort (.slice c 0) (fn [i1 i2]
                          (handle-str-compare (f i1) (f i2))))
    (.sort (.slice c 0) (fn [i1 i2]
                          (let [e1 (f i1)
                                e2 (f i2)
                                nil-check (handle-nil-compare e1 e2)]
                            (if nil-check
                              nil-check
                              (- e1 e2)))))))

(defn sort-by-dsc [f c]
  (assert (.isArray js/Array c))
  (if (is-string-attribute? f c)
    (.sort (.slice c 0) (fn [i1 i2]
                          (handle-str-compare (f i2) (f i1))))
    (.sort (.slice c 0) (fn [i1 i2]
                          (let [e1 (f i2)
                                e2 (f i1)
                                nil-check (handle-nil-compare e1 e2)]
                            (if nil-check
                              nil-check
                              (- e1 e2)))))))
