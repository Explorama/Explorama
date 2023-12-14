(ns de.explorama.frontend.ui-base.overview.utils.view
  (:require [de.explorama.frontend.ui-base.utils.view :as view]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "View"
   :require-statement "[de.explorama.frontend.ui-base.utils.view :as view]"
   :desc "Functions for handling ui-components or working with dom-nodes"})

(defdocu
  {:fn-metas (meta #'view/bounding-rect-node)
   :returns :map})

(defdocu
  {:fn-metas (meta #'view/bounding-rect-id)
   :returns :map})

(defdocu
  {:fn-metas (meta #'view/is-inside?)
   :returns :boolean})