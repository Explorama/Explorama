(ns de.explorama.shared.data-transformer.util
  (:require [de.explorama.shared.common.unification.time :as time]))

(def string->char first)

(def formatter time/formatter)
(def parse time/parse)
(def unparse time/unparse)

(def date-format time/date-format)
