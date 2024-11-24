(ns de.explorama.frontend.ui-base.utils.colors
  (:require [clojure.string :refer [join]]
            [goog.string :as gstring]))

(def ^:private sRGB
  (mapv #(Math/pow (/ % 255.0) 2.2) (range 256)))

(defn ^:export RGB->sRGB
  "Translates [R G B] (0-255) to sRGB (0-1) vector
    Example:
   ```clojure
     => (RGB->sRGB [255 0 255])
     => (1 0 1)
   ```"
  [RGB-vec]
  (map sRGB RGB-vec))

(defn ^:export css-RGB-string
  "Translates [R G B] (0-255) to css-rgb string
   Example:
   ```clojure
     => (css-RGB-string [255 0 255])
     => rgb(255,0,255)
   ```"
  [RGB-vec]
  (if (vector? RGB-vec)
    (gstring/format "rgb(%s)" (join "," RGB-vec))
    RGB-vec))

(defn ^:export sRGB->svg-color-matrix
  "Translates [R G B] (0-1) to svg-color matrix
   Example:
   ```clojure
     => (sRGB->svg-color-matrix [1 0 1])
     => 0 0 0 0 1 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0
   ```"
  [sRGB-vec]
  (apply gstring/format "0 0 0 0 %f 0 0 0 0 %f 0 0 0 0 %f 0 0 0 1 0"
         sRGB-vec))

(defn ^:export RGB->svg-color-matrix
  "Translates [R G B] (0-255) to svg-color matrix
   Example:
   ```clojure
     => (RGB->svg-color-matrix [255 0 255])
     => 0 0 0 0 1 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0
   ```"
  [rgb-vec]
  (sRGB->svg-color-matrix
   (RGB->sRGB rgb-vec)))

(defn ^:export svg-color-node
  "Translates [R G B] (0-255) to svg-color node, which can be used inside [:svg ...] to apply a color
   Example:
   ```clojure
     => [svg-color-node [255 0 255])
     => [:filter {:id \"2514e888-be58-40a7-ba50-0eb100a3717a\"} [:feColorMatrix {:in \"SourceGraphic\" :type \"matrix\" :values \"0 0 0 0 1 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0\"}]]
   ```"
  [RGB-vec]
  [:filter {:id (str (random-uuid))}
   [:feColorMatrix {:in "SourceGraphic"
                    :type "matrix"
                    :values (RGB->svg-color-matrix RGB-vec)}]])

(defn ^:export font-color
  "Calculated the brightness of the input colour string to determine the font colour.
    Example:
   ```clojure
     => (font-color \"#EEFF99\")
     => \"#000000\"
   ```"
  [color]
  (let [red (js/parseInt (subs color 1 3) 16)
        green (js/parseInt (subs color 3 5) 16)
        blue (js/parseInt (subs color 5 7) 16)
        brightness (+ (* red 0.299) (* green 0.587) (* blue 0.114))]
    (if (< 180 brightness)
      "#000000"
      "#FFFFFF")))
