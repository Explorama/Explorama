(ns de.explorama.frontend.aggregation-vpl.render.workspaces.core
  (:require [de.explorama.frontend.aggregation-vpl.render.instance-interface :as ri]
            [de.explorama.frontend.aggregation-vpl.render.common :as rc]
            [re-frame.core :as re-frame]))

(declare draw-workspace-mutable draw-workspace-static reset-workspace-mutable)

;;;;; STATE FUNCTIONS START

(defn- add-connection [instance x direction]
  (let [{:keys [matrix]}
        @(ri/state instance)
        col (get matrix x)
        last-element (peek col)
        connection (:connection last-element)
        start (aget connection "startX")
        end (aget connection "endX")
        new-x (if (= :left direction)
                (max 0 (dec start))
                (min (count matrix)
                     (inc end)))]
    (when (= :left direction)
      (aset connection "startX" new-x))
    (when (= :right direction)
      (aset connection "endX" new-x))
    (swap! (ri/state instance)
           update-in
           [:matrix new-x]
           conj
           (rc/create-op last-element
                         connection))))

(defn- remove-connection [instance x direction]
  (let [{:keys [matrix]}
        @(ri/state instance)
        col (get matrix x)
        last-element (peek col)
        connection (:connection last-element)
        start (aget connection "startX")
        end (aget connection "endX")
        new-x (if (= :left direction)
                (min end (inc start))
                (max start (dec end)))]
    (when (= :left direction)
      (aset connection "startX" new-x))
    (when (= :right direction)
      (aset connection "endX" new-x))
    (swap! (ri/state instance)
           update-in
           [:matrix (if (= :left direction)
                      start
                      end)]
           pop)))

;;;;; STATE FUNCTIONS END

(defn draw-workspace-static [instance]
  (let [state (ri/state instance)
        {{:keys [font-size font font-color pixi-align]}
         :consts}
        @state
        stage (.-stage (ri/app instance))
        workspace-fixed-container (.getChildAt stage rc/workspace-fixed-idx)]
    (.addChild workspace-fixed-container
               (rc/text "Workspace"
                        40
                        0
                        font-size font font-color pixi-align))))

(defn reset-workspace-mutable [instance]
  (let [container (-> (ri/app instance)
                      .-stage
                      (.getChildAt rc/workspace-scroll-idx))]
    (rc/clear-children container)))

(defn- col-handle [instance xc origin-x {:keys [height
                                                op-width
                                                workspace-x-offset
                                                workspace-space-hor
                                                header lines]}]
  (let [app (ri/app instance)
        state (ri/state instance)
        drag-container (js/PIXI.Graphics.)
        del-container (js/PIXI.Graphics.)
        del-hover (js/PIXI.Graphics.)
        add-container (js/PIXI.Graphics.)
        add-hover (js/PIXI.Graphics.)
        drag-handle (js/PIXI.Graphics.)
        copy-right (js/PIXI.Graphics.)
        delete (js/PIXI.Graphics.)
        hover (js/PIXI.Graphics.)
        x (+ workspace-x-offset
             (* origin-x (+ op-width workspace-space-hor)))]
    (rc/polygon-x delete
                  (+ x (* 0.15 op-width))
                  (- header workspace-x-offset workspace-x-offset workspace-x-offset workspace-x-offset)
                  0.7
                  0x000000
                  0.4)
    (rc/rect del-hover
             (- (+ x (* 0.15 op-width))
                2)
             (- header workspace-x-offset workspace-x-offset workspace-x-offset workspace-x-offset 2)
             23
             23
             0x000000
             0.3)
    (rc/polygon-+ copy-right
                  (+ x (* 0.60 op-width))
                  (- header workspace-x-offset workspace-x-offset workspace-x-offset workspace-x-offset)
                  1.05
                  0x000000
                  0.4)
    (rc/rect add-hover
             (- (+ x (* 0.60 op-width))
                2)
             (- header workspace-x-offset workspace-x-offset workspace-x-offset workspace-x-offset 2)
             23
             23
             0x000000
             0.3)
    (rc/rect drag-handle
             (+ x (* 0.05 op-width))
             (- header workspace-x-offset workspace-x-offset workspace-x-offset workspace-x-offset workspace-x-offset)
             (* 0.9 op-width)
             (* 6.5 (+ workspace-x-offset lines))
             0x000000
             0.05)
    (rc/rect drag-handle
             (+ x (* 0.25 op-width))
             (+ header workspace-x-offset)
             (* 0.5 op-width)
             lines
             0x000000
             0.3)
    (rc/rect drag-handle
             (+ x (* 0.1 op-width))
             (+ header workspace-x-offset
                workspace-x-offset)
             (* 0.8 op-width)
             lines
             0x000000
             0.3)
    (rc/rect drag-handle
             (+ x (* 0.1 op-width))
             (+ header workspace-x-offset
                workspace-x-offset
                workspace-x-offset)
             (* 0.8 op-width)
             lines
             0x000000
             0.3)
    (rc/interactable drag-handle
                     (fn [e]
                       (when (and @rc/workspace-drag
                                  (not= origin-x @rc/workspace-drag))
                         (rc/rect hover
                                  (+ x (* 0.05 op-width))
                                  (- (+ header workspace-x-offset)
                                     (* 3.5 (+ workspace-x-offset lines)))
                                  (* 0.9 op-width)
                                  height
                                  0x333399
                                  0.05)
                         (reset! rc/workspace-drop-target origin-x)))
                     (fn [e]
                       (when (= origin-x @rc/workspace-drop-target)
                         (.clear hover)
                         (reset! rc/workspace-drop-target nil)))
                     (fn [e]
                       (reset! rc/workspace-drag origin-x)
                       (swap! state assoc :drag-obj xc))
                     nil
                     true)

    (rc/interactable app add-container add-hover (fn [e]
                                                   (swap! state
                                                          update
                                                          :matrix
                                                          (fn [matrix]
                                                            (reduce (fn [new-matrix [x col]]
                                                                      (if (= origin-x x)
                                                                        (-> (conj new-matrix col)
                                                                            (conj (mapv (fn [{connection :connection :as field}]
                                                                                          (if connection
                                                                                            #_; Not sure about this behaviour
                                                                                              (let [start (aget connection "startX")
                                                                                                    end (aget connection "endX")]
                                                                                                (if (not= start end)
                                                                                                  (do
                                                                                                    (aset connection "endX" (inc end))
                                                                                                    (assoc field :id (str (random-uuid))))
                                                                                                  (assoc field
                                                                                                         :id (str (random-uuid))
                                                                                                         :connection (rc/create-con (inc x)
                                                                                                                                    (inc x)))))
                                                                                            (assoc field
                                                                                                   :id (str (random-uuid))
                                                                                                   :connection (rc/create-con (inc x)
                                                                                                                              (inc x)))
                                                                                            field))
                                                                                        col)))
                                                                        (conj new-matrix col)))
                                                                    []
                                                                    (map-indexed vector matrix))))
                                                   (reset-workspace-mutable instance)
                                                   (draw-workspace-mutable instance)
                                                   (rc/render app)))
    (rc/interactable app del-container del-hover (fn [e]
                                                   (swap! state
                                                          update
                                                          :workspace-elements
                                                          dissoc origin-x)
                                                   (swap! state
                                                          update
                                                          :workspace-elements
                                                          (fn [val]
                                                            (reduce (fn [acc [k v]]
                                                                      (cond (= k origin-x)
                                                                            acc
                                                                            (< k origin-x)
                                                                            (assoc acc k v)
                                                                            :else
                                                                            (assoc acc (dec k) v)))
                                                                    {}
                                                                    val)))
                                                   (reset-workspace-mutable instance)
                                                   (draw-workspace-mutable instance)
                                                   (rc/render app)))

    (.addChild drag-container drag-handle)
    (.addChild drag-container hover)
    (.addChild add-container copy-right)
    (.addChild del-container delete)
    (.addChild xc drag-container)
    (.addChild xc add-container)
    (.addChild xc del-container)))

(defn expand-right- [instance expand-right-handle expand-right-handle-hover x y id expand-right {:keys [height
                                                                                                        op-width
                                                                                                        workspace-x-offset
                                                                                                        workspace-space-hor
                                                                                                        workspace-drop-zone
                                                                                                        workspace-expand-handle-width
                                                                                                        workspace-op-height
                                                                                                        workspace-space-vert]}]
  (rc/rect expand-right-handle
           (+ workspace-x-offset
              (* (+ x expand-right)
                 (+ op-width workspace-space-hor))
              op-width)
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x333366"
           1)
  (rc/rect expand-right-handle-hover
           (+ workspace-x-offset
              (* (+ x expand-right)
                 (+ op-width workspace-space-hor))
              op-width)
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x336633"
           0.3)
  (rc/interactable (ri/app instance)
                   expand-right-handle
                   expand-right-handle-hover
                   (fn [e]
                     (add-connection instance x :right)
                     (reset-workspace-mutable instance)
                     (draw-workspace-mutable instance)
                     (rc/render (ri/app instance)))))

(defn- expand-left- [instance expand-left-handle expand-left-handle-hover x y id expand-right {:keys [height
                                                                                                      op-width
                                                                                                      workspace-x-offset
                                                                                                      workspace-space-hor
                                                                                                      workspace-drop-zone
                                                                                                      workspace-expand-handle-width
                                                                                                      workspace-op-height
                                                                                                      workspace-space-vert]}]
  (rc/rect expand-left-handle
           (- (+ workspace-x-offset
                 (* x (+ op-width workspace-space-hor)))
              workspace-expand-handle-width)
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x333366"
           1)
  (rc/rect expand-left-handle-hover
           (- (+ workspace-x-offset
                 (* x (+ op-width workspace-space-hor)))
              workspace-expand-handle-width)
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x336633"
           1)
  (rc/interactable (ri/app instance)
                   expand-left-handle
                   expand-left-handle-hover
                   (fn [e]
                     (add-connection instance x :left)
                     (reset-workspace-mutable instance)
                     (draw-workspace-mutable instance)
                     (rc/render (ri/app instance)))))

(defn- reduce-left- [instance reduce-left-handle reduce-left-handle-hover x y id expand-right {:keys [height
                                                                                                      op-width
                                                                                                      workspace-x-offset
                                                                                                      workspace-space-hor
                                                                                                      workspace-drop-zone
                                                                                                      workspace-expand-handle-width
                                                                                                      workspace-op-height
                                                                                                      workspace-space-vert]}]
  (rc/rect reduce-left-handle
           (+ workspace-x-offset
              (* x (+ op-width workspace-space-hor)))
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x991111"
           1)
  (rc/rect reduce-left-handle-hover
           (+ workspace-x-offset
              (* x (+ op-width workspace-space-hor)))
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x119911"
           0.3)
  (rc/interactable (ri/app instance)
                   reduce-left-handle
                   reduce-left-handle-hover
                   (fn [e]
                     (remove-connection instance x :left)
                     (reset-workspace-mutable instance)
                     (draw-workspace-mutable instance)
                     (rc/render (ri/app instance)))))

(defn- reduce-right- [instance reduce-right-handle reduce-right-handle-hover x y id expand-right {:keys [height
                                                                                                         op-width
                                                                                                         workspace-x-offset
                                                                                                         workspace-space-hor
                                                                                                         workspace-drop-zone
                                                                                                         workspace-expand-handle-width
                                                                                                         workspace-op-height
                                                                                                         workspace-space-vert]}]
  (rc/rect reduce-right-handle
           (- (+ workspace-x-offset
                 (* (+ x expand-right)
                    (+ op-width workspace-space-hor))
                 op-width)
              workspace-expand-handle-width)
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x991111"
           1)
  (rc/rect reduce-right-handle-hover
           (- (+ workspace-x-offset
                 (* (+ x expand-right)
                    (+ op-width workspace-space-hor))
                 op-width)
              workspace-expand-handle-width)
           (- height
              workspace-drop-zone
              (* (+ workspace-space-vert workspace-op-height)
                 (inc y)))
           workspace-expand-handle-width
           workspace-op-height
           "0x119911"
           0.3)
  (rc/interactable (ri/app instance)
                   reduce-right-handle
                   reduce-right-handle-hover
                   (fn [e]
                     (remove-connection instance x :right)
                     (reset-workspace-mutable instance)
                     (draw-workspace-mutable instance)
                     (rc/render (ri/app instance)))))

(defn- draw-workspace-mutable-reduce [instance {:keys [height
                                                       font pixi-align
                                                       op-width
                                                       op-height
                                                       workspace-x-offset
                                                       workspace-space-hor
                                                       workspace-drop-zone
                                                       workspace-op-height
                                                       workspace-space-vert
                                                       workspace-drop-zone-padding]
                                                :as consts}]
  (let [app (ri/app instance)
        state (ri/state instance)
        matrix (:matrix @state)]
    (js/console.log "matrix" matrix)
    (reduce (fn [acc [x column]]
              (let [xc (js/PIXI.Container.)]
                (col-handle instance xc x consts)
                (-> (reduce (fn [acc [y {:keys [op-name op-type id connection]
                                         {{:keys [arguments]} :steering :as meta-data} :meta-data
                                         :as element}]]
                              (when element
                                (let [y (- (dec (count column)) y)
                                      [start-x end-x]
                                      (if connection
                                        [(aget connection "startX")
                                         (aget connection "endX")]
                                        [0 0])
                                      expand-right (- end-x start-x)]
                                  (when (or (= start-x x)
                                            (nil? connection))

                                    (let [c (js/PIXI.Container.)
                                          g (js/PIXI.Graphics.)
                                          text-obj
                                          (rc/text op-name
                                                   (+ workspace-x-offset
                                                      5
                                                      (* x (+ op-width workspace-space-hor))
                                                      (/ (* expand-right op-width) 2))
                                                   (+ 2 (- height
                                                           workspace-drop-zone
                                                           (* (+ workspace-space-vert workspace-op-height)
                                                              (inc y))))
                                                   14 font 0xffffff pixi-align)]
                                      (aset g "interactive" true)
                                      (aset g "buttonMode" true)
                                      (.on g "pointerover" (fn [e]
                                                             (reset! rc/over-options true)))
                                      (.on g "pointerleave" (fn [e]
                                                              (reset! rc/over-options false)))
                                      (rc/double-click g (fn [e]
                                                           (re-frame/dispatch [:de.explorama.frontend.aggregation-vpl.core/details
                                                                               (:frame-id @state)
                                                                               (assoc element
                                                                                      :additional-info
                                                                                      {:x x
                                                                                       :y y
                                                                                       :matrix matrix})])))
                                      (if (= :dataset op-type)
                                        (rc/rect-rounded g
                                                         (+ workspace-x-offset
                                                            (* x (+ op-width workspace-space-hor)))
                                                         (- height
                                                            workspace-drop-zone
                                                            (* (+ workspace-space-vert workspace-op-height)
                                                               (inc y)))
                                                         op-width
                                                         workspace-op-height
                                                         "0x333366"
                                                         1
                                                         20)
                                        (rc/rect g
                                                 (+ workspace-x-offset
                                                    (* x
                                                       (+ op-width workspace-space-hor)))
                                                 (- height
                                                    workspace-drop-zone
                                                    (* (+ workspace-space-vert workspace-op-height)
                                                       (inc y)))
                                                 (+ op-width
                                                    (* (+ op-width workspace-space-hor)
                                                       expand-right))
                                                 workspace-op-height
                                                 "0x666666"
                                                 1))
                                      (.addChild c g)
                                      (.addChild c text-obj)

                                      (when (and (= y 0)
                                                 (not= op-type :dataset))
                                        (let [del-op-con (js/PIXI.Container.)
                                              del-op (js/PIXI.Graphics.)
                                              del-op-hover (js/PIXI.Graphics.)]

                                          (rc/polygon-x del-op
                                                        (+ workspace-x-offset
                                                           5
                                                           (* x (+ op-width workspace-space-hor))
                                                           (* (* expand-right op-width) 1))
                                                        (+ 2 (- height
                                                                workspace-drop-zone
                                                                (* (+ workspace-space-vert (* workspace-op-height 0.4))
                                                                   (inc y))))
                                                        0.5
                                                        0xffffff
                                                        1)
                                          (rc/rect del-op-hover
                                                   (+ workspace-x-offset
                                                      3
                                                      (* x (+ op-width workspace-space-hor))
                                                      (* (* expand-right op-width) 1))
                                                   (+ 0 (- height
                                                           workspace-drop-zone
                                                           (* (+ workspace-space-vert (* workspace-op-height 0.4))
                                                              (inc y))))
                                                   18
                                                   18
                                                   0xffffff
                                                   0.3)
                                          (rc/interactable app del-op-con del-op-hover (fn [e]
                                                                                         (let [{:keys [matrix]} @(ri/state instance)
                                                                                               {:keys [connection]} (peek (get matrix x))
                                                                                               start (aget connection "startX")
                                                                                               end (aget connection "endX")]
                                                                                           (swap! (ri/state instance)
                                                                                                  assoc
                                                                                                  :matrix
                                                                                                  (reduce (fn [matrix x]
                                                                                                            (update matrix x pop))
                                                                                                          matrix
                                                                                                          (range start (inc end)))))
                                                                                         (reset-workspace-mutable instance)
                                                                                         (draw-workspace-mutable instance)
                                                                                         (rc/render (ri/app instance))))
                                          (.addChild del-op-con del-op)
                                          (.addChild c del-op-con)))
                                      (when (= y 0)
                                        (let [add-button-con (js/PIXI.Container.)
                                              add-button (js/PIXI.Graphics.)
                                              add-button-hover (js/PIXI.Graphics.)]
                                          (rc/rect add-button
                                                   (+ workspace-x-offset
                                                      (* x (+ op-width workspace-space-hor)))
                                                   (- height workspace-drop-zone workspace-drop-zone-padding)
                                                   (+ op-width
                                                      (* (+ op-width workspace-space-hor) expand-right))
                                                   op-height
                                                   "0x333366"
                                                   0.35)
                                          (rc/polygon-+ add-button
                                                        (+ workspace-x-offset
                                                           (* x (+ op-width workspace-space-hor))
                                                           (* (+ op-width
                                                                 (* (+ op-width workspace-space-hor) expand-right))
                                                              0.4))
                                                        (- height workspace-drop-zone workspace-drop-zone-padding)
                                                        1.05
                                                        0x000000
                                                        0.4)
                                          (rc/rect add-button-hover
                                                   (+ workspace-x-offset
                                                      (* x (+ op-width workspace-space-hor)))
                                                   (- height workspace-drop-zone workspace-drop-zone-padding)
                                                   (+ op-width
                                                      (* (+ op-width workspace-space-hor) expand-right))
                                                   op-height
                                                   "0x333366"
                                                   0.3)
                                          (rc/interactable app
                                                           add-button
                                                           add-button-hover
                                                           (fn [e]
                                                             (re-frame/dispatch [:de.explorama.frontend.aggregation-vpl.core/options
                                                                                 (:frame-id @state)
                                                                                 (assoc element
                                                                                        :additional-info
                                                                                        {:x x
                                                                                         :y y
                                                                                         :matrix matrix})])))
                                          (.addChild add-button-con add-button)
                                          (.addChild xc add-button)))
                                      (when (and (= arguments 0)
                                                 (= y 0))
                                        (let [expand-left-handle-con (js/PIXI.Container.)
                                              expand-left-handle (js/PIXI.Graphics.)
                                              expand-left-handle-hover (js/PIXI.Graphics.)
                                              expand-right-handle-con (js/PIXI.Container.)
                                              expand-right-handle (js/PIXI.Graphics.)
                                              expand-right-handle-hover (js/PIXI.Graphics.)

                                              reduce-left-handle-con (js/PIXI.Container.)
                                              reduce-left-handle (js/PIXI.Graphics.)
                                              reduce-left-handle-hover (js/PIXI.Graphics.)
                                              reduce-right-handle-con (js/PIXI.Container.)
                                              reduce-right-handle (js/PIXI.Graphics.)
                                              reduce-right-handle-hover (js/PIXI.Graphics.)]
                                          (when (seq (get (:matrix @(ri/state instance))
                                                          (inc (+ x expand-right))))
                                            (expand-right- instance expand-right-handle expand-right-handle-hover x y id expand-right consts))

                                          (when (not= x 0)
                                            (expand-left- instance expand-left-handle expand-left-handle-hover x y id expand-right consts))

                                          (when (< 0 expand-right)
                                            (reduce-left- instance reduce-left-handle reduce-left-handle-hover x y id expand-right consts))

                                          (when (< 0 expand-right)
                                            (reduce-right- instance reduce-right-handle reduce-right-handle-hover x y id expand-right consts))

                                          (.addChild expand-left-handle-con expand-left-handle)
                                          (.addChild expand-right-handle-con expand-right-handle)
                                          (.addChild reduce-left-handle-con reduce-left-handle)
                                          (.addChild reduce-right-handle-con reduce-right-handle)
                                          (.addChild c expand-left-handle-con)
                                          (.addChild c expand-right-handle-con)
                                          (.addChild c reduce-left-handle-con)
                                          (.addChild c reduce-right-handle-con)))

                                      (.addChild xc c)))))
                              acc)
                            acc
                            (map-indexed vector column))
                    (update :container conj xc))))
            {:max-elements 0
             :container []}
            (map-indexed vector matrix))))

(defn draw-workspace-mutable [instance]
  (let [state (ri/state instance)
        {consts :consts}
        @state
        app (ri/app instance)
        stage (.-stage app)
        workspace-scroll-container (.getChildAt stage rc/workspace-scroll-idx)
        {:keys [container]}
        (draw-workspace-mutable-reduce instance consts)]
    (doseq [container container]
      (.addChild workspace-scroll-container container))))