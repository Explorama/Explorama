(ns de.explorama.frontend.table.config
  (:require [de.explorama.frontend.common.i18n :as i18n]))

(def default-namespace :table)
(def default-vertical-str (name default-namespace))

(def tool-name-table "tool-table")
(defn frame-body-dom-id-table [frame-id]
  (str frame-id "__" default-vertical-str "-frame-body"))

(def vis-origin (aget js/window "EXPLORAMA_table_ORIGIN"))

;; ----- table -----

(def ^number column-width 120) ;px
(def ^number scrollbar-width 17) ;px

; Timeout in ms after some change like scrolling to check if it should be logged
(def ^number table-delayed-logging-timeout 3000)
(def ^number double-click-timeout 400) ;ms between 2 on-click
(def available-page-sizes [1000 500 250 100])

(defn ^String attribute->display [attribute]
  (str (i18n/attribute-label-with-unit attribute)))

;<attribute> <num px>
;; (def default-column-sizes {"date" 100
;;                            "location" 100
;;                            "organisation" 160
;;                            "notes" 200})

(def ^number default-page-size (first available-page-sizes))
(def default-sorting [{:attr "date" :direction "asc"}])

(def ignore-attributes #{"id"})
(def default-freeze-attributes ["date" "annotation"])
