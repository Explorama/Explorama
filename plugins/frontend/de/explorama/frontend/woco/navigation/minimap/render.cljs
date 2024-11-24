(ns de.explorama.frontend.woco.navigation.minimap.render
  (:require ["pixi.js" :refer [Sprite  Graphics Color Application Container]]
            [clojure.set :refer [difference]]
            [clojure.string :refer [starts-with?]]
            [taoensso.timbre :refer [error]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.navigation.resources :as resources]))

(defonce hex-color-cache (atom {}))

(defn- disable-tickers
  "Disabled tickers in pixi to prevent performance leaks due to automatic loops (copies from mosaic)"
  []
  (let [shared-ticker (aget js/PIXI "Ticker" "shared")
        system-ticker (aget js/PIXI "Ticker" "system")]
    (aset system-ticker "autoStart" false)
    (aset shared-ticker "autoStart" false)
    (when (aget system-ticker "started")
      (.stop system-ticker))
    (when (aget shared-ticker "started")
      (.stop shared-ticker))))

(disable-tickers)

(defn- rgb->hex
  "translates [r g b] to hex with caching hex values"
  [rgb]
  (if-let [hex (get @hex-color-cache rgb)]
    hex
    (-> (swap! hex-color-cache assoc rgb (-> (Color. (clj->js (mapv #(/ % 255) rgb)))
                                             (.toNumber)))
        (get rgb))))

(defn- hex->int
  "translates hex to integer"
  [hex]
  (cond-> hex
    (and (string? hex)
         (starts-with? hex "#"))
    (-> (subs 1)
        (js/parseInt 16))))

(defn handle-color [{:keys [type value]}]
  (case type
    :hex (hex->int value)
    :rgb (rgb->hex value)
    (error "not supported type" type)))

(defn- rect
  "Draws an rectangle to pixi container g (copied from mosaic)"
  [g ^number x ^number y ^number h ^number w c {:keys [a] :or {a 1}}]
  (.beginFill g (if (number? c)
                  c
                  (rgb->hex c)) a)
  (.drawRect g x y h w)
  (.endFill g))

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

(defn- icon-sprite
  "Get sprite with icon as texture"
  [texture-id]
  (when-let [tex (resources/texture texture-id)]
    (when-let [sprite (Sprite. tex)]
      {:ico sprite
       :w-h-ratio (/ (aget tex "orig" "width")
                     (max (aget tex "orig" "height") 1))
       :ico-original-w (* config/minimap-icon-scale-factor (aget tex "orig" "width"))
       :ico-original-h (* config/minimap-icon-scale-factor (aget tex "orig" "height"))})))

(defn- render-icon
  "Sets the position and size of an (icon-) sprite"
  [sprite ^number w ^number h ^number original-w ^number original-h ^number w-h-ratio]
  (if (or (< w config/minimap-show-icon-min-width)
          (< h config/minimap-show-icon-min-height))
    (aset sprite "visible" false)
    (let [^number sprite-h (min original-h (- h 100))
          ^number sprite-w (min original-w (- w 100))
          ^number sprite-h (min sprite-w sprite-h)
          ^number sprite-w (* sprite-h w-h-ratio)
          x (+
             (/ w 2)
             (- (/ sprite-w 2)))
          y (+
             (/ h 2)
             (- (/ sprite-h 2)))]
      (set-position sprite x y)
      (set-size sprite sprite-w sprite-h)
      (aset sprite "visible" true))))

(defn- render-frame
  "Renders a frame as a container. Only updates changed attributes"
  [state {:keys [id di ^number left ^number top ^number full-width ^number full-height
                 color-group color ^number z-index ^boolean is-minimized?]}]
  (let [{:keys [frames frames-container]} @state]
    (when-not (get frames id)
     ;; ----- initialize frame: Only when not exists
      (let [frame (Container.)
            bg (Graphics.)
            {:keys [ico ico-original-w ico-original-h w-h-ratio]}
            (when config/minimap-render-icons?
              (icon-sprite (resources/mm-texture-id (:vertical id))))]
        (when (and frames-container frame bg)
          (.addChild frame bg)
          (when ico
            (.addChild frame ico))
          (swap! state update-in
                 [:frames id]
                 assoc
                 :frame frame
                 :bg bg
                 :ico ico
                 :ico-original-w ico-original-w
                 :ico-original-h ico-original-h
                 :w-h-ratio w-h-ratio)
          (.addChild frames-container frame))))
    ;; ----- Update Frame: Apply only changes
    (let [{:keys [frames]} @state
          {:keys [frame bg ico color-grp old-color l t w h ico-original-w ico-original-h z-idx is-min? w-h-ratio]} (get frames id)]
      (when (or (not= color-grp color-group)
                (not= w full-width)
                (not= h full-height)
                (not= old-color color))
        (.clear bg)

        (when (not= is-minimized? is-min?)
          (aset ico "visible" (not is-minimized?)))

        (when (< 0 config/minimap-frame-border-size)
          (rect bg 0 0 full-width full-height config/minimap-frame-border-color {:a config/minimap-frame-border-alpha}))
        (rect bg
              config/minimap-frame-border-size
              config/minimap-frame-border-size
              (- full-width (* 2 config/minimap-frame-border-size))
              (- full-height (* 2 config/minimap-frame-border-size))
              (cond di (resources/color color-group config/minimap-default-frame-color)
                    color (handle-color color)
                    :else config/minimap-default-frame-color)
              {}))

      (when (or (not= l left)
                (not= t top))
        (set-position frame left top))

      (when (or (not= w full-width)
                (not= h full-height))
        (when ico
          (render-icon ico full-width full-height ico-original-w ico-original-h w-h-ratio))
        (set-size frame full-width full-height))

      (when (not= z-idx z-index)
        (aset frame "zIndex" z-index))
      (swap! state update-in
             [:frames id]
             assoc
             :color-grp (when di color-group)
             :old-color color
             :l left
             :t top
             :w full-width
             :h full-height
             :is-min? is-minimized?
             :z-idx z-index))))

(defn- remove-frames
  "Remove frame-containers which are not there anymore"
  ([state new-ids-set]
   (let [{:keys [frames]} @state
         removed-frames (difference (set (keys frames))
                                    new-ids-set)]
     (doseq [id removed-frames]
       (when-let [{:keys [frame]} (get frames id)]
         (.destroy frame
                   (clj->js {:children true})))
       (swap! state update :frames dissoc id))))
  ([state]
   (remove-frames state #{})))

(defn position
  "Get current position of minimap"
  [state]
  (when-let [app (:app @state)]
    (let [transform (aget app "stage" "transform")]
      {:x (aget transform "position" "x")
       :y (aget transform "position" "y")
       :z (aget transform "scale" "x")})))

(defn render-frames
  "Renders and removes frames on minimap"
  [state frames-infos]
  (when-let [app (get @state :app)]
    (let [force-rerender? (get @state :force-rerender?)
          _ (when force-rerender?
              (remove-frames state))
          stage (aget app "stage")
          ;; l t r b are the boundings to calculate the scale
          [^number l ^number t ^number r ^number b frame-set]
          (reduce (fn [[^number l ^number t ^number r ^number b frame-set]
                       {:keys [id ^number left ^number top ^number full-width ^number full-height] :as frame}]
                    (render-frame state frame)
                    [(if l (min l left) left)
                     (if t (min t top) top)
                     (if r
                       (max r (+ left full-width))
                       (+ left full-width))
                     (if b
                       (max b (+ top full-height))
                       (+ top full-height))
                     (conj (or frame-set #{})
                           id)])
                  []
                  frames-infos)
          w (max config/minimap-render-min-width (- r l))
          h (max config/minimap-render-min-height (- b t))
          w (+ w (* 2 config/minimap-render-padding-horizontal))
          h (+ h (* 2 config/minimap-render-padding-vertical))
          scale-x (/ config/minimap-width
                     w)
          scale-y (/ config/minimap-height
                     h)
          transform-z (min scale-x scale-y)
          transform-x (+ (* transform-z (- l))
                         (* config/minimap-render-padding-horizontal transform-z))

          transform-y (+ (* transform-z (- t))
                         (* config/minimap-render-padding-vertical transform-z))]
      (remove-frames state frame-set)
      (set-position stage transform-x transform-y transform-z)
      (-> (aget app "renderer")
          (.render stage)))))

(defn render-viewport
  "Renders the woco viewport (visible part on workspace)"
  [state ^number x ^number y ^number z {:keys [^number width ^number height]}]
  (when-let [{:keys [app viewport-container viewport-graphic
                     ^number vx ^number vy ^number vz
                     ^number vw ^number vh
                     ^boolean force-rerender?]} @state]
    (let [stage (aget app "stage")]
      ;; ---- Position -----
      (when (or force-rerender?
                (not= vx x)
                (not= vy y)
                (not= vz z))
        (let [nx (/ x z)
              ny (/ y z)]
          (set-position viewport-container (- nx) (- ny))))
      ;; ---- Size -----
      (when (or force-rerender?
                (not= vw width)
                (not= vh height)
                (not= vz z))
        (let [width (/ width z)
              height (/ height z)]
          (.clear viewport-graphic)
          (rect viewport-graphic
                0
                0
                width height
                config/minimap-viewport-color
                {:a config/minimap-viewport-alpha})
          (set-size viewport-container width height)))

      (-> (aget app "renderer")
          (.render stage))
      (swap! state assoc
             :vx x
             :vy y
             :vz z
             :vw width
             :vh height))))

(defn init
  "Initialize minimap application"
  [state]
  (when-let [host (:host @state)]
    (let [app (Application. (clj->js {:autoStart false
                                      :width config/minimap-width
                                      :height config/minimap-height
                                      :antialias true
                                      :resolution config/minimap-resolution
                                      :roundPixels false
                                      :backgroundAlpha 0
                                      :forceCanvas true
                                      :sharedTicker false
                                      :view host}))
          viewport (Container.)
          viewport-graphic (Graphics.)
          cont (Container.)
          stage (aget app "stage")]
      (aset cont "sortableChildren" true)
      (aset viewport "zIndex" 9999999)
      (.addChild viewport viewport-graphic)
      (.addChild stage cont)
      (.addChild stage viewport)
      (swap! state assoc
             :app app
             :frames-container cont
             :viewport-container viewport
             :viewport-graphic viewport-graphic))))