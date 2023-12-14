(ns de.explorama.frontend.ui-base.overview.page)

(defmacro defcomponent [infos]
  (list* `de.explorama.frontend.ui-base.overview.renderer.page/add-component-infos
         [(assoc infos
                 :namespace (str (:name (:ns &env))))]))

(defn- add-example [fn-ns fn-body code options]
  (list* `de.explorama.frontend.ui-base.overview.renderer.page/add-example
         (merge options
                {:fn-namespace fn-ns
                 :plain code
                 :code (get code 1)})
         [fn-body]))

(defmacro defexample [fn-body & options]
  (add-example (str (:name (:ns &env)))
               fn-body
               (mapv str &form)
               (first options)))

(defmacro defdocu [infos]
  (list* `de.explorama.frontend.ui-base.overview.renderer.page/add-doc
         [(assoc infos
                 :namespace (str (:name (:ns &env))))]))

(defmacro defutils [infos]
  (list* `de.explorama.frontend.ui-base.overview.renderer.page/add-doc-infos
         [(assoc infos
                 :namespace (str (:name (:ns &env))))]))

