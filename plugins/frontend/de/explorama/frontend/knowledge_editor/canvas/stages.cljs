(ns de.explorama.frontend.knowledge-editor.canvas.stages)

(def main-stage 0)

(defn get-stage [state stage]
  (.getChildAt (aget (:app state)
                     "stage")
               stage))

(defn render
  ([{app :app}]
   (.render (.-renderer app)
            (.-stage app)))
  ([app stage]
   (.render (.-renderer app)
            stage)))

