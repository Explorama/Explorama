(ns de.explorama.frontend.configuration.views.layout-management.overview
  (:require [clojure.string :refer [join lower-case starts-with? trim]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.views.legend :refer [attr->display-name]]
            [de.explorama.frontend.configuration.components.dialog :as dialog :refer [confirm-dialog]]
            [de.explorama.frontend.configuration.components.save-dialog :refer [layout-overlayer-save-dialog]]
            [de.explorama.frontend.configuration.components.share-dialog :refer [layout-overlayer-share-dialog]]
            [de.explorama.frontend.configuration.config :as config-config]
            [de.explorama.frontend.configuration.configs.config-types.layout :as layout-configs]
            [de.explorama.frontend.configuration.configs.config-types.overlayer :as overlayer-configs]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.configuration.data.core :as data]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.configuration.views.layout-management.editing :as editing]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position context-menu]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.shared.common.configs.layouts :refer [is-layout-valid?]]
            [de.explorama.shared.common.configs.overlayers :refer [is-overlayer-valid?]]
            [de.explorama.shared.common.configs.platform-specific :as config-platform]
            [re-frame.core :as re-frame :refer [dispatch reg-event-db reg-sub
                                                subscribe]]
            [reagent.core :as r]))

(reg-sub
 ::sidebar-title
 (fn [db]
   (let [base (i18n/translate db :layout-management-label)
         extra (get-in db path/layout-sidebar-title)]
     (if extra
       (str base ": " (i18n/translate db extra))
       base))))

(reg-event-db
 ::set-sidebar-title-extra
 (fn [db [_ title]]
   (assoc-in db path/layout-sidebar-title title)))

(reg-sub
 ::burger-menu-infos
 (fn [db]
   (get-in db path/burger-menu-infos)))

(reg-event-db
 ::set-burger-menu-infos
 (fn [db [_ infos]]
   (assoc-in db path/burger-menu-infos infos)))

(defn- config-type [layout-desc]
  (let [layout (val-or-deref layout-desc)]
    (if (or (get layout :layer-type)
            (get layout :grouping-attribute))
      :overlayers
      :layouts)))

(defn- gen-copy-name [layout-name type]
  (let [configs (case type
                  :layouts @(subscribe [::layout-configs/existing-user-layout-names])
                  :overlayers @(subscribe [::overlayer-configs/existing-user-overlayer-names])
                  #{})
        same-name-layouts (->> (keys configs)
                               (filterv #(starts-with? % layout-name)))
        max-num-used (->> same-name-layouts
                          ; return the x from the substring "(copy x)" or "1" in case of (copy)
                          (mapv #(or (re-find #"(?<=\(copy )[0-9]*(?=\))" %) "1"))
                          (mapv #(js/parseInt %))
                          (apply max))
        ; only add a number, if there already is a copy (+ the base layout itself, hence > 1)
        new-num (when (> (count same-name-layouts) 1) (str " " (inc max-num-used)))]
    (str layout-name " (copy" new-num ")")))

(defn burger-menu [{:keys [share?]}]
  (let [{:keys [layout event default? on-edit type]} @(subscribe [::burger-menu-infos])
        overlay-module @(fi/call-api :service-target-sub :config-module :overlay-edit)
        {:keys [id]} layout
        {:keys [copy-label view-label send-copy-label edit-label delete-label]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :copy-label
                              :view-label
                              :send-copy-label
                              :edit-label
                              :delete-label])
        {:keys [top left]} (calc-menu-position event)
        disabled? (and (not= type :layouts)
                       (not overlay-module))
        edit-label-key (cond (= type :layouts)
                             :edit-layout-label
                             (= type :overlayers)
                             :edit-overlayer-label)
        delete-dialog (cond (= type :layouts)
                            :delete-layout
                            (= type :overlayers)
                            :delete-overlayer)]
    [context-menu
     {:show? (boolean id)
      :on-close #(dispatch [::set-burger-menu-infos])
      :position  {:top top :left left}
      :menu-z-index 250000
      :items (cond-> []
               default?
               (conj {:label view-label
                      :disabled? disabled?
                      :icon :eye
                      :on-click #(do
                                   (on-edit :view)
                                   (dispatch [::set-sidebar-title-extra edit-label-key]))})
               (not default?)
               (conj {:label edit-label
                      :disabled? disabled?
                      :icon :edit
                      :on-click #(do
                                   (on-edit :edit)
                                   (dispatch [::set-sidebar-title-extra edit-label-key]))})
               default?
               (conj {:label copy-label
                      :disabled? disabled?
                      :icon :copy
                      :on-click #(do
                                   (on-edit :copy)
                                   (dispatch [::set-sidebar-title-extra edit-label-key]))})
               (and (not default?)
                    config-platform/explorama-multi-user)
               (conj {:label send-copy-label
                      :disabled? disabled?
                      :icon :share
                      :on-click #(reset! share? layout)})
               (not default?)
               (conj {:label delete-label
                      :disabled? disabled?
                      :icon :trash
                      :on-click
                      (fn [e]
                        (.stopPropagation e)
                        (dispatch [::dialog/set-data delete-dialog {:delete-event [::persistence/delete-config type id]}])
                        (dispatch [::dialog/show-dialog delete-dialog true delete-dialog]))}))}]))

(defn- layout-summary [{:keys [edit-layout-desc layout]}]
  (let [{card-title :name
         :keys [attributes
                default?
                color-scheme]} layout
        {:keys [attribute-label attributes-label]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :attribute-label
                              :attributes-label])
        colors (:colors color-scheme)
        labels @(fi/call-api [:i18n :get-labels-sub])
        type (config-type layout)
        card-subtitle (when (seq attributes) (-> (if (= 1 (count attributes))
                                                   (str attribute-label ": ")
                                                   (str attributes-label ": "))
                                                 (str (join ", " (map #(attr->display-name % labels) attributes)))))]
    [:li.disabled
     ;[:div.unread-indicator] ;TODO r1/notifications
     [:div.card__image>div.color__scale__preview
      (reduce (fn [res [_ color]]
                (conj res
                      [:div {:style {:background-color color}}]))
              [:<>]
              (sort-by first colors))]
     [:div.card__text.align-self-start
      [:div.title.flex
       [tooltip {:text card-title :direction :up}
        card-title]]
      (when card-subtitle
        [:div.subtitle.flex
         [tooltip {:text card-subtitle :direction :up}
          card-subtitle]])]
     [:div.card__actions
      [:div
       {:on-click
        (fn [e]
          (.stopPropagation e)
          (dispatch [::set-burger-menu-infos {:event e
                                              :default? default?
                                              :type type
                                              :on-edit (fn [action]
                                                         (reset! edit-layout-desc
                                                                 (case action
                                                                   :copy (-> layout
                                                                             (assoc :id (str (random-uuid)))
                                                                             (update :name #(gen-copy-name % type))
                                                                             (dissoc :default? :datasources))
                                                                   :edit layout
                                                                   :view (-> layout
                                                                             (assoc :read-only? true))
                                                                   layout)))
                                              :layout layout}]))}
       [icon {:icon :menu}]]]]))

(defn overview-block [{:keys [title layouts edit-layout-desc]}]
  [:div.section__cards>div
   [:h2 title]
   (reduce (fn [acc {:keys [id] :as layout}]
             (conj
              acc
              (with-meta
                [layout-summary {:edit-layout-desc edit-layout-desc
                                 :layout layout}]
                {:key (str "conf-layout-summary " id)})))
           [:ul]
           (sort-by #(lower-case (trim (:name %)))
                    (vals (val-or-deref layouts))))])

(defn layouts-settings [props]
  (let [{:keys [#_label-default-layouts label-my-layouts]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :label-default-layouts
                              :label-my-layouts])]
    [:<>
     #_; TODO r1/layouts removes default layouts for now
     [overview-block (assoc props
                            :title label-default-layouts
                            :layouts (fi/call-api [:config :get-config-sub]
                                                  :default-layouts))]
     [overview-block (assoc props
                            :title label-my-layouts
                            :layouts (fi/call-api [:config :get-config-sub]
                                                  :layouts))]]))

(defn overlay-settings [props]
  (let [{:keys [#_label-default-overlayers label-my-overlayers]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :label-default-overlayers
                              :label-my-overlayers])]
    [:<>
     #_; TODO r1/layouts removes default layouts for now
     [overview-block (assoc props
                            :title label-default-overlayers
                            :layouts (fi/call-api [:config :get-config-sub]
                                                  :default-overlayers))]
     [overview-block (assoc props
                            :title label-my-overlayers
                            :layouts (fi/call-api [:config :get-config-sub]
                                                  :overlayers))]]))

(defn overview-footer [{:keys [current-tab-index edit-layout-desc]}]
  (let [{:keys [label-create-layout label-create-overlayer label-no-access-map]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :label-create-layout
                              :label-create-overlayer
                              :label-no-access-map])
        overlay-module @(fi/call-api :service-target-sub :config-module :overlay-edit)]
    [:div.footer
     (when (and (not overlay-module)
                (= 1 @current-tab-index))
       [:h3 label-no-access-map])
     (case @current-tab-index
       0 [button {:start-icon :plus
                  :variant :primary
                  :size :big
                  :on-click #(reset! edit-layout-desc (merge config-config/create-layout-selection
                                                             {:id (str (random-uuid))
                                                              :timestamp (js/Date.now)
                                                              :name "my-layout-name"}))
                  :label label-create-layout}]
       1 [button {:start-icon :plus
                  :variant :primary
                  :size :big
                  :disabled? (not overlay-module)
                  :on-click  #(reset! edit-layout-desc {:id (str (random-uuid))
                                                        :timestamp (js/Date.now)
                                                        :name "my-overlayer-name"
                                                        :grouping-attribute "country"})
                  :label label-create-overlayer}])]))


(defn- tab [{:keys [label tab-idx current-tab-index]}]
  [:div.tab {:class (when (= @current-tab-index tab-idx)
                      "active")
             :on-click #(reset! current-tab-index tab-idx)}
   label])

(defn- tabs [props]
  (let [{:keys [label-layouts label-overlayers]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :label-layouts
                              :label-overlayers])]
    [:div.tabs__navigation.full-width
     [tab (assoc props
                 :label label-layouts
                 :tab-idx 0)]
     [tab (assoc props
                 :label label-overlayers
                 :tab-idx 1)]]))

(defn- overview-view [{:keys [current-tab-index] :as props}]
  [:<>
   [tabs props]
   [:div.content {:on-click #(dispatch [::set-burger-menu-infos nil])}
    (case @current-tab-index
      0 [layouts-settings props]
      1 [overlay-settings props]
      [:<>])]
   [overview-footer props]])

(defn- editing-header [{:keys [edit-layout-desc]}]
  [button {:on-click #(do (reset! edit-layout-desc nil)
                          (dispatch [::set-sidebar-title-extra nil]))
           :label @(re-frame/subscribe [::i18n/translate :back-label])
           :start-icon :previous
           :size :big
           :variant :back}])

(defn- handle-layout-change [edit-layout-desc path new-val]
  (if (fn? path)
    (swap! edit-layout-desc path)
    (swap! edit-layout-desc
           (if (vector? path)
             assoc-in
             assoc)
           path new-val)))

(defn- editing-footer [{:keys [save-dialog? edit-layout-desc]} type]
  (let [layout-desc (val-or-deref edit-layout-desc)
        valid? (cond (= type :overlayers) (is-overlayer-valid? layout-desc)
                     (= type :layouts) (is-layout-valid? layout-desc)
                     :else false)
        save-as-label @(re-frame/subscribe [::i18n/translate :save-title])
        has-errors? @(re-frame/subscribe [::data/layout-error-status :sidebar])]
    (when-not (get layout-desc :read-only?)
      [:div.footer
       [button {:start-icon :save
                :size :big
                :disabled? (boolean
                            (or (not valid?)
                                has-errors?))
                :variant :primary
                :on-click #(reset! save-dialog? true)
                :label save-as-label}]])))

(defn- layout-editing-view [{:keys [edit-layout-desc] :as props}]
  (let [{overlay-editing-view :component} @(fi/call-api :service-target-sub :config-module :overlay-edit)
        type (config-type edit-layout-desc)
        edit-props {:collapsible? false
                    :layout edit-layout-desc
                    :translate (fn [key]
                                 (subscribe [::i18n/translate key]))
                    :translate-multi (fn [& keys]
                                       (subscribe (into [::i18n/translate-multi] keys)))
                    :on-unmount (fn []
                                  (dispatch [::data/clear-characteristics]))
                    :color-scales (fi/call-api [:config :get-config-sub]
                                               :color-scales)
                    :request-characteristics (fn [attributes]
                                               (dispatch [::data/request-characteristics nil attributes]))
                    :ac-attribute-types (fn [context]
                                          (subscribe [::data/attr-types nil context]))
                    :ac-vals (fn [attributes filter-vals]
                               (subscribe [::data/acs nil attributes filter-vals]))
                    :on-change-layout (partial handle-layout-change edit-layout-desc)
                    :error-status-callback (fn [status]
                                             (dispatch [::data/set-layout-error-status :sidebar status]))}]
    [:<>
     [editing-header props]
     [:div.content>div {:on-click #(dispatch [::set-burger-menu-infos nil])}
      (case type
        :layouts
        [editing/editing-view edit-props]
        :overlayers
        [overlay-editing-view edit-props])]

     [editing-footer props type]]))

(defn view [_]
  (let [edit-layout-desc (r/atom nil)
        current-tab-index (r/atom 0)
        save-dialog? (r/atom nil)
        share-layout-desc (r/atom nil)
        can-edit-save-dialog? (r/atom true)
        props {:edit-layout-desc edit-layout-desc
               :current-tab-index current-tab-index
               :save-dialog? save-dialog?
               :share-layout-desc share-layout-desc
               :type-check config-type
               :share-users (subscribe [::data/share-users])
               :can-edit-save-dialog? can-edit-save-dialog?}
        receive-sync-events? (fi/call-api :project-receive-sync?-sub)
        no-sync-hint (re-frame/subscribe [::i18n/translate :no-sync-hint])]
    (r/create-class
     {:display-name "layout-management-overview"
      :reagent-render (fn [sidebar-props]
                        [error-boundary
                         (cond
                           @receive-sync-events? [:div.no-data-placeholder
                                                  [:span
                                                   [:div.loader-sm.pr-8
                                                    [:span]]
                                                   [:div @no-sync-hint]]]
                           @edit-layout-desc [:<>
                                              [layout-overlayer-save-dialog
                                               (assoc props
                                                      :config-type-fn config-type
                                                      :handle-layout-change handle-layout-change)]
                                              [layout-editing-view props]]
                           :else [:<>
                                  [layout-overlayer-share-dialog (assoc props
                                                                        :config-type-fn config-type)]
                                  [burger-menu (assoc sidebar-props :share? share-layout-desc)]
                                  [confirm-dialog :delete-layout]
                                  [confirm-dialog :delete-overlayer]
                                  [overview-view props]])])})))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ _ close-event]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::close-action-sync close-event])
     {:db db
      :dispatch-n [close-event]})))

(re-frame/reg-event-fx
 ::close-action-sync
 (fn [{db :db} [_ close-event]]
   {:db db
    :fx [(when close-event
           [:dispatch close-event])
         [:dispatch (fi/call-api :hide-sidebar-event-vec "config-layout-settings")]]}))

(re-frame/reg-event-fx
 ::settings-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event]} params]
     (case action
       :frame/init
       {:db db
        :dispatch [::set-sidebar-title-extra]}
       :frame/close
       {:dispatch [::close-action frame-id callback-event]}
       {}))))

(defn- open-sidebar-fx []
  {:fx [[:dispatch (fi/call-api :sidebar-create-event-vec
                                {:module "config-layout-settings"
                                 :title [::sidebar-title]
                                 :event ::settings-view-event
                                 :id "config-layout-settings"
                                 :vertical config-config/default-vertical-str
                                 :position :right
                                 :close-event [::close-action]
                                 :width 600})]]})

(re-frame/reg-event-fx
 ::open-sidebar
 (fn [{db :db} _]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::sync-open-sidebar])
     (open-sidebar-fx))))

(re-frame/reg-event-fx
 ::sync-open-sidebar
 (fn [_ _]
   (open-sidebar-fx)))