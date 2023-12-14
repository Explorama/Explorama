(ns de.explorama.frontend.configuration.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.configuration.config :as config]
            [de.explorama.frontend.configuration.configs.access :as config-access]
            [de.explorama.frontend.configuration.configs.config-types.theme :as theme]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.configuration.i18n.labels :as labels]
            [de.explorama.frontend.configuration.i18n.translations :as translations]
            [de.explorama.frontend.configuration.project.post-processing :as project-post-processing]
            [de.explorama.frontend.configuration.project.post-processing-dialog :as project-post-processing-dialog]
            [de.explorama.frontend.configuration.views.data-management.overview :as dview]
            [de.explorama.frontend.configuration.views.layout-management.overview :as lview]
            [de.explorama.frontend.configuration.views.layout-management.picker :as layout-picker]
            [de.explorama.frontend.configuration.views.user-settings :as user-settings-view]
            [de.explorama.frontend.configuration.views.user-settings-comp :as user-settings-comp]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id "data-manager"
                                       :icon "icon-data-manager"
                                       :action-key :data-manager
                                       :component :configuration-ui
                                       :action [::dview/open-sidebar]
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :data-manager-label]
                                       :vertical config/default-vertical-str
                                       :tool-group :header
                                       :header-group :middle
                                       :sort-order 5})]
           [:dispatch (service-register :modules "config-dm-settings" dview/view)]
           [:dispatch (info "Data Management arriving!")]]})))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get]
          info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          overlay-register :overlay-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     (theme/add-system-theme-listener)
     {:fx [[:backend-tube [ws-api/get-acs {:client-callback [ws-api/set-acs]}]]
           [:backend-tube [ws-api/get-search-config {:client-callback [ws-api/set-search-config]}]]
           [:dispatch [::labels/request-labels user-info]]
           [:dispatch [::persistence/init-configs]]
           [:dispatch project-post-processing/register-event]
           [:dispatch project-post-processing/check-for-update-register-event]
           [:dispatch (overlay-register :config-project-post-processing-dialog project-post-processing-dialog/dialog)]
           [:dispatch (service-register :config-module
                                        :layout-picker
                                        {:component layout-picker/picker})]
           [:dispatch (service-register :config-module
                                        :legend-edit
                                        {:component layout-picker/editing-view})]
           [:dispatch (service-register :db-get
                                        :get-config
                                        config-access/get-config)]
           [:dispatch (service-register :sub-vector
                                        :get-config
                                        [::config-access/get-config])]
           [:dispatch (service-register :db-get
                                        :available-languages
                                        translations/available-languages)]
           [:dispatch (service-register :db-get
                                        :translate
                                        translations/translate)]
           [:dispatch (service-register :db-get
                                        :translate-multi
                                        translations/translate-multi)]
           [:dispatch (service-register :db-get
                                        :get-labels
                                        labels/get-labels)]
           [:dispatch (service-register :sub-vector
                                        :get-labels
                                        [::labels/get-labels])]
           [:dispatch (service-register :db-get
                                        :get-theme
                                        theme/current-theme)]
           [:dispatch (service-register :sub-vector
                                        :get-theme
                                        [::theme/current-theme])]
           [:dispatch (tools-register {:id "user-settings"
                                       :icon :cog1
                                       :action-key :user-settings
                                       :component :configuration
                                       :action [::user-settings-view/open-sidebar]
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :user-settings-label]
                                       :vertical config/default-vertical-str
                                       :sort-order 1
                                       :tool-group :header
                                       :header-group :right})]
           [:dispatch (tools-register {:id "layout-manager"
                                       :icon "icon-palette"
                                       :action-key :layout-settings
                                       :component :configuration
                                       :action [::lview/open-sidebar]
                                       :tooltip-text [:de.explorama.frontend.common.i18n/translate :layout-manager-label]
                                       :vertical config/default-vertical-str
                                       :tool-group :header
                                       :header-group :middle
                                       :sort-order 4})]
           [:dispatch (service-register :clean-workspace
                                        ::clean-workspace
                                        [::clean-workspace])]
           [:dispatch (service-register :modules "config-layout-settings" lview/view)]
           [:dispatch (service-register :modules "config-settings" user-settings-comp/view)]
           [:dispatch (info (str config/default-vertical-str " arriving!"))]
           [:dispatch (init-done config/default-vertical-str)]
           [:dispatch [::arrive]]]})))

(re-frame/reg-event-fx
 ws-api/handle-errors
 (fn [_ [_ error-msg]]
   (error error-msg)
   {}))

(re-frame/reg-event-fx
 ::update-language
 (fn [{db :db} [_ language]]
   (let [{:keys [user-info-db-get]} (fi/api-definitions)
         user-info (when (fn? user-info-db-get)
                     (user-info-db-get db))]
     {:dispatch-n [[::labels/request-labels user-info]]})))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event]]
   (let [frames (fi/call-api :list-frames-vertical-db-get db config/default-vertical-str)]
     {:dispatch-n (concat [(conj follow-event ::clean-workspace)]
                          (mapv #(fi/call-api :frame-delete-quietly-event-vec %)
                                frames))})))

(defn init []
  (register-init 0))
