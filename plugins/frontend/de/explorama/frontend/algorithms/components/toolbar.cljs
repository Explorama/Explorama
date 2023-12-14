(ns de.explorama.frontend.algorithms.components.toolbar
  (:require [re-frame.core :refer [dispatch]]))

(def toolbar-impl
  {:on-duplicate-fn (fn [frame-id]
                      (dispatch [:de.explorama.frontend.algorithms.view/duplicate-ki-frame frame-id]))
   :on-toggle (fn [frame-id show?])})
