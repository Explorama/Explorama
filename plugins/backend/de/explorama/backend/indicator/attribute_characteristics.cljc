(ns de.explorama.backend.indicator.attribute-characteristics
  (:require [clojure.string :as string]
            [de.explorama.backend.indicator.ac-api :as ac-api]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :refer [debug]]))

(defonce client-options (atom {}))

(def calculation-type-whitelist #{"integer" "decimal" "float" "double"})

(defn update-with-conj
  [col key value]
  (update col key #(conj (or % #{}) value)))

;;;;; UI

(def volatile-ac-ignore #{"year" "day" "datasource" "date" "month" "country"})

(defn build-volatile-options [volatile-acs]
  (reduce (fn [acc {:keys [type name display-name]}]
            (let [option {:value name :label display-name}]
              (cond
                (volatile-ac-ignore name) acc
                (calculation-type-whitelist type) (update acc :calc-attributes conj option)
                :else (-> acc
                          (update :group-attributes conj option)
                          #_(update :x-options conj option)))))
          {:group-attributes #{}
           :calc-attributes #{}}
          volatile-acs))

(defn ui-options-for-data [key-set volatile-acs]
  (let [{volatile-group-attributes :group-attributes
         volatile-calc-attributes :calc-attributes} (build-volatile-options volatile-acs)
        filtered-calc-attributes (filter #(key-set (attrs/access-key (:value %)))
                                         (set (concat (:calc-attributes @client-options)
                                                      volatile-calc-attributes)))
        filtered-group-attributes (-> (filter #(key-set (attrs/access-key (:value %)))
                                              (set (concat (:group-attributes @client-options)
                                                           volatile-group-attributes)))
                                      (conj {:value "datasource" :label "datasource"})
                                      distinct)
        ;TODO r1/indicator Base this on the actual data (need to get all date values to see what the lowest granularity is)
        time-granularity [{:value "year" :label "year"}
                          {:value "month" :label "month"}
                          {:value "day" :label "day"}]]
    (into {}
          (mapv
           (fn [[k values]]
             [k (->> values
                     (sort-by (fn [{:keys [value]}]
                                (string/lower-case value)))
                     vec)])
           {:calc-attributes filtered-calc-attributes
            :group-attributes filtered-group-attributes
            :time-attributes time-granularity}))))

;;;;; SUBSCRIBTIONS

(defn create-options-ui
  "Creates a map with the possible sum-options and y-options.
  Only facts with the types specified in the whitelist are possible for the y-axis.
  Also for the sum-options the concrete values are getting extracted."
  []
  (debug "Create UI options based on ac-service response")
  (let [all-attributes (ac-api/attribute-types)]
    (reset! client-options
            (into {}
                  (mapv (fn [[key values]]
                          [key (mapv
                                (fn [v]
                                  {:label v :value v})
                                (sort values))])
                        (reduce
                         (fn [result [[attr node-label] type]]
                           (cond
                             (= node-label "Context") (update-with-conj result :group-attributes attr)
                             (= node-label "Datasource") (update-with-conj result :group-attributes "datasource")
                             (= node-label "Date") (update-with-conj result :group-attributes attr)
                             (and (= node-label "Fact")
                                  (calculation-type-whitelist type)) (update-with-conj result :calc-attributes attr)
                             #_#_(and (= node-label "Fact") ;Later for other indicator-types maybe
                                      (= "string" node-characteristic)) (update-with-conj result :wordcloud-attrs node-type)
                             (= node-label "Feature") (update-with-conj result :group-attributes attr)
                             :else result))
                         {:group-attributes #{}
                          :calc-attributes #{}}
                         all-attributes))))))

(comment
  @client-options
  (create-options-ui)
  (reset! client-options (create-options-ui))
  (reset! client-options nil))