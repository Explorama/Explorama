(ns de.explorama.frontend.ui-base.overview.utils.subs
  (:require [de.explorama.frontend.ui-base.utils.subs :as subs]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Subs"
   :require-statement "[de.explorama.frontend.ui-base.utils.subs :as subs]"
   :desc "Functions for handling derefable things like re-frame subscriptions or atoms"})

(defdocu
  {:fn-metas (meta #'subs/is-derefable?)
   :returns :any})

(defdocu
  {:fn-metas (meta #'subs/val-or-deref)
   :returns :any})

(defdocu
  {:fn-metas (meta #'subs/set-translation-fn)
   :returns :any})

(defdocu
  {:fn-metas (meta #'subs/translate-label)
   :returns :any})