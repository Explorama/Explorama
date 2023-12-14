(ns de.explorama.frontend.reporting.views.dashboards.overview
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.data.dashboards :as dbs]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.util.date :refer [timestamp->date-str]]
            [de.explorama.frontend.reporting.util.frames :refer [gen-frame-id]]
            [de.explorama.frontend.reporting.views.dashboards.template-schema :refer [schema-modules-grid]]
            [de.explorama.frontend.reporting.views.dashboards.view :as dashboard-view]
            [de.explorama.frontend.reporting.screenshot.core :refer [screenshot-api]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx subscribe]]))

(defn- fetch-tool-infos [db d-id modules]
  (mapv (fn [{:keys [tool vertical] :as module-desc}]
          (assoc module-desc
                 :frame-id (gen-frame-id vertical {:dashboard d-id})
                 :module (fi/call-api :module-db-get db tool)
                 :tool-desc (fi/call-api :tool-desc-db-get db tool)))
        modules))

(reg-event-fx
 ::close-dashboard
 (fn [{db :db} [_ d-id]]
   {:db (dr-path/dissoc-in db (dr-path/visible-dr d-id))}))

(reg-event-fx
 ::show-dashboard
 (fn [{db :db} [_ d-id]]
   (let [d (or (get-in db (dr-path/dashboard d-id))
               (get-in db (dr-path/shared-dashboard d-id)))
         d (update d :modules (partial fetch-tool-infos db d-id))
         tab-title (str (i18n/translate db :dashboard-label)
                        ": "
                        (:name d))
         last-updated (:timestamp d)]
     {:db (cond-> db
            (every? (fn [[id {:keys [timestamp]}]]
                      (or (not= id d-id)
                          (and id d-id
                               (not= last-updated timestamp))))
                    (get-in db dr-path/visible-drs))
            (assoc-in (dr-path/visible-dr d-id) d))
      :dispatch-n [(fi/call-api [:tabs :register-event-vec]
                                {:context-id {:id d-id
                                              :type :dashboard}
                                 :export-fn screenshot-api
                                 :content-context :dashboard
                                 :origin config/default-namespace
                                 :label tab-title
                                 :on-render dashboard-view/dashboard-view
                                 :active? true
                                 :on-close (fn [] (dispatch [::close-dashboard d-id]))})]})))

(reg-event-db
 ::set-burger-menu-infos
 (fn [db [_ infos]]
   (assoc-in db dr-path/burger-menu-infos infos)))

(defn- dashboard-cards [dashboards is-plugin? creator? standalone-link-fn browser-url-fn]
  (let [dr-card-subtitle-prefix @(subscribe [::i18n/translate :last-changed-label])]
    (reduce (fn [acc {:keys [id name timestamp] :as dashboard-desc}]
              (conj acc
                    [:li {:on-click #(dispatch [::show-dashboard id])}
                     [:div.card__image
                      [schema-modules-grid dashboard-desc]]
                     [:div.card__text
                      [:div.title name]
                      [:div.subtitle (str dr-card-subtitle-prefix " " (timestamp->date-str timestamp))]]
                     (when (and is-plugin? creator?)
                       [:div.card__actions {:on-mouse-up #(.stopPropagation %)
                                            :on-click #(do
                                                         (.stopPropagation %)
                                                         (dispatch [::set-burger-menu-infos {:event %
                                                                                             :dr dashboard-desc}]))}
                        [:div [icon {:icon :menu}]]])]))
            [:ul]
            (sort-by :timestamp
                     >
                     dashboards))))

(defn overview [browser-url-fn standalone-link-fn]
  (let [is-plugin? (boolean standalone-link-fn)
        {user-dashboards :created
         shared-dashboards :shared}
        @(subscribe [::dbs/dashboards])
        {:keys [my-dashboards-label created-by-me-label shared-with-me-label]}
        @(subscribe [::i18n/translate-multi :shared-dashboards-label :my-dashboards-label :created-by-me-label :dashboards-label :shared-with-me-label])]
    [:div.section__cards
     (when-not is-plugin? [:h2 my-dashboards-label])
     [:div
      [:h3 (if is-plugin? my-dashboards-label created-by-me-label)]
      [dashboard-cards (vals user-dashboards) is-plugin? true standalone-link-fn browser-url-fn]]
     [:div
      [:h3 shared-with-me-label]
      [dashboard-cards (vals shared-dashboards) is-plugin? false standalone-link-fn browser-url-fn]]]))