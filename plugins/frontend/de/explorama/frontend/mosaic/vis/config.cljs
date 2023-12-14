(ns de.explorama.frontend.mosaic.vis.config)

(def operations-context-menu-offset-x -12)
(def operations-context-menu-offset-y -20)

(def tool-name "tool-mosaic")

;Drag & Drop ---------------------
(def header-height 36)

(def frame-width 600)
(def frame-height 550)
(def frame-size [frame-width frame-height])
(def min-frame-size [(* frame-width 0.7) (* frame-height 0.9)])

(defn frame-body-dom-id [frame-id]
  (str frame-id "__mosaic-frame-body"))

;mosaic-views ---------------------
(def mosaic-top-width frame-width)
(def mosaic-top-height (- frame-height header-height))

(defn body-height [frame-height]
  (- frame-height
     header-height))

(def group-x-factor 1)
(def group-y-factor 1)

(def padding-top 4)
(def padding-bottom 2)
(def padding-left 2)
(def padding-right 2)

(def mosaic-topbox-offset-y (+ padding-top padding-bottom))
(def mosaic-topbox-offset-x (+ padding-left padding-right))

(def mosaic-group-header-height 102)

(def mosaic-group-margin 4)

(def magical-offset 5) ; TODO the scaling changes sizes slightly, so we need a "buffer"

(defn margin-x-count [x-factor]
  (+ 2 ; left and right side
     (* 2 ; every container has a margin and these margins
          ; don't collapse
        (int (/ 1 x-factor)))))

(defn calc-size [dim factor]
  (- (* dim factor)
     (* (margin-x-count factor)
        mosaic-group-margin)
     magical-offset))

(def mosaic-group-width (calc-size mosaic-top-width group-x-factor))
(def mosaic-group-height (+ (calc-size mosaic-top-height group-y-factor)
                            mosaic-group-header-height))

(defn canvas-size [width height]
  {:width (- width mosaic-topbox-offset-x)
   :height (- height mosaic-topbox-offset-y)})

(def z-index-comment 1000)
(def comment-parent-id "frames-workspace")