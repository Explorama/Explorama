(ns de.explorama.frontend.map.map.core
  (:require [clojure.data :as data]
            [de.explorama.shared.data-format.aggregations :as dfl-agg]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.common.tubes :as tubes]
            [de.explorama.frontend.map.components.frame-notifications
             :refer [not-supported-redo-ops-event]]
            [de.explorama.frontend.map.config :as  config]
            [de.explorama.frontend.map.configs.util :as util]
            [de.explorama.frontend.map.interaction.selection :as selection]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.map.config :as geo-config]
            [de.explorama.frontend.map.map.geojson :as geojson-store]
            [de.explorama.frontend.map.map.render-helper
             :refer [add-render-done-listener
                     show-event-popup-fn-wrapper show-feature-layer-popup-fn-wrapper
                     show-overlayer-popup-fn-wrapper]]
            [de.explorama.frontend.map.operations.redo :as redo]
            [de.explorama.frontend.map.operations.tasks :as task]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.views.details :as details]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error info warn]]
            [taoensso.tufte :as tufte]
            [vimsical.re-frame.fx.track :as track]))

(defn- register-fx
  [track-or-tracks]
  (try
    (track/register-fx track-or-tracks)
    (catch js/Error
           e
      (info e)
      {})))

(defn- dispose-fx
  [track-or-tracks]
  (try
    (track/dispose-fx track-or-tracks)
    (catch js/Error
           e
      (info e)
      {})))

(re-frame/reg-fx ::register register-fx)
(re-frame/reg-fx ::dispose dispose-fx)

(re-frame/reg-sub
 ::frame-datasource
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-di frame-id))))

;; Triggered by server
(re-frame/reg-event-fx
 ::invalid-operations
 (fn [{db :db} [_ frame-id not-supported-ops]]
   (when (redo/show-notification? not-supported-ops)
     {:db (redo/remove-invalid-operations db frame-id not-supported-ops)
      :dispatch (not-supported-redo-ops-event db frame-id not-supported-ops)})))

(re-frame/reg-event-fx
 ::highlight-event
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id [id di] lat lon]]
   (let [id-key (attrs/access-key "id")
         selected {id-key id (attrs/access-key "location") [lat lon]}
         already-selected? (some (fn [s] (= id (attrs/value s id-key)))
                                 (:current (get-in db (geop/frame-selections frame-id) [])))]
     {:dispatch (fi/call-api (if already-selected?
                               :deselect-event-vec
                               :select-event-vec)
                             frame-id
                             selected)})))

(re-frame/reg-event-fx
 ::update-selection
 (fn [{db :db} [_ frame-id {:keys [current]} callback]]
   {:fx [[:dispatch [::task/execute-wrapper
                     frame-id
                     :highlight-marker
                     {:highlighted-markers current}]]
         (when (seq callback)
           [:dispatch callback])]}))

(re-frame/reg-event-fx
 ::resize-listener
 (fn [{db :db} [_ frame-id _]]
   (when (= (get-in db (conj (geop/frame-state frame-id) :state))
            :running)
     (map-api/resize-map frame-id))
   {}))

(re-frame/reg-event-fx
 ::initialize
 (fn [{db :db} [_ frame-id task-id]]
   (let [all-base-layers (get-in db geop/base-layers)
         init-state {:state :initialized
                     :init-task-id task-id
                     :cluster? true
                     :base-layer (some (fn [{:keys [default name]}]
                                         (when default name))
                                       all-base-layers)}]
     {:db (assoc-in db (geop/frame-state frame-id) init-state)})))

(re-frame/reg-sub
 ::initialized?
 (fn [db [_ frame-id]]
   (= (get-in db (conj (geop/frame-state frame-id) :state))
      :initialized)))

(re-frame/reg-sub
 ::map-state
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-state frame-id))))

(re-frame/reg-event-fx
 ::register-state-tracker
 (fn [_ [_ frame-id]]
   {::register
    {:id [::state frame-id]
     :subscription [::map-state frame-id]
     :event-fn (fn [val]
                 (when (seq val)
                   [::update-instances frame-id]))}}))

(re-frame/reg-event-fx
 ::dispose-state-tracker
 (fn [_ [_ frame-id]]
   {::dispose
    {:id [::state frame-id]}}))

(re-frame/reg-sub
 ::current-marker-layout
 (fn [db [_ frame-id]]
   (first (get-in db (geop/selected-marker-layouts frame-id)))))

(re-frame/reg-sub
 ::feature-layer-config
 (fn [db [_ feature-layer-id]]
   (get-in db (geop/feature-color-layer feature-layer-id))))

(defn- retrieved-event-data [[_ frame-id event-id result-callback] [event-data]]
  (map-api/store-event-data frame-id event-id event-data)
  (when (and result-callback (vector? result-callback))
    (re-frame/dispatch result-callback)))

(re-frame/reg-event-fx
 ws-api/retrieved-event-data
 (fn [_ [_
         frame-id
         event-id
         result-callback
         event-data]]
   (map-api/store-event-data frame-id event-id event-data)
   (when (and result-callback (vector? result-callback))
     {:fx [[:dispatch result-callback]]})))

(re-frame/reg-event-fx
 ::show-event-popup
 (fn [{db :db} [_ frame-id event-id event-color clicked-position]]
   (show-event-popup-fn-wrapper db frame-id event-id event-color clicked-position)
   {}))

(defn- marker-clicked [frame-id event-id event-color clicked-position view-position]
  (re-frame/dispatch [:de.explorama.frontend.map.operations.tasks/execute-wrapper
                      frame-id
                      :popup
                      {:event-id event-id
                       :event-color event-color
                       :marker? true
                       :clicked-position clicked-position
                       :view-position view-position}]))

(defn- overlayer-feature-clicked [frame-id overlayer-id feature-properties clicked-position view-position]
  (re-frame/dispatch [:de.explorama.frontend.map.operations.tasks/execute-wrapper
                      frame-id
                      :popup
                      {:overlayer-id overlayer-id
                       :feature-properties feature-properties
                       :clicked-position clicked-position
                       :view-position view-position}]))

(defn- area-feature-clicked [frame-id area-feature-id feature-properties clicked-position view-position]
  (re-frame/dispatch [:de.explorama.frontend.map.operations.tasks/execute-wrapper
                      frame-id
                      :popup
                      {:area-feature-id area-feature-id
                       :feature-properties feature-properties
                       :clicked-position clicked-position
                       :view-position view-position}]))

(defn- marker-dbl-clicked [mouse-event frame-id event-id]
  (when (seq event-id)
    (let [di @(re-frame/subscribe [::frame-datasource frame-id])]
      (re-frame/dispatch [::details/prepare-add-to-details-view frame-id di event-id mouse-event]))))

(re-frame/reg-event-fx
 ::focus-event
 (fn [_ [_ frame-id event-id]]
   (map-api/move-to-marker frame-id event-id)
   {}))

(re-frame/reg-sub
 ::feature-layer-desc
 (fn [db [_ frame-id layer-id]]
   (get-in db (conj (geop/selected-feature-layers frame-id)
                    layer-id))))

(re-frame/reg-sub
 ::popup-desc
 (fn [db [_ frame-id]]
   (get-in db (geop/popup-desc frame-id))))

(defn extra-fns [frame-id]
  (let [marker-click-timeout (atom nil)
        view-position-change (atom nil)]
    {:do-panning? geo-config/do-panning?
     :attribute-label (fn [attr]
                        (let [attr-labels @(fi/call-api [:i18n :get-labels-sub])]
                          (or (get attr-labels attr)
                              (when-let [agg-label (get-in dfl-agg/descs [(keyword attr) :label])]
                                @(re-frame/subscribe [::i18n/translate agg-label]))
                              attr)))
     :workspace-scale (partial fi/call-api :workspace-scale-sub)
     :max-hover-marker (fn [] (atom config/max-hover-marker))
     :move-data-max-zoom (fn [] (atom 10))
     :localize-number (fn [num]
                        (i18n/localized-number num))
     :marker-stroke-rgb-color (fn []
                                config/marker-stroke-rgb-color)
     :highlighted-marker-stroke-rgb-color (fn []
                                            config/marker-highlight-stroke-rgb-color)
     :feature-layer-desc (fn [layer-id]
                           @(re-frame/subscribe [::feature-layer-desc frame-id layer-id]))
     :hide-popup (fn []
                   (re-frame/dispatch [:de.explorama.frontend.map.operations.tasks/execute-wrapper
                                       frame-id
                                       :popup
                                       {}]))
     :get-popup-desc (fn []
                       @(re-frame/subscribe [::popup-desc frame-id]))
     :track-view-position-change (fn [mouse-leave?]
                                   (if mouse-leave?
                                     (re-frame/dispatch [:de.explorama.frontend.map.operations.tasks/execute-wrapper
                                                         frame-id
                                                         :position-change
                                                         (map-api/view-position frame-id)])
                                     (warn "Zoom/Pan end not supported yet.")))
     :highlight-event (fn [id location]
                        (when @(fi/call-api :flags-sub frame-id :data-interaction?)
                          (if-not (map-api/marker-highlighted? frame-id id)
                            (do (map-api/highlight-marker frame-id id)
                                (re-frame/dispatch [::selection/select
                                                    frame-id
                                                    {"id" id
                                                     "location" location}]))
                            (do (map-api/de-highlight-marker frame-id id)
                                (re-frame/dispatch [::selection/deselect
                                                    frame-id
                                                    {"id" id
                                                     "location" location}])))))
     :event-highlighted? (fn [id]
                           (map-api/marker-highlighted? frame-id id))
     :marker-dbl-clicked (fn [mouse-event event-id]
                           (when-let [old-timeout @marker-click-timeout]
                             (js/clearTimeout old-timeout))
                           (marker-dbl-clicked mouse-event frame-id event-id)
                           (when-let [old-timeout @marker-click-timeout]
                             (js/clearTimeout old-timeout))
                           (reset! marker-click-timeout nil))
     :marker-clicked (fn [event-id event-color clicked-position view-position]
                       (when-let [old-timeout @marker-click-timeout]
                         (js/clearTimeout old-timeout))
                       (reset! marker-click-timeout
                               (js/setTimeout (fn []
                                                (marker-clicked frame-id event-id event-color
                                                                clicked-position view-position)
                                                (reset! marker-click-timeout nil))
                                              config/click-timeout)))
     :overlayer-feature-clicked (fn [overlayer-id feature-properties clicked-position view-position]
                                  (overlayer-feature-clicked frame-id overlayer-id feature-properties clicked-position view-position))
     :area-feature-clicked (fn [area-feature-id feature-properties clicked-position view-position]
                             (area-feature-clicked frame-id area-feature-id feature-properties clicked-position view-position))
     :geojson-object (fn [geojson-path]
                       (geojson-store/get-geojson geojson-path))
     :feature-layer-config (fn [feature-layer-id]
                             @(re-frame/subscribe [::feature-layer-config feature-layer-id]))
     :active-feature-layers (fn []
                              (map-api/list-active-feature-layers frame-id))}))

(defn- init-headless [db frame-id]
  (let [current-state (get-in db (geop/frame-state frame-id))
        base-layers (get-in db geop/base-layers)
        overlayers (get-in db geop/overlayers)
        init-task-id (:init-task-id current-state)]
    (map-api/create-instance frame-id (extra-fns frame-id))
    (map-api/create-map-instance frame-id true)
    (map-api/create-base-layers frame-id base-layers)
    (map-api/create-overlayers frame-id overlayers)
    {:db (assoc-in db (geop/headless? frame-id) true)
     :fx [[:dispatch [::ddq/finish-task frame-id init-task-id ::init]]]}))

(re-frame/reg-event-fx
 ::init
 (fn [{db :db} [_ frame-id]]
   (let [current-state (get-in db (geop/frame-state frame-id))
         base-layers (get-in db geop/base-layers)
         overlayers (get-in db geop/overlayers)
         featurelayers (-> @(re-frame/subscribe [::feature-layer-config])
                           vector)
         init-task-id (:init-task-id current-state)
         was-headless? (get-in db (geop/headless? frame-id))
         default-base-layer (some (fn [{:keys [default name]}]
                                    (when default name))
                                  base-layers)
         clustering? (get-in db (geop/cluster-marker? frame-id))
         {:keys [is-minimized?]} (fi/call-api :frame-db-get db frame-id)]
     (cond
       (not (map-api/instances-exist? frame-id))
       (do (map-api/create-instance frame-id (extra-fns frame-id))
           (map-api/create-map-instance frame-id false)
           (map-api/create-base-layers frame-id base-layers)
           (map-api/switch-base-layer frame-id default-base-layer)
           (map-api/create-overlayers frame-id overlayers)
           (map-api/create-feature-layer frame-id featurelayers))
       (and (map-api/instances-exist? frame-id) was-headless?)
       (add-render-done-listener db frame-id))
     {:db (-> db
              (assoc-in (geop/headless? frame-id) false)
              (assoc-in (conj (geop/frame-state frame-id) :state) :running))
      :fx [[:dispatch [::ddq/finish-task frame-id init-task-id ::init]]
           (when (or (not was-headless?) is-minimized?)
             [:dispatch (fi/call-api :render-done-event-vec
                                     frame-id config/default-namespace)])]})))

(defn- switch-base-layer [db frame-id]
  (let [base-layer (get-in db (geop/base-layer frame-id))]
    (map-api/switch-base-layer frame-id base-layer)))

(defn- update-base-layer [db fx frame-id task-id]
  (switch-base-layer db frame-id)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :base-layer)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::base-layer]])})

(defn- popup [db frame-id render?]
  (let [{:keys [event-id event-color
                overlayer-id feature-properties
                area-feature-id
                clicked-position]
         {:keys [zoom center]} :view-position} (get-in db (geop/popup-desc frame-id))
        event-data (when (seq event-id) (map-api/get-event-data frame-id event-id))
        cluster-marker? (get-in db (geop/cluster-marker? frame-id))]
    (cond
      (and (seq event-id)
           render?
           (seq event-data)) (show-event-popup-fn-wrapper db frame-id event-id event-color clicked-position)
      (and (seq event-id)
           render?) (tubes/dispatch-to-server [ws-api/retrieve-event-data
                                               {:client-callback [ws-api/retrieved-event-data
                                                                  frame-id
                                                                  event-id
                                                                  [::show-event-popup
                                                                   frame-id event-id
                                                                   event-color clicked-position]]}
                                               event-id])
      (and (seq overlayer-id)
           render?) (show-overlayer-popup-fn-wrapper db frame-id overlayer-id feature-properties clicked-position)
      (and (seq area-feature-id)
           render?) (show-feature-layer-popup-fn-wrapper db frame-id area-feature-id feature-properties clicked-position)
      (and (not (seq event-id))
           (not (seq overlayer-id))
           (not (seq area-feature-id))
           render?) (map-api/hide-popup frame-id)
      (not render?) (map-api/move-to frame-id zoom center))))

(defn- marker-steps [frame-id]
  (let [marker-data (map-api/get-marker-data frame-id)
        all-created-marker (set (map-api/get-created-marker-ids frame-id))
        marker-data-keys (set (keys marker-data))
        [to-be-created to-be-deleted to-be-updated] (data/diff marker-data-keys all-created-marker)]
    (map-api/temp-hide-marker-layer frame-id)

    (when (and (seq marker-data)
               (seq to-be-deleted))
      (map-api/remove-marker frame-id to-be-deleted))

    (when-not (seq marker-data)
      (map-api/clear-markers frame-id))

    (when (and (seq marker-data)
               (seq to-be-created))
      (map-api/create-markers frame-id (select-keys marker-data to-be-created)))

    (when (and (seq marker-data)
               (seq to-be-updated))
      (map-api/update-marker-styles frame-id to-be-updated))

    (map-api/restore-temp-hidden-marker-layer frame-id)))

(defn- highlight-markers [db frame-id live?]
  (let [current-highlighted-marker (map-api/list-highlighted-marker frame-id)
        new-highlighted-markers (mapv #(get % "id") (get-in db (geop/highlighted-markers frame-id)))
        [highlight-ids de-highlight-ids _] (data/diff (set new-highlighted-markers)
                                                      current-highlighted-marker)]
    (doseq [marker-id de-highlight-ids]
      (map-api/de-highlight-marker frame-id marker-id))

    (when highlight-ids
      (doseq [marker-id new-highlighted-markers]
        (when live?
          (map-api/move-to-marker frame-id marker-id))
        (map-api/highlight-marker frame-id marker-id)))))

(defn- movement-feature-layer [frame-id {:keys [layer-id] :as layer-data}]
  (let [layer-obj (if (map-api/feature-layer-created? frame-id layer-id)
                    (do
                      (map-api/hide-feature-layer frame-id layer-id)
                      (map-api/get-feature-layer-obj frame-id layer-id))
                    (map-api/create-feature-layer frame-id layer-data))
        arrow-features (map-api/create-arrow-features frame-id layer-id (:data-set layer-data))]
    (map-api/display-feature-layer frame-id layer-id)))

(defn- area-coloring-layer [frame-id {:keys [layer-id] :as layer-data}]
  (let [layer-obj (if (map-api/feature-layer-created? frame-id layer-id)
                    (do
                      (map-api/remove-feature-layer frame-id layer-id)
                      (map-api/create-feature-layer frame-id layer-data))
                    (map-api/create-feature-layer frame-id layer-data))]
    (map-api/display-feature-layer frame-id layer-id)))

(defn- heatmap-feature-layer [frame-id {:keys [layer-id] :as layer-data}]
  (let [layer-obj (if (map-api/feature-layer-created? frame-id layer-id)
                    (do
                      (map-api/hide-feature-layer frame-id layer-id)
                      (map-api/get-feature-layer-obj frame-id layer-id))
                    (map-api/create-feature-layer frame-id layer-data))
        heatmap-features (map-api/create-heatmap-features frame-id layer-id (get-in layer-data [:data-set :data]))]
    (map-api/display-feature-layer frame-id layer-id)))

(defn- feature-layers [db frame-id]
  (let [selected-feature-layers (get-in db (geop/selected-feature-layers frame-id))
        removed-feature-layers (get-in db (geop/removed-feature-layers frame-id))]
    (doseq [id removed-feature-layers]
      (map-api/hide-feature-layer frame-id id))
    (doseq [[id desc] selected-feature-layers]
      (let [{:keys [type] :as desc} desc
            layer-data (map-api/get-feature-data frame-id id)]
        (case type
          :movement (movement-feature-layer frame-id layer-data)
          :feature (area-coloring-layer frame-id layer-data)
          :heatmap (heatmap-feature-layer frame-id layer-data)
          (warn "Unknown layer-type used" {:layer-desc desc
                                           :type type}))))))

(defn- update-init-di [db fx frame-id task-id live?]
  (let [selected-marker-layouts (get-in db (geop/selected-marker-layouts frame-id))]
    (map-api/clear-markers frame-id)
    (map-api/clear-feature-layers frame-id)
    (marker-steps frame-id)
    (highlight-markers db frame-id live?)
    (feature-layers db frame-id)
    {:db (-> db
             (update-in (geop/running-tasks frame-id)
                        dissoc :init-di)
             (assoc-in (geop/removed-feature-layers frame-id)
                       nil))
     :fx (conj fx
               [:dispatch [::ddq/finish-task frame-id task-id ::init-di]])}))

(defn- update-marker [db fx frame-id task-id render?]
  (marker-steps frame-id)
  (popup db frame-id render?)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :marker)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::marker]])})

(defn- clustering [db frame-id]
  (let [cluster? (get-in db (geop/cluster-marker? frame-id) true)]
    (if cluster?
      (map-api/cluster-marker frame-id)
      (map-api/no-clustering-marker frame-id))))

(defn- update-clustering [db fx frame-id task-id]
  (clustering db frame-id)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :cluster-switch)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::marker]])})

(defn- update-feature-layer [db fx frame-id task-id]
  (feature-layers db frame-id)
  {:db (-> db
           (update-in (geop/running-tasks frame-id)
                      dissoc :feature-layer)
           (assoc-in (geop/removed-feature-layers frame-id)
                     nil))
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::feature-layer]])})

(defn- hide-feature-layer [db frame-id]
  (let [current-active-layers (map-api/list-active-feature-layers frame-id)
        selected-feature-layers (set (keys (get-in db (geop/selected-feature-layers frame-id))))
        [deactivate-ids] (data/diff current-active-layers selected-feature-layers)]
    (doseq [hide-id deactivate-ids]
      (map-api/hide-feature-layer frame-id hide-id))))

(defn- update-hide-feature-layer [db fx frame-id task-id render?]
  (hide-feature-layer db frame-id)
  (popup db frame-id render?)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :hide-feature-layer)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::hide-feature-layer]])})

(defn- update-popup [db fx frame-id task-id render?]
  (popup db frame-id render?)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :popup)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::popup]])})

(defn- update-position [db fx frame-id task-id render?]
  (let [{:keys [center zoom]} (get-in db (geop/view-position frame-id))]
    (when-not render?
      (map-api/move-to frame-id zoom center))
    {:db (update-in db
                    (geop/running-tasks frame-id)
                    dissoc :position-change)
     :fx (conj fx
               [:dispatch [::ddq/finish-task frame-id task-id ::position-change]])}))

(defn- apply-local-filtering [db frame-id _live?]
  (let [marker-data (map-api/get-marker-data frame-id)
        all-created-marker (set (map-api/get-created-marker-ids frame-id))
        marker-data-keys (set (keys marker-data))
        [to-create to-hide _] (when marker-data-keys
                                (data/diff marker-data-keys all-created-marker))]

    (when (seq to-create)
      (map-api/create-markers frame-id (select-keys marker-data to-create)))

    (when (and to-hide (seq to-hide))
      (map-api/hide-markers-with-id frame-id to-hide))

    (when (not (seq to-hide))
      (map-api/display-all-markers frame-id))

    (feature-layers db frame-id)))

(defn- update-filtering [db fx frame-id task-id live?]
  (apply-local-filtering db frame-id live?)
  {:db (-> db
           (update-in (geop/running-tasks frame-id)
                      dissoc :filter)
           (assoc-in (geop/removed-feature-layers frame-id)
                     nil))
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::local-filtering]])})

(defn- update-highlighted-marker [db fx frame-id task-id live?]
  (highlight-markers db frame-id live?)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :highlight-marker)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::highlight-marker]])})

(defn- overlayers [db frame-id]
  (let [current-active-overlyers (map-api/list-active-overlayer frame-id)
        updated-overlayers (get-in db (geop/selected-overlayers frame-id) #{})
        [hide-overlayers display-overlayers _] (data/diff current-active-overlyers updated-overlayers)]
    (doseq [overlayer-id hide-overlayers]
      (map-api/hide-overlayer frame-id overlayer-id))
    (doseq [overlayer-id display-overlayers]
      (map-api/display-overlayer frame-id overlayer-id))))

(defn- update-overlayer [db fx frame-id task-id render?]
  (overlayers db frame-id)
  (popup db frame-id render?)
  {:db (update-in db
                  (geop/running-tasks frame-id)
                  dissoc :highlight-marker)
   :fx (conj fx
             [:dispatch [::ddq/finish-task frame-id task-id ::highlight-marker]])})

(defn- complete-replay [db frame-id live?]
  (let [{:keys [center zoom]} (get-in db (geop/view-position frame-id))]
    (map-api/create-marker-layers frame-id)
    (switch-base-layer db frame-id)
    (marker-steps frame-id)
    (apply-local-filtering db frame-id live?)
    (clustering db frame-id)
    (highlight-markers db frame-id live?)
    (overlayers db frame-id)
    (feature-layers db frame-id)
    (map-api/move-to frame-id zoom center)))

(defn- update-instances [db frame-id render?]
  (tufte/profile
   {:when config/profile-time}
   (let [running-tasks (get-in db (geop/running-tasks frame-id))]
     (cond
       (and (seq running-tasks)
            render?) (reduce (fn [{:keys [db fx] :as n-fx} [task-type task-id]]
                               (case task-type
                                 :base-layer (update-base-layer db fx frame-id task-id)
                                 :init-di (update-init-di db fx frame-id task-id true)
                                 :copy-frame (do
                                               (complete-replay db frame-id true)
                                               {:db (assoc-in db (geop/running-tasks frame-id) {})
                                                :fx [[:dispatch [::ddq/finish-task frame-id task-id ::copy-frame]]
                                                     [:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id false]]]})
                                 :filter (update-filtering db fx frame-id task-id true)
                                 :marker (update-marker db fx frame-id task-id render?)
                                 :feature-layer (update-feature-layer db fx frame-id task-id)
                                 :hide-feature-layer (update-hide-feature-layer db fx frame-id task-id render?)
                                 :cluster-switch (update-clustering db fx frame-id task-id)
                                 :popup (update-popup db fx frame-id task-id render?)
                                 :position-change (update-position db fx frame-id task-id render?)
                                 :highlight-marker (update-highlighted-marker db fx frame-id task-id render?)
                                 :overlayer (update-overlayer db fx frame-id task-id render?)
                                 (do
                                   (warn "Unkown update-task-type" {:task-type task-type})
                                   n-fx)))
                             {:db db
                              :fx [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id false]]]}
                             running-tasks)
       (seq running-tasks) (do
                             (complete-replay db frame-id false)
                             {:db (assoc-in db (geop/running-tasks frame-id) {})
                              :fx [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id false]]
                                   [:dispatch [::ddq/finish-task frame-id (second (first running-tasks)) ::replay-operation]]]})))))

(re-frame/reg-event-fx
 ::update-instances
 (fn [{db :db} [_ frame-id]]
   (let [render? (fi/call-api [:interaction-mode :render-db-get?]
                              db)]
     (cond (and (not render?)
                (not (map-api/instances-exist? frame-id)))
           (init-headless db frame-id)
           (map-api/instances-exist? frame-id)
           (update-instances db frame-id render?)))))

(defn- updated-layouts [db frame-id deleted-configs]
  (let [raw-marker-layouts (get-in db geop/raw-marker-layouts)
        selected-layouts (into {}
                               (map #(vector (:id %) %))
                               (get-in db (geop/selected-marker-layouts frame-id)))
        selected-deleted (reduce (fn [acc [deleted-config-id deleted-config]]
                                   (if (nil? (get selected-layouts deleted-config-id))
                                     acc
                                     (assoc acc
                                            deleted-config-id
                                            (assoc deleted-config
                                                   :temporary? true))))
                                 (get-in db (geop/temp-raw-marker-layouts frame-id))
                                 deleted-configs)
        any-selected-changed? (some (fn [[k selected-def]]
                                      (boolean (and (not (contains? deleted-configs k))
                                                    (not= selected-def
                                                          (util/translate-layer (get raw-marker-layouts k))))))
                                    selected-layouts)
        updated-selected (when any-selected-changed?
                           (reduce (fn [acc [k v]]
                                     (if (not (contains? deleted-configs k))
                                       (assoc acc k (util/translate-layer (get raw-marker-layouts k)))
                                       (assoc acc k v)))
                                   {}
                                   selected-layouts))]
    {:db (cond-> db
           (seq selected-deleted) (assoc-in (geop/temp-raw-marker-layouts frame-id) selected-deleted)
           updated-selected (assoc-in (geop/selected-marker-layouts frame-id) updated-selected))
     :fx [(when any-selected-changed?
            [:dispatch [::task/execute-wrapper
                        frame-id
                        :marker
                        {:marker-layouts (vec
                                          (vals
                                           (select-keys (merge raw-marker-layouts
                                                               selected-deleted)
                                                        (keys updated-selected))))}]])]}))

(defn- updated-feature-layers [db frame-id deleted-configs]
  (let [raw-feature-layers (get-in db geop/raw-feature-layers)
        selected-feature-layers (get-in db (geop/selected-feature-layers frame-id))
        selected-deleted (reduce (fn [acc [deleted-config-id deleted-config]]
                                   (if (nil? (get selected-feature-layers deleted-config-id))
                                     acc
                                     (assoc acc
                                            deleted-config-id
                                            (assoc deleted-config
                                                   :temporary? true))))
                                 (get-in db (geop/temp-raw-feature-layers frame-id))
                                 deleted-configs)
        any-selected-changed? (some (fn [[k selected-def]]
                                      (boolean (and (not (contains? deleted-configs k))
                                                    (not= selected-def
                                                          (util/translate-layer (get raw-feature-layers k))))))
                                    selected-feature-layers)
        updated-selected (when any-selected-changed?
                           (reduce (fn [acc [k v]]
                                     (if (not (contains? deleted-configs k))
                                       (assoc acc k (util/translate-layer (get raw-feature-layers k)))
                                       (assoc acc k v)))
                                   {}
                                   selected-feature-layers))]
    {:db (cond-> db
           (seq selected-deleted) (assoc-in (geop/temp-raw-feature-layers frame-id) selected-deleted)
           updated-selected (assoc-in (geop/selected-feature-layers frame-id) updated-selected))
     :fx [(when any-selected-changed?
            [:dispatch [::task/execute-wrapper
                        frame-id
                        :feature-layer
                        {:feature-layers (vec
                                          (vals
                                           (select-keys (merge raw-feature-layers
                                                               selected-deleted)
                                                        (keys updated-selected))))}]])]}))

(re-frame/reg-event-fx
 ws-api/update-usable-config-result
 (fn [{db :db} [_ frame-id {:keys [config-type deleted-configs
                                   usable-marker-layouts-id usable-feature-layouts-id]}]]
   (let [updated-db
         (-> db
             (assoc-in (geop/usable-marker-layouts-id frame-id) usable-marker-layouts-id)
             (assoc-in (geop/usable-feature-layouts-id frame-id) usable-feature-layouts-id))]
     (case config-type
       :layouts (updated-layouts updated-db frame-id deleted-configs)
       :overlayers (updated-feature-layers updated-db frame-id deleted-configs)
       (do
         (warn "Unkonwn config-type updated" config-type)
         nil)))))

(re-frame/reg-event-fx
 ws-api/update-usable-config-failed
 (fn [{db :db} [_ frame-id {:keys [error-desc] :as resp}]]
   (let [render? (fi/call-api [:interaction-mode :render-db-get?]
                              db)]
     (error "Updating usable config ids (feature-layer, marker-layouts) failed." resp)
     {:fx [(when render?
             [:dispatch [:de.explorama.frontend.map.views.stop-screen/stop-view-display frame-id
                         (case (:error error-desc)
                           :too-much-data :stop-view-too-much-data
                           :unknown :stop-view-unknown)
                         error-desc]])]})))

(re-frame/reg-event-fx
 ::config-updated
 (fn [{db :db} [_ frame-id config-type deleted-configs]]
   {:backend-tube [ws-api/update-usable-config-ids
                   {:client-callback [ws-api/update-usable-config-result frame-id]
                    :failed-callback [ws-api/update-usable-config-failed frame-id]}
                   (-> db
                       (task/build-base-payload frame-id)
                       (select-keys [:di :local-filter])
                       (assoc :frame-id frame-id)
                       (task/add-raw-config db))
                   {:config-type config-type
                    :deleted-configs deleted-configs}]}))
