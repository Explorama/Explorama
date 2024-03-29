(ns de.explorama.frontend.mosaic.event-replay
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.event-logging.util :as log-util]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.common.tubes :as tubes]
            [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.mosaic :as gg]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [de.explorama.frontend.mosaic.vis.details :as details]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(def event-version 1)

(defn is-mosaic? [frame-id]
  (= config/default-vertical-str (:vertical frame-id)))

(defn frame-exist [db frame-id]
  (not-empty (get-in db (gp/frame-path-id frame-id))))

(defn check-frame-id
  ([db frame-id callback-vec body]
   (check-frame-id true db frame-id callback-vec body))
  ([_ _ frame-id callback-vec body]
   (if (is-mosaic? frame-id)
     (if (fn? body)
       (body)
       body)
     (do (debug "ignore event " frame-id callback-vec)
         (cond-> {}
           callback-vec (assoc :dispatch callback-vec))))))

(defn save-callbackvec-in-appstate [db frame-id callback-vec]
  (ddq/set-event-callback db
                          frame-id
                          callback-vec))

(defn resize-event
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec {:keys [path width height]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (cond-> db
          :always (save-callbackvec-in-appstate frame-id callback-vec))
    :dispatch-n [[::ddq/queue (gp/frame-id path)
                  [:de.explorama.frontend.mosaic.render.actions/resize path width height]]]}))

(defn adjust-event
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec {:keys [path cpl zoom key]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   (fn []
     {:db (-> (save-callbackvec-in-appstate db frame-id callback-vec)
              (assoc-in (gp/canvas-state-replay frame-id) [{:cpl cpl
                                                            :zoom zoom} nil]))
      :dispatch-n [[::ddq/queue (gp/frame-id path)
                    [:de.explorama.frontend.mosaic.render.actions/update
                     path
                     [[:adjust-replay? {:cpl cpl
                                        :zoom zoom
                                        :key key}]]]]]})))

(re-frame/reg-event-fx
 ::load-event
 (fn [{db :db} [_ path event-id bucket callback]]
   (let [removed-events (get-in db (gp/removed-detail-view-events path) #{})]
     (cond (removed-events event-id)
           {:db (update-in db
                           (gp/removed-detail-view-events path)
                           #(-> (or % #{})
                                (disj event-id)))}
           (grc/get-event (gp/frame-id path)
                          bucket event-id)
           {:fx [(when callback [:dispatch callback])]}
           :else
           {:dispatch-later [{:ms 500
                              :dispatch [::load-event path event-id bucket callback]}]}))))

(defn add-to-details-view-event
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec {:keys [path event-id bucket] :as desc}]
  (when-not (grc/get-event (gp/frame-id path) bucket event-id)
    (tubes/dispatch-to-server [ws-api/get-events-route
                               {:client-callback [ws-api/get-events-result
                                                  path
                                                  pc/main-stage-index]}
                               [[bucket event-id]]]))

  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (update-in db
                   (gp/removed-detail-view-events frame-id)
                   #(-> (or % #{})
                        (disj event-id)))
    :fx [[:dispatch [::load-event path event-id bucket
                     [::details/add-to-details-view
                      path
                      (assoc desc
                             :while-project-loading?
                             (boolean (fi/call-api :project-loading-db-get db)))]]]
         (when callback-vec
           [:dispatch callback-vec])]}))

(defn remove-from-details-view
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec desc]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (update-in db
                   (gp/removed-detail-view-events frame-id)
                   #(-> (or % #{})
                        (conj (:event-id desc)))) ; event-id wird rein genommen in den removed
    :fx [[:dispatch [::details/remove-from-details-view frame-id desc]]
         (when callback-vec
           [:dispatch callback-vec])]}))

(defn reset-event
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec {:keys [path]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (save-callbackvec-in-appstate db frame-id callback-vec)
    :dispatch-n [[::ddq/queue (gp/frame-id path)
                  [:de.explorama.frontend.mosaic.render.actions/update path :reset?]]]}))

(defn copy-event
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec {:keys [source] :as params}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (save-callbackvec-in-appstate db frame-id callback-vec)
    :dispatch-n [[::ddq/queue (gp/frame-id source)
                  [:de.explorama.frontend.mosaic.operations.util/copy params]]]}))

(defn create-frame-event
  "save-callbackvec-in-appstate"
  [db frame-id callback-vec _]
  (check-frame-id
   false
   db
   frame-id
   callback-vec
   {:db (-> (gg/initialize db frame-id [nil nil])
            (save-callbackvec-in-appstate frame-id callback-vec))
    :fx [[:dispatch [::ddq/register-tracks frame-id
                     [:de.explorama.frontend.mosaic.operations.core/track-register-canvas-rendering (gp/canvas frame-id)]]]]}))

(defn close-event
  "save-callbackvec-for-on-exit"
  [db frame-id callback-vec {:keys [path]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (assoc-in db [:mosaic :on-exit-callback frame-id] callback-vec)
    :dispatch-n [[:de.explorama.frontend.mosaic.operations.util/close path]]}))

(defn close-frame-event
  "save-callbackvec-for-on-exit"
  [db frame-id callback-vec _]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:dispatch-n [[:de.explorama.frontend.mosaic.core/close-action frame-id callback-vec]]}))

(defn add-annotation-to-information-event [db frame-id callback-vec {:keys [path annotation]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:dispatch-n [callback-vec
                 [:de.explorama.frontend.mosaic.views.top-level/annotation path annotation]]}))

(defn top-level-rename-event [db frame-id callback-vec {:keys [path title]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:dispatch-n [callback-vec
                 [:de.explorama.frontend.mosaic.views.top-level/rename-title path title]]}))

(defn top-level-set-title-event [db frame-id callback-vec {:keys [path title]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:dispatch-n [callback-vec
                 [:de.explorama.frontend.mosaic.views.top-level/set-title path title]]}))

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(defn connect-event [db _ callback-vec {{:keys [di selections]} :connection-data
                                        :keys [frame-target-id]}]
  (let [update-done-vec [::update-done-check callback-vec]]
    (check-frame-id
     db
     frame-target-id
     callback-vec
     (fn []
       (if (and di
                (not= di (get-in db (gp/data-instance frame-target-id))))
         {:db (-> (if selections
                    (assoc-in db (gp/selections frame-target-id) selections)
                    db)
                  (assoc-in gp/replay-update-needed 1)
                  (ddq/set-event-callback frame-target-id update-done-vec))
          :fx [[:dispatch [:de.explorama.frontend.mosaic.operations.tasks/execute-wrapper
                           frame-target-id
                           :init
                           {:di di
                            :operation-desc nil}
                           false]]
               (when selections
                 [:dispatch [::ddq/queue frame-target-id
                             [:de.explorama.frontend.mosaic.render.core/highlight-instance frame-target-id]]])]}
         {:fx [(when callback-vec
                 [:dispatch callback-vec])]})))))

(re-frame/reg-event-fx
 ::update-done-check
 (fn [{db :db} [_ callback-vec]]
   (let [max-to-be-waited (get-in db gp/replay-update-needed 1)
         new-needed-done (inc (get-in db gp/replay-update-needed 0))]
     (if (>= new-needed-done max-to-be-waited)
       {:db (update db gp/mosaic-root dissoc gp/replay-update-needed-key gp/replay-update-current-key)
        :dispatch callback-vec}
       {:db (assoc-in db gp/replay-update-current new-needed-done)}))))

(defn pan-zoom-event
  [db frame-id callback-vec {:keys [x y z zoom app-state-path]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (save-callbackvec-in-appstate db frame-id callback-vec)
    :dispatch-n [[::ddq/queue (gp/frame-id app-state-path)
                  [:de.explorama.frontend.mosaic.render.actions/update (gp/top-level app-state-path)
                   [[:pan-and-zoom? {:x x
                                     :y y
                                     :z z
                                     :next-zoom zoom}]]]]]}))

(defn operations-event [db frame-id callback-vec {:keys [payload action]}]
  (check-frame-id
   db
   frame-id
   callback-vec
   {:db (cond-> (save-callbackvec-in-appstate db frame-id callback-vec)
          (seq (:layouts payload))
          (assoc-in (gp/selected-layouts frame-id) (:layouts payload)))
    :dispatch [::tasks/execute-wrapper
               (gp/top-level frame-id)
               action
               (assoc payload
                      :send-data-acs? true
                      :scatter-axis-fallback? true
                      :raw-layouts (get-in db gp/raw-layouts))
               false]}))

(def event-vector-funcs
  "[event-name event-version]"
  {["resize" 1] resize-event
   ["adjust" 1] adjust-event
   ["reset" 1] reset-event
   ["copy" 1] copy-event
   ["create-frame" 1] create-frame-event
   ["close" 1] close-event
   ["add-annotation-to-information" 1] add-annotation-to-information-event
   ["top-level-rename" 1] top-level-rename-event
   ["top-level-set-title" 1] top-level-set-title-event
   ["close-frame" 1] close-frame-event
   ["add-to-details-view" 1] add-to-details-view-event
   ["remove-from-details-view" 1] remove-from-details-view
   ["canvas-state" 1] pan-zoom-event
   ["operation" 1] operations-event})

(defn- sync-frame-event [db frame-id {:keys [size]}]
  (when-not (get-in db (gp/canvas-status frame-id))
    (check-frame-id
     false
     db
     frame-id
     nil
     {:db (-> (gg/initialize db frame-id size)
              (save-callbackvec-in-appstate frame-id nil))
      :fx [[:dispatch [::ddq/register-tracks frame-id
                       [:de.explorama.frontend.mosaic.operations.core/track-register-canvas-rendering (gp/canvas frame-id)]]]]})))

(def sync-event-vector-funcs
  (merge event-vector-funcs
         {}))

(defn event-func
  ([event-name]
   (event-func event-name event-version))
  ([event-name event-version]
   (get event-vector-funcs [event-name event-version])))

(defn- sync-event-func
  ([event-name]
   (sync-event-func event-name 1))
  ([event-name event-version]
   (get sync-event-vector-funcs [event-name event-version])))

(re-frame/reg-event-fx
 ::frame-broadcast-receiver
 (fn [{db :db} [_ frame-id frame-desc]]
   (sync-frame-event db frame-id frame-desc)))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (sync-event-func event-name event-version)]
     (debug "sync-event mosaic" {:frame-id frame-id
                                 :event-name event-name
                                 :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id nil parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] frames callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "REPLAY MOSAIC"  event-name " - " frame-id)
     (if event-func
       (event-func db frame-id callback-vec parsed-event-params (:ignored frames))
       (do
         (debug "no event-function found for " [event-name event-version])
         {:dispatch callback-vec})))))

(re-frame/reg-event-fx
 ::next-replay-event
 (fn [{db :db} [_ current-event rest-events events-done max-events done-event frames plogs-id profiling-state]]
   (let [profiling-state (when profiling-state
                           (-> (assoc-in profiling-state
                                         [(get profiling-state :last-event)
                                          :end]
                                         (.now js/Date.))
                               (assoc current-event {:start (.now js/Date.)
                                                     :num (inc events-done)}
                                      :last-event current-event)))]
     (when (aget js/window "sendHealthPing")
       (.sendHealthPing js/window))
     (cond-> {:db (assoc-in db gp/replay-progress (if (> max-events 0)
                                                    (/ events-done
                                                       max-events)
                                                    0))}
       current-event
       (assoc :dispatch [::replay-event
                         current-event
                         frames
                         [::next-replay-event
                          (first rest-events)
                          (rest rest-events)
                          (inc events-done)
                          max-events
                          done-event
                          frames
                          plogs-id
                          (when profiling-state
                            profiling-state)]])
       (not current-event)
       (assoc :fx (-> (mapv (fn [frame-id]
                              [:dispatch [:de.explorama.frontend.mosaic.interaction.resize/trigger-resize frame-id]])
                            (:exist frames))
                      (conj (cond-> [:dispatch done-event]
                              profiling-state
                              (conj profiling-state)))))))))

(def pre-process-events (partial log-util/pre-process-events
                                 is-mosaic?
                                 (constantly false)
                                 (constantly false)
                                 #{"add-to-details-view" "remove-from-details-view"}))

(re-frame/reg-event-fx
 ::replay-events
 (fn [_ [_ events done-event plogs-id test-and-profile?]]
   (let [{events :result frames :frames :as result}
         (pre-process-events events)]
     (debug ::pre-process-events result)
     {:dispatch-n
      [[::next-replay-event
        (first events)
        (rest events)
        0
        (count events)
        done-event
        frames
        plogs-id
        (when test-and-profile? test-and-profile?)]]})))
