(ns de.explorama.frontend.mosaic.project.post-processing
  (:require [clojure.set :refer [union]]
            [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]
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

;; Logic for layout checks:
;;   User triggers project loading:
;;     -> Projects load steps (+ verticals say that are done) 
;;     -> post-process -> vertical 
;;        -> apply check-layouts to config
;;        -> config checks with on his own post-process event
;;        -> sending updates to mosaic if there are any
;;           -> Do updates
;;        -> say done with callback from config 
;;        -> config fires post-processing done callback
;;     -> project is loaded

(defn- gather-layouts [db]
  (let [frame-ids (vec (keys (get-in db gp/instances)))]
    (reduce (fn [acc frame-id]
              (let [frame-layouts (get-in db (gp/selected-layouts frame-id))
                    temp-layouts (get-in db (gp/selected-layouts frame-id))]
                (reduce (fn [acc layout-id]
                          (let [{:keys [temporary?] :as desc} (get temp-layouts layout-id)]
                            (cond-> acc
                              (and (map? desc)
                                   (not temporary?))
                              (update layout-id (fn [o]
                                                  (-> o
                                                      (assoc :layout-desc desc)
                                                      (update :frames #(-> (or % #{})
                                                                           (conj frame-id)))))))))
                        acc
                        frame-layouts)))
            {}
            frame-ids)))

(reg-event-fx
 ::post-process
 (fn [{db :db} [_ {:keys [callback]}]]
   (debug "execute post-process")
   (let [check-layouts (gather-layouts db)]
     {:db (if (seq check-layouts)
            (fi/call-api [:config :project-post-processing :check-for-updates-db-update]
                         db
                         :layouts
                         {:check-layouts check-layouts
                          :handle-updates-event [::handle-updates]})
            db)
      :dispatch-n [callback]})))

(reg-event-fx
 ::handle-updates
 (fn [{db :db} [_ {:keys [updates callback]}]]
   (debug "handle-updates")
   (let [frame-ids (->> updates
                        vals
                        (map :frames)
                        (apply union))
         updated-layouts (select-keys (fi/call-api [:config :get-config-db-get]
                                                   db
                                                   :layouts)
                                      (vec (keys updates)))
         update-events (mapv (fn [frame-id]
                               [:dispatch [:de.explorama.frontend.mosaic.views.legend/change-layout
                                           frame-id
                                           (mapv #(get updated-layouts %)
                                                 (get-in db (gp/selected-layouts frame-id)))]])
                             frame-ids)]
     {#_#_:db (reduce (fn [db frame-id]
                        (update-in db
                                   (gp/temporary-layouts frame-id)
                                   merge
                                   updated-layouts))
                      db
                      frame-ids)
      :fx (conj update-events
                [:dispatch-n [callback]])})))