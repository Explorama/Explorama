(ns de.explorama.frontend.rights-roles.login
  (:require [ajax.core :as ajax]
            [cljs.reader :as edn]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.rights-roles.path :as path]
            [de.explorama.shared.common.configs.platform-specific :as config-shared-platform]
            [de.explorama.shared.rights-roles.ws-api :as ws-api]
            [re-frame.core :as re-frame]))

(defn logged-in? [db]
  (get-in db path/logged-in?))

(re-frame/reg-sub
 ::logged-in?
 (fn [db _]
   (logged-in? db)))

(re-frame/reg-sub
 ::user-info
 (fn [db _]
   (get-in db path/user-info)))

(re-frame/reg-sub
 ::current-name
 :<- [::user-info]
 :name)

(defn set-user-db [db user-info]
  (-> db
      (assoc-in path/login-root {:logged-in? true
                                 :user-info user-info
                                 :login-message nil})
      (update path/root dissoc path/login-form-key)))

(re-frame/reg-event-fx
 ::logout-success
 (fn [_ [_ resp]]
   (if (and (seq resp) (string? resp))
     (aset js/window "location" resp)
     (js/window.location.reload))
   {}))

(re-frame/reg-event-fx
 ::logout-dont-care
 (fn [_ _]
   (js/window.location.reload)
   {}))

(re-frame/reg-event-fx
 ::logout-post
 (fn [{db :db} _]
   (fi/call-api [:client-preferences :preference-set-raw]
                "" "token" "")
   {:db (assoc-in db path/login-root {:logged-in? false})
    :http-xhrio {:method          :post
                 :uri             "/logout"
                 :timeout         8000
                 :format          (ajax/json-request-format)
                 :response-format (ajax/raw-response-format)
                 :on-success      [::logout-success]
                 :on-failure      [::logout-dont-care]}}))

(re-frame/reg-event-fx
 ::logout
 (fn [{db :db} [_ confirmed-logout?]]
   (let [logout-events (fi/call-api :service-category-db-get db :logout-events)]
     (if confirmed-logout?
       {:fx (conj (mapv (fn [[_ ev]]
                          [:dispatch ev])
                        logout-events)
                  [:dispatch [::logout-post]])}
       {:db (assoc-in db path/try-logout? true)}))))

(re-frame/reg-event-fx
 ::retrive-token-payload
 (fn [_ [_ token]]
   {:rights-roles-tubes [ws-api/token-payload
                         {:client-callback [ws-api/token-valid true]
                          :failed-callback [ws-api/token-invalid]}
                         token]}))

(defn- expires-in->ms [token-expires]
  (let [expires-offset 3] ; take 3 seconds of so we have enough time to request and answere with a new token
    (-> token-expires
        (- expires-offset)
        (* 1000))))

(re-frame/reg-event-fx
 ::refresh-token
 (fn [_ [_ login?]]
   (when config-shared-platform/explorama-multi-user
     {:http-xhrio {:method          :get
                   :uri             (str config-shared-platform/explorama-origin "/validate")
                   :timeout         8000
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [::token-valid login?]
                   :on-failure      [::token-invalid]}})))

(re-frame/reg-event-fx
 ::token-valid
 (fn [{db :db} [_
                execute-login-success?
                {{:keys [token] :as user-info} :user-info
                 expires-in :expires-in}]]
   (let [success-login-events (fi/call-api :service-category-db-get db :login-success-events)]
     {:db (set-user-db db user-info)
      :fx (cond-> (mapv (fn [restart-ws-vec]
                          [:dispatch restart-ws-vec])
                        (fi/call-api :service-category-db-get db :update-user-info-event-vec))
            :always (conj
                     [:dispatch (fi/call-api [:client-preferences :set-event-vec]
                                             "token" token)]
                     [:dispatch-later [{:ms (expires-in->ms expires-in)
                                        :dispatch [::refresh-token false]}]])
            execute-login-success? (into
                                    (map (fn [[_ ev]]
                                           [:dispatch ev])
                                         success-login-events)))})))

(re-frame/reg-event-fx
 ::token-invalid
 (fn [{db :db} _]
   {:db (assoc-in db path/login-root {:logged-in? false
                                      :login-message nil})
    :fx [[:dispatch (fi/call-api [:client-preferences :set-event-vec]
                                 "token" "")]
         [:dispatch [::logout]]]}))

(re-frame/reg-sub
 ::keep-logged-in?
 (fn [db _]
   (get-in db
           (path/login-value :keep-logged-in?)
           (edn/read-string
            (fi/call-api [:client-preferences :preference-get-raw]
                         "" "keepLoggedIn" "false")))))

(let [storage-val (fi/call-api [:client-preferences :preference-get-raw]
                               "" "openTabs" "0")
      current-num (js/parseInt storage-val)]
  (fi/call-api [:client-preferences :preference-set-raw]
               "" "openTabs" (inc current-num)))

(defn init []
  (when config-shared-platform/explorama-multi-user
    (re-frame/dispatch [::refresh-token true]))

  (when-not config-shared-platform/explorama-multi-user
    (.addEventListener
     js/window
     "beforeunload"
     (fn []
       (let [storage-val (fi/call-api [:client-preferences :preference-get-raw]
                                      "" "openTabs" "0")
             keep-logged-in? (fi/call-api [:client-preferences :preference-get-raw]
                                          "" "keepLoggedIn" "false")
             current-num (js/parseInt storage-val)
             updated-num (dec current-num)]
         (cond
           (and (<= updated-num 0)
                (= keep-logged-in? "false")) (do (fi/call-api [:client-preferences :preference-set-raw]
                                                              "" "token" "")
                                                 (fi/call-api [:client-preferences :preference-set-raw]
                                                              "" "openTabs" 0))
           (<= updated-num 0) (fi/call-api [:client-preferences :preference-set-raw]
                                           "" "openTabs" 0)
           :else (fi/call-api [:client-preferences :preference-set-raw]
                              "" "openTabs" updated-num))))))

  (when-not config-shared-platform/explorama-multi-user
    (fi/call-api [:client-preferences :add-watcher-raw]
                 "token"
                 (fn [_ old-val new-val]
                 ;Other case is when the token was updated to a new one => can be ignored
                   (cond (and (seq old-val)
                              (not (seq new-val))) ; Logout in another tab
                         (re-frame/dispatch [::logout])
                         (and (not (seq old-val))
                              (seq new-val)) ; Login in another tab
                         (re-frame/dispatch [::retrive-token-payload new-val]))))))
