(ns uk-stats19
  (:require [clojure.string :as str]))

(def collision-severity
  {"1" "Fatal"
   "2" "Serious"
   "3" "Slight"})

(def day-of-week
  {"1" "Sunday"
   "2" "Monday"
   "3" "Tuesday"
   "4" "Wednesday"
   "5" "Thursday"
   "6" "Friday"
   "7" "Saturday"})

(def light-conditions
  {"1" "Daylight"
   "4" "Darkness - lights lit"
   "5" "Darkness - lights unlit"
   "6" "Darkness - no lighting"
   "7" "Darkness - lighting unknown"
   "-1" "Data missing or out of range"})

(def weather-conditions
  {"1" "Fine no high winds"
   "2" "Raining no high winds"
   "3" "Snowing no high winds"
   "4" "Fine + high winds"
   "5" "Raining + high winds"
   "6" "Snowing + high winds"
   "7" "Fog or mist"
   "8" "Other"
   "9" "Unknown"
   "-1" "Data missing or out of range"})

(def road-surface-conditions
  {"1" "Dry"
   "2" "Wet or damp"
   "3" "Snow"
   "4" "Frost or ice"
   "5" "Flood over 3cm. deep"
   "6" "Oil or diesel"
   "7" "Mud"
   "9" "unknown (self reported)"
   "-1" "Data missing or out of range"})

(def road-type
  {"1" "Roundabout"
   "2" "One way street"
   "3" "Dual carriageway"
   "6" "Single carriageway"
   "7" "Slip road"
   "9" "Unknown"
   "12" "One way street/Slip road"
   "-1" "Data missing or out of range"})

(def urban-or-rural
  {"1" "Urban"
   "2" "Rural"
   "3" "Unallocated"
   "-1" "Data missing or out of range"})

(def first-road-class
  {"1" "Motorway"
   "2" "A(M)"
   "3" "A"
   "4" "B"
   "5" "C"
   "6" "Unclassified"
   "-1" "Data missing or out of range"})

(defn cell [row column]
  (str/trim (str (get row column ""))))

(defn label [table column]
  (fn [row]
    (get table (cell row column) "Unknown")))

(defn int-str [row column]
  (let [value (cell row column)
        negative? (str/starts-with? value "-")
        digits (str/replace value #"[^0-9]" "")
        digits (str/replace digits #"^0+(?=\d)" "")]
    (cond (= "" digits) "0"
          negative? (str "-" digits)
          :else digits)))

(defn dec-str [row column]
  (let [value (cell row column)]
    (if (str/includes? value ".")
      value
      (str value ".0"))))

(def desc
  {:meta-data {:file-format :csv
               :csv {:separator ","
                     :quote "\""
                     :encoding "UTF-8"
                     :limit 1000}}
   :mapping
   {:datasource {:name [:value "UK Road Safety Collisions"]
                 :global-id [:value "source-uk-stats19"]}
    :items
    [{:global-id [:field "collision_index"]
      :features
      [{:facts [{:name [:value "casualties"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "number_of_casualties"))]}
                {:name [:value "vehicles"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "number_of_vehicles"))]}
                {:name [:value "speed limit"]
                 :type [:value "integer"]
                 :unit [:value "mph"]
                 :value [:convert (fn [row] (int-str row "speed_limit"))]}]
        :locations [{:point [:convert (fn [row]
                                        [(dec-str row "latitude")
                                         (dec-str row "longitude")])]}]
        :contexts
        [{:name [:convert (label collision-severity "collision_severity")]
          :global-id [:id-generate ["severity" :text] :name]
          :type [:value "severity"]}
         {:name [:convert (label light-conditions "light_conditions")]
          :global-id [:id-generate ["light" :text] :name]
          :type [:value "light conditions"]}
         {:name [:convert (label weather-conditions "weather_conditions")]
          :global-id [:id-generate ["weather" :text] :name]
          :type [:value "weather"]}
         {:name [:convert (label road-surface-conditions "road_surface_conditions")]
          :global-id [:id-generate ["surface" :text] :name]
          :type [:value "road surface"]}
         {:name [:convert (label road-type "road_type")]
          :global-id [:id-generate ["road-type" :text] :name]
          :type [:value "road type"]}
         {:name [:convert (label first-road-class "first_road_class")]
          :global-id [:id-generate ["road-class" :text] :name]
          :type [:value "road class"]}
         {:name [:convert (label urban-or-rural "urban_or_rural_area")]
          :global-id [:id-generate ["area" :text] :name]
          :type [:value "area"]}
         {:name [:convert (label day-of-week "day_of_week")]
          :global-id [:id-generate ["weekday" :text] :name]
          :type [:value "weekday"]}
         {:name [:field "local_authority_ons_district"]
          :global-id [:id-generate ["ons-district" :text] :name]
          :type [:value "ONS district"]}]
        :dates [{:value [:date-schema "dd/MM/yyyy" [:field "date"]]
                 :type [:value "occured-at"]}]}]}]}})

desc
