(ns data-format-lib.core
  (:require [data-format-lib.date-filter :as f]
            [data-format-lib.operations :as op]
            [data-format-lib.data-instance :as di]))

(def group-by-data f/group-by-data-api)

(def filter-data f/filter-data-api)
(def check-datapoint f/check-datapoint)

(def transform op/transform)
