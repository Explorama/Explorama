(ns de.explorama.frontend.ui-base.overview.utils.timeout
  (:require [de.explorama.frontend.ui-base.utils.timeout :as timeout]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Timeout"
   :require-statement "[de.explorama.frontend.ui-base.utils.timeout :as timeout]"
   :desc "Functions for handling timeouts"})

(defdocu
  {:fn-metas (meta #'timeout/handle-timeout)})