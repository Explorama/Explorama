(ns de.explorama.frontend.mosaic.operations.tasks
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.operations.util :as util]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.actions :as gra]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [warn]]))

(defn get-sort-desc [db frame-id]
  (get-in db
          (gp/sort-desc frame-id)
          gcp/start-desc-sort))

(defn get-sort-grp-desc [db frame-id]
  (get-in db
          (gp/sort-grp-desc frame-id)
          gcp/start-desc-sort-groups))

(defn get-sort-sub-grp-desc [db frame-id]
  (get-in db
          (gp/sort-sub-grp-desc frame-id)
          gcp/start-desc-sort-groups))

(defn- transfer-desc [frame-id task-id]
  {:client-callback [ws-api/operations-result frame-id task-id]
   :custom {:data-acs-async-callback [ws-api/data-acs-async frame-id]}})

(defn attach-operations [db frame-id operation-desc log-info]
  (-> (assoc-in db (gp/operation-desc frame-id) operation-desc)
      (assoc-in (gp/operation-desc-prev frame-id)
                (get-in db (gp/operation-desc frame-id)))
      (assoc-in (gp/operation-desc-current-logged frame-id)
                log-info)
      (assoc-in (gp/operation-desc-last-logged frame-id)
                (get-in db (gp/operation-desc-current-logged frame-id)))))

(defn attach-lang [db payload]
  (assoc payload :lang (i18n/current-language db)))

;;;;;;;;;;;;;;;;;;;;; SUBS -- Operation Desc

(re-frame/reg-sub
 ::operations
 (fn [db [_ frame-id-or-path]]
   (get-in db (gp/operation-desc frame-id-or-path))))

;;;;;;;;;;;;;;;;;;;;; UTIL -- Operation Desc

(defn sort-desc-from-operations-desc [operation-desc attr]
  (let [{grp-by-key gcp/grp-by-key
         sub-grp-by-key gcp/sub-grp-by-key
         :as ops}
        operation-desc]
    (cond (= grp-by-key attr)
          (get ops gcp/sort-grp-key)
          (= sub-grp-by-key attr)
          (get ops gcp/sort-sub-grp-key)
          :else nil)))

;;;;;;;;;;;;;;;;;;;;; TASK -- REQUESTS

(defn- init-task [db
                  frame-id
                  task-id
                  {:keys [di local-filter layouts operation-desc source-action]
                   :as params}
                  live?]
  (let [sort-desc (get-sort-desc db frame-id)
        layouts (or layouts (get-in db (gp/selected-layouts frame-id)))
        local-filter (or local-filter (get-in db (gp/applied-filter frame-id)))
        payload (cond-> (if live?
                          (cond-> {:di di
                                   :operations-desc (merge
                                                     {gcp/sort-key sort-desc
                                                      gcp/render-mode-key gcp/render-mode-key-raster}
                                                     (get-in db (gp/operation-desc frame-id))
                                                     operation-desc)
                                   :send-data-acs? true
                                   :new-di? true
                                   :validate-operations-desc? true}
                            local-filter (assoc :local-filter local-filter))
                          params)
                  :always
                  (assoc :raw-layouts (get-in db gp/raw-layouts))
                  layouts
                  (assoc :layouts layouts))
        operations-desc (:operations-desc payload)]
    {:db (cond-> (attach-operations db frame-id operations-desc
                                    {:action :init
                                     :payload (dissoc payload :raw-layouts)})
           :always
           (assoc-in (gp/data-request-pending frame-id) true)
           layouts
           (assoc-in (gp/selected-layouts frame-id) layouts)
           local-filter
           (assoc-in (gp/applied-filter frame-id) local-filter)
           :always (gp/reset-stop-views frame-id))
     :fx (cond-> []
           live?
           (conj [:dispatch [:de.explorama.frontend.mosaic.event-logging/log-event frame-id "operation" {:action :init
                                                                                                         :source-action source-action ; only for the protocol
                                                                                                         :payload (dissoc payload :raw-layouts)}]]))
     :backend-tube [ws-api/operations-route
                    (transfer-desc frame-id task-id)
                    (attach-lang db payload)]}))

(defn- by-tasks [action operations-fn db-fn db frame-id task-id params live?]
  (let [path (gp/top-level frame-id)
        payload
        (if live?
          (let [local-filter (get-in db (gp/applied-filter path))]
            (cond-> {:di (get-in db (gp/data-instance path))
                     :operations-desc (operations-fn db frame-id params)
                     :layouts (get-in db (gp/selected-layouts frame-id))}
              local-filter (assoc :local-filter local-filter)))
          params)
        operations-desc (:operations-desc payload)]
    (cond-> {:db (cond-> (attach-operations db frame-id operations-desc {:action action
                                                                         :payload payload})
                   db-fn
                   (db-fn frame-id params))
             :backend-tube [ws-api/operations-route
                            (transfer-desc frame-id task-id)
                            (attach-lang db payload)]}
      live?
      (assoc :dispatch [:de.explorama.frontend.mosaic.event-logging/log-event frame-id "operation" {:action action
                                                                                                    :payload payload}]))))

(defn- group-by-op-desc [db frame-id {:keys [by]}]
  (assoc (get-in db (gp/operation-desc frame-id))
         gcp/grp-by-key by
         gcp/sort-grp-key (if (= "layout" by)
                            (gcp/sort-grp-desc "layout" :asc nil nil)
                            gcp/start-desc-sort-groups)))

(defn- sub-group-by-op-desc [db frame-id {:keys [by]}]
  (assoc (get-in db (gp/operation-desc frame-id))
         gcp/sub-grp-by-key by
         gcp/sort-sub-grp-key (if (= "layout" by)
                                (gcp/sort-grp-desc "layout" :asc nil nil)
                                gcp/start-desc-sort-groups)))

(defn- sort-by-op-desc [db frame-id {:keys [by]}]
  (let [sort-desc (get-sort-desc db frame-id)
        sort-desc-new (assoc sort-desc
                             :by by
                             :direction gcp/start-order)]
    (assoc (get-in db (gp/operation-desc frame-id))
           gcp/sort-key (if (gcp/is-same-sort-desc? sort-desc
                                                    sort-desc-new
                                                    gcp/sort-equal-vec)
                          (gcp/change-direction sort-desc)
                          sort-desc-new))))

(defn- sort-grp-by-op-desc [db frame-id {:keys [by method attr]}]
  (let [sort-desc (get-sort-grp-desc db frame-id)
        sort-desc-new (assoc sort-desc
                             :by by
                             :method method
                             :attr attr
                             :direction gcp/start-order)]
    (assoc (get-in db (gp/operation-desc frame-id))
           gcp/sort-grp-key (if (gcp/is-same-sort-desc? sort-desc
                                                        sort-desc-new
                                                        gcp/sort-grp-equal-vec)
                              (gcp/change-direction sort-desc)
                              sort-desc-new))))

(defn- sort-sub-grp-by-op-desc [db frame-id {:keys [by method attr]}]
  (let [sort-desc (get-sort-sub-grp-desc db frame-id)
        sort-desc-new (assoc sort-desc
                             :by by
                             :method method
                             :attr attr
                             :direction gcp/start-order)]
    (assoc (get-in db (gp/operation-desc frame-id))
           gcp/sort-sub-grp-key (if (gcp/is-same-sort-desc? sort-desc
                                                            sort-desc-new
                                                            gcp/sort-grp-equal-vec)
                                  (gcp/change-direction sort-desc)
                                  sort-desc-new))))

(defn- ungroup-op-desc [db frame-id _]
  (dissoc (get-in db (gp/operation-desc frame-id))
          gcp/grp-by-key
          gcp/sub-grp-by-key))

(defn- unsub-group-op-desc [db frame-id _]
  (dissoc (get-in db (gp/operation-desc frame-id))
          gcp/sub-grp-by-key))

(defn- couple-op-desc [db frame-id {:keys [by groups] :as desc}]
  (let [sort-desc {:by :name
                   :direction :asc}]
    (assoc (dissoc (get-in db (gp/operation-desc frame-id))
                   gcp/sub-grp-by-key
                   gcp/sort-sub-grp-key)
           gcp/coupled-key (dissoc desc :groups)
           gcp/grp-by-key by
           gcp/sort-grp-key sort-desc
           gcp/couple-key groups)))

(defn- decouple-op-desc [db frame-id _]
  (dissoc (get-in db (gp/operation-desc frame-id))
          gcp/coupled-key
          gcp/grp-by-key
          gcp/sort-grp-key
          gcp/couple-key
          gcp/sub-grp-by-key
          gcp/sort-sub-grp-key))

(defn- tree-algo-op-desc [db frame-id {:keys [algo]}]
  (assoc (get-in db (gp/operation-desc frame-id))
         gcp/treemap-algorithm algo))

(defn- tree-op-desc [db frame-id _]
  (cond-> (assoc (get-in db (gp/operation-desc frame-id))
                 gcp/render-mode-key gcp/render-mode-key-treemap
                 gcp/treemap-algorithm "squared")
    (not (get-in db (conj (gp/operation-desc frame-id) gcp/grp-by-key)))
    (assoc gcp/grp-by-key "country")
    (and (not (get-in db (conj (gp/operation-desc frame-id) gcp/grp-by-key)))
         (not (get-in db (conj (gp/operation-desc frame-id) gcp/sub-grp-by-key))))
    (assoc gcp/sub-grp-by-key "year")
    :always
    (dissoc gcp/sort-sub-grp-key
            gcp/sort-grp-key
            gcp/scatter-x
            gcp/scatter-y)))

(defn- scatter-op-desc [db frame-id _]
  (let [{:keys [width height]} (get-in db (gp/canvas frame-id))]
    (-> (assoc (get-in db (gp/operation-desc frame-id))
               gcp/render-mode-key gcp/render-mode-key-scatter
               gcp/scatter-client-dims {:width width
                                        :height height
                                        :card-width 520
                                        :card-height 552
                                        :card-margin 30})
        (dissoc gcp/grp-by-key
                gcp/sub-grp-by-key
                gcp/sort-sub-grp-key
                gcp/sort-grp-key
                gcp/scatter-x
                gcp/scatter-y))))

(defn- scatter-axis-op-desc [db frame-id {:keys [path axis]}]
  (assoc (get-in db (gp/operation-desc frame-id))
         path axis))

(defn- raster-op-desc [db frame-id _]
  (assoc (get-in db (gp/operation-desc frame-id))
         gcp/render-mode-key gcp/render-mode-key-raster))

(defn- remove-group [db frame-id task-id {:keys [group-type grp-attr-val] :as params} live?]
  (let [path (gp/top-level frame-id)
        payload (if live?
                  (let [di (get-in db (gp/data-instance path))
                        local-filter (get-in db (gp/applied-filter path))
                        operation-desc (get-in db (gp/operation-desc frame-id))
                        grouped-by (get operation-desc group-type)
                        layouts (get-in db (gp/selected-layouts frame-id))
                        new-filter (util/build-filter-entry
                                    grp-attr-val
                                    grouped-by
                                    layouts
                                    :not=)]
                    (cond-> {:di (util/add-filter-to-di di new-filter)
                             :operations-desc operation-desc
                             :layouts layouts}
                      local-filter (assoc :local-filter local-filter)))
                  params)
        new-di (:di payload)
        operations-desc (:operations-desc payload)]
    (cond-> {:db (-> (attach-operations db frame-id operations-desc {:action :remove
                                                                     :payload (dissoc payload :raw-layouts)})
                     (assoc-in (gp/data-instance frame-id) new-di))
             :backend-tube [ws-api/operations-route
                            (transfer-desc frame-id task-id)
                            (attach-lang db payload)]}
      live?
      (assoc :fx [[:dispatch [:de.explorama.frontend.mosaic.event-logging/log-event frame-id "operation" {:action :remove
                                                                                                          :payload (dissoc payload :raw-layouts)}]]
                  #_; TODO r11/window-handling fix remove group
                    [:dispatch (fi/call-api :connection-update-event-vec
                                            (get-in db (gp/connection-publishing path))
                                            nil
                                            {:di (:di payload)
                                             :selections (get-in db (gp/selections path))}
                                            {:new-colors? true
                                             :prio-colors? true}
                                            frame-id)]]))))

(defn- layout-task [db frame-id task-id {layouts :layouts :as params} live?]
  (let [path (gp/top-level frame-id)
        payload
        (cond-> (if live?
                  (let [local-filter (get-in db (gp/applied-filter path))]
                    (cond-> {:di (get-in db (gp/data-instance path))
                             :layouts layouts
                             :operations-desc (get-in db (gp/operation-desc frame-id))}
                      local-filter (assoc :local-filter local-filter)))
                  params)
          (empty? layouts)
          (assoc :raw-layouts (get-in db gp/raw-layouts)))
        operations-desc (:operations-desc payload)]
    (cond-> {:db (-> (attach-operations db frame-id operations-desc {:action :layout
                                                                     :payload (dissoc payload :raw-layouts)})
                     (assoc-in (gp/selected-layouts frame-id) layouts))
             :backend-tube [ws-api/operations-route
                            (transfer-desc frame-id task-id)
                            (attach-lang db payload)]}
      live?
      (assoc :dispatch [:de.explorama.frontend.mosaic.event-logging/log-event frame-id "operation" {:action :layout
                                                                                                    :payload (dissoc payload :raw-layouts)}]))))

(defn- filter-task [db frame-id task-id {local-filter :filter-desc :as params} live?]
  (let [path (gp/top-level frame-id)
        payload
        (if live?
          (cond-> {:di (get-in db (gp/data-instance path))
                   :operations-desc (get-in db (gp/operation-desc frame-id))
                   :layouts (get-in db (gp/selected-layouts frame-id))}
            local-filter (assoc :local-filter local-filter))
          params)
        operations-desc (:operations-desc payload)]
    (cond-> {:db (-> (attach-operations db frame-id operations-desc {:action :filter
                                                                     :payload payload})
                     (assoc-in (gp/applied-filter frame-id) local-filter)
                     (gp/reset-stop-views frame-id))
             :backend-tube [ws-api/operations-route
                            (transfer-desc frame-id task-id)
                            (attach-lang db payload)]}
      live?
      (assoc :dispatch [:de.explorama.frontend.mosaic.event-logging/log-event frame-id "operation" {:action :filter
                                                                                                    :payload payload}]))))

(defn- sync-coupled [_ db frame-id task-id {:keys [sync-id group-sizes] :as payload}]
  (if (and (not= sync-id (get-in db (gp/frame-couple-synced? frame-id)))
           group-sizes)
    {:db (-> (assoc-in db (gp/frame-couple-synced? frame-id) sync-id)
             (gra/update-canvas frame-id
                                [[:sync-couple payload]]
                                task-id))}
    {:dispatch [::ddq/finish-task frame-id task-id ::no-syncing-necessary]}))

(defn- no-task [task-type _ frame-id task-id params]
  (warn "Dispatched task without a valid task-type" task-type frame-id params)
  {:dispatch [::ddq/finish-task frame-id task-id ::no-task]})

(re-frame/reg-event-fx
 ::execute
 (fn [{db :db} [_ task-type frame-id params live? task-id]]
   ((case task-type
      :init init-task
      :group-by (partial by-tasks :group-by group-by-op-desc nil)
      :ungroup (partial by-tasks :ungroup ungroup-op-desc nil)
      :sub-group-by (partial by-tasks :sub-group-by sub-group-by-op-desc nil)
      :unsub-group (partial by-tasks :unsub-group unsub-group-op-desc nil)
      :sort-by (partial by-tasks :sort-by sort-by-op-desc nil)
      :sort-group-by (partial by-tasks :sort-group-by sort-grp-by-op-desc nil)
      :sort-sub-group-by (partial by-tasks :sort-sub-group-by sort-sub-grp-by-op-desc nil)
      :couple (partial by-tasks :couple couple-op-desc nil)
      :sync-coupled (partial sync-coupled :sync-coupled)
      :decouple (partial by-tasks :decouple decouple-op-desc (fn [db frame-id _]
                                                               (gp/dissoc-in db (gp/frame-couple-synced? frame-id))))
      :scatter (partial by-tasks :scatter scatter-op-desc nil)
      :scatter-axis (partial by-tasks :scatter-axis scatter-axis-op-desc nil)
      :treemap (partial by-tasks :treemap tree-op-desc nil)
      :treemap-algorithm (partial by-tasks :treemap-algorithm tree-algo-op-desc nil)
      :raster (partial by-tasks :raster raster-op-desc nil)
      :layout layout-task
      :filter filter-task
      :remove remove-group
      (partial no-task task-type))
    db frame-id task-id params live?)))

(re-frame/reg-event-fx
 ::execute-wrapper
 (fn [_ [_ path action params & [live?]]]
   {:dispatch [::ddq/queue
               (gp/frame-id path)
               [::execute
                action
                (gp/frame-id path)
                params
                (if (boolean? live?)
                  live?
                  true)]]}))

(re-frame/reg-event-fx
 ::execute-wrapper-woq ;without-queue
 (fn [_ [_ path action params task-id]]
   {:dispatch [::execute
               action
               (gp/frame-id path)
               params
               true
               task-id]}))
