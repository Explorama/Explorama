(ns de.explorama.frontend.search.views.util
  (:require [clojure.set :as cljset]
            [clojure.string :as st]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.search.backend.di :as di-backend]
            [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.backend.util :refer [build-options-request-params]]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.search.data.topics :refer [formdata-topics->datasource]]
            [de.explorama.shared.common.unification.time :as t]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.shared.search.ws-api :as ws-api]
            [taoensso.timbre :refer [debug]]))

(defn val->select-option [v int-value?]
  {:value (if int-value?
            (js/parseInt v)
            v)
   :label v})

(defn vec->reactselect-options
  ([inputvec int-value? default-val]
   (if inputvec
     (reduce (fn [res v]
               (if (nil? v)
                 res
                 (conj res (val->select-option v int-value?))))
             []
             inputvec)
     default-val))
  ([inputvec int-value?]
   (vec->reactselect-options inputvec int-value? [])))

(defn range-options [v options comp-func]
  (if (and v (not-empty v))
    (filterv (fn [val]
               (comp-func val (get v :value)))
             options)
    options))

(defn- val->month-option [v lang]
  (let [num (js/parseInt v)]
    {:value num
     :label (i18n/month-name num lang)}))

(defn month-options [values lang]
  (let [months (->> values
                    (map (fn [v]
                           (second (st/split v #"-"))))
                    set
                    sort
                    (mapv #(val->month-option % lang)))]

    months))

(defn day-str->moment-objects [dates]
  (mapv (fn [d]
          (t/date-str->obj :day d))
        dates))

(defn contains-vec? [vec1 vec2]
  (cljset/subset? (set vec2)
                  (set vec1)))

(defn conj-multiple-components [parent & components]
  (reduce
   (fn [p c]
     (conj p c))
   parent
   components))

(defn- normalize-value [value attribute-type]
  (let [norm-fn (fn [{:keys [label value] :as r}]
                  (or value label r))]
    (cond
      (vector? value)
      (mapv norm-fn value)
      (map? value)
      (norm-fn value)
      (and
       (= attribute-type "decimal")
       (or (double? value)
           (float? value)))
      (let [n (js/parseInt value)]
        (if (neg? value)
          [(dec n) n]
          [n (inc n)]))
      :else value)))

(defn- parse-number [value]
  (js/parseInt value))

(defn update-db-multiple-values [db base-path attr attr-type values]
  (debug "Multiple-values init row ui")
  (let [first-ele (if (map? (first values))
                    (:label (first values))
                    (first values))
        last-ele (if (map? (peek values))
                   (:label (peek values))
                   (peek values))]
    (cond
      (= [attrs/year-attr attrs/date-node] attr)
      (update-in db
                 base-path
                 assoc
                 :from {:value (parse-number first-ele)
                        :label first-ele}
                 :to {:value (parse-number last-ele)
                      :label last-ele})
      (= ["day" attrs/date-node] attr)
      (update-in db
                 base-path
                 assoc
                 :start-date first-ele
                 :end-date last-ele)
      (= ["month" attrs/date-node] attr)
      (let [lang (i18n/current-language db)]
        (update-in db
                   base-path
                   assoc
                   :ui-selection (mapv
                                  #(val->month-option % lang)
                                  values)
                   :values (mapv (fn [value]
                                   (parse-number value))
                                 values)))
      (#{"integer" "decimal" "double"} attr-type) (update-in db
                                                             base-path
                                                             assoc
                                                             :from (first values)
                                                             :to (peek values))
      :else (update-in db
                       base-path
                       assoc
                       :ui-selection (mapv (fn [value]
                                             {:value value :label value})
                                           values)
                       :values values))))

(defn update-db-single-date-value [db base-path [precision] value]
  (cond-> db
    :always (update-in base-path
                       assoc
                       :advanced true
                       :cond {:value "=" :label "="})
    (= precision attrs/year-attr) (update-in
                                   base-path
                                   assoc
                                   :value {:value (parse-number value)
                                           :label value})
    (= precision "month") (update-in
                           base-path
                           assoc
                           :ui-selection [{:value (parse-number value) :label (val->month-option value (i18n/current-language db))}]
                           :values [(parse-number value)])
    (= precision "day") (update-in
                         base-path
                         assoc
                         :selected-date value)))

(defn update-db-single-value [db base-path attr attr-type value]
  (let [[_ node-type] attr
        date-attr? (= node-type attrs/date-node)
        number? (#{"decimal" "integer" "double"} attr-type)]
    (cond
      date-attr? (update-db-single-date-value db base-path attr value)
      number? (update-in
               db
               base-path
               assoc
               :advanced true
               :cond {:value "=" :label "="}
               :value (parse-number value))
      :else (update-in
             db
             base-path
             assoc
             :ui-selection [{:value value :label value}]
             :values [value]))))

(re-frame/reg-event-fx
 ::row-selection-init
 (fn [{db :db} [_ frame-id row-name value directly-create-di?]]
   (debug "Set Ui-Selection" {:frame-id frame-id
                              :row-name row-name
                              :value value})
   (let [base-path (spath/search-row-data frame-id row-name)
         timestamp (.getTime (js/Date.))
         attr-type (get-in db (spath/attribute-type row-name))
         value (normalize-value value attr-type)
         formdata (-> (get-in db (spath/frame-search-rows frame-id))
                      (formdata-topics->datasource))
         row-attrs (vec (keys formdata))
         open-vis? (and directly-create-di?
                        (every? true? (map
                                       #(validation/match-parameter-conf? (set (map first row-attrs)) %)
                                       formdata)))
         db (cond-> db
              open-vis?
              (assoc-in (spath/frame-direct-vis-opened? frame-id) true)
              (vector? value)
              (-> (update-db-multiple-values base-path row-name attr-type value)
                  (assoc-in (spath/search-row-options frame-id row-name)
                            value))
              (not (vector? value))
              (-> (update-db-single-value base-path row-name attr-type value)
                  (assoc-in (spath/search-row-options frame-id row-name)
                            [value]))
              :always (update-in base-path assoc :timestamp timestamp))
         {:keys [formdata]} (build-options-request-params db frame-id nil formdata false)
         datasources (get-in db spath/search-enabled-datasources)]
     (if open-vis?
       {:db db
        :dispatch-n [[:de.explorama.frontend.search.views.formdata/search-changed frame-id false]
                     [::di-backend/submit-form frame-id]
                     [ws-api/request-attributes datasources frame-id row-attrs formdata]]}
       {:db db
        :dispatch-later [{:ms config/direct-search-request-delay
                          :dispatch [::options-backend/request-options frame-id row-name true]}]
        :dispatch [ws-api/request-attributes datasources frame-id row-attrs formdata]}))))
