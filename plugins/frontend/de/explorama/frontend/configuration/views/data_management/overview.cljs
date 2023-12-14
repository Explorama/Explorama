(ns de.explorama.frontend.configuration.views.data-management.overview
  (:require [de.explorama.frontend.configuration.config :as config]
            [de.explorama.frontend.configuration.data.core :as data :refer [default-bucket]]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.configuration.views.data-management.datasources :as datasources]
            [de.explorama.frontend.configuration.views.data-management.geographic-attributes :as ga]
            [de.explorama.frontend.configuration.views.data-management.topics :as topics]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame :refer [dispatch reg-event-db reg-event-fx
                                                reg-sub subscribe]]))

(reg-event-db
 ::active-tab
 (fn [db [_ tab-name]]
   (assoc-in db path/active-dm-tab tab-name)))

(reg-sub
 ::active-tab-name
 (fn [db _]
   (get-in db path/active-dm-tab :topics)))

(reg-sub
 ::sidebar-title
 (fn [db]
   (let [data-management-label (i18n/translate db :data-management-label)]
     data-management-label)))

(reg-event-fx
 ::close-action
 (fn [{db :db} [_ _ close-event]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::close-action-sync close-event])
     {:db db
      :dispatch-n [close-event]})))

(reg-event-fx
 ::close-action-sync
 (fn [{db :db} [_ close-event]]
   {:db db
    :fx [(when close-event
           [:dispatch close-event])
         [:dispatch (fi/call-api :hide-sidebar-event-vec "config-dm-settings")]]}))

(reg-event-fx
 ::settings-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event]} params]
     (case action
       :frame/init
       {:db db}
       :frame/close
       {:dispatch [::close-action frame-id callback-event]}
       {}))))

(defn- open-sidebar-fx []
  {:fx [[:dispatch [::data/request-available-datasources default-bucket]]
        [:dispatch (fi/call-api :sidebar-create-event-vec
                                {:module "config-dm-settings"
                                 :title [::sidebar-title]
                                 :event ::settings-view-event
                                 :id "config-dm-settings"
                                 :vertical config/default-vertical-str
                                 :position :right
                                 :close-event [::close-action]
                                 :width 600})]]})

(re-frame/reg-event-fx
 ::open-sidebar
 (fn [{db :db} _]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::sync-open-sidebar])
     (open-sidebar-fx))))

(re-frame/reg-event-fx
 ::sync-open-sidebar
 (fn [{db :db} _]
   (when-let [_ (fi/call-api :service-target-db-get db :module "config-dm-settings")]
     (open-sidebar-fx))))

(defn tabs []
  (let [topics-label @(subscribe [::i18n/translate :config-topics-tab-name])
        geo-attr-label  @(subscribe [::i18n/translate :config-geo-attr-tab-name])
        datasources-label  @(subscribe [::i18n/translate :config-datasources-tab-name])
        geo-attributes  @(subscribe [::data/geographic-attributes])
        current-tab @(subscribe [::active-tab-name])]
    [:<>
     [:div.tabs__navigation.full-width
      [:div.tab
       {:class (when (= :topics current-tab) "active")
        :on-click #(do
                     (dispatch [::topics/reset-topic-management])
                     (dispatch [::active-tab :topics]))}
       topics-label]
      [:div.tab
       {:class (when (= :datasources current-tab) "active")
        :on-click #(do
                     (dispatch [::active-tab :datasources])
                     (dispatch [::datasources/init-edit-datasource nil]))}
       datasources-label]
      [:div.tab
       {:class (when (= :geo-attributes current-tab) "active")
        :on-click #(do
                     (dispatch [::active-tab :geo-attributes])
                     (dispatch [::ga/set-current-geographic-attributes geo-attributes]))}
       geo-attr-label]]
     (case current-tab
       :geo-attributes
       [ga/geo-attributes]
       :topics
       [topics/topics]
       :datasources
       [datasources/datasources])]))

(defn view [_]
  (let [receive-sync-events? @(fi/call-api :project-receive-sync?-sub)
        no-sync-hint @(re-frame/subscribe [::i18n/translate :no-sync-hint])]
    (if receive-sync-events?
      [:div.no-data-placeholder
       [:span
        [:div.loader-sm.pr-8
         [:span]]
        [:div no-sync-hint]]]
      [tabs])))