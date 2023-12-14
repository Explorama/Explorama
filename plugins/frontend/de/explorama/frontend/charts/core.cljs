(ns de.explorama.frontend.charts.core
  (:require [data-format-lib.data-instance :as dfl-di]
            [de.explorama.frontend.charts.acs]
            [de.explorama.frontend.charts.charts.core :as charts]
            [de.explorama.frontend.charts.charts.utils :as cutils]
            [de.explorama.frontend.charts.config :as vconfig]
            [de.explorama.frontend.charts.event-logging :as event-log]
            [de.explorama.frontend.charts.event-replay :as event-replay]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.charts.interaction.selection]
            [de.explorama.frontend.charts.operations.filter]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.plugin-impl :as plugi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug error info]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event "charts")
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id vconfig/tool-name-charts
                                       :icon "charts"
                                       :component :charts
                                       :action [::chart-view-open]
                                       :tooltip-text [::i18n/translate :chart-component-label]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :vertical vconfig/default-vertical-str
                                       :tool-group :bar
                                       :bar-group :middle
                                       :sort-order 4})]
           [:dispatch (service-register :visual-option
                                        :charts
                                        {:icon :charts
                                         :sort-class "tool__charts"
                                         :event ::chart-view-open
                                         :tooltip [::i18n/translate :chart-component-label]
                                         :tooltip-search [::i18n/translate :charts-tooltip-search]})]
           [:dispatch (init-done "charts")]
           [:dispatch (info "charts arriving!")]]})))

(re-frame/reg-event-fx
 ::init-client
 (fn [_ [_ _user-info]]
   {}))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get frame-info-api-register-event-vec frame-instance-api-register-event-vec]
          service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:dispatch-n [[::arrive]
                   [::init-client user-info]
                   (service-register :logout-events :css-logout [::logout])
                   (service-register :config :vis-pap {:url vconfig/vis-origin
                                                       :vertical :charts
                                                       :type :abac
                                                       :get-url-postfix "/pap-all"
                                                       :save-url-postfix "/pap-save"
                                                       :get-capabilities-url "/pap-capabilites"})
                   (service-register :event-replay "charts" {:event-replay ::event-replay/replay-events
                                                             :replay-progress path/replay-progress})
                   (service-register :event-sync "charts" ::event-replay/sync-event)
                   (service-register :event-protocol "charts" event-log/events->steps)
                   (service-register :modules vconfig/tool-name-charts charts/frame-body)
                   (frame-info-api-register-event-vec "charts" {:local-filter #(get-in %1 (path/applied-filter %2))
                                                                :datasources (fn [db frame-id]
                                                                               (when-let [dim-info (get-in db (path/dim-info frame-id))]
                                                                                 (:datasources dim-info)))
                                                                :layouts (fn [_] nil)
                                                                :di #(get-in %1 (path/frame-di %2))
                                                                :selections (fn [_] nil)
                                                                :undo-event (fn [_] nil)
                                                                :custom {:charts #(get-in %1 (path/charts %2))}})
                   (frame-instance-api-register-event-vec "charts" {})
                   (papi-register "charts" plugi/charts-desc)
                   (service-register
                    :clean-workspace
                    ::clean-workspace
                    [::clean-workspace])
                   [::event-log/log-pseudo-init]]})))

(re-frame/reg-event-fx
 ::clean-up
 (fn [_ [_ follow-event frame-ids]]
   {:dispatch-n (if (coll? frame-ids)
                  (mapv (fn [frame-id]
                          [::charts/close-action frame-id (conj follow-event frame-id)])
                        frame-ids)
                  [[::clean-workspace follow-event]])}))

(defn clear-path [db path frames]
  (if (seq frames)
    (apply update-in db path dissoc frames)
    db))


(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (let [frame-ids (fi/call-api :list-frames-vertical-db-get db "charts")
         cleaned (-> db
                     (dissoc :charts)
                     (dissoc :charts.loading/loading-view)
                     (clear-path [path/root-key] frame-ids)
                     (update path/root-key
                             dissoc
                             :replay-callback
                             path/replay-progress-key))]
     {:db cleaned
      :fx [[:dispatch-n (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frame-ids)]
           (when (not= reason :logout)
             [:dispatch [::event-log/log-pseudo-init]])
           [:dispatch (conj follow-event ::clean-workspace)]]})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {}))

(defn create-frame
  [frame-id coords size]
  {:id frame-id
   :module vconfig/tool-name-charts
   :coords-in-pixel coords
   :size-in-pixel size
   :size-min [600 275]
   :event ::charts/charts-view-event
   :data-consumer true
   :vertical vconfig/default-vertical-str
   :type :frame/content-type
   :resizable true})

(defn create-frame-chart
  ([frame-id coords size]
   (create-frame frame-id coords size))
  ([coords size]
   (create-frame nil coords size)))

(re-frame/reg-event-fx
 ::chart-view-open
 (fn [_ [_ source-frame-id create-position ignore-scroll-position? opts]]
   {:dispatch (fi/call-api :frame-create-event-vec
                           (assoc (create-frame-chart (or create-position
                                                          [100 200])
                                                      [600 550])
                                  :ignore-scroll-position? ignore-scroll-position?
                                  :opts (merge {:publishing-frame-id source-frame-id}
                                               opts)))}))

(re-frame/reg-event-fx
 ::duplicate-chart-frame
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ source-frame-id]]
   (let [source-infos (fi/call-api :frame-db-get db source-frame-id)
         copy-active? (fi/call-api [:product-tour :component-active?-db-get]
                                   db :charts-chart :copy)]
     (when copy-active?
       (let [{pixel-coords :coords
              pixel-size :size
              source-title :title
              {original-pixel-coords :coords
               original-pixel-size :size} :before-minmaximized} source-infos
             pos (or original-pixel-coords pixel-coords)
             size (or original-pixel-size pixel-size)
             local-filter (get-in db (path/applied-filter source-frame-id))
             frame-desc (assoc (create-frame-chart pos size)
                               :ignore-scroll-position? true
                               :opts {:publishing-frame-id source-frame-id
                                      :overwrites {:info {:custom {:chart {:type :duplicate
                                                                           :source-frame source-frame-id
                                                                           :source-title source-title
                                                                           :source-selection (cutils/frame-selection db source-frame-id)}}}
                                                   :attributes {:custom-title source-title
                                                                :last-applied-filters local-filter}}})]
         {:dispatch-n [(fi/call-api :frame-create-event-vec frame-desc)]})))))

(re-frame/reg-event-fx
 ::abort
 (fn [_ [_ msg]]
   {:dispatch (fi/call-api :error-event-vec msg)}))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id operation-type source-or-target params event]]
   (debug ::provide-content frame-id operation-type source-or-target params event)
   (let [local-filter (fi/call-api :frame-filter-db-get db frame-id)
         local-filter-id (dfl-di/ctn->sha256-id local-filter)
         di (get-in db (path/frame-di frame-id))
         new-di (-> di
                    (update :di/operations (fn [ops]
                                             [:filter local-filter-id ops]))
                    (assoc-in [:di/filter local-filter-id] local-filter))]
     (cond
       (#{:difference
          :intersection-by
          :union
          :sym-difference} operation-type)
       (if (seq local-filter)
         {:dispatch (conj event {:di new-di :cancel? false})}
         {:dispatch (conj event {:di di :cancel? false})})
       (#{:couple} operation-type)
       {:dispatch (conj event {:cancel? true})}
       (and (#{:override} operation-type)
            (= :target source-or-target))
       {:dispatch (conj event {:cancel? false})}
       (and (#{:override} operation-type)
            (= :source source-or-target))
       {:dispatch (conj event {:cancel? false})}
       :else
       {:dispatch (conj event {:cancel? true})}))))

;; (when vconfig/vis-origin
;;   (pap/register-feature vconfig/vis-origin (str ::load-charts) "charts" nil))

(defn init []
  (register-init 0))
