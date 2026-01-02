(ns de.explorama.frontend.common.calculations.data-acs-client
  (:require ["moment" :as momentModule]))

(defn date<- [date-str]
  (if (string? date-str)
    (momentModule date-str)
    date-str))

(defn post-process [data-acs]
  (if (empty? data-acs)
    data-acs
    (update-in data-acs ["date" :std :vals] #(mapv date<- %))))
