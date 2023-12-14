(ns de.explorama.frontend.woco.tabs
  "Implementation and view for the Tabs"
  (:require [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.shared.common.unification.time :refer [current-ms]]
            [reagent.core :as r]
            [taoensso.timbre :refer [debug]]))

(defonce ^:private tabs-state (r/atom {}))
(defonce ^:private current-tab-id (r/atom nil))

(defn get-current-tab-id []
  @current-tab-id)

(defn activate-tab [context-id]
  (when-let [tab (get @tabs-state context-id)]
    (let [{:keys [on-before-show on-show]} tab]
      (when (fn? on-before-show)
        (on-before-show))
      (reset! current-tab-id context-id)
      (when (fn? on-show)
        (on-show)))))

(defn deregister
  "Remove the specified tab related to context-id"
  [context-id]
  (debug "Degister tab" context-id)
  (swap! tabs-state dissoc context-id)
  (when (= (get-current-tab-id) context-id)
    (reset! current-tab-id (ffirst @tabs-state)))
  nil)

(defn register
  "Add the tab"
  [{:keys [context-id active?] :as tool-desc}]
  (debug "Register tab" context-id)
  (let [existing-tab (get @tabs-state context-id)]
    (when (not= (:label existing-tab)
                (:label tool-desc))
      (when existing-tab
        (deregister context-id))
      (swap! tabs-state assoc context-id (-> tool-desc
                                             (assoc :timestamp (current-ms))
                                             (dissoc :active?)))))
  (when active?
    (activate-tab context-id))
  nil)

(defn active-title []
  (when-let [curr-tab (get-current-tab-id)]
    (-> (get-in @tabs-state [curr-tab :label])
        (val-or-deref))))

(defn active-tab-details []
  (when-let [curr-tab (get-current-tab-id)]
    (-> (get @tabs-state curr-tab)
        (select-keys [:context-id :label :origin :content-context]))))

(defn is-project-tab? []
  (when-let [curr-tab (get-current-tab-id)]
    (= :project (get-in @tabs-state [curr-tab :content-context]))))

(defn is-reporting-context? []
  (when-let [curr-tab (get-current-tab-id)]
    (#{:dashboard :report} (get-in @tabs-state [curr-tab :content-context]))))

(defn export-fn []
  (when-let [curr-tab (get-current-tab-id)]
    (get-in @tabs-state [curr-tab :export-fn])))

(defn- tab [{:keys [context-id label visible? active? on-click on-close]}]
  [:div.tab {:class (when active?
                      "active")
             :style (when-not visible?
                      {:display :none})
             :on-click on-click}
   [:div.label
    (val-or-deref label)]
   (when on-close
     [:div
      [button {:start-icon :close
               :size :small
               :on-click (fn [e]
                           (.stopPropagation e)
                           (when (fn? on-close)
                             (on-close context-id))
                           (deregister context-id))
               :variant :tertiary
               :icon-params {:color-important? true
                             :custom-color "#b8b5b578"}
               :extra-style {:width 20
                             :height 20}}]])])

(defn collapse-button [collapse-state]
  [button {:start-icon (if @collapse-state
                         :chevron-right
                         :chevron-left)
           :size :small
           :on-click (fn [e]
                       (.stopPropagation e)
                       (swap! collapse-state not))
           :variant :tertiary
           :icon-params {:color-important? true
                         :custom-color "#b8b5b578"}}])


(defn view [props]
  (let [collapse-state (r/atom false)]
    (fn [props]
      (let [tabs @tabs-state
            collapse? @collapse-state
            multiple-tabs? (< 1 (count tabs))
            tab-id (get-current-tab-id)]
        [:div.tabs__navigation.app-tabs
         (when multiple-tabs?
           [collapse-button collapse-state])
         (reduce (fn [acc [context-id tab-desc]]
                   (conj acc
                         (with-meta
                           [tab (cond-> tab-desc
                                  :always (assoc :on-click #(activate-tab context-id))
                                  (not collapse?) (assoc :visible? true)
                                  (= context-id tab-id) (assoc :active? true))]
                           {:key (str ::tab context-id)})))
                 [:<>]
                 (sort-by (fn [[_ {:keys [timestamp]}]]
                            timestamp)
                          tabs))]))))

(defn tabs-render-content []
  (let [tabs @tabs-state
        tab-id (get-current-tab-id)]
    (reduce (fn [acc [context-id {:keys [on-render]}]]
              (cond-> acc
                on-render
                (conj (with-meta
                        [:div {:id (str context-id)
                               :style {:display (when-not (= tab-id context-id)
                                                  :none)}}

                         [on-render context-id]]
                        {:key (str ::tab-content context-id)}))))
            [:<>]
            tabs)))