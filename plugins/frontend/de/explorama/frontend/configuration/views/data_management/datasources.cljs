(ns de.explorama.frontend.configuration.views.data-management.datasources
  (:require [clojure.set :as set]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.configuration.configs.config-types.topic :as topic-configs]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.configuration.data.core :as data]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button select]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog loading-screen]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position context-menu]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.select :as select]
            [de.explorama.shared.configuration.ws-api :as ws-api]
            [re-frame.core :as re-frame :refer [dispatch reg-event-db
                                                reg-event-fx reg-sub subscribe]]))


(reg-sub
 ::burger-menu-infos
 (fn [db]
   (get-in db path/burger-menu-infos)))

(reg-event-db
 ::set-burger-menu-infos
 (fn [db [_ infos]]
   (assoc-in db path/burger-menu-infos infos)))

(re-frame/reg-sub
 ::datasource
 (fn [db]
   (get-in db path/current-datasource)))

(re-frame/reg-event-db
 ::set-datasource
 (fn [db [_ ds]]
   (assoc-in db path/current-datasource ds)))

(defn calc-mapped-topics [ds all-topics]
  (filterv (fn [topic] (some #(= ds %) (:datasources topic)))
           all-topics))

(reg-event-db
 ::delete-datasource-dialog
 (fn [db [_ datasource]]
   (assoc-in db path/delete-datasource-dialog datasource)))

(reg-sub
 ::delete-datasource-dialog
 (fn [db _]
   (get-in db path/delete-datasource-dialog)))

(reg-sub
 ::delete-datasource-loadscreen
 (fn [db _]
   (get-in db path/delete-datasource-loadscreen)))

(reg-sub
 ::initially-mapped-topics
 (fn [db]
   (get-in db path/initially-mapped-topics)))

(reg-event-db
 ::set-initially-mapped-topics
 (fn [db [_ mapped-topics]]
   (assoc-in db path/initially-mapped-topics mapped-topics)))

(reg-sub
 ::mapped-topics-options
 (fn [db]
   (get-in db path/mapped-topics-options)))

(reg-event-db
 ::set-mapped-topics-options
 (fn [db [_ options]]
   (assoc-in db path/mapped-topics-options options)))

(reg-event-fx
 ::init-edit-datasource
 (fn [{db :db} [_ ds]]
   (let [lang (i18n/current-language db)
         topics (vals (get-in db
                              (path/config-type :topics)))
         mapped-topics (calc-mapped-topics ds topics)
         selected-options (mapv #(select/to-option  (:id %)
                                                    (get-in % [:title lang] (get-in % [:title i18n/default-language])))
                                mapped-topics)]
     {:fx [[:dispatch [::set-datasource ds]]
           [:dispatch [::set-initially-mapped-topics mapped-topics]]
           [:dispatch [::set-mapped-topics-options selected-options]]]})))

(reg-event-fx
 ::delete-datasource
 (fn [{db :db} [_ ds]]
   {:db (assoc-in db path/delete-datasource-loadscreen true)
    :backend-tube [ws-api/delete-datasource-route
                   {:client-callback [ws-api/delete-datasource-result ds]}
                   ds]}))

(reg-event-fx
 ws-api/delete-datasource-result
 (fn [{db :db} [_ [bucket]]]
   {:db (assoc-in db path/delete-datasource-loadscreen false)
    :dispatch [::data/request-available-datasources bucket]}))

(re-frame/reg-sub
 ::datasource-value
 (fn [db]
   (get-in db path/datasource-value [])))

(re-frame/reg-event-db
 ::set-datasource-value
 (fn [db [_ options]]
   (assoc-in db path/datasource-value options)))

(re-frame/reg-sub
 ::datasource->topics
 (fn [db]
   (get-in db path/datasource->topics)))

(defn save-change [topics-to-update]
  (let [ids-to-update (atom (into #{} (map :id topics-to-update)))]
    (doseq [topic topics-to-update]
      (dispatch [::persistence/save-and-commit
                 topic-configs/config-type
                 (:id topic)
                 topic
                 {:trigger-action :list-entries
                  :success-callback
                  (fn []
                    (swap! ids-to-update disj (:id topic))
                    (when (= 0 (count @ids-to-update))
                      (dispatch (fi/call-api :notify-event-vec
                                             {:type :success
                                              :category {:config :save}
                                              :message :config-datasources-save-success-msg}))
                      (dispatch [::init-edit-datasource])))}
                 {:failed-callback
                  (fn []
                    (dispatch (fi/call-api :notify-event-vec
                                           {:type :error
                                            :category {:config :save}
                                            :message :config-datasources-save-failed-msg})))}]))))

(defn footer []
  (let [datasource @(subscribe [::datasource])
        topics (vals @(re-frame/subscribe [::data/topics]))
        initially-mapped-topics-ids (into #{} (map :id @(subscribe [::initially-mapped-topics])))
        mapped-topics-ids (into #{} (map :value @(subscribe [::mapped-topics-options])))
        added-topics-ids (set/difference mapped-topics-ids initially-mapped-topics-ids)
        removed-topics-ids (set/difference initially-mapped-topics-ids mapped-topics-ids)
        topics-to-save (concat (map #(update-in % [:datasources] (fn [dss] (remove (fn [ds] (= ds datasource)) dss)))
                                    (filter (fn [topic] (removed-topics-ids (:id topic))) topics))
                               (map #(update-in % [:datasources] conj datasource)
                                    (filter (fn [topic] (added-topics-ids (:id topic))) topics)))]
    [:div.footer
     [button {:start-icon :save
              :size :big
              :variant :primary
              :disabled? (= initially-mapped-topics-ids mapped-topics-ids)
              :on-click #(save-change topics-to-save)
              :label @(subscribe [::i18n/translate :save-label])}]]))


(defn- editing-header []
  [button {:on-click #(do (dispatch [::set-datasource nil]))
           :label @(subscribe [::i18n/translate :back-label])
           :start-icon :previous
           :size :big
           :variant :back}])

(defn edit-ds-view []
  (let [selected-ds @(subscribe [::datasource])
        [ds-type ds-name] selected-ds
        lang @(subscribe [::i18n/current-language])
        all-topics (vals @(subscribe [::data/topics]))
        topic-options (mapv #(select/to-option  (:id %)
                                                (get-in % [:title lang] (get-in % [:title i18n/default-language])))
                            all-topics)
        active-topics (if-let [selected @(subscribe [::mapped-topics-options])]
                        selected
                        (mapv #(select/to-option  (:id %)
                                                  (get-in % [:title lang] (get-in % [:title i18n/default-language])))
                              (filter (fn [topic] (some #(= (:value selected-ds) %) (:datasources topic)))
                                      all-topics)))
        config-mapped-topics-label @(subscribe [::i18n/translate :config-mapped-topics-label])
        ds-type-label @(subscribe [::i18n/translate (keyword (str "config-" (name ds-type) "-datasource-label"))])]
    [:<>
     [editing-header]
     [:div.content
      [:div.container
       [:div.row
        [:h3 ds-type-label]]
       [:div.row
        [:div.col-11
         [:div.explorama__form__input.explorama__form--info
          [:label ds-name]]]]
       [:div.row
        [:h3 config-mapped-topics-label]
        [select
         {:options topic-options,
          :values active-topics
          :on-change (fn [selection] (dispatch [::set-mapped-topics-options selection])),
          :is-multi? true,
          :mark-invalid? true}]]]]
     [footer]]))


(defn burger-menu []
  (let [{:keys [datasource event]} @(subscribe [::burger-menu-infos])
        {:keys [edit-label delete-label]}
        @(subscribe [::i18n/translate-multi
                     :edit-label
                     :delete-label])
        {:keys [top left]} (calc-menu-position event)]
    [context-menu
     {:show? (boolean datasource)
      :on-close #(dispatch [::set-burger-menu-infos])
      :position  {:top top :left left}
      :menu-z-index 250000
      :items [{:label edit-label
               :icon :edit
               :on-click #(do
                            (dispatch [::init-edit-datasource datasource]))}
              {:label delete-label
               :icon :trash
               :on-click #(do
                            (dispatch [::delete-datasource-dialog datasource]))}]}]))

(defn ds-summary [ds]
  [:li.disabled
   [:div.card__text
    [:div.title.flex
     [tooltip {:text  (second ds) :direction :up}
      (second ds)]]]
   [:div.card__actions
    [:div
     {:on-click (fn [e]
                  (.stopPropagation e)
                  (dispatch [::set-burger-menu-infos {:event e
                                                      :datasource ds}]))}
     [icon {:icon :menu}]]]])

(defn ds-type-section [ds-groups datasource-type]
  (let [label @(subscribe [::i18n/translate
                           (keyword (str "config-" (name datasource-type) "-datasources-label"))])]
    [:div.section__cards>div
     [:h2 label]
     [:<>
      (into
       [:ul]
       (map (fn [ds]
              [ds-summary ds])
            (sort-by second (get ds-groups datasource-type))))]]))

(defn delete-datasource-dialog []
  (let [{:keys [delete-datasource-title delete-datasource-question yes cancel-label]}
        @(subscribe [::i18n/translate-multi :delete-datasource-title :delete-datasource-question :yes :cancel-label])
        ds  @(subscribe [::delete-datasource-dialog])]
    [dialog {:title delete-datasource-title
             :message delete-datasource-question
             :show? ds
             :hide-fn #(re-frame/dispatch [::delete-datasource-dialog nil])
             :yes {:label yes
                   :on-click #(do
                                (re-frame/dispatch [::delete-datasource ds]))}
             :no {:label cancel-label
                  :variant :secondary}}]))

(defn- delete-loadscreen []
  (let [show? @(re-frame/subscribe [::delete-datasource-loadscreen])]
    ;TODO r1/mapping progress bar
    [loading-screen {:show? show?}]))

(defn datasources []
  (let [ds-groups @(subscribe [::data/datasources])
        sorted-keys (sort (keys ds-groups))]
    [:<>
     [burger-menu]
     [delete-datasource-dialog]
     [delete-loadscreen]
     (let [current-datasource @(re-frame/subscribe [::datasource])]
       (if current-datasource
         [edit-ds-view]
         [:<>
          [:div.content
           (into
            [:<>]
            (map (fn [datasource-type] [ds-type-section ds-groups datasource-type])
                 sorted-keys))]]))]))