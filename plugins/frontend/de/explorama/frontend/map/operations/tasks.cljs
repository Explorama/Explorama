(ns de.explorama.frontend.map.operations.tasks
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.configs.util :as util]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]
            [taoensso.tufte :as tufte]))


;=> Needed for redo later
(defn attach-operations [db frame-id  log-info]
  (-> db
      #_(assoc-in (geop/operation-desc-current-logged frame-id)
                  log-info)
      #_(assoc-in (geop/operation-desc-last-logged frame-id)
                  (get-in db (geop/operation-desc-current-logged frame-id)))))

(defn build-base-payload
  "The base payload for all task-operations.
   This should include everything to calculate from this payload alone."
  [db frame-id]
  (let [di (get-in db (geop/frame-di frame-id))
        local-filter (get-in db (geop/applied-filter frame-id))
        base-layer (get-in db (geop/base-layer frame-id))
        marker-layouts (get-in db (geop/selected-marker-layouts frame-id))
        feature-layers (get-in db (geop/selected-feature-layers frame-id))
        overlayers (get-in db (geop/selected-overlayers frame-id))
        cluster? (get-in db (geop/cluster-marker? frame-id))
        popup-desc (get-in db (geop/popup-desc frame-id))
        highlighted-marker-ids (get-in db (geop/highlighted-markers frame-id))
        current-view-position (get-in db (geop/view-position frame-id))]
    {:di di
     :local-filter local-filter
     :base-layer base-layer
     :cluster? cluster?
     :marker-layouts marker-layouts
     :feature-layers feature-layers
     :popup-desc popup-desc
     :highlighted-markers highlighted-marker-ids
     :overlayers overlayers
     :view-position current-view-position}))

(defn add-raw-config [payload db]
  (let [raw-marker-layouts (util/raw-layer->translate-layer (vals (get-in db geop/raw-marker-layouts)))
        raw-feature-layers (util/raw-layer->translate-layer (vals (get-in db geop/raw-feature-layers)))]
    (assoc payload
           :raw-marker-layouts raw-marker-layouts
           :raw-feature-layers raw-feature-layers)))

(defn- init-di
  "Initialize DI"
  [db
   frame-id
   task-id
   {:keys [di local-filter highlighted-markers marker-layouts feature-layers]
    :as params}
   live?]
  (let [local-filter (or local-filter (get-in db (geop/applied-filter frame-id)))
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :di di
                                         :marker-layouts (when marker-layouts
                                                           (util/raw-layer->translate-layer marker-layouts))
                                         :feature-layers feature-layers
                                         :highlighted-markers highlighted-markers
                                         :send-data-acs? true
                                         :update-usable-layouts? true
                                         :new-di? true
                                         :local-filter local-filter)
                                  params)
                                db)]
    {:db (cond-> db
           :always
           (assoc-in (geop/frame-di frame-id) (:di payload))
           :always
           (attach-operations frame-id
                              {:action :init-di
                               :payload (dissoc payload
                                                :raw-marker-layouts
                                                :raw-feature-layers)})
           :always
           (assoc-in (geop/data-request-pending frame-id) true)
           marker-layouts
           (assoc-in (geop/selected-marker-layouts frame-id) marker-layouts)
           feature-layers
           (assoc-in (geop/selected-feature-layers frame-id) feature-layers)
           :always
           (assoc-in (geop/applied-filter frame-id) local-filter))
     :fx (cond-> [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id true ::init-di]]]
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                             {:action :init-di
                              :payload (dissoc payload
                                               :raw-marker-layouts
                                               :raw-feature-layers)}]])
           :always (conj
                    [:backend-tube [ws-api/operation
                                    {:client-callback [ws-api/operation-result frame-id]
                                     :failed-callback [ws-api/operation-failed frame-id]
                                     :async-callback [ws-api/operation-async-acs frame-id]}
                                    {:task-type :init-di
                                     :task-id task-id
                                     :payload payload
                                     :live? live?}]]))}))

(defn- copy-frame-task [db
                        frame-id
                        task-id
                        {:keys [source-frame-id]
                         :as params}
                        live?]
  (let [{:keys [local-filter]
         :as payload} (add-raw-config (if (and live? source-frame-id)
                                        (assoc (build-base-payload db source-frame-id)
                                               :send-data-acs? true
                                               :update-usable-layouts? true
                                               :new-di? true)
                                        params)
                                      db)
        temp-marker-layouts (get-in db (geop/temp-raw-marker-layouts source-frame-id))
        temp-feature-layers (get-in db (geop/temp-raw-feature-layers source-frame-id))
        payload (-> payload
                    (update :raw-marker-layouts into (vals temp-marker-layouts))
                    (update :raw-feature-layers into (vals temp-feature-layers)))]
    {:db (-> db
             (attach-operations frame-id
                                {:action :copy-frame
                                 :payload (dissoc payload
                                                  :raw-marker-layouts
                                                  :raw-feature-layers)})
             (assoc-in (geop/frame-di frame-id) (:di payload))
             (update-in (geop/running-tasks frame-id) assoc :copy-frame task-id))
     :fx (cond-> [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id true ::copy-frame]]]
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation" {:action :copy-frame
                                                                                                      :payload (dissoc payload
                                                                                                                       :raw-marker-layouts
                                                                                                                       :raw-feature-layers)}]])
           :always
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :copy-frame
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- filter-task [db frame-id task-id {local-filter :filter-desc :as params} live?]
  (let [payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :local-filter local-filter)
                                  params)
                                db)]
    (cond-> {:db (-> (attach-operations db frame-id  {:action :filter
                                                      :payload payload})
                     (assoc-in (geop/applied-filter frame-id) local-filter)
                     (update-in (geop/running-tasks frame-id) assoc :filter task-id))
             :fx (cond-> [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id true ::filter-task]]]
                   live?
                   (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation" {:action :filter
                                                                                                              :payload payload}]])
                   :always
                   (conj [:backend-tube [ws-api/operation
                                         {:client-callback [ws-api/operation-result frame-id]
                                          :failed-callback [ws-api/operation-failed frame-id]
                                          :async-callback [ws-api/operation-async-acs frame-id]}
                                         {:task-type :filter
                                          :task-id task-id
                                          :payload payload
                                          :live? live?}]]))})))

(defn- base-layer-task [db frame-id task-id {:keys [base-layer] :as params} live?]
  (let [payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :base-layer base-layer)
                                  params)
                                db)]
    {:db (-> db
             (assoc-in (geop/base-layer frame-id) base-layer)
             (update-in (geop/running-tasks frame-id) assoc :base-layer task-id))
     :fx (cond-> []
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                             {:action :base-layer
                              :payload (dissoc payload
                                               :raw-marker-layouts
                                               :raw-feature-layers)}]])
           (not live?)
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :base-layer
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- marker-task [db frame-id task-id {:keys [marker-layouts] :as params} live?]
  (let [marker-layouts (util/raw-layer->translate-layer marker-layouts)
        {popup-event-id :event-id
         popup-event-color :event-color
         :as c-popup-desc} (get-in db (geop/popup-desc frame-id))
        popup-desc (if (and popup-event-id popup-event-color)
                     {}
                     c-popup-desc)
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :marker-layouts marker-layouts
                                         :popup-desc popup-desc)
                                  params)
                                db)
        temp-layouts (->> (:marker-layouts params)
                          (filter :temporary?)
                          (map #(vector (:id %) %))
                          (into {}))]
    {:db (-> db
             (update-in (geop/running-tasks frame-id) assoc :marker task-id)
             (assoc-in (geop/temp-raw-marker-layouts frame-id) temp-layouts)
             (assoc-in (geop/selected-marker-layouts frame-id) marker-layouts))
     :fx (cond-> [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id true ::marker-task]]]
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                             {:action :marker
                              :payload (dissoc payload
                                               :raw-marker-layouts
                                               :raw-feature-layers)}]])
           :always
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :marker
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- cluster-task [db frame-id task-id {:keys [cluster?] :as params} live?]
  (let [payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :cluster? cluster?)
                                  params)
                                db)]
    {:db (-> db
             (assoc-in (geop/cluster-marker? frame-id) cluster?)
             (update-in (geop/running-tasks frame-id) assoc :cluster-switch task-id))
     :fx (cond-> []
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                             {:action :cluster-switch
                              :payload (dissoc payload
                                               :raw-marker-layouts
                                               :raw-feature-layers)}]])
           (not live?)
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :cluster-switch
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- overlayer-task [db frame-id task-id {:keys [overlayers] :as params} live?]
  (let [{popup-overlayer-id :overlayer-id
         :as c-popup-desc} (get-in db (geop/popup-desc frame-id))
        popup-desc (if (and popup-overlayer-id
                            (not (overlayers popup-overlayer-id)))
                     {}
                     c-popup-desc)
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :overlayers overlayers)
                                  params)
                                db)]
    {:db (-> db
             (update-in (geop/running-tasks frame-id) assoc :overlayer task-id)
             (assoc-in (geop/selected-overlayers frame-id) overlayers)
             (assoc-in (geop/popup-desc frame-id) popup-desc))
     :fx (cond-> []
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                             {:action :overlayer
                              :payload (dissoc payload
                                               :raw-marker-layouts
                                               :raw-feature-layers)}]])
           (not live?)
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :overlayer
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- feature-layer-task
  "comes from the user"
  [db frame-id task-id {feature-layer-id :feature-layer-id
                        feature-layers :feature-layers
                        :as params} live?]
  (let [feature-layer (get-in db (conj geop/raw-feature-layers feature-layer-id))
        feature-layout (util/translate-layer feature-layer)
        current-layouts (get-in db (geop/selected-feature-layers frame-id))
        updated-layouts (if feature-layer-id
                          (assoc current-layouts feature-layer-id feature-layout)
                          (into {}
                                (map #(vector (:id %) %))
                                (util/raw-layer->translate-layer feature-layers)))
        temp-layouts (->> feature-layers
                          (filter :temporary?)
                          (map #(vector (:id %) %))
                          (into {}))
        removed-layouts (filterv #(not (contains? updated-layouts %))
                                 (keys current-layouts))
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :feature-layers updated-layouts)
                                  params)
                                db)]
    {:db (cond-> db
           :always (assoc-in (geop/selected-feature-layers frame-id) (:feature-layers payload))
           live? (assoc-in (geop/removed-feature-layers frame-id) removed-layouts)
           :always (update-in (geop/temp-raw-feature-layers frame-id) (fnil merge {}) temp-layouts)
           :always (update-in (geop/running-tasks frame-id) assoc :feature-layer task-id))
     :fx (cond-> [[:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id true :feature-layer-task]]]
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                             {:action :feature-layer
                              :payload (dissoc payload
                                               :raw-marker-layouts
                                               :raw-feature-layers)}]])
           :always
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :feature-layer
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- hide-feature-layer-task
  [db frame-id task-id {feature-layer-id :feature-layer
                        :as params} live?]
  (let [current-layouts (get-in db (geop/selected-feature-layers frame-id))
        updated-layouts (dissoc current-layouts feature-layer-id)
        {popup-feature-id :area-feature-id
         popup-feature-properties :feature-properties
         :as c-popup-desc} (get-in db (geop/popup-desc frame-id))
        popup-desc (if (and popup-feature-properties
                            (= feature-layer-id  popup-feature-id))
                     {}
                     c-popup-desc)
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :feature-layers updated-layouts
                                         :popup-desc popup-desc)
                                  params)
                                db)]
    {:db (-> db
             (assoc-in (geop/selected-feature-layers frame-id) updated-layouts)
             (assoc-in (geop/popup-desc frame-id) popup-desc)
             (update-in (geop/running-tasks frame-id) assoc :hide-feature-layer task-id)
             (update-in (geop/temp-raw-feature-layers frame-id) dissoc feature-layer-id))
     :fx (cond-> []
           live?
           (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation" {:action :hide-feature-layer
                                                                                                      :payload (dissoc payload
                                                                                                                       :raw-marker-layouts
                                                                                                                       :raw-feature-layers)}]])
           live?
           (conj [:dispatch [:de.explorama.frontend.map.map.core/update-instances frame-id]])
           (not live?)
           (conj [:backend-tube [ws-api/operation
                                 {:client-callback [ws-api/operation-result frame-id]
                                  :failed-callback [ws-api/operation-failed frame-id]
                                  :async-callback [ws-api/operation-async-acs frame-id]}
                                 {:task-type :hide-feature-layer
                                  :task-id task-id
                                  :payload payload
                                  :live? live?}]]))}))

(defn- popup-task
  [db frame-id task-id {:keys [event-id event-color marker?
                               overlayer-id feature-properties
                               clicked-position view-position
                               area-feature-id]
                        :as params} live?]
  (let [popup-desc {:event-id event-id
                    :event-color event-color
                    :marker? marker?
                    :overlayer-id overlayer-id
                    :area-feature-id area-feature-id
                    :feature-properties feature-properties
                    :clicked-position clicked-position
                    :view-position view-position}
        current-popup-desc (get-in db (geop/popup-desc frame-id) {})
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :popup-desc popup-desc)
                                  params)
                                db)]
    (if (not= current-popup-desc popup-desc)
      {:db (cond-> db
             :always (update-in (geop/running-tasks frame-id) assoc :popup task-id)
             live? (assoc-in (geop/popup-desc frame-id) popup-desc)
             live? (assoc-in (geop/frame-state-update frame-id) (js/Date.)))
       :fx (cond-> []
             live?
             (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                               {:action :popup
                                :payload (dissoc payload
                                                 :raw-marker-layouts
                                                 :raw-feature-layers)}]])
             (not live?)
             (conj [:backend-tube [ws-api/operation
                                   {:client-callback [ws-api/operation-result frame-id]
                                    :failed-callback [ws-api/operation-failed frame-id]
                                    :async-callback [ws-api/operation-async-acs frame-id]}
                                   {:task-type :popup
                                    :task-id task-id
                                    :payload payload
                                    :live? live?}]]))}
      {:fx [[:dispatch [::ddq/finish-task frame-id task-id ::popup]]]})))

(defn- position-change-task [db frame-id task-id {:keys [center zoom]
                                                  :as params}
                             live?]
  (let [curren-view-position (get-in db (geop/view-position frame-id))
        new-view-position {:center center
                           :zoom zoom}
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :view-position new-view-position)
                                  params)
                                db)]
    (if (not= curren-view-position new-view-position)
      {:db (if live?
             (assoc-in db (geop/view-position frame-id) new-view-position)
             (update-in db (geop/running-tasks frame-id) assoc :position-change task-id))
       :fx (cond-> []
             live?
             (conj [:dispatch [:de.explorama.frontend.map.event-logging/log-event frame-id "operation"
                               {:action :position-change
                                :payload (dissoc payload
                                                 :raw-marker-layouts
                                                 :raw-feature-layers)}]]
                   [:dispatch [::ddq/finish-task frame-id task-id ::position-change]])
             (not live?)
             (conj [:backend-tube [ws-api/operation
                                   {:client-callback [ws-api/operation-result frame-id]
                                    :failed-callback [ws-api/operation-failed frame-id]
                                    :async-callback [ws-api/operation-async-acs frame-id]}
                                   {:task-type :popup
                                    :task-id task-id
                                    :payload payload
                                    :live? live?}]]))}
      {:fx [[:dispatch [::ddq/finish-task frame-id task-id ::position-change]]]})))

(defn- higlight-marker-task [db frame-id task-id {:keys [highlighted-markers]
                                                  :as params} live?]
  (let [current-highlighted (get-in db (geop/highlighted-markers frame-id))
        payload (add-raw-config (if live?
                                  (assoc (build-base-payload db frame-id)
                                         :highlighted-markers highlighted-markers)
                                  params)
                                db)]
    (if (not= current-highlighted highlighted-markers)
      {:db (-> db
               (update-in (geop/running-tasks frame-id) assoc :highlight-marker task-id)
               (assoc-in (geop/highlighted-markers frame-id) highlighted-markers))}
      {:fx [[:dispatch [::ddq/finish-task frame-id task-id ::highlight-marker]]]})))

(defn- find-base-layer [db base-layer]
  (let [base-layers (get-in db geop/base-layers)
        base-layer
        (some (fn [{name :name}]
                (when (= name base-layer)
                  name))
              base-layers)]
    (or base-layer
        (:name (first (filter :default base-layers)))
        (:name (first base-layers)))))

(re-frame/reg-event-fx
 ws-api/operation-result
 (fn [{db :db} [_ frame-id {:keys [task-id task-type]
                            {:keys [stop-filterview? warn-filterview?
                                    data-count filtered-data-count
                                    best-marker-layouts usable-marker-layouts-id
                                    usable-feature-layouts-id filtered-marker-layout-data
                                    base-layer popup-desc
                                    event-data-desc cluster? view-position local-filter
                                    highlighted-markers overlayers too-much
                                    feature-layout-data filtered-feature-layout-data best-feature-layouts
                                    displayable-datapoints]
                             :as response} :response}]]
   (tufte/profile
    {:when config/profile-time}
    (when (ddq/is-current-job? db frame-id task-id)
      (tufte/p
       ::set-marker-data
       (map-api/set-marker-data frame-id filtered-marker-layout-data))
      (tufte/p
       ::set-feature-data
       (map-api/set-feature-data frame-id feature-layout-data))
      (tufte/p
       ::set-filtered-feature-data
       (map-api/set-filtered-feature-data frame-id filtered-feature-layout-data))
      (when-let [{:keys [event-id event-data]} event-data-desc]
        (tufte/p
         ::store-event-data-popup
         (map-api/store-event-data frame-id event-id event-data)))
      {:db (cond-> (-> db
                       (assoc-in (geop/frame-di-desc frame-id)
                                 (select-keys response [:years :countries
                                                        :datasources :dim-info
                                                        :filtered-data-infos]))
                       (assoc-in (geop/applied-filter frame-id) local-filter)
                       (assoc-in (geop/warn-filterview frame-id) warn-filterview?)
                       (assoc-in (geop/stop-filterview frame-id) stop-filterview?)
                       (assoc-in (geop/frame-count-global frame-id) data-count)
                       (assoc-in (geop/frame-count-local frame-id) data-count)
                       (assoc-in (geop/base-layer frame-id) (find-base-layer db base-layer))
                       (assoc-in (geop/popup-desc frame-id) popup-desc)
                       (assoc-in (geop/cluster-marker? frame-id) cluster?)
                       (assoc-in (geop/view-position frame-id) view-position)
                       (assoc-in (geop/highlighted-markers frame-id) highlighted-markers)
                       (assoc-in (geop/selected-overlayers frame-id) overlayers)
                       (assoc-in (geop/selected-feature-layers frame-id) best-feature-layouts)
                       (assoc-in (geop/selected-marker-layouts frame-id) best-marker-layouts)
                       (assoc-in (geop/frame-state-update frame-id) (js/Date.))
                       (update-in (geop/running-tasks frame-id) assoc task-type task-id))
             usable-marker-layouts-id
             (assoc-in (geop/usable-marker-layouts-id frame-id) usable-marker-layouts-id)
             usable-feature-layouts-id
             (assoc-in (geop/usable-feature-layouts-id frame-id) usable-feature-layouts-id)
             filtered-data-count (assoc-in (geop/frame-count-local frame-id) filtered-data-count)
             displayable-datapoints
             (assoc-in (geop/frame-displayable-data frame-id) displayable-datapoints))
       :fx [(if too-much
              [:dispatch [:de.explorama.frontend.map.views.stop-screen/stop-view-display frame-id :stop-view-stop-event-layer too-much]]
              [:dispatch [:de.explorama.frontend.map.views.stop-screen/stop-view-display frame-id nil]])
            (when too-much
              [:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id false ::op-result-too-much-data]])]}))))

(re-frame/reg-event-db
 ws-api/operation-async-acs
 (fn [db [_ frame-id data-acs]]
   (debug "Recived data-acs for frame " frame-id)
   (assoc-in db (geop/data-acs frame-id) data-acs)))

(re-frame/reg-event-fx
 ws-api/operation-failed
 (fn [{db :db} [_ frame-id {:keys [task-id base-layer error-desc] :as resp}]]
   (when (ddq/is-current-job? db frame-id task-id)
     (error "Operation execution failed" resp)
     {:db (-> db
              (assoc-in (geop/base-layer frame-id) (find-base-layer db base-layer))
              (assoc-in (geop/frame-state-update frame-id) (js/Date.)))
      :fx [[:dispatch [:de.explorama.frontend.map.views.stop-screen/stop-view-display frame-id
                       (case (:error error-desc)
                         :too-much-data :stop-view-too-much-data
                         :unknown :stop-view-unknown)
                       error-desc]]
           [:dispatch [::ddq/finish-task frame-id task-id ::operation-failed]]
           [:dispatch [:de.explorama.frontend.map.views.map/set-loading frame-id false ::op-failed]]]})))

(re-frame/reg-event-fx
 ::execute
 (fn [{db :db} [_ task-type frame-id params live? task-id]]
   ((case task-type
      :init-di init-di
      :copy-frame copy-frame-task
      :base-layer base-layer-task ;=> used by legend
      :marker marker-task ;=> used by legend
      :cluster-switch cluster-task
      :highlight-marker higlight-marker-task
      :overlayer overlayer-task ;=> used by legend
      :feature-layer feature-layer-task ;=> used by legend
      :hide-feature-layer hide-feature-layer-task ;=> used by legend
      :popup popup-task ;=> used by the map
      :position-change position-change-task;=> used by the map vio extra-fns 
      :filter filter-task)
    db frame-id task-id params live?)))

(re-frame/reg-event-fx
 ::execute-wrapper
 (fn [_ [_ frame-id action params & [live?]]]
   {:dispatch [::ddq/queue
               frame-id
               [::execute
                action
                frame-id
                params
                (if (boolean? live?)
                  live?
                  true)]]}))

(re-frame/reg-event-fx
 ::execute-wrapper-woq ;without-queue
 (fn [_ [_ frame-id action params task-id]]
   {:dispatch [::execute
               action
               frame-id
               params
               true
               task-id]}))
