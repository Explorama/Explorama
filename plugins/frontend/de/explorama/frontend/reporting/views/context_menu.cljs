(ns de.explorama.frontend.reporting.views.context-menu
  (:require [re-frame.core :refer [dispatch subscribe reg-sub reg-event-db reg-event-fx]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [checkbox]]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.common.i18n :as i18n]))

; ----------- Module Builder Menu ------------

(reg-sub
 ::menu-tile
 (fn [db]
   (get-in db dr-path/menu-tile)))

(reg-sub
 ::get-position
 (fn [db]
   (get-in db dr-path/menu-position)))

(reg-event-db
 ::set-module-menu
 (fn [db [_ tile-idx position]]
   (let [old-tile-idx (get-in db dr-path/menu-tile)]
     (if old-tile-idx
       (assoc-in db dr-path/context-menu nil)
       (-> db
           (assoc-in dr-path/menu-position position)
           (assoc-in dr-path/menu-tile tile-idx)
           (assoc-in dr-path/menu-type :module-options))))))

(reg-event-db
 ::show-legend
 (fn [db [_ tile-idx]]
   (update-in db (dr-path/show-legend? tile-idx) not)))

(reg-sub
 ::show-legend?
 (fn [db [_ tile-idx]]
   (get-in db (dr-path/show-legend? tile-idx) false)))

(reg-event-db
 ::change-legend-position
 (fn [db [_ tile-idx bool]]
   (assoc-in db (dr-path/legend-left? tile-idx) bool)))

(reg-sub
 ::legend-left?
 (fn [db [_ tile-idx]]
   (get-in db (dr-path/legend-left? tile-idx) false)))

(defn- handle-click [in-menu? menu-comp hide-fn e]
  (when (and (not @in-menu?)
             @menu-comp
             (fn? hide-fn)
             (not (.contains @menu-comp (aget e "target"))))
    (hide-fn)))

(defn- legend-options [tile-idx]
  (let [legend-right? @(subscribe [::legend-left? tile-idx])
        show-legend-label @(subscribe [::i18n/translate :show-legend-label])
        legend-pos-label @(subscribe [::i18n/translate :legend-pos-label])
        bottom-label @(subscribe [::i18n/translate :bottom-label])
        right-label @(subscribe [::i18n/translate :right-label])]
    [:div.legend__options
     [:div
      [checkbox {:id "show-legend-dr-module"
                 :checked? @(subscribe [::show-legend? tile-idx])
                 :label show-legend-label
                 :on-change #(dispatch [::show-legend tile-idx])}]]
     [:div.explorama__form__select
      [:label legend-pos-label]
      [:ul.select__button__group
       [:li {:class (when-not legend-right? "active")}
        [:a {:href "#"
             :on-click #(dispatch [::change-legend-position tile-idx false])}
         bottom-label]]
       [:li {:class (when legend-right? "active")}
        [:a {:href "#"
             :on-click #(dispatch [::change-legend-position tile-idx true])}
         right-label]]]]]))

(reg-event-db
 ::select-option
 (fn [db [_ tile-idx option overwrite?]]
   (update-in db (dr-path/options-selection tile-idx) #(if overwrite?
                                                         option
                                                         (or % option)))))

(reg-sub
 ::selected-option
 (fn [db [_ tile-idx]]
   (get-in db (dr-path/options-selection tile-idx))))

(reg-sub
 ::register-fn
 (fn [db]
   (get-in db dr-path/register-fns)))

(defn- options-list [tile-idx options title]
  (let [active-option @(subscribe [::selected-option tile-idx])]
    [:<>
     [:h3 title]
     [:ul.options__list
      (for [{:keys [label value]} options]
        ^{:key (str "reporting-dr-context-menu-option" value)}
        [:li {:class (when (= value active-option) "active")
              :on-click #(dispatch [::select-option tile-idx value true])}
         [:span]
         label])]]))

(defn- module-menu []
  (let [tile-idx @(subscribe [::menu-tile])
        menu-comp (atom nil)
        in-menu? (atom false)
        hide-fn #(dispatch [::set-module-menu])
        check-hide-click (partial handle-click in-menu? menu-comp hide-fn)
        {:keys [context-menu]} @(subscribe [:de.explorama.frontend.reporting.views.builder/tile-data tile-idx])
        {:keys [legend? options options-title]} context-menu]
    (r/create-class
     {:component-did-mount #(do
                              (dispatch [::select-option tile-idx (get-in options [0 :value]) false])
                              (reset! menu-comp (rdom/dom-node %))
                              (js/document.addEventListener "mousedown" check-hide-click))
      :component-will-unmount #(js/document.removeEventListener "mousedown" check-hide-click)

      :reagent-render
      (fn []
        (let [{:keys [top left]} @(subscribe [::get-position])]
          [:div.menu__overlay {:on-mouse-enter #(reset! in-menu? true)
                               :on-mouse-leave #(reset! in-menu? false)
                               :style {:position "absolute"
                                       :top (- top 50)
                                       :left (- left 200)}}
           (when legend? [legend-options tile-idx])
           (when options [options-list tile-idx options options-title])]))})))

; ----------- Add Row Report Builder Menu ------------

(reg-event-db
 ::set-row-menu
 (fn [db [_ position register-tiles-fn]]
   (-> db
       (assoc-in dr-path/menu-position position)
       (assoc-in dr-path/register-fns register-tiles-fn)
       (assoc-in dr-path/menu-type :row-options))))

(defn- row-menu []
  (let [{:keys [single-label two-columns-label]}
        @(subscribe [::i18n/translate-multi :single-label :two-columns-label])
        menu-comp (atom nil)
        in-menu? (atom false)
        hide-fn #(dispatch [::set-menu-type nil])
        check-hide-click (partial handle-click in-menu? menu-comp hide-fn)]
    (r/create-class
     {:component-did-mount #(do
                              (reset! menu-comp (rdom/dom-node %))
                              (js/document.addEventListener "mousedown" check-hide-click))
      :component-will-unmount #(js/document.removeEventListener "mousedown" check-hide-click)

      :reagent-render
      (fn []
        (let [{:keys [top left]} @(subscribe [::get-position])
              register-fn @(subscribe [::register-fn])]
          [:div.menu__overlay--small {:on-mouse-enter #(reset! in-menu? true)
                                      :on-mouse-leave #(reset! in-menu? false)
                                      :style {:position "absolute"
                                              :top top
                                              :left (- left 150)}}
           [:div.layout__selection
            [:div.column__layout__icon
             {:on-click #(do
                           (dispatch [:de.explorama.frontend.reporting.views.reports.template-builder/add-template-row 1 2 register-fn])
                           (dispatch [::set-menu-type nil]))}
             [:div>span]
             single-label]
            [:div.column__layout__icon
             {:on-click #(do
                           (dispatch [:de.explorama.frontend.reporting.views.reports.template-builder/add-template-row 2 1 register-fn])
                           (dispatch [::set-menu-type nil]))}
             [:div [:span] [:span]]
             two-columns-label]]]))})))

; ----------- Base ------------

(reg-event-db
 ::set-menu-type
 (fn [db [_ type]]
   (assoc-in db dr-path/menu-type type)))

(reg-sub
 ::menu-type
 (fn [db]
   (get-in db dr-path/menu-type)))

(defn view []
  (when-let [menu-type @(subscribe [::menu-type])]
    (case menu-type
      :module-options [module-menu]
      :row-options [row-menu])))