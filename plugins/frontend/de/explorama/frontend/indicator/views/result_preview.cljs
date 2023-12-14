(ns de.explorama.frontend.indicator.views.result-preview
  (:require [clojure.string :as str]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button section]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.path :as path]
            [de.explorama.frontend.indicator.views.management :as management]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [react-window :refer [FixedSizeList]]
            [reagent.core :as reagent]))

(re-frame/reg-event-db
 ws-api/data-sample-result
 (fn [db [_ indicator-id result]]
   (assoc-in db (path/preview-result indicator-id) result)))

(re-frame/reg-sub
 ::preview-result
 (fn [db [_ indicator-id]]
   (let [data (get-in db (path/preview-result indicator-id))]
     (when data
       (let [header (->> data first keys (remove #{"id"}) vec)]
         {:header header
          :data data})))))

(re-frame/reg-event-fx
 ::calculate-preview
 (fn [{db :db} [_ indicator-id]]
   (let [final-desc (management/indicator->final-description db indicator-id)]
     {;:db (update-in db path/preview-results dissoc indicator-id)
      :backend-tube [ws-api/data-sample
                     {:client-callback [ws-api/data-sample-result indicator-id]}
                     final-desc]})))

(defn format-changes [language item-org]
  (cond (number? item-org)
        (i18n/localized-number item-org)
        (sequential? item-org)
        (str/join ", "
                  (map (partial format-changes language) item-org))
        :else
        item-org))

(defn- header-render [attribute-labels row]
  [:ul {:key "indicator-data-preview-row-header"
        :style {:width "100%"
                :height 10}}
   (for [[index ele] (map-indexed vector row)]
     (let [attribute-label (get attribute-labels ele ele)]
       [:li {:key (str "indicator-data-preview-col-h-" index)
             :title attribute-label
             :style {:float :left
                     :display :block
                     :width 120
                     :overflow :hidden
                     :white-space :nowrap
                     :text-overflow :ellipsis}}
        attribute-label]))])

(defn- data-row-render [index style header row]
  (let [language @(re-frame/subscribe [::i18n/current-language])
        no-element-at @(re-frame/subscribe [::i18n/translate :no-element-at])
        items (or row (str no-element-at index))]
    [:ul {:style style
          :key (str "indicator-data-preview-row-" index)}
     (doall
      (for [[num item-key] (map-indexed vector header)
            :let [item-org (get items item-key)
                  item-org (format-changes language item-org)]]
        [:li {:key (str "indicator-data-preview-col-" num "-" index)
              :title item-org
              :style {:float :left
                      :display :block
                      :width 120
                      :overflow :hidden
                      :white-space :nowrap
                      :text-overflow :ellipsis}}
         item-org]))]))

(defn view [indicator-id]
  (let [{:keys [data header] :as preview-result} @(re-frame/subscribe [::preview-result indicator-id])
        {:keys [input-data-section calculate-preview-button
                no-data]} @(re-frame/subscribe [::i18n/translate-multi
                                            :input-data-section :calculate-preview-button
                                            :no-data])
        attribute-labels @(fi/call-api [:i18n :get-labels-sub])
        hint-label @(re-frame/subscribe [::i18n/translate :input-data-section-hint])
        {valid? :valid?
         non-valid-reasons :reasons} @(re-frame/subscribe [::management/valid? indicator-id])
        reasons-msg (->> non-valid-reasons
                         (filter identity)
                         (map #(deref (re-frame/subscribe %)))
                         (str/join "\n"))]
    [section {:label input-data-section
              :default-open? false
              :hint hint-label}
     [:div
      [button (cond-> {:label calculate-preview-button
                       :extra-style {:pointer-events :auto}
                       :disabled? (not valid?)
                       :on-click #(re-frame/dispatch [::calculate-preview indicator-id])}
                (not valid?)
                (assoc :title reasons-msg))]
      (when preview-result
        [icon {:icon :check :color :green}])]
     [:div.prediction__data__list
      (if-not preview-result
        no-data
        [:<>
         [header-render attribute-labels header]
         [:> FixedSizeList
          {:item-count (count data)
           :height 200
           :item-size 30}
          (reagent/reactify-component
           (fn [{:keys [index style]}]
             (let [row (get data index)]
               [data-row-render index style header row])))]])]]))
