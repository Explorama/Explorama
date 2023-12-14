(ns de.explorama.frontend.mosaic.event-logging
  (:require [ajax.core :as ajax]
            [clojure.string :as str]
            [de.explorama.frontend.common.event-logging.util :as log-util :refer [not-creating-and-target?
                                                                                  base-desc
                                                                                  access-attrs]]
            [de.explorama.frontend.mosaic.config :as config]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.event-replay :as e-replay]))

(re-frame/reg-event-fx
 ::event-log-success
 (fn [_ _]
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
             e-replay/event-version))))

(re-frame/reg-event-fx
 ::log-pseudo-init
 (fn [{db :db} _]
   (let [workspace-id (fi/call-api :workspace-id-db-get db)
         pseudo-frame-id {:workspace-id workspace-id
                          :vertical "mosaic"}]
     {:dispatch [::log-event pseudo-frame-id "init-mosaic" nil]})))

(re-frame/reg-event-fx
 ::execute-on-exit-callback-vec
 (fn [{db :db} [_ frame-id]]
   (if-let [event-callback-vec (get-in db (gp/on-exit-callback frame-id))]
     (do
       (debug "CALLBACK ON EXIT - " frame-id " - " event-callback-vec)
       {:db (update-in db gp/on-exit-callback-root dissoc frame-id)
        :dispatch event-callback-vec})
     {})))


(re-frame/reg-event-fx
 ::ui-wrapper
 [(.-uiInterceptor js/window)]
 (fn [{db :db} [_ frame-id event-name event-params]]
   (if-let [event-func (e-replay/event-func event-name)]
     (merge-with into
                 (event-func db frame-id nil event-params)
                 {:dispatch-n [[:de.explorama.frontend.mosaic.event-logging/log-event frame-id event-name event-params]]})
     (do
       (debug "no event-function found for " [event-name e-replay/event-version])
       nil))))

(defn- action-desc [base-op action both _ only-new]
  (cond (= "operation" action)
        (cond (not-creating-and-target? :remove base-op both only-new)
              (base-desc :mosaic-protocol-action-remove-group)

              (and (= base-op :create)
                   (= :init (:action only-new))
                   (= :duplicate (access-attrs both only-new [:payload :source-action])))
              (base-desc :mosaic-protocol-action-copy)

              (and (= base-op :create)
                   (= :init (:action only-new))
                   (= :copy-card (access-attrs both only-new [:payload :source-action])))
              (base-desc :mosaic-protocol-action-copy-card)

              (and (= base-op :create)
                   (= :init (:action only-new))
                   (= :copy-group (access-attrs both only-new [:payload :source-action])))
              (base-desc :mosaic-protocol-action-copy-group)

              (and (= :init (:action only-new))
                   (= :recreate (access-attrs both only-new [:payload :source-action])))
              (base-desc :mosaic-protocol-action-set-op)

              (and (= :init (:action only-new))
                   (= :override (access-attrs both only-new [:payload :source-action])))
              (base-desc :mosaic-protocol-action-overwrite)

              (or (= :init (:action only-new))
                  (= :init (:action both)))
              (base-desc :mosaic-protocol-action-load-data)

              (not-creating-and-target? :layout base-op both only-new)
              (base-desc :mosaic-protocol-action-change-layout
                         [(str/join ", "
                                    (-> (->> (into (get-in both [:payload :layouts] [])
                                                   (get-in only-new [:payload :layouts] []))
                                             (map :name)
                                             set)
                                        (disj nil)))])

              (not-creating-and-target? :group-by base-op both only-new)
              (base-desc :mosaic-protocol-action-group-by
                         (access-attrs both only-new [:payload :operations-desc gcp/grp-by-key]))

              (not-creating-and-target? :ungroup base-op both only-new)
              (base-desc :mosaic-protocol-action-ungroup)

              (not-creating-and-target? :sub-group-by base-op both only-new)
              (base-desc :mosaic-protocol-action-sub-group-by
                         (access-attrs both only-new [:payload :operations-desc gcp/sub-grp-by-key]))

              (not-creating-and-target? :unsub-group base-op both only-new)
              (base-desc :mosaic-protocol-action-ungroup-sub-group)

              (not-creating-and-target? :sort-by base-op both only-new)
              (base-desc :mosaic-protocol-action-sort-by
                         [[:label (access-attrs both only-new [:payload :operations-desc gcp/sort-key :by] "date")]
                          :mosaic-protocol-action-with-direction
                          (case (access-attrs both only-new [:payload :operations-desc gcp/sort-key :direction] :asc)
                            :asc :mosaic-protocol-action-with-direction-asc
                            :desc :mosaic-protocol-action-with-direction-desc
                            "")])

              (not-creating-and-target? :sort-group-by base-op both only-new)
              (base-desc :mosaic-protocol-action-sort-group
                         (into (condp = (access-attrs both only-new [:payload :operations-desc gcp/sort-grp-key :by])
                                 :event-count
                                 [:mosaic-protocol-action-agg-event-count]
                                 :name
                                 [:mosaic-protocol-action-agg-name]
                                 "layout"
                                 [:mosaic-protocol-action-agg-layout]
                                 :aggregate
                                 [[:prefix "mosaic-protocol-agg-aggregate-" (access-attrs both only-new [:payload :operations-desc gcp/sort-sub-grp-key :method])]
                                  :mosaic-protocol-action-agg-attr
                                  [:label (access-attrs both only-new [:payload :operations-desc gcp/sort-grp-key :attr])]])
                               [:mosaic-protocol-action-with-direction
                                (case (access-attrs both only-new [:payload :operations-desc gcp/sort-grp-key :direction] :asc)
                                  :asc :mosaic-protocol-action-with-direction-asc
                                  :desc :mosaic-protocol-action-with-direction-desc
                                  "")]))

              (not-creating-and-target? :sort-sub-group-by base-op both only-new)
              (base-desc :mosaic-protocol-action-sub-sort-group
                         (into (condp = (access-attrs both only-new [:payload :operations-desc gcp/sort-sub-grp-key :by])
                                 :event-count
                                 [:mosaic-protocol-action-agg-event-count]
                                 :name
                                 [:mosaic-protocol-action-agg-name]
                                 "layout"
                                 [:mosaic-protocol-action-agg-layout]
                                 :aggregate
                                 [[:prefix "mosaic-protocol-agg-aggregate-" (access-attrs both only-new [:payload :operations-desc gcp/sort-sub-grp-key :method])]
                                  :mosaic-protocol-action-agg-attr
                                  [:label (access-attrs both only-new [:payload :operations-desc gcp/sort-sub-grp-key :attr])]])
                               [:mosaic-protocol-action-with-direction
                                (case (access-attrs both only-new [:payload :operations-desc gcp/sort-sub-grp-key :direction] :asc)
                                  :asc :mosaic-protocol-action-with-direction-asc
                                  :desc :mosaic-protocol-action-with-direction-desc
                                  "")]))

              (not-creating-and-target? :couple base-op both only-new)
              (base-desc :mosaic-protocol-action-couple-by)

              (not-creating-and-target? :couple base-op both only-new)
              (base-desc :mosaic-protocol-action-decouple)

              (not-creating-and-target? :scatter base-op both only-new)
              (base-desc :mosaic-protocol-action-activate-scatter)

              (not-creating-and-target? :scatter-axis base-op both only-new)
              (base-desc :mosaic-protocol-action-scatter-change
                         (cond (get-in only-new [:payload :operations-desc gcp/scatter-x])
                               [:mosaic-protocol-action-scatter-change-x
                                (get-in only-new [:payload :operations-desc gcp/scatter-x])]
                               (get-in only-new [:payload :operations-desc gcp/scatter-y])
                               [:mosaic-protocol-action-scatter-change-y
                                (get-in only-new [:payload :operations-desc gcp/scatter-y])]
                               :else nil))

              (not-creating-and-target? :raster base-op both only-new)
              (base-desc :mosaic-protocol-action-set-grid)

              :else
              nil)))

(def events->steps (partial log-util/events->steps config/default-vertical-str action-desc))