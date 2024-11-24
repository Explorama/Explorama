(ns de.explorama.frontend.woco.navigation.util
  (:require [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-id]]))

(defn respect-boundaries [check-value min-value max-value]
  (cond
    (> check-value max-value) max-value
    (< check-value min-value) min-value
    :else check-value))

(defn respect-boundaries-relaxed
  "If the old-value is already outside the boundaries,
   accept check-values that are closer to the boundaries."
  [check-value old-value min-value max-value]
  (cond
    (>= check-value max-value old-value) max-value
    (<= check-value min-value old-value) min-value
    (or (>= check-value old-value max-value)
        (<= check-value old-value min-value)) old-value
    :else check-value))

(defn z-factor [wheel-delta]
  (let [wheel-delta (respect-boundaries wheel-delta config/min-wheel-delta config/max-wheel-delta)]
    (-> (/ wheel-delta 120)
        (* config/zoom-speed)
        (+ 1))))

(defn safe-number? [number]
  ;; Ensures that:
  ;;   * Number is not null
  ;;   * Number is not infinity/-infinity (number? is true for infinity)
  ;;   * Number is not NaN
  ;;   * Number is a number
  ;;   See https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Number/isFinite
  (js/Number.isFinite number))

(defn workspace-rect []
  (bounding-rect-id config/workspace-parent-id))
