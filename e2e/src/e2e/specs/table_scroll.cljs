(ns e2e.specs.table-scroll
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [e2e.pages.search :as search]
            [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

;; The body's scrollable range is 840px (scrollWidth 1440 - clientWidth 600)
;; at the frame size below. Deliberately not a multiple of the 120px column
;; width: which columns a virtualized grid renders at an offset that lands
;; exactly on a column boundary is implementation-defined, so a boundary
;; offset could flip this spec's outcome for reasons unrelated to whether
;; the header stays aligned with the body.
(def ^:private scroll-offset 350)

(defn- cell [frame testid column-index]
  (.first (.locator frame (str "[data-testid=\"" testid "\"]"
                               "[data-column-index=\"" column-index "\"]"))))

(defspec "the table header stays aligned with the body when scrolled sideways"
  (fn [page expect]
    (p/let [_ (search/run-with-datasource page expect dataset/netflix-name)
            _ (ws/create-frame page "#tool-table" 700 500)
            _ (ws/connect page :search :table)
            frame (ws/frame page :table)
            _ (-> (expect frame)
                  (.toContainText dataset/netflix-name #js {:timeout 60000}))
            body (.locator frame "[data-testid=\"table-body\"]")
            _ (-> (expect body) (.toBeVisible))
            achieved-scroll-left (.evaluate body
                                            (fn [el offset]
                                              (set! (.-scrollLeft el) offset)
                                              (.dispatchEvent el (js/Event. "scroll"))
                                              (.-scrollLeft el))
                                            scroll-offset)
            _ (-> (expect achieved-scroll-left) (.toBe scroll-offset))
            _ (.waitForTimeout page 1000)
            ;; The body scrolls natively and virtualizes its rendered columns
            ;; off that raw scroll position regardless of whether the header
            ;; stays in sync, so at this offset column 0 is gone from the
            ;; body's DOM entirely in both the working and the broken case -
            ;; there is nothing there to compare. Column 2 is the first index
            ;; that stays rendered on both sides, so it's the first index
            ;; that can actually catch a desync.
            header-cell (cell frame "table-header-cell" 2)
            body-cell (cell frame "table-body-cell" 2)
            _ (-> (expect header-cell) (.toBeVisible))
            _ (-> (expect body-cell) (.toBeVisible))
            header-box (.boundingBox header-cell)
            body-box (.boundingBox body-cell)
            drift (js/Math.abs (- (.-x header-box) (.-x body-box)))]
      (-> (expect drift) (.toBeLessThan 2)))))
