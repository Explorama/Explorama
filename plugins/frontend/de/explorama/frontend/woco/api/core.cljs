(ns de.explorama.frontend.woco.api.core
  (:require [de.explorama.frontend.common.fi.user-preferences :as user-preferences]
            [de.explorama.frontend.common.validation :refer [validate?]]
            [de.explorama.frontend.woco.api.compare :as compare]
            [de.explorama.frontend.woco.api.config :as aconfig]
            [de.explorama.frontend.woco.api.couple :as couple]
            [de.explorama.frontend.woco.api.flags :as flags-api]
            [de.explorama.frontend.woco.api.interaction-mode :as im]
            [de.explorama.frontend.woco.api.key :as key-api]
            [de.explorama.frontend.woco.api.link-info :as link-info-api]
            [de.explorama.frontend.woco.api.overlay :as overlay-api]
            [de.explorama.frontend.woco.api.product-tour :as product-tour]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.api.selection :as aselections]
            [de.explorama.frontend.woco.api.statusbar :as statusbar-api]
            [de.explorama.frontend.woco.api.tabs :as tabs-api]
            [de.explorama.frontend.woco.api.welcome :as welcome-api]
            [de.explorama.frontend.woco.api.workspace :as workspace-api]
            [de.explorama.frontend.woco.components.context-menu :as wcm]
            [de.explorama.frontend.woco.db :as db]
            [de.explorama.frontend.woco.details-view :as details-view]
            [de.explorama.frontend.woco.frame.api :as aframe]
            [de.explorama.frontend.woco.frame.color :as frame-color]
            [de.explorama.frontend.woco.frame.core :as frame-creation]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.filter.core :as filter]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.frame.plugin-api :as papi]
            [de.explorama.frontend.woco.frame.view.core :as view-core]
            [de.explorama.frontend.woco.frame.view.header :as frame-header]
            [de.explorama.frontend.woco.frame.view.overlay.notifications :as frame-notifications]
            [de.explorama.frontend.woco.log :as logging]
            [de.explorama.frontend.woco.navigation.control :as nav-control]
            [de.explorama.frontend.woco.navigation.core :as nav]
            [de.explorama.frontend.woco.navigation.util :as nav-util]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.preferences.client :as client-preferences]
            [de.explorama.frontend.woco.scale :as scale]
            [de.explorama.frontend.woco.screenshot.core :as screenshot]
            [de.explorama.frontend.woco.sidebar :as sidebar]
            [de.explorama.frontend.woco.tools :as tools]
            [de.explorama.frontend.woco.util.api :refer [db-get-error-boundary event-error-boundary
                                                         sub-error-boundary]]
            [de.explorama.shared.common.configs.platform-specific :as config-shared-platform]
            [de.explorama.shared.common.fi.ws-api :as fi-ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

;TODO r1/malli use the schema or remove it
(def ^:private create-frame-event-spec
  [:map
   [:publishing-frame-id {:optional true} map?]
   [:multiple-windows? {:optional true} boolean?]
   [:payload
    [:map
     [:local-filter {:optional true} vector?]
     [:layouts {:optional true} vector?]
     [:di {:optional true} map?]
     [:selections {:optional true} vector?]]]])

;TODO r1/malli use the schema or remove it
(def ^:private init-create-frame-event-spec
  [:map
   [:opts {:optional true}
    [:map
     [:publishing-frame-id {:optional true} map?]
     [:multiple-windows? {:optional true} boolean?]
     [:overwrites {:optional true}
      [:info {:optional true}
       [:map
        [:local-filter {:optional true} vector?]
        [:layouts {:optional true} vector?]
        [:di {:optional true} map?]
        [:selections {:optional true} vector?]]]
      [:configuration {:optional true} map?]]]]])

;TODO r1/malli use the schema or remove it
(def ^:private frame-info-api-spec
  [:map
   [:local-filter fn?]
   [:layouts fn?]
   [:di fn?]
   [:selections fn?]])

;TODO r1/malli use the schema or remove it
(def ^:private frame-desc
  [:map
   [:local-filter fn?]
   [:layouts fn?]
   [:di fn?]
   [:selections fn?]])

(re-frame/reg-event-fx
 ::no-op
 (fn [_ _]))

;; Naming Pattern:
;; :...-raw (fn [] raw-result)
;; :...-fn (fn [] res-fn)
;; :...-sub (fn [] res-sub)
;; :...-sub-vec (fn [] res-sub-vec)
;; :...-db-update (fn [] db)
;; :...-db-get (fn [] res-from-db)
;; :...-event-vec (fn [] event-vec)
;; :...-event-dispatch (fn [] <dispatch> nil)

(set! (.-FIApi js/window)
      {:fi-origin-raw (fn [] config-shared-platform/explorama-origin)
       :fi-origin-assets-raw (fn [] config-shared-platform/explorama-asset-origin)
       :init-register-event-dispatch (fn [callback-event vertical]
                                       (re-frame/dispatch [::db/reg-init-event callback-event vertical]))
       :init-done-event-vec (fn [vertical]
                              [::db/init-done vertical])

       :service-register-event-vec (fn [category service target]
                                     [::registry/register-ui-service category service target])
       :service-register-db-update registry/register-ui-service

       :service-deregister-event-vec (fn [category service]
                                       [::registry/unregister-ui-service category service])
       :service-deregister-db-update registry/unregister-ui-service

       :service-category-sub (fn [category]
                               (re-frame/subscribe [::registry/lookup-category category]))
       :service-category-sub-vec (fn [category]
                                   [::registry/lookup-category category])
       :service-category-db-get registry/lookup-category

       :service-target-sub (fn [category service]
                             (re-frame/subscribe [::registry/lookup-target category service]))
       :service-target-sub-vec (fn [category service]
                                 [::registry/lookup-target category service])
       :service-target-db-get registry/lookup-target

       :tabs {:tab-content-size-db-get tabs-api/tab-content-size
              :tab-content-size-sub (fn []
                                      (re-frame/subscribe [::tabs-api/tab-content-size]))
              :register-event-vec (fn [desc]
                                    [::tabs-api/register desc])
              :deregister-event-vec (fn [desc]
                                      [::tabs-api/deregister desc])}
       :tools-register-event-vec (fn [desc]
                                   [:de.explorama.frontend.woco.api.tools/register desc])
       :tools-deregister-event-vec (fn [desc]
                                     [:de.explorama.frontend.woco.api.tools/deregister desc])
       :tool-desc-db-get (fn [db tool-id]
                           (tools/get-tool db tool-id))
       :module-db-get (fn [db id]
                        (registry/lookup-target db :modules id))

       :overlay-register-event-vec (fn [id module]
                                     [::overlay-api/register id module])
       :overlay-deregister-event-vec (fn [id]
                                       [::overlay-api/deregister id])
       :overlayer-active-db-get (fn [db]
                                  (get-in db path/overlayer-active?))
       :overlayer-active-sub (fn []
                               (re-frame/subscribe [::overlay-api/overlayer-active?]))
       :overlayer-active-event-vec (fn [active?]
                                     [::overlay-api/overlayer-active active?])

       :welcome-register-interceptor-event-vec (fn [callback-fx]
                                                 [::welcome-api/register-interceptor callback-fx])
       :welcome-dismiss-page-event-vec (fn [callback-events]
                                         [::welcome-api/dismiss-page callback-events])
       :welcome-close-page-event-vec (fn []
                                       [::welcome-api/close-page])

       :load-users-roles-event-vec (fn []
                                     [:de.explorama.frontend.woco.api.config/load-users-roles])

       :user-info-sub (fn []
                        (sub-error-boundary [::registry/lookup-target :sub-vector :user-info]))
       :user-info-db-get (fn [db]
                           (db-get-error-boundary db
                                                  (registry/lookup-target db :db-get :user-info)
                                                  :user-info))

       :users-sub (fn [] (re-frame/subscribe [::aconfig/users]))
       :users-db-get (fn [db] (aconfig/users db))

       :roles-sub (fn [] (re-frame/subscribe [::aconfig/roles]))
       :roles-db-get (fn [db] (aconfig/roles db))

       :name-for-user-db-get (fn [db username]
                               (aconfig/name-for-user db username))
       :name-for-user-sub (fn [username]
                            (re-frame/subscribe [::aconfig/name-for-user username]))

       :frame-create-event-vec (fn [desc]
                                 (validate? ::frame-creation/create-frame init-create-frame-event-spec desc)
                                 [::frame-creation/create-frame desc])
       :frame-close-event-vec (fn [frame-id]
                                [::aframe/close frame-id])
       :sidebar-create-event-vec (fn [desc]
                                   [:de.explorama.frontend.woco.sidebar/create-sidebar desc])

       :frame-query-event-vec (fn [frame-id type callback]
                                ; THIS API IS DEPRECATED!
                                [::aframe/query frame-id type callback])

       :frame-info-api-get-db (fn [db frame-id & [vertical]]
                                (registry/lookup-target db
                                                        :frame-info-api
                                                        (or vertical
                                                            (:vertical frame-id))))
       :frame-info-api-value-sub (fn [frame-id values]
                                   (re-frame/subscribe [::frame-info/api-value frame-id values]))
       :frame-info-api-register-event-vec (fn [vertical desc]
                                            (validate? ::services frame-info-api-spec desc)
                                            [::registry/register-ui-service :frame-info-api vertical desc])
       :frame-instance-api-register-event-vec (fn [vertical desc]
                                                [::registry/register-ui-service :frame-instance-api vertical desc])
       :frame-instance-api-sub (fn [frame-id]
                                 (re-frame/subscribe [::registry/lookup-target :frame-instance-api (:vertical frame-id)]))
       :frame-instance-api-db-get (fn [db frame-id]
                                    (registry/lookup-target db :frame-instance-api (:vertical frame-id)))
       :frame-header-color-event-vec (fn [frame-id]
                                       [::frame-color/new-header-color frame-id])
       :frame-published-by-sub (fn [frame-id]
                                 (re-frame/subscribe [::frame-info/published-by frame-id]))
       :frame-publishing-sub (fn [frame-id]
                               (re-frame/subscribe [::frame-info/publishing? frame-id]))
       :frame-set-publishing-event-vec (fn [frame-id value]
                                         [::frame-info/set-publishing frame-id value])
       :hide-sidebar-event-dispatch (fn [id] (re-frame/dispatch [::sidebar/hide-sidebar id]))
       :hide-sidebar-event-vec (fn [id] [::sidebar/hide-sidebar id])
       :sidebar-width-sub (fn []
                            (re-frame/subscribe [::sidebar/sidebar-width]))

       :interaction-mode {:set-no-render-event (fn [] [::im/set-no-render])
                          :set-pending-read-only-event (fn [] [::im/set-pending-read-only])
                          :set-pending-normal-event (fn [] [::im/set-pending-normal])
                          :set-render-event (fn [render-done-event origin] [::im/set-render render-done-event origin])
                          :set-normal-event (fn [] [::im/set-normal])
                          :set-read-only-event (fn [] [::im/set-read-only])

                          :read-only-sub? (fn [additional-infos]
                                            (re-frame/subscribe [::im/read-only? additional-infos]))
                          :read-only-db-get? im/read-only?
                          :pending-read-only-sub? (fn []
                                                    (re-frame/subscribe [::im/pending-read-only?]))
                          :pending-read-only-db-get? im/pending-read-only?
                          :current-sub? (fn [additional-infos]
                                          (re-frame/subscribe [::im/current additional-infos]))
                          :current-sub-vec (fn [additional-infos] [::im/current additional-infos])
                          :current-db-get? (fn [db additional-infos] ; make sure there is only one parameter
                                             (im/interaction-mode db additional-infos))
                          :render-sub? (fn []
                                         (re-frame/subscribe [::im/render?]))
                          :render-sub-vec (fn [] [::im/render?])
                          :render-db-get? im/render?
                          :normal-sub? (fn [additional-infos]
                                         (re-frame/subscribe [::im/normal? additional-infos]))
                          :normal-sub-vec? (fn [additional-infos]
                                             [::im/normal? additional-infos])
                          :normal-db-get? im/normal?}

       :workspace-id-vec (fn [id]
                           [::workspace-api/id id])
       :workspace-id-db-get (fn [db]
                              (get-in db (path/workspace-id)))
       :workspace-id-sub-vec (fn []
                               [::workspace-api/id])


       :details-view {:add-event-compare-event-vec (fn [desc] [::compare/add-event desc])
                      :update-event-data-db-update details-view/update-event-data
                      :add-to-details-view-db-update details-view/add-to-details-view
                      :can-add?-db-get details-view/can-add?
                      :remove-event-from-details-view-db-update details-view/remove-from-details-view
                      :remove-frame-events-from-details-view-db-update details-view/remove-frame-events-from-details-view}

       :papi-register-event-vec (fn [vertical desc]
                                  [::papi/register vertical desc])
       :papi-api-db-get (fn [db vertical access-key]
                          (papi/api db vertical access-key))
       :papi-api-sub (fn [frame-id & [path]]
                       (re-frame/subscribe [::papi/api frame-id path]))
       :papi-loading-screen-sub (fn [frame-id]
                                  (re-frame/subscribe [::papi/loading-screen frame-id]))
       :papi-frame-header-desc-sub (fn [frame-id]
                                     (re-frame/subscribe [::papi/frame-header frame-id]))
       :connect-code-event-vec (fn [frame-source-id connection-id frame-target-id]
                                 [::aframe/connect-code frame-source-id connection-id frame-target-id])

       :frame-update-children (fn [frame-id values]
                                [::frame-info/update-children frame-id values])

       :frame-title-all-db-get aframe/title-all
       :frame-title-all-sub-vec (fn [frame-id]
                                  [::aframe/title-all frame-id])
       :frame-size-delta-db-get aframe/size-delta
       :frame-size-delta-sub-vec (fn [frame-id]
                                   [::aframe/size-delta frame-id])
       :frame-notifications-not-supported-redo-ops-event-vec (fn [frame-id not-supported-ops]
                                                               [::frame-notifications/not-supported-redo-ops frame-id not-supported-ops])
       :frame-notifications-clear-event-vec (fn [frame-id]
                                              [::frame-notifications/clear-notification frame-id])
       :frame-notifications-clear-event-dispatch (fn [frame-id]
                                                   (re-frame/dispatch [::frame-notifications/clear-notification frame-id]))

       :frame-title-all-sub (fn [frame-id]
                              (re-frame/subscribe [::aframe/title-all frame-id]))
       :frame-db-get (fn [db frame-id]
                       (evts/frame-state-aware db frame-id))
       :frame-sub (fn [frame-id]
                    (re-frame/subscribe [::evts/frame-state-aware frame-id]))
       :frame-content-padding-raw (fn []
                                    {:height 32 ;Header height
                                     :width 10})

       :frame-filter-db-get (fn [db frame-id]
                              (get-in db (path/frame-last-applied-filters frame-id)))
       :frame-filter-db-update (fn [db frame-id]
                                 (filter/update-last-applied-filters db frame-id))
       :frame-filter-sub (fn [frame-id]
                           [::filter/last-applied-filters frame-id])

       :frame-set-title-event-vec (fn [title frame-id]
                                    [::aframe/set-title title frame-id])
       :frame-title-sub (fn [frame-id]
                          [::aframe/title frame-id])
       :frame-title-db-get (fn [db frame-id]
                             (aframe/title db frame-id))
       :full-frame-title-raw (fn [frame-id]
                               (frame-header/full-title frame-id))

       :frame-delete-quietly-event-vec (fn [frame-id]
                                         [::aframe/delete-quietly frame-id])
       :make-screenshot-raw (fn [{:keys [dom-id frame-id] :as props}]
                              (when-let [screenshot-fn (cond frame-id screenshot/make-frame-screenshot
                                                             dom-id screenshot/make-screenshot)]
                                (screenshot-fn props)))
       :trigger-optimization-screenshot! (fn [frame-id]
                                           (view-core/trigger-screenshot-ext frame-id))

       :frame-bring-to-front-event-vec (fn [frame-id]
                                         [::aframe/bring-to-front frame-id])
       :initiate-vertical-drag-event-vec (fn [drag-infos]
                                           [::aframe/initiate-vertical-drag drag-infos])
       :reset-vertical-drag-event-vec (fn []
                                        [::aframe/reset-vertical-drag])

       :list-frames-vertical-db-get (fn [db vertical & [query]]
                                      (aframe/list-id-vertical db vertical query))
       :list-frames-vertical-sub (fn [vertical & [query]]
                                   (re-frame/subscribe [::aframe/list-id-vertical vertical query]))

       :user-preferences {:save-event-vec (fn [pref-key pref-val]
                                            [fi-ws-api/save-user-preference :backend-tube pref-key pref-val])
                          :preferences-loaded-db-get user-preferences/user-preferences-loaded
                          :preferences-loaded-sub (fn []
                                                    (re-frame/subscribe [::user-preferences/user-preferences-loaded]))
                          :preferences-db-get user-preferences/get-preferences
                          :preferences-sub (fn []
                                             (re-frame/subscribe [::user-preferences/user-preferences]))
                          :preference-db-get user-preferences/get-preference
                          :preference-sub (fn [pref-key default-val]
                                            (re-frame/subscribe [::user-preferences/user-preference pref-key default-val]))
                          :add-preference-watcher (fn [pref-key event default-val & [not-immediate?]]
                                                    (re-frame/dispatch [::user-preferences/add-watcher pref-key event default-val not-immediate?]))
                          :add-preference-watcher-event-vec (fn [pref-key event default-val & [not-immediate?]]
                                                              [::user-preferences/add-watcher pref-key event default-val not-immediate?])
                          :rm-preference-watcher (fn [pref-key event]
                                                   (re-frame/dispatch [::user-preferences/rm-watcher pref-key event]))}

       :client-preferences {:set-event-vec (fn [pref-key pref-val]
                                             [::client-preferences/set-preference pref-key pref-val])
                            :preference-set-raw (fn [username pref-key pref-val]
                                                  (client-preferences/set-preference username pref-key pref-val))
                            :preference-get-raw (fn [username pref-key & [default-val]]
                                                  (client-preferences/get-preference username pref-key default-val))
                            :preference-db-get client-preferences/get-preferences-db
                            :preference-sub (fn [pref-key default-val]
                                              (re-frame/subscribe [::client-preferences/get-preference pref-key default-val]))
                            :add-watcher-raw (fn [pref-key changed-fn]
                                               (client-preferences/add-storage-watcher pref-key changed-fn))
                            :remove-watcher-raw (fn [pref-key]
                                                  (client-preferences/remove-storage-watcher pref-key))}
       :translate-size-sub (fn [params]
                             (re-frame/subscribe [::nav/translate-size params]))
       :workspace-coords->page-coords-db-get (fn [db coords]
                                               (nav-control/workspace->page db coords))
       :workspace-coords->page-coords-sub (fn [coords respect-app-header-heigt?]
                                            (re-frame/subscribe [::nav-control/workspace->page coords respect-app-header-heigt?]))
       :workspace-position->page-position-fn nav-control/workspace-position->page
       :page->workspace-sub (fn [coords]
                              (re-frame/subscribe [::nav-control/page->workspace coords]))
       :page->workspace-db-get nav-control/page->workspace
       :workspace-position-sub-vec (fn []
                                     [::nav-control/position])
       :workspace-rect-fn nav-util/workspace-rect
       :workspace-scale-sub (fn []
                              (re-frame/subscribe [::nav-control/workspace-scale]))
       :scale-info-sub (fn [_]
                         (re-frame/subscribe [::scale/scale-info]))

       :list-frames-get-db (fn [db query]
                             (aframe/list-frames db query))
       :list-frames-sub (fn [query]
                          (re-frame/subscribe [::aframe/list query]))
       :clean-workspace-event-vec (fn [follow-event reason]
                                    [::aframe/clean-workspace follow-event reason])
       :couple-submit-action-vec (fn [source-frame-id action-desc]
                                   [::couple/submit-couple-action source-frame-id action-desc])
       :coupled-with-db-get (fn [db frame-id]
                              (couple/couple-with db frame-id))
       :coupled-with-sub (fn [frame-id]
                           (re-frame/subscribe [::couple/couple-with frame-id]))
       :couple-event-vec (fn [source-frame-id target-frame-id couple-infos no-event-logging?]
                           [::couple/couple source-frame-id target-frame-id couple-infos no-event-logging?])
       :decouple-event-vec (fn [frame-id]
                             [::couple/decouple frame-id])
       :reset-selections-event-vec (fn [source-frame-id source-infos]
                                     [::aselections/reset-selections source-frame-id source-infos])
       :selections-sub (fn [frame-id]
                         (re-frame/subscribe [::aselections/selections frame-id]))
       :selections-db-get (fn [db frame-id]
                            (aselections/selections db frame-id))

       :select-event-vec (fn [frame-id selection source-infos]
                           [::aselections/select frame-id selection source-infos])
       :deselect-event-vec (fn [frame-id deselection source-infos]
                             [::aselections/deselect frame-id deselection source-infos])

       :render-done-event-vec (fn [frame-id vertical]
                                ;this event should be injected
                                (debug "render-done is deprecated")
                                [::aframe/render-done frame-id vertical])

       :open-search-with-rows-event-vec (fn [db rows create-id?]
                                          (event-error-boundary (registry/lookup-target db :event-vec :search-open-with-rows)
                                                                rows
                                                                create-id?))
       :config-theme-db-get  (fn [db]
                               (db-get-error-boundary db
                                                      (registry/lookup-target db :db-get :get-theme)
                                                      :get-theme-db-get))
       :config-theme-sub (fn []
                           (sub-error-boundary [::registry/lookup-target :sub-vector :get-theme]))

       :i18n {:available-languages-db-get (fn [db]
                                            (db-get-error-boundary db
                                                                   (registry/lookup-target db :db-get :available-languages)
                                                                   :available-languages-db-get))
              :get-labels-db-get (fn [db]
                                   (db-get-error-boundary db
                                                          (registry/lookup-target db :db-get :get-labels)
                                                          :get-labels-db-get))
              :get-labels-sub (fn []
                                (sub-error-boundary [::registry/lookup-target :sub-vector :get-labels]))
              :translate-db-get (fn [db word-key]
                                  (db-get-error-boundary db
                                                         (registry/lookup-target db :db-get :translate)
                                                         :translate-db-get
                                                         word-key))
              :translate-multi-db-get (fn [db word-keys]
                                        (db-get-error-boundary db
                                                               (registry/lookup-target db :db-get :translate-multi)
                                                               :translate-multi-db-get
                                                               word-keys))}

       :config {:get-config-db-get (fn [db config-type config-id]
                                     (db-get-error-boundary db
                                                            (registry/lookup-target db :db-get :get-config)
                                                            :get-config-db-get
                                                            config-type
                                                            config-id))
                :get-config-sub (fn [config-type config-id]
                                  (sub-error-boundary [::registry/lookup-target :sub-vector :get-config]
                                                      config-type
                                                      config-id))

                :project-post-processing {:check-for-updates-db-update (fn [db config-type configs]
                                                                         (db-get-error-boundary db
                                                                                                (registry/lookup-target db :db-update :check-for-updates)
                                                                                                :check-for-updates-db-update
                                                                                                config-type
                                                                                                configs))}}

       :project-loading-db-get (fn [db]
                                 (db-get-error-boundary db
                                                        (registry/lookup-target db :db-get :project-loading?)
                                                        :project-loading-db-get))
       :loaded-project-db-get (fn [db]
                                (db-get-error-boundary db
                                                       (registry/lookup-target db :db-get :loaded-project)
                                                       :loaded-project))
       :project-loading-sub (fn [_]
                              (sub-error-boundary [::registry/lookup-target :sub-vector :project-loading?]))

       :loaded-project-sub (fn []
                             (sub-error-boundary [::registry/lookup-target :sub-vector :loaded-project]))

       :project-current-step-db-get (fn [db]
                                      (db-get-error-boundary db
                                                             (registry/lookup-target db :db-get :current-project-step)
                                                             :current-project-step))

       :project-load-step-event-vec (fn [db step]
                                      (event-error-boundary (registry/lookup-target db :event-vec :load-step)
                                                            step))

       :project-receive-sync?-sub (fn []
                                    (sub-error-boundary [::registry/lookup-target :sub-vector :receive-sync-events?]))
       :project-receive-sync?-db-get (fn [db]
                                       (db-get-error-boundary db
                                                              (registry/lookup-target db :db-get :receive-sync-events?)
                                                              :receive-sync-events?p))

       :product-tour {:active?-sub (fn []
                                     (re-frame/subscribe [::product-tour/product-tour-active?]))
                      :component-active?-db-get product-tour/component-active?-db
                      :component-active?-sub-vec (fn [component additional-info]
                                                   [::product-tour/component-active? component additional-info])
                      :component-active?-sub (fn [component additional-info]
                                               (re-frame/subscribe [::product-tour/component-active? component additional-info]))
                      :next-event-vec (fn [vertical step]
                                        [::product-tour/next-step vertical step])
                      :previous-event-vec (fn []
                                            [::product-tour/previous-step])
                      :cancel-event-vec (fn []
                                          [::product-tour/cancel-product-tour])
                      :max-steps-sub-vec (fn []
                                           [::product-tour/max-steps])
                      :max-steps-sub (fn []
                                       (re-frame/subscribe [::product-tour/max-steps]))
                      :current-db-get product-tour/current-step
                      :current-sub-vec (fn []
                                         [::product-tour/current-step])
                      :current-sub (fn []
                                     (re-frame/subscribe [::product-tour/current-step]))}
       :statusbar-register-vec (fn [id status]
                                 [::statusbar-api/reg-status id status])
       :statusbar-deregister-vec (fn [id]
                                   [::statusbar-api/del-status id])
       :statusbar-info-sub-vec (fn [] [::statusbar-api/status-info])
       :statusbar-display-sub-vec (fn [] [::statusbar-api/status-display])


       :notify-event-dispatch (fn [desc]
                                (re-frame/dispatch [:de.explorama.frontend.woco.api.notifications/notify desc]))
       :notify-event-vec (fn [desc]
                           [:de.explorama.frontend.woco.api.notifications/notify desc])

       :info-event-vec (fn [msg]
                         [::logging/info msg])
       :debug-event-vec (fn [msg]
                          [::logging/debug msg])
       :error-event-vec (fn [msg]
                          [::logging/error msg])

       :client-id-db-get (fn [db]
                           (get-in db path/client-id))
       :client-id-sub (fn []
                        (re-frame/subscribe [:de.explorama.frontend.woco.api.client/client-id]))

       :link-info-url-infos-db-get (fn [db keys]
                                     (link-info-api/url-infos db keys))
       :link-info-url-infos-sub-vec (fn [keys]
                                      [::link-info-api/url-infos keys])

       :link-info-remove-infos-db-update (fn [db url-key]
                                           (link-info-api/remove-url-info db url-key))
       :link-info-remove-infos-event-vec (fn [url-key]
                                           [::link-info-api/remove-url-info url-key])

       :link-info-url-info-db-get (fn [db url-key]
                                    (link-info-api/url-info db url-key))
       :link-info-url-info-sub-vec (fn [url-key]
                                     [::link-info-api/url-info url-key])
       :clear-url-info-event-vec (fn [] [::link-info-api/clear-url-info])
       :key-pressed-sub (fn []
                          (re-frame/subscribe [::key-api/pressed]))

       :context-menu-event-dispatch (fn [event dialog-infos ctx-infos]
                                      (re-frame/dispatch [::wcm/open event dialog-infos ctx-infos]))
       :context-menu-event-vec (fn [event dialog-infos ctx-infos]
                                 [::wcm/open event dialog-infos ctx-infos])
       :flags-db-get (fn [db frame-id flag-keys]
                       (flags-api/flags db frame-id flag-keys))
       :flags-sub (fn [frame-id flag-keys]
                    (re-frame/subscribe [::flags-api/flags frame-id flag-keys]))})
                       
