(ns de.explorama.frontend.ui-base.overview.utils.select
  (:require [de.explorama.frontend.ui-base.utils.select :as select]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Select"
   :require-statement "[de.explorama.frontend.ui-base.utils.select :as select]"
   :desc "Functions to handle options for select-component"})

(defdocu
  {:fn-metas (meta #'select/selected-option)
   :returns :map})

(defdocu
  {:fn-metas (meta #'select/selected-options)
   :returns :vector})

(defdocu
  {:fn-metas (meta #'select/normalize)
   :returns :any})

(defdocu
  {:fn-metas (meta #'select/to-option)
   :returns :map})

(defdocu
  {:fn-metas (meta #'select/vals->options)
   :returns :vector})