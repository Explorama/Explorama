(ns de.explorama.frontend.charts.charts.settings
  (:require [data-format-lib.filter]
            [de.explorama.frontend.charts.charts.bar :as bar]
            [de.explorama.frontend.charts.charts.bubble :as bubble]
            [de.explorama.frontend.charts.charts.line :as line]
            [de.explorama.frontend.charts.charts.pie :as pie]
            [de.explorama.frontend.charts.charts.scatter :as scatter]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.charts.wordcloud :as wordcloud]
            [de.explorama.frontend.charts.config :as vconfig]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.charts.config :refer [explorama-charts-chartjs-selectlimit
                                                       explorama-charts-max-multiple]]
            [de.explorama.shared.charts.ws-api :as ws-api]
            [de.explorama.shared.common.data.attributes :as attrs]
            [re-frame.core :as re-frame]))

(defonce attr-options (atom nil))

(defn clean-options [frame-id]
  (swap! attr-options dissoc frame-id))

(defn y-options [frame-id]
  (get-in @attr-options [frame-id :y-options] {}))

(defn aggregation-options [frame-id]
  (get-in @attr-options [frame-id :aggregation-options] []))

(defn x-options [frame-id]
  (vec (get-in @attr-options [frame-id :x-options] [])))

(defn sum-options [frame-id]
  (vec (get-in @attr-options [frame-id :sum-options] [])))

(defn sum-by-characteristics [frame-id selected-attr-key]
  (vec (get-in @attr-options [frame-id selected-attr-key] [])))

(defn wordcloud-attrs [frame-id]
  (vec (get-in @attr-options [frame-id :wordcloud-attrs] [])))

(def possible-charts
  (array-map path/line-id-key line/chart-desc
             path/bar-id-key bar/chart-desc
             path/scatter-id-key scatter/chart-desc
             path/pie-id-key pie/chart-desc
             path/bubble-id-key bubble/chart-desc
             path/wordcloud-id-key wordcloud/chart-desc))

(def default-chart-desc line/chart-desc)

(defn default-options [frame-id]
  {:typ (get default-chart-desc path/chart-desc-id-key)
   :aggregation-method ws-api/default-aggregation-method
   :y (get-in (y-options frame-id)
              [:groups 1 :options 0 :value])
   :x ws-api/default-x-attribute
   :r (get-in (y-options frame-id)
              [:groups 0 :options 0 :value])
   :sum-by ws-api/default-sum-by
   :sum-filter ws-api/default-sum-filter
   :stopping-attributes ws-api/default-stopping-attrs
   :stemming-attributes ws-api/default-stemming-attrs})

(defn- translate-helper [db attr]
  (if (keyword? attr)
    (i18n/translate db attr)
    (i18n/attribute-label
     (fi/call-api [:i18n :get-labels-db-get] db)
     attr)))

(defn- set-initial-y-option [db translate-attr-fn frame-id chart-index datasets]
  (let [{:keys [y-axis-attr]} (nth datasets chart-index)
        y-option (->> (into (get-in (y-options frame-id)
                                    [:groups 1 :options])
                            (get-in (y-options frame-id)
                                    [:groups 0 :options]))
                      (some (fn [{:keys [value] :as o}]
                              (when (= value y-axis-attr)
                                o))))]
    (cond-> db
      y-option (assoc-in (path/y-option frame-id chart-index)
                         (update y-option :label translate-attr-fn)))))

(defn- update-chart-selections [db frame-id datasets chart-index applied-params]
  (let [{:keys [y-axis x-axis r-attr sum-by sum-filter aggregation-method sum-remaining? changed-y-range]
         chart-type :type}
        applied-params
        type-desc (get possible-charts chart-type)
        translate-attr-fn (partial translate-helper db)
        option-fn (fn [option is-attr?]
                    (cond
                      (map? option) option
                      (and is-attr? option) {:value option :label (translate-attr-fn option)}
                      option {:value option :label option}))
        find-option-fn (fn [v options translate?]
                         (some (fn [{:keys [value] :as opt}]
                                 (when (= value v)
                                   (cond-> opt
                                     translate? (update :label translate-attr-fn))))
                               options))]
    (cond-> (-> db
                (assoc-in (path/chart-type frame-id chart-index) type-desc))
      x-axis (assoc-in (path/x-option frame-id) (option-fn x-axis true))
      r-attr (assoc-in (path/r-option frame-id chart-index) (option-fn r-attr true))
      y-axis (assoc-in (path/y-option frame-id chart-index) (option-fn y-axis true))
      (not y-axis) (set-initial-y-option translate-attr-fn frame-id chart-index datasets)
      sum-by (assoc-in (path/sum-by-option frame-id chart-index)
                       (find-option-fn sum-by (sum-options frame-id) true))
      sum-filter (assoc-in (path/sum-by-values frame-id chart-index)
                           (mapv #(option-fn % false) sum-filter))
      aggregation-method (assoc-in (path/aggregate-method frame-id chart-index)
                                   (find-option-fn aggregation-method (aggregation-options frame-id) true)))))

(defn update-charts-selections [db frame-id applied-params datasets]
  (reduce (fn [db [chart-index applied-params]]
            (update-chart-selections db frame-id datasets chart-index applied-params))
          db
          (map-indexed vector applied-params)))

(re-frame/reg-event-db
 ::use-preexisting-settings
 (fn [db [_ frame-id chart-desc]]
   (let [chart-desc (-> chart-desc
                        (update :charts
                                (fn [chart-descs]
                                  (mapv (fn [chart-desc]
                                          (let [desc-id (get chart-desc path/chart-desc-id-key)]
                                            (update chart-desc
                                                    path/chart-type-desc-key
                                                    #(merge (get possible-charts desc-id) %))))
                                        chart-descs)))
                        (update :x-option (fn [v]
                                            (cond
                                              (nil? v) nil
                                              (string? v) {:label v :value v}
                                              (map? v) v))))]
     (update-in db (path/chart-desc frame-id) merge chart-desc))))

(defn chart-desc->chart-type [db frame-id chart-index]
  (let [chart-desc (get-in db (path/chart-type frame-id chart-index) default-chart-desc)
        desc-id (get chart-desc path/chart-desc-id-key)]
    (merge (get possible-charts desc-id) chart-desc)))

(re-frame/reg-sub
 ::chart-type
 (fn [db [_ frame-id chart-index]]
   (chart-desc->chart-type db frame-id chart-index)))

(defn- num-of-charts [db frame-id]
  (count (get-in db (path/charts frame-id))))

(re-frame/reg-sub
 ::num-of-charts
 (fn [db [_ frame-id]]
   (count (get-in db (path/charts frame-id)))))

(re-frame/reg-sub
 ::add-chart-active?
 (fn [db [_ frame-id]]
   (let [chart-count (num-of-charts db frame-id)
         {active-chart-multi-possible? path/chart-desc-multiple-key} (chart-desc->chart-type db frame-id 0)
         max-number-of-characteristics (/ explorama-charts-chartjs-selectlimit (inc chart-count))
         all-max-characteristics-not-reached? (every? (fn [chart-index]
                                                        (<= (count (get-in db (path/sum-by-values frame-id chart-index) []))
                                                            max-number-of-characteristics))
                                                      (range chart-count))
         active? (and (or (> chart-count 1)
                          active-chart-multi-possible?)
                      (< chart-count explorama-charts-max-multiple)
                      all-max-characteristics-not-reached?)]
     {:active? active?
      :not-active-reason (cond
                           (>= chart-count explorama-charts-max-multiple) :charts-max-charts-exceeding
                           (not active-chart-multi-possible?) :charts-multi-charts-not-allowed
                           (not all-max-characteristics-not-reached?) :charts-max-number-chars-reached-new-charts)
      :max-allow-chars max-number-of-characteristics})))

(re-frame/reg-event-fx
 ::add-chart
 (fn [{db :db} [_ frame-id]]
   {:db (update-in db (path/charts frame-id) conj {})
    :dispatch-n (cutils/req-datasets db frame-id)}))

(re-frame/reg-event-fx
 ::remove-chart
 (fn [{db :db} [_ frame-id chart-index]]
   {:db (update-in db (path/charts frame-id)
                   (fn [coll]
                     (into (subvec coll 0 chart-index)
                           (subvec coll (inc chart-index)))))
    :dispatch-n (cutils/req-datasets db frame-id)}))

(re-frame/reg-sub
 ::y-option
 (fn [db [_ frame-id chart-index]]
   (if (nil? chart-index)
     (mapv (fn [chart-desc]
             (get chart-desc path/y-option-key))
           (get-in db (path/charts frame-id)))
     (get-in db
             (path/y-option frame-id chart-index)))))

(re-frame/reg-sub
 ::y-range-change?
 (fn [db [_ frame-id chart-index]]
   (get-in db (path/y-range-change? frame-id chart-index) false)))

(re-frame/reg-event-fx
 ::y-range-change
 (fn [{db :db} [_ frame-id chart-index change?]]
   {:db (assoc-in db (path/y-range-change? frame-id chart-index) change?)
    :dispatch [::y-range-request-dataset frame-id]}))

(re-frame/reg-event-fx
 ::y-range-request-dataset
 (fn [db [_ frame-id]]
   (let [chart-descs (get-in db (path/charts frame-id))
         y-ranges-valid? (every? (fn [chart-desc]
                                   (cutils/y-range-change-valid? chart-desc))
                                 chart-descs)]
     (cutils/clear-timeout-request frame-id)
     (when y-ranges-valid?
       {:dispatch-n (cutils/req-datasets db frame-id)}))))

(re-frame/reg-sub
 ::original-min-y
 (fn [db [_ frame-id chart-index]]
   (get-in db (path/org-min-y frame-id chart-index))))

(re-frame/reg-sub
 ::changed-min-y
 (fn [db [_ frame-id chart-index]]
   (get-in db
           (path/changed-min-y frame-id chart-index)
           (get-in db (path/org-min-y frame-id chart-index)))))

(re-frame/reg-event-fx
 ::change-min-y
 (fn [{db :db} [_ frame-id chart-index new-val valid?]]
   (if (and (= new-val "") (nil? valid?)) ;clicked on clear-button
     {:db (-> db
              (assoc-in (path/changed-min-y frame-id chart-index)
                        (get-in db (path/org-min-y frame-id chart-index)))
              (assoc-in (path/changed-min-y-valid? frame-id chart-index) true))
      :dispatch-n (cutils/req-datasets db frame-id)}
     {:db (-> db
              (assoc-in (path/changed-min-y frame-id chart-index) new-val)
              (assoc-in (path/changed-min-y-valid? frame-id chart-index) valid?))})))

(re-frame/reg-sub
 ::original-max-y
 (fn [db [_ frame-id chart-index]]
   (get-in db (path/org-max-y frame-id chart-index))))

(re-frame/reg-sub
 ::changed-max-y
 (fn [db [_ frame-id chart-index]]
   (get-in db
           (path/changed-max-y frame-id chart-index)
           (get-in db (path/org-max-y frame-id chart-index)))))

(re-frame/reg-event-fx
 ::change-max-y
 (fn [{db :db} [_ frame-id chart-index new-val valid?]]
   (if (and (= new-val "") (nil? valid?)) ;clicked on clear-button
     {:db (-> db
              (assoc-in (path/changed-max-y frame-id chart-index)
                        (+ (get-in db (path/org-max-y frame-id chart-index)) 1)) ;To get a small gap between last point and upper chart border
              (assoc-in (path/changed-max-y-valid? frame-id chart-index) true))
      :dispatch-n (cutils/req-datasets db frame-id)}
     {:db (-> db
              (assoc-in (path/changed-max-y frame-id chart-index) new-val)
              (assoc-in (path/changed-max-y-valid? frame-id chart-index) valid?))})))

(re-frame/reg-event-fx
 ::change-y-option
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [db (assoc-in db
                      (path/y-option frame-id chart-index)
                      selection)]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))

(re-frame/reg-sub
 ::aggregate-methods
 (fn [db [_ frame-id]]
   (mapv (fn [option]
           (-> option
               (update :label #(i18n/translate db %))))
         (aggregation-options frame-id))))

(re-frame/reg-sub
 ::aggregate-method
 (fn [db [_ frame-id chart-index]]
   (get-in db
           (path/aggregate-method frame-id chart-index)
           (-> (filterv #(= ws-api/default-aggregation-method (:value %))
                        (aggregation-options frame-id))
               (first)
               (update :label #(i18n/translate db %))))))


(re-frame/reg-event-fx
 ::change-aggregate-method
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [db (assoc-in db
                      (path/aggregate-method frame-id chart-index)
                      selection)]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))


(re-frame/reg-sub
 ::x-option
 (fn [db [_ frame-id]]
   (let [default-select (update ws-api/default-selected-x
                                :label
                                i18n/attribute-label)]
     (into {} (get-in db (path/x-option frame-id) default-select)))))

(re-frame/reg-event-fx
 ::change-x-option
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id selection]]
   (let [db (assoc-in db
                      (path/x-option frame-id)
                      selection)]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))

(re-frame/reg-sub
 ::r-option
 (fn [db [_ frame-id chart-index]]
   (into {}
         (get-in db (path/r-option frame-id chart-index)
                 (-> (get-in (y-options frame-id)
                             [:groups 0 :options 0])
                     (update :label #(i18n/translate db %)))))))

(re-frame/reg-event-fx
 ::change-r-option
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [db (assoc-in db
                      (path/r-option frame-id chart-index)
                      selection)]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))

(re-frame/reg-sub
 ::sum-by-option
 (fn [db [_ frame-id chart-index]]
   (get-in db (path/sum-by-option frame-id chart-index) {:value "all"
                                                         :label "All"})))

(re-frame/reg-event-fx
 ::change-sum-by-option
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [db (assoc-in db
                      (path/sum-by-option frame-id chart-index)
                      selection)]
     {:db db
      :dispatch-n [[::change-sum-by-values frame-id chart-index []]]})))

(re-frame/reg-sub
 ::sum-by-values
 (fn [db [_ frame-id chart-index]]
   (vec (get-in db (path/sum-by-values frame-id chart-index) []))))

(re-frame/reg-event-fx
 ::change-sum-by-values
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [db (assoc-in db
                      (path/sum-by-values frame-id chart-index)
                      selection)]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))

(re-frame/reg-sub
 ::sum-remaining
 (fn [db [_ frame-id chart-index]]
   (get-in db (path/sum-remaining frame-id chart-index))))

(re-frame/reg-event-fx
 ::change-sum-remaining
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [db (assoc-in db
                      (path/sum-remaining frame-id chart-index)
                      selection)]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))

(re-frame/reg-sub
 ::y-options
 (fn [db [_ frame-id]]
   (mapv (fn [group]
           (-> group
               (update :label #(i18n/translate db %))
               (update :options
                       #(->> %
                             (mapv (fn [{:keys [label] :as opt}]
                                     (if (keyword? label)
                                       (assoc opt :label (i18n/translate db label))
                                       opt)))))))

         (:groups (y-options frame-id)))))

(re-frame/reg-sub
 ::x-options
 (fn [_ [_ frame-id]]
   (x-options frame-id)))

(re-frame/reg-sub
 ::sum-by-options
 (fn [_ [_ frame-id]]
   (sum-options frame-id)))

(re-frame/reg-sub
 ::sum-by-characteristics
 (fn [db [_ frame-id chart-index]]
   (let [selected-attr (get-in db (conj (path/sum-by-option frame-id chart-index)
                                        :value))]
     (if (not= selected-attr "all")
       (let [sum-by-values (count (get-in db (path/sum-by-values frame-id chart-index) []))
             selected-attr-key (attrs/access-key selected-attr)]
         (if (> (/ explorama-charts-chartjs-selectlimit (num-of-charts db frame-id))
                sum-by-values)
           (sum-by-characteristics frame-id selected-attr-key)
           []))
       []))))

(re-frame/reg-sub
 ::hidden-datasets
 (fn [db [_ frame-id]]
   (get-in db (path/hidden-datasets frame-id) #{})))

(defn- show-legend-elem [frame-id chart-type idx]
  (if-not (= :pie chart-type)
    (cutils/show-dataset frame-id idx true)
    (cutils/pie-show-dataset frame-id idx true)))

(defn- hide-legend-elem [frame-id chart-type idx]
  (if-not (= :pie chart-type)
    (cutils/show-dataset frame-id idx false)
    (cutils/pie-show-dataset frame-id idx false)))

(re-frame/reg-event-db
 ::change-hidden-datasets
 (fn [db [_ frame-id chart-type idx label action]]
   (condp = action
     :add (do
            (hide-legend-elem frame-id chart-type idx)
            (update-in db (path/hidden-datasets frame-id)
                       (fn [o] (-> (or o #{})
                                   (conj {:idx idx
                                          :label label})))))
     :rm (do
           (show-legend-elem frame-id chart-type idx)
           (update-in db (path/hidden-datasets frame-id)
                      (fn [o] (-> (or o #{})
                                  (disj {:idx idx
                                         :label label})))))
     db)))

