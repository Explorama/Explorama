(ns de.explorama.backend.map.client-api
  (:require [de.explorama.backend.common.aggregation :as common-aggregation]
            [de.explorama.backend.common.layout :as layouts]
            [de.explorama.backend.common.middleware.cache :as data-cache]
            [de.explorama.backend.map.attribute-characteristics :as acs]
            [de.explorama.backend.map.data.core :as data]
            [de.explorama.backend.map.overlayers :as overlayers]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.map.config :as config]
            [taoensso.timbre :refer [debug warn]]
            [taoensso.tufte :as tufte]))

(defn load-layer-config [{:keys [client-callback]} _]
  (let [geojson-layers (filterv #(= (:type %) "geojson")
                                (:overlayers (config/extern-config)))
        parsed-geojsons [] ;;TODO r1/map load layers from files? 
        #_(mapv (fn [{:keys [file-path]}]
                  [file-path (slurp file-path :encoding "UTF-8")])
                geojson-layers)
        layer-config (update (config/extern-config)
                             :overlayers
                             (fn [overlayers]
                               (let [{feature-layers true
                                      overlays false} (group-by :as-layer-base? overlayers)]
                                 {:overlays overlays
                                  :feature-layers (into {}
                                                        (map (fn [{:keys [id] :as f}]
                                                               [id f])
                                                             feature-layers))})))]
    (client-callback layer-config parsed-geojsons)))

(defn load-acs [{:keys [client-callback]} _]
  (client-callback (acs/extract-geolocated-context)))

(defn not-too-much-data? [data data-count]
  (boolean
   (or (and data
            #_{:clj-kondo/ignore [:type-mismatch]}
            (<= data-count config/explorama-map-max-events-data-amount))
       (not data))))

(defn- async-data-acs [async-callback di not-too-much? send-data-acs? live?]
  (when (fn? async-callback)
    (tufte/profile
     {:when config-shared/explorama-profile-time}
     (let [data-acs (when (and di
                               not-too-much?
                               (or send-data-acs? (not live?)))
                      (tufte/p
                       ::get-data-acs
                       (data/get-data-acs di)))]
       (when data-acs
         (async-callback data-acs))))))

(defn operation-tasks [{:keys [client-callback failed-callback async-callback]}
                       [{:keys [task-type
                                task-id
                                live?]
                         {:keys [frame-id base-layer popup-desc di local-filter send-data-acs? update-usable-layouts? new-di?
                                 raw-marker-layouts marker-layouts feature-layers raw-feature-layers cluster? view-position
                                 highlighted-markers overlayers]} :payload}]]
  (tufte/profile
   {:when config-shared/explorama-profile-time}
   (try
     (let [data
           (when di
             (tufte/p
              ::fetch-data
              (data/get-data di)))
           data-count (when data (count data))
           not-too-much? (not-too-much-data? data data-count)
           _ (when (and data
                        #_{:clj-kondo/ignore [:type-mismatch]}
                        (> data-count config/explorama-map-max-data-amount))
               (throw (ex-info "Too much data" {:error :too-much-data
                                                :data-count data-count
                                                :max-data-amount config/explorama-map-max-data-amount})))
           _ ;;TODO r1/async do we need something like a go her? (go 
           (async-data-acs async-callback di not-too-much? send-data-acs? live?)
           ds-acs (tufte/p ::ds-acs
                           (merge (acs/ds-acs)
                                  (get-in di [:di/acs :ac])))
           {:keys [countries years datasources dim-info]} (common-aggregation/calculate-dimensions data)
           filtered-data (if local-filter
                           (tufte/p
                            ::filter-data
                            (data/update-filtered-data data local-filter))
                           data)
           filtered-data-infos (when local-filter
                                 (select-keys (common-aggregation/calculate-dimensions filtered-data)
                                              [:countries :years :datasources]))
           filtered-data-count (count filtered-data)
           displayable-datapoints (when new-di? ;; TODO future as before?
                                    (atom (overlayers/displayable-data filtered-data)))
           stop-filterview? (and (number? config/explorama-map-stop-filterview-amount)
                                 data-count
                                 #_{:clj-kondo/ignore [:type-mismatch]}
                                 (> data-count config/explorama-map-stop-filterview-amount))
           warn-filterview? (and (number? config/explorama-map-warn-filterview-amount)
                                 data-count
                                 #_{:clj-kondo/ignore [:type-mismatch]}
                                 (> data-count config/explorama-map-warn-filterview-amount))
           usable-marker-layouts (tufte/p
                                  ::calc-usable-marker-layouts
                                  (if (and (or update-usable-layouts? (not live?))
                                           not-too-much?)
                                    (overlayers/renderable-layers raw-marker-layouts filtered-data)
                                    nil))
           usable-feature-layouts (tufte/p
                                   ::calc-usable-feature-layouts
                                   (if (or update-usable-layouts? (not live?))
                                     (overlayers/renderable-layers raw-feature-layers filtered-data)
                                     nil))
           best-marker-layouts (tufte/p
                                ::find-best-marker-layout
                                (layouts/check-layouts ds-acs dim-info
                                                       marker-layouts raw-marker-layouts))
           filtered-marker-layout-data (overlayers/marker-layer-calc best-marker-layouts data)
           feature-layers (vals feature-layers)
           best-feature-layout (cond (seq feature-layers)
                                     feature-layers

                                     new-di?
                                     []
                                     #_;TODO r1/map there is currently no useful default overlay
                                       (layouts/check-layouts ds-acs dim-info
                                                              feature-layers raw-feature-layers
                                                              (partial overlayers/create-overlayers config/default-layers)))
           feature-layout-data (when best-feature-layout
                                 (tufte/p
                                  ::calc-feature-layout-data
                                  (overlayers/layers-data best-feature-layout filtered-data not-too-much?)))
           event-id (:event-id popup-desc)
           event-data (when (seq event-id)
                        (tufte/p
                         ::lookup-event-popup
                         (data-cache/lookup-event event-id)))
           response (tufte/p
                     ::build-response-map
                     (cond-> {:di di
                              :years years
                              :countries countries
                              :datasources datasources
                              :base-layer base-layer
                              :popup-desc popup-desc
                              :dim-info dim-info
                              :data-count data-count
                              :new-di? new-di?
                              :stop-filterview? stop-filterview?
                              :warn-filterview? warn-filterview?
                              :cluster? cluster?
                              :view-position view-position
                              :highlighted-markers highlighted-markers
                              :overlayers overlayers}
                       (not not-too-much?)
                       (assoc :too-much {:max-data-amount config/explorama-map-max-events-data-amount
                                         :data-count data-count})
                       local-filter
                       (assoc :filtered-data-infos filtered-data-infos
                              :local-filter local-filter
                              :filtered-data-count filtered-data-count)
                       usable-marker-layouts
                       (assoc :usable-marker-layouts-id usable-marker-layouts)
                       usable-feature-layouts
                       (assoc :usable-feature-layouts-id usable-feature-layouts)
                       best-marker-layouts
                       (assoc :best-marker-layouts best-marker-layouts)
                       filtered-marker-layout-data
                       (assoc :filtered-marker-layout-data filtered-marker-layout-data)
                       best-feature-layout
                       (assoc :best-feature-layouts (into {}
                                                          (map (fn [{layer-id :id :as desc}]
                                                                 [layer-id desc]))
                                                          best-feature-layout))
                       feature-layout-data
                       (assoc :feature-layout-data feature-layout-data)
                       event-data
                       (assoc :event-data-desc {:event-id event-id
                                                :event-data event-data})
                       displayable-datapoints
                       (assoc :displayable-datapoints @displayable-datapoints)))]
       (client-callback {:task-id task-id
                         :task-type task-type
                         :live? live?
                         :response response}))
     (catch #?(:clj Throwable :cljs :default) e
       (warn e "Error during data preparation")
       (failed-callback {:task-id task-id
                         :task-type task-type
                         :base-layer base-layer
                         :error-desc (if-let [data (ex-data e)]
                                       data
                                       {:error :unknown})})))))

(defn update-usable-configs [{:keys [client-callback failed-callback]}
                             [{:keys [di local-filter
                                      raw-marker-layouts raw-feature-layers]}
                              base-resp]]
  (try
    (let [data (data/get-data di)
          filtered-data (if local-filter
                          (data/update-filtered-data data local-filter)
                          data)
          usable-marker-layouts (overlayers/renderable-layers raw-marker-layouts filtered-data)
          usable-feature-layouts (overlayers/renderable-layers raw-feature-layers filtered-data)]
      (client-callback (assoc base-resp
                              :usable-marker-layouts-id usable-marker-layouts
                              :usable-feature-layouts-id usable-feature-layouts)))
    (catch #?(:clj Throwable :cljs :default) e
      (warn e "Error during data preparation")
      (failed-callback {:error-desc (if-let [data (ex-data e)]
                                      data
                                      {:error :unknown})}))))

(defn retrieve-event-data [{:keys [client-callback]}
                           [event-id]]
  (let [event-data (data-cache/lookup-event event-id)]
    (debug "retrieved event-data" {:event-id event-id
                                   :data event-data})
    (client-callback event-data)))
