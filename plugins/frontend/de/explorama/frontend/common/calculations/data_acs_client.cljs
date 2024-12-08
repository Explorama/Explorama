(ns de.explorama.frontend.common.calculations.data-acs-client
  (:require ["moment$default" :as moment]))

(defn date<- [date-str]
  (if (string? date-str)
    (moment date-str)
    date-str))

(defn post-process [data-acs]
  (if (empty? data-acs)
    data-acs
    (update-in data-acs ["date" :std :vals] #(mapv date<- %))))