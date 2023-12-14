(ns de.explorama.frontend.reporting.views.legend
  (:require [clojure.set :refer [difference union]]
            [clojure.string :refer [join]]
            [data-format-lib.dates :as dfdates]
            [data-format-lib.filter :as dfl-filter]
            [data-format-lib.simplified-view :as dflsv]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.reporting.views.parameters :as parameters]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [re-frame.core :refer [dispatch reg-event-fx reg-sub subscribe]]
            [reagent.core :as r]
            [de.explorama.frontend.reporting.config :refer [legend-height]]
            [de.explorama.frontend.reporting.paths.discovery-base :as discovery-path]
            [de.explorama.frontend.reporting.util.frames :refer [handle-param]]))


(defn- legend-open-db-get [db frame-id]
  (let [visible? (boolean
                  (handle-param
                   (:visible? (fi/call-api :papi-api-db-get db (:vertical frame-id) :legend))))]
    (boolean (and
              (get-in db
                      (discovery-path/open-legend frame-id)
                      (fi/call-api :flags-db-get db frame-id :legend-default-open?))
              visible?))))

(defn legend-open? [frame-id]
  @(subscribe [::legend-open? frame-id]))

(reg-sub
 ::legend-desc
 (fn [db [_ frame-id]]
   (fi/call-api :papi-api-db-get db (:vertical frame-id) :legend)))

(reg-sub
 ::legend-open?
 (fn [db [_ frame-id]]
   (legend-open-db-get db frame-id)))

(reg-event-fx
 ::toggle-legend
 (fn [{db :db} [_ frame-id]]
   (let [db (assoc-in db
                      (discovery-path/open-legend frame-id)
                      (not (legend-open-db-get db frame-id)))]
     {:db db})))

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
  (let [{:keys [base local-filter?]
         {:keys [incomplete? di-filter-primitives local-filter-primitives]} :additional} @(di-desc-sub frame-id)
        primitives (merge-primitives di-filter-primitives local-filter-primitives)
        {:keys [legend-show-less
                legend-show-more
                legend-icomplete-filter-explanation]}
        @(subscribe [::i18n/translate-multi
                     :legend-show-less
                     :legend-show-more
                     :legend-icomplete-filter-explanation
                     :data-source-frame-title])]
    [:div.subsection__content
     [:dl
      [info-block "datasource"
       (:datasources base)]
      [info-block "year"
       (:years base)]
      [info-block "country"
       (:countries base)]]
     (when (or local-filter? (seq primitives))
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

(defn configuration-components [frame-id activate-configurations]
  (when (vector? activate-configurations)
    (let [show-actions? false]
      (reduce (fn [acc {:keys [module] :as configuration-props}]
                (let [config-module (cond
                                      (keyword? module)
                                      (:component @(fi/call-api :service-target-sub :config-module module))
                                      (fn? module)
                                      module)]
                  (cond-> acc
                    config-module
                    (conj
                     (with-meta
                       [config-module (assoc configuration-props
                                             :show-actions? show-actions?
                                             :edit-mode? false
                                             :frame-id frame-id
                                             :read-only? (fi/call-api [:interaction-mode :read-only-sub?]
                                                                      {:frame-id frame-id}))]
                       {:key (str ::legend-config frame-id module)})))))
              [:<>]
              activate-configurations))))

(defn- legend-label-wrapper [show? & childs]
  (if show?
    (apply conj
           [:div.panel__section.legend {:style {:overflow-y :unset}}
            [:div.section__title @(subscribe [::i18n/translate :legend-section-title])]]
           childs)
    (apply conj [:<>] childs)))

(defn- config-wrapper [parent? & childs]
  (if parent?
    (apply conj [:div.section__content] childs)
    (apply conj [:<>] childs)))

(defn- data-title-section [frame-id {:keys [data-display-count display-tooltip-info]
                                     :or {data-display-count (fn [& _] nil)}}]
  (let [main-title @(subscribe [::i18n/translate :data-section-title])
        {:keys [all-data displayed-data]} (val-or-deref (data-display-count frame-id))
        displayed-label @(subscribe [::i18n/translate :data-section-title-displayed])]
    [:div.section__title main-title
     (when all-data
       [:span
        (str " " (i18n/localized-number all-data) " Events")
        (when (and displayed-data
                   (not= all-data displayed-data))
          (str " / " (format displayed-label
                             (i18n/localized-number displayed-data))))
        (when (and displayed-data
                   (not= all-data displayed-data)
                   display-tooltip-info)
          [tooltip {:text (display-tooltip-info)}
           [icon {:icon :info-circle}]])])]))

(defn legend [_frame-id _legend-position _legend-active?]
  (let [show-more-state (r/atom false)]
    (r/create-class
     {:display-name "module legend"
      :reagent-render
      (fn [frame-id legend-position legend-active?]
        [error-boundary
         (let [{:keys [visible? di-desc-sub configuration] :as legend-desc}
               @(subscribe [::legend-desc frame-id])
               show-legend-label? (boolean configuration)]
           (when (and legend-active? visible? legend-desc)
             [:div.legend__panel {:style (cond-> {:display "flex"
                                                  ;; :min-height legend-height
                                                  :overflow-y :auto}
                                           (= legend-position :right)
                                           (assoc :max-width parameters/dashboard-legend-max-width)
                                           (= legend-position :bottom)
                                           (assoc :max-height parameters/dashboard-legend-max-height))}
              [legend-label-wrapper show-legend-label?
               [config-wrapper
                show-legend-label?
                [configuration-components frame-id configuration]]
               [:div.panel__section
                (when di-desc-sub
                  [:<>
                   [data-title-section frame-id legend-desc]
                   [:div.section__content>div.panel__subsection
                    [data-section frame-id legend-desc show-more-state]]])]]]))])})))

(defn legend-toggle [frame-id legend-active?]
  [button {:extra-class (cond-> "legend-toggle"
                          legend-active? (str " active"))
           :start-icon :info-square
           :title (subscribe [::i18n/translate :legend-section-title])
           :on-click #(dispatch [::toggle-legend frame-id])}])