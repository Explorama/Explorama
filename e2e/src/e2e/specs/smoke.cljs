(ns e2e.specs.smoke
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

(defspec "the workspace boots and renders the tool palette"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (-> (expect page) (.toHaveTitle "Explorama"))
      (-> (expect (.locator page "[id^=tool-]")) (.toHaveCount dataset/tool-count)))))

(defspec "creating a search frame renders the search form"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (ws/create-frame page "#tool-search" 480 380)
      (-> (expect (ws/frame page :search)) (.toBeVisible))
      (-> (expect (ws/frame page :search)) (.toContainText "Topic/Datasource")))))

;; .toolbar-wrapper gets its position solely from the Tailwind ".absolute"
;; utility class - neither of its two component-CSS rules sets position -
;; so this guards the production stylesheet link order/completeness rather
;; than any element's mere presence.
(defspec "the production stylesheet supplies its utility classes"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (-> (expect (.locator page ".toolbar-wrapper"))
          (.toHaveCSS "position" "absolute")))))
