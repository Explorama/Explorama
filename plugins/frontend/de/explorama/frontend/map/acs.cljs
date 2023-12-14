(ns de.explorama.frontend.map.acs
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.tracks]
            [de.explorama.shared.map.ws-api :refer [set-acs]]
            [re-frame.core :as re-frame]))

(defn- sort-acs [acs]
  (sort-by (fn [{:keys [name]}]
             (string/lower-case name))
           acs))

(re-frame/reg-sub
 ::geo-acs
 (fn [db _]
   (->> (get-in db geop/geolocated-acs)
        sort-acs
        vec)))

(re-frame/reg-event-db
 set-acs
 (fn [db [_ geo-acs]]
   (assoc-in db geop/geolocated-acs geo-acs)))
