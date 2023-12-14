(ns de.explorama.frontend.data-atlas.views.core
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.data-atlas.db-utils :as db-utils]
            [de.explorama.frontend.data-atlas.path :as db-path]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                            input-field]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [de.explorama.shared.data-atlas.ws-api :as ws-api]
            [goog.string :as gstring]
            [goog.string.format]
            [re-frame.core :as re-frame]
            [react-window :refer [FixedSizeList]]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [debug]]))

(re-frame/reg-event-db
 ::title-set-logging
 (fn [db [_ frame-id]]
   (update-in db db-path/replay dissoc frame-id)))

(re-frame/reg-event-fx
 ::no-event
 (fn [_ _]
   {}))

(def frame-header-impl
  {:frame-icon :atlas
   :frame-title-sub (fn [frame-id] (atom nil))
   :frame-title-prefix-sub (fn [frame-id vertical-count-number]
                             (re-frame/subscribe [::i18n/translate :vertical-label-data-atlas]))
   :can-change-title? false
   :on-minimize-event (fn [frame-id] [::no-event])
   :on-maximize-event (fn [frame-id] [::no-event])
   :on-normalize-event (fn [frame-id] [::no-event])
   :on-close-fn (fn [frame-id done-fn]
                  (done-fn))})

(def toolbar-impl
  {})

(def product-tour-impl
  {:component :data-atlas})

(defn data-element-list
  [frame-id element-type selected-item matches list-atom]
  (let [element-type-label @(re-frame/subscribe [::i18n/translate (keyword (str (name element-type) "-list-label"))])
        attr-labels @(fi/call-api [:i18n :get-labels-sub])
        sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)]
    [:div.data__list
     [:h1 element-type-label
      (when (= element-type :characteristic)
        [:span.note @(re-frame/subscribe [::i18n/translate :characteristic-max-list-label])])]
     [:> FixedSizeList
      {:item-count (count matches)
       :height 200
       :item-size 25
       :ref (partial reset! list-atom)}
      (reagent/reactify-component
       (fn [{:keys [index style]}]
         (let [element-not-found (str "no element at " index)
               item (get matches index element-not-found)
               display-item (get item 2)]
           [:div {:class ["data__element"
                          (when (= item selected-item) "active")]
                  :style style}
            [:a {:href "#"
                 :on-click #(do
                              (sync-event-fn [::data-element-clicked frame-id element-type item])
                              (re-frame/dispatch [::data-element-clicked frame-id element-type item]))}
             (if (map? display-item) (get attr-labels (get display-item :value) (:value display-item)) display-item)]])))]]))

(defn data-source-list
  [frame-id selected-ds data-sources temp-datasources list-atom]
  (let [datasource-label @(re-frame/subscribe [::i18n/translate :data-source-list-label])
        temp-label @(re-frame/subscribe [::i18n/translate :temp-data-source-list-label])
        is-temp? (->> temp-datasources (map #(get % 2)) set)
        matches (into data-sources temp-datasources)
        sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)]
    [:div.data__list
     [:h1 datasource-label
      [:span.note temp-label]]
     [:> FixedSizeList
      {:item-count (count matches)
       :height 200
       :item-size 25
       :ref (partial reset! list-atom)}
      (reagent/reactify-component
       (fn [{:keys [index style]}]
         (let [element-not-found (str "no element at " index)
               item (get matches index element-not-found)
               display-item (get item 2)]
           [:div {:class ["data__element"
                          (when (= item selected-ds) "active")]
                  :style style}
            [:a {:href "#"
                 :on-click (fn []
                             (let [element-typ (if (is-temp? display-item)
                                                 :temp-data-source
                                                 :data-source)]
                               (sync-event-fn [::data-element-clicked frame-id element-typ item])
                               (re-frame/dispatch [::data-element-clicked frame-id element-typ item])))}
             display-item
             (when (is-temp? display-item)
               [:span.temp_data_source
                {:style {:color "#0292b5"
                         :font-size "small"}}
                "*"])]])))]]))

(defn toggle [current-item new-item]
  (when (not= current-item new-item)
    new-item))

(re-frame/reg-event-fx
 ::data-element-clicked
 (fn [{db :db} [_ frame-id element-type item]]
   (debug ::data-element-clicked element-type item)
   (let [path (db-path/data-selection frame-id)
         db' (as-> (db-utils/frame-exist-guard
                    frame-id
                    db
                    (update-in db (conj path element-type) toggle item))
                   $
               (db-utils/frame-exist-guard
                frame-id
                $
                (assoc-in $ (db-path/data-search frame-id) "")))
         selection (get-in db' path)]
     (debug (.now js/Date) "selection" selection)
     {:db db'
      :dispatch [::get-data-elements
                 frame-id
                 selection
                 nil
                 (i18n/current-language db')]})))

(re-frame/reg-sub
 ::data-sources
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-lists-data-sources frame-id))))

(re-frame/reg-sub
 ::temp-data-sources
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-lists-temp-data-sources frame-id))))

(re-frame/reg-sub
 ::attributes
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-lists-attributes frame-id))))

(re-frame/reg-sub
 ::characteristics
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-lists-characteristics frame-id))))

(re-frame/reg-sub
 ::selected-data-source
 (fn [db [_  frame-id]]
   (or (get-in db (db-path/data-selection-data-source frame-id))
       (get-in db (db-path/data-selection-temp-data-source frame-id)))))

(re-frame/reg-sub
 ::selected-attribute
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-selection-attribute frame-id))))

(re-frame/reg-sub
 ::selected-characteristic
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-selection-characteristic frame-id))))

(re-frame/reg-sub
 ::search
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-search frame-id))))

(re-frame/reg-sub
 ::descriptions
 (fn [db [_  frame-id]]
   (get-in db (db-path/data-descriptions frame-id))))

(re-frame/reg-sub
 ::attributes-ranges
 (fn [db [_ frame-id]]
   (get-in db (db-path/data-lists-attributes-ranges frame-id))))

(defn- rename-node-type
  "Renaming has the goal to prevent confused users with technical informations
   like Fact which naming is different in other services like temp. importer"
  [node-type]
  (case node-type
    "Fact" "Property"
    "Feature" "Interaction"
    node-type))

(defmulti describe (fn [desc _labels _extra-params] (first desc)))
(defmethod describe :data-source [[_ v] labels _]
  [:h1 [:span (str (:data-source labels) ": ")] v])
(defmethod describe :temp-data-source [[_ v] labels _]
  [:h1 [:span (str (:temp-data-source labels) ": ")] v])
(defmethod describe :attribute
  [[_ v type-info] labels {:keys [ranges]
                           [context-type context-name] :context}]
  (let [data-type-lable @(re-frame/subscribe [::i18n/translate :data-type-label])
        attr-labels @(fi/call-api [:i18n :get-labels-sub])
        attr-range (when (or (= context-type :data-source)
                             (= context-type :temp-data-source))
                     (get-in ranges [context-name v]))
        {:keys [min max]} attr-range
        {min-label :min
         max-label :max} @(re-frame/subscribe [::i18n/translate-multi :min :max])]

    [:h2
     [:span (str (:attribute labels) ": ")]
     (if-let [[node-type attribute-type] type-info]
       (let [v (if (map? v) (get v :label) v)]
         (gstring/format "%s (%s: %s, %s)" (get attr-labels v v) data-type-lable (rename-node-type node-type) attribute-type))
       v)
     (when attr-range
       [:div {:class ["flex"
                      "flex-wrap"
                      "align-items-center"
                      "gap-x-4"
                      "py-4"
                      "text-xs"]}
        [:span min-label ": " (i18n/localized-number min)
         (gstring/unescapeEntities " &#8211; ")]
        [:span max-label ": " (i18n/localized-number max)]])]))

(defmethod describe :characteristic [[_ v] labels _]
  [:h3 [:span (str (:characteristic labels) ": ")] v])

(defn describe-hierarchy [desc labels extra-params]
  (if (map? desc)
    (mapcat (fn [[k d]] (cons (describe k labels extra-params)
                              (describe-hierarchy d labels (assoc extra-params :context k))))
            (sort desc))
    (when desc
      [[:p [:span (str (:info labels) ": ")] desc]])))

(re-frame/reg-sub
 ::bring-to-search-active?
 (fn [db [_ frame-id]]
   (let [ds (get-in db (db-path/data-selection-data-source frame-id))
         temp-ds (get-in db (db-path/data-selection-temp-data-source frame-id))
         data-source-selection (or ds temp-ds)
         attribute-selection (get-in db (db-path/data-selection-attribute frame-id))
         characteristic-selection (get-in db (db-path/data-selection-characteristic frame-id))
         possible-attributes (second (get-in db (db-path/data-lists-attributes frame-id)))]
     (or (and data-source-selection
              attribute-selection
              characteristic-selection)
         (and data-source-selection
              characteristic-selection
              (= (count possible-attributes) 1))
         (and data-source-selection
              attribute-selection)
         (and data-source-selection
              (not attribute-selection)
              (not characteristic-selection))
         (and characteristic-selection
              (or (= (count possible-attributes) 1)
                  attribute-selection))
         (and attribute-selection
              (not characteristic-selection)
              (not data-source-selection))
         characteristic-selection))))

(re-frame/reg-event-fx
 ::bring-to-search
 (fn [{db :db} [_ frame-id]]
   (let [data-source-selection (get-in db (db-path/data-selection-data-source frame-id))
         temp-ds (get-in db (db-path/data-selection-temp-data-source frame-id))
         attribute-selection (get-in db (db-path/data-selection-attribute frame-id))
         characteristic-selection (get-in db (db-path/data-selection-characteristic frame-id))
         open-rows (cond-> []
                     data-source-selection
                     (conj [attrs/datasource-attr (get data-source-selection 2)])

                     temp-ds
                     (conj [attrs/datasource-attr (get temp-ds 2)])

                     (and attribute-selection
                          (not characteristic-selection))
                     (conj [(get-in attribute-selection [2 :value]) nil])

                     (and attribute-selection
                          characteristic-selection)
                     (conj [(get-in attribute-selection [2 :value])
                            (get characteristic-selection 2)])

                     (and (not attribute-selection)
                          characteristic-selection)
                     (conj [(get-in characteristic-selection [0 :value])
                            (get characteristic-selection 2)]))]
     {:dispatch-n [(when (seq open-rows)
                     (fi/call-api :open-search-with-rows-event-vec db open-rows false))]})))

(defn search-container [frame-id disable-reset-selection?]
  (let [search-placeholder @(re-frame/subscribe [::i18n/translate :search-placeholder])
        search-label @(re-frame/subscribe [::i18n/translate :search-label])
        reset-selection-label @(re-frame/subscribe [::i18n/translate :reset-selection-label])
        bring-to-search-label @(re-frame/subscribe [::i18n/translate :bring-to-search-label])
        bring-to-search-active? @(re-frame/subscribe [::bring-to-search-active? frame-id])
        bring-to-search-disabled-hint @(re-frame/subscribe [::i18n/translate :bring-to-search-disabled-hint])
        search-term @(re-frame/subscribe [::search frame-id])
        search-disabled? (str/blank? search-term)
        sync-event-fn @(fi/call-api :service-target-sub :project-fns :event-sync)]
    [:div.data__search__container
     [input-field (cond-> {:placeholder search-placeholder
                           :on-change (fn [new-val]
                                        (sync-event-fn [::data-search-term frame-id (str/triml new-val)])
                                        (re-frame/dispatch [::data-search-term frame-id (str/triml new-val)]))
                           :on-clear #(when-not search-disabled?
                                        (sync-event-fn [::data-search-term frame-id ""])
                                        (sync-event-fn [::data-search frame-id])

                                        (re-frame/dispatch [::data-search-term frame-id ""])
                                        (re-frame/dispatch [::data-search frame-id]))
                           :on-key-up (fn [event]
                                        (when (= 13 (aget event "which"))
                                          (sync-event-fn [::data-search frame-id])
                                          (re-frame/dispatch [::data-search frame-id])))}
                    search-term (assoc :value search-term))]
     [button {:label search-label
              :on-click #(do
                           (sync-event-fn [::data-search frame-id])
                           (re-frame/dispatch [::data-search frame-id]))}]
     [:div.data__reset
      [button {:label bring-to-search-label
               :start-icon :search
               :variant :secondary
               :disabled? (not bring-to-search-active?)
               :on-click #(do
                            (sync-event-fn [::bring-to-search frame-id])
                            (re-frame/dispatch [::bring-to-search frame-id]))}]
      [button {:label reset-selection-label
               :start-icon :reset
               :variant :secondary
               :on-click #(do
                            (sync-event-fn [::selection-reset frame-id])
                            (re-frame/dispatch [::selection-reset frame-id]))
               :disabled? disable-reset-selection?}]]]))

(defn main-panel [frame-id _]
  (let [ds-list-atom (atom nil)
        a-list-atom (atom nil)
        c-list-atom (atom nil)]
    (re-frame/dispatch [::set-list-component-atoms frame-id {:data-source ds-list-atom}
                        :attribute a-list-atom
                        :characteristic c-list-atom])
    (fn [frame-id {:keys [drag-props] :as vertical-frame-props}]
      (let [data-source-detail-label @(re-frame/subscribe [::i18n/translate :data-source-detail-label])
            temp-data-source-detail-label @(re-frame/subscribe [::i18n/translate :temp-data-source-detail-label])
            attribute-detail-label @(re-frame/subscribe [::i18n/translate :attribute-detail-label])
            characteristic-detail-label @(re-frame/subscribe [::i18n/translate :characteristic-detail-label])
            info-detail-label @(re-frame/subscribe [::i18n/translate :info-detail-label])
            {:keys [is-minimized? is-maximized?]} @(fi/call-api :frame-sub frame-id)
            data-sources @(re-frame/subscribe [::data-sources frame-id])
            temp-data-sources @(re-frame/subscribe [::temp-data-sources frame-id])
            attributes @(re-frame/subscribe [::attributes frame-id])
            characteristics @(re-frame/subscribe [::characteristics frame-id])
            selected-data-source @(re-frame/subscribe [::selected-data-source frame-id])
            selected-attribute @(re-frame/subscribe [::selected-attribute frame-id])
            selected-characteristic @(re-frame/subscribe [::selected-characteristic frame-id])
            descriptions @(re-frame/subscribe [::descriptions frame-id])
            disable-reset-selection? (every? nil? [selected-data-source
                                                   selected-attribute
                                                   selected-characteristic])
            attribute-ranges @(re-frame/subscribe [::attributes-ranges frame-id])]
        [:<>
         (when-not is-minimized?
           [:div.window__body {:style {:display (when is-minimized?
                                                  "none")}}
            [search-container frame-id disable-reset-selection?]
            [:div.data__lists__container
             [data-source-list
              frame-id
              selected-data-source
              data-sources
              temp-data-sources
              ds-list-atom]
             [data-element-list
              frame-id
              :attribute
              selected-attribute
              attributes
              a-list-atom]
             [data-element-list
              frame-id
              :characteristic
              selected-characteristic
              characteristics
              c-list-atom]]
            (into [:div.data__details__container]
                  (describe-hierarchy descriptions
                                      {:data-source data-source-detail-label
                                       :temp-data-source temp-data-source-detail-label
                                       :attribute attribute-detail-label
                                       :characteristic characteristic-detail-label
                                       :info info-detail-label}
                                      {:ranges attribute-ranges}))])]))))

(re-frame/reg-event-db
 ::data-search-term
 (fn [db [_ frame-id query]]
   (db-utils/frame-exist-guard
    frame-id
    db
    (assoc-in db (db-path/data-search frame-id) query))))

(re-frame/reg-event-fx
 ::data-search
 (fn [{db :db} [_ frame-id query]]
   (debug (.now js/Date) ::data-search query)
   {:dispatch [::get-data-elements
               frame-id
               (get-in db (db-path/data-selection frame-id))
               (str/trim (get-in db (db-path/data-search frame-id) ""))
               (i18n/current-language db)]}))

(re-frame/reg-event-fx
 ::selection-reset
 (fn [{db :db} [_ frame-id]]
   (let [db' (db-utils/frame-exist-guard
              frame-id
              db
              (assoc-in db (db-path/data-selection frame-id) {}))]
     (debug (.now js/Date) "selection reset")
     {:db db'
      :dispatch [::get-data-elements
                 frame-id
                 {}
                 (get-in db' (db-path/data-search frame-id))
                 (i18n/current-language db')]})))

(re-frame/reg-event-fx
 ::get-data-elements
 (fn [{db :db} [_ frame-id selection search-term lang]]
   (debug (.now js/Date) "getting data elments")
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:dispatch-later {:dispatch [::set-loading frame-id true (.now js/Date)]
                       :ms 300}
      :backend-tube [ws-api/get-data-elements-route
                     {:client-callback [ws-api/get-data-elements-result frame-id]}
                     user-info
                     frame-id
                     selection
                     search-term
                     lang]})))

(re-frame/reg-event-db
 ::set-list-component-atoms
 (fn [db [_ frame-id list-atom-map]]
   (db-utils/frame-exist-guard
    frame-id
    db
    (assoc-in db (db-path/list-component-atoms frame-id) list-atom-map))))

(re-frame/reg-event-fx
 ::data-elements
 (fn [{db :db} [_ frame-id data-elements]]
   (debug (.now js/Date) ::data-elements frame-id data-elements)
   (run! #(when-let [react-window-list @%] (.scrollTo react-window-list 0))
         (vals (get-in db (db-path/list-component-atoms frame-id))))
   {:db  (db-utils/frame-exist-guard
          frame-id
          db
          (update-in db (db-path/data-lists frame-id) merge data-elements))
    :dispatch [::set-loading frame-id false (.now js/Date)]}))

(re-frame/reg-event-db
 ::descriptions
 (fn [db [_ frame-id descriptions]]
   (debug (.now js/Date) :data-atlas.description/descriptions frame-id descriptions)
   (db-utils/frame-exist-guard
    frame-id
    db
    (assoc-in db (db-path/data-descriptions frame-id) descriptions))))

(re-frame/reg-event-fx
 ws-api/get-data-elements-result
 (fn [{db :db} [_ frame-id data-elements data-descriptions]]
   {:fx [[:dispatch [::data-elements frame-id data-elements]]
         [:dispatch [::descriptions frame-id data-descriptions]]]}))

(re-frame/reg-event-db
 ::set-loading
 (fn [db [_ frame-id loading? time]]
   (if (< (get-in db (conj (db-path/loading? frame-id) :current-time))
          time)
     (db-utils/frame-exist-guard
      frame-id
      db
      (assoc-in db (db-path/loading? frame-id) {:current-time time
                                                :state loading?}))
     db)))

(re-frame/reg-sub
 ::is-loading?
 (fn [db [_ frame-id]]
   (get-in db (conj (db-path/loading? frame-id) :state) false)))
