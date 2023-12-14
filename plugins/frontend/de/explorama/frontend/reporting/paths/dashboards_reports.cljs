(ns de.explorama.frontend.reporting.paths.dashboards-reports
  (:require [de.explorama.frontend.reporting.paths.discovery-base :as discovery-base-path]
            [clojure.string :refer [index-of]]
            [taoensso.timbre :refer [error]]
            [taoensso.tufte :as tufte]))

(def dashboards-reports-root-key :dashboards-reports)
(def dashboards-reports-root (conj discovery-base-path/root dashboards-reports-root-key))


(def visible-drs (conj dashboards-reports-root :visible))
(defn visible-dr [id]
  (conj visible-drs id))


(def creation-key :creation)
(def creation (conj dashboards-reports-root creation-key))

(def creation-show? (conj creation :show?))
(def creation-edit-mode? (conj creation :edit-mode?))
(def creation-save-pending? (conj creation :save-pending?))
(def creation-save-response (conj creation :creation-save-response))


(def creation-dr-id (conj creation :id))
(def creation-name (conj creation :name))
(def creation-subtitle (conj creation :subtitle))
(def creation-type (conj creation :type))
(def creation-template-id (conj creation :template-id))
(def creation-selected-template (conj creation :selected-template))
(def creation-selected-text-module (conj creation :text-module))
(def creation-modules (conj creation :modules))
(defn creation-module-desc [tile-idx]
  (conj creation-modules tile-idx))

(def context-menu (conj creation :context-menu))
(def menu-type (conj context-menu :type))
(def tile (conj context-menu :tile-idx))
(def register-fns (conj context-menu :register-fns))
(def menu-tile (conj context-menu :tile-idx))
(def menu-position (conj context-menu :position))

(defn module-settings [tile-idx]
  (conj (creation-module-desc tile-idx) :state :used-settings))
(defn legend [tile-idx]
  (conj (module-settings tile-idx) :legend))
(defn show-legend? [tile-idx]
  (conj (legend tile-idx) :show?))
(defn legend-left? [tile-idx]
  (conj (legend tile-idx) :left?))
(defn options-selection [tile-idx]
  (conj (module-settings tile-idx) :selection))

(def burger-menu-infos
  (conj dashboards-reports-root :burger-menu))

(def share-key :share)
(def share (conj dashboards-reports-root share-key))

(def share-dr-desc (conj share :dr-desc))
(def share-with (conj share :share-with))
(def share-status (conj share :status))
(def share-last-removed (conj share :last-removed))
(def share-with-link (conj share :link))
(def share-with-value (conj share :share-with-value))
(def shared-with-users-key :user-read-only)
(def shared-with-users (conj share-with shared-with-users-key))
(def shared-with-groups-key :groups-read-only)
(def shared-with-groups (conj share-with shared-with-groups-key))
(def public-read-only? (conj share :public-read-only?))

(def mail-receiver (conj share :mail-receiver))
(def show-mail-text? (conj share :mail-text?))

(def dashboards-key :dashboards)
(def dashboards (conj dashboards-reports-root dashboards-key))
(def created-dashboards (conj dashboards-reports-root dashboards-key :created))
(def shared-dashboards (conj dashboards-reports-root dashboards-key :shared))

(defn dashboard [d-id]
  (conj created-dashboards d-id))
(defn shared-dashboard [d-id]
  (conj shared-dashboards d-id))

(def reports-key :reports)
(def reports (conj dashboards-reports-root reports-key))
(def created-reports (conj dashboards-reports-root reports-key :created))
(def shared-reports (conj dashboards-reports-root reports-key :shared))

(defn report [r-id]
  (conj created-reports r-id))
(defn shared-report [r-id]
  (conj shared-reports r-id))

(def tile-dom-id-postfix "content-tile_")
(def tile-idx-separator "___")

(defn dom-id->tile-idx [dom-id]
  (when-let [sep-idx (index-of dom-id tile-idx-separator)]
    (js/parseInt (subs dom-id 0 sep-idx))))

(defn tile-idx->dom-id [tile-idx]
  (str tile-idx tile-idx-separator tile-dom-id-postfix))

(defn template->dom-ids [{:keys [tiles]}]
  (let [tile-ids (range 0 (count tiles))]
    (when tile-ids
      (mapv tile-idx->dom-id tile-ids))))

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))