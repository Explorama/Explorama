(ns de.explorama.frontend.charts.path
  (:require [taoensso.timbre :refer [error]]))

(def root-key :charts)
(def filter-key :filter)
(def chart-type-desc-key :type-desc)
(def chart-data-key :chart-data)
(def aggregate-method-key :aggregate-method)
(def y-option-key :y-option)
(def x-option-key :x-option)
(def r-option-key :r-option)
(def sum-by-option-key :sum-by)
(def sum-by-values-key :sum-values)
(def sum-remaining-key :sum-remaining?)
(def attributes-key :attributes)
(def stopping-attrs-key :stopping-attributes)
(def stemming-attrs-key :stemming-attributes)
(def use-nlp?-key :use-nlp?)
(def use-nlp-attributes-key :use-nlp-attributes)
(def min-occurence-key :min-occurence)
(def search-selection-key :search-selection)
(def di-key :di)
(def data-loading-key :data-loading)
(def chartdata-loading-key :chart-data-loading)
(def chartdata-req-id-key :request-id)

(def replay-progress-key :replay-progress)
(def replay-progress [root-key replay-progress-key])

(defn frame-desc [frame-id]
  [root-key frame-id])

(defn removed-detail-view-events [frame-id]
  (conj (frame-desc frame-id) :removed-detail-view-events))

(defn di-desc [frame-id]
  (conj (frame-desc frame-id)
        :di-desc))

(defn dim-info [frame-id]
  (conj (di-desc frame-id)
        :dim-info))

(defn custom-title [frame-id]
  (conj (frame-desc frame-id)
        :custom-title))

(defn frame-external-refs [frame-id]
  (conj (frame-desc frame-id)
        :external-refs))

(defn frame-filter [frame-id]
  (conj (frame-desc frame-id)
        filter-key))

(defn filter-warn-limit-reached [frame-id]
  (conj (frame-filter frame-id)
        :filter-warn-limit-reached?))

(defn filter-stop-limit-reached [frame-id]
  (conj (frame-filter frame-id)
        :filter-stop-limit-reached?))

;; Handle Dialog -> Different then flags before, because user can hide
(defn stop-view-display [frame-id]
  (conj (frame-desc frame-id)
        :display-stop?))

(defn stop-view-details [frame-id]
  (conj (frame-desc frame-id)
        :stop-view-details))

(defn frame-warn [frame-id]
  (conj (frame-desc frame-id)
        :warn))

(defn warn-view-display [frame-id]
  (conj (frame-warn frame-id)
        :display?))

(defn warn-view-callback [frame-id]
  (conj (frame-warn frame-id)
        :callback))

(def warn-view-cancel-event-key :cancel-event)
(def warn-view-proceed-event-key :proceed-event)
(def warn-view-stop-event-key :stop-event)

(defn warn-view-cancel-event [frame-id]
  (conj (frame-desc frame-id)
        warn-view-cancel-event-key))

(defn warn-view-proceed-event [frame-id]
  (conj (frame-desc frame-id)
        warn-view-proceed-event-key))

(defn warn-view-stop-event [frame-id]
  (conj (frame-desc frame-id)
        warn-view-stop-event-key))

(defn applied-filter [frame-id]
  (conj (frame-desc frame-id)
        :applied-filter))

(defn show-filter? [frame-id]
  (conj (frame-desc frame-id)
        :show-filter?))

(defn last-request-params [frame-id]
  (conj (frame-desc frame-id)
        :last-request-params))

(def volatile-acs [root-key :volatile-acs])

(defn volatile-acs-frame [frame-id]
  [root-key :volatile-acs frame-id])

(def replay-update-needed-key :replay-update-con-needed)
(def replay-update-needed [root-key replay-update-needed-key])

(def replay-update-current-key :replay-update-con-done)
(def replay-update-current [root-key replay-update-current-key])

(defn dissoc-in [db path]
  (if (vector? path)
    (update-in db (pop path)
               dissoc
               (peek path))
    (do
      (error (str "can't dissoc-in. " path " is not a vector"))
      db)))

;Still used???

(def notifications-key :notifications)
(def not-supported-redo-ops-key :not-supported-redo-ops)
(def undo-connection-update-event-key :undo-connection-update-event)

(defn notifications [vis-path]
  (conj vis-path notifications-key))

(defn not-supported-redo-ops [vis-path]
  (conj (notifications vis-path)
        not-supported-redo-ops-key))

(defn undo-connection-update-event [vis-path]
  (conj vis-path undo-connection-update-event-key))

(defn reset-stop-views [db frame-id]
  (-> db
      (dissoc-in (stop-view-display frame-id))
      (dissoc-in (stop-view-details frame-id))
      (dissoc-in (filter-stop-limit-reached frame-id))
      (dissoc-in (filter-warn-limit-reached frame-id))))

(def min-width 375)
(def min-height 200)

(def chart-root [root-key])

(def chart-frame frame-desc)

(defn chart-data [frame-id]
  (conj (chart-frame frame-id)
        chart-data-key))

(def chart-desc-key :chart-desc)

(defn chart-desc [frame-id]
  (conj (chart-frame frame-id)
        chart-desc-key))

(defn x-option [frame-id]
  (conj (chart-desc frame-id)
        x-option-key))

(def charts-key :charts)

(defn charts [frame-id]
  (conj (chart-desc frame-id)
        charts-key))

(defn chart [frame-id chart-index]
  (conj (charts frame-id)
        chart-index))

(defn aggregate-method [frame-id chart-index]
  (conj (chart frame-id chart-index)
        aggregate-method-key))

(defn y-option [frame-id chart-index]
  (conj (chart frame-id chart-index)
        y-option-key))

(def y-range-change?-key :y-range-change?)

(defn y-range-change? [frame-id chart-index]
  (conj (chart frame-id chart-index)
        y-range-change?-key))

(def changed-y-range-key :changed-y-range)

(defn- changed-y-range [frame-id chart-index]
  (conj (chart frame-id chart-index)
        changed-y-range-key))

(defn changed-min-y [frame-id chart-index]
  (conj (changed-y-range frame-id chart-index)
        :min))

(defn changed-max-y [frame-id chart-index]
  (conj (changed-y-range frame-id chart-index)
        :max))

(def changed-y-range-valid?-key :change-valid?)

(defn changed-y-range-valid? [frame-id chart-index]
  (conj (changed-y-range frame-id chart-index)
        changed-y-range-valid?-key))

(defn changed-min-y-valid? [frame-id chart-index]
  (conj (changed-y-range-valid? frame-id chart-index)
        :min))

(defn changed-max-y-valid? [frame-id chart-index]
  (conj (changed-y-range-valid? frame-id chart-index)
        :max))

(defn- org-y-range [frame-id chart-index]
  (conj (chart-data frame-id)
        chart-index
        :org-y-range))

(defn org-min-y [frame-id chart-index]
  (conj (org-y-range frame-id chart-index)
        :org-min-y))

(defn org-max-y [frame-id chart-index]
  (conj (org-y-range frame-id chart-index)
        :org-max-y))

(defn chart-type [frame-id chart-index]
  (conj (chart frame-id chart-index)
        chart-type-desc-key))

(defn r-option [frame-id chart-index]
  (conj (chart frame-id chart-index)
        r-option-key))

(defn sum-by-option [frame-id chart-index]
  (conj (chart frame-id chart-index)
        sum-by-option-key))

(defn sum-by-values [frame-id chart-index]
  (conj (chart frame-id chart-index)
        sum-by-values-key))

(defn sum-remaining [frame-id chart-index]
  (conj (chart frame-id chart-index)
        sum-remaining-key))

(defn attributes [frame-id chart-index]
  (conj (chart frame-id chart-index)
        attributes-key))

(defn stopping-attributes [frame-id chart-index]
  (conj (chart frame-id chart-index)
        stopping-attrs-key))

(defn stemming-attributes [frame-id chart-index]
  (conj (chart frame-id chart-index)
        stemming-attrs-key))

(defn use-nlp? [frame-id chart-index]
  (conj (chart frame-id chart-index)
        use-nlp?-key))

(defn use-nlp-attributes [frame-id chart-index]
  (conj (chart frame-id chart-index)
        use-nlp-attributes-key))

(defn min-occurence [frame-id chart-index]
  (conj (chart frame-id chart-index)
        min-occurence-key))

(defn search-selection [frame-id]
  (conj (chart-frame frame-id)
        search-selection-key))

(defn frame-di [frame-id]
  (conj (chart-frame frame-id)
        di-key))

(defn frame-request-id [frame-id]
  (conj (chart-frame frame-id)
        chartdata-req-id-key))

(defn hidden-datasets [frame-id]
  (conj (chart-frame frame-id)
        :hidden-datasets))

;############## Chart Desc #################
(def chart-desc-id-key :cid)
(def chart-desc-label-key :label)
(def chart-desc-content-key :content)
(def chart-desc-filter-key :filter)
(def chart-desc-settings-key :settings-panel)
(def chart-desc-selector-class-key :type-selector-class)
(def chart-desc-multiple-key :multiple-possible?)
(def chart-desc-icon-key :icon)

(def chart-desc-settings-update-key :settings-update-fn)

(def bar-id-key :bar)
(def scatter-id-key :scatter)
(def bubble-id-key :bubble)
(def line-id-key :line)
(def pie-id-key :pie)
(def wordcloud-id-key :wordcloud)

(defn desc-id [frame-id chart-index]
  (conj (chart-type frame-id chart-index)
        chart-desc-id-key))

(defn height [frame-id]
  (conj (chart-frame frame-id)
        :height))

(defn width [frame-id]
  (conj (chart-frame frame-id)
        :width))

