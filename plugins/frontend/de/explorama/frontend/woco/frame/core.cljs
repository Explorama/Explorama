
(ns de.explorama.frontend.woco.frame.core
  (:require [clojure.set :as set]
            [de.explorama.shared.common.configs.woco :as woco-config-shared]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [warn]]
            [de.explorama.frontend.woco.api.interaction-mode :as im-api]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.frame.info :as frame-info]
            [de.explorama.frontend.woco.frame.interaction.z-order
             :refer [bring-to-front z-index-lookup]]
            [de.explorama.frontend.woco.frame.size-position
             :refer [calculate-size legend-size set-frame-position]]
            [de.explorama.frontend.woco.frame.view.legend :refer [legend-open?]]
            [de.explorama.frontend.woco.navigation.control :as nav-control]
            [de.explorama.frontend.woco.navigation.util :refer [workspace-rect]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.workspace.config :refer [fixed-height fixed-width]]
            [de.explorama.frontend.woco.workspace.rearrange :as wwrearrange]
            [de.explorama.frontend.woco.workspace.window-creation :as wwc]))

(defn calc-vertical-number [db vertical]
  (inc (get-in db (path/id-counter vertical) 0)))

(defn build-frame-desc
  "Creates an frame-desc"
  [{:keys [actual-id
           new-coords size min-width min-h
           legend-open-flag]
    :as new-frame-desc}
   overwrites]
  (cond-> (assoc (select-keys new-frame-desc [:size :event
                                              :module :type :vertical
                                              :vertical-number
                                              :z-index :published-by-frame
                                              :data-consumer
                                              :stick-to-frames?
                                              :ignore-drop-on-frame? :min-h
                                              :no-event-logging? :title
                                              :optional-class :on-drop
                                              :resizable
                                              :position-handling-target])
                 :id actual-id
                 :coords new-coords
                 :creation-date (js/Date.now)
                 :full-size size)
    min-width (assoc :min-resize-width (or min-width
                                           (* 50
                                              (/ (first size)
                                                 100))))
    min-h (assoc :min-resize-height (or min-h
                                        (* 50
                                           (/ (second size)
                                              100))))
    :always
    (legend-size legend-open-flag)
    :always
    (assoc :frame-open-legend legend-open-flag)
    (:attributes overwrites)
    (merge (:attributes overwrites))))

(defn current-configuration [db & [overwrites]]
  (merge
   {woco-config-shared/new-window-pref-key
    (:action (fi/call-api [:user-preferences :preference-db-get]
                          db
                          woco-config-shared/new-window-pref-key
                          woco-config-shared/new-window-default))
    woco-config-shared/published-window-pref-key
    (:action (fi/call-api [:user-preferences :preference-db-get]
                          db
                          woco-config-shared/published-window-pref-key
                          woco-config-shared/published-window-default))
    woco-config-shared/published-windows-pref-key
    (:action (fi/call-api [:user-preferences :preference-db-get]
                          db
                          woco-config-shared/published-windows-pref-key
                          woco-config-shared/published-windows-default))}
   (:configuration overwrites)))

(defn- new-window-left-or-right-positioning [db configuration size actual-id publishing-frame-id]
  (let [frames (get-in db path/frames)
        offset 15
        publishing-frame (get frames publishing-frame-id)
        [x y] (:coords publishing-frame)
        [width height] (:full-size publishing-frame)
        {frames-order :frames
         moved-frames :moved-frames}
        (:position-handling-source publishing-frame)
        frames-order (->> (set/difference (set (flatten frames-order))
                                          moved-frames)
                          (sort-by (fn [fid] (:creation-date (get frames fid))))
                          (partition-all 2)
                          ((fn [d]
                             (if (= :left configuration)
                               (reverse d)
                               d)))
                          (mapv vec))
        frames-order (if (= :left configuration)
                       (let [head-col (first frames-order)]
                         (cond (empty? head-col)
                               [[actual-id]]
                               (= 1 (count head-col))
                               (vec (cons (vec
                                           (cons actual-id head-col))
                                          (vec (rest frames-order))))
                               (= 2 (count head-col))
                               (vec (cons [actual-id] frames-order))))
                       (let [last-col (peek frames-order)]
                         (cond (empty? last-col)
                               [[actual-id]]
                               (= 1 (count last-col))
                               (vec (conj (pop frames-order)
                                          (vec (conj last-col actual-id))))
                               (= 2 (count last-col))
                               (vec (conj frames-order [actual-id])))))
        frame-size-pos-map (wwrearrange/frame-size-pos-map (assoc frames
                                                                  actual-id {:full-size size})
                                                           frames-order)
        [nx ny :as new-window-coords] (wwrearrange/calc-cords x y width offset frame-size-pos-map actual-id)
        {px :x py :y z :z} (nav-control/position db)
        {ww :width wh :height} (workspace-rect)
        wx (* 1 (/ px z))
        wy (* 1 (/ py z))
        [new-frames events] (wwrearrange/reorder db publishing-frame-id frames-order
                                                 (get-in db path/frames)
                                                 x y width offset frame-size-pos-map)]
    [new-window-coords
     (assoc-in db
               path/frames
               (-> new-frames
                   (assoc-in [publishing-frame-id :position-handling-source :frames] frames-order)))
     (if (and (= :right configuration)
              (or (not (< wx nx (+ wx (/ ww z))))
                  (not (< wy ny (+ wy (/ wh z))))))
       (conj events
             [:de.explorama.frontend.woco.navigation.control/focus actual-id (or z (:z config/default-position)) true])
       events)]))

(defn- new-window-grid-positioning [db id]
  (let [frames (get-in db (conj path/current-workspace-grid :num) [])
        num (loop [i 0
                   frames frames]
              (if (or (empty? frames)
                      (nil? (first frames)))
                i
                (recur (inc i)
                       (rest frames))))
        frames (assoc frames num id)
        {wsp-zoom :z wsp-x :x wsp-y :y} (nav-control/position db)
        number-of-cols (cond (<= wsp-zoom 0.30)
                             8
                             (<= wsp-zoom 0.55)
                             5
                             (<= wsp-zoom 0.8)
                             4
                             (<= wsp-zoom 1.05)
                             3
                             (<= wsp-zoom 1.5)
                             2)
        x (mod num number-of-cols)
        y (Math/floor (/ num number-of-cols))
        x (+ (* x fixed-width)
             (/ (- wsp-x)
                wsp-zoom)
             100)
        y (+ (* y fixed-height)
             (/ (- wsp-y)
                wsp-zoom)
             200)]
    [[x y]
     (assoc-in db (conj path/current-workspace-grid :num) frames)
     nil]))

(defn- new-windowws-box-drop [[x y] idx]
  (let [idx (or idx 0)
        offset-y (mod idx 2)
        offset-x (Math/floor (/ idx 2))]
    [(+ x (* offset-x fixed-width))
     (+ y (* offset-y fixed-height))]))

(defn- determine-behavior [configuration
                           {{behavior-overwrites :behavior} :overwrites
                            publishing-frame-id :publishing-frame-id
                            multiple-windows? :multiple-windows?
                            idx :idx}
                           mouse-pos]
  (let [published? (boolean publishing-frame-id)
        behavior (when (seq behavior-overwrites)
                   (let [{:keys [config-key config-map force]}
                         behavior-overwrites]
                     (or force
                         (config-map
                          (get configuration config-key)))))]
    (cond (and mouse-pos
               (or (= behavior :drop)
                   (and (not behavior)
                        (or (and (not multiple-windows?)
                                 (not published?)
                                 (= :drop (get configuration woco-config-shared/new-window-pref-key)))
                            (and (not multiple-windows?)
                                 published?
                                 (= :drop (get configuration woco-config-shared/published-window-pref-key)))
                            (and multiple-windows?
                                 published?
                                 idx
                                 (= :drop (get configuration woco-config-shared/published-windows-pref-key)))))))
          :drop
          (and published?
               (or (= behavior :left)
                   (and (not behavior)
                        (or (and (not multiple-windows?)
                                 (= :left (get configuration woco-config-shared/published-window-pref-key)))
                            (and multiple-windows?
                                 (= :left (get configuration woco-config-shared/published-windows-pref-key)))))))
          :left
          (and published?
               (or (= behavior :right)
                   (and (not behavior)
                        (or (and (not multiple-windows?)
                                 (= :right (get configuration woco-config-shared/published-window-pref-key)))
                            (and multiple-windows?
                                 (= :right (get configuration woco-config-shared/published-windows-pref-key)))))))
          :right
          (or (= behavior :grid)
              (and (not behavior)
                   (or (and (not multiple-windows?)
                            (not published?)
                            (= :grid (get configuration woco-config-shared/new-window-pref-key)))
                       (and published?
                            (:vertical publishing-frame-id)))))
          :grid
          (= behavior :provided-position)
          :provided-position
          :else
          (do (warn "Using fallback window positioning behavior")
              :grid))))

(defn- create-frame-with-id [db
                             {:keys [id event vertical
                                     size size-in-pixel size-min
                                     type mouse-pos]
                              {{{set-header-color? :set-header-color?} :color
                                :as overwrites}
                               :overwrites
                               publishing-frame-id :publishing-frame-id
                               idx :idx
                               :as opts}
                              :opts
                              :as frame-api-desc}
                             provider-id]
  (let [configuration (current-configuration db overwrites)
        behavior (determine-behavior configuration opts mouse-pos)
        legend-open-flag (legend-open? db id)
        actual-id (if provider-id provider-id id)
        size (if size-in-pixel
               size-in-pixel
               (calculate-size size))
        [min-width min-h] size-min
        vertical-number (calc-vertical-number db vertical)
        [new-coords db dispatch-n]
        (case behavior
          :provided-position
          [(:coords-in-pixel frame-api-desc)
           db
           nil]
          :right
          (new-window-left-or-right-positioning db
                                                :right
                                                (if legend-open-flag
                                                  (update size 0 + config/legend-width)
                                                  size)
                                                actual-id
                                                publishing-frame-id)
          :left
          (new-window-left-or-right-positioning db
                                                :left
                                                (if legend-open-flag
                                                  (update size 0 + config/legend-width)
                                                  size)
                                                actual-id
                                                publishing-frame-id)
          :drop
          [(if idx
             (new-windowws-box-drop (nav-control/page->workspace db mouse-pos) idx)
             (nav-control/page->workspace db mouse-pos))
           db
           nil]
          :grid
          (new-window-grid-positioning db id))
        new-zindex (if (= type evts/custom-type)
                     config/custom-frame-z-index-ontop
                     (z-index-lookup db))
        new-frame-desc (build-frame-desc (assoc frame-api-desc
                                                :legend-open-flag legend-open-flag
                                                :published-by-frame publishing-frame-id
                                                :actual-id actual-id
                                                :size size
                                                :min-width min-width
                                                :vertical-number vertical-number
                                                :min-h min-h
                                                :new-coords new-coords
                                                :z-index new-zindex)
                                         overwrites)
        db (cond-> (assoc-in db
                             (path/frame-desc actual-id)
                             new-frame-desc)
             (not= type evts/custom-type)
             (assoc-in path/curr-max-zindex new-zindex)
             (#{evts/content-type evts/custom-type} type)
             (assoc-in (path/id-counter vertical) vertical-number))
        ;; workaround to respect bring to front for custom-type
        [db] (if (= type evts/custom-type)
               (bring-to-front db actual-id)
               [db])
        information-bundle (when publishing-frame-id
                             (frame-info/gather-information db publishing-frame-id overwrites))]
    (set-frame-position actual-id new-coords)
    {:db db
     :dispatch-n (cond-> (or dispatch-n [])
                   (and (keyword? event)
                        (im-api/render? db))
                   (conj [:de.explorama.frontend.woco.event-logging/log-frame-event actual-id]
                         [:de.explorama.frontend.woco.event-logging/log-frame-event publishing-frame-id]
                         [event evts/init {:frame-id actual-id
                                           :publishing-frame-id publishing-frame-id
                                           :size size
                                           :coords new-coords
                                           :min-h min-h
                                           :payload information-bundle
                                           :opts opts}])
                   set-header-color?
                   (conj (fi/call-api :frame-header-color-event-vec actual-id))
                   set-header-color?
                   (conj (fi/call-api :frame-set-publishing-event-vec actual-id true)))
     :dispatch-later (when (and (keyword? event)
                                (im-api/render? db))
                       [{:ms 20 :dispatch [:de.explorama.frontend.woco.workspace.background/draw-connecting-edges]}])}))

(re-frame/reg-event-fx
 ::create-frame-with-id
 (fn [{db :db} [_ frame-api-desc provider-id]]
   (create-frame-with-id db frame-api-desc provider-id)))

(defn gen-frame-id [db vertical]
  {:frame-id (str vertical "-" (random-uuid))
   :workspace-id (get-in db (path/workspace-id))
   :provider-origin config/default-namespace
   :vertical vertical})

(re-frame/reg-event-fx
 ::create-frame-action
 (fn [{db :db}
      [_ {:keys [id vertical]
          :as new-frame}]]
   (let [default-id (gen-frame-id db vertical)]
     (if id
       {:dispatch-n [[:de.explorama.frontend.woco.frame.api/normalize]
                     [::create-frame-with-id new-frame]]}
       {:dispatch-n [[:de.explorama.frontend.woco.frame.api/normalize]
                     [::create-frame-with-id (assoc new-frame :id default-id)]]}))))

(re-frame/reg-event-fx
 ::create-frame
 (fn [{db :db} [_ {{overwrites :overwrites
                    idx :idx
                    :as opts}
                   :opts
                   :as new-frame}]]
   (let [configuration (current-configuration db overwrites)
         behavior (determine-behavior configuration opts true)
         drop-placement? (= :drop behavior)]
     (if drop-placement?
       (do
         (wwc/create-new-drop-event {:action [::create-frame-action]
                                     :payload new-frame
                                     ;:cursor "url(\"/assets/img/window-placement.svg\"), cell"
                                     ;Verticals could provide a cursor 
                                     #_(:cursor opts)})
         {})
       {:dispatch [::create-frame-action new-frame]}))))
