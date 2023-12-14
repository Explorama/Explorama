(ns de.explorama.frontend.woco.copyright
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.shared.woco.ws-api :as ws-api]))

(re-frame/reg-event-db
 ::reset-datalink
 (fn [db [_]]
   (assoc-in db path/datalink nil)))

(re-frame/reg-event-fx
 ::set-datalink
 (fn [{:keys [db]} [_ type]]
   (let [language (i18n/current-language db)]
     {:backend-tube [ws-api/request-datalink
                     {:client-callback [ws-api/request-datalink-result]}
                     type language]})))

(re-frame/reg-event-db
 ws-api/request-datalink-result
 (fn [db [_ data]]
   (assoc-in db path/datalink data)))

(re-frame/reg-sub
 ::datalink
 (fn [db]
   (get-in db path/datalink nil)))

(defn links [align]
  [:<>
   (when (= "TRIAL" config/environment)
     (let [{:keys [terms-of-use-label impressum-label privacy-label]} @(re-frame/subscribe [::i18n/translate-multi
                                                                                            :terms-of-use-label
                                                                                            :impressum-label
                                                                                            :privacy-label])]
       [:div
        {:class (str "app-footer "
                     (if (= align :center)
                       "app-footer--center"
                       "app-footer--right"))}
        [:a {:href "#"
             :onClick (fn [event] (re-frame/dispatch [::set-datalink :terms-of-use]))}
         (str terms-of-use-label " | ")]
        [:a {:href "#"
             :onClick (fn [event] (re-frame/dispatch [::set-datalink :privacy]))}
         (str privacy-label " | ")]
        [:a {:href "#"
             :onClick (fn [event] (re-frame/dispatch [::set-datalink :impressum]))}
         impressum-label]]))
   (when (= "PRODUCTION" config/environment)
     (let [{:keys [accessibility-label]} @(re-frame/subscribe [::i18n/translate-multi
                                                               :accessibility-label])]
       [:div
        {:class (str "app-footer "
                     (if (= align :center)
                       "app-footer--center"
                       "app-footer--right"))}
        [:a {:href "#"
             :onClick (fn [event] (re-frame/dispatch [::set-datalink :accessibility]))}
         accessibility-label]]))])

(defn sheet []
  [:<>
   (when (= "TRIAL" config/environment)
     (when-let [datalink @(re-frame/subscribe [::datalink])]
       [:div
        (conj
         [:div {:class "explorama-overlay", :style {:display "flex"}, :id "overlay"}
          datalink]
         [button {:start-icon :close2
                  :extra-class "explorama-overlay-close"
                  :on-click (fn [event] (re-frame/dispatch [::reset-datalink]))}])]))
   (when (= "PRODUCTION" config/environment)
     (when-let [datalink @(re-frame/subscribe [::datalink])]
       [:div
        (conj
         [:div {:class "explorama-overlay", :style {:display "flex"}, :id "overlay"}
          datalink]
         [button {:start-icon :close2
                  :extra-class "explorama-overlay-close"
                  :on-click (fn [event] (re-frame/dispatch [::reset-datalink]))}])]))])
