(ns de.explorama.frontend.reporting.views.dashboards.template-builder
  (:require [re-frame.core :refer [dispatch subscribe reg-event-db]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field button]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position]]
            [de.explorama.shared.reporting.description-types]
            [de.explorama.frontend.reporting.views.dashboards.template-schema :refer [schema-modules-grid]]
            [de.explorama.frontend.reporting.views.dropzone :as dropzone]
            [de.explorama.frontend.reporting.views.context-menu :as menu]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.data.templates :as templates]
            [reagent.core :as r]
            [de.explorama.frontend.common.i18n :as i18n]))

(reg-event-db
 ::select-template
 (fn [db [_ {:keys [id tiles] :as template}]]
   (let [tiles-count (count tiles)]
     (-> db
         (assoc-in dr-path/creation-template-id id)
         (assoc-in dr-path/creation-selected-template template)
         (assoc-in dr-path/creation-modules (mapv (fn [_]
                                                    {})
                                                  (range 0 tiles-count)))))))


(def dashboard-item-class "dashboard__item")
(defn- tile-content [tile-idx _ _]
  (let [is-dropzone-active-sub (dropzone/tile-dropzone-sub tile-idx)
        tile-dom-id (dr-path/tile-idx->dom-id tile-idx)]
    (r/create-class
     {:display-name "tile-content"
      :component-will-unmount (fn []
                                (dropzone/clear-dropzone-state tile-idx))
      :reagent-render (fn [tile-idx {:keys [tile-classes]} {:keys [drop-on-tile-props disabled?]}]
                        (let [{:keys [title preview loading context-menu]} @(subscribe [:de.explorama.frontend.reporting.views.builder/tile-data tile-idx])
                              {:keys [drag-hint reporting-placeholder-short-label]}
                              @(subscribe [::i18n/translate-multi :drag-hint :reporting-placeholder-short-label])
                              is-dropzone-active? @is-dropzone-active-sub]
                          [:div {:class (conj tile-classes dashboard-item-class)
                                 :id tile-dom-id}

                           (if (not (or preview title loading))
                             [:div {:class ["drag-drop-area" "drag-drop-area--empty"
                                            (when is-dropzone-active? "drop-target")]}
                              [:span drag-hint]]
                             [:<>
                              (when is-dropzone-active?
                                [:div {:class ["drag-drop-area" "drag-drop-area--empty" "drop-target"]}
                                 [:span reporting-placeholder-short-label]])
                              [:div.title
                               [:div.explorama__form__textarea
                                [input-field {:value title
                                              :disabled? disabled?
                                              :extra-class "mosaic__box__title__input"
                                              :on-change #(dispatch [:de.explorama.frontend.reporting.views.builder/tile-title-change tile-idx %])}]]
                               [:div.options
                                (when context-menu
                                  [button {:start-icon :burgermenu
                                           :disabled? disabled?
                                           :on-click #(dispatch [::menu/set-module-menu
                                                                 tile-idx
                                                                 (calc-menu-position %)])}])
                                [button {:start-icon :close
                                         :disabled? disabled?
                                         :on-click #(do
                                                      (dispatch [:de.explorama.frontend.reporting.views.builder/reset-tile tile-idx])
                                                      (dropzone/clear-dropzone-state tile-idx))}]]]

                              [:img {:src preview
                                     :on-context-menu #(.preventDefault %)
                                     :on-drag-start #(.preventDefault %)
                                     :style {:width "100%"
                                             :height "calc(100% - 35px)"
                                             :object-fit :contain}}]])]))})))

(defn- template-selection-render [{:keys [register-tiles unregister-tiles]} _ _]
  (let [templates @(subscribe [::templates/all-templates])
        template-sort (fn [templates] (sort-by #(count (get % :tiles)) templates))
        template-selection-title @(subscribe [::i18n/translate :template-selection-label])]
    (r/create-class
     {:display-name "dashboard template-selection"
      :component-did-mount (fn [this argv]
                             (let [[_ _ _ selected-template] (r/argv this)
                                   template-dom-ids (dr-path/template->dom-ids selected-template)]
                               (when template-dom-ids
                                 (register-tiles template-dom-ids))))
      :component-did-update (fn [this argv]
                              (let [[_ _ _ {old-selected-template-id :id}] argv
                                    [_ _ _ {selected-template-id :id :as selected-template}] (r/argv this)]
                                (when (and
                                       selected-template-id
                                       (not= old-selected-template-id
                                             selected-template-id))
                                  (let [template-dom-ids (dr-path/template->dom-ids selected-template)]
                                    (register-tiles template-dom-ids))
                                  true)))
      :component-will-unmount (fn []
                                (dropzone/clear-dropzone-state)
                                (when (fn? unregister-tiles)
                                  (unregister-tiles)))
      :reagent-render (fn [_ save-pending? {selected-template-id :id}]
                        [:<>
                         [:h3 template-selection-title]
                         (reduce (fn [acc {:keys [id] :as template}]
                                   (conj acc
                                         [:li (cond-> {:on-click #(when-not save-pending?
                                                                    (dispatch [::select-template template])
                                                                    (dropzone/clear-dropzone-state))}
                                                (= id selected-template-id) (assoc :class "selected"))
                                          [schema-modules-grid {:template-id id}]]))
                                 [:ul.select-layout]
                                 (template-sort (vals templates)))])})))

(defn- template-selection [sidebar-props save-pending?]
  (let [selected-template @(subscribe [:de.explorama.frontend.reporting.views.builder/selected-template])]
    [template-selection-render sidebar-props save-pending? selected-template]))

(defn- fill-template-grid [{template-id :id :as t} save-pending?]
  (let [{:keys [grid tiles]} @(subscribe [::templates/template template-id])
        [gw gh] grid]
    (reduce (fn [acc [tile-idx {[x y] :position
                                [w h] :size :as tile}]]
              (let [x (inc x)
                    y (inc y)
                    tile-classes [(str "x" x)
                                  (str "y" y)
                                  (str "w" w)
                                  (str "h" h)]]
                (conj acc
                      [tile-content
                       tile-idx
                       (assoc tile :tile-classes tile-classes)
                       {:disabled? save-pending?}])))
            [:div.dashboard__layout (cond-> {:class [(str "c" gw)
                                                     (str "r" gh)]})]
            (map-indexed (fn [idx itm] [idx itm]) tiles))))

(defn- fill-template [sidebar-props save-pending?]
  (let [selected-template @(subscribe [:de.explorama.frontend.reporting.views.builder/selected-template])
        fill-label @(subscribe [::i18n/translate :dashboard-label])
        title-label @(subscribe [::i18n/translate :title-label])
        subtitle-label @(subscribe [::i18n/translate :subtitle-label])
        title-exists? @(subscribe [:de.explorama.frontend.reporting.views.builder/name-exists?])
        caption (when title-exists?
                  @(subscribe [::i18n/translate :dashboard-name-exists]))]
    (when selected-template
      [:<>
       [:h3 fill-label]
       [:div.dashboard__container.in-app
        [input-field {:label title-label
                      :caption caption
                      :invalid? (boolean caption)
                      :disabled? save-pending?
                      :value (subscribe [:de.explorama.frontend.reporting.views.builder/name])
                      :on-change #(dispatch [:de.explorama.frontend.reporting.views.builder/name %])}]
        [fill-template-grid selected-template save-pending?]
        [input-field {:label subtitle-label
                      :disabled? save-pending?
                      :value (subscribe [:de.explorama.frontend.reporting.views.builder/subtitle])
                      :on-change #(dispatch [:de.explorama.frontend.reporting.views.builder/subtitle %])}]]])))

(defn template-builder [sidebar-props save-pending?]
  [:<>
   [template-selection sidebar-props save-pending?]
   [fill-template sidebar-props save-pending?]])