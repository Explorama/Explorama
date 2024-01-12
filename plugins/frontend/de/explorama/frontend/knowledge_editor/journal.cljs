(ns de.explorama.frontend.knowledge-editor.journal
  (:require [de.explorama.frontend.knowledge-editor.markdown-helper :as md-helper]
            [de.explorama.frontend.ui-base.components.formular.core :refer [textarea]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(re-frame/reg-event-db
 ::save
 (fn [db [_ frame-id editor-value]]
   db))

(defn editor [frame-id default-value editor-state]
  (reset! editor-state default-value)
  (fn []
    [textarea {:value @editor-state
               :max-length 2056
               :extra-class "input--w100 h-full textarea-h-full"
               :on-change (fn [new-val]
                            (reset! editor-state new-val))}]))

(defn edit-viewer [frame-id editor-state]
  (let [#_#_tooltip (reagent/atom nil)] ;TODO
    (fn [frame-id editor-state]
      [md-helper/markdown->hiccup editor-state])))