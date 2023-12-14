(ns de.explorama.frontend.charts.charts.backend-interface
  (:require [de.explorama.frontend.common.calculations.data-acs-client :as data-acs]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.charts.charts.redo :as redo]
            [de.explorama.frontend.charts.charts.settings :as settings]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.config :as config]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.shared.charts.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as timbre :refer-macros [debug warn]]))

(defn- reset-chart-data [db frame-id]
  (update-in db
             (path/chart-frame frame-id)
             dissoc
             path/chart-data-key path/chart-type-desc-key
             path/sum-by-option-key path/sum-by-values-key
             path/min-occurence-key path/attributes-key
             path/search-selection-key path/use-nlp?-key
             path/use-nlp-attributes-key))

(defn- chart-desc->request-desc
  "Translates one chart-desc (:charts [{...}]) into a request description used in the server."
  [x-axis
   {default-chart-type :typ
    default-y :y
    default-r :r
    default-sum-by :sum-by
    default-sum-filter :sum-filter
    default-aggregation-method :aggregation-method
    default-stopping-attrs :stopping-attributes
    default-stemming-attrs :stemming-attributes}
   chart-desc]
  (let [chart-type (get-in chart-desc
                           [path/chart-type-desc-key path/chart-desc-id-key]
                           default-chart-type)
        chart-keyword (keyword chart-type)
        aggregation-method (get-in chart-desc [path/aggregate-method-key :value] default-aggregation-method)
        y-axis (get-in chart-desc [path/y-option-key :value] default-y)
        y-range-changed? (get chart-desc path/y-range-change?-key)
        changed-y-range (get chart-desc path/changed-y-range-key)
        r (get-in chart-desc [path/r-option-key :value] default-r)
        sum-by (get-in chart-desc [path/sum-by-option-key :value] default-sum-by)
        sum-remaining? (get chart-desc path/sum-remaining-key)
        sum-filter (set (map :value (get chart-desc path/sum-by-values-key default-sum-filter)))
        attributes (mapv :value (get chart-desc path/attributes-key [{:value :characteristics}]))
        stopping-attributes (set (map :value (get chart-desc path/stopping-attrs-key default-stopping-attrs)))
        stemming-attributes (set (map :value (get chart-desc path/stemming-attrs-key default-stemming-attrs)))
        min-occurence (get chart-desc path/min-occurence-key 1)]
    (cond-> {:type chart-type
             :y-axis y-axis
             :sum-by sum-by
             :sum-filter sum-filter
             :aggregation-method aggregation-method
             :sum-remaining? sum-remaining?}
      y-range-changed?
      (assoc :changed-y-range changed-y-range)
      (not= chart-keyword path/pie-id-key)
      (assoc :x-axis x-axis)
      (= chart-keyword path/bubble-id-key)
      (assoc :r-attr r)
      (= chart-keyword path/wordcloud-id-key)
      (assoc :stopping-attributes stopping-attributes
             :stemming-attributes stemming-attributes
             :attributes attributes
             :min-occurence min-occurence)
      (= chart-keyword path/wordcloud-id-key)
      (dissoc :x-axis :y-axis :sum-by :sum-filter))))

(defn- request-params [db frame-id task-id {:keys [new-di? filter-update? di force-operations-state?]}]
  (let [{default-x :x
         :as default-options} (settings/default-options frame-id)
        x-axis (get-in db
                       (conj (path/x-option frame-id)
                             :value)
                       default-x)

        chart-request-descs (mapv (partial chart-desc->request-desc x-axis default-options)
                                  (if (empty? (get-in db (path/charts frame-id)))
                                    [{}] ;Default one chart with no description => using all defaults
                                    (get-in db (path/charts frame-id))))
        di (or di (get-in db (path/frame-di frame-id)))
        local-filter (get-in db (path/applied-filter frame-id))
        is-in-render-mode? (fi/call-api [:interaction-mode :render-db-get?] db)]
    (cond-> {:frame-id frame-id
             :vis-type :charts
             :di di
             :local-filter local-filter
             :task-id task-id
             :charts chart-request-descs}
      new-di?
      (assoc :volatile-acs (get-in db (path/volatile-acs-frame frame-id))
             :operations-state (when (or is-in-render-mode? force-operations-state?)
                                 (redo/build-operations-state db frame-id))
             :calc {:options? true
                    :data-acs? true
                    :di-desc? true})
      filter-update?
      (assoc :calc {:di-desc? true})
      :always (assoc-in [:calc :datasets?] true))))

(defn- request-valid? [{:keys [di charts]}]
  (boolean
   (and di
        (every? (fn [{:keys [sum-by sum-filter attributes x-axis y-axis type]
                      :as chart-desc}]
                  (let [chart-keyword (keyword type)]

                    (and type
                         (cond
                           (= chart-keyword path/pie-id-key) (and sum-by
                                                                  sum-filter)
                           (= chart-keyword path/wordcloud-id-key) (seq attributes)
                           :else (and y-axis
                                      x-axis
                                      sum-by
                                      sum-filter
                                      (cutils/y-range-change-valid? chart-desc))))))
                charts)
        (or (= (count charts) 1)
            (every? (fn [{:keys [type]}]
                      (let [chart-keyword (keyword type)]
                        (and (not= chart-keyword path/pie-id-key)
                             (not= chart-keyword path/wordcloud-id-key))))
                    charts)))))

(re-frame/reg-event-fx
 ::apply-filter
 (fn [{db :db} [_ frame-id local-filter task-id]]
   (let [db (-> db
                (assoc-in (path/applied-filter frame-id) local-filter)
                (assoc-in (path/hidden-datasets frame-id) #{}))
         req-params (request-params db frame-id task-id {:filter-update? true})]
     {:db (-> db
              (assoc-in (path/last-request-params frame-id) req-params)
              (path/reset-stop-views frame-id))
      :fx []
      :backend-tube [ws-api/chart-datasets {:client-callback [ws-api/chart-datasets-result]
                                            :failed-callback [ws-api/chart-error frame-id task-id]}
                     req-params]})))

(re-frame/reg-event-fx
 ::connect-to-datainstance
 (fn [{db :db} [_
                {:keys [frame-target-id di keep-selection? remove-local-filter? force-operations-state?]}
                [source-local-filter]
                task-id]]
   (debug "--> connect to di" {:frame-target-id frame-target-id
                               :keep-selection? keep-selection?
                               :di di
                               :task-id task-id
                               :source-local-filter source-local-filter})
   (let [req-params (cond-> (request-params db frame-target-id task-id {:new-di? true
                                                                        :force-operations-state? force-operations-state?
                                                                        :di di})
                      remove-local-filter? (dissoc :local-filter)
                      (seq source-local-filter) (assoc :local-filter source-local-filter))]
     (if di
       (let [old-local-filter (get-in db (path/applied-filter frame-target-id))
             db (cond-> db
                  (not keep-selection?) (reset-chart-data frame-target-id) ;Alte Daten löschen
                  source-local-filter (assoc-in (path/applied-filter frame-target-id) source-local-filter)
                  remove-local-filter? (assoc-in (path/applied-filter frame-target-id) nil)
                  (not (get-in db (path/charts frame-target-id)))
                  (assoc-in (path/charts frame-target-id) [])
                  :always (assoc-in (path/last-request-params frame-target-id) req-params)
                  :always (assoc-in (path/frame-di frame-target-id)
                                    di)
                  :always (path/reset-stop-views frame-target-id))]
         {:db db
          :backend-tube [ws-api/chart-datasets {:client-callback [ws-api/chart-datasets-result]
                                                :failed-callback [ws-api/chart-error frame-target-id task-id]}
                         req-params]
          :fx [[:dispatch (fi/call-api :frame-notifications-clear-event-vec frame-target-id)]]})
       (do
         (warn "Connect not possible: " {:di di})
         {})))))

(re-frame/reg-event-fx
 ::request-datasets
 (fn [{db :db} [_ frame-id task-id]]
   (let [req-params (request-params db frame-id task-id {})]
     (debug "request-datasets" {:frame-id frame-id})
     (if (request-valid? req-params)
       {:db (cond-> (assoc-in db
                              (path/last-request-params frame-id)
                              req-params)
              (not (get-in db (path/charts frame-id)))
              (assoc-in (path/charts frame-id) [])
              :always (path/reset-stop-views frame-id))
        :backend-tube [ws-api/chart-datasets {:client-callback [ws-api/chart-datasets-result]
                                              :failed-callback [ws-api/chart-error frame-id task-id]}
                       req-params]}

       (do
         (warn "Request params invalid" {:frame-id frame-id
                                         :request-params req-params})
         {:fx [[:dispatch [::ddq/execute-callback-vec frame-id]]
               [:dispatch [::ddq/finish-task frame-id task-id ::request-datasets]]]})))))

(defn- set-attr-labels [m]
  (let [translate-fn (fn [{:keys [label value]}]
                       {:value value
                        :label (cond-> label
                                 (string? label)
                                 (config/attribute->display))})]
    (-> (reduce (fn [acc [k opts]]
                  (if (and (map? opts)
                           (:grouped? opts))
                    (assoc! acc k
                            (update-in opts
                                       [:groups 1 :options]
                                       #(mapv translate-fn %)))
                    (assoc! acc k
                            (mapv translate-fn opts))))

                (transient {})
                m)
        (persistent!))))

(defn- handle-connected-result [db frame-id {:keys [data-acs di-desc
                                                    data-count filtered-count
                                                    invalid-operations options
                                                    stop-filterview? warn-filterview? not-too-much?]}]
  (debug "<-- connected to di !!" {:frame-id frame-id
                                   :data-count data-count
                                   :filtered-count filtered-count
                                   :stop-filterview? stop-filterview?
                                   :warn-filterview? warn-filterview?
                                   :not-too-much? not-too-much?
                                   :options options})
  (when options
    (swap! settings/attr-options assoc frame-id
           (set-attr-labels options)))
  (when (get-in db (path/chart-frame frame-id))
    (let [not-supported-redo-ops-ev (when (redo/show-notification? invalid-operations)
                                      (fi/call-api :frame-notifications-not-supported-redo-ops-event-vec
                                                   frame-id invalid-operations))]
      {:db (cond-> (-> db
                       (assoc-in (path/di-desc frame-id) di-desc))
             data-acs
             (assoc-in (conj (path/frame-filter frame-id) :data-acs)
                       (data-acs/post-process data-acs))
             invalid-operations (redo/remove-invalid-operations frame-id invalid-operations)
             (boolean? warn-filterview?)
             (assoc-in (path/filter-warn-limit-reached frame-id)
                       warn-filterview?)
             (boolean? stop-filterview?)
             (assoc-in (path/filter-stop-limit-reached frame-id)
                       stop-filterview?))
       :fx (cond-> []
             not-supported-redo-ops-ev
             (conj [:dispatch not-supported-redo-ops-ev])
             (or data-count filtered-count)
             (conj [:dispatch [:de.explorama.frontend.charts.components.frame-header/set-counts
                               frame-id data-count filtered-count]]))})))

(re-frame/reg-event-fx
 ws-api/chart-datasets-result
 (fn [{db :db} [_ {:keys [frame-id applied-params datasets di-desc task-id] :as result}]]
   (debug "Datasets response" {:frame-id frame-id} datasets)
   (let [{:keys [fx] ndb :db} (when di-desc (handle-connected-result db frame-id result))
         log-state?  (not (fi/call-api :project-loading-db-get db))
         db (-> (or ndb db)
                (assoc-in (path/chart-data frame-id)
                          datasets)
                (update-in (path/chart-frame frame-id)
                           dissoc
                           path/chartdata-loading-key)
                (assoc-in (path/hidden-datasets frame-id) #{})
                (settings/update-charts-selections frame-id applied-params datasets))]
     {:db db
      :fx (cond-> (or fx [])
            log-state? (conj [:dispatch (cutils/log-chart-update db frame-id)])
            :always (conj
                     [:dispatch [::ddq/finish-task frame-id task-id ::chart-datasets-result]]))})))

(re-frame/reg-event-fx
 ::cancel-loading
 (fn [{db :db} [_ frame-id]]
   {:db db
    :backend-tube [ws-api/set-backend-canceled frame-id]}))

(re-frame/reg-event-fx
 ws-api/chart-error
 (fn [{db :db} [_ frame-id task-id {{:keys [error] :as error-desc} :error-desc}]]
   (warn "ERROR while backend request" {:frame-id frame-id
                                        :task-id task-id
                                        :error-desc error-desc})

   (let [stop-view-label (case error
                           :too-much-data :stop-view-too-much-data
                           :same-attributes :stop-view-invalid-selection
                           :invalid-aggregation :stop-view-invalid-selection
                           :same-time-selection :stop-view-invalid-time-selection
                           :unknown :stop-view-unknown)]

     {:fx (cond-> [[:dispatch [:de.explorama.frontend.charts.components.stop-screen/stop-view-display frame-id stop-view-label error-desc]]]
            frame-id
            (conj [:dispatch [::ddq/execute-callback-vec frame-id]])
            (and frame-id task-id)
            (conj [:dispatch [::ddq/finish-task frame-id task-id ::chart-datasets-result]]))})))