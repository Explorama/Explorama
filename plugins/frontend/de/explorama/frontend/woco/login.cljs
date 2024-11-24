(ns de.explorama.frontend.woco.login
  (:require [re-frame.core :as re-frame]
            [vimsical.re-frame.fx.track :as track]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.util.api :refer [db-get-error-boundary]]))

(re-frame/reg-sub
 ::current-name
 (fn [db [_]]
   (:name
    (db-get-error-boundary db
                           (registry/lookup-target db :db-get :user-info)
                           :user-info))))

(re-frame/reg-sub
 ::reg-init-event
 (fn [db _]
   (get-in db path/init-events)))

(defn update-init-event-maps
  ([db flag]
   (update-init-event-maps db flag (constantly true)))
  ([db flag update-filter]
   (update-in db
              path/init-events
              #(map (fn [event-map]
                      (if (update-filter (:event event-map))
                        (assoc event-map :executed flag)
                        event-map))
                    %))))

(re-frame/reg-event-fx
 ::init
 (fn [{db :db} _]
   (let [all-events (get-in db path/init-events)
         event-maps-filtered (filter #(not (:executed %))
                                     all-events)
         logged-in? true]
                  ;;  (db-get-error-boundary db
                  ;;                          (registry/lookup-target db :db-get :logged-in?)
                  ;;                          :logged-in?)]
     (when logged-in?
       {:fx (mapv #(vector :dispatch [(:event %)]) event-maps-filtered)
        :db (update-init-event-maps db true (set (map :event event-maps-filtered)))}))))

(re-frame/reg-event-fx
 ::dispose-track
 (fn [cofx event]
   {::track/dispose
    {:id 1}}))

(re-frame/reg-event-fx
 ::check-rights
 (fn [cofx event]
   {::track/register
    {:id 1
     :subscription [::reg-init-event]
     :event-fn (fn [event-maps]
                 [::init (filter #(not (:executed %))
                                 event-maps)])}}))

(re-frame/reg-event-fx
 ::logout
 (fn [{db :db} _]
   {:db (-> db
            (update-init-event-maps false)
            (update-in path/root dissoc path/direct-search-key)
            (assoc-in path/welcome-active? true))
    :dispatch-n [[::dispose-track]
                 [:de.explorama.frontend.woco.api.notifications/clear-notifications]
                 [:de.explorama.frontend.woco.help/close]
                 [:de.explorama.frontend.woco.frame.api/clean-workspace nil :logout]
                 [:de.explorama.frontend.common.tubes/close-tube]]}))
