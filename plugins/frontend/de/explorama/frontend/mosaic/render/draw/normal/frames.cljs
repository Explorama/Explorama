(ns de.explorama.frontend.mosaic.render.draw.normal.frames
  (:require [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.frontend.mosaic.interaction.state :as tooltip]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.common :refer [data-interaction?]]
            [de.explorama.frontend.mosaic.render.draw.text-handler :as text-handler]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [re-frame.core :as re-frame]))

(def black [0 0 0])

(def white [255 255 255])

(def blue [46 64 87])

(def font-size-factor 0.65)

(def font-size-margin (* (- 1 font-size-factor) 0.5))

(defn frame-text [instance stage x y width header border title]
  (text-handler/draw-text-new instance
                              stage
                              (+ x (* header 0.2) border)
                              (+ y border (* font-size-margin header))
                              (* width 0.97)
                              :one-line
                              nil
                              nil
                              title
                              {:size (* header font-size-factor)
                               :color white
                               :adjust-width? true}))

(defn render-text [_mode
                   instance
                   _stage-key
                   stage
                   {:keys [border-grp]}
                   {{:keys [header width]} :params
                    {:keys [short-title]} :title
                    :keys [is-root?]
                    [x y] :offset-absolute}
                   _path
                   render-path]
  (when (and (not= render-path [])
             (not is-root?)
             short-title)
    ;draws informationroom title on canvas (was ignored by old pixi version)
    (frame-text instance stage x y width header border-grp short-title)))

(defn frame-body [instance stage x y width height header border title-desc render-path]
  (let [frame-id (gre/frame-id instance)
        path (gp/top-level frame-id)]
    (when (data-interaction? frame-id)
      (gre/interaction-primitive instance
                                 stage
                                 "rightclick"
                                 (fn [_ m e]
                                   (let [{:keys [pmx pmy]} (gre/state instance)]
                                    ;;panning state is always true here, because mouse-down is always before click
                                     (when (and (nil? pmx)
                                                (nil? pmy))
                                       (re-frame/dispatch [:de.explorama.frontend.mosaic.interaction.context-menu.canvas/canvas
                                                           path
                                                           (aget e "data" "originalEvent")
                                                           m
                                                           :group
                                                           (assoc title-desc
                                                                  :data-path (pc/data-path render-path)
                                                                  :group-type (cond (= (count render-path) 2)
                                                                                    gcp/sub-grp-by-key
                                                                                    (= (count render-path) 1)
                                                                                    gcp/grp-by-key
                                                                                    :else
                                                                                    nil))]))))
                                 nil
                                 1)
      (gre/interaction-primitive instance
                                 stage
                                 "dragndrop"
                                 (fn [_modifier _coords path _e]
                                   (re-frame/dispatch [:de.explorama.frontend.mosaic.operations.util/copy-group-ui-wrapper
                                                       path {:data-path (pc/data-path render-path)
                                                             :overwrite-behavior? true}]))
                                 {:path path
                                  :data-path (pc/data-path render-path)}
                                 1))
    (gre/interaction-primitive instance
                               stage
                               "hover"
                               (fn [_ m _ action]
                                 (case action
                                   :show
                                   (tooltip/show-tooltip {:type :raster
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
    (gre/rect instance
              stage
              x
              y
              width
              border
              black
              {:a 0.2})
    (gre/rect instance
              stage
              x
              (+ border y)
              border
              (- (+ height
                    header)
                 border
                 border)
              black
              {:a 0.2})
    (gre/rect instance
              stage
              (- (+ width x) border)
              (+ border y)
              border
              (- (+ height
                    header)
                 border
                 border)
              black
              {:a 0.2})
    (gre/rect instance
              stage
              x
              (+ y (- (+ header height) border))
              width
              border
              black
              {:a 0.2})
    (gre/rect instance
              stage
              (+ border x)
              (+ border y)
              (- width border border)
              header
              blue
              {:interactive? true})))

(defn render-container [_mode
                        instance
                        _stage-key
                        stage
                        {:keys [border-grp]}
                        {{:keys [header height width]} :params
                         :keys [title]
                         [x y] :offset-absolute}
                        _parent-grouped?
                        render-path]
  (when (not= render-path [])
    (frame-body instance stage x y width height header border-grp title render-path)))