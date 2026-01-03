(ns de.explorama.frontend.mosaic.render.draw.common-cards
  (:require [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.actions :as gra]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.draw.text-handler :as text-handler]
            [de.explorama.frontend.mosaic.render.draw.color :as color]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as common]
            [de.explorama.frontend.mosaic.vis.details :as details]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error]]))

(def hightlight-color [255 0 255])

;DEBUGGING ONLY - comment out when done
(def ^:private debugging? true)
(when debugging?
  (error "Card rendering is currently in debugging mode"))

(defn- layout-icon->svg [icon]
  (get {"location" "pin"
        "else" "info"
        "notes" "note"
        "organisation" "group"
        "search" "search"}
       icon icon))

(defn- debugging-border [instance stage
                         x
                         y
                         w h
                         color
                         props]
  (let [border 2]
    (gre/rect instance stage
              x
              y
              w
              border
              color
              (assoc props :debug-level? true :z-index 3))
    (gre/rect instance stage
              (+ x w)
              y
              border
              h
              color
              (assoc props :debug-level? true :z-index 3))
    (gre/rect instance stage
              x
              (+ y h)
              w
              border
              color
              (assoc props :debug-level? true :z-index 3))
    (gre/rect instance stage
              x
              y
              border
              h
              color
              (assoc props :debug-level? true :z-index 3))))

(defn- details-interaction [instance stage frame-id {data :card
                                                     data-key :data-key
                                                     render-path :render-path
                                                     row-major-index :row-major-index
                                                     grouped? :grouped?
                                                     read-only? :read-only?}]
  (when-not (common/setting :disable-canvas-dbl-click?)
    (gre/interaction-primitive instance
                               stage
                               "dblclick"
                               (fn [_ _m e]
                                 (re-frame/dispatch
                                  [::details/prepare-add-to-details-view
                                   frame-id
                                   data-key
                                   (aget e "data" "originalEvent")]))
                               nil
                               0))
  (when-not (common/setting :disable-canvas-right-click?)
    (when (not read-only?)
      (gre/interaction-primitive instance
                                 stage
                                 "rightclick"
                                 (fn [_ m e]
                                   (let [{:keys [pmx pmy]} (gre/state instance)]
                                    ;;panning state is always true here, because mouse-down is always before click
                                     (when (and (nil? pmx)
                                                (nil? pmy))
                                       (re-frame/dispatch
                                        [:de.explorama.frontend.mosaic.interaction.context-menu.canvas/canvas
                                         (gp/canvas frame-id)
                                         (aget e "data" "originalEvent")
                                         (update m
                                                 1
                                                 (fn [y] (if grouped?
                                                           (- y 60)
                                                           y)))
                                         :card
                                         {:card data
                                          :data-key data-key
                                          :render-path render-path
                                          :row-major-index row-major-index
                                          :grouped? grouped?}]))))
                                 nil
                                 0)))
  (when-not (common/setting :disable-canvas-highlight?)
    (when (not read-only?)
      (gre/interaction-primitive instance
                                 stage
                                 "click"
                                 (fn [mods _coords]
                                   (when (and (:ctrl mods)
                                              (= :raster (get-in (gre/state instance) [:contexts common/main-stage-index [] :render-type])))
                                     (let [new-element (gdal/select-keys data
                                                                         ["id" "location"])
                                           new-element (gdal/g-> new-element)
                                           highlights (:highlights (gre/state instance))
                                           data-id (grc/get-id data-key)]
                                       (gre/assoc-state! instance [:highlights (cond (nil? highlights)
                                                                                     #{data-id}
                                                                                     (highlights data-id)
                                                                                     (disj highlights data-id)
                                                                                     :else
                                                                                     (conj highlights data-id))])
                                       (gre/update-highlights instance {:render-path render-path
                                                                        :grouped? grouped?
                                                                        :row-major-index row-major-index})
                                       (gra/publish-highlight instance
                                                              (gp/canvas frame-id)
                                                              (cond (nil? highlights)
                                                                    :select
                                                                    (highlights data-id)
                                                                    :deselect
                                                                    :else
                                                                    :select)
                                                              new-element))))
                                 nil
                                 0))))

(def field-count->layout-desc
  {8 "layout6"
   6 "layout4"
   4 "layout2"})

(def ^:private radius 6)

(defn- tile-properties [background-color]
  (let [[font-color-top-bot-t font-color-top-bot-v] (color/font-color background-color 0.6)
        [font-color-top-bot-t-2 font-color-top-bot-v-2] (color/font-color background-color 0.55)
        [font-color-light-mid-t font-color-light-mid-v] (color/font-color "#FFFFFF" 0.4)
        [font-color-dark-mid-t font-color-dark-mid-v] (color/font-color "#1B1C1E" 0.4)
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
     :font-color-light-mid-t font-color-light-mid-t
     :font-color-light-mid-v font-color-light-mid-v
     :font-color-dark-mid-t font-color-dark-mid-t
     :font-color-dark-mid-v font-color-dark-mid-v
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
                                                  :radius radius}]]
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
    {"layout2" (fn [background-color mode highlighted?]
                 (let [{:keys [font-color-top-bot-t
                               font-color-top-bot-v
                               font-color-top-bot-t-2
                               font-color-top-bot-v-2
                               font-color-light-mid-t
                               font-color-light-mid-v
                               font-color-dark-mid-t
                               font-color-dark-mid-v
                               bot-field-props-t
                               bot-field-props-v
                               top-bot-field-props-t-2
                               top-bot-field-props-v-2]}
                       (tile-properties background-color)
                       layout-id (case mode
                                   :light "layout2-light"
                                   :dark "layout2-dark"
                                   "layout2-light")
                       mid-field-props-v {:size-small 23
                                          :size-big 52
                                          :color (case mode
                                                   :light font-color-light-mid-v
                                                   :dark font-color-dark-mid-v
                                                   font-color-light-mid-v)}
                       mid-field-props-v-2 {:size-small 32
                                            :size-big 64
                                            :color (case mode
                                                     :light font-color-light-mid-v
                                                     :dark font-color-dark-mid-v
                                                     font-color-light-mid-v)}
                       mid-field-props-t {:color (case mode
                                                   :light font-color-light-mid-t
                                                   :dark font-color-dark-mid-t
                                                   font-color-light-mid-t)
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
     "layout4" (fn [background-color mode highlighted?]
                 (let [{:keys [font-color-top-bot-t
                               font-color-top-bot-v
                               font-color-top-bot-t-2
                               font-color-top-bot-v-2
                               font-color-light-mid-t
                               font-color-light-mid-v
                               font-color-dark-mid-t
                               font-color-dark-mid-v
                               bot-field-props-t
                               bot-field-props-v
                               top-bot-field-props-t-2
                               top-bot-field-props-v-2]}
                       (tile-properties background-color)
                       layout-id (case mode
                                   :light "layout4-light"
                                   :dark "layout4-dark"
                                   "layout4-light")
                       mid-field-props-v {:size-small 19
                                          :size-big 32
                                          :color (case mode
                                                   :light font-color-light-mid-v
                                                   :dark font-color-dark-mid-v
                                                   font-color-light-mid-v)}
                       mid-field-props-v-2 {:size-small 30
                                            :size-big 42
                                            :color (case mode
                                                     :light font-color-light-mid-v
                                                     :dark font-color-dark-mid-v
                                                     font-color-light-mid-v)}
                       mid-field-props-t {:color (case mode
                                                   :light font-color-light-mid-t
                                                   :dark font-color-dark-mid-t
                                                   font-color-light-mid-t)}]
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
     "layout6" (fn [background-color mode highlighted?]
                 (let [{:keys [font-color-top-bot-t
                               font-color-top-bot-v
                               font-color-top-bot-t-2
                               font-color-top-bot-v-2
                               font-color-light-mid-t
                               font-color-light-mid-v
                               font-color-dark-mid-t
                               font-color-dark-mid-v
                               bot-field-props-t
                               bot-field-props-v
                               top-bot-field-props-t-2
                               top-bot-field-props-v-2]}
                       (tile-properties background-color)
                       layout-id (case mode
                                   :light "layout6-light"
                                   :dark "layout6-dark"
                                   "layout6-light")
                       mid-field-props-v {:size-small 19
                                          :size-big 32
                                          :color (case mode
                                                   :light font-color-light-mid-v
                                                   :dark font-color-dark-mid-v
                                                   font-color-light-mid-v)}
                       mid-field-props-v-2 {:size-small 30
                                            :size-big 42
                                            :color (case mode
                                                     :light font-color-light-mid-v
                                                     :dark font-color-dark-mid-v
                                                     font-color-light-mid-v)}
                       mid-field-props-t {:color (case mode
                                                   :light font-color-light-mid-t
                                                   :dark font-color-dark-mid-t
                                                   font-color-light-mid-t)}]
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

(defn draw-card-static- [coords data data-key meta constraints
                         zoom-level instance _root-container-idx stage
                         field-assignments layout-desc color
                         offset-x offset-y render-path
                         row-major-index _data-index frame-id grouped?
                         x-relative y-relative highlights]
  (let [[offset-x offset-y] (coords offset-x offset-y row-major-index meta constraints x-relative y-relative)
        {:keys [init-pending-read-only? init-interaction-mode theme]} (gre/state instance)
        read-only? (or init-pending-read-only?
                       (= init-interaction-mode :read-only))
        layout-desc (layout-desc color theme (highlights (grc/get-id data-key)))
        dict (gre/label-dict instance)
        payload {:card data
                 :data-key data-key
                 :render-path render-path
                 :row-major-index row-major-index
                 :grouped? grouped?
                 :read-only? read-only?}]
    (when (and field-assignments layout-desc color)
      (let [[_ {:keys [scene interactable content]}] (get layout-desc (dec zoom-level))]
        (doseq [[type x y w h props] scene]
          (when debugging?
            (debugging-border instance stage
                              (+ offset-x x)
                              (+ offset-y y)
                              w h
                              "#FF10F0"
                              {:a 3}))
          (case type
            :rect (gre/rect instance
                            stage
                            (+ offset-x x)
                            (+ offset-y y)
                            w h
                            (if (= (:color props) :layout-color)
                              color
                              (:color props))
                            props)
            :rect-rounded (gre/rect instance
                                    stage
                                    (+ offset-x x)
                                    (+ offset-y y)
                                    w h
                                    (if (= (:color props) :layout-color)
                                      color
                                      (:color props))
                                    (assoc props :rounded? true))
            :svg (gre/img instance
                          stage
                          (:id props)
                          (+ offset-x x)
                          (+ offset-y y)
                          w h
                          props)))

        (doseq [[type x y w h props] interactable
                :when (or (not (:attribute props))
                          (object? (gdal/get data "annotation"))
                          (map? (gdal/get data "annotation")))]
          (when debugging?
            (debugging-border instance stage
                              (+ offset-x x)
                              (+ offset-y y)
                              w h
                              "#FF10F0"
                              {:a 3}))
          (case type
            :rect (do
                    ((:fn props) instance stage frame-id payload)
                    (gre/rect instance stage
                              (+ offset-x x)
                              (+ offset-y y)
                              w h
                              (if (= (:color props) :layout-color)
                                color
                                (:color props))
                              props))
            :rect-rounded (do
                            (when (:fn props)
                              ((:fn props) instance stage frame-id payload))
                            (gre/rect instance stage
                                      (+ offset-x x)
                                      (+ offset-y y)
                                      w h
                                      (if (= (:color props) :layout-color)
                                        color
                                        (:color props))
                                      (assoc props :rounded? true)))
            :svg (gre/img instance
                          stage
                          (:id props)
                          (+ offset-x x)
                          (+ offset-y y)
                          w h
                          (if (:interaction-fn props)
                            (assoc (dissoc props :interaction-fn)
                                   :interaction ((:interaction-fn props)
                                                 frame-id
                                                 payload))
                            props))))
        (mapv (fn [field-ctn [icon attribute]]
                (doseq [[type x y w h props] field-ctn]
                  (when debugging?
                    (debugging-border instance stage
                                      (+ offset-x x)
                                      (+ offset-y y)
                                      w h
                                      "#FF10F0"
                                      {:a 3}))
                  (case type
                    :gicon (gre/img instance
                                    stage
                                    (layout-icon->svg icon)
                                    (+ offset-x x)
                                    (+ offset-y y)
                                    w h
                                    props)
                    :title (text-handler/draw-text-new instance stage
                                                       (+ offset-x x)
                                                       (+ offset-y y)
                                                       w h
                                                       nil
                                                       dict
                                                       attribute
                                                       props)
                    :value (text-handler/draw-text-new instance stage
                                                       (+ offset-x x)
                                                       (+ offset-y y)
                                                       w h
                                                       data
                                                       dict
                                                       attribute
                                                       props))))
              content
              field-assignments)))))

(defn draw-card-loadscreen- [coords data meta {:keys [card-height card-width] :as constraints}
                             instance _root-container-idx stage offset-x offset-y
                             row-major-index _data-idx _path _grouped? x-relative y-relative
                             _highlights]
  (let [color (grc/get-color data)
        [x y] (coords offset-x offset-y row-major-index meta constraints x-relative y-relative)]
    (gre/rect instance
              stage
              x
              y
              card-width
              card-height
              (if (= "#ffffff" color)
                config/white-replacement
                color))))
