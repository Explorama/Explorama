(ns de.explorama.shared.data-transformer.suggestions
  (:require [clojure.string :as str]))

(defn- check-types [_ value]
  (if (string? value)
    nil
    (cond (integer? value)
          {:type :integer}
          (double? value)
          {:type :decimal}
          (float? value)
          {:type :decimal}
          (number? value)
          {:type :decimal}
          :else {:raw-type (type value)})))

(def ^:private simple-integer-schema-en #"^[\-]{0,1}\d+$")
(def ^:private simple-decimal-schema-en #"^[\-]{0,1}[\d]+\.\d+$")
(def ^:private complex-decimal-schema-en #"^[\-]{0,1}([1-9]\d{0,2}(,?\d{3})*|0)(\.\d+)+$")
(def ^:private complex-integer-schema-en #"^[\-]{0,1}([1-9]\d{0,2}(,?\d{3})*|0)$")

(def ^:private simple-decimal-schema-de #"^[\-]{0,1}[\d]+\,\d+$")
(def ^:private complex-decimal-schema-de #"^[\-]{0,1}([1-9]\d{0,2}(.?\d{3})*|0)(\,\d+)+$")
(def ^:private complex-integer-schema-de #"^[\-]{0,1}([1-9]\d{0,2}(.?\d{3})*|0)$")

(defn- check-numbers [_ value]
  (cond (re-find simple-integer-schema-en value)
        {:type :integer}
        (re-find simple-decimal-schema-en value)
        {:type :decimal}
        (re-find complex-decimal-schema-en value)
        {:type :decimal}
        (re-find complex-integer-schema-en value)
        {:type :integer}
        (re-find simple-decimal-schema-de value)
        {:type :decimal}
        (re-find complex-decimal-schema-de value)
        {:type :decimal}
        (re-find complex-integer-schema-de value)
        {:type :integer}
        :else nil))

(def ^:private date-schema-1 #"^(\d{2})([\/\-\.])(\d{2})([\/\-\.])(\d{4})$")
(def ^:private date-schema-2 #"^(\d{2})([\/\-\.])(\d{2})([\/\-\.])(\d{2})$")
(def ^:private date-schema-3 #"^(\d{1,2})([\/\-\.])(\d{1,2})([\/\-\.])(\d{4})$")
(def ^:private date-schema-4 #"^(\d{1,2})([\/\-\.])(\d{1,2})([\/\-\.])(\d{2})$")

(def ^:private date-schema-5 #"^(\d{4})([\/\-\.])(\d{2})([\/\-\.])(\d{2})$")
(def ^:private date-schema-6 #"^(\d{2})([\/\-\.])(\d{2})([\/\-\.])(\d{2})$")
(def ^:private date-schema-7 #"^(\d{4})([\/\-\.])(\d{1,2})([\/\-\.])(\d{1,2})$")
(def ^:private date-schema-8 #"^(\d{2})([\/\-\.])(\d{1,2})([\/\-\.])(\d{1,2})$")

(def ^:private date-schema-9 #"^(\d{4})([\/\-\.])([A-Z]{3})([\/\-\.])(\d{2})$")
(def ^:private date-schema-10 #"^(\d{2})([\/\-\.])([A-Z]{3})([\/\-\.])(\d{4})$")

(def ^:private date-schema-11 #"^(\d{2})([\/]{1})([A-Z]{3})([\/]{1})(\d{4})$")
(def ^:private date-schema-12 #"^(\d{4})([\/]{1})([A-Z]{3})([\/]{1})(\d{2})$")

(def ^:private date-schema-13 #"^(\d{1,2})\ ([a-zA-Z]{3})\.\ (\d{2})$")
(def ^:private date-schema-14 #"^(\d{1,2})\ ([a-zA-Z]{3})\.\ (\d{4})$")

(def ^:private date-schema-15 #"^([a-zA-Z]{3})\.\ (\d{1,2})\,\ (\d{4})$")
(def ^:private date-schema-16 #"^([a-zA-Z]{3})\.\ (\d{1,2})\,\ (\d{2})$")

(defn- check-dates [_ value]
  ;TODO r1/mapping this could maybe faster if there is a regex/parser/lib checking all variants
  (loop [schemas [[date-schema-1 "dd.MM.YYYY" true]
                  [date-schema-2 "dd.MM.YY" true]
                  [date-schema-3 "dd.MM.YYYY" true]
                  [date-schema-4 "dd.MM.YY" true]
                  [date-schema-5 "YYYY.MM.dd" true]
                  [date-schema-6 "YY.MM.dd" true]
                  [date-schema-7 "YYYY.MM.dd" true]
                  [date-schema-8 "YY.MM.dd" true]
                  [date-schema-9 "YYYY.MMM.dd" true]
                  [date-schema-10 "YY.MMM.dd" true]
                  [date-schema-11 "dd/MMM/YYYY" false]
                  [date-schema-12 "dd/MMM/YY" false]
                  [date-schema-13 "dd MMM. YY" false]
                  [date-schema-14 "dd MMM. YYYY" false]
                  [date-schema-15 "MMM. dd, YYYY" false]
                  [date-schema-16 "MMM. dd, YY" false]]]
    (if (empty? schemas)
      nil
      (let [[schema field-schema replace-dot?] (first schemas)
            matcher (re-matches schema value)]
        (if (seq matcher)
          {:type :date
           :date-schema (if replace-dot?
                          (str/replace field-schema
                                       #"\."
                                       (str (get matcher 2)))
                          field-schema)}
          (recur (rest schemas)))))))

(def ^:private lon-schema #"(?i)(lng|lon|longitude)")
(def ^:private lat-schema #"(?i)(lat|latitude)")
(def ^:private pos-schema #"[0-9]{1,2}\.[0-9]+\,[\ ]{0,1}[0-9]{1,2}\.[0-9]+")
(def ^:private pos-field-schema #"[0-9]{1,2}\.[0-9]+")

(defn- check-locations [col-name value]
  (cond (and (re-find lat-schema col-name)
             (re-find pos-field-schema value))
        {:type :location
         :location :lat}
        (and (re-find lon-schema col-name)
             (re-find pos-field-schema value))
        {:type :location
         :location :lon}
        (re-find pos-schema value)
        {:type :location
         :location :position}
        :else
        nil))

(defn- check-ids [col-name _]
  (if (or (re-find #"(?i).*[_-]{1}(id).*" col-name)
          (re-find #"(?i)^(id).*" col-name))
    {:type :id}
    nil))

(defn- check-text [_ value]
  ;TODO r1/mapping what is a good number?
  (if (< 124 (count value))
    {:type :text}
    nil))

(defn- find-types [{{check-lines :check-lines} :suggestions}
                   content]
  (reduce (fn [acc row]
            (reduce-kv (fn [acc col-name value]
                         (update acc
                                 col-name
                                 (fnil conj [])
                                 (loop [funcs [check-types check-dates check-locations check-numbers check-text check-ids]]
                                   (if (empty? funcs)
                                     {:type :string}
                                     (if-let [result ((first funcs) col-name value)]
                                       result
                                       (recur (rest funcs)))))))
                       acc
                       (dissoc row :row-number "" " ")))
          {}
          (take (min (or check-lines 200)
                     (count content))
                content)))

(def ^:private type-priority
  {:id 0
   :location 1
   :date 2
   :number 3
   :text 4
   :string 5})

(defn- priotize-types [types]
  (reduce-kv (fn [acc col-name col-types]
               (let [col-types (set col-types)
                     priotize-type (reduce (fn [final-type type]
                                             (if (< (get type-priority (:type type) 0)
                                                    (get type-priority (:type final-type) 0))
                                               type
                                               final-type))
                                           {:type :string}
                                           col-types)]
                 (assoc acc col-name priotize-type)))
             {}
             types))

(defn- fact [[col-name {type :type}]]
  {:value [:field col-name]
   :name [:value (str/lower-case col-name)]
   :type [:value (case type
                   :integer "integer"
                   :decimal "decimal")]})

(defn- context [[col-name _]]
  {:name [:field col-name]
   :global-id [:id-generate [(str/lower-case col-name) :text] :name]
   :type [:value (str/lower-case col-name)]})

(defn- text [[col-name _]]
  [:field col-name ""])

(defn- location [locations]
  ;TODO r1/mapping handle multiple locations
  (cond (= (count locations) 1)
        (let [[col-name] (first locations)]
          [{:point [:position [:field col-name]]}])
        (= (count locations) 2)
        [{:point [:lat-lon
                  [:field (ffirst (filter (fn [[_ {:keys [location]}]]
                                            (= location :lat)) locations))]
                  [:field (ffirst (filter (fn [[_ {:keys [location]}]]
                                            (= location :lon)) locations))]]}]))

(defn- date [[col-name {date-schema :date-schema}]]
  {:value [:date-schema date-schema [:field col-name]]
   :type [:value "occured-at"]})

(defn- global-id [global-id]
  (if (seq global-id)
    (let [[col-name _] global-id]
      [:field col-name])
    [:id-rand :uuid]))

(defn- filter-types [types fitler-pred]
  (filter (fn [[_ {:keys [type]}]]
            (fitler-pred type))
          types))

(defn create [desc content]
  (let [data-source-name "Placeholder"
        types (find-types desc content)
        types (priotize-types types)
        global-ids (filter-types types #{:id})
        facts (filter-types types #{:decimal :integer})
        locations (filter-types types #{:location})
        dates (filter-types types #{:date})
        texts (filter-types types #{:text})
        contexts (filter-types types #{:string})
        desc (merge desc
                    {:mapping {:datasource {:name [:value data-source-name]
                                            :global-id [:value (str "source-" (str/lower-case data-source-name))]}
                               :items [{:global-id (global-id (first global-ids))
                                        :features [{:facts (mapv fact facts)
                                                    :locations (or (location locations) [])
                                                    :contexts (mapv context contexts)
                                                    :dates (mapv date dates)
                                                    :texts (mapv text texts)}]}]}})]
    desc))
