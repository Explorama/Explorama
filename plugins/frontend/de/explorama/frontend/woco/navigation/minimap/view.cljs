(ns de.explorama.frontend.woco.navigation.minimap.view
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.navigation.minimap.render :as mm-render]
            [reagent.core :as r]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.navigation.util :refer [z-factor]]
            ["@pixi/core"]))

(re-frame/reg-event-db
 ::toggle
 (fn [db [_ flag]]
   (cond-> db
     (boolean? flag) (assoc-in path/show-minimap? flag)
     (nil? flag) (update-in path/show-minimap? not))))

(re-frame/reg-sub
 ::show?
 (fn [db]
   (get-in db path/show-minimap? false)))

(defn- dom->minimap [state x y]
  (let [{mm-x :x mm-y :y mm-z :z} (mm-render/position state)]
    {:x (- (/ x mm-z)
           (/ mm-x mm-z))
     :y (- (/ y mm-z)
           (/ mm-y mm-z))}))

(defn- minimap->workspace-viewport [mm-x mm-y z {:keys [width height]}]
  (let [width (/ (/ width 2) z)
        height (/ (/ height 2) z)]
    {:x (- mm-x width)
     :y (- mm-y height)}))

(defn- move-to [state event z w-rect]
  (let [offset-x (aget event "nativeEvent" "offsetX")
        offset-y (aget event "nativeEvent" "offsetY")
        {mm-x :x mm-y :y} (dom->minimap state offset-x offset-y)
        {:keys [x y]} (minimap->workspace-viewport mm-x mm-y z w-rect)]
    (re-frame/dispatch [:de.explorama.frontend.woco.navigation.control/move-to x y])))

(defn- zoom-handler [event]
  (.preventDefault event)
  (.stopPropagation event)
  (re-frame/dispatch [:de.explorama.frontend.woco.navigation.control/center-zoom
                      {:z-factor (z-factor (aget event "wheelDelta"))}]))

(defn- canvas [x y z frames-infos w-rect]
  (let [canvas-id (str ::woco-navigation-minimap)
        state (atom {:host nil
                     :app nil
                     :frames {}
                     :viewport-container nil
                     :frames-container nil
                     :dragging? false})]
    (r/create-class {:display-name "woco minimap"
                     :reagent-render (fn [_ _ z _ w-rect]
                                       [:canvas
                                        {:key canvas-id
                                         :ref #(swap! state assoc :host %)
                                         :id canvas-id
                                         :on-mouse-down (fn [e]
                                                          (swap! state assoc :dragging? true)
                                                          (move-to state e z w-rect))
                                         :on-mouse-up #(swap! state assoc :dragging? false)
                                         :on-mouse-leave #(swap! state assoc :dragging? false)
                                         :on-mouse-move (fn [e]
                                                          (when (:dragging? @state)
                                                            (move-to state e z w-rect)))
                                         :style {:width config/minimap-width
                                                 :height config/minimap-height
                                                 :background-color "rgba(234, 240, 255, 0.4)"}}])
                     :component-did-mount (fn []
                                            (mm-render/init state)
                                            (mm-render/render-frames state frames-infos)
                                            (mm-render/render-viewport state x y z w-rect)
                                            (.addEventListener (:host @state)
                                                               "wheel"
                                                               zoom-handler
                                                               #js{:passive false}))
                     :should-component-update (fn [_
                                                   [_ ox oy oz oframes-infos ow-rect]
                                                   [_ nx ny nz nframes-infos nw-rect]]
                                                (or (not= ox nx)
                                                    (not= oy ny)
                                                    (not= oz nz)
                                                    (not= ow-rect nw-rect)
                                                    (not= oframes-infos nframes-infos)))
                     :component-did-update (fn [this [_ ox oy oz oframes-infos ow-rect]]
                                             (let [[_ nx ny nz nframes-infos nw-rect] (r/argv this)]
                                               (when (or (not= ox nx)
                                                         (not= ox nx)
                                                         (not= oy ny)
                                                         (not= oz nz)
                                                         (not= ow-rect nw-rect))
                                                 (mm-render/render-viewport state nx ny nz nw-rect))
                                               (when (or (not= oframes-infos nframes-infos)
                                                         (not= ow-rect nw-rect))
                                                 (mm-render/render-frames state nframes-infos))))

                     :component-will-unmount #(let [{:keys [app host]} @state]
                                                (.removeEventListener host "wheel" zoom-handler)
                                                (reset! state nil)
                                                (when app
                                                  (.destroy ^js app false (clj->js {:children true}))))})))

(defn minimap [x y z]
  (let [frames-infos @(re-frame/subscribe [:de.explorama.frontend.woco.frame.api/frames-positions])
        w-rect @(re-frame/subscribe [:de.explorama.frontend.woco.page/workspace-rect])]
    [canvas x y z frames-infos w-rect]))

(defn toggle [toggle-minimap?]
  (let [navigation-minimap @(re-frame/subscribe [::i18n/translate :navigation-minimap])]
    {:id "viewport-minimap"
     :title navigation-minimap
     :icon "icon-minimap"
     :on-click #(re-frame/dispatch [::toggle])
     :active? toggle-minimap?}))