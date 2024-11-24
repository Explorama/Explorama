(ns de.explorama.frontend.ui-base.overview.misc.copy-field
  (:require [de.explorama.frontend.ui-base.components.misc.copy-field :refer [copy-field default-parameters parameter-definition]]
            [de.explorama.frontend.ui-base.overview.page :refer [defcomponent defexample]]))

(defcomponent
  {:name "Copy Field"
   :desc "Input-Field that is only for the user to copy the current content to the clipboard."
   :require-statement "[de.explorama.frontend.ui-base.components.misc.core :refer [copy-field]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defexample
  [copy-field {:copy-value "https://explorama.de"
               :aria-label "copy field"}]
  {:title "Simple Copy Field"})