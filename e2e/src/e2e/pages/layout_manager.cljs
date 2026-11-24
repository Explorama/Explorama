(ns e2e.pages.layout-manager
  (:require [e2e.pages.workspace :as ws]
            [promesa.core :as p]))

;; The header tool renders as navbar-item-<tool-id>-<icon>, so the tool id
;; alone is the stable part of it.
(def ^:private tool-sel "[id^=\"navbar-item-layout-manager\"]")
(def ^:private rows-sel ".color__assignments .drag-container")
(def ^:private handles-sel ".color__assignments .drag-handle")

(defn open [page]
  (p/do
    (ws/open-workspace page)
    (.click (.locator page tool-sel))
    (.waitForSelector page ".sidebar" #js {:timeout 15000})))

(defn create-layout [page]
  (p/do
    (.click (.getByRole page "button" #js {:name "Create Layout"}))
    (.waitForSelector page rows-sel #js {:timeout 15000})))

(defn rows [page]
  (.locator page rows-sel))

(defn handles [page]
  (.locator page handles-sel))

(defn add-color [page]
  (p/let [before (.count (rows page))]
    (p/do
      (.click (-> (.locator page ".color__assignments__row.empty")
                  (.filter #js {:hasText "Add color"})))
      (-> (.nth (rows page) before) (.waitFor #js {:timeout 10000})))))

;; A row's colour input is the only per-row value a reorder carries along
;; unchanged, so the order of these is what says which row moved where.
(defn swatches [page]
  (.evaluate page
             (str "Array.from(document.querySelectorAll('" rows-sel
                  " input[type=color]')).map(e => e.value)")))

;; The handle is the row's only non-interactive spot: the drag sensor
;; refuses to start on an input, select or button, and the rest of the row
;; is nothing but those.
(defn drag-row [page from-idx to-idx]
  (p/let [handle (.nth (handles page) from-idx)
          target (.nth (rows page) to-idx)
          hb     (.boundingBox handle)
          tb     (.boundingBox target)
          sx     (+ (.-x hb) (/ (.-width hb) 2))
          sy     (+ (.-y hb) (/ (.-height hb) 2))
          tx     (+ (.-x tb) (/ (.-width tb) 2))
          ty     (+ (.-y tb) (/ (.-height tb) 2))]
    (p/do
      (.move (.-mouse page) sx sy)
      (.down (.-mouse page))
      ;; the sensor arms only once the pointer clears its 5px start
      ;; threshold, and it tracks the drag through real intermediate moves
      (.move (.-mouse page) sx (+ sy 12) #js {:steps 5})
      (.move (.-mouse page) tx ty #js {:steps 20})
      (.waitForTimeout page 400)
      (.up (.-mouse page))
      (.waitForTimeout page 700))))

;; The accessible drag the library ships: the row itself carries the drag
;; handle props, so it is focusable and space/arrow/space lifts, moves and
;; drops it without a pointer.
(defn keyboard-move-row [page from-idx direction]
  (p/do
    (.focus (.nth (rows page) from-idx))
    (.press (.-keyboard page) "Space")
    (.waitForTimeout page 300)
    (.press (.-keyboard page) direction)
    (.waitForTimeout page 300)
    (.press (.-keyboard page) "Space")
    (.waitForTimeout page 700)))
