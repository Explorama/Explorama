(ns de.explorama.frontend.expdb.settings
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.expdb.path :as path]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button upload]]
            [de.explorama.frontend.ui-base.components.misc.core :refer [icon]]
            [de.explorama.frontend.ui-base.utils.data-exchange :as data-exchange]
            [de.explorama.shared.expdb.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]))

(re-frame/reg-event-fx
 ::load-buckets
 (fn [_ _]
   {:backend-tube [ws-api/load-buckets
                   {:client-callback [ws-api/load-buckets-result]}]}))

(re-frame/reg-event-db
 ws-api/load-buckets-result
 (fn [db [_ result]]
   (assoc-in db path/buckets result)))

(re-frame/reg-event-fx
 ::download-bucket
 (fn [_ [_ type bucket]]
   {:backend-tube [ws-api/download-bucket
                   {:client-callback [ws-api/download-bucket-result]}
                   type
                   bucket]}))

(re-frame/reg-event-fx
 ws-api/download-bucket-result
 (fn [_ [_ bucket-name content]]
   (data-exchange/download-content
    (str bucket-name ".edn")
    content)
   {}))

(re-frame/reg-event-fx
 ::download-expdb
 (fn [_ _]
   {:backend-tube [ws-api/download-expdb
                  {:client-callback [ws-api/download-expdb-result]}]}))

(re-frame/reg-event-fx
 ws-api/download-expdb-result
 (fn [_ [_ content]]
   (data-exchange/download-content
    "expdb.edn"
    content)
   {}))

(re-frame/reg-event-fx
 ::upload-expdb
 (fn [{db :db} [_ data]]
   {:db (assoc-in db path/uploading? true)
    :backend-tube [ws-api/upload-expdb
                  {:client-callback [ws-api/upload-expdb-result]}
                  data]}))

(re-frame/reg-event-fx
 ws-api/upload-expdb-result
 (fn [{db :db} _]
   {:db (assoc-in db path/uploading? false)
    :dispatch (fi/call-api :notify-event-vec {:type :success
                                              :message :expdb-settings-upload-success})}))

(re-frame/reg-event-fx
 ::upload-bucket
 (fn [{db :db} [_ type bucket data]]
   {:db (assoc-in db path/uploading? true)
    :backend-tube [ws-api/upload-expdb
                  {:client-callback [ws-api/upload-bucket-result]}
                  type bucket data]}))

(re-frame/reg-event-fx
 ws-api/upload-bucket-result
 (fn [{db :db} _]
   {:db (assoc-in db path/uploading? false)
    :dispatch (fi/call-api :notify-event-vec {:type :success
                                              :message :expdb-settings-upload-success})}))

(re-frame/reg-sub
 ::buckets
 (fn [db]
   (get-in db path/buckets)))

(defn- bucket-section [[type bucket-name]]
  (let [{:keys [expdb-settings-export
                expdb-settings-import]}
        @(re-frame/subscribe [::i18n/translate-multi
                              :expdb-settings-export
                              :expdb-settings-import])]
    [:div {:style {:display :flex
                   :justify-content :space-between}}
     bucket-name
     [:div {:style {:display :flex
                    :justify-content :space-between
                    :align-items :flex-start}}
      [button
       {:label expdb-settings-export
        :start-icon :download
        :on-click (fn [_]
                    (re-frame/dispatch [::download-bucket type bucket-name]))}]
      [upload
       {:multi-files? false
        :variant :button
        :upload-button-params {:label expdb-settings-import
                               :start-icon :upload}
        :on-file-loaded (fn [result _]
                          (re-frame/dispatch [::upload-bucket type bucket-name result]))
        :file-type [".edn"]
        :local-read-as :clj}]]]))

(defn expdb-settings []
  (re-frame/dispatch [::load-buckets])
  (fn []
    (let [{:keys [expdb-settings-all-buckets
                  expdb-settings-export
                  expdb-settings-import
                  expdb-settings-parts]}
          @(re-frame/subscribe [::i18n/translate-multi
                                :expdb-settings-all-buckets
                                :expdb-settings-export
                                :expdb-settings-import
                                :expdb-settings-parts])
          buckets @(re-frame/subscribe [::buckets])]
      [:div.content.settings
       [:div {:style {:display :flex
                      :justify-content :space-between}}
        [:h2
         [icon {:icon :database}]
         expdb-settings-all-buckets]
        [:div {:style {:display :flex
                       :justify-content :space-between
                       :align-items :flex-start}}
         [button
          {:label expdb-settings-export
           :start-icon :download
           :on-click (fn [_]
                       (re-frame/dispatch [::download-expdb]))}]
         [upload
          {:multi-files? false
           :variant :button
           :upload-button-params {:label expdb-settings-import
                                  :start-icon :upload}
           :on-file-loaded (fn [result _]
                             (re-frame/dispatch [::upload-expdb result]))
           :file-type [".edn"]
           :local-read-as :clj}]]]
       (into
        [:div
         [:h2
          [icon {:icon :database}]
          expdb-settings-parts]]
        (map (fn [bucket]
               (bucket-section bucket))
             buckets))])))