(ns de.explorama.frontend.indicator.core
  (:require [de.explorama.shared.data-format.data-instance :as dfl-di]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.indicator.config :as config]
            [de.explorama.frontend.indicator.data-instances]
            [de.explorama.frontend.indicator.event-logging :as event-log]
            [de.explorama.frontend.indicator.event-replay :as event-replay]
            [de.explorama.frontend.indicator.path :as ip]
            [de.explorama.frontend.indicator.plugin-impl :as plugi]
            [de.explorama.frontend.indicator.views.core :as views]
            [de.explorama.frontend.indicator.views.management :as management]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id "indicator"
                                       :icon "indicator"
                                       :component :indicator
                                       :action [::open]
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :vertical-label-indicator]
                                       :vertical config/default-vertical-str
                                       :type :frame/management-type
                                       :tool-group :bar
                                       :bar-group :bottom
                                       :sort-order 3})]
           [:dispatch (init-done "indicator")]
           [:dispatch (info "indicator arriving!")]]})))

(re-frame/reg-event-fx
 ::init-client
 (fn [_ [_ user-info]]
   {:fx [[:backend-tube [ws-api/all-indicators {:client-callback [ws-api/all-indicators-result]} user-info]]
         [:backend-tube [ws-api/load-indicator-ui-descs {:client-callback [ws-api/loaded-indicator-ui-descs]}]]]}))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get]
          service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:fx [[:dispatch [::arrive]]
           [:dispatch [::init-client user-info]]
           [:dispatch (fi/call-api :service-register-event-vec
                                   :modules "indicator-window" views/main-panel)]
           [:dispatch (fi/call-api :service-register-event-vec
                                   :clean-workspace ::clean-workspace [::clean-workspace])]
           [:dispatch (fi/call-api :service-register-event-vec
                                   :event-protocol config/default-vertical-str event-log/events->steps)]
           [:dispatch (service-register :logout-events :indicator-logout [::logout])]
           [:dispatch (papi-register "indicator" plugi/desc)]]})))

(re-frame/reg-event-db
 ws-api/loaded-indicator-ui-descs
 (fn [db [_ templates]]
   (assoc-in db ip/indicator-ui-templates templates)))

(re-frame/reg-event-fx
 ws-api/all-indicators-result
 (fn [{db :db} [_ result]]
   (let [result (into {} (map #(vector (:id %) %) result))]
     {:db (assoc-in db ip/indicators result)
      :fx [[:dispatch (fi/call-api :service-register-event-vec
                                   :event-replay
                                   config/default-vertical-str
                                   {:event-replay ::event-replay/replay-events
                                    :replay-progress ip/replay-progress})]
           [:dispatch (fi/call-api :service-register-event-vec
                                   :event-sync
                                   config/default-vertical-str
                                   ::event-replay/sync-event)]]})))

(defn clear-path [db path frames]
  (if (seq frames)
    (apply update-in db path dissoc frames)
    db))

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
 (fn [{db :db} [_ follow-event reason]]
   (let [frame-ids (fi/call-api :list-frames-vertical-db-get
                                db "indicator")]
     {:db (if (= reason :logout)
            (update db
                    ip/root
                    #(apply dissoc
                            %
                            ip/clean-up-keys))
            (-> db
                (update ip/root #(dissoc %
                                         ip/open-frame-id-key))
                (clear-path ip/replay frame-ids)))
      :dispatch-n (conj (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frame-ids)
                        (conj follow-event ::clean-workspace))})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {}))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   (debug ::create-frame frame-id coords size)
   {:id frame-id
    :coords-in-pixel coords
    :size-in-pixel   size
    :size-min [600 600]
    :event ::view-event
    :module "indicator-window"
    :vertical "indicator"
    :optional-class "explorama__indicator"
    :type :frame/consumer-type
    :data-consumer true
    :ignore-drop-on-frame? true
    :resizable true}))

(re-frame/reg-sub
 ::project-is-loading
 (fn [db [_ frame-id]]
   (ip/replay=? db frame-id)))

(re-frame/reg-event-fx
 ::open
 (fn [_ _]
   {:dispatch (fi/call-api :frame-create-event-vec (create-frame [100 200] [800 700]))}))

(re-frame/reg-event-fx
 ::connect-to-frame-query
 (fn [{db :db} [_ _frame-id new? data-desc]]
   {:fx [[:dispatch [::views/set-loading true]]]
    :backend-tube [ws-api/connect-to-di {:client-callback [ws-api/connect-to-di-result new?]}
                   (assoc data-desc
                          :indicator-id (:id (get-in db ip/active-indicator)))]}))
(re-frame/reg-event-fx
 ws-api/connect-to-di-result
 (fn [{db :db} [_  new? {:keys [indicator-id di]
                         :as dataset-result}]]
   (let [di-id (dfl-di/ctn->sha256-id di)]
     {:db (if (management/indicator-exist? db indicator-id)
            (let [add-timestamp (.getTime (js/Date.))]
              (assoc-in db
                        (ip/indicator-dataset indicator-id di-id)
                        (assoc dataset-result
                               :timestamp add-timestamp)))
            db)
      :fx [[:dispatch [::views/set-loading false]]
           (when new?
             [:dispatch [::management/add-dataset indicator-id di-id]])]})))

(re-frame/reg-event-fx
 ::view-event
 (fn [{db :db} [_ action params]]
   (debug ::view-event action params)
   (let [{:keys [frame-id callback-event]} params]
     (case action
       :frame/init {:db (assoc-in db ip/open-frame-id frame-id)}
       :frame/close {:dispatch [::close-action frame-id callback-event]}
       :frame/connection-negotiation
       (let [{:keys [type frame-id result connected-frame-id]} params
             options (when (= type :target)
                       {:type :cancel
                        :frame-id frame-id})]
         {:fx [[:dispatch (conj result options)]
               [:dispatch (fi/call-api :frame-query-event-vec
                                       connected-frame-id
                                       :data-desc
                                       [::connect-to-frame-query frame-id true])]]})
       {}))))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ frame-id follow-event]]
   (debug "%% CLOSE %% INDICATOR %%" frame-id)
   {:db (update db ip/root dissoc ip/open-frame-id-key)
    :dispatch-n [follow-event]}))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event "Indicator")
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(defn init []
  (register-init 0))
