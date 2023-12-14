(ns de.explorama.profiling-tool.verticals.map
  (:require [de.explorama.profiling-tool.benchmark :refer [bench bench-report
                                                           benchmark-all
                                                           report->save]]
            [de.explorama.profiling-tool.config :refer [test-frame-id]]
            [de.explorama.profiling-tool.data.map :refer [all-raw-feature-layers
                                                          all-raw-marker-layouts context-2-layout country-num-of-events fact1-country-sum-layer
                                                          fact1-layout fact1-province-sum-layer province-num-of-events raw-heatmap-layer-global
                                                          raw-heatmap-layer-local raw-movement-layer]]
            [de.explorama.profiling-tool.data.search :refer [context-2-local-filter
                                                             country-50-formdata-di data-a-100k+-data-formdata-di data-a-formdata-di fact1-formdata-di
                                                             fact1-local-filter location-formdata-di notes-formdata-di]]
            [de.explorama.profiling-tool.env :refer [create-ws-with-user
                                                     wait-for-result]]
            [de.explorama.shared.common.unification.misc :refer [cljc-json->edn]]
            [de.explorama.shared.map.ws-api :as ws-api]
            [taoensso.timbre :refer [info]]
            [taoensso.tufte :as tufte]))

(defn- load-layer-config [{:keys [result-atom send-fn]}]
  (send-fn
   [ws-api/load-layer-config
    {:client-callback [:result]}])
  (let [result (wait-for-result result-atom)
        _ #?(:cljs (js/console.log "load-layer-config result" result)
             :clj [])
        [_ layer-config parsed-geojsons] result]
    [layer-config parsed-geojsons]))

(defn benchmark-load-layer-config []
  (let [bench-layer-config "Load layer config"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "geomap" (atom nil))

        request-result (-> (load-layer-config ws-con)
                           (update 0 (fn [{:keys [base-layers overlayers]}]
                                       {:base-layers (count base-layers)
                                        :overlayers (count overlayers)}))
                           (update 1 (fn [geojsons]
                                       (into {}
                                             (map (fn [[path geojson]]
                                                    [path (count (get (cljc-json->edn geojson)
                                                                      "features"))]))
                                             geojsons))))]

    (bench bench-layer-config
           []
           (partial load-layer-config ws-con))

    (close-fn)

    (assoc (bench-report)
           :name "load layer config"
           :service "geomap"
           (str bench-layer-config " -> result counts") request-result)))

(defn- load-acs [{:keys [result-atom send-fn]}]
  (send-fn
   [ws-api/load-acs
    {:client-callback [:result]}])
  (let [[_ acs] (wait-for-result result-atom)]
    acs))

(defn benchmark-load-acs-config []
  (let [bench-load-acs "Load acs for overlayer config"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "geomap" (atom nil))
        result (count (load-acs ws-con))]

    (bench bench-load-acs
           []
           (partial load-acs ws-con))

    (close-fn)

    (assoc (bench-report)
           :name "load overlayer acs"
           :service "geomap"
           (str bench-load-acs " -> result count") result)))

(defn- operation-payload [{:keys [di-desc
                                  raw-marker-layouts
                                  raw-feature-layouts
                                  marker-layouts feature-layers
                                  send-data-acs? new-di? local-filter]
                           :or {send-data-acs? false
                                marker-layouts []
                                new-di? false}}]
  {:task-type ""
   :task-id ""
   :live? true
   :payload {:frame-id test-frame-id
             :di di-desc
             :local-filter local-filter

             :base-layer ["World Base"]
             :raw-marker-layouts raw-marker-layouts
             :raw-feature-layers raw-feature-layouts

             :marker-layouts marker-layouts
             :feature-layers (when feature-layers
                               (into {}
                                     (map (fn [{:keys [id] :as layer}]
                                            [id layer]))
                                     feature-layers))

             :view-position {:center [0 0]
                             :zoom 5} ;Not needed will be just send back to client
             :highlighted-markers nil ;Not needed will be just send back to client
             :overlayers [] ;Not needed will be just send back to client

             :popup-desc nil

             :send-data-acs? send-data-acs?
             :update-usable-layouts? true
             :new-di? new-di?
             :cluster? true}})

(defn- operation-request [{:keys [result-atom send-fn]}
                          {:keys [send-data-acs?]
                           :as operation-params}]
  (let [payload (operation-payload operation-params)]
    (send-fn [ws-api/operation
              {:client-callback [:result]
               :async-callback [:async-result]
               :failed-callback [:failed-result]}
              payload])
    (let [result1 (tufte/p
                   :wait-result1
                   (wait-for-result result-atom))
          result2 (when (and send-data-acs?
                             ;Only check for result2 when result1 is client-callback and too-much is nil
                             ;or when result1 is async-result, then either client-callback or failed-callback is missing
                             (or (and (= (first result1) :result)
                                      (nil? (get-in result1 [1 :response :too-much])))
                                 (= (first result1) :async-result)))
                    (tufte/p
                     :wait-result2
                     (wait-for-result result-atom)))]
      (tufte/p
       :return-responses
       (assoc {}
              :result1 result1
              :result2 result2)))))

(defn benchmark-data-acs []
  (let [bench-data-a-country-50 "data-a, country-50, data-acs"
        bench-data-a-100k+ "data-a, 100k+ data-points, data-acs"
        bench-data-a "data-a, data-acs"
        bench-fact1 "fact1 not nil, data-acs"
        bench-notes "Notes includes bom, data-acs"
        bench-location "Location area, data-acs"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "geomap" (atom nil))

        result-type-data-a-country-50 (-> (operation-request ws-con
                                                       {:di-desc country-50-formdata-di
                                                        :send-data-acs? true})
                                    (update :result1 first)
                                    (update :result2 first))
        result-type-data-a (-> (operation-request ws-con
                                                 {:di-desc data-a-formdata-di
                                                  :send-data-acs? true})
                              (update :result1 first)
                              (update :result2 first))
        result-type-data-a-100k+ (-> (operation-request ws-con
                                                       {:di-desc data-a-100k+-data-formdata-di
                                                        :send-data-acs? true})
                                    (update :result1 first)
                                    (update :result2 first))
        result-type-fact1 (-> (operation-request ws-con
                                                      {:di-desc fact1-formdata-di
                                                       :send-data-acs? true})
                                   (update :result1 first)
                                   (update :result2 first))
        result-type-notes (-> (operation-request ws-con
                                                 {:di-desc notes-formdata-di
                                                  :send-data-acs? true})
                              (update :result1 first)
                              (update :result2 first))
        result-type-location (-> (operation-request ws-con
                                                    {:di-desc location-formdata-di
                                                     :send-data-acs? true})
                                 (update :result1 first)
                                 (update :result2 first))]

    (bench bench-data-a-country-50
           []
           (partial operation-request
                    ws-con
                    {:di-desc country-50-formdata-di
                     :send-data-acs? true})
           {:num-of-executions 10})
    (bench bench-data-a
           []
           (partial operation-request
                    ws-con
                    {:di-desc data-a-formdata-di
                     :send-data-acs? true})
           {:num-of-executions 10})
    (bench bench-data-a-100k+
           []
           (partial operation-request
                    ws-con
                    {:di-desc data-a-100k+-data-formdata-di
                     :send-data-acs? true})
           {:num-of-executions 10})
    (bench bench-fact1
           []
           (partial operation-request
                    ws-con
                    {:di-desc fact1-formdata-di
                     :send-data-acs? true})
           {:num-of-executions 10})
    (bench bench-notes
           []
           (partial operation-request
                    ws-con
                    {:di-desc notes-formdata-di
                     :send-data-acs? true})
           {:num-of-executions 10})
    (bench bench-location
           []
           (partial operation-request
                    ws-con
                    {:di-desc location-formdata-di
                     :send-data-acs? true})
           {:num-of-executions 10})

    (close-fn)
    (assoc (bench-report)
           :name "data-acs"
           :service "geomap"
           :data-a-country-50-di country-50-formdata-di
           :data-a-di data-a-formdata-di
           :data-a-100k+-di data-a-100k+-data-formdata-di
           :fact1-di fact1-formdata-di
           :notes-di notes-formdata-di
           :location-di location-formdata-di
           (str bench-data-a-country-50 "-> result type") result-type-data-a-country-50
           (str bench-data-a "-> result type") result-type-data-a
           (str bench-data-a-100k+ "-> result type") result-type-data-a-100k+
           (str bench-fact1 "-> result type") result-type-fact1
           (str bench-notes "-> result type") result-type-notes
           (str bench-location "-> result type") result-type-location)))

(defn benchmark-layout-layer-calc []
  (let [bench-fact1-layout-data-a-100k+-di "data-a 100k+ di, fact1 layout, calc layout"
        bench-context-2-layout-data-a-100k+-di "data-a 100k+ di, context-2 layout, calc layout"
        bench-fact1-layout-fact1-di "fact1 di, fact1 layout, calc layout"
        bench-context-2-layout-fact1-di "fact1 di, context-2 layout, calc layout"

        bench-heatmap-global-layer-data-a-100k+-di "data-a 100k+ di, heatmap point density, calc layout"
        bench-heatmap-local-layer-data-a-100k+-di "data-a 100k+ di, heatmap weigthed (fact1), calc layout"

        bench-movement-layer-data-a-100k+-di "data-a 100k+ di, movement, calc layout"

        bench-fact1-country-sum-data-a-100k+-di "data-a 100k+ di, fact1 country sum, calc layer"
        bench-country-num-events-data-a-100k+-di "data-a 100k+ di, country number of events, calc layer"
        bench-fact1-country-sum-fact1-di  "fact1 di, fact1 country sum, calc layer"
        bench-country-num-events-fact1-di "fact1 di, country number of events, calc layer"

        bench-fact1-province-sum-data-a-100k+-di "data-a 100k+ di, fact1 province sum, calc layer"
        bench-province-num-events-data-a-100k+-di "data-a 100k+ di, province number of events, calc layer"
        bench-fact1-province-sum-fact1-di "fact1 di, fact1 province sum, calc layer"
        bench-province-num-events-fact1-di "fact1 di, province number of events, calc layer"

        bench-fact1-layout-all-layer-data-a-100k+-di "data-a 100k+ di, fact1 layout, all layer, calc all"
        bench-context-2-layout-all-layer-data-a-100k+-di "data-a 100k+ di, context-2 layout, all layer, calc all"
        bench-fact1-layout-all-layer-fact1-di "fact1 di, fact1 layout, all layer, calc all"
        bench-context-2-layout-all-layer-fact1-di "fact1 di, context-2 layout, all layer, calc all"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "geomap" (atom nil))

        _ (info "Make first request for data-a-100k+ data count ")
        data-a-100k+-data-count (get-in (operation-request ws-con
                                                          {:di-desc data-a-100k+-data-formdata-di
                                                           :send-data-acs? false})
                                       [:result1 1 :response :data-count])
        _ (info "Make first request for fact1 not nil data count ")
        fact1-data-count (get-in (operation-request ws-con
                                                         {:di-desc fact1-formdata-di
                                                          :send-data-acs? false})
                                      [:result1 1 :response :data-count])]

    (bench bench-fact1-layout-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :marker-layouts [fact1-layout]})
           {:num-of-executions 5})
    (bench bench-context-2-layout-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :marker-layouts [context-2-layout]})
           {:num-of-executions 5})
    (bench bench-fact1-layout-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :marker-layouts [fact1-layout]})
           {:num-of-executions 5})
    (bench bench-context-2-layout-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :marker-layouts [context-2-layout]})
           {:num-of-executions 5})

    (bench bench-heatmap-global-layer-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [raw-heatmap-layer-global]})
           {:num-of-executions 5})
    (bench bench-heatmap-local-layer-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [raw-heatmap-layer-local]})
           {:num-of-executions 5})
    (bench bench-movement-layer-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [raw-movement-layer]})
           {:num-of-executions 5})

    (bench bench-fact1-country-sum-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [fact1-country-sum-layer]})
           {:num-of-executions 5})
    (bench bench-country-num-events-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [country-num-of-events]})
           {:num-of-executions 5})
    (bench bench-fact1-country-sum-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [fact1-country-sum-layer]})
           {:num-of-executions 5})
    (bench bench-country-num-events-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [country-num-of-events]})
           {:num-of-executions 5})

    (bench bench-fact1-province-sum-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [fact1-province-sum-layer]})
           {:num-of-executions 5})
    (bench bench-province-num-events-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [province-num-of-events]})
           {:num-of-executions 5})
    (bench bench-fact1-province-sum-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [fact1-province-sum-layer]})
           {:num-of-executions 5})
    (bench bench-province-num-events-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [province-num-of-events]})
           {:num-of-executions 5})

    (bench bench-fact1-layout-all-layer-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :raw-feature-layers [all-raw-feature-layers]
                                              :marker-layouts [fact1-layout]
                                              :feature-layers all-raw-feature-layers})
           {:num-of-executions 5})
    (bench bench-context-2-layout-all-layer-data-a-100k+-di
           []
           (partial operation-request ws-con {:di-desc data-a-100k+-data-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :raw-feature-layers all-raw-feature-layers
                                              :marker-layouts [context-2-layout]
                                              :feature-layers all-raw-feature-layers})
           {:num-of-executions 5})

    (bench bench-fact1-layout-all-layer-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :raw-feature-layers [all-raw-feature-layers]
                                              :marker-layouts [fact1-layout]
                                              :feature-layers all-raw-feature-layers})
           {:num-of-executions 5})
    (bench bench-context-2-layout-all-layer-fact1-di
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :raw-feature-layers all-raw-feature-layers
                                              :marker-layouts [context-2-layout]
                                              :feature-layers all-raw-feature-layers})
           {:num-of-executions 5})

    (close-fn)

    (assoc (bench-report)
           :name "calc-layout-layer"
           :service "geomap"
           :data-a-100k+-di data-a-100k+-data-formdata-di
           :fact1-di fact1-formdata-di
           :all-marker-layouts all-raw-marker-layouts
           :all-feature-layers all-raw-feature-layers
           "data-a, Country selection to get 100k+ -> data-count" data-a-100k+-data-count
           "fact1, not empty -> data-count" fact1-data-count)))



(defn benchmark-filter-layout-layer-calc []
  (let [bench-filter-fact1-fact1-di-fact1-marker-layout "fact1 di, Filter fact1, fact1 marker-layout, filter + calc layout"
        bench-filter-context-2-fact1-di-context-2-marker-layout "fact1 di, Filter context-2, context-2 marker-layout, filter + calc layout"

        bench-filter-fact1-fact1-di-fact1-country-layer "fact1 di, Filter fact1, fact1 country sum layer, filter + calc layer"
        bench-filter-context-2-fact1-di-fact1-country-layer "fact1 di, Filter context-2, fact1 country sum layer, filter + calc layer"

        bench-filter-fact1-fact1-di-fact1-movement-layer "fact1 di, Filter fact1, movement layer, filter + calc layer"
        bench-filter-fact1-fact1-di-fact1-heatmap-global-layer "fact1 di, Filter fact1, heatmap point density layer, filter + calc layer"
        bench-filter-fact1-fact1-di-fact1-heatmap-local-layer "fact1 di, Filter fact1, heatmap weighted (fact1) layer, filter + calc layer"

        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "geomap" (atom nil))

        _ (info "Make first request for fact1 not nil data count ")
        fact1-data-count (get-in (operation-request ws-con
                                                         {:di-desc fact1-formdata-di
                                                          :send-data-acs? false})
                                      [:result1 1 :response :data-count])

        _ (info "Make first request for fact1 not nil filtered by fact1 data count ")
        filtered-by-fact1-data-count (get-in (operation-request ws-con
                                                                     {:di-desc fact1-formdata-di
                                                                      :local-filter fact1-local-filter
                                                                      :send-data-acs? false})
                                                  [:result1 1 :response :filtered-data-count])

        _ (info "Make first request for fact1 not nil filtered by context-2 data count ")
        filtered-by-context-2-data-count (get-in (operation-request ws-con
                                                                     {:di-desc fact1-formdata-di
                                                                      :local-filter context-2-local-filter
                                                                      :send-data-acs? false})
                                                  [:result1 1 :response :filtered-data-count])]

    (bench bench-filter-fact1-fact1-di-fact1-marker-layout
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter fact1-local-filter
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :marker-layouts [fact1-layout]})
           {:num-of-executions 5})
    (bench bench-filter-context-2-fact1-di-context-2-marker-layout
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter context-2-local-filter
                                              :raw-marker-layouts all-raw-marker-layouts
                                              :marker-layouts [context-2-layout]})
           {:num-of-executions 5})

    (bench bench-filter-fact1-fact1-di-fact1-country-layer
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter fact1-local-filter
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [fact1-country-sum-layer]})
           {:num-of-executions 5})
    (bench bench-filter-context-2-fact1-di-fact1-country-layer
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter context-2-local-filter
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [fact1-country-sum-layer]})
           {:num-of-executions 5})

    (bench bench-filter-fact1-fact1-di-fact1-movement-layer
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter fact1-local-filter
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [raw-movement-layer]})
           {:num-of-executions 5})
    
    (bench bench-filter-fact1-fact1-di-fact1-heatmap-global-layer
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter fact1-local-filter
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [raw-heatmap-layer-global]})
           {:num-of-executions 5})
    
    (bench bench-filter-fact1-fact1-di-fact1-heatmap-local-layer
           []
           (partial operation-request ws-con {:di-desc fact1-formdata-di
                                              :local-filter fact1-local-filter
                                              :raw-feature-layers all-raw-feature-layers
                                              :feature-layers [raw-heatmap-layer-local]})
           {:num-of-executions 5})

    (close-fn)

    (assoc (bench-report)
           :name "filter-calc-layout-layer"
           :service "geomap"
           :fact1-di fact1-formdata-di
           :all-marker-layouts all-raw-marker-layouts
           :all-feature-layers all-raw-feature-layers
           :context-2-filter context-2-local-filter
           :fact1-filter fact1-local-filter
           "fact1, not empty -> data-count" fact1-data-count
           "fact1, not empty, filtered by fact1 -> data-count" filtered-by-fact1-data-count
           "fact1, not empty, filtered by context-2 -> data-count" filtered-by-context-2-data-count)))

(defn vertical-benchmark-all 
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save (benchmark-load-layer-config))
         (report->save (benchmark-load-acs-config))
         (report->save (benchmark-data-acs))
         (report->save (benchmark-layout-layer-calc))
         (report->save (benchmark-filter-layout-layer-calc)))
     (report->save
      (benchmark-all benchmark-load-layer-config
                     benchmark-load-acs-config
                     benchmark-data-acs
                     benchmark-layout-layer-calc
                     benchmark-filter-layout-layer-calc)))))
