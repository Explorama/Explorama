(ns e2e.pages.workspace
  (:require [promesa.core :as p]))

(def ^:private frame-prefix
  {:search     "woco_frame-search-"
   :table      "woco_frame-table-"
   :mosaic     "woco_frame-mosaic-"
   :map        "woco_frame-map-"
   :charts     "woco_frame-charts-"
   :prediction "woco_frame-algorithms-"
   :note       "woco_frame-notes-"})

(defn frames [page]
  (.locator page ".frame"))

;; .last: when a plugin has multiple frames, this is the most recently
;; created one.
(defn frame [page plugin-kw]
  (.last (.locator page (str "[id^=\"" (get frame-prefix plugin-kw) "\"]"))))

;; Both overlays are optional and may mount asynchronously after
;; #workspace-root appears, so absence must not be treated as failure: a
;; bounded, auto-retrying click that swallows its own timeout is the only
;; way to wait for "either it appears and gets dismissed, or it never
;; shows up" without a fixed sleep.
(defn- dismiss-optional [locator]
  (-> (.click locator #js {:timeout 5000})
      (p/catch (fn [err]
                 (if (= "TimeoutError" (.-name err))
                   nil
                   (throw err))))))

(defn- dismiss-welcome [page]
  (dismiss-optional (.getByRole page "button" #js {:name "Close overview"})))

;; The hint carousel's other button is "next", which advances it rather than
;; closing it.
(defn- dismiss-tour [page]
  (dismiss-optional (.getByRole (.locator page ".window-handling-tour")
                                "button" #js {:name "Close"})))

(defn dismiss-overlays [page]
  (p/do
    (dismiss-welcome page)
    (dismiss-tour page)))

(defn open-workspace [page]
  (p/do
    (.goto page "/" #js {:waitUntil "load"})
    (.waitForSelector page "#workspace-root" #js {:timeout 30000})
    (dismiss-overlays page)))

(defn open-welcome [page]
  (p/do
    (.goto page "/" #js {:waitUntil "load"})
    (.waitForSelector page "#workspace-root" #js {:timeout 30000})))

;; connect drops on the target's centre, so a frame wider than the viewport
;; gets its drop point clamped to the edge and the connection never lands.
;; Zooming out first is what keeps a wide frame - the prediction one - whole.
(defn zoom-out [page steps]
  (p/run! (fn [_] (.click (.locator page "#viewport-zoom-out")))
          (range steps)))

(defn create-frame [page tool-id x y]
  (p/do
    (.click (.locator page tool-id))
    (.waitForSelector page ".window-placement-overlay" #js {:timeout 10000})
    (.click (.-mouse page) x y)
    (.waitForSelector page ".window-placement-overlay"
                      #js {:state "detached" :timeout 10000})))

(def ^:private blank-png
  (.from js/Buffer
         "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
         "base64"))

;; Both configured tile hosts (config.cljc: the default OpenStreetMap
;; mirror and the two Swiss mirrors), matched on hostname so a
;; cache-busted query string still matches.
(def ^:private tile-hosts
  #{"a.tile.openstreetmap.de" "tile.osm.ch"})

(defn- tile-request? [url]
  (contains? tile-hosts (.-hostname url)))

;; A request matching a tile host proves nothing by itself - Playwright
;; fires "request" whether or not a route answers it. serverAddr() is the
;; signal that distinguishes the two: it resolves to null only when the
;; response came from fulfill() and no socket was opened. A request that
;; never got a response at all is just as much a leak as one that reached
;; the real host, so a missing response also counts.
(defn- request-leaked? [request]
  (p/let [response (.response request)]
    (if (nil? response)
      true
      (p/let [addr (.serverAddr response)]
        (boolean addr)))))

;; Registers the interception and, independently, a plain request listener
;; that records every request matching a tile host regardless of whether
;; the route above claims it. Returns a promise resolving to a 0-arg
;; accessor for that record, so callers can await route registration
;; before navigating rather than racing it.
(defn stub-map-tiles [page]
  (let [seen (atom [])]
    (.on page "request"
         (fn [request]
           (when (tile-request? (js/URL. (.url request)))
             (swap! seen conj request))))
    (p/then (.route page tile-request?
                    (fn [route _request]
                      (.fulfill route #js {:status 200
                                           :contentType "image/png"
                                           :body blank-png})))
            (fn [_] (fn [] @seen)))))

(defn assert-no-live-tile-requests [expect requests-fn]
  (p/let [requests (requests-fn)
          leaked?  (p/all (map request-leaked? requests))]
    (-> (expect (count requests)) (.toBeGreaterThan 0))
    (-> (expect (boolean (some true? leaked?))) (.toBe false))))

;; Resolves to a JS array of str-printed frame-descriptor maps (e.g.
;; "{:frame-id \"table-...\", :workspace-id \"...\", :vertical \"table\"}"),
;; not bare id strings.
(defn frame-ids [page]
  (.evaluate page "de.explorama.frontend.woco.debug_api.frame_ids()"))

(defn connect [page source-kw target-kw]
  (p/let [src (.boundingBox (frame page source-kw))
          dst (.boundingBox (frame page target-kw))
          sx  (+ (.-x src) (/ (.-width src) 2))
          ;; The frame header is 40px tall; y+8 sits below re-resizable's
          ;; 5px top drag handle and clear of the corner resize handles.
          sy  (+ (.-y src) 8)
          dx  (+ (.-x dst) (/ (.-width dst) 2))
          dy  (+ (.-y dst) (/ (.-height dst) 2))]
    (p/do
      (.move (.-mouse page) sx sy)
      (.down (.-mouse page))
      (.move (.-mouse page) dx dy #js {:steps 25})
      (.up (.-mouse page)))))
