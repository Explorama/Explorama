(ns de.explorama.profiling-tool.verticals.mosaic
  (:require [de.explorama.profiling-tool.env :refer [wait-for-result create-ws-with-user]]
            [de.explorama.profiling-tool.benchmark :refer [bench
                                                           bench-report benchmark-all
                                                           report->save]]
            [de.explorama.profiling-tool.data.mosaic :refer [raw-layouts default-sort-grp
                                                             sort-grp-by-fact1-min
                                                             sort-grp-by-fact1-max
                                                             sort-grp-by-fact1-sum
                                                             sort-grp-by-event-count
                                                             sort-by-fact1-asc
                                                             sort-by-fact1-desc
                                                             sort-by-context-2-asc
                                                             sort-by-context-2-desc
                                                             sort-by-country-asc
                                                             sort-by-country-desc]]
            [de.explorama.profiling-tool.data.search :refer [notes-formdata-di
                                                             data-a-100k+-data-formdata-di
                                                             fact1-local-filter
                                                             context-2-local-filter]]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [taoensso.timbre :refer [info]]))

(defn- operations-desc [{:keys [grp-key sort-grp type x-axis y-axis
                                sub-grp-key sort-sub-grp sort]
                         :or {type :raster
                              sort-grp default-sort-grp
                              sort-sub-grp default-sort-grp
                              x-axis "context-2"
                              y-axis "fact1"}}]
  (cond-> {:type type}
    sort (assoc :sort sort)
    grp-key (assoc :grp-key grp-key
                   :sort-grp sort-grp)
    sub-grp-key (assoc :sub-grp-key sub-grp-key
                       :sort-sub-grp sort-sub-grp)
    (= type :scatter) (assoc :x x-axis
                             :y y-axis
                             :client-dims
                             {:width 596,
                              :height 508,
                              :card-width 520,
                              :card-height 552,
                              :card-margin 30})))

(defn- operation-payload [{:keys [di operations-desc local-filter
                                  send-data-acs? update-usable-layouts?
                                  new-di? validate-operations-desc? layouts]}]
  (cond->
   {:di di
    :operations-desc operations-desc
    :send-data-acs? send-data-acs?
    :update-usable-layouts? update-usable-layouts?
    :new-di? new-di?
    :validate-operations-desc? validate-operations-desc?
    :lang :en-GB}
    local-filter (assoc :local-filter local-filter)
    layouts (assoc :layouts layouts)
    (not layouts) (assoc :raw-layouts raw-layouts)))

(defn- send-operation [{:keys [result-atom send-fn]}
                       {:keys [send-data-acs?]
                        :as operation-params}]
  (let [payload (operation-payload operation-params)]
    (send-fn
     [ws-api/operations-route
      {:client-callback [:result]
       :custom {:data-acs-async-callback [:acs]}}
      payload])
    (let [[_ _ {error-desc :error-desc
                data-count :data-count
                filtered-data-count :filtered-data-count
                data :data}]
          (wait-for-result result-atom)
          di-acs (when send-data-acs?
                   (wait-for-result result-atom :acs))]
      {:di-acs di-acs
       :events data
       :error? error-desc
       :data-count data-count
       :filtered-data-count filtered-data-count})))

(defn benchmark-init-connect []
  (let [bench-data-100k+ "data-a 100k+, init connect, get data-acs, mosaic Opertation"
        bench-notes "notes includes bom, init connect, get data-acs, mosaic Opertation"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        data-100k+-data-count (get (send-operation ws-con
                                                   {:di data-a-100k+-data-formdata-di
                                                    :send-data-acs? false
                                                    :operations-desc (operations-desc {})})
                                   :data-count)

        _ (info "Request Note includes bom data")
        note-data-count (get (send-operation ws-con
                                             {:di notes-formdata-di
                                              :send-data-acs? false
                                              :operations-desc (operations-desc {})})
                             :data-count)]

    (bench bench-data-100k+
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? true
                                           :operations-desc {}})
           {:num-of-executions 25})

    (bench bench-notes
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? true
                                           :operations-desc {}})
           {:num-of-executions 25})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic init-connect"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(defn benchmark-group []
  (let [bench-data-100k+-group-by-country "data-a 100k+, group by country, mosaic Opertation"
        bench-data-100k+-group-by-context-2 "data-a 100k+, group by context-2, mosaic Opertation"
        bench-data-100k+-group-by-year "data-a 100k+, group by year, mosaic Opertation"
        bench-data-100k+-filter-fact1-group-by-country "data-a 100k+, Filter fact1, group by country, mosaic Opertation"
        bench-data-100k+-filter-context-2-group-by-context-2 "data-a 100k+, Filter context-2 group by context-2, mosaic Opertation"
        bench-data-100k+-filter-fact1-group-by-year "data-a 100k+, Filter fact1, group by year, mosaic Opertation"

        bench-notes-group-by-country "notes includes bom, group by country, mosaic Opertation"
        bench-notes-group-by-context-2 "notes includes bom, group by context-2, mosaic Opertation"
        bench-notes-group-by-year "notes includes bom, group by year, mosaic Opertation"
        bench-notes-filter-fact1-group-by-country "notes includes bom, group by country, mosaic Opertation"
        bench-notes-filter-context-2-group-by-context-2 "notes includes bom, Filter context-2 group by context-2, mosaic Opertation"
        bench-notes-filter-fact1-group-by-year "notes includes bom, Filter fact1, group by year, mosaic Opertation"

        group-by-country-operation (operations-desc {:grp-key "country"})
        group-by-context-2-operation (operations-desc {:grp-key "context-2"})
        group-by-year-operation (operations-desc {:grp-key "year"})

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        data-100k+-data-count (get (send-operation ws-con
                                                   {:di data-a-100k+-data-formdata-di
                                                    :send-data-acs? false
                                                    :operations-desc (operations-desc {})})
                                   :data-count)

        _ (info "Request data-a 100k+ local filter fact1 data")
        data-100k+local-filter-fact1-data-count (get (send-operation ws-con
                                                                     {:di data-a-100k+-data-formdata-di
                                                                      :local-filter fact1-local-filter
                                                                      :send-data-acs? false
                                                                      :operations-desc (operations-desc {})})
                                                     :filtered-data-count)

        _ (info "Request data-a 100k+ local filter context-2 data")
        data-100k+-local-filter-context-2-data-count (get (send-operation ws-con
                                                                          {:di data-a-100k+-data-formdata-di
                                                                           :local-filter context-2-local-filter
                                                                           :send-data-acs? false
                                                                           :operations-desc (operations-desc {})})
                                                          :filtered-data-count)

        _ (info "Request Note includes bom data")
        note-data-count (get (send-operation ws-con
                                             {:di notes-formdata-di
                                              :send-data-acs? false
                                              :operations-desc (operations-desc {})})
                             :data-count)

        _ (info "Request Note includes bom local filter fact1 data")
        note+local-filter-fact1-data-count (get (send-operation ws-con
                                                                {:di notes-formdata-di
                                                                 :local-filter fact1-local-filter
                                                                 :send-data-acs? false
                                                                 :operations-desc (operations-desc {})})
                                                :filtered-data-count)

        _ (info "Request Note includes bom local filter context-2 data")
        note-local-filter-context-2-data-count (get (send-operation ws-con
                                                                    {:di notes-formdata-di
                                                                     :local-filter context-2-local-filter
                                                                     :send-data-acs? false
                                                                     :operations-desc (operations-desc {})})
                                                    :filtered-data-count)]

    (bench bench-data-100k+-group-by-country
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-context-2
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-year
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-filter-fact1-group-by-country
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-country-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-filter-context-2-group-by-context-2
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :local-filter context-2-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-filter-fact1-group-by-year
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-year-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-country
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-context-2
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-year
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-operation})
           {:num-of-executions 10})

    (bench bench-notes-filter-fact1-group-by-country
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-country-operation})
           {:num-of-executions 10})

    (bench bench-notes-filter-context-2-group-by-context-2
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :local-filter context-2-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-operation})
           {:num-of-executions 10})

    (bench bench-notes-filter-fact1-group-by-year
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-year-operation})
           {:num-of-executions 10})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic group operation"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           :context-2-filter context-2-local-filter
           :fact1-filter fact1-local-filter
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "data-a, Country selection to get 100k+, filtered by fact1 -> data-count" data-100k+local-filter-fact1-data-count
           "data-a, Country selection to get 100k+, filtered by context-2 -> data-count" data-100k+-local-filter-context-2-data-count
           "Notes, includes bom -> data-count" note-data-count
           "Notes, includes bom, filtered by fact1 -> data-count" note+local-filter-fact1-data-count
           "Notes, includes bom, filtered by context-2 -> data-count" note-local-filter-context-2-data-count)))

(defn benchmark-group-sort-grp []
  (let [bench-data-100k+-group-by-country-sort-by-fact1-max "data-a 100k+, group by country, Sort by fact1 max, mosaic Opertation"
        bench-data-100k+-group-by-context-2-sort-by-fact1-max "data-a 100k+, group by context-2, Sort by fact1 max, mosaic Opertation"
        bench-data-100k+-group-by-year-sort-by-fact1-max "data-a 100k+, group by year, Sort by fact1 max, mosaic Opertation"

        bench-data-100k+-group-by-country-sort-by-fact1-min "data-a 100k+, group by country, Sort by fact1 min, mosaic Opertation"
        bench-data-100k+-group-by-context-2-sort-by-fact1-min "data-a 100k+, group by context-2, Sort by fact1 min, mosaic Opertation"
        bench-data-100k+-group-by-year-sort-by-fact1-min "data-a 100k+, group by year, Sort by fact1 min, mosaic Opertation"

        bench-data-100k+-group-by-country-sort-by-fact1-sum "data-a 100k+, group by country, Sort by fact1 sum, mosaic Opertation"
        bench-data-100k+-group-by-context-2-sort-by-fact1-sum "data-a 100k+, group by context-2, Sort by fact1 sum, mosaic Opertation"
        bench-data-100k+-group-by-year-sort-by-fact1-sum "data-a 100k+, group by year, Sort by fact1 sum, mosaic Opertation"

        bench-notes-group-by-country-sort-by-event-count "notes includes bom, group by country, Sort by event-count, mosaic Opertation"
        bench-notes-group-by-context-2-sort-by-event-count "notes includes bom, group by context-2, Sort by event-count, mosaic Opertation"
        bench-notes-group-by-year-sort-by-event-count "notes includes bom, group by year, Sort by event-count, mosaic Opertation"

        group-by-country-sort-by-fact1-max-operation (operations-desc {:grp-key "country"
                                                                       :sort-grp sort-grp-by-fact1-max})
        group-by-context-2-sort-by-fact1-max-operation (operations-desc {:grp-key "context-2"
                                                                         :sort-grp sort-grp-by-fact1-max})
        group-by-year-sort-by-fact1-max-operation (operations-desc {:grp-key "year"
                                                                    :sort-grp sort-grp-by-fact1-max})

        group-by-country-sort-by-fact1-min-operation (operations-desc {:grp-key "country"
                                                                       :sort-grp sort-grp-by-fact1-min})
        group-by-context-2-sort-by-fact1-min-operation (operations-desc {:grp-key "context-2"
                                                                         :sort-grp sort-grp-by-fact1-min})
        group-by-year-sort-by-fact1-min-operation (operations-desc {:grp-key "year"
                                                                    :sort-grp sort-grp-by-fact1-min})

        group-by-country-sort-by-fact1-sum-operation (operations-desc {:grp-key "country"
                                                                       :sort-grp sort-grp-by-fact1-sum})
        group-by-context-2-sort-by-fact1-sum-operation (operations-desc {:grp-key "context-2"
                                                                         :sort-grp sort-grp-by-fact1-sum})
        group-by-year-sort-by-fact1-sum-operation (operations-desc {:grp-key "year"
                                                                    :sort-grp sort-grp-by-fact1-sum})

        group-by-country-sort-by-event-count-operation (operations-desc {:grp-key "country"
                                                                         :sort-grp sort-grp-by-event-count})
        group-by-context-2-sort-by-event-count-operation (operations-desc {:grp-key "context-2"
                                                                           :sort-grp sort-grp-by-event-count})
        group-by-year-sort-by-event-count-operation (operations-desc {:grp-key "year"
                                                                      :sort-grp sort-grp-by-event-count})

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        data-100k+-data-count (get (send-operation ws-con
                                                   {:di data-a-100k+-data-formdata-di
                                                    :send-data-acs? false
                                                    :operations-desc (operations-desc {})})
                                   :data-count)

        _ (info "Request Note includes bom data")
        note-data-count (get (send-operation ws-con
                                             {:di notes-formdata-di
                                              :send-data-acs? false
                                              :operations-desc (operations-desc {})})
                             :data-count)]

    (bench bench-data-100k+-group-by-country-sort-by-fact1-max
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sort-by-fact1-max-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-context-2-sort-by-fact1-max
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sort-by-fact1-max-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-year-sort-by-fact1-max
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sort-by-fact1-max-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-country-sort-by-fact1-min
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sort-by-fact1-min-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-context-2-sort-by-fact1-min
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sort-by-fact1-min-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-year-sort-by-fact1-min
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sort-by-fact1-min-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-country-sort-by-fact1-sum
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sort-by-fact1-sum-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-context-2-sort-by-fact1-sum
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sort-by-fact1-sum-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-year-sort-by-fact1-sum
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sort-by-fact1-sum-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-country-sort-by-event-count
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sort-by-event-count-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-context-2-sort-by-event-count
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sort-by-event-count-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-year-sort-by-event-count
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sort-by-event-count-operation})
           {:num-of-executions 10})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic group and sort operation"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           :context-2-filter context-2-local-filter
           :fact1-filter fact1-local-filter
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(defn benchmark-sub-group []
  (let [bench-data-100k+-group-by-country-sub-grp-province "data-a 100k+, group by country, sub group by province, mosaic Opertation"
        bench-data-100k+-group-by-context-2-sub-grp-country "data-a 100k+, group by context-2, sub group by country, mosaic Opertation"
        bench-data-100k+-group-by-year-sub-grp-month "data-a 100k+, group by year, sub group by month, mosaic Opertation"

        bench-data-100k+-filter-fact1-group-by-country-sub-grp-province "data-a 100k+, Filter fact1, group by country, sub group by province, mosaic Opertation"
        bench-data-100k+-filter-context-2-group-by-context-2-sub-grp-country "data-a 100k+, Filter context-2 group by context-2, sub group by country, mosaic Opertation"
        bench-data-100k+-filter-fact1-group-by-year-sub-grp-month "data-a 100k+, Filter fact1, group by year, sub group by month, mosaic Opertation"


        bench-notes-group-by-country-sub-grp-province "notes includes bom, group by country, sub group by province, mosaic Opertation"
        bench-notes-group-by-context-2-sub-grp-country "notes includes bom, group by context-2, sub group by country, mosaic Opertation"
        bench-notes-group-by-year-sub-grp-month "notes includes bom, group by year, sub group by month, mosaic Opertation"

        bench-notes-filter-fact1-group-by-country-sub-grp-province "notes includes bom, group by country, sub group by province, mosaic Opertation"
        bench-notes-filter-context-2-group-by-context-2-sub-grp-country "notes includes bom, Filter context-2 group by context-2, sub group by country, mosaic Opertation"
        bench-notes-filter-fact1-group-by-year-sub-grp-month "notes includes bom, Filter fact1, group by year, sub group by month, mosaic Opertation"

        group-by-country-sub-grp-province-operation (operations-desc {:grp-key "country"
                                                                      :sub-grp-key "province"})
        group-by-context-2-sub-grp-country-operation (operations-desc {:grp-key "context-2"
                                                                       :sub-grp-key "country"})
        group-by-year-sub-grp-month-operation (operations-desc {:grp-key "year"
                                                                :sub-grp-key "month"})

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        data-100k+-data-count (get (send-operation ws-con
                                                   {:di data-a-100k+-data-formdata-di
                                                    :send-data-acs? false
                                                    :operations-desc (operations-desc {})})
                                   :data-count)

        _ (info "Request Note includes bom data")
        note-data-count (get (send-operation ws-con
                                             {:di notes-formdata-di
                                              :send-data-acs? false
                                              :operations-desc (operations-desc {})})
                             :data-count)]

    (bench bench-data-100k+-group-by-country-sub-grp-province
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sub-grp-country-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-context-2-sub-grp-country
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sub-grp-province-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-group-by-year-sub-grp-month
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sub-grp-month-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-filter-fact1-group-by-country-sub-grp-province
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sub-grp-country-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-filter-context-2-group-by-context-2-sub-grp-country
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :local-filter context-2-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sub-grp-province-operation})
           {:num-of-executions 10})

    (bench bench-data-100k+-filter-fact1-group-by-year-sub-grp-month
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sub-grp-month-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-country-sub-grp-province
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sub-grp-country-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-context-2-sub-grp-country
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sub-grp-province-operation})
           {:num-of-executions 10})

    (bench bench-notes-group-by-year-sub-grp-month
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sub-grp-month-operation})
           {:num-of-executions 10})

    (bench bench-notes-filter-fact1-group-by-country-sub-grp-province
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-context-2-sub-grp-country-operation})
           {:num-of-executions 10})

    (bench bench-notes-filter-context-2-group-by-context-2-sub-grp-country
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :local-filter context-2-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-country-sub-grp-province-operation})
           {:num-of-executions 10})

    (bench bench-notes-filter-fact1-group-by-year-sub-grp-month
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :local-filter fact1-local-filter
                                           :send-data-acs? false
                                           :operations-desc group-by-year-sub-grp-month-operation})
           {:num-of-executions 10})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic sub-group operation"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(defn benchmark-sort []
  (let [bench-data-100k+-sort-by-fact1-asc "data-a 100k+, sort by fact1 ascending, mosaic Opertation"
        bench-data-100k+-sort-by-fact1-desc "data-a 100k+, sort by fact1 descending, mosaic Opertation"
        bench-data-100k+-sort-by-context-2-asc "data-a 100k+, sort by context-2 ascending, mosaic Opertation"
        bench-data-100k+-sort-by-context-2-desc "data-a 100k+, sort by context-2 descending, mosaic Opertation"
        bench-data-100k+-sort-by-country-asc "data-a 100k+, sort by country ascending, mosaic Opertation"
        bench-data-100k+-sort-by-country-desc "data-a 100k+, sort by country descending, mosaic Opertation"

        bench-notes-sort-by-fact1-asc "notes includes bom, sort by fact1 ascending, mosaic Opertation"
        bench-notes-sort-by-fact1-desc "notes includes bom, sort by fact1 descending, mosaic Opertation"
        bench-notes-sort-by-context-2-asc "notes includes bom, sort by context-2 ascending, mosaic Opertation"
        bench-notes-sort-by-context-2-desc "notes includes bom, sort by context-2 descending, mosaic Opertation"
        bench-notes-sort-by-country-asc "notes includes bom, sort by country ascending, mosaic Opertation"
        bench-notes-sort-by-country-desc "notes includes bom, sort by country descending, mosaic Opertation"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        data-100k+-data-count (get (send-operation ws-con
                                                   {:di data-a-100k+-data-formdata-di
                                                    :send-data-acs? false
                                                    :operations-desc (operations-desc {})})
                                   :data-count)

        _ (info "Request Note includes bom data")
        note-data-count (get (send-operation ws-con
                                             {:di notes-formdata-di
                                              :send-data-acs? false
                                              :operations-desc (operations-desc {})})
                             :data-count)]

    (bench bench-data-100k+-sort-by-fact1-asc
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-fact1-asc})})
           {:num-of-executions 10})
    (bench bench-data-100k+-sort-by-fact1-desc
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-fact1-desc})})
           {:num-of-executions 10})
    (bench bench-data-100k+-sort-by-context-2-asc
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-context-2-asc})})
           {:num-of-executions 10})
    (bench bench-data-100k+-sort-by-context-2-desc
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-context-2-desc})})
           {:num-of-executions 10})
    (bench bench-data-100k+-sort-by-country-asc
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-country-asc})})
           {:num-of-executions 10})
    (bench bench-data-100k+-sort-by-country-desc
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-country-desc})})
           {:num-of-executions 10})

    (bench bench-notes-sort-by-fact1-asc
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-fact1-asc})})
           {:num-of-executions 10})
    (bench bench-notes-sort-by-fact1-desc
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-fact1-desc})})
           {:num-of-executions 10})
    (bench bench-notes-sort-by-context-2-asc
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-context-2-asc})})
           {:num-of-executions 10})
    (bench bench-notes-sort-by-context-2-desc
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-context-2-desc})})
           {:num-of-executions 10})
    (bench bench-notes-sort-by-country-asc
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-country-asc})})
           {:num-of-executions 10})
    (bench bench-notes-sort-by-country-desc
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:sort sort-by-country-desc})})
           {:num-of-executions 10})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic sort events operation"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(defn benchmark-scatter []
  (let [bench-data-100k+-scatter-country-fact1 "data-a 100k+, scatter, x: country y: fact1, mosaic Opertation"
        bench-data-100k+-scatter-country-context-2 "data-a 100k+, scatter, x: country y: context-2, mosaic Opertation"
        bench-data-100k+-scatter-organisation-fact1 "data-a 100k+, scatter, x: organisation y: fact1, mosaic Opertation"
        bench-data-100k+-scatter-date-context-2 "data-a 100k+, scatter, x: date y: context-2, mosaic Opertation"
        bench-data-100k+-scatter-date-fact1 "data-a 100k+, scatter, x: date y: fact1, mosaic Opertation"

        bench-notes-scatter-country-fact1 "notes includes bom, scatter, x: country y: fact1, mosaic Opertation"
        bench-notes-scatter-country-context-2 "notes includes bom, scatter, x: country y: context-2, mosaic Opertation"
        bench-notes-scatter-organisation-fact1 "notes includes bom, scatter, x: organisation y: fact1, mosaic Opertation"
        bench-notes-scatter-date-context-2 "notes includes bom, scatter, x: date y: context-2, mosaic Opertation"
        bench-notes-scatter-date-fact1 "notes includes bom, scatter, x: date y: fact1, mosaic Opertation"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        data-100k+-data-count (get (send-operation ws-con
                                                   {:di data-a-100k+-data-formdata-di
                                                    :send-data-acs? false
                                                    :operations-desc (operations-desc {})})
                                   :data-count)

        _ (info "Request Note includes bom data")
        note-data-count (get (send-operation ws-con
                                             {:di notes-formdata-di
                                              :send-data-acs? false
                                              :operations-desc (operations-desc {})})
                             :data-count)]

    (bench bench-data-100k+-scatter-country-fact1
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "country"
                                                                              :y-axis "fact1"})})
           {:num-of-executions 10})
    (bench bench-data-100k+-scatter-country-context-2
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "country"
                                                                              :y-axis "context-2"})})
           {:num-of-executions 10})
    (bench bench-data-100k+-scatter-organisation-fact1
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "organisation"
                                                                              :y-axis "fact1"})})
           {:num-of-executions 10})
    (bench bench-data-100k+-scatter-date-fact1
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "date"
                                                                              :y-axis "fact1"})})
           {:num-of-executions 10})
    (bench bench-data-100k+-scatter-date-context-2
           []
           (partial send-operation ws-con {:di data-a-100k+-data-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "date"
                                                                              :y-axis "context-2"})})
           {:num-of-executions 10})

    (bench bench-notes-scatter-country-fact1
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "country"
                                                                              :y-axis "fact1"})})
           {:num-of-executions 10})
    (bench bench-notes-scatter-country-context-2
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "country"
                                                                              :y-axis "context-2"})})
           {:num-of-executions 10})
    (bench bench-notes-scatter-organisation-fact1
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "organisation"
                                                                              :y-axis "fact1"})})
           {:num-of-executions 10})
    (bench bench-notes-scatter-date-fact1
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "date"
                                                                              :y-axis "fact1"})})
           {:num-of-executions 10})
    (bench bench-notes-scatter-date-context-2
           []
           (partial send-operation ws-con {:di notes-formdata-di
                                           :send-data-acs? false
                                           :operations-desc (operations-desc {:type :scatter
                                                                              :x-axis "date"
                                                                              :y-axis "context-2"})})
           {:num-of-executions 10})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic scatter operation"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(defn- request-get-events [{:keys [result-atom send-fn]}
                           event-ids]
  (send-fn
   [ws-api/get-events-route event-ids [:result]])
  (let [[_ event-details] (wait-for-result result-atom)]
    event-details))

(defn- event->event-id [[_ id bucket _]]
  [bucket id])

(defn benchmark-event-details []
  (let [bench-data-100k+-event-details-first "data-a 100k+, event details first, mosaic get-events"
        bench-data-100k+-event-details-mean "data-a 100k+, event details mean, mosaic get-events"
        bench-data-100k+-event-details-last "data-a 100k+, event details last, mosaic get-events"
        bench-data-100k+-event-details-all-three "data-a 100k+, event details all three, mosaic get-events"

        bench-notes-event-details-first "notes includes bom, event details first, mosaic get-events"
        bench-notes-event-details-mean "notes includes bom, event details mean, mosaic get-events"
        bench-notes-event-details-last "notes includes bom, event details last, mosaic get-events"
        bench-notes-event-details-all-three "notes includes bom, event details all three, mosaic get-events"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "mosaic" (atom nil))

        _ (info "Request data-a 100k+ data")
        {data-100k+-data-count :data-count
         data-100k+-events :events} (send-operation ws-con
                                                    {:di data-a-100k+-data-formdata-di
                                                     :send-data-acs? false
                                                     :operations-desc (operations-desc {})})
        data-100k+-first-event (event->event-id (first data-100k+-events))
        data-100k+-mean-event (event->event-id (nth data-100k+-events (int (/ data-100k+-data-count 2))))
        data-100k+-last-event (event->event-id (peek data-100k+-events))

        _ (info "Request Note includes bom data")
        {note-data-count :data-count
         notes-events :events} (send-operation ws-con
                                               {:di notes-formdata-di
                                                :send-data-acs? false
                                                :operations-desc (operations-desc {})})
        notes-first-event (event->event-id (first notes-events))
        notes-mean-event (event->event-id (nth notes-events (int (/ note-data-count 2))))
        notes-last-event (event->event-id (peek notes-events))]

    (bench bench-data-100k+-event-details-first
           []
           (partial request-get-events ws-con [data-100k+-first-event])
           {:num-of-executions 10})
    (bench bench-data-100k+-event-details-mean
           []
           (partial request-get-events ws-con [data-100k+-mean-event])
           {:num-of-executions 10})
    (bench bench-data-100k+-event-details-last
           []
           (partial request-get-events ws-con [data-100k+-last-event])
           {:num-of-executions 10})
    (bench bench-data-100k+-event-details-all-three
           []
           (partial request-get-events ws-con [data-100k+-first-event
                                               data-100k+-mean-event
                                               data-100k+-last-event])
           {:num-of-executions 10})

    (bench bench-notes-event-details-first
           []
           (partial request-get-events ws-con [notes-first-event])
           {:num-of-executions 10})
    (bench bench-notes-event-details-mean
           []
           (partial request-get-events ws-con [notes-mean-event])
           {:num-of-executions 10})
    (bench bench-notes-event-details-last
           []
           (partial request-get-events ws-con [notes-last-event])
           {:num-of-executions 10})
    (bench bench-notes-event-details-all-three
           []
           (partial request-get-events ws-con [notes-first-event
                                               notes-mean-event
                                               notes-last-event])
           {:num-of-executions 10})

    (close-fn)

    (assoc (bench-report)
           :name "request mosaic event-details"
           :service "mosaic"
           :data-100k+-di data-a-100k+-data-formdata-di
           :notes-di notes-formdata-di
           "data-a, Country selection to get 100k+ -> data-count" data-100k+-data-count
           "Notes, includes bom -> data-count" note-data-count)))

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save (benchmark-init-connect))
         (report->save (benchmark-group))
         (report->save (benchmark-group-sort-grp))
         (report->save (benchmark-sub-group))
         (report->save (benchmark-sort))
         (report->save (benchmark-event-details))
         (report->save (benchmark-scatter)))
     (report->save
      (benchmark-all benchmark-init-connect
                     benchmark-group
                     benchmark-group-sort-grp
                     benchmark-sub-group
                     benchmark-sort
                     benchmark-event-details
                     benchmark-scatter)))))
