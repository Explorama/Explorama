(ns de.explorama.frontend.woco.frame.util
  (:require [de.explorama.frontend.ui-base.utils.subs :refer [val-or-deref]]
            [de.explorama.frontend.woco.frame.events :as evts]
            [de.explorama.frontend.woco.path :as path]))

(defn list-frames [db query]
  (let [query-fun (if (empty? query) (constantly true) (set query))]
    (reduce (fn [result [frame-id frame]]
              (if (query-fun (:type frame))
                (conj result
                      {:id frame-id
                       :z-index (:z-index frame)
                       :title (:title frame)})
                result))
            []
            (get-in db path/frames))))

(defn is-content-frame?
  ([type]
   (= type evts/content-type))
  ([db frame-id]
   (is-content-frame? (get-in db (path/frame-type frame-id)))))

(defn is-custom-frame?
  ([type]
   (= type evts/custom-type))
  ([db frame-id]
   (is-custom-frame? (get-in db (path/frame-type frame-id)))))

(defn handle-param [func & params]
  (cond
    (fn? func)
    (apply func params)
    (not (nil? func))
    (val-or-deref func)))

(defn find-maximized-frame [db]
  (some (fn [[fid frame-desc]]
          (when (:is-maximized? frame-desc)
            [fid frame-desc]))
        (get-in db path/frames)))

(defn which-is-maximized [db]
  (boolean (find-maximized-frame db)))
