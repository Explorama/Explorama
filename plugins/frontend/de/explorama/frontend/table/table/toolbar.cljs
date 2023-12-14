(ns de.explorama.frontend.table.table.toolbar
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [re-frame.core :refer [dispatch]]))

(def toolbar-impl
  {:on-duplicate-fn (fn [frame-id]
                      (dispatch [:de.explorama.frontend.table.core/duplicate-table-frame frame-id]))
   :on-toggle (fn [frame-id show?])
   :items (fn [frame-id]
            (if-let [read-only? @(fi/call-api [:interaction-mode :read-only-sub?]
                                              {:frame-id frame-id})]
              []
              [:filter]))})