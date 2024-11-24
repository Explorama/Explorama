(ns de.explorama.frontend.woco.components.context-menu
  (:require [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position
                                                                       context-menu]]
            [de.explorama.frontend.woco.workspace.background :as background]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.path :as path]
            [re-frame.core :refer [dispatch subscribe reg-event-db reg-event-fx reg-sub]]))

(def ^:private follow-action (atom nil))

(reg-sub
 ::infos
 (fn [db]
   (get-in db path/context-menu)))

(defn close-context-menu-db [db]
  (path/dissoc-in db path/context-menu))

(reg-event-db
 ::close close-context-menu-db)

(reg-event-fx
 ::close-trigger
 (fn [{db :db} [_ trigger-source event ctx-infos]]
   (let [action (or @follow-action :commit-move)]
     (reset! follow-action nil)
     {:db (path/dissoc-in db path/context-menu)
      :fx [(when event [:dispatch (conj event action ctx-infos)])
           (when (= action :commit-move)
             [:dispatch [:de.explorama.frontend.woco.frame.api/bring-to-front
                         (:source-frame-id ctx-infos)]])]})))

(reg-event-db
 ::open
 (fn [db [_ drop-event dialog-infos ctx-infos]]
   (let [{:keys [top left]} (calc-menu-position drop-event)
         {:keys [fix-top fix-left]} dialog-infos]
     (background/draw-temp-edges)
     (assoc-in db path/context-menu {:dialog-infos (assoc dialog-infos
                                                          :show? true)
                                     :ctx-infos ctx-infos
                                     :position {:x (or fix-left left)
                                                :y (or fix-top top)}}))))

(defn on-click-fn [event type ctx-infos params]
  (fn [e]
    (.stopPropagation e)
    (reset! follow-action (if (#{:override :couple} type)
                            :reset-move
                            :commit-move))
    (dispatch [::close])
    (dispatch (conj event type ctx-infos params))))

(defn view []
  (let [{{x :x y :y} :position
         {items :options
          ui-base-items :items
          event :event
          show :show?
          trigger-source :trigger-source}
         :dialog-infos
         ctx-infos :ctx-infos
         :as infos}
        @(subscribe [::infos])]
    [context-menu
     {:show? (boolean show)
      :menu-z-index 200002
      :position {:left (or x 0)
                 :top (or y 0)}
      :on-close #(dispatch [::close-trigger trigger-source event ctx-infos])
      :items
      (or ui-base-items
          (vec
           (for [{:keys [label icon type children params]} items]
             (cond-> {:label label}
               icon
               (assoc :icon icon)
               children
               (assoc  :sub-items (vec
                                   (for [{:keys [label params]} children]
                                     {:label label
                                      :on-click (on-click-fn event type ctx-infos params)})))
               (not children)
               (assoc :on-click (on-click-fn event type ctx-infos params))))))}]))
