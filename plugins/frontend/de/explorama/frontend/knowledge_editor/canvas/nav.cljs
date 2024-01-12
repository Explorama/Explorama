(ns de.explorama.frontend.knowledge-editor.canvas.nav
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.knowledge-editor.canvas.stages :as stages]
            [de.explorama.shared.common.configs.mouse :refer [mouse-default
                                                              pref-key]]
            [re-frame.core :as re-frame]))

(defn- prepare-config [preference-value]
  (->> (filter #(= :woco (get % :context)) preference-value)
       (mapv #(select-keys % [:button :action]))))

; Set this just in case - the watcher should overwrite this value eventually
(def ^:private mouse-buttons-state (atom (prepare-config mouse-default)))

(defn mouse-buttons []
  @mouse-buttons-state)

(defn panning-event? [e]
  (some (fn [{:keys [button action]}]
          (and (= button (aget e "which"))
               (= action :panning)))
        (mouse-buttons)))

(defn select-event? [e]
  (some (fn [{:keys [button action]}]
          (and (= button (aget e "which"))
               (= action :select)))
        (mouse-buttons)))

(def ^:private context-menu-button 3)

(defn context-menu-event? [event]
  (= (aget event "which")
     context-menu-button))

(re-frame/reg-event-fx
 ::set-mouse-layout
 (fn [_ [_ preference-value]]
   (reset! mouse-buttons-state (prepare-config preference-value))
   {}))

(re-frame/reg-event-fx
 ::init
 (fn [_ _]
   {:dispatch (fi/call-api [:user-preferences :add-preference-watcher-event-vec]
                           pref-key
                           [::set-mouse-layout]
                           mouse-default)}))

(def ^:private panning? (atom false))

(defn- zoom-level [z zoom overview-factor]
  (cond :else 0))

(defn- is-panning-wheel? [e]
  (not= (aget e "wheelDeltaX") 0))

(defn- safe-number? [number]
  ;; Ensures that:
  ;;   * Number is not null
  ;;   * Number is not infinity/-infinity (number? is true for infinity)
  ;;   * Number is not NaN
  ;;   * Number is a number
  ;;   See https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/Number/isFinite
  (js/Number.isFinite number))

(defn set-transform-main [stage x y z]
  (when stage
    (set! (.-x (.-position stage)) x)
    (set! (.-y (.-position stage)) y)
    (set! (.-x (.-scale stage)) z)
    (set! (.-y (.-scale stage)) z)))

(defn set-transform-main-pos [stage x y]
  (when stage
    (set! (.-x (.-position stage)) x)
    (set! (.-y (.-position stage)) y)))

(def ^:private max-wheel-delta 720)
(def ^:private min-wheel-delta -720)

(defn- calculate-zoom [path state x-offset y-offset wheel-delta]
  (let [{:keys [x y z zoom]} (get state path)
        old-coords [x y z]
        wheel-delta (cond
                      (> wheel-delta max-wheel-delta) max-wheel-delta
                      (< wheel-delta min-wheel-delta) min-wheel-delta
                      :else wheel-delta)
        delta (+ 1
                 (* 0.1
                    (/ wheel-delta 120)))
        new-z (* z delta)
        mx (- x-offset)
        my (- y-offset)
        x (- (* (/ x z)
                new-z)
             (- mx (* mx delta)))
        y (- (* (/ y z)
                new-z)
             (- my (* my delta)))
        [x y z :as new-coord] [x y new-z]]
    (when (and (not= old-coords new-coord)
               (safe-number? x)
               (safe-number? y)
               (safe-number? z))
      [x y z zoom])))

(defn zoom [state event]
  (let [stage-key stages/main-stage
        [x y z new-zoom-level :as new-coords]
        (calculate-zoom [:pos stage-key]
                        state
                        (.-offsetX event)
                        (.-offsetY event)
                        (.-wheelDelta event))]
    (if new-coords
      (let [app (:app state)]
        (set-transform-main (stages/get-stage state stages/main-stage)
                            x y z)
        (update state [:pos stage-key] merge {:x x :y y :z z}))
      state)))

(defn- pan [state event wheel?]
  (let [stage-key stages/main-stage
        {opmx :pmx
         opmy :pmy
         app :app
         :as state}
        state
        {:keys [zoom x y z]}
        (get state [:pos stage-key])
        old-coords [x y z]
        [mx my pmx pmy new-x new-y]
        (if wheel?
          (let [new-x (+ x (.-deltaX event))
                new-y (+ y (.-deltaY event))]
            [(- (.-offsetX event))
             (- (.-offsetY event))
             nil nil
             new-x new-y])
          (let [mx (- (.-offsetX event))
                my (- (.-offsetY event))
                pmx (if opmx opmx mx)
                pmy (if opmy opmy my)
                new-x (+ x (- pmx mx))
                new-y (+ y (- pmy my))]
            [mx my pmx pmy new-x new-y]))
        [x y z :as new-coord] [new-x new-y z]]
    (if (and (or (not= old-coords new-coord)
                 (not= pmx opmx)
                 (not= pmy opmy))
             (safe-number? x)
             (safe-number? y)
             (safe-number? z))
      (do
        (set-transform-main-pos (stages/get-stage state stages/main-stage)
                                x y)
        (-> (update state [:pos stage-key] merge {:x x :y y})
            (merge {:pmx mx
                    :pmy my})))
      state)))

(defn wheel [instance]
  (fn [e]
    (if (is-panning-wheel? e)
      (do (reset! instance (pan @instance e true))
          (stages/render @instance))
      (do
        (reset! instance (zoom @instance e))
        (.stopPropagation e)
        (.preventDefault e)
        (stages/render @instance)))))

(defn mousedown [instance]
  (fn [e]
    (when (panning-event? e)
      (swap! instance assoc :pmx nil :pmy nil)
      (reset! panning? true))))

(defn mousemove [instance]
  (fn [e]
    (when @panning?
      (reset! instance (pan @instance e false))
      (stages/render @instance))))

(defn mouseup [instance]
  (fn [e]
    (when (panning-event? e)
      (swap! instance assoc :pmx nil :pmy nil)
      (reset! panning? false))))

(defn mouseleave [instance]
  (fn [e]
    (when (panning-event? e)
      (swap! instance assoc :pmx nil :pmy nil)
      (reset! panning? false))))