(ns data-format-lib.data
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::keyword-coll
  (partial every?
           (partial map
                    (comp keyword first))))

(spec/def ::datapoint
  (spec/and map?
            ::keyword-coll))

(spec/def ::data
  (spec/* ::datapoint))
