(ns de.explorama.frontend.ui-base.utils.resize
  (:require ["react" :as react]))

(defn use-parent-size [ref]
  (let [[size set-size] (react/useState nil)]
    (react/useEffect
     (fn []
       (if-let [parent (some-> (.-current ref) (.-parentElement))]
         (let [observer (js/ResizeObserver.
                         (fn [entries]
                           (let [rect (.-contentRect (aget entries 0))]
                             (set-size {:width (.-width rect)
                                        :height (.-height rect)}))))]
           (.observe observer parent)
           (fn [] (.disconnect observer)))
         js/undefined))
     #js [])
    size))
