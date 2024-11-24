(ns de.explorama.frontend.woco.sidebar
  (:require ["re-resizable"]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]
            [de.explorama.frontend.ui-base.components.formular.core :refer [button]]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-id]]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.core :refer [gen-frame-id]]
            [de.explorama.frontend.woco.frame.events :as fevents]
            [de.explorama.frontend.woco.frame.interaction.move :refer [moving-state]]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.scale :as scale]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub
                                   subscribe]]
            [reagent.core :as r]))

;;TODO r1/ui-base Refactor to extra space/component - Maybe move to ui-base component
(def resizable-comp (r/adapt-react-class (aget js/ReResizable "Resizable")))

(reg-sub
 ::sidebar
 (fn [db]
   (get-in db path/sidebar)))

(reg-sub
 ::sidebar?
 (fn [db]
   (seq (get-in db path/sidebar))))

(reg-event-db
 ::set-sidebar-width
 (fn [db [_ offset]]
   (assoc-in db path/navigation-bar-offset offset)))

(defn sidebar-width [db]
  (if (get-in db path/sidebar)
    (get-in db path/navigation-bar-offset 0)
    0))

(reg-sub ::sidebar-width sidebar-width)

(reg-event-db
 ::hide-sidebar
 (fn [db [_ id]]
   (let [filtered-sidebar (filter #(not= (:id %) id)
                                  (get-in db path/sidebar))]
     (assoc-in db path/sidebar filtered-sidebar))))

(reg-event-fx
 ::create-sidebar
 (fn [{db :db}
      [_ {:keys [id module event position
                 close-event header-items-fn
                 title width vertical]}]]
   (let [old-sidebar (some-> (get-in db path/sidebar)
                             first)
         close? (= (:id old-sidebar) id)
         change-sidebar? (not= (:id old-sidebar) id)]
     (cond-> {:db (assoc-in db
                            path/sidebar
                            (when-not close? [{:id id
                                               :frame-id (when vertical (gen-frame-id db vertical))
                                               :event event
                                               :position position
                                               :close-event close-event
                                               :title title
                                               :width width
                                               :module (registry/lookup-target db :modules module)
                                               :header-items-fn header-items-fn}]))}
       (and change-sidebar? (:close-event old-sidebar)) (assoc :dispatch (:close-event old-sidebar))))))

(def sidebar-props
  {:on-drag-over #(.preventDefault %)
   :on-drag-enter #(.preventDefault %)
   :on-drag-leave #(.preventDefault %)
   :on-mouse-down (fn [e]
                    (.stopPropagation e)
                    (navigation-control/stop-panning))
   :on-mouse-up (fn [e]
                  (moving-state false)
                  (.preventDefault e)
                  (.stopPropagation e))
   :on-drop
   (fn [callback-fn e]
     (.preventDefault e)
     (.stopPropagation e))})

(defn- sidebar-element [{:keys [event frame-id id]} _]
  (let [size (r/atom nil)
        content-id (str "woco_sidebar_node" id)]
    (r/create-class
     {:display-name "woco_sidebar"
      :component-did-mount #(do
                              (when event (dispatch [event fevents/init {:frame-id frame-id}]))
                              (when-let [rect (bounding-rect-id content-id)]
                                (reset! size {:sidebar-width (:width rect)
                                              :sidebar-height (:height rect)})))
      :reagent-render
      (fn [{:keys [frame-id id module title close-event header-items-fn] :as d} curr-width]
        (let [title (if (vector? title)
                      @(subscribe title)
                      title)
              items (when (fn? header-items-fn)
                      (header-items-fn))]
          (dispatch [::set-sidebar-width curr-width])
          [:<>
           [:div.header
            [:h2 title]
            [:div.actions
             (when items
               (map-indexed (fn [idx itm]
                              ^{:key (str "sidebar-header-icon" idx)}
                              [button itm])
                            items))
             [button {:title (subscribe [::i18n/translate :close-sidebar])
                      :on-click (fn []
                                  (dispatch [::hide-sidebar id])
                                  (dispatch close-event))
                      :variant :tertiary
                      :size :small
                      :start-icon :close}]]]
           [module (assoc sidebar-props
                          :frame-id frame-id)]]))})))

(defn- content [old-width]
  (let [open-sidebars @(subscribe [::sidebar?])
        {:keys [inner-width]}  @(subscribe [::scale/scale-info])
        curr-width (or @old-width config/sidebar-width
                       (some :width open-sidebars))
        max-width (- inner-width config/min-content-width)]
    (when (< max-width @old-width)
      (dispatch [::set-sidebar-width max-width]))
    [resizable-comp {:default-size #js{:width curr-width}
                     :enable {:left true}
                     :min-width 400
                     :max-width max-width
                     :id "explorama-sidebar"
                     :class-name "sidebar show"
                     :style  (cond-> {:position :absolute
                                      :z-index 2}) ;;TODO r1/css remove after css fix
                     :on-resize-start (fn [ev _ _]
                                        (.stopPropagation ev))
                     :on-resize-stop (fn [ev direction ref delta]
                                       (when-let [new-width  (aget ref "clientWidth")]
                                         (dispatch [::set-sidebar-width new-width])
                                         (reset! old-width new-width)))}
     (reduce
      (fn [acc {:keys [id module] :as desc}]
        (cond-> acc
          (fn? module) (conj ^{:key id}
                        [sidebar-element desc curr-width])))
      [:<>]
      open-sidebars)]))

(defn sidebar []
  (let [old-width (r/atom nil)]
    (fn []
      [error-boundary
  ;;separation ensures that initial size caluclation works correctly
       (let [sidebar? @(subscribe [::sidebar?])]
         (if sidebar?
           [content old-width]
           (dispatch [::set-sidebar-width 0])))])))

