(ns de.explorama.frontend.configuration.views.user-settings-comp
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.views.export-settings :as export]
            [de.explorama.frontend.configuration.views.user-settings :as settings]
            [de.explorama.frontend.expdb.settings :as expdb]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer-macros [error]]))

;; Direct-builder consumer site for the tabs styling owned by woco/tabs.cljs
;; (see its class-stack comment for the tabs__navigation/tab/active
;; cross-sheet-marker rationale). Duplicated per-file rather than a shared
;; styles ns; this file is triplicated across bundles/{server,browser,electron}
;; same as the rest of its content.
(def ^:private tabs-navigation-class
  "flex flex-row z-1 bg-(--bg) shadow-md")
(def ^:private tab-base-class
  "grow group flex items-center justify-center gap-1 py-2 px-4 text-center font-bold [transition:background-color_.1s_ease,color_.1s_ease,box-shadow_.25s_ease]")
(def ^:private tab-default-class
  "text-(--text-secondary) cursor-pointer hover:bg-(--bg-hover) hover:text-(--link)")
(def ^:private tab-active-class
  "active text-(--text) bg-(--bg) cursor-default shadow-[inset_0_-2px_0_0_var(--text)]")
(def ^:private tab-icon-default-class "[transition:background-color_.1s_ease] bg-(--icon-secondary) group-hover:bg-(--link)")
(def ^:private tab-icon-active-class "[transition:background-color_.1s_ease] bg-(--icon)")

(defn- tab-class [active?]
  (str tab-base-class " " (if active? tab-active-class tab-default-class)))

(defn- tab-icon-class [active?]
  (if active? tab-icon-active-class tab-icon-default-class))

(defn tabs []
  (let [general-label @(re-frame/subscribe [::i18n/translate :general-settings-group])
        expdb-label @(re-frame/subscribe [::i18n/translate :expdb-settings-group])
        ;; import-label @(re-frame/subscribe [::i18n/translate :expdb-import-label])
        ;TODO r1/temporary-import
        ;; import-label (or import-label "Import")
        current-tab @(re-frame/subscribe [::settings/active-tab-name])
        general-active? (= :general current-tab)]
    (into
     [:div.tabs__navigation {:class tabs-navigation-class}
      [:div.tab
       {:class (tab-class general-active?)
        :on-click #(do
                     (re-frame/dispatch [::settings/close-action])
                     (re-frame/dispatch [::settings/active-tab :general]))}
       [icon {:icon :cogs :extra-class (tab-icon-class general-active?)}]
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
        [:div.loader-sm.pr-2
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