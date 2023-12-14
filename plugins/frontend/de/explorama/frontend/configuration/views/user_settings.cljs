(ns de.explorama.frontend.configuration.views.user-settings
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.config :as config]
            [de.explorama.frontend.configuration.configs.config-types.language :as language-configs]
            [de.explorama.frontend.configuration.configs.config-types.mouse :as mouse-layout]
            [de.explorama.frontend.configuration.configs.config-types.theme :as theme-configs]
            [de.explorama.frontend.configuration.configs.config-types.woco :as woco-config]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.configuration.data.core :as data :refer [default-bucket]]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button radio select]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [hint
                                                                        icon]]
            [de.explorama.shared.common.configs.woco :as woco-config-shared]
            [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::delete-warning-frame
 (fn [db [_ active?]]
   (assoc-in db path/delete-warning-frame? active?)))

(re-frame/reg-sub
 ::delte-warning-frame?
 (fn [db _]
   (get-in db path/delete-warning-frame?)))

(re-frame/reg-event-db
 ::active-tab
 (fn [db [_ tab-name]]
   (assoc-in db path/active-tab tab-name)))

(re-frame/reg-sub
 ::active-tab-name
 (fn [db _]
   (get-in db path/active-tab :general)))

(re-frame/reg-event-fx
 ::apply-changes
 (fn [{db :db} [_]]
   (let [new-lang (get-in db path/temporary-user-settings-language)
         new-theme (get-in db path/temporary-user-settings-theme)
         language-changed? (language-configs/language-changed-sub? db)
         theme-changed? (theme-configs/theme-changed-sub? db)
         mouse-layout-changed? (mouse-layout/changes? db)
         woco-changes? (woco-config/changes? db)]
     {:fx (cond-> []
            woco-changes?
            (conj [:dispatch [::woco-config/submit]])

            mouse-layout-changed?
            (conj [:dispatch [::mouse-layout/submit]])

            language-changed?
            (conj [:dispatch [::persistence/save-and-commit
                              language-configs/config-type
                              language-configs/lang-config-id
                              {:value new-lang
                               :key language-configs/lang-config-id}]])

            theme-changed?
            (conj [:dispatch [::persistence/save-and-commit
                              theme-configs/config-type
                              theme-configs/theme-config-id
                              {:value new-theme
                               :key theme-configs/theme-config-id}]]))

      :db (update-in db path/temporary-user-settings dissoc path/language-key)})))

(defn- language-settings []
  (let [language-label @(re-frame/subscribe [::i18n/translate :langloc-language])
        language-drop-down-label @(re-frame/subscribe [::i18n/translate :langloc-language-drop-down])]
    [:div
     [:h2
      [icon {:icon :language}]
      language-label]
     [:div
      [select
       {:label language-drop-down-label
        :is-clearable? false
        :on-change (fn [{new-lang :value}]
                     (re-frame/dispatch [::language-configs/change-temporary-language new-lang]))
        :options @(re-frame/subscribe [::language-configs/available-languages-options])
        :placeholder language-drop-down-label
        :values @(re-frame/subscribe [::language-configs/temporary-language])
        :menu-row-height 35
        :extra-class "input--w100"}]]]))

(defn- theme-settings []
  (let [theme-label @(re-frame/subscribe [::i18n/translate :theme])]
    [:div
     [:h2
      [icon {:icon :palette}]
      theme-label]
     [:div
      [select
       {:label theme-label
        :is-clearable? false
        :on-change (fn [{new-theme :value}]
                     (re-frame/dispatch [::theme-configs/change-temporary-theme new-theme]))
        :options @(re-frame/subscribe [::theme-configs/available-theme-options])
        :values @(re-frame/subscribe [::theme-configs/temporary-theme])
        :placeholder theme-label
        :menu-row-height 35
        :extra-class "input--w100"}]]]))

(defn new-window-config []
  (let [{window-value :action} @(re-frame/subscribe [::woco-config/temporary-value woco-config-shared/new-window-pref-key])
        {published-window :action} @(re-frame/subscribe [::woco-config/temporary-value woco-config-shared/published-window-pref-key])
        #_#_; this value is configurable but unused for now
            published-windows @(re-frame/subscribe [::woco-config/temporary-value woco-config-shared/published-windows-pref-key])
        {:keys [window-init-placement-label window-init-placement-question
                window-init-placement-manual-first window-init-placement-auto-first
                window-init-placement-manual-rest window-init-placement-auto-rest
                window-search-placement-manual-rest window-search-placement-manual-first
                window-search-placement-auto-rest window-search-placement-auto-first
                window-search-placement-left-tooltip window-search-placement-right-tooltip
                window-search-placement-left window-search-placement-right
                window-search-placement-question]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :window-init-placement-label :window-init-placement-question
                              :window-init-placement-manual-first :window-init-placement-auto-first
                              :window-init-placement-manual-rest :window-init-placement-auto-rest
                              :window-search-placement-manual-rest :window-search-placement-manual-first
                              :window-search-placement-auto-rest :window-search-placement-auto-first
                              :window-search-placement-left-tooltip :window-search-placement-right-tooltip
                              :window-search-placement-left :window-search-placement-right
                              :window-search-placement-question])]
    [:div {:style {:flex 3}}
     [:h2
      [icon {:icon :window-plus}]
      window-init-placement-label]
     [:div.mb-24
      [:h4.mb-8 window-init-placement-question]
      [radio {:label window-init-placement-manual-rest
              :strong-label window-init-placement-manual-first
              :name "init-drop-grid"
              :on-change (fn [e]
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/new-window-pref-key {:action :drop}]))
              :checked? (= window-value :drop)}]
      [radio {:label window-init-placement-auto-rest
              :strong-label window-init-placement-auto-first
              :name "init-drop-grid"
              :checked? (= window-value :grid)
              :on-change (fn [e]
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/new-window-pref-key {:action :grid}]))}]]
     [:div
      [:h4.mb-8 window-search-placement-question]
      [radio {:label window-search-placement-manual-rest
              :strong-label window-search-placement-manual-first
              :name "search-auto-manual"
              :on-change (fn [e]
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-window-pref-key {:action :drop}])
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-windows-pref-key {:action :drop}]))
              :checked? (= :drop published-window)}]
      [radio {:label window-search-placement-auto-rest
              :strong-label window-search-placement-auto-first
              :name "search-auto-manual"
              :on-change (fn [e]
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-window-pref-key {:action :left}])
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-windows-pref-key {:action :left}]))
              :checked? (#{:left :right} published-window)}]]
     [:div.ml-24
      [radio {:on-change (fn [e]
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-window-pref-key {:action :left}])
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-windows-pref-key {:action :left}]))
              :name "search-left-right"
              :disabled? (not (#{:left :right} published-window))
              :tooltip window-search-placement-left-tooltip
              :label [:<>
                      [:span window-search-placement-left]
                      [:img {:src "img/window-grow-left.gif" :alt window-search-placement-left}]]
              :tooltip-class "icon-info-circle icon-gray"
              :checked? (#{:left} published-window)}]
      [radio {:on-change (fn [e]
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-window-pref-key {:action :right}])
                           (re-frame/dispatch [::woco-config/temporary-value woco-config-shared/published-windows-pref-key {:action :right}]))
              :name "search-left-right"
              :disabled? (not (#{:left :right} published-window))
              :tooltip window-search-placement-right-tooltip
              :label [:<>
                      [:span window-search-placement-right]
                      [:img {:src "img/window-grow-right.gif" :alt window-search-placement-right}]]
              :tooltip-class "icon-info-circle icon-gray"
              :checked? (#{:right} published-window)}]]]))

(defn mouse-layout []
  (let [{:keys [mouse-label mouse-pan mouse-left mouse-middle mouse-right mouse-tip-drag-first mouse-tip-drag-rest mouse-tip-pan-rest]
         :as translations}
        @(re-frame/subscribe [::i18n/translate-multi :mouse-left :mouse-right :mouse-middle :mouse-pan :mouse-select :mouse-label
                              :mouse-tip-drag-first :mouse-tip-drag-rest :mouse-tip-pan-rest])
        button-1 @(re-frame/subscribe [::mouse-layout/temporary-value 1])
        button-2 @(re-frame/subscribe [::mouse-layout/temporary-value 2])
        button-3 @(re-frame/subscribe [::mouse-layout/temporary-value 3])
        button-assignment-illegal @(re-frame/subscribe [::i18n/translate :mouse-button-assignment-illegal])
        illegal-assignment? (not= 2 (count (conj #{} (:value button-1)  (:value button-2)  (:value button-3))))]
    (re-frame/dispatch [::data/set-mouse-layout-error-status illegal-assignment?])
    [:div
     [:h2
      [icon {:icon :mouse}]
      mouse-label]
     [:div.flex.justify-between.align-items-end.gap-8.mb-8
      [select
       {:options (mouse-layout/translate-options translations)
        :values button-1
        :label mouse-left
        :show-clean-all? false
        :on-change (fn [value]
                     (re-frame/dispatch [::mouse-layout/temporary-value 1 value]))}]
      [select
       {:options (mouse-layout/translate-options translations)
        :values button-2
        :label mouse-middle
        :show-clean-all? false
        :on-change (fn [value]
                     (re-frame/dispatch [::mouse-layout/temporary-value 2 value]))}]
      [select
       {:options (mouse-layout/translate-options translations)
        :values button-3
        :label mouse-right
        :show-clean-all? false
        :on-change (fn [value]
                     (re-frame/dispatch [::mouse-layout/temporary-value 3 value]))}]]
     (when illegal-assignment?
       [:div
        {:style {:width :fit-content}}
        [hint {:variant :error, :content button-assignment-illegal}]])
     [:div.info
      [:div
       [icon {:icon :move-window}]
       [:strong mouse-tip-drag-first ": "]
       mouse-tip-drag-rest]
      [:div
       [:span {:class "icon-maximize" :style {:transform "rotate(45deg)"}}]
       [:strong mouse-pan ": "]
       mouse-tip-pan-rest]]]))

(defn general-settings []
  [:div.content.settings
   [language-settings]
   [theme-settings]
   [mouse-layout]
   [new-window-config]])

(defn footer [current-tab]
  (let [apply-lable @(re-frame/subscribe [::i18n/translate :save-settings-button])
        mouse-layout-changed? @(re-frame/subscribe [::mouse-layout/changes?])
        language-changed? @(re-frame/subscribe [::language-configs/language-changed?])
        theme-changed? @(re-frame/subscribe [::theme-configs/theme-changed?])
        woco-changed? @(re-frame/subscribe [::woco-config/changes?])
        illegal-mouse-button-assignment? @(re-frame/subscribe [::data/mouse-layout-error-status])]
    (when-not (= current-tab :export)
      [:div.footer
       [button {:disabled? (or (and (not language-changed?)
                                    (not theme-changed?)
                                    (not mouse-layout-changed?)
                                    (not woco-changed?))
                               illegal-mouse-button-assignment?)
                :start-icon :save
                :variant :primary
                :size :big
                :on-click #(re-frame/dispatch [::apply-changes])
                :label apply-lable}]])))

(defn- close-update-db [db]
  (-> db
      (update-in path/root dissoc path/delete-warning-frame-key)
      (update-in path/root dissoc path/temporary-user-settings-key)
      (update-in path/root dissoc path/temp-vertical-changes-key)
      (path/dissoc-in path/mouse-layout-temporary)
      (path/dissoc-in path/mouse-layout-error-status)
      (path/dissoc-in path/woco-temporary)))

(re-frame/reg-event-fx
 ::close-action
 (fn [{db :db} [_ _ close-event]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::close-action-sync close-event])
     {:db (close-update-db db)
      :dispatch-n [close-event]})))

(re-frame/reg-event-fx
 ::close-action-sync
 (fn [{db :db} [_ close-event]]
   {:db (close-update-db db)
    :fx [(when close-event
           [:dispatch close-event])
         [:dispatch (fi/call-api :hide-sidebar-event-vec "config-settings")]]}))

(re-frame/reg-event-fx
 ::settings-view-event
 (fn [{db :db} [_ action params]]
   (let [{:keys [frame-id callback-event]} params]
     (case action
       :frame/init
       {}
       :frame/close
       {:dispatch [::close-action frame-id callback-event]}
       {}))))

(defn- open-sidebar-fx []
  {:fx [[:dispatch [::data/request-available-datasources default-bucket]]
        [:dispatch (fi/call-api :sidebar-create-event-vec
                                {:module "config-settings"
                                 :title [::i18n/translate :menusection-settings]
                                 :event ::settings-view-event
                                 :id "config-settings"
                                 :vertical config/default-vertical-str
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
 (fn [{db :db} _]
   (open-sidebar-fx)))