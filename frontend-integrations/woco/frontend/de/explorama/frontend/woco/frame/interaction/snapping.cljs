(ns de.explorama.frontend.woco.frame.interaction.snapping
  (:require [reagent.core :as r]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.workspace.math :refer [collide-rects]]
            [de.explorama.frontend.woco.workspace.background :as wwb]
            [de.explorama.frontend.woco.navigation.control :refer [workspace-position->page]]
            [de.explorama.frontend.woco.navigation.snapping :as snapping]))

(defonce snapped-state (atom {}))
(defonce ^:private snap-state (r/atom {}))

(defn reset-snapping []
  (reset! snapped-state {})
  (reset! snap-state {}))

(defn- ^number check-condition
  "Return hitbox when the hitbox enters from outside, -hitbox when hitbox enters from inside, otherwise nil"
  [^number check-val
   ^number check-coord
   ^number snap-hitbox]
  (let [^number left-border (- check-coord snap-hitbox)
        ^number right-border (+ check-coord snap-hitbox)
        ^boolean inside? (<= left-border check-val right-border)]
    (when inside?
      ;; difference to check-coord (= snap-line)
      (as-> (- check-val check-coord) $
        (if (neg? $)
          (max $ (- snap-hitbox))
          (min $ snap-hitbox))))))

(defn- check-nearest-positions [[^number base-w ^number base-h]
                                ^number check-x ^number check-y
                                ^number snap-hitbox-x ^number snap-hitbox-y
                                ^number trap-hitbox-x ^number trap-hitbox-y
                                frames
                                wsp
                                add-frame-size?]
  (let [wsp->page-fn (partial workspace-position->page wsp)
        inside-frame? (partial collide-rects check-x check-y base-w base-h)
        snap-x-fn (fn [^number x] (check-condition check-x x snap-hitbox-x))
        snap-y-fn (fn [^number y] (check-condition check-y y snap-hitbox-y))
        trap-x-fn (fn [^number x] (check-condition check-x x trap-hitbox-x))
        trap-y-fn (fn [^number y] (check-condition check-y y trap-hitbox-y))]
    (-> (reduce (fn [acc {[^number x ^number y] :coords
                          [^number w ^number h] :full-size}]
                  (let [snap-left (snap-x-fn x)
                        trap-snap-left (trap-x-fn x)
                        snap-right-pos (+ x w)
                        snap-right (snap-x-fn snap-right-pos)
                        trap-snap-right (trap-x-fn snap-right-pos)
                        snap-top (snap-y-fn y)
                        trap-snap-top (trap-y-fn y)
                        snap-bottom-pos (+ y h)
                        snap-bottom (snap-y-fn snap-bottom-pos)
                        trap-snap-bottom (trap-y-fn snap-bottom-pos)
                        hit-frame? (or (:hit-frame? acc)
                                       (inside-frame? x y
                                                      (- w snap-hitbox-x)
                                                      (- h snap-hitbox-y)))]
                    (cond-> acc
                      (not (:hit-frame? acc))
                      (assoc! :hit-frame? hit-frame?)
                      (and (or snap-left trap-snap-left)
                           (not (:snap-right acc))
                           (not hit-frame?))
                      (assoc! :snap-left {:wsp-x (if add-frame-size? (+ x base-w) x)
                                          :offset-x snap-left
                                          :trap-offset-x trap-snap-left
                                          :page-pos (wsp->page-fn [(dec x) 0])})

                      (and (or snap-right trap-snap-right)
                           (not snap-left)
                           (not trap-snap-left)
                           (not (:snap-left acc)))
                      (assoc! :snap-right {:wsp-x (if add-frame-size? (+ snap-right-pos base-w) snap-right-pos)
                                           :offset-x snap-right
                                           :trap-offset-x trap-snap-right
                                           :page-pos (wsp->page-fn [snap-right-pos 0])})

                      (and (or snap-top trap-snap-top)
                           (not (:snap-bottom acc))
                           (not hit-frame?))
                      (assoc! :snap-top {:wsp-y (if add-frame-size? (+ y base-h) y)
                                         :offset-y snap-top
                                         :trap-offset-y trap-snap-top
                                         :page-pos (wsp->page-fn [0 (dec y)])})

                      (and (or snap-bottom trap-snap-bottom)
                           (not snap-top)
                           (not trap-snap-top)
                           (not (:snap-top acc)))
                      (assoc! :snap-bottom {:wsp-y (if add-frame-size? (+ snap-bottom-pos base-h) snap-bottom-pos)
                                            :offset-y snap-bottom
                                            :trap-offset-y trap-snap-bottom
                                            :page-pos (wsp->page-fn [0 snap-bottom-pos])}))))
                (transient {})
                frames)
        (persistent!))))

(defn handle-snapping [{:keys [full-size]}
                       ^number check-x ^number check-y
                       frames
                       {^number wsp-zoom :z :as wsp}]
  (when (snapping/snapping? :frame)
    (let [^number pixel-ratio (or (aget js/window "devicePixelRatio") 1)
          [^number w ^number h] full-size
          ^number snap-hitbox-x (/ config/snap-hitbox-x wsp-zoom)
          ^number snap-hitbox-y (/ config/snap-hitbox-y wsp-zoom)
          ^number snap-trap-x (/ config/snap-trap-x
                                 pixel-ratio
                                 wsp-zoom)
          ^number snap-trap-y (/ config/snap-trap-y
                                 pixel-ratio
                                 wsp-zoom)
          {:keys [hit-frame?] :as top-left-positions} (check-nearest-positions full-size
                                                                               check-x check-y
                                                                               snap-hitbox-x snap-hitbox-y
                                                                               snap-trap-x snap-trap-y
                                                                               frames wsp false)
          top-left-positions (cond-> (dissoc top-left-positions :hit-frame?)
                               hit-frame? (dissoc :snap-top :snap-left))
          bottom-right-positions (check-nearest-positions (mapv - full-size)
                                                          (+ check-x w)
                                                          (+ check-y h)
                                                          snap-hitbox-x snap-hitbox-y
                                                          snap-trap-x snap-trap-y
                                                          frames wsp true)
          bottom-right-positions (cond-> (dissoc bottom-right-positions :hit-frame?)
                                   hit-frame? (dissoc :snap-bottom :snap-right))
          nearest-positions (merge bottom-right-positions top-left-positions)
          snapped @snapped-state
          [^number new-x ^number new-y ^number offset-x ^number offset-y :as new-pos]
          (reduce (fn [[^number x ^number y ^number off-x ^number off-y :as acc]
                       [^keyword orientation
                        {:keys [^number wsp-x ^number wsp-y ^number offset-x ^number offset-y
                                ^number trap-offset-x ^number trap-offset-y]}]]
                    (cond
                      (and (not (get snapped orientation))
                           (or offset-x offset-y))
                      (do
                        (swap! snapped-state assoc orientation true)
                        [(or wsp-x x)
                         (or wsp-y y)
                         (or offset-x off-x)
                         (or offset-y off-y)])
                      (or trap-offset-x trap-offset-y)
                      (do
                        (swap! snapped-state assoc orientation true)
                        [(or wsp-x x)
                         (or wsp-y y)
                         (or trap-offset-x off-x)
                         (or trap-offset-y off-y)])
                      :else acc))
                  []
                  nearest-positions)]
      (reset! snap-state nearest-positions)
      (if (seq new-pos)
        [new-x new-y (* wsp-zoom offset-x) (* wsp-zoom offset-y)]
        false))))

(defn- apply-position [[^number x ^number y] ^keyword orientation]
  (let [is-vertical-orientation? (= orientation :vertical)
        line-class (if is-vertical-orientation?
                     "vertical__snapline"
                     "horizontal__snapline")]

    {:class line-class
     :style {:position :absolute
             :transform (if is-vertical-orientation?
                          (str "translate3d(" x "px, " 0 "px, 0)")
                          (str "translate3d(" 0 "px, " y "px, 0)"))}}))

(defn- snapping-line [pos-id _ _]
  (r/create-class
   {:component-will-unmount #(swap! snapped-state dissoc pos-id)
    :reagent-render (fn [_ position ^keyword orientation]
                      [:div (apply-position position orientation)])}))


(defn snapping-lines []
  (let [{:keys [snap-left snap-right snap-top snap-bottom]} @snap-state]
    [:<>
     (when snap-left
       [snapping-line :snap-left (:page-pos snap-left) :vertical])
     (when snap-right
       [snapping-line :snap-right (:page-pos snap-right) :vertical])
     (when snap-top
       [snapping-line :snap-top (:page-pos snap-top) :horizontal])
     (when snap-bottom
       [snapping-line :snap-bottom (:page-pos snap-bottom) :horizontal])]))

;;; grid snap

(def gridsize
  {wwb/large-bg-idx  50
   wwb/medium-bg-idx 50
   wwb/small-bg-idx  100
   wwb/tiny-bg-idx   200})

(defn round-to-grid [value idx]
  (let [cell-size (get gridsize idx)]
    (* cell-size
       (cond-> value
         :always (/ cell-size)
         (< 0 value) (+ 0.5)
         (>= 0 value) (- 0.5)
         :always int))))

(defn current-idx []
  (let [active (wwb/calc-active-tiling (wwb/get-z))]
    (apply max (map first active))))

(defn snap-to-grid [moving-data move-multiselect-box]
  (let [idx (current-idx)
        [frame-id {:keys [diff-x diff-y new-x new-y workspace-zoom] :as move-data}]
        moving-data
        {window-snap-left :snap-left window-snap-right :snap-right
         window-snap-top :snap-top window-snap-bottom :snap-bottom}
        @snapped-state
        snap-x (round-to-grid new-x idx)
        snap-y (round-to-grid new-y idx)
        snap-diff-x (+ diff-x (* (- snap-x new-x) workspace-zoom))
        snap-diff-y (+ diff-y (* (- snap-y new-y) workspace-zoom))]
    (move-multiselect-box
     snap-diff-x
     snap-diff-y
     workspace-zoom)
    [frame-id
     (cond-> move-data
       (and (not window-snap-left)
            (not window-snap-right))
       (assoc
        :new-x snap-x
        :diff-x snap-diff-x)
       (and (not window-snap-bottom)
            (not window-snap-top))
       (assoc
        :new-y snap-y
        :diff-y snap-diff-y))]))

(defn get-grid-dim
  "First return value is x/y and second return value is the minimum gap to snap."
  []
  (let [idx (current-idx)
        size (get gridsize idx)]
    [size (/ size 3)]))