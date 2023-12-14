(ns de.explorama.frontend.mosaic.render.core
  (:require [clojure.set :as set]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.common.queue :as ddq]
            [de.explorama.frontend.mosaic.config :as gconfig]
            [de.explorama.frontend.mosaic.css :as gcss]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.data :as gdb]
            [de.explorama.frontend.mosaic.path :as gp]
            [de.explorama.frontend.mosaic.render.actions]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.common]
            [de.explorama.frontend.mosaic.render.draw.normal.cards :as grdnc]
            [de.explorama.frontend.mosaic.render.draw.normal.frames :as grdnf]
            [de.explorama.frontend.mosaic.render.draw.scatter.axes :as grdsa]
            [de.explorama.frontend.mosaic.render.draw.scatter.cards :as grdsc]
            [de.explorama.frontend.mosaic.render.draw.scatter.frames :as grdsf]
            [de.explorama.frontend.mosaic.render.draw.tree.cards :as grdtc]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.exit]
            [de.explorama.frontend.mosaic.render.parameter :as grp]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc] ; breaks the encapulation -> not good
            [de.explorama.frontend.mosaic.render.pixi.core :as p]
            [de.explorama.frontend.mosaic.render.pixi.db :refer [instances
                                                                 instances-headless]]
            [de.explorama.frontend.mosaic.render.tracks]
            [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.shared.mosaic.group-by-layout :as gbl]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [re-frame.core :as re-frame]
            [taoensso.timbre :refer [debug error]]
            [taoensso.tufte :as tufte]))

(defn- gen-forced-groups [events1 events2 by]
  (let [values (case by
                 "month"
                 (mapv #(if (> % 9) (str %) (str 0 %)) (range 1 13))
                 "year"
                 (let [all-years (js->clj (gdal/concat (gdal/mapv (fn [group]
                                                                    (js/parseInt (gdal/get (gdal/first group) "key")))
                                                                  (gdal/second events1))
                                                       (gdal/mapv (fn [group]
                                                                    (js/parseInt (gdal/get (gdal/first group) "key")))
                                                                  (gdal/second events2))))
                       min-year (apply min all-years)
                       max-year (apply max all-years)]
                   (->> (range min-year (inc max-year))
                        (mapv str)))
                 [])]
    (->> (map (fn [v] v) values)
         (sort-by (fn [{byv by}] byv)))))

(defn- update-group-size [group-sizes1 group-sizes2]
  (reduce (fn [acc [key v1]]
            (let [v2 (get group-sizes2 key)]
              (assoc acc
                     key
                     (cond (and v1 v2)
                           (into {}
                                 (map (fn [key]
                                        (if (= key :element-type)
                                          [key (get v1 key)]
                                          [key
                                           (max (get v2 key)
                                                (get v1 key))]))
                                      (keys v1)))
                           v1 v1
                           v2 v2
                           :else
                           {:element-type :leaf
                            :event-count 0
                            :event-count-max 0
                            :group-count 1
                            :group-count-max 1}))))
          {[] {:element-type :group
               :group-count (count group-sizes1)
               :group-count-max (count group-sizes1)
               :event-count (max (get-in group-sizes1 [[] :event-count])
                                 (get-in group-sizes2 [[] :event-count]))
               :event-count-children-max (max (get-in group-sizes1 [[] :event-count-children-max])
                                              (get-in group-sizes2 [[] :event-count-children-max]))}}
          group-sizes1))

(defn- inflate-events [by events groups]
  (let [evts (gdal/get events 1)
        new-events (loop [groups groups
                          evts (sort-by (fn [event] (gdal/get event "key")) evts)
                          result #js []]
                     (if (empty? groups)
                       result
                       (let [pair (first evts)
                             group (first groups)
                             group-same? (if pair
                                           (= group (gdal/get (gdal/first pair) "key"))
                                           nil)]
                         (if group-same?
                           (recur (rest groups)
                                  (rest evts)
                                  (gdal/conj result pair))
                           (recur (rest groups)
                                  evts
                                  (gdal/conj result #js[(clj->js {"key" group
                                                                  "attr" by
                                                                  by group
                                                                  "group-key?" true})
                                                        #js[]]))))))]
    #js [(gdal/get events 0)
         new-events]))

;state db frame-id1 {with :with by :by}
(defn- try-to-sync-couple-frame [{:keys [path state db] :as update-state}]
  (let [frame-id1 (gp/frame-id path)
        {with :with by :by} (get state :coupled)]
    (if-not (and frame-id1
                 (seq with)
                 (not (:headless state)))
      update-state
      (let [_ (when (< 1 (count with))
                (error "Couple does not support coupling with more then 1 frame"))
            frame-id2 (first with)
            synced1? (get-in db (gp/frame-couple-synced? frame-id1))
            synced2? (get-in db (gp/frame-couple-synced? frame-id2))
            group-sizes
            (when (not (or synced1?
                           synced2?))
              (let [events1 (gdb/get-events frame-id1)
                    events2 (gdb/get-events frame-id2)]
                (when (and events1
                           (gdal/vec? events1)
                           (= 2 (gdal/count events1))
                           (grp/children-group-key? (gdal/second events1))
                           events2
                           (gdal/vec? events2)
                           (= 2 (gdal/count events2))
                           (grp/children-group-key? (gdal/second events2)))
                  (let [groups (gen-forced-groups events1 events2 by)
                        inflated-events1 (inflate-events by events1 groups)
                        inflated-events2 (inflate-events by events2 groups)
                        group-sizes1 (grp/calc-leaf->root inflated-events1)
                        group-sizes2 (grp/calc-leaf->root inflated-events2)]
                    (gdb/set-events! frame-id1 inflated-events1)
                    (gdb/set-events! frame-id2 inflated-events2)
                    (when (and group-sizes1
                               group-sizes2)
                      (update-group-size group-sizes1 group-sizes2))))))
            hash-group (hash group-sizes)]
        (if (and (not synced1?)
                 (not synced2?)
                 (seq group-sizes))
          (-> update-state
              (update :dispatch-n conj [:de.explorama.frontend.mosaic.operations.tasks/execute-wrapper frame-id1 :sync-coupled {:sync-id hash-group}
                                        :group-sizes group-sizes
                                        :by by])
              (update :dispatch-n conj [:de.explorama.frontend.mosaic.operations.tasks/execute-wrapper frame-id2 :sync-coupled {:sync-id hash-group}
                                        :group-sizes group-sizes
                                        :by by]))
          update-state)))))

(defn- coupled-by [state op-desc]
  (if-let [coupled (get op-desc gcp/coupled-key)]
    (let [{:keys [with by max-levels]} coupled]
      (assoc state :coupled (cond-> {}
                              with (assoc :with with)
                              by (assoc :by by)
                              max-levels (assoc :max-levels max-levels))))
    state))

(defn- adjust-one-row [op-desc group-sizes]
  (if (get op-desc gcp/coupled-key)
    [:one-row group-sizes]
    [false nil]))

(defn- legacy-layout-fallbacks [field-assignments]
  (let [field-assignments-num (count field-assignments)]
    (cond (#{4 6 8} field-assignments-num)
          field-assignments
          (< 8 field-assignments-num)
          (vec (take 8 field-assignments))
          :else
          (let [target (cond (< field-assignments-num 4)
                             4
                             (< field-assignments-num 6)
                             6
                             (< field-assignments-num 8)
                             8)]
            (apply conj
                   field-assignments
                   (map (fn [_]
                          ["else" "country"])
                        (range 0 (- target field-assignments-num))))))))

(defn- transform-selected-layouts [layouts]
  (into {"###fallback-layout"
         [["else" "date"]
          ["else" "datasource"]
          ["location" "country"]
          ["notes" "notes"]]}
        (map (fn [{:keys [field-assignments id]}]
               (if (#{4 6 8} (count field-assignments))
                 [id field-assignments]
                 [id (legacy-layout-fallbacks field-assignments)]))
             layouts)))

(defn update-gre! [{:keys [instance path state args move-to! rerender? resize destroy? reset?]}]
  (tufte/profile
   {:when gconfig/timelog-tubes?}
   (tufte/p
    ::update-gre!
    (if destroy?
      (do
        (gre/destroy instance)
        (swap! instances dissoc (gp/top-level path))
        (swap! instances-headless (fn [val]
                                    (as-> (update val (gp/frame-id path)
                                                  dissoc
                                                  (gp/top-level path)) $
                                      (if (empty? (get $ (gp/frame-id path)))
                                        (dissoc val (gp/frame-id path))
                                        val)))))
      (do
        (tufte/p ::set-state! (gre/set-state! instance state))
        (tufte/p ::set-args! (gre/set-args! instance args))
        (when rerender?
          (tufte/p ::rerender (gre/rerender instance pc/main-stage-index)))
        (when reset?
          (tufte/p ::reset (gre/reset instance pc/main-stage-index)))
        (when resize
          (tufte/p ::resize (apply gre/resize instance resize)))
        (when move-to!
          (tufte/p ::move-to! (apply gre/move-to! instance pc/main-stage-index move-to!)))
        (tufte/p ::update-zoom! (gre/update-zoom instance pc/main-stage-index)))))))

(re-frame/reg-fx ::gre update-gre!)

(defn dissoc-updates [{:keys [state path db updated? destroy? done] :as update-state}]
  (let [update-state-new (assoc update-state
                                :db
                                (update-in db
                                           (gp/updates path)
                                           (fn [val]
                                             (apply dissoc val done))))]
    (cond (and (or (= :new (get-in update-state-new [:desc :status]))
                   updated?)
               (not (:headless state)))
          update-state-new
          (and (= :new (get-in update-state-new [:desc :status]))
               updated?
               (:headless state))
          update-state-new
          destroy?
          (dissoc update-state :dispatch-later)
          :else
          (dissoc update-state :dispatch-later ::gre :dispatch-n))))

(re-frame/reg-event-fx
 ::highlight-instance
 (fn [{db :db}
      [_
       path
       task-id]]
   (let [instance (get @instances (gp/top-level path))
         {:keys [current last-action] {source-frame-id :frame-id} :source-infos} (get-in db (gp/selections path))]
     (when (and (not= source-frame-id (gp/frame-id path))
                (= :raster (get-in (gre/state instance) [:contexts pc/main-stage-index [] :render-type])))
       (let [highlights (->> current (map #(get % "id")) set)
             old-highlights (get (gre/state instance) :highlights)]
         (when (not= old-highlights highlights)
           (gre/assoc-state! instance [:highlights (->> current (map #(get % "id")) set)])
           (gre/update-highlights instance {:move-to (when (= :select last-action) (get (peek current) "id"))
                                            :select? (< (old-highlights highlights)
                                                        (count highlights))}))))
     (when task-id
       {:dispatch [::ddq/finish-task (gp/frame-id path) task-id ::highlight-instance]}))))

(re-frame/reg-event-fx
 ::focus-event
 (fn [_ [_ frame-id event-id]]
   (gre/focus-event (get @instances (gp/top-level frame-id))
                    event-id)
   {}))

(re-frame/reg-event-db
 ::canvas-running
 (fn [db [_ path]]
   (let [canvas (get-in db path)]
     (if (= :new (:status canvas))
       (assoc-in db (conj path :status) :running)
       db))))

(defn exit-instance [{:keys [path desc] :as update-state}]
  (if (:exit? desc)
    (do
      (debug "Render EXIT" path)
      (-> (update update-state
                  :dispatch-n
                  into
                  (cond-> [[:de.explorama.frontend.mosaic.render.tracks/dispose-data-acs-track path]
                           [:de.explorama.frontend.mosaic.render.tracks/dispose-theme-track path]
                           [:de.explorama.frontend.mosaic.render.tracks/dispose-track path]]
                    (:exit-pixi? desc)
                    (conj [:de.explorama.frontend.mosaic.render.exit/ready-for-exit-pixi path])
                    (not (:exit-pixi? desc))
                    (conj [:de.explorama.frontend.mosaic.render.exit/ready-for-exit path])))
          (assoc :destroy? true)))
    update-state))

(defn- default-zoom [contexts]
  (let [{{:keys [count-ctn max-zoom min-zoom]} :params, ctx-type :ctx-type}
        (get contexts [])
        singleton? (and (= 1 count-ctn)
                        (not= :group ctx-type))]
    (if singleton? min-zoom max-zoom)))



(defn update-di [{:keys [desc di path state db] :as update-state}]
  (if (or (get-in desc [:updates :filter?])
          (get-in desc [:updates :reconnect?]))
    (let [selected-layouts (get-in db (gp/selected-layouts (gp/frame-id path)))
          op-desc (get-in db (gp/operation-desc path))
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts
          (grp/grp-contexts (gdb/get-events (gp/canvas path))
                            (gdb/get-scale (gp/canvas path))
                            op-desc
                            state
                            desc
                            (adjust-one-row op-desc nil)
                            (gbl/build-layout-lookup-table
                             selected-layouts)
                            attribute-labels
                            lang)
          layouts (gcss/current-layout-sub db path)
          state (-> state
                    (coupled-by op-desc)
                    (assoc-in [:contexts pc/main-stage-index] contexts)
                    (assoc :di di :clayouts (:names layouts) :layouts (transform-selected-layouts selected-layouts)))]
      (-> update-state
          (assoc :state state
                 :move-to! [0 0 (default-zoom contexts)]
                 :reset? true
                 :reset-highlights? true)
          (update :done conj :filter? :reconnect?)
          (assoc :updated? true)))
    update-state))

(def update-data-actions #{:filter? :sort? :ungroup? :group? :coupled? :set? :group-sort? :group-remove?})

(defn update-data [{:keys [di path state db] {:keys [updates] :as desc} :desc :as update-state}]
  (if (and (or (= di (:di state))
               updates)
           (not-empty (set/intersection (set (keys updates))
                                        update-data-actions)))
    (let [op-desc (get-in db (gp/operation-desc path))
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts
          (grp/grp-contexts (gdb/get-events (gp/canvas path))
                            (gdb/get-scale (gp/canvas path))
                            (get-in db (gp/operation-desc path))
                            state
                            desc
                            (adjust-one-row op-desc nil)
                            (gbl/build-layout-lookup-table
                             (get-in db
                                     (gp/selected-layouts (gp/frame-id path))))
                            attribute-labels
                            lang)
          state (-> state
                    (coupled-by op-desc)
                    (assoc-in [:contexts pc/main-stage-index] contexts)
                    (assoc :di di))]
      (-> update-state
          (assoc :state state
                 :move-to! [0 0 (default-zoom contexts)]
                 :reset? true
                 :updated? true)
          (update :done into update-data-actions)))
    update-state))

(defn resize [{:keys [state db]
               {:keys [width height] :as desc} :desc
               :as update-state}]
  (if (get-in desc [:updates :resize?])
    (let [{:keys [path]} state
          coupled? (get-in db (conj (gp/operation-desc path) gcp/coupled-key))]
      (-> (cond (js/Number.isNaN (get-in state [:contexts pc/main-stage-index [] :factor-overview]))
                (let [{:keys [x y z]} (get-in state [[:pos pc/main-stage-index]])
                      op-desc (get-in db (gp/operation-desc path))
                      attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
                      lang (i18n/current-language db)
                      contexts
                      (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                        (gdb/get-scale (gp/canvas path))
                                        (get-in db (gp/operation-desc path))
                                        state
                                        desc
                                        (or (get-in db (gp/canvas-state-replay (gp/frame-id path)))
                                            (adjust-one-row op-desc nil))
                                        (gbl/build-layout-lookup-table
                                         (get-in db
                                                 (gp/selected-layouts (gp/frame-id path))))
                                        attribute-labels
                                        lang)
                      {:keys [max-zoom bb-min-x bb-min-y]}
                      (get-in contexts [[] :params])
                      [bb-min-x
                       bb-min-y
                       max-zoom]
                      [(- (* bb-min-x max-zoom))
                       (- (* bb-min-y max-zoom))
                       max-zoom]]
                  (cond-> update-state
                    (or (js/Number.isNaN x)
                        (js/Number.isNaN y)
                        (js/Number.isNaN z))
                    (assoc :move-to! [bb-min-x bb-min-y max-zoom]
                           :reset? true)
                    :always
                    (assoc :state (-> (assoc-in state [:contexts pc/main-stage-index] contexts)
                                      (coupled-by op-desc))
                           :rerender? true)))
                (and (= (get-in state [[:pos pc/main-stage-index] :zoom]) 0)
                     (not coupled?))
                (let [op-desc (get-in db (gp/operation-desc path))
                      attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
                      lang (i18n/current-language db)
                      contexts
                      (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                        (gdb/get-scale (gp/canvas path))
                                        (get-in db (gp/operation-desc path))
                                        state
                                        desc
                                        (or (get-in db (gp/canvas-state-replay (gp/frame-id path)))
                                            (adjust-one-row op-desc nil))
                                        (gbl/build-layout-lookup-table
                                         (get-in db
                                                 (gp/selected-layouts (gp/frame-id path))))
                                        attribute-labels
                                        lang)
                      {:keys [max-zoom bb-min-x bb-min-y]}
                      (get-in contexts [[] :params])
                      [bb-min-x
                       bb-min-y
                       max-zoom]
                      [(- (* bb-min-x max-zoom))
                       (- (* bb-min-y max-zoom))
                       max-zoom]]
                  (-> update-state
                      (assoc :state (-> (assoc-in state [:contexts pc/main-stage-index] contexts)
                                        (coupled-by op-desc)))
                      (assoc :move-to! [bb-min-x bb-min-y max-zoom]
                             :reset? true)))
                :else
                (let [context (grp/update-bb (get-in state [:contexts pc/main-stage-index []])
                                             width height
                                             (:constraints state))]
                  (assoc update-state :state (assoc-in state [:contexts pc/main-stage-index []] context))))
          (assoc :resize [width height])
          (update :done conj :resize?)
          (assoc :updated? true)))
    update-state))

(defn pan-and-zoom [{:keys [state desc] :as update-state}]
  (if (get-in desc [:updates :pan-and-zoom?])
    (let [headless? (get state :headless)
          {:keys [x y z next-zoom]} (get-in desc [:updates :pan-and-zoom? :params])]
      (-> update-state
          (assoc :state (cond-> (assoc state :selection-dont-zoom? true)
                          headless?
                          (update [:pos pc/main-stage-index]
                                  assoc
                                  :x x :y y :z z
                                  :zoom next-zoom
                                  :next-zoom next-zoom)))
          (cond-> (not headless?)
            (assoc :move-to! [x y z next-zoom]))
          (update :done conj :pan-and-zoom?)
          (assoc :updated? true)))
    update-state))

(defn reset [{:keys [state path db] desc :desc :as update-state}]
  (if (get-in desc [:updates :reset?])
    (let [op-desc (get-in db (gp/operation-desc path))
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts
          (grp/grp-contexts (gdb/get-events (gp/canvas path))
                            (gdb/get-scale (gp/canvas path))
                            (get-in db (gp/operation-desc path))
                            state
                            desc
                            (adjust-one-row op-desc nil)
                            (gbl/build-layout-lookup-table
                             (get-in db
                                     (gp/selected-layouts (gp/frame-id path))))
                            attribute-labels
                            lang)
          {:keys [max-zoom bb-min-x bb-min-y]}
          (get-in contexts [[] :params])
          [bb-min-x
           bb-min-y
           max-zoom]
          [(- (* bb-min-x max-zoom))
           (- (* bb-min-y max-zoom))
           max-zoom]]
      (-> update-state
          (assoc :state (-> (assoc-in state [:contexts pc/main-stage-index] contexts)
                            (coupled-by op-desc)))
          (assoc :move-to! [bb-min-x bb-min-y max-zoom]
                 :reset? true)
          (update :done conj :reset?)
          (assoc :updated? true)))
    update-state))

(defn render-mode [{:keys [state path db data-acs?] desc :desc :as update-state}]
  (if (or (get-in desc [:updates :scatter-plot?])
          (get-in desc [:updates :grid?]))
    (if data-acs?
      (let [op-desc (get-in db (gp/operation-desc path))
            attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
            lang (i18n/current-language db)
            contexts
            (grp/grp-contexts (gdb/get-events (gp/canvas path))
                              (gdb/get-scale (gp/canvas path))
                              (get-in db (gp/operation-desc path))
                              state
                              desc
                              (adjust-one-row op-desc nil)
                              (gbl/build-layout-lookup-table
                               (get-in db
                                       (gp/selected-layouts (gp/frame-id path))))
                              attribute-labels
                              lang)
            {{:keys [max-zoom bb-min-x bb-min-y]} :params}
            (get-in contexts [pc/main-stage-index []])
            scatter-plot-ignored-events (get-in contexts [pc/main-stage-index [] :optional-desc :ignored] 0)]
        (-> update-state
            (assoc :state (-> (assoc-in state [:contexts pc/main-stage-index] contexts)
                              (coupled-by op-desc)))
            (assoc :move-to! [bb-min-x
                              bb-min-y
                              max-zoom]
                   :reset? true)
            (update :done conj :scatter-plot? :grid?)
            (assoc :updated? true)
            (assoc-in (cons :db (gp/scatter-plot-ignored-events path))
                      scatter-plot-ignored-events)))
      (assoc update-state :updated? false))
    update-state))

(defn adjust [{:keys [state path db] desc :desc :as update-state} query key]
  (if (get-in desc [:updates query])
    (let [{:keys [z zoom]} (get state [:pos pc/main-stage-index])
          z (if (zero? zoom)
              (* z (get-in state [:contexts pc/main-stage-index [] :factor-overview]))
              z)
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                     (gdb/get-scale (gp/canvas path))
                                     (get-in db (gp/operation-desc path))
                                     state
                                     desc
                                     [key nil]
                                     (gbl/build-layout-lookup-table
                                      (get-in db
                                              (gp/selected-layouts (gp/frame-id path))))
                                     attribute-labels
                                     lang)]
      (-> update-state
          (assoc :state (assoc-in state [:contexts pc/main-stage-index] contexts))
          (assoc :move-to! [0 0 z]
                 :rerender? true)
          (assoc :updated? true)
          (update :done conj query)
          (update :dispatch-n (fn [val]
                                (conj
                                 val
                                 [:de.explorama.frontend.mosaic.event-logging/log-event (gp/frame-id path)
                                  "adjust"
                                  {:path path
                                   :query query
                                   :key key
                                   :cpl (get-in contexts [[] :params :cpl-ctn])
                                   :zoom zoom}])))))
    update-state))

(defn layout [{desc :desc :as update-state}]
  (if (get-in desc [:updates :layout?])
    (-> update-state
        (assoc :rerender? true)
        (update :done conj :layout?)
        (assoc :updated? true))
    update-state))

(defn adjust-replay [{:keys [state path db] {:keys [width height] :as desc} :desc :as update-state}]
  (if (get-in desc [:updates :adjust-replay?])
    (let [replay-desc (get-in desc [:updates :adjust-replay? :params])
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                     (gdb/get-scale (gp/canvas path))
                                     (get-in db (gp/operation-desc path))
                                     state
                                     desc
                                     [(:key replay-desc) nil]
                                     (gbl/build-layout-lookup-table
                                      (get-in db
                                              (gp/selected-layouts (gp/frame-id path))))
                                     attribute-labels
                                     lang)
          {{width-ctn :width height-ctn :height} :params factor-overview :factor-overview} (get contexts [])
          z (max (/ width width-ctn)
                 (/ height height-ctn))]
      (cond-> update-state
        (and replay-desc
             (:key replay-desc)
             (:zoom replay-desc))
        (-> (assoc :state (-> (assoc-in state
                                        [:contexts pc/main-stage-index]
                                        contexts)
                              (update [:pos pc/main-stage-index]
                                      assoc
                                      :z (if (zero? (:zoom replay-desc))
                                           (/ z factor-overview)
                                           z)
                                      :zoom (:zoom replay-desc))))
            (assoc :rerender? true)
            (update :done conj :adjust-replay?)
            (assoc :updated? true))))
    update-state))

(defn sync-couple [{:keys [state path db desc] :as update-state}]
  (if (get-in desc [:updates :sync-couple])
    (let [group-sizes (get-in desc [:updates :sync-couple :params])
          op-desc (get-in db (gp/operation-desc path))
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                     (gdb/get-scale (gp/canvas path))
                                     (get-in db (gp/operation-desc path))
                                     state
                                     desc
                                     (adjust-one-row op-desc group-sizes)
                                     (gbl/build-layout-lookup-table
                                      (get-in db
                                              (gp/selected-layouts (gp/frame-id path))))
                                     attribute-labels
                                     lang)]
      (-> update-state
          (assoc :state (-> (assoc-in state [:contexts pc/main-stage-index] contexts)
                            (coupled-by op-desc))
                 :move-to! [0 0 (default-zoom contexts)]
                 :reset? true
                 :updated? true)
          (update :done conj :sync-couple)))
    update-state))

(defn rerender [{:keys [state path db desc] :as update-state}]
  (if (get-in desc [:updates :rerender?])
    (let [op-desc (get-in db (gp/operation-desc path))
          attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
          lang (i18n/current-language db)
          contexts (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                     (gdb/get-scale (gp/canvas path))
                                     (get-in db (gp/operation-desc path))
                                     state
                                     desc
                                     (adjust-one-row op-desc nil)
                                     (gbl/build-layout-lookup-table
                                      (get-in db
                                              (gp/selected-layouts (gp/frame-id path))))
                                     attribute-labels
                                     lang)]
      (-> update-state
          (assoc :state (-> (assoc-in state [:contexts pc/main-stage-index] contexts)
                            (coupled-by op-desc))
                 :rerender? true
                 :updated? true)
          (update :done conj :rerender?)))
    update-state))

(defn update-theme [{:keys [instance state] :as update-state}]
  (let [{:keys [theme]} state
        new-theme @(fi/call-api :config-theme-sub)
        theme-changed? (not= theme new-theme)
        color (if (= :dark new-theme) 0x1B1C1E 0xFFFFFF)]
    (when theme-changed?
      (gre/update-theme instance color))
    (cond-> update-state
      :always
      (assoc :state (assoc state :theme new-theme))
      theme-changed?
      (assoc :rerender? true
             :updated? true))))

(defn finish-tasks [{:keys [done path] :as update-state}]
  (vec (vals (reduce (fn [acc task-done]
                       (let [task-id (get-in update-state [:desc :updates task-done :task-id])]
                         (if task-id
                           (assoc acc
                                  task-id
                                  {:ms 1
                                   :dispatch [::ddq/finish-task (gp/frame-id path) task-id [::update2 done]]})
                           acc)))
                     {}
                     done))))

(defn finish-update [update-state]
  (-> (assoc update-state ::gre update-state)
      (assoc :dispatch-later (finish-tasks update-state))
      dissoc-updates))

(defn update-functions [update-state]
  (-> update-state
      exit-instance
      update-theme
      rerender
      resize
      (adjust :adjust-horizontal? :horizontal)
      (adjust :adjust-one-row? :one-row)
      (adjust :adjust-vertical? :vertical)
      adjust-replay
      render-mode
      layout
      reset
      pan-and-zoom
      update-di
      update-data
      sync-couple
      try-to-sync-couple-frame
      ;Do not remove this line
      finish-update))

(re-frame/reg-event-fx
 ::update
 (fn [{db :db} [_ path]]
   (if-let [instance (get @instances (gp/top-level path))]
     (let [render? (fi/call-api [:interaction-mode :render-db-get?] db)
           desc (get-in db path)
           di (get-in db (gp/data-instance path))
           data-acs? (boolean (get-in db (gp/data-acs path)))
           _ (debug "Render UPDATE STARTED" path (:updates desc))
           update-state
           (update-functions
            {:db db
             :render? render?
             :desc desc
             :di di
             :path path
             :done #{}
             :data-acs? data-acs?
             :instance instance
             :state (gre/state instance)
             :args (gre/args instance)
             :dispatch-n []})]
       (debug "Render UPDATE DONE" path (:done update-state) (:updated? update-state))
       (select-keys update-state
                    [:dispatch-n :db ::gre :dispatch-later :fx]))
     (do
       (error "Render UPDATE ERROR no instance found for path:" path)
       {}))))

(defn constraints-desc
  ([path]
   (let [card-margin 30
         card-height 500
         card-width 500]
     (constraints-desc path card-height card-width card-margin)))
  ([_ card-height card-width card-margin]
   {:card-height card-height
    :card-width card-width
    :card-margin card-margin
    :margin-grp 100
    :border-grp 5}))

(defn init [db path headless? callback _]
  (let [current-layout @(re-frame/subscribe [:de.explorama.frontend.mosaic.css/current-layout path])
        ;Works as long changes on access-writes will not synchronized while project is loaded
        pending-read-only? @(fi/call-api [:interaction-mode :pending-read-only-sub?] db)
        interaction-mode @(fi/call-api [:interaction-mode :current-sub?] db {:frame-id (gp/frame-id path)})
        task-id (get-in db (conj (gp/canvas path) :init-task-id))
        op-desc (get-in db (gp/operation-desc path))
        {:keys [current]} (get-in db (gp/selections path))
        di (get-in db (gp/data-instance path))
        layouts (get-in db (gp/selected-layouts (gp/frame-id path)))
        desc
        (-> (get-in db (gp/canvas path))
            (assoc :container-header-id
                   (get-in db (conj (gp/top-level path)
                                    :container-header-id))))
        label-dict (fi/call-api [:i18n :get-labels-db-get] db)
        attribute-labels (fi/call-api [:i18n :get-labels-db-get] db)
        lang (i18n/current-language db)
        contexts (grp/grp-contexts (gdb/get-events (gp/canvas path))
                                   (gdb/get-scale (gp/canvas path))
                                   (get-in db (gp/operation-desc path))
                                   {:constraints (constraints-desc path)
                                    :di di
                                    :path path}
                                   desc
                                   (adjust-one-row op-desc nil)
                                   (gbl/build-layout-lookup-table layouts)
                                   attribute-labels
                                   lang)
        cur-theme @(fi/call-api :config-theme-sub)
        state (-> {:constraints (constraints-desc path)
                   :highlights (->> current (map #(get % "id")) set)
                   :clayouts (:names current-layout)
                   :path path
                   :di di
                   [:pos pc/main-stage-index] {:x 0
                                               :y 0
                                               :z 0
                                               :zoom 0
                                               :next-zoom 0}
                   :headless headless?
                   :init-pending-read-only? pending-read-only?
                   :init-interaction-mode interaction-mode
                   :layouts (transform-selected-layouts layouts)
                   :label-dict label-dict
                   :contexts {}
                   :background-color (if (= :dark cur-theme) 0x1B1C1E 0xFFFFFF)}
                  (coupled-by op-desc))
        instance (p/init desc
                         (assoc-in state
                                   [:contexts pc/main-stage-index]
                                   contexts)
                         {:raster {:cards {:static {0 grdnc/render-base-static
                                                    1 (partial grdnc/draw-card-static 1)
                                                    2 (partial grdnc/draw-card-static 2)
                                                    3 (partial grdnc/draw-card-static 3)}
                                           :load-screen grdnc/draw-card-loadscreen}
                                   :frames {:static {0 (partial grdnf/render-container 0)
                                                     1 (partial grdnf/render-container 1)}
                                            :dynamic {0 (partial grdnf/render-text 0)
                                                      1 (partial grdnf/render-text 1)}}
                                   :translate-data (fn [[_ id bucket]]
                                                     [bucket id])
                                   :annotations-0 grdnc/render-annotations-0
                                   :highlights-0 grdnc/render-highlights-0
                                   :relevant-annotations grdnc/relevant-annotations
                                   :relevant-highlights grdnc/relevant-highlights
                                   :coords-highlight grdnc/coords-highlight
                                   :data grdnc/get-data
                                   :index grdnc/index
                                   :coords grdnc/coords}
                          :scatter {:cards {:static {0 grdsc/render-base-static
                                                     1 (partial grdsc/draw-card-static 1)
                                                     2 (partial grdsc/draw-card-static 2)
                                                     3 (partial grdsc/draw-card-static 3)}
                                            :load-screen grdsc/draw-card-loadscreen}
                                    :frames {:static {0 (partial grdsf/render-container 0)
                                                      1 (partial grdsf/render-container 1)}
                                             :dynamic {0 (fn [& _]) #_(partial grdsf/render-text 0)
                                                       1 (fn [& _]) #_(partial grdsf/render-text 1)}}
                                    :translate-data (fn [[[_ id bucket]]]
                                                      [bucket id])
                                    :annotations-0 grdsc/render-annotations-0
                                    :highlights-0 grdsc/render-highlights-0
                                    :relevant-annotations grdsc/relevant-annotations
                                    :relevant-highlights grdsc/relevant-highlights
                                    :coords-highlight grdsc/coords-highlight
                                    :axes grdsa/draw-axes
                                    :data grdsc/get-data
                                    :index grdsc/index
                                    :coords grdnc/coords}
                          :treemap {:cards {:static {0 grdtc/render-base-static
                                                     1 (fn [& _])
                                                     2 (fn [& _])
                                                     3 (fn [& _])}
                                            :load-screen grdnc/draw-card-loadscreen}
                                    :frames {:static {0 (fn [& _])
                                                      1 (fn [& _])}
                                             :dynamic {0 (fn [& _])
                                                       1 (fn [& _])}}
                                    :translate-data (fn [[_ id bucket]]
                                                      [bucket id])
                                    :annotations-0 (fn [& _])
                                    :highlights-0 (fn [& _])
                                    :relevant-annotations grdnc/relevant-annotations
                                    :relevant-highlights grdnc/relevant-highlights
                                    :coords-highlight grdnc/coords-highlight
                                    :data grdnc/get-data
                                    :index grdnc/index
                                    :coords grdnc/coords}}
                         (gp/frame-id path))]
    (swap! instances assoc (gp/top-level path) instance)
    (swap! instances-headless assoc-in [(gp/frame-id path) (gp/top-level path)] true)
    {:dispatch-n (cond-> [[::ddq/finish-task (gp/frame-id path) task-id ::init]
                          [:de.explorama.frontend.mosaic.render.tracks/reg-data-acs-track path]
                          [:de.explorama.frontend.mosaic.render.tracks/reg-theme-track path]
                          [:de.explorama.frontend.mosaic.render.tracks/reg-track path]]
                   (not headless?)
                   (conj [::canvas-running path])
                   callback
                   (conj callback))}))

(re-frame/reg-event-fx
 ::init
 (fn [{db :db} [_ path]]
   (let [render? (fi/call-api [:interaction-mode :render-db-get?] db)
         instance (get @instances (gp/top-level path))]
     (debug "Render INIT " path "-" render?)
     (if (and (not instance)
              render?)
       (init db path false nil render?)
       (let [desc
             (-> (get-in db (gp/canvas path))
                 (assoc :container-header-id
                        (get-in db (conj (gp/top-level path)
                                         :container-header-id))))
             state (gre/state instance)
             context
             (get-in state [:contexts pc/main-stage-index []])]
         (gre/set-args! instance desc)
         (gre/assoc-state! instance [:headless false])
         (p/init-engine instance)
         (swap! instances-headless assoc-in [(gp/frame-id path) (gp/top-level path)] false)
         (if (every? (fn [[_ value]] (not value))
                     (get @instances-headless
                          [(gp/frame-id path) (gp/top-level path)]))
           {:dispatch-n (cond-> [[::canvas-running path]]
                          (= (get context :render-type)
                             :scatter)
                          (conj [:de.explorama.frontend.mosaic.render.actions/update path :reset? nil]))}
           (if (= (get context :render-type)
                  :scatter)
             {:dispatch [:de.explorama.frontend.mosaic.render.actions/update path :reset? nil]}
             {})))))))

(re-frame/reg-event-fx
 ::init-headless
 (fn [{db :db} [_ path callback]]
   (let [render? (fi/call-api [:interaction-mode :render-db-get?] db)]
     (if (and (get-in db path)
              (not (get @instances (gp/top-level path)))
              (not render?))
       (init db path true callback render?)
       (do (debug "Render INIT HEADLESS " path "-" render?)
           {:dispatch-n [(when callback
                           callback)]})))))

(defn store-card [_ [_ app-state-path stage-key events]]
  (grc/set-events (gp/frame-id app-state-path)
                  (into {} (map (fn [[key val]]
                                  [key (clj->js val)])
                                events)))
  (when-let [instance (get @instances (gp/top-level app-state-path))]
    (gre/update-zoom instance stage-key)))

(re-frame/reg-event-fx ws-api/get-events-result store-card)
