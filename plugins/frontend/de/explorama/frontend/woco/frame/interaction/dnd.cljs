(ns de.explorama.frontend.woco.frame.interaction.dnd
  (:require [clojure.set :as set]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-id]]
            [goog.events :as events]
            [re-frame.core :refer [dispatch reg-event-fx subscribe] :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :refer-macros [error]]
            [de.explorama.frontend.woco.api.interaction-mode :as inter-mode]
            [de.explorama.frontend.woco.api.product-tour :as product-tour]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.interaction.collision :refer [filter-visible-frames]]
            [de.explorama.frontend.woco.frame.interaction.connection :as iac-conn]
            [de.explorama.frontend.woco.frame.interaction.move :refer [move-event-call moving-data
                                                                       moving-state is-resizing?]]
            [de.explorama.frontend.woco.frame.interaction.snapping :refer [handle-snapping
                                                                           reset-snapping snapping-lines
                                                                           snap-to-grid]]
            [de.explorama.frontend.woco.frame.interaction.z-order :as z-order]
            [de.explorama.frontend.woco.frame.size-position :refer [frame-position-sub]]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]
            [de.explorama.frontend.woco.navigation.panning-handler :as panning-handler]
            [de.explorama.frontend.woco.navigation.snapping :as snapping]
            [de.explorama.frontend.woco.workspace.background :as background]
            [de.explorama.frontend.woco.workspace.config :as wwconfig]
            [de.explorama.frontend.woco.workspace.math :refer [collide-rects point-inside-hitboxes?]]
            [de.explorama.frontend.woco.workspace.states :as wws]
            [de.explorama.frontend.woco.workspace.multiselect :as wwms]
            [de.explorama.frontend.woco.workspace.window-creation :as wwc])
  (:import [goog.events EventType]))

(defonce vertical-drag-state (r/atom nil))

(defn reset-vertical-drag []
  (reset! vertical-drag-state nil))

(defn vertical-drag-data
  ([state]
   (reset! vertical-drag-state state))
  ([]
   @vertical-drag-state))

;; indicates if drop-zone from frame should be visible
(defonce drop-zone-state (r/atom {}))

(defn activate-dropzone
  "Activates drop-zone for frame"
  [frame-id]
  (swap! drop-zone-state assoc frame-id true))

(defn deactivate-dropzone
  "Deactivates drop-zone for frame"
  ([frame-id]
   (swap! drop-zone-state dissoc frame-id))
  ([]
   (reset! drop-zone-state {})))

(defn show-dropzone-sub
  "Returns an cursor to specific frame-id so the sub will only be triggered when state of frame-id is changed"
  [frame-id]
  (r/cursor drop-zone-state [frame-id]))

(defn- clear-drag-shadow
  "Removes default drag shadow"
  [e]
  (try
    (let [temp-elem (js/document.createElement "div")]
      (-> (aget e "dataTransfer")
          (.setDragImage temp-elem 0 0)))
    (catch :default e
      (error "Error while setting drag shadow" e)
      nil)))

(defn abort
  "Will be fired when canceling event bubbleing and panning"
  [e]
  (.stopPropagation e)
  (navigation-control/stop-panning))

;;------- Dropping --------

(defn- frame-drop-on-frame
  "Will be fired when dropping a frame on a frame"
  [source-frame-id target-frame-id commit-move-data e]
  (.preventDefault e)
  (when (not= target-frame-id source-frame-id)
    (.stopPropagation e)
    (let [on-drop @(subscribe [::evts/on-drop target-frame-id])
          on-drop-data {:from source-frame-id
                        :to target-frame-id
                        :drop-event (aget e "nativeEvent")
                        :commit-move-data commit-move-data}
          default-drop-event [::iac-conn/connection-negotiation on-drop-data]]
      (cond
        (and on-drop (fn? on-drop))
        (on-drop (assoc on-drop-data
                        :default-drop-event default-drop-event))
        (and on-drop (keyword? on-drop))
        (dispatch [on-drop (assoc on-drop-data
                                  :default-drop-event default-drop-event)])
        :else
        (dispatch default-drop-event)))))

;;------- Dragging --------

(defn activate-dropzone?
  "Checks if dropzone should be activated"
  [source-frame-id]
  (let [source-frame-di @(fi/call-api :frame-info-api-value-sub source-frame-id :di)
        published-by-frame-id @(fi/call-api :frame-published-by-sub source-frame-id)
        published-by-frame-di @(fi/call-api :frame-info-api-value-sub published-by-frame-id :di)]
    (boolean
     ;Show drop-zone, when datainstance is set on drag-frame or src-frame
     (or (and source-frame-id
              source-frame-di)
         (and published-by-frame-id
              published-by-frame-di)))))

(defn- build-additional-hit-boxes
  "Prepares custom hit boxes for global and workspace context.
   The drag-enter/leave functions will be called when frame is dragged to the node with dom-id and
   the flag :save? will indicate if the result will be used for next iteration"
  []
  (when-let [additional-dropzones @(fi/call-api :service-category-sub :frame-drop-hitbox)]
    (reduce (fn [acc [_ {:keys [dom-ids id on-drop is-on-top? on-drag-enter on-drag-leave global-context? default-connect?]}]]
              (reduce (fn [acc dom-id]
                        (let [{:keys [left top width height]} (bounding-rect-id dom-id)
                              on-drag-leave (when (fn? on-drag-leave)
                                              (fn [f-id]
                                                (on-drag-leave dom-id f-id)
                                                {:save? true
                                                 :leaved? true}))
                              hb-data {:coords [left top]
                                       :full-size [width height]
                                       :dom-id dom-id}
                              hb-data (cond-> hb-data
                                        (fn? on-drag-enter)
                                        (assoc :on-drag-enter (fn [f-id over?]
                                                                (cond-> (assoc hb-data
                                                                               :save? true
                                                                               :default-connect? default-connect?)
                                                                  id (assoc :id id)
                                                                  is-on-top? (assoc :is-on-top? is-on-top?)
                                                                  (fn? on-drop) (assoc :on-drop (partial on-drop dom-id))
                                                                  (and on-drag-enter (not over?))
                                                                  (assoc :on-drag-enter #(on-drag-enter dom-id f-id))
                                                                  on-drag-leave (assoc :on-drag-leave on-drag-leave)))))]

                          (cond-> acc
                            (and global-context? left (or (fn? on-drag-enter) (fn? on-drag-leave)))
                            (update :drag-hbs conj hb-data)
                            (and (not global-context?) left (or (fn? on-drag-enter) (fn? on-drag-leave)))
                            (update :drag-wsp-hbs conj hb-data)
                            (and global-context? left (fn? on-drop))
                            (update :drop-hbs conj hb-data)
                            (and (not global-context?) left (fn? on-drop))
                            (update :drop-wsp-hbs conj hb-data))))
                      acc
                      dom-ids))
            {;On global context 
             :drag-hbs []
             :drop-hbs []
             ;On workspace content (respects pan/zoom)
             :drag-wsp-hbs []
             :drop-wsp-hbs []}
            additional-dropzones)))


(defn- check-hbs
  "Checks if point x,y is inside of some given hitbox and fires drag-enter oder leave"
  [source-frame-id ^number px ^number py hbs dragging-over-id on-drag-leave]
  (let [{:keys [on-drag-enter dom-id]} (point-inside-hitboxes? px py hbs [:on-drag-enter :dom-id])
        dragging-over? (when dragging-over-id
                         (= dragging-over-id dom-id))]
    (cond
      (and (not dragging-over?) on-drag-leave)
      (on-drag-leave source-frame-id)
      (and (not dragging-over?) on-drag-enter)
      (on-drag-enter source-frame-id)
      dragging-over?
      (on-drag-enter source-frame-id true))))

(defn- precompile-check-collisions-fn
  "Precompiles the fn to check if some collision exists. 
   Currently there are 3 possible collisions:
   drag-hbs: custom registered hitboxes in global context (= related to whole page)
   drag-wsp-hbs: custom registered hitboxes in workspace context (= related to current pan/zoom)
   frames: automatically checks if frames overlaps (for connecting etc.) and handles dropzone-highlighting"
  [source-frame-id
   {^number workspace-zoom :z
    ^number wsp-x :x
    ^number wsp-y :y
    :as wsp}
   ^number px ^number py
   ^number start-x ^number start-y
   drag-hbs
   drag-wsp-hbs
   ^boolean dropping-possible?]
  (let [{:keys [width height]} @(subscribe [:de.explorama.frontend.woco.page/workspace-rect])
        viewport-width (/ width workspace-zoom)
        viewport-height (/ height workspace-zoom)
        viewport-x (- (/ wsp-x workspace-zoom))
        viewport-y (- (/ wsp-y workspace-zoom))
        filter-frames-fn (partial filter-visible-frames viewport-x viewport-y viewport-width viewport-height #{source-frame-id})
        visible-frames (-> @(subscribe [::evts/frames])
                           vals
                           filter-frames-fn)
        droppable-frames (when dropping-possible?
                           (-> visible-frames
                               (evts/ordered-consumer-frames
                                #{source-frame-id}
                                (when-not (vertical-drag-data) #{source-frame-id}))))
        base-frame @(subscribe [::evts/frame source-frame-id])
        ;Calculate offset to Mousepos in frame for checking mouse-position
        ^number offset-x (- (/ (- px wsp-x)
                               workspace-zoom)
                            start-x)
        ^number offset-y (- (/ (- py wsp-y config/explorama-header-height)
                               workspace-zoom)
                            start-y)
        multi-selection? (wws/multi-selection?)]
     ;;precalculate for faster collision detection
    (fn [^number px ^number py
         ^number new-x ^number new-y
         ^boolean handle-dropzone?
         {dragging-over-id :dom-id
          :keys [is-on-top? on-drag-leave]
          :as dragging-over}
         ^boolean force-framecheck?]
      (let [^number new-drag-x (+ new-x offset-x)
            ^number new-drag-y (+ new-y offset-y)
            global-check-result (when (and (not force-framecheck?)
                                           (seq drag-hbs))
                                  (check-hbs source-frame-id px py drag-hbs dragging-over-id on-drag-leave))
            wsp-check-result (when (and (not force-framecheck?)
                                        (not global-check-result)
                                        (seq drag-wsp-hbs))
                               (check-hbs source-frame-id new-drag-x new-drag-y drag-wsp-hbs dragging-over-id on-drag-leave))
            _ (when handle-dropzone?
                (deactivate-dropzone))
            frames-check-result (if-let [f (when (and (not is-on-top?)
                                                      (or force-framecheck?
                                                          (and (or (not global-check-result)
                                                                   (:id global-check-result))
                                                               (or (not wsp-check-result)
                                                                   (:id wsp-check-result)))))
                                                          ;(not dragging-over-id)))
                                             (point-inside-hitboxes? new-drag-x new-drag-y droppable-frames [:ignore-drop-on-frame? :id]))]
                                  (do
                                    (when (and handle-dropzone?
                                               (not (:ignore-drop-on-frame? f))
                                               (not= source-frame-id (:id f)))
                                      (activate-dropzone (:id f)))
                                    (cond-> f
                                      (and (not (:ignore-drop-on-frame? f))
                                           (or global-check-result wsp-check-result))
                                      (assoc :ignore-global? true)))
                                  false)
            global-check-result (when-not (:ignore-global? frames-check-result)
                                  global-check-result)
            leave-condition (and on-drag-leave
                                 (or (:ignore-global? frames-check-result)
                                     (and (not= (:id dragging-over)
                                                (:id frames-check-result))
                                          (not= (:id global-check-result)
                                                (:id frames-check-result)))))]
        (when leave-condition
          (on-drag-leave source-frame-id))
        (when (and global-check-result
                   (not leave-condition)
                   (:on-drag-enter global-check-result))
          ((:on-drag-enter global-check-result)))
        (when (or global-check-result
                  wsp-check-result
                  frames-check-result)
          (reset-snapping))
        (when-not multi-selection?
          (let [overwrite-pos (handle-snapping base-frame new-x new-y visible-frames wsp)]
            (cond-> (or global-check-result
                        wsp-check-result
                        frames-check-result
                        {})
              leave-condition (assoc :reset-dragging-over? true)
              (and overwrite-pos (seq overwrite-pos)) (assoc :overwrite-pos overwrite-pos))))))))

(defn- calc-sticky-frame-ids [move-frame-ids]
  (let [is-move-frame-id? (set move-frame-ids)
        check-positions (reduce (fn [acc frame-id]
                                  (let [frame-position (-> (select-keys @(subscribe [::evts/frame frame-id])
                                                                        [:full-size :coords :stick-to-frames?])
                                                           (assoc :minimized? @(subscribe [::evts/is-minimized? frame-id])))]
                                    (cond-> acc
                                      (not (:stick-to-frames? frame-position))
                                      (conj (dissoc frame-position :stick-to-frames?)))))
                                []
                                move-frame-ids)
        collide-with-frames? (fn [{[x y] :coords
                                   [w h] :full-size}]
                               (some (fn [{[mx my] :coords
                                           [mw mh] :full-size
                                           minimized? :minimized?}]
                                       (let [mh (if minimized?
                                                  config/header-height
                                                  mh)]
                                         (collide-rects x y w h mx my mw mh)))
                                     check-positions))]
    (reduce (fn [acc [frame-id {:keys [stick-to-frames?] :as frame-desc}]]
              (cond-> acc
                (and stick-to-frames?
                     (not (is-move-frame-id? frame-id))
                     (collide-with-frames? frame-desc))
                (conj frame-id)))
            move-frame-ids
            @(subscribe [::evts/frames]))))

(defn- drag-frame-start [source-frame-id e frame-type]
  (navigation-control/stop-panning)
  (moving-state false true)
  (if (or @(subscribe [::evts/is-maximized? source-frame-id])
          (and (= frame-type :frame/content-type)
               @(subscribe [::inter-mode/read-only?
                            {:component (:vertical source-frame-id)
                             :additional-info :move}])))
    (.preventDefault e)
    (let [px (aget e "pageX")
          py (aget e "pageY")
          [x y] @(frame-position-sub source-frame-id)
          {workspace-zoom :z :as position} @(subscribe [::navigation-control/position])
          dropping-possible? (or (activate-dropzone? source-frame-id)
                                 (:overwrite-drop-cond? (vertical-drag-data)))
          in-selection? (boolean (@wws/multiselect-current-selection source-frame-id))
          coupled-with-init @(subscribe [:de.explorama.frontend.woco.api.couple/couple-with source-frame-id])
          coupled-with-init (if (and (wws/multi-selection?)
                                     in-selection?)
                              (if coupled-with-init
                                (set/union (set coupled-with-init)
                                           @wws/multiselect-current-selection)
                                @wws/multiselect-current-selection)
                              coupled-with-init)
          coupled-with (calc-sticky-frame-ids (if (seq coupled-with-init)
                                                coupled-with-init
                                                [source-frame-id]))

          {:keys [drag-hbs drop-hbs drag-wsp-hbs drop-wsp-hbs] :as r} (build-additional-hit-boxes)
          check-collisions-fn (when (and (not in-selection?)
                                         (not coupled-with-init))
                                (precompile-check-collisions-fn source-frame-id position px py x y drag-hbs drag-wsp-hbs dropping-possible?))]
      (moving-state true)
      (reset! wws/multiselect-bb-before-move @wws/multiselect-bb)
      (when-not in-selection?
        (dispatch [::wwms/selection-finish nil]))
      (dispatch [:de.explorama.frontend.woco.workspace.background/set-minified-frames])
      (dispatch [::z-order/bring-to-front source-frame-id])
      (moving-data source-frame-id {:new-x x
                                    :new-y y
                                    :diff-x 0
                                    :diff-y 0
                                    :start-px px
                                    :start-py py
                                    :workspace-zoom workspace-zoom
                                    :coupled-with coupled-with
                                    :check-collisions-fn check-collisions-fn
                                    :c 0
                                    :dragging-over nil
                                    :drag-hbs drag-hbs
                                    :drop-hbs drop-hbs
                                    :drag-wsp-hbs drag-wsp-hbs
                                    :drop-wsp-hbs drop-wsp-hbs}))))

(defn- temporary-move
  "Non persistent move of frame, which can be canceled with resetting the moving-data for this frame"
  [frame-id
   ^number px ^number py
   ^number diff-x ^number diff-y
   {:keys [^number workspace-zoom ^number c check-collisions-fn dragging-over] :as  old-moving-data}
   ^boolean is-source-frame?
   ^boolean ignore-move?]
  (let [[x y] @(frame-position-sub frame-id)
        new-x (if (= c 0)
                x
                (+ x (/ diff-x workspace-zoom)))
        new-y (if (= c 0)
                y
                (+ y (/ diff-y workspace-zoom)))
        {:keys [^boolean save? ^boolean reset-dragging-over?]
         [^number overwrite-pos-x ^number overwrite-pos-y ^number diff-x-offset ^number diff-y-offset] :overwrite-pos
         :as dragging-over}
        (when (and is-source-frame? (fn? check-collisions-fn))
          (check-collisions-fn px py new-x new-y true dragging-over))]
    (moving-data frame-id
                 (cond-> (assoc old-moving-data
                                :diff-x diff-x
                                :diff-y diff-y
                                :c (cond-> c
                                     (= 0 c)
                                     (inc)))
                   diff-x-offset
                   (-> (update :start-px + diff-x-offset)
                       (update :diff-x - diff-x-offset))
                   diff-y-offset
                   (-> (update :start-py + diff-y-offset)
                       (update :diff-y - diff-y-offset))
                   ignore-move?
                   (assoc :hidden-new-x new-x
                          :hidden-new-y new-y)
                   (and (not ignore-move?)
                        (not overwrite-pos-x))
                   (assoc :new-x new-x)
                   (and (not ignore-move?)
                        (not overwrite-pos-y))
                   (assoc :new-y new-y)
                   (and (not ignore-move?)
                        overwrite-pos-x)
                   (assoc :new-x overwrite-pos-x)
                   (and (not ignore-move?)
                        overwrite-pos-y)
                   (assoc :new-y overwrite-pos-y)
                  ; Represents the hitbox where the frame is dragging-over
                   save? (assoc :dragging-over dragging-over)
                   reset-dragging-over? (assoc :dragging-over nil)))
    (background/draw-temp-edges)))

(defn- reinit-multiselect-box []
  (when @wws/multiselect-bb
    (swap! wws/multiselect-bb
           (fn [{:keys [min-x min-y max-x max-y
                        start-min-x start-min-y
                        start-max-x start-max-y] :as v}]
             (assoc v
                    :start-min-x (or min-x start-min-x)
                    :start-min-y (or min-y start-min-y)
                    :start-max-x (or max-x start-max-x)
                    :start-max-y (or max-y start-max-y)
                    :min-x nil
                    :min-y nil
                    :max-x nil
                    :max-y nil))))
  (reset! wws/multiselect-bb-before-move nil))

(defn- move-multiselect-box [diff-x diff-y workspace-zoom]
  (when @wws/multiselect-bb
    (swap! wws/multiselect-bb
           (fn [{:keys [^number start-min-x
                        ^number start-max-x
                        ^number start-min-y
                        ^number start-max-y]
                 :as v}]
             (let [new-x (+ start-min-x (/ diff-x workspace-zoom))
                   new-y (+ start-min-y (/ diff-y workspace-zoom))]
               (assoc v
                      :min-x new-x
                      :min-y new-y
                      :max-x (+ new-x (- start-max-x start-min-x))
                      :max-y (+ new-y (- start-max-y start-min-y))))))))

(defn- dragging-move
  "Moves the dragging frame and if exists all coupled frames"
  [e]
  (let [[source-frame-id] (first (moving-data))]
    (when source-frame-id
      (let [^number px (aget e "pageX")
            ^number py (aget e "pageY")
            {:keys [^number start-px ^number start-py coupled-with]
             ^number old-diff-x :diff-x
             ^number old-diff-y :diff-y
             ^number workspace-zoom :workspace-zoom
             :as moving-data}
            (moving-data source-frame-id)
            ^number diff-x (- px start-px)
            ^number diff-y (- py start-py)]
        (move-multiselect-box diff-x diff-y workspace-zoom)
        (when (or (not= old-diff-x diff-x)
                  (not= old-diff-y diff-y))
          (doseq [frame-id  (or coupled-with [source-frame-id])]
            (temporary-move frame-id px py diff-x diff-y moving-data (= frame-id source-frame-id) false)))))))

(defn- drag-frame-end
  "Will be fired when the dragging of frame ended.
   Also handles the dropping on frames or hitboxes if there is a collision"
  [e]
  (let [^number px (aget e "pageX")
        ^number py (aget e "pageY")
        [source-frame-id {:keys [new-x new-y check-collisions-fn dragging-over] :as commit-move-data}]
        (cond-> (moving-data)
          :always first
          (snapping/snapping? :grid) (snap-to-grid move-multiselect-box))
        {:keys [on-drop default-connect?]} dragging-over
        {target-frame-id :id :keys [ignore-drop-on-frame?]} (when (and (or default-connect? (not on-drop))
                                                                       (fn? check-collisions-fn))
                                                              (check-collisions-fn px py new-x new-y true nil true))]
    (cond
      (and (and source-frame-id target-frame-id default-connect?)
           (not @wws/multiselect-bb))
      (do
        (when (fn? on-drop) (on-drop source-frame-id))
        (frame-drop-on-frame source-frame-id target-frame-id commit-move-data e)
        (moving-state false true))

      (and source-frame-id (fn? on-drop)
           (not @wws/multiselect-bb))
      (do
        (on-drop source-frame-id)
        (moving-state false true))

      (and source-frame-id target-frame-id (not ignore-drop-on-frame?)
           (not @wws/multiselect-bb))
      (frame-drop-on-frame source-frame-id target-frame-id commit-move-data e)

      source-frame-id
      (do
        (move-event-call source-frame-id commit-move-data e)
        (moving-state false true)))
    (reinit-multiselect-box)
    (deactivate-dropzone)
    (reset-snapping)))

(def ^:private mouse-down-state (atom nil))

(defn- moved? [e [start-x start-y]]
  (< 3 (Math/sqrt (+ (Math/pow (- (aget e "pageX") start-x)
                               2)
                     (Math/pow (- (aget e "pageY") start-y)
                               2)))))

(defn drag-props
  "Properties to handle dragging/dropping frame"
  [source-frame-id frame-type]
  (let [is-maximized? @(subscribe [::evts/is-maximized? source-frame-id])
        read-only? (when (= frame-type :frame/content-type)
                     @(subscribe [::inter-mode/read-only?
                                  {:component (:vertical source-frame-id)
                                   :additional-info :move}]))]
    (cond-> {:on-mouse-up #(do
                             (reset! mouse-down-state nil)
                             (drag-frame-end %))
             :on-click (fn [e]
                         (let [e (.-nativeEvent ^js e)]
                           (when (and (.-ctrlKey e)
                                      (wwconfig/select-event? e))
                             (dispatch [:de.explorama.frontend.woco.workspace.multiselect/selection-strg source-frame-id]))))
             :on-mouse-down (fn [e]
                              (if (and (not is-maximized?)
                                       (not read-only?)
                                       (not (.-ctrlKey e))
                                       (wwconfig/select-event? (aget e "nativeEvent")))
                                (reset! mouse-down-state [(aget e "pageX")
                                                          (aget e "pageY")])
                                (reset! mouse-down-state nil)))
             :on-mouse-move (fn [e]
                              (when (and @mouse-down-state
                                         (moved? (aget e "nativeEvent")
                                                 @mouse-down-state))
                                (drag-frame-start source-frame-id e frame-type)
                                (.preventDefault e)
                                (.stopPropagation e)))
             :on-mouse-leave #(reset! mouse-down-state nil)
             :on-context-menu (fn [e]
                                (when (= :yes (navigation-control/ignore-context-menu?))
                                  (.preventDefault e)
                                  (.stopPropagation e)
                                  (navigation-control/set-ignore-context-menu false)))}
      (not is-maximized?) (assoc :style {:cursor config/can-move-frame-cursor}))))

;;--------------- Vertical drag (like mosaic group drag) --------------

(defn vertical-drag-start [{:keys [e frame-id] :as drag-infos}]
  (vertical-drag-data (dissoc drag-infos :e))
  (drag-frame-start frame-id e nil))

(defn- vertical-dragging
  "Checking collision when using vertical drag"
  [e]
  (let [[source-frame-id] (first (moving-data))]
    (when source-frame-id
      (let [^number px (aget e "pageX")
            ^number py (aget e "pageY")
            {:keys [^number start-px ^number start-py]
             ^number old-diff-x :diff-x
             ^number old-diff-y :diff-y
             :as moving-data}
            (moving-data source-frame-id)
            ^number diff-x (- px start-px)
            ^number diff-y (- py start-py)]
        (when (or (not= old-diff-x diff-x)
                  (not= old-diff-y diff-y))
          (temporary-move source-frame-id px py diff-x diff-y moving-data true true))))))

(defn- vertical-dragging-end
  "Will be fired when ending vertical drag or dropping it on frame"
  [e]
  (let [px (aget e "pageX")
        py (aget e "pageY")
        [source-frame-id {:keys [hidden-new-x hidden-new-y check-collisions-fn dragging-over]}] (first (moving-data))
        {:keys [on-drop default-connect?]} dragging-over
        {target-frame-id :id :keys [ignore-drop-on-frame?]} (when (and (or default-connect? (not on-drop))
                                                                       (fn? check-collisions-fn))
                                                              (check-collisions-fn px py hidden-new-x hidden-new-y true nil true))
        dragging-infos (vertical-drag-data)]

    (cond
      (and dragging-infos
           target-frame-id
           (not= target-frame-id source-frame-id)
           (not ignore-drop-on-frame?))
      (let [on-drop @(subscribe [::evts/on-drop target-frame-id])
            on-drop-data {:from {:drag-and-drop? true
                                 :drag-infos dragging-infos}
                          :to target-frame-id
                          :drop-event e}
            default-drop-event [::iac-conn/connection-negotiation on-drop-data]]

        (cond
          (and on-drop (fn? on-drop))
          (on-drop (assoc on-drop-data
                          :default-drop-event default-drop-event))
          (and on-drop (keyword? on-drop))
          (dispatch [on-drop (assoc on-drop-data
                                    :default-drop-event default-drop-event)])
          :else
          (dispatch default-drop-event)))
      (and dragging-infos
           source-frame-id)
      (dispatch [::trigger-mouse-up-service e dragging-infos]))
    (.preventDefault e)
    (.stopPropagation e)
    (moving-state false true)))

(reg-event-fx
 ::trigger-mouse-up-service
 (fn [{db :db} [_ event dragging-infos]]
   (when dragging-infos
     (let [page-x (aget event "pageX")
           page-y (aget event "pageY")
           [create-frame-x create-frame-y] (navigation-control/page->workspace db [page-x page-y])
           mouse-up-listeners (vals (registry/lookup-category db :workspace-on-mouse-up))]
       (moving-state false)
       {:dispatch-n (mapv (fn [listener]
                            (conj listener create-frame-x create-frame-y event))
                          mouse-up-listeners)}))))

(defn dragging-overlay-comp [is-panning? is-moving? vertical-drag? animating? creating-new-windows?]
  (let [listener (atom nil)]
    (r/create-class
     {:component-did-mount (fn [_]
                             (reset! listener
                                     (events/listen js/window EventType.KEYDOWN
                                                    (fn [e]
                                                      (when (= "Escape" (aget e "key"))
                                                        (when creating-new-windows?
                                                          (re-frame/dispatch [::product-tour/previous-step true])
                                                          (reset! wws/window-creation-mouse nil)))))))
      :component-will-unmount (fn [_]
                                (events/unlistenByKey @listener))
      :reagent-render
      (fn [is-panning? is-moving? vertical-drag? animating? creating-new-windows?]
        (let [contextmenu-open?  @(subscribe [:de.explorama.frontend.woco.components.context-menu/infos])]
          [:<>
           [:div (cond-> {:style {:cursor (cond
                                            creating-new-windows? (wwc/cursor)
                                            vertical-drag? config/copy-cursor
                                            is-panning? config/panning-cursor
                                            is-moving? config/move-frame-cursor)}
                          :on-mouse-move (fn [e]
                                           (.preventDefault e)
                                           (.stopPropagation e)
                                           (when-not contextmenu-open?
                                             (cond
                                               vertical-drag? (vertical-dragging e)
                                               is-panning? (panning-handler/mouse-move (aget e "nativeEvent"))
                                               is-moving? (dragging-move e))))
                          :on-mouse-leave (fn [e]
                                            (reset! mouse-down-state nil)
                                            (when-not is-moving?
                                              (moving-state false true)
                                              (reset! wws/multiselect-bb @wws/multiselect-bb-before-move)
                                              (reset! wws/multiselect-bb-before-move nil))
                                            (when is-panning?
                                              (panning-handler/mouse-leave (aget e "nativeEvent")))
                                            (reset-vertical-drag)
                                            (deactivate-dropzone))
                          :on-mouse-up (fn [e]
                                         (reset! mouse-down-state nil)
                                         (.preventDefault e)
                                         (.stopPropagation e)
                                         (cond
                                           creating-new-windows?
                                           (wwc/create-window (aget e "pageX")
                                                              (aget e "pageY"))

                                           vertical-drag? (vertical-dragging-end (aget e "nativeEvent"))
                                           is-panning? (panning-handler/mouse-up (aget e "nativeEvent"))
                                           is-moving? (drag-frame-end e))
                                         (reset-vertical-drag)
                                         (deactivate-dropzone)
                                         (dispatch [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]))
                          :on-context-menu (fn [e]
                                             (when (= :yes (navigation-control/ignore-context-menu?))
                                               (.preventDefault e)
                                               (navigation-control/set-ignore-context-menu false)))}
                   (not creating-new-windows?)
                   (assoc :class "window-drag-overlay")
                   creating-new-windows?
                   (assoc :class "window-placement-overlay"))
            (when creating-new-windows?
              (let [{:keys [dnd-card-top dnd-card-bottom]} @(subscribe [::i18n/translate-multi :dnd-card-top :dnd-card-bottom])]
                [:div.card
                 [:div.text-lg.mb-8 dnd-card-top]
                 [:div.text-xs dnd-card-bottom]]))]
           (when  (and (snapping/snapping? :frame)
                       is-moving?
                       (not vertical-drag?))
             [:div {:style {:user-select :none
                            :position :absolute
                            :top "0px"
                            :left "0px"
                            :bottom "0px"
                            :right "0px"
                            :overflow :hidden}}
              [snapping-lines]])]))})))

;;--------------- Overlay --------------

(defn dragging-overlay []
  (let [is-panning? (navigation-control/is-panning?)
        is-moving? (moving-state)
        resizing? (is-resizing?)
        vertical-drag? (vertical-drag-data)
        animating? @(subscribe [::navigation-control/animation-activated?])
        creating-new-windows? (wwc/creating-new-windows?)]
    (when (and (not resizing?)
               (or vertical-drag?
                   is-moving?
                   animating?
                   is-panning?
                   creating-new-windows?))
      [dragging-overlay-comp is-panning? is-moving? vertical-drag? animating? creating-new-windows?])))
