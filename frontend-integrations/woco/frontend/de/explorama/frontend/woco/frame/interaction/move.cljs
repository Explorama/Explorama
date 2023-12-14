(ns de.explorama.frontend.woco.frame.interaction.move
  (:require [clojure.set :as set]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-id]]
            [re-frame.core :refer [dispatch reg-event-fx]]
            [reagent.core :as r]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.size-position :refer [frame-position-sub
                                                           set-frame-position]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.workspace.states :as wws]))

(defn- move-event [frame-id new-coords skip-frame-pos?] ;FIXME HACK TODO woco.frame shouldnt have a dependency to its api
  [:de.explorama.frontend.woco.frame.api/move new-coords frame-id skip-frame-pos?])

(defonce moving-data-store (r/atom {}))

(defn moving-data
  ([frame-id data]
   (if (and data (seq data))
     (swap! moving-data-store assoc frame-id data)
     (swap! moving-data-store dissoc frame-id)))
  ([frame-id]
   @(r/cursor moving-data-store [frame-id]))
  ([]
   @moving-data-store))

(defonce is-moving? (r/atom false))

(defn moving-state
  ([state reset-all?]
   (when reset-all?
     (reset! moving-data-store {}))
   (reset! is-moving? (boolean state)))
  ([state]
   (moving-state state false))
  ([]
   @is-moving?))

(def resizing? (r/atom false))

(defn is-resizing? []
  @resizing?)

(defn stop-resizing [& [force]]
  (when (or @resizing?
            force)
    (reset! resizing? false)))

(defn start-resizing []
  (when-not @resizing?
    (reset! resizing? true)))

(defn move-event-call [frame-id {:keys [diff-x diff-y coupled-with workspace-zoom]} e]
  (let [native-event (aget e "nativeEvent")
        {:keys [width height]} (bounding-rect-id config/workspace-parent-id)
        {nav-height :height} (bounding-rect-id config/navbar-id)
        offset-x (/ diff-x workspace-zoom)
        offset-y (/ diff-y workspace-zoom)]
    (moving-state false true)
    ;; Check if mouse released on workspace and not out of screen
    (when (and (< -1
                  (aget native-event "pageX")
                  width)
               (< nav-height
                  (aget native-event "pageY")
                  (+ nav-height height)))
      (doseq [frame-id (if (wws/multi-selection?)
                         (if coupled-with
                           (set/union (set coupled-with)
                                      @wws/multiselect-current-selection)
                           @wws/multiselect-current-selection)
                         (or coupled-with [frame-id]))]
        (let [[x y] @(frame-position-sub frame-id)
              new-coords [(+ x offset-x)
                          (+ y offset-y)]]
          (set-frame-position frame-id new-coords)
          (dispatch [::move-frame frame-id new-coords (boolean coupled-with)]))))))

(defn move-frame [{:keys [db]} [_ frame-id new-coords skip-frame-pos?]]
  (when (not-empty (get-in db (path/frame-desc frame-id)))
    (let [frame-published-by (get-in db (path/frame-published-by frame-id))]
      {:db (cond-> (path/dissoc-in db path/page-dropped?)
             frame-published-by
             (update-in (conj (path/position-handling-source frame-published-by) :moved-frames)
                        (fnil conj #{}) frame-id))
       :dispatch-n [(move-event frame-id new-coords skip-frame-pos?)
                    [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]]})))

(reg-event-fx ::move-frame #'move-frame)