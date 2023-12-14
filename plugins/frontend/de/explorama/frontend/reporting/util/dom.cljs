(ns de.explorama.frontend.reporting.util.dom)

(defn fit-size
  "Recalculate size in the way that the content is fit completely into available size respecting the ratio of content"
  [available-width available-height
   content-width content-height]
  (let [av-wratio (/ available-width available-height)
        av-hratio (/ available-height available-width)
        c-wratio (/ content-width content-height)
        c-hratio (/ content-height content-width)
        w-diff (- av-wratio c-wratio)
        h-diff (- av-hratio c-hratio)]
    (if (>= h-diff w-diff)
      ;[width, height]
      [available-width
       (* available-width c-hratio)]
      [(* available-height c-wratio)
       available-height])))

(defn get-node-size [dom-node]
  (when dom-node
    (when-let [rect (.getBoundingClientRect dom-node)]
      {:width (aget rect "width")
       :height (aget rect "height")})))

(defn get-size [dom-node-id]
  (when-let [dom-node (js/document.getElementById dom-node-id)]
    (get-node-size dom-node)))

(defn observe-size [resize-observer-ref element callback]
  (let [ro (js/ResizeObserver. #(let [rect (.getBoundingClientRect element)]
                                  (callback {:width (aget rect "width")
                                             :height (aget rect "height")})))]
    (.observe ro element)
    (reset! resize-observer-ref ro)))