(ns de.explorama.shared.data-transformer.util
  (:require #?(:clj [clj-time.format :as f]
               :cljs [cljs-time.format :as f])))

(def string->char first)

(def formatter f/formatter)
(def unparse f/unparse)
(def parse f/parse)

(def date-format "YYYY-MM-dd")
