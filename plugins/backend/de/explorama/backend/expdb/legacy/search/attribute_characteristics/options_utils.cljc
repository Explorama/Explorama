(ns de.explorama.backend.expdb.legacy.search.attribute-characteristics.options-utils
  (:require [de.explorama.shared.common.unification.misc :refer [cljc-parse-int]]))

(defn to-number [nu-str]
  (if (string? nu-str)
    (cljc-parse-int nu-str)
    nu-str))

(defn normalize
  ([option]
   (normalize option :value))
  ([option k]
   (normalize option k false))
  ([option k res-nil?]
   (get option k (when-not res-nil? option))))

(defn to-option
  ([opt-str]
   (to-option opt-str opt-str false))
  ([val lab]
   (to-option val lab false))
  ([val lab val-as-keyword?]
   {:value (if val-as-keyword?
             (keyword val)
             val)
    :label lab}))
