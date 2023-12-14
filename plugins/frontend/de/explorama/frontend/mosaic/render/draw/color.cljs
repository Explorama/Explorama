(ns de.explorama.frontend.mosaic.render.draw.color)

(defn- rgb-values [color]
  (cond (string? color)
        [(js/parseInt (subs color 1 3) 16)
         (js/parseInt (subs color 3 5) 16)
         (js/parseInt (subs color 5 7) 16)]
        (vector? color)
        color))

(defn font-color
  ([color a]
   (let [[red green blue] (rgb-values color)
         brightness (+ (* red 0.299) (* green 0.587) (* blue 0.114))
         [r g b] (if (< 180 brightness)
                   [0 0 0]
                   [255 255 255])
         rgba [(+ (* (- 1 a) red) (* a r))
               (+ (* (- 1 a) green) (* a g))
               (+ (* (- 1 a) blue) (* a b))]]
     (if (< 180 brightness)
       [rgba
        "#000000"
        "#777777"]
       [rgba
        "#FFFFFF"
        "#CCCCCC"])))
  ([color]
   (let [[red green blue] (rgb-values color)
         brightness (+ (* red 0.299) (* green 0.587) (* blue 0.114))]
     (if (< 180 brightness)
       ["#333333"
        "#000000"
        "#777777"]
       ["#BBBBBB"
        "#FFFFFF"
        "#CCCCCC"]))))