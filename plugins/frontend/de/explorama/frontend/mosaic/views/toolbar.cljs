(ns de.explorama.frontend.mosaic.views.toolbar
  (:require [clojure.string :refer [lower-case]]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [goog.string.format]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.frontend.mosaic.data.graph-acs :as graph-acs]
            [de.explorama.frontend.mosaic.interaction.context-menu.shared :refer [desc-items
                                                                                  sort-by-items
                                                                                  sort-group-items]]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [re-frame.core :refer [dispatch reg-event-fx subscribe]]))

(reg-event-fx
 ::no-event
 (fn [_ _]
   {}))

(defn- operation-disabled? [frame-id]
  (or
   @(subscribe [:de.explorama.frontend.mosaic.views.frame-header/loading? frame-id])
   (not @(subscribe [:de.explorama.frontend.mosaic.views.frame-header/data-instance frame-id]))))

(defn- raster? [frame-id]
  (boolean (= :raster (get @(subscribe [::tasks/operations frame-id])
                           gcp/render-mode-key))))

(defn- scatter? [frame-id]
  (boolean (= :scatter (get @(subscribe [::tasks/operations frame-id])
                            gcp/render-mode-key))))

(defn- coupled? [frame-id]
  (boolean (seq @(fi/call-api :coupled-with-sub frame-id))))

(defn- uncoupled-raster-op? [frame-id]
  (and (raster? frame-id)
       (not (coupled? frame-id))))

(defn- tree? [frame-id]
  (boolean (= gcp/render-mode-key-treemap (get @(subscribe [::tasks/operations frame-id])
                                               gcp/render-mode-key))))

;;representation
(def decouple-elem {:icon :uncouple
                    :group "Representation"
                    :label :contextmenu-top-level-decouple
                    :disabled? operation-disabled?
                    :visible? coupled?
                    :on-click (fn [_e frame-id]
                                (dispatch (fi/call-api :decouple-event-vec frame-id)))})

(def grid-elem {:icon :grid
                :group "Representation"
                :label :contextmenu-grid
                :visible? (fn [frame-id] (not (coupled? frame-id)))
                :disabled? (fn [frame-id]
                             (or (operation-disabled? frame-id)
                                 @(fi/call-api [:interaction-mode :read-only-sub?]
                                               {:frame-id frame-id
                                                :component :mosaic
                                                :additional-info :grid})))
                :active? raster?
                :on-click (fn [_e frame-id]
                            (dispatch [::tasks/execute-wrapper frame-id :raster {}]))})

(def scatter-elem {:icon :scatter-plot
                   :group "Representation"
                   :label :scatter
                   :tooltip :contextmenu-scatter-plot
                   :disabled? (fn [frame-id]
                                (or (operation-disabled? frame-id)
                                    @(fi/call-api [:interaction-mode :read-only-sub?]
                                                  {:frame-id frame-id
                                                   :component :mosaic
                                                   :additional-info :scatter})))
                   :visible? (fn [frame-id] (not (coupled? frame-id)))
                   :active? scatter?
                   :on-click (fn [_e frame-id]
                               (dispatch [::tasks/execute-wrapper frame-id :scatter {}]))})

(def treemap-elem {:icon :treemap
                   :group "Representation"
                   :label "Tree"
                   :tooltip :tooltip-treemap
                   :disabled? (fn [frame-id]
                                (or (operation-disabled? frame-id)
                                    @(fi/call-api [:interaction-mode :read-only-sub?]
                                                  {:frame-id frame-id
                                                   :component :mosaic
                                                   :additional-info :tree})))
                   :visible? (fn [frame-id] (not (coupled? frame-id)))
                   :active? tree?
                   :on-click (fn [_e frame-id]
                               (dispatch [::tasks/execute-wrapper frame-id :treemap {}]))})

;;arrangement
(def optimize-elem {:icon :reset
                    :group "Arrangement"
                    :label :optimize
                    :tooltip :arrangement-tooltip-text
                    :disabled? operation-disabled?
                    :visible? (fn [frame-id]
                                (not (coupled? frame-id)))
                    :on-click (fn [_e frame-id]
                                (dispatch [:de.explorama.frontend.mosaic.event-logging/ui-wrapper
                                           frame-id
                                           "reset"
                                           {:path frame-id}]))})

(def fit-width-elem {:icon :reorder
                     :group "Arrangement"
                     :label :reorder-tooltip-text-vertical
                     :disabled? operation-disabled?
                     :visible? (fn [frame-id]
                                 (and (uncoupled-raster-op? frame-id)
                                      (boolean (:grp-key @(subscribe [::tasks/operations frame-id])))))
                     :on-click (fn [_e frame-id]
                                 (dispatch [:de.explorama.frontend.mosaic.views.frame/event-wrapper
                                            [::ddq/queue frame-id
                                             [:de.explorama.frontend.mosaic.render.actions/update frame-id :adjust-vertical?]]]))})

(def timeline-elem {:icon :timeline
                    :group "Arrangement"
                    :label :timeline
                    :tooltip :reorder-tooltip-text-horizontal
                    :disabled? operation-disabled?
                    :visible? (fn [frame-id]
                                (and (raster? frame-id)
                                     (boolean (:grp-key @(subscribe [::tasks/operations frame-id])))
                                     (not (coupled? frame-id))))
                    :on-click (fn [_e frame-id]
                                (let [coupled? (coupled? frame-id)]
                                  (dispatch [:de.explorama.frontend.mosaic.views.frame/event-wrapper
                                             [::ddq/queue frame-id
                                              [:de.explorama.frontend.mosaic.render.actions/update frame-id :adjust-one-row?]]])
                                  (when coupled?
                                    (dispatch (fi/call-api :couple-submit-action-vec
                                                           frame-id
                                                           {:order-timeline true})))))})

;;Operations
(def sort-elem {:icon :sort
                :group "Operations"
                :label :contextmenu-top-level-sortby
                :disabled? (fn [frame-id]
                             (or (operation-disabled? frame-id)
                                 @(fi/call-api [:interaction-mode :read-only-sub?]
                                               {:frame-id frame-id
                                                :component :mosaic
                                                :additional-info :sort-by})))
                :visible? (fn [frame-id]
                            (and (or (operation-disabled? frame-id)
                                     (raster? frame-id)
                                     (not @(fi/call-api [:interaction-mode :read-only-sub?]
                                                        {:frame-id frame-id
                                                         :component :mosaic
                                                         :additional-info :sort-by})))
                                 (not= gcp/render-mode-key-treemap
                                       (gcp/render-mode-key @(subscribe [::tasks/operations frame-id])))
                                 (not (coupled? frame-id))))
                :on-click (fn [_e frame-id]
                            (let [coupled? (coupled? frame-id)]
                              {:items (:sub-items (sort-by-items frame-id
                                                                 @(subscribe [::tasks/operations frame-id])
                                                                 coupled?))}))})

(def group-elem {:icon :group-by
                 :group "Operations"
                 :label :contextmenu-top-level-groupby
                 :disabled? (fn [frame-id]
                              (or (operation-disabled? frame-id)
                                  @(fi/call-api [:interaction-mode :read-only-sub?]
                                                {:frame-id frame-id
                                                 :component :mosaic
                                                 :additional-info :group-by})))
                 :visible? (fn [frame-id]
                             (or (operation-disabled? frame-id)
                                 (uncoupled-raster-op? frame-id)
                                 (= gcp/render-mode-key-treemap
                                    (gcp/render-mode-key @(subscribe [::tasks/operations frame-id])))))
                 :active? (fn [frame-id]
                            (boolean (:grp-key @(subscribe [::tasks/operations frame-id]))))
                 :on-click (fn [_e frame-id]
                             (let [options (->> @(subscribe [::graph-acs/group-by frame-id])
                                                (sort-by (fn [{:keys [name]}]
                                                           (lower-case name))))
                                   click-fn (fn [key]
                                              (dispatch [::tasks/execute-wrapper frame-id :group-by {:by key}]))]
                               {:items (mapv (fn [{:keys [name key]}]
                                               {:label name
                                                :on-click (partial click-fn key)})
                                             options)}))})

(def ungroup-elem {:icon :ungroup
                   :group "Operations"
                   :label :contextmenu-top-level-ungroup
                   :disabled? operation-disabled?
                   :visible? (fn [frame-id]
                               (and (uncoupled-raster-op? frame-id)
                                    (boolean (:grp-key @(subscribe [::tasks/operations frame-id])))))
                   :on-click (fn [_e frame-id]
                               (dispatch [::tasks/execute-wrapper frame-id :ungroup {}]))})


(def subgroup-elem {:icon :subgroup
                    :group "Operations"
                    :label :contextmenu-top-level-groupby-1
                    :disabled? operation-disabled?
                    :visible? (fn [frame-id]
                                (and (or (uncoupled-raster-op? frame-id)
                                         (= gcp/render-mode-key-treemap
                                            (gcp/render-mode-key @(subscribe [::tasks/operations frame-id]))))
                                     (boolean (:grp-key @(subscribe [::tasks/operations frame-id])))))
                    :active? (fn [frame-id]
                               (boolean (:sub-grp-key @(subscribe [::tasks/operations frame-id]))))
                    :on-click (fn [_e frame-id]
                                (let [options (->> @(subscribe [::graph-acs/group-by frame-id])
                                                   (sort-by (fn [{:keys [name]}]
                                                              (lower-case name))))
                                      click-fn (fn [key]
                                                 (dispatch [::tasks/execute-wrapper frame-id :sub-group-by {:by key}]))]
                                  {:items (mapv (fn [{:keys [name key]}]
                                                  {:label name
                                                   :on-click (partial click-fn key)})
                                                options)}))
                    :collapsible? true})

(def ungroup-subgroup-elem {:icon :unsubgroup
                            :group "Operations"
                            :label :contextmenu-top-level-ungroup-1
                            :disabled? operation-disabled?
                            :visible? (fn [frame-id]
                                        (and (or (uncoupled-raster-op? frame-id)
                                                 (= gcp/render-mode-key-treemap
                                                    (gcp/render-mode-key @(subscribe [::tasks/operations frame-id]))))
                                             (boolean (:sub-grp-key @(subscribe [::tasks/operations frame-id])))))
                            :on-click (fn [_e frame-id]
                                        (dispatch [::tasks/execute-wrapper frame-id :unsub-group {}]))})

(def sort-groups-elem {:icon :sort-group
                       :group "Operations"
                       :label :contextmenu-top-level-sort-group
                       :disabled? operation-disabled?
                       :visible? (fn [frame-id]
                                   (and (uncoupled-raster-op? frame-id)
                                        (boolean (:grp-key @(subscribe [::tasks/operations frame-id])))))
                       :on-click (fn [_e frame-id]
                                   (let [{sort-grp-desc gcp/sort-grp-key}
                                         @(subscribe [::tasks/operations frame-id])]
                                     {:items (:sub-items (sort-group-items frame-id sort-grp-desc :contextmenu-top-level-sort-group :sort-group-by))}))})

(def treemap-algorithm-elem {:icon :treemap-settings
                             :group "Operations"
                             :label :tooltip-treemap-algorithm
                             :disabled? operation-disabled?
                             :visible? (fn [frame-id]
                                         (= gcp/render-mode-key-treemap
                                            (gcp/render-mode-key @(subscribe [::tasks/operations frame-id]))))
                             :on-click (fn [_e frame-id]
                                         {:items [{:label (subscribe [::i18n/translate :treemap-algo-squared])
                                                   :on-click #(dispatch [::tasks/execute-wrapper frame-id :treemap-algorithm {:algo "squared"}])}
                                                  {:label (subscribe [::i18n/translate :treemap-algo-binary])
                                                   :on-click #(dispatch [::tasks/execute-wrapper frame-id :treemap-algorithm {:algo "binary"}])}
                                                  {:label (subscribe [::i18n/translate :treemap-algo-slice])
                                                   :on-click #(dispatch [::tasks/execute-wrapper frame-id :treemap-algorithm {:algo "slice"}])}]})})

(def sort-subgroups-elem {:icon :sort-subgroup
                          :group "Operations"
                          :label :contextmenu-top-level-sub-sort-group
                          :disabled? operation-disabled?
                          :visible? (fn [frame-id]
                                      (and (uncoupled-raster-op? frame-id)
                                           (boolean (:sub-grp-key @(subscribe [::tasks/operations frame-id])))))
                          :on-click (fn [_e frame-id]
                                      (let [{sub-sort-grp-desc gcp/sort-sub-grp-key}
                                            @(subscribe [::tasks/operations frame-id])]
                                        {:items (:sub-items (sort-group-items frame-id sub-sort-grp-desc :contextmenu-top-level-sub-sort-group :sort-sub-group-by))}))})

(defn- provided-elem [_frame-id elem-desc]
  (let [{:keys [label icon disabled? visible? sub-items]} elem-desc]
    (cond-> {:icon icon
             :group "Operations"
             :label label
             :disabled? operation-disabled?
             :on-click (fn [_e _frame-id]
                         {:items sub-items})}
      (not (nil? visible?))
      (assoc :visible? visible?)
      (not (nil? disabled?))
      (update :disabled? (fn [local-disabled-fn]
                           (fn [frame-id]
                             (or (local-disabled-fn frame-id)
                                 (disabled? frame-id))))))))

(def toolbar-impl
  {:on-duplicate-fn (fn [frame-id]
                      (dispatch [:de.explorama.frontend.mosaic.core/duplicate-mosaic-frame frame-id]))
   :on-toggle (fn [_frame-id _show?])
   :items (fn [frame-id]
            (let [service-elems @(fi/call-api :service-category-sub :operations)
                  service-elems (if (get service-elems "search-related-to")
                                  (update service-elems "search-related-to" (fn [desc]
                                                                              (update-in desc [:submenu :blacklist] (fnil conj []) "indicator")))
                                  service-elems)
                  provided-elems (->> service-elems
                                      (desc-items [] frame-id true))
                  read-only? @(fi/call-api [:interaction-mode :read-only-sub?] {:frame-id frame-id})
                  product-tour? @(fi/call-api [:product-tour :active?-sub])]
              ;;provided-elems = e.g related-to which is provided from search
              (cond
                product-tour? [grid-elem
                               scatter-elem
                               :divider
                               group-elem
                               sort-elem]
                read-only? []
                :else (into [decouple-elem
                             grid-elem
                             scatter-elem
                             treemap-elem
                             :divider
                             group-elem
                             ungroup-elem
                             subgroup-elem
                             ungroup-subgroup-elem
                             treemap-algorithm-elem
                             sort-elem
                             sort-groups-elem
                             sort-subgroups-elem
                             :filter]
                            (apply conj
                                   (mapv #(provided-elem frame-id %)
                                         provided-elems)
                                   [:divider
                                    optimize-elem
                                    fit-width-elem
                                    timeline-elem])))))})
