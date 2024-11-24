(ns de.explorama.frontend.woco.db
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.cleanup :as cleanup]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.config :as config]))

(def initial-db (atom (-> {::cleanup/before-unload-events []}
                          (assoc-in (path/workspace-id) (str (random-uuid)))
                          (assoc-in path/client-id (str (random-uuid))))))

(defn reg-init
  "Registers a map to be merged into the initial app db.  This will not have any
  effect after the app has started."
  [o]
  (swap! initial-db merge o))

(re-frame/reg-event-db
 ::reg-init-event
 (fn [db [_ init-event-to-call vertical]]
   (update-in db path/init-events conj {:event init-event-to-call
                                        :executed false})))

(defn all-done? [db]
  (let [current-done (get-in db path/plugins-init-done)]
    #_(js/console.info "all-done?" {:current current-done
                                    :available-plugins config/available-plugins})
    (when current-done
      (every? current-done config/available-plugins))))

(re-frame/reg-event-db
 ::init-done
 (fn [db [_ vertical]]
   (update-in db path/plugins-init-done (fn [v]
                                          (conj (or v #{})
                                                vertical)))))

(re-frame/reg-event-db
 ::initialize
 (fn [_ _]
   ;(println (str "init-db with " @initial-db))
   @initial-db))
