(ns de.explorama.frontend.mosaic.render.parameter
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.mosaic.data-access-layer :as gdal]
            [de.explorama.frontend.mosaic.data.graph-acs :refer [attr->display-name]]
            [de.explorama.shared.mosaic.group-by-layout :as gbl]
            [de.explorama.frontend.mosaic.operations.tasks :as tasks]
            [de.explorama.frontend.mosaic.render.common :as grc]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [de.explorama.frontend.mosaic.vis.config :as gvc]
            ["pixi.js" :refer [Color]]
            [de.explorama.shared.mosaic.common-paths :as gcp]))

(defrecord ContextParams [cpl-ctn
                          plain-cpl
                          sibling-max-ctn
                          count-ctn
                          width-ctn
                          height-ctn
                          header-ctn
                          margin-ctn
                          width
                          height
                          header
                          min-zoom
                          max-zoom
                          bb-max-x
                          bb-max-y
                          bb-min-x
                          bb-min-y])

(defrecord Context [is-root?
                    ctx-type
                    render-type
                    optional-desc
                    params
                    title
                    offset-absolute
                    factor-overview])

(defn offset [{:keys [margin-grp]}
              {:keys [header width height]}
              {parent-cpl :cpl}
              [current-offset-x current-offset-y]
              idx]
  (if (and idx
           parent-cpl)
    [(+ current-offset-x
        (* (+ width
              margin-grp
              margin-grp)
           (mod idx parent-cpl)))
     (+ current-offset-y
        (* (+ header
              height
              margin-grp
              margin-grp)
           (Math/floor (/ idx
                          parent-cpl))))]
    [current-offset-x current-offset-y]))


(defn normal-bb [card-width card-height card-margin width-window height-window width height header]
  (let [min-zoom (grc/min-zoom card-width card-height card-margin width-window height-window)
        max-zoom (grc/max-zoom width
                               (+ header height)
                               0 1 1
                               width-window
                               height-window)
        bb-max-x width
        bb-max-y height
        bb-min-x 0
        bb-min-y 0]
    [min-zoom
     max-zoom
     bb-max-x
     bb-max-y
     bb-min-x
     bb-min-y]))

(defn scatter-bb [card-width card-height card-margin width-window height-window width height header
                  {:keys [scale-window-width scale-window-height scale-window-margin]}]
  (let [min-zoom (grc/min-zoom card-width card-height card-margin width-window height-window)
        max-zoom (grc/max-zoom width
                               (+ header height)
                               0 1 1
                               (- width-window scale-window-width scale-window-margin)
                               (- height-window scale-window-height scale-window-margin))
        bb-max-x width
        bb-max-y height
        bb-min-x 0
        bb-min-y 0]
    [min-zoom
     max-zoom
     bb-max-x
     bb-max-y
     bb-min-x
     bb-min-y]))

(defn params [{:keys [event-count cpl plain-cpl width height header container-type]}
              {width-child :width height-child :height  header-child :header}
              {width-window :width height-window :height}
              {{:keys [card-width card-height card-margin margin-grp]} :constraints}
              _path
              render-type
              optional-desc]
  (let [[min-zoom
         max-zoom
         bb-max-x
         bb-max-y
         bb-min-x
         bb-min-y]
        (if (= :scatter render-type)
          (scatter-bb card-width card-height card-margin width-window height-window width height header optional-desc)
          (normal-bb card-width card-height card-margin width-window height-window width height header))]
    (ContextParams.
     ;cpl-ctn
     cpl
     ;plain-cpl
     plain-cpl
     ;sibling-max-ctn
     event-count
     ;count-ctn
     event-count
     (if (= :leaf container-type) card-width width-child)
     (if (= :leaf container-type) card-height height-child)
     (if (= :leaf container-type) 0 header-child)
     (if (= :leaf container-type) card-margin margin-grp)
     ;width
     width
     ;height
     height
     ;header
     header
     min-zoom
     max-zoom
     bb-max-x
     bb-max-y
     bb-min-x
     bb-min-y)))

(defn calc-overview [width-window grp-params plain-cpl cpl {:keys [subgroup-count subgrouped?]}]
  (cond (and (< 0 (:header-ctn grp-params))
             (not subgrouped?))
        (/ 24 (:header-ctn grp-params))
        (and (< 0 (:header-ctn grp-params))
             subgrouped?)
        (/ (Math/ceil (* 24 (Math/sqrt subgroup-count))) (:header-ctn grp-params))
        :else
        (/ (* 4 width-window)
           (* (:width grp-params) (/ plain-cpl cpl)))))

(defn group-modify [lookup-table grp-name grp-attribute labels lang]
  (cond (= grp-attribute "layout")
        (let [layout-id (aget grp-name "id")
              color-code (aget grp-name "color")]
          (gbl/get-group-text lookup-table layout-id color-code attr->display-name labels i18n/localized-number))
        (= grp-attribute "month")
        (i18n/month-name (js/parseInt grp-name) lang)
        :else
        grp-name))

(defn title [grp-key operations-desc cards-count layout-lookup attribute-labels parent-title lang]
  (let [data-value (gdal/get grp-key "key")
        grp-attr (gdal/get grp-key "attr")
        group-name (group-modify layout-lookup
                                 data-value
                                 grp-attr
                                 attribute-labels
                                 lang)
        aggregated-value (gdal/get grp-key "aggregated-value")
        i18n-cards-count (i18n/localized-number cards-count)
        {:keys [attr method by]} (tasks/sort-desc-from-operations-desc operations-desc grp-attr)]
    (cond-> {:short-title
             (str (if group-name group-name "")
                  " ("
                  i18n-cards-count
                  " Events"
                  (cond-> ""
                    (and aggregated-value
                         (= :aggregate by))
                    (str " | "  (i18n/localized-number aggregated-value) " " (i18n/attribute-label attribute-labels attr))
                    method
                    (str " (" (name method) ")")
                    :always
                    (str ")")))
             :i18n-cards-count i18n-cards-count
             :aggregated-value (i18n/localized-number aggregated-value)
             :attr attr
             :grp-attr grp-attr
             :grp-attr-val data-value
             :name group-name
             :cards-count cards-count}
      parent-title
      (assoc :parent-title parent-title))))

(defn update-bb [{:keys [render-type]
                  {:keys [width height header]}
                  :params
                  optional-desc :optional-desc
                  :as context}
                 width-window height-window
                 {:keys [card-width card-height card-margin]}]
  (let [[min-zoom
         max-zoom
         bb-max-x
         bb-max-y
         bb-min-x
         bb-min-y]
        (if (= :scatter render-type)
          (scatter-bb card-width card-height card-margin width-window height-window width height header optional-desc)
          (normal-bb card-width card-height card-margin width-window height-window width height header))]
    (update context
            :params
            assoc
            :min-zoom min-zoom
            :max-zoom max-zoom
            :bb-max-x bb-max-x
            :bb-max-y bb-max-y
            :bb-min-x bb-min-x
            :bb-min-y bb-min-y)))

(defn calc-width [cpl width margin]
  (* cpl (+ width margin margin)))

(defn calc-height [cpl event-count height margin]
  (* (Math/ceil (/ event-count cpl))
     (+ height margin margin)))

(defn calc-cpl [path-length adjust? {:keys [contexts] :as state} group-count width-window height-window card-width card-height card-margin]
  (let [{:keys [zoom z]} (get state [:pos pc/main-stage-index])
        z
        (if (zero? zoom)
          (* z (get-in contexts [pc/main-stage-index [] :factor-overview]))
          z)]
    [(grc/calculate-cards-per-line group-count
                                   {:width width-window
                                    :height height-window}
                                   {:cwidth (+ card-width card-margin card-margin)
                                    :cheight (+ card-height card-margin card-margin)})
     (cond (and (= 0 path-length)
                (= :vertical adjust?))
           (max 1 (Math/floor (/ (/ width-window z)
                                 (+ card-margin card-margin card-width))))
           (and (= 0 path-length)
                (= :horizontal adjust?))
           (Math/ceil (/ group-count
                         (max 1 (Math/floor (/ (/ height-window z)
                                               (+ card-margin card-margin card-height))))))
           (and (= 0 path-length)
                (= :one-row adjust?))
           group-count
           (and (= 0 path-length)
                (:cpl adjust?))
           (:cpl adjust?)
           :else
           (grc/calculate-cards-per-line group-count
                                         {:width width-window
                                          :height height-window}
                                         {:cwidth (+ card-width card-margin card-margin)
                                          :cheight (+ card-height card-margin card-margin)}))]))

;;; ======== TREEEMAP TILING START ======================================

(defn subtiling [res remaining remaining-events current-x current-y max-x max-y]
  (if (empty? remaining)
    res
    (let [{tile-path :path tile-events :event-count} (first remaining)
          event-share (/ tile-events remaining-events)
          range-x (- max-x current-x)
          range-y (- max-y current-y)
          x-direction? (> range-x range-y)
          change-x (if x-direction? (* event-share range-x) 0)
          change-y (if x-direction? 0 (* event-share range-y))]
      (recur (assoc res tile-path {:start-x current-x
                                   :start-y current-y
                                   :end-x (if x-direction? (+ current-x change-x) max-x)
                                   :end-y (if x-direction? max-y (+ current-y change-y))})
             (rest remaining)
             (- remaining-events tile-events)
             (+ current-x change-x)
             (+ current-y change-y)
             max-x
             max-y))))

(def tiling-step-threshold 0.33)

(defn tiling [data res min-x min-y max-x max-y path]
  (let [{:keys [element-type group-count event-count]} (get data path)
        children (if (= element-type :group)
                   (map #(let [cpath (conj path %)]
                           (assoc (get data cpath) :path cpath))
                        (range group-count))
                   (list))]
    (loop [remaining (sort-by :event-count > children)
           selected (list)
           remaining-events event-count
           result res
           current-x min-x
           current-y min-y]
      (if (and (empty? remaining) (empty? selected))
        result
        (let [used-events (apply + (map :event-count selected))]
          (if (or (> used-events
                     (* remaining-events tiling-step-threshold))
                  (empty? remaining))
            (let [event-share (/ used-events remaining-events)
                  range-x (- max-x current-x)
                  range-y (- max-y current-y)
                  x-direction? (> range-x range-y)
                  change-x (if x-direction? (* event-share range-x) 0)
                  change-y (if x-direction? 0 (* event-share range-y))
                  placed-tiles (subtiling {}
                                          selected
                                          used-events
                                          current-x
                                          current-y
                                          (if x-direction? (+ change-x current-x) max-x)
                                          (if x-direction? max-y (+ change-y current-y)))
                  subgroups (mapv (fn [[path {:keys [start-x start-y end-x end-y]}]]
                                    (tiling data
                                            {}
                                            start-x
                                            start-y
                                            end-x
                                            end-y
                                            path))
                                  placed-tiles)]
              ;;add subgroups
              (recur remaining
                     (list)
                     (- remaining-events used-events)
                     (merge result placed-tiles (apply merge subgroups))
                     (+ change-x current-x)
                     (+ change-y current-y)))
            (recur (rest remaining)
                   (conj selected (first remaining))
                   remaining-events
                   result
                   current-x
                   current-y)))))))

(def ^:private magic-factor 1)

(defn calc-tiles [data {:keys [width height]}]
  (let [width (* magic-factor width)
        height (* magic-factor height)
        {:keys [element-type]} (get data [])]
    (if (= element-type :leaf)
      {[] {:start-x 0
           :start-y 0
           :end-x width
           :end-y height}}
      (tiling data {} 0 0 width height []))))

(defn- tile-group-2 [child-map leaf->root res path cur-x cur-y width height split-height]
  (let [children (get child-map path)
        {event-count-parent :event-count} (get leaf->root path)]
    (loop [children children
           cur [cur-x cur-y]
           res res]
      (if (empty? children)
        res
        (let [[cur-x cur-y] cur
              cur-path (first children)
              {:keys [event-count element-type]} (get leaf->root cur-path)
              porp (/ event-count event-count-parent)
              [cur-width cur-height]
              (if split-height
                [width (* porp height)]
                [(* porp width) height])
              res (if (= element-type :group)
                    (tile-group-2 child-map leaf->root res cur-path cur-x cur-y cur-width cur-height (not split-height))
                    res)
              [next-x next-y]
              (if split-height
                [cur-x (+ cur-y cur-height)]
                [(+ cur-x cur-width) cur-y])]
          (recur (rest children)
                 [next-x next-y]
                 (assoc res
                        cur-path
                        {:start-x cur-x
                         :start-y cur-y
                         :end-x (+ cur-x cur-width)
                         :end-y (+ cur-y cur-height)})))))))

(defn- calc-tiles-2 [leaf->root {:keys [width height]}]
  (let [width width
        height height
        child-map (->> (reduce (fn [acc [path {event-count :event-count}]]
                                 (if (= path [])
                                   acc
                                   (update acc (pop path) (fnil conj []) [event-count path])))
                               {}
                               leaf->root)
                       (map (fn [[key children]]
                              [key (->> (sort-by (fn [[event-count]] event-count)
                                                 #(compare %2 %1)
                                                 children)
                                        (mapv (fn [[_ path]] path)))]))
                       (into {}))]
    (tile-group-2 child-map leaf->root {} [] 0 0 width height false)))

(defn- tile-group-3 [binary-trees res path parent-cur-x parent-cur-y parent-width parent-height split-height]
  (let [[size children] (first (get binary-trees path))]
    (loop [current-node
           [[size parent-cur-x parent-cur-y parent-width parent-height (not split-height) 0 size children]]
           res res]
      (if (empty? current-node)
        res
        (let [[parent-size parent-cur-x parent-cur-y width height split-height place children-size children]
              (peek current-node)
              porp (/ children-size parent-size)
              [cur-width cur-height]
              (if split-height
                [width
                 (* porp height)]
                [(* porp width)
                 height])
              [next-x next-y]
              (if split-height
                [parent-cur-x
                 (+ parent-cur-y (* place (- 1 porp) height))]
                [(+ parent-cur-x (* place (- 1 porp) width))
                 parent-cur-y])]
          (recur (if (vector? children)
                   (let [[child0-size children0] (first children)
                         [child1-size children1] (second children)]
                     (conj (pop current-node)
                           [children-size next-x next-y cur-width cur-height (not split-height) 0 child0-size children0]
                           [children-size next-x next-y cur-width cur-height (not split-height) 1 child1-size children1]))
                   (pop current-node))
                 (cond (vector? children)
                       res
                       (seq (get binary-trees (:path children)))
                       (tile-group-3 binary-trees
                                     (assoc res (:path children)
                                            {:start-x next-x
                                             :start-y next-y
                                             :end-x (+ next-x cur-width)
                                             :end-y (+ next-y cur-height)})
                                     (:path children)
                                     next-x next-y
                                     cur-width cur-height
                                     (not split-height))
                       :else
                       (assoc res (:path children) {:start-x next-x
                                                    :start-y next-y
                                                    :end-x (+ next-x cur-width)
                                                    :end-y (+ next-y cur-height)}))))))))

(defn- calc-tree-binary [child-map]
  (loop [child-map child-map]
    (if (= 1 (count child-map))
      child-map
      (let [[count1 :as last1] (peek child-map)
            pop1 (pop child-map)
            [count2 :as last2] (peek pop1)
            pop2 (pop pop1)]
        (recur (vec (sort-by (fn [[event-count]] event-count)
                             #(compare %2 %1)
                             (conj pop2
                                   [(+ count1 count2) [last2 last1]]))))))))

(defn- calc-tiles-3 [leaf->root {:keys [width height]}]
  (let [width width
        height height
        child-map (->> (reduce (fn [acc [path {event-count :event-count}]]
                                 (if (= path [])
                                   acc
                                   (update acc (pop path) (fnil conj []) [event-count {:path path}])))
                               {}
                               leaf->root)
                       (map (fn [[key children]]
                              [key (vec (sort-by (fn [[event-count]] event-count)
                                                 #(compare %2 %1)
                                                 children))]))
                       (into {}))
        binary-trees (reduce (fn [acc [path children]]
                               (assoc acc path (calc-tree-binary children)))
                             {}
                             child-map)]
    (tile-group-3 binary-trees {} [] 0 0 width height false)))

(defn calc-root->leaf-tree [data
                            operations-desc
                            leaf->root
                            tiles
                            {width-window :width height-window :height}
                            {{:keys [card-width card-height card-margin]} :constraints}
                            layout-lookup
                            attribute-labels
                            lang]
  (let [putil (.-utils js/PIXI)
        {:keys [cpl factor]} (reduce (fn [{factor :factor :as acc} [path {:keys [start-x start-y end-x end-y]}]]
                                       (let [{:keys [event-count]} (get leaf->root path)
                                             tile-width (- end-x start-x)
                                             tile-height (- end-y start-y)
                                             cwidth (+ card-width card-margin card-margin)
                                             cheight (+ card-height card-margin card-margin)
                                             cpl (grc/calculate-cards-per-line event-count
                                                                               {:width tile-width
                                                                                :height tile-height}
                                                                               {:cwidth cwidth
                                                                                :cheight cheight})
                                             x-factor (/ (* cpl cwidth)
                                                         tile-width)
                                             y-factor (/ (* (Math/ceil (/ event-count cpl)) cheight)
                                                         tile-height)]
                                         (-> (assoc-in acc [:cpl path] cpl)
                                             (assoc :factor (max factor x-factor y-factor)))))
                                     {}
                                     tiles)
        real-sizes (reduce (fn [acc [path {:keys [start-x start-y end-x end-y] :as desc}]]
                             (assoc
                              acc path
                              (assoc (merge desc (get leaf->root path))
                                     :start-x (* start-x factor)
                                     :start-y (* start-y factor)
                                     :end-x (* end-x factor)
                                     :end-y (* factor end-y)
                                     :plain-cpl (get cpl path)
                                     :cpl (get cpl path)
                                     :header 0)))
                           {}
                           tiles)
        min-zoom (grc/min-zoom card-width card-height card-margin width-window height-window)
        max-zoom (min (/ 1 (* factor magic-factor)))
        factor-overview 1
        root-element (let [{:keys [event-count event-count-max group-count group-count-max]} (get leaf->root [])
                           width (* magic-factor factor width-window)
                           height (* magic-factor factor height-window)
                           cpl (Math/floor (/ width event-count))
                           grp-params (ContextParams.
                                       cpl
                                       cpl
                                       event-count
                                       event-count
                                       card-width
                                       card-height
                                       0
                                       card-margin
                                       width
                                       height
                                       0
                                       min-zoom
                                       max-zoom
                                       width
                                       height
                                       0
                                       0)]
                       (Context. true
                                 :group
                                 :treemap
                                 {:cpl cpl
                                  :group-count group-count
                                  :group-count-max group-count-max
                                  :start-x 0
                                  :start-y 0
                                  :end-x 0
                                  :end-y 0
                                  :header 0
                                  :event-count event-count
                                  :event-count-max event-count-max}
                                 grp-params
                                 {}
                                 [0 0]
                                 factor-overview))]
    (if (empty? (dissoc real-sizes nil))
      {[] root-element}
      (reduce (fn [acc [path real-size]]
                (let [{event-count :event-count
                       plain-cpl :plain-cpl
                       start-x :start-x
                       end-x :end-x
                       end-y :end-y
                       start-y :start-y
                       cpl :cpl
                       element-type :element-type
                       :or {event-count 0}}
                      real-size
                      pair (gdal/get-in data (pc/data-path path))
                      grp-key (gdal/first pair)
                      current-data (gdal/second pair)
                      absolute-offset [start-x start-y]
                      grp-params (ContextParams.
                                  cpl
                                  plain-cpl
                                  event-count
                                  event-count
                                  card-width
                                  card-height
                                  0
                                  card-margin
                                  (- end-x start-x)
                                  (- end-y start-y)
                                  0
                                  min-zoom
                                  max-zoom
                                  end-x
                                  end-y
                                  start-x
                                  start-y)]
                  (assoc acc path (Context. (= path [])
                                            element-type
                                            :treemap
                                            (assoc real-size
                                                   :color
                                                   (let [values-rgb (map (fn [event]
                                                                           (let [color (gdal/first event)]
                                                                             (when (string? color)
                                                                               (-> (Color. color)
                                                                                   (.toRgbArray)))))
                                                                         current-data)
                                                         acc-values-rgb (reduce (fn [acc rgb]
                                                                                  (if rgb
                                                                                    (mapv + acc rgb)
                                                                                    acc))
                                                                                [0 0 0]
                                                                                values-rgb)]
                                                     (mapv #(* 255 (/ % (count values-rgb))) acc-values-rgb)))
                                            grp-params
                                            (title grp-key
                                                   operations-desc
                                                   event-count
                                                   layout-lookup
                                                   attribute-labels
                                                   (if (= 2 (count path))
                                                     (let [parent-path [(first path)]
                                                           {event-count :event-count
                                                            :or {event-count 0}}
                                                           (get real-sizes parent-path)
                                                           pair (gdal/get-in data (pc/data-path parent-path))
                                                           grp-key (gdal/first pair)]
                                                       (title grp-key
                                                              operations-desc
                                                              event-count
                                                              layout-lookup
                                                              attribute-labels
                                                              nil
                                                              lang))
                                                     nil)
                                                   lang)
                                            absolute-offset
                                            factor-overview))))
              {[] root-element}
              real-sizes))))

;;; ======== TREEMAP TILING END ======================================

(defn calc-root->leaf [data
                       operations-desc
                       leaf->root?
                       sizes-per-level
                       {width-window :width :as args}
                       {constraints :constraints :as state}
                       render-type
                       optional-desc
                       layout-lookup
                       attribute-labels
                       lang]
  (let [real-sizes (if (vector? leaf->root?)
                     (second leaf->root?)
                     leaf->root?)
        leaf->root
        (if (vector? leaf->root?)
          (first leaf->root?)
          leaf->root?)
        group-sizes (reduce-kv (fn [{subgrouped? :subgrouped? :as acc} k v]
                                 (cond-> acc
                                   (and (not subgrouped?)
                                        (= 2 (count k)))
                                   (assoc :subgrouped? true)
                                   (= 1 (count k))
                                   (update :subgroup-count max (:group-count-max v))))
                               {:subgrouped? false
                                :group-count-max 0}
                               leaf->root)]
    (loop [result {}
           paths (vec (sort (fn [a b]
                              (compare (count (first b))
                                       (count (first a))))
                            leaf->root))
           offsets {}]
      (if (empty? paths)
        result
        (let [path (first (peek paths))
              {element-type :element-type
               event-count :event-count
               :or {element-type :leaf
                    event-count 0}}
              (get real-sizes path)
              pair (gdal/get-in data (pc/data-path path))
              grp-key (gdal/first pair)
              {parent-absolute-offset :offset-absolute
               factor-overview :factor-overview}
              (if (= path [])
                {:offset-absolute [0 0]
                 :factor-overview 1}
                (get offsets (pop path)))
              {:keys [header plain-cpl cpl] :as sizes} (get sizes-per-level (count path))
              sizes-parent (get sizes-per-level (dec (count path)))
              sizes-child (get sizes-per-level (inc (count path)))
              absolute-offset (offset constraints sizes sizes-parent parent-absolute-offset (peek path))
              grp-params (params sizes sizes-child args state path render-type optional-desc)
              factor-overview
              (if (= path [])
                (calc-overview width-window grp-params plain-cpl cpl group-sizes)
                factor-overview)]
          (if (= element-type :leaf)
            (recur (assoc result path (Context. (= path [])
                                                :leaf
                                                (or render-type :raster)
                                                (or optional-desc nil)
                                                grp-params
                                                (title grp-key
                                                       operations-desc
                                                       event-count
                                                       layout-lookup
                                                       attribute-labels
                                                       nil
                                                       lang)
                                                absolute-offset
                                                factor-overview))
                   (pop paths)
                   offsets)
            (recur (assoc result path (Context. (= path [])
                                                :group
                                                (or render-type :raster)
                                                (or optional-desc nil)
                                                grp-params
                                                (title grp-key
                                                       operations-desc
                                                       event-count
                                                       layout-lookup
                                                       attribute-labels
                                                       nil
                                                       lang)
                                                absolute-offset
                                                factor-overview))
                   (pop paths)
                   (assoc offsets path {:offset-absolute (if (< 0 (count path))
                                                           (update absolute-offset 1 + header)
                                                           absolute-offset)
                                        :factor-overview factor-overview}))))))))

(defn calc-sizes-per-level [{{:keys [card-width card-height card-margin margin-grp]} :constraints :as state}
                            {width-window :width height-window :height}
                            leaf->root adjust?]
  (let [leaf->root (if (vector? leaf->root)
                     (first leaf->root)
                     leaf->root)]
    (reduce (fn [acc [path-length grouped-values]]
              (let [{:keys [event-count group-count]}
                    (reduce (fn [acc [_ {group-count :group-count
                                         event-count :event-count}]]
                              (-> (update acc :group-count max group-count)
                                  (update :event-count max event-count)))
                            {:group-count 0
                             :event-count 1}
                            grouped-values)
                    {:keys [element-type]} (get-in grouped-values [0 1])]
                (assoc acc path-length (if (= :leaf element-type)
                                         (let [[plain-cpl cpl]
                                               (if (zero? path-length)
                                                 (calc-cpl path-length adjust? state event-count width-window height-window card-width card-height card-margin)
                                                 (calc-cpl path-length adjust? state event-count gvc/mosaic-top-width gvc/mosaic-top-height card-width card-height card-margin))
                                               height (calc-height cpl event-count card-height card-margin)]
                                           {:container-type :leaf
                                            :width (calc-width cpl card-width card-margin)
                                            :height height
                                            :header (if (< 0 path-length)
                                                      (* height 0.08)
                                                      0)
                                            :event-count event-count
                                            :cpl cpl
                                            :plain-cpl plain-cpl})
                                         (let [{child-width :width child-height :height child-header :header} (get acc (inc path-length))
                                               [plain-cpl cpl]
                                               (calc-cpl path-length adjust? state group-count width-window height-window child-width child-height margin-grp)
                                               width-grp  (calc-width cpl child-width margin-grp)
                                               height-grp (calc-height cpl group-count (+ child-header child-height) margin-grp)]
                                           {:container-type :group
                                            :width width-grp
                                            :height height-grp
                                            :header (if (< 0 path-length)
                                                      (* height-grp 0.08)
                                                      0)
                                            :event-count group-count
                                            :cpl cpl
                                            :plain-cpl plain-cpl})))))
            {}
            (->
             (group-by (fn [[path _]]
                         (count path))
                       leaf->root)
             sort
             reverse))))

(defn nested-reduce-stacked [func init data-raw]
  (reduce (fn [acc path]
            (func acc (get-in data-raw path) path))
          init
          (loop [paths [[]]
                 path-stack []]
            (if (empty? paths)
              (reverse path-stack)
              (let [path (peek paths)
                    element (gdal/get-in data-raw path)]
                (if (and element
                         (gdal/vec? element)
                         (= (gdal/count element) 2)
                         (gdal/vec? (gdal/second element)))
                  (let [child-values (gdal/second element)]
                    (recur
                     (reduce (fn [paths idx]
                               (conj paths
                                     (conj path 1 idx)))
                             (pop paths)
                             (range (gdal/count child-values)))
                     (conj path-stack path)))
                  (recur
                   (pop paths)
                   (if (= [] path)
                     (conj path-stack path)
                     path-stack))))))))

(defn children-group-key? [value]
  (and value
       (gdal/vec? value)
       (gdal/first value)
       (gdal/vec? (gdal/first value))
       (= 2 (gdal/count (gdal/first value)))
       (gdal/map? (gdal/first (gdal/first value)))))

(defn empty-group? [value]
  (and value
       (gdal/vec? value)
       (= 0 (gdal/count value))))

(defn calc-leaf->root [data]
  (nested-reduce-stacked
   (fn [acc element path]
     (let [path (->> (map-indexed vector path)
                     (filter (fn [[idx]] (odd? idx)))
                     (map (fn [[_ val]] val))
                     vec)]
       (assoc acc path (cond (and element
                                  (gdal/vec? element)
                                  (= 2 (gdal/count element))
                                  (children-group-key? (gdal/second element)))
                             (let [value (gdal/second element)
                                   children-keys (gdal/count value)]
                               (reduce (fn [element-acc child-key]
                                         (-> (update element-acc
                                                     :event-count
                                                     +
                                                     (get-in acc [(conj path child-key) :event-count]))
                                             (update :event-count-children-max
                                                     max
                                                     (get-in acc [(conj path child-key) :event-count]))))
                                       {:element-type :group
                                        :group-count children-keys
                                        :group-count-max children-keys}
                                       (range children-keys)))
                             (and element
                                  (gdal/vec? element)
                                  (= 2 (gdal/count element))
                                  (empty-group? (gdal/second element)))
                             {:element-type :group
                              :event-count 0
                              :event-count-children-max 0
                              :group-count 1
                              :group-count-max 1}
                             (and element
                                  (gdal/vec? element)
                                  (= 2 (gdal/count element))
                                  (gdal/vec? (gdal/second element))
                                  (gdal/map? (gdal/first element)))
                             (let [children-keys (gdal/count (gdal/second element))]
                               {:element-type :leaf
                                :event-count children-keys
                                :event-count-max children-keys
                                :group-count 1
                                :group-count-max 1})
                             :else
                             (let [event-count (gdal/count element)]
                               {:element-type :leaf
                                :event-count event-count
                                :event-count-max event-count
                                :group-count 1
                                :group-count-max 1})))))
   {}
   data))

(defn grp-contexts [data
                    scale-desc
                    operations-desc
                    state
                    {optional-desc-ex :optional-desc :as args}
                    [adjust? group-sizes]
                    layout-lookup
                    attribute-labels
                    lang]
  (case (:type operations-desc)
    :scatter
    (let [{{:keys [card-width card-height card-margin]} :constraints} state
          {x-length :x-value-count y-length :y-value-count
           :as optional-desc}
          (assoc scale-desc
                 :x-label (i18n/attribute-label attribute-labels (:x-label scale-desc))
                 :y-label (i18n/attribute-label attribute-labels (:y-label scale-desc)))
          optional-desc (merge optional-desc-ex
                               optional-desc)
          sizes-per-level {0 {:container-type :leaf
                              :width (calc-width x-length card-width card-margin)
                              :height (calc-height x-length (* x-length y-length) card-height card-margin)
                              :header 0
                              :event-count (* x-length y-length)
                              :cpl x-length
                              :plain-cpl x-length}}]
      (calc-root->leaf data
                       operations-desc
                       {[] {:element-type :leaf
                            :event-count (* x-length y-length)
                            :event-count-max (* x-length y-length)
                            :group-count 1
                            :group-count-max 1}}
                       sizes-per-level
                       args
                       state
                       :scatter
                       optional-desc
                       layout-lookup
                       attribute-labels
                       lang))
    :treemap
    (let [leaf->root (calc-leaf->root data)
          tiles (condp = (get operations-desc gcp/treemap-algorithm)
                  "squared" (calc-tiles leaf->root args)
                  "slice" (calc-tiles-2 leaf->root args)
                  "binary" (calc-tiles-3 leaf->root args)
                  (calc-tiles-3 leaf->root args))]
      (calc-root->leaf-tree data operations-desc leaf->root tiles args state layout-lookup
                            attribute-labels lang))
    (nil :raster)
    (let [leaf->root (if group-sizes
                       [(:group-sizes group-sizes) (calc-leaf->root data)]
                       (calc-leaf->root data))
          sizes-per-level (calc-sizes-per-level state args leaf->root adjust?)]
      (calc-root->leaf data operations-desc leaf->root sizes-per-level args state nil optional-desc-ex layout-lookup
                       attribute-labels lang))))
