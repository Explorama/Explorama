(ns de.explorama.frontend.woco.cleanup
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::clean-finished
 (fn [{db :db} [_ vertical-service-id]]
   (let [new-db (update db ::clean-workspace dissoc vertical-service-id)]
     {:db new-db
      :dispatch-n [(when (empty? (::clean-workspace new-db))
                     (::clean-finished db))]})))
