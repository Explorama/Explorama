(ns de.explorama.frontend.reporting.views.share-dr
  (:require [re-frame.core :refer [dispatch subscribe reg-sub reg-event-db reg-event-fx]]
            [clojure.string :as string]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button select checkbox textarea loading-message]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon copy-field]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [goog.string.format]
            [de.explorama.shared.reporting.ws-api :as ws-api]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as path]
            [de.explorama.frontend.reporting.data.dashboards :as dashboards]
            [de.explorama.frontend.reporting.data.reports :as reports]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.reporting.util.frames :as futil]
            [de.explorama.frontend.common.i18n :as i18n]))

(reg-sub
 ::show?
 (fn [db]
   (boolean (get-in db path/share))))

(reg-sub
 ::share-dr-desc
 (fn [db]
   (get-in db path/share-dr-desc)))

(reg-event-fx
 ::show-share
 (fn [{db :db} [_ {:keys [share] :as share-dr} share-link]]
   (let [all-descs (concat (fi/call-api :users-db-get db)
                           (fi/call-api :roles-db-get db))
         public? (get share :public?)
         share (reduce
                (fn [new-share [k v]]
                  (assoc new-share
                         k
                         (map
                          (fn [value] (some #(when (= value (get % :value)) %) all-descs))
                          v)))
                {}
                (dissoc share :public?))
         share-with (->> (select-keys share [:groups-read-only :user-read-only])
                         vals
                         (apply concat))
         share-with-value (into {} (mapv
                                    #(vector %1 %2)
                                    (range (count share-with))
                                    share-with))]
     {:db (-> db
              (assoc-in path/public-read-only? public?)
              (assoc-in path/share-dr-desc share-dr)
              (assoc-in path/share-with share)
              (assoc-in path/share-with-value share-with-value)
              (assoc-in path/share-with-link share-link))})))

(reg-event-fx
 ::cancel-share
 (fn [{db :db}]
   {:db (update-in db path/dashboards-reports-root dissoc path/share-key)}))

(reg-event-fx
 ::submit-rights
 (fn [{db :db} _]
   (let [ui-rights (select-keys (get-in db path/share-with)
                                [path/shared-with-groups-key path/shared-with-users-key])
         {user :username} (fi/call-api :user-info-db-get db)
         {:keys [id name]} (get-in db path/share-dr-desc)
         dr-type (futil/id-type db id)]
     {:db (assoc-in db path/share-status :pending)
      :reporting-tubes [ws-api/update-dr-rights-route
                        {:client-callback ws-api/update-dr-rights-result
                         :broadcast-callback (case dr-type
                                               :dashboard [::dashboards/request-dashboards]
                                               :report [::reports/request-reports])}
                        user
                        (get-in db (conj path/share-dr-desc :creator))
                        ui-rights
                        id
                        name
                        (get-in db path/public-read-only?)]})))

(reg-event-fx
 ws-api/update-dr-rights-result
 (fn [{db :db} [_ success?]]
   (let [{:keys [id modules]} (get-in db path/share-dr-desc)
         dr-type (futil/id-type db id)
         share-with (as-> (get-in db path/share-with) sw-db
                      {:group (map #(get % :value) (get sw-db path/shared-with-groups-key))
                       :user (map #(get % :value) (get sw-db path/shared-with-users-key))})
         states (mapv #(get % :state) modules)
         share-settings-services (fi/call-api :service-category-db-get db :share-settings)
         share-settings-dispatches (mapv (fn [[_ e]]
                                           [e states share-with :dr])
                                         share-settings-services)]
     (when success?
       {:db (assoc-in db path/share-status :success)
        :dispatch-n (-> [[::cancel-share]
                         (case dr-type
                           :dashboard [::dashboards/request-dashboards id]
                           :report [::reports/request-reports id])]
                        (into share-settings-dispatches))}))))

(reg-sub
 ::shared-with-roles
 (fn [db]
   (get-in db path/shared-with-groups)))

(reg-sub
 ::shared-with-users
 (fn [db]
   (get-in db path/shared-with-users)))

(reg-event-db
 ::share-with-remove
 (fn [db [_ type desc]]
   (-> db
       (assoc-in path/share-last-removed desc)
       (update-in (if (= type :user)
                    path/shared-with-users
                    path/shared-with-groups)
                  (fn [desc-list] (filter #(not= desc %) desc-list))))))

(reg-sub
 ::share-with-value
 (fn [db]
   (get-in db path/share-with-value {})))

(reg-event-db
 ::set-share-with
 (fn [db [_ index option]]
   (assoc-in db (conj path/share-with-value index) option)))

(reg-event-db
 ::remove-share-with
 (fn [db [_ index]]
   (update-in db path/share-with-value dissoc index)))

(reg-event-db
 ::add-share-row
 (fn [db _]
   (let [new-index (->> (get-in db path/share-with-value)
                        keys
                        (into [0])
                        (apply max)
                        inc)]
     (assoc-in db (conj path/share-with-value new-index) nil))))

(reg-sub
 ::share-with-options
 (fn [db]
   (let [creator (get-in db (conj path/share-dr-desc :creator))
         users-shared (-> (map #(get % :value) (get-in db path/shared-with-users))
                          (conj creator))
         roles-shared (map #(get % :value) (get-in db path/shared-with-groups))
         all-users (filter #(not (some #{(:value %)} users-shared))
                           (sort-by #(string/lower-case (:label %)) (fi/call-api :users-db-get db)))
         all-roles (filter #(not (some #{(:value %)} roles-shared))
                           (sort-by #(string/lower-case (:label %)) (fi/call-api :roles-db-get db)))]
     [{:label @(subscribe [::i18n/translate :groups-label])
       :options (vec all-roles)}
      {:label @(subscribe [::i18n/translate :users-label])
       :options (vec all-users)}])))

(reg-event-db
 ::sort-selections-into-lists
 (fn [db _]
   (let [sorted-values (reduce
                        (fn [sorted selection]
                          (let [keywords (keys selection)]
                            (cond
                              (some #{:options} keywords)
                              (let [options (get selection :options)
                                    nested-keywords (keys (first options))]
                                (if (some #{:mail} nested-keywords)
                                  (update sorted :user concat options)
                                  (update sorted :role concat options)))
                              (some #{:mail} keywords) (update sorted :user conj selection)
                              :else (update sorted :role conj selection))))

                        {}
                        (vals (get-in db path/share-with-value)))]
     (-> db
         (assoc-in path/shared-with-users (:user sorted-values))
         (assoc-in path/shared-with-groups (:role sorted-values))))))

(reg-sub
 ::public?
 (fn [db _]
   (get-in db path/public-read-only? false)))

(reg-event-db
 ::switch-public
 (fn [db]
   (let [old-public? (get-in db path/public-read-only? false)]
     (-> db
         (assoc-in path/public-read-only? (not old-public?))))))

(defn settings-public []
  (let [public? @(subscribe [::public?])
        {:keys [id]} @(subscribe [::share-dr-desc])
        dr-type @(subscribe [::futil/id-type id])
        {:keys [publish-label share-publish-report-label share-publish-dashboard-label share-publish-sublabel]}
        @(subscribe [::i18n/translate-multi :publish-label :share-publish-dashboard-label
                     :share-publish-report-label :share-publish-sublabel])]
    [:div.share-section
     [:h4 publish-label]
     [:div.switch.input
      [checkbox {:id "share-publ-toggle"
                 :as-toggle? true
                 :checked? public?
                 :label (if (= :dashboard dr-type) share-publish-dashboard-label share-publish-report-label)
                 :on-change #(dispatch [::switch-public])}]]
     [:small.text-secondary share-publish-sublabel]]))

(defn settings-use-row [public? options [row-key row-data]]
  (let []
    [:div.input
     [:div.flex.gap-8.w-full.align-items-center
      [select {:options options
               :values row-data
               :on-change #(do (dispatch [::set-share-with row-key %])
                               (dispatch [::sort-selections-into-lists]))
               :is-grouped?          true
               :is-multi?            false
               :disabled?            public?
               :close-on-select?     false
               :show-clean-all?      true
               :extra-class "w-full"}]
      [button (cond-> {:variant :tertiary
                       :on-click #(do (dispatch [::remove-share-with row-key])
                                      (dispatch [::sort-selections-into-lists]))
                       :disabled? public?
                       :aria-label :delete-label
                       :start-icon :trash}
                (not public?) (assoc :type :warning))]]]))

(defn settings-use []
  (let [public? @(subscribe [::public?])
        options (subscribe [::share-with-options])
        share-with-value @(subscribe [::share-with-value])
        {:keys [share-label share-entries-sublabel aria-add-new-row]}
        @(subscribe [:de.explorama.frontend.common.i18n/translate-multi :share-label :share-entries-sublabel :aria-add-new-row])]
    [:div.share-section
     {:class (when public? "disabled")}
     [:h4 share-label]
     (reduce
      (fn [res row]
        (conj res [settings-use-row public? options row]))
      [:<>]
      share-with-value)
     [button {:variant :tertiary
              :on-click #(dispatch [::add-share-row])
              :aria-label aria-add-new-row
              :start-icon :plus
              :disabled? public?}]
     [:small.text-secondary share-entries-sublabel]]))

(reg-sub
 ::share-link
 (fn [db _]
   (get-in db path/share-with-link)))

(defn settings-link []
  (let [{:keys [link-label share-link-sublabel aria-copy-project-link]}
        @(subscribe [:de.explorama.frontend.common.i18n/translate-multi :link-label :share-link-sublabel :aria-copy-project-link])
        url-link @(subscribe [::share-link])]
    [:div.share-section
     [:div.title
      [:h4 link-label]]
     [copy-field {:copy-value url-link
                  :aria-label aria-copy-project-link}]
     [:small.text-secondary share-link-sublabel]]))

(defn share-body []
  [:div.flex.flex-col.gap-8
   [settings-public]
   [settings-use]
   [settings-link]])

(reg-sub
 ::share-status
 (fn [db _]
   (get-in db path/share-status)))

(defn share-options []
  (let [{:keys [name id]} @(subscribe [::share-dr-desc])
        share-status @(subscribe [::share-status])
        {:keys [cancel-label save-label]}
        @(subscribe [::i18n/translate-multi :save-label :cancel-label])
        dr-type @(subscribe [::futil/id-type id])
        publish-header-label (if (= :dashboard dr-type)
                               @(subscribe [::i18n/translate :publish-dashboard-header-label])
                               @(subscribe [::i18n/translate :publish-report-header-label]))]
    [dialog {:title (str publish-header-label " " name)
             :message [share-body]
             :show? (subscribe [::show?])
             :hide-fn #(do)
             :ok {:label save-label
                  :start-icon :save
                  :loading? (= :pending share-status)
                  :disabled? false
                  :on-click #(dispatch [::submit-rights])}
             :cancel {:label cancel-label
                      :loading? (= :pending share-status)
                      :variant :secondary
                      :on-click #(do (when (= :success share-status)
                                       (dispatch (case dr-type
                                                   :dashboard [::dashboards/request-dashboards]
                                                   :report [::reports/request-reports])))
                                     (dispatch [::cancel-share]))}}]))