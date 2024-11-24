(ns de.explorama.frontend.woco.workspace.multiselect
  (:require [de.explorama.frontend.ui-base.components.misc.core :refer [toolbar]]
            [de.explorama.frontend.ui-base.utils.css-classes :refer [toolbar-ignore-class]]
            [re-frame.core :as re-frame]
            [react-dom :as react-dom]
            [reagent.core :as r]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.navigation.control :as nav-control]
            [de.explorama.frontend.woco.navigation.core :as nav-core]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.workspace.math :refer [find-bb frame-bounding-box
                                                      simple-hit-test]]
            [de.explorama.frontend.woco.workspace.rearrange :as wwr]
            [de.explorama.frontend.woco.workspace.states :as wws]))

(defn- translate-css [x y]
  (str "translate(" x "px, " y "px)"))

(defn- update-selected-state-frames [db sx sy ex ey]
  (update-in db path/frames (fn [frames]
                              (->> (map (fn [[fid frame]]
                                          [fid (assoc frame :selected? false)])
                                        frames)
                                   (into {})
                                   (reduce (fn [acc [fid frame]]
                                             (cond (and (simple-hit-test sx sy ex ey
                                                                         (:coords frame)
                                                                         (:full-size frame))
                                                        (empty? (:with (path/couple-infos-key frame))))
                                                   (assoc acc fid (assoc frame :selected? true))
                                                   (and (simple-hit-test sx sy ex ey
                                                                         (:coords frame)
                                                                         (:full-size frame))
                                                        (seq (:with (path/couple-infos-key frame))))
                                                   (reduce (fn [acc cfid]
                                                             (assoc acc cfid (assoc (get acc cfid)
                                                                                    :selected? true)))
                                                           acc
                                                           (:with (path/couple-infos-key frame)))
                                                   :else
                                                   (assoc acc fid frame)))
                                           frames)))))

(defn- reset-selected-state-frames [db]
  (update-in db path/frames (fn [frames]
                              (into {}
                                    (map (fn [[fid frame]]
                                           [fid (assoc frame :selected? false)])
                                         frames)))))

(defn- find-selected-frames [frames]
  (filter (fn [[_ {selected? :selected?}]]
            selected?)
          frames))

(defn- coupled-frames [frames]
  (into {} (map (fn [[fid {{pair :with} path/couple-infos-key}]]
                  [fid (if (seq pair) (set pair) nil)])
                frames)))

(defn- reset-temporary-selections! []
  (reset! wws/temporary-frames {})
  (reset! wws/temporary-selection #{}))

(defn- reset-current-selections! []
  (reset! wws/multiselect-current-selection #{})
  (reset! wws/multiselect-bb nil))

(defn- set-current-selections! [current-selection current-bb]
  (reset! wws/multiselect-current-selection current-selection)
  (reset! wws/multiselect-bb current-bb))

(re-frame/reg-event-db
 ::selection-finish
 (fn [db [_ {:keys [start-x start-y end-x end-y] :as local-selection-bb?}]]
   (if local-selection-bb?
     (let [[[start-x end-x] [start-y end-y]]
           (find-bb start-x start-y end-x end-y)
           [sx sy] (nav-control/page->workspace db [start-x start-y])
           [ex ey] (nav-control/page->workspace db [end-x end-y])
           new-db (update-selected-state-frames db sx sy ex ey)
           selected-frames (find-selected-frames (get-in new-db path/frames))]
       (reset-temporary-selections!)
       (if (empty? selected-frames)
         (reset-current-selections!)
         (set-current-selections! (set (map first selected-frames))
                                  (frame-bounding-box selected-frames)))
       new-db)
     (do
       (reset-temporary-selections!)
       (reset-current-selections!)
       (reset-selected-state-frames db)))))

(re-frame/reg-event-db
 ::selection-strg
 (fn [db [_ frame-id]]
   (let [new-db (cond (and (get-in db (conj (path/frame-desc frame-id) :selected?))
                           (seq (get-in db (conj (path/frame-desc frame-id) path/couple-infos-key :with))))
                      (reduce (fn [db frame-id]
                                (assoc-in db (conj (path/frame-desc frame-id) :selected?) false))
                              db
                              (get-in db (conj (path/frame-desc frame-id) path/couple-infos-key :with)))
                      (and (not (get-in db (conj (path/frame-desc frame-id) :selected?)))
                           (seq (get-in db (conj (path/frame-desc frame-id) path/couple-infos-key :with))))
                      (reduce (fn [db frame-id]
                                (assoc-in db (conj (path/frame-desc frame-id) :selected?) true))
                              db
                              (get-in db (conj (path/frame-desc frame-id) path/couple-infos-key :with)))
                      (get-in db (conj (path/frame-desc frame-id) :selected?))
                      (assoc-in db (conj (path/frame-desc frame-id) :selected?) false)
                      :else
                      (assoc-in db (conj (path/frame-desc frame-id) :selected?) true))
         selected-frames (find-selected-frames (get-in new-db path/frames))]
     (if (empty? selected-frames)
       (reset-current-selections!)
       (set-current-selections! (set (map first selected-frames))
                                (frame-bounding-box selected-frames)))
     new-db)))

(re-frame/reg-event-fx
 ::start-temporary-selection
 (fn [{db :db} _]
   (let [{wsp-zoom :z wsp-x :x wsp-y :y} (nav-control/position db)]
     (reset! wws/temporary-frames {:frames (mapv (fn [[fid {coords :coords full-size :full-size}]]
                                                   [fid [coords full-size]])
                                                 (get-in db path/frames))
                                   :coupled (coupled-frames (get-in db path/frames))
                                   :wsp [wsp-x wsp-y wsp-zoom]}))
   {}))

(defn temporary-hit-test [{:keys [start-x start-y end-x end-y]}]
  (let [[[start-x end-x] [start-y end-y]] (find-bb start-x start-y end-x end-y)
        {frames :frames [x y z] :wsp
         coupled :coupled}
        @wws/temporary-frames
        [sx sy] (nav-control/page->workspace x y z [start-x start-y])
        [ex ey] (nav-control/page->workspace x y z [end-x end-y])]
    (reset! wws/temporary-selection
            (->> (filter (fn [[_ [coords size]]]
                           (simple-hit-test sx sy ex ey coords size))
                         frames)
                 (mapcat (fn [[fid]]
                           (if-let [couple (coupled fid)]
                             couple
                             [fid])))
                 set))))

(re-frame/reg-event-fx
 ::close-selected
 (fn [_ _]
   (let [fx (mapv (fn [fid]
                    [:dispatch [:de.explorama.frontend.woco.frame.api/close fid]])
                  @wws/multiselect-current-selection)]
     (reset! wws/multiselect-current-selection #{})
     (reset! wws/multiselect-bb nil)
     {:fx fx})))

(defn- portal-comp [_props & _childs]
  (r/create-class
   {:display-name "multiselect toolbar portal"
    :reagent-render
    (fn [{:keys [style]} & childs]
      (apply conj
             [:div {:class toolbar-ignore-class
                    :style (assoc style :position :absolute)}]
             childs))}))

(defn- portal [{:keys [portal-target] :as props} childs]
  (let [portal-target-elem (if-let [ele (.getElementById js/document portal-target)]
                             ele
                             js/document.body)]
    (react-dom/createPortal
     (r/as-element
      [portal-comp props childs])
     portal-target-elem)))

(defn selection-toolbar []
  (when @wws/multiselect-bb
    (let [activate-transition? @(re-frame/subscribe [::nav-control/animation-activated?])
          {:keys [start-min-x start-max-x
                  start-min-y
                  min-x min-y max-x]}
          @wws/multiselect-bb
          min-x (or min-x start-min-x)
          min-y (or min-y start-min-y)
          max-x (or max-x start-max-x)
          width (max 0 (- max-x min-x))
          margin-top 35 ;;space where the toolbar should be over the rect
          x (+ min-x (/ width 2))
          y (- min-y margin-top)
          [x y] @(re-frame/subscribe [::nav-control/workspace->page [x y] false])
          ;; respect height of toolbar here, to ensure that toolbar is at every zoomlevel on the same place
          y (max 0 (- y 52))]
      [portal (cond-> {:id (str config/frames-transform-id "selection")
                       :portal-target config/workspace-parent-id
                       :style {:transform-origin "0px 0px 0px"
                               :z-index 300000}
                       :on-transition-end #(re-frame/dispatch [::nav-control/deactivate-animation])}
                (and x y)
                (nav-core/transform-str x y 1)
                activate-transition?
                (nav-core/transition-str))
       (let [{:keys [multiselect-arrange-horizontal multiselect-arrange-vertical
                     multiselect-arrange-dynamic multiselect-close]}
             @(re-frame/subscribe [::i18n/translate-multi
                                   :multiselect-arrange-horizontal
                                   :multiselect-arrange-vertical
                                   :multiselect-arrange-dynamic
                                   :multiselect-close])]
         [toolbar
          {:orientation :horizontal,
           :tooltip-direction :up,
           :separator :symbol,
           :extra-class [toolbar-ignore-class "window-toolbar-center-x" "animation-fade-in" "short-animation"]
           :items [[{:title multiselect-arrange-horizontal
                     :id "selection-rearrange-hort",
                     :icon :arrangeh
                     :on-click (fn [e]
                                 (.stopPropagation e)
                                 (.preventDefault e)
                                 (re-frame/dispatch [::wwr/position-handling-only-right]))}
                    {:title multiselect-arrange-vertical
                     :id "selection-rearrange-vert",
                     :icon :arrangev
                     :on-click (fn [e]
                                 (.stopPropagation e)
                                 (.preventDefault e)
                                 (re-frame/dispatch [::wwr/position-handling-only-down]))}
                    {:title multiselect-arrange-dynamic
                     :id "selection-rearrange-dyn",
                     :icon :reorder
                     :on-click (fn [e]
                                 (.stopPropagation e)
                                 (.preventDefault e)
                                 (re-frame/dispatch [::wwr/position-handling-rearrange-selected]))}]
                   [{:title multiselect-close
                     :id "selection-close",
                     :icon :close
                     :on-click (fn [e]
                                 (.stopPropagation e)
                                 (.preventDefault e)
                                 (re-frame/dispatch [::close-selected]))}]]}])])))

(defn selection-bb []
  (when @wws/multiselect-bb
    (let [{:keys [start-min-x start-max-x
                  start-min-y start-max-y
                  min-x min-y max-x max-y]}
          @wws/multiselect-bb
          min-x (or min-x start-min-x)
          min-y (or min-y start-min-y)
          max-x (or max-x start-max-x)
          max-y (or max-y start-max-y)]
      [:div {:id "selection-bounding-box"
             :style {:transform (translate-css (- min-x 25)
                                               (- min-y 25))
                     :width (+ (- max-x min-x) 50)
                     :height (+ (- max-y min-y) 50)
                     :position :absolute}}])))

(defn box [local-multiselect-bb]
  (when (and @local-multiselect-bb
             (:active? @local-multiselect-bb))
    (let [{:keys [start-x start-y end-x end-y]} @local-multiselect-bb
          [[start-x end-x] [start-y end-y]] (find-bb start-x start-y end-x end-y)]
      [:div {:id "local-selection-bounding-box"
             :style {:transform (translate-css start-x
                                               (- start-y config/explorama-header-height))
                     :width (- end-x start-x)
                     :height (- end-y start-y)}}])))

