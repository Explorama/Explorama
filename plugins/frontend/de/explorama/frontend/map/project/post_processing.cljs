(ns de.explorama.frontend.map.project.post-processing
  (:require [clojure.set :refer [union]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.operations.tasks :as tasks]
            [de.explorama.frontend.map.paths :as geop]
            [re-frame.core :refer [reg-event-fx]]
            [taoensso.timbre :refer-macros [debug]]))

(def register-event
  (fi/call-api :service-register-event-vec
               :project-post-processing-events
               config/default-vertical-str
               {:event-vec [::post-process]
                :type :configs
                :order :pre}))

(def deregister-event
  (fi/call-api :service-deregister-event-vec
               :project-post-processing-events
               config/default-vertical-str))

(defn- gather-configs [db type]
  (let [frame-ids (fi/call-api :list-frames-vertical-db-get db "map")]
    (reduce (fn [acc frame-id]
              (let [[frame-configs temp-configs]
                    (case type
                      :layouts [(get-in db (geop/usable-marker-layouts-id frame-id))
                                (get-in db (geop/temp-raw-marker-layouts frame-id))]
                      :overlayers [(get-in db (geop/selected-overlayers frame-id))
                                   (get-in db (geop/temp-raw-feature-layers frame-id))]
                      [nil nil])]
                (reduce (fn [acc layout-id]
                          (let [{:keys [temporary?] :as desc} (get temp-configs layout-id)]
                            (cond-> acc
                              (and (map? desc)
                                   (not temporary?))
                              (update layout-id (fn [o]
                                                  (-> o
                                                      (assoc :layout-desc desc)
                                                      (update :frames #(-> (or % #{})
                                                                           (conj frame-id)))))))))
                        acc
                        frame-configs)))
            {}
            frame-ids)))

(reg-event-fx
 ::post-process
 (fn [{db :db} [_ {:keys [callback]}]]
   (debug "execute post-process")
   (let [check-layouts (gather-configs db :layouts)
         check-overlayers (gather-configs db :overlayers)]
     {:db (cond-> db
            (seq check-layouts)
            (#(fi/call-api [:config :project-post-processing :check-for-updates-db-update]
                           %
                           :layouts
                           {:check-layouts check-layouts
                            :handle-updates-event [::handle-updates :layouts]}))
            (seq check-overlayers)
            (#(fi/call-api [:config :project-post-processing :check-for-updates-db-update]
                           %
                           :overlayers
                           {:check-overlayers check-overlayers
                            :handle-updates-event [::handle-updates :overlayers]})))
      :dispatch-n [callback]})))

(reg-event-fx
 ::handle-updates
 (fn [{db :db} [_ type {:keys [updates callback]}]]
   (debug "handle-updates")
   (let [frame-ids (->> updates
                        vals
                        (map :frames)
                        (apply union))
         updated-configs (select-keys (fi/call-api [:config :get-config-db-get]
                                                   db
                                                   type)
                                      (vec (keys updates)))
         update-events (mapv (fn [frame-id]
                               (case type
                                 :layouts
                                 [:dispatch-n [[::tasks/execute-wrapper
                                                frame-id
                                                :marker
                                                {:marker-layouts (mapv #(get updated-configs %)
                                                                       (get-in db (geop/usable-marker-layouts-id frame-id)))}]
                                               [:de.explorama.frontend.map.views.map/log-updated-layers
                                                frame-id
                                                (keys updated-configs)]]]
                                 :overlayers
                                 [:dispatch-n [[::tasks/execute-wrapper
                                                frame-id
                                                :feature-layer
                                                {:feature-layers (mapv #(get updated-configs %)
                                                                       (get-in db (geop/selected-overlayers frame-id)))}]
                                               [:de.explorama.frontend.map.views.map/log-updated-layers
                                                frame-id
                                                (keys updated-configs)]]]
                                 nil))
                             frame-ids)]
     {:db (reduce (fn [db frame-id]
                    (update-in db
                               (case type
                                 :layouts (geop/temp-raw-marker-layouts frame-id)
                                 :overlayers (geop/temp-raw-feature-layers frame-id)
                                 nil)
                               merge
                               updated-configs))
                  db
                  frame-ids)
      :fx (conj update-events
                [:dispatch-n [callback]])})))