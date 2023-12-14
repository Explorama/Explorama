(ns de.explorama.frontend.mosaic.views.frame
  (:require [cuerdas.core :as cuerdas]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.interaction.dnd-util :as dnd-util]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.views.empty :as empty]
            [de.explorama.frontend.mosaic.views.frame-header :as frame-header]
            [de.explorama.frontend.mosaic.views.top-level :as gootop]
            [de.explorama.frontend.mosaic.vis.config :as vis-config]
            [de.explorama.frontend.mosaic.vis.state :as vis-state]
            [de.explorama.frontend.ui-base.utils.timeout :refer [handle-timeout]]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(re-frame/reg-event-fx
 ::event-wrapper
 [(fi/ui-interceptor)]
 (fn [_ [_ event]]
   {:dispatch event}))

(defn frame-body [frame-id {:keys [size] :as vis-desc}]
  (let [infos-sub (if vis-desc
                    (r/atom {:is-minimized? false
                             :hide-settings? true
                             :disable-canvas-click? true
                             :disable-canvas-dbl-click? true
                             :disable-canvas-highlight? true
                             :disable-canvas-right-click? true
                             :size (or size
                                       [140 140])})
                    (fi/call-api :frame-sub frame-id))
        timeout-state (atom nil)]
    (r/create-class
     {:display-name (str "mosaic body" frame-id)
      :component-did-mount (fn [_]
                             (when vis-desc
                               (re-frame/dispatch [::vis-state/restore-vis-state frame-id vis-desc])))
      :component-did-update (fn [this argv]
                              (let [[_ _ {old-size :size}] argv
                                    [_ _ {new-size :size}] (r/argv this)]
                                (when (and vis-desc
                                           (not= old-size new-size))
                                  (swap! infos-sub assoc :size new-size)
                                  (handle-timeout timeout-state
                                                  200
                                                  #(re-frame/dispatch [:de.explorama.frontend.mosaic.interaction.resize/resize-listener frame-id
                                                                       {:ignore-header? false
                                                                        :force-use-resize-infos? true
                                                                        :width (first new-size)
                                                                        :height (second new-size)}])))))
      :reagent-render
      (fn [frame-id _vis-desc]
        (let [path (gp/top-level frame-id)
              {[x y] :coords
               :keys [is-minimized?] :as vis-settings}
              @infos-sub
              empty-frame? @(re-frame/subscribe [::empty-frame? frame-id])
              {:keys [local] :as counts} @(re-frame/subscribe [::frame-header/get-counts frame-id])]
          [:div.window__body.flex (merge {:draggable false
                                          :id (vis-config/frame-body-dom-id frame-id)
                                          :style {:overflow :none
                                                  :padding "0px"
                                                  :display (when is-minimized? :none)
                                                  :position :relative}}
                                         (dnd-util/prevent-drag-group)
                                         {:on-drag-enter #() ;For Woco handling drop-target
                                          :on-drag-leave #()}) ;For Woco handling drop-target})
           (when (and (or empty-frame?
                          (and (not empty-frame?)
                               local
                               (= local 0)))
                      (not @(re-frame/subscribe [::loading-screen? frame-id])))
             [empty/empty-component frame-id (when-not empty-frame?
                                               {:counts-sub counts})])
           [gootop/top-level-container (gp/top-level frame-id) vis-settings]]))})))

(re-frame/reg-sub
 ::empty-frame?
 (fn [db [_ frame-id]]
   (not (seq (get-in db (gp/operation-desc frame-id))))))

(re-frame/reg-sub
 ::show-warn-screen?
 (fn [db [_ frame-id]]
   (get-in db (gp/warn-view frame-id))))

(re-frame/reg-event-fx
 ::warning-proceed
 (fn [{db :db} [_ frame-id]]
   {:db (-> (gp/dissoc-in db (gp/warn-view frame-id))
            (gp/dissoc-in (gp/warn-view-callback frame-id)))
    :dispatch (get-in db (gp/warn-view-callback frame-id))}))

(re-frame/reg-event-db
 ::warning-stop
 (fn [db [_ frame-id]]
   (gp/dissoc-in db (gp/warn-view frame-id))))

(def warn-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::show-warn-screen? frame-id]))
   :title-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-title]))
   :message-1-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-message-part-1]))
   :message-2-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-message-part-2]))
   :recommendation-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-recommendation]))
   :stop-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-follow-recommendation]))
   :proceed-sub
   (fn [_ _]
     (re-frame/subscribe [::i18n/translate :load-warning-screen-not-follow-recommendation]))
   :stop-fn
   (fn [_ frame-id _]
     (re-frame/dispatch [::warning-stop frame-id]))
   :proceed-fn
   (fn [_ frame-id _]
     (re-frame/dispatch [::warning-proceed frame-id]))})

(re-frame/reg-sub
 ::show-stop-screen?
 (fn [db [_ frame-id]]
   (get-in db (gp/stop-view frame-id))))

(re-frame/reg-event-db
 ::show-stop-view
 (fn [db [_ frame-id show?]]
   (cond-> db
     :always (assoc-in (gp/stop-view frame-id) show?)
     (not show?) (gp/dissoc-in (gp/stop-view-details frame-id)))))

(def ^:private stop-screens
  {:stop-view-display {:title :load-stop-screen-title
                       :message-1 :load-stop-screen-message-part-1
                       :message-2 :load-stop-screen-message-part-2
                       :stop :load-stop-screen-follow-recommendation}

   :stop-view-too-much-data {:title :too-much-data-title
                             :message-1 :too-much-data-message-part-1-min-max
                             :message-2 :too-much-data-message-part-2
                             :stop :too-much-data-follow-recommendation}

   :stop-view-scatter-no-values-for-axis {:title :scatter-no-values-for-axis-title
                                          :message-1 :scatter-no-values-for-axis-part-1
                                          :message-2 :scatter-no-values-for-axis-part-2
                                          :stop :scatter-no-values-for-axis-recommendation}

   :stop-view-scatter-empty {:title :scatter-no-data-title
                             :message-1 :scatter-no-data-title-part-1
                             :message-2 :scatter-no-data-title-part-2
                             :stop :scatter-no-data-title-recommendation}

   :stop-view-unknown {:title :stop-view-unknown-title
                       :message-1 :stop-view-unknown-part-1
                       :message-2 :stop-view-unknown-part-2
                       :stop :stop-view-unknown-recommendation}})

(re-frame/reg-sub
 ::stopscreen-label
 (fn [db [_ frame-id k label]]
   (let [details (get-in db (gp/stop-view-details frame-id))
         label (i18n/translate db (get-in stop-screens [k label]))]
     (cond-> label
       (= k :stop-view-too-much-data)
       (cuerdas/format {:data-count (i18n/localized-number (:data-count details))
                        :max-data-amount (i18n/localized-number (:max-data-amount details))})))))

(def stop-screen-impl
  {:show? (fn [frame-id]
            (re-frame/subscribe [::show-stop-screen? frame-id]))
   :title-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :title]))
   :message-1-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-1]))
   :message-2-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-2]))
   :stop-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :stop]))
   :ok-fn (fn [_ frame-id]
            (re-frame/dispatch [::show-stop-view frame-id nil]))})

(def product-tour-impl
  {:component :mosaic})

(re-frame/reg-sub
 ::loading-screen?
 (fn [[_ frame-id]]
   (let [tl-path (gp/top-level frame-id)]
     [(re-frame/subscribe [:de.explorama.frontend.mosaic.operations.core/exit-frame tl-path])
      (re-frame/subscribe [::ddq/queue frame-id])
      (re-frame/subscribe [::ddq/job frame-id])
      (re-frame/subscribe [::ddq/finished? frame-id])]))
 (fn [[exit-frame queue job counter] _]
   (not (and (empty? queue)
             (empty? job)
             (nil? exit-frame)
             counter))))

(re-frame/reg-sub
 ::cancellable?
 (fn [_ _]
   false))

(re-frame/reg-event-fx
 ::cancel-loading
 (fn [_ [_ _]]
   {}))

(def loading-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::loading-screen? frame-id]))
   :cancellable?
   (fn [_]
     (re-frame/subscribe [::cancellable?]))
   :cancel-fn
   (fn [frame-id _]
     (re-frame/dispatch [::cancel-loading frame-id]))
   :loading-screen-message-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-message]))
   :loading-screen-tip-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip]))
   :loading-screen-tip-titel-sub
   (fn [_]
     (re-frame/subscribe [::i18n/translate :loading-screen-tip-titel]))})
