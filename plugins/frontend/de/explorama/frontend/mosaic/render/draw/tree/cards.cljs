(ns de.explorama.frontend.mosaic.render.draw.tree.cards
  (:require [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.render.draw.text-handler :as text-handler]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.parameter :as grp]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [de.explorama.frontend.mosaic.interaction.state :as tooltip]
            ["pixi.js-legacy" :refer [Container Graphics]]))

(def black [0 0 0])

(def white [255 255 255])

(def blue [46 64 87])

(def font-size-factor 0.65)

(def font-size-margin (* (- 1 font-size-factor) 0.5))

(defn- theme-color [theme]
  (case theme :light "#FFFFFF" :dark "#1B1C1E" "#FFFFFF"))

; currently unused
(defn frame-text [instance stage header title overview-factor optional-desc]
  (let [{:keys [start-x start-y end-x]} optional-desc]
    (text-handler/draw-text-new instance
                                stage
                                (+ start-x 10) ;(+ x (* header 0.2) border)
                                (+ start-y 10) ;(+ y border (* font-size-margin header))
                                (* (- end-x start-x) 0.95) ;(* width 0.97)
                                :one-line
                                nil
                                nil
                                title
                                {:size (* overview-factor (* header font-size-factor))
                                 :color white
                                 :adjust-width? true})))

(defn get-data
  ([path data-path idx]
   [(gdal/get (get-data path data-path)
              idx)])
  ([path data-path idx _ {{:keys [mapping]} :optional-desc}]
   (let [data (get-data path data-path)
         idx-map (get mapping idx)]
     (mapv (fn [[idx x-relative y-relative]]
             [(gdal/get data idx) idx x-relative y-relative])
           idx-map)))
  ([path data-path]
   (gdal/get-in (gdb/get-events path)
                data-path)))

(def border 2)

(defn- leaf-body [instance stage-parent overview-factor title-desc render-path optional-desc]
  (let [{:keys [start-x start-y end-x end-y color]} optional-desc
        {:keys [path theme]}
        (gre/state instance)
        data-path (pc/data-path render-path)
        stage (Container.)
        frame-id (gre/frame-id instance)]
    (.addChild ^js stage-parent stage)
    (.addChild stage (Graphics.))
    (.addChild stage (Graphics.))
    (gre/interaction-primitive instance
                               stage
                               "dblclick"
                               (fn [_ _ _e]
                                 (when-not (:inspector? (gre/state instance))
                                   (let [data (gdal/second (get-data path data-path))
                                         main-stage (.-stage ^js (gre/app instance))
                                         {:keys [width height]} (gre/args instance)
                                         inspector-margin-x 50
                                         inspector-margin-y 50
                                         inspector-header-y 0
                                         size-x (* 0.8 width)
                                         size-y (* 0.8 height)
                                         inspector-stage (Container.)
                                         main-container (Container.)
                                         axes-container (Container.)
                                         background-container (Container.)
                                         contexts
                                         (grp/grp-contexts data
                                                           nil
                                                           {:type :raster}
                                                           (select-keys (gre/state instance)
                                                                        [:constraints :di :path])
                                                           {:width size-x :height (- size-y inspector-header-y)
                                                            :optional-desc {:inspector-width size-x
                                                                            :inspector-height (- size-y inspector-header-y)
                                                                            :inspector-margin-x (* width 0.1)
                                                                            :inspector-margin-y (* height 0.1)
                                                                            :inspector-header-y inspector-header-y}}
                                                           [nil nil]
                                                           nil
                                                           nil
                                                           nil)
                                         max-zoom (get-in contexts [[] :params :max-zoom])]
                                     (gre/assoc-state! instance [:inspector-idx pc/inspector-stage-index
                                                                 :inspector? true
                                                                 :inspector-init {pc/inspector-stage-index false}
                                                                 :custom-data {pc/inspector-stage-index {:path data-path
                                                                                                         :grouped? true}}])
                                     (gre/assoc-state! instance
                                                       [[:pos pc/inspector-stage-index]
                                                        {:x 0
                                                         :y 0
                                                         :z 0
                                                         :zoom 0
                                                         :next-zoom 0}])
                                     (gre/assoc-in-state! instance
                                                          [:contexts pc/inspector-stage-index]
                                                          contexts)
                                     (.addChildAt ^js main-stage
                                                  inspector-stage
                                                  pc/inspector-stage-index),
                                     (aset inspector-stage "name" "inspector-stage")
                                     (aset main-container "name" "inspector-main-container")
                                     (aset axes-container "name" "inspector-axes-container")
                                     (aset background-container "name" "inspector-background-container")
                                     (.addChildAt inspector-stage
                                                  background-container
                                                  (pc/background-container pc/inspector-stage-index))
                                     (.addChildAt inspector-stage
                                                  main-container
                                                  (pc/main-container pc/inspector-stage-index))
                                     (.addChildAt inspector-stage
                                                  axes-container
                                                  (pc/axes-container pc/inspector-stage-index))
                                     (.addChildAt axes-container
                                                  (Container.)
                                                  (pc/axes-background-container pc/inspector-stage-index))
                                     (.addChildAt axes-container
                                                  (Container.)
                                                  (pc/axes-text-container pc/inspector-stage-index))
                                     (gre/move-to! instance pc/inspector-stage-index
                                                   (+ (/ inspector-margin-x max-zoom))
                                                   (+ (/ inspector-margin-y max-zoom))
                                                   max-zoom (if (< (count data) 17) 1 0))
                                     (gre/update-zoom instance pc/inspector-stage-index)
                                     (tooltip/block-tooltips frame-id))))
                               nil
                               1)
    (gre/interaction-primitive instance
                               stage
                               "hover"
                               (fn [_ m _ action]
                                 (case action
                                   :show
                                   (tooltip/show-tooltip {:type :treemap
                                                          :pos m
                                                          :text title-desc
                                                          :frame-id frame-id})
                                   :move
                                   (tooltip/update-tooltip frame-id {:pos m})
                                   :hide
                                   (tooltip/hide-tooltip)))
                               path
                               1
                               {:track-move? true})
    (when optional-desc
      (gre/rect instance
                stage
                (* start-x overview-factor)
                (* start-y overview-factor)
                (* (- end-x start-x) overview-factor)
                (* (- end-y start-y) overview-factor)
                color
                {:interactive? true
                 :outline {:width 50
                           :color (theme-color theme)}}))))

(defn- group-body [instance stage-parent overview-factor optional-desc]
  (let [{:keys [start-x start-y end-x end-y]} optional-desc
        {:keys [theme]} (gre/state instance)
        stage (Container.)]
    (.addChild ^js stage-parent stage)
    (.addChild ^js stage (Graphics.))
    (.addChild ^js stage (Graphics.))
    (when optional-desc
      (gre/rect instance
                stage
                (* start-x overview-factor)
                (* start-y overview-factor)
                (* (- end-x start-x) overview-factor)
                (* (- end-y start-y) overview-factor)
                [255 255 255]
                {:interactive? true
                 :a 0
                 :outline {:width 300
                           :color (theme-color theme)}}))))

(defn render-base-static [instance stage-key _render-path stage _contraints _ctx _highlights _parent-grouped? _]
  (doseq [[ctx-path ctx] (get-in (gre/state instance) [:contexts stage-key])
          :when (= :leaf (:ctx-type ctx))
          :let [{:keys [title optional-desc]} ctx]]
    (leaf-body instance stage 1 title ctx-path optional-desc))
  (doseq [[ctx-path ctx] (get-in (gre/state instance) [:contexts stage-key])
          :when (and (= :group (:ctx-type ctx))
                     (= 1 (count ctx-path)))
          :let [{:keys [optional-desc]} ctx]]
    (group-body instance stage 1 optional-desc))
  [[]
   []])