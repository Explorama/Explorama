(ns de.explorama.frontend.algorithms.core
  (:require [de.explorama.frontend.algorithms.components.main :as main]
            [de.explorama.frontend.algorithms.config :as config]
            [de.explorama.frontend.algorithms.event-logging :as event-log]
            [de.explorama.frontend.algorithms.path.core :as paths]
            [de.explorama.frontend.algorithms.plugin-impl :as plugi]
            [de.explorama.frontend.algorithms.view :as view]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.algorithms.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error warn]]))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id config/tool-name
                                       :icon "head-cogs"
                                       :component :algorithms
                                       :action [:de.explorama.frontend.algorithms.view/open]
                                       :tooltip-text [::i18n/translate :algorithms-simple-title]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :vertical config/default-vertical
                                       :tool-group :bar
                                       :bar-group :middle
                                       :sort-order 5})]
           [:dispatch (service-register :visual-option
                                        :algorithms
                                        {:icon :head-cogs
                                         :sort-class "tool__anchor"
                                         :event ::de.explorama.frontend.algorithms.view/open
                                         :tooltip [::i18n/translate :algorithms-simple-title]
                                         :tooltip-search [::i18n/translate :algorithms-tooltip-search]})]
           [:dispatch (init-done "algorithms")]
           [:dispatch (info "algorithms arriving!")]]})))

(re-frame/reg-event-fx
 ::init-client
 (fn [_ [_ _user-info]]
   {:fx [[:backend-tube [ws-api/init
                             {:client-callback ws-api/init-result}]]]}))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get frame-info-api-register-event-vec
                 frame-instance-api-register-event-vec]
          service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:dispatch-n [[::arrive]
                   [::init-client user-info]
                   (service-register :modules view/module-key main/react-view)
                   (service-register :config :algorithms-pap {:url config/ki-origin
                                                              :vertical :algorithms
                                                              :type :abac
                                                              :get-url-postfix "/pap-all"
                                                              :save-url-postfix "/pap-save"
                                                              :get-capabilities-url "/pap-capabilites"})
                   (service-register :clean-workspace
                                     ::clean-workspace
                                     [::clean-workspace])
                   (service-register :event-protocol "algorithms" event-log/events->steps)
                   (service-register :event-replay "algorithms" {:event-replay :de.explorama.frontend.algorithms.event-logging/replay-events
                                                                 :replay-progress paths/replay-progress})
                   (service-register :event-sync "algorithms" :de.explorama.frontend.algorithms.event-logging/sync-event)
                   (service-register :modules config/tool-name main/react-view)
                   (service-register :logout-events :algorithms-logout [::logout])
                   (frame-info-api-register-event-vec "algorithms" {:local-filter (fn [_ _] nil)
                                                                    :datasources (fn [db frame-id]
                                                                                   (when-let [di-desc (get-in db (paths/di-desc frame-id))]
                                                                                     (-> (:datasources di-desc)
                                                                                         (second))))
                                                                    :layouts (fn [_ _] nil)
                                                                    :di #(or (get-in %1 (paths/data-instance-publishing %2))
                                                                             (get-in %1 (paths/data-instance-consuming %2)))
                                                                    :selections (fn [_ _] nil)
                                                                    :undo-event (fn [_ _] nil)
                                                                    :custom {:algorithms {}}})
                   (frame-instance-api-register-event-vec "algorithms" {})
                   (papi-register "algorithms" plugi/frame-desc)
                   [::event-log/log-pseudo-init]]})))

(re-frame/reg-event-fx
 ::clean-up
 (fn [_ [_ follow-event frame-ids]]
   {:dispatch-n (if (coll? frame-ids)
                  (mapv (fn [frame-id]
                          [::view/close-action frame-id (conj follow-event frame-id)])
                        frame-ids)
                  [[::clean-workspace follow-event]])}))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (let [frames (keys (get-in db paths/frames))
         frame-ids (fi/call-api :list-frames-vertical-db-get db config/default-vertical)]
     {:db (paths/clean-frames db frame-ids)
      :fx [[:dispatch-n (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frame-ids)]
           [:backend-tube-n (mapv (fn [frame-id]
                                        [:de.explorama.frontend.algorithms.handler/remove-temp-prediction (get-in db (paths/data-instance-publishing frame-id))])
                                      frame-ids)]
           (when (not= reason :logout)
             [:dispatch [::event-log/log-pseudo-init]])
           [:dispatch (conj follow-event ::clean-workspace)]]
      :dispatch-n (conj (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frame-ids)
                        (when (not= reason :logout)
                          [::event-log/log-pseudo-init])
                        (conj follow-event ::clean-workspace))})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {}))

(re-frame/reg-event-fx
 ::inform-status
 (fn [{db :db} [_ frame-id status-msg done?]]
   (debug "Status Update-MSG: " status-msg "; Done?" done?)
   {:db (assoc-in db (paths/status frame-id) status-msg)}))

(re-frame/reg-event-fx
 ::data-instance-published
 (fn [_ [_ diid]]
   (warn "data-instance-published" diid)
   {}))

;; (when config/ki-origin
;;  (pap/register-feature config/ki-origin (str ::load-ki) "algorithms" nil))

(re-frame/reg-event-fx
 ws-api/init-result
 (fn [_ [_ {:keys [problem-types procedures]}]]
   {:dispatch-n [[:de.explorama.frontend.algorithms.components.main/problem-types problem-types]
                 [:de.explorama.frontend.algorithms.components.main/procedures procedures]]}))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(defn init []
  (register-init 0))
