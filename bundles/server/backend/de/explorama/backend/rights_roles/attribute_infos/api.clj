(ns de.explorama.backend.rights-roles.attribute-infos.api
  (:require [de.explorama.shared.abac.util :as abac-util]
            [clojure.string :as str]
            [de.explorama.backend.rights-roles.attribute-infos.adapter-keycloak :as keycloak]
            [de.explorama.backend.rights-roles.attribute-infos.adapter-ldap :as ldap]
            [de.explorama.backend.rights-roles.attribute-infos.adapter-no-credentials :as no-cred]
            [de.explorama.backend.rights-roles.attribute-infos.interface :as interface]
            [de.explorama.backend.rights-roles.attribute-infos.persistence.api :as persistence]
            [taoensso.timbre :refer [warn info debug]])
  (:import (java.util.concurrent
            Executors
            ScheduledFuture
            ThreadFactory
            TimeUnit)))

(defonce ^:private instance (atom nil))
(defonce ^:private loader-future (atom nil))

(let [scheduler (Executors/newSingleThreadScheduledExecutor
                 (reify ThreadFactory
                   (newThread [this runnable]
                     (let [t (.newThread (Executors/defaultThreadFactory)
                                         runnable)]
                       (.setName t (str/replace (.getName t)
                                                #"pool-\d+-(.*)"
                                                "ldap-loader-$1"))
                       (.setDaemon t true)
                       t))))]
  (defn-
    ^ScheduledFuture schedule-loader
    "Schedules the LDAP loader. Returns the future of the scheduled task
  which can be used to check when the task will run the next time or to cancel
  it altogether."
    [{:keys [load-rate-seconds]
      :or {load-rate-seconds (.toSeconds java.util.concurrent.TimeUnit/MINUTES 5)}}
     attrs-instance]
    (info "Reloading user-info data every" load-rate-seconds "seconds")
    (.scheduleAtFixedRate scheduler
                          (partial interface/refresh-loaded-user-infos attrs-instance)
                          0
                          load-rate-seconds
                          TimeUnit/SECONDS)))

(defn- start-loader [attrs-config]
  (let [instance @instance
        lf @loader-future]
    (if (or (nil? lf) (.isDone lf))
      (reset! loader-future (schedule-loader attrs-config instance))
      (info "user-info refresh loader already running."))))

(defn- stop-current []
  (when-let [lf @loader-future]
    (when (.cancel lf true)
      (info "user-info refresh loader stopped."))
    (reset! loader-future nil)))

(defn init-instance [{:keys [type storage-config] :as attr-config}]
  (debug "Init attribute-infos storage")
  (persistence/init-instance storage-config)
  (stop-current)
  (debug "start attribute-infos provider")
  (reset! instance
          (case type
            :ldap (ldap/new-instance attr-config)
            :keycloak (keycloak/new-instance attr-config)
            (do
              (warn "No credential mode configured." attr-config)
              (no-cred/new-instance))))
  (debug "start auto refresh")
  (start-loader attr-config))

(defn username->user-info
  "Returns a user-info map. 
   The role gets updated based on the blacklist-roles.
   If no role is given (supplied by auth-provider) then the role is removed from the user-info."
  ([username role]
   (let [blacklist-roles (persistence/blacklist-attribute-values :role)
         username (abac-util/normalize-username username)]
     (when-let [user-info (interface/username->user-info @instance username)]
       (assoc user-info :role (cond
                                (vector? role) (filterv #(not (blacklist-roles %)) role)
                                (blacklist-roles role) nil
                                :else role)))))
  ([username]
   (dissoc (username->user-info username nil)
           :role)))

(defn list-users []
  (interface/list-users @instance))

(defn refresh-loaded-user-infos []
  (interface/refresh-loaded-user-infos @instance))

(defn possible-user-attribute-vals [& attrs]
  (interface/possible-user-attribute-vals @instance attrs))

(defn blacklist-role [role-name]
  (persistence/blacklist-attribute-value :role role-name))

(defn whitelist-roles [role-names]
  (persistence/whitelist-attriubte-values :role role-names))