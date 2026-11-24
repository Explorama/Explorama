(ns e2e.specs.prediction-render
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [e2e.pages.prediction :as prediction]
            [e2e.pages.search :as search]
            [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

;; The prediction plugin builds its chart through its own chart.js call site,
;; separate from the charts plugin, so it needs its own coverage. The default
;; Year unit leaves the 2021-only sample with a single data point, which the
;; form rejects; Month spreads it over the three months the data spans.
(defspec "a value forecast renders its result chart"
  (fn [page expect]
    (p/do
      (search/run-with-datasource page expect dataset/netflix-name)
      (ws/zoom-out page 4)
      (ws/create-frame page "#tool-algorithms" 900 250)
      (ws/connect page :search :prediction)
      (-> (expect (prediction/frame page))
          (.toContainText "Run Prediction" #js {:timeout 60000}))
      (prediction/select-unit page "Year" "Month")
      (prediction/run page)
      (-> (expect (.locator (prediction/frame page) "canvas"))
          (.toBeVisible #js {:timeout 60000}))
      (-> (expect (.locator (prediction/frame page) "canvas"))
          (.toHaveCount 1)))))
