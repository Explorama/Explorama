(ns de.explorama.backend.charts.attribute-characteristics
  (:require [clojure.string :as string]
            [de.explorama.shared.data-format.filter]
            [de.explorama.backend.charts.ac-api :as ac-api]
            [de.explorama.backend.charts.config :as config]
            [de.explorama.backend.charts.util :refer [attr-value
                                                      data-min-max-date month-range
                                                      relevant-agg-attribute-options relevant-agg-methods-options]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.timbre :refer [debug]]))

(defonce ui-options (atom nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; generic code to read AC Exchange graphs
;;

(defn update-with-conj
  [col key value]
  (update col key #(conj (or % #{}) value)))

(def y-type-whitelist #{"integer" "decimal" "float" "double"})

(def apply-aggregate-attributes #{:y-options})

(defn- add-aggregation-attributes [data-options]
  {:grouped? true
   :groups [{:label :aggregate-attributes
             :options relevant-agg-attribute-options}
            {:label :data-attributes
             :options data-options}]})

(defn- create-options-ui
  "Creates a map with the possible sum-options and y-options.
  Only facts with the types specified in the whitelist are possible for the y-axis.
  Also for the sum-options the concrete values are getting extracted."
  []
  (debug "Create UI options based on ac-api result")
  (into {}
        (mapv (fn [[key values]]
                [key (if (= key :aggregation-options)
                       values
                       (cond-> (mapv
                                (fn [v]
                                  {:label v :value v})
                                (sort values))
                         (apply-aggregate-attributes key)
                         (add-aggregation-attributes)))])
              (reduce
               (fn [result [node-label node-type node-characteristic]]
                 (cond
                   (and (= node-label "Context")
                        (not (= node-type "location"))) (-> result
                                                            (update-with-conj node-type node-characteristic)
                                                            (update-with-conj :sum-options node-type)
                                                            (update-with-conj :x-options node-type)
                                                            (update-with-conj :wordcloud-attrs node-type))
                   (= node-label "Datasource") (-> result
                                                   (update-with-conj :x-options "datasource")
                                                   (update-with-conj :sum-options "datasource")
                                                   (update-with-conj "datasource" node-characteristic))
                   (and (= node-label "Date")
                        (= node-type "year")) (-> result
                                                  (update-with-conj node-type node-characteristic)
                                                  (update-with-conj :sum-options node-type)
                                                  (update-with-conj :x-options node-type))
                   (and (= node-label "Date")
                        (= node-type "month")) (-> result
                                                   (update-with-conj node-type (-> node-characteristic
                                                                                   (string/split #"-")
                                                                                   second))
                                                   (update-with-conj :sum-options node-type)
                                                   (update-with-conj :x-options node-type))
                   (and (= node-label "Date")
                        (= node-type "day")) (-> result
                                                 #_(update-with-conj node-type (-> node-characteristic
                                                                                   (string/split #"-")
                                                                                   second))
                                                 #_(update-with-conj :sum-options node-type)
                                                 (update-with-conj :x-options node-type))
                   (and (= node-label "Fact")
                        (y-type-whitelist node-characteristic)) (update-with-conj result :y-options node-type)
                   (and (= node-label "Fact")
                        (= "string" node-characteristic)) (update-with-conj result :wordcloud-attrs node-type)
                   (= node-label "Feature") (-> result
                                                (update-with-conj :x-options node-type)
                                                (update-with-conj node-type node-characteristic)
                                                (update-with-conj :sum-options node-type)
                                                (update-with-conj :wordcloud-attrs node-type))
                   :else result))
               {:sum-options #{"all"}
                :aggregation-options relevant-agg-methods-options
                :wordcloud-attrs #{"notes"}}
               (ac-api/attributes)))))

(defn populate-ui-options! []
  (reset! ui-options
          (create-options-ui)))

(def volatile-ac-ignore #{"year" "day" "datasource" "date" "month" "country"})

(defn- build-volatile-options [volatile-acs]
  (reduce (fn [acc {:keys [type name display-name]}]
            (let [option {:value name :label display-name}]
              (cond
                (volatile-ac-ignore name) acc
                (y-type-whitelist type) (update acc :y-options conj option)
                :else (-> acc
                          (update :sum-options conj option)
                          (update :x-options conj option)))))
          {:x-options #{}
           :y-options #{}
           :sum-options #{}}
          (:obj-ac volatile-acs)))

(defn get-client-options []
  @ui-options)

(defn ui-options-for-data [data key-set volatile-acs]
  (let [acs-options (get-client-options)
        {min-date :min
         max-date :max} (data-min-max-date data)
        [_ min-month min-day :as min-date] (string/split (str min-date) #"-")
        max-date (string/split (str max-date) #"-")
        months-count (count (month-range min-date max-date))
        {volatile-x-options :x-options
         volatile-y-options :y-options
         volatile-sum-options :sum-options
         volatile-wordcloud-attrs :wordcloud-attrs} (build-volatile-options volatile-acs)
        filtered-options (:aggregation-options acs-options)
        filtered-y (:y-options
                    (update-in acs-options
                               [:y-options :groups 1 :options]
                               (fn [x-options]
                                 (filterv #(key-set (attrs/access-key (:value %)))
                                          (set (concat x-options volatile-y-options))))))
        filtered-x (cond-> (filterv #(key-set (attrs/access-key (:value %)))
                                    (set (concat (:x-options acs-options)
                                                 volatile-x-options)))
                     :always (conj {:value "year" :label "year"})
                     :always (conj {:value "datasource" :label "datasource"})
                     min-month (conj {:value "month" :label "month"})
                     (and min-day (>= config/explorama-charts-max-allowed-month-range months-count))
                     (conj {:value "day" :label "day"})
                     :always (-> distinct vec))
        filtered-sum (as-> (set (concat (:sum-options acs-options)
                                        volatile-sum-options)) $
                       (filterv #(key-set (attrs/access-key (:value %))) $)
                       (conj $
                             {:value "datasource" :label "datasource"}
                             {:value "month" :label "month"}
                             {:value "year" :label "year"})
                       (distinct $)
                       (vec $))
        filtered-sum-keys (mapv #(attrs/access-key (:value %))
                                filtered-sum)
        filtered-wordcloud-attrs (filterv #(key-set (attrs/access-key (:value %)))
                                          (set (concat (:wordcloud-attrs acs-options)
                                                       volatile-wordcloud-attrs)))]
    (into {}
          (mapv
           (fn [[k values]]
             (if (and (map? values)
                      (:grouped? values))
               [k (update values :groups (fn [groups]
                                           (mapv (fn [group]
                                                   (update group
                                                           :options
                                                           (fn [options]
                                                             (vec (sort-by #(string/lower-case (str (:value %)))
                                                                           options)))))
                                                 groups)))]
               [k (sort-by (fn [{:keys [value]}]
                             (cond-> value
                               (keyword? value) (name)
                               :always (string/lower-case)))
                           values)]))
           (update-with-conj
            (reduce (fn [result data-point]
                      (reduce (fn [res sum-key]
                                (let [sum-value (attr-value sum-key data-point)
                                      sum-name (attrs/access-key sum-key)]
                                  (cond
                                    (and (vector? sum-value) (empty? sum-value)) res
                                    (vector? sum-value) (update
                                                         res
                                                         sum-name
                                                         (fn [orig]
                                                           (apply conj (or orig #{}) (mapv (fn [v]
                                                                                             {:label v
                                                                                              :value v})
                                                                                           sum-value))))
                                    sum-value (update-with-conj res sum-name {:label sum-value
                                                                              :value sum-value})
                                    :else res)))
                              result
                              filtered-sum-keys))
                    {:y-options   filtered-y
                     :aggregation-options filtered-options
                     :x-options   filtered-x
                     :sum-options filtered-sum
                     :wordcloud-attrs filtered-wordcloud-attrs}
                    data)
            :sum-options
            {:label "All" :value "all"})))))

(comment
  (reset! ui-options
          (create-options-ui)))