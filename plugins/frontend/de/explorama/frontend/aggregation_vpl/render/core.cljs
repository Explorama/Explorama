(ns de.explorama.frontend.aggregation-vpl.render.core
  (:require [clojure.string :as str]
            [data-format-lib.operations :refer [functions]]
            [de.explorama.frontend.aggregation-vpl.render.common :as rc]
            [de.explorama.frontend.aggregation-vpl.render.instance-interface :as ri]
            [de.explorama.frontend.aggregation-vpl.render.workspaces.core :as rw]))

(defn- wheel [instance]
  (fn [e]
    (when @rc/over-options
      (let [app (ri/app instance)
            state @(ri/state instance)
            delta (max (min rc/max-wheel-delta (.-wheelDelta e)) rc/min-wheel-delta)
            translate (* (/ delta 120)
                         120)]
        (when (.-shiftKey e)
          (let [pos (.-x (.-position (.getChildAt (.-stage app)
                                                  rc/options-scroll-hor-vert-idx)))
                new-pos (min (max (+ pos translate)
                                  0)
                             (:min-options-width state))]
            (set! (.-x (.-position (.getChildAt (.-stage app)
                                                rc/options-scroll-hor-vert-idx)))
                  new-pos)
            (set! (.-x (.-position (.getChildAt (.-stage app)
                                                rc/options-scroll-bar-hor-idx)))
                  (+ (:min-options-width state)
                     (* (:options-scroll-hor-bar-length state)
                        (- 1
                           (/ new-pos
                              (:min-options-width state))))))
            (swap! (ri/state instance) assoc :options-pos new-pos)))
        (.render (.-renderer app)
                 (.-stage app))))
    (.stopPropagation e)
    (.preventDefault e)))

(defn- mousedown [instance]
  (fn [e]
    (when (and (= (.-which e) 1)
               (not @rc/options-drag)
               (not @rc/workspace-drag))
      (swap! (ri/state instance) assoc :pmx nil :pmy nil)
      (reset! rc/panning? true))))

(defn- mousemove [instance]
  (fn [e]
    (when @rc/options-drag
      (let [app (ri/app instance)
            {:keys [pmx pmy drag-obj]} @(ri/state instance)
            x (.-x (.-position drag-obj))
            y (.-y (.-position drag-obj))
            mx (- (.-offsetX e))
            my (- (.-offsetY e))
            pmx (if pmx pmx mx)
            pmy (if pmy pmy my)
            new-x (+ x (- pmx mx))
            new-y (+ y (- pmy my))]
        (set! (.-x (.-position drag-obj))
              new-x)
        (set! (.-y (.-position drag-obj))
              new-y)
        (swap! (ri/state instance)
               assoc
               :pmx mx :pmy my)
        (rc/render app)))
    (when @rc/workspace-drag
      (let [app (ri/app instance)
            {:keys [pmx drag-obj]} @(ri/state instance)
            x (.-x (.-position drag-obj))
            mx (- (.-offsetX e))
            pmx (if pmx pmx mx)
            new-x (+ x (- pmx mx))]
        (set! (.-x (.-position drag-obj))
              new-x)
        (swap! (ri/state instance)
               assoc
               :pmx mx)
        (rc/render app)))))

(defn- mouseup [instance]
  (fn [e]
    (when (= (.-which e) 1)
      (swap! (ri/state instance) assoc :pmx nil :pmy nil)
      (reset! rc/panning? false))
    (when @rc/workspace-drag
      (let [workspace-elements (:workspace-elements @(ri/state instance))
            drag-x @rc/workspace-drag
            drop-x @rc/workspace-drop-target
            app (ri/app instance)]
        (when (and drag-x drop-x)
          (swap! (ri/state instance) assoc-in [:workspace-elements drag-x] (get workspace-elements drop-x))
          (swap! (ri/state instance) assoc-in [:workspace-elements drop-x] (get workspace-elements drag-x)))
        (reset! rc/workspace-drag nil)
        (reset! rc/workspace-drop-target nil)
        (swap! (ri/state instance) dissoc :drag-obj)
        (rw/reset-workspace-mutable instance)
        (rw/draw-workspace-mutable instance)
        (rc/render app)))))

(defn- mouseleave [instance]
  (fn [e]
    (when (= (.-which e) 1)
      (swap! (ri/state instance) assoc :pmx nil :pmy nil)
      (reset! rc/panning? false))))

(deftype Renderer [state- app-]
  ri/AggregationRenderer
  (state [instance]
    state-)
  (app [instance]
    app-))

(defn init [frame-id host width height {:keys [data-acs dim-infos dis] :as current-dis}]
  (let [pixi-canvas (.getElementById js/document host)
        app (js/PIXI.Application. (clj->js {:autoStart false
                                            :width width
                                            :height height
                                            :backgroundColor 0xFFFFFF
                                            :antialias true
                                            :forceCanvas true
                                            :view pixi-canvas}))
        align :left
        op-height 30
        dis (loop [data-acs data-acs
                   dim-infos dim-infos
                   dis dis
                   i 0
                   result []]
              (if (empty? data-acs)
                result
                (let [data-ac (first data-acs)
                      dim-info (first dim-infos)
                      di (first dis)]
                  (recur
                   (rest data-acs)
                   (rest dim-infos)
                   (rest dis)
                   (inc i)
                   (conj result
                         {:op-name (str/join ", " (:datasources dim-info))
                          :op-type :dataset
                          :id (str (random-uuid))
                          #_#_:data-ac data-ac
                          :dim-info dim-info
                          #_#_:di di
                          :di-idx i})))))
        state (atom {:frame-id frame-id
                     :host host
                     :width width
                     :height height
                     :current-dis current-dis
                     :dis dis
                     :matrix (rc/initialize-matrix dis)
                     :consts {:width width
                              :height height
                              :border 1
                              :lines 3
                              :options-width 250
                              :scrollbar 10
                              :op-width 80
                              :op-height 30
                              :header 65
                              :font "Arial"
                              :font-size 20
                              :font-color 0x000000
                              :align align
                              :workspace-drop-zone 45
                              :workspace-drop-zone-padding 6
                              :workspace-x-offset 6
                              :workspace-space-hor 25
                              :workspace-expand-handle-width 11
                              :workspace-space-vert 10
                              :workspace-op-height (* 2 op-height)
                              :pixi-align (cond (= align :center)
                                                :left
                                                :else
                                                align)}})
        instance (Renderer. state
                            app)
        stage (.-stage app)
        listener [["wheel" (wheel instance) {:passive false}]
                  ["mousedown" (mousedown instance) {:passive true}]
                  ["mousemove" (mousemove instance) {:passive true}]
                  ["mouseup" (mouseup instance) {:passive true}]
                  ["mouseleave" (mouseleave instance) {:passive true}]]
        options-fixed-container (js/PIXI.Container.)
        options-scroll-hor-vert-container (js/PIXI.Container.)
        options-scroll-vert-container (js/PIXI.Container.)
        options-scroll-bar-vert-container (js/PIXI.Container.)
        options-scroll-bar-hor-container (js/PIXI.Container.)
        workspace-fixed-container (js/PIXI.Container.)
        drag-container (js/PIXI.Container.)
        workspace-scroll-container (js/PIXI.Container.)
        workspace-scroll-bar-vert-container (js/PIXI.Container.)
        workspace-scroll-bar-hor-container (js/PIXI.Container.)
        workspace-drop-container (js/PIXI.Container.)]
    (js/console.log "functions" functions)
    (aset options-fixed-container "name" "options-fixed")
    (aset options-scroll-hor-vert-container "name" "options-scroll-hor-vert")
    (aset options-scroll-vert-container "name" "options-scroll-vert")
    (aset options-scroll-bar-vert-container "name" "options-scroll-bar-vert")
    (aset options-scroll-bar-hor-container "name" "options-scroll-bar-hor")
    (aset drag-container "name" "drag")
    (aset workspace-fixed-container "name" "workspace-fixed")
    (aset workspace-scroll-container  "name" "workspace-scroll")
    (aset workspace-scroll-bar-vert-container "name" "workspace-scroll-vert-bar")
    (aset workspace-scroll-bar-hor-container "name" "workspace-scroll-hor-bar")
    (aset workspace-drop-container "name" "workspace-drop")
    (doseq [[on func opts] listener]
      (.addEventListener pixi-canvas on func (clj->js opts)))
    (swap! state assoc :listener listener)

    (.addChildAt stage
                 options-fixed-container
                 rc/options-fixed-idx)
    (.addChildAt stage
                 options-scroll-hor-vert-container
                 rc/options-scroll-hor-vert-idx)
    (.addChildAt stage
                 options-scroll-vert-container
                 rc/options-scroll-vert-idx)
    (.addChildAt stage
                 options-scroll-bar-hor-container
                 rc/options-scroll-bar-hor-idx)
    (.addChildAt stage
                 options-scroll-bar-vert-container
                 rc/options-scroll-bar-vert-idx)
    (.addChildAt stage
                 workspace-fixed-container
                 rc/workspace-fixed-idx)
    (.addChildAt stage
                 workspace-drop-container
                 rc/workspace-drop-idx)
    (.addChildAt stage
                 workspace-scroll-container
                 rc/workspace-scroll-idx)
    (.addChildAt stage
                 workspace-scroll-bar-hor-container
                 rc/workspace-scroll-bar-hor-idx)
    (.addChildAt stage
                 workspace-scroll-bar-vert-container
                 rc/workspace-scroll-bar-vert-idx)
    (.addChildAt stage
                 drag-container
                 rc/drag-idx)

    (rw/draw-workspace-static instance)
    (rw/draw-workspace-mutable instance)
    #_(ro/draw-options instance)
    (rc/render app)

    instance))