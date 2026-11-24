(ns e2e.specs.workspace-mechanics
  (:require [e2e.registry :refer [defspec]]
            [e2e.pages.workspace :as ws]
            [promesa.core :as p]))

(def ^:private transform-sel
  "#\\:de\\.explorama\\.frontend\\.woco\\.config\\/frames-transform")

(defspec "zooming out rescales the workspace"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (.click (.locator page "#viewport-zoom-out"))
      (-> (expect (.locator page transform-sel))
          (.toHaveAttribute "style" (js/RegExp. "scale\\(0\\.9\\)"))))))

(defspec "resetting zoom restores the default scale"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (.click (.locator page "#viewport-zoom-out"))
      (-> (expect (.locator page transform-sel))
          (.toHaveAttribute "style" (js/RegExp. "scale\\(0\\.9\\)")))
      (.click (.locator page "#viewport-zoom-reset"))
      (-> (expect (.locator page transform-sel))
          (.toHaveAttribute "style" (js/RegExp. "scale\\(1\\)"))))))

(defspec "closing a frame removes it from the workspace"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (ws/create-frame page "#tool-table" 700 400)
      (-> (expect (ws/frame page :table)) (.toBeVisible))
      (.click (.getByRole (ws/frame page :table) "button" #js {:name "Close"}))
      (-> (expect (ws/frame page :table)) (.toHaveCount 0)))))

(defspec "multiple frames of different plugins coexist"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (ws/create-frame page "#tool-table" 500 350)
      (ws/create-frame page "#tool-note" 1100 350)
      (-> (expect (ws/frame page :table)) (.toBeVisible))
      (-> (expect (ws/frame page :note)) (.toBeVisible))
      (-> (expect (.locator (ws/frame page :note) ".ql-editor"))
          (.toBeVisible)))))

(defspec "app-db frame registry matches the rendered frames"
  (fn [page expect]
    (p/do
      (ws/open-workspace page)
      (ws/create-frame page "#tool-table" 600 400)
      (ws/create-frame page "#tool-note" 1100 400)
      (-> (expect (ws/frames page)) (.toHaveCount 2))
      (p/let [ids      (ws/frame-ids page)
              rendered (.count (ws/frames page))]
        (-> (expect (.-length ids)) (.toBe rendered))))))
