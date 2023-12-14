(ns de.explorama.frontend.mosaic.render.actions
  (:require [clojure.set :as set]
            [de.explorama.frontend.mosaic.interaction.selection :as selection]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.db :refer [instances]]
            [de.explorama.frontend.mosaic.vis.config :as config]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]
            [de.explorama.frontend.common.queue :as ddq]))

(def update-types {:adjust-horizontal? #{}
                   :adjust-vertical? #{}
                   :adjust-one-row? #{}
                   :adjust-replay? #{:cpl :zoom}
                   :rerender? #{}
                   :reset? #{}
                   :filter? #{}
                   #_#_:sort? #{}
                   :set? #{}
                   :resize? #{}
                   #_#_:ungroup? #{}
                   #_#_:group? #{}
                   #_#_:group-sort? #{}
                   #_#_:group-remove? #{}
                   :sync-couple #{:group-sizes :by}
                   :coupled? #{}
                   :reconnect? #{}
                   #_#_:layout? #{}
                   #_#_:scatter-plot? #{}
                   #_#_:grid? #{}
                   :pan-and-zoom? #{:x :y :z :next-zoom}})

(defn generate-action [action task-id & [parameters]]
  (let [correct-params? (set/subset? (update-types action)
                                     (-> parameters keys set))]
    (if correct-params?
      {action {:task-id task-id
               :params parameters}}
      (do
        (error "Update action " action " not valid " (if parameters (str "with parameters " parameters) "without parameters"))
        {}))))

(defn build-updates [updates task-id]
  (cond (vector? updates)
        (reduce (fn [acc element]
                  (cond (vector? element)
                        (let [[action parameters] element]
                          (merge acc (generate-action action task-id parameters)))
                        (update-types element)
                        (merge acc (generate-action element task-id))
                        :else
                        (do
                          (error "Unknown update action" element)
                          acc)))
                {}
                updates)
        (updates update-types)
        (generate-action updates task-id)
        :else
        (do
          (error "Unknown update action" updates)
          {})))

(defn update-canvas [db path updates task-id]
  (assoc-in db (gp/updates path) (build-updates updates task-id)))

;rerenders all available instances (For changing of i18n number format)
(re-frame/reg-event-fx
 ::rerender-all
 (fn [{db :db}]
   {:dispatch-n
    (reduce (fn [r frame-id]
              (let [p (gp/top-level frame-id)]
                (cond-> r
                  (and p frame-id)
                  (conj [::ddq/queue frame-id [::update p :rerender?]]))))

                      
            []
            (keys (get-in db gp/instances)))}))

(re-frame/reg-event-db
 ::update
 (fn [db [_ path updates task-id]]
   (-> db
       (update-canvas path updates task-id))))

(re-frame/reg-event-db
 ::resize
 (fn [db [_ path width height task-id]]
   (let [{canvas-width :width canvas-height :height} (config/canvas-size width height)]
     (-> db
         (update-in (gp/top-level path) (fn [val]
                                          (-> (assoc val
                                                     :width width
                                                     :height height)
                                              (assoc-in [:canvas :width] canvas-width)
                                              (assoc-in [:canvas :height] canvas-height))))
         (assoc-in (gp/updates path) (build-updates :resize? task-id))))))

(defn publish-highlight [_this path action single]
  (case action
    :select (re-frame/dispatch [::selection/select path single])
    :deselect (re-frame/dispatch [::selection/deselect path single])
    (error "Publish highlight action unknown" action path)))

(defn update-annotations [path params]
  (let [instance (get @instances (gp/top-level path))]
    (gre/update-annotations instance params)))
  