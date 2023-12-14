(ns de.explorama.backend.mosaic.data.aggregation
  (:require [data-format-lib.core :as dfl-core]
            [data-format-lib.operations :as dfl-op]
            [de.explorama.backend.common.aggregation :as common-aggregation]
            [de.explorama.backend.common.calculations.data-acs :as data-acs]
            [de.explorama.backend.common.environment.probe :as probe]
            [de.explorama.backend.common.layout :as gbrl]
            [de.explorama.backend.common.middleware.cache :as cache]
            [de.explorama.backend.configuration.middleware.i18n :refer [get-labels]]
            [de.explorama.backend.expdb.middleware.ac :as ac-api]
            [de.explorama.backend.mosaic.attribute-characteristics :as ac]
            [de.explorama.backend.mosaic.client.redo :as gcr]
            [de.explorama.backend.mosaic.client.scale :as gcs]
            [de.explorama.backend.mosaic.config :as config-mosaic]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.common.unification.misc :as cljc-misc]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.shared.mosaic.config :as config-shared-mosaic]
            [de.explorama.shared.mosaic.group-by-layout :as gbl]
            [de.explorama.shared.mosaic.layout :as gsrl]
            [taoensso.timbre :as log]
            [taoensso.tufte :as tufte]))

(defn- get-data [data-instance]
  (try
    (tufte/p ::merge-data
             (dfl-core/transform data-instance
                                 ac-api/data-tiles-ref-api
                                 cache/lookup
                                 :attach-buckets? true
                                 :post-fn (fn [result]
                                            ;TODO r1/caching handle this when we have caching and this does not matter
                                            (when (:di/aggregation-caching? data-instance)
                                              (cache/index-aggregation-result data-instance result))
                                            result)))
    (catch #?(:clj Throwable :cljs :default) e
      (log/error e "Unable to retrieve your data."
                 {:data-instance data-instance})
      [])))

(defn- extract-event [annotations fallback-layout? event]
  (when (attrs/value event "annotation")
    (swap! annotations
           assoc
           [(attrs/value event "id")
            (attrs/value event "bucket")]
           (attrs/value event "annotation")))
  (if-let [color (get-in event ["layout" "color"])]
    [color
     (attrs/value event "id")
     (attrs/value event "bucket")
     (get-in event ["layout" "id"])]
    (do
      #_:clj-kondo/ignore
      (reset! fallback-layout? true)
      [config-shared-mosaic/explorama-fallback-layout-color
       (attrs/value event "id")
       (attrs/value event "bucket")
       "###fallback-layout"])))

(defn- minimize-data-client [data {grp-by-key gcp/grp-by-key

                                   sub-grp-by-key gcp/sub-grp-by-key
                                   type :type
                                   :or {type :raster}}]
  (let [annotations (atom {})
        ; sure these are a side effects, but a easy and fast way to
        ; gather annotations and the usage of fallback-layouts
        fallback-layout? (atom false)]
    [(cond (and (not grp-by-key)
                (not sub-grp-by-key)
                (or (= type :raster)
                    (= type :scatter)
                    (= type :treemap)))
           (mapv (partial extract-event annotations fallback-layout?)
                 data)
           (and grp-by-key
                (not sub-grp-by-key)
                (or (= type :raster)
                    (= type :treemap)))
           (mapv (fn [[group-key events]]
                   [(-> (assoc group-key "key" (get group-key grp-by-key))
                        (assoc "attr" grp-by-key)
                        (assoc "group-key?" true))
                    (mapv (partial extract-event annotations fallback-layout?)
                          events)])
                 data)
           (and grp-by-key
                sub-grp-by-key
                (or (= type :raster)
                    (= type :treemap)))
           (mapv (fn [[group-key sub-groups]]
                   [(-> (assoc group-key "key" (get group-key grp-by-key))
                        (assoc "attr" grp-by-key)
                        (assoc "group-key?" true))
                    (mapv (fn [[sub-group-key events]]
                            [(-> (assoc sub-group-key "key" (get sub-group-key sub-grp-by-key))
                                 (assoc "attr" sub-grp-by-key)
                                 (assoc "group-key?" true))
                             (mapv (partial extract-event annotations fallback-layout?)
                                   events)])
                          sub-groups)])
                 data))
     @annotations
     @fallback-layout?]))

#_(defn scale-data-up [data up-to]
    (log/error "#########################")
    (log/error "do not use in production!")
    (log/error "#########################")
    (let [data-count (count data)]
      (mapv (fn [_]
              (assoc (get dat
                          a (rand-int data-count))
                     :id (uuid)))
            (range up-to))))

(defn- find-attr [by group-key attr]
  (if (or (= by :name)
          (= by "layout"))
    group-key
    attr))

(defn- attach-ignore-hierarchy [desc grp-by-key]
  (if (#{"month" "day"} grp-by-key)
    (assoc desc :ignore-hierarchy? true)
    desc))

(defn- generate-compare [layouts attr grp-sort-by lang]
  (let [locale (cljc-misc/cljc-number-locale lang)]
    (cond
      (= grp-sort-by "layout")
      (gbl/generate-layout-compare layouts)
      (= attr "layout")
      (gbl/generate-compare-by-group-text layouts
                                          (fn [attr labels] (get labels attr attr))
                                          (get-labels lang)
                                          (fn [n] (cljc-misc/cljc-format-number
                                                   locale
                                                   (cljc-misc/cljc-bigdec n)))))))

(defn- apply-operations [best-layout

                         {grp-by-key gcp/grp-by-key

                          couple-grps-key gcp/couple-key

                          sub-grp-by-key gcp/sub-grp-by-key

                          render-mode-key gcp/render-mode-key

                          {sort-attr :by
                           sort-direction :direction}
                          gcp/sort-key

                          {grp-sort-by :by
                           grp-sort-direction :direction
                           grp-sort-attr :attr
                           grp-sort-method :method}
                          gcp/sort-grp-key

                          {sub-grp-sort-by :by
                           sub-grp-sort-direction :direction
                           sub-grp-sort-attr :attr
                           sub-grp-sort-method :method}
                          gcp/sort-sub-grp-key}
                         lang]
  (let [obj-acs (ac/obj-acs)
        layouts (vec best-layout)
        types (into {} (map (fn [{:keys [type name]}]
                              [name type])
                            obj-acs))
        raster? (or (= render-mode-key gcp/render-mode-key-raster)
                    (= render-mode-key gcp/render-mode-key-treemap))
        operation
        (cond->> "di1"
          (seq best-layout)
          (conj [:apply-layout {:layouts layouts}])
          (and grp-by-key
               raster?)
          (conj [:group-by (attach-ignore-hierarchy {:attributes [grp-by-key] :forced-groups couple-grps-key :mode :keep}
                                                    grp-by-key)])
          (and sub-grp-by-key
               raster?)
          (conj [:group-by (attach-ignore-hierarchy {:attributes [sub-grp-by-key] :mode :keep}
                                                    sub-grp-by-key)])
          (and sub-grp-by-key
               sub-grp-sort-by
               sub-grp-sort-direction
               raster?)
          (conj [:sort-by (cond-> (let [attr (find-attr sub-grp-sort-by
                                                        sub-grp-by-key
                                                        sub-grp-sort-attr)]
                                    {:attribute attr
                                     :return-map? false
                                     :apply-to :group
                                     :level 1
                                     :direction sub-grp-sort-direction
                                     :attribute-types {attr (keyword (get types attr :number))}
                                     :custom-compare (generate-compare layouts attr grp-sort-by lang)})
                            (= sub-grp-sort-by :aggregate)
                            (assoc :aggregate [sub-grp-sort-method {:attribute sub-grp-sort-attr}])

                            (= grp-sort-by :event-count)
                            (assoc :aggregate [:count-events {}]))])
          (and grp-by-key
               grp-sort-by
               grp-sort-direction
               raster?)
          (conj [:sort-by (cond-> (let [attr (find-attr grp-sort-by
                                                        grp-by-key
                                                        grp-sort-attr)]
                                    {:attribute attr
                                     :return-map? false
                                     :apply-to :group
                                     :level 0
                                     :direction grp-sort-direction
                                     :attribute-types {attr (keyword (get types attr :number))}
                                     :custom-compare (generate-compare layouts attr grp-sort-by lang)})

                            (and (= grp-sort-by :event-count)
                                 (and sub-grp-by-key
                                      sub-grp-sort-by
                                      sub-grp-sort-direction
                                      raster?))
                            (assoc :aggregate [:count-events {:join? true}])
                            (and (= grp-sort-by :event-count)
                                 (not (and sub-grp-by-key
                                           sub-grp-sort-by
                                           sub-grp-sort-direction
                                           raster?)))
                            (assoc :aggregate [:count-events {}])

                            (and (= grp-sort-by :aggregate)
                                 (and sub-grp-by-key
                                      sub-grp-sort-by
                                      sub-grp-sort-direction
                                      raster?))
                            (assoc :aggregate [grp-sort-method {:attribute grp-sort-attr
                                                                :join? true}])
                            (and (= grp-sort-by :aggregate)
                                 (not (and sub-grp-by-key
                                           sub-grp-sort-by
                                           sub-grp-sort-direction
                                           raster?)))
                            (assoc :aggregate [grp-sort-method {:attribute grp-sort-attr}]))])
          (and sort-attr
               sort-direction
               raster?)
          (conj [:sort-by {:attribute sort-attr
                           :direction sort-direction
                           :attribute-types {sort-attr (keyword (get types sort-attr :number))}
                           :apply-to :events}]))]
    (log/debug "Operations description" operation)
    operation))

(defn- not-too-much-data? [data data-count]
  (when data
    (<= data-count config-mosaic/explorama-mosaic-max-data-amount)))

(defn aysnc-send-calculate-data-acs [callback data-fn send-data-acs?]
  (when send-data-acs?
    (try
      (callback
       (tufte/profile {:when config-shared/explorama-profile-time}
                      (data-fn)))
      (catch #?(:clj Throwable :cljs :default) e
        (log/warn e "Error dispatching event to websocket/tube:" {:task "sending async async-send-data-ac"}))))
  nil)

(defn async-send-data-ac [callback data-fn send-data-acs?]
  (let [data (data-fn)]
    (when send-data-acs?
      (try
        (callback
         (tufte/profile {:when config-shared/explorama-profile-time}
                        data))
        (catch #?(:clj Throwable :cljs :default) e
          (log/warn e "Error dispatching event to websocket/tube:" {:task "sending async async-send-data-ac"}))))
    data))

(defn- operations* [{:keys [data-acs-async-callback]}
                    {:keys [local-filter di new-di? operations-desc layouts raw-layouts send-data-acs?
                            update-usable-layouts? validate-operations-desc? lang scatter-axis-fallback?]}]
  (try
    (let [data (get-data di)
          data-count (count data)
          not-too-much-data? (not-too-much-data? data data-count)
          _ (when-not not-too-much-data?
              (throw (ex-info "Too much data" {:error :too-much-data
                                               :data-count data-count
                                               :max-data-amount config-mosaic/explorama-mosaic-max-data-amount})))
          ;data (tufte/p ::scale (scale-data-up filtered-data 2000000)) ;! This line will increase the data send to the client with random events
          {:keys [years countries datasources dim-info]} (tufte/p ::calc-dimensions (common-aggregation/calculate-dimensions data))
          data-count (count data)
          ds-acs (merge (ac/ds-acs)
                        (get-in di [:di/acs :ac]))
          [invalid-options operations-desc] (if validate-operations-desc?
                                              (gcr/validate-operations-desc ds-acs dim-info operations-desc)
                                              [nil operations-desc])
          scatter? (= gcp/render-mode-key-scatter (get operations-desc gcp/render-mode-key))
          best-layout  (cond (and new-di? (seq layouts) (not= 0 data-count))
                             (gbrl/check-layouts ds-acs dim-info layouts raw-layouts)
                             (seq layouts)
                             layouts
                             (empty? layouts)
                             (gbrl/find-layout ds-acs dim-info raw-layouts nil))
          invalid-options (if (empty? best-layout)
                            (assoc invalid-options gcp/layouts true)
                            invalid-options)
          usable-layout-ids (if update-usable-layouts?
                              (gsrl/usable-layouts ds-acs dim-info raw-layouts best-layout)
                              nil)
          [filtered-data
           filtered-count]
          (if local-filter
            (let [filtered-data (tufte/p ::perform-operation
                                         (dfl-op/perform-operation {"di1" (vec data)}
                                                                   {"f1" local-filter}
                                                                   [:filter "f1" "di1"]))]
              [filtered-data (count filtered-data)])
            [data data-count])
          oped-data (tufte/p ::perform-operation
                             (dfl-op/perform-operation {"di1" (vec filtered-data)}
                                                       {}
                                                       (apply-operations best-layout operations-desc lang)))
          data-acs (if scatter?
                     (async-send-data-ac data-acs-async-callback
                                         (fn [] (data-acs/data-acs data))
                                         send-data-acs?)
                     (aysnc-send-calculate-data-acs data-acs-async-callback
                                                    (fn [] (data-acs/data-acs data))
                                                    send-data-acs?))
          scale (when scatter?
                  (gcs/mapping-layer oped-data data-acs scatter-axis-fallback? operations-desc))
          filtered-data-infos (when (and not-too-much-data? local-filter)
                                (tufte/p ::filter-calc-dimensions
                                         (select-keys (common-aggregation/calculate-dimensions filtered-data)
                                                      [:countries :years :datasources])))
          [oped-data
           annotations
           fallback-layout?]
          (tufte/p ::minimize-data-client
                   (minimize-data-client oped-data operations-desc))]
      [oped-data
       {:years years
        :countries countries
        :datasources datasources
        :dim-info dim-info
        :best-layout best-layout
        :annotations annotations
        :scale scale
        :filtered-data-infos filtered-data-infos
        :fallback-layout? fallback-layout?
        :new-di? new-di?
        :di di
        :usable-layout-ids usable-layout-ids
        :invalid-options invalid-options
        :filtered-count filtered-count
        :scatter? scatter?
        :data-count data-count
        :data-acs data-acs
        :update-usable-layouts? update-usable-layouts?
        :operations-desc operations-desc}])
    (catch #?(:clj Throwable :cljs :default) e
      (log/warn e "Error during data preparation")
      [[]
       {:error-desc (if-let [data (ex-data e)]
                      data
                      (do
                        (probe/rate-exception e)
                        {:error :unknown}))}])))

(defn operations
  [{:keys [client-callback] :as desc}
   {:keys [local-filter di new-di? layouts validate-operations-desc? scatter-axis-fallback?]
    :as payload}]
  (log/debug "inform-client" local-filter new-di? di)
  (tufte/profile
   {:when config-shared/explorama-profile-time}
   (tufte/p ::inform-client-with-dispatch
            (try
              (let [[batches
                     {:keys [best-layout
                             scale filtered-data-infos
                             fallback-layout? operations-desc
                             usable-layout-ids invalid-options
                             filtered-count scatter? data-acs]
                      :as result}]
                    (operations* desc payload)]
                (client-callback (cond-> (merge {:transform-server? true
                                                 :batches (count batches)
                                                 :filtered-data-info filtered-data-infos}
                                                (select-keys result
                                                             [:di :years :countries
                                                              :datasources :dim-info
                                                              :data-count
                                                              :new-di? :annotations
                                                              :error-desc]))
                                   local-filter
                                   (assoc :local-filter local-filter)
                                   (and not-too-much-data? local-filter)
                                   (assoc :filtered-data-count filtered-count)
                                   (and data-acs scatter?)
                                   (assoc :data-acs data-acs)
                                   (and scatter? scatter-axis-fallback?)
                                   (assoc :scatter-axis-fallback? true)
                                   (and scale scatter?)
                                   (assoc :scale scale)
                                   usable-layout-ids
                                   (assoc :usable-layout-ids usable-layout-ids)
                                   (or (empty? layouts)
                                       (get invalid-options gcp/layouts))
                                   (assoc :best-layout-ids (map :id best-layout))
                                   (:default? (first best-layout))
                                   (assoc :generated-layout best-layout)
                                   (and (and new-di? (seq layouts))
                                        (not (get invalid-options gcp/layouts)))
                                   (assoc :redo-layouts best-layout)
                                   fallback-layout?
                                   (assoc :fallback-layout? true)
                                   (and validate-operations-desc? (seq invalid-options))
                                   (assoc :new-operations-desc operations-desc)
                                   (and validate-operations-desc? (seq invalid-options))
                                   (assoc :invalid-options invalid-options)
                                   :always
                                   (assoc :data (cljc-misc/cljc-edn->json batches)))))
              (catch #?(:clj Throwable :cljs :default) e
                (log/warn e "Error dispatching event to websocket/tube:"   {:desc desc
                                                                            :local-filter  local-filter
                                                                            :new-di? new-di?}))))))

(defn initialize [{:keys [client-callback]} [_]]
  (client-callback {:acs (ac/ds-acs)
                    :obj-acs (ac/obj-acs)
                    :color-acs (ac/color-acs)}))