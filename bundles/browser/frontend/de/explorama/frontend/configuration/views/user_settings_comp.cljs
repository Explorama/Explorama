(ns de.explorama.frontend.configuration.views.user-settings-comp
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.views.export-settings :as export]
            [de.explorama.frontend.configuration.views.user-settings :as settings]
            [de.explorama.frontend.expdb.settings :as expdb]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error]]))

(defn tabs []
  (let [general-label @(re-frame/subscribe [::i18n/translate :general-settings-group])
        expdb-label @(re-frame/subscribe [::i18n/translate :expdb-settings-group])
        ;; import-label @(re-frame/subscribe [::i18n/translate :expdb-import-label])
        ;TODO r1/temporary-import
        ;; import-label (or import-label "Import")
        current-tab @(re-frame/subscribe [::settings/active-tab-name])]
    (into
     [:div.tabs__navigation.full-width
      [:div.tab
       {:class (when (= :general current-tab) "active")
        :on-click #(do
                     (re-frame/dispatch [::settings/close-action])
                     (re-frame/dispatch [::settings/active-tab :general]))}
       [icon {:icon :cogs}]
       general-label]
      #_;TODO r1/export-data fix the data export
      [:div.tab
       {:class (when (= :expdb current-tab) "active")
        :on-click #(do
                     (re-frame/dispatch [::settings/close-action])
                     (re-frame/dispatch [::settings/active-tab :expdb]))}
       [icon {:icon :database}]
       expdb-label]])))
      ;; [:div.tab
      ;;  {:class (when (= :general current-tab) "active")
      ;;   :on-click #(do
      ;;                (re-frame/dispatch [::settings/close-action])
      ;;                (re-frame/dispatch [::settings/active-tab :expdb-import]))}
      ;;  [icon {:icon :tempimport}]
      ;;  import-label]

(defn view []
  (let [current-tab @(re-frame/subscribe [::settings/active-tab-name])
        receive-sync-events? @(fi/call-api :project-receive-sync?-sub)
        no-sync-hint @(re-frame/subscribe [::i18n/translate :no-sync-hint])]
    (if receive-sync-events?
      [:div.no-data-placeholder
       [:span
        [:div.loader-sm.pr-8
         [:span]]
        [:div no-sync-hint]]]
      [:<>
       [tabs]
       (cond
         (= current-tab :general) [settings/general-settings]
         (= current-tab :export) [export/view]
         (= current-tab :expdb) [expdb/expdb-settings]
          ;;  (= current-tab :expdb-import) [expdb-import/view]
         :else (do (error "unknown tab" current-tab) [:div.content]))
       (when (not (#{:expdb :expdb-import} current-tab))
         [settings/footer current-tab])])))