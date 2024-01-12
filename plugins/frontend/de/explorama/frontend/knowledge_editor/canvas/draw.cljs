(ns de.explorama.frontend.knowledge-editor.canvas.draw
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.knowledge-editor.canvas.nav :as nav]
            [de.explorama.frontend.knowledge-editor.canvas.stages :as stages]
            [de.explorama.frontend.knowledge-editor.config :as config]
            [de.explorama.frontend.knowledge-editor.store :refer [init-data
                                                                  lookup-context
                                                                  lookup-event]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [error]]))

(def ^:private double-click-hack (atom 0))
(def ^:private last-double-click-hack (atom 0))
(def ^:private hover-active-hack? (atom false))
(def ^:private hover-over-hack? (atom false))
(def ^:private drag-interaction (atom nil))
(def connection-active? (reagent/atom false))
(def delete-active? (reagent/atom false))
(def edit-active? (reagent/atom false))
(def investigate-active? (reagent/atom false)) ;TODO
(def ^:private connection-start (atom nil))

(def ^:private render-states (atom {}))
(def ^:private render-listener (atom {}))

(def ^:private double-click-threshold 300)
(def ^:private double-click-threshold-min 30)
(def ^:private double-click-threshold-trigger 600)

(def ^:private hover-delay 500)

(def ^:private font-name #js["Open Sans" "sans-serif"])

(defn reset-connection-active! [& [force?]]
  (reset! connection-start nil)
  (if (boolean? force?)
    (reset! connection-active? force?)
    (swap! connection-active? not)))

(defn toggle-connect-active? []
  (reset! delete-active? false)
  (reset! edit-active? false)
  (reset-connection-active!))

(defn toggle-delete-active? []
  (reset-connection-active! false)
  (reset! edit-active? false)
  (swap! delete-active? not))

(defn toggle-edit-active? []
  (reset! delete-active? false)
  (reset-connection-active! false)
  (swap! edit-active? not))

(defn reset-drag-interaction []
  (reset! drag-interaction nil))

(defn- get-time []
  (.getTime (js/Date.)))

(defn- modifier [event]
  {:ctrl (-> event .-data .-originalEvent .-ctrlKey)
   :alt (-> event .-data .-originalEvent .-altKey)
   :shift (-> event .-data .-originalEvent .-shiftKey)})

(defn- coords [event]
  [(-> event .-data .-global .-x)
   (-> event .-data .-global .-y)])

(declare inform-listener)

(defn- delete-container [container main-container id]
  (when container
    (.removeChild main-container container)
    (.destroy container (clj->js {:children true}))
    (swap! render-states dissoc id)))

(defn- interaction-handler [instance
                            id
                            {:keys [on func is-a]
                             {delay-ms :delay}
                             :opts
                             :or {delay-ms hover-delay}}
                            container]
  (cond
    (= on "dblclick")
    (.on container
         "click"
         (fn [e]
           (let [time (get-time)
                 mods (modifier e)]
             (if (and (< double-click-threshold-min
                         (- time
                            @double-click-hack)
                         double-click-threshold)
                      (not (:ctrl mods))
                      (< double-click-threshold-trigger
                         (- time
                            @last-double-click-hack)))
               (do
                 (reset! last-double-click-hack (get-time))
                 (func (modifier e)
                       (coords e)
                       e))
               (reset! double-click-hack time)))))
    (= on "hover")
    (do
      (.on container
           "pointerover"
           (fn [e]
             (if (= delay-ms :instant)
               (do
                 (when-not @hover-over-hack?
                   (func (modifier e)
                         (coords e)
                         :show))
                 (reset! hover-over-hack? true))
               (do
                 (reset! hover-over-hack? true)
                 (reset! hover-active-hack? false)
                 (js/setTimeout (fn []
                                  (when (and
                                         @hover-over-hack?
                                         (not @hover-active-hack?))
                                    (reset! hover-active-hack? true)
                                    (func (modifier e)
                                          (coords e)
                                          :show)))
                                delay-ms)))))
      (.on container
           "pointerout"
           (fn [e]
             (reset! hover-active-hack? false)
             (reset! hover-over-hack? false)
             (func (modifier e)
                   (coords e)
                   :hide))))
    (= is-a "card")
    (do
      (.on container "click"
           (fn [e]
             (when (or (and (not @connection-active?)
                            (not @edit-active?)
                            (not @delete-active?))
                       @investigate-active?)
               (let [time (get-time)
                     mods (modifier e)]
                 (if (and (< double-click-threshold-min
                             (- time
                                @double-click-hack)
                             double-click-threshold)
                          (not (:ctrl mods))
                          (< double-click-threshold-trigger
                             (- time
                                @last-double-click-hack)))
                   (do
                     (reset! last-double-click-hack (get-time))
                     (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.main-view/investigate-figure-element (:frame-id @instance) id]))
                   (reset! double-click-hack time))))
             (when @connection-active?
               (if @connection-start
                 (do (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/create-connection (:frame-id @instance) id @connection-start])
                     (reset! connection-start nil))
                 (reset! connection-start id)))
             (when @delete-active?
               (let [con (get @render-states id)
                     main-container (.getChildAt (.-stage (:app @instance)) stages/main-stage)]
                 (inform-listener instance id :delete true)
                 (delete-container con main-container id)
                 (swap! render-listener dissoc id)
                 (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/delete (:frame-id @instance) id])
                 (stages/render @instance)))
             (when @edit-active?
               (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/edit (:frame-id @instance) id]))))
      (.on container
           "mousedown"
           (fn [e]
             (when (and (not @drag-interaction)
                        (not @connection-active?)
                        (not @delete-active?))
               (let [event (-> e .-data .-originalEvent)
                     x (.-pageX event)
                     y (.-pageY event)
                     con (get @render-states id)]
                 (reset! drag-interaction [id x y (.-x (.-position con)) (.-y (.-position con))])
                 #_(func id "draganddrop-start" {:pos [x y]})))))
      (.on container
           "mousemove"
           (fn [e]
             (when (and (= id (first @drag-interaction))
                        (not @connection-active?)
                        (not @delete-active?))
               (let [[_ sx sy con-x con-y] @drag-interaction
                     {:keys [z]} (get @instance [:pos stages/main-stage])
                     event (-> e .-data .-originalEvent)
                     nx (.-pageX event)
                     ny (.-pageY event)
                     con (get @render-states id)]
                 (nav/set-transform-main-pos con
                                             (+ con-x
                                                (- (/ (- sx nx)
                                                      z)))
                                             (+ con-y
                                                (- (/ (- sy ny)
                                                      z))))
                 (inform-listener instance id :move false)

                 (stages/render @instance)))))
      (.on container
           "mouseup"
           (fn [e]
             (when (and (= id (first @drag-interaction))
                        (not @connection-active?)
                        (not @delete-active?))
               (let [[_ sx sy con-x con-y] @drag-interaction
                     {:keys [z]} (get @instance [:pos stages/main-stage])
                     event (-> e .-data .-originalEvent)
                     nx (.-pageX event)
                     ny (.-pageY event)
                     con (get @render-states id)
                     new-state-id (str (random-uuid))
                     newx (+ con-x
                             (- (/ (- sx nx)
                                   z)))
                     newy (+ con-y
                             (- (/ (- sy ny)
                                   z)))]
                 (reset! drag-interaction nil)
                 (nav/set-transform-main-pos con
                                             newx
                                             newy)
                 (inform-listener instance id :move true)
                 (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/update-state-id
                                     id
                                     (:frame-id @instance)
                                     new-state-id
                                     {:pos [newx newy]}])
                 (aset con "state-id" new-state-id)
                 (stages/render @instance)
                 (func id "draganddrop-end" {:pos [nx ny]}))))))
    :else
    (.on container on #(func (modifier %)
                             (coords %)
                             %))))

(defn- rgb->hex [rgb]
  (if (vector? rgb)
    (-> js/PIXI .-utils (.rgb2hex (clj->js (mapv #(/ % 255) rgb))))
    (-> js/PIXI .-utils (.string2hex rgb))))

(defn- rect [g x y w h c {:keys [a rounded? radius] :or {a 1}}]
  (.beginFill g (rgb->hex c) a)
  (if rounded?
    (.drawRoundedRect g
                      x
                      y
                      w
                      h
                      radius)
    (.drawRect g
               x
               y
               w
               h))
  (.endFill g))

(defn- circle [g x y r c {:keys [a] :or {a 1}}]
  (.beginFill g (rgb->hex c) a)
  (.drawCircle g
               x
               y
               r)
  (.endFill g))

(defn- polygon [g points c {:keys [a] :or {a 1}}]
  (.beginFill g (rgb->hex c) a)
  (.drawPolygon g
                (clj->js points))
  (.endFill g))

(defn- point [x y]
  (js/PIXI.Point. x y))

(defn- interaction-primitive
  "stage-num is usally 0 but for header 1 because goose uses usally one graphics object per zoomlevel and if we want to have "
  [instance stage on is-a func id & [opts]]
  (set! (.-interactive stage) true)
  (interaction-handler instance
                       id
                       (cond-> {:on on
                                :is-a is-a
                                :func func
                                :opts opts}
                         on
                         (assoc :on on)
                         is-a
                         (assoc :is-a is-a))
                       stage))

(defn img [stage id x y w h {interaction :interaction
                             color :color}]
  (let [tex-resource (aget js/PIXI "Loader" "shared" "resources" id)
        sprite (when tex-resource
                 (js/PIXI.Sprite. (aget tex-resource "texture")))] ; js/PIXI.Texture.WHITE)]
    (when sprite
      (when interaction
        (set! (.-interactive sprite) true)
        (interaction-handler nil nil interaction sprite))
      (set! (.-x sprite) x)
      (set! (.-y sprite) y)
      (set! (.-width sprite) w)
      (set! (.-height sprite) h)
      (when color
        (set! (.-tint sprite) (rgb->hex color)))
      (.addChild stage sprite))))

(defn- debug-border [stage x y w h]
  (let [border 2
        g  (js/PIXI.Graphics.)]
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
    (.addChild stage g)))

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
          (js/PIXI.Text. (str text-str postfix)
                         (js/PIXI.TextStyle. (clj->js (cond-> {:fontSize font-size
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
                               (.updateText text-obj)
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
                  (.updateText text-obj))
              new-text (if (< 2 height-factor)
                         (let [new-text (subs new-text 0 (max 0 (int (* 2 (/ (count new-text)
                                                                             height-factor)))))]
                           (aset text-obj "text" (str new-text postfix))
                           (.updateText text-obj)
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
                               (.updateText text-obj)
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
      (.addChild stage text-obj)
      [(aget text-obj "width")
       (aget text-obj "height")])
    (catch js/Error _
      nil)))

(defn font-color
  ([color a]
   (let [red (js/parseInt (subs color 1 3) 16)
         green (js/parseInt (subs color 3 5) 16)
         blue (js/parseInt (subs color 5 7) 16)
         brightness (+ (* red 0.299) (* green 0.587) (* blue 0.114))
         [r g b] (if (< 180 brightness)
                   [0 0 0]
                   [255 255 255])
         rgba [(+ (* (- 1 a) red) (* a r))
               (+ (* (- 1 a) green) (* a g))
               (+ (* (- 1 a) blue) (* a b))]]
     (if (< 180 brightness)
       [rgba
        "#000000"]
       [rgba
        "#FFFFFF"])))
  ([color]
   (let [red (js/parseInt (subs color 1 3) 16)
         green (js/parseInt (subs color 3 5) 16)
         blue (js/parseInt (subs color 5 7) 16)
         brightness (+ (* red 0.299) (* green 0.587) (* blue 0.114))]
     (if (< 180 brightness)
       ["#333333"
        "#000000"]
       ["#BBBBBB"
        "#FFFFFF"]))))

(defn- handle-raw-data
  [data]
  (cond
    (vector? data) (str/join ", " data)
    (number? data) (i18n/localized-number data)
    :else data))

(defn draw-text-new [stage x y w h data label-dict attribute props]
  (let [cur-text (cond
                   (and (nil? data)
                        label-dict)
                   (get label-dict attribute (handle-raw-data attribute))
                   (nil? data)
                   (str (handle-raw-data attribute))
                   (vector? attribute)
                   (doall
                    (reduce (fn [i j]
                              (str i ", " (name j)
                                   ": "
                                   (handle-raw-data (get data j))
                                   " "))
                            (str (name (first attribute))
                                 " : "
                                 (handle-raw-data (get data (first attribute))))
                            (rest attribute)))
                   (string? attribute)
                   (str (handle-raw-data (get data attribute)))
                   (nil? attribute)
                   ""
                   :else
                   (str (handle-raw-data attribute)))
        copy-props (select-keys props [:postfix :debug? :adjust-width?])
        number-attribute? (and (string? attribute)
                               (number? (get data attribute)))
        org-font-size (if (and (:size-small props)
                               (:size-big props))
                        (if (< (count cur-text) 8)
                          (:size-big props)
                          (:size-small props))
                        (or (:size props)
                            20))
        font-color (or (:color props)
                       "#000000")
        font-size org-font-size
        y (cond-> y
            (not= font-size org-font-size)
            (+ (- org-font-size font-size)))
        ha (cond (:horizontal-align props) {:align (:horizontal-align props)}
                 number-attribute? {:align :left}
                 :else {:align :left})
        va {:vertical-align (or (:vertical-align props) :top)}]
    (text stage
          cur-text
          x
          y
          w
          h
          (merge {:font-size org-font-size
                  :font-color font-color}
                 ha
                 va
                 copy-props))))

(def hightlight-color [255 0 255])

;TODO r10/layouts DEBUGGING ONLY - comment out when done
(def ^:private debugging? false)
(when debugging?
  (error "Card rendering is currently in debugging mode"))

(defn- layout-icon->svg [icon]
  (get {"location" "pin"
        "else" "info"
        "notes" "note"
        "organisation" "group"
        "search" "search"}
       icon icon))

(defn- annotation-interaction [frame-id {card :card
                                         data-key :data-key
                                         read-only? :read-only?
                                         :as payload}]
  (when (not read-only?)
    {:on "dblclick"
     :func (fn [_ coords])}))

(defn- details-interaction [instance stage id]
  (interaction-primitive instance
                         stage
                         nil
                         "card"
                         (fn [mods coords event])
                         id)
  (interaction-primitive instance
                         stage
                         "dblclick"
                         nil
                         (fn [mods coords event])
                         id)
  (interaction-primitive instance
                         stage
                         "rightclick"
                         nil
                         (fn [mods coords event])
                         id)
  (interaction-primitive instance
                         stage
                         "click"
                         nil
                         (fn [mods coords event])
                         id))

(def ^:private radius 6)

(defn- tile-properties [background-color]
  (let [[font-color-top-bot-t font-color-top-bot-v] (font-color background-color 0.6)
        [font-color-top-bot-t-2 font-color-top-bot-v-2] (font-color background-color 0.55)
        [font-color-mid-t font-color-mid-v] (font-color "#FFFFFF" 0.4)
        bot-field-props-t {:size 20
                           :color font-color-top-bot-t}
        bot-field-props-v {:size 20
                           :color font-color-top-bot-v}
        top-bot-field-props-t-2 {:a 0.15
                                 :color font-color-top-bot-t}
        top-bot-field-props-v-2 {:a 0.2
                                 :color font-color-top-bot-v}]
    {:font-color-top-bot-t-2 font-color-top-bot-t-2
     :font-color-top-bot-v-2 font-color-top-bot-v-2
     :font-color-top-bot-t font-color-top-bot-t
     :font-color-top-bot-v font-color-top-bot-v
     :font-color-mid-t font-color-mid-t
     :font-color-mid-v font-color-mid-v
     :bot-field-props-t bot-field-props-t
     :bot-field-props-v bot-field-props-v
     :top-bot-field-props-t-2 top-bot-field-props-t-2
     :top-bot-field-props-v-2 top-bot-field-props-v-2}))

(defn- zoom-1 [highlighted? font-color-top-bot-t-2 font-color-top-bot-v-2 item-count]
  [1 {:scene (if highlighted?
               [[:rect-rounded -10 -10 520 520 {:color hightlight-color :radius radius :a 0.2}]]
               [[:rect-rounded -2 -2 504 504 {:color "#DDDDDD" :radius radius}]])
      :interactable [[:rect-rounded 0 0 500 500 {:color :layout-color
                                                 :radius radius
                                                 :fn details-interaction}]
                     [:rect-rounded 50 250 400 3 {:color "#FFFFFF"
                                                  :a 0.6
                                                  :radius radius}]
                     [:svg 425 15 60 60 {:id "speech-bubble-text"
                                         :interaction-fn annotation-interaction
                                         :attribute "annotation"}]]
      :content (apply conj
                      [[#_[:gicon 50 50 80 80 {:color font-color-top-bot-t-2}]
                        [:title 75 56 305 80 {:size 58
                                              :color font-color-top-bot-t-2}]
                        [:value 75 144 350 100 {:size 66
                                                :color font-color-top-bot-v-2}]]
                       [#_[:gicon 50 300 80 80 {:color font-color-top-bot-t-2}]
                        [:title 75 286 305 80 {:size 58
                                               :color font-color-top-bot-t-2}]
                        [:value 75 374 350 100 {:size 66
                                                :color font-color-top-bot-v-2}]]
                       []]
                      (map (fn [_] []) (range 0 (- item-count 3))))}])

(defn- zoom-1-min-event [font-color-top-bot-t-2 font-color-top-bot-v-2]
  [1 {:scene [[:rect-rounded -2 -2 754 504 {:color "#DDDDDD" :radius radius}]]
      :interactable [[:rect-rounded 0 0 750 500 {:color :layout-color
                                                 :radius radius
                                                 :fn details-interaction}]
                     [:rect-rounded 50 250 650 3 {:color "#FFFFFF"
                                                  :a 0.6
                                                  :radius radius}]]
      :content [[#_[:gicon 50 50 80 80 {:color font-color-top-bot-t-2}]
                 [:title 75 56 675 80 {:size 58
                                       :color font-color-top-bot-t-2}]
                 [:value 75 144 675 100 {:size 66
                                         :color font-color-top-bot-v-2}]]
                [#_[:gicon 50 300 80 80 {:color font-color-top-bot-t-2}]
                 [:title 75 286 675 80 {:size 58
                                        :color font-color-top-bot-t-2}]
                 [:value 75 374 675 100 {:size 66
                                         :color font-color-top-bot-v-2}]]]}])

(defn- zoom-1-min-context [font-color-top-bot-t-2 font-color-top-bot-v-2]
  [1 {:scene [[:rect-rounded -2 -2 754 504 {:color "#DDDDDD" :radius radius}]]
      :interactable [[:rect-rounded 0 0 750 500 {:color :layout-color
                                                 :radius radius
                                                 :fn details-interaction}]
                     [:rect-rounded 50 250 650 3 {:color "#FFFFFF"
                                                  :a 0.6
                                                  :radius radius}]]
      :content [[#_[:gicon 50 50 80 80 {:color font-color-top-bot-t-2}]
                 [:title 75 56 675 80 {:size 58
                                       :color font-color-top-bot-t-2}]
                 [:value 75 144 675 100 {:size 66
                                         :color font-color-top-bot-v-2}]]
                [#_[:gicon 50 300 80 80 {:color font-color-top-bot-t-2}]
                 [:title 75 286 675 80 {:size 58
                                        :color font-color-top-bot-t-2}]
                 [:value 75 374 675 100 {:size 66
                                         :color font-color-top-bot-v-2}]]]}])

(defn- zoom-2-scene [layout-id _ _1]
  [[:rect-rounded 0 0 500 500 {:color :layout-color :radius radius}]
   [:svg 0 0 500 500 {:id layout-id}]
   [:rect 249 10 2 130 {:color "#FFFFFF"
                        :a 0.4}]
   [:rect 249 460 2 30 {:color "#FFFFFF"
                        :a 0.4}]
   #_[:rect 10 455 85 18 top-bot-field-props-t-2]
   #_[:rect 10 477 175 18 top-bot-field-props-v-2]
   #_[:rect 260 455 85 18 top-bot-field-props-t-2]
   #_[:rect 260 477 175 18 top-bot-field-props-v-2]])

(defn- zoom-3-scene [layout-id]
  [[:rect-rounded 0 0 500 500 {:color :layout-color :radius radius}]
   [:svg 0 0 500 500 {:id layout-id}]
   [:rect 249 10 2 130 {:color "#FFFFFF"
                        :a 0.4}]
   [:rect 249 460 2 30 {:color "#FFFFFF"
                        :a 0.4}]])

(defn- zoom-2-content-header [font-color-top-bot-v]
  [[[:value 18 15 210 120 {:size-small 32
                           :size-big 52
                           :color font-color-top-bot-v}]]
   [[:value 270 15 180 120 {:size-small 32
                            :size-big 52
                            :color font-color-top-bot-v}]]])

(defn- zoom-3-content-header [font-color-top-bot-t font-color-top-bot-v]
  [[[:gicon 18 15 30 30 {:color font-color-top-bot-t}]
    [:title 55 16 180 30 {:size 26
                          :color font-color-top-bot-t}]
    [:value 20 55 210 70 {:size-small 28
                          :size-big 52
                          :color font-color-top-bot-v}]]
   [[:gicon 268 15 30 30 {:color font-color-top-bot-t}]
    [:title 305 16 145 30 {:size 28
                           :color font-color-top-bot-t}]
    [:value 270 55 210 70 {:size-small 28
                           :size-big 52
                           :color font-color-top-bot-v}]]])

(defn- zoom-3-content-footer [_ _]
  [[#_[:title 15 455 230 18 bot-field-props-t]
    #_[:value 15 477 230 18 bot-field-props-v]]
   [#_[:title 265 455 230 18 bot-field-props-t]
    #_[:value 265 477 230 18 bot-field-props-v]]])

(def layout-desc
  ;[x,y,w,h]-[3,2,1]-[tt,ic,va,ti,tx,al]-[ri,le,hw,hx,fw,fx,dc,00]
  ;x: x, y: y, w: width, h:height
  ;[3,2,1] number of rows
  ;tt: title, ic: icon, va: value, ti: title and icon, tx: title and value, al: all
  ;ri: right, le: left, hw/x: half width with/out icon, fw/x: full width/out with icon, dc: dont care, 00: row number starting with 0

  (let [x-3-ic-le 21
        x-3-tx-le 81
        x-3-ic-ri 266
        x-3-tx-ri 326
        h-3-va-dc 39
        h-3-tt-dc 20
        h-3-al-dc 64
        y-3-ic-00 179
        y-3-tt-00 171
        y-3-va-00 196
        y-3-ic-01 276
        y-3-tt-01 268
        y-3-va-01 293
        y-3-ic-02 373
        y-3-tt-02 365
        y-3-va-02 390
        w-3-tx-hw 153
        w-3-tx-hx 212
        w-3-tx-fw 398
        w-3-tx-fx 457
        w-3-ic-dc 48

        x-2-ic-le x-3-ic-le
        x-2-tx-le 115
        h-2-va-dc 77
        h-2-tt-dc 30
        h-2-al-dc 112
        y-2-tt-00 y-3-tt-00
        y-2-ic-00 186
        y-2-va-00 206
        y-2-ic-01 331
        y-2-tt-01 316
        y-2-va-01 351
        w-2-tx-fw 367
        w-2-tx-fx w-3-tx-fx
        w-2-ic-dc 82]
    {:event (fn [background-color highlighted?]
              (let [{:keys [font-color-top-bot-t-2
                            font-color-top-bot-v-2]}
                    (tile-properties background-color)]
                [(zoom-1-min-event font-color-top-bot-t-2 font-color-top-bot-v-2)]))
     :context (fn [background-color highlighted?]
                (let [{:keys [font-color-top-bot-t-2
                              font-color-top-bot-v-2]}
                      (tile-properties background-color)]
                  [(zoom-1-min-context font-color-top-bot-t-2 font-color-top-bot-v-2)]))
     "layout2" (fn [background-color highlighted?]
                 (let [{:keys [font-color-top-bot-t
                               font-color-top-bot-v
                               font-color-top-bot-t-2
                               font-color-top-bot-v-2
                               font-color-mid-t
                               font-color-mid-v
                               bot-field-props-t
                               bot-field-props-v
                               top-bot-field-props-t-2
                               top-bot-field-props-v-2]}
                       (tile-properties background-color)
                       layout-id "layout2"
                       mid-field-props-v {:size-small 23
                                          :size-big 52
                                          :color font-color-mid-v}
                       mid-field-props-v-2 {:size-small 32
                                            :size-big 64
                                            :color font-color-mid-v}
                       mid-field-props-t {:color font-color-mid-t
                                          :size 26}]
                   [(zoom-1 highlighted? font-color-top-bot-t-2 font-color-top-bot-v-2 4)
                    [2 {:scene (zoom-2-scene layout-id top-bot-field-props-t-2 top-bot-field-props-v-2)
                        :interactable [[:svg 455 15 30 30 {:id "speech-bubble-text"
                                                           :attribute "annotation"}]]
                        :content (conj (zoom-2-content-header font-color-top-bot-v)
                                       [[:value x-2-ic-le y-2-tt-00 w-2-tx-fx h-2-al-dc mid-field-props-v-2]]
                                       [[:value x-2-ic-le y-2-tt-01 w-2-tx-fx h-2-al-dc mid-field-props-v-2]]
                                       []
                                       [])}]
                    [3 {:content (into (conj (zoom-3-content-header font-color-top-bot-t font-color-top-bot-v)
                                             [[:gicon x-2-ic-le y-2-ic-00 w-2-ic-dc w-2-ic-dc {:color "#adadad"}]
                                              [:title x-2-tx-le y-2-tt-00 w-2-tx-fw h-2-tt-dc mid-field-props-t]
                                              [:value x-2-tx-le y-2-va-00 w-2-tx-fw h-2-va-dc mid-field-props-v]]
                                             [[:gicon x-2-ic-le y-2-ic-01 w-2-ic-dc w-2-ic-dc {:color "#adadad"}]
                                              [:title x-2-tx-le y-2-tt-01 w-2-tx-fw h-2-tt-dc mid-field-props-t]
                                              [:value x-2-tx-le y-2-va-01 w-2-tx-fw h-2-va-dc mid-field-props-v]])
                                       (zoom-3-content-footer bot-field-props-t bot-field-props-v))
                        :interactable [[:svg 455 15 30 30 {:id "speech-bubble-text"
                                                           :attribute "annotation"}]]
                        :scene (zoom-3-scene layout-id)}]]))
     "layout4" (fn [background-color highlighted?]
                 (let [{:keys [font-color-top-bot-t
                               font-color-top-bot-v
                               font-color-top-bot-t-2
                               font-color-top-bot-v-2
                               font-color-mid-t
                               font-color-mid-v
                               bot-field-props-t
                               bot-field-props-v
                               top-bot-field-props-t-2
                               top-bot-field-props-v-2]}
                       (tile-properties background-color)
                       layout-id "layout4"
                       mid-field-props-v {:size-small 19
                                          :size-big 32
                                          :color font-color-mid-v}
                       mid-field-props-v-2 {:size-small 30
                                            :size-big 42
                                            :color font-color-mid-v}
                       mid-field-props-t {:color font-color-mid-t}]
                   [(zoom-1 highlighted? font-color-top-bot-t-2 font-color-top-bot-v-2 4)
                    [2 {:scene (zoom-2-scene layout-id top-bot-field-props-t-2 top-bot-field-props-v-2)
                        :interactable [[:svg 455 15 30 30 {:id "speech-bubble-text"
                                                           :attribute "annotation"}]]
                        :content (conj (zoom-2-content-header font-color-top-bot-v)
                                       [[:value x-3-ic-le y-3-tt-00 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-ri y-3-tt-00 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-le y-3-tt-01 w-3-tx-fx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-le y-3-tt-02 w-3-tx-fx h-3-al-dc mid-field-props-v-2]]
                                       []
                                       [])}]
                    [3 {:content (into (conj (zoom-3-content-header font-color-top-bot-t font-color-top-bot-v)
                                             [[:gicon x-3-ic-le y-3-ic-00 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-le y-3-tt-00 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-le y-3-va-00 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-ri y-3-ic-00 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-ri y-3-tt-00 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-ri y-3-va-00 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-le y-3-ic-01 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-le y-3-tt-01 w-3-tx-fw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-le y-3-va-01 w-3-tx-fw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-le y-3-ic-02 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-le y-3-tt-02 w-3-tx-fw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-le y-3-va-02 w-3-tx-fw h-3-va-dc mid-field-props-v]])
                                       (zoom-3-content-footer bot-field-props-t bot-field-props-v))
                        :interactable [[:svg 455 15 30 30 {:id "speech-bubble-text"
                                                           :attribute "annotation"}]]
                        :scene (zoom-3-scene layout-id)}]]))
     "layout6" (fn [background-color highlighted?]
                 (let [{:keys [font-color-top-bot-t
                               font-color-top-bot-v
                               font-color-top-bot-t-2
                               font-color-top-bot-v-2
                               font-color-mid-t
                               font-color-mid-v
                               bot-field-props-t
                               bot-field-props-v
                               top-bot-field-props-t-2
                               top-bot-field-props-v-2]}
                       (tile-properties background-color)
                       layout-id "layout6"
                       mid-field-props-v {:size-small 19
                                          :size-big 32
                                          :color font-color-mid-v}
                       mid-field-props-v-2 {:size-small 30
                                            :size-big 42
                                            :color font-color-mid-v}
                       mid-field-props-t {:color font-color-mid-t}]
                   [(zoom-1 highlighted? font-color-top-bot-t-2 font-color-top-bot-v-2 8)
                    [2 {:scene (zoom-2-scene layout-id top-bot-field-props-t-2 top-bot-field-props-v-2)
                        :interactable [[:svg 455 15 30 30 {:id "speech-bubble-text"
                                                           :attribute "annotation"}]]
                        :content (conj (zoom-2-content-header font-color-top-bot-v)
                                       [[:value x-3-ic-le y-3-tt-00 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-ri y-3-tt-00 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-le y-3-tt-01 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-ri y-3-tt-01 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-le y-3-tt-02 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       [[:value x-3-ic-ri y-3-tt-02 w-3-tx-hx h-3-al-dc mid-field-props-v-2]]
                                       []
                                       [])}]
                    [3 {:content (into (conj (zoom-3-content-header font-color-top-bot-t font-color-top-bot-v)
                                             [[:gicon x-3-ic-le y-3-ic-00 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-le y-3-tt-00 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-le y-3-va-00 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-ri y-3-ic-00 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-ri y-3-tt-00 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-ri y-3-va-00 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-le y-3-ic-01 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-le y-3-tt-01 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-le y-3-va-01 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-ri y-3-ic-01 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-ri y-3-tt-01 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-ri y-3-va-01 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-le y-3-ic-02 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-le y-3-tt-02 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-le y-3-va-02 w-3-tx-hw h-3-va-dc mid-field-props-v]]
                                             [[:gicon x-3-ic-ri y-3-ic-02 w-3-ic-dc w-3-ic-dc {:color "#adadad"}]
                                              [:title x-3-tx-ri y-3-tt-02 w-3-tx-hw h-3-tt-dc mid-field-props-t]
                                              [:value x-3-tx-ri y-3-va-02 w-3-tx-hw h-3-va-dc mid-field-props-v]])
                                       (zoom-3-content-footer bot-field-props-t bot-field-props-v))
                        :interactable [[:svg 455 15 30 30 {:id "speech-bubble-text"
                                                           :attribute "annotation"}]]
                        :scene (zoom-3-scene layout-id)}]]))}))

(defn- box [data])

(defn- calc-fun [[x1 y1] [x2 y2]]
  (fn [x]
    (+ (* x (/ (- y2 y1)
               (- x2 x1)))
       (/ (- (* x2 y1)
             (* x1 y2))
          (- x2 x1)))))

(defn- calc-distance [x1 y1 x2 y2]
  (Math/sqrt (+ (Math/pow (- x2 x1) 2)
                (Math/pow (- y2 y1) 2))))

(defn- connection [instance main-container {:keys [from to id color label]} new-state-id]
  (let [con (js/PIXI.Container.)
        scene-g (js/PIXI.Graphics.)
        scene-g2 (js/PIXI.Graphics.)
        from-con (get @render-states from)
        to-con (get @render-states to)
        from-x-center (+ (.-x (.-position from-con)) 375)
        from-y-center (+ (.-y (.-position from-con)) 250)
        to-x-center (+ (.-x (.-position to-con)) 375)
        to-y-center (+ (.-y (.-position to-con)) 250)
        distance (calc-distance from-x-center from-y-center to-x-center to-y-center)
        kat (Math/sqrt (Math/pow (- to-y-center from-y-center) 2))
        angle (Math/acos (/ kat distance))
        pos (/ (* 2 angle) Math/PI)
        ox (* (- 1 pos) 5)
        oy (* pos 5)
        neg-x (neg? (- from-x-center to-x-center))
        neg-y (neg? (- from-y-center to-y-center))
        poly (if (or (and neg-x neg-y)
                     (and (not neg-x)
                          (not neg-y)))
               #js[(js/PIXI.Point. (+ from-x-center ox)
                                   (- from-y-center oy))
                   (js/PIXI.Point. (- from-x-center ox)
                                   (+ from-y-center oy))
                   (js/PIXI.Point. (- to-x-center ox)
                                   (+ to-y-center oy))
                   (js/PIXI.Point. (+ to-x-center ox)
                                   (- to-y-center oy))
                   (js/PIXI.Point. (- from-x-center ox)
                                   (+ from-y-center oy))]
               #js[(js/PIXI.Point. (- from-x-center ox)
                                   (- from-y-center oy))
                   (js/PIXI.Point. (+ from-x-center ox)
                                   (+ from-y-center oy))
                   (js/PIXI.Point. (+ to-x-center ox)
                                   (+ to-y-center oy))
                   (js/PIXI.Point. (- to-x-center ox)
                                   (- to-y-center oy))
                   (js/PIXI.Point. (- from-x-center ox)
                                   (- from-y-center oy))])
        func (calc-fun [from-x-center
                        from-y-center]
                       [to-x-center
                        to-y-center])
        center-x (if (< from-x-center to-x-center)
                   (+ from-x-center
                      (/ (- to-x-center
                            from-x-center)
                         2))
                   (+ to-x-center
                      (/ (- from-x-center
                            to-x-center)
                         2)))]
    (swap! render-states assoc id con)
    (swap! render-listener update from (fnil conj #{}) id)
    (swap! render-listener update to (fnil conj #{}) id)
    (when new-state-id
      (aset con "state-id" new-state-id))
    (aset con "draw-type" "connection")
    (aset con "draw-from" from)
    (aset con "draw-to" to)
    (aset con "zIndex" -10)
    (aset con "element-id" id)
    (.addChild con scene-g)
    (.addChild main-container con)
    (.lineStyle scene-g 6 (rgb->hex color) 1)
    #_(.beginFill scene-g (rgb->hex color))
    (.moveTo scene-g from-x-center from-y-center)
    (.lineTo scene-g to-x-center to-y-center)
    #_(.drawPolygon scene-g poly)
    #_(.endFill scene-g)

    (.addChild con scene-g2)
    (when label
      (let [[width height]
            (draw-text-new con
                           center-x
                           (func center-x)
                           200
                           :one-line
                           nil
                           nil
                           label
                           {:horizontal-align :center
                            :vertical-align :center})]
        (rect scene-g2
              (- center-x (/ width 2) 5)
              (- (func center-x) (/ height 2) 5)
              (+ 10 width)
              (+ 10 height)
              "#FFFFFF"
              {})))

    (aset scene-g "interactive" true)
    (aset scene-g "hitArea" (js/PIXI.Polygon. poly))
    (.on scene-g "click"
         (fn [e]
           (when @delete-active?
             (let [con (get @render-states id)
                   main-container (.getChildAt (.-stage (:app @instance)) stages/main-stage)]
               (inform-listener instance id :delete true)
               (delete-container con main-container id)
               (swap! render-listener dissoc id)
               (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/delete (:frame-id @instance) id])
               (stages/render @instance)))
           (when @edit-active?
             (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/edit (:frame-id @instance) id]))
           (when (or (and (not @connection-active?)
                          (not @edit-active?)
                          (not @delete-active?))
                     @investigate-active?)
             (let [time (get-time)
                   mods (modifier e)]
               (if (and (< double-click-threshold-min
                           (- time
                              @double-click-hack)
                           double-click-threshold)
                        (not (:ctrl mods))
                        (< double-click-threshold-trigger
                           (- time
                              @last-double-click-hack)))
                 (do
                   (reset! last-double-click-hack (get-time))
                   (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.main-view/investigate-figure-connection (:frame-id @instance) id]))
                 (reset! double-click-hack time))))))))

(defn- event [instance main-container element new-state-id]
  (let [{:keys [type pos element-id id color]}
        element
        event (if (= :event type)
                (lookup-event element-id)
                (lookup-context element-id))
        data (reduce (fn [acc [type values]]
                       (if (#{:properties :contexts} type)
                         (reduce (fn [acc value]
                                   (case type
                                     :properties
                                     (let [[name _ val] value]
                                       (assoc acc name val))
                                     :contexts
                                     (let [[name val] value]
                                       (assoc acc name val))
                                     acc))
                                 acc
                                 values)
                         (assoc acc (name type) values)))
                     {}
                     event)
        [offset-x offset-y] pos
        field-assignments (get (get init-data 3) type)
        zoom-level 4 ;(get-in state [[:pos stages/main-stage] :zoom])
        dict @(fi/call-api [:i18n :get-labels-sub])
        layout ((get layout-desc type) color false)
        payload
        {:card data}
        con (js/PIXI.Container.)
        scene-g (js/PIXI.Graphics.)]

    (swap! render-states assoc id con)
    (aset con "state-id" new-state-id)
    (aset con "element-id" id)
    (aset con "draw-type" "event")
    (.addChild con scene-g)
    (.addChild main-container con)
    (doseq [zoom-level (reverse (range 1 zoom-level))
            :let [{:keys [scene interactable content]} (case type
                                                         :box (box data)
                                                         (second (get layout (dec zoom-level))))]]
      (doseq [[type x y w h props] scene]
        (when debugging? ;TODO r10/layouts DEBUGGING ONLY - comment out when done
          (debug-border main-container
                        x
                        y
                        w h))
        (case type
          :rect (rect scene-g
                      x
                      y
                      w h
                      (if (= (:color props) :layout-color)
                        color
                        (:color props))
                      props)
          :rect-rounded (rect scene-g
                              x
                              y
                              w h
                              (if (= (:color props) :layout-color)
                                color
                                (:color props))
                              (assoc props :rounded? true))
          :svg (img scene-g
                    (:id props)
                    x
                    y
                    w h
                    props)))

      (doseq [[type x y w h props] interactable
              :when (or (not (:attribute props))
                        (object? (get data "annotation"))
                        (map? (get data "annotation")))
              :let [g (js/PIXI.Graphics.)]]
        (.addChild con g)
        (when debugging? ;TODO r10/layouts DEBUGGING ONLY - comment out when done
          (debug-border main-container
                        x
                        y
                        w h))
        (case type
          :rect (do
                  (when (:fn props)
                    ((:fn props) instance g id))
                  (rect g
                        x
                        y
                        w h
                        (if (= (:color props) :layout-color)
                          color
                          (:color props))
                        props))
          :rect-rounded (do
                          (when (:fn props)
                            ((:fn props) instance g id))
                          (rect g
                                x
                                y
                                w h
                                (if (= (:color props) :layout-color)
                                  color
                                  (:color props))
                                (assoc props :rounded? true)))
          :svg (img g
                    (:id props)
                    x
                    y
                    w h
                    (if (:interaction-fn props)
                      (assoc (dissoc props :interaction-fn)
                             :interaction ((:interaction-fn props)
                                           instance
                                           g
                                           id
                                           payload))
                      props))))
      (mapv (fn [field-ctn [icon attribute]]
              (doseq [[type x y w h props] field-ctn]
                (when debugging? ;TODO r10/layouts DEBUGGING ONLY - comment out when done
                  (debug-border main-container
                                x
                                y
                                w h))
                (case type
                  :gicon (img con
                              (layout-icon->svg icon)
                              x
                              y
                              w h
                              props)
                  :title (draw-text-new con
                                        x
                                        y
                                        w h
                                        nil
                                        dict
                                        attribute
                                        props)
                  :value (draw-text-new con
                                        x
                                        y
                                        w h
                                        data
                                        dict
                                        attribute
                                        props))))
            content
            field-assignments))
    (nav/set-transform-main-pos con
                                offset-x
                                offset-y)))

(defn draw-card-static [id state-id instance element]
  (let [container (get @render-states id)]
    (when (or (not container)
              (not= state-id (aget container "state-id")))
      (let [state @instance
            app (:app state)
            main-container (.getChildAt (.-stage app) stages/main-stage)
            new-state-id (str (random-uuid))]
        (delete-container container main-container id)
        ((case (:type element)
           (:context :event) event
           :connection connection)
         instance main-container element new-state-id)
        new-state-id))))

(defn clean-up-drawing [instance active-ids]
  (let [state @instance
        app (:app state)
        main-container (.getChildAt (.-stage app) stages/main-stage)
        children @render-states]
    (doseq [[element-id child] children]
      (when-not (active-ids element-id)
        (delete-container child main-container element-id)))))

(defn- inform-listener [instance id action publish?]
  (doseq [update-id (get @render-listener id)
          :when (get @render-states update-id)
          :let [app (:app @instance)
                update-con (get @render-states update-id)
                new-state-id (str (random-uuid))
                main-container (.getChildAt (.-stage app) stages/main-stage)
                state {:id update-id
                       :from (aget update-con "draw-from")
                       :to (aget update-con "draw-to")}]]
    (delete-container update-con main-container update-id)
    (condp = (aget update-con "draw-type")
      "event" (event instance update-con nil nil) ;TODO
      "connection" (case action
                     :delete (when publish?
                               (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/delete (:frame-id @instance) update-id]))
                     :move (do (connection instance
                                           main-container
                                           state
                                           new-state-id)
                               (when publish?
                                 (re-frame/dispatch [:de.explorama.frontend.knowledge-editor.canvas/update-state-id id (:frame-id @instance) new-state-id])))))))
