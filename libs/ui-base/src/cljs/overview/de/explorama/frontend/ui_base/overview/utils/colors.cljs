(ns de.explorama.frontend.ui-base.overview.utils.colors
  (:require [de.explorama.frontend.ui-base.utils.colors :as colors]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Colors"
   :require-statement "[de.explorama.frontend.ui-base.utils.colors :as colors]"
   :desc "Functions for handling colors"})

(defdocu
  {:fn-metas (meta #'colors/RGB->sRGB)
   :returns [:vector]})

(defdocu
  {:fn-metas (meta #'colors/css-RGB-string)
   :returns [:string]})

(defdocu
  {:fn-metas (meta #'colors/sRGB->svg-color-matrix)
   :returns [:string]})

(defdocu
  {:fn-metas (meta #'colors/RGB->svg-color-matrix)
   :returns [:string]})

(defdocu
  {:fn-metas (meta #'colors/svg-color-node)
   :returns [:reagent-component]})

(defdocu
  {:fn-metas (meta #'colors/font-color)
   :returns [:string]})