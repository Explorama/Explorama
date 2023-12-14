(ns de.explorama.frontend.map.views.toolbar
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [goog.string.format]
            [re-frame.core :refer [dispatch]]))

(def toolbar-impl
  {:on-duplicate-fn (fn [frame-id]
                      (dispatch [:de.explorama.frontend.map.core/duplicate-map-frame frame-id]))
   :on-toggle (fn [frame-id show?])
   :items (fn [frame-id]
            (let [read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                           {:frame-id frame-id})]
              (if read-only?
                []
                [:filter])))})

