(ns de.explorama.frontend.table.table.legend
  (:require [clojure.string :refer [join]]
            [de.explorama.frontend.common.i18n :as i18n]
            [goog.string.format]
            [re-frame.core :as re-frame]
            [de.explorama.shared.data-format.simplified-view :as dflsv]
            [de.explorama.frontend.table.path :as path]
            [de.explorama.frontend.table.config :as config]
            [de.explorama.frontend.table.table.data :as table-data]
            [de.explorama.shared.table.ws-api :as ws-api]))

(defn i18n-sub [i18n-key]
  (re-frame/subscribe [::i18n/translate i18n-key]))

(re-frame/reg-sub
 ::applied-filter
 (fn [db [_ frame-id]]
   (get-in db (path/applied-filter frame-id))))

(re-frame/reg-sub
 ::di-desc
 (fn [db [_ frame-id]]
   (get-in db (path/di-desc frame-id))))

(re-frame/reg-sub
 ::frame-datasource
 (fn [db [_ path]]
   (get-in db (path/frame-di path))))

(re-frame/reg-sub
 ::simplified-di-desc
 (fn [[_ frame-id]]
   [(re-frame/subscribe [::frame-datasource frame-id])
    (re-frame/subscribe [::di-desc frame-id])
    (re-frame/subscribe [::applied-filter frame-id])])
 (fn [[di-filter {:keys [filtered-data-info] :as di-desc} local-filter] _]
   (let [base (if (and local-filter (seq filtered-data-info))
                filtered-data-info
                di-desc)]


     {:base base
      :local-filter? (boolean local-filter)
      :additional (dflsv/simplified-filter di-filter local-filter)})))

(re-frame/reg-sub
 ::data-display-count
 (fn [db [_ frame-id]]
   (let [{:keys [global local]} (get-in db (conj (path/frame-filter frame-id) :counts))]
     {:all-data global
      :local-data local})))

(defn- info-block [label attributes]
  [:div
   [:dt label]
   [:dd (join ", " attributes)]])

(defn operation-infos [{:keys [frame-id]}]
  (let [current-sorting (table-data/frame-table-single-config frame-id ws-api/sorting-key config/default-sorting)
        {:keys [info-sorted-by contextmenu-operations-group-label]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :info-sorted-by
                              :contextmenu-operations-group-label])]
    (when current-sorting
      [:div.panel__section
       [:div.section__content>div.panel__subsection
        [:div.section__title contextmenu-operations-group-label]
        [:div.subsection__content>dl
         (cond-> [:<>]
           current-sorting
           (conj [info-block
                  info-sorted-by
                  (mapv (fn [{:keys [attr direction]}]
                          (i18n/attribute-label attr))
                        current-sorting)]))]]])))

(def legend-impl
  {:visible? true
   :disabled? (fn [frame-id]
                false)

   :data-display-count
   (fn [frame-id]
     (re-frame/subscribe [::data-display-count frame-id]))

   :di-desc-sub
   (fn [frame-id]
     (re-frame/subscribe [::simplified-di-desc frame-id]))

   :configuration [{:module operation-infos}]})