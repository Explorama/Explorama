(ns de.explorama.frontend.aggregation-vpl.render.common)

(def options-fixed-idx 0)
(def options-scroll-hor-vert-idx 1)
(def options-scroll-vert-idx 2)
(def options-scroll-bar-hor-idx 3)
(def options-scroll-bar-vert-idx 4)
(def workspace-fixed-idx 5)
(def workspace-drop-idx 6)
(def workspace-scroll-idx 7)
(def workspace-scroll-bar-hor-idx 8)
(def workspace-scroll-bar-vert-idx 9)
(def drag-idx 10)
(def max-wheel-delta 720)
(def min-wheel-delta -720)
(def drag-delay 300)

(def panning? (atom false))
(def options-drag (atom nil))
(def workspace-drag (atom nil))
(def over-options (atom nil))
(def options-drop-target (atom nil))
(def workspace-drop-target (atom nil))
(def click-delay (atom nil))

(defn render [app]
  (.render (.-renderer app)
           (.-stage app)))

(defn rect [g x y h w c a]
  (.beginFill g c a)
  (.drawRect g
             x
             y
             h
             w)
  (.endFill g))

(defn rect-rounded [g x y h w c a r]
  (.beginFill g c a)
  (.drawRoundedRect g
                    x
                    y
                    h
                    w
                    r)
  (.endFill g))

(defn polygon [g points c a]
  (.beginFill g c a)
  (.drawPolygon g
                points)
  (.endFill g))

(defn polygon-x [g x y size c a]
  (polygon g
           #js [(js/PIXI.Point.
                 (+ (* 0 size) x)
                 (+ (* 6 size) y))
                (js/PIXI.Point.
                 (+ (* 6 size) x)
                 (+ (* 0 size) y))
                (js/PIXI.Point.
                 (+ (* 14 size) x)
                 (+ (* 8 size) y))
                (js/PIXI.Point.
                 (+ (* 22 size) x)
                 (+ (* 0 size) y))
                (js/PIXI.Point.
                 (+ (* 28 size) x)
                 (+ (* 6 size) y))
                (js/PIXI.Point.
                 (+ (* 20 size) x)
                 (+ (* 14 size) y))
                (js/PIXI.Point.
                 (+ (* 28 size) x)
                 (+ (* 22 size) y))
                (js/PIXI.Point.
                 (+ (* 22 size) x)
                 (+ (* 28 size) y))
                (js/PIXI.Point.
                 (+ (* 14 size) x)
                 (+ (* 20 size) y))
                (js/PIXI.Point.
                 (+ (* 6 size) x)
                 (+ (* 28 size) y))
                (js/PIXI.Point.
                 (+ (* 0 size) x)
                 (+ (* 22 size) y))
                (js/PIXI.Point.
                 (+ (* 8 size) x)
                 (+ (* 14 size) y))]
           c a))

(defn polygon-+ [g x y size c a]
  (polygon g
           #js [(js/PIXI.Point.
                 (+ (* 6 size) x)
                 (+ (* 0 size) y))
                (js/PIXI.Point.
                 (+ (* 12 size) x)
                 (+ (* 0 size) y))
                (js/PIXI.Point.
                 (+ (* 12 size) x)
                 (+ (* 6 size) y))
                (js/PIXI.Point.
                 (+ (* 18 size) x)
                 (+ (* 6 size) y))
                (js/PIXI.Point.
                 (+ (* 18 size) x)
                 (+ (* 12 size) y))
                (js/PIXI.Point.
                 (+ (* 12 size) x)
                 (+ (* 12 size) y))
                (js/PIXI.Point.
                 (+ (* 12 size) x)
                 (+ (* 18 size) y))
                (js/PIXI.Point.
                 (+ (* 6 size) x)
                 (+ (* 18 size) y))
                (js/PIXI.Point.
                 (+ (* 6 size) x)
                 (+ (* 12 size) y))
                (js/PIXI.Point.
                 (+ (* 0 size) x)
                 (+ (* 12 size) y))
                (js/PIXI.Point.
                 (+ (* 0 size) x)
                 (+ (* 6 size) y))
                (js/PIXI.Point.
                 (+ (* 6 size) x)
                 (+ (* 6 size) y))]
           c a))

(defn get-time [] (.getTime (js/Date.)))

(def ^:private double-click-threshold 300)
(def ^:private double-click-threshold-min 30)

(defn double-click [c func]
  (.on c "pointerup" (fn [e]
                       (let [time (get-time)
                             cur-click-delay @click-delay]
                         (if (and cur-click-delay
                                  (< double-click-threshold-min
                                     (- time cur-click-delay)
                                     double-click-threshold))
                           (func e)
                           (reset! click-delay time))))))

(defn interactable
  ([container over out down up button?]
   (aset container "interactive" true)
   (when button?
     (aset container "buttonMode" true))
   (when over
     (.on container "pointerover" over))
   (when out
     (.on container "pointerout" out))
   (when up
     (.on container "pointerup" up))
   (when down
     (.on container "pointerdown" down)))
  ([app container hover-container up]
   (interactable container
                 (fn [e]
                   (.addChild container
                              hover-container)
                   (render app))
                 (fn [e]
                   (.removeChild container
                                 hover-container)
                   (render app))
                 nil
                 up
                 true)))

(defn destroy [obj]
  (.destroy obj (clj->js {:children true})))

(defn clear-children [container]
  (let [children (.-children container)]
    (.removeChildren container)
    (doseq [child children]
      (destroy child))))

(defn text [text-str x y font-size font font-color pixi-align]
  (let [text-obj (js/PIXI.Text. text-str
                                (js/PIXI.TextStyle. #js{:fontSize (str font-size "px")
                                                        :fontFamily font
                                                        :fill font-color
                                                        :align pixi-align}))]
    (aset text-obj "x" x)
    (aset text-obj "y" y)
    text-obj))

(defn hor-scroll [state
                  fixed-container scroll-container
                  width height scrollbar
                  min-width max-width container-width
                  target-keyword]
  (let [fixed (js/PIXI.Graphics.)
        moving (js/PIXI.Graphics.)
        scroll-bar-length (* container-width
                             (/ min-width
                                max-width))]
    (rect fixed
          0
          (- height scrollbar)
          width
          scrollbar
          0x000000
          0.2)
    (rect moving
          0
          (- height scrollbar)
          (* container-width
             (/ min-width
                max-width))
          scrollbar
          0x000000
          0.2)
    (swap! state assoc target-keyword (- container-width scroll-bar-length))
    (.addChild fixed-container fixed)
    (.addChild scroll-container moving)))

(defn initialize-matrix [dis]
  (mapv (fn [x]
          [(get dis x)])
        (range (count dis))))

(defn create-op [{op-name :op-name meta-data :meta-data} connection]
  {:op-name op-name :op-type :op :meta-data meta-data :id (str (random-uuid))
   :connection connection})

(defn create-con [start end]
  #js {"startX" start
       "endX" end
       "id" (str (random-uuid))})
