(ns de.explorama.frontend.search.views.attribute-bar
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.search.config :as config]
            [de.explorama.frontend.search.path :as spath]
            [reagent.core :as reagent]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.search.data.topics :refer [topic-attr-desc is-topic-attr-desc?]]
            [de.explorama.frontend.search.data.categories :as categories]
            [de.explorama.frontend.search.backend.options :as options-backend]
            [de.explorama.frontend.search.backend.util :refer [build-options-request-params]]
            [taoensso.timbre :refer-macros [debug warn error]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [input-field collapsible-list]]
            [de.explorama.shared.search.ws-api :as ws-api]))

(re-frame/reg-event-fx
 ::init-request-attributes
 (fn [{db :db} [_ frame-id]]
   (let [datasources (get-in db spath/search-enabled-datasources)]
     {:dispatch [ws-api/request-attributes datasources frame-id [] {}]})))

(re-frame/reg-sub
 ::attributes
 (fn [db [_ frame-id]]
   (get-in db (spath/frame-attributes frame-id) [])))

(re-frame/reg-event-fx
 ::update-attributes
 (fn [{db :db} [_ frame-id attributes]]
   (let [render? (fi/call-api [:interaction-mode :render-db-get?] db)
         frame-infos (fi/call-api :frame-db-get db frame-id)]
     (when (or frame-infos
               (not render?))
       {:db (-> db
                (assoc-in (spath/frame-attributes frame-id) attributes)
                (update-in spath/event-callback dissoc frame-id)
                (update :search dissoc spath/frame-open-event-key))
        :dispatch-n [(get-in db (spath/frame-event-callback frame-id))
                     (when-let [open-event (get-in db spath/frame-open-event)]
                       (vec (conj open-event frame-id)))]}))))

(re-frame/reg-event-fx
 ::add-search-row
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id attr-desc path]]
   (let [[attr] attr-desc
         formdata (get-in db (spath/frame-search-rows frame-id) {})
         {fd :formdata} (build-options-request-params db frame-id attr formdata false)
         timestamp (.getTime (js/Date.))
         datasources (get-in db spath/search-enabled-datasources)]
     (debug "add-search-row\nframe-id" frame-id "\nattr" attr "\npath" path)
     (cond-> {:db (-> db
                      (assoc-in (spath/search-frame-changed? frame-id) true)
                      (assoc-in path {:timestamp timestamp}))}
       fd
       (assoc :fx [[:dispatch [ws-api/search-options
                               datasources frame-id [attr-desc] fd]]
                   [:dispatch [ws-api/request-attributes
                               datasources
                               frame-id
                               (conj (vec (keys formdata))
                                     attr-desc)
                               fd]]])))))

(re-frame/reg-sub
 ::sorted-search-attributes
 (fn [db [_ frame-id]]
   (vec (keys (sort-by #(get-in % [1 :timestamp]) (get-in db (conj config/search-pre-path frame-id) []))))))

(re-frame/reg-event-fx
 ::delete-search-row
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id attribute ignore-request?]]
   (cond-> {:db (-> db
                    (update-in (conj config/search-pre-path frame-id) dissoc attribute)
                    (assoc-in (spath/search-frame-changed? frame-id) true))}
     (not ignore-request?)
     (assoc :dispatch [::options-backend/request-options frame-id nil true]))))

(re-frame/reg-event-db
 ::delete-all-search-rows
 [(fi/ui-interceptor)]
 (fn [db [_ frame-id]]
   (update-in db config/search-pre-path dissoc frame-id)))

(re-frame/reg-sub
 ::get-search-rows
 (fn [db [_ frame-id]]
   (get-in db
           (conj config/search-pre-path frame-id))))

(re-frame/reg-sub
 ::get-search-row
 (fn [db [_ frame-id attr]]
   (get-in db
           (conj config/search-pre-path frame-id attr))))

(defn scroll-to-end [frame-id]
  (.setTimeout js/window
               (fn [_]
                 (let [div-con (.getElementById js/document (str "searchformend-" frame-id))
                       div-height (aget div-con "scrollHeight")]
                   (aset div-con "scrollTop" div-height)))
               100))

(defn attributes-selection-bar [_frame-id]
  (let [filter-text-state (reagent/atom "")
        reset-filtertext-fn #(reset! filter-text-state "")]
    (fn [frame-id]
      (let [filter-placeholder @(re-frame/subscribe [::i18n/translate :attributes-filter-placeholder])
            read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                     {:frame-id frame-id
                                      :component :search
                                      :additional-info :search})
            filter-text @filter-text-state]
        [:div.search__sidebar
         [:div.search__sidebar__filter
          ;; [:h1 attributes-title]
          [input-field
           {:extra-class "search__filter input--w100"
            :disabled? read-only?
            :placeholder filter-placeholder
            :value filter-text
            :on-change (fn [value]
                         (reset! filter-text-state (str/lower-case value)))}]]
         [:div.search__sidebar__list
          [collapsible-list {:items (re-frame/subscribe [::categories/items frame-id :free filter-text true])
                             :disabled? read-only?
                             :open-all? (boolean
                                         (and (seq filter-text)
                                              (not (str/blank? filter-text))))
                             :on-click (fn [{:keys [attr-desc] :as item}]
                                         (if (vector? attr-desc)
                                           (do
                                             (re-frame/dispatch [::add-search-row frame-id attr-desc (spath/search-row-data frame-id attr-desc)])
                                             (when (is-topic-attr-desc? attr-desc)
                                               (let [path (spath/search-row-data frame-id topic-attr-desc)
                                                     pref-topic-select? @(fi/call-api [:user-preferences :preference-sub]
                                                                                      "search-topic-selection?")]
                                                 (when pref-topic-select?
                                                   (re-frame/dispatch [:de.explorama.frontend.search.views.formdata/add-data-for-attr path :topic-selection? true]))))
                                             (scroll-to-end frame-id)
                                             (reset-filtertext-fn))
                                           (error "Failed to add search-row " {:item item
                                                                               :frame-id frame-id})))
                             :collapse-items-fn (fn [category]
                                                  (re-frame/subscribe [::categories/child-items frame-id category filter-text]))}]]]))))
