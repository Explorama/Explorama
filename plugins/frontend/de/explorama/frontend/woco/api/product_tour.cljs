(ns de.explorama.frontend.woco.api.product-tour
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]
            [de.explorama.frontend.ui-base.components.misc.core :as comp-misc]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.woco.api.welcome :as welcome]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.product-tour :as product-tour]))

(def ^:private pref-key "start-tour")
(def ^:private pref-default true)

(defn component-active? [current-step component additional-info]
  (let [{active-comp :component
         active-info :additional-info
         minor-steps :additional-minor-steps
         :as current-step} current-step
        curr-comp (keyword (get component :vertical component))
        correct-infos? (or (nil? additional-info)
                           (= additional-info active-info)
                           (some true? (map #(= additional-info %) minor-steps)))
        correct-comp? (or (= component :*)
                          (= curr-comp active-comp))]
    (or (nil? current-step)
        (and correct-comp?
             correct-infos?))))

(defn current-step [db]
  (get-in db path/product-tour-current-step))

(re-frame/reg-sub
 ::current-step
 (fn [db _]
   (current-step db)))

(re-frame/reg-sub
 ::product-tour-active?
 (fn [db _]
   (boolean (current-step db))))

(defn component-active?-db [db component additional-info]
  (component-active? (current-step db)
                     component
                     additional-info))

(re-frame/reg-sub
 ::component-active?
 (fn [db [_ component additional-info]]
   (component-active?-db db component additional-info)))


(re-frame/reg-event-fx
 ::next-step
 (fn [{db :db} [_ component additional-info next-component]]
   (let [component (keyword component)
         {cur-comp :component
          cur-infos :additional-info} (current-step db)]
     (when (and (= cur-comp component)
                (or (nil? additional-info)
                    (= additional-info cur-infos)))
       (product-tour/next-step db next-component)))))

(re-frame/reg-event-fx
 ::previous-step
 (fn [{db :db} [_ original-prev-step?]]
   (product-tour/previous-step db original-prev-step?)))

(re-frame/reg-event-fx
 ::cancel-product-tour
 (fn [{db :db} _]
   (product-tour/cancel-tour db)))

(re-frame/reg-event-fx
 ::auto-start
 (fn [{db :db} _]
   (let [{:keys [inited?]} (get-in db path/product-tour-popup)
         start? (fi/call-api [:user-preferences :preference-db-get]
                             db
                             pref-key
                             pref-default)]
     (when (and (= start? true)
                (not inited?))
       {:dispatch [::open-popup true]}))))

(re-frame/reg-sub
 ::max-steps
 (fn [db _]
   (product-tour/num-of-max-steps db)))

(re-frame/reg-event-db
 ::open-popup
 (fn [db [_ open?]]
   (assoc-in db path/product-tour-popup {:inited? true :value open?})))

(re-frame/reg-event-fx
 ::ok-start-tour
 (fn [_ _]
   {:dispatch [::welcome/dismiss-page [[::product-tour/start-tour]]]}))

(re-frame/reg-sub
 ::popup-open?
 (fn [db _]
   (get-in db path/product-tour-popup {:inited? false :value false})))

(defn start-popup []
  (let [dont-show-again? (r/atom false)]
    (fn []
      (let [show? (:value @(re-frame/subscribe [::popup-open?]))
            title @(re-frame/subscribe [::i18n/translate :product-tour-popup-title])
            message @(re-frame/subscribe [::i18n/translate :product-tour-popup-msg])
            checkbox-label @(re-frame/subscribe [::i18n/translate :dont-show-again])]
        [dialog
         {:show? show?
          :title title
          :message [:p message]
          :hide-fn #(re-frame/dispatch [::open-popup false])
          :ok {:label (re-frame/subscribe [::i18n/translate :product-tour-popup-start])
               :on-click #(re-frame/dispatch [::ok-start-tour])}
          :cancel {:label (re-frame/subscribe [::i18n/translate :product-tour-popup-cancel])
                   :variant :secondary
                   :on-click #(when @dont-show-again?
                                (re-frame/dispatch (fi/call-api [:user-preferences :save-event-vec]
                                                                "start-tour" false)))}
          :checkbox {:label checkbox-label
                     :checked? dont-show-again?
                     :on-change (fn [val]
                                  (swap! dont-show-again? not))}}]))))

(defn product-tour-step [params]
  (let [current-step (re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/current-step])
        current-language (re-frame/subscribe [::i18n/current-language])
        steps-label (re-frame/subscribe [::i18n/translate :product-tour-steps-label])
        next-button-label (re-frame/subscribe [::i18n/translate :product-tour-next-button-label])
        back-button-label (re-frame/subscribe [::i18n/translate :product-tour-back-button-label])
        max-steps (re-frame/subscribe [:de.explorama.frontend.woco.api.product-tour/max-steps])]
    [comp-misc/product-tour-step (assoc params
                                        :current-step current-step
                                        :language current-language
                                        :steps-label steps-label
                                        :next-button-label next-button-label
                                        :back-button-label back-button-label
                                        :val-sub-fn (fn [keyword-vector]
                                                      @(re-frame/subscribe (if (vector? keyword-vector)
                                                                             keyword-vector
                                                                             [::i18n/translate keyword-vector])))
                                        :prev-fn (fn [_]
                                                   (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/previous-step]))
                                        :next-fn (fn [{:keys [component additional-info]}]
                                                   (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/next-step component additional-info]))
                                        :cancel-fn (fn [_]
                                                     (re-frame/dispatch [:de.explorama.frontend.woco.api.product-tour/cancel-product-tour]))
                                        :max-steps max-steps)]))

(re-frame/reg-event-fx
 ::init
 (fn [_ _]
   {:dispatch (fi/call-api [:user-preferences :add-preference-watcher-event-vec]
                           pref-key
                           [::auto-start]
                           pref-default
                           true)}))
