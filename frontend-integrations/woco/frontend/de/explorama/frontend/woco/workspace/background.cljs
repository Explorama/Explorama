(ns de.explorama.frontend.woco.workspace.background
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.color :as frame-color]
            [de.explorama.frontend.woco.frame.interaction.move :as move]
            [de.explorama.frontend.woco.path :as path]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [debug]]))

(def ^:private host-key "background-canvas")
(defonce ^:private state (atom nil))

(def large-bg-idx 0)
(def medium-bg-idx 1)
(def small-bg-idx 2)
(def tiny-bg-idx 3)

(def tiling-sprite (atom nil))

(def minified-frames (atom nil))

(defn workspace-dims []
  (when-let [workspace-container (.getElementById js/document "frames-workspace")]
    (let [rect (.getBoundingClientRect workspace-container)]
      [(aget rect "width") (aget rect "height")])))

(defn- header-color-mapping [css-class]
  ;TODO r1/prototype-window-handling this is a hack. The color will be initially wrong (default value)
  (get
   {"explorama__window__group-1" 0x0c7c7c
    "explorama__window__group-2" 0xbe550f
    "explorama__window__group-3" 0x105aa3
    "explorama__window__group-4" 0xd13434
    "explorama__window__group-5" 0x0c7c44
    "explorama__window__group-6" 0xc19c28
    "explorama__window__group-7" 0x7b2885
    "explorama__window__group-8" 0xc82d88
    "explorama__window__group-9" 0x387c8d
    "explorama__window__group-10" 0x835130
    "explorama__window__group-11" 0x0a3a68
    "explorama__window__group-12" 0x6a0000
    "explorama__window__group-13" 0x08522d
    "explorama__window__group-14" 0x7f671a
    "explorama__window__group-15" 0xba4861}
   css-class
   0xBBBBBB))

(defn state-aware-size [fdesc [ws-width ws-height]]
  (cond (:is-maximized? fdesc)
        (let [{legend? :frame-open-legend} fdesc]
          (assoc fdesc
                 :size [(if legend?
                          (- ws-width config/legend-width)
                          ws-width)
                        ws-height]))
        (:is-minimized? fdesc)
        (assoc fdesc :size [(get-in fdesc [:size 0])
                            config/minimized-height])
        :else fdesc))

(re-frame/reg-event-fx
 ::set-minified-frames
 (fn [{db :db}]
   (if (get-in db path/show-connecting-edges?)
     (let [ws-dims (workspace-dims)
           frames  (map (fn [[fid fdesc]]
                          [fid (state-aware-size fdesc ws-dims)])
                        (get-in db path/frames))]
       (reset! minified-frames (map (fn [[fid fdesc]]
                                      [fid {:coords (:coords fdesc)
                                            :size (:size fdesc)
                                            :published-by-frame (:published-by-frame fdesc)}
                                       ;;TODO remarks header-color algorithms is a special case here, because it
                                       ;;has to use a different color for the connection from search.
                                       (if (= "algorithms" (:vertical fid))
                                         (if-let [color (frame-color/header-color db (:published-by-frame fdesc))]
                                           (header-color-mapping color)
                                           (header-color-mapping (frame-color/header-color db fid)))
                                         (header-color-mapping (frame-color/header-color db fid)))])
                                    frames)))
     (reset! minified-frames nil))
   {}))



(defn calc-active-tiling [z]
  (cond
    (>= 0.20 z) [[tiny-bg-idx 1]]
    (>= 0.25 z) [[tiny-bg-idx 0.9] [small-bg-idx 0.5]]
    (>= 0.30 z) [[tiny-bg-idx 0.2] [small-bg-idx 1]]
    (>= 0.40 z) [[small-bg-idx 1]]
    (>= 0.50 z) [[small-bg-idx 0.9] [medium-bg-idx 0.5]]
    (>= 0.60 z) [[small-bg-idx 0.2] [medium-bg-idx 1]]
    (>= 1.0 z) [[medium-bg-idx 1]]
    :else [[large-bg-idx 1]]))

(defn- bg-container [app]
  (.getChildAt (.-stage app) 0))

(defn- get-connecting-edges-container [app]
  (.getChildAt (.-stage app) 1))

(defn- center-pos [{[width height] :size [x y] :coords}]
  (when (and width height x y)
    [(+ x (/ width 2))
     (+ y (/ height 2))]))

(defn draw-edges [frames]
  (when-let [app (:app @state)]
    (let [g (.getChildAt (get-connecting-edges-container app) 0)
          center-window (into {} (map (fn [[fid fdesc]]
                                        [fid (center-pos fdesc)])
                                      frames))]
      (.clear g)
      (doseq [[_ fdesc color] frames]
        (let [source-frame-id (:published-by-frame fdesc)
              center (get center-window source-frame-id)
              window-center (center-pos fdesc)]
          (when (and (seq center)
                     (seq window-center))
            (.lineStyle g 2
                        color
                        1)
            (.moveTo g
                     (first center)
                     (second center))
            (.lineTo g
                     (first window-center)
                     (second window-center)))))
      (.render (.-renderer app) (.-stage app)))))

(defn draw-temp-edges []
  (when @minified-frames
    (let [updated-frames (map (fn [[fid fdesc color :as frame]]
                                (let [{move-x :new-x move-y :new-y} (move/moving-data fid)]
                                  (if (and move-x move-y)
                                    [fid (assoc fdesc :coords (list move-x move-y)) color]
                                    frame)))
                              @minified-frames)]
      (draw-edges updated-frames))))


(defn remove-edges []
  (let [app (:app @state)
        g (.getChildAt (get-connecting-edges-container app) 0)]
    (.clear g)
    (.render (.-renderer app) (.-stage app))))


(re-frame/reg-event-fx
 ::draw-connecting-edges
 (fn [{db :db}]
   (if (get-in db path/show-connecting-edges?)
     (let [ws-dims (workspace-dims)]
       (draw-edges (map (fn [[fid fdesc]]
                          [fid (state-aware-size fdesc ws-dims)
                           ;;TODO remarks header-color algorithms is a special case here, because it
                           ;;has to use a different color for the connection from search.
                           (if (= "algorithms" (:vertical fid))
                             (if-let [color (frame-color/header-color db (:published-by-frame fdesc))]
                               (header-color-mapping color)
                               (header-color-mapping (frame-color/header-color db fid)))
                             (header-color-mapping (frame-color/header-color db fid)))])
                        (get-in db path/frames))))
     (remove-edges))
   nil))


(defn- set-position
  "Sets the container position. When applied on stage it definies the viewport of minimap"
  ([cont x y z]
   (when cont
     (when x
       (aset cont "position" "x" x))
     (when y
       (aset cont "position" "y" y))
     (when z
       (aset cont "scale" "x" z)
       (aset cont "scale" "y" z))))
  ([cont x y]
   (set-position cont x y nil)))

(defn- set-size
  "Sets the container size"
  [cont width height]
  (when cont
    (when width
      (aset cont "width" width))
    (when height
      (aset cont "height" height))))

(defn get-z []
  (get @state :z))

(defn pan-zoom [x y z]
  (swap! state assoc :x x :y y :z z)
  (when (:app @state)
    (let [app (:app @state)
          [width height] (workspace-dims)
          active-sprites (calc-active-tiling z)
          active-sprites-set (into #{} (map first active-sprites))
          connecting-edges (get-connecting-edges-container app)]
      (aset (.getChildAt (bg-container (:app @state)) large-bg-idx) "visible" (boolean (active-sprites-set large-bg-idx)))
      (aset (.getChildAt (bg-container (:app @state)) medium-bg-idx) "visible" (boolean (active-sprites-set medium-bg-idx)))
      (aset (.getChildAt (bg-container (:app @state)) small-bg-idx) "visible" (boolean (active-sprites-set small-bg-idx)))
      (aset (.getChildAt (bg-container (:app @state)) tiny-bg-idx) "visible" (boolean (active-sprites-set tiny-bg-idx)))
      (set-position connecting-edges x y z)
      (doseq [[tiling-sprite-idx alpha] active-sprites]
        (let [tiling-sprite (.getChildAt (bg-container app) tiling-sprite-idx)]
          (set! (.-y (.-scale tiling-sprite)) z)
          (set! (.-x (.-scale tiling-sprite)) z)
          (set-size tiling-sprite (/ width z) (/ height z))
          (set! (.-alpha tiling-sprite) alpha)
          (set! (.-x (.-tilePosition tiling-sprite)) (/ x z))
          (set! (.-y (.-tilePosition tiling-sprite)) (/ y z))))
      (.render (.-renderer app)
               (.-stage app)))))


(re-frame/reg-event-fx
 ::reset
 (fn [{db :db}]
   (when-let [app (:app @state)]
     (let [g (.getChildAt (get-connecting-edges-container app) 0)]
       (.clear g)
       (.render (.-renderer app) (.-stage app)))
     (pan-zoom (:x config/default-position)
               (:y config/default-position)
               (:z config/default-position)))
   nil))

(defn resize []
  (when-let [app (:app @state)]
    (let [z (:z @state)
          [width height] (workspace-dims)
          active-sprites (calc-active-tiling z)]
      (doseq [[tiling-sprite-idx _] active-sprites]
        (set-size (.getChildAt (bg-container app) tiling-sprite-idx)
                  (/ width z) (/ height z)))
      (.resize (.-renderer app) width height)
      (.render (.-renderer app) (.-stage app)))))

(defn- texture
  "Get texture with given id"
  [texture-id]
  (aget js/PIXI.utils.TextureCache texture-id))

(defn- texture-cached?
  "Checks if a texture is cached"
  [texture-id]
  (boolean (texture texture-id)))

(defn- cache-texture [id fname]
  (when-not (texture-cached? id)
    (let [temp-elem (js/document.createElement "img")]
      (.setAttribute temp-elem "src" fname)
      (.setAttribute temp-elem "style" "{display: none}")
      (js/document.body.appendChild temp-elem)
      (js/PIXI.Texture.addToCache (js/PIXI.Texture.from temp-elem)
                                  id)

      (js/document.body.removeChild temp-elem))))

(defn load-resources
  "Loads relevant resources for drawing like frame-colors and frame-icons"
  []
  (debug "load background resources")
  (doseq [[id fname]
          [["bg-grid-100" "img/bg-grid-100.png"]
           ["bg-grid-50" "img/bg-grid-50.png"]
           ["bg-grid-25" "img/bg-grid-25.png"]
           ["bg-grid-0" "img/bg-grid-0.png"]]]
    (cache-texture id fname)))

(defn canvas []
  (let [bg-container (js/PIXI.Container.)]
    (reagent/create-class
     {:display-name host-key
      :reagent-render
      (fn []
        (let [current-theme (when-let [sub (fi/call-api :config-theme-sub)] @sub)
              alpha (case current-theme
                      :light 0.05
                      :dark 0.1
                      0.05)]
          (aset bg-container "alpha" alpha)
          [:canvas {:id host-key
                    :style {:position :absolute
                            :top 0
                            :bottom 0
                            :left 0
                            :right 0
                            :width "100%"
                            :height "100%"}}]))
      :component-did-mount
      (fn [_]
        (let [pixi-canvas (.getElementById js/document host-key)
              [width height] (workspace-dims)
              app (js/PIXI.Application. (clj->js {:autoStart false
                                                  :width width
                                                  :height height
                                                  :antialias true
                                                  :roundPixels true
                                                  :resolution 2
                                                  :backgroundAlpha 0
                                                  :sharedTicker false
                                                  :autoDensity true
                                                  :forceCanvas true
                                                  :view pixi-canvas
                                                  :scaleMode js/PIXI.SCALE_MODES.NEAREST}))
              connecting-edges-container (js/PIXI.Container.)]
          (.addEventListener js/window "resize" resize)
          (aset bg-container "name" "grid")
          (.addChildAt (.-stage app) bg-container 0)
          (.addChildAt bg-container
                       (js/PIXI.TilingSprite. (texture "bg-grid-100"))
                       large-bg-idx)
          (.addChildAt bg-container
                       (js/PIXI.TilingSprite. (texture "bg-grid-50"))
                       medium-bg-idx)
          (.addChildAt bg-container
                       (js/PIXI.TilingSprite. (texture "bg-grid-25"))
                       small-bg-idx)
          (.addChildAt bg-container
                       (js/PIXI.TilingSprite. (texture "bg-grid-0"))
                       tiny-bg-idx)
          (.addChildAt connecting-edges-container (js/PIXI.Graphics.) 0)
          (.addChildAt (.-stage app) connecting-edges-container 1)
          (swap! state assoc :app app)
          (pan-zoom (:x config/default-position)
                    (:y config/default-position)
                    (:z config/default-position))))
      :should-component-update
      (fn [_ _ _] true)

      :component-did-update
      (fn [_])

      :component-will-unmount
      (fn [_]
        (remove-edges)
        (.destroy (:app @state))
        (reset! state nil))})))

                     