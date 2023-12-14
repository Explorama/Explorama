(ns de.explorama.backend.indicator.persistence.store.expdb
  (:require [de.explorama.backend.common.storage.agent.core :as pcore]
            [de.explorama.backend.expdb.middleware.db :as db]
            [de.explorama.backend.indicator.persistence.store.adapter :as adapter]
            [taoensso.timbre :refer [debug error]]))

(defonce ^:private bucket "/indicator/indicators/")
(defonce ^:private bucket-index "/indicator/indicators-index/")
(defonce ^:private index-path "index")

(def index-keys [:id :creator :name :description :shared-by])

(defn- indicator-path [creator id]
  (str creator "/" id))

(defn write-indicator [index {:keys [creator id] :as indicator}]
  (debug "Write indicator description" {:id id :creator creator})
  (let [path (indicator-path creator id)
        index-info (select-keys indicator index-keys)]
    (db/set bucket path indicator)
    (get-in
     (swap! index update creator assoc id index-info)
     [creator id])))

(defn read-indicator [{creator :username} id]
  (debug "Read indicator description" {:id id :creator creator})
  (let [path (indicator-path creator id)
        indicator (db/get bucket path)]
    (if indicator
      indicator
      (do
        (error "Indicator file doesn't exist anymore." {:id id :creator creator})
        nil))))

(defn list-indicators [index]
  (vec
   (mapcat (fn [[_ indicators]]
             (vals indicators))
           @index)))

(defn list-all-user-indicators [index {:keys [username]}]
  (->> (get @index username) vals vec))

(defn short-indicator-desc [index {:keys [username]} id]
  (get-in @index [username id]))

(defn user-for-indicator-id [index id]
  (some
   (fn [{indicator-id :id
         creator-name :creator}]
     (when (= indicator-id id)
       {:username creator-name}))
   (list-indicators index)))

(defn delete-indicator [index user id]
  (let [{creator :username} user]
    (try
      (db/del bucket (indicator-path creator id))
      (swap! index
             update creator
             dissoc id)
      [creator id]
      (catch #?(:clj Throwable :cljs :default) e
        (error "Something went wrong while deleting the file for the de.explorama.backend.indicator.
                Deletion aborted for this indicator version."
               {:id id
                :creator creator}
               e)
        nil))))

(deftype ExpDBBackend [index]
  adapter/Backend
  (write-indicator [instance indicator]
    (write-indicator index indicator))
  (read-indicator [instance user id]
    (read-indicator user id))
  (list-indicators [instance]
    (list-indicators index))
  (short-indicator-desc [instance user id]
    (short-indicator-desc index user id))
  (list-all-user-indicators [instance user]
    (list-all-user-indicators index user))
  (user-for-indicator-id [instance id]
    (user-for-indicator-id index id))
  (delete-indicator [instance user id]
    (delete-indicator index user id)))

(defn new-instance []
  (let [index (pcore/create {:impl :expdb
                             :bucket bucket-index
                             :path index-path
                             :init {}})]
    (pcore/start! index-path)
    (ExpDBBackend. index)))
