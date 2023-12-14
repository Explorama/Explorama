(ns de.explorama.frontend.woco.frame.view.legend
  (:require [clojure.set :refer [difference union]]
            [clojure.string :refer [join]]
            [data-format-lib.dates :as dfdates]
            [data-format-lib.filter :as dfl-filter]
            [data-format-lib.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.woco.frame.view.overlay.filter :as filter-view]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [re-frame.core :as re-frame :refer [subscribe]]
            [reagent.core :as r]
            [de.explorama.frontend.woco.api.interaction-mode :as im-api]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.util :refer [handle-param]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.frame.size-position :refer [resize-info]]))

(defn- loc-num [attribute value]
  (cond-> value
    (and (number? value)
         (not (#{::dfdates/year ::dfdates/month}
               attribute)))
    (i18n/localized-number)))

(defn- merge-primitives [di-filter-primitives local-filter-primitives]
  (let [ignore-attributes #{"datasource" "country" ::dfdates/year}]
    (if-not (seq local-filter-primitives)
      (filterv (fn [{attr ::dfl-filter/prop}]
                 (not (ignore-attributes attr)))
               di-filter-primitives)
      (let [relevant-di-ops #{:not-in-range :not=}
            prepared-filter (reduce (fn [acc {attr ::dfl-filter/prop :as entry}]
                                      (update acc attr conj entry))
                                    {}
                                    local-filter-primitives)
            filter-attributes (set (keys prepared-filter))
            prepared-di (reduce (fn [acc {op ::dflsv/op attr ::dfl-filter/prop :as entry}]
                                  (cond-> acc
                                    (or (not (filter-attributes attr))
                                        (relevant-di-ops op))
                                    (update attr conj entry)))
                                {}
                                di-filter-primitives)
            di-attributes (set (keys prepared-di))
            apply-fn (fn [acc coll attr] (apply conj acc (get coll attr)))]
        (reduce (fn [acc attr]
                  (cond-> acc
                    (filter-attributes attr)
                    (apply-fn prepared-filter attr)
                    (di-attributes attr)
                    (apply-fn prepared-di attr)))
                []
                (sort-by (fn [val]
                           (cond
                             (= val ::dfdates/full-date) "date"
                             (= val ::dfdates/year) "year"
                             (= val ::dfdates/month) "month"
                             :else val))
                         (difference
                          (union filter-attributes di-attributes)
                          ignore-attributes)))))))

(defn- info-block
  ([label attribute tooltip parent-class]
   (let [label (cond
                 (= label ::dfdates/full-date) "date"
                 (= label ::dfdates/year) "year"
                 (= label ::dfdates/month) "month"
                 :else label)]
     [:div {:class parent-class}
      [:dt (i18n/attribute-label label)]
      [:dd (cond-> {:style {:overflow-x :hidden
                            :white-space :nowrap
                            :text-overflow :ellipsis
                            :max-width "65%"}}
             tooltip
             (assoc :title tooltip))
       attribute]]))
  ([label attribute tooltip]
   [info-block label attribute tooltip nil])
  ([label attribute]
   [info-block label attribute (i18n/attribute-label attribute)]))

(defn- more-data-row [{:data-format-lib.filter/keys [prop value]
                       op ::dflsv/op}]
  (let [lang @(subscribe [::i18n/current-language])
        value (if (= prop :data-format-lib.dates/month)
                (if (vector? value)
                  (mapv #(i18n/month-name % lang) value)
                  (i18n/month-name value lang))
                value)
        {:keys [legend-range-separator
                legend-not-operator]}
        @(subscribe [::i18n/translate-multi
                     :legend-range-separator
                     :legend-not-operator])
        to legend-range-separator
        v (case op
            :in (join "," value)
            (:in-range :not-in-range) (str
                                       (loc-num prop (first value))
                                       to
                                       (loc-num prop (second value)))
            (str (loc-num prop value)))
        o (case op
            (:in := :in-range) nil
            (:not= :not-in-range) legend-not-operator
            (name op))
        text (if o (str " " v) v)]

    [info-block
     prop
     [:<>
      (when o [:span.operator o])
      text]
     (str (or o "")
          text)]))

(defn- data-section [frame-id {:keys [di-desc-sub]} show-more-state]
  (let [{:keys [base]
         {:keys [incomplete? di-filter-primitives local-filter-primitives]} :additional} @(di-desc-sub frame-id)
        primitives (merge-primitives di-filter-primitives local-filter-primitives)
        ;; source-frame-id @(fi/call-api :connection-soure-main-frame-id-sub nil frame-id)
        ;; source-title (full-title source-frame-id)
        {:keys [legend-show-less
                legend-show-more
                legend-icomplete-filter-explanation
                data-source-frame-title]}
        @(subscribe [::i18n/translate-multi
                     :legend-show-less
                     :legend-show-more
                     :legend-icomplete-filter-explanation
                     :data-source-frame-title])]
    [:div.subsection__content
     [:dl
      ;; (when (and source-frame-id source-title)
      ;;   [info-block data-source-frame-title
      ;;    source-title
      ;;    source-title
      ;;    "source"])
      [info-block "datasource"
       (:datasources base)]
      [info-block "year"
       (:years base)]
      [info-block "country"
       (:countries base)]]
     (when (seq primitives)
       [:div.collapsible
        [:div.content {:class (when @show-more-state "open")}
         (when (seq primitives)
           (into [:dl] (map more-data-row) primitives))]
        [:a {:href "#"
             :on-click #(swap! show-more-state not)}
         [:div.collapsible__bar>div.label>span
          (if @show-more-state
            legend-show-less
            legend-show-more)]]])
     (when incomplete?
       [:div.data__hint
        [icon {:icon :info-circle}]
        legend-icomplete-filter-explanation])]))

(defn legend-open?
  ([db frame-id]
   (let [visible? (boolean
                   (handle-param
                    (get-in db (path/vertical-plugin-api frame-id :legend :visible?))))]
     (boolean (and
               (get-in db
                       (path/frame-open-legend frame-id)
                       (fi/call-api :flags-db-get db frame-id :legend-default-open?))
               visible?))))
  ([frame-id]
   @(subscribe [::legend-open? frame-id])))

(re-frame/reg-sub
 ::legend-open?
 (fn [db [_ frame-id]]
   (legend-open? db frame-id)))

(defn configuration-components [frame-focus? frame-id activate-configurations maximized-view maximized-view-id maximized-data edit-mode?]
  (when (vector? activate-configurations)
    (let [show-actions? (boolean
                         (and (val-or-deref frame-focus?)
                              (not @(fi/call-api [:interaction-mode :read-only-sub?]
                                                 {:frame-id frame-id}))))]
      (reduce (fn [acc {:keys [module] :as configuration-props}]
                (let [config-module (cond
                                      (keyword? module)
                                      (:component @(fi/call-api :service-target-sub :config-module module))
                                      (fn? module)
                                      module)]
                  (cond-> acc
                    (and config-module (or (not maximized-view-id)
                                           (= maximized-view-id module)))
                    (conj
                     (with-meta
                       [config-module (assoc configuration-props
                                             :show-actions? show-actions?
                                             :edit-mode? edit-mode?
                                             :on-maximize (fn [new-maximized-data maximized-module]
                                                            (reset! maximized-data new-maximized-data)
                                                            (reset! maximized-view (or maximized-module [:config-module module])))
                                             :on-normalize (fn []
                                                             (reset! maximized-view nil)
                                                             (reset! maximized-data nil))
                                             :is-maximized? (boolean maximized-view-id)
                                             :maximized-data maximized-data
                                             :frame-id frame-id
                                             :read-only? (fi/call-api [:interaction-mode :read-only-sub?]
                                                                      {:frame-id frame-id}))]
                       {:key (str ::legend-config frame-id module)})))))
              [:<>]
              activate-configurations))))

(defn- legend-label-wrapper [show? & childs]
  (if show?
    (apply conj
           [:div.panel__section.legend
            [:div.section__title @(subscribe [::i18n/translate :legend-section-title])]]
           childs)
    (apply conj [:<>] childs)))

(defn- config-wrapper [parent? & childs]
  (if parent?
    (apply conj [:div.section__content] childs)
    (apply conj [:<>] childs)))

(defn- filtered-data-tooltip [all-data local-data]
  (let [all-filter-tooltip-text @(re-frame/subscribe [::i18n/translate :all-filter-tooltip-text])
        filtered-tooltip-text @(re-frame/subscribe [::i18n/translate :filtered-tooltip-text])]
    (str all-filter-tooltip-text
         ": "
         (i18n/localized-number all-data)
         "\n "
         filtered-tooltip-text
         ": "
         (i18n/localized-number local-data))))

(defn- data-title-section [frame-id {:keys [data-display-count display-tooltip-info]
                                     :or {data-display-count (fn [& _] nil)}}]
  (let [main-title @(subscribe [::i18n/translate :data-section-title])
        {:keys [all-data local-data displayed-data]} (val-or-deref (data-display-count frame-id))
        displayed-label @(subscribe [::i18n/translate :data-section-title-displayed])
        filter-active? @(subscribe [::filter-view/filter-active? frame-id])]
    [:div.section__title main-title
     (when all-data
       [:span
        (when (and displayed-data
                   (not= all-data displayed-data))
          (format displayed-label
                  (i18n/localized-number displayed-data)))
        (str " "
             (i18n/localized-number (or local-data all-data))
             " Events")
        (when (and displayed-data
                   (not= all-data displayed-data)
                   display-tooltip-info)
          [tooltip {:text (display-tooltip-info)}
           [icon {:icon :info-circle}]])
        (when (and filter-active? local-data)
          [tooltip {:text (filtered-data-tooltip all-data local-data)}
           [icon {:icon :filter}]])])]))

(defn legend [frame-id _]
  (let [maximized-view (r/atom nil)
        maximized-data (r/atom nil)
        show-more-state (r/atom false)]
    (r/create-class
     {:display-name "window legend"
      :reagent-render
      (fn [frame-id frame-focus?]
        [error-boundary
         (let [{:keys [di-desc-sub configuration] :as legend-desc}
               @(subscribe [:de.explorama.frontend.woco.frame.plugin-api/legend frame-id])
               [maximized-view-type maximized-view-id] @maximized-view
               show-legend-label? (and configuration (not maximized-view-type))]
           (when legend-desc
             [:div.legend__panel {:style {:display "flex"}}
              [legend-label-wrapper show-legend-label?
               (when (or (not maximized-view-type)
                         (= maximized-view-type :config-module))
                 [config-wrapper
                  (and configuration (not (= maximized-view-type :config-module)))
                  [configuration-components frame-focus? frame-id configuration maximized-view maximized-view-id maximized-data]])]
              (when (or (not maximized-view-type)
                        (= maximized-view-type :data-section))
                [:div.panel__section
                 (when di-desc-sub
                   [:<>
                    [data-title-section frame-id legend-desc]
                    [:div.section__content>div.panel__subsection
                     [data-section frame-id legend-desc show-more-state]]])])]))])})))

(defn trigger-legend-resize [{[fw fh] :full-size
                              [cw ch] :size
                              frame-id :id}
                             show-legend?]
  (let [delta-w (cond-> config/legend-width
                  (not show-legend?) (* -1))
        resize-infos (resize-info delta-w 0 cw ch (+ fw delta-w) fh :force?)]
                              ;; coupled-with (assoc :recalc-positions? true)})]+
    [[:dispatch [:de.explorama.frontend.woco.frame.view.core/resize-stop frame-id resize-infos]]]))

(re-frame/reg-event-fx
 ::toggle-legend
 (fn [{db :db} [_ frame-id flag]]
   (when-let [frame-desc (get-in db (path/frame-desc frame-id))]
     (when (or (nil? flag)
               (not= flag (legend-open? db frame-id)))
       (let [db (assoc-in db
                          (path/frame-open-legend frame-id)
                          (if (boolean? flag)
                            flag
                            (not (legend-open? db frame-id))))
             flag (get-in db (path/frame-open-legend frame-id))
             fx (cond-> (or (trigger-legend-resize frame-desc flag)
                            [])
                  (im-api/render? db)
                  (conj [:dispatch [:de.explorama.frontend.woco.event-logging/log-frame-event frame-id]]))]
         {:db db
          :fx fx})))))