(ns de.explorama.frontend.mosaic.render.pixi.shapes
  (:require [clojure.string :as str]
            [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.render.config :refer [select-event?]]
            [de.explorama.frontend.mosaic.render.draw.color :as color]
            ["pixi.js" :refer [utils Sprite Text TextStyle TextMetrics Graphics Point Color]]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]))

(def ^:private double-click (atom 0))
(def ^:private last-double-click (atom 0))
(def ^:private hover-active? (atom false))
(def hover-over? (atom false))
(def ^:private mouse-down-atom (atom nil))

(def ^:private double-click-threshold 300)
(def ^:private double-click-threshold-min 30)
(def ^:private double-click-threshold-trigger 600)

(def ^:private hover-delay 500)

(def ^:private font-name #js["Open Sans" "sans-serif"])

(defn- get-time []
  (.getTime (js/Date.)))

(defn- interaction-handler [{:keys [on func path]
                             {delay-ms :delay
                              track-move? :track-move?}
                             :opts
                             :or {delay-ms hover-delay}}
                            container]
  (cond
    (= on "click")
    (let [click-fn #(func (pc/modifier %)
                          (pc/coords %)
                          %)]
      (.on ^js container "click" click-fn)
      (.on ^js container "touchstart" click-fn))
    (= on "dblclick")
    (let [click-fn (fn [e]
                     (let [time (get-time)
                           mods (pc/modifier e)]
                       (if (and (< double-click-threshold-min
                                   (- time
                                      @double-click)
                                   double-click-threshold)
                                (not (:ctrl mods))
                                (< double-click-threshold-trigger
                                   (- time
                                      @last-double-click)))
                         (do
                           (reset! last-double-click (get-time))
                           (func (pc/modifier e)
                                 (pc/coords e)
                                 e))
                         (reset! double-click time))))]
      (.on ^js container "click" click-fn)
      (.on ^js container "touchstart" click-fn))

    (= on "hover")
    (do
      (.on ^js container
           "pointerover"
           (fn [e]
             (if (= delay-ms :instant)
               (do
                 (when-not @hover-over?
                   (func (pc/modifier e)
                         (pc/coords e)
                         path
                         :show))
                 (reset! hover-over? true))
               (do
                 (reset! hover-over? true)
                 (reset! hover-active? false)
                 (js/setTimeout (fn []
                                  (when (and
                                         @hover-over?
                                         (not @hover-active?))
                                    (reset! hover-active? true)
                                    (func (pc/modifier e)
                                          (pc/coords e)
                                          path
                                          :show)))
                                delay-ms)))))
      (when track-move?
        (.on ^js container
             "pointermove"
             (fn [e]
               (func (pc/modifier e)
                     (pc/coords e)
                     path
                     :move))))
      (.on ^js container
           "pointerout"
           (fn [e]
             (reset! hover-active? false)
             (reset! hover-over? false)
             (func (pc/modifier e)
                   (pc/coords e)
                   path
                   :hide))))
    (= on "dragndrop")
    (do
      (.on ^js container
           "pointerdown"
           (fn [e]
             (let [event (-> ^js e .-data .-originalEvent)
                   x (.-pageX ^js event)
                   y (.-pageY ^js event)]
               (when (select-event? event)
                 (reset! mouse-down-atom [x y e event path])))))
      (.on ^js container
           "pointermove"
           (fn [e]
             (let [event (-> ^js e .-data .-originalEvent)
                   x (.-pageX ^js event)
                   y (.-pageY ^js event)]
               (when @mouse-down-atom
                 (let [[sx sy e _ cpath] @mouse-down-atom]
                   (when (and (= cpath path)
                              (< 10 (Math/sqrt (+ (Math/pow (- x sx)
                                                            2)
                                                  (Math/pow (- y sy)
                                                            2)))))
                     (reset! pc/drag-interaction [(pc/modifier e)
                                                  (pc/coords e)
                                                  [sx sy]
                                                  path])
                     (func (pc/modifier e)
                           (pc/coords e)
                           (if (map? path)
                             (:path path)
                             path)
                           (aget e "data" "originalEvent"))
                     (reset! mouse-down-atom nil)))))))
      (.on ^js container
           "pointerout"
           (fn [e]
             (when @mouse-down-atom
               (let [[x y _ event] @mouse-down-atom]
                 (when (not= 2 (aget event "which"))
                   (reset! pc/drag-interaction [(pc/modifier e)
                                                (pc/coords e)
                                                [x y]
                                                path])
                   (func (pc/modifier e)
                         (pc/coords e)
                         (if (map? path)
                           (:path path)
                           path)
                         (aget e "data" "originalEvent")))))
             (reset! mouse-down-atom nil))))
    :else
    (.on ^js container on #(func (pc/modifier %)
                             (pc/coords %)
                             %))))

(defn- rgb->hex [rgb]
  (-> (Color. (if (vector? rgb)
                (clj->js (mapv #(/ % 255) rgb))
                rgb))
      (.toNumber)))

(defn rect [g x y w h c {:keys [a rounded? radius outline] :or {a 1}}]
  (when outline
    (.lineStyle ^js g (clj->js {:width (:width outline)
                            :color (condp = (:color outline)
                                     :auto-0
                                     (rgb->hex (get (color/font-color c a) 0))
                                     :auto-1
                                     (rgb->hex (get (color/font-color c a) 1))
                                     :auto-2
                                     (rgb->hex (get (color/font-color c a) 2))
                                     (rgb->hex (:color outline)))})))
  (.beginFill ^js g (rgb->hex c) a)
  (if rounded?
    (.drawRoundedRect ^js g
                      x
                      y
                      w
                      h
                      radius)
    (.drawRect ^js g
               x
               y
               w
               h))
  (.endFill ^js g))

(defn circle [g x y r c {:keys [a] :or {a 1}}]
  (.beginFill ^js g (rgb->hex c) a)
  (.drawCircle ^js g
               x
               y
               r)
  (.endFill ^js g))

(defn polygon [g points c {:keys [a] :or {a 1}}]
  (.beginFill ^js g (rgb->hex c) a)
  (.drawPolygon ^js g
                (clj->js points))
  (.endFill ^js g))

(defn point [x y]
  (Point. x y))

(defn interaction-primitive
  "stage-num is usally 0 but for header 1 because mosaic uses usally one graphics object per zoomlevel and if we want to have "
  [stage on func path stage-num & [opts]]
  (set! (.-eventMode ^js (.getChildAt ^js stage stage-num)) "static")
  (interaction-handler {:on on
                        :func func
                        :path path
                        :opts opts}
                       (.getChildAt ^js stage stage-num)))


(defn img [stage id x y w h {interaction :interaction
                             color :color}]
  (let [tex-resource (aget utils.TextureCache id)
        sprite (when tex-resource
                 (Sprite. tex-resource))] ; pixi-texture.WHITE)]
    (when sprite
      (when interaction
        (set! (.-eventMode ^js sprite) "static")
        (interaction-handler interaction sprite))
      (set! (.-x ^js sprite) x)
      (set! (.-y ^js sprite) y)
      (set! (.-width ^js sprite) w)
      (set! (.-height ^js sprite) h)
      (when color
        (set! (.-tint ^js sprite) (rgb->hex color)))
      (.addChild ^js stage sprite))))

(defn- debug-border [stage x y w h]
  (let [border 2
        g  (Graphics.)]
    (rect g
          x
          y
          w
          border
          "#FF6666"
          (assoc {} :debug-level? true :z-index 3))
    (rect  g
           (+ x w)
           y
           border
           h
           "#FF6666"
           (assoc {} :debug-level? true :z-index 3))
    (rect  g
           x
           (+ y h)
           w
           border
           "#FF6666"
           (assoc {} :debug-level? true :z-index 3))
    (rect  g
           x
           y
           border
           h
           "#FF6666"
           (assoc {} :debug-level? true :z-index 3))
    (.addChild ^js stage g)))

(defn text [stage text-str x y w h {:keys [font-size font-color align angle vertical-align debug? postfix adjust-width?]
                                    :or {font-size 12
                                         font-color 0x000000
                                         align :left
                                         vertical-align :top}}]
  (try
    (let [[text-str postfix] (if postfix
                               (str/split text-str (re-pattern postfix))
                               [text-str ""])
          generosity (+ (* 0.5 (/ 1 (Math/floor (/ h font-size))))
                        1)
          h (if (= h :one-line)
              (+ font-size 1)
              h)
          text-obj
          (Text. (str text-str postfix)
                 (TextStyle. (clj->js (cond-> {:fontSize font-size
                                               :fontFamily font-name
                                               :fill (rgb->hex font-color)
                                               :align align}
                                        (not adjust-width?)
                                        (merge {:wordWrap true
                                                :wordWrapWidth w
                                                :breakWords true})))))
          height-factor (/ (aget text-obj "height") h)]
      (when (or (< 1 height-factor)
                adjust-width?)
        (let [iterations (if (< 20 (count text-str))
                           40
                           15)
              skip (if (< 20 (count text-str))
                     4
                     2)
              new-text (if-not adjust-width?
                         text-str
                         (loop [width-factor (/ (aget text-obj "width") w)
                                i 0
                                text-str text-str]
                           (if (and (< 1 width-factor)
                                    (< i iterations))
                             (let [new-text (subs text-str 0 (max 0 (- (count text-str) skip)))]
                               (aset text-obj "text" (str new-text postfix))
                               (.updateText ^js text-obj)
                               (recur (/ (aget text-obj "width") w)
                                      (inc i)
                                      new-text))
                             text-str)))
              _ (when adjust-width?
                  (aset text-obj "style" #js{:fontSize font-size
                                             :fontFamily font-name
                                             :fill (rgb->hex font-color)
                                             :align align
                                             :wordWrap true
                                             :wordWrapWidth w
                                             :breakWords true})
                  (.updateText ^js text-obj))
              new-text (if (< 2 height-factor)
                         (let [new-text (subs new-text 0 (max 0 (int (* 2 (/ (count new-text)
                                                                             height-factor)))))]
                           (aset text-obj "text" (str new-text postfix))
                           (.updateText ^js text-obj)
                           new-text)
                         new-text)
              new-text (if (< generosity height-factor)
                         (loop [height-factor (/ (aget text-obj "height") h)
                                i 0
                                text-str new-text]
                           (if (and (< generosity height-factor)
                                    (< i iterations))
                             (let [new-text (subs text-str 0 (max 0 (- (count text-str) skip)))]
                               (aset text-obj "text" (str new-text postfix))
                               (.updateText ^js text-obj)
                               (recur (/ (aget text-obj "height") h)
                                      (inc i)
                                      new-text))
                             text-str))
                         new-text)]
          (when (not= new-text text-str)
            (aset text-obj "text" (str (subs new-text 0 (max 0 (- (count new-text) 3)))
                                       config/prune-char
                                       postfix))
            (.updateText text-obj))))
      (let [y (cond (= :bottom vertical-align)
                    (+ y (- h (aget text-obj "height")))
                    (= :center vertical-align)
                    (- y (* 0.5 (aget text-obj "height")))
                    :else
                    y)
            x (cond (= :right align)
                    (+ x (- w (aget text-obj "width")))
                    (= :center align)
                    (- x (* 0.5 (aget text-obj "width")))
                    :else
                    x)]
        (aset text-obj "y" y)
        (aset text-obj "x" x)
        (when debug?
          (debug-border stage x y w h)))
      (when angle
        (aset text-obj "angle" angle))
      (.addChild ^js stage text-obj))
    (catch :default _
      nil)))

(defn text-metrics [text-str {:keys [font font-size font-color align]
                              :or {font "Arial"
                                   font-color 0x000000
                                   font-size 12
                                   align :left}}]
  (let [pixi-align (cond (= align :center)
                         :left
                         :else
                         align)
        style #js{:fontSize font-size
                  :fontFamily font
                  :fill font-color
                  :align pixi-align}
        n-style (TextStyle. style)
        metrics (TextMetrics.measureText (str text-str) n-style)]
    {:width (.-width metrics)
     :height (.-height metrics)}))