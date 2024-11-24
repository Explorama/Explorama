(ns de.explorama.frontend.woco.api.key
  (:require [re-frame.core :as re-frame]
            [clojure.set :as set])
  (:import goog.events.BrowserEvent))

(re-frame/reg-event-fx
 ::down
 (fn [{db :db} [_ event]]
   (let [key (-> event BrowserEvent. .-key)]
     (when ((get-in db [:woco :keys-prevent-default] #{}) key)
       (.preventDefault event))
     (when ((get-in db [:woco :keys-stop-propagation] #{}) key)
       (.stopPropagation event))
     {:db (update-in db
                     [:woco :keys]
                     assoc
                     key
                     true)})))

(re-frame/reg-event-fx
 ::up
 (fn [{db :db} [_ event]]
   (let [key (-> event BrowserEvent. .-key)]
     (when ((get-in db [:woco :keys-prevent-default] #{}) key)
       (.preventDefault event))
     (when ((get-in db [:woco :keys-stop-propagation] #{}) key)
       (.stopPropagation event))
     {:db (update-in db
                     [:woco :keys]
                     dissoc
                     key)})))

(re-frame/reg-sub
 ::pressed
 (fn [db]
   (get-in db [:woco :keys])))

(re-frame/reg-event-db
 ::register
 (fn [db [_ {pd-keys :prevent-default
             sp-keys :stop-propagation}]]
   (-> db
       (update-in [:woco :keys-prevent-default] #(set/union (or % #{}) pd-keys))
       (update-in [:woco :keys-stop-propagation] #(set/union (or % #{}) sp-keys)))))
