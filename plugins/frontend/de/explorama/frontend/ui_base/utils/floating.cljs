(ns de.explorama.frontend.ui-base.utils.floating
  (:require [clojure.string :as clj-str]
            ["@floating-ui/dom" :refer [computePosition offset flip shift arrow autoUpdate]]))

(def ^:private collision-padding 10)
(def ^:private arrow-padding 5)
(def default-distance 10)

(def ^:private placements
  {:up "top" :down "bottom" :left "left" :right "right"})

(def ^:private sides
  {"top" :top "bottom" :bottom "left" :left "right" :right})

(defn direction->placement [direction]
  (get placements direction "top"))

(defn placement->side [placement]
  (get sides (first (clj-str/split (str placement) #"-")) :top))

(defn- middleware [distance arrow-el]
  (to-array
   (cond-> [(offset distance)
            (flip #js {:padding collision-padding})
            (shift #js {:padding collision-padding})]
     arrow-el (conj (arrow #js {:element arrow-el :padding arrow-padding})))))

(defn compute-position! [reference floating {:keys [placement distance arrow-el]
                                              :or {distance default-distance}}]
  (-> (computePosition reference floating
                       #js {:placement placement
                            :strategy "fixed"
                            :middleware (middleware distance arrow-el)})
      (.then (fn [result]
               (let [arrow-data (.-arrow (.-middlewareData result))]
                 {:x (.-x result)
                  :y (.-y result)
                  :placement (.-placement result)
                  :arrow-x (some-> arrow-data (.-x))
                  :arrow-y (some-> arrow-data (.-y))})))))

(defn auto-update! [reference floating cb]
  (autoUpdate reference floating cb))
