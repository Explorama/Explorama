(ns de.explorama.frontend.mosaic.render.pixi.core
  (:require ["@pixi/core"]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [de.explorama.frontend.mosaic.render.pixi.lod :as pl]
            [de.explorama.frontend.mosaic.render.pixi.mouse :as pm]
            [de.explorama.frontend.mosaic.render.pixi.navigation :as pn]
            [de.explorama.frontend.mosaic.render.pixi.shapes :as ps]
            ["pixi.js" :refer [Container Graphics Texture utils Application]] 
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]))

(defn- update-highlights-cards [this state stage-key render-path row-major-index]
  (let [wrapper-indices-path [stage-key :wrapper render-path row-major-index]
        wrapper-container (get-in state [:indices wrapper-indices-path 0])]
    (when (and (get-in state [[:pos stage-key] :init 1])
               wrapper-container)
      (.removeChild (.-parent wrapper-container)
                    wrapper-container)
      (.destroy wrapper-container #js {"children" true})
      (gre/assoc-in-state! this [:indices wrapper-indices-path] nil)
      (letfn [(content-path [zoom]
                [:indices [stage-key :content :static zoom render-path row-major-index]])]
        (doseq [zoom (range 1 4)]
          (when (get-in state [[:pos stage-key] :init zoom])
            (gre/assoc-in-state! this (content-path 1) nil)
            (gre/assoc-in-state! this (content-path 2) nil)
            (gre/assoc-in-state! this (content-path 3) nil)))))))

(defn- update-highlights-cards-delete-all [this state stage-key render-path]
  (when (get-in state [[:pos stage-key] :init 1])
    (let [indices (filter (fn [[[current-stage-key type] _]]
                            (and (= stage-key current-stage-key)
                                 (= type :wrapper)))
                          (get state :indices))]
      (doseq [[idx] indices
              :let [[_ _ _ row-major-index] idx
                    wrapper-container (get-in state [:indices idx 0])]]
        (when (and (get-in state [[:pos stage-key] :init 1])
                   wrapper-container)
          (.removeChild (.-parent wrapper-container)
                        wrapper-container)
          (.destroy wrapper-container #js {"children" true})
          (gre/assoc-in-state! this [:indices idx] nil)
          (letfn [(content-path [zoom]
                    [:indices [stage-key :content :static zoom render-path row-major-index]])]
            (doseq [zoom (range 1 4)]
              (when (get-in state [[:pos stage-key] :init zoom])
                (gre/assoc-in-state! this (content-path 1) nil)
                (gre/assoc-in-state! this (content-path 2) nil)
                (gre/assoc-in-state! this (content-path 3) nil)))))))))

(defn- update-highlights-0 [this state stage-key render-funcs render-path grouped? relevant-highlights]
  (when (get-in state [[:pos stage-key] :init 0])
    (let [{:keys [contexts constraints cache highlights]} state
          ctx (get-in contexts [stage-key render-path])
          relevant-highlights (or relevant-highlights
                                  ((get-in render-funcs [(pl/render-type ctx) :relevant-highlights])
                                   this stage-key render-path ctx highlights grouped? pl/cluster-data))
          static-indices-path [stage-key :base :static 0 render-path]
          frame-container (get cache static-indices-path)]
      (when frame-container
        (let [capsule-contaier (.getChildAt frame-container 0)
              highlight-container (.getChildAt capsule-contaier 3)]
          (doseq [highlight-sprite (.-children highlight-container)]
            (.removeChild highlight-container highlight-sprite)
            (.destroy highlight-sprite #js {"children" true}))
          (.addChildAt highlight-container (Graphics.) 0)
          (when (and relevant-highlights (not= 0 (count relevant-highlights)))
            ((get-in render-funcs [(pl/render-type ctx) :highlights-0])
             this highlight-container constraints ctx relevant-highlights)))))))

(deftype Pixi [^:unsynchronized-mutable app
               render-funcs
               ^:unsynchronized-mutable args
               ^:unsynchronized-mutable state]
  gre/Engine
  (gre/rect [_ stage x y h w c {interactive? :interactive? stage-num :stage debug-level? :debug-level? :or {stage-num 1} :as args}]
    (when-not (:headless state)
      (cond interactive?
            (ps/rect (.getChildAt stage stage-num) x y h w c args)
            debug-level?
            (let [g (Graphics.)]
              (when (:z-index args)
                (aset g "zIndex" (:z-index args)))
              (aset stage "sortableChildren" true)
              (.addChild stage g)
              (ps/rect g x y h w c args))
            :else
            (ps/rect (.getChildAt stage 0) x y h w c args))))

  (gre/rect [_ stage x y h w c]
    (when-not (:headless state)
      (ps/rect (.getChildAt stage 0) x y h w c {})))

  (gre/circle [_ stage x y r c]
    (when-not (:headless state)
      (ps/circle (.getChildAt stage 0) x y r c {})))

  (gre/circle [_ stage x y r c {interactive? :interactive? stage-num :stage :or {stage-num 1} :as args}]
    (when-not (:headless state)
      (if interactive?
        (ps/circle (.getChildAt stage stage-num) x y r c args)
        (ps/circle (.getChildAt stage 0) x y r c args))))

  (gre/polygon [_ stage points c {interactive? :interactive? stage-num :stage :or {stage-num 1} :as args}]
    (when-not (:headless state)
      (if interactive?
        (ps/polygon (.getChildAt stage stage-num) points c args)
        (ps/polygon (.getChildAt stage 0) points c args))))

  (gre/polygon [_ stage points c]
    (when-not (:headless state)
      (ps/polygon (.getChildAt stage 0) points c {})))

  (gre/point [_ x y]
    (when-not (:headless state)
      (ps/point x y)))

  (gre/text [_ stage text-str x y w h args]
    (when-not (:headless state)
      (ps/text stage text-str x y w h args)))

  (gre/text [_ stage text-str x y w h]
    (when-not (:headless state)
      (ps/text stage text-str x y w h {})))

  (gre/img [_ stage id x y w h args]
    (when-not (:headless state)
      (ps/img stage id x y w h args)))

  (gre/img [_ stage id x y w h]
    (when-not (:headless state)
      (ps/img stage id x y w h {})))

  (gre/interaction-primitive [_ stage on func path stage-num]
    (when-not (:headless state)
      (ps/interaction-primitive stage on func path stage-num)))
  (gre/interaction-primitive [_ stage on func path stage-num opts]
    (when-not (:headless state)
      (ps/interaction-primitive stage on func path stage-num opts)))

  (gre/move-to! [this stage-key index]
    (when-not (:headless state)
      (pn/move-to! this stage-key index)))

  (gre/move-to! [this stage-key x y z]
    (pn/move-to! this stage-key x y z))

  (gre/move-to! [this stage-key x y]
    (pn/move-to! this stage-key x y))

  (gre/move-to! [this stage-key x y z zoom]
    (pn/move-to! this stage-key x y z zoom))

  (gre/update-zoom [this stage-key]
    (when-not (:headless state)
      (pl/update-zoom this stage-key)))

  (gre/update-theme [_ color]
    (when-not (:headless state)
      (aset (.-renderer app) "background" "color" color)))

  (gre/state [_]
    state)

  (gre/label-dict [_]
    (:label-dict state))

  (gre/frame-id [_]
    (:frame-id state))

  (gre/dissoc-state! [_ values]
    (set! state (apply dissoc state values)))

  (gre/merge-state! [_ values]
    (set! state (merge state values)))

  (gre/merge-in-state! [_ path values]
    (set! state (update state path merge values)))

  (gre/set-state! [_ new-state]
    (set! state new-state))

  (gre/assoc-in-state! [_ path value]
    (set! state (assoc-in state path value)))

  (gre/assoc-state! [_ values]
    (set! state (apply assoc state values)))

  (gre/dirty? [_]
    (:dirty state))

  (gre/dirty! [_ value]
    (set! state (assoc state :dirty value)))

  (gre/set-args! [_ new-args]
    (set! args new-args))

  (gre/args [_]
    args)

  (gre/app [_]
    app)

  (gre/set-app! [_ new-app]
    (set! app new-app))

  (gre/render-funcs [_]
    render-funcs)

  (gre/resize [instance new-width new-height]
    (gre/set-args! instance (assoc args :width new-width :height new-height))
    (when-not (:headless state)
      (.resize (.-renderer app) new-width new-height)))

  (gre/destroy [_]
    (when-not (:headless state)
      (doseq [[on func opts] (:listener state)
              :let [canvas (.-view app)]]
        (.removeEventListener canvas on func (clj->js opts)))
      (.destroy app false (clj->js {:children true}))))

  (gre/update-highlights [this {:keys [render-path grouped? row-major-index move-to select?]}]
    (when-not (:headless state)
      (if (and render-path row-major-index)
        (doseq [stage-key (if (:inspector? state)
                            [pc/main-stage-index pc/inspector-stage-index]
                            [pc/main-stage-index])]
          (update-highlights-cards this state stage-key render-path row-major-index)
          (update-highlights-0 this state stage-key render-funcs render-path grouped? nil))
        (doseq [stage-key (if (:inspector? state)
                            [pc/main-stage-index pc/inspector-stage-index]
                            [pc/main-stage-index])]
          (let [{:keys [contexts highlights]} state
                state-context (get contexts stage-key)
                render-paths (group-by (fn [render-path]
                                         (count render-path))
                                       (keys state-context))
                max-key (reduce max (keys render-paths))
                moved? (atom false)]
            (doseq [render-path (get render-paths max-key)]
              (let [{:keys [card-width card-height card-margin]} (:constraints state)
                    {render-type :render-type :as ctx} (get state-context render-path)
                    grouped? (< 0 (count render-path))
                    relevant-highlights ((get-in render-funcs [(pl/render-type ctx) :relevant-highlights])
                                         this stage-key render-path ctx highlights
                                         grouped? pl/cluster-data)]
                (when (and move-to
                           (not @moved?))
                  (doseq [[info [_ id-fnc]] relevant-highlights]
                    (when (id-fnc move-to)
                      (let [[x y] ((get-in render-funcs [(pl/render-type ctx) :coords-highlight])
                                   info card-width card-height card-margin)]
                        (pn/move-to! this
                                     stage-key
                                     (if (= :scatter render-type)
                                       (- x)
                                       (- x))
                                     (if (= :scatter render-type)
                                       (- y)
                                       (- y))
                                     1
                                     3))
                      (reset! moved? true))))
                (if select?
                  (doseq [[_ [row-major-index]] relevant-highlights]
                    (update-highlights-cards this state stage-key render-path row-major-index))
                  (update-highlights-cards-delete-all this state stage-key render-path))
                (update-highlights-0 this state stage-key render-funcs render-path grouped? relevant-highlights))))))
      (doseq [stage-key (if (:inspector? state)
                          [pc/main-stage-index pc/inspector-stage-index]
                          [pc/main-stage-index])]
        (gre/update-zoom this stage-key))))

  (gre/focus-event [this event-id]
    (let [render-type (get-in (gre/state this) [:contexts pc/main-stage-index [] :render-type])]
      (when (= :raster render-type)
        (let [stage-key pc/main-stage-index
              {:keys [contexts]} state
              state-context (get contexts stage-key)
              render-paths (group-by (fn [render-path]
                                       (count render-path))
                                     (keys state-context))
              max-key (reduce max (keys render-paths))
              moved? (atom false)]
          (doseq [render-path (get render-paths max-key)]
            (let [{:keys [card-width card-height card-margin]} (:constraints state)
                  ctx (get state-context render-path)
                  grouped? (< 0 (count render-path))
                  relevant-highlights ((get-in render-funcs [(pl/render-type ctx) :relevant-highlights])
                                       this stage-key render-path ctx #{event-id}
                                       grouped? pl/cluster-data)]
              (when (not @moved?)
                (doseq [[info [_ id-fnc]] relevant-highlights]
                  (when (id-fnc event-id)
                    (let [[x y] ((get-in render-funcs [(pl/render-type ctx) :coords-highlight])
                                 info card-width card-height card-margin)]
                      (pn/move-to! this
                                   stage-key
                                   (- x)
                                   (- y)
                                   1
                                   3)))
                  (reset! moved? true))))))
        (gre/update-zoom this pc/main-stage-index))))

  (gre/update-annotations [this {:keys [render-path grouped? row-major-index]}]
    (when-not (:headless state)
      (doseq [stage-key (if (:inspector? state)
                          [pc/main-stage-index pc/inspector-stage-index]
                          [pc/main-stage-index])]
        (let [wrapper-indices-path [stage-key :wrapper render-path row-major-index]
              wrapper-container (get-in state [:indices wrapper-indices-path 0])]
          (when wrapper-container
            (.removeChild (.-parent wrapper-container)
                          wrapper-container)
            (.destroy wrapper-container #js {"children" true})
            (gre/assoc-in-state! this [:indices wrapper-indices-path] nil)
            (letfn [(content-path [zoom]
                      [:indices [stage-key :content :static zoom render-path row-major-index]])]
              (doseq [zoom (range 1 4)]
                (when (get-in state [[:pos stage-key] :init zoom])
                  (gre/assoc-in-state! this (content-path 1) nil)
                  (gre/assoc-in-state! this (content-path 2) nil)
                  (gre/assoc-in-state! this (content-path 3) nil))))))
        (when (get-in state [[:pos stage-key] :init 0])
          (let [{:keys [contexts constraints cache]} state
                ctx (get-in contexts [stage-key render-path])
                relevant-annotations ((get-in render-funcs [(pl/render-type ctx) :relevant-annotations])
                                      this stage-key render-path ctx grouped? pl/cluster-data)
                static-indices-path [stage-key :base :static 0 render-path]
                frame-container (get cache static-indices-path)]
            (when frame-container
              (let [capsule-contaier (.getChildAt frame-container 0)
                    comment-container (.getChildAt capsule-contaier 2)]
                (doseq [comment-sprite (.-children comment-container)]
                  (.removeChild comment-container)
                  (.destroy comment-sprite #js {"children" true}))
                (when (and relevant-annotations (not= 0 (count relevant-annotations)))
                  ((get-in render-funcs [(pl/render-type ctx) :annotations-0])
                   this comment-container constraints ctx relevant-annotations))))))
        (gre/update-zoom this stage-key))))

  (gre/rerender [this stage-key]
    (pl/rerender this stage-key))

  (gre/reset [this stage-key]
    (pl/reset this stage-key))

  (gre/render [this]
    (when-not (:headless state)
      (.render (.-renderer app) (.-stage app))
      (when (:init-headless? state)
        (re-frame/dispatch [:de.explorama.frontend.mosaic.core/render-done-top-level (get state :path)])
        (gre/set-state! this (assoc state :init-headless? false)))))

  (gre/click [_this _event-type _modifier _m]))

(defn text-metrics [text-str args]
  (ps/text-metrics text-str args))

(defn disable-tickers []
  (let [shared-ticker (aget js/PIXI "Ticker" "shared")
        system-ticker (aget js/PIXI "Ticker" "system")]
    (aset system-ticker "autoStart" false)
    (aset shared-ticker "autoStart" false)
    (when (aget system-ticker "started")
      (.stop system-ticker))
    (when (aget shared-ticker "started")
      (.stop shared-ticker))))

(defn init-engine [instance]
  (disable-tickers)
  (when-not (aget instance "app")
    (let [{:keys [contexts background-color] :as state} (gre/state instance)
          {:keys [x y z zoom next-zoom]} (get state [:pos pc/main-stage-index])
          {:keys [width height host]} (gre/args instance)
          canvas (.getElementById js/document host)
          app (Application. (clj->js {:autoStart false
                                   :width width
                                   :height height
                                   :backgroundColor 0xFFFFFF
                                   :antialias false
                                   :roundPixels false
                                   :resolution 2
                                   :autoDensity true
                                   :sharedTicker false
                                                 ;:autoResize true
                                   :forceCanvas true
                                   :view canvas}))
          listener [["wheel" (pm/wheel instance) {:passive false}]
                    ["pointerdown" (pm/mousedown instance) {:passive true}]
                    ["pointermove" (pm/pointermove instance) {:passive true}]
                    ["pointerup" (pm/mouseup instance) {:passive true}]
                    ["pointerleave" (pm/mouseleave instance) {:passive true}]
                    ["pointercancel" (pm/mouseleave instance) {:passive true}]]
          {{:keys [count-ctn max-zoom min-zoom #_bb-min-x #_bb-min-y #_x #_y]} :params
           factor-overview :factor-overview}
          (get-in contexts [pc/main-stage-index []])
          root-container (Container.)
          main-container (Container.)
          axes-container (Container.)
          ui-container (Container.)]
      (aset root-container "name" "root")
      (aset main-container "name" "main")
      (aset axes-container "name" "axes")
      (aset ui-container "name" "ui")
      (gre/set-app! instance app)
      (doseq [[on func opts] listener]
        (.addEventListener canvas on func (clj->js opts)))
      (gre/assoc-state! instance [:listener listener])
      (.addChildAt (.-stage app)
                   root-container
                   pc/main-stage-index)
      (.addChildAt root-container
                   main-container
                   (pc/main-container pc/main-stage-index))
      (.addChildAt root-container
                   axes-container
                   (pc/axes-container pc/main-stage-index))
      (.addChildAt axes-container
                   (Container.)
                   (pc/axes-background-container pc/main-stage-index))
      (.addChildAt axes-container
                   (Container.)
                   (pc/axes-text-container pc/main-stage-index))
      (.addChildAt root-container
                   ui-container
                   (pc/ui-container pc/main-stage-index))
      (cond
        (= 0 x y z next-zoom)
        (gre/move-to! instance
                      pc/main-stage-index
                      x
                      y
                      (cond
                        (and (= 1 count-ctn)
                             (not (= (get-in contexts [pc/main-stage-index [] :ctx-type]) :group)))
                        min-zoom
                        (and z (zero? zoom)
                             (not (nil? factor-overview)))
                        (* z factor-overview)
                        z z
                        :else max-zoom))
        (and x y z)
        (let [stage (pc/zoom-context-stage app pc/main-stage-index)]
          (pn/set-transform-main (pc/main-container stage pc/main-stage-index) x y z)
          (pn/set-transform-main (pc/axes-background-container-direct stage pc/main-stage-index) x y z)))
      (gre/update-theme instance background-color)
      (gre/update-zoom instance pc/main-stage-index)
      instance)))

(defn init [args
            {:keys [headless] :as state}
            render-funcs
            frame-id]
  (let [instance (Pixi. nil
                        render-funcs
                        args
                        (assoc state
                               :frame-id frame-id
                               :init-headless? headless))]
    (if headless
      instance
      (init-engine instance))))

(defonce pixi-init (atom false))

(defn- texture
  "Get texture with given id"
  [texture-id]
  (aget utils.TextureCache texture-id))

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
      (Texture.addToCache (texture.from temp-elem)
                               id)
      (js/document.body.removeChild temp-elem))))

(defn- add-assets [assets]
  (let [path (fn [fname]
               (str  "img/mosaic/" fname ".svg"))]
    (doseq [asset assets]
      (let [[id fname]
            (if (vector? asset)
              asset
              [asset asset])]
        (when (and id fname)
          (cache-texture id (path (or fname id))))))))

(defn load-resources []
  (when-not @pixi-init
    (add-assets [["layout2-light" "eventcard_2"]
                 ["layout4-light" "eventcard_4"]
                 ["layout6-light" "eventcard_6"]
                 ["layout2-dark" "eventcard_dark_2"]
                 ["layout4-dark" "eventcard_dark_4"]
                 ["layout6-dark" "eventcard_dark_6"]])
    (add-assets ["calendar" "clock" "drop" "flame" "globe2" "health" "info" "map" "percentage"
                 "rain" "star" "transfer" "charts" "city" "coin" "euro" "globe" "group" "heart"
                 "leaf" "note" "pin" "sun" "search" "speech-bubble" "circle" "speech-bubble-text"]))
  (reset! pixi-init true))

(disable-tickers)
