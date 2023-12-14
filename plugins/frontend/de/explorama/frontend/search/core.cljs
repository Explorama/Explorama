(ns de.explorama.frontend.search.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.api.core]
            [de.explorama.frontend.search.backend.core]
            [de.explorama.frontend.search.backend.di :as di-backend]
            [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.search.data.acs]
            [de.explorama.frontend.search.direct-search]
            [de.explorama.frontend.search.event-logging :as event-log]
            [de.explorama.frontend.search.komplexe-suche]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.timeout]
            [de.explorama.frontend.search.util :as sutils]
            [de.explorama.frontend.search.views.komplexe-suche-view :as komplexesuche]
            [de.explorama.frontend.search.views.plugin-impl :as plugin-impl]
            [de.explorama.shared.common.data.attributes :as attr]
            [de.explorama.shared.search.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error debug info warn]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event config/default-vertical-str)
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::register-retrieve
 (fn [{db :db} [_ context user config-key event]]
   (if-let [config (fi/call-api [:config :get-config-db-get]
                                db
                                config-key)]
     {:dispatch (conj event config)}
     {:dispatch-later {:ms 300
                       :dispatch [::register-retrieve context user config-key event]}})))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          frame-info-api-register-event-vec :frame-info-api-register-event-vec
          frame-instance-api-register-event-vec :frame-instance-api-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id config/tool-name
                                       :component :search
                                       :icon "search"
                                       :action [::search-open]
                                       :tooltip-text [::i18n/translate :search-label]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :vertical config/default-vertical-str
                                       :tool-group :bar
                                       :bar-group :top})]
           [:dispatch (frame-info-api-register-event-vec "search" {:local-filter (fn [_ _] nil)
                                                                   :layouts (fn [_ _] [])
                                                                   :di #(get-in %1 (spath/data-instance %2))
                                                                   :datasources (fn [db frame-id]
                                                                                  (when-let [ds-row (get-in db (spath/search-row-data frame-id [attr/datasource-attr attr/datasource-node]))]
                                                                                    (:values ds-row)))
                                                                   :selections (fn [_ _] nil)
                                                                   :undo-event (fn [_ frame-id]
                                                                                 [:de.explorama.frontend.search.api.core/undo-to-last-search frame-id])
                                                                   :custom {:search (fn [_ _] nil)}})]
           [:dispatch (frame-instance-api-register-event-vec "search" {})]
           [:dispatch (service-register :operations
                                        "search-related-to"
                                        {:label [::i18n/translate :related-to]
                                         :disabled? (fn [frame-id]
                                                      (boolean (:is-maximized? @(fi/call-api :frame-sub frame-id))))
                                         :visible? (fn [frame-id]
                                                     (not @(fi/call-api [:interaction-mode :read-only-sub?]
                                                                        {:frame-id frame-id})))
                                         :icon :related-by
                                         :submenu {:type :event-attributes
                                                   :event [:de.explorama.frontend.search.api.core/related-search]
                                                   :blacklist [:location :id :annotation :notes :fulltext :month :bucket]}})]
           [:dispatch (init-done config/default-vertical-str)]
           [:dispatch [::event-log/log-pseudo-init]]
           [:dispatch (info (str config/default-vertical-str " arriving!"))]]})))

(re-frame/reg-event-fx
 ::init-event
 (fn [_ _]
   (let [{service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)]
     {:dispatch-n [[::arrive]
                   [ws-api/init-client]
                   [ws-api/search-query-list]

                   (service-register :update-user-info-event-vec
                                     [ws-api/update-user-info])
                   (service-register :event-replay config/default-vertical-str {:event-replay :de.explorama.frontend.search.event-logging/replay-events
                                                                                :replay-progress spath/replay-progress})
                   (service-register :event-sync config/default-vertical-str :de.explorama.frontend.search.event-logging/sync-event)
                   (service-register :event-protocol config/default-vertical-str event-log/events->steps)
                   (service-register :modules config/tool-name komplexesuche/view)
                   (service-register :clean-workspace
                                     ::clean-workspace
                                     [::clean-workspace])
                   (service-register :event-vec
                                     :search-open-with-rows
                                     [:de.explorama.frontend.search.api.core/open-with-rows])
                   (service-register :logout-events :search-logout [::logout])
                   (papi-register config/default-vertical-str plugin-impl/desc)]})))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (let [frames (fi/call-api :list-frames-vertical-db-get db config/default-vertical-str)]
     {:db         (cond-> db
                    :always (sutils/clean-frames frames)
                    :always (update spath/root-key
                                    dissoc
                                    spath/replay-progress-key)
                    (= reason :logout) (update-in spath/search-query dissoc :queries)
                    (= reason :logout) (update spath/root-key dissoc spath/enabled-datasource-key))
      :dispatch-n (conj (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frames)
                        (when (not= reason :logout)
                          [::event-log/log-pseudo-init])
                        (conj follow-event ::clean-workspace))})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {:fx [[:dispatch [::close-tube]]]}))

(defn create-frame
  ([coords size]
   (create-frame nil coords size))
  ([frame-id coords size]
   {:id             frame-id
    :coords-in-pixel coords
    :size-in-pixel   size
    :size-min       [600 320]
    :event          ::search-view-event
    :module         config/tool-name
    :vertical       config/default-vertical-str
    :type           :frame/content-type
    :legacy? false
    :resizable      true
    :optional-class "explorama__window--search"}))

(re-frame/reg-sub
 ::project-is-loading
 (fn [db [_ frame-id]]
   (get-in db [:search :replay frame-id])))

(re-frame/reg-event-fx
 ::search-open
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-open-event opts]]
   (debug "Event to dispatch after frame-opened" frame-open-event)
   {:db         (assoc-in db [:search :frame-open-event] frame-open-event)
    :dispatch-n [(fi/call-api :frame-create-event-vec (assoc (create-frame [100 200] [600 550])
                                                             :opts opts))
                 (when-not (get-in db spath/attribute-types)
                   [ws-api/init-client])]}))

(re-frame/reg-event-fx
 ::duplicate-search-frame
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ source-frame-id]]
   (let [source-infos (fi/call-api :frame-db-get db source-frame-id)
         copy-active? (fi/call-api [:product-tour :component-active?-db-get]
                                   db :search :copy)]
     (when copy-active?
       (let [source-formdata-path (spath/frame-search-rows source-frame-id)
             di-create? (get-in db (spath/di-creation-success source-frame-id))
             {pixel-coords :coords
              pixel-size :size
              source-title :frame/title
              {original-pixel-coords :coords
               original-pixel-size :size} :before-minmaximized} source-infos
             pos (or original-pixel-coords pixel-coords)
             size (or original-pixel-size pixel-size)
             frame-desc (assoc (create-frame pos size)
                               :ignore-scroll-position? true
                               :opts {:publishing-frame-id source-frame-id
                                      :source-path source-formdata-path
                                      :di-create? di-create?
                                      :source-title source-title})]
         {:dispatch-n [(fi/call-api :frame-create-event-vec frame-desc)]})))))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id woco-type source-or-target params event]]
   (cond (and (#{:difference
                 :intersection-by
                 :union
                 :sym-difference} woco-type)
              (= source-or-target :source))
         {:dispatch (conj event {:cancel? false})}
         (and (#{:override} woco-type)
              (= source-or-target :source))
         {:dispatch (conj event {:cancel? false})}
         :else
         {:dispatch (conj event {:cancel? true})})))

(re-frame/reg-event-fx
 ::search-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event opts query]} params
         datasources (get-in db spath/search-enabled-datasources)]
     (debug "search-view-event" {:action action
                                 :params params})
     (case action
       :frame/query (case query
                      :local-filter {:dispatch callback-event}
                      :temp-layouts {:dispatch (conj callback-event {})}
                      :data-desc {:dispatch (conj callback-event
                                                  {:di (get-in db (spath/data-instance frame-id))})}
                      (error "unknown query" query))
       :frame/init (let [{:keys [di-create? source-path source-title]} opts
                         source-formdata (get-in db source-path)
                         db (cond-> db
                              source-path (assoc-in (spath/search-frame-changed? frame-id) (not di-create?))
                              source-path (assoc-in (spath/frame-search-rows frame-id) source-formdata)
                              di-create? (assoc-in (spath/frame-direct-vis-opened? frame-id) true))]
                     {:db db
                      :dispatch-n [(if source-path
                                     [::options-backend/request-options frame-id nil true]
                                     [:de.explorama.frontend.search.views.attribute-bar/init-request-attributes frame-id])
                                   (when di-create?
                                     [::di-backend/submit-search-form frame-id])
                                   (when source-path
                                     [ws-api/search-options datasources frame-id (vec (keys source-formdata)) source-formdata])]})
       :frame/connect-to {}
       :frame/recreate {}
       :frame/close {:db       (sutils/clean-frames db [frame-id])
                     :dispatch callback-event}
       :frame/connection-negotiation
       (let [{:keys [type frame-id connected-frame-id result]} params
             options
             (cond (and (get-in db (spath/data-instance frame-id))
                        (= type :target)
                        (not= "search" (:vertical connected-frame-id)))
                   {:type :connect
                    :frame-id frame-id
                    :event [::provide-content frame-id]}
                   (and (get-in db (spath/data-instance frame-id))
                        (= type :source)
                        (not= "search" (:vertical connected-frame-id)))
                   {:type :options
                    :frame-id frame-id
                    :event [::provide-content frame-id]
                    :options [{:label "Intersection-by"
                               :type :intersection-by
                               :children
                               [{:label "Identifier"
                                 :params {:by "id"}}]}
                              {:label "Union"
                               :type :union}
                              {:label "Difference"
                               :type :difference}
                              {:label "Symmetric Difference"
                               :type :sym-difference}
                              {:label "Override"
                               :type :override}]}
                   :else
                   {:type :cancel})]
         {:dispatch (conj result options)})
       {}))))

;; Register the default policies.
;; (when config/suche-origin)
  ;; (pap/register-feature config/suche-origin (str ::load-search) "search-name" nil)
  ;; (pap/register-feature config/suche-origin (str :de.explorama.frontend.search.direct-search/unified) "direct-search-unified" nil))

(defn init []
  (register-init 0))