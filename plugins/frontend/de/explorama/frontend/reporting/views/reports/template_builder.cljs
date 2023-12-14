(ns de.explorama.frontend.reporting.views.reports.template-builder
  (:require [re-frame.core :refer [dispatch subscribe reg-event-db]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position]]
            [de.explorama.shared.reporting.description-types]
            [de.explorama.frontend.reporting.views.dropzone :as dropzone]
            [de.explorama.frontend.reporting.views.context-menu :as menu]
            [de.explorama.frontend.reporting.views.text-module :as text]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [reagent.core :as r]
            [de.explorama.frontend.common.i18n :as i18n]))

(def report-base-template {:id "report-base"
                           :type :report
                           :grid [2 1]
                           :tiles [{:position [0 0]
                                    :size [2 1]
                                    :legend-position :right}]})

(reg-event-db
 ::set-text-module
 (fn [db [_ tile-idx]]
   (assoc-in db (dr-path/creation-module-desc tile-idx) {:tool "text"})))

(defn- tile-header [tile-idx title edit-title? context-menu show-dropzone-state disabled?]
  [:div.title
   (when edit-title?
     [:div.explorama__form__textarea
      [input-field {:value title
                    :disabled? disabled?
                    :extra-class "mosaic__box__title__input"
                    :on-change #(dispatch [:de.explorama.frontend.reporting.views.builder/tile-title-change tile-idx %])}]])
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
                          (reset! show-dropzone-state false))}]]])

(def report-item-class "report__element")

(reg-event-db
 ::add-template-row
 (fn [db [_ columns column-width register-tiles]]
   (let [row-index (get-in db (conj dr-path/creation-selected-template :grid 1))
         new-tiles (for [pos (range columns)]
                     {:position [pos row-index]
                      :size [column-width 1]
                      :legend-position (if (= columns 1)
                                         :right
                                         :bottom)})
         new-db (-> db
                    (assoc-in  dr-path/creation-selected-text-module nil)
                    (update-in dr-path/creation-modules #(apply conj % (repeat columns {})))
                    (update-in dr-path/creation-selected-template
                               (fn [template]
                                 (-> template
                                     (update-in [:grid 1] inc)
                                     (update-in [:tiles] #(apply conj % new-tiles))))))]
     (when (fn? register-tiles)
       (register-tiles (dr-path/template->dom-ids (get-in new-db dr-path/creation-selected-template))))

     new-db)))


(defn calc-tile-indices-per-row [tiles total-row-num]
  (let [indexed-tiles  (map-indexed vector tiles)
        row-to-indexed-tiles  (group-by (fn [[_ tile]] (get-in tile [:position 1])) indexed-tiles)
        indices-per-row (map (fn [row-num]
                               (map (fn [[idx _]] idx)
                                    (get row-to-indexed-tiles row-num)))
                             (range total-row-num))]
    indices-per-row))


(reg-event-db
 ::move-row-up-or-down
 (fn [db [_ column-id direction register-tiles]]
   (let [total-row-num (get-in db (conj dr-path/creation-selected-template :grid 1))
         modules (get-in db dr-path/creation-modules)
         tiles (get-in db (conj dr-path/creation-selected-template :tiles))
         tile-indices-per-row (calc-tile-indices-per-row tiles total-row-num)
         swap-row-index-1 (if (= direction :up)
                            (dec column-id)
                            column-id)
         swap-row-index-2 (inc swap-row-index-1)]
     (if (or (< swap-row-index-1 0)
             (>= swap-row-index-2 total-row-num))
       db
       (let [update-row-tiles (fn [row-tiles new-y-pos]
                                (map (fn [tile]
                                       (assoc-in tile [:position 1]  new-y-pos))
                                     row-tiles))
             swap-1-elem-indices (nth tile-indices-per-row swap-row-index-1)
             swap-2-elem-indices (nth tile-indices-per-row swap-row-index-2)
             up-to-tile-index  (apply min swap-1-elem-indices)
             tile-after-index (inc (apply max swap-2-elem-indices))
             new-tiles (into [] (concat (take  up-to-tile-index tiles)
                                        (update-row-tiles
                                         (map #(get tiles %) swap-2-elem-indices)
                                         swap-row-index-1)
                                        (update-row-tiles
                                         (map #(get tiles %) swap-1-elem-indices)
                                         swap-row-index-2)
                                        (drop tile-after-index tiles)))
             new-modules (into [] (concat (take up-to-tile-index modules)
                                          (map #(get modules %) swap-2-elem-indices)
                                          (map #(get modules %) swap-1-elem-indices)
                                          (drop tile-after-index modules)))
             new-db (-> db
                        (assoc-in  dr-path/creation-selected-text-module nil)
                        (assoc-in  dr-path/creation-modules new-modules)
                        (assoc-in (conj dr-path/creation-selected-template :tiles)
                                  new-tiles))]
         new-db)))))

(reg-event-db
 ::remove-template-row
 (fn [db [_ column-id {:keys [register-tiles unregister-tiles]}]]
   (let [{:keys [remaining-modules remaining-tiles]}
         (reduce
          (fn [res [module tile]]
            (let [column-pos (get-in tile [:position 1])
                  new-tile (update-in tile [:position 1]
                                      #(if (< column-id %) (dec %) %))]
              (if (= column-pos column-id)
                res
                (-> res
                    (update-in [:remaining-modules]
                               #(conj % module))
                    (update-in [:remaining-tiles]
                               #(conj % new-tile))))))
          {:remaining-modules []
           :remaining-tiles []}
          (map vector
               (get-in db dr-path/creation-modules)
               (get-in db (conj dr-path/creation-selected-template :tiles))))
         new-db (-> db
                    (assoc-in  dr-path/creation-selected-text-module nil)
                    (assoc-in dr-path/creation-modules remaining-modules)
                    (update-in dr-path/creation-selected-template
                               (fn [template]
                                 (-> template
                                     (update-in [:grid 1] dec)
                                     (assoc :tiles remaining-tiles)))))

         {:keys [tiles] :as sel-template} (get-in new-db dr-path/creation-selected-template)]

     (when (and (fn? register-tiles)
                (fn? unregister-tiles))
       (if (seq tiles)
         (register-tiles (dr-path/template->dom-ids sel-template))
         (unregister-tiles)))
     new-db)))

(defn- tile-content [tile-idx _ _]
  (let [show-dropzone-state (r/atom nil)
        is-dropzone-active-sub (dropzone/tile-dropzone-sub tile-idx)
        tile-dom-id (dr-path/tile-idx->dom-id tile-idx)]
    (r/create-class
     {:display-name "tile-content"
      :component-will-unmount (fn []
                                (dropzone/clear-dropzone-state tile-idx))
      :reagent-render
      (fn [tile-idx {:keys [tile-classes]} {:keys [drop-on-tile-props disabled?]}]
        (let [{:keys [tool title preview loading state context-menu]} @(subscribe [:de.explorama.frontend.reporting.views.builder/tile-data tile-idx])
              drag-hint @(subscribe [::i18n/translate :drag-hint])
              {:keys [reporting-placeholder-short-label report-placeholder-1-label report-placeholder-2-label report-placeholder-3-label]}
              @(subscribe [::i18n/translate-multi :reporting-placeholder-short-label :report-placeholder-1-label :report-placeholder-2-label :report-placeholder-3-label])
              show-dropzone? @is-dropzone-active-sub]
          [:div {:id tile-dom-id
                 :class [report-item-class (when show-dropzone? "active") (when-not tool "empty")]
                 :on-drop #(when-not disabled?
                             (reset! show-dropzone-state false))
                 :on-drag-enter #(reset! show-dropzone-state true)
                 :on-drag-leave (fn [e]
                                  (when-not (boolean (and
                                                      (aget e "nativeEvent" "fromElement")
                                                      (-> (aget e "nativeEvent" "fromElement")
                                                          (.closest (str "." report-item-class)))))
                                    (reset! show-dropzone-state false)))}
           (cond
             (= tool "text")
             [:<>
              (when show-dropzone?
                [:div {:class ["drag-drop-area" "drag-drop-area--empty" "drop-target"]}
                 [:span reporting-placeholder-short-label]])
              [text/text-module tile-idx state (nil? state)]]

             (not (or preview title loading))
             (if show-dropzone?
               [:div {:class ["drag-drop-area" "drag-drop-area--empty" "drop-target"]}
                [:span drag-hint]]
               [:div.placeholder {:on-drag-enter #(reset! show-dropzone-state true)
                                  :on-click #(dispatch [::set-text-module tile-idx])
                                  :style {:height "100%"
                                          :width "100%"}} ;TODO r1/css create a class - or use one
                report-placeholder-1-label
                [:span.divider {:on-drag-enter #(reset! show-dropzone-state true)}
                 report-placeholder-2-label]
                report-placeholder-3-label])

             :else
             [:<>
              (when show-dropzone?
                [:div.explorama__form__file-upload.overlay.active
                 [:span.drop-text reporting-placeholder-short-label]])
              [tile-header tile-idx title true context-menu show-dropzone-state disabled?]
              [:img {:src preview
                     :on-context-menu #(.preventDefault %)
                     :on-drag-start #(.preventDefault %)
                     :style {:width "100%"
                             :height "calc(100% - 35px)"
                             :object-fit :contain}}]])]))})))

(defn- grid [_ _ _]
  (let [id-focus-state (r/atom nil)]
    (fn [{:keys [grid tiles]}
         {:keys [tile-content-props]}
         rows-removeable?]
      [:<>
       (let [[_ gh] grid
             id-focus @id-focus-state
             row-tiles (group-by #(get-in % [:position 1])
                                 (map-indexed (fn [idx itm] (assoc itm :id idx)) tiles))]
         (for [row-num (range gh)]
           ^{:key (str "reporting-report-row-" row-num)}
           [:div.report__row {:class (when (= row-num id-focus) "focused")}
            (when rows-removeable?
              [:div.report__row__reorder
               [button
                {:start-icon :arrow-up
                 :extra-class "order__up"
                 :on-click #(dispatch [::move-row-up-or-down row-num :up (:register-tiles (:sidebar-props tile-content-props))])}]
               [button
                {:start-icon :arrow-down
                 :extra-class "order__down"
                 :on-click #(dispatch [::move-row-up-or-down row-num :down (:register-tiles (:sidebar-props tile-content-props))])}]])
            (let [row-num-items (count (get row-tiles row-num))]
              (for [{:keys [id] :as tile} (get row-tiles row-num)]
                (when tile-content
                  ^{:key (str "reporting-report-row-tile-" id)}
                  [tile-content
                   id
                   (assoc tile :row-num-items row-num-items)
                   tile-content-props])))
            (when rows-removeable?
              [:div.report__row__remove {:on-mouse-enter #(reset! id-focus-state row-num)
                                         :on-mouse-leave #(reset! id-focus-state nil)}
               [button {:start-icon :trash
                        :on-click #(dispatch [::remove-template-row row-num (:sidebar-props tile-content-props)])}]])]))])))


(defn template-builder [{:keys [register-tiles] :as sidebar-props} save-pending?]
  (let [title-label @(subscribe [::i18n/translate :title-label])
        selected-template @(subscribe [:de.explorama.frontend.reporting.views.builder/selected-template])
        add-row-label @(subscribe [::i18n/translate :add-row])
        subtitle-label @(subscribe [::i18n/translate :subtitle-label])
        title-exists? @(subscribe [:de.explorama.frontend.reporting.views.builder/name-exists?])
        caption (when title-exists?
                  @(subscribe [::i18n/translate :report-name-exists]))]
    [:div.report__container.in-app
     [input-field {:label title-label
                   :disabled? save-pending?
                   :caption caption
                   :invalid? (boolean caption)
                   :value (subscribe [:de.explorama.frontend.reporting.views.builder/name])
                   :on-change #(dispatch [:de.explorama.frontend.reporting.views.builder/name %])}]
     [input-field {:label subtitle-label
                   :disabled? save-pending?
                   :value (subscribe [:de.explorama.frontend.reporting.views.builder/subtitle])
                   :on-change #(dispatch [:de.explorama.frontend.reporting.views.builder/subtitle %])}]
     [grid
      selected-template
      {:tile-content-props {:sidebar-props sidebar-props
                            :disabled? save-pending?}}]
     [:div.report__add-row {:on-click (fn [e]
                                        (dropzone/clear-dropzone-state)
                                        (dispatch [::menu/set-row-menu
                                                   (calc-menu-position e)
                                                   register-tiles]))}
      [icon {:icon :plus}]
      add-row-label]]))