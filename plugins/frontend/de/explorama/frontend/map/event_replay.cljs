(ns de.explorama.frontend.map.event-replay
  (:require [cljs.reader :as edn]
            [de.explorama.frontend.common.event-logging.util :as log-util]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.configs.util :refer [translate-layer]]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.map.core :as map]
            [de.explorama.frontend.map.operations.tasks :as tasks]
            [de.explorama.frontend.map.paths :as geop]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug]]
            [vimsical.re-frame.cofx.inject :as inject]))

(defn is-map? [frame-id]
  (= config/default-vertical-str (:vertical frame-id)))

(defn check-frame-id [frame-id callback-vec body]
  (if (is-map? frame-id)
    body
    (do (debug "ignore event " frame-id)
        {:dispatch callback-vec})))

(defn save-callbackvec-in-appstate [db frame-id callback-vec]
  (ddq/set-event-callback db
                          frame-id
                          callback-vec))

(defn close-event [db frame-id callback-vec _ _ _]
  (check-frame-id
   frame-id
   callback-vec
   {:db         (update db geop/root dissoc frame-id)
    :dispatch-n [callback-vec]}))

(re-frame/reg-event-fx
 ::update-done-check
 (fn [{db :db} [_ callback-vec]]
   (let [max-to-be-waited (get-in db geop/replay-update-needed 1)
         new-needed-done (inc (get-in db geop/replay-update-needed 0))]
     (if (>= new-needed-done max-to-be-waited)
       {:db (update db geop/root dissoc geop/replay-update-needed-key geop/replay-update-current-key)
        :fx [(when callback-vec
               [:dispatch callback-vec])]}
       {:db (assoc-in db geop/replay-update-current new-needed-done)}))))

(defn recreate-frame [db _ callback-vec {:keys [frame-id] :as params}]
  {:db (save-callbackvec-in-appstate db frame-id callback-vec)
   :dispatch [:de.explorama.frontend.map.core/map-view-event :frame/recreate (assoc params
                                                                              :live? false)]})

(defn override-frame [db _ callback-vec {:keys [frame-id] :as params}]
  {:db (save-callbackvec-in-appstate db frame-id callback-vec)
   :dispatch [:de.explorama.frontend.map.core/map-view-event :frame/override (assoc params
                                                                              :live? false)]})

(defn create-di-event [_ _ callback-vec {:keys [di]}]
  {:fx [(when callback-vec
          [:dispatch (conj callback-vec di)])]})

(defn add-to-details-view-event
  [db frame-id callback-vec desc]
  (check-frame-id
   frame-id
   callback-vec
   {:db
    (update-in db
               (geop/removed-detail-view-events frame-id)
               #(-> (or % #{}) ;
                    (disj (:event-id desc))))
    :fx [[:dispatch [:de.explorama.frontend.map.views.details/add-to-details-view
                     frame-id
                     (assoc desc
                            :while-project-loading?
                            (boolean (fi/call-api :project-loading-db-get db)))]]
         (when callback-vec
           [:dispatch callback-vec])]}))

(defn remove-from-details-view
  [db frame-id callback-vec desc]
  (check-frame-id
   frame-id
   callback-vec
   {:db
    (update-in db
               (geop/removed-detail-view-events frame-id)
               #(-> (or % #{})
                    (conj (:event-id desc))))
    :fx [[:dispatch [:de.explorama.frontend.map.views.details/remove-from-details-view frame-id desc]]
         (when callback-vec
           [:dispatch callback-vec])]}))

(defn remove-event-from-details-view
  [_ frame-id callback-vec desc]
  (check-frame-id
   frame-id
   callback-vec
   {:fx [[:dispatch [:de.explorama.frontend.map.views.details/remove-event-from-details-view frame-id desc]]
         (when callback-vec
           [:dispatch callback-vec])]}))

(defn create-frame-event [db frame-id callback-vec _ _ _]
  {:db (-> (save-callbackvec-in-appstate db frame-id callback-vec)
           (assoc-in (geop/frame-desc frame-id) {}))
   :fx [[:dispatch [::ddq/register-tracks frame-id
                    [::map/register-state-tracker frame-id]]]
        [:dispatch [::ddq/queue frame-id
                    [::map/initialize frame-id]]]
        [:dispatch [::tasks/execute-wrapper frame-id :init-di {} false]]]})

(defn- prepare-operation [db payload]
  (let [raw-marker-layouts (get-in db geop/raw-marker-layouts)
        temp-marker-layouts (->> (:marker-layouts payload)
                                 (filter :temporary?)
                                 (map #(vector (:id %) %))
                                 (into {}))
        temp-feature-layers (->> (:feature-layers payload)
                                 (filter :temporary?)
                                 (map #(vector (:id %) %))
                                 (into {}))
        ;Fallback if the project step is too old where the translated marker-layout doesn't contain the field-assignments and the raw-layout doesn't exist
        payload (update payload
                        :marker-layouts
                        (fn [layouts]
                          (when layouts
                            (mapv (fn [{:keys [id field-assignments] :as layout}]
                                    (if (seq field-assignments)
                                      layout
                                      (or (translate-layer (get raw-marker-layouts id))
                                          (assoc layout
                                                 :card-scheme "scheme-2"
                                                 :field-assignments [["else" "date"]
                                                                     ["else" "datasource"]
                                                                     ["else" "country"]
                                                                     ["notes" "notes"]]))))
                                  layouts))))]
    [temp-marker-layouts temp-feature-layers payload]))

(defn operation-event [db frame-id callback-vec {:keys [action payload]}]
  (let [[temp-marker-layouts temp-feature-layers payload] (prepare-operation db payload)]
    {:db (-> db
             (save-callbackvec-in-appstate frame-id callback-vec)
             (update-in (geop/temp-raw-marker-layouts frame-id) (fnil merge {}) temp-marker-layouts)
             (update-in (geop/temp-raw-feature-layers frame-id) (fnil merge {}) temp-feature-layers)
             (assoc-in (geop/frame-di frame-id) (:di payload)))
     :fx [[:dispatch [::tasks/execute-wrapper
                      frame-id
                      action
                      payload
                      false]]]}))

(defn no-event [_ _ callback-vec _ _]
  {:dispatch callback-vec})

(def event-vector-funcs
  "[event-name event-version]"
  {["create-frame" 1] create-frame-event
   ["move-frame" 1] no-event
   ["resize-stop" 1] no-event
   ["maximize" 1] no-event
   ["minimize" 1] no-event
   ["normalize" 1] no-event
   ["close-frame" 1] close-event
   ["z-index" 1] no-event
   ["recreate-frame" 1] no-event
   ["override-frame" 1] no-event
   ["create-di" 1] no-event
   ["add-to-details-view" 1] add-to-details-view-event
   ["remove-from-details-view" 1] remove-from-details-view
   ["remove-event-from-details-view" 1] remove-event-from-details-view
   ["operation" 1] operation-event})

(defn operation-event-sync [db frame-id callback-vec {:keys [action]
                                                      {{:keys [center zoom]} :view-position
                                                       :as payload} :payload}]
  (let [[temp-marker-layouts temp-feature-layers payload] (prepare-operation db payload)]
    (cond
      (= action :position-change) (map-api/move-to frame-id zoom center)
      (= action :popup) (let [{{{:keys [center zoom]} :view-position} :popup-desc} payload]
                          (map-api/move-to frame-id zoom center)))
    (when (not= action :filter)
      {:db (-> db
               (save-callbackvec-in-appstate frame-id callback-vec)
               (update-in (geop/temp-raw-marker-layouts frame-id) (fnil merge {}) temp-marker-layouts)
               (update-in (geop/temp-raw-feature-layers frame-id) (fnil merge {}) temp-feature-layers)
               (assoc-in (geop/frame-di frame-id) (:di payload)))
       :fx [[:dispatch [::tasks/execute-wrapper
                        frame-id
                        action
                        payload
                        false]]]})))

(defn- sync-frame-event [db frame-id _]
  (when (and (is-map? frame-id)
             (not (map-api/instances-exist? frame-id)))
    (create-frame-event db frame-id nil nil nil nil)))

(def sync-event-vector-funcs
  (merge event-vector-funcs
         {["operation" 1] operation-event-sync}))

(defn event-func
  ([event-name]
   (event-func event-name 1))
  ([event-name event-version]
   (get event-vector-funcs [event-name event-version])))

(defn- sync-event-func
  ([event-name]
   (sync-event-func event-name 1))
  ([event-name event-version]
   (get sync-event-vector-funcs [event-name event-version])))

(re-frame/reg-event-fx
 ::frame-broadcast-receiver
 (fn [{db :db} [_ frame-id frame-desc]]
   (sync-frame-event db frame-id frame-desc)))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [cur-lang (i18n/current-language db)
         parsed-event-params (edn/read-string event-params)
         event-func (sync-event-func event-name event-version)]
     (debug "sync-event map" {:frame-id frame-id
                              :event-name event-name
                              :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db frame-id nil parsed-event-params #{} cur-lang)
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(re-frame/reg-event-fx
 ::replay-event
 [(re-frame/inject-cofx ::inject/sub (with-meta [:de.explorama.frontend.common.i18n/current-language] {:ignore-dispose true}))]
 (fn [{db :db
       cur-lang :de.explorama.frontend.common.i18n/current-language} [_ [_ frame-id event-name event-params event-version] ignored-frames callback-vec]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (event-func event-name event-version)]
     (debug "replay-event map" {:frame-id frame-id
                                :event-name event-name
                                :parsed-event-params parsed-event-params
                                :callback-vec callback-vec})
     (if event-func
       (event-func db frame-id callback-vec parsed-event-params ignored-frames cur-lang)
       (do
         (debug "no event-function found for " [event-name event-version] ", dispatching callback-vec.")
         {:dispatch callback-vec})))))

(re-frame/reg-event-fx
 ::next-replay-event
 (fn [{db :db} [_ current-event rest-events events-done max-events done-event ignored-frames plogs-id profiling-state]]
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
     {:db (assoc-in db geop/replay-progress (/ events-done
                                               max-events))
      :dispatch (if current-event
                  [::replay-event current-event
                   ignored-frames
                   [::next-replay-event
                    (first rest-events)
                    (rest rest-events)
                    (inc events-done)
                    max-events
                    done-event
                    ignored-frames
                    plogs-id
                    (when profiling-state
                      profiling-state)]]
                  (cond-> done-event
                    profiling-state
                    (conj profiling-state)))})))

(def pre-process-events (partial log-util/pre-process-events
                                 is-map?
                                 (constantly false)
                                 (constantly false)
                                 #{"add-to-details-view" "remove-event-from-details-view"}))

(defn- filter-settings [all-layers used-settings]
  (let [all-layer-ids (set (map :id all-layers))]
    (filterv
     (fn [v]
       (not (all-layer-ids v)))
     used-settings)))

(re-frame/reg-event-fx
 ::wait-for-settings
 (fn [{db :db} [_ replay-next used-settings wait-tries]]
   (let [needed-settings (filter-settings (get-in db geop/layers-path)
                                          used-settings)]
     (debug "map WAITING FOR CONFIGS" {:used-settings used-settings
                                          :needed-settings needed-settings
                                          :in-db (get-in db geop/layers-path)
                                          :wait-tries wait-tries
                                          :need-to-wait? (and
                                                          (not= wait-tries config/max-config-wait-tries)
                                                          (not (empty? needed-settings)))})
     (if (or (empty? needed-settings)
             (= wait-tries config/max-config-wait-tries))
       {:dispatch replay-next}
       {:dispatch-later {:ms 100
                         :dispatch [::wait-for-settings replay-next used-settings (inc wait-tries)]}}))))

(re-frame/reg-event-fx
 ::replay-events
 (fn [{db :db} [_ events done-event plogs-id test-and-profile?]]
   (let [{user :username} (fi/call-api :user-info-db-get db)
         {events :result
          :keys [ignored-frames used-settings]} (pre-process-events events)
         replay-next [::next-replay-event
                      (first events)
                      (rest events)
                      0
                      (count events)
                      done-event
                      ignored-frames
                      plogs-id
                      (when test-and-profile? test-and-profile?)]
         needed-settings (filter-settings (get-in db geop/layers-path)
                                          used-settings)]
     (debug "map REPLAY-EVENTS" events)
     {:dispatch-n
      [(when (empty? needed-settings)
         replay-next)
       (when (seq needed-settings)
         [::wait-for-settings replay-next used-settings 0])]})))
