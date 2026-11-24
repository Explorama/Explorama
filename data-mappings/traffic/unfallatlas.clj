(ns unfallatlas
  (:require [clojure.string :as str]))

(def federal-state
  {"01" "Schleswig-Holstein"
   "02" "Hamburg"
   "03" "Lower Saxony"
   "04" "Bremen"
   "05" "North Rhine-Westphalia"
   "06" "Hesse"
   "07" "Rhineland-Palatinate"
   "08" "Baden-Württemberg"
   "09" "Bavaria"
   "10" "Saarland"
   "11" "Berlin"
   "12" "Brandenburg"
   "13" "Mecklenburg-Western Pomerania"
   "14" "Saxony"
   "15" "Saxony-Anhalt"
   "16" "Thuringia"})

(def weekday
  {"1" "Sunday"
   "2" "Monday"
   "3" "Tuesday"
   "4" "Wednesday"
   "5" "Thursday"
   "6" "Friday"
   "7" "Saturday"})

(def accident-category
  {"1" "Accident with fatalities"
   "2" "Accident with serious injuries"
   "3" "Accident with slight injuries"})

(def accident-type
  {"0" "Other type of accident"
   "1" "Collision with a starting, stopping or parked vehicle"
   "2" "Collision with a vehicle ahead or waiting"
   "3" "Collision with a vehicle moving sideways in the same direction"
   "4" "Collision with an oncoming vehicle"
   "5" "Collision with a turning or crossing vehicle"
   "6" "Collision between vehicle and pedestrian"
   "7" "Impact with an obstacle on the carriageway"
   "8" "Leaving the carriageway to the right"
   "9" "Leaving the carriageway to the left"})

(def accident-kind
  {"1" "Driving accident"
   "2" "Turning-off accident"
   "3" "Turning-into or crossing accident"
   "4" "Pedestrian crossing accident"
   "5" "Accident involving stationary vehicles"
   "6" "Accident in longitudinal traffic"
   "7" "Other accident"})

(def light-conditions
  {"0" "Daylight"
   "1" "Twilight"
   "2" "Darkness"})

(def road-surface-condition
  {"0" "dry"
   "1" "wet, damp or slippery"
   "2" "icy or snow-covered"})

(def involvement
  [["IstRad" "Bicycle"]
   ["IstPKW" "Car"]
   ["IstFuss" "Pedestrian"]
   ["IstKrad" "Motorcycle"]
   ["IstGkfz" "Goods vehicle"]
   ["IstSonstige" "Other"]])

(defn cell [row column]
  (str/trim (str (get row column ""))))

(defn label [table column]
  (fn [row]
    (get table (cell row column) "unknown")))

(defn involved [row]
  (mapv second
        (filter (fn [[column _]] (= "1" (cell row column)))
                involvement)))

(defn int-str [row column]
  (let [digits (str/replace (cell row column) #"[^0-9]" "")
        digits (str/replace digits #"^0+(?=\d)" "")]
    (if (= "" digits) "0" digits)))

(defn dec-str [row column]
  (let [value (str/replace (cell row column) "," ".")]
    (if (str/includes? value ".")
      value
      (str value ".0"))))

;; The Unfallatlas deliberately carries no day of month, only year and month.
(defn accident-date [row]
  (let [month (cell row "UMONAT")
        month (if (= 1 (count month)) (str "0" month) month)]
    (str (cell row "UJAHR") "-" month "-01")))

(def desc
  {:meta-data {:file-format :csv
               :csv {:separator ";"
                     :quote "\""
                     :encoding "UTF-8"
                     :limit 1000}}
   :mapping
   {:datasource {:name [:value "Unfallatlas"]
                 :global-id [:value "source-unfallatlas"]}
    :items
    [{:global-id [:field "UIDENTSTLAE"]
      :features
      [{:facts [{:name [:value "hour of accident"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "USTUNDE"))]}]
        :locations [{:point [:convert (fn [row]
                                        [(dec-str row "YGCSWGS84")
                                         (dec-str row "XGCSWGS84")])]}]
        :contexts
        [{:name [:convert (label federal-state "ULAND")]
          :global-id [:id-generate ["federal-state" :text] :name]
          :type [:value "federal state"]}
         {:name [:convert (label accident-category "UKATEGORIE")]
          :global-id [:id-generate ["accident-category" :text] :name]
          :type [:value "accident category"]}
         {:name [:convert (label accident-type "UART")]
          :global-id [:id-generate ["accident-type" :text] :name]
          :type [:value "accident type"]}
         {:name [:convert (label accident-kind "UTYP1")]
          :global-id [:id-generate ["accident-kind" :text] :name]
          :type [:value "accident kind"]}
         {:name [:convert (label light-conditions "ULICHTVERH")]
          :global-id [:id-generate ["light" :text] :name]
          :type [:value "light conditions"]}
         {:name [:convert (label road-surface-condition "IstStrassenzustand")]
          :global-id [:id-generate ["surface" :text] :name]
          :type [:value "road surface condition"]}
         {:name [:convert (label weekday "UWOCHENTAG")]
          :global-id [:id-generate ["weekday" :text] :name]
          :type [:value "weekday"]}
         {:name [:convert involved]
          :global-id [:id-generate ["involvement" :text] :name]
          :type [:value "involvement"]}]
        :dates [{:value [:convert accident-date]
                 :type [:value "occured-at"]}]}]}]}})

desc
