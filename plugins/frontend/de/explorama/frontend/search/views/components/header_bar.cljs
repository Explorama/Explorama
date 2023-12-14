(ns de.explorama.frontend.search.views.components.header-bar
  (:require [re-frame.core :refer [subscribe]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.views.search-bar :as search-bar]))

(defn- title-comp [{:keys [title]}]
  (when title
    (let [title @(subscribe [::i18n/translate title])]
      [:div.search__direct__label
       title])))

(defn- search-bar-comp [frame-id {:keys [search-bar?]}]
  (when search-bar?
    [:div
     [search-bar/bar frame-id]]))

(defn header-bar [frame-id {:keys [mode-toggle?] :as params}]
  [error-boundary
   [:div.search__direct
    [title-comp params]
    [search-bar-comp frame-id params]]])
