(ns de.explorama.frontend.woco.workspace.rearrange
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.size-position :refer [set-frame-position]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.workspace.sticky :refer [calc-sticky-offsets
                                                        set-sticky-frames-positions set-sticky-frames-positions-db]]
            [clojure.set :refer [difference]]
            [de.explorama.frontend.woco.workspace.math :refer [frame-bounding-box collide-rects]]
            [de.explorama.frontend.woco.workspace.states :as wws]))

(defn- calc-cords-down [start-x start-y i space {heights :heights}]
  [start-x
   (+ start-y
      (* i space)
      (reduce + (map heights (range i))))])

(defn- calc-cords-right [start-x start-y i space {widths :widths}]
  [(+ start-x
      (* i space)
      (reduce + (map widths (range i))))
   start-y])

(defn frame-size-pos-map [frames frames-order]
  (reduce (fn [acc [col-idx col]]
            (reduce (fn [acc [row-idx fid]]
                      (-> (assoc-in acc [:frame-pos-map fid] [col-idx row-idx])
                          (update-in [:coll-width col-idx] max (get-in frames [fid :full-size 0] 0))
                          (update-in [:row-height row-idx] max (get-in frames [fid :full-size 1] 0))))
                    acc
                    (map-indexed vector col)))
          {}
          (map-indexed vector frames-order)))

(defn- frame-size-pos-map-one-line [frames frames-order]
  (reduce (fn [acc [idx fid]]
            (-> acc
                (update-in [:widths idx] max (get-in frames [fid :full-size 0] 0))
                (update-in [:heights idx] max (get-in frames [fid :full-size 1] 0))))
          {}
          (map-indexed vector frames-order)))

(defn calc-cords [x y width offset {:keys [coll-width row-height frame-pos-map]} fid]
  (let [[col row] (get frame-pos-map fid)]
    [(+ x
        width
        (* (inc col) offset)
        (reduce + (map coll-width (range col))))
     (+ y
        (* row offset)
        (reduce + (map row-height (range row))))]))

(defn reorder [db publishing-frame-id frames-order frames x y width offset
               {frame-pos-map :frame-pos-map :as frame-size-pos-map}]
  (let [sticky-offsets (calc-sticky-offsets db frames)
        relevant-frames (filter (fn [[fid desc]]
                                  (and (= publishing-frame-id (:published-by-frame desc))
                                       (get frame-pos-map fid)))
                                frames)]
    [(-> (merge frames
                (into {}
                      (map (fn [[fid desc]]
                             [fid
                              (let [coords (calc-cords x y width offset frame-size-pos-map fid)]
                                (set-frame-position fid coords)
                                (assoc desc :coords coords))])
                           relevant-frames)))
         (set-sticky-frames-positions sticky-offsets))
     (mapv (fn [[fid]]
             [:de.explorama.frontend.woco.event-logging/log-frame-event fid])
           relevant-frames)]))

(defn- position-handling-down-right-event [calc-cords- {db :db} _]
  (let [space 100
        start-x (or (:min-x @wws/multiselect-bb)
                    (:start-min-x @wws/multiselect-bb))
        start-y (or (:min-y @wws/multiselect-bb)
                    (:start-min-y @wws/multiselect-bb))
        selected-frames-ids @wws/multiselect-current-selection
        selected-frames (select-keys (get-in db path/frames)
                                     selected-frames-ids)
        frame-order (sort-by (fn [fid]
                               (get-in selected-frames [fid :creation-date]))
                             selected-frames-ids)
        frame-size-pos-map (frame-size-pos-map-one-line selected-frames frame-order)
        sticky-offsets (calc-sticky-offsets db selected-frames)
        i (atom 0)
        db (-> (reduce (fn [db fid]
                         (if (get sticky-offsets fid)
                           db
                           (let [new-coords (calc-cords- start-x start-y @i space frame-size-pos-map)]
                             (swap! i inc)
                             (set-frame-position fid new-coords)
                             (assoc-in db (conj (path/frame-desc fid) :coords) new-coords))))
                       db
                       frame-order)
               (set-sticky-frames-positions-db sticky-offsets))
        new-frames (select-keys (get-in db path/frames)
                                selected-frames-ids)]
    (reset! wws/multiselect-bb (frame-bounding-box new-frames))
    {:db db
     :fx (mapv (fn [fid]
                 [:dispatch [:de.explorama.frontend.woco.frame.interaction.move/move-frame fid (get-in db (conj (path/frame-desc fid) :coords))]])
               (keys selected-frames))}))

(re-frame/reg-event-fx
 ::position-handling-only-down
 (partial position-handling-down-right-event calc-cords-down))

(re-frame/reg-event-fx
 ::position-handling-only-right
 (partial position-handling-down-right-event calc-cords-right))

(defn- rearrange-size-map [space-x space-y rows-per-grouping leading-frames->simple-followers]
  (reduce (fn [acc [lead-idx [[_ adesc] followers]]]
            (let [followers-pos-map (reduce (fn [acc [col-idx col]]
                                              (reduce (fn [acc [row-idx [_ desc]]]
                                                        (-> (update-in acc [:coll-width col-idx]
                                                                       max
                                                                       (+ space-x (get-in desc [:full-size 0] 0)))
                                                            (update-in [:row-height row-idx]
                                                                       max
                                                                       (+ space-y (get-in desc [:full-size 1] 0)))))
                                                      acc
                                                      (map-indexed vector col)))
                                            {}
                                            (map-indexed vector (partition-all rows-per-grouping followers)))
                  lead-height (max (->> (:row-height followers-pos-map)
                                        vals
                                        (reduce +))
                                   (+ space-y (get-in adesc [:full-size 1] 0)))]

              (-> (assoc-in acc [:follower lead-idx] followers-pos-map)
                  (assoc-in [:lead-height lead-idx] lead-height)
                  (update :lead-width max (+ space-x (get-in adesc [:full-size 0] 0))))))
          {}
          (map-indexed vector leading-frames->simple-followers)))


(re-frame/reg-event-fx
 ::position-handling-rearrange-selected
 (fn [{db :db} _]
   (let  [rows-per-grouping 2
          space-x 100
          space-y 50
          start-x (or (:min-x @wws/multiselect-bb)
                      (:start-min-x @wws/multiselect-bb))
          start-y (or (:min-y @wws/multiselect-bb)
                      (:start-min-y @wws/multiselect-bb))
          selected-frames-ids (into #{} @wws/multiselect-current-selection)
          selected-frames (select-keys (get-in db path/frames)
                                       selected-frames-ids)
          content-frames (filter (fn [[_ fdesc]]
                                   (= (:type fdesc) :frame/content-type))
                                 selected-frames)
          sorted-consumer-frames (sort-by (fn [[_ fdesc]] (:creation-date fdesc))
                                          (filter (fn [[_ fdesc]]
                                                    (= (:type fdesc) :frame/consumer-type))
                                                  selected-frames))
          sorted-management-frames  (sort-by (fn [[_ fdesc]] (:vertical fdesc))
                                             (filter (fn [[_ fdesc]]
                                                       (= (:type fdesc) :frame/management-type))
                                                     selected-frames))
          trimmed-content-frames (into {} (map (fn [[fid fdesc :as frame]]
                                                 (if (or
                                                      (not (:published-by-frame fdesc))
                                                      (and (:published-by-frame fdesc)
                                                           (selected-frames-ids (:published-by-frame fdesc))))
                                                   frame
                                                   [fid (assoc fdesc :published-by-frame nil)]))
                                               content-frames))
          followers (into {} (filter (fn [[_ fdesc]] (:published-by-frame fdesc)) trimmed-content-frames))
          supposed-leaders-id-set (reduce (fn [acc [_ fdesc]]
                                            (if (:published-by-frame fdesc)
                                              (conj acc (:published-by-frame fdesc))
                                              acc))
                                          #{}
                                          followers)
          leaders (into {} (filter (fn [[fid fdesc]]
                                     (supposed-leaders-id-set fid)) trimmed-content-frames))
          simple-followers (into {} (filter (fn [[fid desc]] (not ((into #{} (keys leaders)) fid))) followers))
          single-content-frames (filter (fn [[fid desc]]
                                          (not ((into (into #{} (keys  simple-followers))
                                                      (keys leaders)) fid)))
                                        content-frames)
          leading-frames (sort-by (fn [[fid fdesc]] (:creation-date fdesc))
                                  (concat leaders single-content-frames))
          independent-frames (concat sorted-consumer-frames sorted-management-frames)
          leading-frames->simple-followers (reduce (fn [acc [afid adesc :as anchor]]
                                                     (assoc acc anchor
                                                            (sort-by (fn [[fid fdesc]] (:creation-date fdesc))
                                                                     (filter (fn [[fid fdesc]]
                                                                               (= afid (:published-by-frame fdesc)))
                                                                             simple-followers))))
                                                   {}
                                                   leading-frames)
          sticky-offsets (calc-sticky-offsets db selected-frames)
          {:keys [lead-height lead-width] follower-size :follower :as sizes} (rearrange-size-map space-x space-y rows-per-grouping leading-frames->simple-followers)
          current-y (atom (+ start-y (reduce + (vals lead-height))))
          db
          (as-> (reduce (fn [db [lead-idx [[afid] followers]]]
                          (let [y (+ start-y
                                     (reduce + (map lead-height (range lead-idx))))
                                new-coords [start-x y]
                                follower-size (get follower-size lead-idx)]
                            (set-frame-position afid new-coords)
                            (cond-> (assoc-in db (conj (path/frame-desc afid) :coords) new-coords)
                              (seq followers)
                              ((fn [db]
                                 (reduce (fn [acc [col-idx column]]
                                           (reduce (fn [acc2 [row-idx [ffid]]]
                                                     (let [f-coords [(+ start-x
                                                                        lead-width
                                                                        (reduce + (map (:coll-width follower-size) (range col-idx))))
                                                                     (+ y
                                                                        (reduce + (map (:row-height follower-size) (range row-idx))))]]

                                                       (set-frame-position ffid f-coords)
                                                       (assoc-in acc2 (conj (path/frame-desc ffid) :coords) f-coords)))
                                                   acc
                                                   (map-indexed vector column)))
                                         db
                                         (map-indexed vector (partition-all rows-per-grouping followers))))))))
                        db
                        (map-indexed vector leading-frames->simple-followers))
                new-db
            (reduce (fn [db [ifid idesc]]
                      (let [[_ height] (:full-size idesc)
                            new-coords [start-x
                                        @current-y]]
                        (reset! current-y (+ @current-y space-y height))
                        (set-frame-position ifid new-coords)
                        (assoc-in db (conj (path/frame-desc ifid) :coords) new-coords)))
                    new-db
                    independent-frames)
            (set-sticky-frames-positions-db new-db sticky-offsets))
          new-frames (select-keys (get-in db path/frames)
                                  selected-frames-ids)]
     (reset! wws/multiselect-bb (frame-bounding-box new-frames))
     {:db db
      :fx (mapv (fn [fid]
                  [:dispatch [:de.explorama.frontend.woco.frame.interaction.move/move-frame fid (get-in db (conj (path/frame-desc fid) :coords))]])
                selected-frames-ids)})))

