(ns de.explorama.frontend.mosaic.render.draw.scatter.axes
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.common.date.utils :as dutil]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.mosaic.render.engine :as gre]))

(def ^:private font-color-light [154 154 154])
(def ^:private line-color-light [200 200 200])
(def ^:private background-color-light [255 255 255])
(def ^:private font-color-dark [195 199 203])
(def ^:private line-color-dark [195 199 203])
(def ^:private background-color-dark [27 28 30])
(def ^:private line-alpha 1)

(def ^:private units-per-100 1)

(defn- colors [this]
  (let [{:keys [theme]} (gre/state this)]
    (case theme
      :light [background-color-light line-color-light font-color-light]
      :dark [background-color-dark line-color-dark font-color-dark]
      [background-color-light line-color-light font-color-light])))

(defn- number-axis [size min-col max-col mapping]
  (let [values (max 0 (Math/floor (* (/ size 100)
                                     units-per-100)))
        available-values (max 0 (- max-col min-col))]
    (if (<= available-values values)
      (map (fn [[idx value]]
             [idx (i18n/localized-number value)])
           (select-keys mapping (range min-col (inc max-col))))
      (let [step (int (Math/ceil (/ available-values values)))]
        (if (< 0 step)
          (merge {min-col (str (i18n/localized-number (get mapping min-col)))}
                 (loop [cur-col (+ min-col step)
                        result {}]
                   (if (<= cur-col max-col)
                     (recur (+ cur-col step)
                            (assoc result
                                   cur-col
                                   (i18n/localized-number (get mapping cur-col))))
                     result)))
          {})))))

(defn- step-result [result step]
  (let [ignore-counter (int (Math/ceil (/ (count result) step)))]
    (into []
          (comp
           (map-indexed vector)
           (filter (fn [[idx]]
                     (= 0 (mod idx ignore-counter))))
           (map (fn [[_ v]] v)))
          result)))

(defn- pick-date-scale [min-idx max-idx min-value max-value granularity step]
  (let [day-granularity (dutil/day-granularity min-value max-value)
        grid-idxs (/ (- max-idx min-idx)
                     (count day-granularity))]
    (loop [days (map-indexed vector day-granularity)
           result []]
      (if (empty? days)
        (step-result result step)
        (let [[idx date] (first days)
              add? (case granularity
                     :year (and (= 1 (get date 1))
                                (= 1 (get date 2)))
                     :month (= 1 (get date 2))
                     :day true)]
          (if add?
            (recur (rest days)
                   (conj result (case granularity
                                  :year [(+ min-idx (* idx grid-idxs))
                                         (str (first date))]
                                  :month [(+ min-idx (* idx grid-idxs))
                                          (str (get date 0)
                                               "-"
                                               (dutil/complete-date (get date 1)))]
                                  :day [(+ min-idx (* idx grid-idxs))
                                        (str/join "-" (map dutil/complete-date date))])))
            (recur (rest days)
                   result)))))))

(defn- date-axis [size min-col max-col mapping]
  (let [max-col (if (< max-col (count mapping))
                  (max min-col (dec max-col))
                  max-col)
        values (Math/floor (* (/ size 100)
                              units-per-100))
        min-values (dutil/date-convert (get mapping min-col))
        max-values (dutil/date-convert (get mapping max-col))]
    (loop [granularities [:year :month :day]
           prev nil]
      (let [granularity (first granularities)
            min-value (get min-values :day)
            max-value (get max-values :day)
            value-range (pick-date-scale min-col
                                         max-col
                                         min-value
                                         max-value
                                         granularity
                                         values)]
        (cond (and (<= (- values 2) (count prev))
                   (<= values (count value-range)))
              prev
              (or (empty? (rest granularities))
                  (and (< (count prev) values)
                       (<= values (count value-range))))
              value-range
              :else
              (recur (rest granularities)
                     value-range))))))

(defn- string-axis [size min-col max-col mapping]
  (let [values (Math/floor (* (/ size 100)
                              units-per-100))
        available-values (- max-col min-col)]
    (if (<= available-values values)
      (mapv (fn [[idx value]]
              [idx nil value])
            (select-keys mapping (range min-col (inc max-col))))
      (loop [current-num 1
             result {}]
        (let [new-result (reduce (fn [acc [idx value]]
                                   (-> acc
                                       (update-in [(subs value 0 current-num) :min] (fnil min max-col) idx)
                                       (update-in [(subs value 0 current-num) :max] (fnil max min-col) idx)))
                                 {}
                                 (select-keys mapping (range min-col (inc max-col))))
              in-range? (and (< (count result) values)
                             (<= values (count new-result)))]
          (cond (= current-num 2)
                :no-scale
                in-range?
                (step-result
                 (mapv (fn [[axis-value {:keys [min max]}]]
                         [min max axis-value])
                       new-result)
                 values)
                :else
                (recur
                 (inc current-num)
                 new-result)))))))

(def debug-string-axis-example
  (into {}
        (->>
         (range 28)
         (map (fn [_]
                (apply str (take 10 (repeatedly #(char (+ (rand 4) 65)))))))
         sort
         (map-indexed vector))))

(defn- debug-grid-axis [_ min-col max-col _]
  (into {}
        (map (fn [v] [v (str v)])
             (range min-col
                    max-col))))

(defn- draw-y-axis [this
                    background-container
                    text-container
                    y-axis-type y-label y-mapping
                    _width height
                    factor
                    size-base-x size-base-y
                    _size-x size-y
                    font-size text-pos-factor
                    pos-x pos-y
                    line-thickness line-height axis-margin-top _axis-margin-bottom
                    cpl-height _cpl-width
                    _min-col _max-col
                    min-row max-row
                    x y z max-zoom
                    scale-window-width
                    _scale-window-height]
  (let [lang @(re-frame/subscribe [::i18n/current-language])
        [background-color line-color font-color] (colors this)
        margin-left 30
        line-thickness-fixed 15
        y-text-style {:font-size font-size
                      :font-color font-color
                      :align :right
                      :vertical-align :center}
        y-text-style-label {:font-size font-size
                            :font-color font-color
                            :angle 270}
        y-axis ((case y-axis-type
                  :grid debug-grid-axis
                  :number number-axis
                  :date date-axis
                  :string string-axis)
                height
                min-row
                max-row
                y-mapping)
        show-y-axis-cond? (fn [row-idx]
                            (< (* (+ y
                                     (* 2 line-height))
                                  factor)
                               (* (+ (- line-thickness)
                                     (* row-idx cpl-height)
                                     (+ (* 0.5 cpl-height)))
                                  factor)
                               (* (+ y
                                     (- size-y
                                        size-base-x
                                        (* 2 line-height)))
                                  factor)))]
    (gre/rect this
              background-container
              (* x factor)
              (* y factor)
              (* (+ size-base-y axis-margin-top) factor)
              (* (- size-y size-base-x) factor)
              background-color)
    (gre/rect this
              background-container
              (* (+ x size-base-y (- line-thickness)) factor)
              (* y factor)
              (* line-thickness factor)
              (* (- size-y size-base-x) factor)
              line-color
              {:a line-alpha})
    (when (#{:grid :number :date} y-axis-type)
      (doseq [[row-idx row-label] y-axis]
        (when (show-y-axis-cond? row-idx)
          (gre/rect this
                    background-container
                    (* (+ x
                          size-base-y
                          (- line-height)
                          (- line-thickness))
                       factor)
                    (* (+ (- line-thickness)
                          (* row-idx cpl-height)
                          (+ (* 0.5 cpl-height)))
                       factor)
                    (* line-height factor)
                    (* line-thickness factor)
                    line-color
                    {:a line-alpha})
          (gre/text this
                    text-container
                    (str row-label)
                    (+ margin-left
                       (* (+ x pos-x)
                          text-pos-factor))
                    (* (+ (- line-thickness)
                          (* row-idx cpl-height)
                          (+ (* 0.5 cpl-height))
                          pos-y)
                       text-pos-factor)
                    (- scale-window-width margin-left line-thickness-fixed)
                    :one-line
                    y-text-style))))

    (when (#{:string} y-axis-type)
      (if (= y-axis :no-scale)
        (gre/text this
                  text-container
                  (if (= :en-GB lang)
                    "Not able to show a scale - too many values"
                    "Achse wird nicht angezeigt - zu viele Werte")
                  (* (+ x
                        (* 0.5 size-base-y)
                        pos-x)
                     text-pos-factor)
                  (max 0 (+ (* 0.5 height) 140))
                  (* 0.7 height)
                  :one-line
                  {:font-size font-size
                   :font-color font-color
                   ;:align :center
                   ;:vertical-align :center
                   :angle 270})
        (doseq [[min-idx max-idx row-label] y-axis]
          (let [row-idx (if (and min-idx max-idx)
                          (* (+ max-idx min-idx) 0.5)
                          min-idx)]
            (when (show-y-axis-cond? row-idx)
              (gre/rect this
                        background-container
                        (* (+ x
                              size-base-y
                              (- line-height)
                              (- line-thickness))
                           factor)
                        (* (+ (- line-thickness)
                              (* row-idx
                                 cpl-height)
                              (* 0.5 cpl-height))
                           factor)
                        (* line-height factor)
                        (* line-thickness factor)
                        line-color
                        {:a line-alpha})
              (gre/text this
                        text-container
                        (str row-label)
                        (+ margin-left
                           (* (+ x pos-x)
                              text-pos-factor))
                        (* (+ (- line-thickness)
                              (* row-idx
                                 cpl-height)
                              (+ (* 0.5 cpl-height))
                              pos-y)
                           text-pos-factor)
                        (- scale-window-width margin-left line-thickness-fixed)
                        :one-line
                        y-text-style))))))

    (gre/polygon this
                 background-container
                 [(gre/point this
                             (* (+ x
                                   size-base-y
                                   (- (* 0.5 line-thickness)))
                                factor)
                             (* y factor))
                  (gre/point this
                             (* (+ x
                                   size-base-y
                                   (- (* 0.5 line-thickness))
                                   (- (* line-height 0.6)))
                                factor)
                             (* (+ y line-height) factor))
                  (gre/point this
                             (* (+ x
                                   size-base-y
                                   (- (* 0.5 line-thickness))
                                   (* line-height 0.6))
                                factor)
                             (* (+ y line-height) factor))]
                 line-color
                 {:a line-alpha})
    (when-not (= max-zoom (* z factor))
          ;TOP
      (gre/rect this
                background-container
                (* (+ x
                      size-base-y
                      (- (* 0.5 line-thickness))
                      (- (* line-height 0.5)))
                   factor)
                (* (+ y
                      (* 1.3 line-height))
                   factor)
                (* line-height factor)
                (* line-thickness factor)
                line-color
                {:a line-alpha})
      (gre/rect this
                background-container
                (* (+ x
                      size-base-y
                      (- (* 0.5 line-thickness))
                      (- (* line-height 0.5)))
                   factor)
                (* (+ y
                      (* 1.6 line-height))
                   factor)
                (* line-height factor)
                (* line-thickness factor)
                line-color
                {:a line-alpha})
          ;BOT
      (gre/rect this
                background-container
                (* (+ x
                      size-base-y
                      (- (* 0.5 line-thickness))
                      (- (* line-height 0.5)))
                   factor)
                (* (+ y
                      size-y
                      (- size-base-x)
                      (- (* 1 line-height)))
                   factor)
                (* line-height factor)
                (* line-thickness factor)
                line-color
                {:a line-alpha})
      (gre/rect this
                background-container
                (* (+ x
                      size-base-y
                      (- (* 0.5 line-thickness))
                      (- (* line-height 0.5)))
                   factor)
                (* (+ y
                      size-y
                      (- size-base-x)
                      (- (* 1.3 line-height)))
                   factor)
                (* line-height factor)
                (* line-thickness factor)
                line-color
                {:a line-alpha}))

    (gre/text this
              text-container
              (str y-label)
              (* (+ x
                    (* size-base-x 0.04)
                    pos-x)
                 text-pos-factor)
              (* (+ y
                    (* size-y 0.5)
                    pos-y)
                 text-pos-factor)
              (* height 0.75)
              :one-line
              y-text-style-label)))

(defn- draw-x-axis [this
                    background-container
                    text-container
                    x-axis-type x-label x-mapping
                    width _height
                    factor
                    size-base-x size-base-y
                    size-x size-y
                    font-size text-pos-factor
                    pos-x pos-y
                    line-thickness line-height axis-margin-top axis-margin-bottom
                    _cpl-height cpl-width
                    min-col max-col
                    _min-row _max-row
                    x y z max-zoom
                    scale-window-width
                    _scale-window-height]
  (let [lang @(re-frame/subscribe [::i18n/current-language])
        [background-color line-color font-color] (colors this)
        x-text-style {:font-size font-size
                      :font-color font-color
                      :align :center
                      :vertical-align :top}
        x-text-style-label {:font-size font-size
                            :font-color font-color
                            :align :center
                            :vertical-align :top}
        show-x-axis-cond? (fn [col-idx]
                            (< (* (+ x
                                     size-base-y
                                     (* 2 line-height))
                                  factor)
                               (* (+ (- line-thickness)
                                     (* col-idx
                                        cpl-width)
                                     (* 0.5 cpl-width))
                                  factor)
                               (* (- (+ x size-x)
                                     (* 2 line-height))
                                  factor)))
        x-axis ((case x-axis-type
                  :grid debug-grid-axis
                  :number number-axis
                  :date date-axis
                  :string string-axis)
                width
                min-col
                max-col
                x-mapping)]
    (gre/rect this
              background-container
              (* x factor)
              (* (+ y
                    (- size-y
                       size-base-x
                       axis-margin-top))
                 factor)
              (* size-x factor)
              (* (+ size-base-x
                    axis-margin-top)
                 factor)
              background-color)
    (gre/rect this
              background-container
              (* (+ x size-base-y (- line-thickness)) factor)
              (* (+ y (- size-y size-base-x)) factor)
              (* (+ line-thickness (- size-x size-base-x)) factor)
              (* line-thickness factor)
              line-color
              {:a line-alpha})
    (when (#{:grid :number :date} x-axis-type)
      (let [values (filter (fn [[col-idx]]
                             (show-x-axis-cond? col-idx))
                           x-axis)
            values-c (count values)
            step (Math/abs (if (< 1 values-c)
                             (- (first (second values))
                                (ffirst values))
                             1))
            scale-width (* width step text-pos-factor 0.95)]
        (doseq [[col-idx col-value] values]
          (gre/rect this
                    background-container
                    (* (+ (- line-thickness)
                          (* col-idx cpl-width)
                          (+ (* 0.5 cpl-width)))
                       factor)
                    (* (+ y (- size-y size-base-x) line-thickness) factor)
                    (* line-thickness factor)
                    (* line-height factor)
                    line-color
                    {:a line-alpha})
          (gre/text this
                    text-container
                    (str col-value)
                    (* (+ (- line-thickness)
                          (* col-idx cpl-width)
                          (+ (* 0.5 cpl-width))
                          pos-x)
                       text-pos-factor)
                    (* (+ y
                          (- size-y size-base-x)
                          (* axis-margin-bottom 0.25)
                          line-height
                          pos-y)
                       text-pos-factor)
                    scale-width
                    :one-line
                    x-text-style))))

    (when (#{:string} x-axis-type)
      (if (= x-axis :no-scale)
        (gre/text this
                  text-container
                  (if (= :en-GB lang)
                    "Not able to show a scale - too many values"
                    "Achse wird nicht angezeigt - zu viele Werte")
                  (* width 0.6)
                  (* (+ y
                        (- size-y size-base-x)
                        (* 15 line-thickness)
                        line-height
                        pos-y)
                     text-pos-factor)
                  (* width 0.5)
                  cpl-width
                  {:font-size font-size
                   :font-color font-color
                   :align :center
                   :vertical-align :center
                   :angle 0
                   :bitmap? false})
        (let [values (filter (fn [[col-idx]]
                               (show-x-axis-cond? col-idx))
                             x-axis)
              values-c (count values)
              step (if (< 1 values-c)
                     (let [[min-idx max-idx] (first values)
                           lower (if (and min-idx max-idx)
                                   (* (+ min-idx max-idx) 0.5)
                                   min-idx)
                           [min-idx max-idx] (second values)
                           upper (if (and min-idx max-idx)
                                   (* (+ min-idx max-idx) 0.5)
                                   min-idx)]
                       (- upper lower))
                     1)
              scale-width (* cpl-width step text-pos-factor 0.95)]
          (doseq [[min-idx max-idx col-value] values]
            (let [col-idx (if (and min-idx max-idx)
                            (* (+ min-idx max-idx) 0.5)
                            min-idx)]
              (gre/rect this
                        background-container
                        (* (+ (- line-thickness)
                              (* col-idx
                                 cpl-width)
                              (* 0.5 cpl-width))
                           factor)
                        (* (+ y (- size-y size-base-x) line-thickness) factor)
                        (* line-thickness factor)
                        (* line-height factor)
                        line-color
                        {:a line-alpha})
              (gre/text this
                        text-container
                        (str col-value)
                        (* (+ (- line-thickness)
                              (* col-idx cpl-width)
                              pos-x)
                           text-pos-factor)
                        (* (+ y
                              (- size-y size-base-x)
                              axis-margin-bottom
                              line-height
                              pos-y)
                           text-pos-factor)
                        scale-width
                        :one-line
                        x-text-style))))))

    (gre/polygon this
                 background-container
                 [(gre/point this
                             (* (+ x size-x) factor)
                             (* (+ y
                                   (- size-y size-base-x)
                                   (* 0.5 line-thickness))
                                factor))
                  (gre/point this
                             (* (- (+ x size-x)
                                   line-height)
                                factor)
                             (* (+ y
                                   (- size-y size-base-x)
                                   (* 0.5 line-thickness)
                                   (* line-height 0.6))
                                factor))
                  (gre/point this
                             (* (- (+ x size-x)
                                   line-height)
                                factor)
                             (* (+ y
                                   (- size-y size-base-x)
                                   (* 0.5 line-thickness)
                                   (- (* line-height 0.6)))
                                factor))]
                 line-color
                 {:a line-alpha})

    (when-not (= max-zoom (* z factor))
          ; RIGHT
      (gre/rect this
                background-container
                (* (+ x
                      size-x
                      (- (* 1.4 line-height)))
                   factor)
                (* (+ y
                      size-y
                      (- size-base-x)
                      (- (* 0.5 line-height)))
                   factor)
                (* line-thickness factor)
                (* line-height factor)
                line-color
                {:a line-alpha})
      (gre/rect this
                background-container
                (* (+ x
                      size-x
                      (- (* 1.7 line-height)))
                   factor)
                (* (+ y
                      size-y
                      (- size-base-x)
                      (- (* 0.5 line-height)))
                   factor)
                (* line-thickness factor)
                (* line-height factor)
                line-color
                {:a line-alpha})
          ; LEFT
      (gre/rect this
                background-container
                (* (+ x
                      size-base-y
                      (+ (* 1 line-height)))
                   factor)
                (* (+ y
                      size-y
                      (- size-base-x)
                      (- (* 0.5 line-height)))
                   factor)
                (* line-thickness factor)
                (* line-height factor)
                line-color
                {:a line-alpha})
      (gre/rect this
                background-container
                (* (+ x
                      size-base-y
                      (+ (* 1.3 line-height)))
                   factor)
                (* (+ y
                      size-y
                      (- size-base-x)
                      (- (* 0.5 line-height)))
                   factor)
                (* line-thickness factor)
                (* line-height factor)
                line-color
                {:a line-alpha}))

    (gre/text this
              text-container
              (str x-label)
              (* (+ x
                    (* size-x 0.5)
                    pos-x)
                 text-pos-factor)
              (- (* (+ y size-y pos-y)
                    text-pos-factor)
                 25)
              (* scale-window-width 2)
              :one-line
              x-text-style-label)))

(defn draw-axes [this background-container text-container ctx ctx-params zoom container-key [min-col max-col min-row max-row _x _y _content-size-x _content-size-y]]
  (let [state (gre/state this)
        {pos-x :x pos-y :y :keys [z]} (get state [:pos container-key])
        {:keys [width height]} (gre/args this)
        {{:keys [width-ctn height-ctn margin-ctn max-zoom]} :params
         {:keys [x-label y-label x-axis-type y-axis-type x-mapping y-mapping scale-window-width scale-window-height]} :optional-desc
         :keys [factor-overview]}
        ctx
        {:keys [x y]
         size-x :size-x
         size-y :size-y}
        ctx-params
        x-axis-type (if (#{:integer :decimal} x-axis-type)
                      :number
                      x-axis-type)
        y-axis-type (if (#{:integer :decimal} y-axis-type)
                      :number
                      y-axis-type)
        factor (if (zero? zoom) factor-overview 1)
        size-base-x (/ scale-window-height (* z factor))
        size-base-y (/ scale-window-width (* z factor))
        font-size 18
        line-thickness (* size-base-y 0.01)
        line-height (* size-base-y 0.075)
            ;bitmap? false
        axis-margin-top (* line-height 0.6)
        axis-margin-bottom (* size-base-y 0.15)
        cpl-width (+ width-ctn margin-ctn margin-ctn)
        cpl-height (+ height-ctn margin-ctn margin-ctn)

        max-col (if (<= (count x-mapping) max-col)
                  (dec (count x-mapping))
                  max-col)
        max-row (if (<= (count y-mapping) max-row)
                  (dec (count y-mapping))
                  max-row)
        text-pos-factor (* z factor)
        pos-x (/ pos-x (* z factor))
        pos-y (/ pos-y (* z factor))]
    (when (and x-axis-type y-axis-type)
      (draw-x-axis this
                   background-container
                   text-container
                   x-axis-type x-label x-mapping
                   width height
                   factor
                   size-base-x size-base-y
                   size-x size-y
                   font-size text-pos-factor pos-x pos-y
                   line-thickness line-height axis-margin-top axis-margin-bottom
                   cpl-height cpl-width
                   min-col max-col
                   min-row max-row
                   x y z max-zoom
                   scale-window-width
                   scale-window-height)
      (draw-y-axis this
                   background-container
                   text-container
                   y-axis-type y-label y-mapping
                   width height
                   factor
                   size-base-x size-base-y
                   size-x size-y
                   font-size text-pos-factor pos-x pos-y
                   line-thickness line-height axis-margin-top axis-margin-bottom
                   cpl-height cpl-width
                   min-col max-col
                   min-row max-row
                   x y z max-zoom
                   scale-window-width
                   scale-window-height))))