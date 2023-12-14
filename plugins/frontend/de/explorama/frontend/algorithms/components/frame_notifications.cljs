(ns de.explorama.frontend.algorithms.components.frame-notifications
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.algorithms.path.core :as path]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.ui-base.components.frames.core :as frames :refer [notification]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]))

(re-frame/reg-event-fx
 ::not-supported-redo-ops
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id not-supported-ops valid-operations-state]]
   {:db (-> db
            (assoc-in (path/not-supported-redo-ops frame-id)
                      not-supported-ops)
            (assoc-in (path/redo-problem-type-before frame-id)
                      (:problem-type valid-operations-state)))
    :dispatch (fi/call-api :notify-event-vec {:message (if (= 1 (count not-supported-ops))
                                                         :global-redo-not-possible-single
                                                         :global-redo-not-possible-multi)
                                              :source-icon :head-cogs
                                              :category {:operations :redo-on-reconnect}
                                              :source-frame-id frame-id
                                              :type :warn})}))

(re-frame/reg-event-fx
 ::undo
 [(fi/ui-interceptor)]
 (fn [{db :db} [_ frame-id]]
   (when-let [undo-event (get-in db (path/undo-connection-update-event frame-id))]
     {:dispatch undo-event})))

(re-frame/reg-sub
 ::undo-available?
 (fn [db [_ frame-id]]
   (boolean (get-in db (path/undo-connection-update-event frame-id)))))

(re-frame/reg-sub
 ::problem-type-before
 (fn [db [_ frame-id]]
   (get-in db (path/redo-problem-type-before frame-id))))

(re-frame/reg-sub
 ::not-supported-redo-ops
 (fn [db [_ frame-id]]
   (get-in db (path/not-supported-redo-ops frame-id))))

(re-frame/reg-event-fx
 ::clear-notification
 (fn [{db :db} [_ frame-id]]
   {:db (update-in db
                   (path/frame frame-id)
                   dissoc
                   path/notifications-key)}))

(defn- build-message [frame-id not-supported-ops]
  (when (coll? not-supported-ops)
    (let [show-undo? @(re-frame/subscribe [::undo-available? frame-id])
          {:keys [redo-not-possible-single
                  redo-not-possible-multi
                  load-warning-screen-not-follow-recommendation
                  undo-button-label
                  dependent-variable
                  independent-variable
                  feature]}
          @(re-frame/subscribe [::i18n/translate-multi
                            :redo-not-possible-single
                            :redo-not-possible-multi
                            :undo-button-label
                            :dependent-variable
                            :independent-variable
                            :load-warning-screen-not-follow-recommendation
                            :feature])
          problem-type-before @(re-frame/subscribe [::problem-type-before frame-id])]

      (cond-> {:message  (str (if (= 1 (count not-supported-ops))
                                redo-not-possible-single
                                redo-not-possible-multi)
                              (->> (map #(cond (and (= problem-type-before :linear-model)
                                                    (= % :header)) dependent-variable
                                               (and (= problem-type-before :linear-model)
                                                    (= % :future-header)) independent-variable
                                               (and (= problem-type-before :k-means-model)
                                                    (= % :header)) feature)
                                        (set (mapv :op not-supported-ops)))
                                   (sort)
                                   (clj-str/join ", ")))}
        show-undo?
        (assoc :actions [{:label load-warning-screen-not-follow-recommendation
                          :on-click #(re-frame/dispatch [::clear-notification frame-id])}
                         {:label undo-button-label
                          :start-icon :back
                          :variant :secondary
                          :on-click #(do
                                       (re-frame/dispatch [::undo frame-id])
                                       (re-frame/dispatch [::clear-notification frame-id]))}])))))

(defn frame-notifications [frame-id]
  [error-boundary
   (let [not-supported-redo-ops @(re-frame/subscribe [::not-supported-redo-ops frame-id])
         {:keys [actions message]} (build-message frame-id not-supported-redo-ops)]
     [notification {:show? (boolean not-supported-redo-ops)
                    :extra-props {:style {:white-space :pre}}
                    :message message
                    :actions actions
                    :on-close #(re-frame/dispatch [::clear-notification frame-id])}])])