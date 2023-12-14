(ns de.explorama.frontend.indicator.data-instances
  (:require [clojure.set :as set]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.indicator.path :as path]
            [de.explorama.shared.indicator.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.indicator.event-logging :as event-log]))

(re-frame/reg-event-fx
 ws-api/publish-di-success
 (fn [_ [_ callback-event di project? indicator-desc]]
   {:fx [[:dispatch [:de.explorama.frontend.indicator.views.core/set-loading false]]
         (when-not project?
           [:dispatch [::event-log/log-event
                       "restore-indicator-desc"
                       {:indicator indicator-desc}]])
         [:dispatch (conj callback-event di)]]}))

(re-frame/reg-event-fx
 ::provide-content
 (fn [{db :db} [_ frame-id operation-type source-or-target params event]]
   (debug ::provide-content frame-id operation-type source-or-target params event)
   ;; Is not a valid-frame-id here when copied from non-frame (e.g. subgroup)
   ;; If it's a subgroup drag-infos/path/drag-and-drop? is set
   (let [{:keys [drag-and-drop?]
          {indicator-id :indicator-id
           project? :project?} :drag-infos} frame-id
         indicator-desc (when project?
                          (get-in db (path/project-indicator-desc indicator-id)))]
     (cond
       (and (#{:difference
               :intersection-by
               :union
               :sym-difference} operation-type)
            (= :source source-or-target)
            drag-and-drop?)
       {:backend-tube [ws-api/create-and-publish-di
                       {:client-callback [ws-api/publish-di-success event]
                        :failed-callback (conj event {:cancel? true})}
                       (or indicator-desc indicator-id)
                       project?]}
       (and (#{:override} operation-type)
            (= :source source-or-target)
            drag-and-drop?)
       {:backend-tube [ws-api/create-and-publish-di
                       {:client-callback [ws-api/publish-di-success event]
                        :failed-callback (conj event {:cancel? true})}
                       (or indicator-desc indicator-id)
                       project?]}

       :else
       {:dispatch (conj event {:cancel? true})}))))

(re-frame/reg-event-fx
 ::event
 (fn [{db :db}
      [_ action params]]
   (case action
     :frame/connection-negotiation
     (let [{:keys [type frame-id result]} params
           options
           (cond
             (= type :target)
             {:type :cancel
              :frame-id frame-id
              :event [::provide-content frame-id]}
             :else
             {:type :options
              :frame-id frame-id
              :event [::provide-content frame-id]
              :options [#_{:label (i18n/translate db :contextmenu-operations-intersect)
                           :icon :intersect
                           :type :intersection-by
                           :params {:by "id"}}
                        #_{:label (i18n/translate db :contextmenu-operations-union)
                           :icon :union
                           :type :union}
                        #_{:label (i18n/translate db :contextmenu-operations-difference)
                           :icon :difference
                           :type :difference}
                        #_{:label (i18n/translate db :contextmenu-operations-symdifference)
                           :icon :symdiff
                           :type :sym-difference}
                        {:label (i18n/translate db :contextmenu-operations-override)
                         :icon :replace
                         :type :override}]})]
       {:dispatch (conj result options)})
     {})))
