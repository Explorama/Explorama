(ns de.explorama.main.wrapper.env
  (:require [clojure.string :as string]
            [de.explorama.main.wrapper.file :refer [file-exists?]]
            [electron :refer [app screen]]
            [os]
            [path]
            [process]))

(defn get-env [k]
  (aget process "env" k))

(defn is-prod? []
  (not goog.DEBUG))

(defn static-file-path [filename]
  (let [dirname js/__dirname
        public? (string/includes? dirname "public")
        prefix  (if public? "/" "/public/")] ;ugly but works xD
    (if (is-prod?)
      (str dirname prefix filename)
      (->> (path/join dirname "../../../../../../" filename)
           (path/resolve)))))

(defn os-name []
  (let [os (.platform os)]
    (case os
      "win32" "Windows"
      "darwin" "Mac"
      "Linux")))

(defn get-window-display [window & {:keys [filter-keys]
                                    :or {filter-keys [:id :label :bounds]}}]
  (cond-> (js->clj (screen.getDisplayMatching (.getNormalBounds window))
                   :keywordize-keys true)
    (vector? filter-keys)
    (select-keys filter-keys)))

(defn get-displays [& {:keys [filter-keys]
                       :or {filter-keys [:id :label :bounds]}}]
  (mapv (fn [display]
          (cond-> display
            (vector? filter-keys)
            (select-keys filter-keys)))
        (js->clj (screen.getAllDisplays)
                 :keywordize-keys true)))

(defn screen-config-exists? [screen-config]
  (some #(= % screen-config)
        (get-displays)))

(defn screen-center [{{:keys [width height x y]} :bounds}
                     & {content-width :width
                        content-height :height}]
  (let [w-center (int (/ width 2))
        h-center (int (/ height 2))
        x (cond-> (+ x w-center)
            content-width (- (int (/ content-width 2))))
        y (cond-> (+ y h-center)
            content-height (- (int (/ content-height 2))))]
    {:x x
     :y y}))

(defn is-portable? []
    ;; a bit hacky, but works to decide if installed locally or not
  (not (file-exists? "Uninstall Explorama.exe")))

(def app-env
  {:os (os-name)
   :os-details {:arch (aget process "arch")
                :platform (aget process "platform")
                :system-version (process.getSystemVersion)}
   :electron-version (aget process "versions" "electron")
   :node-version (aget process "versions" "node")
   :modules-version (aget process "versions" "modules")
   :chromium-version (aget process "versions" "chrome")
   :executable-path (.getPath app "exe")
   :app-path (.getAppPath app)
   :portable? (is-portable?)
   :production-mode? (is-prod?)})