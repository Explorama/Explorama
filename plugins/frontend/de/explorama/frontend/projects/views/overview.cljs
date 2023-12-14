(ns de.explorama.frontend.projects.views.overview
  "Overview of all available projects.
   This supports two ways to be used.

   One is as a complete overview (project-overview).
   The other one is just a section in a overlayer (project-section).

   The project-overview uses the project-section so it can be used as an example."
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.projects.path :as path]
            [de.explorama.frontend.projects.protocol.core :as protocol]
            [de.explorama.frontend.projects.utils.overview :as o-utils]
            [de.explorama.frontend.projects.views.project-card :as card]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button input-field select]]
            [de.explorama.shared.projects.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [re-frame.registrar :as registrar]
            [taoensso.timbre :refer-macros [error]]))

(spec/def :overview.close/text vector?) ; sub-vector defines the close text to be shown
(spec/def :overview.close/event vector?) ; even-vector defines what happens when on close clicked
(spec/def :overview.close/icon #{:close :back :folder-open}) ;change close button appearance

(spec/def :overview.close/desc (spec/keys :req-un [:overview.close/icon
                                                   :overview.close/text
                                                   :overview.close/event]))

(defn- event-handler-exist?
  "Just checks in the re-frame registrar if the event with the id was registered."
  [id]
  (not (nil? (registrar/get-handler :event id))))

(re-frame/reg-event-fx
 ::check-and-load-link
 (fn [{db  :db} _]
   (let [user-info (fi/call-api :user-info-db-get db)
         project-id (fi/call-api :link-info-url-info-db-get db :project-id)]
     (when (and project-id (seq user-info) (not-every? empty? (vals (get-in db path/projects))))
       (let [p-desc (some identity (map #(get % project-id) (vals (get-in db path/projects))))
             locks (get-in db (path/locks))
             notify-info (cond
                           (nil? p-desc) {:message  "No rights to load project"
                                          :type     :warn
                                          :category {:projects :rights}
                                          :vertical "projects"}
                           (get locks project-id) {:message  "Project already loaded"
                                                   :type     :warn
                                                   :category {:projects :rights}
                                                   :vertical "projects"}
                           :else {:message  "Project loading"
                                  :type     :info
                                  :category {:projects :rights}
                                  :vertical "projects"})
             already-loaded? (get locks project-id)]
         (when-not (nil? project-id)
           ;TODO r1/projects this check can be removed when we completly refactor de.explorama.frontend.projects.core
           ;so that the overview can require the namespace that contains the start-loading-project event
           (if (event-handler-exist? :de.explorama.frontend.projects.core/start-loading-project) ; Make sure the handler really exist
             {:fx [[:dispatch (fi/call-api :link-info-remove-infos-event-vec :project-id)]
                   (when (and p-desc (not already-loaded?))
                     [:dispatch (fi/call-api :welcome-dismiss-page-event-vec
                                             [[:de.explorama.frontend.projects.core/start-loading-project p-desc]])])
                   [:dispatch (fi/call-api :notify-event-vec notify-info)]]}
             {:dispatch-later {:ms 100
                               :dispatch [::check-and-load-link]}})))))))

(defn load-project-from-code [loading? project-id head-desc]
  (let [all-projects @(re-frame/subscribe [::projects])
        selected-project (or (get-in all-projects [:created-projects project-id])
                             (get-in all-projects [:allowed-projects project-id])
                             (get-in all-projects [:read-only-projects project-id]))]
    (when (and project-id
               selected-project
               (not loading?))
      {:backend-tube [ws-api/load-project-infos-route
                      {:client-callback [ws-api/load-project-infos-result]}
                      selected-project {:head-desc head-desc}]
       :dispatch-n [(fi/call-api :clean-workspace-event-vec [:de.explorama.frontend.projects.core/clean-workspace-done])
                    [:de.explorama.frontend.projects.views.project-loading-screen/show-dialog project-id]]})))

(re-frame/reg-event-fx
 ::set-result-projects
 (fn [{db :db} [_ query projects]]
   (when (= query (get-in db path/current-search-query))
     {:db (assoc-in db path/projects projects)
      :dispatch-n [[::check-and-load-link]
                   [::protocol/update-based-events]]})))

(defn- request-projects-tube [user-info query]
  (if (and query (< 2 (count query)))
    [ws-api/search-projects-route
     {}
     user-info query]
    [ws-api/request-projects-route
     {:client-callback [ws-api/request-projects-result]}
     user-info]))

(re-frame/reg-event-fx
 ws-api/request-projects-result
 (fn [{db :db} [_ projects]]
   {:db (-> db
            (assoc-in path/projects projects)
            (assoc-in path/created-project-titles (set (map :title (vals (:created-projects projects))))))
    :dispatch-n (conj (map (fn [p]
                             (fi/call-api :service-register-event-vec
                                          :projects-frames-lookup
                                          (:project-id p)
                                          (:frames-map p)))
                           (concat (vals (:created-projects projects))
                                   (vals (:allowed-projects projects))
                                   (vals (:read-only-projects projects))))
                      [::check-and-load-link]
                      [::protocol/update-based-events])}))

(re-frame/reg-event-fx
 ws-api/search-projects-route
 (fn [{db :db} [_ query]]
   (let [user-info (fi/call-api :user-info-db-get db)]
     {:db (assoc-in db path/current-search-query query)
      :backend-tube (request-projects-tube user-info query)})))

(re-frame/reg-event-fx
 ws-api/request-projects-route
 (fn [{db :db} [_ given-user-infos]]
   (let [user-info (fi/call-api :user-info-db-get db)
         search-query (get-in db path/current-search-query)]
     {:backend-tube (request-projects-tube (or given-user-infos
                                               user-info)
                                           search-query)})))

(re-frame/reg-sub
 ::current-search-query
 (fn [db]
   (get-in db path/current-search-query)))

(re-frame/reg-sub
 ::overview-sorting
 (fn [db]
   (get-in db path/current-project-sorting :last-modified)))

(re-frame/reg-event-db
 ::overview-sorting
 (fn [db [_ value]]
   (assoc-in db path/current-project-sorting value)))

(re-frame/reg-sub
 ::overview-filter
 (fn [db]
   (get-in db path/current-project-filter :all)))

(re-frame/reg-event-db
 ::overview-filter
 (fn [db [_ value]]
   (assoc-in db path/current-project-filter value)))

(re-frame/reg-event-fx
 ws-api/copy-project-result
 (fn [_ _]
   {:dispatch [ws-api/request-projects-route]}))

(defn- gen-copy-name [db project-name]
  (let [project-name (string/replace project-name #"\(copy [0-9]*\)| \(copy\)" "")
        same-name-layouts (->> (get-in db path/created-project-titles #{})
                               (filterv #(string/starts-with? % project-name)))
        max-num-used (->> same-name-layouts
                          ; return the x from the substring "(copy x)" or "1" in case of (copy)
                          (mapv #(or (re-find #"(?<=\(copy )[0-9]*(?=\))" %) "1"))
                          (mapv #(js/parseInt %))
                          (apply max))
        ; only add a number, if there already is a copy (+ the base layout itself, hence > 1)
        new-num (when (> (count same-name-layouts) 1) (str " " (inc max-num-used)))]
    (str project-name " (copy" new-num ")")))

(re-frame/reg-event-fx
 ::copy-project
 (fn [{db :db} [_ project-desc]]
   (let [user-info (fi/call-api :user-info-db-get db)
         new-p-id (str (random-uuid))
         new-p-desc (-> project-desc
                        (select-keys [:creator :title :description])
                        (assoc :project-id new-p-id)
                        (update :title #(gen-copy-name db %)))]
     {:backend-tube [ws-api/copy-project-route
                     {:client-callback [ws-api/copy-project-result]}
                     project-desc new-p-desc user-info]})))

(re-frame/reg-sub
 ::projects
 (fn [db _]
   (get-in db path/projects)))

(re-frame/reg-event-fx
 ::show-overview
 (fn [{db :db} _]
   {:db (assoc-in db path/overview-overlayer-active? true)
    :dispatch (fi/call-api :overlayer-active-event-vec true)}))

(re-frame/reg-event-fx
 ::close-overview
 (fn [{db :db} _]
   {:db (-> db
            (assoc-in path/overview-overlayer-active? false)
            (update :projects dissoc :title :description))
    :dispatch (fi/call-api :overlayer-active-event-vec false)}))

(re-frame/reg-event-fx
 ::toggle-project-overview
 (fn [{db :db} _]
   (let [is-active? (get-in db path/overview-overlayer-active?)]
     {:db (cond-> db
            :always (update-in path/overview-overlayer-active? not)
            :always (assoc-in path/overview-show-all? false)
            is-active? (update :projects dissoc :title :description))
      :dispatch-n [(fi/call-api :overlayer-active-event-vec (not is-active?))
                   (when-not is-active?
                     [ws-api/request-projects-route])]})))

(re-frame/reg-sub
 ::is-active?
 (fn [db _]
   (get-in db path/overview-overlayer-active? false)))

(re-frame/reg-sub
 ::show-all?
 (fn [db _]
   (get-in db path/overview-show-all? false)))

;; Internal UI Components

(defn- section-title-bar [{:keys [event
                                  icon
                                  text]}
                          overlayer-close-event
                          on-welcome-page?]
  (let [close-text @(re-frame/subscribe text)
        project-id @(re-frame/subscribe [:de.explorama.frontend.projects.core/loaded-project-id])
        new-project-text @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :new-project])
        clean-workspace-text
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :clean-before-new-project])
        overview-title @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :overview-title])]
    [:div.flex.justify-between
     [:h2
      overview-title]
     [:div.actions.flex.align-items-center
      [:div {:style {:width :fit-content}}
       [tooltip {:text (if project-id clean-workspace-text new-project-text)
                 :use-hover? (some? project-id)}
        [button
         (cond-> {:label new-project-text
                  :start-icon :plus
                  :on-click (fn [_]
                              (when-not project-id
                                (re-frame/dispatch [:de.explorama.frontend.projects.views.create-project/show-dialog true])
                                (re-frame/dispatch overlayer-close-event)))}
           on-welcome-page?
           (assoc :variant :tertiary)
           project-id
           (assoc :disabled? "disabled"))]]]
      [button {:variant :tertiary
               :start-icon icon
               :label  close-text
               :on-click #(re-frame/dispatch event)}]]]))

(defn- search-project []
  (let [placeholder (re-frame/subscribe [:de.explorama.frontend.common.i18n/translate :search-projects])
        {:keys [last-modified-label title-asc title-des share-public-read-only-label
                created-by-me shared-with-me all-projects-label]}
        @(re-frame/subscribe [:de.explorama.frontend.common.i18n/translate-multi :last-modified-label
                              :title-asc :title-des :share-public-read-only-label
                              :created-by-me :shared-with-me :all-projects-label])
        sort-options [{:label last-modified-label
                       :value :last-modified}
                      {:label title-asc
                       :value :title-a-z}
                      {:label title-des
                       :value :title-z-a}]
        filter-options [{:label all-projects-label
                         :value :all}
                        {:label created-by-me
                         :value :created}
                        {:label shared-with-me
                         :value :shared}
                        {:label share-public-read-only-label
                         :value :public}]
        sorting-value @(re-frame/subscribe [::overview-sorting])
        filter-value @(re-frame/subscribe [::overview-filter])]
    [:div.flex.gap-8
     [input-field {:start-icon :search
                   :extra-class "flex-auto"
                   :placeholder placeholder
                   :value (re-frame/subscribe [::current-search-query])
                   :on-change #(re-frame/dispatch [ws-api/search-projects-route %])}]
     [select {:options sort-options
              :is-clearable? false
              :values (first (filter #(= sorting-value (:value %)) sort-options))
              :start-icon :sort
              :on-change #(re-frame/dispatch [::overview-sorting (:value %)])
              :extra-class "input--w12"}]
     [select {:options filter-options
              :is-clearable? false
              :values (first (filter #(= filter-value (:value %)) filter-options))
              :start-icon :filter
              :on-change #(re-frame/dispatch [::overview-filter (:value %)])
              :extra-class "input--w12"}]]))

(defn project-card [project-desc]
  ^{:key (str "project-card-" (:project-id project-desc))}
  [card/project-card project-desc])

(defn- project-list [on-welcome-page?]
  (let [projects @(re-frame/subscribe [::projects])
        show-all? @(re-frame/subscribe [::show-all?])
        overview-sorting @(re-frame/subscribe [::overview-sorting])
        overview-filter @(re-frame/subscribe [::overview-filter])
        shown-projects (->> projects
                            (o-utils/filter-projects overview-filter)
                            (o-utils/sort-projects overview-sorting))]
    [:div {:class (if on-welcome-page?
                    ["projects-container" "narrow"]
                    "projects-container")}
     (into [:div.projects-grid] (map #(project-card %)) shown-projects)]))

;; External UI Components

(defn project-section
  "Section to be shown inside welcome__panel.

   close-action-desc => see spec overview.close/desc

   on-welcome-page? => will add a narrow class to the project list + change the button

   overlayer-close-event => gets called when project create is clicked
   this should in turn completly close the overlayer so the workspace is seen."
  [close-action-desc on-welcome-page? overlayer-close-event]
  (when-not (spec/valid? :overview.close/desc close-action-desc)
    (error "close-action-desc not conform with spec"
           {:desc close-action-desc
            :explain (spec/explain-str :overview.close/desc close-action-desc)}))
  [:div.welcome__section.projects
   [card/card-menu]
   [section-title-bar close-action-desc overlayer-close-event on-welcome-page?]
   [search-project]
   [project-list on-welcome-page?]
   [protocol/open-project-at-step-dialog]])

(defn project-overview
  "Complete Overlayer."
  []
  (let [is-active? @(re-frame/subscribe [::is-active?])]
    (when is-active?
      [:div.welcome__page
       {:style {:z-index 3
                :position :absolute}}
       [:div.welcome__panel
        [project-section
         {:event [::toggle-project-overview]
          :icon :close
          :text [:de.explorama.frontend.common.i18n/translate :close]}
         false
         [::toggle-project-overview]]]])))

(defn welcome-section
  "The project overview section in the welcome page."
  []
  [project-section
   {:event [::show-overview]
    :icon  :folder-open
    :text [:de.explorama.frontend.common.i18n/translate :welcome-project-overview-text]}
   true
   [::close-overview]])
