(ns de.explorama.shared.common.data.attributes
  (:require [clojure.walk :as walk]))

(def bucket-attr "bucket")

;; Ac related node names
(def datasource-node "Datasource")
(def feature-node "Feature")
(def context-node "Context")
(def date-node "Date")
(def fact-node "Fact")
(def notes-node "Notes")

;; All fixed data related attributes
(def country-attr "country")
(def date-attr "date")
(def year-attr "year")
(def datasource-attr "datasource")
(def location-attr "location")
(def notes-attr "notes")

(defn user-readable-name [this]
  (cond (keyword? this) (name this)
        :else this))

(defn access-key [this]
  (cond (keyword? this) (name this)
        :else this))

(defn value
  ([this attribute]
   (get this attribute))
  ([this attribute default]
   (get this attribute default)))

(defn ensure-structure [data-tiles]
  (walk/stringify-keys data-tiles))