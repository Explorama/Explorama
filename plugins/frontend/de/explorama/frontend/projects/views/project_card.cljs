(ns de.explorama.frontend.projects.views.project-card
  "Containing everything to display a single project-card.
   This is in turn used by the projects.overview."
  (:require [cljs-time.coerce :as date-coerce]
            [cljs-time.format :as date-format]
            [clojure.string :as str]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.projects.config :as config-projects]
            [de.explorama.frontend.projects.path :as path]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field textarea]]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position context-menu]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.interop :refer [format]]
            [de.explorama.shared.common.config :as config-shared]
            [de.explorama.shared.common.configs.platform-specific :as config-platform]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

;Project-desc
#_{:project-id ""
   :title ""
   :description "" ; can be nil
   :creator "" ;creator Username
   :allowed-user [] ; allowed username list
   :allowed-group [] ; allowed group list
   :exportable? true|false; can be exported as link/pdf
   :shared-to {} ; map containing the informetion by whom the user/role got this project shared
   ; key can be <username>, <role>, "*" value: <username> of the one that shared the project
   :steps 0} ;Number of steps for this project (not pre-process so raw logged-events)

(def ^:private opposite-type {:title :description
                              :description :title})

;; Internal events/Subs

(re-frame/reg-event-db
 ::update-project-details
 [(fi/ui-interceptor)]
 (fn [db [_ update-type project-id]]
   (cond-> db
     :always (assoc-in (path/project-update-detail update-type) project-id)
     project-id (assoc-in (path/project-update-detail (get opposite-type update-type)) nil))))

(re-frame/reg-sub
 ::update-project-detail
 (fn [db [_ project-id update-type]]
   (= (get-in db (path/project-update-detail update-type))
      project-id)))

(re-frame/reg-event-fx
 ::update-project-detail
 (fn [_ [_ project-id update-type new-value userinfo]]
   {:backend-tube [ws-api/update-project-detail-route
                   {:client-callback [ws-api/request-projects-result]}
                   project-id update-type new-value userinfo]}))

(re-frame/reg-event-db
 ::set-loading-project
 (fn [db [_ project]]
   (assoc-in db path/to-be-loaded project)))

(re-frame/reg-sub
 ::locks
 :<- [:de.explorama.frontend.projects.core/locks]
 (fn [locks [_ project-id]]
   (get locks project-id)))

(re-frame/reg-sub
 ::notification
 :<- [:de.explorama.frontend.projects.core/relevant-notifications]
 (fn [notification [_ project-id]]
   ((set notification)
    project-id)))

(re-frame/reg-event-db
 ::card-menu
 (fn [db [_ value]]
   (assoc-in db path/card-menu-props value)))

(re-frame/reg-sub
 ::card-menu
 (fn [db _]
   (get-in db path/card-menu-props)))

;; Dialog releated Subs/Events

(re-frame/reg-event-db
 ::confirm-dialog
 (fn [db [_ value]]
   (assoc-in db path/confirm-dialog value)))

(re-frame/reg-sub
 ::confirm-dialog
 (fn [db _]
   (get-in db path/confirm-dialog)))

(re-frame/reg-sub
 ::show-confirm-dialog
 (fn [db _]
   (get-in db path/project-show-confirm-dialog true)))

(re-frame/reg-event-db
 ::reset-show-confirm-dialog
 (fn [db [_ val]]
   (assoc-in db path/project-show-confirm-dialog val)))

(re-frame/reg-event-db
 ::reset-confirm-dialog
 (fn [db [_]]
   (update db :projects dissoc :confirm-dialog)))

;; Helper Functions

(defn- is-pinfo-editable [rw-rights? locked? username]
  (and rw-rights?
       (or (nil? locked?)
           (= (get-in locked? [:lock-user :username])
              username))))

(defn- update-project-detail [userinfo project-id update-type current-val new-val]
  (let [info-at-least-chars @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :info-at-least-chars])
        info-at-least-chars (format info-at-least-chars 4)
        already-exists? (when-not (= :description update-type)
                          @(re-frame/subscribe [:de.explorama.frontend.projects.views.create-project/project-already-exists? new-val userinfo]))
        valid-min-chars? (or (not= update-type :title) (>= (count new-val) config-projects/min-project-title-length))]
    (cond
      (= current-val new-val) (re-frame/dispatch [::update-project-details update-type nil])
      already-exists?
      (fi/call-api :notify-event-dispatch {:message @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :warning-project-title-already-exists])
                                           :category {:projects :project-settings}
                                           :type :error})
      (not valid-min-chars?)
      (fi/call-api :notify-event-dispatch {:message info-at-least-chars
                                           :category {:projects :project-settings}
                                           :type :error})
      :else
      (do
        (re-frame/dispatch [::update-project-detail project-id update-type new-val userinfo])
        (re-frame/dispatch [::update-project-details update-type nil])))))

;; Project-Card Title/Desc (left part)

(defn- input-form [_project-id _update-type title-replacement _userinfo]
  (let [new-val (reagent/atom (or title-replacement ""))]
    (fn [project-id update-type title-replacement userinfo]
      (let [project-desc-placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :project-desc-placeholder])
            placeholder (when (and (= :description update-type)
                                   (empty? title-replacement))
                          project-desc-placeholder)
            max-length (if (= :description update-type)
                         config-projects/max-project-desc-length
                         config-projects/max-project-title-length)
            ;dont prevent for typing whitespace at end while typing, but trim it when finished and for check
            visible-normalized (if (= :description update-type) @new-val (str/triml @new-val))
            new-val-normalized (if (= :description update-type) @new-val (str/trim @new-val))
            update-detail-fn (partial update-project-detail userinfo project-id update-type title-replacement)
            already-exists? (when-not (= :description update-type)
                              (and (not= title-replacement new-val-normalized)
                                   @(re-frame/subscribe [:de.explorama.frontend.projects.views.create-project/project-already-exists? new-val-normalized userinfo])))
            valid-min-chars? (or (not= update-type :title) (>= (count new-val-normalized) config-projects/min-project-title-length))
            valid? (and valid-min-chars? (not already-exists?))
            info-at-least-chars @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :info-at-least-chars])
            info-at-least-chars (format info-at-least-chars config-projects/min-project-title-length)
            warning-project-title-already-exists @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :warning-project-title-already-exists])]
        [:div.explorama__form__textarea
         {:class (when-not valid?
                   "explorama__form--error")}
         [:form
          {:on-submit #(do
                         (.preventDefault %)
                         (when valid?
                           (update-detail-fn new-val-normalized)))
           :on-key-down (fn [ev]
                          (cond
                            (and (= (.-keyCode ev) 13)
                                 (not (.-shiftKey ev))) ; Enter without shift
                            (do
                              (.preventDefault ev)
                              (.blur (.. ev -target)))
                            (= (.-keyCode ev) 27) ; Escape
                            (re-frame/dispatch [::update-project-details update-type nil])))}
          [(if (= :description update-type)
             textarea
             input-field)
           {:parent-container [:<>]
            :required? true
            :autofocus? true
            :max-length max-length
            :value visible-normalized
            :placeholder placeholder
            :extra-class "mosaic__box__title__input"
            :id (str "project-input-" update-type project-id)
            :on-blur #(do (.stopPropagation %)
                          (when (and valid? (.hasFocus js/document))
                            (update-detail-fn new-val-normalized)))
            :on-change (fn [val]
                         (reset! new-val val))}]]
         (when-not valid-min-chars?
           [:div.form__message info-at-least-chars])
         (when already-exists?
           [:div.form__message warning-project-title-already-exists])]))))

(defn- edit-on-double-click
  "Edit-info-key => what is being edit :title|:description, also used for the dialog tag
   editable? => if the project infos can be edit right now"
  [{:keys [project-id] :as project-desc}
   edit-info-key
   editable? rw-rights?]
  (if editable?
    (re-frame/dispatch [::update-project-details edit-info-key project-id])
    (if rw-rights?
      (do
        (re-frame/dispatch [::confirm-dialog {:project project-desc
                                              :tag (name edit-info-key)}])
        (re-frame/dispatch [:de.explorama.frontend.projects.views.confirm-dialog/show-dialog true]))
      (do
        (re-frame/dispatch [::confirm-dialog {:project project-desc
                                              :tag "read-only"}])
        (re-frame/dispatch [:de.explorama.frontend.projects.views.confirm-dialog/show-dialog true])))))

;; Notifications

(defn- card-notifications
  [{:keys [project-id]}]
  (let [notifications @(re-frame/subscribe [::notification project-id])]
    (when notifications
      [:div.new-indicator])))

;; Context Menu

(defn card-menu []
  (let [{:keys [position] :or {position {}} project-desc :desc rw-rights? :rw :as desc}
        @(re-frame/subscribe [::card-menu])
        {:keys [project-id shared-to creator
                exportable?
                hide-in-overview
                allowed-user allowed-group] :as project-desc}
        project-desc
        {:keys [username role] :as user-info} @(fi/call-api :user-info-sub)
        is-creator? (= creator username)
        project-lock-info @(re-frame/subscribe [::locks project-id])
        show-confirm-dialog? @(re-frame/subscribe [::show-confirm-dialog])
        project-opened-by (get-in project-lock-info [:lock-user :name])
        is-loaded? (= project-id @(re-frame/subscribe [:de.explorama.frontend.projects.core/loaded-project-id]))
        shared? (not-empty shared-to)
        share-button-deactivated (and (or (not (or is-creator?
                                                   (some #(= username %) allowed-user)
                                                   (some #(= role %) allowed-group)
                                                   exportable?))
                                          (not rw-rights?))
                                      (not config-platform/explorama-multi-user))
        shared-with (-> shared-to
                        (dissoc username "*")
                        keys)
        {:keys [share-label delete-label copy-label open-project-step]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi :share-label :delete-label :copy-label :open-project-step])]
    [context-menu
     {:on-close #(re-frame/dispatch [::card-menu nil])
      :show? (boolean project-desc)
      :position position
      :items [(when config-platform/explorama-multi-user
                {:label share-label
                 :icon :share
                 :disabled? share-button-deactivated
                 :on-click #(re-frame/dispatch [:de.explorama.frontend.projects.views.share-project/show-dialog
                                                true
                                                project-desc
                                                user-info
                                                rw-rights?])})
              {:label copy-label
               :icon :copy
               :on-click #(re-frame/dispatch [:de.explorama.frontend.projects.views.overview/copy-project project-desc])}
              {:label open-project-step
               :icon :history
               :disabled? (or is-loaded? project-lock-info)
               :on-click (fn []
                           (re-frame/dispatch [:de.explorama.frontend.projects.protocol.core/open-at-step-dialog project-desc])
                           (re-frame/dispatch [:de.explorama.frontend.projects.protocol.core/show-step-dialog true]))}
              {:label delete-label
               :icon :trash
               :disabled? (if config-platform/explorama-multi-user
                            (or project-lock-info
                                (not rw-rights?))
                            project-lock-info)
               :on-click #(do
                            (re-frame/dispatch [::confirm-dialog {:project project-desc
                                                                  :tag "delete"
                                                                  :shared-with shared-with}])
                            (re-frame/dispatch [:de.explorama.frontend.projects.views.confirm-dialog/show-dialog true]))}]}]))

;; Title + Desc

(defn card-header [{:keys [title project-id] :as project-desc} rw-rights? user-info pinfo-editable?]
  (let [rename-title? @(re-frame/subscribe [::update-project-detail project-id :title])]
    [:div.header
     [:div.title
      {:on-double-click #(edit-on-double-click
                          project-desc :title pinfo-editable? rw-rights?)}
      (if rename-title?
        [input-form project-id :title title user-info]
        title)]
     [button {:variant :tertiary
              :on-click #(re-frame/dispatch [::card-menu {:desc project-desc
                                                          :position (calc-menu-position %)
                                                          :rw rw-rights?}])
              :aria-label :aria-label-card-menu
              :start-icon :menu}]]))

(defn card-description [{:keys [description project-id] :as project-desc} rw-rights? user-info pinfo-editable?]
  (let [change-description? @(re-frame/subscribe [::update-project-detail project-id :description])
        description-empty? (empty? ((fnil str/trim "") description))
        placeholder @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :project-desc-placeholder])
        project-description (if description-empty?
                              placeholder
                              description)]
    [:div {:class (cond-> ["description"]
                    (nil? description) (conj "placeholder"))
           :on-double-click #(edit-on-double-click
                              project-desc :description pinfo-editable? rw-rights?)}
     (if change-description?
       [input-form project-id :description description user-info]
       project-description)]))

;; Footer

(defn timestamp->string [timestamp]
  (let [current-lang @(re-frame/subscribe [:de.explorama.frontend.common.i18n/current-language])
        date-formatter (case current-lang
                         :de-DE (date-format/formatter "dd MMM yyyy")
                         (date-format/formatter "MMM dd yyyy"))]
    (when timestamp
      (cond-> (date-format/unparse date-formatter
                                   (date-coerce/from-long timestamp))
        (= current-lang :de-DE)
        (-> (str/replace #"Mar" "Mär")
            (str/replace #"May" "Mai")
            (str/replace #"Oct" "Okt")
            (str/replace #"Dec" "Dez"))))))

(defn card-footer [{:keys [shared-to creator last-modified] :as project-desc} rw-rights? is-loaded? lock-info]
  (let [is-shared? (not (empty? shared-to))
        opened-by (get-in lock-info [:lock-user :name])
        locked-by-client (:client-id lock-info)
        date (timestamp->string last-modified)
        creator-name @(fi/call-api :name-for-user-sub creator)
        {:keys [shared-project-label last-modified-label author-label read-only close
                open locked project-already-loaded]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi :shared-project-label
                              :last-modified-label :author-label :read-only :close
                              :open :locked :project-already-loaded])
        own-client-id @(fi/call-api :client-id-sub)
        show-edit-icon? (and lock-info
                             config-shared/explorama-project-sync?
                             (not= own-client-id locked-by-client))]
    [:div.footer
     (if is-shared?
       [tooltip {:text shared-project-label}
        [icon {:icon :users
               :size :large}]]
       [icon {:icon :user
              :size :large}])
     [:div.meta
      [:div (str author-label " " creator-name)]
      [:div (str last-modified-label " " date)]]
     (when-not rw-rights?
       [tooltip {:text read-only}
        [icon {:icon "icon-read-only"}]])
     (when show-edit-icon?
       [tooltip {:text project-already-loaded}
        [icon {:icon :user-edit
               :extra-class ["animation-pulse-weak"
                             "loop-animation"]}]])
     [button
      (cond
        is-loaded?
        {:variant :tertiary
         :start-icon :close
         :label close
         :on-click #(do
                      (re-frame/dispatch [:de.explorama.frontend.projects.core/dont-cleanall-veto])
                      (re-frame/dispatch (fi/call-api [:interaction-mode :set-pending-normal-event]))
                      (re-frame/dispatch (fi/call-api :clean-workspace-event-vec
                                                      [:de.explorama.frontend.projects.core/clean-workspace-done])))}

        (and lock-info config-shared/explorama-project-sync?)
        {:variant :primary
         :start-icon :folder-open
         :title (str)
         :label open
         :on-click #(do
                      (re-frame/dispatch [:de.explorama.frontend.projects.views.overview/close-overview])
                      (re-frame/dispatch (fi/call-api :welcome-close-page-event-vec))
                      (re-frame/dispatch (fi/call-api :welcome-dismiss-page-event-vec
                                                      [[::set-loading-project {:project project-desc}]
                                                       [:de.explorama.frontend.projects.views.warning-dialog/handle-load-project-with-warning
                                                        project-desc nil true]])))}

        lock-info
        {:variant :primary
         :start-icon :lock
         :title (str)
         :label locked
         :on-click #(do)
         :disabled? true}

        :else {:variant :primary
               :start-icon :folder-open
               :label open
               :on-click #(when-not lock-info
                            (re-frame/dispatch [:de.explorama.frontend.projects.views.overview/close-overview])
                            (re-frame/dispatch (fi/call-api :welcome-close-page-event-vec))
                            (re-frame/dispatch (fi/call-api :welcome-dismiss-page-event-vec
                                                            [[::set-loading-project {:project project-desc}]
                                                             [:de.explorama.frontend.projects.views.warning-dialog/handle-load-project-with-warning
                                                              project-desc]])))})]]))

;; External UI Component

(defn project-card
  "Representing one project-card."
  [{:keys [project-id allowed-user allowed-groups] :as project-desc}]
  (let [{:keys [username role] :as user-info} @(fi/call-api :user-info-sub)
        rw-rights? (or (some #{username} allowed-user)
                       (some #{role} allowed-groups))
        is-loaded? (= project-id @(re-frame/subscribe [:de.explorama.frontend.projects.core/loaded-project-id]))
        lock-info @(re-frame/subscribe [::locks project-id])
        {:keys [username role] :as user-info} @(fi/call-api :user-info-sub)
        pinfo-editable? (is-pinfo-editable rw-rights? lock-info username)]
    [:div {:class (cond-> ["card"]
                    is-loaded? (conj "active"))
           :on-click #(.stopPropagation %)}
     [card-notifications project-desc]
     [card-header project-desc rw-rights? user-info pinfo-editable?]
     [card-description project-desc rw-rights? user-info pinfo-editable?]
     [card-footer project-desc rw-rights? is-loaded? lock-info]]))
