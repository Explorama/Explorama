(ns de.explorama.frontend.woco.navigation.panning-handler
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.frame.interaction.move :refer [moving-state]]
            [de.explorama.frontend.woco.navigation.control :as navigation-control]
            [de.explorama.frontend.woco.workspace.config :as wwconfig]))

(defn mouse-down [event]
  (when (and (wwconfig/panning-event? event)
             (not (moving-state)))
    (navigation-control/set-mouse-position (aget event "pageX")
                                           (aget event "pageY"))
    (navigation-control/set-mouse-position-start (aget event "pageX")
                                                 (aget event "pageY")
                                                 (js/Date.now))
    (when (wwconfig/context-menu-event? event)
      (navigation-control/set-ignore-context-menu :maybe))))

(defn mouse-move [event]
  (let [^number mx (aget event "pageX")
        ^number my (aget event "pageY")]
    (when (and (not (navigation-control/is-panning?))
               (navigation-control/mouse-position-start))
      (let [{:keys [^number x ^number y]}
            (navigation-control/mouse-position-start)]
        (when (< 10 (Math/sqrt (+ (Math/pow (- x mx) 2)
                                  (Math/pow (- y my) 2))))
          (navigation-control/start-panning)
          (navigation-control/set-mouse-position-start nil)
          (when (= :maybe (navigation-control/ignore-context-menu?))
            (navigation-control/set-ignore-context-menu :yes)))))
    (when (navigation-control/is-panning?)
      (let [{^number opmx :x
             ^number opmy :y}
            (navigation-control/mouse-position)
            pmx (if opmx opmx mx)
            pmy (if opmy opmy my)]
        (re-frame/dispatch [::navigation-control/pan
                            (- pmx mx)
                            (- pmy my)])))
    (navigation-control/set-mouse-position mx my)))

(defn mouse-up [event]
  (when (navigation-control/is-panning?)
    (.preventDefault event)
    (.stopPropagation event))
  (navigation-control/stop-panning))

(defn mouse-leave [event]
  (navigation-control/stop-panning))
