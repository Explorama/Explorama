(ns de.explorama.frontend.mosaic.interaction.context-menu.canvas
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.interaction.context-menu.shared :refer [desc-items item]]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.vis.config :as gconfig]
            [de.explorama.frontend.ui-base.components.misc.context-menu :refer [calc-menu-position context-menu]]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::canvas
 (fn [db [_ path]]
   (get-in db (gp/top-level-canvas-context-menu path))))

(re-frame/reg-event-db
 ::hide-canvas
 (fn [db [_ path]]
   (assoc-in db
             (gp/top-level-canvas-context-menu path)
             {})))

(re-frame/reg-event-db
 ::canvas
 [(fi/ui-interceptor)]
 (fn [db [_ path event [mouse-x mouse-y] context payload]]
   (let [mouse-offset-x 30
         mouse-offset-y 30
         top (- (+ mouse-y gconfig/header-height)
                mouse-offset-y)
         left (- mouse-x mouse-offset-x)]
     (assoc-in db
               (gp/top-level-canvas-context-menu path)
               {:payload payload
                :top top
                :left left
                :position (calc-menu-position event)
                :origin path
                :type context}))))

(defn copy-card-item [{:keys [origin]
                       {:keys [card]} :payload}]
  (item {:label :contextmenu-top-level-copy
         :icon :copy
         :click-fn #(re-frame/dispatch [:de.explorama.frontend.mosaic.operations.util/copy-card
                                        {:source origin
                                         :card-id card}])}))

(defn build-card-menu-items [path canvas-data contextmenu-descs]
  (cond-> [(copy-card-item canvas-data)]
    contextmenu-descs (desc-items path false contextmenu-descs)))

(defn copy-group-item [origin payload]
  (item {:label :contextmenu-top-level-copy
         :icon :copy
         :click-fn #(re-frame/dispatch [:de.explorama.frontend.mosaic.operations.util/copy-group-ui-wrapper origin payload])}))

(defn remove-group-item [origin payload]
  (item {:label :contextmenu-top-level-remove
         :icon :trash
         :click-fn #(do
                      (re-frame/dispatch [:de.explorama.frontend.mosaic.operations.tasks/execute-wrapper origin :remove payload]))}))

(defn build-group-menu-items [path {:keys [payload origin]}]
  (let [path (gp/canvas path)
        subgroup? (= gcp/sub-grp-by-key (:group-type payload))
        coupled? @(fi/call-api :coupled-with-sub (gp/frame-id path))]
    (cond-> [(copy-group-item origin payload)]
      (and (not coupled?)
           (not subgroup?))
      (conj (remove-group-item origin payload)))))

(defn context-menu-canvas [path]
  (let [{ctx :type
         :as canvas-menu-data
         :keys [position]}
        @(re-frame/subscribe [::canvas path])
        contextmenu-descs (reduce (fn [acc [id item]]
                                    (assoc acc
                                           id
                                           (dissoc item :visible? :disabled?)))
                                  {}
                                  @(fi/call-api :service-category-sub :operations))
        items (cond (= ctx :group)
                    (build-group-menu-items path canvas-menu-data)
                    (= ctx :card)
                    (build-card-menu-items path canvas-menu-data contextmenu-descs)
                    :else [])
        openable? (boolean (seq items))
        {[_ height] :size} @(fi/call-api :frame-sub (gp/frame-id path))
        read-only-sub? @(fi/call-api [:interaction-mode :read-only-sub?]
                                     {:frame-id (gp/frame-id path)})]
    (when read-only-sub?
      (re-frame/dispatch [::hide-canvas path]))
    (when (and (not read-only-sub?)
               (:left position)
               (:top position))
      [context-menu {:position position
                     :show? true
                     :menu-max-height height
                     :items items
                     :openable? openable?
                     :on-close #(re-frame/dispatch [::hide-canvas path])}])))
