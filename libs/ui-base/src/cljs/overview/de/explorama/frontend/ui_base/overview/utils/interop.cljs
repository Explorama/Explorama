(ns de.explorama.frontend.ui-base.overview.utils.interop
  (:require [de.explorama.frontend.ui-base.utils.interop :as interop]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Interop"
   :require-statement "[de.explorama.frontend.ui-base.utils.interop :as interop]"
   :desc "Util functions for javascript interops"})

(defdocu
  {:fn-metas (meta #'interop/safe-aget)
   :returns :any})

(defdocu
  {:fn-metas (meta #'interop/format)
   :returns :string})

(defdocu
  {:fn-metas (meta #'interop/safe-number?)
   :returns :boolean})

