(ns de.explorama.frontend.search.views.search-bar
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.components.common.label :refer [label]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field loading-message button]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.search.views.validation :as validation]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.shared.search.ws-api :as ws-api]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.backend.util :refer [build-options-request-params]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.frontend.search.config :as config]
            [clojure.string :as string]))

(defonce search-fields (atom {}))

(defn- new-search-field [frame-id]
  (let [search-field (reagent/atom "")]
    (swap! search-fields assoc frame-id search-field)
    search-field))

(defn- reset-search-field [frame-id]
  (swap! search-fields
         update
         frame-id
         (fn [field]
           (reset! field "")
           field)))

(defn- remove-search-field [frame-id]
  (swap! search-fields dissoc frame-id))

(defonce search-loading-state (reagent/atom {}))

(defn- loading? [frame-id]
  (get @search-loading-state frame-id false))

(defn- set-loading-state [frame-id loading?]
  (swap! search-loading-state assoc frame-id loading?))

(defn- search-term-valid? [search-term]
  (and
   (not (nil? search-term))
   (>= (count (string/trim search-term))
       3)))

(re-frame/reg-event-fx
 ::search-elements
 (fn [{db :db} [_ frame-id search-term]]
   (let [compl-formdata (get-in db (spath/frame-search-rows frame-id))
         {:keys [formdata]} (build-options-request-params db frame-id nil compl-formdata false)
         config (get-in db spath/search-bar-config)
         search-task-id (str (random-uuid))
         datasources (get-in db spath/search-enabled-datasources)]
     (when (search-term-valid? search-term)
       (set-loading-state frame-id true)
       {:db (-> db
                (assoc-in (spath/search-bar-frame-results frame-id) {})
                (assoc-in (spath/search-bar-frame-result-open? frame-id) true)
                (assoc-in (spath/search-bar-frame-task-id frame-id) search-task-id))
        :dispatch [ws-api/search-bar-find-elements datasources frame-id search-term formdata config search-task-id]}))))

(re-frame/reg-event-db
 ::set-search-result
 (fn [db [_ frame-id search-key search-result task-id]]
   (let [searching? (loading? frame-id)
         current-task-id (get-in db (spath/search-bar-frame-task-id frame-id))]
     (if (and searching?
              (= current-task-id task-id))
       (assoc-in db (spath/search-bar-frame-result frame-id search-key) search-result)
       db))))

(re-frame/reg-sub
 ::search-results
 (fn [db [_ frame-id]]
   (get-in db (spath/search-bar-frame-results frame-id))))

(re-frame/reg-event-db
 ::reset
 (fn [db [_ frame-id]]
   (set-loading-state frame-id false)
   (-> db
       (assoc-in (spath/search-bar-frame-results frame-id) {})
       (assoc-in (spath/search-bar-frame-result-open? frame-id) false))))

(re-frame/reg-sub
 ::results-open?
 (fn [db [_ frame-id]]
   (get-in db (spath/search-bar-frame-result-open? frame-id))))

(re-frame/reg-event-fx
 ::search-done
 (fn [{db :db} [_ frame-id task-id]]
   (let [current-task-id (get-in db (spath/search-bar-frame-task-id frame-id))]
     (when (= current-task-id task-id)
       (set-loading-state frame-id false)
       {:db (update-in db
                       (spath/search-bar-frame frame-id)
                       dissoc :task-id)}))))

(defn- label-with-highlight [{:keys [prefix highlight suffix]} on-click]
  [:li {:on-click on-click}
   prefix
   [:span.highlight highlight]
   suffix])

(defn- add-search-row [frame-id attr-desc value]
  (re-frame/dispatch [:de.explorama.frontend.search.api.core/add-search-row
                      attr-desc
                      value
                      false
                      frame-id])
  (reset-search-field frame-id)
  (re-frame/dispatch [::reset frame-id]))

(defn- found-attribute-row [frame-id {:keys [label on-click-value]}]
  [label-with-highlight
   label
   #(add-search-row frame-id on-click-value nil)])

(defn- characteristic-row-values [frame-id attr-desc found-vals]
  [:ul.list__entries
   (map (fn [{:keys [label on-click-value]}]
          (with-meta
            [label-with-highlight
             label
             #(add-search-row frame-id attr-desc [{:label on-click-value}])]
            {:key (str "attr-" attr-desc "-" frame-id "-" label)}))
        (take 5 found-vals))])

(defn- characteristic-row-label [frame-id attr-desc found-vals]
  (let [select-all-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-results-select-all])
        warning-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-warn-select-all])
        val-count (count found-vals)
        labels @(fi/call-api [:i18n :get-labels-sub])
        attribute-name (first attr-desc)]
    [:div.list__title
     (get labels attribute-name attribute-name)
     [:span.entries__number " (" val-count ")"]
     (if (> val-count config/max-show-all)
       [:span.select__all
        {:style {:color "#5e5e5e"}} ;TODO r1/css create a class for this
        warning-label]
       [:span.select__all
        {:on-click #(add-search-row frame-id attr-desc (mapv (fn [{:keys [on-click-value]}]
                                                               {:label on-click-value}) found-vals))}
        select-all-label])]))

(defn result-view [frame-id]
  (let [attributes-list-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-result-attributes-label])
        characteristics-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-result-charateristic-label])
        result-hint-constraint @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-result-hint-constraint])
        rusult-hint-no-notes @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-result-hint-no-notes])
        search-results @(re-frame/subscribe [::search-results frame-id])
        search-loading? (loading? frame-id)
        attributes-results (get search-results :attributes)
        attribute-value-results (dissoc search-results :attributes)
        attributes-submitable? @(re-frame/subscribe [::validation/search-formdata-valid? frame-id])]
    [:div.window__wrapper__searchlist
     [:div.list__section
      [:p
       (when attributes-submitable?
         [:<> result-hint-constraint [:br]])
       rusult-hint-no-notes]]
     [:div.list__section
      [:h2
       (format attributes-list-label
               (count attributes-results))
       [loading-message {:show? (and (nil? attributes-results)
                                     search-loading?)}]]
      (reduce (fn [acc result-desc]
                (conj acc (with-meta
                            [found-attribute-row frame-id result-desc]
                            {:key (str "attr-" result-desc "-" frame-id)})))
              [:ul.list__attributes]
              attributes-results)]
     [:div.list__section
      [:h2 characteristics-label
       (when (and (not search-loading?)
                  (every? (fn [[_ r]] (empty? r)) attribute-value-results))
         " (0)")
       [loading-message {:show? search-loading?}]]
      (map
       (fn [[[attr-name _ :as attr-desc] found-vals]]
         (with-meta
           [:div.list__subsection
            [characteristic-row-label frame-id attr-desc found-vals]
            [characteristic-row-values frame-id attr-desc found-vals]]
           {:key (str "attr-values-result" attr-name "-" frame-id)}))
       (filter
        (fn [[_ found-vals]]
          (> (count found-vals) 0))
        attribute-value-results))]]))

(defn bar [frame-id]
  (let [search (new-search-field frame-id)
        request-delay (atom nil)]
    (reagent/create-class
     {:component-will-unmount (fn []
                                (remove-search-field frame-id)
                                (set-loading-state frame-id false))
      :reagent-render
      (fn [frame-id]
        (let [search-label (re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-label])
              search-placeholder (re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-bar-placeholder])
              read-only? (not @(fi/call-api [:interaction-mode :normal-sub?] {:frame-id frame-id}))]
          [input-field {:start-icon :search
                        :extra-class "input--w100"
                        :disabled? read-only?
                        :placeholder search-placeholder
                        :on-key-up (fn [ev]
                                     (when (= (.-keyCode ev) 27) ; Escape
                                       (reset-search-field frame-id)
                                       (re-frame/dispatch [::reset frame-id])))
                        :on-change (fn [val]
                                     (reset! search val)
                                     (when (= (count val) 0)
                                       (re-frame/dispatch [::reset frame-id]))
                                     (when (search-term-valid? val)
                                       (when @request-delay
                                         (js/clearTimeout @request-delay))
                                       (reset! request-delay
                                               (js/setTimeout
                                                #(re-frame/dispatch [::search-elements frame-id @search])
                                                config/search-bar-request-delay))))
                        :value search}]))})))
