(ns de.explorama.frontend.search.views.components.direct-visualization
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button-group button]]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.data.di :as data-di]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.config :as config]
            [taoensso.timbre :refer-macros [error]]))

(def ^:private pref-key "active-directvis")

(defn- preferences-set [preferences]
  (->> (string/split preferences #",")
       (filter #(seq %))
       (map keyword)
       set))

(re-frame/reg-event-fx
 ::update-visualization-options
 (fn [{db :db} [_ _ option]]
   (let [prefs (fi/call-api [:user-preferences :preference-db-get]
                            db
                            pref-key
                            config/default-direct-vis-options)
         preferences (preferences-set prefs)
         new-active? (not (preferences option))
         active-vis-options (->> (if new-active?
                                   (conj preferences option)
                                   (disj preferences option))
                                 (map name)
                                 (string/join ","))]
     {:dispatch (fi/call-api [:user-preferences :save-event-vec]
                             pref-key active-vis-options)})))

(re-frame/reg-event-fx
 ::open-visualizations
 (fn [{db :db} [_ frame-id]]
   (let [prefs (fi/call-api [:user-preferences :preference-db-get]
                            db
                            pref-key
                            config/default-direct-vis-options)
         vis-options (fi/call-api :service-category-db-get db :visual-option)
         frame-infos (fi/call-api :frame-db-get db frame-id)
         product-tour-step (fi/call-api [:product-tour :current-db-get]
                                        db)
         {pixel-coords :coords
          {original-pixel-coords :coords} :before-minmaximized} frame-infos
         pos (or original-pixel-coords pixel-coords)
         options        (preferences-set prefs)
         direct-vis-opened? (get-in db (spath/frame-direct-vis-opened? frame-id))
         visualizations (map-indexed (fn [idx option]
                                       [(get-in vis-options [option :event])
                                        frame-id pos true
                                        {:multiple-windows? true
                                         :idx idx}])
                                     options)
         product-tour? (seq product-tour-step)]
     (when (not direct-vis-opened?)

       {:db         (-> db
                        (assoc-in spath/base-visualization-options options)
                        (assoc-in (spath/frame-direct-vis-opened? frame-id) true))
        :dispatch-n (if-not product-tour?
                      visualizations
                      [])}))))

(re-frame/reg-event-fx
 ::open-visualization
 (fn [{db :db} [_ frame-id vis-event]]
   (let [frame-infos (fi/call-api :frame-db-get db frame-id)
         {pixel-coords :coords
          {original-pixel-coords :coords} :frame/before-minmaximized} frame-infos
         pos (or original-pixel-coords pixel-coords)]
     {:dispatch-n [[vis-event frame-id pos true]
                   (fi/call-api [:product-tour :next-event-vec]
                                :search :direct-vis)]})))

(re-frame/reg-sub
 ::component-active?
 (fn [db [_ frame-id]]
   (not (get-in db (spath/frame-direct-vis-opened? frame-id) false))))

(defn direct-visualization-button-group [frame-id available-visualizations disabled?]
  (let [preferences (-> @(fi/call-api [:user-preferences :preference-sub]
                                      pref-key
                                      config/default-direct-vis-options)
                        (preferences-set))
        tooltip-addition @(re-frame/subscribe [::i18n/translate :direct-visualization-toggle-addition])
        items (mapv
               (fn [[key {:keys [event sort-class tooltip-search] vertical-icon :icon}]]
                 (let [tooltip-text (if tooltip-search
                                      @(re-frame/subscribe tooltip-search)
                                      (error "tooltip-search not found for key " key))]
                   {:id key
                    :extra-props {:class (str "tool-" (name key))}
                    :compact? true
                    :label [tooltip {:text (str tooltip-text tooltip-addition)} [icon {:icon vertical-icon}]]
                    :on-click #(re-frame/dispatch [::update-visualization-options frame-id key preferences])
                    :disabled? disabled?}))
               available-visualizations)
        active-items (mapv
                      (fn [[key]]
                        (preferences key))
                      available-visualizations)]
    [button-group {:items items
                   :active-items active-items}]))

(defn direct-visualization-single-buttons [frame-id available-visualizations disabled?]
  (let [items (mapv
               (fn [[key {:keys [event sort-class tooltip-search] vertical-icon :icon}]]
                 (let [tooltip-text (if tooltip-search
                                      @(re-frame/subscribe tooltip-search)
                                      (error "tooltip-search not found for key " key))]
                   {:extra-class (str "tool-" (name key))
                    :title tooltip-text
                    :start-icon vertical-icon
                    :variant :primary
                    :on-click #(re-frame/dispatch [::open-visualization frame-id event])
                    :disabled? disabled?}))
               available-visualizations)]
    (reduce
     (fn [res item]
       (conj res
             [button item]))
     [:div.flex.gap-2]
     items)))

(defn direct-visualization [frame-id changed-flag]
  (let [change-possible? @(re-frame/subscribe [::component-active? frame-id])
        available-visualizations @(fi/call-api :service-category-sub :visual-option)
        read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                 {:frame-id frame-id
                                  :component :search
                                  :additional-info :direct-vis})
        di-creation-pending? @(re-frame/subscribe [::data-di/di-creation-pending? frame-id])
        disabled? (or read-only? di-creation-pending? changed-flag)]
    (if change-possible?
      [direct-visualization-button-group frame-id available-visualizations disabled?]
      [direct-visualization-single-buttons frame-id available-visualizations disabled?])))