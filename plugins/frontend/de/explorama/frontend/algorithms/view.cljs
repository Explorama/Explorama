(ns de.explorama.frontend.algorithms.view
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.algorithms.components.main :as main]
            [de.explorama.frontend.algorithms.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.algorithms.operations.redo :as redo]
            [de.explorama.frontend.algorithms.path.core :as paths]
            [de.explorama.frontend.algorithms.vis-state :as vis-state]
            [taoensso.timbre :refer [debug error]]
            [de.explorama.shared.algorithms.ws-api :as ws-api]))

(def module-key "algorithms-window")

(re-frame/reg-event-fx
 ::debug-log
 (fn [_ [_ message]]
   (debug message)
   {}))

(re-frame/reg-event-fx
 ::connect-to-di
 (fn [{db :db} [_ frame-id di]]
   (let [is-in-render-mode? (fi/call-api [:interaction-mode :render-db-get?]
                                         db)
         operations-state (when is-in-render-mode?
                            (redo/build-operations-state db frame-id))
         task-id (str (random-uuid))]
     {:db (-> db
              (assoc-in (paths/data-instance-consuming frame-id) di)
              (assoc-in (paths/loading frame-id) true)
              (assoc-in (paths/connect-task-id frame-id) task-id)
              (paths/reset-stop-views frame-id))
      :dispatch (fi/call-api :frame-notifications-clear-event-vec frame-id) #_[:de.explorama.frontend.algorithms.components.frame-notifications/clear-notification frame-id]
      :backend-tube-n [[ws-api/data-options
                        {:client-callback [ws-api/data-options-result frame-id false nil task-id]}
                        di operations-state]]})))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id operation-type source-or-target params event]]
   (debug ::provide-content frame-id operation-type source-or-target params event)
   (cond
     (#{:difference
        :intersection-by
        :union
        :sym-difference} operation-type)
     (let [di (or (get-in db (paths/data-instance-publishing frame-id))
                  (get-in db (paths/data-instance-consuming frame-id)))]
       (debug ::provide-content-set-ops di)
       {:dispatch (conj event {:di di
                               :cancel? false})})
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
    :local-filter {:dispatch (conj callback-event nil)}
    :data-desc {:dispatch (conj callback-event
                                {:di (or (get-in db (paths/data-instance-publishing frame-id))
                                         (get-in db (paths/data-instance-consuming frame-id)))})}
    :temp-layouts {:dispatch (conj callback-event {})}
    :vis-desc (let [vis-desc (vis-state/vis-desc db frame-id)]
                (fi/call-api :make-screenshot-raw
                             {:dom-id (config/frame-body-dom-id frame-id)
                              :callback-fn (fn [base64]
                                             (re-frame/dispatch (conj callback-event (assoc vis-desc
                                                                                            :preview base64))))})
                {})
    (error "unknown query" query)))

(re-frame/reg-event-fx
 ::view-event
 (fn [{db :db}
      [_ action params]]
   (let [user-info (fi/call-api :user-info-db-get db)
         {:keys [frame-id callback-event frame-target-id replay? payload query]} params
         duplicate? (= :duplicate (get-in payload [:algorithms :type]))
         [local-filter di undo-event]
         (cond :else
               (let [{:keys [local-filter di undo-event]} payload]
                 [local-filter di undo-event]))]
     (debug "algorithms view-event" action params)
     (case action
       :frame/init
       (cond-> {:db (assoc-in db (paths/frame frame-id) {})}

         :always
         (assoc :dispatch-n [])

         :always
         (assoc :backend-tube [ws-api/load-predictions
                               {:callback-event [ws-api/load-predictions-result]}
                               (:username user-info)])

         (and di (not replay?))
         (update :dispatch-n
                 conj
                 [::connect-to-di frame-id di nil]
                 ; Legacy api
                 [:de.explorama.frontend.algorithms.event-logging/log-event frame-id "connect" {:connection-data {:di di}
                                                                                                :frame-target-id frame-id}])

         (and callback-event (not duplicate?))
         (update :dispatch-n conj callback-event)

         duplicate?
         (update :dispatch-n
                 conj
                 [::duplicate-frame-infos-export frame-id (get payload :algorithms) callback-event]
                 (fi/call-api :frame-header-color-event-vec frame-id)
                 (fi/call-api :frame-set-publishing-event-vec frame-id true)))
       :frame/connect-to
       (when di
         {:db (cond-> db
                (vector? undo-event) (assoc-in (paths/undo-connection-update-event frame-id)
                                               undo-event)
                :always (update-in paths/data-instances (fnil conj #{}) di))
          :backend-tube-n [(when local-filter [:de.explorama.frontend.algorithms.handler/filter-data di frame-target-id local-filter])]
          :dispatch-n [[::connect-to-di frame-target-id di nil]
                       ; Legacy api
                       [:de.explorama.frontend.algorithms.event-logging/log-event frame-target-id "connect" {:connection-data {:di di}}
                        :frame-target-id frame-target-id]
                       [:de.explorama.frontend.algorithms.view/set-filter di frame-target-id local-filter]
                       (when callback-event
                         callback-event)]})
       :frame/query (query-handler db frame-id query callback-event)
       :frame/recreate
       {:db (update-in db paths/data-instances (fnil conj #{}) di)
        :dispatch-n [[::connect-to-di frame-id di nil]
                     (when callback-event
                       callback-event)]}
       :frame/update
       (cond
         di
         {:db (cond-> db
                (vector? undo-event) (assoc-in (paths/undo-connection-update-event frame-id)
                                               undo-event)
                :always (update-in paths/data-instances (fnil conj #{}) di))
          :dispatch-n [[::connect-to-di frame-id di nil]
                       [:de.explorama.frontend.algorithms.event-logging/log-event frame-id "connect" {:connection-data {:di di}}
                        :frame-target-id frame-id]]}

         :else {})
       :frame/connection-negotiation
       (let [{:keys [type frame-id result connected-frame-id]} params
             options
             (cond
               (and (empty? (get-in db (paths/data-instance-consuming frame-id)))
                    (= type :target))
               {:type :connect
                :frame-id frame-id
                :event [::provide-content frame-id]}
               (and (empty? (get-in db (paths/data-instance-publishing frame-id)))
                    (empty? (get-in db (paths/data-instance-consuming frame-id)))
                    (= type :source))
               {:type :cancel
                :frame-id frame-id
                :event [::provide-content frame-id]}
               (and (get-in db (paths/data-instance-publishing frame-id))
                    (= type :source))
               {:type :connect
                :frame-id frame-id
                :event [::provide-content frame-id]}
               :else
               {:type :options
                :frame-id frame-id
                :event [::provide-content frame-id]
                :options [#_{:label (i18n/translate db :contextmenu-operations-intersect)
                             :icon :intersect
                             :type :intersection-by
                             :params {:by "id"}}
                          #_{:label (i18n/translate db :contextmenu-operations-union)
                             :icon :union
                             :type :union}
                          #_{:label (i18n/translate db :contextmenu-operations-difference)
                             :icon :difference
                             :type :difference}
                          #_{:label (i18n/translate db :contextmenu-operations-symdifference)
                             :icon :symdiff
                             :type :sym-difference}
                          {:label (i18n/translate db :contextmenu-operations-override)
                           :icon :replace
                           :type :override}]})]
         {:dispatch (conj result options)})
       :frame/override
       {:db (update-in db paths/data-instances (fnil conj #{}) di)
        :dispatch-n [[::connect-to-di frame-id di nil]
                     [:de.explorama.frontend.algorithms.event-logging/log-event frame-target-id "update-connection" {:connection-data {:di di}}
                      :frame-target-id frame-id]
                     [:de.explorama.frontend.algorithms.view/set-filter di frame-id local-filter]
                     (when callback-event
                       callback-event)]}
       :frame/close
       {:dispatch [::close-action frame-id callback-event]}

       {}))))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ frame-id follow-event]]
   (debug "%% CLOSE %% algorithms %%" frame-id)
   (let [old-di (get-in db (paths/data-instance-consuming frame-id))]
     {:db (paths/clean-frames db [frame-id])
      :dispatch-n [follow-event]})))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   {:id frame-id
    :coords-in-pixel coords
    :size-in-pixel size
    :size-min [900 550]
    :event ::view-event
    :module module-key
    :vertical config/default-vertical
    :legacy? false
    :data-consumer true
    :resizable true
    :optional-class "explorama__prediction"
    :type :frame/content-type}))

(re-frame/reg-event-fx
 ::set-filter
 (fn [{db :db} [_ di frame-id filter-desc]]
   {:db (assoc-in db (paths/frame-filter frame-id) filter-desc)
    :backend-tube-n [(when filter-desc [:de.explorama.frontend.algorithms.handler/filter-data di frame-id filter-desc])]}))

(re-frame/reg-event-fx
 ::open
 (fn [_ [_ source-frame-id create-position ignore-scroll-position? opts]]
   {:dispatch (fi/call-api :frame-create-event-vec
                           (assoc (create-frame (or create-position [100 200])
                                                [900 550])
                                  :ignore-scroll-position? ignore-scroll-position?
                                  :opts (merge {:publishing-frame-id source-frame-id}
                                               opts)))}))

(re-frame/reg-event-fx
 ::duplicate-ki-frame
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ source-frame-id]]
   (let [source-infos (fi/call-api :frame-db-get db source-frame-id)
         copy-active? (fi/call-api [:product-tour :component-active?-db-get]
                                   db :algorithms :copy)]
     (when copy-active?
       (let [{pixel-coords :coords
              pixel-size :size
              source-title :title
              {original-pixel-coords :coords
               original-pixel-size :size} :before-minmaximized} source-infos
             pos (or original-pixel-coords pixel-coords)
             size (or original-pixel-size pixel-size)
             local-filter (get-in db (paths/frame-filter source-frame-id))
             frame-desc (assoc (create-frame pos size)
                               :ignore-scroll-position? true
                               :opts {:publishing-frame-id source-frame-id
                                      :overwrites {:info {:algorithms {:type :duplicate}
                                                          :source-frame source-frame-id}
                                                   :attributes {:custom-title source-title
                                                                :last-applied-filters local-filter}}})]
         {:dispatch-n [(fi/call-api :frame-create-event-vec frame-desc)]})))))

(re-frame/reg-event-fx
 ::duplicate-frame-infos-import
 (fn [{db :db} [_ frame-id opts callback-event {:keys [goal-state settings-state parameter-state
                                                       simple-parameter-state future-data-state]}]]
   (let [task-id (str (random-uuid))
         {:keys [source-frame source-title]} opts
         source-di (get-in db (paths/data-instance-consuming source-frame))
         published-source-di (get-in db (paths/data-instance-publishing source-frame))
         update-temp-state (fn [db]
                             (if-let [state (get-in db (paths/ratom-app-state-sync source-frame))]
                               (assoc-in db (paths/ratom-app-state-sync frame-id) state)
                               (-> db
                                   (assoc-in (paths/goal-state frame-id) goal-state)
                                   (assoc-in (paths/settings-state frame-id) settings-state)
                                   (assoc-in (paths/parameter-state frame-id) parameter-state)
                                   (assoc-in (paths/simple-parameter-state frame-id) simple-parameter-state)
                                   (assoc-in (paths/future-data-state frame-id) future-data-state))))]
     (cond-> {:db (-> (assoc-in db (paths/export-states source-frame) false)

                      (update-in (paths/frame frame-id) merge (select-keys (get-in db (paths/frame source-frame) {})
                                                                           [:result]))
                      update-temp-state)

              :dispatch-n [callback-event]}

       source-di
       (update :dispatch-n conj [::connect-to-di frame-id source-di])

       published-source-di
       (update :db (fn [db]
                     (assoc-in db (paths/data-instance-publishing frame-id) task-id)))))))

(re-frame/reg-event-fx
 ::duplicate-frame-infos-export
 (fn [{db :db} [_ frame-id {:keys [source-frame] :as opts} callback-event]]
   (let [is-in-render-mode? (fi/call-api [:interaction-mode :render-db-get?]
                                         db)]
     (if is-in-render-mode?
       {:db (assoc-in db (paths/export-states source-frame) [::duplicate-frame-infos-import frame-id opts callback-event])}
       {:dispatch [::duplicate-frame-infos-import frame-id opts callback-event {}]}))))
