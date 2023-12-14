(ns de.explorama.frontend.reporting.views.parameters)

(def dashboard-container-padding 8)
(defn dashboard-container-size [{:keys [height width top]}]
  {:top dashboard-container-padding
   :left dashboard-container-padding
   :height (- height (* 2 dashboard-container-padding))
   :width (- width (* 2 dashboard-container-padding))
   :padding dashboard-container-padding})

(def dashboard-module-padding 8)
(def dashboard-header-height 26)
(def dashboard-datasources-height 24)
(def dashboard-legend-max-width 240)
(def dashboard-legend-max-height 220)
(defn dashboard-module-size [{:keys [height width]} legend-position legend-active?]
  (let [m-width (if (and legend-active? (= legend-position :right))
                  dashboard-legend-max-width
                  0)
        m-height (if (and legend-active? (= legend-position :bottom))
                   dashboard-legend-max-height
                   0)]
    [(- width m-width (* 2 dashboard-module-padding))
     (- height m-height dashboard-header-height (* 2 dashboard-module-padding))]))

(def report-container-padding 8)
(def report-max-width 793.7007874) ;px - = 21cm (a4)
(def min-height 500)
(defn report-container-size [{:keys [height width top]}]
  {:top report-container-padding
   :left report-container-padding
   :height min-height
   :width (min report-max-width (- width (* 2 report-container-padding)))
   :padding report-container-padding})

(def report-module-padding 8)
(def report-header-height 26)
(def report-datasources-height 24)
(def report-legend-max-width 240)
(def report-legend-max-height 220)
(defn report-module-size [{:keys [height width]} legend-position legend-active?]
  (let [m-width (if (and legend-active? (= legend-position :right))
                  report-legend-max-width
                  0)
        m-height (if (and legend-active? (= legend-position :bottom))
                   report-legend-max-height
                   0)]
    [(- (min width report-max-width)
        m-width (* 2 report-module-padding))
     (- (max height min-height)
        m-height report-header-height report-datasources-height (* 2 report-module-padding))]))
