(ns ntsb-aviation
  (:require [clojure.string :as str]))

(defn cell [row column]
  (str/trim (str (get row column ""))))

(defn code [column]
  (fn [row]
    (let [value (cell row column)]
      (if (= "" value) "UNK" value))))

(defn int-str [row column]
  (let [digits (str/replace (cell row column) #"[^0-9]" "")
        digits (str/replace digits #"^0+(?=\d)" "")]
    (if (= "" digits) "0" digits)))

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
   {:datasource {:name [:value "NTSB Aviation Accidents"]
                 :global-id [:value "source-ntsb-aviation"]}
    :items
    [{:global-id [:field "ev_id"]
      :features
      [{:facts [{:name [:value "fatalities"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "inj_tot_f"))]}
                {:name [:value "serious injuries"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "inj_tot_s"))]}
                {:name [:value "minor injuries"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "inj_tot_m"))]}
                {:name [:value "uninjured"]
                 :type [:value "integer"]
                 :value [:convert (fn [row] (int-str row "inj_tot_n"))]}]
        :locations [{:point [:convert (fn [row]
                                        [(dec-str row "dec_latitude")
                                         (dec-str row "dec_longitude")])]}]
        :contexts
        [{:name [:convert (code "ev_type")]
          :global-id [:id-generate ["event-type" :text] :name]
          :type [:value "event type"]}
         {:name [:convert (code "ev_highest_injury")]
          :global-id [:id-generate ["injury-level" :text] :name]
          :type [:value "highest injury level"]}
         {:name [:convert (code "ev_country")]
          :global-id [:id-generate ["country" :text] :name]
          :type [:value "country"]}
         {:name [:convert (code "ev_state")]
          :global-id [:id-generate ["state" :text] :name]
          :type [:value "state"]}
         {:name [:convert (code "ev_city")]
          :global-id [:id-generate ["city" :text] :name]
          :type [:value "city"]}
         {:name [:convert (code "light_cond")]
          :global-id [:id-generate ["light" :text] :name]
          :type [:value "light condition"]}
         {:name [:convert (code "wx_cond_basic")]
          :global-id [:id-generate ["weather" :text] :name]
          :type [:value "basic weather condition"]}
         {:name [:convert (code "invest_agy")]
          :global-id [:id-generate ["agency" :text] :name]
          :type [:value "investigating agency"]}]
        :dates [{:value [:date-schema "yyyy-MM-dd" [:field "ev_date"]]
                 :type [:value "occured-at"]}]
        :texts [[:field "apt_name" ""]]}]}]}})

desc
