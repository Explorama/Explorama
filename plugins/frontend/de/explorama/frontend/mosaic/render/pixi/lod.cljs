(ns de.explorama.frontend.mosaic.render.pixi.lod
  (:require [de.explorama.frontend.common.tubes :as tubes]
            [de.explorama.frontend.mosaic.interaction.state :as tooltip]
            [de.explorama.frontend.mosaic.render.cache :as grc]
            [de.explorama.frontend.mosaic.render.engine :as gre]
            [de.explorama.frontend.mosaic.render.pixi.common :as pc]
            [de.explorama.frontend.mosaic.render.pixi.shapes :as grps]
            [de.explorama.shared.mosaic.ws-api :as ws-api]
            [taoensso.timbre :refer [error]]))

(defn- index-access [zoom]
  (if (< 0 zoom)
    1
    0))

(defn render-type [{render-type :render-type}]
  (case render-type
    :scatter :scatter
    :raster :raster
    :treemap :treemap
    :raster))

(defn- custom-data [state render-funcs stage-key path data-path row-major-index grouped? ctx]
  (cond (= pc/main-stage-index stage-key)
        ((get-in render-funcs [(render-type ctx) :data]) path data-path row-major-index grouped? ctx)
        (map? (get-in state [:custom-data stage-key]))
        ((get-in render-funcs [(render-type ctx) :data])
         path
         (get-in state [:custom-data stage-key :path])
         row-major-index
         (get-in state [:custom-data stage-key :grouped?])
         ctx)
        :else
        [(get-in state [:custom-data stage-key row-major-index])]))

(defn- discard [this stage-key zoom]
  (let [app (gre/app this)
        stage (pc/main-container (pc/zoom-context-stage app stage-key) stage-key)
        container (.removeChildAt stage (index-access zoom))]
    (.destroy container (clj->js {:children true}))))

(defn- calculate-proj [factor-overview zoom value]
  (if (= 0 zoom)
    (/ value factor-overview)
    value))

(defn- ctx-cal [{:keys [width height]}
                {:keys [x y z zoom]}
                {{:keys [width-ctn height-ctn header-ctn margin-ctn cpl-ctn count-ctn header]} :params
                 [offset-absolute-x offset-absolute-y] :offset-absolute
                 :keys [factor-overview ctx-type]}]
  (let [grouped? (= :group ctx-type)
        size-x (calculate-proj factor-overview zoom (/ width z))
        size-y (calculate-proj factor-overview zoom (/ height z))
        ox (calculate-proj factor-overview zoom (/ (- x) z))
        x (- ox offset-absolute-x)
        oy (calculate-proj factor-overview zoom (/ (- y) z))
        y (- oy offset-absolute-y)
        min-idx-x (max 0 (Math/floor (/ x
                                        (+ width-ctn
                                           margin-ctn
                                           margin-ctn))))
        min-idx-y (max 0 (Math/floor (/ y
                                        (+ height-ctn
                                           header-ctn
                                           header
                                           margin-ctn
                                           margin-ctn))))
        max-idx-x (if grouped?
                    (min cpl-ctn
                         (max 1
                              (Math/ceil (/ (+ x size-x)
                                            (+ width-ctn
                                               margin-ctn
                                               margin-ctn)))))
                    1)
        max-idx-y (if grouped?
                    (min (Math/ceil (/ count-ctn cpl-ctn))
                         (max 1
                              (Math/ceil (/ (+ y size-y)
                                            (+ height-ctn
                                               header-ctn
                                               margin-ctn
                                               margin-ctn)))))
                    1)]
    {:size-x size-x
     :size-y size-y
     :x ox
     :y oy
     :min-idx-x min-idx-x
     :min-idx-y min-idx-y
     :max-idx-x max-idx-x
     :max-idx-y max-idx-y}))

(defn- ctx-cal-tree [{:keys [width height]}
                     {:keys [x y z zoom]}
                     contexts
                     length]
  (let [{:keys [factor-overview]} (get contexts [])
        size-x (calculate-proj factor-overview zoom (/ width z))
        size-y (calculate-proj factor-overview zoom (/ height z))
        ox (calculate-proj factor-overview zoom (/ (- x) z))
        oy (calculate-proj factor-overview zoom (/ (- y) z))
        visible-context-paths (into []
                                    (comp (filter (fn [[path {{:keys [start-x start-y end-x end-y]} :optional-desc}]]
                                                    (and (< start-x (+ ox size-x))
                                                         (< ox end-x)
                                                         (< start-y (+ oy size-y))
                                                         (< oy end-y)
                                                         (= length (count path)))))
                                          (map first))
                                    contexts)]
    {:size-x size-x
     :size-y size-y
     :x ox
     :y oy
     :visible-context-paths visible-context-paths}))

(defn- ir-calc [{:keys [size-x size-y x y]}
                {{:keys [cpl-ctn count-ctn width height header width-ctn height-ctn margin-ctn]} :params
                 [offset-x offset-y] :offset-absolute}]
  (let [ctn-space-x (+ width-ctn margin-ctn margin-ctn)
        ctn-space-x-correction 0
        ctn-space-y  (+ height-ctn margin-ctn margin-ctn)
        ctn-space-y-correction 0
        x (+ x ctn-space-x-correction)
        y (+ y ctn-space-y-correction)
        ctx-max-x (max (- (min (+ offset-x width)
                               (+ x size-x))
                          offset-x)
                       0)
        ctx-max-y (max (- (min (+ offset-y
                                  header
                                  height)
                               (+ y size-y))
                          (+ offset-y header))
                       0)
        ctx-min-x (- (max offset-x x)
                     offset-x)
        ctx-min-y (- (max offset-y y)
                     (+ offset-y header))
        min-col (max (Math/floor (/ ctx-min-x
                                    ctn-space-x))
                     0)
        max-col (min (Math/ceil (/ ctx-max-x
                                   ctn-space-x))
                     cpl-ctn)
        min-row (max (- (Math/floor (/ ctx-min-y
                                       ctn-space-y))
                        1)
                     0)
        max-row (min (Math/ceil (/ ctx-max-y
                                   ctn-space-y))
                     (Math/ceil (/ count-ctn
                                   cpl-ctn)))]
    [min-col
     max-col
     min-row
     max-row
     ctx-min-x
     ctx-min-y
     ctn-space-x
     ctn-space-y]))

(defn cluster-data [data]
  (let [ct (count data)
        w 520
        w2 (* w 0.5)
        h 552
        h2 (* h 0.5)]
    (loop [i 0
           cluster {}
           cluster-idx {}]
      (if (< i ct)
        (let [[relative-x relative-y]
              (if (vector? (get data i))
                (let [event (get data i)
                      x (get event 2)
                      y (get event 3)]
                  (if (< ct 3)
                    [x y]
                    [w2 h2]))
                [nil nil])]
          (recur (inc i)
                 (update cluster [relative-x relative-y] (fnil conj []) i)
                 (assoc cluster-idx i [relative-x relative-y])))
        [(-> cluster
             vec
             sort)
         cluster-idx]))))

(defn- remove-pixi-obj [container obj destroy?]
  (try
    (.removeChild container obj)
    (when (and obj destroy?)
      (.destroy obj (clj->js {:children true})))
    (catch :default e
      (error e))))

(defn- idx->zoom-level [idx]
  (get idx 3))

(defn- keep-different-stage-indices [all-indices all-old-indices stage-key]
  (reduce (fn [all-indices [[current-stage-key :as key] val]]
            (if (not= current-stage-key stage-key)
              (assoc all-indices key val)
              all-indices))
          all-indices
          all-old-indices))

(defn- update-indices! [this stage-key all-indices all-old-indices]
  (let [app (gre/app this)
        stage (pc/main-container (pc/zoom-context-stage app stage-key) stage-key)
        all-indices (keep-different-stage-indices all-indices all-old-indices stage-key)]
    (when-not (-> stage .-children count zero?)
      (let [indices-to-remove (sort-by (fn [path]
                                         (case (path 1)
                                           :loadscreen 0
                                           :content 0
                                           :wrapper 1
                                           :base 2))
                                       (remove (set (keys all-indices))
                                               (keys all-old-indices)))]
        (doseq [idx indices-to-remove
                :let [[current-stage-key idx-type _ base-zoom] idx]]
          (cond (= idx-type :content)
                (let [[_ _ _ _ render-path row-major-index] idx
                      wrapper-path [current-stage-key :wrapper render-path row-major-index]
                      [_ containers] (get all-old-indices wrapper-path)
                      children (get all-old-indices idx)
                      children-count (count children)]
                  (loop [i 0]
                    (if (< i children-count)
                      (do
                        (remove-pixi-obj (get containers i)
                                         (get children i)
                                         true)
                        (recur (inc i)))
                      :done)))
                (and (= idx-type :base)
                     (= base-zoom 0))
                (when (get-in (gre/state this) [[:pos stage-key] :init 0])
                  (remove-pixi-obj (.getChildAt stage (index-access (idx->zoom-level idx)))
                                   (get all-old-indices idx)
                                   false))
                (and (= idx-type :base)
                     (< 0 base-zoom))
                (when (get-in (gre/state this) [[:pos stage-key] :init 1])
                  (remove-pixi-obj (.getChildAt stage (index-access (idx->zoom-level idx)))
                                   (get all-old-indices idx)
                                   true))
                (= idx-type :wrapper)
                (when (get-in (gre/state this) [[:pos stage-key] :init 1])
                  (remove-pixi-obj (.getChildAt stage (index-access 1))
                                   (get-in all-old-indices [idx 0])
                                   true))))))))

(defn- handle-lower-zl-2 [[indices cache missing-data :as update-state]
                          this stage-key container zoom all-old-indices render-path parent-grouped? _content?]
  (assert (= 3 (count update-state)) "handle-lower-zl-2 - update-state does not contain the right amout of parameters")
  (let [{:keys [constraints] :as state} (gre/state this)
        ctx (get-in state [:contexts stage-key render-path])
        indices-path [stage-key :base :static zoom render-path]]
    (if (and (= zoom 1)
             (or (not= render-path [])
                 (= (render-type ctx) :scatter)))
      (cond (get all-old-indices indices-path)
            [(assoc indices
                    indices-path
                    (get all-old-indices indices-path))
             cache
             missing-data]
            :else
            (let [frame-container (js/PIXI.Container.)
                  capsule-contaier (js/PIXI.Container.)
                  render-funcs (gre/render-funcs this)]
              (.addChild container frame-container)
              (.addChild frame-container capsule-contaier)
              (.addChildAt capsule-contaier (js/PIXI.Graphics.) 0)
              (.addChildAt capsule-contaier (js/PIXI.Graphics.) 1)
              ((get-in render-funcs [(render-type ctx) :frames :static 1])
               this stage-key capsule-contaier constraints ctx parent-grouped? render-path)
              ((get-in render-funcs [(render-type ctx) :frames :dynamic 1])
               this stage-key frame-container constraints ctx render-path)
              [(assoc indices indices-path frame-container)
               cache;(assoc-in cache cache-path frame-container)
               missing-data]))
      [indices cache missing-data])))

(defn- handle-lower-zl [[indices cache missing-data :as update-state]
                        this stage-key container zoom _target-zoom old-indices render-path parent-grouped? content?]
  (assert (= 3 (count update-state)) "handle-lower-zl - update-state does not contain the right amout of parameters")
  (let [{:keys [constraints contexts highlights]} (gre/state this)
        ctx (get-in contexts [stage-key render-path])
        {:keys [z]} (get (gre/state this) [:pos stage-key])
        text-threshold 0.005
        static-indices-path [stage-key :base :static zoom render-path]
        dynamic-indices-path [stage-key :base :dynamic zoom render-path]
        [indices cache missing-data]
        (cond (get old-indices static-indices-path)
              [(assoc indices
                      static-indices-path
                      (get old-indices static-indices-path))
               cache
               missing-data]
              (get cache static-indices-path)
              (let [frame-container (get cache static-indices-path)]
                (.addChild container frame-container)
                [(assoc indices
                        static-indices-path
                        frame-container)
                 cache
                 missing-data])
              :else
              (let [frame-container (js/PIXI.Container.)
                    capsule-contaier (js/PIXI.Container.)
                    comment-container (js/PIXI.Container.)
                    highlight-container (js/PIXI.Container.)
                    render-funcs (gre/render-funcs this)]
                (.addChild container frame-container)
                (.addChild frame-container capsule-contaier)
                (.addChildAt capsule-contaier (js/PIXI.Graphics.) 0)
                (.addChildAt capsule-contaier (js/PIXI.Graphics.) 1)
                (.addChildAt capsule-contaier comment-container 2)
                (.addChildAt capsule-contaier highlight-container 3)
                (.addChildAt highlight-container (js/PIXI.Graphics.) 0)
                ((get-in render-funcs [(render-type ctx) :frames :static 0])
                 this stage-key capsule-contaier constraints ctx parent-grouped? render-path)
                (when content?
                  (let [[relevant-annotations
                         relevant-highlights]
                        ((get-in render-funcs [(render-type ctx) :cards :static zoom])
                         this stage-key render-path capsule-contaier constraints ctx highlights parent-grouped? cluster-data)]
                    (when (and relevant-annotations (not= 0 (count relevant-annotations)))
                      ((get-in render-funcs [(render-type ctx) :annotations-0])
                       this comment-container constraints ctx relevant-annotations))
                    (when (and relevant-annotations (not= 0 (count relevant-annotations)))
                      ((get-in render-funcs [(render-type ctx) :highlights-0])
                       this highlight-container constraints ctx relevant-highlights))))
                [(assoc indices static-indices-path frame-container)
                 (assoc cache static-indices-path frame-container)
                 missing-data]))]
    (cond (and (get old-indices dynamic-indices-path)
               (< text-threshold z))
          [(assoc indices
                  dynamic-indices-path
                  (get old-indices dynamic-indices-path))
           cache
           missing-data]
          (and (< text-threshold z)
               (get indices static-indices-path))
          (let [text-container (js/PIXI.Container.)
                render-funcs (gre/render-funcs this)]
            (.addChild container text-container)
            ((get-in render-funcs [(render-type ctx) :frames :dynamic 0])
             this stage-key text-container constraints ctx render-path)
            [(assoc indices dynamic-indices-path text-container)
             cache
             missing-data])
          :else
          [indices
           cache
           missing-data])))

(defn- cluster-content [[indices cache missing-data]
                        old-indices
                        [cur-row cur-col min-col max-col]
                        this stage-key container zoom render-path constraints ctx
                        row-major-index data static-indices-path loadscreen-static-indices-path
                        highlights grouped?]
  (let [render-funcs (gre/render-funcs this)
        ct (count data)
        [cluster cluster-idx] (cluster-data data)
        containers (loop [i 0
                          result {}]
                     (if (< i ct)
                       (recur (inc i)
                              (if (get result (get cluster-idx i))
                                result
                                (let [current-container (js/PIXI.Container.)]
                                  (.addChild (get container i) current-container)
                                  (.addChildAt current-container (js/PIXI.Graphics.) 0)
                                  (assoc result
                                         (get cluster-idx i)
                                         current-container))))
                       result))
        data-struct (loop [i 0
                           result []]
                      (if (< i ct)
                        (recur (inc i)
                               (conj result
                                     [(get data i)
                                      (get containers (get cluster-idx i))]))
                        result))
        event-cache (grc/get-frame-events (gre/frame-id this))
        available-data (map (fn [datapoint]
                              (let [[bucket id] ((get-in render-funcs [(render-type ctx) :translate-data]) datapoint)]
                                [[bucket id] (get event-cache [bucket id])]))
                            data)
        not-available-data (filter (fn [[_ data]]
                                     (not data))
                                   available-data)
        data-struct [data-struct cluster containers]
        containers (vec (vals containers))]
    (cond (empty? data-struct)
          [(if (<= max-col (inc cur-col))
             [min-col (inc cur-row)]
             [(inc cur-col) cur-row])
           [indices cache missing-data]]
          (and data
               (empty? not-available-data))
          (let [data-struct (update data-struct 0 (fn [value]
                                                    (mapv (fn [[key container]]
                                                            (if (vector? key)
                                                              (let [[[_ id bucket]] key]
                                                                [(get event-cache [bucket id])
                                                                 key
                                                                 container])
                                                              (let [[_ id bucket] key]
                                                                [(get event-cache [bucket id])
                                                                 key
                                                                 container])))
                                                          value)))]
            ((get-in render-funcs [(render-type ctx) :cards :static zoom])
             this stage-key row-major-index ctx data-struct constraints render-path highlights grouped?)
            [(if (<= max-col (inc cur-col))
               [min-col (inc cur-row)]
               [(inc cur-col) cur-row])
             [(assoc indices
                     static-indices-path
                     containers)
              cache
              missing-data]])
          (and data
               (seq not-available-data)
               (= 1 zoom)
               (get old-indices loadscreen-static-indices-path))
          [(if (<= max-col (inc cur-col))
             [min-col (inc cur-row)]
             [(inc cur-col) cur-row])
           [(assoc indices
                   loadscreen-static-indices-path
                   (get old-indices loadscreen-static-indices-path))
            cache
            missing-data]]
          (and data
               (seq not-available-data)
               (= 1 zoom))
          (do
            ((get-in render-funcs [(render-type ctx) :cards :load-screen])
             this stage-key row-major-index ctx data-struct constraints render-path highlights)
            [(if (<= max-col (inc cur-col))
               [min-col (inc cur-row)]
               [(inc cur-col) cur-row])
             [(assoc indices
                     loadscreen-static-indices-path
                     containers)
              cache
              (merge missing-data not-available-data)]])
          :else
          [(if (<= max-col (inc cur-col))
             [min-col (inc cur-row)]
             [(inc cur-col) cur-row])
           [indices
            cache
            missing-data]])))

(defn- handle-higher-zl [[indices cache missing-data :as update-state]
                         this stage-key container zoom old-indices render-path ctx-params grouped?]
  (assert (= 3 (count update-state)) "handle-higher-zl - update-state does not contain the right amout of parameters")
  (let [{:keys [contexts path constraints highlights] :as state} (gre/state this)
        {{:keys [cpl-ctn count-ctn]} :params :as ctx} (get-in contexts [stage-key render-path])
        [min-col max-col min-row max-row]
        (ir-calc ctx-params ctx)
        data-path (pc/data-path render-path)]
    (loop [[[cur-col cur-row]
            [indices cache missing-data]]
           [[min-col min-row]
            [indices cache missing-data]]]
      (let [row-major-index (pc/twod->oned cur-col cur-row cpl-ctn)]
        (if (and (< row-major-index count-ctn)
                 (<= cur-row max-row))
          (let [wrapper-indices-path [stage-key :wrapper render-path row-major-index]
                static-indices-path [stage-key :content :static zoom render-path row-major-index]
                loadscreen-static-indices-path [stage-key :loadscreen :static zoom render-path row-major-index]
                render-funcs (gre/render-funcs this)]
            (cond (and (get old-indices wrapper-indices-path)
                       (get old-indices static-indices-path))
                  (recur  [(if (<= max-col (inc cur-col))
                             [min-col (inc cur-row)]
                             [(inc cur-col) cur-row])
                           [(assoc indices
                                   wrapper-indices-path (get old-indices wrapper-indices-path)
                                   static-indices-path (get old-indices static-indices-path))
                            cache
                            missing-data]])
                  (and (get old-indices wrapper-indices-path)
                       (not (get old-indices static-indices-path)))
                  (let [[_ children :as containers] (get old-indices wrapper-indices-path)
                        data (custom-data state render-funcs stage-key path data-path row-major-index grouped? ctx)]
                    (recur (cluster-content [(assoc indices
                                                    wrapper-indices-path
                                                    containers)
                                             cache
                                             missing-data]
                                            old-indices
                                            [cur-row cur-col min-col max-col]
                                            this stage-key children zoom render-path constraints ctx
                                            row-major-index data static-indices-path loadscreen-static-indices-path highlights grouped?)))
                  (and (get indices wrapper-indices-path)
                       (not (get old-indices static-indices-path)))
                  (let [[_ children] (get indices wrapper-indices-path)
                        data (custom-data state render-funcs stage-key path data-path row-major-index grouped? ctx)]
                    (recur (cluster-content [indices
                                             cache
                                             missing-data]
                                            old-indices
                                            [cur-row cur-col min-col max-col]
                                            this stage-key children zoom render-path constraints ctx
                                            row-major-index data static-indices-path loadscreen-static-indices-path highlights grouped?)))
                  :else
                  (let [cluster-container (js/PIXI.Container.)
                        data (custom-data state render-funcs stage-key path data-path row-major-index grouped? ctx)
                        children (mapv (fn [_] (js/PIXI.Container.)) data)]
                    (.addChild container cluster-container)
                    (doseq [child children]
                      (.addChild cluster-container child))
                    (recur
                     (cluster-content [(assoc indices
                                              wrapper-indices-path
                                              [cluster-container children])
                                       cache
                                       missing-data]
                                      old-indices
                                      [cur-row cur-col min-col max-col]
                                      this stage-key children zoom render-path constraints ctx
                                      row-major-index data static-indices-path loadscreen-static-indices-path highlights grouped?)))))
          [indices cache missing-data])))))

(defn- handle-dynamic-axes [[indices cache missing-data]
                            this stage-key ax-container _state zoom _target-zoom _render-path _all-old-indices ctx-params _grouped?]
  (let [state (gre/state this)
        ctx (get-in state [:contexts stage-key []])
        background-container (pc/axes-background-container ax-container stage-key)
        text-container (pc/axes-text-container ax-container stage-key)
        render-funcs (gre/render-funcs this)
        {render-type :render-type} ctx]
    (doseq [child (.-children background-container)]
      (remove-pixi-obj background-container child true))
    (doseq [child (.-children text-container)]
      (remove-pixi-obj text-container child true))
    (when (fn? (get-in render-funcs [render-type :axes]))
      (let [ir-calc-result (ir-calc ctx-params ctx)
            background-container-content (js/PIXI.Container.)
            background-graphics (js/PIXI.Graphics.)
            text-container-content (js/PIXI.Container.)]
        (.addChild background-container-content background-graphics)
        (.addChildAt background-container background-container-content 0)
        (.addChild text-container text-container-content)
        ((get-in render-funcs [render-type :axes])
         this
         background-container-content
         text-container-content
         ctx
         ctx-params
         zoom
         stage-key
         ir-calc-result)))
    [indices
     cache
     missing-data]))

(def ^:private inspector-button-color "#727272")
(def ^:private inspector-hover-button-color "#626262")
(def ^:private inspector-background-color "#b7b7b7")

(defn- inspector-background-box [this background-container
                                 {:keys [inspector-margin-x inspector-margin-y inspector-header-y
                                         inspector-width inspector-height color]}]
  (let [x inspector-margin-x
        y inspector-margin-y
        size-x inspector-width
        size-y (+ inspector-height inspector-header-y)
        background-color (or color inspector-background-color)]
    (gre/rect this
              background-container
              x
              y
              size-x
              size-y
              background-color
              {:a 0.8})))

(defn- close-inspector [this]
  (set! (.-cursor js/document.documentElement.style) "auto")
  (when (:inspector? (gre/state this))
    (gre/assoc-state! this [:inspector? false])
    (gre/assoc-in-state! this [:inspector-init pc/inspector-stage-index] false)
    (gre/assoc-state! this [[:pos pc/inspector-stage-index] nil])
    (gre/assoc-state! this [:indices (->> (:indices (gre/state this))
                                          (filter (fn [[[stage-key]]]
                                                    (not= pc/inspector-stage-index
                                                          stage-key)))
                                          (into {}))])
    (gre/assoc-state! this [:cache (->> (:cache (gre/state this))
                                        (filter (fn [[[stage-key]]]
                                                  (not= pc/inspector-stage-index
                                                        stage-key)))
                                        (into {}))])
    (let [main-stage (.-stage (gre/app this))
          inspector-container (pc/zoom-context-stage (gre/app this)
                                                     pc/inspector-stage-index)]
      (.removeChild main-stage inspector-container)
      (.destroy inspector-container #js {"children" true})
      (reset! grps/hover-over? false) ;not cool - but have to reset the state
      (gre/update-zoom this pc/main-stage-index))))

(defn- close-button [this items-container x size-x y size-y border-size color]
  (let [w 30]
    (gre/rect this
              items-container
              (+ x size-x 1.5 (- w))
              (+ y)
              w
              w
              color
              {:interactive? true
               :stage 1})
    (gre/interaction-primitive this
                               items-container
                               "hover"
                               (fn [_ _ _ show-or-hide]
                                 (when (< 0 (count (.-children items-container)))
                                   (case show-or-hide
                                     :show (do
                                             (aset js/document.documentElement.style "cursor" "pointer")
                                             (remove-pixi-obj items-container
                                                              (.getChildAt items-container 1)
                                                              true)
                                             (.addChildAt items-container
                                                          (js/PIXI.Graphics.)
                                                          1)
                                             (close-button this items-container x size-x y size-y border-size inspector-hover-button-color))
                                     :hide (do
                                             (aset js/document.documentElement.style "cursor" "auto")
                                             (remove-pixi-obj items-container
                                                              (.getChildAt items-container 1)
                                                              true)
                                             (.addChildAt items-container
                                                          (js/PIXI.Graphics.)
                                                          1)
                                             (close-button this items-container x size-x y size-y border-size inspector-button-color))))
                                 (let [app (gre/app this)]
                                   (.render (.-renderer app) (.-stage app))))
                               nil
                               1
                               {:delay :instant})
    (let [close-size 0.5
          close-x (+ x size-x 1.5 (* w -0.75))
          close-y (+ y (* w 0.25))]
      (gre/polygon this
                   items-container
                   [(gre/point this
                               (+ (* 0 close-size) close-x)
                               (+ (* 6 close-size) close-y))
                    (gre/point this
                               (+ (* 6 close-size) close-x)
                               (+ (* 0 close-size) close-y))
                    (gre/point this
                               (+ (* 14 close-size) close-x)
                               (+ (* 8 close-size) close-y))
                    (gre/point this
                               (+ (* 22 close-size) close-x)
                               (+ (* 0 close-size) close-y))
                    (gre/point this
                               (+ (* 28 close-size) close-x)
                               (+ (* 6 close-size) close-y))
                    (gre/point this
                               (+ (* 20 close-size) close-x)
                               (+ (* 14 close-size) close-y))
                    (gre/point this
                               (+ (* 28 close-size) close-x)
                               (+ (* 22 close-size) close-y))
                    (gre/point this
                               (+ (* 22 close-size) close-x)
                               (+ (* 28 close-size) close-y))
                    (gre/point this
                               (+ (* 14 close-size) close-x)
                               (+ (* 20 close-size) close-y))
                    (gre/point this
                               (+ (* 6 close-size) close-x)
                               (+ (* 28 close-size) close-y))
                    (gre/point this
                               (+ (* 0 close-size) close-x)
                               (+ (* 22 close-size) close-y))
                    (gre/point this
                               (+ (* 8 close-size) close-x)
                               (+ (* 14 close-size) close-y))]
                   [255 255 255]
                   {:interactive? true
                    :stage 1})))
  (gre/interaction-primitive this
                             items-container
                             "click"
                             (fn [_mods _coords]
                               (close-inspector this)
                               (tooltip/allow-tooltips (gre/frame-id this)))
                             nil
                             1))

(defn- inspector-background-box-items [this items-container
                                       {:keys [inspector-margin-x inspector-margin-y inspector-header-y
                                               inspector-width inspector-height]}]
  (let [x inspector-margin-x
        y inspector-margin-y
        size-x inspector-width
        size-y (+ inspector-header-y
                  inspector-height)
        border-size 3
        color-border inspector-background-color]

      ;;BORDER
    (gre/rect this
              items-container
              x
              y
              border-size
              (- size-y border-size)
              color-border
              {:a 1})
    (gre/rect this
              items-container
              x
              y
              (- size-x border-size)
              border-size
              color-border
              {:a 1})
    (gre/rect this
              items-container
              x
              (+ y size-y (- border-size))
              (- size-x border-size)
              border-size
              color-border
              {:a 1})
    (gre/rect this
              items-container
              (+ x size-x (- border-size))
              y
              border-size
              size-y
              color-border
              {:a 1})
    ;header - currently removed
    #_(gre/rect this
                items-container
                (+ x border-size)
                (+ y border-size)
                (- size-x (* 2 border-size))
                header-size
                [38 95 126]
                {:interactive? false
                 :stage 0})
    (close-button this items-container x size-x y size-y border-size inspector-button-color)))

(defn- mask-box [this mask-container {:keys [inspector-margin-x inspector-margin-y inspector-header-y
                                             inspector-width inspector-height]}]
  (let [x inspector-margin-x
        y inspector-margin-y
        size-x inspector-width
        size-y (+ inspector-height
                  inspector-header-y)]
    (gre/rect this
              mask-container
              x
              y
              size-x
              size-y
              [255 255 255]
              {})))

(defn- handle-inspector [[indices cache missing-data]
                         this state]
  (if (and (:inspector? (gre/state this))
           (not (get-in (gre/state this) [:inspector-init pc/inspector-stage-index])))
    (let [optional-desc
          (get-in state [:contexts pc/inspector-stage-index [] :optional-desc])
          inspector-container (pc/zoom-context-stage (gre/app this)
                                                     pc/inspector-stage-index)
          background-container (pc/background-container inspector-container pc/inspector-stage-index)
          background-graphics (js/PIXI.Graphics.)
          items-container (js/PIXI.Container.)
          items-graphic (js/PIXI.Graphics.)
          interactive-items-graphics (js/PIXI.Graphics.)
          mask-container (js/PIXI.Container.)
          mask-graphics (js/PIXI.Graphics.)]
      (.addChildAt inspector-container items-container 3)
      (.addChildAt background-container background-graphics 0)
      (.addChildAt items-container items-graphic 0)
      (.addChildAt items-container interactive-items-graphics 1)
      (.addChildAt mask-container mask-graphics 0)
      (.addChildAt inspector-container mask-container 4)
      (mask-box this mask-container optional-desc)
      (set! (.-mask inspector-container) mask-graphics)
      (inspector-background-box this background-container optional-desc)
      (inspector-background-box-items this items-container optional-desc)
      (gre/assoc-in-state! this [:inspector-init pc/inspector-stage-index] true)
      [indices cache missing-data])
    [indices cache missing-data]))

(declare ^:private handle-zoom-level)

(defn- render-ctx [[indices cache missing-data]
                   this stage-key container ax-container state zoom target-zoom render-path all-old-indices ctx-params grouped?]
  (let [group? (and (= :group (get-in state [:contexts stage-key render-path :ctx-type]))
                    (not= (get-in state [:contexts stage-key render-path :render-type]) :treemap))]
    (->
     (cond (not (get-in state [:contexts stage-key]))
           [indices cache missing-data]
           (and (= zoom 0)
                (= target-zoom 0))
           (if group?
             (-> [indices cache missing-data]
                 (handle-lower-zl this stage-key container zoom target-zoom all-old-indices render-path grouped? false)
                 (handle-zoom-level this stage-key all-old-indices zoom target-zoom render-path))
             (handle-lower-zl [indices cache missing-data]
                              this stage-key container zoom target-zoom all-old-indices render-path grouped? true))
           (and (= zoom 0)
                (> target-zoom 0))
           (if group?
             (-> [indices cache missing-data]
                 (handle-lower-zl-2 this stage-key container zoom all-old-indices render-path grouped? false)
                 (handle-zoom-level this stage-key all-old-indices zoom target-zoom render-path))
             (handle-lower-zl-2 [indices cache missing-data] this stage-key container zoom all-old-indices render-path grouped? true))
           (not= target-zoom 0)
           (if group?
             (-> [indices cache missing-data]
                 (handle-lower-zl-2 this stage-key container zoom all-old-indices render-path grouped? false)
                 (handle-zoom-level this stage-key all-old-indices zoom target-zoom render-path))
             (-> [indices cache missing-data]
                 (handle-lower-zl-2 this stage-key container zoom all-old-indices render-path grouped? false)
                 (handle-higher-zl this stage-key container zoom all-old-indices render-path ctx-params grouped?)))
           :else [indices cache missing-data])
     (handle-dynamic-axes this stage-key ax-container state zoom target-zoom render-path all-old-indices ctx-params grouped?)
     (handle-inspector this state))))

(defn- render-ui [[indices cache missing-data] this stage-key render-path _all-old-indices]
  (let [render-funcs (gre/render-funcs this)
        {:keys [contexts]} (gre/state this)
        ctx (get-in contexts [stage-key render-path])
        ui-fns (get-in render-funcs [(render-type ctx) :ui])]
    (if (seq ui-fns)
      (reduce (fn [indices {:keys [idx]}]
                (let [{:keys [path]} (gre/state this)
                      app (gre/app this)
                      current-ui-container (js/PIXI.Container.)
                      _ (.addChild current-ui-container (js/PIXI.Graphics.))
                      stage (pc/main-container (pc/zoom-context-stage app stage-key) stage-key)
                      _ (-> stage pc/ui-container (.addChildAt current-ui-container idx))]
                  #_(fun 0 this current-ui-container args constraints ctx render-path)
                  [(assoc indices
                          path
                          current-ui-container)
                   cache
                   missing-data]))
              indices
              ui-fns)
      [indices
       cache
       missing-data])))

(defn- handle-zoom-level [[indices cache missing-data :as update-state]
                          this stage-key all-old-indices zoom target-zoom render-path]
  (assert (= 3 (count update-state)) "handle-zoom-level - update-state does not contain the right amout of parameters")
  (let [state (gre/state this)
        args (gre/args this)
        ctx (get-in state [:contexts stage-key render-path])
        {{:keys [cpl-ctn]} :params ctx-type :ctx-type ctx-render-type :render-type} ctx
        grouped? (= :group ctx-type)
        app (gre/app this)
        stage (pc/zoom-context-stage app stage-key)
        main-container (pc/main-container stage stage-key)
        container (.getChildAt main-container (index-access zoom))
        ax-container (pc/axes-container stage stage-key)
        [indices cache missing-data]
        (if (= ctx-render-type :treemap)
          (let [ctx-params (ctx-cal-tree args (get state [:pos stage-key]) (get-in state [:contexts stage-key]) (inc (count render-path)))]
            (if (=  zoom target-zoom)
              (render-ctx [indices cache missing-data]
                          this stage-key container ax-container state zoom target-zoom render-path
                          all-old-indices ctx-params false)
              (reduce (fn [[indices cache missing-data] child-path]
                        (render-ctx [indices cache missing-data]
                                    this stage-key container ax-container state zoom target-zoom child-path
                                    all-old-indices ctx-params false))
                      [indices cache missing-data]
                      (:visible-context-paths ctx-params))))
          (let [{:keys [min-idx-x min-idx-y max-idx-x max-idx-y] :as ctx-params}
                (ctx-cal args (get state [:pos stage-key]) ctx)]
            (if-not grouped?
              (render-ctx [indices cache missing-data]
                          this stage-key container ax-container state zoom target-zoom render-path all-old-indices ctx-params false)
              (reduce (fn [[indices cache missing-data] idx-x]
                        (reduce (fn [[indices cache missing-data] idx-y]
                                  (let [idx (+ (* idx-y cpl-ctn)
                                               idx-x)
                                        child-path (conj render-path idx)]
                                    (render-ctx [indices cache missing-data]
                                                this stage-key container ax-container state zoom target-zoom child-path
                                                all-old-indices ctx-params grouped?)))
                                [indices cache missing-data]
                                (range min-idx-y max-idx-y)))
                      [indices cache missing-data]
                      (range min-idx-x max-idx-x)))))]
    [indices cache missing-data]))

(defn- update-stages [this stage-key]
  (let [{:keys [cache] all-old-indices :indices :as state} (gre/state this)
        {target-zoom :zoom} (get state [:pos stage-key])
        render-path []
        [indices cache missing-data]
        (reduce (fn [[indices cache missing-data] zoom]
                  (if (get-in state [[:pos stage-key] :init zoom])
                    (handle-zoom-level [indices cache missing-data]
                                       this stage-key all-old-indices zoom target-zoom render-path)
                    [indices cache missing-data]))
                [{}
                 cache
                 {}]
                (range 0 (inc target-zoom)))
        [indices cache missing-data]
        (render-ui [indices cache missing-data] this stage-key render-path all-old-indices)]
    (gre/assoc-state! this [:cache cache
                            :indices indices])
    (update-indices! this
                     stage-key
                     indices
                     all-old-indices)
    (when (seq missing-data)
      (tubes/dispatch-to-server [ws-api/get-events-route
                                 {:client-callback [ws-api/get-events-result
                                                    (:app-state-path (gre/args this))
                                                    stage-key]}
                                 (keys missing-data)]))))

(defn render [this stage-key zoom]
  (let [app (gre/app this)
        container (js/PIXI.Container.)
        stage (pc/main-container (pc/zoom-context-stage app stage-key) stage-key)]
    (aset container "name" (str "zoom-" zoom))
    (.addChildAt stage
                 container
                 (index-access zoom))
    (.addChildAt container (js/PIXI.Graphics.) 0)))

(defn update-zoom [this stage-key]
  (let [state (gre/state this)
        {:keys [zoom next-zoom]} (get state [:pos stage-key])
        new-zoom-level (or next-zoom 0)]
    (cond (< new-zoom-level zoom)
          (doseq [zoom-level (reverse (range (inc new-zoom-level)
                                             (inc zoom)))]
            (when (get-in state [[:pos stage-key] :init zoom-level])
              (when (< zoom-level 2)
                (discard this stage-key zoom-level))
              (gre/assoc-in-state! this [[:pos stage-key] :init zoom-level] false)))
          :else ;(> new-zoom-level zoom)
          (doseq [zoom-level (range 0 (inc new-zoom-level))]
            (when-not (get-in state [[:pos stage-key] :init zoom-level])
              (when (< zoom-level 2)
                (render this stage-key zoom-level))
              (gre/assoc-in-state! this [[:pos stage-key] :init zoom-level] true))))
    (gre/assoc-in-state! this [[:pos stage-key] :zoom] new-zoom-level)
    (update-stages this stage-key)
    (gre/render this)))

(defn reset [this stage-key]
  (when-not (:headless (gre/state this))

    (close-inspector this)


    (doseq [zoom-level (reverse (range 0 2))]
      (when (get-in (gre/state this) [[:pos stage-key] :init zoom-level])
        (discard this stage-key zoom-level)))
    (let [app (gre/app this)
          stage (pc/main-container (pc/zoom-context-stage app stage-key) stage-key)
          children (.-children stage)]
      (doseq [container children]
        (.removeChild stage container)
        (.destroy container (clj->js {:children true})))))
  (gre/assoc-state! this [:indices nil
                          :cache nil])
  (gre/merge-in-state! this
                       [:pos stage-key]
                       {:x 0
                        :y 0
                        :z 0
                        :zoom 0
                        :init nil
                        :next-zoom 0}))

(defn rerender [this stage-key]
  (when-not (:headless (gre/state this))
    (doseq [zoom-level (reverse (range 0 2))]
      (when (get-in (gre/state this) [[:pos stage-key] :init zoom-level])
        (discard this stage-key zoom-level)))
    (let [app (gre/app this)
          stage (pc/main-container (pc/zoom-context-stage app stage-key) stage-key)
          children (.-children stage)]
      (doseq [container children]
        (.removeChild stage container)
        (.destroy container (clj->js {:children true})))))
  (gre/assoc-state! this [:indices nil
                          :cache nil])
  (gre/merge-in-state! this
                       [:pos stage-key]
                       {:init nil}))
