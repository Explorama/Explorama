(ns de.explorama.frontend.indicator.path
  (:require [taoensso.timbre :refer [error]]))

(def root :indicator)
(def replay [root :replay])
(def session [root :session])

(def volatile-acs [root :volatile-acs])

(defn replay=? [db frame-id]
  (get-in db (conj replay
                   frame-id)))

(def replay-progress-key :replay-progress)
(def replay-progress [root replay-progress-key])

(def templates-key :templates)
(def indicators-key :indicators)
(def project-indicators-key :indicators-project)
(def new-indicator-key :new-indicator)
(def changes-key :changes)
(def active-indicator-key :active-indicator)
(def connected-data-key :connected-data)
(def changed-connected-data-key :changed-connected-data)
(def loading-key :data-loading?)
(def dialog-show?-key :dialog-show?)
(def open-frame-id-key :current-frame-id)
(def preview-result-key :preview-result)
(def clean-up-keys
  [templates-key indicators-key new-indicator-key
   changes-key active-indicator-key connected-data-key
   loading-key open-frame-id-key preview-result-key
   changed-connected-data-key replay-progress-key])

(def open-frame-id
  [root open-frame-id-key])

(def loading? [root loading-key])

(def show? [root dialog-show?-key])

(def indicator-ui-templates
  [root templates-key])

(defn template-desc [template-key]
  (conj indicator-ui-templates
        template-key))

(defn template-ui-desc [template-key]
  (conj (template-desc template-key)
        :ui))

(defn template-calc-desc [template-key]
  (conj (template-desc template-key)
        :description))

(def project-indicators [root project-indicators-key])

(defn project-indicator-desc [indicator-id]
  (conj project-indicators
        indicator-id))

(def indicators [root indicators-key])

(defn indicator-desc [indicator-id]
  (conj indicators
        indicator-id))

(def new-indicator [root new-indicator-key])

(def indicators-changes [root changes-key])

(defn indicator-changes [indicator-id]
  (conj indicators-changes
        indicator-id))

(defn indicator-prop-change [indicator-id property]
  (conj (indicator-changes indicator-id)
        property))

(defn indicator-ui-desc-change [indicator-id template-key comp-id]
  (conj (indicator-prop-change indicator-id :ui-desc)
        template-key
        comp-id))

(defn indicator-addon-rows [indicator-id template-key]
  (indicator-ui-desc-change indicator-id template-key :additional))

(defn indicator-addon-row-value [indicator-id template-key row-id attribute-id]
  (conj (indicator-ui-desc-change indicator-id template-key :additional)
        row-id
        attribute-id))

(def active-indicator [root active-indicator-key])

(def indicators-data [root connected-data-key])

(defn indicator-data [indicator-id]
  (conj indicators-data
        indicator-id))

(defn indicator-dataset [indicator-id di]
  (conj (indicator-data indicator-id)
        di))

(def changed-indicators-data [root changed-connected-data-key])

(defn changed-indicator-data [indicator-id]
  (conj changed-indicators-data
        indicator-id))

(defn added-indicator-data [indicator-id]
  (conj (changed-indicator-data indicator-id)
        :added))

(defn removed-indicator-data [indicator-id]
  (conj (changed-indicator-data indicator-id)
        :removed))

(def preview-results [root preview-result-key])

(defn preview-result [indicator-id]
  (conj preview-results
        indicator-id))

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

(def indicator-dialog-root
  [root :dialog])

(defn dialog [dialog-key]
  (conj indicator-dialog-root dialog-key))

(defn dialog-is-active? [dialog-key]
  (conj (dialog dialog-key)
        :is-active?))

(defn dialog-tag [dialog-key]
  (conj (dialog dialog-key)
        :tag))

(defn dialog-data [dialog-key]
  (conj (dialog dialog-key)
        :data))