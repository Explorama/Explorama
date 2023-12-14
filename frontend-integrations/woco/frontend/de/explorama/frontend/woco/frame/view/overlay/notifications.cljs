(ns de.explorama.frontend.woco.frame.view.overlay.notifications
  (:require [clojure.string :as clj-str]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.frame.plugin-api :as papi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [error warn]]
            [de.explorama.frontend.ui-base.components.frames.core :as frames :refer [notification]]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]))

(re-frame/reg-event-fx
 ::not-supported-redo-ops
 (fn [{db :db} [_ frame-id not-supported-ops]]
   {:db (assoc-in db
                  (path/frame-not-supported-redo-ops frame-id)
                  not-supported-ops)
    :dispatch [:de.explorama.frontend.woco.api.notifications/notify {:message (if (= 1 (count not-supported-ops))
                                                                       :global-redo-not-possible-single
                                                                       :global-redo-not-possible-multi)
                                                            :category {:operations :redo-on-reconnect}
                                                            :source-frame-id frame-id
                                                            :type :warn}]}))

(re-frame/reg-event-fx
 ::undo
 (fn [{db :db} [_ undo-path-fn frame-id]]
   (when-let [undo-event (get-in db (undo-path-fn frame-id))]
     {:dispatch undo-event})))

(re-frame/reg-sub
 ::undo-available?
 (fn [db [_ undo-path-fn frame-id]]
   (boolean (get-in db (undo-path-fn frame-id)))))

(re-frame/reg-sub
 ::not-supported-redo-ops
 (fn [db [_ frame-id]]
   (get-in db (path/frame-not-supported-redo-ops frame-id))))

(re-frame/reg-event-db
 ::clear-notification
 (fn [db [_ frame-id]]
   (update-in db (path/frame-desc frame-id)
              dissoc :not-supported-redo-ops)))

(defn- build-message [frame-id {:keys [show-undo? not-supported-ops clear-notification-fn undo-fn]}]
  (when (set? not-supported-ops)
    (let [show-undo? @(re-frame/subscribe [::undo-available? frame-id])
          {:keys [redo-not-possible-single
                  redo-not-possible-multi
                  undo-button-label
                  contextmenu-top-level-groupby
                  contextmenu-top-level-sortby
                  contextmenu-top-level-groupby-1
                  contextmenu-top-level-sort-group
                  contextmenu-top-level-sub-sort-group
                  load-warning-screen-not-follow-recommendation
                  scatter-plot-settings-title]}
          @(re-frame/subscribe [::i18n/translate-multi
                                :redo-not-possible-single
                                :redo-not-possible-multi
                                :undo-button-label
                                :contextmenu-top-level-groupby
                                :contextmenu-top-level-sortby
                                :contextmenu-top-level-groupby-1
                                :contextmenu-top-level-sort-group
                                :contextmenu-top-level-sub-sort-group
                                :load-warning-screen-not-follow-recommendation
                                :scatter-plot-settings-title])]
      (cond-> {:message (str (if (= 1 (count not-supported-ops))
                               redo-not-possible-single
                               redo-not-possible-multi)
                             (->> (map #(cond (= % :group-by) contextmenu-top-level-groupby
                                              (= % :sort-by) contextmenu-top-level-sortby
                                              (= % :sub-group-by) contextmenu-top-level-groupby-1
                                              (= % :sort-group) contextmenu-top-level-sort-group
                                              (= % :sub-sort-group) contextmenu-top-level-sub-sort-group
                                              (= % :scatter-axes) scatter-plot-settings-title
                                              :else (error "Not supported operation" %))
                                       (set (mapv (fn [{:keys [op]}] (cond-> op
                                                                       (vector? op)
                                                                       (first)))
                                                  not-supported-ops)))
                                  (set)
                                  (sort)
                                  (clj-str/join ", ")))}

        show-undo?
        (assoc :actions [{:label load-warning-screen-not-follow-recommendation
                          :on-click clear-notification-fn}
                         {:label undo-button-label
                          :start-icon :back
                          :variant :secondary
                          :on-click undo-fn}])))))

(defn frame-notifications [frame-id]
  (let [{:keys [build-message undo-path-fn show?]
         :as papi-notifi
         :or {build-message (fn [& _]
                              (warn "no build-message fn for frame registered." frame-id))
              undo-path-fn (fn [& _]
                             (warn "no undo-path-fn for frame registered." frame-id)
                             (path/frame-undo-connection-update-event frame-id))}}
        @(re-frame/subscribe [::papi/notifications frame-id])
        show? (val-or-deref show?)]
    (when show?
      [error-boundary
       (let [not-supported-redo-ops @(re-frame/subscribe [::not-supported-redo-ops frame-id])
             show-undo? @(re-frame/subscribe [::undo-available? undo-path-fn frame-id])
             {:keys [actions message]} (build-message frame-id
                                                      {:not-supported-redo-ops not-supported-redo-ops
                                                       :show-undo? show-undo?
                                                       :clear-notification-fn #(re-frame/dispatch [::clear-notification frame-id])
                                                       :undo-fn #(do
                                                                   (re-frame/dispatch [::undo undo-path-fn frame-id])
                                                                   (re-frame/dispatch [::clear-notification frame-id]))})]
         [notification (cond-> {:show? (boolean (and not-supported-redo-ops
                                                     message))
                                :extra-props {:style {:white-space :pre
                                                      :z-index 50}}
                                :on-close #(re-frame/dispatch [::clear-notification frame-id])}
                         message (assoc :message message)
                         actions (assoc :actions actions))])])))
