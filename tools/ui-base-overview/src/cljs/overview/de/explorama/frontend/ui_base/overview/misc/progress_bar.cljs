(ns de.explorama.frontend.ui-base.overview.misc.progress-bar
  (:require [de.explorama.frontend.ui-base.components.misc.progress-bar :refer [progress-bar default-parameters parameter-definition]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Progress bar"
   :desc "Component to display an progress or numeric state. Is used by progress-loading-screen component"
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [progress-bar]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [progress-bar {:progress 0}]
  {:title "0% Progress"})

(defexample
  [progress-bar {:progress 50}]
  {:title "50% Progress"})

(defexample
  [progress-bar {:progress 100}]
  {:title "100% Progress"})

(defexample
  [progress-bar {:progress 100
                 :animated? false}]
  {:title "No animation"})

(defn do-with-timeout [func timeout]
  (js/window.setTimeout #(do
                           (func)
                           (do-with-timeout func timeout))
                        timeout))

(defexample
  (let [progress (reagent/atom 0)]
    (do-with-timeout #(if (>= @progress 100)
                        (reset! progress 0)
                        (swap! progress inc))
                     25)
    [progress-bar {:progress progress}])
  {:title "With derefable"
   :code-before "(defn do-with-timeout [func timeout]
 (js/window.setTimeout #(do
                         (func)
                         (do-with-timeout func timeout))
                       timeout))"})