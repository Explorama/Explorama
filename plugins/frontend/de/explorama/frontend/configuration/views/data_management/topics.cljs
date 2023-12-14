(ns de.explorama.frontend.configuration.views.data-management.topics
  (:require [clojure.set :refer [difference]]
            [clojure.string :as str]
            [de.explorama.frontend.configuration.components.dialog :as dialog :refer [confirm-dialog]]
            [de.explorama.frontend.configuration.configs.config-types.topic :as topic-configs]
            [de.explorama.frontend.configuration.configs.persistence :as persistence]
            [de.explorama.frontend.configuration.data.core :as data]
            [de.explorama.frontend.configuration.path :as path]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                   input-field select
                                                                   textarea]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position
                                                                       context-menu]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.select :as util-select]
            [re-frame.core :as re-frame :refer [dispatch reg-event-db reg-event-fx
                                                reg-sub subscribe]]))

(defn available-languages-options [db]
  (when-let [langs (fi/call-api [:i18n :available-languages-db-get] db)]
    (into [] (set (mapv (fn [{lang :name}]
                          {:value (keyword lang)
                           :label (i18n/translate db (keyword lang))})
                        langs)))))

(re-frame/reg-sub
 ::available-languages-options
 (fn [db]
   (available-languages-options db)))

(reg-sub
 ::topic-id
 (fn [db]
   (get-in db path/topic-id)))

(reg-event-db
 ::set-topic-id
 (fn [db [_ id]]
   (assoc-in db path/topic-id id)))

(reg-sub
 ::loaded-topic
 (fn [db]
   (get-in db path/loaded-topic)))

(reg-event-db
 ::set-loaded-topic
 (fn [db [_ topic]]
   (assoc-in db path/loaded-topic topic)))

(reg-sub
 ::rows
 (fn [db]
   (get-in db path/rows-path)))

(reg-sub
 ::mapped-datasources
 (fn [db]
   (get-in db path/mapped-datasources)))

(reg-event-db
 ::set-mapped-datasources
 (fn [db [_ datasources]]
   (assoc-in db path/mapped-datasources datasources)))

(defn- get-topic [db]
  (let [rows (get-in db path/rows-path)
        title (reduce (fn [acc t] (update-in acc [:title] assoc (first t) (:title (second t)))) {}
                      rows)
        title+description (reduce (fn [acc t] (update-in acc [:desc] assoc (first t) (:desc (second t)))) title
                                  rows)]
    (merge
     {:id (get-in db path/topic-id)
      :datasources (get-in db path/mapped-datasources)}
     title+description)))

(reg-sub
 ::topic
 (fn [db]
   (get-topic db)))

(reg-event-db
 ::rows
 (fn [db [_]]
   (get-in db
           path/rows-path)))

(reg-event-fx
 ::set-topic
 (fn [_ [_ topic]]
   (let [topic-langs (reduce (fn [acc m] (into acc (keys m)))
                             #{}
                             (vals (select-keys topic [:title :desc])))
         rows (or (mapv (fn [lang] [lang {:title (get-in topic [:title lang])
                                          :desc (get-in topic [:desc lang])}])
                        topic-langs)
                  [{}])]
     {:fx  [[:dispatch [::set-topic-id (:id topic)]]
            [:dispatch [::set-mapped-datasources (:datasources topic)]]
            [:dispatch [::init-rows rows]]]})))

(reg-event-db
 ::init-rows
 (fn [db [_ rows]]
   (assoc-in db
             path/rows-path
             rows)))


(re-frame/reg-event-db
 ::add-row
 (fn [db [_]]
   (update-in db
              path/rows-path
              (fn [old]
                (let [res
                      (conj (vec (or old [])) {})]
                  res)))))

(reg-event-db
 ::set-row
 (fn [db [_ idx row]]
   (assoc-in db (conj path/rows-path idx) row)))


(re-frame/reg-event-db
 ::remove-row
 (fn [db [_ idx]]
   (update-in db
              path/rows-path
              (fn [old]
                (if (and (= idx 0)
                         (= 1 (count old)))
                  [{}]
                  (into (subvec old 0 idx)
                        (subvec old (inc idx))))))))

(reg-sub
 ::burger-menu-infos
 (fn [db]
   (get-in db path/burger-menu-infos)))

(reg-event-db
 ::set-burger-menu-infos
 (fn [db [_ infos]]
   (assoc-in db path/burger-menu-infos infos)))

(defn- other-topics [db]
  (let [current-topic-id (get-in db path/topic-id)]
    (-> db
        (get-in (path/config-type :topics))
        (dissoc current-topic-id)
        vals)))

(defn- title-exist?
  ([_ title other-topics]
   (let [title (when (seq title) (str/trim title))]
     (some (fn [{titles :title}]
             (some (fn [[_ other-title]]
                     (= other-title title))
                   titles))
           other-topics)))
  ([db title]
   (let [other-topics (other-topics db)]
     (title-exist? db title other-topics))))

(reg-sub
 ::title-exist?
 (fn [db [_ title]]
   (title-exist? db title)))

(reg-sub
 ::cant-save?
 (fn [db _]
   (let [topic (get-topic db)
         loaded-topic (get-in db path/loaded-topic)
         rows (get-in db path/rows-path)
         other-topics (other-topics db)]
     (or
      (= (dissoc loaded-topic :timestamp) topic)
      (some false? (map (fn [row]
                          (if (map? row)
                            false
                            (let [[lang texts] row]
                              (and (keyword? lang)
                                   (boolean (not (str/blank? (:title texts))))))))
                        rows))
      (some (fn [[_ {:keys [title]}]]
              (title-exist? db title other-topics))
            rows)))))

(defn- editing-header []
  [button {:on-click #(do (dispatch [::set-loaded-topic nil]))
           :label @(re-frame/subscribe [::i18n/translate :back-label])
           :start-icon :previous
           :size :big
           :variant :back}])

(defn save-topic-footer []
  (let [topic @(subscribe [::topic])
        save-disabled? @(subscribe [::cant-save?])
        {:keys [config-save-topic
                config-saved-topic-msg
                config-topic-save-failed-msg]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :config-save-topic
                              :config-saved-topic-msg
                              :config-topic-save-failed-msg])]
    [:div.footer
     [button {:start-icon :save
              :size :big
              :disabled? save-disabled?
              :on-click #(do
                           (dispatch [::persistence/save-and-commit
                                      topic-configs/config-type
                                      (:id topic)
                                      (update topic :title (fn [v]
                                                             (into {}
                                                                   (map (fn [[k title]]
                                                                          [k (str/trim title)]))
                                                                   v)))
                                      {:trigger-action :list-entries
                                       :success-callback
                                       (fn []
                                         (dispatch (fi/call-api :notify-event-vec
                                                                {:type :success
                                                                 :category {:config :save}
                                                                 :message config-saved-topic-msg}))
                                         (dispatch [::set-loaded-topic]))
                                       :failed-callback
                                       (fn []
                                         (dispatch (fi/call-api :notify-event-vec
                                                                {:type :error
                                                                 :category {:config :save}
                                                                 :message config-topic-save-failed-msg})))}]))
              :variant :primary
              :label config-save-topic}]]))

(defn- row-add [disabled?]
  [button {:start-icon :plus
           :size :big
           :disabled? disabled?
           :on-click #(dispatch [::add-row])
           :variant :secondary}])

(defn- remove-row [lang idx]
  [button {:start-icon :trash
           :disabled? (= lang i18n/default-language)
           :on-click (fn []
                       (dispatch [::remove-row idx]))
           :variant :secondary}])

(defn topic-texts [lang-opts remaining-langs [idx row]]
  (let [{:keys [config-language-label
                config-title-label
                config-desc-label
                config-topic-already-exists]}
        @(subscribe [::i18n/translate-multi
                     :config-language-label
                     :config-title-label
                     :config-desc-label
                     :config-topic-already-exists])
        [lang {:keys [title desc]}]
        (when (seq row)
          row)
        rows  @(subscribe [::rows])
        selected-lang (util-select/selected-option :value lang-opts lang)
        title-already-exist? @(subscribe [::title-exist? title])
        title-exist-caption (when title-already-exist?
                              config-topic-already-exists)]
    [:<>
     [select
      {:options (util-select/selected-options :value lang-opts
                                              remaining-langs)
       :label config-language-label
       :disabled? (= lang i18n/default-language)
       :on-change #(dispatch [::set-row idx [(:value %) {:title title :desc desc}]])
       :values selected-lang}]
     [:div.flex.flex-col.flex-grow.gap-4
      [input-field {:value (or title "")
                    :invalid? title-already-exist?
                    :caption title-exist-caption
                    :label config-title-label
                    :on-change #(dispatch [::set-row idx [lang {:title % :desc desc}]])}]
      [textarea {:value (or desc "")
                 :label config-desc-label
                 :placeholder config-desc-label
                 :on-change #(dispatch [::set-row idx [lang {:title title :desc %}]])}]]

     [remove-row lang idx]]))

(defn build-datasource-options [datasources]
  (mapv (fn [ds]
          {:value (second ds) :label (second ds) :gkey (first ds)}) datasources))


(defn edit-topic-view []
  (let [lang-opts @(re-frame/subscribe [::available-languages-options])
        {:keys [config-topic-label
                config-temp-datasources-label
                config-default-datasources-label]}
        @(subscribe [::i18n/translate-multi
                     :config-topic-label
                     :config-temp-datasources-label
                     :config-default-datasources-label])
        rows  @(subscribe [::rows])
        highest-idx (dec (count rows))
        max-idx (dec (count lang-opts))
        all-datasources @(subscribe [::data/datasources])
        default-datasources-options (build-datasource-options (:default all-datasources))
        temporary-datasources-options (build-datasource-options (:temp all-datasources))
        datasource-options [{:label config-default-datasources-label
                             :gkey :default
                             :options default-datasources-options}
                            {:label config-temp-datasources-label
                             :gkey :temp
                             :options temporary-datasources-options}]
        languages (map :value lang-opts)
        remaining-langs (filter (difference (into #{} languages)
                                            (into #{} (filter keyword?
                                                              (map first rows))))
                                languages)
        mapped-datasources @(subscribe [::mapped-datasources])
        mapped-to-datasources-label @(re-frame/subscribe [::i18n/translate :config-mapped-to-datasources-label])]
    [:<>
     [editing-header]
     [:div.content
      [:div.container.spaced-y-8
       [:h3 config-topic-label]
       [:div.grid.grid-cols-1-4.gap-8
        (for [r (map-indexed vector rows)]
          ^{:key (str "edit-topic-view-" (first r))}
          [topic-texts lang-opts remaining-langs r])]
       [row-add (or (>= 0 (count remaining-langs))
                    (= max-idx highest-idx))]
       [:div.row
        [:h3 mapped-to-datasources-label]
        [select
         {:options datasource-options
          :select-class "input--w4"
          :on-change #(dispatch [::set-mapped-datasources (map (fn [o]
                                                                 [(:gkey o) (:value o)])
                                                               %)])
          :values (build-datasource-options mapped-datasources)
          :is-multi? true,
          :is-grouped? true,
          :group-value-key :gkey,
          :group-selectable? false,
          :mark-invalid? true}]]]]

     [save-topic-footer]]))


(defn burger-menu []
  (let [{:keys [event topic]} @(subscribe [::burger-menu-infos])
        {:keys [id]} topic
        {:keys [edit-label delete-label config-deleted-topic-msg]}
        @(subscribe [::i18n/translate-multi
                     :edit-label
                     :delete-label
                     :config-deleted-topic-msg])
        {:keys [top left]} (calc-menu-position event)]
    [context-menu
     {:show? (boolean id)
      :on-close #(dispatch [::set-burger-menu-infos])
      :position  {:top top :left left}
      :menu-z-index 250000
      :items [{:label edit-label
               :icon :edit
               :on-click #(do
                            (dispatch [::set-loaded-topic topic])
                            (dispatch [::set-topic topic]))}
              {:label delete-label
               :icon :trash
               :on-click (fn [e]
                           (.stopPropagation e)
                           (dispatch [::dialog/set-data :delete-topic {:delete-event [::persistence/delete-config
                                                                                      topic-configs/config-type
                                                                                      (:id topic)
                                                                                      {:trigger-action :list-entries
                                                                                       :success-callback
                                                                                       (fn []
                                                                                         (dispatch [::set-loaded-topic nil])
                                                                                         (dispatch (fi/call-api :notify-event-vec
                                                                                                                {:type :success
                                                                                                                 :category {:config :save}
                                                                                                                 :message config-deleted-topic-msg})))}]}])
                           (dispatch [::dialog/show-dialog :delete-topic true :delete-topic])
                           (dispatch [::set-burger-menu-infos]))}]}]))

(defn- topic-val [lang val-key topic]
  (get-in topic [val-key lang] (get-in topic [val-key i18n/default-language])))

(defn topic-summary [lang topic]
  (let [title (topic-val lang :title topic)
        desc (topic-val lang :desc topic)]
    [:li.disabled
     [:div.card__text.align-self-start
      [:div.title.flex
       [tooltip {:text title :direction :up}
        title]]
      [:div.subtitle.flex
       [tooltip {:text desc :direction :up}
        desc]]]
     [:div.card__actions
      [:div
       {:on-click (fn [e]
                    (.stopPropagation e)
                    (dispatch [::set-burger-menu-infos {:event e
                                                        :topic topic}]))}
       [icon {:icon :menu}]]]]))

(defn topic-creation-footer []
  (let [label-create-topic @(re-frame/subscribe [::i18n/translate :config-create-topic])]
    [:div.footer
     [button {:start-icon :plus
              :size :big
              :variant :primary
              :on-click #(let [new-topic  {:id (str (random-uuid))
                                           :title {i18n/default-language nil}
                                           :desc {i18n/default-language nil}}]
                           (dispatch [::set-topic new-topic])
                           (dispatch [::set-loaded-topic new-topic]))
              :label label-create-topic}]]))


(defn topics-view []
  (let [lang @(subscribe [::i18n/current-language])
        topics (vals @(re-frame/subscribe [::data/topics]))
        existing-topics-label @(re-frame/subscribe [::i18n/translate :existing-topics-label])]
    [:<>
     [confirm-dialog :delete-topic]
     [:div.content
      [:<>
       [:div.section__cards>div
        [:h2 existing-topics-label]
        [:<>
         (into
          [:ul]
          (map (fn [topic]
                 [topic-summary lang topic])
               (sort-by (fn [topic]
                          (str/lower-case (topic-val lang :title topic)))
                        topics)))]
        [burger-menu]]]]
     [topic-creation-footer]]))

(reg-event-fx
 ::reset-topic-management
 (fn [_ _]
   {:fx [[:dispatch [::set-loaded-topic nil]]]}))

(defn topics []
  [:<>
   (let [loaded-topic @(re-frame/subscribe [::loaded-topic])]
     (if loaded-topic
       [edit-topic-view]
       [topics-view]))])

