(ns data-format-lib.data-instance
  (:require [clojure.spec.alpha :as spec]
            [data-format-lib.filter :as lib-filter]
            [clojure.walk :as walk]
            [malli.core :as m]
            #?(:clj [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            #?(:cljs [goog.crypt.Sha256])
            #?(:cljs [goog.crypt]))
  #?(:clj (:import [java.security MessageDigest])))


(def data-instance
  [:map
   [:di/operations {:optional true} vector?]
   [:di/filter {:optional true} [:map-of
                                 :string
                                 [:vector any?]]]
   [:di/data-tile-ref [:map-of
                       :string
                       [:map
                        [:di/identifier :string]]]]
   [:di/external-ref
    {:optional true}
    [:map-of any? any?]]])

(def data-instance?
  (m/validator data-instance))

(defn data-tile-access-key [this]
  (cond (keyword? this) (name this)
        :else this))

(defn data-tile-value
  ([this dimension]
   (get this dimension))
  ([this dimension default]
   (get this dimension default)))

(def sha-256 #?(:cljs (goog.crypt.Sha256.)
                :clj (MessageDigest/getInstance "SHA-256")))
(defn ctn->sha256-id [ctn]
  (let [ctn (str ctn)]
    #?(:clj
       (do (doto sha-256
             (.reset)
             (.update (.getBytes ctn)))
           (apply str (map #(format "%02x" (bit-and % 0xff)) (.digest sha-256))))
       :cljs
       (do
         (doto sha-256
           (.reset)
           (.update (str ctn)))
         (goog.crypt/byteArrayToHex (.digest sha-256))))))

(defn add-filter [diid new-filter]
  (let [filter-id (ctn->sha256-id new-filter)]
    (-> (update diid :di/operations (fn [operations]
                                      [:filter filter-id operations]))
        (update :di/filter assoc filter-id new-filter))))
