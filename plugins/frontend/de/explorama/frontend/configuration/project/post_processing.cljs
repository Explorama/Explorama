(ns de.explorama.frontend.configuration.project.post-processing
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.configuration.config :as config]
            [de.explorama.frontend.configuration.configs.config-types.layout :refer [default-layouts user-layouts]]
            [de.explorama.frontend.configuration.configs.config-types.overlayer :refer [default-overlayers user-overlayers]]
            [de.explorama.frontend.configuration.project.post-processing-dialog :as post-processing-dialog]
            [re-frame.core :refer [reg-event-fx reg-sub]]
            [de.explorama.frontend.configuration.path :as path]
            [reagent.core :as r]
            [taoensso.timbre :refer-macros [warn error debug]]))

(reg-event-fx
 ::handle-event-done
 (fn [{db :db}]
   (let [num-of-events (get-in db path/project-post-handles-count)
         done (inc (get-in db path/project-post-handles-done))]
     (debug "handle-event-done" done "/" num-of-events)
     (if (<= num-of-events done)
       {:db (update-in db path/root dissoc path/project-post-checks-root-key)
        :dispatch-n [(get-in db path/project-post-handles-callback)]}
       {:db (update-in db path/project-post-handles-done inc)}))))

(reg-event-fx
 ::execute-update
 (fn [{db :db} [_ callback]]
   (let [checks (get-in db path/project-post-checks-root)
         handle-events (reduce (fn [acc [config-type {:keys [outdated]}]]
                                 (reduce (fn [acc [config-id update-infos]]
                                           (reduce (fn [acc {:keys [handle-updates-event] :as desc}]
                                                     (cond-> acc
                                                       (and config-id (vector? handle-updates-event))
                                                       (update handle-updates-event
                                                               assoc
                                                               config-id
                                                               desc)))
                                                   acc
                                                   update-infos))
                                         acc
                                         outdated))
                               {}
                               checks)
         num-of-events (count handle-events)]
     (if (< 0 num-of-events)
       {:db (-> db
                (assoc-in path/project-post-handles-count num-of-events)
                (assoc-in path/project-post-handles-done 0)
                (assoc-in path/project-post-handles-callback callback))
        :dispatch-n (mapv (fn [[event updates]]
                            (conj event {:updates updates
                                         :callback [::handle-event-done]}))
                          handle-events)}
       {:dispatch-n [callback]}))))

(reg-event-fx
 ::post-process
 (fn [{db :db} [_ {:keys [type callback]}]]
   (debug "execute post-process")
   {:dispatch-n [(if (seq (get-in db path/project-post-checks-root))
                   [::post-processing-dialog/show-dialog {:yes-callback [::execute-update callback]
                                                          :no-callback callback}]
                   callback)]}))

(defn- configs-check [db {:keys [check-layouts check-overlayers handle-updates-event]} config-type]
  (let [[default-configs configs check-configs]
        (case config-type
          :layouts [(default-layouts db)
                    (user-layouts db)
                    check-layouts]
          :overlayers [(default-overlayers db)
                       (user-overlayers db)
                       check-overlayers])
        outdated-layouts (reduce (fn [acc [layout-id {:keys [timestamp]}]]
                                   (let [outdated-desc (get check-configs layout-id)
                                         check-timestamp (get-in outdated-desc [:layout-desc :timestamp])]
                                     (if (and check-timestamp timestamp
                                              (< check-timestamp timestamp))
                                       (assoc acc layout-id (assoc outdated-desc
                                                                   :handle-updates-event handle-updates-event))
                                       acc)))
                                 {}
                                 (merge default-configs configs))]
    (cond-> db
      (seq outdated-layouts)
      (update-in (path/project-outdated-configs config-type)
                 (fn [old]
                   (reduce (fn [acc [config-id desc]]
                             (update acc config-id conj desc))
                           (or old {})
                           outdated-layouts))))))

(defn- check-for-updates [db config-type check-configs]
  (case config-type
    :layouts (configs-check db check-configs config-type)
    :overlayers (configs-check db check-configs config-type)
    (do
      (warn "No check fn for config-type" config-type check-configs)
      db)))


(reg-sub
 ::outdated-configs
 (fn [db]
   (get-in db path/project-post-checks-root)))

(def register-event
  (fi/call-api :service-register-event-vec
               :project-post-processing-events
               config/default-vertical-str
               {:event-vec [::post-process]
                :type :configs
                :order :post}))

(def deregister-event
  (fi/call-api :service-deregister-event-vec
               :project-post-processing-events
               config/default-vertical-str))

(def check-for-update-register-event
  (fi/call-api :service-register-event-vec
               :db-update
               :check-for-updates
               check-for-updates))