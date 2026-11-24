(ns e2e.specs.legend-layout
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.layout-manager :as lm]
            [promesa.core :as p]))

;; A new layout starts with a single colour, and reordering needs at least
;; three rows to tell a move apart from a swap.
(defn- three-row-editor [page]
  (p/do
    (lm/open page)
    (lm/create-layout page)
    (lm/add-color page)
    (lm/add-color page)))

(defspec "the layout editor renders one draggable row per colour assignment"
  (fn [page expect]
    (p/do
      (three-row-editor page)
      (-> (expect (lm/rows page)) (.toHaveCount 3))
      (-> (expect (lm/handles page)) (.toHaveCount 3)))))

(defspec "dragging a colour row to the bottom reorders the assignments"
  (fn [page expect]
    (p/do
      (three-row-editor page)
      (p/let [before (lm/swatches page)]
        (p/do
          (lm/drag-row page 0 2)
          (p/let [after (lm/swatches page)]
            (-> (expect after)
                (.toEqual #js [(aget before 1) (aget before 2) (aget before 0)]))))))))

(defspec "a colour row can be reordered by keyboard alone"
  (fn [page expect]
    (p/do
      (three-row-editor page)
      (p/let [before (lm/swatches page)]
        (p/do
          (lm/keyboard-move-row page 0 "ArrowDown")
          (p/let [after (lm/swatches page)]
            (-> (expect after)
                (.toEqual #js [(aget before 1) (aget before 0) (aget before 2)]))))))))
