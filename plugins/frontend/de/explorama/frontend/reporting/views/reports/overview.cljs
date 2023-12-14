(ns de.explorama.frontend.reporting.views.reports.overview
  (:require [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.reporting.paths.dashboards-reports :as dr-path]
            [de.explorama.frontend.reporting.data.reports :as reps]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.reporting.util.date :refer [timestamp->date-str]]
            [de.explorama.frontend.reporting.util.frames :refer [gen-frame-id]]
            [de.explorama.frontend.reporting.config :as config]
            [de.explorama.frontend.reporting.screenshot.core :refer [screenshot-api]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.reporting.views.reports.view :as report-view]))

(defn- fetch-tool-infos [db r-id modules]
  (mapv (fn [{:keys [tool vertical] :as module-desc}]
          (assoc module-desc
                 :frame-id (gen-frame-id vertical {:report r-id})
                 :module (fi/call-api :module-db-get db tool)
                 :tool-desc (fi/call-api :tool-desc-db-get db tool)))
        modules))

(reg-event-fx
 ::close-report
 (fn [{db :db} [_ r-id]]
   {:db (dr-path/dissoc-in db (dr-path/visible-dr r-id))}))

(reg-event-fx
 ::show-report
 (fn [{db :db} [_ r-id]]
   (let [r (or (get-in db (dr-path/report r-id))
               (get-in db (dr-path/shared-report r-id)))
         r (update r :modules (partial fetch-tool-infos db r-id))
         tab-title (str (i18n/translate db :report-label)
                        ": "
                        (:name r))]
     {:db (cond-> db
            (every? (fn [[id]]
                      (not= id r-id))
                    (get-in db dr-path/visible-drs))
            (assoc-in (dr-path/visible-dr r-id) r))
      :dispatch-n [(fi/call-api [:tabs :register-event-vec]
                                {:context-id {:id r-id
                                              :type :report}
                                 :export-fn screenshot-api
                                 :content-context :report
                                 :origin config/default-namespace
                                 :label tab-title
                                 :on-render report-view/report-view
                                 :active? true
                                 :on-close (fn [] (dispatch [::close-report r-id]))})]})))

(reg-event-db
 ::set-burger-menu-infos
 (fn [db [_ infos]]
   (assoc-in db dr-path/burger-menu-infos infos)))

(defn- report-cards [reports is-plugin? creator? standalone-link-fn browser-url-fn]
  (let [dr-card-subtitle-prefix @(subscribe [::i18n/translate :last-changed-label])]
    (reduce (fn [acc {:keys [id name timestamp] :as report-desc}]
              (conj acc
                    [:li {:on-click #(dispatch [::show-report id])}
                     [:div.card__image
                      [icon {:icon :report :size :xxl :color :gray :brightness 3}]]
                     [:div.card__text
                      [:div.title name]
                      [:div.subtitle (str dr-card-subtitle-prefix " " (timestamp->date-str timestamp))]]
                     (when (and is-plugin? creator?)
                       [:div.card__actions {:on-mouse-up #(.stopPropagation %)
                                            :on-click #(do
                                                         (.stopPropagation %)
                                                         (dispatch [::set-burger-menu-infos {:event %
                                                                                             :dr report-desc}]))}
                        [:div [icon {:icon :menu}]]])]))
            [:ul]
            (sort-by :timestamp
                     >
                     reports))))

(defn overview [browser-url-fn standalone-link-fn]
  (let [is-plugin? (boolean standalone-link-fn)
        {user-reports :created
         shared-reports :shared}
        @(subscribe [::reps/reports])
        {:keys [my-reports-label  created-by-me-label shared-with-me-label]}
        @(subscribe [::i18n/translate-multi :shared-reports-label :my-reports-label :created-by-me-label :reports-label :shared-with-me-label])]
    [:div.section__cards
     (when-not is-plugin? [:h2 my-reports-label])
     [:div
      [:h3 (if is-plugin? my-reports-label created-by-me-label)]
      [report-cards (vals user-reports) is-plugin? true standalone-link-fn browser-url-fn]]
     [:div
      [:h3 shared-with-me-label]
      [report-cards (vals shared-reports) is-plugin? false standalone-link-fn browser-url-fn]]]))