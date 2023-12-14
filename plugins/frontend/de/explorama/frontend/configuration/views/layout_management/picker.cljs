(ns de.explorama.frontend.configuration.views.layout-management.picker
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [icon chip]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field select checkbox]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.select :refer [to-option]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [export-ignore-class]]
            [de.explorama.shared.common.configs.layouts :refer [is-layout-valid?]]
            [de.explorama.shared.common.configs.overlayers :refer [is-overlayer-valid?]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.views.legend :refer [attr->display-name]]
            [de.explorama.frontend.configuration.components.save-dialog :refer [layout-overlayer-save-dialog]]
            [re-frame.core :refer [subscribe dispatch]]
            [data-format-lib.dates :as dfdates]
            [clojure.string :refer [join blank? lower-case replace trim]]
            [reagent.core :as r]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.data.core :as data]
            [de.explorama.frontend.configuration.views.layout-management.editing :as layout-editing]
            [de.explorama.frontend.configuration.config :as config]))


(defn plus-minus-infinity-or-localized [v]
  (let [{:keys [legend-negative-infinity legend-infinity]}
        @(subscribe [::i18n/translate-multi :legend-negative-infinity :legend-infinity])]
    (cond (= ##-Inf v)
          legend-negative-infinity
          (= ##Inf v)
          legend-infinity
          :else
          (i18n/localized-number v))))

(defn- loc-num [attribute value]
  (cond-> value
    (and (number? value)
         (not= attribute ::dfdates/year))
    (plus-minus-infinity-or-localized)))

(defn- calc-color-legend [attribute assignment color]
  (cond
    (map? assignment)
    (str (loc-num attribute (:from assignment))
         " - "
         (loc-num attribute (:to assignment)))
    (and (vector? assignment)
         (= 2 (count assignment))
         (every? number? assignment))
    (str
     (loc-num attribute (first assignment))
     " - "
     (loc-num attribute (second assignment)))
    (vector? assignment)
    (join "," assignment)
    :else
    assignment))

(defn- color-code-list [props layout]
  (when-let [attributes (:attributes layout)]
    (let [{:keys [value-assigned attribute-type color-scheme]} layout]
      (reduce (fn [acc [idx [_ color]]]
                (let [value (calc-color-legend attributes (get value-assigned idx) color)]
                  (cond-> acc
                    (seq value)
                    (conj
                     [:li
                      [:span.legend__color {:style {:background-color color}}]
                      [:span.legend__value (if (= "string" attribute-type)
                                             [tooltip {:text (replace value #"," "\n")
                                                       :direction :right}
                                              value]
                                             value)]]))))
              [:ul]
              (map-indexed vector (sort-by #(js/parseInt (name (first %)))
                                           (:colors color-scheme)))))))

(defn- gen-add-label [{:keys [name]} attr-labels colors]
  [:div.flex.flex-col
   [:div.flex.flex-row.justify-between.align-items-center
    [:div.text-truncate.text-xs.text-bold
     name]
    (reduce
     #(conj %1 [chip {:label %2
                      :size :extra-small}])
     [:div.flex.gap-2]
     attr-labels)]
   (reduce
    (fn [res [_ color]]
      (conj res [:span.h-full.flex-1
                 {:style {:background-color color}}]))
    [:div.flex.flex-row.h-4.mt-4.rounded-full.overflow-hidden]
    (sort-by first colors))])

(defn- gen-add-tooltip [attributes label]
  (str @(subscribe [::i18n/translate :color-base-label])
       (if (= 1 (count attributes))
         "attribute"
         "attributes")
       ":"
       "\n"
       label))

(defn- layout-options [frame-id options-fn]
  (mapv (fn [{:keys [name id]}]
          (to-option id name))
        (val-or-deref (options-fn frame-id))))

(defn color-scale-options [{:keys [add-layout-options frame-id]}]
  (let [labels @(fi/call-api [:i18n :get-labels-sub])]
    (mapv (fn [{:keys [id attributes value-assigned]
                {:keys [colors]} :color-scheme
                :as layout}]
            (let [attr-labels (map #(attr->display-name % labels) attributes)
                  tooltip-label (join ", " attr-labels)
                  colors-with-value (reduce
                                     (fn [res [k v]]
                                       (let [val (get value-assigned (js/parseInt (name k)))]
                                         (if (and (vector? val)
                                                  (not-empty val))
                                           (assoc res k v)
                                           res)))
                                     {}
                                     colors)]
              {:label [gen-add-label layout attr-labels colors-with-value]
               :tooltip (gen-add-tooltip attributes tooltip-label)
               :value id}))
          (sort-by (fn [{:keys [name default?]}]
                     (lower-case (trim name)))
                   (val-or-deref (add-layout-options frame-id))))))

(defn- layout-selection [{:keys [frame-id read-only? disabled? on-change selected-layouts add-layout-options edit-layout-desc on-maximize] :as props} {:keys [id name temporary?] :as layout-desc}]
  (let [read-only? (val-or-deref read-only?)
        temporary-name @(subscribe [::i18n/translate :temporary-layout-name])]
    [:div.w-full.min-w-0
     [select {:values {:label (if temporary? temporary-name name) :value id}
              :options (color-scale-options props)
              :tooltip-key :tooltip
              :disabled? (or read-only?
                             (when disabled?
                               (val-or-deref (disabled? frame-id))))
              :is-multi? false
              :is-clearable? false
              :menu-row-height 40
              :menu-min-width 250
              :on-change (fn [{:keys [value]}]
                           (let [selected-layouts (val-or-deref (selected-layouts frame-id))
                                 selected-layouts (filterv #(not= id (:id %))
                                                           selected-layouts)
                                 layout-desc (some (fn [{:keys [id] :as layout-desc}]
                                                     (when (= id value)
                                                       layout-desc))
                                                   (val-or-deref (add-layout-options frame-id)))]
                             (on-change frame-id
                                        (conj (or selected-layouts [])
                                              layout-desc))))}]]))

(defn- swap-items [a-vec elem-idx delta]
  (let [i elem-idx
        j (+ i delta)]
    (if (and (< -1 i)
             (< -1 j (count a-vec)))
      (assoc a-vec i (a-vec j) j (a-vec i))
      a-vec)))

(defn- layout-shifters [{:keys [frame-id selected-layouts on-change]}
                        {layout-id :id}
                        last-index current-index read-only?]
  (let [change-fn (fn [delta]
                    (let [selected-layouts (val-or-deref (selected-layouts frame-id))]
                      (on-change frame-id
                                 (swap-items selected-layouts current-index delta))))]

    [:<>
     [:div.draggable__controls.no-drag
      [:button.draggable__up {:on-click (partial change-fn -1)
                              :disabled (or (zero? current-index) read-only?)}
       [:span]]
      [:span>span]
      [:button.draggable__down {:on-click (partial change-fn 1)
                                :disabled (or (= last-index current-index) read-only?)}
       [:span]]]]))

(defn- layout-actions [{:keys [frame-id read-only? disabled?
                               on-change selected-layouts
                               on-maximize single?]
                        :as props}
                       {:keys [id] :as layout-desc}]
  (let [read-only? (val-or-deref read-only?)
        disabled? (when disabled?
                    (val-or-deref (disabled? frame-id)))]
    [:div.flex
     [button {:start-icon :edit
              :label (subscribe [::i18n/translate :edit-label])
              :aria-label :edit-label
              :variant :tertiary
              :size :small
              :on-click #(on-maximize layout-desc)
              :disabled? (or read-only? disabled?)}]
     [button {:start-icon :close
              :aria-label :close
              :variant :tertiary
              :size :small
              :on-click (fn []
                          (let [selected-layouts (val-or-deref (selected-layouts frame-id))]
                            (on-change frame-id
                                       (filterv #(not= id (:id %))
                                                selected-layouts))))
              :disabled? (or read-only? disabled?)}]]))

(defn- layout-comp [{:keys [read-only? single? show-actions?] :as props}
                    last-index current-index layout-desc]
  (let [{:keys [id name attributes temporary?]} layout-desc
        read-only? (val-or-deref read-only?)
        temporary-name @(subscribe [::i18n/translate :temporary-layout-name])
        labels @(fi/call-api [:i18n :get-labels-sub])]
    [:div.subsection__element
     (when (and show-actions?
                (not single?))
       [layout-shifters props layout-desc last-index current-index read-only?])
     [:div.draggable__content
      [:div.subsection__element__title
       (if (and single? show-actions?)
         [layout-selection props layout-desc]
         [:div.truncate-text (if temporary? temporary-name name)])
       (when show-actions?
         [layout-actions props layout-desc])]

      [:dl>div
       [:dd.truncate-text (join ", " (map #(attr->display-name % labels) attributes))]]
      [color-code-list props layout-desc]]]))

(defn- calc-value-range [value-assigned]
  (let [values (flatten value-assigned)]
    [(apply min values)
     (apply max values)]))

(defn- add-layout [{:keys [frame-id selected-layouts add-layout-options show-actions?
                           add-layout? read-only? disabled? on-change single?]
                    :as props}]
  (when (and
         show-actions?
         (or (not single?)
             (and single?
                  (empty? (val-or-deref (selected-layouts frame-id))))))
    (let [color-scale-options (color-scale-options props)]
      [:div.add-layout
       [:<>
        [button {:on-click #(reset! add-layout? true)
                 :label @(subscribe [::i18n/translate :add-layout-label])
                 :start-icon :plus
                 :size :big
                 :variant :secondary
                 :disabled? (or (val-or-deref read-only?)
                                (when disabled?
                                  (val-or-deref (disabled? frame-id))))}]]
       (when @add-layout?
         [:div.menu__overlay--mosaic
          (let [close-select #(reset! add-layout? false)]
            [select {:on-change (fn [{:keys [value]}]
                                  (when-let [layout-desc (some (fn [{:keys [id] :as layout-desc}]
                                                                 (when (= id value)
                                                                   layout-desc))
                                                               (val-or-deref (add-layout-options frame-id)))]
                                    (on-change frame-id
                                               (conj (or (val-or-deref (selected-layouts frame-id))
                                                         [])
                                                     layout-desc))))
                     :placeholder @(subscribe [::i18n/translate :add-layout-label])
                     :disabled? (or (not read-only?)
                                    (when disabled?
                                      (val-or-deref (disabled? frame-id))))
                     :autofocus? true
                     :on-blur close-select
                     :is-multi? false
                     :is-clearable? false
                     :menu-row-height 50
                     :options color-scale-options ;(layout-options frame-id add-layout-options)
                     :values {}
                     :tooltip-key :tooltip}])])])))

(defn- overview-view [{:keys [frame-id selected-layouts show-actions? on-maximize] :as props}]
  (let [selected-layouts (val-or-deref (selected-layouts frame-id))
        max-idx (dec (count selected-layouts))
        props (assoc props :show-actions? false)]
    [:div.panel__subsection.layouts
     [:div.subsection__title
      "Layouts"
      (when (val-or-deref show-actions?)
        [button {:start-icon :edit
                 :label (subscribe [::i18n/translate :edit-label])
                 :aria-label :edit-label
                 :variant :tertiary
                 :size :small
                 :extra-class export-ignore-class
                 :on-click #(on-maximize {:overview? true})}])]
     (into [:div.subsection__content]
           (map-indexed (fn [idx layout]
                          ^{:key (str ::selected-layout frame-id idx)}
                          [layout-comp
                           props
                           max-idx
                           idx layout])
                        selected-layouts))]))

(defn- overview-edit [{:keys [frame-id selected-layouts single? on-normalize]
                       :as props}]
  (let [selected-layouts (val-or-deref (selected-layouts frame-id))
        max-idx (dec (count selected-layouts))]
    [:div.panel__section
     [:div.panel__header
      [button {:on-click #(on-normalize)
               :label @(subscribe [::i18n/translate :back-label])
               :start-icon :previous
               :size :big
               :variant :back}]]
     [:div.section__title
      "Layouts"]
     [:div.section__content>div.panel__subsection {:class (when-not single? "draggable-content")}
      [:div.subsection__content
       (map-indexed (fn [idx layout]
                      ^{:key (str ::edit-selected-layout frame-id idx)}
                      [layout-comp
                       props
                       max-idx
                       idx layout])
                    selected-layouts)
       [add-layout props]]]]))

(defn- handle-layout-change [edit-layout-desc path new-val]
  (if (fn? path)
    (swap! edit-layout-desc path)
    (swap! edit-layout-desc
           (if (vector? path)
             assoc-in
             assoc)
           path new-val)))

(defn- replace-layout [layouts match-id new-desc]
  (mapv (fn [{:keys [id] :as l}]
          (if (= id match-id)
            new-desc
            l))
        layouts))

(defn- config-type [layout-desc]
  (let [layout (val-or-deref layout-desc)]
    (if (or (get layout :layer-type)
            (get layout :grouping-attribute))
      :overlayers
      :layouts)))

(defn- editing-footer [{:keys [on-change frame-id maximized-data selected-layouts
                               on-normalize read-only? save-dialog?]
                        :as props}]
  (let [{:keys [save-title apply-for-frame-label]}
        @(subscribe [::i18n/translate-multi
                     :save-title
                     :apply-for-frame-label])
        has-errors?  @(subscribe [::data/layout-error-status frame-id])
        read-only? (val-or-deref read-only?)
        selected-layouts (val-or-deref (selected-layouts frame-id))
        {:keys [id] :as layout-desc} (val-or-deref maximized-data)
        type (config-type layout-desc)
        valid? (cond (= type :overlayers) (is-overlayer-valid? layout-desc)
                     (= type :layouts) (is-layout-valid? layout-desc)
                     :else false)
        changed? (some (fn [{compare-id :id :as compare-layout-desc}]
                         (when (= id compare-id)
                           (not= layout-desc compare-layout-desc)))
                       selected-layouts)
        can-save? (and valid?
                       (not read-only?)
                       (not has-errors?))
        can-apply? (and can-save?
                        changed?)]
    [:div.panel__footer
     [button {:label apply-for-frame-label
              :disabled? (not can-apply?)
              :start-icon :brush
              :on-click #(let [config-desc (-> (val-or-deref maximized-data)
                                               (dissoc :default? :datasources))
                               config-id (:id config-desc)
                               new-config-id (str (random-uuid))
                               new-config-desc (assoc config-desc
                                                      :name (cond-> (:name config-desc)
                                                              (not (:temporary? config-desc))
                                                              (str " (temp)"))
                                                      :id new-config-id
                                                      :temporary? true)]
                           (reset! maximized-data new-config-desc)
                           (on-change frame-id (replace-layout selected-layouts
                                                               config-id
                                                               new-config-desc)))}]
                          ;;  (on-normalize))}]
     [button {:label save-title
              :start-icon :save
              :disabled? (not can-save?)
              :on-click #(reset! save-dialog? true)}]]))

(defn- editing-view-imp [{:keys [on-change frame-id selected-layouts
                                 maximized-data on-normalize read-only? data-acs-path]
                          :as props}
                         view-fn]
  (let [{:keys [name default? temporary?] layout-id :id :as mdata} (val-or-deref maximized-data)
        data-acs-path (when (fn? data-acs-path)
                        (data-acs-path frame-id))]
    [:<>
     [:div.panel__header
      [button {:on-click #(on-normalize)
               :label @(subscribe [::i18n/translate :back-label])
               :start-icon :previous
               :size :big
               :variant :back}]]
     [:div.panel__scroll__container
      [layout-overlayer-save-dialog (assoc props
                                           :on-close (fn [{updated-id :id :as updated-desc} old-id]
                                                       (on-normalize)
                                                       (when (and updated-id updated-desc frame-id)
                                                         (on-change frame-id
                                                                    (mapv (fn [{:keys [id] :as old-desc}]
                                                                            ;;old-id is set, when some layout will be overwritten
                                                                            (if (= id (or old-id updated-id))
                                                                              updated-desc
                                                                              old-desc))
                                                                          (or (val-or-deref (selected-layouts frame-id))
                                                                              [])))))
                                           :replace-fn #(on-change frame-id
                                                                   (replace-layout (val-or-deref (selected-layouts frame-id)) %1 %2))
                                           :edit-layout-desc maximized-data
                                           :config-type-fn config-type
                                           :handle-layout-change handle-layout-change)]
      [view-fn {:naming? false
                :sharing? false
                :default-open #{:coloring}
                :layout maximized-data
                :translate (fn [key]
                             (subscribe [::i18n/translate key]))
                :translate-multi (fn [& keys]
                                   (subscribe (into [::i18n/translate-multi] keys)))
                :on-unmount (fn []
                              (dispatch [::data/clear-characteristics frame-id]))
                :color-scales (fi/call-api [:config :get-config-sub]
                                           :color-scales)
                :request-characteristics (fn [attributes]
                                           (dispatch [::data/request-characteristics frame-id attributes]))
                :ac-attribute-types (fn [context]
                                      (subscribe [::data/attr-types data-acs-path context]))
                :ac-vals (fn [attributes filter-vals]
                           (subscribe [::data/acs data-acs-path attributes filter-vals]))
                :on-change-layout (fn [path new-val]
                                    (handle-layout-change maximized-data path new-val))
                :error-status-callback (fn [status]
                                         (dispatch [::data/set-layout-error-status frame-id status]))}]]
     [editing-footer props]]))

(defn editing-view [_ _]
  (let [save-dialog? (r/atom nil)
        can-edit-save-dialog? (r/atom true)]
    (r/create-class
     {:display-name "editing-view"
      :reagent-render (fn [props view-fn]
                        [editing-view-imp
                         (assoc props
                                :save-dialog? save-dialog?
                                :can-edit-save-dialog? can-edit-save-dialog?)
                         view-fn])})))


(defn picker [_]
  (let [add-layout? (r/atom nil)]
    (r/create-class
     {:display-name "layout-picker"
      :reagent-render
      (fn [props]
        (let [{:keys [maximized-data] :as props}
              (assoc props :add-layout? add-layout?)
              {:keys [overview?] :as maximized-data} (val-or-deref maximized-data)]
          (cond
            overview? [overview-edit props]
            maximized-data [editing-view props layout-editing/editing-view]
            :else [overview-view props])))})))