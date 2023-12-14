(ns de.explorama.frontend.table.acs
  (:require [clojure.set :as set]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.table.path :as path]))

(defn retrieve-volatile-acs [db]
  (->>  (get-in db path/volatile-acs)
        vals))

(defn obj-or-color-acs [db key]
  (vec (reduce (fn [acc {ac key}]
                 (set/union acc (set ac)))
               #{}
               (retrieve-volatile-acs db))))

(re-frame/reg-sub
 ::volatile-acs
 (fn [db _]
   (obj-or-color-acs db :color-ac)))

(re-frame/reg-event-db
 ::volatile-acs
 (fn [db [_ volatile-acs-paths]]
   (assoc-in db path/volatile-acs volatile-acs-paths)))
