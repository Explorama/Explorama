(ns de.explorama.frontend.search.event-logging
  (:require [ajax.core :as ajax]
            [clojure.string :as string]
            [cljs.reader :as edn]
            [de.explorama.frontend.common.event-logging.util :as log-util :refer [not-creating-and-target?
                                                                                  base-desc
                                                                                  access-attrs]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.search.backend.di :as di-backend]
            [taoensso.timbre :refer-macros [debug error]]
            [de.explorama.frontend.search.util :as sutils]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.config :as config]))

(def event-version 1.0)

(defn is-suche? [frame-id]
  (= config/default-vertical-str
     (:vertical frame-id)))

(defn check-frame-id [frame-id callback-vec body]
  (if (= config/default-vertical-str (:vertical frame-id))
    body
    (do (debug "ignore event " frame-id)
        {:dispatch callback-vec})))

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ _]
   #_{:dispatch (fi/call-api :info-event-vec (str "Event-logging success" resp))}
   {}))

(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ [_ resp]]
   {:dispatch (fi/call-api :error-event-vec (str "Event-logging failed" resp))}))

(re-frame/reg-event-fx
 ::log-event
 [(fi/ui-interceptor)]
 (fn [{db :db}
      [_ frame-id event-name description]]
   (when-let [log-fn (fi/call-api :service-target-db-get db :project-fns :event-log)]
     (log-fn db
             config/default-vertical-str
             frame-id
             event-name
             description
             event-version))))

(re-frame/reg-event-fx
 ::log-pseudo-init
 (fn [{db :db} _]
   (let [workspace-id (fi/call-api :workspace-id-db-get db)
         pseudo-frame-id {:workspace-id workspace-id
                          :vertical "search"}]
     {:dispatch [::log-event pseudo-frame-id "init-search" nil]})))

(defn create-data-instance-event
  "time dependent data instance op"
  [db frame-id callback-vec {:keys [formdata diid]}]
  {:db (-> db
           (assoc-in (spath/frame-wait-callback frame-id) callback-vec)
           (assoc-in (spath/frame-direct-vis-opened? frame-id) true))
   :dispatch-n [[::di-backend/replay-create-data-instance formdata frame-id diid]]})

(defn create-frame-event
  "frame-time-dependent-op"
  [db frame-id callback-vec {:keys [vertical]}]
  (check-frame-id
   frame-id
   callback-vec
   {:db (-> db
            (assoc-in (spath/frame-event-callback frame-id) callback-vec)
            (assoc-in [:search :replay frame-id] true))
    :dispatch-n [[:de.explorama.frontend.search.views.attribute-bar/init-request-attributes frame-id]]}))

(defn set-title-event [_ frame-id callback-vec {:keys [title]}]
  {:dispatch-n [callback-vec
                [:de.explorama.frontend.search.komplexe-suche/set-title title false frame-id]]})

(defn reset-attr-filter-event [_ frame-id callback-vec {:keys [path changed? follow-event follow-event-attr]}]
  {:dispatch-n [callback-vec
                [:de.explorama.frontend.search.views.formdata/row-changed-wrapper path changed? [follow-event frame-id follow-event-attr] frame-id]]})

(defn close-event [db frame-id callback-vec _]
  (check-frame-id
   frame-id
   callback-vec
   {:db       (sutils/clean-frames db [frame-id])
    :dispatch callback-vec}))

(defn no-event [_ _ callback-vec _]
  {:dispatch callback-vec})

(def events-vektor-funcs
  "[event-name event-version]"
  {["create-data-instance" 1] create-data-instance-event
   ["filter-data-instance" 1] create-data-instance-event
   ["create-frame" 1] create-frame-event
   ["set-title" 1] set-title-event
   ["reset-attr-filter" 1] reset-attr-filter-event
   ["move-frame" 1] no-event
   ["resize-stop" 1] no-event
   ["maximize" 1] no-event
   ["minimize" 1] no-event
   ["normalize" 1] no-event
   ["close-frame" 1] close-event
   ["connect" 1] no-event
   ["update-connection" 1] no-event
   ["z-index" 1] no-event})

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (get events-vektor-funcs [event-name event-version])]
     (debug "replay-event search" {:frame-id frame-id
                                   :event-name event-name
                                   :callback-vec callback-vec
                                   :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id callback-vec parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         {:dispatch callback-vec})))))

(re-frame/reg-event-fx
 ::next-replay-event
 (fn [{db :db} [_ current-event rest-events events-done max-events done-event plogs-id profiling-state]]
   (let [profiling-state (when profiling-state
                           (-> (assoc-in profiling-state
                                         [(get profiling-state :last-event)
                                          :end]
                                         (.now js/Date.))
                               (assoc current-event {:start (.now js/Date.)
                                                     :num (inc events-done)}
                                      :last-event current-event)))]
     (when (aget js/window "sendHealthPing")
       (.sendHealthPing js/window))
     {:db (assoc-in db spath/replay-progress (/ events-done
                                                max-events))
      :dispatch (if current-event
                  [::replay-event current-event
                   [::next-replay-event
                    (first rest-events)
                    (rest rest-events)
                    (inc events-done)
                    max-events
                    done-event
                    plogs-id
                    (when profiling-state
                      profiling-state)]]
                  (cond-> done-event
                    profiling-state
                    (conj profiling-state)))})))

(def pre-process-events (partial log-util/pre-process-events
                                 is-suche?
                                 (constantly false)
                                 (constantly false)))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (get events-vektor-funcs [event-name event-version])]
     (debug "sync-event search" {:frame-id frame-id
                                 :event-name event-name
                                 :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id nil parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(re-frame/reg-event-fx
 ::replay-events
 (fn [{db :db} [_ events done-event plogs-id test-and-profile?]]
   (let [{events :result} (pre-process-events events)]
     {:dispatch [::next-replay-event
                 (first events)
                 (rest events)
                 0
                 (count events)
                 done-event
                 plogs-id
                 (when test-and-profile? test-and-profile?)]})))

(re-frame/reg-event-fx
 ::ui-wrapper
 (fn [{db :db} [_ frame-id event-name event-params]]
   (let [event-func (get events-vektor-funcs [event-name event-version])]
     (if event-func
       (merge-with into
                   (event-func db frame-id nil event-params)
                   {:dispatch-n [[::log-event frame-id event-name event-params]]})
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(defn search-settings [formdata filter? db]
  (let [lang (fi/call-api [:config :get-config-db-get]
                          db
                          :i18n
                          :lang)
        attr-labels (fi/call-api [:i18n :get-labels-db-get] db)
        {:keys [any-empty-values-label
                any-non-empty-values-label
                unknown-setting-label]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :any-empty-values-label
                              :any-non-empty-values-label
                              :unknown-setting-label])]
    (string/join "\n"
                 (reduce (fn [acc [[attr] {vals :values
                                           val :value
                                           all-values? :all-values?
                                           empty-values? :empty-values?
                                           from :from
                                           to :to
                                           active? :active?
                                           sel-date :selected-date
                                           s-date :start-date
                                           e-date :end-date
                                           {condi :label} :cond
                                           :as params}]]
                           (let [use-attr (if filter?
                                            active?
                                            true)
                                 attribute-label (i18n/attribute-label attr-labels attr)
                                 [vals val] (if (= attr "month")
                                              [(map #(i18n/month-name % lang) vals)
                                               (i18n/month-name val lang)]
                                              [vals val])]
                             (if use-attr
                               (conj acc
                                     (str attribute-label
                                          ": "
                                          (if condi
                                            (str condi " ")
                                            "")
                                          (cond
                                            vals (string/join ", " vals)
                                            (and condi
                                                 val
                                                 (= attr
                                                    "year"))
                                            (str (:label val))
                                            (and (= attr
                                                    "year")
                                                 from
                                                 to)
                                            (str (:label from) " - " (:label to))
                                            (and from
                                                 to)
                                            (str from " - " to)
                                            val val
                                            sel-date sel-date
                                            (and s-date
                                                 e-date) (str s-date " - " e-date)
                                            all-values?
                                            any-non-empty-values-label
                                            empty-values?
                                            any-empty-values-label
                                            :else unknown-setting-label)))
                               acc)))
                         []
                         formdata))))

(defn- action-desc [base-op action both only-old only-new new-desc db]
  (cond (= "create-data-instance" action)
        (base-desc :search-protocol-action-search
                   (search-settings (:formdata new-desc)
                                    false db))))

(def events->steps (partial log-util/events->steps "search" action-desc))