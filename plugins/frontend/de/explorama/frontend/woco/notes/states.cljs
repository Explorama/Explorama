(ns de.explorama.frontend.woco.notes.states
  (:require "quill"
            [reagent.core :as r]
            [de.explorama.frontend.woco.config :as config]))

(def ^:private  default-editor-config
  {:theme "snow"
   :formats ["bold" "italic" "underline" "strike" "blockquote"
             "list" "color" "background" "size" "align" "clean"]
   :modules {:history {:maxStack 40}
             :toolbar nil}})

(defn notes-id [frame-id]
  (str (config/frame-dom-id frame-id) "-notes"))

(defonce ^:private instances (r/atom {}))
(defonce ^:private in-edit-mode (r/cursor instances [:in-edit-mode]))
(defonce ^:private in-hover-mode (r/cursor instances [:in-hover-mode]))
(defonce ^:private on-top (r/cursor instances [:on-top]))


(defn clean-states []
  (reset! instances {}))

(defn set-text [frame-id init-text]
  (swap! instances assoc-in [frame-id :init-text] init-text))

(defn get-text [frame-id]
  (r/cursor instances [frame-id :init-text]))

(defn set-bg-color [frame-id bg-color]
  (swap! instances assoc-in [frame-id :bg-color] bg-color))

(defn get-bg-color [frame-id]
  (r/cursor instances [frame-id :bg-color]))

(defn set-formatting [frame-id formatting]
  (swap! instances assoc-in [frame-id :formatting] formatting))

(defn get-formatting
  ([frame-id]
   (r/cursor instances [frame-id :formatting]))
  ([frame-id prop]
   (r/cursor instances [frame-id :formatting prop])))

(defn get-instance [frame-id]
  (r/cursor instances [frame-id :instance]))

(defn init-note-state [frame-id]
  (set-text frame-id nil)
  (set-bg-color frame-id config/note-default-bgcolor))

(defn toolbar-id [frame-id]
  (str "note-tb" (:frame-id frame-id)))

(defn create-instance [frame-id]
  (let [div-id (notes-id frame-id)
        instance (when (js/document.getElementById div-id)
                   (js/Quill. (str "#" div-id)
                              (clj->js default-editor-config)))]
    (swap! instances assoc-in [frame-id :instance] instance)
    instance))

(defn get-note-content [frame-id]
  (when-let [instance (deref (get-instance frame-id))]
    (->> instance
         (.getContents)
         (.stringify js/JSON))))

(defn set-instance-content [frame-id content]
  (when-let [instance (deref (get-instance frame-id))]
    (.setContents instance (js/JSON.parse content))))

(defn copy-note-state [source-frame-id target-frame-id]
  (let [source-text (get-note-content source-frame-id)
        source-color @(get-bg-color source-frame-id)]
    (set-instance-content target-frame-id source-text)
    (set-text target-frame-id source-text)
    (set-bg-color target-frame-id source-color)))

(defn destroy-instance [frame-id]
  (swap! instances dissoc frame-id))

(defn set-edit-mode [frame-id]
  (reset! in-edit-mode frame-id))

(defn in-edit-mode? [frame-id]
  (= frame-id @in-edit-mode))

(defn set-hover-mode [frame-id]
  (reset! in-hover-mode frame-id))

(defn in-hover-mode? [frame-id]
  (= frame-id @in-hover-mode))

(defn instance-style [frame-id]
  {:background-color @(get-bg-color frame-id)})
