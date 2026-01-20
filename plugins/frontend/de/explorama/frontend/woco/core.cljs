(ns de.explorama.frontend.woco.core
  (:require [cljs.spec.alpha :as spec]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.core]
            [de.explorama.frontend.ui-base.utils.client :refer [console-open?]]
            [de.explorama.frontend.ui-base.utils.subs :as ui-base-util]
            [de.explorama.frontend.woco.api.client]
            [de.explorama.frontend.woco.api.compare]
            [de.explorama.frontend.woco.api.config]
            [de.explorama.frontend.woco.api.core]
            [de.explorama.frontend.woco.api.couple]
            [de.explorama.frontend.woco.api.interaction-mode]
            [de.explorama.frontend.woco.api.key]
            [de.explorama.frontend.woco.api.link-info]
            [de.explorama.frontend.woco.api.overlay :as overlay]
            [de.explorama.frontend.woco.api.product-tour :as wapt]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.api.selection]
            [de.explorama.frontend.woco.api.tools :as tools]
            [de.explorama.frontend.woco.api.welcome]
            [de.explorama.frontend.woco.api.workspace]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.configs.core]
            [de.explorama.frontend.woco.db :as db]
            [de.explorama.frontend.woco.details-view :as details-view]
            [de.explorama.frontend.woco.event-logging :as event-log]
            [de.explorama.frontend.woco.frame.api]
            [de.explorama.frontend.woco.frame.plugin-api]
            [de.explorama.frontend.woco.frame.size-position :refer [reset-frame-positions]]
            [de.explorama.frontend.woco.frame.view.core]
            [de.explorama.frontend.woco.log]
            [de.explorama.frontend.woco.login :as login]
            [de.explorama.frontend.woco.navigation.fullscreen-handler :as fullscreen-handler]
            [de.explorama.frontend.woco.navigation.resources :as nav-resources]
            [de.explorama.frontend.woco.navigation.snapping :as wns]
            [de.explorama.frontend.woco.navigation.zoom-handler :as zoom-handler]
            [de.explorama.frontend.woco.notes.core :as notes]
            [de.explorama.frontend.woco.notes.plugin-impl :as plugin-impl]
            [de.explorama.frontend.woco.notes.view :as notes-view]
            [de.explorama.frontend.woco.operations]
            [de.explorama.frontend.woco.page :as page]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.presentation.sidebar :as presentation-sidebar]
            [de.explorama.frontend.woco.scale :as scale]
            [de.explorama.frontend.woco.util.api :refer [db-get-error-boundary]]
            [de.explorama.frontend.woco.welcome :as welcome]
            [de.explorama.frontend.woco.workspace.background :as background]
            [de.explorama.frontend.woco.workspace.config :as wwc]
            [de.explorama.frontend.woco.workspace.connecting-edges :as wwce]
            [de.explorama.shared.common.configs.platform-specific :refer [explorama-multi-user]]
            [de.explorama.shared.common.fi.ws-api :as fi-ws-api]
            [de.explorama.shared.woco.ws-api :as ws-api]
            [mount.core :as mount]
            [re-frame.core :as re-frame]
            [reagent.dom :as dom]
            [taoensso.timbre :refer [info]]))

(re-frame/reg-event-fx
 ::init
 (fn [{db :db} _]
   (let [user-info (db-get-error-boundary db
                                          (registry/lookup-target db :db-get :user-info)
                                          :user-info)
         logout-event (:logout-event (registry/lookup-target db :login :rights-roles))
         papi-register (fi/api-definition :papi-register-event-vec)]
     (background/load-resources)
     (nav-resources/load-tool-resource {:icon config/details-view-icon
                                        :vertical config/details-view-vertical-namespace})
     (js/console.log "init woco event")
     {:dispatch-later [{:ms 3000 :dispatch [:de.explorama.frontend.woco.page/update-workspace-rect]}] ;; Otherwise container is not mounted -> size is wrong
      :dispatch-n [[:de.explorama.frontend.common.tubes/init-tube user-info
                    [[:backend-tube [ws-api/roles-and-users]
                      {:client-callback [ws-api/roles-and-users-result]}]
                     [:dispatch [fi-ws-api/user-preferences :backend-tube]]]]
                   [::registry/register-ui-service :update-user-info-event-vec
                    [:de.explorama.frontend.common.tubes/update-user-info]]
                   [:de.explorama.frontend.woco.navigation.control/reset-position true]
                   [::registry/register-ui-service
                    :clean-workspace
                    ::clean-workspace
                    [::clean-workspace]]
                   [::registry/register-ui-service :modules "details-view-sidebar" details-view/sidebar-view]
                   [::registry/register-ui-service :modules "presentation-sidebar-window" presentation-sidebar/sidebar-view]
                   [::registry/register-ui-service
                    :welcome-section
                    ::welcome/cards
                    {:order 10
                     :render-fn welcome/cards}]
                   [::registry/register-ui-service
                    :welcome-section
                    ::welcome/tips
                    {:order 30
                     :render-fn welcome/tips}]
                   [::tools/register {:id "workspace-export"
                                      :action-key :export
                                      :icon "icon-download"
                                      :tooltip-text [::i18n/translate :menusection-export]
                                      :title "Export"
                                      :tool-group :project
                                      :sort-order 200
                                      :action [:de.explorama.frontend.woco.screenshot.core/export-tab]}]
                   [::tools/register {:id "reporting-export"
                                      :action-key :export
                                      :icon "icon-download"
                                      :tooltip-text [::i18n/translate :menusection-export]
                                      :title "Export"
                                      :tool-group :reporting
                                      :sort-order 200
                                      :action [:de.explorama.frontend.woco.screenshot.core/export-tab]}]
                   (when explorama-multi-user
                     [::tools/register {:id "woco-logout" ;Move to rights-roles itself?
                                        :icon "icon-sign-out"
                                        :action logout-event
                                        :action-key :*
                                        :sort-order 2
                                        :tool-group :header
                                        :header-group :right
                                        :tooltip-text [::i18n/translate :log-out-tooltip]}])
                   [::tools/register {:id "presentation-edit"
                                      :icon "icon-presentation"
                                      :action-key :presentation
                                      :action [:de.explorama.frontend.woco.presentation.core/toggle-modes :editing :standard]
                                      :tool-group :header
                                      :header-group :left
                                      :sort-order 4
                                      :tooltip-text [::i18n/translate :presentation-mode]}]
                   [::tools/register {:id "details-view"
                                      :icon "icon-details-view"
                                      :action [::details-view/open-details-view false]
                                      :action-key :*
                                      :sort-order 1
                                      :tool-group :header
                                      :header-group :middle
                                      :tooltip-text [::i18n/translate :details-view-title]}]
                   [::tools/register {:icon :note
                                      :sort-order 6
                                      :action-key :*
                                      :tooltip-text [::i18n/translate :notes]
                                      :id config/note-id
                                      :tool-group :bar
                                      :bar-group :middle
                                      :vertical config/notes-vertical-str
                                      :action [::notes/spawn-new-note]}]
                   (fi/call-api [:tabs :register-event-vec]
                                {:context-id (str config/default-namespace (random-uuid))
                                 :content-context :project
                                 :origin config/default-namespace
                                 :label (re-frame/subscribe (fi/call-api :statusbar-display-sub-vec))
                                 :on-render page/workspace
                                 :active? true})
                   [::registry/register-ui-service :modules config/note-id notes-view/view]
                   (papi-register config/notes-vertical-str plugin-impl/desc)
                   [::overlay/register ::welcome/page welcome/page]
                   [::wwc/init]
                   [::wwce/init]
                   [::wns/init]
                   [::wapt/init]]

                          ; Overlay configuration - removed for now, because its not suitable for a
                          ; customer release
      #_(overlayer-config/events user-info)})))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db}
      [_ follow-event reason]]
   (reset-frame-positions)
   {:db (cond-> db
          :always (assoc-in path/details-view-events {})
          :always (update-in path/root dissoc path/details-view-key)
          :always (update-in path/root dissoc path/navigation-key)
          :always (update-in path/root dissoc path/maximized-frame-key)
          :always (update-in path/root dissoc path/sidebar-key)
          :always (dissoc :data-instances)
          (= reason :logout) (update-in path/root dissoc path/plugins-init-done-key))
    :dispatch-n [[:de.explorama.frontend.woco.api.notifications/clear-notifications]
                 [:de.explorama.frontend.woco.navigation.control/reset-position true]
                 [:de.explorama.frontend.woco.presentation.core/clean-workspace]
                 [:de.explorama.frontend.woco.notes.core/clean-workspace]
                 (conj follow-event ::clean-workspace)
                 [:de.explorama.frontend.woco.workspace.background/reset]]}))

(defn dev-setup []
  (when config/debug?
    (spec/check-asserts true)
    (info "dev mode")))

(defn rerender []
  (dom/render [:f> page/main-panel]
              (.getElementById js/document "app")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (dom/render [:f> page/main-panel]
              (.getElementById js/document "app"))
  (re-frame/dispatch [::registry/register-ui-service :login-success-events :woco-check-rights [::login/check-rights]])
  (re-frame/dispatch [::registry/register-ui-service :logout-events :woco-logout [::login/logout]])
  (re-frame/dispatch [::registry/register-ui-service :event-replay "woco" {:event-replay ::event-log/replay-events
                                                                           :replay-progress path/replay-progress}])
  (re-frame/dispatch [::registry/register-ui-service :event-sync "woco" ::event-log/sync-event])
  (re-frame/dispatch [::registry/register-ui-service :event-protocol "woco" event-log/events->steps])
  (re-frame/dispatch [::db/reg-init-event ::init "woco"])
  (re-frame/dispatch [:de.explorama.frontend.woco.api.link-info/read-location])
  (re-frame/dispatch [::scale/update-scale-info]))

(defn ^:export init []
  (js/console.log "init woco")
  (dev-setup)
  (mount/start)
  (re-frame/dispatch-sync [::db/initialize])
  (ui-base-util/set-translation-fn #(re-frame/subscribe [::i18n/translate %]))
  (mount-root)
  (aset (aget js/document "body")
        "onresize" #(do
                      (re-frame/dispatch [::page/update-workspace-rect])
                      (re-frame/dispatch [::scale/update-scale-info]))))

(zoom-handler/register-handler)
(fullscreen-handler/register-handler)

;; disables default contextmenu when browser console is closed
(.addEventListener js/document "contextmenu" #(when (and config/disable-default-context-menu?
                                                         (not (console-open?)))
                                                (.preventDefault %)))
(.addEventListener js/document "keyup" #(re-frame/dispatch [:de.explorama.frontend.woco.api.key/up %]))
(.addEventListener js/document "keydown" #(re-frame/dispatch [:de.explorama.frontend.woco.api.key/down %]))

