(ns de.explorama.frontend.rights-roles.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.rights-roles.login :as login]
            [de.explorama.frontend.rights-roles.path :as path]
            [de.explorama.frontend.rights-roles.warning-dialog :as warning-dialog]
            [de.explorama.shared.common.configs.platform-specific :as config-shared-platform]
            [de.explorama.shared.rights-roles.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log :refer [error]]))

(def plugin-name :rights-roles)

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{info :info-event-vec
          init-done :init-done-event-vec
          overlay-register :overlay-register-event-vec}
         (fi/api-definitions)]
     {:fx [[:dispatch (overlay-register :warning-screen warning-dialog/warning-screen)]
           [:dispatch (init-done "rights-roles")]
           [:dispatch (info "rights-roles arriving!")]]})))

(re-frame/reg-event-db
 ::register-services
 (fn [db _]
   (let [register-fn (fi/api-definition :service-register-db-update)]
     (-> db
         (register-fn :login plugin-name {:logged-in?-sub-vec [::login/logged-in?]
                                          :login-success ws-api/logged-in
                                          :logout-event [::login/logout]})
         (register-fn :blacklist-role plugin-name [::blacklist-role])
         (register-fn :sub-vector :user-info [::login/user-info])
         (register-fn :db-get :logged-in? login/logged-in?)
         (register-fn :db-get :user-info (fn [db]
                                           (get-in db path/user-info)))))))

(re-frame/reg-event-fx
 ::blacklist-role
 (fn [{db :db} [_ rolename]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:rights-roles-tubes [ws-api/blacklist-role
                           {:client-callback (fi/call-api :load-users-roles-event-vec)
                            :failed-callback [ws-api/blacklist-failed rolename]}
                           user-info rolename]})))

(re-frame/reg-event-fx
 ws-api/blacklist-failed
 (fn [_ [_ rolename fail-reason]]
   (error "Failed to blacklist role" {:reason fail-reason
                                      :role rolename})
   {}))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (do (fi/call-api :init-register-event-dispatch ::init-event "rights-roles")
                             (re-frame/dispatch [::register-services]))
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))


(defn init []
  (login/init)
  (register-init 0)
  (when-not config-shared-platform/explorama-multi-user
    (let [dummy-user-info {:user-info {:username "localuser"
                                       :name "Local User"
                                       :role "admin"}}]
      (re-frame/dispatch [::login/token-valid
                          true
                          dummy-user-info]))))
