(ns de.explorama.frontend.woco.workspace.sticky
  (:require [de.explorama.frontend.woco.frame.size-position :refer [set-frame-position]]
            [de.explorama.frontend.woco.path :as path]
            [de.explorama.frontend.woco.workspace.math :refer [collide-rects]]))

(defn calc-sticky-offsets [db selected-frames]
  (let [sticky-flagged (filter (fn [[_ {:keys [stick-to-frames?]}]]
                                 stick-to-frames?)
                               ; need to check all frames, because it could be that a sticky frame is not selected
                               (get-in db path/frames))
        sticky-frame-ids (set (keys sticky-flagged))
        selected-frames (sort-by (fn [[_ {[mx] :coords}]] mx)
                                 (apply dissoc selected-frames sticky-frame-ids))]
    (reduce (fn [acc [sticky-fid {[x y] :coords [w h] :full-size}]]
              (if-let [offset (some (fn [[id {[mx my] :coords [mw mh] :full-size}]]
                                      (when (and (not= sticky-fid id)
                                                 (collide-rects x y w h mx my mw mh))
                                        {:offset [(- x mx)
                                                  (- y my)]
                                         :sticky-to-frame-id id}))
                                    selected-frames)]
                (assoc acc sticky-fid offset)
                acc))
            {}
            sticky-flagged)))


(defn- set-sticky-frames-positions-internal [frames sticky-offsets]
  (reduce (fn [acc [sticky-fid {[offset-x offset-y] :offset :keys [sticky-to-frame-id]}]]
            (let [[new-frame-x new-frame-y] (get-in frames [sticky-to-frame-id :coords])
                  new-pos (when (and new-frame-x new-frame-y)
                            [(+ new-frame-x offset-x)
                             (+ new-frame-y offset-y)])]
              (when new-pos
                (set-frame-position sticky-fid new-pos))
              (cond-> acc
                new-pos
                (assoc sticky-fid new-pos))))
          {}
          sticky-offsets))

(defn set-sticky-frames-positions
  "Precondition: New positions are already set in db for non sticky frames
   - frames: frames with new positions set for non sticky frames
   - sticky-offsets: Map of calculated sticky frames with offsets, calculated by calc-sticky-offsets
   Returns new db with new positions and sets internal positions"
  [frames sticky-offsets]
  (let [sticky-frames-pos (set-sticky-frames-positions-internal
                           frames
                           sticky-offsets)]
    (reduce (fn [db [sticky-fid new-pos]]
              (cond-> db
                (and sticky-fid new-pos)
                (assoc-in [sticky-fid :coords]
                          new-pos)))
            frames
            sticky-frames-pos)))

(defn set-sticky-frames-positions-db
  "Precondition: New positions are already set in db for non sticky frames
   - db: new db with new positions set for non sticky frames
   - sticky-offsets: Map of calculated sticky frames with offsets, calculated by calc-sticky-offsets
   Returns new db with new positions and sets internal positions"
  [db sticky-offsets]
  (let [sticky-frames-pos (set-sticky-frames-positions-internal
                           (get-in db path/frames)
                           sticky-offsets)]
    (reduce (fn [db [sticky-fid new-pos]]
              (cond-> db
                (and sticky-fid new-pos)
                (assoc-in (conj (path/frame-desc sticky-fid) :coords)
                          new-pos)))
            db
            sticky-frames-pos)))
