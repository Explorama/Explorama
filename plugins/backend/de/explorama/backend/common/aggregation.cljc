(ns de.explorama.backend.common.aggregation
  (:require [clojure.string :as string]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.misc :as cljc-misc]
            [taoensso.tufte :as tufte]))

(defn- stringify-years [years]
  (let [{:keys [result]} (reduce
                          (fn [{:keys [result prev-year]} year]
                            {:result
                             (if (= prev-year 0)
                               (str year)
                               (if (= year (inc prev-year))
                                 (if (string/ends-with? result "-")
                                   result
                                   (str result "-"))
                                 (if (string/ends-with? result "-")
                                   (str result prev-year ", " year)
                                   (str result ", " year))))
                             :prev-year year})
                          {:result    ""
                           :prev-year 0}
                          years)]
    (if (string/ends-with? result "-")
      (str result (last years))
      result)))

(defn- stringify-countries [countries]
  (string/join ", " countries))

(defn- stringify-datasource [datasources]
  (string/join ", " (sort datasources)))

(defn calculate-dimensions [data]
  (let [{:keys [datasources years countries buckets] :as dim-info}
        (->> (reduce (fn [acc {country (attrs/access-key "country")
                               date-elem (attrs/access-key "date")
                               bucket attrs/bucket-attr
                               datasource attrs/datasource-attr}]
                       (let [year (-> date-elem
                                      (subs 0 4)
                                      cljc-misc/cljc-parse-int)]
                         (-> acc
                             (update :years conj year)
                             (update :datasources conj datasource)
                             (update :countries (fn [o]
                                                  (if (vector? country)
                                                    (into o country)
                                                    (conj o country))))
                             (update :buckets conj bucket))))
                     {:datasources #{}
                      :years #{}
                      :countries #{}
                      :buckets #{}}
                     data)
             (reduce (fn [acc [k v]]
                       (assoc acc k (sort v)))
                     {}))]
    {:buckets buckets
     :datasources (tufte/p ::calc-datasources (stringify-datasource datasources))
     :years (tufte/p ::calc-years (stringify-years years))
     :countries (tufte/p ::calc-countries (stringify-countries countries))
     :dim-info dim-info}))