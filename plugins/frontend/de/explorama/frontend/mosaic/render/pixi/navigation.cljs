(ns de.explorama.frontend.mosaic.render.pixi.navigation
  (:require [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [taoensso.timbre :refer [error]]))

(defn set-transform-main [stage x y z]
  (set! (.-x (.-position stage)) x)
  (set! (.-y (.-position stage)) y)
  (set! (.-x (.-scale stage)) z)
  (set! (.-y (.-scale stage)) z))

(defn move-to!
  ([this stage-key index]
   (let [{:keys [cpl card-width card-height card-margin min-zoom]} (gre/state this)
         [cx cy] (pc/oned->twod index cpl)
         x (- (* cx
                 (+ card-width card-margin card-margin)
                 min-zoom))
         y (- (* cy
                 (+ card-height card-margin card-margin)
                 min-zoom))]
     (gre/move-to! this stage-key x y min-zoom)))
  ([this stage-key x y]
   (let [app (gre/app this)
         state (gre/state this)]
     (when-not (:headless state)
       (let [stage (pc/zoom-context-stage app stage-key)]
         (set! (.-x (.-position (pc/main-container stage stage-key))) x)
         (set! (.-y (.-position (pc/main-container stage stage-key))) y)
         (set! (.-x (.-position (pc/axes-background-container-direct stage stage-key))) x)
         (set! (.-y (.-position (pc/axes-background-container-direct stage stage-key))) y)))
     (gre/merge-in-state!
      this
      [:pos stage-key]
      {:x x
       :y y})))
  ([this stage-key x y z]
   (let [app (gre/app this)
         args (gre/args this)
         state (gre/state this)
         {overview-factor  :factor-overview
          render-type :render-type}
         (get-in (gre/state this) [:contexts stage-key []])
         new-zoom-a (pc/zoom-level z
                                   (get-in state [[:pos stage-key] :zoom] 0)
                                   overview-factor
                                   render-type)
         z-a (pc/overview-op? new-zoom-a overview-factor z /)
         new-zoom-b (pc/zoom-level z-a
                                   new-zoom-a
                                   overview-factor
                                   render-type)
         z-b (pc/overview-op? new-zoom-b overview-factor z-a /)
         [x-bb y-bb z-bb] (pc/new-zoom stage-key state args [x y z-b] new-zoom-b)
         [x-aa y-aa z-aa] (pc/new-zoom stage-key state args [x y z-a] new-zoom-a)
         [new-x new-y new-z new-zoom]
         (if (= new-zoom-a new-zoom-b)
           [x-aa y-aa z-aa new-zoom-a]
           [x-bb y-bb z-bb new-zoom-b])]
     (try
       (when-not (:headless state)
         (let [stage (pc/zoom-context-stage app stage-key)]
           (set-transform-main (pc/main-container stage stage-key) new-x new-y new-z)
           (set-transform-main (pc/axes-background-container-direct stage stage-key) new-x new-y new-z)))
       (gre/merge-in-state!
        this
        [:pos stage-key]
        {:x new-x
         :y new-y
         :z new-z
         :next-zoom new-zoom})
       (catch :default e
         (error "Error while updating canvas:" e)))))
  ([this stage-key x y z zoom]
   (let [app (gre/app this)
         state (gre/state this)
         args (gre/state this)
         [x y z]
         (pc/new-zoom stage-key state args [x y z] zoom)]
     (try
       (when (and (not (:headless state))
                  (not (nil? x))
                  (not (nil? y)))
         (let [stage (pc/zoom-context-stage app stage-key)]
           (set-transform-main (pc/main-container stage stage-key) x y z)
           (set-transform-main (pc/axes-background-container-direct stage stage-key) x y z)))
       (gre/merge-in-state!
        this
        [:pos stage-key]
        {:x x
         :y y
         :z z
         :next-zoom zoom})
       (catch :default e
         (error "Error while updating canvas:" e))))))