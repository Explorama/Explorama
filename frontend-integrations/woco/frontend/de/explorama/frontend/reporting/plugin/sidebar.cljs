(ns de.explorama.frontend.reporting.plugin.sidebar
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.reporting.components.dialog :as dialog]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.data.dashboards :as dashboards]
            [de.explorama.frontend.reporting.data.reports :as reports]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.util.frames :refer [id-type]]
            [de.explorama.frontend.reporting.views.builder :as builder]
            [de.explorama.frontend.reporting.views.dashboards.overview :as d-overview]
            [de.explorama.frontend.reporting.views.dropzone :as dropzone]
            [de.explorama.frontend.reporting.views.reports.overview :as r-overview]
            [de.explorama.frontend.reporting.views.share-dr :as share-dr]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position context-menu]]
            [de.explorama.frontend.ui-base.utils.interop :refer [safe-number?]]
            [de.explorama.shared.common.configs.platform-specific :as config-shared-platform]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub
                                   subscribe]]
            [taoensso.timbre :refer [debug error]]))

(defn- handle-drop [dom-id dropped-frame-id]
  (if-not (config/support-vertical? (:vertical dropped-frame-id))
    (debug "Vertical not supported for dr" dropped-frame-id)
    (let [tile-idx (dr-path/dom-id->tile-idx dom-id)]
      (if (safe-number? tile-idx)
        (do (dispatch
             (fi/call-api :frame-query-event-vec
                          dropped-frame-id
                          :vis-desc
                          [::vis-data-response tile-idx dropped-frame-id]))
            (dropzone/clear-dropzone-state))
        (error "tile-idx extraction failed" tile-idx)))))

(defn- handle-drag-enter [dom-id dragging-frame-id]
  (let [tile-idx (dr-path/dom-id->tile-idx dom-id)]
    (when (and (config/support-vertical? (:vertical dragging-frame-id))
               (safe-number? tile-idx))
      (dropzone/activate-dropzone-state tile-idx))))

(defn- handle-drag-leave [dom-id dragging-frame-id]
  (let [tile-idx (dr-path/dom-id->tile-idx dom-id)]
    (when (and (config/support-vertical? (:vertical dragging-frame-id))
               (safe-number? tile-idx))
      (dropzone/clear-dropzone-state tile-idx))))


(reg-event-fx
 ::register-drop-handler
 (fn [{db :db} [_ dom-ids]]
   (when-let [service-register-db-update (fi/api-definition :service-register-db-update)]
     {:db (-> db
              (service-register-db-update :frame-drop-hitbox
                                          ::hitboxes
                                          {:dom-ids dom-ids
                                           :global-context? true
                                           :is-on-top? true
                                           :on-drag-enter handle-drag-enter
                                           :on-drag-leave handle-drag-leave
                                           :on-drop handle-drop}))})))

(reg-event-fx
 ::unregister-drop-handler
 (fn [{db :db}]
   (when-let [service-deregister-db-update (fi/api-definition :service-deregister-db-update)]
     {:db (-> db
              (service-deregister-db-update :frame-drop-hitbox
                                            ::hitboxes))})))

(reg-event-fx
 ::vis-data-response
 (fn [{db :db} [_ tile-idx dropped-frame-id {:keys [tool preview title vertical di context-menu] :as state}]]
   {:db (assoc-in db (dr-path/creation-module-desc tile-idx) {:title title
                                                              :preview preview
                                                              :vertical vertical
                                                              :context-menu context-menu
                                                              :tool tool
                                                              :state (dissoc state :preview :ratio :context-menu)
                                                              :di di})}))

(defn fetch-vis-data [tile-idx dropped-frame-id]
  (dispatch (fi/call-api :frame-query-event-vec
                         dropped-frame-id
                         :vis-desc
                         [::vis-data-response tile-idx dropped-frame-id])))

(reg-event-db
 ::show-creation
 (fn [db [_ show?]]
   (assoc-in db dr-path/creation-show? (if (boolean? show?)
                                         show?
                                         true))))

(reg-sub
 ::show-creation?
 (fn [db]
   (get-in db dr-path/creation-show?)))

(reg-event-fx
 ::edit-dr
 (fn [{db :db} [_ {:keys [id] :as dr} sidebar-props]]
   (let [type (id-type db id)]
     {:fx [[:dispatch [::builder/edit type dr sidebar-props]]
           [:dispatch [::show-creation]]]})))

(reg-event-fx
 ::delete-dr
 (fn [{db :db} [_ id]]
   (when-let [dispatch (case (id-type db id)
                         :dashboard [::dashboards/delete-dashboard id]
                         :report [::reports/delete-report id]
                         nil)]
     {:dispatch dispatch})))

(reg-sub
 ::url-key
 (fn [db [_ id]]
   (case (id-type db id)
     :dashboard  "dashboard"
     :report "report"
     nil)))

(reg-sub
 ::edit-disabled?
 (fn [db [_ creator]]
   (let [{user :username} (fi/call-api :user-info-db-get db)]
     (not= creator user))))

(defn- standalone-link [id]
  (when-let [url-key @(subscribe [::url-key id])]
    (str config-shared-platform/explorama-origin "/"
         url-key "/"
         id "/")))

(reg-sub
 ::burger-menu-infos
 (fn [db]
   (get-in db dr-path/burger-menu-infos)))

(reg-event-db
 ::set-burger-menu-infos
 (fn [db [_ infos]]
   (assoc-in db dr-path/burger-menu-infos infos)))

(defn burger-menu [sidebar-props]
  (let [{:keys [dr event]} @(subscribe [::burger-menu-infos])
        {:keys [id type]} dr
        url (standalone-link id)
        share-label @(subscribe [::i18n/translate :share-label])
        edit-label @(subscribe [::i18n/translate :edit-label])
        delete-label @(subscribe [::i18n/translate :delete-label])
        open-new-tab-label @(subscribe [::i18n/translate :open-new-tab-label])
        {:keys [top left]} (calc-menu-position event)]
    [context-menu
     {:show? dr
      :on-close #(dispatch [::set-burger-menu-infos])
      :position  {:top top :left left}
      :menu-z-index 250000
      :items [{:label open-new-tab-label
               :icon :open-new-tab
               :on-click #(if (= type :dashboard)
                            (dispatch [::d-overview/show-dashboard id])
                            (dispatch [::r-overview/show-report id]))}
              {:label edit-label
               :icon :edit
               :on-click #(dispatch [::edit-dr dr sidebar-props])}
              {:label share-label
               :disabled? (not config-shared-platform/explorama-multi-user)
               :icon :upload
               :on-click #(dispatch [:de.explorama.frontend.reporting.views.share-dr/show-share dr url])}
              {:label delete-label
               :icon :trash
               :on-click (fn [e]
                           (.stopPropagation e)
                           (dispatch [::dialog/set-data :delete-dr {:delete-event [::delete-dr id]
                                                                    :type type}])
                           (dispatch [::dialog/show-dialog :delete-dr true :delete]))}]}]))

(defn- dashboard-column [sidebar-props]
  (let [dashboards-label @(subscribe [::i18n/translate :dashboards-label])]
    [:div.h-6-12.overflow-auto
     [:h2 dashboards-label]
     [button {:on-click (fn []
                          (dispatch [::builder/init-new :dashboard sidebar-props])
                          (dispatch [::show-creation]))
              :start-icon :plus
              :label @(subscribe [::i18n/translate :create-dashboard-label])
              :size :big}]
     [d-overview/overview nil standalone-link]]))

(defn- report-column [sidebar-props]
  (let [reports-label @(subscribe [::i18n/translate :reports-label])]
    [:div.h-6-12.overflow-auto
     [:h2 reports-label]
     [button {:on-click (fn []
                          (dispatch [::builder/init-new :report sidebar-props])
                          (dispatch [::show-creation]))
              :start-icon :plus
              :label @(subscribe [::i18n/translate :create-report-label])
              :size :big}]
     [r-overview/overview nil standalone-link]]))

(defn content [sidebar-props]
  (let [sidebar-props (assoc sidebar-props
                             :register-tiles (fn [dom-ids]
                                               (dispatch [::register-drop-handler dom-ids]))
                             :unregister-tiles (fn []
                                                 (dispatch [::unregister-drop-handler])))
                            ;;  :on-drop (fn [tile-idx]
                            ;;             (partial on-drop (partial fetch-vis-data tile-idx))))
        pending-creation? @(subscribe [::show-creation?])
        receive-sync-events? @(fi/call-api :project-receive-sync?-sub)
        no-sync-hint @(subscribe [::i18n/translate :no-sync-hint])]
    (cond
      receive-sync-events?
      [:div.no-data-placeholder
       [:span
        [:div.loader-sm.pr-8
         [:span]]
        [:div no-sync-hint]]]
      pending-creation? [builder/builder sidebar-props]
      :else
      [:div.content {:on-click #(dispatch [::set-burger-menu-infos nil])}
       [burger-menu sidebar-props]
       [share-dr/share-options]
       [dialog/confirm-dialog :delete-dr]
       [dashboard-column sidebar-props]
       [report-column sidebar-props]])))



