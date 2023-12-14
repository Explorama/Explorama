(ns de.explorama.frontend.woco.api.welcome
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.welcome :as impl]))

(re-frame/reg-event-fx
 ::dismiss-page
 (fn [{db :db} [_ callback-events]]
   (impl/dismiss-page db callback-events)))

(re-frame/reg-event-fx
 ::close-page
 (fn [{db :db} [_]]
   {:dispatch-later {:ms 500
                     :dispatch [::impl/welcome-active false]}}))

(re-frame/reg-event-fx
 ::register-interceptor
 (fn [_ [_ callback-fx]]
   {:dispatch [::impl/welcome-callback callback-fx]}))