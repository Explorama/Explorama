(ns de.explorama.frontend.knowledge-editor.canvas
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.knowledge-editor.canvas.draw :as draw]
            [de.explorama.frontend.knowledge-editor.canvas.nav :as nav]
            [de.explorama.frontend.knowledge-editor.canvas.stages :as stages]
            [de.explorama.frontend.knowledge-editor.config :as config :refer [height
                                                                              width]]
            [de.explorama.frontend.knowledge-editor.path :as path]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [toolbar]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [error warn]]))

(def ^:private host-key "knowledge-editor-canvas")

(def ^:private instance (atom {}))

(defn- disable-tickers []
  (let [shared-ticker (aget js/PIXI "Ticker" "shared")
        system-ticker (aget js/PIXI "Ticker" "system")]
    (aset system-ticker "autoStart" false)
    (aset shared-ticker "autoStart" false)
    (when (aget system-ticker "started")
      (.stop system-ticker))
    (when (aget shared-ticker "started")
      (.stop shared-ticker))))

(defn- init-engine [frame-id width height]
  (disable-tickers)
  (when-not (:app @instance)
    (let [pixi-canvas (.getElementById js/document host-key)
          app (js/PIXI.Application. (clj->js {:autoStart false
                                              :width width
                                              :height height
                                              :backgroundColor 0xFFFFFF
                                              :antialias true
                                              :roundPixels true
                                              :resolution 2
                                              :autoDensity true
                                                 ;:autoResize true
                                              :forceCanvas false
                                              :view pixi-canvas}))
          listener [["wheel" (nav/wheel instance) {:passive false}]
                    ["mousedown" (nav/mousedown instance) {:passive true}]
                    ["mousemove" (nav/mousemove instance) {:passive true}]
                    ["mouseup" (nav/mouseup instance) {:passive true}]
                    ["mouseleave" (nav/mouseleave instance) {:passive true}]]
          main-container (js/PIXI.Container.)
          stage (aget app "stage")]
      (aset main-container "sortableChildren" true)
      (doseq [[on func opts] listener]
        (.addEventListener pixi-canvas on func (clj->js opts)))
      (.addChild stage main-container)
      (nav/set-transform-main main-container 0 0 1)
      (swap! instance
             assoc
             :app app
             :listener listener
             :frame-id frame-id
             [:pos stages/main-stage] {:x 0 :y 0 :z 1 :zoom 3}))))

(defn- update-content [instance content]
  (let [app (:app @instance)]
    (reduce (fn [acc [type elements]]
              (.render (.-renderer app)
                       (.-stage app))
              (reduce (fn [acc {:keys [state-id id] :as element}]
                        (if-let [new-state-id (draw/draw-card-static id state-id instance element)]
                          (assoc acc id new-state-id)
                          acc))
                      acc
                      elements))
            {}
            (group-by (fn [{:keys [type]}]
                        type)
                      content))
    (draw/clean-up-drawing instance (->> (map (fn [{:keys [id]}] id) content)
                                         set))
    (.render (.-renderer app)
             (.-stage app))))

(re-frame/reg-event-fx
 ::init
 (fn [{db :db} [_ frame-id]]
   (init-engine frame-id (/ width 2) height)
   {:db (assoc-in db (path/canvas-content-root frame-id) {:id (str (random-uuid))})}))

(re-frame/reg-event-fx
 ::update
 (fn [{db :db} [_ frame-id]]
   (update-content instance (vals (get-in db (path/canvas-content frame-id))))
   {}))

(re-frame/reg-event-fx
 ::add-event
 (fn [{db :db} [_ frame-id event pos]]
   (let [id (str (random-uuid))]
     {:db (assoc-in db
                    (conj (path/canvas-content frame-id) id)
                    {:id id
                     :type :event
                     :state-id nil
                     :element-id (:id event)
                     :color config/default-color-events
                     :pos pos})
      :dispatch [::update frame-id]})))

(re-frame/reg-event-fx
 ::add-context
 (fn [{db :db} [_ frame-id event pos]]
   (let [id (str (random-uuid))]
     {:db (assoc-in db
                    (conj (path/canvas-content frame-id) id)
                    {:id id
                     :type :context
                     :state-id nil
                     :element-id (:id event)
                     :color config/default-color-contexts
                     :pos pos})
      :dispatch [::update frame-id]})))

(re-frame/reg-event-fx
 ::update-state-id
 (fn [{db :db} [_ id frame-id new-state-id state-update]]
   {:db (-> (assoc-in db (conj (path/canvas-content frame-id) id :state-id) new-state-id)
            (update-in (conj (path/canvas-content frame-id) id) merge state-update))
    :dispatch [::update frame-id]}))

(re-frame/reg-event-fx
 ::create-connection
 (fn [{db :db} [_ frame-id from to]]
   (let [connection-id (str (random-uuid))]
     {:db (assoc-in db (conj (path/canvas-content frame-id) connection-id)
                    {:id connection-id
                     :state-id (str (random-uuid))
                     :type :connection
                     :color config/default-color-connection
                     :from from
                     :to to})
      :dispatch [::update frame-id]})))

(re-frame/reg-event-fx
 ::delete
 (fn [{db :db} [_ frame-id id]]
   {:db (path/dissoc-in db (conj (path/canvas-content frame-id) id))
    :dispatch [::update frame-id]}))

(defn- render-guard [status]
  (= :new
     status))

(re-frame/reg-sub
 ::status
 (fn [db [_ path]]
   (get-in db (path/canvas-status path) :new)))

(re-frame/reg-event-fx
 ::open-figure
 (fn [{db :db} [_ frame-id row]]
   (let [parked? (some (fn [{id :id :as current}]
                         (when (= id (:id row))
                           current))
                       (get-in db (path/canvas-parked frame-id)))]
     (cond (and (not= (:id row) (get-in db (conj (path/canvas-content-root frame-id) :id)))
                (nil? parked?))
           {:db (-> (assoc-in db (path/canvas-content-root frame-id) row)
                    (update-in (path/canvas-parked frame-id)
                               (fnil conj [])
                               (get-in db (path/canvas-content-root frame-id))))
            :dispatch [::update frame-id]}
           (seq parked?)
           {:db (-> (assoc-in db (path/canvas-content-root frame-id) parked?)
                    (update-in (path/canvas-parked frame-id) (fn [parked]
                                                               (filterv (fn [{cur-id :id}]
                                                                          (not= (:id row) cur-id))
                                                                        parked))))
            :dispatch [::update frame-id]}))))

(re-frame/reg-event-fx
 ::open-empty-figure
 (fn [{db :db} [_ frame-id]]
   {:db (-> (assoc-in db (path/canvas-content-root frame-id) {:id (str (random-uuid))})
            (update-in (path/canvas-parked frame-id)
                       (fnil conj [])
                       (get-in db (path/canvas-content-root frame-id))))
    :dispatch [::update frame-id]}))

(re-frame/reg-event-fx
 ::open-figure-list
 (fn [{db :db} [_ frame-id id]]
   (let [found-figure (some (fn [{cur-id :id :as fig}]
                              (when (= id cur-id)
                                fig))
                            (get-in db (path/canvas-parked frame-id)))]
     (when-not found-figure
       (error "could not find figure" id))
     (when (seq found-figure)
       {:db (-> (assoc-in db (path/canvas-content-root frame-id) found-figure)
                (update-in (path/canvas-parked frame-id)
                           (fnil conj [])
                           (get-in db (path/canvas-content-root frame-id)))
                (update-in (path/canvas-parked frame-id) (fn [parked]
                                                           (filterv (fn [{cur-id :id}]
                                                                      (not= id cur-id))
                                                                    parked))))
        :dispatch [::update frame-id]}))))

(re-frame/reg-event-fx
 ::close-figure
 (fn [{db :db} [_ frame-id id active?]]
   (if active?
     {:db (-> (assoc-in db (path/canvas-content-root frame-id) (get-in db (conj (path/canvas-parked frame-id) 0) {:id (str (random-uuid))}))
              (update-in (path/canvas-parked frame-id) (fn [parked] (vec (rest parked)))))
      :dispatch [::update frame-id]}
     {:db (update-in db (path/canvas-parked frame-id) (fn [parked]
                                                        (filterv (fn [{cur-id :id}]
                                                                   (not= id cur-id))
                                                                 parked)))})))

(re-frame/reg-sub
 ::title
 (fn [db [_ frame-id default]]
   (get-in db (path/canvas-content-title frame-id) default)))

(re-frame/reg-sub
 ::open-figures
 (fn [db [_ frame-id]]
   (let [current
         (get-in db (path/canvas-content-root frame-id))
         parked-figures
         (get-in db (path/canvas-parked frame-id))]
     (->> (mapv (fn [{active? :active?
                      title :title
                      dirty? :dirty?
                      id :id
                      :or {title "no title (unsaved)"}}]
                  [id title dirty? active? (str (random-uuid))])
                (conj parked-figures
                      (assoc current :active? true)))
          (sort-by second)))))

(defn title [frame-id]
  [:div {:style {:position :absolute
                 :right 10
                 :box-shadow "0 4px 6px -1px var(--box-shadow-color, rgba(0, 0, 0, 0.1)), 0 2px 4px -2px var(--box-shadow-color, rgba(0, 0, 0, 0.1)), 0 -1px 1px 0 var(--box-shadow-color, rgba(0, 0, 0, 0.05))"
                 :top 10
                 :backgroundColor "#EEEEEE"
                 :border-radius 4
                 :padding 5}
         :on-double-click (fn []
                            (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.main-view/save-current-figure frame-id]))}
   [:h3 @(re-frame/subscribe [::title frame-id "no title (unsaved)"])]])

(defn open-figures [frame-id]
  (let [hover (reagent/atom nil)]
    (fn []
      [:div {:style {:position :absolute
                     :left (+ (* width 0.5) 10)
                     :box-shadow "0 4px 6px -1px var(--box-shadow-color, rgba(0, 0, 0, 0.1)), 0 2px 4px -2px var(--box-shadow-color, rgba(0, 0, 0, 0.1)), 0 -1px 1px 0 var(--box-shadow-color, rgba(0, 0, 0, 0.05))"
                     :border-radius 4
                     :bottom 10
                     :display :flex
                     :flex-direction :column
                     :backgroundColor "#F8F8F8"
                     :overflow :auto
                     :margin-top 15
                     :max-width 200
                     :max-height 200}}
       [:h3 {:style {:margin-bottom 10
                     :border-bottom "1px solid grey"
                     :padding 5
                     :display :flex
                     :flex-direction :row
                     :justify-content :space-between}}
        [:span {:style {:margin-right 15}} "Open figures"]
        [button {:title "new"
                 :start-icon :window-plus
                 :variant :tertiary
                 :size :small
                 :on-click (fn [e]
                             (re-frame/dispatch [::open-empty-figure frame-id])
                             (.stopPropagation e))}]]
       (reduce (fn [acc [id name dirty? active? hover-id]]
                 (conj acc [:div.jack-hover-figure-list
                            {:style (cond-> {:padding 4
                                             :display :flex
                                             :flex-direction :row
                                             :justify-content :space-between
                                             :align-items :center
                                             :cursor :pointer}
                                      active?
                                      (assoc :backgroundColor "#07575B"
                                             :color "#EEEEEE")
                                      (and hover-id (= hover-id @hover)
                                           (not active?))
                                      (assoc :backgroundColor "#C4DFE6"
                                             :color "#000000"))
                             :on-mouse-over (fn []
                                              (reset! hover hover-id))
                             :on-mouse-leave (fn []
                                               (reset! hover nil))
                             :on-click (fn [e]
                                         (when-not active?
                                           (re-frame/dispatch [::open-figure-list frame-id id]))
                                         (.stopPropagation e))}
                            [:span {:style {:padding-left 5
                                            :padding-right 5}}
                             name (when dirty? "*")]
                            [button (cond-> {:title "Close"
                                             :start-icon :close
                                             :variant :tertiary
                                             :on-click (fn [e]
                                                         (re-frame/dispatch [::close-figure frame-id id active?])
                                                         (.stopPropagation e))}
                                      active?
                                      (assoc :icon-params {:color :white
                                                           :color-important? true}))]]))
               [:<>]
               @(re-frame/subscribe [::open-figures frame-id]))])))

(defn- toolbar-div [frame-id]
  [:div {:style {:position :absolute
                 :right 10
                 :bottom 10}}
   [toolbar {:orientation :horizontal
             :tooltip-direction :update
             :extra-class ["right-2"]
             :items [[{:title "Connect-Tool (c)" :id "connect-tool" :icon :window-link
                       :active? draw/connection-active?
                       :on-click #(draw/toggle-connect-active?)}
                      {:title "Delete-Tool (d)" :id "delete-tool" :icon :trash
                       :active? draw/delete-active?
                       :on-click #(draw/toggle-delete-active?)}
                      #_{:title "Edit-Tool (e)" :id "edit-tool" :icon :edit
                         :active? draw/edit-active?
                         :on-click #(draw/toggle-edit-active?)}]
                     [{:title "Undo" :id "delete-tool" :icon :chevron-left
                       :disabled? true
                       :on-click #(re-frame/dispatch [::activate-connect-tool frame-id])}
                      {:title "Redo" :id "delete-tool" :icon :chevron-right
                       :disabled? true
                       :on-click #(re-frame/dispatch [::activate-connect-tool frame-id])}]
                     [{:title "Save" :id "save-figure-tool" :icon :save
                       :on-click #(re-frame/dispatch [:de.explorama.frontend.knowledge-editor.main-view/save-current-figure frame-id])}
                      #_{:title "Delete" :id "delete-figure-tool" :icon :trash
                         :on-click #(re-frame/dispatch [::activate-connect-tool frame-id])}]]}]])

(defn reagent-canvas [frame-id]
  (let [host-ref (atom nil)]
    (reagent/create-class {:display-name host-key
                           :reagent-render
                           (fn [frame-id]
                             [:<>
                              [title frame-id]
                              [:canvas.goose-canvas
                               (cond-> {:key host-key
                                        :ref host-key
                                        :id host-key
                                        :style {:width (/ width 2)
                                                :height height}
                                        :on-click #(re-frame/dispatch (fi/call-api :frame-bring-to-front-event-vec frame-id))})]
                              [toolbar-div frame-id]
                              [open-figures frame-id]])
                           :component-did-mount
                           (fn [_]
                             (let [status @(re-frame/subscribe [::status frame-id])]
                               (when (render-guard status)
                                 (re-frame/dispatch [::init frame-id]))))
                           :should-component-update
                           (fn [_ _ _]
                             (let [status @(re-frame/subscribe [::status frame-id])]
                               (render-guard status)))
                           :component-did-update
                           (fn [_ _]
                             (let [status @(re-frame/subscribe [::status frame-id])
                                   container (js/document.getElementById host-key)]
                               (when (nil? container)
                                 (warn "updating nil canvas" host-key))

                               (when (render-guard status)
                                 (re-frame/dispatch [::init frame-id]))))
                           :component-will-unmount
                           (fn [_]
                             (reset! instance {}))})))