(ns de.explorama.frontend.indicator.views.overview
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [section]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format safe-aget]]
            [de.explorama.frontend.ui-base.utils.view :refer [is-inside?]]
            [de.explorama.frontend.indicator.components.dialog :as dialog]
            [de.explorama.frontend.indicator.components.direct-visualization :refer [direct-visualization]]
            [de.explorama.frontend.indicator.views.management :as management]
            [re-frame.core :as re-frame]))

(defn- indicator-card [frame-id
                       project?
                       {:keys [name description
                               shared-by
                               id write-access?]
                        :as indicator-desc}]
  (let [shared-by-name (when shared-by @(fi/call-api :name-for-user-sub shared-by))
        is-changed? @(re-frame/subscribe [::management/changed? id])
        edit-tooltip @(re-frame/subscribe [::i18n/translate :edit-label])
        send-copy-tooltip @(re-frame/subscribe [::i18n/translate :send-copy-label])
        delete-tooltip @(re-frame/subscribe [::i18n/translate :delete-tooltip-title])
        copy-tooltip @(re-frame/subscribe [::i18n/translate :config-copy])
        shared-by-label @(re-frame/subscribe [::i18n/translate :shared-by])
        context-menu-options {:items [{:label edit-tooltip
                                       :icon :edit
                                       :on-click #(re-frame/dispatch [::management/change-active-indicator id project?])}
                                      {:label copy-tooltip
                                       :disabled? is-changed?
                                       :icon :copy
                                       :on-click #(re-frame/dispatch [::management/copy-indicator id project?])}
                                      {:label send-copy-tooltip
                                       :disabled? is-changed?
                                       :icon :share
                                       :on-click #(re-frame/dispatch [::dialog/set-show "send-copy" id true])}
                                      (when write-access?
                                        {:label delete-tooltip
                                         :icon :trash
                                         :on-click #(re-frame/dispatch [::dialog/set-show "delete" id true])})]}]
    [:li.indicator__card
     {:on-context-menu (fn [e]
                         (.preventDefault e)
                         (.stopPropagation e)
                         (fi/call-api :context-menu-event-dispatch
                                      (aget e "nativeEvent")
                                      context-menu-options))}
     [:div.indicator__contextmenu
      {:on-mouse-down (fn [e]
                        (.stopPropagation e))
       :on-click (fn [e]
                   (.stopPropagation e)
                   (fi/call-api :context-menu-event-dispatch
                                (safe-aget e "nativeEvent")
                                context-menu-options))}
      [icon {:icon :menu}]]
     [:div.indicator__info
      [:h1 name]
      [:div.indicator-description
       {:title description}
       description]
      (when shared-by-name
        [:div.credits (format shared-by-label shared-by-name)])]
     [:div.indicator__actions {:on-mouse-down (fn [e]
                                                (when-let [inside-actions? (-> e
                                                                               (safe-aget "nativeEvent" "target")
                                                                               (is-inside? ".indicator__actions"))]
                                                  (.stopPropagation e)))}
      [direct-visualization id project? is-changed?]]]))

(defn- create-indicator-card []
  (let [create-label @(re-frame/subscribe [::i18n/translate :create-new-indicator])]
    [:li.indicator__card.indicator__create
     {:on-click #(re-frame/dispatch [::management/create-new-indicator])}
     [:h1 create-label]
     [icon {:icon :plus}]]))

(defn- indicator-list [frame-id label project? indicators]
  [section {:label label}
   [:div.section__collapsible__content
    (reduce (fn [parent desc]
              (conj parent
                    (with-meta
                      [indicator-card frame-id project? desc]
                      {:keys (str project? "-" desc)})))
            [:ul.indicator__list
             (when-not project?
               [create-indicator-card])]
            indicators)]])

(defn view [frame-id]
  (let [current-indicators @(re-frame/subscribe [::management/all-indicators])
        current-indicators-label (re-frame/subscribe [::i18n/translate :own-indicators-list-label])
        project-indicators @(re-frame/subscribe [::management/project-indicators])
        project-indicators-label (re-frame/subscribe [::i18n/translate :project-indicators-list-label])]
    [:<>
     [indicator-list frame-id current-indicators-label false current-indicators]
     (when (seq project-indicators)
       [indicator-list frame-id project-indicators-label true project-indicators])]))