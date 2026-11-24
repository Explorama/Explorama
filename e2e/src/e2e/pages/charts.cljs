(ns e2e.pages.charts
  (:require [e2e.pages.workspace :as ws]
            [promesa.core :as p]))

;; The legend panel shows the chart's settings only after its edit link is
;; followed, and the type picker is the same custom combobox the search form
;; uses: open it by clicking the currently selected label, then pick the new
;; one. Neither carries an id, so both are matched by their visible text.
(defn select-type [page from to]
  (let [frame (ws/frame page :charts)]
    (p/do
      (.click (.first (.getByText frame "Edit")))
      (.waitFor (.getByText frame "Chart Type")
                #js {:state "visible" :timeout 20000})
      (.click (.getByText frame from #js {:exact true}))
      (.click (.first (.getByText page to #js {:exact true}))))))
