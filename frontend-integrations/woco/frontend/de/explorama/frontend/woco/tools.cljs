(ns de.explorama.frontend.woco.tools
  "Implementation and view for the Tools.
   We have two views toolbar (left) and header (top right)."
  (:require [clojure.spec.alpha :as spec]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-id]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.common.core :as common-ui]
            [de.explorama.frontend.ui-base.components.misc.core :as misc-ui]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :refer-macros [error]]
            [de.explorama.frontend.woco.api.product-tour :as product-tour]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.navigation.control :as nav-control]
            [de.explorama.frontend.woco.navigation.resources :as nav-resources]
            [de.explorama.frontend.woco.workspace.window-creation :as wwc]
            [de.explorama.frontend.woco.path :as path]))

;; External Functions

(defn get-tool [db id]
  (get-in db (path/tool-desc id)))

(defn register
  "Add the tool to the db."
  [db {:keys [id] :as tool-desc}]
  (if (spec/valid? :tool/desc tool-desc)
    (do
      (nav-resources/load-tool-resource tool-desc)
      (assoc-in db (path/tool-desc id) tool-desc))
    (do
      (error "tool-desc not conform with spec"
             {:desc tool-desc
              :explain (spec/explain-str :tool/desc tool-desc)})
      db)))

(defn deregister
  "Remove the specified tool-id from the db."
  [db tool-id]
  (update-in db path/tool-descs dissoc tool-id))

;; Internal Functions/Subs

(defn- sort-items [items]
  (sort-by #(:sort-order % 500)
           items))

(defn- sort-grouped-items
  "Sort grouped-items by the sort-order.
   Items that dont have a sort-order set get 500 as sort value."
  [grouped-items]
  (into {}
        (map (fn [[group-key group-items]]
               [group-key (sort-items group-items)])
             grouped-items)))

(re-frame/reg-sub
 ::tool-descs
 (fn [db _]
   (vals (get-in db path/tool-descs))))

(re-frame/reg-sub
 ::tool-minimap-icons
 (fn [db _]
   (reduce (fn [acc {:keys [vertical icon]}]
             (cond-> acc
               (and vertical icon)
               (assoc vertical icon)))
           {}
           (vals (get-in db path/tool-descs)))))

(re-frame/reg-sub
 ::sidebar-items ;Returns all toolbar tool-descs, these are grouped and sorted
 :<- [::tool-descs]
 (fn [tool-descs _]
   (->> tool-descs
        (filter #(= :sidebar (:tool-group %)))
        sort-items)))

(re-frame/reg-sub
 ::toolbar-items ;Returns all toolbar tool-descs, these are grouped and sorted
 :<- [::tool-descs]
 (fn [tool-descs _]
   (->> tool-descs
        (filter #(= :bar (:tool-group %)))
        (group-by :bar-group)
        sort-grouped-items)))

(re-frame/reg-sub
 ::header-items ;Returns all header tool-descs, these are grouped and sorted
 :<- [::tool-descs]
 (fn [tool-descs _]
   (->> tool-descs
        (filter #(= :header (:tool-group %)))
        (group-by :header-group)
        sort-grouped-items)))

(re-frame/reg-sub
 ::sync-project-items
 :<- [::tool-descs]
 (fn [tool-descs _]
   (->> tool-descs
        (filter #(= :sync-project (:tool-group %)))
        sort-items)))

(re-frame/reg-sub
 ::project-actions
 :<- [::tool-descs]
 (fn [tool-descs _]
   (->> tool-descs
        (filter #(= :project (:tool-group %)))
        (sort-by #(:sort-order % 500)))))

(re-frame/reg-sub
 ::reporting-actions
 :<- [::tool-descs]
 (fn [tool-descs _]
   (->> tool-descs
        (filter #(= :reporting (:tool-group %)))
        (sort-by #(:sort-order % 500)))))

(defn- notifications [notification-sub]
  (let [notifications (when notification-sub
                        @(re-frame/subscribe notification-sub))]
    (when (and notifications (< 0 notifications))
      [:div.new-indicator {:style {:z-index 1}}
       notifications])))

(defn- header-tool
  "Representing one item in the header-toolbar."
  [{:keys [id icon]} _]
  (let [id (str "navbar-item-" id "-" icon)
        bounding-rect (r/atom nil)]
    (r/create-class
     {:component-did-mount #(reset! bounding-rect (bounding-rect-id id))
      :reagent-render
      (fn [{:keys [icon
                   tooltip-text
                   action
                   action-key
                   enabled-sub
                   notification-sub]}
           disable-menu?]
        (let [active? (if enabled-sub
                        @(re-frame/subscribe enabled-sub)
                        true)
              tooltip-text (cond
                             (vector? tooltip-text)
                             @(re-frame/subscribe tooltip-text)
                             (string? tooltip-text)
                             tooltip-text)
              comp-active? @(re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/component-active?
                                                 :header-tools action-key])]
          [:<>
           [product-tour/product-tour-step {:parent-bounding-rect bounding-rect
                                            :component :header-tools
                                            :additional-info action-key
                                            :offset-top config/userbar-height}]
           [:a {:id id
                :href "#"
                :aria-label tooltip-text
                :on-click #(do
                             (when (and active?
                                        (not disable-menu?)
                                        comp-active?
                                        (seq action))
                               (re-frame/dispatch action))
                             (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/next-step :header-tools action-key]))}
            [misc-ui/icon (cond-> {:icon icon
                                   :tooltip tooltip-text}
                            (or (not active?)
                                disable-menu?
                                (not comp-active?))
                            (assoc :extra-class "disabled"))]
            [notifications notification-sub]]]))})))

(defn- header-section
  "Represent a section (left, middle, right)."
  [tools disable-menu?]
  (reduce (fn [acc {:keys [id] :as tool-desc}]
            (conj acc
                  (with-meta
                    [header-tool tool-desc disable-menu?]
                    {:key {:key (str "header-item-" id)}})))

          [:<>]
          tools))

(re-frame/reg-event-fx
 ::open-management-type
 (fn [{db :db} [_ vertical action]]
   (if-let [open-fid (some (fn [[{cvertical :vertical :as fid}]]
                             (when (= cvertical vertical)
                               fid))
                           (get-in db path/frames))]
     {:fx [[:dispatch (fi/call-api :frame-bring-to-front-event-vec open-fid)]
           [:dispatch [:de.explorama.frontend.woco.navigation.control/focus open-fid nil true]]]}
     {:dispatch action})))

;;; External UI-Elements

(defn- gen-tool-item [{:keys [id component tooltip-text action enabled-sub icon type vertical] :as tool-desc}
                      toolbar-normal? current-button ignore-action? add-class?]
  (let [tooltip-text (cond
                       (vector? tooltip-text)
                       @(re-frame/subscribe tooltip-text)
                       (string? tooltip-text)
                       tooltip-text)
        active? (and toolbar-normal?
                     (or (= current-button component)
                         (= current-button :*)
                         (and (nil? current-button)
                              (if enabled-sub
                                @(re-frame/subscribe enabled-sub)
                                true))))]
    (cond-> {:id id
             :title tooltip-text
             :disabled? (not active?)
             :icon (keyword icon)
             :on-click (fn []
                         (when (and active?
                                    (seq action)
                                    (not ignore-action?))
                           (if (= :frame/management-type type)
                             (re-frame/dispatch [::open-management-type vertical action])
                             (re-frame/dispatch action)))
                         (when (and active?
                                    current-button)
                           (re-frame/dispatch [::product-tour/next-step :toolbar current-button component])))}
      add-class? (assoc :extra-class id))))

(defn- gen-tool-product-tour-step [{:keys [id component]}]
  (let [bounding-rect (r/atom nil)]
    (r/create-class
     {:component-did-mount #(reset! bounding-rect (bounding-rect-id id))
      :reagent-render
      (fn [_]
        [product-tour/product-tour-step (cond-> {:component :toolbar
                                                 :additional-info component
                                                 :offset-left 50
                                                 :offset-top -80}
                                          @bounding-rect
                                          (assoc :parent-bounding-rect @bounding-rect))])})))

(defn toolbar
  "Left-Toolbar containing all tools registered for the tool-group bar."
  []
  (let [{:keys [top bottom middle]} @(re-frame/subscribe [::toolbar-items])
        maximized-frame @(re-frame/subscribe [:de.explorama.frontend.woco.frame.api/which-is-maximized])
        toolbar-normal? @(re-frame/subscribe [:de.explorama.frontend.woco.api.interaction-mode/normal? {:component :toolbar}])
        {current-button :additional-info
         ignore-action? :ignore-action?} @(re-frame/subscribe [::product-tour/current-step])]
    (when-not maximized-frame
      [:<>
       [product-tour/product-tour-step {:component :toolbar
                                        :additional-info :*
                                        :left 50
                                        :top "50%"}]
       [misc-ui/toolbar
        {:extra-class ["fixed" "center-y" "left-8"]
         :items (cond-> []
                  top
                  (conj (mapv #(gen-tool-item % toolbar-normal? current-button ignore-action? false) top))
                  middle
                  (conj (mapv #(gen-tool-item % toolbar-normal? current-button ignore-action? true) middle))
                  bottom
                  (conj (mapv #(gen-tool-item % toolbar-normal? current-button ignore-action? false) bottom)))}]
       (reduce
        (fn [res {:keys [component] :as item}]
          (if component
            (conj res [gen-tool-product-tour-step item])
            res))
        [:<>]
        (concat top middle bottom))])))

(defn header-tools
  "Right header-tools containing all tools registered for the tool-group header."
  []
  (let [{:keys [left right middle]} @(re-frame/subscribe [::header-items])
        sidebar-items @(re-frame/subscribe [::sidebar-items])
        project-loading-sub (fi/call-api [:project-loading-sub])
        project-loading? (when project-loading-sub @project-loading-sub)
        creating-new-windows? (wwc/creating-new-windows?)
        disable-menu? (or project-loading? creating-new-windows?)
        show? (not @(re-frame/subscribe [:de.explorama.frontend.woco.welcome/welcome-active?]))]
    [:div.menu
     (when show?
       [:<>
        [header-section sidebar-items disable-menu?]
        [header-section left disable-menu?]
        [:span.divider]
        [header-section middle disable-menu?]
        [:span.divider]])
     [header-section right disable-menu?]]))

(defn- action-item [{:keys [id]} _]
  (let [id (str "project-item-" id)
        bounding-rect (r/atom nil)]
    (r/create-class
     {:component-did-mount #(reset! bounding-rect (-> (bounding-rect-id id)
                                                      (update :top + 50)))
      :reagent-render (fn [{:keys [action icon tooltip-text action-key]} disabled?]
                        (let [component-active? @(re-frame/subscribe [::product-tour/component-active? :project-action action-key])]
                          [:<>
                           [button {:id id
                                    :start-icon icon
                                    :extra-class "explorama-overlay-close"
                                    :title (some-> tooltip-text re-frame/subscribe deref)
                                    :disabled? (or (not component-active?) disabled?)
                                    :on-click #(do
                                                 (re-frame/dispatch (conj action (aget % "nativeEvent")))
                                                 (re-frame/dispatch [::product-tour/next-step :project-action action-key]))}]
                           (when action-key
                             [product-tour/product-tour-step {:parent-bounding-rect bounding-rect
                                                              :component :project-action
                                                              :additional-info action-key
                                                              :offset-top 0}])]))})))

(defn project-actions []
  (let [project-loading? @(fi/call-api [:project-loading-sub])
        creating-new-windows? (wwc/creating-new-windows?)
        all-project-actions @(re-frame/subscribe [::project-actions])
        project-actions (filterv (fn [{:keys [enabled-sub]}]
                                   (or (nil? enabled-sub)
                                       @(re-frame/subscribe enabled-sub)))
                                 all-project-actions)]
    (reduce (fn [acc desc]
              (conj acc
                    [action-item desc (or project-loading? creating-new-windows?)]))
            [:div.actions.btn-group]
            project-actions)))

(defn reporting-actions []
  (let [creating-new-windows? (wwc/creating-new-windows?)
        all-reporting-actions @(re-frame/subscribe [::reporting-actions])
        reporting-actions (filterv (fn [{:keys [enabled-sub]}]
                                     (or (nil? enabled-sub)
                                         @(re-frame/subscribe enabled-sub)))
                                   all-reporting-actions)]
    (reduce (fn [acc desc]
              (conj acc
                    [action-item desc creating-new-windows?]))
            [:div.actions.btn-group]
            reporting-actions)))


(defn- sync-project-tool [{:keys [id action tooltip-text icon active-sub enabled-sub notification-sub]}]
  (let [enabled? (or (nil? enabled-sub)
                     (and (vector? enabled-sub)
                          @(re-frame/subscribe enabled-sub)))
        active? (and (vector? active-sub)
                     @(re-frame/subscribe active-sub))
        notifications (when notification-sub
                        @(re-frame/subscribe notification-sub))]
    [common-ui/tooltip {:text (when (vector? tooltip-text)
                                (re-frame/subscribe tooltip-text))
                        :direction :up}
     [:button (cond-> {:aria-label @(re-frame/subscribe tooltip-text)
                       :class []}
                (vector? action)
                (assoc :on-click #(re-frame/dispatch action))
                active?
                (update :class conj "active")
                (not enabled?)
                (assoc :disabled true))

      (when icon
        [misc-ui/icon {:icon icon}])
      (when (and notifications (< 0 notifications))
        [:span.ml-6 notifications])]]))

(defn sync-projects []
  (let [items @(re-frame/subscribe [::sync-project-items])
        items (reduce (fn [acc {:keys [id visible-sub] :as tool-desc}]
                        (cond-> acc
                          (and id (or (nil? visible-sub)
                                      (and (vector? visible-sub)
                                           @(re-frame/subscribe visible-sub))))
                          (conj
                           ^{:key id}
                           [sync-project-tool tool-desc])))
                      []
                      items)]
    (when (seq items)
      [:div.toolbar-wrapper.bottom-8.absolute {:style {:left 10}}
       [:div.toolbar.toolbar-horizontal
        (apply conj [:div.toolbar-section] items)]])))