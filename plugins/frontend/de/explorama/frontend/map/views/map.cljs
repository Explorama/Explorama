(ns de.explorama.frontend.map.views.map
  (:require [cuerdas.core :as cuerdas]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.timeout :refer [handle-timeout]]
            [de.explorama.frontend.map.config :as config]
            [de.explorama.frontend.map.map.api :as map-api]
            [de.explorama.frontend.map.paths :as geop]
            [de.explorama.frontend.map.views.stop-screen :as stop-screen]
            [de.explorama.frontend.map.views.warning-screen :as warning-screen]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-event-db
 ::set-loading
 (fn [db [_ frame-id display source]]
   (debug "frame set-loading" {:frame-id frame-id
                               :source source
                               :display? display})
   (if (get-in db (geop/frame-desc frame-id)) ;Check if Frame is still there
     (assoc-in db (geop/frame-loading frame-id) display)
     db)))

(re-frame/reg-event-fx
 ::cancel-loading
 (fn [_ [_ frame-id]]
   {:backend-tube [ws-api/cancel-loading {} frame-id]
    :dispatch [::set-loading frame-id false ::cancel-loading]}))

(re-frame/reg-sub
 ::is-loading?
 (fn [db [_ frame-id]]
   (get-in db (geop/frame-loading frame-id) false)))

(defn frame-body [frame-id {:keys [size] :as vis-desc}]
  (let [infos-sub (if vis-desc
                    (r/atom {:is-minimized? false
                             :hide-layer-control? true
                             :size (or size
                                       [140 140])})
                    (fi/call-api :frame-sub frame-id))
        parent-comp (r/atom nil)
        timeout-state (atom nil)]
    (r/create-class
     {:display-name (str "map body" frame-id)
      :component-did-mount #(do (reset! parent-comp (rdom/dom-node %))
                                (let [init? @(re-frame/subscribe [:de.explorama.frontend.map.map.core/initialized? frame-id])]
                                  (cond
                                    (seq vis-desc) (re-frame/dispatch [:de.explorama.frontend.map.vis-state/restore-vis-desc frame-id vis-desc])
                                    init? (re-frame/dispatch [:de.explorama.frontend.map.map.core/init frame-id]))))
      :component-did-update (fn [this argv]
                              (let [[_ _ {old-size :size}] argv
                                    [_ _ {new-size :size}] (r/argv this)
                                    _ @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language])
                                    init? @(re-frame/subscribe [:de.explorama.frontend.map.map.core/initialized? frame-id])
                                    frame-state @(re-frame/subscribe [:de.explorama.frontend.map.map.core/map-state frame-id])]
                                (when (and vis-desc
                                           (not= old-size new-size))
                                  (swap! infos-sub assoc :size new-size)
                                  (handle-timeout timeout-state
                                                  200
                                                  #(map-api/resize-map frame-id)))

                                (when (= (:state frame-state)
                                         :running)
                                  (map-api/render-map frame-id))

                                (when init?
                                  (re-frame/dispatch [:de.explorama.frontend.map.map.core/init frame-id]))))
      :reagent-render
      (fn [frame-id vis-desc]
        (let [is-project-loading? (fi/call-api :project-loading-sub)
              layer-config @(re-frame/subscribe [:de.explorama.frontend.map.core/layer-config])
              {:keys [is-minimized? size hide-layer-control?]} @infos-sub
              _ @(re-frame/subscribe [:de.explorama.frontend.map.map.core/initialized? frame-id])
              _ @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language])]
          [error-boundary
           [:div.window__body.flex
            {:id (config/frame-body-dom-id frame-id)
             :style (cond-> {:display (when is-minimized?
                                        "none")}
                      (and vis-desc size)
                      (assoc
                       :width (first size)
                       :height (second size)))}]]))})))

(def product-tour-impl
  {:component :map})

(def warn-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [:de.explorama.frontend.map.views.warning-screen/warning-view-display frame-id]))
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
     (re-frame/dispatch [::warning-screen/warning-stop frame-id]))
   :proceed-fn
   (fn [_ frame-id _]
     (re-frame/dispatch [::warning-screen/warning-proceed frame-id]))})

(def ^:private stop-screens
  {:stop-view-display {:title :load-stop-screen-title
                       :message-1 :load-stop-screen-message-part-1
                       :message-2 :load-stop-screen-message-part-2
                       :stop :load-stop-screen-follow-recommendation}

   :stop-view-too-much-data {:title :too-much-data-title
                             :message-1 :too-much-data-message-part-1-min-max
                             :message-2 :too-much-data-message-part-2
                             :stop :too-much-data-follow-recommendation}

   :stop-view-unknown {:title :stop-view-unknown-title
                       :message-1 :stop-view-unknown-part-1
                       :message-2 :stop-view-unknown-part-2
                       :stop :stop-view-unknown-recommendation}

   :stop-view-stop-event-layer {:title :stop-view-stop-event-layer-title
                                :message-1 :stop-view-stop-event-layer-part-1
                                :message-2 :stop-view-stop-event-layer-part-2
                                :stop :stop-view-stop-event-layer-recommendation}})

(re-frame/reg-sub
 ::stopscreen-label
 (fn [db [_ frame-id k label]]
   (let [details (get-in db (geop/stop-view-details frame-id))
         label (i18n/translate db (get-in stop-screens [k label]))]
     (cond-> label
       (#{:stop-view-too-much-data :stop-view-stop-event-layer} k)
       (cuerdas/format {:data-count (i18n/localized-number (:data-count details))
                        :max-data-amount (i18n/localized-number (:max-data-amount details))})))))

(def stop-screen-impl
  {:show? (fn [frame-id]
            (re-frame/subscribe [::stop-screen/stop-view-display frame-id]))
   :title-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :title]))
   :message-1-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-1]))
   :message-2-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :message-2]))
   :stop-sub (fn [k frame-id] (re-frame/subscribe [::stopscreen-label frame-id k :stop]))
   :ok-fn (fn [_ frame-id]
            (re-frame/dispatch [::stop-screen/stop-view-display frame-id false]))})

(def loading-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::is-loading? frame-id]))
   :cancellable?
   (fn [_]
     (atom false)) ;! FIXME
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
