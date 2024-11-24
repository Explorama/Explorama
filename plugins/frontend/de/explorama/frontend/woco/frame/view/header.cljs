(ns de.explorama.frontend.woco.frame.view.header
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [header]]
            [re-frame.core :refer [dispatch reg-event-db reg-sub subscribe reg-event-fx]]
            [de.explorama.frontend.woco.api.interaction-mode :refer [ro-interceptor]]
            [de.explorama.frontend.woco.frame.color :as frame-color]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.view.legend :refer [legend-open?]]
            [de.explorama.frontend.woco.path :as path]))

(reg-event-fx
 ::set-custom-title
 [ro-interceptor]
 (fn [{db :db} [_ frame-id-or-path title]]
   {:db (assoc-in db (path/frame-custom-title frame-id-or-path) title)
    :dispatch [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id-or-path]}))

(reg-sub
 ::custom-title
 (fn [db [_ frame-id-or-path]]
   (get-in db (path/frame-custom-title frame-id-or-path))))

(defn full-title [frame-id]
  (let [{:keys [frame-title-sub frame-title-prefix-sub]} @(subscribe [:de.explorama.frontend.woco.frame.plugin-api/frame-header frame-id])
        vertical-count-number @(subscribe [:de.explorama.frontend.woco.frame.api/vertical-count frame-id])
        custom-title (or @(subscribe [::custom-title frame-id])
                         (when frame-title-sub
                           @(frame-title-sub frame-id)))
        title-spacer (if (= \space (first custom-title)) "" " ")
        title-prefix (when frame-title-prefix-sub
                       @(frame-title-prefix-sub frame-id vertical-count-number))]
    (str title-prefix title-spacer custom-title)))

(defn frame-header [{frame-id :id} drag-props]
  (let [{:keys [frame-title-sub frame-title-prefix-sub frame-icon
                on-minimize-event on-maximize-event on-normalize-event
                on-close-fn can-change-title?]}
        @(subscribe [:de.explorama.frontend.woco.frame.plugin-api/frame-header frame-id])
        coupled? @(subscribe [:de.explorama.frontend.woco.api.couple/couple-with frame-id])
        {:keys [is-maximized? is-minimized? type]} @(subscribe [::evts/frame frame-id])
        show-legend? (legend-open? frame-id)
        close-tooltip @(subscribe [::i18n/translate :close-tooltip])
        is-content-type? (= type :frame/content-type)
        custom-title (or @(subscribe [::custom-title frame-id])
                         (when frame-title-sub
                           @(frame-title-sub frame-id)))
        vertical-count-number @(subscribe [:de.explorama.frontend.woco.frame.api/vertical-count frame-id])
        frame-icon (if (fn? frame-icon)
                     (frame-icon frame-id)
                     frame-icon)
        title-prefix (when frame-title-prefix-sub
                       @(frame-title-prefix-sub frame-id vertical-count-number))
        header-classes [@(subscribe [::frame-color/header-color frame-id]) "window__header"]]
    [header (cond-> {:title title-prefix
                     :icon frame-icon
                     :maximize-tooltip (subscribe [::i18n/translate :maximize-tooltip])
                     :minimize-tooltip (subscribe [::i18n/translate :minimize-tooltip])
                     :normalize-tooltip (subscribe [::i18n/translate :normalize-tooltip])
                     :close-tooltip close-tooltip
                     :on-close #(on-close-fn
                                 frame-id
                                 (fn []
                                   (dispatch [:de.explorama.frontend.woco.frame.api/close frame-id])
                                   (when @(subscribe [:de.explorama.frontend.woco.api.couple/couple-with frame-id])
                                     (dispatch [:de.explorama.frontend.woco.api.couple/decouple frame-id]))))
                     :extra-props (assoc drag-props
                                         :class header-classes)}
              (and is-content-type? can-change-title?)
              (assoc :on-title-set
                     (fn [new-title]
                       (dispatch [:de.explorama.frontend.woco.event-logging/ui-wrapper
                                  frame-id
                                  "set-custom-title"
                                  [::set-custom-title frame-id new-title]
                                  {:title new-title}])))
              is-content-type?
              (assoc :title-read-only?
                     (fi/call-api [:interaction-mode :read-only-sub?]
                                  {:frame-id frame-id
                                   :component (:vertical frame-id)
                                   :additional-info :rename-title}))
              custom-title
              (assoc :user-title custom-title)
              (and on-minimize-event on-maximize-event on-normalize-event
                   (not coupled?))
              (assoc :on-minimize #(dispatch [:de.explorama.frontend.woco.frame.api/minimize frame-id show-legend?])
                     :on-maximize #(dispatch [:de.explorama.frontend.woco.frame.api/maximize frame-id show-legend?])
                     :on-normalize #(dispatch [:de.explorama.frontend.woco.frame.api/normalize frame-id show-legend?])
                     :is-maximized? (boolean is-maximized?)
                     :is-minimized? (boolean is-minimized?)))]))