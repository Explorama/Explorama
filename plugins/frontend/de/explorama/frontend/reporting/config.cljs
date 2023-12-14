(ns de.explorama.frontend.reporting.config
  (:require [clojure.string :refer [includes?]]
            [cljs.reader :as edn]))
            ;[devtools.core :as devtools]

(def debug?
  ^boolean goog.DEBUG)

(def use-devtools? (aget js/window "EXPLORAMA_DEVTOOLS"))

(def enabled-notifications (edn/read-string (aget js/window "EXPLORAMA_ENABLED_NOTIFICATIONS")))
(def available-plugins (js->clj (aget js/window "EXPLORAMA_PLUGIN_LIST")))

(def supported-verticals #{"mosaic" "table" "charts" "map" "algorithms"})
(defn support-vertical? [vertical]
  (boolean (or (supported-verticals vertical)
               ;; For multiple components like visualization-table and -charts
               (some #(includes? vertical %)
                     supported-verticals))))

(def link-infos (edn/read-string (aget js/window "REPORTING_LINK_INFO")))

;; (when (or debug? use-devtools?)
;;   (devtools/install! [:formatters :hints]))

(def default-namespace :reporting)

(def dr-grid-dom-id (str ::dr-grid-dom-id))
(def standalone-dom-id (str ::standalone))

(defn export-dom-id
  ([details]
   (export-dom-id (:type details)
                  (:id details)))
  ([rep-type id]
   (str ::exp rep-type id)))

(def console-threshold 160) ; min-width of browser console
(def disable-default-context-menu? true) ; Enabled when Browser console is opened

(def legend-height 280)
(def legend-width 250)

(def export-theme :light)

;; For preventing problems drawing the pdf
(def ^number report-text-module-max-length 2000)

(def system-name (aget js/window "EXPLORAMA_SYSTEM_NAME"))
