(ns de.explorama.frontend.woco.navigation.resources
  (:require [cljsjs.pixi-legacy]
            [de.explorama.frontend.ui-base.components.misc.icon :as icon]
            [taoensso.timbre :refer [debug]]
            [de.explorama.frontend.woco.config :as config]
            [de.explorama.frontend.woco.frame.color :as frame-color]
            [de.explorama.frontend.woco.screenshot.core :as screenshot]))

(defonce frame-colors (atom {}))
(def mm-prefix "mm-")

(defn frame-color->css-rgb [[r g b]]
  (str "rgb(" r "," g "," b ")"))

(defn color
  ([class default]
   (get @frame-colors class default))
  ([class]
   (color class nil)))

(defn extract-color
  "Read (icon-) color from css and save it as rgb-vec in frame-colors atom"
  [class]
  (let [temp-elem (js/document.createElement "span")
        icon-class (get icon/colors (keyword class))
        dom-id (str "extract_" icon-class)]
    (when icon-class
      (aset temp-elem "style" "width" "10px")
      (aset temp-elem "style" "height" "10px")
      (aset temp-elem "style" "display" "block")
      (.setAttribute temp-elem "id" dom-id)
      (.setAttribute temp-elem "class" icon-class)
      (js/document.body.appendChild temp-elem)
      (screenshot/make-screenshot {:dom-id dom-id
                                   :callback-fn (fn [r]
                                                  (js/document.body.removeChild temp-elem)
                                                  (swap! frame-colors assoc class [(aget r 0)
                                                                                   (aget r 1)
                                                                                   (aget r 2)]))
                                   :type :pixel}))))

(defn mm-texture-id [id]
  (str mm-prefix id))

(defn texture
  "Get texture with given id"
  [texture-id]
  (aget js/PIXI.utils.TextureCache texture-id))

(defn texture-cached?
  "Checks if a texture is cached"
  [texture-id]
  (boolean (texture texture-id)))

(defn- extract-icon
  "Read icon from css and save it as texture in pixi texture-cache"
  [icon-class icon-id]
  (let [temp-elem (js/document.createElement "span")
        icon-class (get-in icon/icon-collection [(keyword icon-class) :class])
        dom-id (str "extract_" icon-class)]
    (when icon-class
      (.setAttribute temp-elem "id" dom-id)
      (.setAttribute temp-elem "class" (str icon-class
                                            " "
                                            (get icon/colors :white)))
      (js/document.body.appendChild temp-elem)
      (screenshot/make-screenshot {:dom-id dom-id
                                   :callback-fn (fn [r]
                                                  (js/document.body.removeChild temp-elem)
                                                  (when (and icon-id (not (texture-cached? icon-id)))
                                                    (js/PIXI.Texture.addToCache (js/PIXI.Texture.from r)
                                                                                icon-id)))
                                   :type :canvas}))))

(defn- extract-colors
  "Extract colors for all group-classes"
  []
  (doseq [color frame-color/group-classes]
    (extract-color color))
  (debug "minimap resources: icon textures loaded"))

(defn load-tool-resource
  "Loading resource for tool, when icon and vertical are defined"
  [{:keys [icon vertical]}]
  (let [texture-id (mm-texture-id vertical)]
    (when (and icon vertical (not (texture-cached? texture-id)))
      (extract-icon icon texture-id))))

(defonce loaded? (atom false))
(defn load-resources
  "Loads relevant resources for drawing like frame-colors and frame-icons"
  []
  (when-not @loaded?
    (reset! loaded? true)
    (debug "load minimap resources")
    (extract-colors)))