(ns de.explorama.frontend.woco.frame.api
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug]]
            [vimsical.re-frame.cofx.inject :as inject]
            [de.explorama.frontend.woco.api.interaction-mode :as inter-mode]
            [de.explorama.frontend.woco.cleanup :as cleanup]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.event-logging :as event-log]
            [de.explorama.frontend.woco.frame.color :as frame-color]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.filter.core :as filter-core]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.frame.interaction.collision :refer [inside-other-frame?]]
            [de.explorama.frontend.woco.frame.interaction.dnd :as dnd]
            [de.explorama.frontend.woco.frame.interaction.move :as move :refer [moving-state]]
            [de.explorama.frontend.woco.frame.interaction.z-order :as z-order]
            [de.explorama.frontend.woco.frame.size-position :refer [delete-frame-position
                                                                    set-frame-full-size
                                                                    set-frame-position set-frame-size set-resize-infos]]
            [de.explorama.frontend.woco.frame.util :refer [is-content-frame? is-custom-frame? which-is-maximized
                                                           find-maximized-frame]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.workspace.states :as wws]))

(defn log-event? [db frame-id]
  (and (not (get-in db (path/frame-no-event-logging frame-id)))
       (inter-mode/render? db)
       (or (is-custom-frame? db frame-id)
           (is-content-frame? db frame-id))))

(re-frame/reg-event-fx
 ::move
 (fn [{db :db} [_ new-coords frame-id skip-frame-pos?]]
   (let [frame (get-in db (path/frame-desc frame-id))
         frame-exist? (not-empty frame)
         frame-event (get-in db (path/frame-event frame-id))
         frame-component (:vertical frame-id)
         {current-step-info :additional-info} (get-in db path/product-tour-current-step)
         is-move-step? (= current-step-info :move)
         updated-frame (assoc frame :coords new-coords)
         inside-other? (when is-move-step?
                         (inside-other-frame? updated-frame
                                              (dissoc (get-in db path/frames)
                                                      frame-id)))]
     (when (and frame-id frame-exist?)
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info :move}
                                    (fn []
                                      (when-not skip-frame-pos?
                                        (set-frame-position frame-id new-coords))
                                      {:db (assoc-in db
                                                     (path/frame-coords frame-id)
                                                     new-coords)
                                       :dispatch-n [(if (or (is-custom-frame? db frame-id)
                                                            (is-content-frame? db frame-id))
                                                      [::event-log/log-frame-event frame-id]
                                                      [::event-log/broadcast-frame-event frame-id])
                                                    (when (and is-move-step? (not inside-other?))
                                                      [:de.explorama.frontend.woco.api.product-tour/next-step frame-component :move])
                                                    [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]}))))))

;; Moves a frame related to another one like "move frame a to right of frame b"
(re-frame/reg-event-fx
 ::move-to-frame
 (fn [{db :db} [_ move-frame-id related-frame-id direction]]
   (let [related-frame-desc (get-in db (path/frame-desc related-frame-id))
         [related-x related-y] (when related-frame-desc (:coords related-frame-desc))
         [related-width related-height] (when related-frame-desc (:full-size related-frame-desc))]
     (when (and related-x related-y related-width related-height)
       (when-let [new-coords (case direction
                               :right [(+ related-x related-width config/move-to-frame-offset)
                                       related-y]
                               :bottom [related-x
                                        (+ related-y related-height config/move-to-frame-offset)]
                               nil)]
         {:dispatch [::move/move-frame move-frame-id new-coords]})))))

(re-frame/reg-event-fx
 ::close
 (fn [{db :db} [_ frame-id no-broadcast?]]
   (let [frame-event (get-in db (path/frame-event frame-id))
         close-event [::close-final frame-id]
         frame-component (:vertical frame-id)
         {current-step-info :additional-info} (get-in db path/product-tour-current-step)
         maximized-frame-id (get-in db path/maximized-frame)
         existing-or-refed-frames (reduce (fn [acc [frame-id frame-desc]]
                                            (conj acc frame-id (:published-by-frame frame-desc)))
                                          #{}
                                          (get-in db path/frames))]
     (when frame-event
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info :close}
                                    (fn []
                                      (delete-frame-position frame-id)
                                      (let [automatic-placement-list (get-in db (conj path/current-workspace-grid :num) [])
                                            db (cond-> (if (empty? automatic-placement-list)
                                                         db
                                                         (assoc-in db
                                                                   (conj path/current-workspace-grid :num)
                                                                   (mapv #(if (= % frame-id) nil %)
                                                                         automatic-placement-list)))
                                                 :always (update-in path/frame-header-colors select-keys existing-or-refed-frames)
                                                 (= frame-id maximized-frame-id) (assoc-in path/maximized-frame false))]
                                        {:db db
                                         :dispatch-n [[frame-event evts/close {:frame-id frame-id
                                                                               :callback-event close-event}]
                                                      [:de.explorama.frontend.woco.api.product-tour/next-step frame-component :close]
                                                      (when (= evts/content-type
                                                               (get-in db (path/frame-type frame-id)))
                                                        [::event-log/log-event
                                                         frame-id
                                                         "header-colors"
                                                         (get-in db path/frame-header-colors)])
                                                      (cond
                                                        (#{evts/content-type evts/custom-type}
                                                         (get-in db (path/frame-type frame-id))) [::event-log/log-event
                                                                                                  frame-id
                                                                                                  "close-frame"
                                                                                                  {}]
                                                        (not no-broadcast?) [::event-log/broadcast-frame-close frame-id])]})))))))

(re-frame/reg-event-fx
 ::override
 (fn [{db :db} [_ frame-id {source-frame-id :frame-source-id :as params}]]
   (let [frame-event (get-in db (path/frame-event frame-id))
         frame-component (:vertical frame-id)
         frame-published-by (frame-info/parent db source-frame-id)
         source-filter (get-in db (path/frame-last-applied-filters source-frame-id))
         db (cond-> (assoc-in db (path/frame-published-by frame-id)  frame-published-by)
              (get-in db (conj (path/frame-filter source-frame-id) :active?))
              (filter-core/set-external-filter frame-id source-filter)
              :always
              (assoc-in (path/frame-publishing? frame-id) false))]
     (when frame-event
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      {:db db
                                       :fx [[:dispatch [frame-event evts/override params]]
                                            (if (or
                                                 (is-custom-frame? db frame-id)
                                                 (is-content-frame? db frame-id))
                                              [::event-log/log-frame-event frame-id]
                                              [::event-log/broadcast-frame-event frame-id])
                                            [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]}))))))

(re-frame/reg-event-fx
 ::recreate
 (fn [{db :db} [_ frame-id params]]
   (let [frame-event (get-in db (path/frame-event frame-id))
         frame-component (:vertical frame-id)
         db (-> (path/dissoc-in db (path/frame-published-by frame-id))
                (path/dissoc-in (path/frame-last-applied-filters frame-id))
                (path/dissoc-in (path/frame-filter frame-id))
                (frame-color/new-header-color frame-id)
                (assoc-in (path/frame-publishing? frame-id) true)
                (path/dissoc-in (conj (path/frame-desc frame-id) :custom-title)))]
     (when frame-event
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      {:db db
                                       :dispatch-n [[frame-event evts/recreate params]
                                                    (if (or
                                                         (is-custom-frame? db frame-id)
                                                         (is-content-frame? db frame-id))
                                                      [::event-log/log-frame-event frame-id]
                                                      [::event-log/broadcast-frame-event frame-id])]}))))))

(re-frame/reg-event-fx
 ::delete-quietly
 (fn [{db :db} [_ frame-id callback]]
   {:db (-> db
            (path/dissoc-in (path/frame-desc frame-id))
            (update-in path/frames dissoc nil "")) ;HACK Sometimes there is a empty frame 
    :dispatch-n [(when callback (conj callback frame-id))]}))

(re-frame/reg-event-fx
 ::close-final
 (fn [{db :db} [_ frame-id callback-event]]
   {:db (-> db
            (path/dissoc-in (path/frame-desc frame-id))
            (update-in path/frames dissoc nil "")) ;HACK Sometimes there is a empty frame 
    :dispatch-n [(when callback-event
                   callback-event)
                 [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]}))

(defn lookup-frame-id [frame-lookup-table frame-id]
  (get frame-lookup-table frame-id frame-id))

(re-frame/reg-event-fx
 ::connect
 (fn [{db :db} [_ frame-source-id frame-target-id]]
   (debug ::connect frame-source-id frame-target-id)
   (let [target-frame-event (get-in db (path/frame-event frame-target-id))
         frame-source-id (frame-info/parent db frame-source-id)
         frame-source-data (frame-info/gather-information db frame-source-id)
         payload {:frame-source-id frame-source-id
                  :frame-target-id frame-target-id
                  :payload frame-source-data}
         frame-component (:vertical frame-source-id)
         {current-step-info :additional-info} (get-in db path/product-tour-current-step)
         db (-> (assoc-in db (path/frame-published-by frame-target-id) frame-source-id)
                (assoc-in (path/frame-publishing? frame-target-id) false))]
     (moving-state false true)
     (inter-mode/check-inter-mode db
                                  (get-in db (path/frame-type frame-source-id))
                                  {:frame-id frame-source-id
                                   :component frame-component
                                   :additional-info :connect}
                                  (fn []
                                    {:db db
                                     :dispatch-n [[:de.explorama.frontend.woco.api.product-tour/next-step frame-component :connect]
                                                  [::event-log/log-frame-event frame-target-id]
                                                  (when target-frame-event
                                                    [target-frame-event
                                                     evts/connect-to
                                                     payload])
                                                  [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]})))))

(re-frame/reg-event-fx
 ::connect-code
 (fn [{db :db} [_ frame-source-id frame-target-id]]
   (debug ::connect-code frame-source-id frame-target-id)
   (let [target-frame-event (get-in db (path/frame-event frame-target-id))
         frame-source-id (frame-info/parent db frame-source-id)
         frame-source-data (frame-info/gather-information db frame-source-id)
         payload {:frame-source-id frame-source-id
                  :frame-target-id frame-target-id
                  :payload frame-source-data}
         db (-> (assoc-in db (path/frame-published-by frame-target-id) frame-source-id)
                (assoc-in (path/frame-publishing? frame-target-id) false))]
     (inter-mode/check-inter-mode db
                                  (get-in db (path/frame-type frame-source-id))
                                  {:frame-id frame-source-id
                                   :component :*
                                   :additional-info nil}
                                  (fn []
                                    {:db db
                                     :dispatch-n [[::event-log/log-frame-event frame-target-id]
                                                  (when target-frame-event
                                                    [target-frame-event
                                                     evts/connect-to
                                                     payload])]})))))

(re-frame/reg-event-fx
 ::set-frame-size
 (fn [{db :db} [_ frame-id width height full-width full-height]]
   (let [abort (or (nil? frame-id) (nil? width) (nil? height))
         frame-exist? (not-empty (get-in db (path/frame-desc frame-id)))
         frame-component (:vertical frame-id)]
     (when frame-exist?
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      {:db (if abort
                                             db
                                             (-> db
                                                 (set-frame-size frame-id width height)
                                                 (set-frame-full-size frame-id
                                                                      (or full-width width)
                                                                      (or full-height height))))}))))))

(re-frame/reg-event-fx
 ::set-resize-infos
 (fn [{db :db} [_ frame-id resized-infos]]
   (when (not-empty (get-in db (path/frame-desc frame-id)))
     (let [frame-component (:vertical frame-id)]
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      (let [db (set-resize-infos db frame-id resized-infos)
                                            resize-listener (:resize-listener (fi/call-api :frame-instance-api-db-get db frame-id))]
                                        (cond-> {:db db}
                                          resize-listener
                                          (assoc :dispatch (conj resize-listener frame-id resized-infos))))))))))


(re-frame/reg-event-fx
 ::set-frame-coords
 (fn [{db :db} [_ frame-id left top]]
   (let [abort (or (nil? frame-id) (nil? left) (nil? top))
         frame-exist? (not-empty (get-in db (path/frame-desc frame-id)))
         frame-component (:vertical frame-id)]
     (when frame-exist?
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      (when-not abort
                                        (set-frame-position frame-id [left top])
                                        {:db (assoc-in db
                                                       (path/frame-coords frame-id)
                                                       [left top])})))))))

(re-frame/reg-event-db
 ::set-size-delta
 (fn [db [_ frame-id width-delta height-delta]]
   (when (not-empty (get-in db (path/frame-desc frame-id)))
     (if (or width-delta height-delta)
       (assoc-in db (path/frame-size-delta frame-id) [width-delta height-delta])
       (path/dissoc-in db (path/frame-size-delta frame-id))))))

(defn size-delta [db frame-id]
  (get-in db (path/frame-size-delta frame-id)))

(re-frame/reg-sub
 ::size-delta
 (fn [db [_ frame-id]]
   (size-delta db frame-id)))

(re-frame/reg-sub
 ::is-maximized?
 (fn [db [_ frame-id]]
   (get-in db (path/frame-is-maximized frame-id) false)))

(re-frame/reg-sub
 ::is-minimized?
 (fn [db [_ frame-id]]
   (get-in db (path/frame-is-minimized frame-id) false)))

(re-frame/reg-sub
 ::any-frame-maximized?
 (fn [db _]
   (which-is-maximized db)))

(re-frame/reg-event-fx
 ::minimize
 [inter-mode/ro-interceptor]
 (fn [{db :db} [_ frame-id]]
   (let [frame-component (:vertical frame-id)
         log? (log-event? db frame-id)
         frame-infos (get-in db (path/frame-desc frame-id))]
     (when (not-empty frame-infos)
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info :minimize}
                                    (fn []
                                      (-> {:db (-> db
                                                   (assoc-in (path/frame-is-minimized frame-id) true)
                                                   (assoc-in (path/frame-is-maximized frame-id) false))}
                                          (assoc :fx
                                                 [(if log?
                                                    [:dispatch [::event-log/log-frame-event frame-id]]
                                                    [:dispatch [::event-log/broadcast-frame-event frame-id]])
                                                  [:dispatch [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]]))))))))

(re-frame/reg-event-fx
 ::maximize
 [inter-mode/ro-interceptor]
 (fn [{db :db} [_ frame-id]]
   (let [frame-infos (get-in db (path/frame-desc frame-id))
         frame-exist? (not-empty frame-infos)
         frame-component (:vertical frame-id)
         log? (log-event? db frame-id)]
     (when frame-exist?
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info :maximize}
                                    (fn []
                                      (cond-> {:db (-> (assoc-in db (path/frame-is-maximized frame-id) true)
                                                       (assoc-in (path/frame-is-minimized frame-id) false))}
                                        log?
                                        (assoc :dispatch [::event-log/log-frame-event frame-id])
                                        (not log?)
                                        (assoc :dispatch [::event-log/broadcast-frame-event frame-id]))))))))

(re-frame/reg-event-fx
 ::normalize
 [inter-mode/ro-interceptor]
 (fn [{db :db} [_ frame-id]]
   (let [[frame-id frame-infos]
         (if frame-id
           [frame-id (get-in db (path/frame-desc frame-id))]
           (or (find-maximized-frame db)
               [nil nil]))]
     (when (and frame-id frame-infos)
       (let [frame-exist? (not-empty frame-infos)
             frame-component (:vertical frame-id)
             log? (log-event? db frame-id)]
         (when frame-exist?
           (inter-mode/check-inter-mode db
                                        (get-in db (path/frame-type frame-id))
                                        {:frame-id frame-id
                                         :component frame-component
                                         :additional-info :normalize}
                                        (fn []
                                          (-> {:db (-> db
                                                       (assoc-in (path/frame-is-maximized frame-id) false)
                                                       (assoc-in (path/frame-is-minimized frame-id) false))}
                                              (assoc :fx
                                                     [(if log?
                                                        [:dispatch [::event-log/log-frame-event frame-id]]
                                                        [:dispatch [::event-log/broadcast-frame-event frame-id]])
                                                      [:dispatch [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]]))))))))))

(re-frame/reg-event-fx
 ::frame-did-update
 (fn [{db :db} [_ frame-id]]
   (let [instance-api (fi/call-api :frame-instance-api-db-get
                                   db
                                   frame-id)
         desc (fi/call-api :frame-db-get db frame-id)]
     (when (:resize-listener instance-api)
       {:dispatch (conj (:resize-listener instance-api) frame-id {:width (get-in desc [:size 0])
                                                                  :height (get-in desc [:size 1])})}))))

(re-frame/reg-event-fx
 ::set-title
 (fn [{db :db} [_ title frame-id]]
   (let [frame-component (:vertical frame-id)]
     (if (not-empty (get-in db (path/frame-desc frame-id)))
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      {:db (-> db
                                               (assoc-in (path/frame-title frame-id) title)
                                               (assoc-in (path/all-frame-title frame-id) title))}))
       (inter-mode/check-inter-mode db
                                    (get-in db (path/frame-type frame-id))
                                    {:frame-id frame-id
                                     :component frame-component
                                     :additional-info nil}
                                    (fn []
                                      {:db (-> db
                                               (assoc-in (path/all-frame-title frame-id) title))}))))))
(defn title [db frame-id]
  (get-in db (path/frame-title frame-id)))

(re-frame/reg-sub
 ::title
 (fn [db [_ frame-id]]
   (title db frame-id)))

(defn title-all [db frame-id]
  (get-in db (path/all-frame-title frame-id)))

(re-frame/reg-sub
 ::title-all
 (fn [db [_ frame-id]]
   (title-all db frame-id)))

(re-frame/reg-sub
 ::vertical-count
 (fn [db [_ frame-id]]
   (get-in db (path/frame-vertical-number frame-id))))

(re-frame/reg-event-fx
 ::set-optional-class
 (fn [{db :db} [_ frame-id optional-class to-log?]]
   (when (not-empty (get-in db (path/frame-desc frame-id)))
     {:db (assoc-in db (path/frame-optional-class frame-id) optional-class)
      :dispatch-n [(if to-log?
                     [::event-log/log-frame-event frame-id]
                     [::event-log/broadcast-frame-event frame-id])]})))

(re-frame/reg-event-fx
 ::send-view-event-to-all
 (fn [{db :db
       frames ::list} [_ action-key event-arg]]
   {:dispatch-n (mapv (fn [{frame-id :id}]
                        [(get-in db (path/frame-event frame-id)) action-key event-arg])
                      frames)}))

(re-frame/reg-event-fx
 ::bring-to-front
 (fn [_ [_ frame-id]]
   {:dispatch [::z-order/bring-to-front frame-id]}))

(defn list-frames [db query]
  (let [query-fun (if (empty? query) (constantly true) (set query))]
    (reduce (fn [result [frame-id frame]]
              (if (query-fun (:type frame))
                (conj result
                      {:id frame-id
                       :z-index (:z-index frame)
                       :title (:title frame)})
                result))
            []
            (get-in db path/frames))))

(re-frame/reg-sub
 ::list
 (fn [db [_ query]]
   (list-frames db query)))

(re-frame/reg-sub
 ::frames-positions
 (fn [db [_ frame-filter]]
   (let [frame-filter (or frame-filter identity)]
     (reduce (fn [result [frame-id {[left top] :coords
                                    [width height] :size
                                    [full-width full-height] :full-size
                                    title :title
                                    z-index :z-index
                                    is-minimized? :is-minimized?
                                    published-frame-id :published-by-frame
                                    :as desc}]]
               (if (and frame-id
                        (frame-filter desc))
                 (let [group (or (get-in db (path/frame-header-color frame-id))
                                 (when published-frame-id
                                   (get-in db (path/frame-header-color published-frame-id))))
                       di (frame-info/api-value db frame-id :di)]
                   (conj result {:id frame-id
                                 :z-index z-index
                                 :title title
                                 :left left
                                 :top top
                                 :width width
                                 :height height
                                 :full-width full-width
                                 :full-height full-height
                                 :is-minimized? is-minimized?
                                 :color-group group
                                 :di di}))
                 result))
             []
             (get-in db path/frames)))))

(re-frame/reg-sub
 ::list-as-map
 (fn [db [_ query]]
   (let [query-fun (if (empty? query) (constantly true) (set query))]
     (reduce (fn [result [frame-id frame]]
               (if (query-fun (:type frame))
                 (assoc result
                        frame-id
                        {:id frame-id
                         :title (:title frame)})
                 result))
             {}
             (get-in db path/frames)))))

(re-frame/reg-sub
 ::list-vertical
 (fn [db [_ vertical query]]
   (let [query-fun (if (empty? query) (constantly true) (set query))]
     (reduce (fn [result [_ frame]]
               (if (and (query-fun (:type frame)) (= vertical (:vertical frame)))
                 (conj result
                       frame)
                 result))
             []
             (get-in db path/frames)))))

(defn list-id-vertical [db vertical query]
  (let [query-fun (if (empty? query) (constantly true) (set query))]
    (reduce (fn [result [frame-id frame]]
              (if (and (query-fun (:type frame)) (= vertical (:vertical frame)))
                (conj result
                      frame-id)
                result))
            []
            (get-in db path/frames))))

(re-frame/reg-sub
 ::list-id-vertical
 (fn [db [_ vertical query]]
   (list-id-vertical db vertical query)))

(re-frame/reg-sub
 ::which-is-maximized
 (fn [db]
   (which-is-maximized db)))

(re-frame/reg-sub
 ::infos
 (fn [db [_ id]]
   (dissoc (get-in db (path/frame-desc id))
           :module)))

(re-frame/reg-event-fx
 ::clean-workspace
 [(re-frame/inject-cofx ::inject/sub
                        (with-meta
                          [:de.explorama.frontend.woco.api.registry/lookup-category :clean-workspace]
                          {:ignore-dispose true}))]
 (fn [{db :db
       services :de.explorama.frontend.woco.api.registry/lookup-category} [_ follow-event reason]]
   (reset! wws/multiselect-current-selection #{})
   (reset! wws/multiselect-bb-before-move nil)
   (reset! wws/multiselect-bb nil)
   (reset! wws/temporary-frames {})
   (reset! wws/temporary-selection #{})
   (reset! wws/window-creation-mouse nil)
   {:db (-> (assoc db
                   ::cleanup/clean-workspace services
                   ::cleanup/clean-finished follow-event)
            (assoc-in (path/workspace-id) (str (random-uuid)))
            (path/dissoc-in path/id-counter-root)
            (assoc-in (conj path/current-workspace-grid :num) [])
            (update-in path/root
                       dissoc
                       path/curr-max-zindex-key
                       path/current-group-key
                       path/replay-progress-key
                       path/overlayer-active-key
                       path/product-tour-key)
            (dissoc :woco.frame/all-frames-title)
            (path/dissoc-in path/interaction-mode))
    :dispatch-n (mapv #(conj % [::cleanup/clean-finished] reason)
                      (vals services))}))

(re-frame/reg-event-fx
 ::render-done
 (fn [{db :db} [_ frame-id origin]]
   (let [new-frames-render (filterv #(not (= frame-id %))
                                    (get-in db [:woco :frames-to-render]))]
     (debug "Frame " frame-id " done with render from " (or origin "unknown service"))
     (debug "New render wait " new-frames-render)
     {:db (cond-> db
            (empty? new-frames-render) (update :woco dissoc :render-frames-callback)
            :always (assoc-in [:woco :frames-to-render] new-frames-render))
      :dispatch-n [(when (empty? new-frames-render)
                     (get-in db [:woco :render-frames-callback]))]})))

(re-frame/reg-event-fx
 ::initiate-vertical-drag
 (fn [{db :db} [_ {:keys [frame-id] :as drag-infos}]]
   (inter-mode/check-inter-mode db
                                (get-in db (path/frame-type frame-id))
                                {:frame-id frame-id
                                 :component (:vertical frame-id)
                                 :additional-info :drag-vertical}
                                (fn []
                                  (dnd/vertical-drag-start drag-infos)))

   {}))

(re-frame/reg-event-db
 ::reset-vertical-drag
 (fn [db _]
   (dnd/reset-vertical-drag)
   db))

(re-frame/reg-sub
 ::all-frame-ids
 (fn [db _]
   (into #{}
         (comp (map (fn [[fkey frame]]
                      (when-not (or (empty? fkey)
                                    (nil? frame))
                        fkey)))
               (filter identity))
         (get-in db path/frames))))

(re-frame/reg-event-fx
 ::query
 (fn [{db :db} [_ frame-id query callback-event]]
   (let [frame-event (get-in db (path/frame-event frame-id))]
     {:dispatch-n [(when-not frame-event
                     callback-event)
                   (when (and callback-event frame-event)
                     [frame-event
                      evts/query
                      {:query query
                       :frame-id frame-id
                       :callback-event callback-event}])]})))
