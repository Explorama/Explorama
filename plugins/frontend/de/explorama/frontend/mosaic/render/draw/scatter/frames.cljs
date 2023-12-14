(ns de.explorama.frontend.mosaic.render.draw.scatter.frames
  (:require [de.explorama.frontend.mosaic.render.draw.scatter.consts :refer [line-thickness]]
            [de.explorama.frontend.mosaic.render.engine :as gre]))

(defn render-container [mode
                        instance
                        stage
                        _
                        {{:keys [height width width-ctn height-ctn margin-ctn]} :params
                         overview-factor :factor-overview
                         [x y] :offset-absolute
                         {guide-lines? :guide-lines?
                          :keys [x-value-count y-value-count]}
                         :optional-desc}
                        _parent-grouped?
                        _render-path
                        _path]
  (let [overview-factor (if (= mode 0) overview-factor 1)
        card-height (+ height-ctn margin-ctn margin-ctn)
        card-width (+ width-ctn margin-ctn margin-ctn)]
    (when guide-lines?
      (loop [i 0]
        (when (< i x-value-count)
          (gre/rect instance
                    stage
                    (* (+ x
                          (* i card-width)
                          (* 0.5 card-width))
                       overview-factor)
                    (- y)
                    (* line-thickness 0.2 overview-factor)
                    (* width overview-factor)
                    [200 200 200]
                    {:a 1})
          (recur (inc i))))
      (loop [i 0]
        (when (< i y-value-count)
          (gre/rect instance
                    stage
                    (* overview-factor x)
                    (* (+ y
                          (* i card-height)
                          (* 0.5 card-height))
                       overview-factor)
                    (* height overview-factor)
                    (* line-thickness 0.2 overview-factor)
                    [200 200 200]
                    {:a 1})
          (recur (inc i)))))))
                   