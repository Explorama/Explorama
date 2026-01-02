(ns de.explorama.frontend.projects.views.share-project
  (:require [clojure.data :refer [diff]]
            [clojure.string :as string]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button checkbox select]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [copy-field]]
            [de.explorama.shared.common.configs.platform-specific :as config-shared-platform]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [goog.string :as gstring]
            [goog.string.format]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [taoensso.timbre :refer [error]]
            ["moment" :as momentModule]))

(def input-val (r/atom ""))

(defn project-link [project]
  (str config-shared-platform/explorama-origin "/?project-id=" (:project-id project)))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get-in db [:projects :share-project :is-active?] false)))

(defn pdf-name [project-id]
  (str (.format momentModule "YYYY-MM-DDTHH-mm-ss") "---" project-id ".pdf"))

(defn user-label [all-users username]
  (first
   (filter #(= username (:value %))
           all-users)))

(defn build-share-with-entry [db user-or-group user-label-func read-only? is-role?]
  (let [index (count (get-in db [:projects :share-project :share-with] {}))
        label (if-not is-role?
                (:label (user-label-func user-or-group))
                user-or-group)
        options-map {:label label
                     :value user-or-group
                     :role? is-role?
                     :read-only? read-only?}]
    (if (get user-or-group :only-public-read-only?)
      db
      (assoc-in db
                [:projects :share-project :share-with index]
                {:index index
                 :user-or-group options-map
                 :read-only? read-only?
                 :is-role? is-role?}))))

(defn build-share-with-entries [db users-or-groups all-users read-only? is-role?]
  (let [user-label-func (partial user-label all-users)]
    (reduce (fn [ndb user-or-group]
              (build-share-with-entry ndb user-or-group user-label-func read-only? is-role?))
            db
            users-or-groups)))

(defn filter-user [user-vec creator share-user]
  (filter (fn [u]
            (and #_(not (= u (:username share-user))) ;Filter user
             (not (= u creator)))) ;Filter the creator of the project
          user-vec))

(defn build-share-with-rows-from-project [db
                                          all-users
                                          {:keys [creator allowed-user read-only-user
                                                  read-only-groups allowed-groups]}
                                          share-user]
  (let [allowed-user (filter-user allowed-user creator share-user)
        read-only-user (filter-user read-only-user creator share-user)
        new-db (-> db
                   (build-share-with-entries read-only-user all-users true false)
                   (build-share-with-entries allowed-user all-users false false)
                   (build-share-with-entries read-only-groups all-users true true)
                   (build-share-with-entries allowed-groups all-users false true))]
    (if (get-in new-db [:projects :share-project :share-with 0])
      new-db
      (assoc-in db [:projects :share-project :share-with 0] {:index 0}))))

(re-frame/reg-event-fx
 ::show-dialog
 (fn [{db :db} [_ show? project user share-project?]]
   (let [all-users (fi/call-api :users-db-get db)]
     {:db (-> db
              (assoc-in [:projects :share-project :is-active?] show?)
              (assoc-in [:projects :share-project :project] project)
              (assoc-in [:projects :share-project :public-read-only?] (get project :public-read-only? false))
              (assoc-in [:projects :share-project :user] user)
              (assoc-in [:projects :share-project :share-project?] share-project?)
              (assoc-in [:projects :share-project :e-mail?] false)
              (assoc-in [:projects :share-project :mail-receiver] [])
              (build-share-with-rows-from-project all-users project user))})))

(re-frame/reg-sub
 ::share-project?
 (fn [db]
   (get-in db [:projects :share-project :share-project?])))

(re-frame/reg-sub
 ::project
 (fn [db _]
   (get-in db [:projects :share-project :project])))

(re-frame/reg-event-db
 ::add-share-row
 (fn [db _]
   (let [new-index (inc (reduce (fn [r [idx]] (max r idx))
                                0
                                (get-in db [:projects :share-project :share-with] {})))]
     (assoc-in db [:projects :share-project :share-with new-index] {:index new-index}))))

(re-frame/reg-event-db
 ::delete-share-row
 (fn [db [_ index]]
   (update-in db [:projects :share-project :share-with] dissoc index)))

(re-frame/reg-event-db
 ::share-with
 (fn [db [_ index user-or-group read-only?]]
   (assoc-in db [:projects :share-project :share-with index] {:index index
                                                              :user-or-group (assoc user-or-group
                                                                                    :read-only? read-only?)})))
(re-frame/reg-event-db
 ::public-read-only
 (fn [db [_ public-read-only?]]
   (assoc-in db [:projects :share-project :public-read-only?] public-read-only?)))

(re-frame/reg-sub
 ::public-read-only
 (fn [db _]
   (get-in db [:projects :share-project :public-read-only?] false)))

(re-frame/reg-sub
 ::share-with
 (fn [db _]
   (get-in db [:projects :share-project :share-with])))

(re-frame/reg-event-fx
 ::cancel-share
 (fn [{db :db}]
   {:db (update-in db [:projects] dissoc :share-project)}))

(defn group-share-entries [share-with]
  (reduce (fn [acc [_ {{:keys [value role? read-only?]} :user-or-group}]]
            (cond
              (and role? read-only?) (update acc :share-read-only-group conj value)
              role? (update acc :share-allowed-group conj value)
              read-only? (update acc :share-read-only-user conj value)
              :else (update acc :share-allowed-user conj value)))
          {:share-read-only-user #{}
           :share-read-only-group #{}
           :share-allowed-user #{}
           :share-allowed-group #{}}
          share-with))

(defn check-new-removed [updated-set current-set]
  (let [[added
         removed _] (diff updated-set current-set)]
    [(filterv identity added)
     (filterv identity removed)]))

(defn deleted-added-shares [{current-username :username}
                            {:keys [read-only-groups allowed-groups
                                    read-only-user allowed-user
                                    creator]}
                            {:keys [share-read-only-group share-allowed-group
                                    share-read-only-user share-allowed-user]}]
  (let [share-allowed-user (cond-> share-allowed-user
                             :always (conj creator)
                             (contains? allowed-user current-username) (conj current-username))
        [added-read-only-groups
         deleted-read-only-groups] (check-new-removed share-read-only-group
                                                      (set read-only-groups))
        [new-allowed-groups
         deleted-allowed-groups] (check-new-removed share-allowed-group
                                                    (set allowed-groups))
        [added-read-only-users
         deleted-read-only-users] (check-new-removed share-read-only-user
                                                     (set read-only-user))
        [new-allowed-users
         deleted-allowed-users] (check-new-removed (conj share-allowed-user creator)
                                                   (set allowed-user))]
    (cond-> {}
      (seq new-allowed-users) (assoc :added-allowed-user new-allowed-users)
      (seq new-allowed-groups) (assoc :added-allowed-groups new-allowed-groups)
      (seq added-read-only-users) (assoc :added-read-only-user added-read-only-users)
      (seq added-read-only-groups) (assoc :added-read-only-groups added-read-only-groups)
      (seq deleted-allowed-users) (assoc :deleted-allowed-user  deleted-allowed-users)
      (seq deleted-allowed-groups) (assoc :deleted-allowed-groups  deleted-allowed-groups)
      (seq deleted-read-only-users) (assoc :deleted-read-only-user deleted-read-only-users)
      (seq deleted-read-only-groups) (assoc :deleted-read-only-groups deleted-read-only-groups))))

(re-frame/reg-event-fx
 ::share
 (fn [{db :db}]
   (let [current-user (fi/call-api :user-info-db-get db)
         {:keys [project-id creator] :as p-desc} (get-in db [:projects :share-project :project])
         public-read-only? (get-in db
                                   [:projects :share-project :public-read-only?]
                                   false)
         grouped-share (group-share-entries (get-in db [:projects :share-project :share-with]))
         changed-share-map (deleted-added-shares current-user p-desc grouped-share)]
     {:dispatch [::cancel-share]
      :backend-tube-n [[ws-api/share-project-route
                        {}
                        (assoc changed-share-map
                               :project-id project-id
                               :project-creator creator
                               :public-read-only? public-read-only?
                               :shared-by (:username current-user))]]})))

(re-frame/reg-event-fx
 ::share-settings
 (fn [{db :db} [_ plogs share-with]]
   (let [share-settings-services (fi/call-api :service-category-db-get db :share-settings)]
     {:dispatch-n (mapv (fn [[_ e]]
                          [e plogs share-with :project])
                        share-settings-services)})))

(re-frame/reg-event-fx
 ::get-screenshot
 (fn [_ [_ url]]
   (js/window.open url "_blank")
   {}))

(re-frame/reg-event-fx
 ::failed
 (fn [_ _]
   (error "Creation of Project failed")
   {}))

(re-frame/reg-event-fx
 ::clean-input-field
 (fn [_ _]
   (reset! input-val "")
   {}))

(re-frame/reg-event-fx
 ::project-shared
 (fn [{db :db} [_ project-infos]]
   {:dispatch [::show-dialog false]}))

(defn build-grouped-option [label options]
  {:label label :options options})

(defn user->options [users]
  (mapv (fn [{:keys [label value]}]
          {:value value
           :label label
           :is-role? false})
        users))

(defn filter-share-entries [share-with-rows index]
  (mapv (fn [[_ {:keys [user-or-group]}]]
          (get user-or-group :value))
        (filter (fn [[ind _]]
                  (not= index ind))
                share-with-rows)))

(defn filter-user-or-groups [filtered-share-rows user-or-groups]
  (filterv (fn [value]
             (not-any? #(= % (get value :value))
                       filtered-share-rows))
           user-or-groups))

(re-frame/reg-sub
 ::share-user
 (fn [db _]
   (get-in db [:projects :share-project :user])))

(re-frame/reg-sub
 ::share-project-creator
 (fn [db _]
   (get-in db [:projects :share-project :project :creator])))

(re-frame/reg-sub
 ::user-and-roles-selection-entries
 (fn [_]
   [(re-frame/subscribe [::share-user])
    (re-frame/subscribe [::share-project-creator])
    (re-frame/subscribe [::share-with])
    (fi/call-api :users-sub)
    (fi/call-api :roles-sub)
    (re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :user-group])
    (re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :roles-group])])
 (fn [[share-dialog-user
       project-creator
       share-with
       possible-users
       possible-roles
       user-group-label
       roles-group-label] [_ share-with-index]]
   (let [users (filter (fn [u]
                         (and (not (= (:value u)
                                      (:username share-dialog-user))) ;Filter loged-in user
                              (not (= (:value u)
                                      project-creator)))) ;Filter creator of the project
                       possible-users)
         share-with-rows (filter-share-entries share-with share-with-index)
         user-options (->> users
                           (map #(assoc % :role? false))
                           (sort-by (fn [{label :label}]
                                      (if (string? label)
                                        (string/lower-case label)
                                        label)))
                           user->options
                           (filter-user-or-groups share-with-rows))
         roles-options (->> possible-roles
                            (map #(assoc % :role? true))
                            (sort-by (fn [{label :label}]
                                       (if (string? label)
                                         (string/lower-case label)
                                         label)))
                            (filter-user-or-groups share-with-rows))]
     [(build-grouped-option roles-group-label roles-options)
      (build-grouped-option user-group-label user-options)])))

(defn select-user-or-group [index user-or-group read-only? is-role?]
  (let [select-placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :select-placeholder-projects])]
    [select {:placeholder          select-placeholder
             :options              (re-frame/subscribe [::user-and-roles-selection-entries index])
             :values               user-or-group
             :on-change            (fn [entry]
                                     (re-frame/dispatch [::share-with index
                                                         entry
                                                         read-only?
                                                         is-role?]))
             :is-grouped?           true
             :group-selectable?     false ;for all filter we have to change to true
             :close-on-select?      true
             :show-clean-all?       false
             :mark-invalid?         false
             :autofocus?            true
             :extra-class           "input--w14"}]))

(re-frame/reg-event-db
 ::export-option
 (fn [db [_ active-option]]
   (assoc-in db [:projects :share-project :export-option] active-option)))

(re-frame/reg-sub
 ::export-option
 (fn [db]
   (get-in db [:projects :share-project :export-option])))

(defn export-options []
  (let [select-placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate
                                                 :share-dialog-export-options-placeholder])
        complete-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-create-pdf-complete])
        visible-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-create-pdf-visible])
        separate-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-create-pdf-separate])
        export-as-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-dialog-export-as])
        options [{:label complete-label :value "complete"}
                 {:label separate-label :value "separate-verticals"}
                 {:label visible-label :value "visible"}]]

    [:div.explorama__form__select
     [select {:label export-as-label
              :placeholder          select-placeholder
              :options              options
              :values               (re-frame/subscribe [::export-option])
              :on-change            (fn [entry]
                                      (re-frame/dispatch [::export-option entry]))
              :close-on-select?      true
              :show-clean-all?       false
              :mark-invalid?         false
              :autofocus?            true
              :extra-class           "input--w16"}]]))

(re-frame/reg-event-db
 ::mail-receiver
 (fn [db [_ active-option]]
   (assoc-in db [:projects :share-project :mail-receiver] active-option)))

(re-frame/reg-sub
 ::mail-receiver
 (fn [db]
   (get-in db [:projects :share-project :mail-receiver])))

(re-frame/reg-event-db
 ::link-mail-receiver
 (fn [db [_ active-option]]
   (assoc-in db [:projects :share-project :link-mail-receiver] active-option)))

(re-frame/reg-sub
 ::link-mail-receiver
 (fn [db]
   (get-in db [:projects :share-project :link-mail-receiver])))

(re-frame/reg-sub
 ::mail-receiver-options
 (fn [_]
   [(fi/call-api :users-sub)])
 (fn [[users]]
   (into [] (sort-by (comp clojure.string/lower-case :label)
                     (map (fn [{:keys [label mail]}]
                            {:label label
                             :value (if mail
                                      mail
                                      "")})
                          users)))))

(defn e-mail-options []
  (let [select-placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate
                                                 :share-dialog-e-mail-options-placeholder])
        e-mail? @(re-frame/subscribe [::e-mail])]
    (when e-mail?
      [:div.explorama__form__select
       [select {:placeholder          select-placeholder
                :options              (re-frame/subscribe [::mail-receiver-options])
                :values               (re-frame/subscribe [::mail-receiver])
                :on-change            (fn [entry]
                                        (re-frame/dispatch [::mail-receiver entry]))
                :is-multi?            true
                :close-on-select?     false
                :show-clean-all?      true
                :mark-invalid?        false
                :autofocus?           true
                :extra-class          "input--w14"}]])))

(defn share-with-row [share-row-data]
  (let [index (get share-row-data :index 0)
        user-or-group (get share-row-data :user-or-group)
        read-only? (get-in share-row-data [:user-or-group :read-only?] true)
        is-role? (get share-row-data :is-role? false)
        checkbox-id (str "share-project" index)
        read-only-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-entry-read-only-label])]
    [:div.input
     [:div.flex.gap-8.w-full.align-items-center
      [select-user-or-group index user-or-group read-only? is-role?]
      [checkbox {:id checkbox-id
                 :label read-only-label
                 :value index
                 :checked? read-only?
                 :on-change #(re-frame/dispatch [::share-with index
                                                 user-or-group
                                                 (not read-only?)
                                                 is-role?])}]
      [button {:variant :tertiary
               :type :warning
               :on-click #(re-frame/dispatch [::delete-share-row index])
               :aria-label :delete-label
               :start-icon :trash}]]]))

(re-frame/reg-sub
 ::pdf-text
 (fn [db]
   (get-in db [:projects :share-project :text])))

(re-frame/reg-event-db
 ::pdf-text
 (fn [db [_ pdf-text]]
   (assoc-in db [:projects :share-project :text] pdf-text)))

(re-frame/reg-sub
 ::e-mail
 (fn [db]
   (get-in db [:projects :share-project :e-mail?])))

(re-frame/reg-event-db
 ::e-mail
 (fn [db [_ e-mail?]]
   (assoc-in db [:projects :share-project :e-mail?] e-mail?)))

(re-frame/reg-sub
 ::link-e-mail
 (fn [db]
   (get-in db [:projects :share-project :link-e-mail?])))

(re-frame/reg-event-db
 ::link-e-mail
 (fn [db [_ e-mail?]]
   (assoc-in db [:projects :share-project :link-e-mail?] e-mail?)))

(defn e-mail-link [project-title download-url mail-receiver? user-info]
  (str "mailto:" (string/join "; " mail-receiver?)
       "?subject=Export: " project-title
       "&body=Hello,%0D"
       "download the pdf for Project:"
       project-title
       " under "
       config-shared-platform/explorama-origin
       "/screenshot/"
       download-url
       "%0DKind Regards,%0D"
       (:name user-info)))

(defn checkbox-comp [sub disp translate share-class?]
  (let [value (re-frame/subscribe [sub])
        label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate translate])]
    [checkbox {:checked? value
               :label label
               :on-change #(re-frame/dispatch [disp %])
               :id (str "explorama-checkbox3-" translate)
               :extra-class (when share-class? "explorama__share__email")}]))

(defn email-project [project]
  (let [project-title (:title project)
        user-info @(fi/call-api :user-info-sub)
        select-placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-dialog-e-mail-options-placeholder])
        send-link @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-dialog-e-mail-send-link])
        project-url (project-link project)
        mail-receiver (mapv :value @(re-frame/subscribe [::link-mail-receiver]))
        project-link-email-body-template @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-dialog-e-mail-project-link-body])
        email-body (-> project-link-email-body-template
                       (gstring/format project-title project-url (:name user-info))
                       (string/replace "NEWLINE" "%0D"))]
    [:div.explorama__form__select.row.explorama__form--flex
     [:div.col-6
      [select {:placeholder          select-placeholder
               :options              (re-frame/subscribe [::mail-receiver-options])
               :values               (re-frame/subscribe [::link-mail-receiver])
               :on-change            (fn [entry]
                                       (re-frame/dispatch [::link-mail-receiver entry]))
               :is-multi?            true
               :close-on-select?     false
               :show-clean-all?      true
               :mark-invalid?        false
               :autofocus?           true
               :extra-class          "input--w14"}]]
     [:div.col-3
      [button {:as-link (str "mailto:" (string/join "; " mail-receiver)
                             "?subject=Project link: " project-title
                             "&body=" email-body)
               :link-target "_blank_"
               :label send-link}]]]))

(defn scroll-to-end [container-id]
  (.setTimeout js/window
               (fn [_]
                 (let [div-con (.getElementById js/document container-id)]
                   (when div-con
                     (aset div-con "scrollTop" (aget div-con "scrollHeight")))))
               100))

(defn link-section [project]
  (let [{:keys [link-label share-link-sublabel aria-copy-project-link]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi :link-label :share-link-sublabel :aria-copy-project-link])
        e-mail? @(re-frame/subscribe [::link-e-mail])
        project-url (project-link project)]
    [:div.share-section
     [:h4 link-label]
     [copy-field {:copy-value project-url
                  :aria-label aria-copy-project-link}]
     [:small.text-secondary share-link-sublabel]
     #_[checkbox-comp ::link-e-mail ::link-e-mail :share-dialog-checkbox-link-e-mail true]
     #_(when e-mail?
         [email-project project])]))

(defn share-section []
  (let [share-with-rows @(re-frame/subscribe [::share-with])
        {:keys [share-label share-entries-sublabel aria-add-new-row]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi :share-label :share-entries-sublabel :aria-add-new-row])]
    [:div.share-section
     [:h4 share-label]
     (reduce (fn [parent [_ share-row-data]]
               (conj
                parent
                [share-with-row share-row-data]))
             [:<>]
             share-with-rows)
     [button {:variant :tertiary
              :on-click #(re-frame/dispatch [::add-share-row])
              :aria-label aria-add-new-row
              :start-icon :plus}]
     [:small.text-secondary share-entries-sublabel]]))

(defn public-section [project share-project?]
  (let [{:keys [publish-label share-publish-label share-publish-sublabel]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi :publish-label :share-publish-label :share-publish-sublabel])
        public-read-only? @(re-frame/subscribe [::public-read-only])]
    [:div.share-section
     [:h4 publish-label]
     [:div.switch.input
      [checkbox {:id "share-publ-toggle"
                 :as-toggle? true
                 :checked? public-read-only?
                 :label share-publish-label
                 :on-change #(re-frame/dispatch [::public-read-only (not public-read-only?)])}]]
     [:small.text-secondary share-publish-sublabel]]))

(defn share-panel []
  (let [is-active? @(re-frame/subscribe [::is-active?])
        project @(re-frame/subscribe [::project])
        share-project-dialog-title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :share-dialog-title])
        cancel-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :cancel-label])
        save-label @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :save-label])
        project-title (:title project)
        share-project? @(re-frame/subscribe [::share-project?])]
    [dialog {:title (str share-project-dialog-title ": " project-title)
             :message
             (cond-> [:div.flex.flex-col.gap-8]
               share-project?
               (conj [public-section project share-project?]
                     [share-section]
                     [link-section project]))
             :show? is-active?
             :hide-fn #(do)
             :ok {:label save-label
                  :start-icon :save
                  :disabled? (not share-project?)
                  :on-click #(re-frame/dispatch [::share])}
             :cancel {:label cancel-label
                      :on-click #(re-frame/dispatch [::cancel-share])
                      :variant :secondary}}]))
