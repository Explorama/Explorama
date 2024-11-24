(ns de.explorama.frontend.woco.api.overlay
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]))

(re-frame/reg-event-db
 ::register
 (fn [db [_ id module]]
   (assoc-in db (path/overlay id) module)))

(re-frame/reg-event-db
 ::deregister
 (fn [db [_ id]]
   (path/dissoc-in db (path/overlay id))))

(re-frame/reg-sub
 ::list
 (fn [db _]
   (get-in db path/overlays {})))

(re-frame/reg-sub
 ::overlayer-active?
 (fn [db _]
   (get-in db path/overlayer-active?)))

(re-frame/reg-event-db
 ::overlayer-active
 (fn [db [_ active?]]
   (assoc-in db path/overlayer-active? active?)))