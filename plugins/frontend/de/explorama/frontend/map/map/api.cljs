(ns de.explorama.frontend.map.map.api
  (:require [de.explorama.frontend.map.map.impl.deckgl.object-manager :as obj-manager]
            [de.explorama.frontend.map.map.impl.deckgl.state-handler :as state-handler]
            [de.explorama.frontend.map.map.protocol.object-manager :as obj-protocol]
            [de.explorama.frontend.map.map.protocol.state-handler :as state-protocol]
            [taoensso.timbre :refer-macros [error warn]]
            [taoensso.tufte :as tufte]))

(def map-type :openlayers)

(defonce ^:private instances (atom {}))

(defn- create-object-manager [frame-id extra-fns]
  (case map-type
    :openlayers (obj-manager/create-instance frame-id extra-fns)))

(defn- create-state-handler [frame-id object-manager-instance extra-fns]
  (case map-type
    :openlayers (state-handler/create-instance frame-id object-manager-instance extra-fns)))

(defn create-instance [frame-id extra-fns]
  (if-let [instances (get @instances frame-id)]
    (do (warn "Instances for frame already created")
        [(:object-manager instances)
         (:state-handler instances)])
    (let [object-manager-instance (create-object-manager frame-id extra-fns)
          state-handler-instance (create-state-handler frame-id object-manager-instance extra-fns)]
      (swap! instances assoc frame-id {:object-manager object-manager-instance
                                       :state-handler state-handler-instance})
      [object-manager-instance
       state-handler-instance])))

(defn instances-exist? [frame-id]
  (not-empty (get @instances frame-id)))

(defn destroy-instance [frame-id]
  (let [{:keys [object-manager state-handler]} (get @instances frame-id)]
    (when object-manager
      (obj-protocol/destroy-instance object-manager))
    (when state-handler
      (state-protocol/destroy-instance state-handler))
    (swap! instances dissoc frame-id)))

(defn- get-obj-manager [frame-id]
  (if-let [obj-manager (get-in @instances [frame-id :object-manager])]
    obj-manager
    (do
      (error "No object-manager instance available for " frame-id)
      nil)))

(defn- get-state-handler [frame-id]
  (if-let [state-handler (get-in @instances [frame-id :state-handler])]
    state-handler
    (do
      (error "No state-handler instance available for " frame-id)
      nil)))

(defn create-map-instance [frame-id headless?]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/create-map-instance obj-manager headless?)))

(defn render-map [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/render-map state-handler)))

(defn create-base-layers [frame-id base-layers]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (tufte/p
     ::create-base-layers
     (doseq [base-layer base-layers]
       (obj-protocol/create-base-layer obj-manager base-layer)))))

(defn switch-base-layer [frame-id base-layer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/switch-base-layer state-handler base-layer-id)))

(defn create-overlayers [frame-id overlayers]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (tufte/p
     ::create-overlayers
     (doseq [overlayer overlayers]
       (obj-protocol/create-overlayer obj-manager overlayer)))))

(defn resize-map [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/resize-map state-handler)))

(defn set-marker-data [frame-id marker-data]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/set-marker-data state-handler marker-data)))

(defn get-marker-data [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/get-marker-data state-handler)))

(defn set-feature-data [frame-id feature-data]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/set-feature-data state-handler feature-data)))

(defn get-feature-data [frame-id feature-layer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/get-feature-data state-handler feature-layer-id)))

(defn set-filtered-feature-data [frame-id feature-data]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/set-filtered-feature-data state-handler feature-data)))

(defn get-filtered-feature-data
  "This will be used only while project loading.
   Its used to get the orignal and filtered data into the client."
  [frame-id feature-layer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/get-filtered-feature-data state-handler feature-layer-id)))

(defn get-created-marker-ids [frame-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/marker-ids obj-manager)))

(defn remove-marker [frame-id marker-ids]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/remove-markers obj-manager marker-ids)))

(defn create-marker-layers [frame-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (when-not (obj-protocol/marker-layer-created? obj-manager)
      (tufte/p 
       ::create-marker-layer
       (obj-protocol/create-marker-layer obj-manager)))))

(defn create-markers [frame-id marker-data]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (when-not (obj-protocol/marker-layer-created? obj-manager)
      (obj-protocol/create-marker-layer obj-manager)) 
    
    (tufte/p
     ::create-markers
     (obj-protocol/create-markers obj-manager marker-data))
    
    (when-let [state-handler (get-state-handler frame-id)]
      (tufte/p
       ::display-marker-cluster
       (state-protocol/display-marker-cluster state-handler)))))

(defn clear-markers [frame-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (tufte/p
     ::clear-markers
     (obj-protocol/clear-markers obj-manager))))

(defn update-marker-styles [frame-id to-be-updated]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p 
     ::update-marker-styles
     (state-protocol/update-marker-styles state-handler to-be-updated))))

(defn cluster-marker [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/display-marker-cluster state-handler)))

(defn no-clustering-marker [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/display-markers state-handler)))

(defn store-event-data [frame-id event-id event-data]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/cache-event-data state-handler event-id event-data)))

(defn get-event-data [frame-id event-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/cached-event-data state-handler event-id)))

(defn show-popup [frame-id coordinates content-desc]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/display-popup state-handler coordinates content-desc)))

(defn hide-popup [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/hide-popup state-handler)))

(defn move-to [frame-id zoom center]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/move-to state-handler zoom center)))

(defn move-to-marker [frame-id marker-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/move-to-marker state-handler marker-id)))

(defn select-cluster-with-marker [frame-id marker-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/select-cluster-with-marker state-handler marker-id)))

(defn add-onetime-render-done-listener [frame-id listener-fn]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/one-time-render-done-listener state-handler listener-fn)))

(defn move-to-data [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/move-to-data state-handler)))

(defn view-position [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/view-position state-handler)))

(defn hide-markers-with-id [frame-id marker-ids]
  (when-let [state-handler (get-state-handler frame-id)] 
    (state-protocol/hide-markers-with-id state-handler marker-ids)))

(defn display-all-markers [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/display-all-markers state-handler)))

(defn marker-highlighted? [frame-id marker-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/marker-higlighted? state-handler marker-id)))

(defn list-highlighted-marker [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/list-highlighted-marker state-handler)))

(defn highlight-marker [frame-id marker-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/highlight-marker state-handler marker-id)))

(defn de-highlight-marker [frame-id marker-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/de-highlight-marker state-handler marker-id)))

(defn display-overlayer [frame-id overlayer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::display-overlayer
     (state-protocol/display-overlayer state-handler overlayer-id))))

(defn list-active-overlayer [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/list-active-overlayers state-handler)))

(defn hide-overlayer [frame-id overlayer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::hide-overlayer
     (state-protocol/hide-overlayer state-handler overlayer-id))))

(defn list-active-feature-layers [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (state-protocol/list-active-feature-layers state-handler)))

(defn create-feature-layer [frame-id feature-layer]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (tufte/p
     ::create-feature-layer
     (obj-protocol/create-feature-layer obj-manager feature-layer))))

(defn remove-feature-layer [frame-id feature-layer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::remove-feature-layer
     (state-protocol/remove-feature-layer state-handler feature-layer-id))))

(defn feature-layer-created? [frame-id feature-layer-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/feature-layer-created? obj-manager feature-layer-id)))

(defn get-feature-layer-obj [frame-id feature-layer-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/get-feature-layer-obj obj-manager feature-layer-id))) 

(defn create-arrow-features [frame-id feature-layer-id arrow-descs]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (tufte/p
     ::create-arrow-features
     (obj-protocol/create-arrow-features obj-manager feature-layer-id arrow-descs))))

(defn clear-arrow-features [frame-id feature-layer-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/clear-arrow-features obj-manager feature-layer-id)))

(defn create-heatmap-features [frame-id feature-layer-id heatmap-data]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (tufte/p
     ::create-heatmap-features
     (obj-protocol/create-heatmap-features obj-manager feature-layer-id heatmap-data))))

(defn clear-heatmap-features [frame-id feature-layer-id]
  (when-let [obj-manager (get-obj-manager frame-id)]
    (obj-protocol/clear-heatmap-features obj-manager feature-layer-id)))

(defn display-feature-layer [frame-id feature-layer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::display-feature-layer
     (state-protocol/display-feature-layer state-handler feature-layer-id))))

(defn hide-feature-layer [frame-id feature-layer-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::hide-feature-layer
     (state-protocol/hide-feature-layer state-handler feature-layer-id))))

(defn clear-feature-layers [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::cleare-feature-layer
     (state-protocol/clear-feature-layers state-handler))))

(defn temp-hide-marker-layer [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::temp-hide-marker-layer
     (state-protocol/temp-hide-marker-layer state-handler))))

(defn restore-temp-hidden-marker-layer [frame-id]
  (when-let [state-handler (get-state-handler frame-id)]
    (tufte/p
     ::restore-temp-hidden-marker-layer
     (state-protocol/restore-temp-hidden-marker-layer state-handler))))
