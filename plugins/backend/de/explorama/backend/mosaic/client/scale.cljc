(ns de.explorama.backend.mosaic.client.scale
  (:require [de.explorama.shared.mosaic.common-paths :as gcp]
            [de.explorama.backend.common.scales :as scales]))

(defn mapping-layer [data
                     data-acs
                     scatter-axis-fallback?
                     {x gcp/scatter-x
                      y gcp/scatter-y
                      {:keys [width height card-width card-height card-margin]} gcp/scatter-client-dims}]
  (let [_ (when (empty? data)
            (throw (ex-info "Scatterplot empty" {:error :scatter-plot-empty})))
        x (scales/get-x-axis x)
        y (scales/get-y-axis data-acs y)
        {:keys [counter ignore]} (scales/attribute-counting data x y)
        x-axis-type (get-in data-acs [x :std :specific-type])
        y-axis-type  (get-in data-acs [y :std :specific-type])
        [[x y]
         [x-axis-type y-axis-type]]
        (cond (and (not (and x-axis-type y-axis-type))
                   (not scatter-axis-fallback?))
              (throw (ex-info "Axis-type not set" {:error :axis-type-not-set
                                                   :x-axis-type x-axis-type
                                                   :y-axis-type y-axis-type}))
              (and (not (and x-axis-type y-axis-type))
                   scatter-axis-fallback?)
              (let [x (if (nil? x-axis-type)
                        (scales/get-x-axis nil)
                        x)
                    y (if (nil? y-axis-type)
                        (scales/get-y-axis data-acs nil)
                        y)
                    x-axis-type (get-in data-acs [x :std :specific-type])
                    y-axis-type  (get-in data-acs [y :std :specific-type])]
                [[x y]
                 [x-axis-type y-axis-type]])
              :else
              [[x y] [x-axis-type y-axis-type]])
        x-data-acs (get data-acs x)
        y-data-acs (get data-acs y)
        [x-mapping y-mapping]
        (scales/axis-length-adjust counter
                            width
                            height
                            x-axis-type
                            y-axis-type
                            (fn [num-event length-other-axis]
                              (case x-axis-type
                                :date (scales/date-axis x-data-acs num-event length-other-axis)
                                :string (scales/string-axis x-data-acs)
                                :integer (scales/number-axis-integer x-data-acs length-other-axis true)
                                :decimal (scales/number-axis-decimal x-data-acs length-other-axis true)))
                            (fn [num-event length-other-axis]
                              (case y-axis-type
                                :date (scales/date-axis y-data-acs num-event length-other-axis)
                                :string (scales/string-axis y-data-acs)
                                :integer (scales/number-axis-integer y-data-acs length-other-axis false)
                                :decimal (scales/number-axis-decimal y-data-acs length-other-axis false))))]
    {:mapping (-> (scales/calculate-relative-positions data x y
                                                x-axis-type
                                                y-axis-type
                                                x-mapping
                                                y-mapping
                                                card-width
                                                card-height
                                                card-margin)
                  :mapping)
     :x-axis-type x-axis-type
     :y-axis-type y-axis-type
     :x-mapping (into {} (:scale x-mapping))
     :x-value-count (get x-mapping :length)
     :y-mapping (into {} (:scale y-mapping))
     :y-value-count (get y-mapping :length)
     :ignored ignore
     :x-label x
     :y-label y
     :scale-window-width 120
     :scale-window-height 80
     :scale-window-margin 10
     :guide-lines? true}))
