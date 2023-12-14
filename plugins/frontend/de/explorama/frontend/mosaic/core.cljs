(ns de.explorama.frontend.mosaic.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.connection]
            [de.explorama.frontend.mosaic.css :as gcss]
            [de.explorama.frontend.mosaic.data-instances]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.data.di-acs]
            [de.explorama.frontend.mosaic.data.graph-acs :as gac]
            [de.explorama.frontend.mosaic.event-logging :as event-log]
            [de.explorama.frontend.mosaic.interaction-mode :refer [render?]]
            [de.explorama.frontend.mosaic.interaction.core]
            [de.explorama.frontend.mosaic.interaction.resize]
            [de.explorama.frontend.mosaic.interaction.state :as tooltip]
            [de.explorama.frontend.mosaic.mosaic :as gg]
            [de.explorama.frontend.mosaic.operations.core]
            [de.explorama.frontend.mosaic.operations.couple]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.operations.util :as gou]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.plugin-impl :as plugi]
            [de.explorama.frontend.mosaic.project.post-processing :as project-post-processing]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.config :as grconfig]
            [de.explorama.frontend.mosaic.render.core]
            [de.explorama.frontend.mosaic.render.pixi.common :as grpc]
            [de.explorama.frontend.mosaic.render.pixi.core :as pixicore]
            [de.explorama.frontend.mosaic.render.pixi.text-metrics]
            [de.explorama.frontend.mosaic.tracks]
            [de.explorama.frontend.mosaic.views.frame :as view]
            [de.explorama.frontend.mosaic.vis.config :as vis-config]
            [de.explorama.frontend.mosaic.vis.details]
            [de.explorama.frontend.mosaic.vis.state :as vis-state]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.shared.mosaic.layout :as grl]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log :refer [debug error]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::config-changed
 (fn [{db :db} [_ changed-config-types]]
   (reduce (fn [{:keys [db dispatch-n] :as fx} config-type]
             (if (= :layouts config-type)
               (let [default-layouts (fi/call-api [:config :get-config-db-get]
                                                  db
                                                  :default-layouts)
                     new-layouts (fi/call-api [:config :get-config-db-get]
                                              db
                                              :layouts)
                     ds-acs (gac/ac db)
                     new-layouts-id (set
                                     (map (fn [[_ {:keys [id]}]]
                                            id)
                                          new-layouts))
                     raw-layouts (merge default-layouts new-layouts)
                     {new-db :db new-dispatch-n :dispatch-n}
                     (reduce (fn [{:keys [db dispatch-n]} frame-id]
                               (let [usable-layout-ids (grl/usable-layouts ds-acs
                                                                           (get-in db (gp/dim-info frame-id))
                                                                           raw-layouts
                                                                           nil)
                                     selected-layouts (get-in db (gp/selected-layouts frame-id))
                                     layout-details (gcss/transform-style
                                                     (select-keys raw-layouts
                                                                  usable-layout-ids))
                                     layout-details-id-map (into {}
                                                                 (map (fn [[_ {id :id :as layout}]]
                                                                        [id layout])
                                                                      layout-details))
                                     db (-> (assoc-in db
                                                      (gp/usable-layouts frame-id)
                                                      usable-layout-ids)
                                            (assoc-in (gp/layout-details frame-id) layout-details))
                                     new-selected-layouts (mapv (fn [{:keys [id temporary?] :as layout}]
                                                                  (if (and (new-layouts-id id)
                                                                           (not temporary?))
                                                                    (get layout-details-id-map id)
                                                                    layout))
                                                                selected-layouts)]
                                 {:db db
                                  :dispatch-n (conj dispatch-n
                                                    [::tasks/execute-wrapper
                                                     (gp/top-level frame-id)
                                                     :layout
                                                     {:layouts new-selected-layouts}])}))
                             {:db db
                              :dispatch-n dispatch-n}
                             (keys (get-in db gp/instances)))]
                 {:db (assoc-in new-db gp/raw-layouts raw-layouts)
                  :dispatch-n new-dispatch-n})
               fx))
           {:db db
            :dispatch-n []}
           changed-config-types)))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch project-post-processing/register-event]
           [:dispatch (tools-register {:id vis-config/tool-name
                                       :icon "mosaic2"
                                       :component :mosaic
                                       :action [::mosaic-create]
                                       :tooltip-text [::i18n/translate :vertical-label-mosaic]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :vertical config/default-vertical-str
                                       :tool-group :bar
                                       :bar-group :middle
                                       :sort-order 1})]
           [:dispatch (service-register :config-changed-action
                                        config/default-vertical-str
                                        {:on-change-event [::config-changed]
                                         :configs [:layouts]})]
           [:dispatch (service-register :visual-option
                                        :mosaic
                                        {:icon :mosaic2
                                         :sort-class vis-config/tool-name
                                         :event ::mosaic-create
                                         :tooltip [::i18n/translate :vertical-label-mosaic]
                                         :tooltip-search [::i18n/translate :mosaic-tooltip-search]})]
           [:dispatch (init-done config/default-vertical-str)]
           [:dispatch (info (str config/default-vertical-str " arriving!"))]]})))

(re-frame/reg-event-fx
 ::on-mouse-up
 (fn [_ [_ create-frame-x create-frame-y _event]]
   (when (and @grpc/drag-interaction
              @grpc/drag-interaction-left)
     (let [[modifier coords page path] @grpc/drag-interaction]
       (reset! grpc/drag-interaction nil)
       {:dispatch-n [[:de.explorama.frontend.mosaic.operations.util/copy-group-ui-wrapper-pixi path page [create-frame-x create-frame-y] coords modifier]
                     (fi/call-api :reset-vertical-drag-event-vec)]}))))

(re-frame/reg-event-fx
 ::init-event
 (fn [_ _]
   (let [{:keys [frame-info-api-register-event-vec
                 frame-instance-api-register-event-vec]
          service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)]
     (pixicore/load-resources)
     {:dispatch-n (concat [[::arrive]
                           [:de.explorama.frontend.mosaic.connection/initialize]
                           [::ready-check]
                           (service-register :frame-broadcast-receivers
                                             config/default-vertical-str
                                             [:de.explorama.frontend.mosaic.event-replay/frame-broadcast-receiver])

                           (service-register
                            :clean-workspace
                            ::clean-workspace
                            [::clean-workspace])
                           (service-register :focus-event
                                             :mosaic
                                             [:de.explorama.frontend.mosaic.render.core/focus-event])
                           (service-register :event-protocol config/default-vertical-str event-log/events->steps)
                           (service-register :modules vis-config/tool-name view/frame-body)
                           (service-register :logout-events :mosaic-logout [::logout])
                           (papi-register config/default-vertical-str plugi/desc)
                           (frame-info-api-register-event-vec "mosaic" {:local-filter #(get-in %1 (gp/applied-filter %2))
                                                                        :datasources (fn [db frame-id]
                                                                                       (when-let [dim-info (get-in db (gp/dim-info frame-id))]
                                                                                         (:datasources dim-info)))
                                                                        :layouts #(get-in %1 (gp/selected-layouts %2))
                                                                        :di #(get-in %1 (gp/data-instance %2))
                                                                        :selections #(get-in %1 (gp/selections %2))
                                                                        :undo-event (fn [_] nil)
                                                                        :custom {:mosaic #(get-in %1 (gp/operation-desc %2))}})
                           (frame-instance-api-register-event-vec "mosaic" {:couple-infos {:on-couple :de.explorama.frontend.mosaic.operations.couple/couple,
                                                                                           :on-action :de.explorama.frontend.mosaic.operations.couple/action,
                                                                                           :on-decouple :de.explorama.frontend.mosaic.operations.couple/decouple}
                                                                            :resize-listener [:de.explorama.frontend.mosaic.interaction.resize/resize-listener]
                                                                            :minmax {:before []
                                                                                     :after []}})
                           [::event-log/log-pseudo-init]
                           [::grconfig/init]])})))

(re-frame/reg-event-fx
 ::ready-check
 (fn [{db :db} _]
   (let [layouts (fi/call-api [:config :get-config-db-get]
                              db
                              :layouts)
         service-register (fi/api-definition :service-register-event-vec)]
     (if (map? layouts)
       {:db (assoc-in db gp/raw-layouts layouts)
        :dispatch-n [(service-register :event-replay
                                       config/default-vertical-str
                                       {:event-replay :de.explorama.frontend.mosaic.event-replay/replay-events
                                        :replay-progress gp/replay-progress})
                     (service-register :event-sync
                                       config/default-vertical-str
                                       :de.explorama.frontend.mosaic.event-replay/sync-event)]}
       {:dispatch-later {:ms 500
                         :dispatch [::ready-check]}}))))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   {:id frame-id
    :module vis-config/tool-name
    :coords-in-pixel coords
    :size-in-pixel size
    :size-min vis-config/min-frame-size
    :event ::mosaic-view-event
    :vertical config/default-vertical-str
    :data-consumer true
    :type :frame/content-type
    :resizable true}))

(re-frame/reg-event-fx
 ::mosaic-create
 (fn [_ [_ source-frame-id create-position ignore-scroll-position? opts]]
   {:dispatch (fi/call-api :frame-create-event-vec
                           (assoc (create-frame (or create-position [100 200]) vis-config/frame-size)
                                  :ignore-scroll-position? ignore-scroll-position?
                                  :opts (merge {:publishing-frame-id source-frame-id}
                                               opts)))}))

(re-frame/reg-event-fx
 ::create-copy-frame ;Groups/Subgroups
 (fn [_ [_ frame-id desc]]
   {:dispatch (fi/call-api :frame-create-event-vec
                           (assoc (create-frame [0 0] vis-config/frame-size)
                                  :ignore-scroll-position? true
                                  :opts {:publishing-frame-id frame-id
                                         :overwrites (cond-> {:info {:custom {:mosaic desc}}}
                                                       (:overwrite-behavior? desc)
                                                       (assoc :behavior {:force :drop})
                                                       (:group-title desc)
                                                       (assoc :attributes {:custom-title (:group-title desc)}))}))}))

(re-frame/reg-event-fx
 ::duplicate-mosaic-frame
 [(.-uiInterceptor js/window)]
 (fn [{db :db} [_ source-frame-id]]
   (let [source-infos (fi/call-api :frame-db-get db source-frame-id)
         copy-active? (fi/call-api [:product-tour :component-active?-db-get]
                                   db :mosaic :copy)]
     (when copy-active?
       (let [{pixel-coords :coords
              pixel-size :size
              source-title :title} source-infos
             pos pixel-coords
             size pixel-size
             local-filter (get-in db (gp/applied-filter source-frame-id))
             frame-desc (assoc (create-frame pos size)
                               :ignore-scroll-position? true
                               :opts {:publishing-frame-id source-frame-id
                                      :overwrites {:info {:custom {:mosaic {:type :duplicate
                                                                            :local-filter local-filter
                                                                            :layouts (get-in db (gp/selected-layouts source-frame-id))
                                                                            :di (get-in db (gp/data-instance source-frame-id))
                                                                            :selections (get-in db (gp/selections source-frame-id))
                                                                            :operation-desc (dissoc (get-in db (gp/operation-desc source-frame-id))
                                                                                                    gcp/coupled-key
                                                                                                    gcp/couple-key)}}}
                                                   :attributes {:custom-title source-title
                                                                :last-applied-filters local-filter}}})]
         {:dispatch (fi/call-api :frame-create-event-vec frame-desc)})))))

(re-frame/reg-event-fx
 ::start-instance
 (fn [{db :db} [_ di frame-id local-filter
                {mosaic-operation-desc :mosaic-operation-desc
                 source-action :source-action}
                task-id]]
   (debug "start-instance" di frame-id local-filter task-id)
   (if (some? di)
     (let [{:keys [db dispatch-n]} (fi/call-api [:details-view :remove-frame-events-from-details-view-db-update]
                                                db frame-id)]
       (cond-> {:dispatch-n [(fi/call-api :frame-notifications-clear-event-vec frame-id)
                             [:de.explorama.frontend.mosaic.operations.tasks/execute
                              :init
                              frame-id
                              (cond-> {:di di
                                       :operation-desc mosaic-operation-desc}
                                source-action
                                (assoc :source-action source-action)
                                local-filter
                                (assoc :local-filter local-filter))
                              true
                              task-id]]}
         db (assoc :db db)
         dispatch-n (update :dispatch-n (fn [o] (apply conj o dispatch-n)))))
     {})))

(re-frame/reg-event-fx
 ::final-close
 (fn [{db :db} [_ frame-id]]
   (gdb/unset-events! frame-id)
   (gdb/unset-scale! frame-id)
   (gdb/unset-annotations! frame-id)
   (grc/delete-frame-events frame-id)
   {:db (-> db
            (update-in gp/filter-view dissoc frame-id)
            (update gp/mosaic-root dissoc frame-id)
            (update-in gp/instances dissoc frame-id)
            (update-in gp/render-wait-root dissoc frame-id)
            (update :de.explorama.frontend.mosaic.views.frame/stop-view-too-much-data dissoc frame-id))}))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ frame-id close-event]]
   (let [{:keys [db dispatch-n]}
         (fi/call-api [:details-view
                       :remove-frame-events-from-details-view-db-update]
                      db frame-id)]
     (cond-> {:dispatch-n (conj (or dispatch-n [])
                                [:de.explorama.frontend.mosaic.operations.core/on-canvas-exit
                                 (gp/frame-path-id frame-id)
                                 [[::final-close frame-id]
                                  close-event]])
              :dispatch-later {:ms 25
                               :dispatch [:de.explorama.frontend.mosaic.render.exit/exit-frame frame-id]}}
       db (assoc :db db)))))

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
 (fn [{db :db}
      [_ follow-event reason]]
   (let [frame-ids (vec (keys (get-in db gp/instances)))
         follow-event (conj follow-event ::clean-workspace)
         pseudo-init-log (when (not= reason :logout)
                           [::event-log/log-pseudo-init])]
     (reset! tooltip/allow-tooltips-state {})
     (reset! tooltip/state {})
     (if (empty? frame-ids)
       {:fx (into [(when follow-event
                     [:dispatch follow-event])
                   (when pseudo-init-log
                     [:dispatch pseudo-init-log])])}
       {:fx (into [(when pseudo-init-log
                     [:dispatch pseudo-init-log])]
                  (conj (mapv (fn [frame-id]
                                [:dispatch [::close-action frame-id [::frame-done frame-id]]])
                              frame-ids)
                        [:dispatch [::all-frames-done follow-event]]))}))))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]))
  ;;  {:fx []]}))

(re-frame/reg-event-fx
 ::frame-done
 (fn [_ [_ frame-id]]

   {:dispatch (fi/call-api :frame-delete-quietly-event-vec
                           frame-id)}))

(re-frame/reg-event-fx
 ::all-frames-done
 (fn [{db :db} [_ follow-event]]
   (let [frame-ids (fi/call-api :list-frames-vertical-db-get
                                db config/default-vertical-str)]
     (if (empty? frame-ids)
       {:db (-> db
                (update gp/mosaic-root dissoc gp/instances-key)
                (update gp/mosaic-root dissoc gp/filter-view-key)
                (update gp/mosaic-root dissoc gp/replay-progress-key)
                (update gp/mosaic-root dissoc gp/top-level-render-wait-key)
                (gp/dissoc-in gp/canvas-states-replay))
        :dispatch-n [follow-event]}
       {:dispatch-later {:ms 100
                         :dispatch [::all-frames-done follow-event]}}))))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id operation-type source-or-target params event]]
   (debug ::provide-content frame-id operation-type source-or-target params event)
   ;; Is not a valid-frame-id here when copied from non-frame (e.g. subgroup)
   ;; If it's a subgroup drag-infos/path/drag-and-drop? is set
   (let [{:keys [drag-and-drop?] {path :path :as drag-infos} :drag-infos} frame-id]
     (cond
       (and (#{:difference
               :intersection-by
               :union
               :sym-difference} operation-type)
            (not drag-and-drop?))
       (let [di (get-in db (gp/data-instance frame-id))
             local-filter (fi/call-api :frame-filter-db-get db frame-id)
             new-di (gou/add-filter-to-di di local-filter)]
         (if (and local-filter (not-empty local-filter))
           {:dispatch (conj event {:di new-di
                                   :cancel? false})}
           {:dispatch (conj event {:di di
                                   :cancel? false})}))

       (and (#{:difference
               :intersection-by
               :union
               :sym-difference} operation-type)
            drag-and-drop?)
       (let [new-filter (gou/create-sub-group-filter db drag-infos)
             di (get-in db (gp/data-instance path))
             new-di (gou/add-filter-to-di di new-filter)]
         {:dispatch (conj event {:di new-di
                                 :cancel? false})})

       (and (#{:couple} operation-type)
            (not drag-and-drop?))
       {:dispatch (conj event {:cancel? false})}

       (and (#{:override} operation-type)
            (= :source source-or-target)
            drag-and-drop?)
       (let [new-filter (gou/create-sub-group-filter db drag-infos)
             di (get-in db (gp/data-instance path))
             new-di (gou/add-filter-to-di di new-filter)]
         {:dispatch (conj event {:di new-di
                                 :cancel? false})})

       (and (#{:override} operation-type)
            (= :source source-or-target)
            (not drag-and-drop?))
       {:dispatch (conj event {:cancel? false})}

       (and (#{:override} operation-type)
            (= :target source-or-target))
       {:dispatch (conj event {:cancel? false})}

       :else
       {:dispatch (conj event {:cancel? true})}))))

(defn context-menu-child-items [data-acs]
  (debug "context-menu-child-items" data-acs)
  (mapv (fn [{:keys [name key]}]
          {:label name
           :params {:by key}})
        data-acs))

(re-frame/reg-event-fx
 ::excecute-connect
 (fn [{db :db} [_ frame-target-id di selections source-local-filter mosaic-operation-desc]]
   (debug "execute connect" frame-target-id di selections source-local-filter)
   (when (not= di (get-in db (gp/data-instance frame-target-id)))
     (cond-> {:fx (cond-> [[:dispatch [::ddq/queue frame-target-id
                                       [::start-instance
                                        di
                                        frame-target-id
                                        (if source-local-filter
                                          source-local-filter
                                          nil)
                                        {:mosaic-operation-desc mosaic-operation-desc}]]]]
                    selections
                    (conj [:dispatch [::ddq/queue frame-target-id
                                      [:de.explorama.frontend.mosaic.render.core/highlight-instance frame-target-id]]]))}
       selections
       (assoc :db (assoc-in db (gp/selections frame-target-id) selections))))))

#_; TODO r11/window-handling necessary??
  (re-frame/reg-event-fx
   ::apply-layouts
   (fn [{db :db} [_ frame-target-id di {:keys [group-layer selections]} {:keys [temp-layouts selected-layouts]}]]
     (debug "execute connect" frame-target-id di group-layer selections temp-layouts selected-layouts)
     {:db (update-in db (gp/selected-layouts frame-target-id) #(if selected-layouts selected-layouts %))
      :fx [[:dispatch [:de.explorama.frontend.mosaic.configs.layout.core/change-design frame-target-id temp-layouts]]]}))

(defn- handle-frame-query [db frame-id query callback-event]
  (case query
    :local-filter {:dispatch (conj callback-event
                                   (fi/call-api :frame-filter-db-get db frame-id))}
    :data-desc {:dispatch (conj callback-event
                                {:di (get-in db (gp/data-instance frame-id))
                                 :local-filter (fi/call-api :frame-filter-db-get db frame-id)})}
    :temp-layouts {:dispatch (conj callback-event
                                   {:selected-layouts (get-in db (gp/selected-layouts frame-id))
                                    :temp-layouts (get-in db (gp/selected-layouts frame-id))})}
    :vis-desc (let [vis-desc (vis-state/get-state db frame-id)]
                (fi/call-api :make-screenshot-raw
                             {:dom-id (vis-config/frame-body-dom-id frame-id)
                              :callback-fn (fn [base64]
                                             (re-frame/dispatch (conj callback-event (assoc vis-desc
                                                                                            :preview base64))))})
                {})
    (error "unknown query" query)))

(re-frame/reg-event-fx
 ::mosaic-view-event
 (fn [{db :db}
      [_ action params]]
   (let [is-in-render-mode? ((render?) db)
         {:keys [coupled-with-db-get decouple-event-vec]} (fi/api-definitions)
         {:keys [frame-id size callback-event frame-target-id payload query]} params
         copy-group? (= :copy-group (get-in payload [:custom :mosaic :type]))
         duplicate? (= :duplicate (get-in payload [:custom :mosaic :type]))
         coupled-with (coupled-with-db-get db (case action
                                                :frame/connect-to frame-target-id
                                                frame-id))
         [local-filter layouts di selections operation-desc undo-event source-action]
         (cond (and (get-in payload [:custom :mosaic])
                    (= :frame/override action))
               (let [operation-desc (get-in payload [:custom :mosaic])
                     {:keys [local-filter layouts di selections]} payload]
                 [local-filter layouts di selections operation-desc nil nil])
               duplicate?
               (let [{:keys [local-filter layouts di selections operation-desc]} (get-in payload [:custom :mosaic])]
                 [local-filter layouts di selections operation-desc nil :duplicate])
               copy-group?
               (let [{:keys [di layouts operation-desc source-action]} (get-in payload [:custom :mosaic])]
                 [nil layouts di nil operation-desc nil source-action])
               :else
               (let [{:keys [local-filter layouts di selections undo-event]} payload]
                 [local-filter layouts di selections {} undo-event nil]))]
     (debug "mosaic-view-event" action params)
     (reset! grpc/drag-interaction nil)
     (case action
       :frame/query (handle-frame-query db frame-id query callback-event)
       :frame/init
       (do
         (when layouts
           (gcss/transform-style
            (reduce (fn [result layout]
                      (assoc result (get layout :id) layout))
                    {} layouts)))
         {:db (cond-> (gg/initialize db frame-id size)
                operation-desc
                (assoc-in (gp/operation-desc frame-id) operation-desc)
                di (-> (assoc-in (gp/data-instance frame-id) di)
                       (assoc-in (gp/volatile-acs-frame frame-id) (:di/acs di))))
          :dispatch-n [(when (and is-in-render-mode? coupled-with)
                         (decouple-event-vec frame-id))
                       (when (or copy-group?
                                 duplicate?)
                         (fi/call-api :frame-header-color-event-vec frame-id))
                       (when (or copy-group?
                                 duplicate?)
                         (fi/call-api :frame-set-publishing-event-vec frame-id true))
                       [::ddq/register-tracks frame-id
                        [:de.explorama.frontend.mosaic.operations.core/track-register-canvas-rendering (gp/canvas frame-id)]]
                       (when di
                         [::tasks/execute-wrapper
                          (gp/top-level frame-id)
                          :init
                          (cond-> {:operation-desc (assoc operation-desc :selections selections)
                                   :local-filter local-filter
                                   :di di
                                   :layouts layouts}
                            source-action
                            (assoc :source-action source-action))])]})
       :frame/update
       (cond di
             {:db (cond-> db
                    di (-> (assoc-in (gp/data-instance frame-id) di)
                           (assoc-in (gp/volatile-acs-frame frame-id) (:di/acs di))))
              :fx [[:dispatch [::ddq/queue frame-id
                               [::start-instance di frame-id local-filter nil]]]
                   (when callback-event
                     [:dispatch callback-event])]}
             :else {})
       :frame/recreate
       {:db (-> db
                (assoc-in (gp/ignore-redo-ops frame-id) true)
                (assoc-in (gp/applied-filter frame-id) nil)
                (assoc-in (gp/volatile-acs-frame frame-id) (:di/acs di))
                (assoc-in (gp/operation-desc frame-id) {}))
        :fx [(when (and is-in-render-mode? coupled-with)
               [:dispatch (decouple-event-vec frame-id)])
             [:dispatch [::ddq/queue frame-id
                         [::start-instance di frame-id local-filter {:source-action :recreate}]]]
             (when callback-event
               [:dispatch callback-event])]}
       :frame/connection-negotiation
       (let [top-level-path (if (:drag-and-drop? frame-id)
                              (gp/top-level (get-in frame-id [:drag-infos :path]))
                              (gp/top-level frame-id))
             {:keys [type frame-id connected-frame-id result coupled?]} params
             options
             (cond
               (and (empty? (get-in db (gp/data-instance frame-id)))
                    (= type :target)
                    (not (:drag-and-drop? frame-id)))
               {:type :connect
                :frame-id frame-id
                :event [::provide-content frame-id]}
               (and (empty? (get-in db (gp/data-instance frame-id)))
                    (= type :source)
                    (not (:drag-and-drop? frame-id)))
               {:type :cancel
                :frame-id frame-id
                :event [::provide-content frame-id]}
               :else
               {:type :options
                :frame-id frame-id
                :event [::provide-content frame-id]
                :options (cond-> [{:label (i18n/translate db :contextmenu-operations-intersect)
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
                                   :type :override}]
                           (and (= config/default-vertical-str (:vertical connected-frame-id))
                                (not coupled?)
                                (not (:drag-and-drop? frame-id))
                                (= gcp/render-mode-key-raster
                                   (get (get-in db (gp/operation-desc frame-id))
                                        gcp/render-mode-key)))
                           (conj {:label (i18n/translate db :contextmenu-operations-couple-by)
                                  :icon :couple
                                  :type :couple
                                  :children
                                  (context-menu-child-items (gac/couple-by db top-level-path))}))})]
         {:dispatch (conj result options)})
       :frame/override
       {:db (-> (assoc-in db (gp/ignore-redo-ops frame-id) true)
                (assoc-in (gp/volatile-acs-frame frame-id) (:di/acs di))
                (assoc-in (gp/operation-desc frame-id) operation-desc))
        :fx [(when (and is-in-render-mode? coupled-with)
               [:dispatch (decouple-event-vec frame-id)])
             [:dispatch [::ddq/queue frame-id
                         [::start-instance di frame-id local-filter {:source-action :override}]]]
             (when callback-event
               [:dispatch callback-event])]}
       :frame/connect-to
       (if di
         {:db (cond-> (-> db
                          (assoc-in (gp/volatile-acs-frame frame-target-id) (:di/acs di))
                          (assoc-in (gp/ignore-redo-ops frame-target-id) true))
                (and (vector? undo-event)
                     (get-in db (gp/frame-desc frame-target-id)))
                (assoc-in (gp/undo-connection-update-event frame-target-id) undo-event))
          :dispatch-n [(when (and is-in-render-mode? coupled-with)
                         (decouple-event-vec frame-target-id))
                       [::excecute-connect frame-target-id di selections local-filter operation-desc]]}
         {})
       :frame/close
       {:db (gp/dissoc-in db (gp/volatile-acs-frame frame-id))
        :dispatch [::close-action frame-id callback-event]}
       :frame/selection
       {:db (assoc-in db (gp/selections frame-id) selections)
        :dispatch [:de.explorama.frontend.mosaic.render.core/highlight-instance frame-id]}
       {}))))

;! TODO Move me somewehere else :D

(re-frame/reg-event-db
 ::register-render-wait
 (fn [db [_ frame-id]]
   (assoc-in db (gp/render-wait frame-id) true)))

(re-frame/reg-event-fx
 ::render-done-top-level
 (fn [{db :db} [_ path]]
   (let [frame-id (gp/frame-id path)]
     {:db (assoc-in db (gp/render-wait frame-id) false)
      :dispatch-later {:ms 300
                       :dispatch (fi/call-api :render-done-event-vec
                                              frame-id config/default-namespace)}})))

(defn init []
  (register-init 0))