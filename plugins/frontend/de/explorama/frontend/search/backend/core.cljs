(ns de.explorama.frontend.search.backend.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :refer [reg-event-fx]]
            [de.explorama.frontend.search.backend.acs :as acs-backend]
            [de.explorama.frontend.search.backend.data-descs]
            [de.explorama.frontend.search.backend.di]
            [de.explorama.frontend.search.backend.direct-search]
            [de.explorama.frontend.search.backend.options]
            [de.explorama.frontend.search.backend.search-query]
            [de.explorama.frontend.search.backend.traffic-lights]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.shared.search.ws-api :as ws-api]
            [taoensso.timbre :refer-macros [debug error]]))

(reg-event-fx
 ws-api/failed-handler
 (fn [_ [_ identifier infos]]
   (error "Request failed" identifier infos)))

(reg-event-fx
 ws-api/init-client
 (fn [{db :db}]
   (debug "Init client")
   {:backend-tube [ws-api/init-client
                   {:client-callback [ws-api/init-client-result]
                    :failed-callback [ws-api/failed-handler :init-client]}
                   (fi/call-api :user-info-db-get db)]}))

(reg-event-fx
 ws-api/init-client-result
 (fn [_ [_ {:keys [enabled-datasources search-parameter-config attr-types bucket-datasources] :as r}]]
   (debug "Init client result" r)
   {:fx [(when search-parameter-config
           [:dispatch [::config/set-search-parameter-config search-parameter-config]])
         (when enabled-datasources
           [:dispatch [::acs-backend/set-enabled-datasources enabled-datasources]])
         (when attr-types
           [:dispatch [::acs-backend/set-attr-types attr-types]])
         (when bucket-datasources
           [:dispatch [::acs-backend/set-bucket-datasources bucket-datasources]])]}))

;; ;;Called from server side
;; (reg-event-fx
;;  ws-api/broadcast-bucket-datasources-result
;;  (fn [_ [_ bucket-datasource]]
;;    (debug "Broadcast bucket datasources result" bucket-datasource)
;;    {}))

(reg-event-fx
 ws-api/request-attributes
 (fn [_ [_ datasources frame-id row-attrs formdata callback-event]]
   {:backend-tube [ws-api/request-attributes
                   {:client-callback [ws-api/request-attributes-result]
                    :failed-callback [ws-api/failed-handler :request-attributes]}
                   datasources frame-id row-attrs formdata callback-event]}))

(reg-event-fx
 ws-api/request-attributes-result
 (fn [_ [_ frame-id attributes callback-event]]
   {:fx [[:dispatch [:de.explorama.frontend.search.views.attribute-bar/update-attributes frame-id attributes]]
         (when callback-event
           [:dispatch callback-event])]}))

(reg-event-fx
 ws-api/search-bar-find-elements
 (fn [{db :db} [_ datasources frame-id search-term formdata config search-task-id]]
   {:backend-tube [ws-api/search-bar-find-elements
                   {:client-callback [ws-api/search-bar-find-elements-result]
                    :failed-callback [ws-api/failed-handler :search-bar-find-elements]}
                   datasources
                   frame-id
                   (fi/call-api [:i18n :get-labels-db-get] db)
                   (i18n/current-language db)
                   search-term
                   formdata
                   config
                   search-task-id]}))

(reg-event-fx
 ws-api/search-bar-find-elements-result
 (fn [_ [_ frame-id action result task-id]]
   (if (= action :done)
     {:dispatch [:de.explorama.frontend.search.views.search-bar/search-done frame-id task-id]}
     {:dispatch [:de.explorama.frontend.search.views.search-bar/set-search-result
                 frame-id
                 action
                 result
                 task-id]})))