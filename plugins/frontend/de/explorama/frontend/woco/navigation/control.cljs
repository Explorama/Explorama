(ns de.explorama.frontend.woco.navigation.control
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.misc.core :refer [toolbar]]
            [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.ui-base.utils.view :refer [bounding-rect-node]]
            [re-frame.core :as re-frame]
            [reagent.core :as r]
            [de.explorama.frontend.woco.api.interaction-mode :as interaction-mode]
            [de.explorama.frontend.woco.api.registry :as registry]
            [de.explorama.frontend.woco.components.context-menu :refer [close-context-menu-db]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.util :refer [which-is-maximized]]
            [de.explorama.frontend.woco.navigation.framelist :as framelist]
            [de.explorama.frontend.woco.navigation.fullscreen-handler :as fullscreen-handler]
            [de.explorama.frontend.woco.navigation.minimap.view :as minimap]
            [de.explorama.frontend.woco.navigation.panning-borders :refer [panning-borders]]
            [de.explorama.frontend.woco.navigation.resources :as resources]
            [de.explorama.frontend.woco.navigation.snapping :as snapping]
            [de.explorama.frontend.woco.navigation.util :refer [respect-boundaries-relaxed
                                                                safe-number? workspace-rect]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.util.api :refer [db-get-error-boundary]]
            [de.explorama.frontend.woco.welcome :as welcome]
            [de.explorama.frontend.woco.workspace.background :as background]
            [de.explorama.frontend.woco.workspace.connecting-edges :as ce]
            [de.explorama.frontend.woco.workspace.cubic-bezier :as cb]
            [de.explorama.frontend.woco.workspace.window-creation :as wwc]))

(defn workspace-frame-id
  "Generates an frame-id for event-logging, because an frame-id is necessary there"
  [db]
  {:frame-id "woco-workspace"
   :workspace-id (get-in db (path/workspace-id))
   :vertical "woco"})

;; For detecting changes while moving mouse.
;; Important for keyboard zoom (no mouse positions available) and for panning
(def mouse-position-state (atom nil))
(def mouse-position-state-start (atom nil))

(defn mouse-position []
  @mouse-position-state)

(defn set-mouse-position [x y]
  (reset! mouse-position-state {:x x :y y}))

(defn mouse-position-start []
  @mouse-position-state-start)

(defn set-mouse-position-start [x & [y d]]
  (if x
    (reset! mouse-position-state-start {:x x :y y :d d})
    (reset! mouse-position-state-start nil)))

(def ignore-context-menu (atom false))
(defn set-ignore-context-menu [v]
  (reset! ignore-context-menu v))

(defn ignore-context-menu? []
  @ignore-context-menu)

(def panning? (r/atom false))

(defn is-panning? []
  @panning?)

(defn stop-panning [& [force]]
  (when (or @panning?
            force)
    (reset! panning? false)
    (set-mouse-position-start nil)))

(defn start-panning []
  (when-not @panning?
    (reset! panning? true)))

(defn position
  "Ensures valid coordinates. Access should be always with this fn"
  ([db]
   (get-in db path/navigation-position config/default-position))
  ([db coordinate]
   (-> (position db)
       (get coordinate))))

(defn page->workspace
  "Translates page coordinates to workspace coordinates"
  ([wsp-x wsp-y wsp-zoom [x y]]
   (let [x (/ (- x wsp-x)
              wsp-zoom)
         y (/ (- y wsp-y)
              wsp-zoom)]
     [x y]))
  ([db coords]
   (let [{wsp-zoom :z wsp-x :x wsp-y :y} (position db)]
     (page->workspace wsp-x wsp-y wsp-zoom coords))))

(defn workspace-position->page
  "Translates page coordinates to workspace coordinates"
  [wsp-pos [x y] & [respect-app-header-height?]]
  (let [{wsp-zoom :z wsp-x :x wsp-y :y} wsp-pos
        x (+ (* x wsp-zoom)
             wsp-x)

        y (+ (* y wsp-zoom)
             wsp-y)]
    [x
     (if respect-app-header-height?
       (+ y config/explorama-header-height)
       y)]))

(defn workspace->page
  "Translates workspace coordinates to page coordinates (can be out of view)"
  ([db coords respect-app-header-height?]
   (workspace-position->page (position db)
                             coords
                             respect-app-header-height?))
  ([db coords]
   (workspace->page db coords true)))

(re-frame/reg-sub
 ::workspace-scale
 (fn [db _]
   (if (which-is-maximized db)
     1
     (position db :z))))

(re-frame/reg-sub
 ::page->workspace
 (fn [db [_ coords]]
   (page->workspace db coords)))

(re-frame/reg-sub
 ::workspace->page
 (fn [db [_ coords respect-app-header-heigt?]]
   (if (boolean? respect-app-header-heigt?)
     (workspace->page db coords respect-app-header-heigt?)
     (workspace->page db coords))))

(re-frame/reg-event-fx
 ::log-position
 (fn [{db :db} [_ pos]]
   {:db (assoc-in db path/last-logged-position pos)
    :dispatch [:de.explorama.frontend.woco.event-logging/log-event
               (workspace-frame-id db)
               "woco-pan-zoom"
               pos]}))

(re-frame/reg-event-fx
 ::check-position-logging
 (fn [{db :db} [_ pos origin-workspace-id]]
   (let [logged-in? (db-get-error-boundary db
                                           (registry/lookup-target db :db-get :logged-in?)
                                           :logged-in?)
         log? (and
               (= origin-workspace-id (get-in db (path/workspace-id)))
               (not (interaction-mode/read-only?
                     db
                     {:component :*
                      :additional-info :navigation}))
               (interaction-mode/render? db)
               (not (welcome/welcome-active? db))
               logged-in?
               (not= pos (get-in db path/last-logged-position))
               pos)]
     (cond-> {}
       (and log? (= pos (position db)))
       (assoc :dispatch [::log-position pos])))))

(re-frame/reg-sub
 ::position
 (fn [db]
   (position db)))

(defn set-position
  "Sets the position. Write access should only be made using this function"
  ([db {:keys [x y z]} force?]
   (let [logged-in? (db-get-error-boundary db
                                           (registry/lookup-target db :db-get :logged-in?)
                                           :logged-in?)
         abort-condition? (or (get-in db path/overlayer-active?)
                              #_(interaction-mode/product-tour-read-only? db :* :navigation) ;Temporary deactivated for issue #9791
                              (welcome/welcome-active? db)
                              (not logged-in?)
                              (which-is-maximized db))]
     (when (or (not abort-condition?)
               force?)
       (let [db (cond-> (close-context-menu-db db)
                  (safe-number? x) (assoc-in path/navigation-position-x x)
                  (safe-number? y) (assoc-in path/navigation-position-y y)
                  (safe-number? z) (assoc-in path/navigation-position-z z))]
         (cond-> {:db db}
           (not abort-condition?)
           (assoc :dispatch-later [{:ms config/position-logging-timeout
                                    :dispatch [::check-position-logging
                                               (position db)
                                               (get-in db (path/workspace-id))]}]))))))
  ([db pos]
   (set-position db pos false))
  ([db x y z]
   (set-position db {:x x :y y :z z})))

(re-frame/reg-event-fx
 ::set-position
 (fn [{db :db} [_ position force?]]
   (set-position db position force?)))

(defn move-to
  "Translates coordinates to transform position"
  ([db x y offset-x offset-y]
   (when (and (safe-number? x)
              (safe-number? y))
     (let [{:keys [z]} (position db)
           offset-x (or offset-x 0)
           offset-y (or offset-y 0)]
       (set-position db {:x (- (* (- x offset-x)
                                  z))
                         :y (- (* (- y offset-y)
                                  z))}))))
  ([db x y]
   (move-to db x y nil nil)))

(re-frame/reg-event-fx
 ::move-to
 (fn [{db :db} [_ x y offset-x offset-y z]]
   (let [db (if z
              (:db (set-position db {:z z}))
              db)]
     (move-to db x y offset-x offset-y))))


(re-frame/reg-event-fx
 ::focus
 (fn [{db :db} [_ frame-id z animate?]]
   (let [[fx fy] (get-in db (path/frame-coords frame-id))
         [fw fh] (get-in db (path/frame-size frame-id))
         z (if z z 1)]
     (when (and fx fy fw fh)
       (let [{:keys [width height]} (workspace-rect)
             center-x (- (/ width 2)
                         (/ fw 2))
             center-y (- (/ height 2)
                         (/ fh 2))]
         {:dispatch-n [(if animate?
                         [::animated-set-position [(- (* (- fx (- (/ (/ width 2) z)
                                                                  (/ fw 2)))
                                                         z))
                                                   (- (* (- fy (- (/ (/ height 2) z)
                                                                  (/ fh 2)))
                                                         z))
                                                   z]]
                         [::move-to fx fy center-x center-y z])
                       [:de.explorama.frontend.woco.frame.api/bring-to-front frame-id]]})))))

(defn- pan
  "Calculates panning position related to current position and an given diff"
  [db x-diff y-diff]
  (let [{:keys [x y z]} (position db)]
    (let [new-x (- x x-diff)
          new-y (- y y-diff)]
      (background/pan-zoom new-x new-y z)
      (set-position db
                    {:x new-x
                     :y new-y}))))

(re-frame/reg-event-fx
 ::reset-position
 (fn [{db :db} [_ force?]]
   (background/pan-zoom (:x config/default-position)
                        (:y config/default-position)
                        (:z config/default-position))
   (set-position db config/default-position force?)))

(re-frame/reg-event-fx
 ::pan
 (fn [{db :db} [_ x-diff y-diff]]
   (-> (wwc/reset-new-window-num db)
       (pan x-diff y-diff))))

(defn- point-zoom
  "Calculates position after zooming related to some point"
  [db
   {:keys [left top]
    :or {left 0 top 0}}
   {px :x py :y
    :or {px 0 py 0}}
   z-factor]
  (let [{:keys [x y z]} (position db)
        new-z (-> (Math/abs (* z z-factor))
                  (respect-boundaries-relaxed z config/min-zoom config/max-zoom))]
    (when-not (= new-z z)
      (let [scale-factor (/ new-z z)
            px (- px left)
            py (- py top)
            new-x (- px (* scale-factor
                           (- px x)))

            new-y (- py (* scale-factor
                           (- py y)))]
        (background/pan-zoom new-x new-y new-z)
        (set-position db new-x new-y new-z)))))

(re-frame/reg-event-fx
 ::point-zoom
 (fn [{db :db} [_ z-factor point]]
   (-> (wwc/reset-new-window-num db)
       (point-zoom (workspace-rect)
                   (if (map? point)
                     point
                     (mouse-position))
                   z-factor))))

(re-frame/reg-event-fx
 ::center-zoom
 (fn [{db :db} [_ {:keys [z-factor target-zoom]}]]
   (let [{:keys [width height] :as w-rect} (workspace-rect)
         z-factor (if target-zoom
                    (/ target-zoom (position db :z))
                    z-factor)]
     (-> (wwc/reset-new-window-num db)
         (point-zoom w-rect
                     {:x (/ width 2)
                      :y (/ height 2)}
                     z-factor)))))

(defn- frames-boundings [db]
  (loop [frames (get-in db path/frames)
         [_ {[fx fy] :coords
             [fw fh] :full-size
             :as frame}]
         (first (get-in db path/frames))
         l nil
         t nil
         r nil
         b nil]
    (cond (and (empty? frames)
               (not frame))
          {:left (- (or l 0) config/fit-to-content-padding-horizontal)
           :top (- (or t 0) config/fit-to-content-padding-vertical)
           :right (+ (or r 0) config/fit-to-content-padding-horizontal)
           :bottom (+ (or b 0) config/fit-to-content-padding-vertical)}

          (not (or fx fy fw fh frame))
          (recur (rest frames)
                 (first frames)
                 l t r b)

          :else
          (let [l (if l (min l fx) fx)
                t (if t (min t fy) fy)
                r (if r
                    (max r (+ fx fw))
                    (+ fx fw))
                b (if b
                    (max b (+ fy fh))
                    (+ fy fh))]
            (recur (rest frames)
                   (first frames)
                   l t r b)))))

(re-frame/reg-event-fx
 ::fit-content
 (fn [{db :db}]
   (let [{:keys [width height]} (workspace-rect)
         {:keys [left top right bottom]} (frames-boundings db)
         scale-x (/ width (- right left))
         scale-y (/ height (- bottom top))
         new-z (max config/min-zoom (min scale-x scale-y config/max-zoom))]
     (when (and (safe-number? scale-x)
                (safe-number? scale-y)
                (safe-number? left)
                (safe-number? top))
       (let [x (* new-z (- left))
             y (* new-z (- top))
             z new-z]
         (background/pan-zoom x y z)
         (set-position db {:x x
                           :y y
                           :z z}))))))

(defn- zoom-percentage [z]
  (let [{dz :z} config/default-position]
    (Math/round (/ z
                   (/ dz 100)))))

(re-frame/reg-sub
 ::enable-overlay?
 (fn [db [_ force?]]
   (or (val-or-deref force?)
       (<= (zoom-percentage (position db :z))
           (zoom-percentage config/drag-frame-zoom)))))

(defn- zoom-indicator [z]
  (str (zoom-percentage z)
       "%"))

(re-frame/reg-event-fx
 ::deactivate-animation
 (fn [{db :db} _]
   {:db (assoc-in db path/presentation-animation-activated false)}))

(defn animated-pan-zoom [points z]
  (let [time-to-add (/ 700 cb/nominator)]
    (loop [remaining-points points
           timeout 0]
      (when (seq remaining-points)
        (let [[current-x current-y] (first remaining-points)]
          (js/setTimeout #(background/pan-zoom current-x current-y z) timeout)
          (recur (rest remaining-points)
                 (+ timeout time-to-add)))))))

(re-frame/reg-event-fx
 ::animated-set-position
 (fn [{db :db} [_ [x y z]]]
   (let [{ox :x oy :y oz :z} (position db)]
     (when-let [db (:db (set-position db {:x x :y y :z z}))]
       (let [;Difference between the target position of the animation and current position saved in db (in L1-norm).
              ;The animation is supposed to run only if those differ by more than a rounding error.
             position-difference (->> [x y z]
                                      (map - [ox oy oz])
                                      (map Math/abs)
                                      (reduce +))]
         (if (>= position-difference 0.1)
           (do
             (animated-pan-zoom (cb/calculate-points [ox oy] [x y]) z)
             {:db (assoc-in db path/presentation-animation-activated true)})
           {:db db}))))))

(re-frame/reg-sub
 ::animation-activated?
 (fn [db _]
   (get-in db path/presentation-animation-activated)))

(defn control-container [_]
  (let [snap-menu-pos (r/atom nil)]
    (r/create-class
     {:display-name "woco navigation control"
      :component-did-mount #(resources/load-resources)
      :reagent-render (fn [{:keys [show?]}]
                        (when show?
                          (let [{:keys [x y z]} @(re-frame/subscribe [::position])
                                toggle-minimap? @(re-frame/subscribe [::minimap/show?])
                                toggle-framelist? @(re-frame/subscribe [::framelist/show?])
                                toggle-snapping? (snapping/snapping?)
                                min-zoom-reached? (<= z config/min-zoom)
                                max-zoom-reached? (<= config/max-zoom z)
                                {:keys [navigation-fit-to-content
                                        navigation-zoom-out navigation-zoom-in
                                        navigation-reset navigation-snapping]}
                                @(re-frame/subscribe [::i18n/translate-multi
                                                      :navigation-fit-to-content
                                                      :navigation-zoom-out
                                                      :navigation-zoom-in
                                                      :navigation-snapping
                                                      :navigation-reset])
                                navbar-offset @(fi/call-api :sidebar-width-sub)]
                            [:<>
                             [panning-borders]
                             [snapping/snap-menu snap-menu-pos]
                             [toolbar
                              {:id "viewport-toolbar"
                               :orientation :horizontal
                               :tooltip-direction :up
                               :separator :symbol
                               :extra-class ["bottom-8" "absolute"]
                               :offset {:right (+ 10 navbar-offset)}
                               :items [[{:id "viewport-snapping"
                                         :title navigation-snapping
                                         :icon :magnet
                                         :active? toggle-snapping?
                                         :on-click (fn [e]
                                                     (let [{:keys [top left]}
                                                           (bounding-rect-node (.getElementById js/document "viewport-toolbar"))]
                                                       (reset! snap-menu-pos
                                                               {:top (- top 79) ; menu height
                                                                :left left})))}
                                        (ce/toggle)
                                        (framelist/toggle toggle-framelist?)
                                        (minimap/toggle toggle-minimap?)]
                                       [{:id "viewport-zoom-out"
                                         :title navigation-zoom-out
                                         :icon :minus
                                         :on-click #(re-frame/dispatch [::center-zoom {:z-factor config/key-zoom-out-factor}])
                                         :disabled? min-zoom-reached?}
                                        {:id "viewport-zoom-reset"
                                         :title navigation-reset
                                         :label (zoom-indicator z)
                                         :on-click #(re-frame/dispatch [::center-zoom {:target-zoom 1}])}
                                        {:id "viewport-zoom-in"
                                         :title navigation-zoom-in
                                         :icon :plus
                                         :on-click #(re-frame/dispatch [::center-zoom {:z-factor config/key-zoom-in-factor}])
                                         :disabled? max-zoom-reached?}
                                        {:id "viewport-fit-to-content"
                                         :title navigation-fit-to-content
                                         :icon :fit-width
                                         :on-click #(re-frame/dispatch [::fit-content])}
                                        (fullscreen-handler/toggle)]]
                               :popout-position :start
                               :popouts [{:id "viewport-popout-framelist"
                                          :show?  toggle-framelist?
                                          :content [framelist/framelist toggle-framelist?]}
                                         {:id "viewport-popout-minimap"
                                          :show? toggle-minimap?
                                          :content [minimap/minimap x y z]}]}]])))})))
