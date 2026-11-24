(ns e2e.specs.data-import
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [e2e.pages.import :as import]
            [e2e.pages.search :as search]
            [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

(defspec "an imported CSV becomes searchable"
  (fn [page expect]
    (p/do
      (ws/open-welcome page)
      (import/upload-csv page "fixtures/netflix-sample.csv")
      (import/commit page expect)
      ;; The Import card dismisses the welcome screen as it opens the overlay,
      ;; so the workspace is already active; reloading here would assume the
      ;; import survives a page reload.
      (ws/dismiss-overlays page)
      (search/open page)
      (search/select-datasource page dataset/import-name)
      (-> (expect (search/traffic-light page))
          (.toContainText dataset/import-event-count #js {:timeout 30000})))))
