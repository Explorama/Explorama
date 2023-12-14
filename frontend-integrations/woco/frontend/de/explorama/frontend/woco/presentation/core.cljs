(ns de.explorama.frontend.woco.presentation.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [goog.events :as events]
            [re-frame.core :as re-frame]
            [re-frame.db]
            [de.explorama.frontend.woco.api.interaction-mode :as interaction-mode]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.events :as frame]
            [de.explorama.frontend.woco.navigation.control :as nav :refer [position]]
            [de.explorama.frontend.woco.navigation.util :as nav-util :refer [workspace-rect]]
            [de.explorama.frontend.woco.path :as path])
  (:import [goog.events EventType]))

(defn set-slides
  "Write access to current presentation"
  [db slides]
  (assoc-in db path/slides slides))

(defn get-slides
  "Read access to current presentation"
  [db]
  (get-in db path/slides))

(defn slide-by-uid
  [db frame-id]
  (first (keep-indexed (fn [index slide]
                         (when (= (:uid slide) frame-id) index))
                       (get-slides db))))

(defn add-slide
  "Add slide at the end"
  [db slide]
  (let [slides (get-slides db)
        initilized (or slides [])
        slide-exist? (slide-by-uid db (:uid slide))]
    (if-not slide-exist?
      (set-slides db (conj initilized slide))
      db)))

(defn change-slide
  "Replace or delete a slide."
  ([db sindex new-slide]
   (let [slides (get-slides db)]
     (set-slides db (assoc slides sindex new-slide))))
  ([db sindex]
   (let [slides (get-slides db)]
     (set-slides db (into (subvec slides 0 sindex) (subvec slides (inc sindex)))))))

(defn get-slide
  [db index]
  (let [slides (get-slides db)
        slide-count (count slides)]
    (cond
      (< index 0) (get slides 0)
      (>= index slide-count) (peek slides)
      :else (get slides index))))

(defn get-current-slide
  "Keeps track of the state while presenting.
   Current slide is -1, if there are no slides. If there are slides, the default value is 0."
  [db]
  (min (get-in db path/current-slide 0) (- (count (get-slides db)) 1)))

(defn xywh->xyz
  "Yields [x y z] coordinates expected by 'position' given a rectangle of [x y width height]."
  ([x y w h]
   (let [{tw :width th :height} (workspace-rect)
         z  (min (/ tw w) (/ th h) config/max-zoom)
         ox (/ (- (* z w) tw) 2)
         oy (/ (- (* z h) th) 2)]
     [(- (* z (- x)) ox) (- (* z (- y)) oy) z]))
  ([{:keys [x y w h]}]
   (xywh->xyz x y w h)))

(defn slide-coords
  "Yields [x y z] coordinates given an index of a slide. Defaults to first or last slide given illegal indices."
  [db index]
  (let [{:keys [x y w h]} (get-slide db index)]
    (xywh->xyz x y w h)))

(defn next-slide [db from direction]
  (let [slides (get-slides db)
        max-slide (- (count slides) 1)]
    (cond
      (and (> direction 0) (>= from max-slide)) from
      (and (< direction 0) (<= from 0)) from
      :else (+ from direction))))

(re-frame/reg-sub
 ::slide-sub
 (fn [db _]
   (get-current-slide db)))

(re-frame/reg-sub ::slides get-slides)

(re-frame/reg-sub
 ::max-slide-sub
 :<- [::slides]
 count)

(re-frame/reg-sub
 ::no-slides?
 :<- [::slides]
 (comp not seq))

(re-frame/reg-sub
 ::frames?
 :<- [::frame/frames]
 (comp boolean seq))

(defn- sameish-position [s1 s2]
  (->> (merge-with (comp Math/abs -)
                   (select-keys s1 [:x :y :w :h])
                   (select-keys s2 [:x :y :w :h]))
       vals
       (reduce +)
       (> 1)))

(defn- position-occupied? [presentation]
  #(some (partial sameish-position %) presentation))

(re-frame/reg-event-fx
 ::add-slides-to-all-frames
 [interaction-mode/ro-interceptor]
 (fn [{db :db} _]
   (let [padding config/slideframe-padding
         frames (get-in db path/frames)
         frame->slide (fn [{[x y] :coords
                            [w h] :full-size
                            fid :id}]
                        {:x (- x padding)
                         :y (- y padding)
                         :w (+ w (* 2 padding))
                         :h (+ h (* 2 padding))
                         :name (fi/call-api :full-frame-title-raw fid)})]
     {:dispatch-n (into []
                        (comp
                         (map frame->slide)
                         (remove (position-occupied? (get-slides db)))
                         (map #(vector ::add-slide %)))
                        (vals frames))})))

(defn generate-id
  [db]
  {:frame-id (str "slide-" (random-uuid))
   :workspace-id (get-in db (path/workspace-id))
   :vertical "woco"})

(defn- sidebar-width [db] ; TODO: don't duplicate
  (if (get-in db path/sidebar)
    (get-in db path/navigation-bar-offset 0)
    0))

(defn- viewport-rectangle [db subtract-sidebar?]
  (let [{x :x y :y z :z} (position db)
        [x1 y1] [(/ x (- z)) (/ y (- z))]
        {w :width h :height} (workspace-rect)
        [w1 h1] [(/ w z) (/ h z)]]
    (cond-> {:x x1 :y y1 :w w1 :h h1}
      subtract-sidebar? (update :w - (/ (sidebar-width db) z)))))

(defn- get-swpan-coords
  [db]
  (let [{:keys [x y w h]} (viewport-rectangle db true)
        pad-x (* config/viewport-padding w)
        pad-y (* config/viewport-padding h)
        candidate {:x (+ x pad-x)
                   :y (+ y pad-y)
                   :w (- w (* pad-x 2))
                   :h (- h (* pad-y 2))}
        offset-slide #(-> %
                          (update :x + (* pad-x 0.5))
                          (update :y + (* pad-y 0.5)))
        slide-candidates (iterate offset-slide candidate)]
    (->> slide-candidates
         (remove (position-occupied? (get-slides db)))
         first)))

(defn- new-viewport-to-focus-slide [db s-coords]
  (let [vr (viewport-rectangle db false)
        {z :z} (position db)
        nvr vr
        vr (update vr :w -   (/ (sidebar-width db) z))
        ;if the right border of the slide is not visible, pan x-direction
        ox (if (<= (:x vr) (+ (:x s-coords) (:w s-coords)) (+ (:x vr) (:w vr)))
             0
             (- (:x s-coords) (:x vr)))
        ;if the top border of the slide is not visiible, pan y-direction
        oy (if (<= (:y vr)  (:y s-coords)  (+ (:y vr) (:h vr)))
             0
             (- (:y s-coords) (:y vr) (/ (- (:h vr) (:h s-coords)) 2)))]
    (-> nvr
        (update :x + ox)
        (update :y + oy))))

(defn- call-sync-event [db event]
  (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
    (sync-event-fn event)))

(re-frame/reg-event-fx
 ::add-slide
 [interaction-mode/ro-interceptor]
 (fn [{db :db} [_ slide suppress-log?]]
   (let [slide (cond-> slide
                 (not (:uid slide)) (assoc :uid (generate-id db))
                 (not (:name slide)) (assoc :name (str "Slide " (+ 1 (count (get-slides db))))))]
     (call-sync-event db [::sync-add-slide slide])
     (cond-> {:db (add-slide db slide)}
       (not suppress-log?) (update :dispatch-n into [[:de.explorama.frontend.woco.event-logging/log-event (:uid slide) "add-slide" slide]])))))

(re-frame/reg-event-db
 ::sync-add-slide
 (fn [db [_ slide]]
   (add-slide db slide)))

(defn get-new-slide-name [slide-str presentation]
  (let [pattern (map str
                     (repeat (str slide-str " "))
                     (iterate inc 1))
        name-occupied? (set (map :name presentation))]
    (->> pattern
         (remove name-occupied?)
         first)))

(re-frame/reg-event-fx
 ::spawn-new-slide
 [interaction-mode/ro-interceptor]
 (fn [{db :db} [_ uid]]
   (let [s-coords (get-swpan-coords db)
         name (get-new-slide-name (i18n/translate db :slide) (get-slides db))
         uid (or uid (generate-id db))
         nvr (new-viewport-to-focus-slide db s-coords)
         [x y z] (xywh->xyz nvr)]
     {:dispatch-n [[::add-slide (merge s-coords {:name name :uid uid})]
                   [::nav/animated-set-position [x y z]]]})))

(defn- calc-new-order [db from-idx to-idx]
  (let [slides (get-slides db)
        max-index (- (count slides) 1)]
    (when (and (<= 0 from-idx max-index) (<= 0 to-idx max-index))
      (let [si (get slides from-idx)
            middle (if (< from-idx to-idx)
                     (conj (subvec slides (inc from-idx) (inc to-idx)) si)
                     (into [si] (subvec slides to-idx from-idx)))
            [a b] [(min from-idx to-idx) (max from-idx to-idx)]
            lfront (subvec slides 0 a)
            ltail (subvec slides (inc b))]
        [si (into [] (concat lfront middle ltail))]))))

(re-frame/reg-event-fx
 ::change-slide-order
 [interaction-mode/ro-interceptor]
 (fn [{db :db} [_ from-idx to-idx suppress-log?]]
   (when-let [[si new-order] (calc-new-order db from-idx to-idx)]
     (call-sync-event db [::sync-slide-order from-idx to-idx])
     (cond-> {:db (set-slides db new-order)}
       (not suppress-log?) (assoc :dispatch [:de.explorama.frontend.woco.event-logging/log-event
                                             (:uid si)
                                             "change-slide-order"
                                             [from-idx to-idx]])))))

(re-frame/reg-event-db
 ::sync-slide-order
 (fn [db [_ from-idx to-idx]]
   (let [[_ new-order] (calc-new-order db from-idx to-idx)]
     (set-slides db new-order))))

(re-frame/reg-event-fx
 ::update-slide
 [interaction-mode/ro-interceptor]
 (fn [{db :db} [_ uid props suppress-log?]]
   (let [si (slide-by-uid db uid)
         slide-infos (get-slide db si)
         updated-slide-infos (merge slide-infos props)]
     (call-sync-event db [::sync-update-slide uid props])
     (cond-> {:db (change-slide db si updated-slide-infos)}
       (not suppress-log?) (assoc :dispatch [:de.explorama.frontend.woco.event-logging/log-event
                                             uid
                                             "update-slide"
                                             (dissoc updated-slide-infos :uid)])))))

(re-frame/reg-event-db
 ::sync-update-slide
 (fn [db [_ uid props]]
   (let [si (slide-by-uid db uid)
         slide-infos (get-slide db si)
         updated-slide-infos (merge slide-infos props)]
     (change-slide db si updated-slide-infos))))

(re-frame/reg-event-fx
 ::remove-slide-by-uid
 [interaction-mode/ro-interceptor]
 (fn [{db :db} [_ uid suppress-log?]]
   (call-sync-event db [::sync-remove-slide uid])
   (cond-> {:db (change-slide db (slide-by-uid db uid))}
     (not suppress-log?) (assoc :dispatch [:de.explorama.frontend.woco.event-logging/log-event
                                           uid
                                           "remove-slide"
                                           nil]))))

(re-frame/reg-event-db
 ::sync-remove-slide
 (fn [db [_ uid]]
   (change-slide db (slide-by-uid db uid))))

(re-frame/reg-event-fx
 ::remove-all-slides
 [interaction-mode/ro-interceptor]
 (fn [{db :db} _]
   (let [slides (get-slides db)]
     {:dispatch-n (mapv #(vector ::remove-slide-by-uid (:uid %)) slides)})))

(re-frame/reg-event-fx
 ::move-to-slide
 (fn [{db :db} [_ {:keys [uid]}]]
   (when-not (get-in db path/overlayer-active?)
     (let [index (or (slide-by-uid db uid) (get-current-slide db))
           s-coords (slide-coords db index)]
       #_(call-sync-event db [::sync-coord s-coords])
       {:dispatch [::nav/animated-set-position s-coords]}))))

(re-frame/reg-event-fx
 ::sync-coord
 (fn [_ [_ s-coords]]
   {:dispatch [::nav/animated-set-position s-coords]}))

(defn- switch-slide [db direction]
  (let [current-slide (get-current-slide db)
        n-slide (next-slide db current-slide direction)]
    (when-not (= current-slide n-slide)
      {:db (assoc-in db path/current-slide n-slide)
       :dispatch [::move-to-slide n-slide]})))

(re-frame/reg-event-fx
 ::switch-slide
 (fn [{db :db} [_ direction]]
   (call-sync-event db [::sync-switch-slide direction])
   (switch-slide db direction)))

(re-frame/reg-event-fx
 ::sync-switch-slide
 (fn [{db :db} [_ direction]]
   (switch-slide db direction)))

(re-frame/reg-sub
 ::slide-infos-by-index
 (fn [db [_ index]]
   (assoc (get-slide db index) :index index)))

(re-frame/reg-sub
 ::slide-infos-by-uid
 (fn [db [_ uid]]
   (let [index (slide-by-uid db uid)]
     (assoc (get-slide db index) :index index))))

(re-frame/reg-event-fx
 ::start-presentation
 (fn [{db :db} [_ skip-sync?]]
   (when-not skip-sync?
     (call-sync-event db [::start-presentation true]))
   {:db (assoc-in db path/current-slide 0)
    :dispatch-n
    [[:de.explorama.frontend.woco.frame.api/normalize]
     [::switch-mode :presenting]
     [::move-to-slide]]}))


(re-frame/reg-sub
 ::all-slide-ids
 :<- [::slides]
 (fn [slides _]
   (map :uid slides)))

(re-frame/reg-sub
 ::current-mode
 (fn [db _]
   (get-in db path/presentation-current-mode)))

(def prevent-keyhandling-for
  [js/HTMLInputElement js/HTMLTextAreaElement])

(re-frame/reg-event-fx
 ::bind-keys
 (fn [{db :db} _]
   (let [evt-key (get-in db path/presentation-key-handler)]
     (if-not evt-key
       {:db (assoc-in db path/presentation-key-handler
                      (events/listen js/window EventType.KEYDOWN
                                     (fn me [e] (let [k (aget e "key")]
                                                  (when-not (some #(instance? % (aget e "target")) prevent-keyhandling-for)
                                                    (condp apply [k]
                                                      config/presentation-next-slide-keys (re-frame/dispatch [::switch-slide 1])
                                                      config/presentation-prev-slide-keys (re-frame/dispatch [::switch-slide -1])
                                                      config/presentation-exit-keys (re-frame/dispatch [::switch-mode])
                                                      nil))))))}
       nil))))

(re-frame/reg-event-fx
 ::unbind-keys
 (fn [{db :db} _]
   (when-let [evt-key (get-in db path/presentation-key-handler)]
     (events/unlistenByKey evt-key)
     {:db (update-in db path/presentation-mode dissoc :key-handler)})))

;; Different modes to work in: presenting, editing, standard
;; leaving 'presenting' always enters the last used mode 
;; leaving 'editing' enters 'standard' mode
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(re-frame/reg-event-fx
 ::enter-mode
 (fn [{db :db} [_ mode]]
   (cond-> {:db (assoc-in db path/presentation-current-mode mode) :dispatch-n []}
     (= mode :presenting) (update :dispatch-n conj [::bind-keys])
     (= mode :editing) (update :dispatch-n conj [:de.explorama.frontend.woco.presentation.sidebar/open-window]))))

(re-frame/reg-event-fx
 ::exit-mode
 (fn [{db :db} _]
   (let [mode (get-in db path/presentation-current-mode :standard)]
     (cond-> {:db (assoc-in db path/presentation-last-mode mode) :dispatch-n []}
       (= mode :presenting) (update :dispatch-n conj [::unbind-keys])
       (= mode :editing) (update :dispatch-n conj [:de.explorama.frontend.woco.presentation.sidebar/hide-window])))))

(re-frame/reg-event-fx
 ::switch-mode
 (fn [{db :db} [_ new-mode]]
   (let [new-mode (or new-mode (get-in db path/presentation-last-mode))]
     {:dispatch-n [[::exit-mode] [::enter-mode new-mode]]})))

(defn- toggle-modes-fx [db mode other]
  (let [other (or other (get-in db path/presentation-last-mode))
        new-mode (if (= (get-in db path/presentation-current-mode) mode)
                   other
                   mode)]
    {:dispatch [::switch-mode new-mode]}))

(re-frame/reg-event-fx
 ::toggle-modes
 (fn [{db :db} [_ mode other]]
   (let [sync-event-fn (fi/call-api :service-target-db-get db :project-fns :event-sync)]
     (sync-event-fn [::toggle-modes-sync mode other])
     (toggle-modes-fx db mode other))))

(re-frame/reg-event-fx
 ::toggle-modes-sync
 (fn [{db :db} [_ mode other]]
   (toggle-modes-fx db mode other)))

(re-frame/reg-event-db
 ::clean-db
 (fn [db _]
   (update-in db path/root dissoc path/presentation-mode-key)))

(re-frame/reg-event-fx
 ::clean-workspace
 (fn [{db :db} _]
   {:dispatch-n [[::unbind-keys]
                 [::clean-db]]}))
