(ns e2e.pages.search
  (:require [e2e.pages.workspace :as ws]
            [promesa.core :as p]))

;; Ids are frame-scoped, so both locators are prefix matches inside the frame.
(defn traffic-light [page]
  (.locator (ws/frame page :search) "[id^=search-traffic-light-]"))

(defn- run-button [page]
  (.locator (ws/frame page :search) "[id^=search-run-]"))

;; A successful search resets the form's "changed" flag, which is itself one
;; of the run button's disable conditions, so the button never re-enables
;; after a search completes. Completion is observed instead through the
;; green ready indicator that the search view renders next to the button
;; once the result has been created.
(defn- ready-indicator [page]
  (.locator (ws/frame page :search) "[id^=search-ready-]"))

(defn open [page]
  (p/do
    (ws/create-frame page "#tool-search" 480 340)
    (.click (.first (.getByText (ws/frame page :search) "Topic/Datasource")))))

;; The picker is a custom combobox, not a <select>: open it, then choose the
;; option by its visible label.
(defn select-datasource [page label]
  (p/do
    (.click (.first (.getByText (ws/frame page :search) "Select...")))
    (.click (.first (.getByText page label #js {:exact true})))))

;; Valid for a frame's first submission only: the ready indicator does not
;; reset on a later edit, so a second run on an already-completed frame
;; needs a different completion signal.
(defn run [page expect]
  (p/do
    (-> (expect (run-button page)) (.toBeEnabled #js {:timeout 30000}))
    (.click (run-button page))
    (-> (expect (ready-indicator page)) (.toBeVisible #js {:timeout 60000}))))

;; Common preamble shared by every spec that needs a search frame with a
;; datasource picked, whether or not it goes on to run the search.
(defn open-with-datasource [page datasource]
  (p/do
    (ws/open-workspace page)
    (open page)
    (select-datasource page datasource)))

;; The same preamble, taken all the way through a completed run.
(defn run-with-datasource [page expect datasource]
  (p/do
    (open-with-datasource page datasource)
    (run page expect)))
