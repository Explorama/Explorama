(ns de.explorama.frontend.table.core
  (:require [data-format-lib.data-instance :as dfl-di]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.table.acs]
            [de.explorama.frontend.table.config :as vconfig]
            [de.explorama.frontend.table.event-logging :as event-log]
            [de.explorama.frontend.table.event-replay :as event-replay]
            [de.explorama.frontend.table.interaction.selection]
            [de.explorama.frontend.table.operations.filter]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.plugin-impl :as plugi]
            [de.explorama.frontend.table.table.core :as table]
            [de.explorama.frontend.table.table.data :as table-data]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [debug error info]]))

(def ^:private max-check-tries 100)

(defn register-init [current-tries]
  (cond
    (fi/api-definitions) (fi/call-api :init-register-event-dispatch ::init-event "table")
    (< current-tries max-check-tries) (js/setTimeout #(register-init (inc current-tries)) 100)
    :else (error "Max number of tries reached to check for frontend-interface api.")))

(re-frame/reg-event-fx
 ::arrive
 (fn [_ _]
   (let [{info :info-event-vec
          service-register :service-register-event-vec
          tools-register :tools-register-event-vec
          init-done :init-done-event-vec} (fi/api-definitions)]
     {:fx [[:dispatch (tools-register {:id vconfig/tool-name-table
                                       :icon "table"
                                       :component :table
                                       :action [::table-open]
                                       :tooltip-text [::i18n/translate :vertical-label-table]
                                       :enabled-sub (fi/call-api [:interaction-mode :normal-sub-vec?])
                                       :vertical vconfig/default-vertical-str
                                       :tool-group :bar
                                       :bar-group :middle
                                       :sort-order 2})]
           [:dispatch (service-register :visual-option
                                        :table
                                        {:icon :table
                                         :sort-class "tool__table"
                                         :event ::table-open
                                         :tooltip [::i18n/translate :vertical-label]
                                         :tooltip-search [::i18n/translate :table-tooltip-search]})]
           [:dispatch (init-done "table")]
           [:dispatch (info "table arriving!")]]})))

(re-frame/reg-event-fx
 ::init-client
 (fn [_ [_ _user-info]]
   {}))

(re-frame/reg-event-fx
 ::init-event
 (fn [{db :db} _]
   (let [{:keys [user-info-db-get frame-info-api-register-event-vec frame-instance-api-register-event-vec]
          service-register :service-register-event-vec
          papi-register :papi-register-event-vec} (fi/api-definitions)
         user-info (user-info-db-get db)]
     {:dispatch-n [[::arrive]
                   [::init-client user-info]
                   (service-register :logout-events :css-logout [::logout])
                   (service-register :config :vis-pap {:url vconfig/vis-origin
                                                       :vertical :table
                                                       :type :abac
                                                       :get-url-postfix "/pap-all"
                                                       :save-url-postfix "/pap-save"
                                                       :get-capabilities-url "/pap-capabilites"})
                   (service-register :event-replay "table" {:event-replay ::event-replay/replay-events
                                                            :replay-progress path/replay-progress})
                   (service-register :event-sync "table" ::event-replay/sync-event)
                   (service-register :event-protocol "table" event-log/events->steps)
                   (service-register :focus-event
                                     :table
                                     [::table/focus-event])
                   (service-register :modules vconfig/tool-name-table table/frame-body)
                   (frame-info-api-register-event-vec "table" {:local-filter #(get-in %1 (path/applied-filter %2))
                                                               :datasources (fn [db frame-id]
                                                                              (when-let [dim-info (get-in db (path/dim-info frame-id))]
                                                                                (:datasources dim-info)))
                                                               :layouts (fn [_] nil)
                                                               :di #(get-in %1 (path/table-datasource %2))
                                                               :selections #(get-in %1 (path/current-selection %2))
                                                               :undo-event (fn [_] nil)
                                                               :custom {:table #(get-in %1 (path/table-infos %2))}})
                   (frame-instance-api-register-event-vec "table" {})
                   (papi-register "table" plugi/table-desc)
                   (service-register
                    :clean-workspace
                    ::clean-workspace
                    [::clean-workspace])
                   [::event-log/log-pseudo-init]]})))

(re-frame/reg-event-fx
 ::clean-up
 (fn [_ [_ follow-event frame-ids]]
   {:dispatch-n (if (coll? frame-ids)
                  (mapv (fn [frame-id]
                          [::table/close-action frame-id (conj follow-event frame-id)])
                        frame-ids)
                  [[::clean-workspace follow-event]])}))

(defn clear-path [db path frames]
  (if (seq frames)
    (apply update-in db path dissoc frames)
    db))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} [_ follow-event reason]]
   (let [frame-ids (fi/call-api :list-frames-vertical-db-get db "table")
         cleaned (-> db
                     (dissoc :table)
                     (dissoc :table.loading/loading-view)
                     (clear-path [path/root-key] frame-ids)
                     (update path/root-key
                             dissoc
                             :replay-callback
                             path/replay-progress-key))]
     (doseq [frame-id frame-ids]
       (table/remove-frame-data frame-id))
     {:db cleaned
      :fx [[:dispatch-n (mapv #(fi/call-api :frame-delete-quietly-event-vec %) frame-ids)]
           (when (not= reason :logout)
             [:dispatch [::event-log/log-pseudo-init]])
           [:dispatch (conj follow-event ::clean-workspace)]]})))

(re-frame/reg-event-fx
 ::logout
 (fn [_ _]
   {}))

(defn create-frame
  [frame-id coords size]
  {:id frame-id
   :module vconfig/tool-name-table
   :coords-in-pixel coords
   :size-in-pixel size
   :size-min [600 275]
   :event ::table/table-view-event
   :data-consumer true
   :vertical vconfig/default-vertical-str
   :type :frame/content-type
   :resizable true})

(defn create-frame-table
  ([frame-id coords size]
   (create-frame frame-id coords size))
  ([coords size]
   (create-frame nil coords size)))

(re-frame/reg-event-fx
 ::table-open
 (fn [_ [_ source-frame-id create-position ignore-scroll-position? opts]]
   {:dispatch (fi/call-api :frame-create-event-vec
                           (assoc (create-frame-table (or create-position
                                                          [100 200])
                                                      [600 550])
                                  :ignore-scroll-position? ignore-scroll-position?
                                  :opts (merge {:publishing-frame-id source-frame-id}
                                               opts)))}))

(re-frame/reg-event-fx
 ::duplicate-table-frame
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ source-frame-id]]
   (let [source-infos (fi/call-api :frame-db-get db source-frame-id)
         copy-active? (fi/call-api [:product-tour :component-active?-db-get]
                                   db :table :copy)]
     (when copy-active?
       (let [selections (fi/call-api :selections-db-get db source-frame-id)
             {pixel-coords :coords
              pixel-size :size
              source-title :title
              {original-pixel-coords :coords
               original-pixel-size :size} :before-minmaximized} source-infos
             pos (or original-pixel-coords pixel-coords)
             size (or original-pixel-size pixel-size)
             local-filter (get-in db (path/applied-filter source-frame-id))
             frame-desc (assoc (create-frame-table pos size)
                               :ignore-scroll-position? true
                               :opts {:publishing-frame-id source-frame-id
                                      :overwrites {:info {:custom {:table {:type :duplicate
                                                                           :local-filter local-filter
                                                                           :source-frame source-frame-id
                                                                           :selections selections
                                                                           :source-title source-title
                                                                           :di (get-in db (path/table-datasource source-frame-id))
                                                                           :table-infos (table-data/copy-params source-frame-id)}}}
                                                   :attributes {:custom-title source-title
                                                                :last-applied-filters local-filter}}})]
         {:dispatch-n [(fi/call-api :frame-create-event-vec frame-desc)]})))))

(re-frame/reg-event-fx
 ::abort
 (fn [_ [_ msg]]
   {:dispatch (fi/call-api :error-event-vec msg)}))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id operation-type source-or-target params event]]
   (debug ::provide-content frame-id operation-type source-or-target params event)
   (let [local-filter (fi/call-api :frame-filter-db-get db frame-id)
         local-filter-id (dfl-di/ctn->sha256-id local-filter)
         di (get-in db (path/table-datasource frame-id))
         new-di (-> di
                    (update :di/operations (fn [ops]
                                             [:filter local-filter-id ops]))
                    (assoc-in [:di/filter local-filter-id] local-filter))]
     (cond
       (#{:difference
          :intersection-by
          :union
          :sym-difference} operation-type)
       (if (seq local-filter)
         {:dispatch (conj event {:di new-di :cancel? false})}
         {:dispatch (conj event {:di di :cancel? false})})
       (#{:couple} operation-type)
       {:dispatch (conj event {:cancel? true})}
       (and (#{:override} operation-type)
            (= :target source-or-target))
       {:dispatch (conj event {:cancel? false})}
       (and (#{:override} operation-type)
            (= :source source-or-target))
       {:dispatch (conj event {:cancel? false})}
       :else
       {:dispatch (conj event {:cancel? true})}))))

;; (when vconfig/vis-origin
;;   (pap/register-feature vconfig/vis-origin (str ::load-table) "table" nil))

(defn init []
  (register-init 0))
