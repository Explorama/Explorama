(ns e2e.specs.fact-units
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [e2e.pages.import :as import]
            [e2e.pages.search :as search]
            [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

;; search/run-with-datasource reloads the page first (ws/open-workspace), and
;; a freshly imported datasource does not survive that reload - the same
;; reason import.cljs's own commit doc-comment gives for never reloading
;; after an import. So the search step is composed by hand here instead of
;; via that helper, staying on the page the import already landed on.
(defspec "a unit assigned at import reaches the table header"
  (fn [page expect]
    (p/do
      (ws/open-welcome page)
      (import/upload-csv page "fixtures/units-sample.csv")
      (import/set-fact-unit page dataset/units-fact-column dataset/units-fact-unit)
      (import/commit-as page expect dataset/units-import-name 3)
      (ws/dismiss-overlays page)
      (search/open page)
      (search/select-datasource page dataset/units-import-name)
      (search/run page expect)
      (ws/create-frame page "#tool-table" 1080 600)
      (ws/connect page :search :table)
      (-> (expect (ws/frame page :table))
          (.toContainText dataset/units-fact-header #js {:timeout 60000})))))
