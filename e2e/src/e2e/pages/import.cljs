(ns e2e.pages.import
  (:require [e2e.fixtures.dataset :as dataset]
            [promesa.core :as p]))

;; The import tool is registered with :tool-group :hidden, so the welcome
;; screen's Import card is its only entry point.
(def ^:private welcome-card-text "Use your data for analysis.")

(defn upload-csv [page path]
  (p/do
    (.click (.getByRole page "button" #js {:name welcome-card-text}))
    (.setInputFiles (.first (.locator page "input[type=file]")) path)))

;; The warning/summary/done dialogs and the workspace behind the overlay both
;; render their own "Close"-labeled controls, so every locator here is scoped
;; to the import overlay panel to avoid ambiguity.
;;
;; .welcome__page is also the base welcome screen's class; scoping here is
;; safe only because the Import card dispatches welcome-active false and
;; the tool-start in one :fx, so the two are never mounted together.
(defn- overlay [page]
  (.locator page ".welcome__page"))

(defn- overlay-button [page name]
  (.getByRole (overlay page) "button" #js {:name name :exact true}))

(defn set-fact-unit [page column unit]
  (let [field (.getByRole (overlay page) "textbox"
                          #js {:name (str "Unit for " column) :exact true})]
    (p/do
      (.fill field unit)
      (.blur field))))

;; The mapping step's dialogs (warning/summary/done) each unmount entirely
;; when hidden, so a button locator never matches across two of them at
;; once - but two of the three happen to share a button label ("Yes"), so
;; a wait keyed on the button alone can't tell which one is currently up.
;;
;; The fixture's CSV has no explicit id column, so the mapping's warning
;; dialog always appears once ("The ID for each row will be automatically
;; generated."); proceeding through it ("Yes") triggers the real backend
;; import-file call, whose success re-shows an import-summary dialog also
;; confirmed via "Yes" - that second click is the actual commit-import call.
(defn commit-as [page expect datasource-name event-count]
  (p/do
    (-> (expect (overlay-button page "Import")) (.toBeVisible #js {:timeout 30000}))
    (.fill (.getByRole (overlay page) "textbox" #js {:name "Datasource name"}) datasource-name)
    (.blur (.getByRole (overlay page) "textbox" #js {:name "Datasource name"}))
    (.click (overlay-button page "Import"))
    (.click (overlay-button page "Yes"))
    (-> (expect (.getByText (overlay page) (str "New Events: " event-count)))
        (.toBeVisible #js {:timeout 30000}))
    (.click (overlay-button page "Yes"))
    (-> (expect (overlay-button page "OK")) (.toBeVisible #js {:timeout 30000}))
    (.click (overlay-button page "OK"))
    (.click (overlay-button page "Close"))
    (.waitForSelector page ".welcome__page" #js {:state "detached" :timeout 30000})))

(defn commit [page expect]
  (commit-as page expect dataset/import-name 3))
