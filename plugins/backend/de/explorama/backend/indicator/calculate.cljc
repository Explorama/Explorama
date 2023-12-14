(ns de.explorama.backend.indicator.calculate
  (:require [clojure.set :as set]
            [data-format-lib.filter-functions :as ff]
            [data-format-lib.operations :as of]
            [de.explorama.backend.indicator.data.core :as data]
            [de.explorama.backend.indicator.persistence.core :as persistence]))

(defn- apply-calculation-desc [description di-data]
  (when (seq description)
    (let [operation-result (of/perform-operation di-data
                                                 nil
                                                 description
                                                 ff/default-impl)]
      operation-result)))

(defn calculate-indicator
  ([indicator]
   (let [local-filters (->> indicator
                            :local-filters
                            (filter second))
         dis (get indicator :dis)
         di-data (data/load-di-data dis local-filters)]
     (calculate-indicator indicator di-data)))
  ([indicator di-data]
   (let [{:keys [calculation-desc
                 group-attributes
                 additional-attributes]} indicator]
     (apply-calculation-desc calculation-desc di-data))))

(defn- type-mapping [key vt]
  (cond (= key "date")
        [:date :date]
        (= key "location")
        [:location :location]
        (= key "notes")
        [:fulltext :fulltext]
        (int? vt)
        [:numeric :integer]
        (double? vt)
        [:numeric :double]
        (boolean? vt)
        [:categoric :boolean]
        (string? vt)
        [:categoric :string]
        (keyword? vt)
        [:categoric :keyword]))

(defn- event-acs [event]
  (->> (map (fn [[attr-key attr-value]]
              (let [[_ attr-data-type] (type-mapping attr-key attr-value)
                    [name type label key]
                    (cond (= attr-key "location")
                          [attr-key "string" "Context" attr-key]
                          (= attr-key "notes")
                          [attr-key "fulltext" "Notes" attr-key]
                          (= attr-data-type :integer)
                          [attr-key "integer" "Fact" attr-key]
                          (= attr-data-type :double)
                          [attr-key "decimal" "Fact" attr-key]
                          (= attr-data-type :boolean)
                          [attr-key "boolean" "Fact" attr-key]
                          (= attr-data-type :string)
                          [attr-key "string" "Context" attr-key]
                          (= attr-data-type :keyword)
                          [attr-key "string" "Context" attr-key]
                          :else
                          [attr-key "string" "Context" attr-key])]
                (when (and name type label key)
                  {:name  name
                   :type  type
                   :label label
                   :key   key})))
            event)
       (filterv identity)
       set))

(def base-acs #{{:name  "date"
                 :type  "date"
                 :label "Date"
                 :key   "date"}
                {:name  "year"
                 :type  "date"
                 :label "Date"
                 :key   "year"}
                {:name  "month"
                 :type  "date"
                 :label "Date"
                 :key   "month"}
                {:name  "day"
                 :type  "date"
                 :label "Date"
                 :key   "day"}
                {:name  "datasource"
                 :type  "string"
                 :label "Datasource"
                 :key   "datasource"}})

(defn color-acs [ac-options indicator-id ignore-values]
  (->> ac-options
       (remove (comp ignore-values :key))
       (mapv (fn [{:keys [name type]}]
               {:name         name
                :display-name (if (#{"integer" "decimal" "float" "double"} type)
                                (str name " (number)")
                                name)
                :info         (list indicator-id)
                :type         type}))))

(defn generate-volatile-acs [{indicator-name :name
                              indicator-id :id
                              :as indicator-desc}]
  (let [indicator-data (calculate-indicator indicator-desc)
        ac-options (into [] (reduce (fn [acc event]
                                      (set/union acc
                                                 (event-acs (dissoc event
                                                                    "date"
                                                                    "datasource"
                                                                    "id"))))
                                    base-acs
                                    indicator-data))]
    {:ac       {indicator-name ac-options}
     :color-ac (color-acs ac-options indicator-id #{"notes" "location" "year" "month" "day"})
     :obj-ac   (color-acs ac-options indicator-id #{"year" "month" "day"})}))

(defn get-data-tile [{:keys [id description dis] :as data-tile}]
  (let [indicator-desc (if id
                         (persistence/read-indicator id)
                         description)
        di-data (when (not-empty dis)
                  (data/load-di-data dis nil))]
    [data-tile
     (if (not-empty dis)
       (calculate-indicator indicator-desc di-data)
       (calculate-indicator indicator-desc))]))

(defn get-data-tiles [data-tiles _]
  (mapv get-data-tile data-tiles))

(defn create-di-and-acs [{:keys [client-callback]}
                         [indicator-id-or-desc project?]]
  (let [indicator-desc (if project?
                         indicator-id-or-desc
                         (persistence/read-indicator indicator-id-or-desc))
        di (data/generate-di indicator-desc)
        volatile-acs (generate-volatile-acs indicator-desc)]
    (client-callback (assoc di
                            :di/acs volatile-acs)
                     project?
                     indicator-desc)))
