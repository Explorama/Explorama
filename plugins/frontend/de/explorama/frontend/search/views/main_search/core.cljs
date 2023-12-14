(ns de.explorama.frontend.search.views.main-search.core
  (:require [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.search.views.components.search-selection-component :refer [search-selection-component topic-datasource-switch]]
            [de.explorama.frontend.search.views.components.dialog :as sdialog]
            [re-frame.core :refer [subscribe dispatch]]
            [de.explorama.frontend.search.data.acs :as acs]
            [de.explorama.frontend.search.data.topics :refer [is-topic-attr-desc?]]
            [de.explorama.frontend.search.backend.traffic-lights :as traffic-lights-backend]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.frontend.search.views.components.header-bar :refer [header-bar]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.search.backend.di :as di-backend]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.data.di :as data-di]
            [de.explorama.frontend.search.views.attribute-bar :as ac-view]
            [de.explorama.frontend.search.views.search-bar :as search-bar]
            [de.explorama.frontend.search.views.components.direct-visualization :refer [direct-visualization]]
            [de.explorama.frontend.search.views.components.traffic-light :refer [traffic-light]]))

(defn error-classes [{:keys [req-attrs-valid?
                             req-attrs-num-valid?
                             validation-result]}]
  (when (or (false? req-attrs-valid?)
            (false? req-attrs-num-valid?)
            (and (not (:empty? validation-result)) (not (:valid? validation-result))))
    (if (:details validation-result)
      (cond-> " "
        (-> validation-result :details :valid-start? false?) (str " search__block--error-startdate")
        (-> validation-result :details :valid-end? false?) (str " search__block--error-enddate")
        (-> validation-result :details :valid-selected? false?) (str " search__block--error-startdate"))
      " search__block--error")))

(defn search-attribute-row [frame-id attr-desc idx is-last? read-only?]
  (let [path (spath/search-row-data frame-id attr-desc)
        validation @(subscribe [::validation/row-valid-infos frame-id path attr-desc])
        labels @(fi/call-api [:i18n :get-labels-sub])
        attr->display-name (fn [[attr _]]
                             (if (and (is-topic-attr-desc? attr-desc)
                                      @(subscribe [:de.explorama.frontend.search.views.formdata/topic-selection? path]))
                               @(subscribe [::i18n/translate :topic-category])
                               (get labels attr attr)))]
    [:div {:class (str "search__block"
                       (error-classes validation))}
     [:div.search__block__label
      [:label {:for   "input-select"
               :class "explorama__form__label"}
       (attr->display-name attr-desc)]
      [topic-datasource-switch {:frame-id frame-id
                                :attr-desc attr-desc
                                :path path
                                :disabled? read-only?}]]
     [:div.search__block__input
      [search-selection-component {:frame-id frame-id
                                   :path path
                                   :attr-desc attr-desc
                                   :read-only? read-only?
                                   :is-last? is-last?}]]
     [:div.search__block__actions
      [button {:variant :tertiary
               :aria-label :delete-label
               :disabled? read-only?
               :on-click #(dispatch [:de.explorama.frontend.search.views.attribute-bar/delete-search-row frame-id attr-desc])
               :start-icon :trash}]]]))

(defn- main-search-view [frame-id]
  (let [attribute-select-hint @(subscribe [::i18n/translate :attribute-select-hint])
        searchbutton-label @(subscribe [::i18n/translate :searchbutton-label])
        apply-changes-label @(subscribe [::i18n/translate :apply-changes])
        is-requesting? @(subscribe [::acs/is-requesting? frame-id])
        is-clicked? @(subscribe [:de.explorama.frontend.search.views.formdata/search-button-is-clicked? frame-id])
        search-changed? @(subscribe [:de.explorama.frontend.search.views.formdata/search-changed? frame-id])
        di-creation-pending? @(subscribe [::data-di/di-creation-pending? frame-id])
        attributes @(subscribe [:de.explorama.frontend.search.views.attribute-bar/sorted-search-attributes frame-id])
        unavailable? @(subscribe [::acs/unavailable-types? attributes])
        attributes-submitable? @(subscribe [::validation/search-formdata-valid? frame-id])
        di-created? (boolean (seq @(subscribe [::data-di/di frame-id])))
        searchbutton-tooltip (subscribe [::i18n/translate (if di-created?
                                                            :searchbutton-update-tooltip
                                                            :searchbutton-tooltip)])
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id
                                  :component :search
                                  :additional-info :search})
        product-tour-search-active? @(fi/call-api [:product-tour :component-active?-sub]
                                                  :search :search)
        {:keys [success status] :as current-light} @(subscribe [::traffic-lights-backend/traffic-light frame-id])]
    [:div {:class "search__main"}
     [:div {:id    (str "searchformend-" frame-id)
            :class "search__container"}
      (if (seq attributes)
        (reduce (fn [par [idx attr]]
                  (conj par
                        (with-meta
                          [search-attribute-row
                           frame-id
                           attr
                           idx
                           (= idx (dec (count attributes)))
                           read-only?]
                          {:key (str (spath/frame-search-rows frame-id)
                                     attr "-row")})))
                [:div {:class "search__section"}]
                (map-indexed vector attributes))
        [:div {:class "search__section"}
         [:div.explorama__form__row
          [:div.col-2]
          [:div.col-8 [:span attribute-select-hint]]]])]
     [traffic-light
      frame-id
      (when attributes-submitable? current-light)
      {:parent-class "search__resultinfo"}]
     [:div.search__actions
      [button {:loading? (or di-creation-pending?
                             (and is-requesting?
                                  is-clicked?)
                             (= status :pending))
               :disabled? (or di-creation-pending?
                              (or (not attributes-submitable?)
                                  (not search-changed?))
                              (not product-tour-search-active?)
                              (not (or success
                                       unavailable?))
                              (= status :pending))
               :on-click (fn [e]
                                        ;  (.preventDefault e)
                           (when attributes-submitable?
                             (dispatch [:de.explorama.frontend.search.api.core/save-undo-state frame-id])
                             (dispatch [::acs/set-create-data-instance frame-id])
                             (dispatch [:de.explorama.frontend.search.views.formdata/search-button-is-clicked frame-id true])
                             (dispatch [:de.explorama.frontend.search.views.formdata/search-changed frame-id false])
                             (dispatch [::di-backend/submit-form frame-id])
                             (when-let [next-event-vec (fi/call-api [:product-tour :next-event-vec]
                                                                    :search :search)]
                               (dispatch next-event-vec))))
               :start-icon :search
               :label (if di-created?
                        apply-changes-label
                        searchbutton-label)}]
      (when (and di-created?
                 (not search-changed?))
        [:div.search__ready
         [icon {:icon :check
                :color :green}]
         [tooltip {:text searchbutton-tooltip}
          [icon {:icon :info-circle
                 :color :gray
                 :brightness 6}]]])
      [:div.search__modules
       [direct-visualization frame-id (and di-created? search-changed?)]]]]))

(defn- header [frame-id]
  [header-bar frame-id {:search-bar? true}])

(defn free-view [frame-id]
  (let [search-bar-result? @(subscribe [:de.explorama.frontend.search.views.search-bar/results-open? frame-id])]
    [:<>
     [:div.search__direct__wrapper
      [sdialog/frame-dialog frame-id]
      [header frame-id]
      [:div.window__body {:style {:overflow-y "auto"
                                  :height     "100%"}}
       (if search-bar-result?
         [search-bar/result-view frame-id]
         [:div.window__wrapper__search
          [ac-view/attributes-selection-bar frame-id]
          [main-search-view frame-id]])]]]))

