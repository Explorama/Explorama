(ns de.explorama.frontend.ui-base.utils.view
  (:require [taoensso.timbre :refer-macros [warn]]))

(defn ^:export bounding-rect-node
  "Returns the current calculated size and position properties from given dom-node.

| Parameter    | description  |
| ------------- | ------------- |
| `node`  | The dom-node |
| `selector-keys`  | Keys which should be extracted. Default: [:left :top :width :height] |
   
   Example:
   ```clojure
  => (def node (js/document.getElementById \"my-dom-node-id\")
  => (bounding-rect-node nil)
  => WARN [de.explorama.frontend.ui-base.utils.view:97] - No dom-node provided to calculate bounding-rect
  => (bounding-rect-node node)
  => {:left 20 :top 10 :width 300 :height :100}
  => (bounding-rect-node node [:width :height :bottom])
  => {:width 300 :height :100 :bottom 250} 
   ```
   "
  ([node selector-keys]
   (if-not node
     (warn "No dom-node provided to calculate bounding-rect")
     (when-let [rect (.getBoundingClientRect node)]
       (reduce (fn [acc select-k]
                 (if-let [select-v (aget rect (name select-k))]
                   (assoc acc select-k select-v)
                   acc))
               {}
               selector-keys))))
  ([node]
   (bounding-rect-node node [:left :top :width :height])))

(defn ^:export bounding-rect-id
  "Same as `bounding-rect-node` but with given dom-node-id instead of dom-node
   
   Example:
   ```clojure
  => (bounding-rect-id nil)
  => WARN [de.explorama.frontend.ui-base.utils.view:125] - Dom-node with id \"\" does not exist to calculate bounding-rect
  => (bounding-rect-id \"test\")
  => WARN [de.explorama.frontend.ui-base.utils.view:125] - Dom-node with id \"test\" does not exist to calculate bounding-rect
  => (bounding-rect-id \"my-dom-node-id\")
  => {:left 20 :top 10 :width 300 :height :100}
  => (bounding-rect-id \"my-dom-node-id\" [:width :height :bottom])
  => {:width 300 :height :100 :bottom 250} 
   ```"
  ([dom-id selector-keys]
   (if-let [node (js/document.getElementById dom-id)]
     (if selector-keys
       (bounding-rect-node node selector-keys)
       (bounding-rect-node node))
     (warn (str "Dom-node with id \"" dom-id "\" does not exist to calculate bounding-rect"))))
  ([dom-id]
   (bounding-rect-id dom-id nil)))

(defn ^:export is-inside?
  "Checks if the gives dom-node is a div or node-subtree witch has the class defined in search-term.
   
   Example:
   => (is-inside? <dom-node> \".explorama__button\")
   => true/false"
  [dom-node search-term]
  (boolean
   (and
    dom-node
    search-term
    (.closest dom-node search-term))))