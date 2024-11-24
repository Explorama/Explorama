(ns de.explorama.frontend.ui-base.overview.utils.specification
  (:require [de.explorama.frontend.ui-base.utils.specification :as spec]
            [de.explorama.frontend.ui-base.overview.page :refer [defutils defdocu]]))

(defutils
  {:name "Malli Specification"
   :require-statement "[de.explorama.frontend.ui-base.utils.specification :as spec]"
   :desc "Functions for downloading and uploading data"})

(defdocu
  {:fn-metas (meta #'spec/enable-validation)
   :signatures ["[]"]})

(defdocu
  {:fn-metas (meta #'spec/validate)
   :signatures ["[component spec params force?]"
                "[component spec params]"]})

(defdocu
  {:fn-metas (meta #'spec/parameters->malli)
   :signatures ["[parameters sub-parameters]"]
   :returns :vector})

(defdocu
  {:fn-metas (meta #'spec/parameters->malli-str)
   :signatures ["[parameters sub-parameters]"]
   :returns :string})