(ns de.explorama.frontend.mosaic.render.draw.text-handler
  (:require [clojure.string :as str]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.mosaic.config :as config]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            ;; maybe move to mosaic render engine for abstraction?
            [de.explorama.frontend.mosaic.render.pixi.text-metrics :as text-metrics]))

; Good ratio for our font. font-width = font-height * font-width-height-ratio
(def font-width-height-ratio 2.25)

(defn get-char-width [font-size char-code]
  (* font-size
     (get text-metrics/arial-utf16-width-metrics char-code)))

(defn estimate-width [text-length font-size]
  (* text-length font-size text-metrics/avg-char-width))

(defn calculate-pruned-text [text font-size width _height max-lines]
  (let [text-length (count text)
        max-width (* max-lines width)
        char-width-fn (partial get-char-width font-size)]
    ;Ignore calculation when its not necessary due to short string
    (if (< (estimate-width text-length font-size)
           max-width)
      (clojure.string/replace text #"\r\n|\n|\r" " ")
      (loop [char-idx 0
             chars-left (dec text-length)
             curr-width 0
             result ""]
        (let [char-code (.charCodeAt text char-idx)
              char-width (char-width-fn char-code)
              new-curr-width (+ curr-width char-width)]
          (cond
            (= chars-left 0)
            (cond-> result
              (not (text-metrics/charcode-blacklist char-code))
              (str (.charAt text char-idx)))

            (text-metrics/charcode-blacklist char-code)
            (recur (inc char-idx)
                   (dec chars-left)
                   (+ curr-width
                      (char-width-fn text-metrics/whitespace-charcode))
                   (str result " "))

            (>= new-curr-width max-width)
            (str (subs result
                       0
                       (max (- (count result) 2)
                            0))
                 config/prune-char)

            :else
            (recur (inc char-idx)
                   (dec chars-left)
                   new-curr-width
                   (str result (.charAt text char-idx)))))))))

(defn- scale-text [text font-size width _height]
  (try (let [width-ratio (/ width (* (/ font-size font-width-height-ratio)
                                     (count text)))]
         (min font-size
              (Math/floor (* font-size width-ratio))))

       (catch :default _
         font-size)))

(defn- handle-raw-data
  [data]
  (cond
    (gdal/g? data) (gdal/join-strings ", " data)
    (number? data) (i18n/localized-number data)
    (and (map? data)
         (get data :content)) (get data :content)
    (and (object? data)
         (aget data "content")) (aget data "content")
    :else data))

(defn- recalculate-font [text font-size width height is-number?]
  (if-not (and (string? text)
               (seq text))
    {:font-size font-size
     :text text}
    (let [max-lines (if (and (not is-number?)
                             font-size height (> height 0))
                      (Math/floor (/ (max height font-size)
                                     font-size))
                      1)
          font-size (cond
                      is-number? (scale-text text font-size width height)
                      :else font-size)
          text (cond-> text
                 (not is-number?)
                 (calculate-pruned-text font-size width height max-lines))]
      {:font-size font-size
       :text text})))

(defn draw-text
  [element x y data mapping instance stage]
  (when (and (not (-> element :svg :path))
             (> (:ml element) 0))
    (let [text (cond
                 (vector? mapping)
                 (doall
                  (reduce (fn [i j]
                            (str i ", " (name j)
                                 ": "
                                 (handle-raw-data (gdal/get data j))
                                 " "))
                          (str (name (first mapping))
                               " : "
                               (handle-raw-data (gdal/get data (first mapping))))
                          (rest mapping)))
                 (string? mapping)
                 (str (handle-raw-data (gdal/get data mapping)))
                 (nil? mapping)
                 ""
                 (and (map? mapping)
                      (= :label (:type mapping)))
                 (str (handle-raw-data (:value mapping)))
                 :else
                 (str (handle-raw-data mapping)))
          x1 (+ x (:x element) (nth (:p element) 3))
          y1 (+ y (:y element) (nth (:p element) 0))
          org-font-size (or (:s (:f element))
                            14)
          font-color (or (:c (:f element))
                         [0 0 0])
          w (- (:w element) (nth (:p element) 1))
          h (- (:h element) (nth (:p element) 2))
          {:keys [font-size text]} (recalculate-font text
                                                     org-font-size
                                                     w
                                                     h
                                                     (and (string? mapping)
                                                          (number? (gdal/get data mapping))))
          y1 (cond-> y1
               (not= font-size org-font-size)
               (+ (- org-font-size font-size)))
          ;va (-> element :f :vertical-align)
          ha (-> element :f :horizontal-align)]
          ;lh (* (-> element :f :height) font-size)]
      ;
      (gre/text instance
                stage
                text
                x1
                y1
                w
                {:font-size font-size
                 :font-color font-color
                 :algin ha})
      #_(when (or va ha)
          (q/text-align
           (or va :baseline)))
      #_(when lh
          (q/text-leading lh))

      #_(q/text text x1 y1 w h))))

(defn draw-text-new
  [instance stage x y w h data label-dict attribute props]
  (let [text (cond
               (and (nil? data)
                    label-dict)
               (get label-dict attribute (handle-raw-data attribute))
               (nil? data)
               (str (handle-raw-data attribute))
               (vector? attribute)
               (doall
                (reduce (fn [i j]
                          (str i ", " (name j)
                               ": "
                               (handle-raw-data (gdal/get data j))
                               " "))
                        (str (name (first attribute))
                             " : "
                             (handle-raw-data (gdal/get data (first attribute))))
                        (rest attribute)))
               (string? attribute)
               (str (handle-raw-data (gdal/get data attribute)))
               (nil? attribute)
               ""
               :else
               (str (handle-raw-data attribute)))
        copy-props (select-keys props [:postfix :debug? :adjust-width?])
        number-attribute? (and (string? attribute)
                               (number? (gdal/get data attribute)))
        org-font-size (if (and (:size-small props)
                               (:size-big props))
                        (if (< (count text) 8)
                          (:size-big props)
                          (:size-small props))
                        (or (:size props)
                            20))
        font-color (or (:color props)
                       "#000000")
        font-size org-font-size
        y (cond-> y
            (not= font-size org-font-size)
            (+ (- org-font-size font-size)))
        ha (cond (:horizontal-align props) {:align (:horizontal-align props)}
                 number-attribute? {:align :left}
                 :else {:align :left})
        va {:vertical-align (or (:vertical-align props) :top)}]
    (gre/text instance
              stage
              text
              x
              y
              w
              h
              (merge {:font-size org-font-size
                      :font-color font-color}
                     ha
                     va
                     copy-props))))
