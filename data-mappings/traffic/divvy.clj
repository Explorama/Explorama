(ns divvy
  (:require [de.explorama.shared.common.unification.time :as time]
            [clojure.string :as str]))

(def timestamp (time/formatter "yyyy-MM-dd HH:mm:ss.SSS"))

(defn cell [row column]
  (str/trim (str (get row column ""))))

(defn day-of [column]
  (fn [row]
    (subs (cell row column) 0 10)))

(defn station [column]
  (fn [row]
    (let [value (cell row column)]
      (if (= "" value) "unknown station" value))))

(defn duration-minutes [row]
  (let [started (time/to-long (time/parse timestamp (cell row "started_at")))
        ended (time/to-long (time/parse timestamp (cell row "ended_at")))
        minutes (/ (- ended started) 60000.0)]
    (str (/ (double (int (+ 0.5 (* 100.0 minutes)))) 100.0))))

(def desc
  {:meta-data {:file-format :csv
               :csv {:separator ","
                     :quote "\""
                     :encoding "UTF-8"
                     :limit 1000}}
   :mapping
   {:datasource {:name [:value "Divvy Bikeshare Trips"]
                 :global-id [:value "source-divvy"]}
    :items
    [{:global-id [:field "ride_id"]
      :features
      [{:facts [{:name [:value "duration"]
                 :type [:value "decimal"]
                 :unit [:value "min"]
                 :value [:convert duration-minutes]}]
        :locations [{:point [:lat-lon [:field "start_lat"] [:field "start_lng"]]}]
        :contexts
        [{:name [:field "rideable_type"]
          :global-id [:id-generate ["rideable" :text] :name]
          :type [:value "rideable type"]}
         {:name [:field "member_casual"]
          :global-id [:id-generate ["membership" :text] :name]
          :type [:value "membership"]}
         {:name [:convert (station "start_station_name")]
          :global-id [:id-generate ["start-station" :text] :name]
          :type [:value "start station"]}
         {:name [:convert (station "end_station_name")]
          :global-id [:id-generate ["end-station" :text] :name]
          :type [:value "end station"]}]
        :dates [{:value [:convert (day-of "started_at")]
                 :type [:value "start-at"]}
                {:value [:convert (day-of "ended_at")]
                 :type [:value "end-at"]}]}]}]}})

desc
