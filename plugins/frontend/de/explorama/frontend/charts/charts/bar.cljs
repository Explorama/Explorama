(ns de.explorama.frontend.charts.charts.bar
  (:require [cljsjs.chartjs]
            [cljsjs.chartjs-adapter-date-fns]
            [cljsjs.date-fns]
            [de.explorama.frontend.charts.charts.combined :as comb]
            [de.explorama.frontend.charts.path :as path]))

(defonce chart-id-prefix "vis_bar-")

(def chart-desc {path/chart-desc-id-key path/bar-id-key
                 path/chart-desc-label-key :bar-chart-label
                 path/chart-desc-selector-class-key "chart__bar"
                 path/chart-desc-content-key comb/content
                 path/chart-desc-multiple-key true
                 path/chart-desc-icon-key :charts-bar})
