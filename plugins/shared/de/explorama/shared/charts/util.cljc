(ns de.explorama.shared.charts.util)

(def default-namespace :charts)

(def default-vertical-charts-str (name default-namespace))

(defn is-charts? [frame-id]
  (= (:vertical frame-id) default-vertical-charts-str))

(def color-transparency 0.8)

(defn- color->rgba [[r g b a]]
  (str "rgba(" r "," g "," b "," a ")"))

(def raw-colors [; Chart 1
                 [68 119 170 color-transparency]
                 [204 187 68 color-transparency]
                 [170 51 119 color-transparency]
                 [34 136 51 color-transparency]
                 [187 187 187 color-transparency]
                 ; Chart 2
                 [238 102 119 color-transparency]
                 [200 45 136 color-transparency]
                 [188 207 0 color-transparency]
                 [244 152 25 color-transparency]
                 [102 204 238 color-transparency]])

(def raw-colors-second-chart (subvec raw-colors 5))

(def colors
  (cycle (mapv color->rgba raw-colors)))

(def colors-second-chart (cycle (mapv color->rgba raw-colors-second-chart)))

(def remaining-group-color
  "#868e96")
