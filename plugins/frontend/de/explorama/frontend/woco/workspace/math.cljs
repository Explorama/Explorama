(ns de.explorama.frontend.woco.workspace.math)

(defn frame-bounding-box [selected-frames]
  (reduce (fn [acc [_ {[x y] :coords [w h] :full-size}]]
            (-> acc
                (update :start-min-x (fn [ox]
                                       (if ox
                                         (min ox x)
                                         x)))
                (update :start-max-x (fn [ox]
                                       (if ox
                                         (max ox (+ x w))
                                         (+ x w))))
                (update :start-min-y (fn [oy]
                                       (if oy
                                         (min oy y)
                                         y)))
                (update :start-max-y (fn [oy]
                                       (if oy
                                         (max oy (+ y h))
                                         (+ y h))))))
          {}
          selected-frames))

(defn simple-hit-test [^number sx ^number sy
                       ^number ex ^number ey
                       coords size]
  (let [[^number x
         ^number y]
        coords
        [^number width
         ^number height]
        size]
    (and (< sx (+ x width))
         (< x ex)
         (< sy (+ y height))
         (< y ey))))

(defn find-bb [^number start-x ^number start-y
               ^number end-x ^number end-y]
  [(if (< end-x start-x)
     [end-x start-x]
     [start-x end-x])
   (if (< end-y start-y)
     [end-y start-y]
     [start-y end-y])])

(defn point-inside-hitboxes?
  ([^number check-x ^number check-y hitboxes selector]
   (some (fn [{[^number x ^number y] :coords
               [^number w ^number h] :full-size
               :as hb}]
           (when (and (<= x check-x (+ x w))
                      (<= y check-y (+ y h)))
             (let [sel-val (select-keys hb (if (vector? selector)
                                             selector
                                             [selector]))]
               (when (seq sel-val)
                 sel-val))))
         hitboxes))
  ([^number check-x ^number check-y hitboxes]
   (point-inside-hitboxes? check-x check-y hitboxes :id)))

(defn ^boolean collide-rects [^number sleft ^number stop
                              ^number swidth ^number sheight
                              ^number left ^number top
                              ^number width ^number height]
  (and (or (<= top stop (+ top height))
           (<= stop top (+ top height) (+ stop sheight))
           (<= stop top (+ stop sheight)))
       (or (>= (+ left width) sleft left)
           (>= (+ sleft swidth) left sleft)
           (<= sleft left (+ left width) (+ sleft swidth))
           (<= sleft left (+ sleft swidth)))))
