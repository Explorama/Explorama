(ns de.explorama.backend.charts.data.colors
  (:require [data-format-lib.filter]
            [de.explorama.backend.charts.config :as config]
            [de.explorama.shared.charts.util :refer [colors
                                                     colors-second-chart]]))

;{<di> {<group-by-key> {<group-label> <color>}}}
(defonce ^:private color-store (atom {}))

(defn- base-color-key [di group-by-key chart-index]
  [di group-by-key chart-index])

(defn- next-color [k chart-index]
  (let [colors-used (count (keys (get-in @color-store k)))]
    (nth (if (= chart-index 0)
           colors
           colors-second-chart)
         colors-used)))

(defn color [di chart-index group-by-key group-label]
  (let [base-key (base-color-key di group-by-key chart-index)
        label-key (conj base-key (cond-> group-label
                                   (map? group-label)
                                   (get group-by-key)))
        color (get-in @color-store label-key)
        new-color (next-color base-key chart-index)]
    (if color
      color
      (do
        (swap! color-store assoc-in label-key new-color)
        new-color))))

(defn rgba-background-color [[r g b] opacity]
  (str "rgb(" r "," g "," b "," opacity ")"))

(defn rgb->add-opacity [rgb opacity]
  (let [rgb (if (string? rgb)
              (->> rgb
                   (re-seq #"\d+")
                   (take 3))
              rgb)]
    (rgba-background-color rgb opacity)))

(def ^:private max-opacity 1.0)

(defn opacity-value [min-val max-val original-val]
  (if (= min-val max-val)
    max-opacity
    (+ (* (- max-opacity
             config/explorama-charts-scatter-min-oppacity)
          (/ (- original-val min-val)
             (- max-val min-val)))
       config/explorama-charts-scatter-min-oppacity)))

(comment
  (reset! color-store {})
  @color-store)


