(ns de.explorama.frontend.search.views.toolbar
  (:require [clojure.string :refer [blank?]]
            [cuerdas.core :as cuerdas]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                            input-field]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [goog.string.format]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [de.explorama.frontend.search.views.components.dialog :as sdialog]
            [de.explorama.frontend.search.views.search-query :as search-query]
            [de.explorama.shared.search.ws-api :as ws-api]))

(defn- load-query [close-callback frame-id {:keys [id title last-used]}]
  (let [lang @(subscribe [::i18n/current-language])
        {:keys [unit num]} (search-query/last-used->str lang last-used (search-query/now-date))
        {unit unit
         :keys [search-query-used delete-label close cancel-label delete-query-title delete-query-message]}
        @(subscribe [::i18n/translate-multi unit :search-query-used :delete-label :close :cancel-label :delete-query-title :delete-query-message])
        last-used (format search-query-used
                          (cuerdas/format unit {:num num}))]
    [:div.toolbar-preview {:style {:max-width 180}}
     [:span.saved__title {:style {:max-width 170}}
      [tooltip {:text title
                :direction :left}
       [:div.truncate-text title]]]
     [:span.saved__lastused.truncate-text last-used]
     [:div.toolbar__action
      [button {:variant :tertiary
               :aria-label close
               :start-icon :trash
               :on-click #(do
                            (.stopPropagation %)
                            (close-callback)
                            (dispatch [::sdialog/dialog-data
                                       frame-id
                                       {:on-success (fn []
                                                      (dispatch [ws-api/search-query-delete id]))
                                        :no-label cancel-label
                                        :yes-label delete-label
                                        :icon :trash
                                        :dialog-type :warning
                                        :message delete-query-message
                                        :title delete-query-title}]))}]]]))

(def load-query-elem
  {:icon :searchlist
   :group "Search queries"
   :label :load-label
   :tooltip :search-query-tooltip
   :disabled? (fn [frame-id]
                (boolean (not (seq @(subscribe [::search-query/queries ""])))))
   :on-click (fn [e frame-id {:keys [close-callback]}]
               (let [search-queries @(subscribe [::search-query/queries ""])
                     search-queries (sort-by :last-used #(> %1 %2)
                                             search-queries)]
                 {:items (mapv (fn [{:keys [id query] :as q-desc}]
                                 {:label [load-query close-callback frame-id q-desc]
                                  :on-click #(dispatch [::search-query/load-query frame-id id query])})
                               search-queries)}))})

(defn- save-query [frame-id title-state close-callback]
  (let [title @title-state
        {:keys [valid? not-empty? reason]} @(subscribe [::search-query/is-new-title-valid? title])
        invalid? (not (and valid? not-empty?))
        save-fn (fn []
                  (when (fn? close-callback)
                    (close-callback))
                  (dispatch [ws-api/search-query-save @title-state frame-id])
                  (dispatch (fi/call-api :notify-event-vec
                                         {:type :success
                                          :category {:misc :search-query-saved}
                                          :message @(subscribe [::i18n/translate :saved-label])})))]
    [:div {:style {:width 250
                   :display :flex
                   :flex-direction :row}}
     [input-field {:placeholder (subscribe [::i18n/translate :save-title])
                   :value title-state
                   :on-change #(reset! title-state %)
                   :on-key-press (fn [ev]
                                   (when (and valid?
                                              not-empty?
                                              (= 13 (aget ev "which")))
                                     (save-fn)))
                   :autofocus? true
                   :invalid? invalid?
                   :caption (when reason (subscribe [::i18n/translate reason]))}]
     [:div {:style {:display :inline-block
                    :margin-left 30}}
      [button {:variant :tertiary
               :start-icon :save
               :type :normal
               :disabled? invalid?
               :on-click (fn [e]
                           (.stopPropagation e)
                           (save-fn))}]]]))

(def save-query-elem
  (let [title-state (r/atom "")]
    {:icon :save-search
     :group "Search queries"
     :label :save-button-label
     :tooltip :search-query-tooltip-save
     :disabled? (fn [frame-id]
                  (not (boolean (seq @(subscribe [:de.explorama.frontend.search.views.formdata/formdata frame-id])))))
     :on-click (fn [e frame-id {:keys [close-callback]}]
                 (reset! title-state "")
                 {:items
                  [{:label [save-query frame-id title-state close-callback]
                    :is-custom-content? true
                    :on-click (fn [e _ _]
                                (.stopPropagation e)
                                false)}]})}))

(def toolbar-impl
  {:on-toggle (fn [frame-id show?])
   :on-duplicate-fn (fn [frame-id]
                      (dispatch [:de.explorama.frontend.search.core/duplicate-search-frame frame-id]))
   :items (fn [frame-id]
            (let [read-only? @(fi/call-api [:interaction-mode :read-only-sub?] {:frame-id frame-id})]
              (cond-> []
                (not read-only?)
                (conj save-query-elem
                      load-query-elem))))})

