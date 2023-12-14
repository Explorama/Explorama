(ns de.explorama.backend.reporting.persistence.api
  (:require [clojure.set :as set]
            [de.explorama.backend.reporting.persistence.expdb.store :as expdb-store]
            [de.explorama.backend.reporting.persistence.policy-management :as pm]
            [de.explorama.backend.reporting.persistence.store :as store-api]
            [de.explorama.shared.reporting.description-types :refer [valid-desc?]]
            [de.explorama.shared.reporting.ws-api :as ws-api]
            [taoensso.timbre :refer [error]]))

(defonce ^:private dashboard-instance (atom nil))
(defonce ^:private reports-instance (atom nil))

(defn new-instances []
  (let [[dashboards reports] (expdb-store/init-stores)]
    (reset! dashboard-instance dashboards)
    (reset! reports-instance reports)))

(defn- filter-broadcast [client-id desc tube]
  (let [user-info (select-keys tube [:username :role])
        tube-client-id (:client-id tube)]
    (and (not= client-id tube-client-id)
         (pm/read-access? desc
                          user-info))))

(defn- save-desc [store client-callback broadcast-callback client-id type user-info desc]
  (let [{:keys [id name] :as desc} desc
        valid? (valid-desc? type desc)
        policies-exist? (pm/policies-exist? id)
        access-rights? (if policies-exist?
                         (pm/write-access? desc user-info)
                         (boolean user-info))]
    (cond
      (not valid?)
      (client-callback :error :invalid-desc)
      (not access-rights?)
      (client-callback :error :no-rights)
      (and valid? access-rights?)
      (if-let [_write-success? (try (store-api/write-file store
                                                          user-info
                                                          (:id desc)
                                                          desc)
                                    (catch #?(:clj Throwable :cljs :default) e
                                      (error "Writing file failed: " e)
                                      false))]
        (do (when-not policies-exist?
              (pm/create-init-dr-policies (get user-info :username) id name {} false))
            (client-callback :success)
            (broadcast-callback (partial filter-broadcast client-id desc)))
        (client-callback :error :write-failed)))))

(defn- delete-desc [store client-callback broadcast-callback client-id user-info desc-id]
  (let [desc (store-api/read-file store user-info desc-id)
        access-rights? (pm/write-access? desc user-info)]
    (cond
      (not desc-id)
      (client-callback :error :invalid-id)
      (not access-rights?)
      (client-callback :error :no-rights)
      (and desc-id access-rights?)
      (if-let [_delete-success? (try (store-api/delete-file store user-info desc-id)
                                     (catch #?(:clj Throwable :cljs :default) e
                                       (error "Deleting file failed: " e)
                                       false))]
        (do (broadcast-callback (partial filter-broadcast client-id desc))
            (pm/delete-dr-policies desc-id)
            (client-callback :success))
        (client-callback :error :delete-failed)))))

(defn- expand-descs [store descs {:keys [username] :as user-info}]
  (->> descs
       (filter #(pm/read-access? % user-info))
       (mapv #(store-api/read-file store (get % :creator) (get % :id)))
       (reduce (fn [desc-map {:keys [creator id] :as desc}]
                 (assoc-in desc-map [(if (= creator username) :created :shared) id]
                           (assoc desc :share (pm/policies-user-attributes id))))
               {})))

;;--------- Dashboards ---------

(defn all-dashboards
  ^{:doc ws-api/all-dashboards-doc}
  [{:keys [client-callback failed-callback user-validation]} [user-info]]
  (if (user-validation user-info)
    (client-callback (expand-descs @dashboard-instance
                                   (store-api/read-files @dashboard-instance false)
                                   user-info))
    (failed-callback)))

(defn save-dashboard
  ^{:doc ws-api/save-dashboard-doc}
  [{:keys [client-callback broadcast-callback client-id failed-callback user-validation]} [user-info dashboard-desc]]
  (if (user-validation user-info)
    (save-desc @dashboard-instance
               client-callback
               broadcast-callback
               client-id
               :dashboard
               user-info
               dashboard-desc)
    (failed-callback)))

(defn delete-dashboard
  ^{:doc ws-api/delete-dashboard-doc}
  [{:keys [client-callback broadcast-callback client-id failed-callback user-validation]} [user-info dashboard-id]]
  (if (user-validation user-info)
    (delete-desc @dashboard-instance
                 client-callback
                 broadcast-callback
                 client-id
                 user-info
                 dashboard-id)
    (failed-callback)))

;;--------- Reports ---------

(defn all-reports
  ^{:doc ws-api/all-reports-doc}
  [{:keys [client-callback failed-callback user-validation]} [user-info]]
  (if (user-validation user-info)
    (client-callback (expand-descs @reports-instance
                                   (store-api/read-files @reports-instance false)
                                   user-info))
    (failed-callback)))

(defn save-report
  ^{:doc ws-api/save-report-doc}
  [{:keys [client-callback broadcast-callback client-id failed-callback user-validation]} [user-info report-desc]]
  (if (user-validation user-info)
    (save-desc @reports-instance
               client-callback
               broadcast-callback
               client-id
               :report
               user-info
               report-desc)
    (failed-callback)))

(defn delete-report
  ^{:doc ws-api/delete-report-doc}
  [{:keys [client-callback broadcast-callback client-id failed-callback user-validation]} [user-info report-id]]
  (if (user-validation user-info)
    (delete-desc @reports-instance
                 client-callback
                 broadcast-callback
                 client-id
                 user-info
                 report-id)
    (failed-callback)))

;;--------- Rights ---------

(defn update-dr-rights
  ^{:doc ws-api/update-dr-rights-doc}
  [{:keys [client-callback client-id find-tubes broadcast-callback]} update-args]
  (let [[_ creator _ dr-id] update-args
        desc {:id dr-id
              :creator creator}
        current-tubes (set (find-tubes (partial filter-broadcast client-id desc)))
        success? (boolean (apply pm/update-dr-policies update-args))
        new-tubes (set (find-tubes (partial filter-broadcast client-id desc)))
        inform-tubes (set/union current-tubes new-tubes)]
    (when success?
      (broadcast-callback (fn [tube]
                            (boolean (inform-tubes tube)))))
    (client-callback success?)))
