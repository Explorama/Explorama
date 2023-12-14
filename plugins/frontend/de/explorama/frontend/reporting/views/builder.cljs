(ns de.explorama.frontend.reporting.views.builder
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button
                                                                            loading-message]]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub
                                   subscribe]]
            [de.explorama.shared.reporting.description-types]
            [de.explorama.frontend.reporting.data.dashboards :as dashboards]
            [de.explorama.frontend.reporting.data.reports :as reports]
            [de.explorama.frontend.reporting.data.templates :as templates]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.views.dashboards.template-builder :as d-builder]
            [de.explorama.frontend.reporting.views.reports.template-builder :as r-builder]))

(reg-event-fx
 ::save
 (fn [{db :db}]
   (let [{:keys [id type] :as dr-desc} (get-in db dr-path/creation)
         dr-desc (-> dr-desc
                     (select-keys (cond-> [:id :template-id :name :subtitle :desc :modules :type]
                                    (= type :report) (conj :selected-template)))
                     (assoc :creator (get (fi/call-api :user-info-db-get db) :username))
                     (assoc :timestamp (js/Date.now))
                     (update :modules (fn [old]
                                        (mapv #(select-keys % [:di :title :tool :vertical :preview :ratio :state :context-menu])
                                              old))))
         save-event (cond
                      (= type :dashboard) [::dashboards/save-dashboard dr-desc]
                      (= type :report) [::reports/save-report dr-desc])]
     (when save-event
       {:db (-> db
                (assoc-in dr-path/creation-save-pending? true)
                (assoc-in dr-path/creation-save-response nil)
                (dr-path/dissoc-in (dr-path/visible-dr id)))
        :fx [[:dispatch (fi/call-api [:tabs :deregister-event-vec]
                                     {:id id
                                      :type type})]
             [:dispatch-later {:ms 300 :dispatch save-event}]]}))))

(reg-event-db
 ::init-new
 (fn [db [_ type {:keys [register-tiles unregister-tiles]}]]
   (let [new-db (cond-> (-> db
                            (assoc-in dr-path/creation {})
                            (assoc-in dr-path/creation-dr-id (str (random-uuid)))
                            (assoc-in dr-path/creation-type type))
                  (= type :report)
                  (-> (assoc-in dr-path/creation-selected-template r-builder/report-base-template)
                      (assoc-in dr-path/creation-modules [{}])
                      (assoc-in dr-path/creation-template-id (get r-builder/report-base-template :id))))
         {:keys [tiles] :as sel-template} (get-in new-db dr-path/creation-selected-template)]

     (when (and (fn? register-tiles)
                (fn? unregister-tiles))
       (if tiles
         (register-tiles (dr-path/template->dom-ids sel-template))
         (unregister-tiles)))
     new-db)))

(reg-event-db
 ::edit
 (fn [db [_ type {:keys [template-id selected-template] :as desc} {:keys [register-tiles unregister-tiles]}]]
   (let [new-db (-> db
                    (assoc-in dr-path/creation desc)
                    (assoc-in dr-path/creation-edit-mode? true)
                    (assoc-in dr-path/creation-type type)
                    (assoc-in dr-path/creation-selected-template (or selected-template (templates/template db template-id))))
         {:keys [tiles] :as sel-template} (get-in new-db dr-path/creation-selected-template)]
     (when (and (fn? register-tiles)
                (fn? unregister-tiles))
       (if tiles
         (register-tiles (dr-path/template->dom-ids sel-template))
         (unregister-tiles)))
     new-db)))

(reg-sub
 ::edit-mode?
 (fn [db]
   (get-in db dr-path/creation-edit-mode?)))

(defn- request-pending? [db]
  (get-in db dr-path/creation-save-pending? false))

(reg-sub
 ::request-pending?
 request-pending?)

(reg-sub
 ::request-response
 (fn [db]
   (get-in db dr-path/creation-save-response)))

(reg-sub
 ::selected-template
 (fn [db]
   (get-in db dr-path/creation-selected-template)))

(reg-sub
 ::dr-desc
 (fn [db]
   (get-in db dr-path/creation)))

(reg-event-db
 ::name
 (fn [db [_ name]]
   (assoc-in db dr-path/creation-name name)))

(reg-sub
 ::name
 (fn [db]
   (get-in db dr-path/creation-name)))

(reg-event-db
 ::subtitle
 (fn [db [_ subtitle]]
   (assoc-in db dr-path/creation-subtitle subtitle)))

(reg-sub
 ::subtitle
 (fn [db]
   (get-in db dr-path/creation-subtitle)))

(reg-sub
 ::tile-data
 (fn [db [_ tile-idx]]
   (get-in db (dr-path/creation-module-desc tile-idx))))

(reg-event-db
 ::reset-tile
 (fn [db [_ tile-idx]]
   (assoc-in db (dr-path/creation-module-desc tile-idx) nil)))

(reg-event-db
 ::tile-title-change
 (fn [db [_ tile-idx new-title]]
   (assoc-in db
             (conj (dr-path/creation-module-desc tile-idx)
                   :title)
             new-title)))

(defn- name-exist? [db id name type]
  (let [cleaned-name (when name (str/trim name))
        already-used-names (set (map (fn [[_ {n :name}]]  n)
                                     (filter (fn [[_ {sid :id}]]
                                               (not= id sid))
                                             (if (= type :dashboard)
                                               (get-in db dr-path/created-dashboards)
                                               (get-in db dr-path/created-reports)))))]
    (already-used-names cleaned-name)))

(reg-sub
 ::can-save?
 (fn [db]
   (let [{:keys [id template-id name type modules]} (get-in db dr-path/creation)]
     (and id
          template-id
          (not (str/blank? name))
          (not (name-exist? db id name type))
          type
          (not (request-pending? db))
          (seq modules)
          (every? (fn [{:keys [tool di title state]}]
                    (or (and (= tool "text") state (not (:blank? state)))
                        (and di
                             (seq title)
                             state)))
                  modules)))))

(reg-sub
 ::name-exists?
 (fn [db _]
   (let [{:keys [id name type] :as dr} (get-in db dr-path/creation)]
     (boolean (or (not dr)
                  (name-exist? db id name type))))))

(defn- status-and-actions [save-pending? edit-mode? dr-desc]
  (let [save-label @(subscribe [::i18n/translate :save-label])
        saving-label @(subscribe [::i18n/translate :saving-label])
        [status details] @(subscribe [::request-response])
        can-save? @(subscribe [::can-save?])
        user @(fi/call-api :user-info-sub)]
     ;;TODO r1/reporting notify if status is not :success
    (if (or status save-pending?)
      [:div.dashboard__actions
       [loading-message {:show? true
                         :message saving-label}]]
      [button {:start-icon :save
               :variant :primary
               :size :big
               :disabled? (not can-save?)
               :on-click #(dispatch [::save])
               :label save-label}])))

(defn builder [sidebar-props]
  (let [edit-mode? @(subscribe [::edit-mode?])
        save-pending? @(subscribe [::request-pending?])
        [status] @(subscribe [::request-response])
        {:keys [type] :as dr-desc} @(subscribe [::dr-desc])
        builder-fn (if (= type :dashboard) d-builder/template-builder r-builder/template-builder)
        {:keys [back-label edit-dashboard-label new-dashboard-label edit-report-label new-report-label]}
        @(subscribe [::i18n/translate-multi :back-label :edit-dashboard-label :new-dashboard-label :edit-report-label :new-report-label])
        title (if (= type :dashboard)
                (if edit-mode?
                  edit-dashboard-label
                  new-dashboard-label)
                (if edit-mode?
                  edit-report-label
                  new-report-label))]
    [:<>
     [button {:on-click #(dispatch [::init-new nil sidebar-props])
              :label back-label
              :start-icon :previous
              :size :big
              :variant :back}]
     [:div.content
      [:div
       [:h2 title]
       [builder-fn sidebar-props (or save-pending? status)]]]
     [:div.footer
      [status-and-actions save-pending? edit-mode? dr-desc]]]))