(ns de.explorama.frontend.search.path)

(def root-key :search)
(def search-key :search-form)
(def event-callback-key :event-callback)
(def frame-open-event-key :frame-open-event)
(def search-options-space :search)
(def data-instance-key :data-instance)
(def replay-progress-key :replay-progress)
(def search-query-key :search-query)
(def search-bar-key :search-bar)

(def search-formdata [root-key search-key])
(def data-instances [root-key data-instance-key])
(def attribute-types [root-key :attribute-types])
(def event-callback [root-key event-callback-key])
(def frame-open-event [root-key frame-open-event-key])
(def replay-progress [root-key replay-progress-key])
(def wait-callback-keys [root-key :wait-keys])
(def wait-callback [root-key :wait-callback])
(def search-query [root-key search-query-key])
(def search-bar [root-key search-bar-key])
(def search-bar-config [root-key :search-bar-config])

(def enabled-datasource-key :enabled-datasources)
(def search-enabled-datasources [root-key :enabled-datasources])
(def search-bucket-datasources [root-key :bucket-datasources])

(defn search-bar-frame [frame-id]
  (conj search-bar frame-id))
(defn search-bar-frame-result-open? [frame-id]
  (conj (search-bar-frame frame-id)
        :open?))
(defn search-bar-frame-results [frame-id]
  (conj (search-bar-frame frame-id)
        :results))
(defn search-bar-frame-result [frame-id result-key]
  (conj (search-bar-frame-results frame-id)
        result-key))
(defn search-bar-frame-task-id [frame-id]
  (conj (search-bar-frame frame-id)
        :task-id))

(defn search-frame-changed? [frame-id]
  [root-key :search-changed? frame-id])

(defn frame-desc [frame-id]
  [root-key frame-id])

(def data-descs-key :data-descs)
(defn data-descs [frame-id]
  (conj (frame-desc frame-id)
        data-descs-key))

(defn requesting [frame-id]
  (conj (frame-desc frame-id)
        :request-acs))

(defn create-data-instance [frame-id]
  (conj (frame-desc frame-id)
        :create-data-instance))

(defn frame-search-mode [frame-id]
  (conj (frame-desc frame-id)
        :search-mode))

(def frame-row-too-many-related-key :too-many-options-related)

(defn frame-row-too-many-related [frame-id]
  (conj (frame-desc frame-id)
        frame-row-too-many-related-key))

(defn frame-dialog-data [frame-id]
  (conj (frame-desc frame-id)
        :dialog-data))

(defn- frame-search-query [frame-id]
  (conj search-query frame-id))

(defn frame-direct-vis-opened? [frame-id]
  [root-key frame-id :direct-vis-opened?])

(defn search-query-open? [frame-id]
  (conj (frame-search-query frame-id)
        :open?))

(defn search-queries []
  (conj search-query
        :queries))

(defn search-query-desc [query-id]
  (conj (search-queries)
        query-id))

(defn data-instance [frame-id]
  (conj data-instances frame-id))

(defn is-search-path? [[_ space-key]]
  (= search-key space-key))

(defn frame-id [path]
  (get path 2))

(defn di-creation-pending [fid]
  [root-key fid :di-creation-pending])

(defn di-creation-success [fid]
  [root-key fid :di-creation-success])

(defn traffic-light-status [fid]
  [root-key fid :traffic-light])

(defn undo-last-search-step [frame-id]
  [root-key frame-id :undo-last-search-step])

(def base-visualization-options [root-key :visualization-options])

(defn attr [path]
  (get path 3))

(def options-key :options)
(def topic-selection-key :topic-selection?)

(def all-attributes-key :all-attributes)
(def attributes-key :attributes)

(def all-attributes [root-key all-attributes-key])

(defn attribute-type [attr]
  (conj attribute-types
        attr))

(defn frame-attributes [frame-id]
  [root-key frame-id attributes-key])

(defn frame-event-callback [frame-id]
  (conj event-callback
        frame-id))

(defn frame-wait-callback-keys [frame-id]
  (conj wait-callback-keys
        frame-id))

(defn frame-wait-callback [frame-id]
  (conj wait-callback
        frame-id))

(defn frame-search-rows [frame-id]
  (conj search-formdata frame-id))

(defn search-row-data [frame-id attr]
  (conj search-formdata frame-id attr))

(defn search-row-options [frame-id attr]
  (conj (search-row-data frame-id attr)
        options-key))

(defn search-row-topic-select [frame-id attr]
  (conj (search-row-data frame-id attr)
        topic-selection-key))

;------------ formdata -----------
(def advanced-key :advanced)
(def condition-key :cond)
(def value-key :value)
(def values-key :values)
(def to-key :to)
(def from-key :from)
(def selected-date-key :selected-date)
(def last-x-value-key :last-x)
(def start-date-key :start-date)
(def end-date-key :end-date)
(def ui-selection-key :ui-selection)
(def all-values-key :all-values?)
(def empty-values-key :empty-values?)

(defn advanced [path]
  (conj path advanced-key))

(defn condition [path]
  (conj path condition-key))

(defn value [path]
  (conj path value-key))

(defn values [path]
  (conj path values-key))

(defn options [path]
  (conj path options-key))

(defn to [path]
  (conj path to-key))

(defn from [path]
  (conj path from-key))

(defn selected-date [path]
  (conj path selected-date-key))

(defn last-x-value [path]
  (conj path last-x-value-key))

(defn start-date [path]
  (conj path start-date-key))

(defn end-date [path]
  (conj path end-date-key))

(defn ui-selection [path]
  (conj path ui-selection-key))

(defn all-values? [path]
  (conj path all-values-key))

(defn empty-values? [path]
  (conj path empty-values-key))