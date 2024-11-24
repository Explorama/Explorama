(ns de.explorama.frontend.woco.workspace.window-creation
  (:require [de.explorama.frontend.woco.workspace.states :as wws]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.woco.path :as path]))

(defn create-window [x y]
  (let [windows @wws/window-creation-mouse]
    (doseq [[idx {:keys [action payload]}] (map-indexed vector windows)]
      (re-frame/dispatch (conj action
                               (assoc payload
                                      :mouse-pos [x y]
                                      :idx idx))))
    (reset! wws/window-creation-mouse nil)))

(defn creating-new-windows? []
  (seq @wws/window-creation-mouse))

(defn cursor []
  (:cursor (first @wws/window-creation-mouse)))

(defn create-new-drop-event [data]
  (swap! wws/window-creation-mouse (fnil conj []) data))

(defn reset-new-window-num [db]
  (assoc-in db (conj path/current-workspace-grid :num) []))