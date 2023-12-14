(ns de.explorama.frontend.charts.charts.core
  (:require [de.explorama.frontend.charts.charts.backend-interface :as backend]
            [de.explorama.frontend.charts.charts.settings :as settings]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.components.frame-header :as frame-header]
            [de.explorama.frontend.charts.components.warning-screen :as warning-screen]
            [de.explorama.frontend.charts.config :as vconfig]
            [de.explorama.frontend.charts.empty :as empty]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]
            [de.explorama.frontend.charts.vis-state :as vis-state]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.shared.charts.config :refer [explorama-charts-chartjs-selectlimit]]
            [de.explorama.shared.charts.ws-api :as ws-api]
            [de.explorama.shared.common.data.attributes :as attrs]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :as timbre :refer-macros [debug error]]))

(defn set-chart-selection [db frame-id chart-frame]
  (-> (update-in db (path/chart-frame frame-id)
                 merge
                 chart-frame)
      (update-in (path/chart-type frame-id 0)
                 (fn [original]
                   (let [cid (keyword (get original path/chart-desc-id-key (get settings/default-chart-desc
                                                                                path/chart-desc-id-key)))
                         content (get settings/possible-charts cid)]
                     (merge content original))))))

(re-frame/reg-sub
 ::chart-type-i18n-translate
 (fn [db [_ frame-id chart-index]]
   (when-let [chart-type (settings/chart-desc->chart-type db frame-id chart-index)]
     (assoc chart-type
            :label
            (i18n/translate db
                            (get-in chart-type [path/chart-desc-label-key]))))))

(re-frame/reg-event-fx
 ::change-chart-type
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id chart-index selection]]
   (let [chart-type (get selection path/chart-desc-id-key)
         new-is-pie? (= (keyword chart-type)
                        path/pie-id-key)
         new-is-wordcloud? (= chart-type
                              path/wordcloud-id-key)
         old-chart-type (get-in db (path/desc-id frame-id chart-index))
         old-is-pie? (= (keyword old-chart-type)
                        path/pie-id-key)
         old-is-wordcloud? (= old-chart-type
                              path/wordcloud-id-key)
         db (cond-> db
              :always (assoc-in (path/chart-type frame-id chart-index)
                                selection)
              new-is-wordcloud? (->
                                 (update-in (path/attributes frame-id chart-index)
                                            #(or % [{:value :characteristics}]))
                                 (update-in (path/stopping-attributes frame-id chart-index)
                                            #(or % ws-api/default-stopping-attrs))
                                 (update-in (path/chart-frame frame-id)
                                            dissoc
                                            path/chart-data-key))
              old-is-wordcloud? (update-in (path/chart-frame frame-id)
                                           dissoc
                                           path/chart-data-key)
              :always
              (assoc-in (path/chart-data frame-id)
                        []))]
     {:db db
      :dispatch-n (cutils/req-datasets db frame-id)})))

(re-frame/reg-sub
 ::pending-request?
 (fn [db [_ frame-id]]
   (boolean (get-in db (path/frame-request-id frame-id)))))

(re-frame/reg-sub
 ::datasets
 (fn [db [_ frame-id]]
   (get-in db (path/chart-data frame-id))))

(re-frame/reg-event-fx
 ::copy-frame-infos
 (fn [{db :db} [_ source-frame-id frame-id]]
   {}))

(defn query-handler [frame-id db query callback-event]
  (case query
    :local-filter {:dispatch (conj callback-event
                                   (fi/call-api :frame-filter-db-get db frame-id))}
    :data-desc {:dispatch (conj callback-event
                                {:di (get-in db (path/frame-di frame-id))
                                 :local-filter (fi/call-api :frame-filter-db-get db frame-id)})}
    :temp-layouts {:dispatch (conj callback-event {})}
    :vis-desc (let [vis-desc (vis-state/vis-desc db frame-id)]
                (fi/call-api :make-screenshot-raw
                             {:dom-id (vconfig/frame-body-dom-id-charts frame-id)
                              :callback-fn (fn [base64]
                                             (re-frame/dispatch (conj callback-event (assoc vis-desc
                                                                                            :preview base64))))})
                {})
    (error "unknown query" query)))

(re-frame/reg-event-fx
 ::charts-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id size callback-event frame-target-id query payload]} params
         duplicate? (= :duplicate (get-in payload [:custom :chart :type]))
         [local-filter di undo-event]
         (cond :else
               (let [{:keys [local-filter di undo-event]} payload]
                 [local-filter di undo-event]))]
     (debug "charts-view-event" {:action action, :params params})
     (case action
       :frame/connect-to
       (cond-> {:db db}
         di (assoc :dispatch [::queue-util/queue-wrapper frame-target-id
                              [::backend/connect-to-datainstance
                               {:frame-target-id frame-target-id
                                :di di}]
                              local-filter])

         (:di/acs di)
         (assoc-in (into [:db] (path/volatile-acs-frame frame-target-id)) (:di/acs di))

         :always
         (update :db assoc-in (path/frame-di frame-target-id) di))

       :frame/query (query-handler frame-id db query callback-event)

       :frame/init
       (let [{:keys [source-frame source-title source-selection]} (get-in payload [:custom :chart])]
         {:db (cond-> db
                :always (assoc-in (path/width frame-id)
                                  (- (first size)
                                     50))
                :always (assoc-in (path/charts frame-id) [])
                (and duplicate? source-frame)
                (ddq/set-event-callback frame-id [::copy-frame-infos source-frame frame-id])
                (and duplicate? source-selection)
                (set-chart-selection frame-id source-selection))
          :fx [(when di
                 [:dispatch [::queue-util/queue-wrapper frame-id
                             [::backend/connect-to-datainstance
                              {:frame-target-id frame-id
                               :di di}]
                             local-filter]])
               (when (and di duplicate?)
                 [:dispatch [::queue-util/queue-wrapper frame-id
                             [::backend/connect-to-datainstance
                              {:frame-target-id frame-id
                               :di di
                               :keep-selection? true}]
                             local-filter]])
               (when duplicate?
                 [:dispatch (fi/call-api :frame-header-color-event-vec frame-id)])
               (when duplicate?
                 [:dispatch (fi/call-api :frame-set-publishing-event-vec frame-id true)])]})
       :frame/recreate
       {:db (cond-> db
              :always (assoc-in (path/applied-filter frame-id) nil)
              :always (assoc-in (path/volatile-acs-frame frame-id) (:di/acs di))
              callback-event (ddq/set-event-callback frame-id callback-event))
        :dispatch-n [[::queue-util/queue-wrapper frame-id
                      [::backend/connect-to-datainstance
                       {:frame-target-id frame-id
                        :di di
                        :remove-local-filter? true}]]]}
       :frame/update
       (cond
         di
         {:db (cond-> db
                (vector? undo-event) (assoc-in (path/undo-connection-update-event (path/chart-frame frame-id))
                                               undo-event))
          :dispatch-n [[:de.explorama.frontend.charts.components.frame-header/reset-counts frame-id]
                       [::queue-util/queue-wrapper frame-id
                        [::backend/connect-to-datainstance
                         {:frame-target-id frame-id
                          :di di
                          :keep-selection? true}]
                        (get-in db (path/applied-filter frame-id))]]}
         :else {})
       :frame/connection-negotiation
       (let [{:keys [type frame-id result connected-frame-id]} params
             options
             (cond
               (and (empty? (get-in db (path/frame-di frame-id)))
                    (= type :target))
               {:type :connect
                :frame-id frame-id
                :event [:de.explorama.frontend.charts.core/provide-content frame-id]}
               (and (empty? (get-in db (path/frame-di frame-id)))
                    (= type :source))
               {:type :cancel
                :frame-id frame-id
                :event [:de.explorama.frontend.charts.core/provide-content frame-id]}
               :else
               {:type :options
                :frame-id frame-id
                :event [:de.explorama.frontend.charts.core/provide-content frame-id]
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
       {:db (cond-> db
              :always (assoc-in (path/volatile-acs-frame frame-id) (:di/acs di))
              callback-event (ddq/set-event-callback frame-id callback-event))
        :dispatch-n [[::queue-util/queue-wrapper frame-id
                      [::backend/connect-to-datainstance
                       {:frame-target-id frame-id
                        :di di}]
                      local-filter]]}
       :frame/close
       {:dispatch [::close-action frame-id callback-event]}

       {}))))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ frame-id follow-event]]
   (settings/clean-options frame-id)
   {:db (-> db
            (update-in path/chart-root dissoc frame-id)
            (update path/root-key dissoc frame-id))
    :dispatch-n [follow-event]}))

(re-frame/reg-sub
 ::y-options
 (fn [_ [_ frame-id]]
   (settings/y-options frame-id)))

(re-frame/reg-sub
 ::x-options
 (fn [_ [_ frame-id]]
   (settings/x-options frame-id)))

(re-frame/reg-sub
 ::sum-by-options
 (fn [_ [_ frame-id]]
   (settings/sum-options frame-id)))

(re-frame/reg-sub
 ::sum-by-characteristics
 (fn [db [_ frame-id chart-index]]
   (let [selected-attr (get-in db (conj (path/sum-by-option frame-id chart-index)
                                        :value))]
     (if (not= selected-attr "all")
       (let [sum-by-values (count (get-in db (path/sum-by-values frame-id chart-index) []))
             selected-attr-key (attrs/access-key selected-attr)]
         (if (> explorama-charts-chartjs-selectlimit
                sum-by-values)
           (settings/sum-by-characteristics frame-id selected-attr-key)
           []))
       []))))

(re-frame/reg-sub
 ::wordcloud-attr-options
 (fn [_ [_ frame-id]]
   (settings/wordcloud-attrs frame-id)))

(re-frame/reg-sub
 ::height
 (fn [db [_ frame-id]]
   (get-in db
           (path/height frame-id)
           path/min-height)))

(re-frame/reg-sub
 ::width
 (fn [db [_ frame-id]]
   (get-in db
           (path/width frame-id)
           path/min-width)))

(defn chart-view [frame-id chart-index vis-desc]
  (let [selected-chart (re-frame/subscribe [::chart-type-i18n-translate frame-id chart-index])
        options-are-loading? @(re-frame/subscribe [::queue-util/loading? frame-id])
        {:keys [local] :as counts} @(re-frame/subscribe [::frame-header/get-counts frame-id])]
    (if (and @selected-chart
             (not options-are-loading?)
             (or (and local
                      (> local 0))
                 vis-desc)
             (or (some? options-are-loading?)
                 vis-desc))
      [(get-in @selected-chart [path/chart-desc-content-key])
       frame-id vis-desc]
      [empty/empty-component frame-id {:counts-sub counts}])))

(defn frame-body [frame-id {:keys [size] :as vis-desc}]
  (let [infos-sub (if vis-desc
                    (reagent/atom {:is-minimized? false
                                   :size (or size
                                             [140 140])})
                    (fi/call-api :frame-sub frame-id))
        chart-index 0] ;; Todo R10 change to dynamic index based available chart-descs
    (reagent/create-class
     {:display-name (str "charts body" frame-id)
      :component-did-update (fn [this argv]
                              (let [[_ _ {old-size :size}] argv
                                    [_ _ {new-size :size}] (reagent/argv this)]
                                (when (and vis-desc
                                           (not= old-size new-size))
                                  (swap! infos-sub assoc :size new-size))))
      :reagent-render
      (fn [frame-id vis-desc]
        (let [{:keys [size is-minimized?]} @infos-sub
              vis-desc (when vis-desc (assoc vis-desc :size size))]
          [:<>
           [:div.window__body.flex
            {:id (vconfig/frame-body-dom-id-charts frame-id)
             :style {:display (when is-minimized?
                                "none")}}
            [chart-view frame-id chart-index vis-desc]]]))})))

(def product-tour-impl
  {:component :charts-charts})

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
