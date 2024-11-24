(ns de.explorama.frontend.ui-base.overview.utils.client
  (:require [de.explorama.frontend.ui-base.utils.client :as client]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Client"
   :require-statement "[de.explorama.frontend.ui-base.utils.client :as client]"
   :desc "Functions for getting informations from client like os"})

(defdocu
  {:fn-metas (meta #'client/client-os)
   :returns [:win :mac :linux :unix :unknown]})

(defdocu
  {:fn-metas (meta #'client/is-win-client?)
   :returns :boolean})

(defdocu
  {:fn-metas (meta #'client/console-open?)
   :returns :boolean})