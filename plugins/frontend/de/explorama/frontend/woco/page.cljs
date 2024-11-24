(ns de.explorama.frontend.woco.page
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.common.core :refer [tooltip]]
            [de.explorama.frontend.ui-base.components.frames.core :refer [loading-screen]]
            [de.explorama.frontend.ui-base.utils.view :refer [is-inside?]]
            [de.explorama.frontend.woco.api.notifications :as notifications]
            [de.explorama.frontend.woco.api.overlay :as overlays]
            [de.explorama.frontend.woco.api.product-tour :as product-tour]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.api.statusbar :as statusbar]
            [de.explorama.frontend.woco.components.context-menu :as wcm]
            [de.explorama.frontend.woco.components.dialog :as comp-dialog]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.copyright :as cr]
            [de.explorama.frontend.woco.frame.api :as frame-api]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.interaction.collision :refer [filter-visible-frames]]
            [de.explorama.frontend.woco.frame.interaction.dnd :refer [deactivate-dropzone dragging-overlay reset-vertical-drag]]
            [de.explorama.frontend.woco.frame.view.core :as frame-view]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]
            [de.explorama.frontend.woco.navigation.core :as navigation-core]
            [de.explorama.frontend.woco.navigation.panning-handler :as panning-handler]
            [de.explorama.frontend.woco.navigation.util :refer [workspace-rect]]
            [de.explorama.frontend.woco.navigation.zoom-handler :as zoom-handler]
            [de.explorama.frontend.woco.tabs :as tabs]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.presentation.core :as presentation]
            [de.explorama.frontend.woco.presentation.view :as presentation-view]
            [de.explorama.frontend.woco.sidebar :as sidebar]
            [de.explorama.frontend.woco.tools :as tools]
            [de.explorama.frontend.woco.welcome :as welcome]
            [de.explorama.frontend.woco.workspace.background :as background]
            [de.explorama.frontend.woco.workspace.config :as wwconfig]
            [de.explorama.frontend.woco.workspace.hints :as hints]
            [de.explorama.frontend.woco.workspace.multiselect :as wwms]
            [de.explorama.frontend.woco.workspace.states :as wws]
            [de.explorama.frontend.woco.workspace.window-creation :as wwc]
            [dommy.core :refer-macros [sel1] :as dommy]
            [clojure.string :as st]
            [re-frame.core :as re-frame]
            [reagent.core :as r]))

(re-frame/reg-sub
 ::workspace-rect
 (fn [db]
   (get-in db path/workspace-rect)))

(defn- update-workspace-rect [db value]
  (assoc-in db path/workspace-rect value))

(re-frame/reg-event-fx
 ::update-workspace-rect
 (fn [{db :db} _]
   (let [maximized-frame (some (fn [[fid fdesc]]
                                 (when (:is-maximized? fdesc)
                                   fdesc))
                               (get-in db path/frames))
         {:keys [width height] :as rect} (workspace-rect)]
     (cond-> {:db (update-workspace-rect db rect)}
       (and maximized-frame
            (:resize-listener (fi/call-api :frame-instance-api-db-get db (:id maximized-frame))))
       (assoc :dispatch (conj (:resize-listener (fi/call-api :frame-instance-api-db-get db (:id maximized-frame)))
                              (:id maximized-frame)
                              {:width (if (:frame-open-legend maximized-frame)
                                        (- width config/legend-width)
                                        width)
                               :height height}))))))

(re-frame/reg-sub
 ::global-loadingscreen?
 (fn [db]
   (get-in db path/global-loadingscreen)))

(defn- export-file-name []
  (-> (tabs/active-title)
      (st/replace #"\s+" " ")
      (st/replace #"\s" "_")
      (st/replace #"[\: \* \. \? \=]" "")))

(re-frame/reg-event-fx
 ::screenshot
 (fn [{db :db} [_ type]]
   (let [title-sub (-> (statusbar/get-status-infos db)
                       vals
                       first
                       (get :status-message-sub))
         {:keys [width height]} @(re-frame/subscribe [::workspace-rect])
         {^number workspace-zoom :z
          ^number wsp-x :x
          ^number wsp-y :y}
         @(re-frame/subscribe [::navigation-control/position])
         viewport-width (/ width workspace-zoom)
         viewport-height (/ height workspace-zoom)
         viewport-x (- (/ wsp-x workspace-zoom))
         viewport-y (- (/ wsp-y workspace-zoom))
         frames (vals @(re-frame/subscribe [::evts/frames]))
         screenshot-frame-ids (set (map :id (filter-visible-frames viewport-x viewport-y viewport-width viewport-height #{} frames)))
         export-fn (tabs/export-fn)
         export-fn (if (fn? export-fn)
                     (partial export-fn db (tabs/active-tab-details))
                     (partial fi/call-api :make-screenshot-raw))]
     (export-fn
      (cond-> {:dom-id (if-not (tabs/is-project-tab?)
                         (tabs/get-current-tab-id)
                         config/workspace-parent-id)
               :type type
               :optional-title-sub-vec title-sub
               :add-export-details? true
               :frame-ids screenshot-frame-ids
               :screenshot-params {:style {:position :absolute
                                           :inset "0px 0px 0px"
                                           :overflow :hidden
                                           :bottom 0
                                           :right 0
                                           :zIndex -1000}
                                   :quality 1.0
                                   :cacheBust true
                                   :pixelRatio 2}
               :file-name (str (export-file-name)
                               "."
                               (name type))
               :callback-fn (fn [& _]
                              (re-frame/dispatch [::global-loadingscreen false]))}
        (seq (get-in db path/sidebar))
        (assoc :sidebar "explorama-sidebar")))
     {})))

(re-frame/reg-event-db
 ::global-loadingscreen
 (fn [db [_ value]]
   (assoc-in db path/global-loadingscreen value)))

(re-frame/reg-event-fx
 ::screenshot-later
 (fn [{db :db} [_ type]]
   (when (#{:pdf :png} type)
     {:db (assoc-in db path/global-loadingscreen true)
      :dispatch-later [{:ms 400
                        :dispatch [::screenshot type]}
                       {:ms 8000
                        :dispatch [::global-loadingscreen false]}]})))

(defn context-header []
  (let [status-display (tabs/active-title)]
    [:div.project
    ;;  [:span.title status-display]
     (cond
       (tabs/is-project-tab?) [tools/project-actions]
       (tabs/is-reporting-context?) [tools/reporting-actions])]))

(defn explorama-header []
  (let [welcomepage @(re-frame/subscribe [::i18n/translate :menusection-logo])
        project-loading? false ;@(fi/call-api [:project-loading-sub])
        product-tour-active? @(re-frame/subscribe [:de.explorama.frontend.woco.product-tour/running?])
        creating-new-windows? (wwc/creating-new-windows?)]
    [:header
     [:div.navbar {:id config/navbar-id}
      [:div.flex.align-item-center.gap-16
       [tooltip {:text welcomepage}
        [:a {:href "#"
             :class "logo__link"
             :on-click (fn [_]
                         (when-not (or project-loading? creating-new-windows? product-tour-active?)
                           (if @(re-frame/subscribe [::welcome/welcome-active?])
                             (re-frame/dispatch [::welcome/welcome-active false])
                             (re-frame/dispatch [::welcome/welcome-active true]))))}]]]
      (let [overlayer-active? @(re-frame/subscribe [:de.explorama.frontend.woco.api.overlay/overlayer-active?])
            show-tabs? (not (or overlayer-active?
                                @(re-frame/subscribe [:de.explorama.frontend.woco.welcome/welcome-active?])))]
        (when show-tabs?
          [tabs/view {}]))
      (when-not @(re-frame/subscribe [:de.explorama.frontend.woco.welcome/welcome-active?])
        [context-header])
      [tools/header-tools]]]))

(defn- scroll-handler [cont event]
  (when-let [cont @cont]
    (when (or (not= 0 (aget cont "scrollLeft"))
              (not= 0 (aget cont "scrollTop")))
      (.scroll cont 0 0)))
  (when (instance? js/Event event)
    (.preventDefault event)
    (.stopPropagation event)))

(defn- workspace-impl [_ _ _]
  (let [cont (atom nil)
        scroll-handler-fn (partial scroll-handler cont)
        local-multiselect-bb (r/atom nil)]
    (r/create-class
     {:display-name "woco workspace"
      :reagent-render (fn [render? maximized-frame _]
                        [:div {:id config/workspace-parent-id
                               :ref #(reset! cont %)
                               :style {:position :absolute
                                       :user-select :none
                                       :z-index 0
                                       :top "0px"
                                       :left "0px"
                                       :bottom "0px"
                                       :right "0px"
                                       :overflow :hidden}
                               :on-mouse-down (fn [e]
                                                (let [ne (aget e "nativeEvent")]
                                                  (when (and (aget ne "target")
                                                             (is-inside? (aget ne "target")
                                                                         (str "#" config/workspace-parent-id)))
                                                    (panning-handler/mouse-down ne))

                                                  (when (and (wwconfig/select-event? ne)
                                                             @(fi/call-api [:interaction-mode :normal-sub?]))
                                                    (re-frame/dispatch [::wwms/start-temporary-selection])
                                                    (reset! local-multiselect-bb
                                                            {:start-x (aget ne "pageX")
                                                             :start-y (aget ne "pageY")}))))
                               :on-mouse-move (fn [e]
                                                (if @local-multiselect-bb
                                                  (let [{:keys [active? start-x start-y]} @local-multiselect-bb]
                                                    (swap! local-multiselect-bb
                                                           assoc
                                                           :end-x (aget e "pageX")
                                                           :end-y (aget e "pageY"))
                                                    (when (and (not active?)
                                                               (< 10 (Math/sqrt (+ (Math/pow (- (aget e "pageX") start-x)
                                                                                             2)
                                                                                   (Math/pow (- (aget e "pageY") start-y)
                                                                                             2)))))
                                                      (swap! local-multiselect-bb
                                                             assoc
                                                             :active? true))
                                                    (wwms/temporary-hit-test @local-multiselect-bb))
                                                  (panning-handler/mouse-move (aget e "nativeEvent"))))
                               :on-mouse-up (fn [e]
                                              (navigation-control/stop-panning true)
                                              (when (:active? @local-multiselect-bb)
                                                (re-frame/dispatch [::wwms/selection-finish (assoc @local-multiselect-bb
                                                                                                   :end-x (aget e "pageX")
                                                                                                   :end-y (aget e "pageY"))]))
                                              (when (and @local-multiselect-bb
                                                         (not (:active? @local-multiselect-bb)))
                                                (re-frame/dispatch [::wwms/selection-finish nil]))
                                              (reset! wws/temporary-frames {})
                                              (reset! local-multiselect-bb nil))
                               :on-context-menu (fn [e]
                                                  (when (= :yes (navigation-control/ignore-context-menu?))
                                                    (.preventDefault e)
                                                    (navigation-control/set-ignore-context-menu false))
                                                  (when (=  :maybe (navigation-control/ignore-context-menu?))
                                                    (navigation-control/stop-panning true)
                                                    (reset-vertical-drag)
                                                    (deactivate-dropzone)))}
                         (when render?
                           [navigation-core/transform-container
                            {:transform-active? (and render? (not maximized-frame))}
                            [wwms/selection-bb]
                            (for [frame-id @(re-frame/subscribe [::frame-api/all-frame-ids])]
                              ^{:key frame-id}
                              [frame-view/frame frame-id])
                            (when (= :editing @(re-frame/subscribe [::presentation/current-mode]))
                              (for [uid @(re-frame/subscribe [::presentation/all-slide-ids])]
                                ^{:key uid}
                                [presentation-view/slideframe uid]))])
                         [wwms/box local-multiselect-bb]
                         [hints/view]])
      :component-did-mount (fn []
                             (.addEventListener @cont
                                                "scroll"
                                                scroll-handler-fn
                                                #js{:passive false})
                             (.addEventListener @cont
                                                "wheel"
                                                zoom-handler/zoom-handler
                                                #js{:passive false}))
      :component-did-update (fn [this argv]
                              (let [[_ render? maximized-frame global-loadingscreen?] (r/argv this)]

                                (when (and render?
                                           maximized-frame
                                           (not global-loadingscreen?)
                                           @cont)
                                  ;;ensures that the hidden scrollbar is not somewhere else (e.g. effect of autofocus)
                                  (scroll-handler-fn))))
      :component-will-unmount (fn []
                                (.removeEventListener @cont "scroll" scroll-handler-fn)
                                (.removeEventListener @cont "wheel" zoom-handler/zoom-handler))})))

(re-frame/reg-event-db
 ::dropped?
 (fn [db]
   (-> db
       (assoc-in path/page-dropped? true))))

(defn workspace []
  (let [maximized-frame @(re-frame/subscribe [:de.explorama.frontend.woco.frame.api/which-is-maximized])
        render? @(re-frame/subscribe [:de.explorama.frontend.woco.api.interaction-mode/render?])
        overlayer-active? @(re-frame/subscribe [:de.explorama.frontend.woco.api.overlay/overlayer-active?])
        global-loadingscreen? @(re-frame/subscribe [::global-loadingscreen?])]
    [:<>
     [:div {:style {:height "calc(100vh - 3rem)"
                    :width "100vw"
                    :overflow :hidden}}
      (when render?
        [background/canvas])
      [dragging-overlay]
      (when (and (= :presenting @(re-frame/subscribe [::presentation/current-mode]))
                 (not overlayer-active?))
        [presentation-view/presentation-control-container])
      [wcm/view]
      [workspace-impl render? maximized-frame global-loadingscreen?]
      [navigation-control/control-container {:show? (and render? (not maximized-frame))}]
      [tools/sync-projects]
      [wwms/selection-toolbar]]
     [tools/toolbar]]))

(defn main-panel []
  (let [{logged-in-vec :logged-in?-sub-vec
         login-module :module} @(re-frame/subscribe [::registry/lookup-target :login :rights-roles])
        logged-in? true ;(when logged-in-vec @(re-frame/subscribe logged-in-vec))
        maximized-frame @(re-frame/subscribe [:de.explorama.frontend.woco.frame.api/which-is-maximized])
        render? @(re-frame/subscribe [:de.explorama.frontend.woco.api.interaction-mode/render?])
        overlayer-active? @(re-frame/subscribe [:de.explorama.frontend.woco.api.overlay/overlayer-active?])
        welcome-active? @(re-frame/subscribe [::welcome/welcome-active?])
        global-loadingscreen? @(re-frame/subscribe [::global-loadingscreen?])
        theme (when-let [sub (fi/call-api :config-theme-sub)]
                @sub)
        overlays @(re-frame/subscribe [::overlays/list])]
    (-> (sel1 :body)
        (dommy/add-class! "login"))
    [:<>
     [product-tour/start-popup]
     [notifications/notification-container]
     (if logged-in?
       (do (-> (sel1 :body)
               (dommy/remove-class! "login"))
           [:div
            {:class (case theme
                      :light "theme-light"
                      :dark "theme-dark"
                      "")}
            (when-not (or welcome-active? overlayer-active? global-loadingscreen?)
              [:div.explorama
               {:class (when (and maximized-frame render?)
                         "explorama--window-maximized")}
               [explorama-header]])
            [:div.explorama__workspace {:id config/workspace-root-id
                                        :style {:height "calc(100vh - 3rem)"
                                                :width "100vw"
                                                :overflow :hidden}}
             [sidebar/sidebar]
             [comp-dialog/view]
             (for [[o-key over] overlays]
               ^{:key o-key}
               [over])
             [tabs/tabs-render-content]]
            (when global-loadingscreen?
              [loading-screen {:show? true
                               :message @(re-frame/subscribe [::i18n/translate :global-loadingscreen-msg])
                               :tip @(re-frame/subscribe [::i18n/translate :global-loadingscreen-tip])}])])
       [:<>
        (when login-module
          [login-module])
        [cr/links :center]])
     [cr/sheet]]))
