(ns de.explorama.frontend.woco.frame.interaction.collision
  (:require [de.explorama.frontend.woco.config :as config]
            [reagent.dom :as dom]
            [de.explorama.frontend.woco.workspace.math :as wwmath]))

(defn drag-inside-frame?
  "Checks if drag is in a frame"
  [frame-comp e]
  (and
   frame-comp
   ;Check if there is a from Element
   (aget e "nativeEvent" "fromElement")
   ;Check if target is a woco frame
   (boolean (and
             (aget e "nativeEvent" "target")
             (-> (aget e "nativeEvent" "target")
                 (.closest (str "." config/window-class)))))))

(defn drag-outside-frame?
  "Checks if drag is not in a frame"
  [frame-comp e]
  ;Check if target is not the same woco-frame or another element (for example frames-workspace)
  (let [rel-target (aget e "nativeEvent" "relatedTarget")
        window-frame (when rel-target (.closest rel-target (str "." config/window-class)))]
    (boolean (and
              frame-comp
              (or
               (nil? rel-target)
               (and window-frame
                    (not= (dom/dom-node @frame-comp)
                          window-frame))
               (not window-frame))))))

(defn filter-visible-frames [^number vp-x ^number vp-y
                             ^number vp-width ^number vp-height
                             ignore-frame-ids
                             frames]
  (let [ignore-frame-ids (or ignore-frame-ids #{})
        check-fn (fn [^number check-x ^number check-y
                      ^number check-w ^number check-h]
                   (wwmath/collide-rects check-x check-y
                                         check-w check-h
                                         vp-x vp-y
                                         vp-width vp-height))]
    (filterv (fn [{fid :id
                   is-minimized? :is-minimized?
                   [^number x ^number y] :coords
                   [^number w ^number h] :full-size}]
               (and (not (ignore-frame-ids fid))
                    (not is-minimized?)
                    (check-fn x y w h)))
             frames)))

(defn inside-other-frame? [{[^number fr-x ^number fr-y] :coords
                            [^number fr-w ^number fr-h] :full-size}
                           frames]
  (let [check-fn (fn [^number check-x ^number check-y
                      ^number check-w ^number check-h]
                   (wwmath/collide-rects check-x check-y
                                         check-w check-h
                                         fr-x fr-y
                                         fr-w fr-h))]
    (some (fn [[_ {[^number x ^number y] :coords
                   [^number w ^number h] :full-size}]]
            (check-fn x y w h))
          frames)))