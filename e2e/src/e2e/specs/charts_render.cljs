(ns e2e.specs.charts-render
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [e2e.pages.charts :as charts]
            [e2e.pages.search :as search]
            [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

;; The default chart is a line chart over "month", which chart.js renders on a
;; time scale: the assertions below cover both the chart.js interop and the
;; date adapter registration, neither of which the cljs suites see in a
;; production bundle. The global pageerror check in explorama.spec.js is what
;; catches a chart that throws on construction.
(defspec "connecting a search to a chart renders the canvas"
  (fn [page expect]
    (p/do
      (search/run-with-datasource page expect dataset/netflix-name)
      (ws/create-frame page "#tool-charts" 1080 600)
      (ws/connect page :search :charts)
      (-> (expect (ws/frame page :charts))
          (.toContainText dataset/netflix-event-count #js {:timeout 60000}))
      (-> (expect (.locator (ws/frame page :charts) "canvas"))
          (.toBeVisible #js {:timeout 60000}))
      (-> (expect (.locator (ws/frame page :charts) "canvas"))
          (.toHaveCount 1)))))

(defspec "switching a chart to the pie type renders the canvas"
  (fn [page expect]
    (p/do
      (search/run-with-datasource page expect dataset/netflix-name)
      (ws/create-frame page "#tool-charts" 1080 600)
      (ws/connect page :search :charts)
      (-> (expect (.locator (ws/frame page :charts) "canvas"))
          (.toBeVisible #js {:timeout 60000}))
      (charts/select-type page "Line chart" "Pie chart")
      (-> (expect (.locator (ws/frame page :charts) "canvas"))
          (.toBeVisible #js {:timeout 60000}))
      (-> (expect (.locator (ws/frame page :charts) "canvas"))
          (.toHaveCount 1)))))
