(ns de.explorama.frontend.ui-base.utils.virtual)

(defn row-style [start size measured?]
  (cond-> {:position "absolute"
           :top start
           :left 0
           :width "100%"}
    (not measured?) (assoc :height size)))

(defn cell-style [column-start column-size row-start row-size]
  {:position "absolute"
   :top row-start
   :left column-start
   :width column-size
   :height row-size})

(defn sizer-style [width height]
  (cond-> {:position "relative"}
    width (assoc :width width)
    height (assoc :height height)))
