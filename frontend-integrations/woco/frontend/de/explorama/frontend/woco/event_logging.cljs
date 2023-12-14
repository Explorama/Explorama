(ns de.explorama.frontend.woco.event-logging
  (:require [ajax.core :as ajax]
            [cljs.reader :as edn]
            [clojure.string :as str]
            [de.explorama.frontend.common.event-logging.util :as log-util :refer [base-desc]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug error]]
            [de.explorama.frontend.woco.api.interaction-mode :as inter-mode]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.filter.core :as filter-core]
            [de.explorama.frontend.woco.frame.size-position :refer [set-frame-position]]
            [de.explorama.frontend.woco.notes.states :as notes-states]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.frame.events :as frame-events]))

(def event-version 1.0)

(def broadcast-event-filter #{"woco-pan-zoom" "frame"})

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ [_ _]]
   {}
   #_{:dispatch [:de.explorama.frontend.woco.log/info (str "Event-logging success" resp)]}))

(re-frame/reg-event-fx
 ::event-log-failure
 (fn [_ [_ resp]]
   {:dispatch [:de.explorama.frontend.woco.log/error (str "Event-logging failed" resp)]}))

(re-frame/reg-event-fx
 ::log-event
 [inter-mode/ro-interceptor]
 (fn [{db :db} [_ frame-id event-name description]]
   (debug ::log-event frame-id event-name)
   (when-let [log-fn (registry/lookup-target db :project-fns :event-log)]
     (log-fn db
             config/default-vertical-str
             frame-id
             event-name
             description
             event-version
             (boolean (broadcast-event-filter event-name))))))

(defn close-event [db _ frame-id callback-vec _]
  {:db (path/dissoc-in db (path/frame-desc frame-id))
   :fx [(when callback-vec
          [:dispatch callback-vec])]})

(defn- set-comparison-event [db _ _ callback-vec new-compare]
  {:db (assoc-in db path/details-view-compare-events new-compare)
   :fx [(when callback-vec
          [:dispatch callback-vec])]})

(defn remove-from-comparison [_ _ _ callback-vec desc]
  {:fx [[:dispatch [:de.explorama.frontend.woco.details-view/remove-from-comparison (assoc desc :no-event-logging? true)]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn remove-all [_ _ _ callback-vec desc]
  {:fx [[:dispatch [:de.explorama.frontend.woco.details-view/remove-all-events (assoc desc :no-event-logging? true)]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn woco-pan-zoom [db _ _ callback-vec pos]
  {:db (assoc-in db path/last-logged-position pos)
   :fx [[:dispatch [:de.explorama.frontend.woco.navigation.control/set-position pos true]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn woco-presentation-mode-add [db _ uid callback-vec params]
  {:fx [[:dispatch [:de.explorama.frontend.woco.presentation.core/add-slide params true]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn woco-presentation-mode-remove [db _ uid callback-vec params]
  {:fx [[:dispatch [:de.explorama.frontend.woco.presentation.core/remove-slide-by-uid uid true]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn woco-presentation-mode-update [db _ uid callback-vec params]
  {:fx [[:dispatch [:de.explorama.frontend.woco.presentation.core/update-slide uid params true]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn woco-presentation-mode-order [db _ uid callback-vec [i j]]
  {:fx [[:dispatch [:de.explorama.frontend.woco.presentation.core/change-slide-order i j true]]
        (when callback-vec
          [:dispatch callback-vec])]})

(defn frame-event [db _ frame-id callback-vec {:keys [coords note-details z-index vertical-number] :as desc}]
  (set-frame-position frame-id coords)
  (when note-details
    (notes-states/set-text frame-id (:content note-details))
    (notes-states/set-bg-color frame-id (:color note-details)))
  (let [desc (cond-> desc
               note-details (dissoc :note-details))]
    {:db (cond-> (assoc-in db (path/frame-desc frame-id) (dissoc desc :resized-infos))
           (:last-applied-filters desc)
           (filter-core/set-external-filter frame-id (:last-applied-filters desc))
           (and (number? z-index)
                (> z-index (get-in db path/curr-max-zindex -1)))
           (assoc-in path/curr-max-zindex z-index)
           (number? vertical-number)
           (update-in (path/id-counter (:vertical frame-id))
                      max vertical-number))
     :fx [(when callback-vec
            [:dispatch callback-vec])]}))

(defn header-colors-event [db _ frame-id callback-vec params]
  (let [highest-color-group-num (try (apply max
                                            (map (fn [[_ group]]
                                                   (if (string? group)
                                                     (-> group
                                                         (str/replace #"explorama\_\_window\_\_group\-" "")
                                                         (js/parseInt))
                                                     0))
                                                 params))
                                     (catch :default e
                                       (error e "Failed to calculate highest color-group-num" {:params params})))]
    {:db (cond-> (assoc-in db path/frame-header-colors params)
           highest-color-group-num
           (assoc-in path/current-group highest-color-group-num))
     :fx [(when callback-vec
            [:dispatch callback-vec])]}))

(defn no-event [_ _ frame-id callback-vec params]
  (debug "IGNORED:" frame-id callback-vec params)
  {:dispatch-n [callback-vec]})

(def events-vektor-funcs
  "[event-name event-version]"
  {["frame" 1] frame-event
   ["close-frame" 1] close-event
   ["header-colors" 1] header-colors-event
   ["set-comparison" 1] set-comparison-event
   ["remove-from-comparison" 1] remove-from-comparison
   ["remove-all" 1] remove-all
   ["woco-pan-zoom" 1] woco-pan-zoom
   ["add-slide" 1] woco-presentation-mode-add
   ["remove-slide" 1] woco-presentation-mode-remove
   ["update-slide" 1] woco-presentation-mode-update
   ["change-slide-order" 1] woco-presentation-mode-order})

(def pre-process-events (partial log-util/pre-process-events
                                 (constantly true)
                                 (fn [origin frame-id event-name]
                                   (or (and (= origin "woco")
                                            (= (:vertical frame-id) config/default-vertical-str)
                                            (= event-name "add-slide"))
                                       (= event-name "woco-pan-zoom")
                                       (= event-name "set-comparison")
                                       (= event-name "remove-from-comparison")
                                       (= event-name "remove-all")))
                                 (fn [origin frame-id event-name]
                                   (and (= origin "woco")
                                        (= (:vertical frame-id) config/default-vertical-str)
                                        (= event-name "remove-slide")))))

(re-frame/reg-event-fx
 ::sync-event
 (fn [{db :db} [_ [frame-id event-name event-params event-version]]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (get events-vektor-funcs [event-name event-version])]
     (debug "sync-event woco" {:frame-id frame-id :event-name event-name :parsed-event-params parsed-event-params})
     (if event-func
       (event-func db nil frame-id nil parsed-event-params)
       (do
         (debug "no event-function found for " [event-name event-version])
         nil)))))

(re-frame/reg-event-fx
 ::replay-event
 (fn [{db :db} [_ [_ frame-id event-name event-params event-version] callback-vec project-id]]
   (let [parsed-event-params (edn/read-string event-params)
         event-func (get events-vektor-funcs [event-name event-version])]
     (debug "replay-event woco" event-name " - " frame-id " -> " callback-vec)
     (if event-func
       (event-func db project-id frame-id callback-vec parsed-event-params)
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
     {:db (cond-> (assoc-in db path/replay-progress (/ events-done
                                                       max-events))
            (not current-event)
            (path/dissoc-in path/ignored-frames))
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

(re-frame/reg-event-fx
 ::replay-events
 (fn [{db :db} [_ events done-event plogs-id test-and-profile?]]
   (let [{events :result
          {ignored-frames :ignored-frames} :frames}
         (pre-process-events events)]
     (debug "REPLAY EVENTS WOCO" events)
     {:db (assoc-in db path/ignored-frames ignored-frames)
      :dispatch [::next-replay-event
                 (first events)
                 (rest events)
                 0
                 (count events)
                 done-event
                 plogs-id
                 (when test-and-profile? test-and-profile?)]})))

(re-frame/reg-event-fx
 ::ui-wrapper
 (fn [_ [_ frame-id event-name event event-params]]
   (when-not event
     (error "No event defined for ui-wrapper" frame-id event-name event event-params))
   {:dispatch-n [(when event event)
                 [::log-event frame-id event-name event-params]]}))

(def ^:private frame-log-blacklist #{frame-events/management-type frame-events/consumer-type})

(re-frame/reg-event-fx
 ::log-frame-event
 (fn [{db :db} [_ frame-id]]
   (let [frame-component (:vertical frame-id)]
     (inter-mode/check-inter-mode
      db
      (get-in db (path/frame-type frame-id))
      {:frame-id frame-id
       :component :*
       :additional-info nil}
      (fn []
        (when-let [frame-desc (get-in db (path/frame-desc frame-id))]
          (let [additional-desc (when (= config/notes-vertical-str (:vertical frame-id))
                                  {:note-details {:content (notes-states/get-note-content frame-id)
                                                  :color @(notes-states/get-bg-color frame-id)}})
                sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
            (sync-event-fn [::sync-frame-event frame-id (cond-> frame-desc
                                                          :always (dissoc :selected?)
                                                          additional-desc (merge additional-desc))])
            (when-not (frame-log-blacklist (:type frame-desc))
              {:dispatch [:de.explorama.frontend.woco.event-logging/log-event
                          frame-id
                          "frame"
                          (cond-> frame-desc
                            :always (dissoc :selected?)
                            additional-desc (merge additional-desc))]}))))))))

(re-frame/reg-event-fx
 ::broadcast-frame-event
 (fn [{db :db} [_ frame-id]]
   (let [frame-component (:vertical frame-id)]
     (inter-mode/check-inter-mode
      db
      (get-in db (path/frame-type frame-id))
      {:frame-id frame-id
       :component frame-component
       :additional-info :log-frame}
      (fn []
        (when-let [frame-desc (get-in db (path/frame-desc frame-id))]
          (let [additional-desc (when (= config/notes-vertical-str (:vertical frame-id))
                                  {:note-details {:content (notes-states/get-note-content frame-id)
                                                  :color @(notes-states/get-bg-color frame-id)}})
                sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
            (sync-event-fn [::sync-frame-event frame-id (cond-> frame-desc
                                                          :always (dissoc :selected?)
                                                          additional-desc (merge additional-desc))])
            {})))))))

(re-frame/reg-event-fx
 ::broadcast-frame-close
 (fn [{db :db} [_ frame-id]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [:de.explorama.frontend.woco.frame.api/close frame-id true])
     {})))

(re-frame/reg-event-fx
 ::sync-frame-event
 (fn [{db :db} [_ frame-id frame-desc]]
   (let [frame-receivers (fi/call-api :service-category-db-get db :frame-broadcast-receivers)
         fx-map (frame-event db nil frame-id nil frame-desc)]
     (reduce (fn [acc [_ event-vec]]
               (update acc :fx conj
                       [:dispatch (conj event-vec frame-id frame-desc)]))
             fx-map
             frame-receivers))))

(defn- action-desc [base-op action both only-old only-new]
  (cond (= "frame" action)
        (cond (and (#{:select :both} base-op)
                   (:custom-title only-new))
              (base-desc :woco-protocol-action-custom-title
                         (:custom-title only-new))
              :else
              nil)
        (= "apply-constraints" action)
        (base-desc :woco-protocol-action-constraint-apply)
        :else
        nil))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))
