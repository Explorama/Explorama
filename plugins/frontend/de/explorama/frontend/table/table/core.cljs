(ns de.explorama.frontend.table.table.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.table.components.warning-screen :as warning-screen]
            [de.explorama.frontend.table.config :as vconfig]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.table.backend-interface :as backend]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.frontend.table.table.state :as table-state]
            [de.explorama.frontend.table.table.view :refer [render-done table-view]]
            [de.explorama.frontend.table.util.queue :as queue-util]
            [de.explorama.frontend.table.vis-state :as vis-state]
            [de.explorama.shared.table.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :as timbre :refer-macros [debug error]]))

(defn- query-handler [frame-id db query callback-event]
  (case query
    :local-filter {:dispatch (conj callback-event
                                   (fi/call-api :frame-filter-db-get db frame-id))}
    :data-desc {:dispatch (conj callback-event
                                {:di (get-in db (path/table-datasource frame-id))
                                 :local-filter (fi/call-api :frame-filter-db-get db frame-id)})}
    :temp-layouts {:dispatch (conj callback-event {})}
    :vis-desc (let [vis-desc (vis-state/vis-desc db frame-id)]
                (fi/call-api :make-screenshot-raw
                             {:dom-id (vconfig/frame-body-dom-id-table frame-id)
                              :callback-fn (fn [base64]
                                             (re-frame/dispatch (conj callback-event (assoc vis-desc
                                                                                            :preview base64))))})
                {})
    (error "unknown query" query)))

(re-frame/reg-event-fx
 ::focus-event
 (fn [_ [_ frame-id event-id]]
   (table-data/set-frame-table-config frame-id
                                      ws-api/focus-event-id-key
                                      event-id)
   {:fx [[:dispatch (table-data/request-data-event-vec frame-id)]]}))

(re-frame/reg-event-fx
 ::table-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event frame-target-id query payload]} params
         duplicate? (= :duplicate (get-in payload [:custom :table :type]))
         [local-filter di selections table-infos]
         (cond (and (get-in payload [:custom :table])
                    (= :frame/override action))
               (let [table-infos (get-in payload [:custom :table])
                     {:keys [local-filter di selections]} payload]
                 [local-filter di selections table-infos nil])
               duplicate?
               (let [{:keys [local-filter di selections table-infos]} (get-in payload [:custom :table])]
                 [local-filter di selections table-infos nil])
               :else
               (let [{:keys [local-filter di selections undo-event]} payload]
                 [local-filter di selections {} undo-event]))]
     (debug "table-view-event" {:action action, :params params})
     (case action
       :frame/connect-to
       (cond-> {:db db}
         di
         (assoc :dispatch-n [[::queue-util/queue-wrapper frame-target-id
                              [::backend/connect-to-datainstance {:di di
                                                                  :frame-target-id frame-target-id
                                                                  :reset-state? true}]
                              local-filter]])
         (:di/acs di)
         (assoc-in (into [:db] (path/volatile-acs-frame frame-target-id)) (:di/acs di))

         (not-empty selections)
         (update :dispatch-n conj [::table-state/update-selection frame-target-id selections true])

         :always
         (update :db
                 assoc-in
                 (path/table-datasource frame-target-id)
                 di))
       :frame/query (query-handler frame-id db query callback-event)
       :frame/init {:fx (cond-> (if duplicate?
                                  (let [{:keys [source-title source-frame]} (get-in payload [:custom :table])]
                                    [[:dispatch [::table-state/init-frame frame-id source-title source-frame di table-infos]]
                                     [:dispatch [::queue-util/queue-wrapper frame-id
                                                 [::backend/connect-to-datainstance
                                                  {:di di
                                                   :frame-target-id frame-id
                                                   :reset-state? true}]
                                                 local-filter]]
                                     [:dispatch (fi/call-api :frame-header-color-event-vec frame-id)]
                                     [:dispatch (fi/call-api :frame-set-publishing-event-vec frame-id true)]])
                                  [[:dispatch [::table-state/init-frame frame-id nil nil di nil]]
                                   [:dispatch [::queue-util/queue-wrapper frame-id
                                               [::backend/connect-to-datainstance
                                                {:di di
                                                 :frame-target-id frame-id}]
                                               local-filter]]])
                          (not-empty selections)
                          (conj [:dispatch [::table-state/update-selection frame-id selections true]]))}
       :frame/recreate
       {:db (cond-> db
              :always (assoc-in (path/applied-filter frame-id) nil)
              :always (assoc-in (path/volatile-acs-frame frame-id) (:di/acs di))
              callback-event (ddq/set-event-callback frame-id callback-event))
        :dispatch-n [[::queue-util/queue-wrapper frame-id
                      [::backend/connect-to-datainstance
                       {:di di
                        :frame-target-id frame-id
                        :reset-state? true}]]]}
       :frame/update
       (cond di
             {:fx [[:dispatch [::queue-util/queue-wrapper frame-id
                               [::backend/connect-to-datainstance
                                {:di di
                                 :state-params {ws-api/current-page-key 1
                                                ws-api/scroll-x-key 0
                                                ws-api/scroll-y-key 0}
                                 :frame-target-id frame-id}]
                               (get-in db (path/applied-filter frame-id))]]]}
             :else {})
       :frame/selection
       {:dispatch [::table-state/update-selection frame-id selections]}
       :frame/connection-negotiation
       (let [{:keys [type frame-id result connected-frame-id]} params
             options
             (cond
               (and (empty? (get-in db (path/table-datasource frame-id)))
                    (= type :target))
               {:type :connect
                :frame-id frame-id
                :event [:de.explorama.frontend.table.core/provide-content frame-id]}
               (and (empty? (get-in db (path/table-datasource frame-id)))
                    (= type :source))
               {:type :cancel
                :frame-id frame-id
                :event [:de.explorama.frontend.table.core/provide-content frame-id]}
               :else
               {:type :options
                :frame-id frame-id
                :event [:de.explorama.frontend.table.core/provide-content frame-id]
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
                (assoc-in (path/volatile-acs-frame frame-id) (:di/acs di)))
        :dispatch-n [[::queue-util/queue-wrapper frame-id
                      [::backend/connect-to-datainstance
                       {:di di
                        :frame-target-id frame-id
                        :reset-state? true}]
                      local-filter]
                     (when (not-empty selections)
                       [::table-state/update-selection frame-id selections])
                     (when callback-event
                       callback-event)]}
       :frame/close
       {:db (path/dissoc-in db (path/volatile-acs-frame frame-id))
        :dispatch [::close-action frame-id callback-event]}

       {}))))

;; for usage from outside e.g. core
(def remove-frame-data table-data/remove-frame-data)

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ frame-id follow-event]]
   (remove-frame-data frame-id)
   {:db (-> db
            (path/dissoc-in (path/table-frame frame-id))
            (path/dissoc-in (path/frame-desc frame-id)))
    :dispatch-n [follow-event]}))

(def product-tour-impl
  {:component :table})

(def warn-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::warning-screen/warning-view-display frame-id]))
   :title-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-title]))
   :message-1-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-message-part-1]))
   :message-2-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-message-part-2]))
   :recommendation-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-recommendation]))
   :stop-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-follow-recommendation]))
   :proceed-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-not-follow-recommendation]))
   :stop-fn
   (fn [_ frame-id _]
     (re-frame/dispatch [::warning-screen/warning-stop frame-id]))
   :proceed-fn
   (fn [_ frame-id _]
     (re-frame/dispatch [::warning-screen/warning-proceed frame-id]))})

(def loading-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::queue-util/loading? frame-id]))
   :cancellable?
   (fn [_]
     (atom false)) ;! FIXME
   :cancel-fn
   (fn [frame-id _]
     (re-frame/dispatch [::backend/cancel-loading frame-id]))
   :loading-screen-message-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-message]))
   :loading-screen-tip-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip]))
   :loading-screen-tip-titel-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip-titel]))})


(defn frame-body [frame-id {:keys [size] :as vis-desc}]
  (let [infos-sub (if vis-desc
                    (r/atom {:is-minimized? false
                             :size (or size
                                       [140 140])})
                    (fi/call-api :frame-sub frame-id))]
    (r/create-class
     {:display-name (str "table body" frame-id)
      :component-did-mount (fn []
                             (let [{:keys [is-minimized?]} @infos-sub]
                               (when is-minimized? (render-done frame-id))))
      :component-did-update (fn [this argv]
                              (let [[_ _ {old-size :size}] argv
                                    [_ _ {new-size :size}] (r/argv this)]
                                (when (and vis-desc
                                           (not= old-size new-size))
                                  (swap! infos-sub assoc :size new-size))))
      :reagent-render
      (fn [frame-id vis-desc]
        (let [{:keys [is-minimized?]} @infos-sub]
          [:div.window__body.flex {:id (vconfig/frame-body-dom-id-table frame-id)
                                   :style {:display (when is-minimized?
                                                      "none")}}
           [table-view frame-id infos-sub vis-desc]]))})))

(when (fi/api-definitions)
  (re-frame/dispatch (fi/call-api :info-event-vec "Table arriving!")))