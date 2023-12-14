(ns de.explorama.frontend.mosaic.interaction.dnd-util
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.mosaic.path :as gp]))

;? REMOVE FILE

(defn drag-start
  [event left top path parent-left parent-top]
  (let [frame-id (cond
                   (= :filter-view (gp/frame-id path)) (gp/top-level path)
                   :else (gp/frame-id path))]
    (re-frame/dispatch (fi/call-api :frame-bring-to-front-event-vec frame-id))
    (.stopPropagation event)
    (re-frame/dispatch [::start-drag {:x (.-clientX event)
                                  :y (.-clientY event)
                                  :left left
                                  :top top
                                  :path path
                                  :parent-left parent-left
                                  :parent-top parent-top}])))


(re-frame/reg-event-db
 ::start-drag
 [(fi/ui-interceptor)]
 (fn [db [_ data]]
   (assoc-in db gp/drag-start-path data)))

(defonce drop-target (atom nil))

(defn drag-end
  [event scale container-path drag-end-action]
  (let [[path e] @drop-target]
    (.stopPropagation event)
    (re-frame/dispatch (conj drag-end-action
                         {:source container-path
                          :target path
                          :target-event e}
                         scale
                         (.-nativeEvent event)))
    (reset! drop-target nil)))

(defn draggable-container
  [{:keys [path left top action scale key ignore-key parent-top parent-left on-start on-end]
    :or {scale 1
         parent-top 0
         parent-left 0}}
   style]
  (let [pressed @(fi/call-api :key-pressed-sub)
        execute? (and (every? #(nil? (get pressed %)) ignore-key)
                      (every? #(get pressed %) key))]
    (merge style
           {:draggable true
            :on-drag-start (fn [e]
                             (when execute?
                               (when on-start
                                 (on-start path))
                               (drag-start e left top path parent-left parent-top)
                               (.setData (.-dataTransfer e)
                                         "text/plain"
                                         {:should-ignore true})))
            :on-drag-end (fn [e]
                           (when execute?
                             (when on-end
                               (on-end path))
                             (drag-end e scale path action)))
            :on-drag-over #(do
                             (.preventDefault %))
            :on-drag-leave #(do
                              (.preventDefault %))
            :on-drag-enter #(do
                              (.preventDefault %))})))

(defn prevent-drag-group []
  {:draggable true
   :on-drag-start #(do
                     (.preventDefault %)
                     (.stopPropagation %))
   :on-drag-end #(do
                   (.preventDefault %)
                   (.stopPropagation %))
   :on-drag-over #(do
                    (.preventDefault %)
                    (.stopPropagation %))
   :on-drag-leave #(do
                     (.preventDefault %)
                     (.stopPropagation %))
   :on-drag-enter #(do
                     (.preventDefault %)
                     (.stopPropagation %))})
