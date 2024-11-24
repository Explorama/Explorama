(ns de.explorama.frontend.woco.util.date)

(defn- pad [number]
  (if (< number 10)
    (str "0" number)
    number))

(defn timestamp->time-str
  ([timestamp]
   (when-let [d (js/Date. timestamp)]
     (let [hours (pad (.getHours d))
           minutes (pad (.getMinutes d))]
          ;;  seconds (pad (.getSeconds d))]
       (str hours ":"
            minutes))))
            ;; ":"))))
            ;; seconds))))
  ([]
   (timestamp->time-str (js/Date.now))))

(defn timestamp->date-str
  ([timestamp]
   (when-let [d (js/Date. timestamp)]
     (let [day (pad (.getDate d))
           month (pad (inc (.getMonth d)))
           year (.getFullYear d)]
       (str year "-"
            month "-"
            day))))
  ([]
   (timestamp->date-str (js/Date.now))))