(ns de.explorama.frontend.woco.workspace.cubic-bezier
  (:require ["bezier-easing" :as BezierEasing]))


(def nominator 24)

(defonce slices (loop [acc [0]
                       n 1]
                  (if (<= n nominator)
                    (recur (conj acc (/ n nominator)) (inc n))
                    acc)))

(defonce easing-fn (.default ^js BezierEasing 0.32 0.57 0.27 1.02))

(def curve-points (map (fn [x] [x (easing-fn x)]) slices))

(defn calculate-corner-point-isosceles-right-triangle [v]
  (let [vec-to-perpendicular-point (map #(* 0.5 %) v)
        cw-perpendicular-vec [(second vec-to-perpendicular-point) (* -1 (first vec-to-perpendicular-point))]
        c-point  [(+ (first vec-to-perpendicular-point)
                     (first cw-perpendicular-vec))
                  (+ (second vec-to-perpendicular-point)
                     (second cw-perpendicular-vec))]]
    c-point))

(defn scale-points [curve-points factor]
  (map (fn [[x y]] [(* factor x) (* factor y)]) curve-points))


(defn- calculate-points-impl [[displacement-x displacement-y]
                              [vec-to-c-x vec-to-c-y]
                              distance-to-corner
                              scaled-points]
  (let [unit-vec-to-c [(/ vec-to-c-x distance-to-corner)
                       (/ vec-to-c-y distance-to-corner)]
        ccw-perpendicular-unit-vec [(* -1 (second unit-vec-to-c))
                                    (first unit-vec-to-c)]
        calc-actual-pos (fn [[time-factor prog-factor]]
                          (let [[time-vec-x time-vec-y] (map #(* % time-factor) unit-vec-to-c)
                                [prog-vec-x prog-vec-y] (map #(* % prog-factor) ccw-perpendicular-unit-vec)]
                            [(+ displacement-x time-vec-x prog-vec-x)
                             (+ displacement-y time-vec-y prog-vec-y)]))]
    (map calc-actual-pos scaled-points)))


(defn calculate-points [[start-x start-y :as start] [end-x end-y]]
  (let [vec-from-orig [(- end-x start-x) (- end-y start-y)]
        [corner-point-x corner-point-y :as corner-point] (calculate-corner-point-isosceles-right-triangle vec-from-orig)
        distance-to-corner (Math/sqrt (+ (Math/pow corner-point-x 2)
                                         (Math/pow corner-point-y 2)))
        scaled-points (scale-points curve-points distance-to-corner)]
    (calculate-points-impl start corner-point distance-to-corner scaled-points)))

