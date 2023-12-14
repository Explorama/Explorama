(ns de.explorama.frontend.mosaic.data.di-acs
  "Access and utils for data-acs, which are a mimized description of possible characters in loaded data.
   Therefore it's not the same as acs, which based on ac-graph"
  (:require [de.explorama.frontend.common.calculations.data-acs-client :as data-acs-calc]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]))

(re-frame/reg-event-db
 ::constraints-stopped
 (fn [db [_ path value]]
   (assoc-in db
             (conj (gp/filter-desc path)
                   :deactivated?)
             value)))

(re-frame/reg-sub
 ::data-acs-available?
 (fn [db [_ path]]
   (boolean (get-in db
                    (conj (gp/filter-desc path)
                          :data-acs)))))

(re-frame/reg-sub
 ::data-acs
 (fn [db [_ path]]
   (get-in db
           (conj (gp/filter-desc path)
                 :data-acs))))

(re-frame/reg-sub
 ::attributes
 (fn [db [_ path blacklist]]
   (let [blacklist (set (map name blacklist))]
     (->> (-> (get-in db (conj (gp/filter-desc path)
                               :data-acs))
              (assoc "year" nil))
          keys
          (filter #(not (blacklist %)))
          set))))

(defn- resolve-date-vals [[from-moment-obj to-moment-obj]]
  (try
    [(.format from-moment-obj "YYYY-MM-DD")
     (.format to-moment-obj "YYYY-MM-DD")]
    (catch :default e
      (error e "Failed to resolve date-vals" from-moment-obj to-moment-obj)
      nil)))

(re-frame/reg-sub
 ::attribute-vals
 (fn [db [_ path attr]]
   (let [access-path (if (= attr "year")
                       [:data-acs "date" :year :vals]
                       [:data-acs attr :std :vals])
         attr-vals (get-in db
                           (apply conj
                                  (gp/filter-desc path)
                                  access-path))]
     (cond-> attr-vals
       (= attr "date")
       (resolve-date-vals)))))

(re-frame/reg-event-fx
 ws-api/data-acs-async
 (fn [{db :db} [_ frame-id acs]]
   (let [filter-path (gp/filter-desc frame-id)]
     {:db (assoc-in db (conj filter-path :data-acs) (data-acs-calc/post-process acs))})))
