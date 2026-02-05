(ns de.explorama.frontend.charts.charts.bubble
  (:require ["chart.js"]
            ["chartjs-adapter-date-fns"]
            ["date-fns"]
            [de.explorama.frontend.charts.charts.combined :as comb]
            [de.explorama.frontend.charts.path :as path]))

(def chart-desc {path/chart-desc-id-key path/bubble-id-key
                 path/chart-desc-label-key :bubble-chart-label
                 path/chart-desc-selector-class-key "chart__bubble"
                 path/chart-desc-content-key comb/content
                 path/chart-desc-multiple-key true
                 path/chart-desc-icon-key :charts-bubble})
