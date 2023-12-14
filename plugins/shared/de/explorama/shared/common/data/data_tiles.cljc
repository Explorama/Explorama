(ns de.explorama.shared.common.data.data-tiles
  (:require [clojure.walk :as walk]))

(defn access-key [this]
  (cond (keyword? this) (name this)
        :else this))

(defn value
  ([this dimension]
   (get this dimension))
  ([this dimension default]
   (get this dimension default)))

(defn ensure-structure [data-tiles]
  (walk/stringify-keys data-tiles))