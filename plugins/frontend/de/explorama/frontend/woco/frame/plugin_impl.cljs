(ns de.explorama.frontend.woco.frame.plugin-impl
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.filter.core :as filter-core]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.path :as path]))

(re-frame/reg-sub
 ::show-warn-screen
 (fn [db [_ frame-id]]
   #_(let [filter-show-warn-path (get-in db (path/vertical-plugin-api frame-id :filter   :show-warn-path))
           filter-show-warn (get-in db (filter-show-warn-path frame-id))]
       (cond (and filter-show-warn
                  (not warn-screen-done?)
                  (= :filter-show action-triggered))
             :filter-show-warn
             :else nil))))

(re-frame/reg-event-fx
 ::warning-proceed
 (fn [{db :db} [_ show? frame-id]]
   (case show?
     :filter-show-warn
     {:dispatch [::filter-core/show frame-id]
      :db (-> (assoc-in db (path/frame-warn-screen-done? frame-id) false)
              (assoc-in (path/frame-last-action-triggered frame-id) nil))}
     {:db (assoc-in db (path/frame-warn-screen-done? frame-id) false)})))

(re-frame/reg-event-fx
 ::warning-stop
 (fn [{db :db} [_ frame-id]]
   {:db (-> (assoc-in db (path/frame-warn-screen-done? frame-id) false)
            (assoc-in (path/frame-last-action-triggered frame-id) nil))}))

(def warn-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::show-warn-screen frame-id]))
   :title-sub
   (fn [show? _]
     (case show?
       :filter-show-warn
       (re-frame/subscribe [::i18n/translate :load-warning-screen-title])))
   :message-1-sub
   (fn [show? _]
     (case show?
       :filter-show-warn
       (re-frame/subscribe [::i18n/translate :load-warning-screen-message-part-1])))
   :message-2-sub
   (fn [show? _]
     (case show?
       :filter-show-warn
       (re-frame/subscribe [::i18n/translate :load-warning-screen-message-part-2])))
   :recommendation-sub
   (fn [show? _]
     (case show?
       :filter-show-warn
       (re-frame/subscribe [::i18n/translate :load-warning-screen-recommendation])))
   :stop-sub
   (fn [show? _]
     (case show?
       :filter-show-warn
       (re-frame/subscribe [::i18n/translate :load-warning-screen-follow-recommendation])))
   :proceed-sub
   (fn [show? _]
     (case show?
       :filter-show-warn
       (re-frame/subscribe [::i18n/translate :load-warning-screen-not-follow-recommendation])))
   :stop-fn
   (fn [show? frame-id _]
     (re-frame/dispatch [::warning-proceed show? frame-id]))
   :proceed-fn
   (fn [show? frame-id _]
     (re-frame/dispatch [::warning-stop show? frame-id]))})

(re-frame/reg-sub
 ::warn-screen
 (fn [_ _]
   warn-screen-impl))

(re-frame/reg-sub
 ::show-stop-screen
 (fn [db [_ frame-id]]
   false))

(def stop-screen-impl
  {:show?
   (fn [frame-id]
     (re-frame/subscribe [::show-stop-screen frame-id]))
   :title-sub
   (fn [show? _]
     nil)
   :message-1-sub
   (fn [show? _]
     nil)
   :message-2-sub
   (fn [show? _]
     nil)
   :stop-sub
   (fn [show? _]
     nil)
   :ok-fn
   (fn [show? frame-id]
     nil)})

(re-frame/reg-sub
 ::stop-screen
 (fn [_ _]
   stop-screen-impl))