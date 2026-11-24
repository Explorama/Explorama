(ns e2e.pages.prediction
  (:require [e2e.pages.workspace :as ws]
            [promesa.core :as p]))

(defn frame [page]
  (ws/frame page :prediction))

;; Both controls are the same custom combobox the search form uses: open it by
;; clicking the selected label, then pick the new one from the list.
(defn select-unit [page from to]
  (p/do
    (.click (.getByText (frame page) from #js {:exact true}))
    (.click (.first (.getByText page to #js {:exact true})))))

(defn run [page]
  (p/do
    (.click (.getByText (frame page) "Run Prediction"))
    (.waitFor (.getByText (frame page) "Results")
              #js {:state "visible" :timeout 60000})))
