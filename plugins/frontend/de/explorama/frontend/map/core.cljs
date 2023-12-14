(ns de.explorama.frontend.map.core
  (:require [data-format-lib.data-instance :as dfl-di]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.map.acs]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.configs.overlayer.core :as overlayer-config]
            [de.explorama.frontend.map.configs.util :as config-util]
            [de.explorama.frontend.map.event-logging :as event-log]
            [de.explorama.frontend.map.event-replay :as event-replay]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.map.config :as map-config]
            [de.explorama.frontend.map.map.core :as map]
            [de.explorama.frontend.map.map.geojson :as geojson-store]
            [de.explorama.frontend.map.operations.tasks :as tasks]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.plugin-impl :as plugi]
            [de.explorama.frontend.map.project.post-processing :as project-post-processing]
            [de.explorama.frontend.map.views.map :as views]
            [de.explorama.frontend.map.vis-state :as vis-state]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug error]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch project-post-processing/register-event]
           [:dispatch (tools-register {:id config/tool-name
                                       :component :map
                                       :icon "map"
                                       :action [::map-open]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :tooltip-text [::i18n/translate :vertical-label-map]
                                       :vertical config/default-vertical-str
                                       :tool-group :bar
                                       :bar-group :middle
                                       :sort-order 3})]
           [:dispatch (service-register :config-module
                                        :overlay-edit
                                        {:component overlayer-config/view})]
           [:dispatch (service-register :config-changed-action
                                        config/default-vertical-str
                                        {:on-change-event [::config-changed]
                                         :configs [:layouts :overlayers]})]
           [:dispatch (service-register :visual-option
                                        :map
                                        {:icon :map
                                         :sort-class "tool__map"
                                         :event ::map-open
                                         :tooltip [::i18n/translate :vertical-label-map]
                                         :tooltip-search [::i18n/translate :map-tooltip-search]})]
           [:dispatch (init-done config/default-vertical-str)]
           [:dispatch (info (str config/default-vertical-str " arriving!"))]]})))

(re-frame/reg-event-fx
 ::init-client
 (fn [_ [_ user-info]]
   {:fx [[:backend-tube [ws-api/load-layer-config
                         {:client-callback [ws-api/set-layer-config]}]]]}))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get frame-info-api-register-event-vec
                 frame-instance-api-register-event-vec]
          service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:fx [[:dispatch [::ready-check]]
           [:dispatch [::arrive]]
           [:dispatch (service-register :clean-workspace
                                        ::clean-workspace
                                        [::clean-workspace])]
           [:dispatch (service-register :event-protocol config/default-vertical-str event-log/events->steps)]
           [:dispatch (service-register :modules config/tool-name views/frame-body)]
           [:dispatch (service-register :logout-events :map-logout [::logout])]
           [:dispatch (service-register :frame-broadcast-receivers
                                        config/default-vertical-str
                                        [::event-replay/frame-broadcast-receiver])]
           [:dispatch (service-register :focus-event
                                        :map
                                        [:de.explorama.frontend.map.map.core/focus-event])]
           [:dispatch (frame-info-api-register-event-vec "map" {:local-filter #(get-in %1 (geop/applied-filter %2))
                                                                :datasources (fn [db frame-id]
                                                                               (when-let [dim-info (get-in db (geop/dim-info frame-id))]
                                                                                 (:datasources dim-info)))
                                                                :layouts #(mapv config-util/translated-layer->raw
                                                                                (get-in %1 (geop/selected-marker-layouts %2)))
                                                                :di #(get-in %1 (geop/frame-di %2))
                                                                :selections #(get-in %1 (geop/frame-selections %2))
                                                                :undo-event (fn [_] nil)
                                                                :custom {:map #(tasks/build-base-payload %1 %2)}})]
           [:dispatch (frame-instance-api-register-event-vec "map" {:resize-listener [::map/resize-listener]})]
           [:dispatch [::init-client user-info]]
           [:dispatch (papi-register config/default-vertical-str plugi/desc)]
           [:dispatch [::overlayer-config/get-acs]]
           [:dispatch [::event-log/log-pseudo-init]]
           [:dispatch [::map-config/init]]]})))

(re-frame/reg-event-fx
 ::ready-check
 (fn [{db :db} _]
   (let [default-layouts (fi/call-api [:config :get-config-db-get]
                                      db
                                      :default-layouts)
         layouts (fi/call-api [:config :get-config-db-get]
                              db
                              :layouts)
         default-overlayers (fi/call-api [:config :get-config-db-get]
                                         db
                                         :default-overlayers)
         overlayers (fi/call-api [:config :get-config-db-get]
                                 db
                                 :overlayers)
         layer-config (get-in db geop/layer-config)]
     (if (and (map? layouts)
              (map? overlayers)
              (map? layer-config))
       {:db (-> db
                (assoc-in geop/raw-marker-layouts (merge default-layouts layouts))
                (assoc-in geop/raw-feature-layers (merge default-overlayers overlayers)))
        :dispatch-n [(fi/call-api :service-register-event-vec
                                  :event-replay
                                  config/default-vertical-str
                                  {:event-replay ::event-replay/replay-events
                                   :replay-progress geop/replay-progress})
                     (fi/call-api :service-register-event-vec
                                  :event-sync
                                  config/default-vertical-str
                                  ::event-replay/sync-event)]}
       {:dispatch-later {:ms 500
                         :dispatch [::ready-check]}}))))

(re-frame/reg-event-fx
 ::fail-ok
 (fn [_ [_ message result]]
   (debug message result)
   {}))

(re-frame/reg-event-fx
 ::config-changed
 (fn [{db :db} [_ changed-config-types]]
   (reduce
    (fn [{db :db} config-type]
      (case config-type
        :layouts (let [default-layouts (fi/call-api [:config :get-config-db-get]
                                                    db
                                                    :default-layouts)
                       new-layouts (fi/call-api [:config :get-config-db-get]
                                                db
                                                :layouts)
                       new-raw-marker-layouts (merge default-layouts new-layouts)
                       deleted-layouts (reduce (fn [acc layout-id]
                                                 (if (nil? (get new-raw-marker-layouts layout-id))
                                                   (assoc acc
                                                          layout-id
                                                          (get-in db (conj geop/raw-marker-layouts layout-id)))
                                                   acc))
                                               {}
                                               (keys (get-in db geop/raw-marker-layouts)))]
                   {:db (assoc-in db geop/raw-marker-layouts new-raw-marker-layouts)
                    :dispatch-n (mapv (fn [frame-id]
                                        [::map/config-updated frame-id config-type deleted-layouts])
                                      (fi/call-api :list-frames-vertical-db-get db "map"))})
        :overlayers (let [default-overlayers (fi/call-api [:config :get-config-db-get]
                                                          db
                                                          :default-overlayers)
                          new-overlayers (fi/call-api [:config :get-config-db-get]
                                                      db
                                                      :overlayers)
                          new-raw-feature-layers (merge default-overlayers new-overlayers)
                          deleted-feature-layers (reduce (fn [acc layout-id]
                                                           (if (nil? (get new-raw-feature-layers layout-id))
                                                             (assoc acc
                                                                    layout-id
                                                                    (get-in db (conj geop/raw-feature-layers layout-id)))
                                                             acc))
                                                         {}
                                                         (keys (get-in db geop/raw-feature-layers)))]
                      {:db (assoc-in db geop/raw-feature-layers new-raw-feature-layers)
                       :dispatch-n (mapv (fn [frame-id]
                                           [::map/config-updated frame-id config-type deleted-feature-layers])
                                         (fi/call-api :list-frames-vertical-db-get db "map"))})
        {:db db}))
    {:db db}
    changed-config-types)))

(re-frame/reg-event-fx
 ws-api/set-layer-config
 (fn [{db :db} [_ layer-config geojsons]]
   (doseq [[file-path geojson] geojsons]
     (geojson-store/store-geojson file-path geojson))
   {:db (assoc-in db geop/layer-config layer-config)}))

(re-frame/reg-sub
 ::layer-config
 (fn [db _]
   (get-in db geop/layer-config)))

(re-frame/reg-event-fx
 ::clean-up
 (fn [_ [_ follow-event frame-ids]]
   {:dispatch-n (if (coll? frame-ids)
                  (mapv (fn [frame-id]
                          [::close-action frame-id (conj follow-event frame-id)])
                        frame-ids)
                  [[::clean-workspace follow-event]])}))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db     :db} [_ follow-event reason]]
   (let [frames (fi/call-api :list-frames-vertical-db-get db config/default-vertical-str)
         pseudo-init-log (when (not= reason :logout)
                           [::event-log/log-pseudo-init])]
     {:db           (update (if (seq frames)
                              (apply update db geop/root dissoc frames)
                              db)
                            geop/root
                            dissoc
                            geop/replay-progress-key)


      :dispatch-n   (conj (mapcat (fn [frame-id]
                                    (map-api/destroy-instance frame-id)
                                    [(fi/call-api :frame-delete-quietly-event-vec
                                                  frame-id)
                                     [::ddq/dispose-tracks frame-id
                                      [::map/dispose-state-tracker frame-id]]])
                                  frames)
                          pseudo-init-log
                          (conj follow-event ::clean-workspace))})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _] {}))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   {:id              frame-id
    :coords-in-pixel coords
    :size-in-pixel   size
    :size-min [300 275]
    :event           ::map-view-event
    :module          config/tool-name
    :vertical        config/default-vertical-str
    :data-consumer   true
    :type            :frame/content-type
    :resizable       true}))

(re-frame/reg-event-fx
 ::map-open
 (fn [_ [_ source-frame-id create-position ignore-scroll-position? opts]]
   {:dispatch (fi/call-api :frame-create-event-vec
                           (assoc (create-frame (or create-position
                                                    [100 200])
                                                [600 550])
                                  :ignore-scroll-position? ignore-scroll-position?
                                  :opts (merge {:publishing-frame-id source-frame-id}
                                               opts)))}))

(re-frame/reg-event-fx
 ::duplicate-map-frame
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ source-frame-id]]
   (let [source-infos (fi/call-api :frame-db-get db source-frame-id)
         copy-active? (fi/call-api [:product-tour :component-active?-db-get]
                                   db :amp :copy)]
     (when copy-active?
       (let [selections (fi/call-api :selections-db-get db source-frame-id)
             {pixel-coords :coords
              pixel-size :size
              source-title :title
              {original-pixel-coords :coords
               original-pixel-size :size} :before-minmaximized} source-infos
             pos (or original-pixel-coords pixel-coords)
             size (or original-pixel-size pixel-size)
             local-filter (get-in db (geop/applied-filter source-frame-id))
             frame-desc (assoc (create-frame pos size)
                               :ignore-scroll-position? true
                               :opts {:publishing-frame-id source-frame-id
                                      :overwrites {:info {:custom {:map {:type :duplicate
                                                                         :source-frame source-frame-id
                                                                         :source-title source-title
                                                                         :selections (assoc selections :copied? true)}}}

                                                   :attributes {:custom-title source-title
                                                                :last-applied-filters local-filter}}})]
         {:dispatch-n [(fi/call-api :frame-create-event-vec frame-desc)]})))))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id operation-type source-or-target params event]]
   (debug ::provide-content frame-id operation-type source-or-target params event)
   (cond
     (#{:difference
        :intersection-by
        :union
        :sym-difference} operation-type)
     (let [di (get-in db (geop/frame-di frame-id))
           local-filter (fi/call-api :frame-filter-db-get db frame-id)
           local-filter-id (dfl-di/ctn->sha256-id local-filter)
           new-di (-> di
                      (update :di/operations (fn [ops]
                                               [:filter local-filter-id ops]))
                      (assoc-in [:di/filter local-filter-id] local-filter))]
       (debug ::provide-content-set-ops di local-filter)
       (if (seq local-filter)
         {:dispatch (conj event {:di new-di
                                 :cancel? false})}
         {:dispatch (conj event {:di di
                                 :cancel? false})}))
     (#{:couple} operation-type)
     {:dispatch (conj event {:cancel? true})}
     (and (#{:override} operation-type)
          (= :target source-or-target))
     {:dispatch (conj event {:cancel? false})}
     (and (#{:override} operation-type)
          (= :source source-or-target))
     {:dispatch (conj event {:cancel? false})}
     :else
     {:dispatch (conj event {:cancel? true})})))

(defn- query-handler [db frame-id query callback-event]
  (case query
    :local-filter {:dispatch (conj callback-event
                                   (fi/call-api :frame-filter-db-get db frame-id))}
    :data-desc {:dispatch (conj callback-event
                                {:di (get-in db (geop/frame-di frame-id))
                                 :local-filter (fi/call-api :frame-filter-db-get db frame-id)})}
    :temp-layouts {:dispatch (conj callback-event
                                   {:selected-layouts (get-in db (geop/selected-marker-layouts frame-id))
                                    :temp-layouts (get-in db (geop/temp-raw-marker-layouts frame-id))
                                    :selected-feature-layers (get-in db (geop/selected-feature-layers frame-id))
                                    :temp-feature-layers (get-in db (geop/temp-raw-feature-layers frame-id))})}
    :vis-desc (let [vis-desc (vis-state/vis-desc db frame-id)]
                (fi/call-api :make-screenshot-raw
                             {:dom-id (config/frame-body-dom-id frame-id)
                              :callback-fn (fn [base64]
                                             (re-frame/dispatch (conj callback-event (assoc vis-desc
                                                                                            :preview base64))))})
                {})
    (error "unknown query" query)))

(re-frame/reg-event-fx
 ::map-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id frame-target-id callback-event query opts live? payload]} params
         duplicate? (= :duplicate (get-in payload [:custom :map :type]))
         [local-filter layouts di selections undo-event]
         (cond :else
               (let [{:keys [local-filter layouts di selections undo-event]} payload]
                 [local-filter layouts di selections undo-event]))]
     (debug (str "map-view event action: " action ", params: ") params)
     (case action
       :frame/init (if duplicate?
                     (let [source-frame (get-in payload [:custom :map :source-frame])]
                       {:db (assoc-in db (geop/frame-selections frame-id) selections)
                        :dispatch-n [[::ddq/register-tracks frame-id
                                      [::map/register-state-tracker frame-id]]
                                     [::ddq/queue frame-id
                                      [::map/initialize frame-id]]
                                     [::tasks/execute-wrapper frame-id :copy-frame {:source-frame-id source-frame}]
                                     (fi/call-api :frame-header-color-event-vec frame-id)
                                     (fi/call-api :frame-set-publishing-event-vec frame-id true)]})
                     {:dispatch-n
                      (cond-> [[::ddq/register-tracks frame-id
                                [::map/register-state-tracker frame-id]]
                               [::ddq/queue frame-id
                                [::map/initialize frame-id]]
                               [::tasks/execute-wrapper frame-id :init-di {:di di} live?]])
                      :db (cond-> db
                            (not-empty selections)
                            (assoc-in (geop/frame-selections frame-id) selections))})
       :frame/connect-to
       (cond-> {:db db}
         di
         (assoc :dispatch-n  [[:de.explorama.frontend.map.views.map/set-loading frame-target-id true ::connect-to]
                              [::tasks/execute-wrapper frame-target-id :init-di
                               {:di di
                                :local-filter local-filter
                                :highlighted-markers selections
                                :marker-layouts (get-in db (geop/selected-marker-layouts frame-target-id))
                                :feature-layers (get-in db (geop/selected-feature-layers frame-target-id))}
                               live?]])
         (:di/acs di)
         (assoc-in (into [:db] (geop/volatile-acs-frame frame-target-id)) (:di/acs di))
         selections
         (update :db
                 assoc-in
                 (geop/frame-selections frame-target-id)
                 (assoc selections :draw-selected? true :last-action :select))

         :always
         (update :db
                 assoc-in
                 (geop/frame-di frame-target-id)
                 di))
       :frame/query (query-handler db frame-id query callback-event)
       :frame/recreate
       (let [task-id (str (random-uuid))]
         {:db (-> db
                  (assoc-in (geop/volatile-acs-frame frame-id) (:di/acs di))
                  (assoc-in (geop/applied-filter frame-id) nil)
                  (assoc-in (geop/frame-di frame-id) di)
                  (assoc-in (geop/frame-task-id frame-id) task-id))
          :dispatch-n [[:de.explorama.frontend.map.views.map/set-loading frame-id true ::recreate]
                       [::tasks/execute-wrapper frame-id :init-di {:di di} live?]
                       (when callback-event
                         callback-event)]})
       :frame/update
       {:db (cond-> db
              :always
              (assoc-in (geop/frame-di frame-id) di)
              (vector? undo-event)
              (assoc-in (geop/undo-connection-update-event frame-id)
                        undo-event))
        :dispatch-n   [[:de.explorama.frontend.map.views.map/set-loading frame-id true ::connection-data-updated]
                       [::tasks/execute-wrapper frame-id :init-di
                        {:di di
                         :marker-layouts (get-in db (geop/selected-marker-layouts frame-id))
                         :feature-layers (get-in db (geop/selected-feature-layers frame-id))}
                        live?]]}
       :frame/selection
       {:dispatch [::map/update-selection frame-id selections]}
       :frame/connection-negotiation
       (let [{:keys [type frame-id result connected-frame-id]} params
             options
             (cond
               (and (empty? (get-in db (geop/frame-di frame-id)))
                    (= type :target))
               {:type :connect
                :frame-id frame-id
                :event [::provide-content frame-id]}
               (and (empty? (get-in db (geop/frame-di frame-id)))
                    (= type :source))
               {:type :cancel
                :frame-id frame-id
                :event [::provide-content frame-id]}
               :else
               {:type :options
                :frame-id frame-id
                :event [::provide-content frame-id]
                :options [{:label (i18n/translate db :contextmenu-operations-intersect)
                           :icon :intersect
                           :type :intersection-by
                           :params {:by "id"}}
                          {:label (i18n/translate db :contextmenu-operations-union)
                           :icon :union
                           :type :union}
                          {:label (i18n/translate db :contextmenu-operations-difference)
                           :icon :difference
                           :type :difference}
                          {:label (i18n/translate db :contextmenu-operations-symdifference)
                           :icon :symdiff
                           :type :sym-difference}
                          {:label (i18n/translate db :contextmenu-operations-override)
                           :icon :replace
                           :type :override}]})]
         {:dispatch (conj result options)})
       :frame/override
       {:db (-> db
                (assoc-in (geop/volatile-acs-frame frame-id) (:di/acs di))
                (assoc-in (geop/frame-di frame-id) di))
        :dispatch-n [[:de.explorama.frontend.map.views.map/set-loading frame-id true ::override]
                     [::tasks/execute-wrapper
                      frame-id
                      :init-di
                      {:di di
                       :local-filter local-filter}
                      live?]
                     (when (not-empty selections)
                       [::map/update-selection frame-id selections])
                     (when callback-event
                       callback-event)]}
       :frame/close {:db (geop/dissoc-in db (geop/volatile-acs-frame frame-id))
                     :dispatch [::close-action frame-id callback-event]}
       :connection/destroyed {}
       {}))))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ frame-id follow-event]]
   (debug "%% CLOSE %% map %%" frame-id)
   (map-api/destroy-instance frame-id)
   {:db         (update db geop/root dissoc frame-id)
    :fx [[:dispatch [::ddq/dispose-tracks frame-id
                     [::map/dispose-state-tracker frame-id]]]
         (when follow-event
           [:dispatch follow-event])]}))

(defn init []
  (register-init 0))
