(ns de.explorama.frontend.mosaic.data.data
  "Holds raw-data and provides some data-operations"
  (:require [de.explorama.frontend.mosaic.path :as gp]))

(defonce ^:private db (atom (. cljs.core/PersistentHashMap -EMPTY)))
(defonce ^:private annotations (atom (. cljs.core/PersistentHashMap -EMPTY)))
(defonce ^:private scales (atom (. cljs.core/PersistentHashMap -EMPTY)))

(defn get-events
  ([path]
   (let [path (gp/top-level path)]
     (get @db path)))
  ([path grp-idx]
   (let [path (gp/top-level path)]
     (get-in @db [path grp-idx]))))

(defn relevant-group-paths [data-group-path]
  (reduce (fn [r counter]
            (conj r
                  (subvec data-group-path 0 (* 2 (inc counter)))))
          []
          (range 0 (/ (count data-group-path)
                      2))))

(defn get-group-value [path data-group-path]
  (get-in (get-events (gp/canvas path))
          (conj data-group-path :key)))
(defn get-group-name [path data-group-path]
  (get-in (get-events (gp/canvas path))
          (conj data-group-path :name)))

(defn get-parent-group-by-attr [path data-group-path]
  (get-in (get-events (gp/canvas path))
          (-> data-group-path
              pop pop
              (conj :group-by :by))))

(defn set-events! [path data]
  (let [path (gp/top-level path)]
    (swap! db assoc path (if (= [] data)
                           #js []
                           data))))

(defn unset-events! [path-or-frame-id]
  (let [path (gp/top-level path-or-frame-id)]
    (swap! db dissoc path)))

(defn get-scale [path]
  (let [path (gp/top-level path)]
    (get @scales path)))

(defn set-scale! [path data]
  (let [path (gp/top-level path)]
    (swap! scales assoc path (if (= [] data)
                               #js []
                               data))))

(defn unset-scale! [path]
  (let [path (gp/top-level path)]
    (swap! scales dissoc (gp/top-level path))))

(defn get-annotations [frame-id]
  (let [frame-id (gp/frame-id frame-id)]
   (get @annotations frame-id)))

(defn merge-annotations! [frame-id new-annotations]
  (let [frame-id (gp/frame-id frame-id)]
    (swap! annotations update frame-id merge new-annotations)))

(defn unset-annotations! [frame-id]
  (let [frame-id (gp/frame-id frame-id)]
    (swap! annotations dissoc frame-id)))
