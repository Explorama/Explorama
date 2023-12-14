(ns de.explorama.profiling-tool.verticals.visualization
  (:require [de.explorama.profiling-tool.benchmark :refer [bench bench-report
                                                           benchmark-all
                                                           report->save]]
            [de.explorama.profiling-tool.config :refer [test-frame-id]]
            [de.explorama.profiling-tool.data.search :refer [context-2-local-filter
                                                             data-a-100k+-data-formdata-di data-a-formdata-di fact1-formdata-di fact1-local-filter
                                                             notes-formdata-di]]
            [de.explorama.profiling-tool.env :refer [create-ws-with-user
                                                     wait-for-result]]
            [de.explorama.shared.visualization.ws-api :as ws-api]
            [taoensso.timbre :refer [info]]))

(defn- build-table-payload [{:keys [di local-filter sorting-options
                                    page-size curr-page event-id
                                    data-acs? di-descs? data-keys?
                                    options?]
                             :or {page-size 1000
                                  curr-page 1}}]
  {:frame-id test-frame-id
   :di di
   :vis-type :table
   :local-filter local-filter
   :calc {:data-keys? data-keys?
          :options? options?
          :di-desc? di-descs?
          :data-acs? data-acs?
          :external-refs? false}
   :sorting sorting-options
   :page-size page-size
   :current-page curr-page
   :focus-event-id event-id})

(defn- send-table-data-request [{:keys [result-atom send-fn]}
                                request-params]
  (let [request-payload (build-table-payload request-params)]
    (send-fn
     [ws-api/table-data
      {:client-callback [:result]
       :failed-callback [:error]}
      request-payload])
    (let [result (wait-for-result result-atom)]
      result)))

(defn- post-process-table-data-response [[response]]
  (let [r (select-keys response [:data-count :filtered-count :last-page :filtered-data-keys])]
    (assoc r :median-page (int (/ (:last-page r) 2)))))

(def ^:private fact1-sorting-desc {:direction :desc
                                   :attr "fact1"})
(def ^:private event-type-sorting-desc {:direction :desc
                                        :attr "event-type"})
(def ^:private country-sorting-desc {:direction :desc
                                     :attr "country"})

(def ^:private fact1-sorting [fact1-sorting-desc])
(def ^:private country-sorting [country-sorting-desc])
(def ^:private event-type-sorting [event-type-sorting-desc])
(def ^:private fact1-event-type-sorting [fact1-sorting-desc
                                         event-type-sorting-desc])
(def ^:private country-event-type-fact1-sorting [country-sorting-desc
                                                 event-type-sorting-desc
                                                 fact1-sorting-desc])

(defn- benchmark-data-a-table-data [ws-con]
  (let [bench-data-a-formdata "data-a, 500/page, page 1, table data"
        bench-data-a-formdata-median-page "data-a, 500/page, median page, table data"
        bench-data-a-formdata-last-page "data-a, 500/page, last page, table data"
        bench-data-a-formdata-fact1-sorting "data-a, 500/page, sort by (fact1), table data"
        bench-data-a-formdata-country-sorting "data-a, 500/page, sort by (country), table data"
        bench-data-a-formdata-event-type-sorting "data-a, 500/page, sort by (event-type), table data"
        bench-data-a-formdata-fact1-event-type-sorting "data-a, 500/page, sort by (fact1, event-type), table data"
        bench-data-a-formdata-country-event-type-fact1-sorting "data-a, 500/page, sort by (country, event-type, fact1), table data"

        _ (info "Request data-a data")
        {data-a-data-count :data-count
         data-a-median-page :median-page
         data-a-last-page :last-page} (post-process-table-data-response
                                       (send-table-data-request
                                        ws-con
                                        {:di data-a-formdata-di
                                         :data-acs? false
                                         :di-descs? false
                                         :data-keys? true}))]
    (bench bench-data-a-formdata
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-median-page
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page data-a-median-page})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-last-page
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page data-a-last-page})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-fact1-sorting
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :sorting-options fact1-sorting})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-country-sorting
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :sorting-options country-sorting})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-event-type-sorting
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :sorting-options event-type-sorting})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-fact1-event-type-sorting
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :sorting-options fact1-event-type-sorting})
           {:num-of-executions 10})

    (bench bench-data-a-formdata-country-event-type-fact1-sorting
           []
           (partial send-table-data-request ws-con {:di data-a-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :sorting-options country-event-type-fact1-sorting})
           {:num-of-executions 10})

    data-a-data-count))

(defn- benchmark-data-a-100k+-table-data [ws-con]
  (let [bench-data-a-100k+-formdata "data-a 100k+, 500/page, page 1, table data"
        bench-data-a-100k+-formdata-median-page "data-a 100k+, 500/page, median page, table data"
        bench-data-a-100k+-formdata-last-page "data-a 100k+, 500/page, last page, table data"

        _ (info "Request data-a 100k+ data")
        {data-a-100k+-data-count :data-count
         data-a-100k+-median-page :median-page
         data-a-100k+-last-page :last-page} (post-process-table-data-response
                                             (send-table-data-request
                                              ws-con
                                              {:di data-a-100k+-data-formdata-di
                                               :data-acs? false
                                               :di-descs? false
                                               :data-keys? true}))]

    (bench bench-data-a-100k+-formdata
           []
           (partial send-table-data-request ws-con {:di data-a-100k+-data-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-formdata-median-page
           []
           (partial send-table-data-request ws-con {:di data-a-100k+-data-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page data-a-100k+-median-page})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-formdata-last-page
           []
           (partial send-table-data-request ws-con {:di data-a-100k+-data-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page data-a-100k+-last-page})
           {:num-of-executions 10})

    data-a-100k+-data-count))

(defn- benchmark-fact1-table-data [ws-con]
  (let [bench-fact1-formdata "fact1 not nil, 500/page, page 1, table data"
        bench-fact1-formdata-median-page "fact1 not nil, 500/page, median page, table data"
        bench-fact1-formdata-last-page "fact1 not nil, 500/page, last page, table data"

        {fact1-data-count :data-count
         fact1-median-page :median-page
         fact1-last-page :last-page} (post-process-table-data-response
                                      (send-table-data-request
                                       ws-con
                                       {:di fact1-formdata-di
                                        :data-acs? false
                                        :di-descs? false
                                        :data-keys? true}))]

    (bench bench-fact1-formdata
           []
           (partial send-table-data-request ws-con {:di fact1-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false})
           {:num-of-executions 10})

    (bench bench-fact1-formdata-median-page
           []
           (partial send-table-data-request ws-con {:di fact1-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page fact1-median-page})
           {:num-of-executions 10})

    (bench bench-fact1-formdata-last-page
           []
           (partial send-table-data-request ws-con {:di fact1-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page fact1-last-page})
           {:num-of-executions 10})

    fact1-data-count))

(defn- benchmark-notes-table-data [ws-con]
  (let [bench-notes-formdata "Notes includes bom, 500/page, page 1, table data"
        bench-notes-formdata-median-page "Notes includes bom, 500/page, median page, table data"
        bench-notes-formdata-last-page "Notes includes bom, 500/page, last page, table data"

        _ (info "Request notes includes bom data")
        {note-data-count :data-count
         note-median-page :median-page
         note-last-page :last-page} (post-process-table-data-response
                                     (send-table-data-request ws-con
                                                              {:di notes-formdata-di
                                                               :data-acs? false
                                                               :di-descs? false
                                                               :data-keys? true}))]
    (bench bench-notes-formdata
           []
           (partial send-table-data-request ws-con {:di notes-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false})
           {:num-of-executions 10})

    (bench bench-notes-formdata-median-page
           []
           (partial send-table-data-request ws-con {:di notes-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page note-median-page})
           {:num-of-executions 10})

    (bench bench-notes-formdata-last-page
           []
           (partial send-table-data-request ws-con {:di notes-formdata-di
                                                    :data-acs? false
                                                    :di-descs? false
                                                    :data-keys? false
                                                    :curr-page note-last-page})
           {:num-of-executions 10})

    note-data-count))

(defn benchmark-table-data []
  (let [{:keys [close-fn]
         :as ws-con} (create-ws-with-user "visualization" (atom nil))

        _ (info "Run data-a 100k+ table data benchmark")
        data-a-100k+-data-count (benchmark-data-a-100k+-table-data ws-con)

        _ (info "Run notes table data benchmark")
        note-data-count (benchmark-notes-table-data ws-con)

        _ (info "Run data-a table data benchmark")
        data-a-data-count (benchmark-data-a-table-data ws-con)

        _ (info "Run fact1 table data benchmark")
        fact1-data-count (benchmark-fact1-table-data ws-con)]

    (close-fn)

    (assoc (bench-report)
           :name "request table data"
           :service "visualization"
           :data-a-di data-a-formdata-di
           :data-a-100k+-di data-a-100k+-data-formdata-di
           :fact1-di fact1-formdata-di
           :notes-di notes-formdata-di
           :fact1-sorting fact1-sorting
           :country-sorting country-sorting
           :event-type-sorting event-type-sorting
           :fact1-event-type-sorting fact1-event-type-sorting
           :country-event-type-fact1-sorting country-event-type-fact1-sorting
           "data-a, Country selection to get 100k+ -> data-count" data-a-100k+-data-count
           "fact1, not empty -> data-count" fact1-data-count
           "data-a -> data-count" data-a-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(def ^:private sum-by-event-type-filter [])

(defn- build-chart-desc [{:keys [di chart-type y-axis x-axis
                                 sum-remaining? r-attr
                                 sum-by sum-filter
                                 aggregation-method
                                 attributes stopping-attributes
                                 stemming-attributes min-occurence]
                          :or {aggregation-method :sum
                               sum-by "all"}}]
  (cond-> {:di di
           :type chart-type}
    (not= chart-type :wordcloud) (assoc :aggregation-method aggregation-method
                                        :sum-by sum-by
                                        :sum-filter sum-filter)
    (= chart-type :wordcloud) (assoc :attributes attributes
                                     :stopping-attributes stopping-attributes
                                     :stemming-attributes stemming-attributes
                                     :min-occurence min-occurence)
    y-axis (assoc :y-axis y-axis)
    x-axis (assoc :x-axis x-axis)
    r-attr (assoc :r-attr r-attr)
    sum-remaining? (assoc :sum-remaining? sum-remaining?)))

(defn- build-charts-payload [{:keys [di local-filter
                                     data-acs? di-descs? data-keys?
                                     options? datasets?
                                     charts]}]
  {:frame-id test-frame-id
   :di di
   :vis-type :charts
   :local-filter local-filter
   :calc {:data-keys? data-keys?
          :options? options?
          :di-desc? di-descs?
          :data-acs? data-acs?
          :datasets? datasets?
          :external-refs? false}
   :operations-state {} ;not sure if i can ignore this
   :charts charts})

(defn- send-charts-dataset-request [{:keys [result-atom send-fn]}
                                    request-params]
  (let [request-payload (build-charts-payload request-params)]
    (send-fn
     [ws-api/chart-datasets
      {:client-callback [:result]
       :failed-callback [:error]}
      request-payload])
    (let [result (wait-for-result result-atom)]
      result)))

(defn- benchmark-line-chart [ws-con]
  (let [bench-data-a-100k+-year "data-a 100k+, x: Year, line chart datasets"
        bench-data-a-100k+-year-local-filter-fact1 "data-a 100k+, Filter fact1, x: Year, line chart datasets"
        bench-data-a-100k+-year-local-filter-event-type "data-a 100k+, Filter event-type, x: Year, line chart datasets"
        bench-data-a-100k+-country "data-a 100k+, x: country, line chart datasets"
        bench-data-a-100k+-year-sum-event-type "data-a 100k+, x: Year, sum by event-type, line chart datasets"
        bench-notes-year "notes includes bom, x: Year, line chart datasets"
        bench-notes-year-local-filter-fact1 "notes includes bom, Filter fact1, x: Year, line chart datasets"
        bench-notes-year-local-filter-event-type "notes includes bom, Filter event-type, x: Year, line chart datasets"
        bench-notes-country "notes includes bom, x: country, line chart datasets"
        bench-notes-year-sum-event-type "notes includes bom, x: Year, sum by event-type, line chart datasets"]

    (bench bench-data-a-100k+-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-local-filter-fact1
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :local-filter fact1-local-filter
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-local-filter-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :local-filter context-2-local-filter
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})

    (bench bench-notes-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-local-filter-fact1
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :local-filter fact1-local-filter
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-local-filter-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :local-filter context-2-local-filter
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-notes-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :line
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})))

(defn- benchmark-scatter-chart [ws-con]
  (let [bench-data-a-100k+-year "data-a 100k+, x: Year, scatter chart datasets"
        bench-data-a-100k+-country "data-a 100k+, x: country, scatter chart datasets"
        bench-data-a-100k+-year-sum-event-type "data-a 100k+, x: Year, sum by event-type, scatter chart datasets"
        bench-notes-year "notes includes bom, x: Year, scatter chart datasets"
        bench-notes-country "notes includes bom, x: country, scatter chart datasets"
        bench-notes-year-sum-event-type "notes includes bom, x: Year, sum by event-type, scatter chart datasets"]

    (bench bench-data-a-100k+-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})


    (bench bench-notes-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-notes-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})))

(defn- benchmark-bar-chart [ws-con]
  (let [bench-data-a-100k+-year "data-a 100k+, x: Year, bar chart datasets"
        bench-data-a-100k+-country "data-a 100k+, x: country, bar chart datasets"
        bench-data-a-100k+-year-sum-event-type "data-a 100k+, x: Year, sum by event-type, bar chart datasets"
        bench-notes-year "notes includes bom, x: Year, bar chart datasets"
        bench-notes-country "notes includes bom, x: country, bar chart datasets"
        bench-notes-year-sum-event-type "notes includes bom, x: Year, sum by event-type, bar chart datasets"]

    (bench bench-data-a-100k+-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})


    (bench bench-notes-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-notes-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})))

(defn- benchmark-bubble-chart [ws-con]
  (let [bench-data-a-100k+-year "data-a 100k+, x: Year r: number-of-events, bubble chart datasets"
        bench-data-a-100k+-country "data-a 100k+, x: country r: number-of-events, bubble chart datasets"
        bench-data-a-100k+-year-sum-event-type "data-a 100k+, x: Year r: number-of-events, sum by event-type, bubble chart datasets"
        bench-notes-year "notes includes bom, x: Year r: number-of-events, bubble chart datasets"
        bench-notes-country "notes includes bom, x: country r: number-of-events, bubble chart datasets"
        bench-notes-year-sum-event-type "notes includes bom, x: Year r: number-of-events, sum by event-type, bubble chart datasets"]

    (bench bench-data-a-100k+-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :r-attr :number-of-events
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})

    (bench bench-notes-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-notes-country
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "country"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "year"
                                                 :y-axis "fact1"
                                                 :r-attr :number-of-events
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})))

(defn- benchmark-pie-chart [ws-con]
  (let [bench-data-a-100k+-year "data-a 100k+, y: fact1, pie chart datasets"
        bench-data-a-100k+-year-sum-event-type "data-a 100k+, y: fact1, sum by event-type, pie chart datasets"
        bench-data-a-100k+-year-sum-event-type-sum-rest "data-a 100k+, y: fact1, sum by event-type, sum rest, pie chart datasets"
        bench-notes-year "notes includes bom, y: fact1, pie chart datasets"
        bench-notes-year-sum-event-type "notes includes bom, y: fact1, sum by event-type, pie chart datasets"
        bench-notes-year-sum-event-type-sum-rest "notes includes bom, y: fact1, sum by event-type, sum rest, pie chart datasets"]

    (bench bench-data-a-100k+-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :pie
                                                 :y-axis "fact1"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :pie
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-sum-event-type-sum-rest
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :pie
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter (vec (take 3 sum-by-event-type-filter))
                                                 :sum-remaining? true})]})
           {:num-of-executions 10})


    (bench bench-notes-year
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :pie
                                                 :y-axis "fact1"})]})
           {:num-of-executions 10})

    (bench bench-notes-year-sum-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :pie
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter sum-by-event-type-filter})]})
           {:num-of-executions 10})

    (bench bench-notes-year-sum-event-type-sum-rest
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di notes-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di notes-formdata-di
                                                 :chart-type :pie
                                                 :y-axis "fact1"
                                                 :sum-by "event-type"
                                                 :sum-filter (vec (take 3 sum-by-event-type-filter))
                                                 :sum-remaining? true})]})
           {:num-of-executions 10})))

(defn- benchmark-wordcloud [ws-con]
  (let [bench-data-a-100k+-all "data-a 100k+, attributes = [:all], wordcload chart datasets"
        bench-data-a-100k+-all-stemming-event-type "data-a 100k+, attributes = [:all], stemming #{event-type}, wordcload chart datasets"
        bench-data-a-100k+-all-stemming-note "data-a 100k+, attributes = [:all], stemming #{note}, wordcload chart datasets"
        bench-data-a-100k+-all-stopping-event-type "data-a 100k+, attributes = [:all], stopping #{event-type}, wordcload chart datasets"
        bench-data-a-100k+-all-stopping-note "data-a 100k+, attributes = [:all], stopping #{note}, wordcload chart datasets"
        bench-data-a-100k+-all-stemming-stopping-event-type "data-a 100k+, attributes = [:all], stemming and stopping #{event-type}, wordcload chart datasets"
        bench-data-a-100k+-all-stemming-stopping-note "data-a 100k+, attributes = [:all], stemming and stopping #{note}, wordcload chart datasets"
        bench-notes-all "notes includes bom, attributes = [:all], wordcload chart datasets"
        bench-notes-all-stemming-event-type "notes includes bom, attributes = [:all], stemming #{event-type}, wordcload chart datasets"
        bench-notes-all-stemming-note "notes includes bom, attributes = [:all], stemming #{note}, wordcload chart datasets"
        bench-notes-all-stopping-event-type "notes includes bom, attributes = [:all], stopping #{event-type}, wordcload chart datasets"
        bench-notes-all-stopping-note "notes includes bom, attributes = [:all], stopping #{note}, wordcload chart datasets"
        bench-notes-all-stemming-stopping-event-type "notes includes bom, attributes = [:all], stemming and stopping #{event-type}, wordcload chart datasets"
        bench-notes-all-stemming-stopping-note "notes includes bom, attributes = [:all], stemming and stopping #{note}, wordcload chart datasets"]

    (bench bench-data-a-100k+-all
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{}
                                                 :stemming-attributes #{}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-all-stemming-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{}
                                                 :stemming-attributes #{"event-type"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-all-stemming-note
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{}
                                                 :stemming-attributes #{"note"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-all-stopping-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"event-type"}
                                                 :stemming-attributes #{}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-all-stopping-note
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"note"}
                                                 :stemming-attributes #{}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-all-stemming-stopping-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"event-type"}
                                                 :stemming-attributes #{"event-type"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-all-stemming-stopping-note
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"note"}
                                                 :stemming-attributes #{"note"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{}
                                                 :stemming-attributes #{}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all-stemming-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{}
                                                 :stemming-attributes #{"event-type"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all-stemming-note
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{}
                                                 :stemming-attributes #{"note"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all-stopping-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"event-type"}
                                                 :stemming-attributes #{}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all-stopping-note
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"note"}
                                                 :stemming-attributes #{}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all-stemming-stopping-event-type
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"event-type"}
                                                 :stemming-attributes #{"event-type"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})

    (bench bench-notes-all-stemming-stopping-note
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? false
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :wordcloud
                                                 :attributes [:all]
                                                 :stopping-attributes #{"note"}
                                                 :stemming-attributes #{"note"}
                                                 :min-occurence 1})]})
           {:num-of-executions 10})))

(defn- benchmark-combine-charts [ws-con]
  (let [bench-data-a-100k+-year-line-scatter "data-a 100k+, x: Year, line + scatter chart datasets"
        bench-data-a-100k+-year-line-bar "data-a 100k+, x: Year, line + bar chart datasets"
        bench-data-a-100k+-year-line-bubble "data-a 100k+, x: Year, line + bubble chart datasets"
        bench-data-a-100k+-year-line-line "data-a 100k+, x: Year, line + line chart datasets"
        bench-data-a-100k+-year-bubble-scatter "data-a 100k+, x: Year, bubble + scatter chart datasets"
        bench-data-a-100k+-year-bar-bar "data-a 100k+, x: Year, bar + bar chart datasets"]

    (bench bench-data-a-100k+-year-line-scatter
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})
                              (build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-line-bar
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})
                              (build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-line-bubble
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})
                              (build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-line-line
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})
                              (build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :line
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-bubble-scatter
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bubble
                                                 :x-axis "year"})
                              (build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :scatter
                                                 :x-axis "year"})]})
           {:num-of-executions 10})

    (bench bench-data-a-100k+-year-bar-bar
           []
           (partial send-charts-dataset-request
                    ws-con
                    {:di data-a-100k+-data-formdata-di
                     :datasets? true
                     :options? true
                     :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"})
                              (build-chart-desc {:di data-a-100k+-data-formdata-di
                                                 :chart-type :bar
                                                 :x-axis "year"})]})
           {:num-of-executions 10})))

(defn benchmark-charts-dataset []
  (let [{:keys [close-fn]
         :as ws-con} (create-ws-with-user "visualization" (atom nil))
        _ (info "Request data-a 100k+ data")
        data-a-100k+-data-count (get-in (send-charts-dataset-request ws-con
                                                                     {:di data-a-100k+-data-formdata-di
                                                                      :datasets? false
                                                                      :options? true
                                                                      :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                                                                  :chart-type :line
                                                                                                  :x-axis "year"})]})
                                        [1 :filtered-count])

        _ (info "Request data-a 100k+ local filter fact1 data")
        data-a-100k+local-filter-fact1-data-count (get-in (send-charts-dataset-request ws-con
                                                                                       {:di data-a-100k+-data-formdata-di
                                                                                        :local-filter fact1-local-filter
                                                                                        :datasets? false
                                                                                        :options? true
                                                                                        :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                                                                                    :chart-type :line
                                                                                                                    :x-axis "year"})]})
                                                          [1 :filtered-count])

        _ (info "Request data-a 100k+ local filter event-type data")
        data-a-100k+-local-filter-event-type-data-count (get-in (send-charts-dataset-request ws-con
                                                                                             {:di data-a-100k+-data-formdata-di
                                                                                              :local-filter context-2-local-filter
                                                                                              :datasets? false
                                                                                              :options? true
                                                                                              :charts [(build-chart-desc {:di data-a-100k+-data-formdata-di
                                                                                                                          :chart-type :line
                                                                                                                          :x-axis "year"})]})
                                                                [1 :filtered-count])

        _ (info "Request Note includes bom data")
        note-data-count (get-in (send-charts-dataset-request ws-con
                                                             {:di notes-formdata-di
                                                              :datasets? false
                                                              :options? true
                                                              :charts [(build-chart-desc {:di notes-formdata-di
                                                                                          :chart-type :line
                                                                                          :x-axis "year"})]})
                                [1 :filtered-count])

        _ (info "Request Note includes bom local filter fact1 data")
        note+local-filter-fact1-data-count (get-in (send-charts-dataset-request ws-con
                                                                                {:di notes-formdata-di
                                                                                 :local-filter fact1-local-filter
                                                                                 :datasets? false
                                                                                 :options? true
                                                                                 :charts [(build-chart-desc {:di notes-formdata-di
                                                                                                             :chart-type :line
                                                                                                             :x-axis "year"})]})
                                                   [1 :filtered-count])

        _ (info "Request Note includes bom local filter event-type data")
        note-local-filter-event-type-data-count (get-in (send-charts-dataset-request ws-con
                                                                                     {:di notes-formdata-di
                                                                                      :local-filter context-2-local-filter
                                                                                      :datasets? false
                                                                                      :options? true
                                                                                      :charts [(build-chart-desc {:di notes-formdata-di
                                                                                                                  :chart-type :line
                                                                                                                  :x-axis "year"})]})
                                                        [1 :filtered-count])]

    (info "Run line chart benchmark")
    (benchmark-line-chart ws-con)

    (info "Run scatter chart benchmark")
    (benchmark-scatter-chart ws-con)

    (info "Run bar chart benchmark")
    (benchmark-bar-chart ws-con)

    (info "Run bubble chart benchmark")
    (benchmark-bubble-chart ws-con)

    (info "Run pie chart benchmark")
    (benchmark-pie-chart ws-con)

    (info "Run wordcload benchmark")
    (benchmark-wordcloud ws-con)

    (info "Run chart combination benchmark")
    (benchmark-combine-charts ws-con)

    (close-fn)

    (assoc (bench-report)
           :name "request charts datasets"
           :service "visualization"
           :sum-by-event-type-filter sum-by-event-type-filter
           :data-a-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           :event-type-filter context-2-local-filter
           :fact1-filter fact1-local-filter
           "data-a, Country selection to get 100k+ -> data-count" data-a-100k+-data-count
           "data-a, Country selection to get 100k+, filtered by fact1 -> data-count" data-a-100k+local-filter-fact1-data-count
           "data-a, Country selection to get 100k+, filtered by event-type -> data-count" data-a-100k+-local-filter-event-type-data-count
           "Notes, includes bom -> data-count" note-data-count
           "Notes, includes bom, filtered by fact1 -> data-count" note+local-filter-fact1-data-count
           "Notes, includes bom, filtered by event-type -> data-count" note-local-filter-event-type-data-count)))

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save (benchmark-table-data))
         (report->save (benchmark-charts-dataset)))
     (report->save
      (benchmark-all benchmark-table-data
                     benchmark-charts-dataset)))))
