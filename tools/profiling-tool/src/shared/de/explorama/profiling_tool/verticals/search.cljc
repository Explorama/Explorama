(ns de.explorama.profiling-tool.verticals.search
  (:require [de.explorama.backend.expdb.middleware.ac :as mwac]
            [de.explorama.backend.expdb.middleware.indexed :as mwidb]
            [de.explorama.profiling-tool.benchmark :refer [bench bench-report
                                                           benchmark-all
                                                           report->save]]
            [de.explorama.profiling-tool.env :refer [wait-for-result create-ws-with-user]]
            [de.explorama.profiling-tool.data.search :refer [data-a-formdata
                                                             data-a-formdata-data-tile-ref empty-formdata empty-formdata-data-tile-ref fact1-formdata
                                                             location-formdata location-formdata-data-tile-ref notes-formdata
                                                             notes-formdata-data-tile-ref organisation-attr fact1-formdata-data-tile-ref]]
            [de.explorama.profiling-tool.data.core :refer [fact1-data-tiles location-data-tiles notes-data-tiles data-a-data-tiles-1k]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.profiling-tool.config :refer [test-frame-id]]
            [de.explorama.profiling-tool.resources :refer [load-test-resource preload-resources]]
            [de.explorama.shared.search.ws-api :as ws-api]
            [taoensso.tufte :as tufte]
            [taoensso.timbre :refer [error]]))

(preload-resources fact1-data-tiles location-data-tiles notes-data-tiles data-a-data-tiles-1k)

(defn- datasources []
  (mwac/attribute-values
   {:formdata []
    :attributes [[attrs/datasource-attr
                  attrs/datasource-node]]}))

(defn- request-attributes [{:keys [result-atom send-fn]} datasources row-attrs formdata]
  (send-fn
   [ws-api/request-attributes
    {:client-callback [:result]}
    datasources
    test-frame-id
    row-attrs
    formdata])
  (let [[_ attributes _] (wait-for-result result-atom)]
    attributes))

(defn benchmark-request-attributes []
  (let [bench-empty-form-request-attributes "No Formdata, request-attributes"
        bench-data-a-request-attributes "Data-A, request-attributes"
        bench-notes-request-attributes "Notes, request-attributes"
        bench-location-request-attributes "Location, request-attributes"
        bench-fact1-request-attributes "fact1, request-attributes"

        all-datasources (datasources)
        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "suche" (atom nil))

        request-fn (partial request-attributes ws-con all-datasources)

        number-empty-request-attributes (count (request-fn []
                                                           empty-formdata))
        number-data-a-request-attributes (count (request-fn [["datasource" "Datasource"]]
                                                            data-a-formdata))
        number-notes-request-attributes (count (request-fn [["notes" "Notes"]]
                                                           notes-formdata))
        number-location-request-attributes (count (request-fn [["location" "Context"]]
                                                              location-formdata))
        number-fact1-request-attributes (count (request-fn [["fact1" "Fact"]]
                                                           fact1-formdata))]

    (bench bench-empty-form-request-attributes
           []
           (partial request-fn [] empty-formdata))
    (bench bench-data-a-request-attributes
           []
           (partial request-fn
                    [["datasource" "Datasource"]]
                    data-a-formdata))
    (bench bench-notes-request-attributes
           []
           (partial request-fn
                    [["notes" "Notes"]]
                    notes-formdata))
    (bench bench-location-request-attributes
           []
           (partial request-fn
                    [["location" "Context"]]
                    location-formdata))
    (bench bench-fact1-request-attributes
           []
           (partial request-fn
                    [["fact-1" "Fact"]]
                    fact1-formdata))

    (close-fn)

    (assoc (bench-report)
           :name "request-attributes"
           :service "suche"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-request-attributes " -> Result Count") number-empty-request-attributes
           (str bench-data-a-request-attributes " -> Result Count") number-data-a-request-attributes
           (str bench-notes-request-attributes " -> Result Count") number-notes-request-attributes
           (str bench-location-request-attributes " -> Result Count") number-location-request-attributes
           (str bench-fact1-request-attributes " -> Result Count") number-fact1-request-attributes)))

(defn- request-attribute-options [{:keys [result-atom send-fn]} datasources attributes formdata]
  (send-fn
   [ws-api/search-options
    {:client-callback [:result]}
    datasources
    test-frame-id
    attributes
    formdata])
  (let [[_ atttribute-options] (wait-for-result result-atom)]
    atttribute-options))

(defn benchmark-request-attribute-options []
  (let [bench-empty-form-org-request-attribute-options "No Formdata, organisation, request-attribute-options"
        bench-data-a-org-request-attribute-options "Data-A, organisation, request-attributes"
        bench-notes-org-request-attribute-options "Notes, organisation, request-attributes"
        bench-location-org-request-attribute-options "Location, organisation, request-attributes"
        bench-fact1-org-request-attribute-options "fact1, organisation, request-attributes"

        all-datasources (datasources)
        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "suche" (atom nil))

        request-fn (partial request-attribute-options ws-con all-datasources [organisation-attr])

        number-empty-org-request-attribute-options (count (get (request-fn empty-formdata) organisation-attr))
        number-data-a-org-request-attribute-options (count (get (request-fn data-a-formdata) organisation-attr))
        number-notes-org-request-attribute-options (count (get (request-fn notes-formdata) organisation-attr))
        number-location-org-request-attribute-options (count (get (request-fn location-formdata) organisation-attr))
        number-fact1-org-request-attribute-options (count (get (request-fn fact1-formdata) organisation-attr))]

    (bench bench-empty-form-org-request-attribute-options
           []
           (partial request-fn empty-formdata))
    (bench bench-data-a-org-request-attribute-options
           []
           (partial request-fn data-a-formdata))
    (bench bench-notes-org-request-attribute-options
           []
           (partial request-fn notes-formdata))
    (bench bench-location-org-request-attribute-options
           []
           (partial request-fn location-formdata))
    (bench bench-fact1-org-request-attribute-options
           []
           (partial request-fn fact1-formdata))

    (close-fn)

    (assoc (bench-report)
           :name "request-attribute-options"
           :service "suche"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-org-request-attribute-options " -> Result Count") number-empty-org-request-attribute-options
           (str bench-data-a-org-request-attribute-options " -> Result Count") number-data-a-org-request-attribute-options
           (str bench-notes-org-request-attribute-options " -> Result Count") number-notes-org-request-attribute-options
           (str bench-location-org-request-attribute-options " -> Result Count") number-location-org-request-attribute-options
           (str bench-fact1-org-request-attribute-options " -> Result Count") number-fact1-org-request-attribute-options)))

(defn recalc-traffic-lights [{:keys [result-atom send-fn]} datasources formdata]
  (send-fn
   [ws-api/recalc-traffic-lights
    {:client-callback [:result]}
    datasources
    test-frame-id
    formdata])
  (let [[_ {:keys [size]} :as data] (wait-for-result result-atom)]
    #?(:cljs (js/console.log "size" size data))
    size))

(defn benchmark-recalc-traffic-lights []
  (let [bench-empty-form-recalc-traffic-lights "No Formdata, recalc-traffic-lights"
        bench-data-a-recalc-traffic-lights "Data-A, recalc-traffic-lights"
        bench-notes-recalc-traffic-lights "Notes, recalc-traffic-lights"
        bench-location-recalc-traffic-lights "Location, recalc-traffic-lights"
        bench-fact1-recalc-traffic-lights "fact1, recalc-traffic-lights"

        all-datasources (datasources)
        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "suche" (atom nil))

        request-fn (partial recalc-traffic-lights ws-con all-datasources)

        number-empty-recalc-traffic-lights (request-fn empty-formdata)
        number-data-a-recalc-traffic-lights (request-fn data-a-formdata)
        number-notes-recalc-traffic-lights (request-fn notes-formdata)
        number-location-recalc-traffic-lights (request-fn location-formdata)
        number-fact1-recalc-traffic-lights (request-fn fact1-formdata)]

    (bench bench-empty-form-recalc-traffic-lights
           []
           (partial request-fn empty-formdata))
    (bench bench-data-a-recalc-traffic-lights
           []
           (partial request-fn data-a-formdata))
    (bench bench-notes-recalc-traffic-lights
           []
           (partial request-fn notes-formdata))
    (bench bench-location-recalc-traffic-lights
           []
           (partial request-fn location-formdata))
    (bench bench-fact1-recalc-traffic-lights
           []
           (partial request-fn fact1-formdata))

    (close-fn)

    (assoc (bench-report)
           :name "recalc traffic lights"
           :service "suche"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-recalc-traffic-lights " -> size") number-empty-recalc-traffic-lights
           (str bench-data-a-recalc-traffic-lights " -> size") number-data-a-recalc-traffic-lights
           (str bench-notes-recalc-traffic-lights " -> size") number-notes-recalc-traffic-lights
           (str bench-location-recalc-traffic-lights " -> size") number-location-recalc-traffic-lights
           (str bench-fact1-recalc-traffic-lights " -> size") number-fact1-recalc-traffic-lights)))

(defn- search-elements [{:keys [result-atom send-fn]} datasources search-term formdata]
  (send-fn
   [ws-api/search-bar-find-elements
    {:client-callback [:result]}
    datasources
    test-frame-id
    {}
    nil
    search-term
    formdata
    nil
    nil])
  (loop [result (wait-for-result result-atom)
         resp-count 1
         result-count {}]
    (let [[_ result-type result] result]
      (if (= result-type :done)
        {:responses resp-count
         :result-counts result-count}
        (if-let [cur-result (wait-for-result result-atom)]
          (recur cur-result
                 (inc resp-count)
                 (assoc result-count
                        result-type
                        (count result)))
          (do
            (error "No result found")
            {:responses resp-count
             :result-counts result-count}))))))

(defn benchmark-search-elements []
  (let [bench-empty-form-bom-search-elements "No Formdata, search bom, search-elements"
        bench-data-a-bom-search-elements "Data-A, search bom, search-elements"
        bench-notes-bom-search-elements "Notes, search bom, search-elements"
        bench-location-bom-search-elements "Location, search bom, search-elements"
        bench-fact1-bom-search-elements "fact1, search bom, search-elements"

        all-datasources (datasources)
        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "suche" (atom nil))

        request-fn (partial search-elements ws-con all-datasources "bom")

        number-empty-bom-search-elements (request-fn empty-formdata)
        number-data-a-bom-search-elements (request-fn data-a-formdata)
        number-notes-bom-search-elements (request-fn notes-formdata)
        number-location-bom-search-elements (request-fn location-formdata)
        number-fact1-bom-search-elements (request-fn fact1-formdata)]

    (bench bench-empty-form-bom-search-elements
           []
           (partial request-fn empty-formdata))
    (bench bench-data-a-bom-search-elements
           []
           (partial request-fn data-a-formdata))
    (bench bench-notes-bom-search-elements
           []
           (partial request-fn notes-formdata))
    (bench bench-location-bom-search-elements
           []
           (partial request-fn location-formdata))
    (bench bench-fact1-bom-search-elements
           []
           (partial request-fn fact1-formdata))

    (close-fn)

    (assoc (bench-report)
           :name "search elements"
           :service "suche"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-bom-search-elements " -> result count") number-empty-bom-search-elements
           (str bench-data-a-bom-search-elements " -> result count") number-data-a-bom-search-elements
           (str bench-notes-bom-search-elements " -> result count") number-notes-bom-search-elements
           (str bench-location-bom-search-elements " -> result count") number-location-bom-search-elements
           (str bench-fact1-bom-search-elements " -> result count") number-fact1-bom-search-elements)))

(defn- create-di [{:keys [result-atom send-fn]} datasources formdata]
  (send-fn
   [ws-api/create-di
    {:client-callback [:result]}
    datasources
    test-frame-id
    formdata])
  (let [[_ created-di] (wait-for-result result-atom)]
    created-di))

(defn benchmark-create-di []
  (let [bench-empty-form-create-di "No Formdata, create-di"
        bench-data-a-create-di "Data-A, create-di"
        bench-notes-create-di "Notes, create-di"
        bench-location-create-di "Location, create-di"
        bench-fact1-create-di "fact1, create-di"

        all-datasources (datasources)
        {:keys [close-fn]
         :as ws-con} (create-ws-with-user "suche" (atom nil))

        request-fn (partial create-di ws-con all-datasources)

        empty-created-di (request-fn empty-formdata)
        data-a-created-di (request-fn data-a-formdata)
        notes-created-di (request-fn notes-formdata)
        location-created-di (request-fn location-formdata)
        fact1-created-di (request-fn fact1-formdata)]

    (bench bench-empty-form-create-di
           []
           (partial request-fn empty-formdata))
    (bench bench-data-a-create-di
           []
           (partial request-fn data-a-formdata))
    (bench bench-notes-create-di
           []
           (partial request-fn notes-formdata))
    (bench bench-location-create-di
           []
           (partial request-fn location-formdata))
    (bench bench-fact1-create-di
           []
           (partial request-fn fact1-formdata))

    (close-fn)

    (assoc (bench-report)
           :name "create-di"
           :service "suche"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-create-di " -> created-di") empty-created-di
           (str bench-data-a-create-di " -> created-di") data-a-created-di
           (str bench-notes-create-di " -> created-di") notes-created-di
           (str bench-location-create-di " -> created-di") location-created-di
           (str bench-fact1-create-di " -> created-di") fact1-created-di)))

(defn- data-tile-ref->data-tiles [data-tile-ref]
  (mwac/data-tiles-ref-api data-tile-ref))

(defn benchmark-data-tile-ref->data-tiles []
  (let [bench-empty-form-data-tile-ref->data-tiles "No Formdata, data-tile-ref->data-tiles"
        bench-data-a-data-tile-ref->data-tiles "Data-A, data-tile-ref->data-tiles"
        bench-notes-data-tile-ref->data-tiles "Notes, data-tile-ref->data-tiles"
        bench-location-data-tile-ref->data-tiles "Location, data-tile-ref->data-tiles"
        bench-fact1-data-tile-ref->data-tiles "fact1, data-tile-ref->data-tiles"

        number-empty-data-tiles (count (data-tile-ref->data-tiles empty-formdata-data-tile-ref))
        number-data-a-data-tiles (count (data-tile-ref->data-tiles data-a-formdata-data-tile-ref))
        number-notes-data-tiles (count (data-tile-ref->data-tiles notes-formdata-data-tile-ref))
        number-location-data-tiles (count (data-tile-ref->data-tiles location-formdata-data-tile-ref))
        number-fact1-data-tiles (count (data-tile-ref->data-tiles fact1-formdata-data-tile-ref))]

    (bench bench-empty-form-data-tile-ref->data-tiles
           []
           (partial data-tile-ref->data-tiles empty-formdata-data-tile-ref))
    (bench bench-data-a-data-tile-ref->data-tiles
           []
           (partial data-tile-ref->data-tiles data-a-formdata-data-tile-ref))
    (bench bench-notes-data-tile-ref->data-tiles
           []
           (partial data-tile-ref->data-tiles notes-formdata-data-tile-ref))
    (bench bench-location-data-tile-ref->data-tiles
           []
           (partial data-tile-ref->data-tiles location-formdata-data-tile-ref))
    (bench bench-fact1-data-tile-ref->data-tiles
           []
           (partial data-tile-ref->data-tiles fact1-formdata-data-tile-ref))

    (assoc (bench-report)
           :name "data-tile-ref->data-tiles"
           :service "suche"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-data-tile-ref->data-tiles " -> result count") number-empty-data-tiles
           (str bench-data-a-data-tile-ref->data-tiles " -> result count") number-data-a-data-tiles
           (str bench-notes-data-tile-ref->data-tiles " -> result count") number-notes-data-tiles
           (str bench-location-data-tile-ref->data-tiles " -> result count") number-location-data-tiles
           (str bench-fact1-data-tile-ref->data-tiles " -> result count") number-fact1-data-tiles)))

(defn- data-tiles->data [data-tiles-res]
  (let [data-tiles (load-test-resource data-tiles-res)
        partitioned-data-tiles (partition-all 1000 data-tiles)
        result (tufte/p
                :request-data
                (reduce (fn [acc data-tiles]
                          (let [result (tufte/p
                                        :request-batch-data-tiles
                                        (try
                                          (mwidb/get+ (vec data-tiles))
                                          (catch #?(:clj Exception :cljs :default) e
                                            (println "request")
                                            (println {:data-tiles (vec data-tiles)})
                                            (throw e))))]
                            (into acc result)))
                        []
                        partitioned-data-tiles))]
    (tufte/p
     :into-vals-flatten-vec-result
     (-> (into {}
               result)
         vals
         flatten
         vec))))

#_
(defn- benchmark-data-tiles->data []
  (let [bench-data-a-data-tiles->data "Data-A, data-tiles->data"
        bench-notes-data-tiles->data "Notes, data-tiles->data"
        bench-location-data-tiles->data "Location, data-tiles->data"
        bench-fact1-data-tiles->data "fact1, data-tiles->data"

        number-data-a-data (count (data-tiles->data data-a-data-tiles))
        number-notes-data (count (data-tiles->data notes-data-tiles))
        number-location-data (count (data-tiles->data location-data-tiles))
        number-fact1-data (count (data-tiles->data fact1-data-tiles))]

    (bench bench-data-a-data-tiles->data
           [data-a-data-tiles]
           (partial data-tiles->data data-a-data-tiles)
           {:num-of-executions 15})
    (bench bench-notes-data-tiles->data
           [notes-data-tiles]
           (partial data-tiles->data notes-data-tiles)
           {:num-of-executions 15})
    (bench bench-location-data-tiles->data
           [location-data-tiles]
           (partial data-tiles->data location-data-tiles)
           {:num-of-executions 15})
    (bench bench-fact1-data-tiles->data
           [fact1-data-tiles]
           (partial data-tiles->data fact1-data-tiles)
           {:num-of-executions 15})

    (assoc (bench-report)
           :name "data-tiles->data"
           :service "suche"
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-data-a-data-tiles->data " -> result count") number-data-a-data
           (str bench-notes-data-tiles->data " -> result count") number-notes-data
           (str bench-location-data-tiles->data " -> result count") number-location-data
           (str bench-fact1-data-tiles->data " -> result count") number-fact1-data)))

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save benchmark-request-attributes)
         (report->save benchmark-request-attribute-options)
         (report->save benchmark-recalc-traffic-lights)
         (report->save benchmark-search-elements)
         (report->save benchmark-create-di)
         (report->save benchmark-data-tile-ref->data-tiles)
         #_(report->save benchmark-data-tiles->data))
     (report->save
      (benchmark-all benchmark-request-attributes
                     benchmark-request-attribute-options
                     benchmark-recalc-traffic-lights
                     benchmark-search-elements
                     benchmark-create-di
                     benchmark-data-tile-ref->data-tiles
                     #_benchmark-data-tiles->data)))))
