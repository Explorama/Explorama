(ns de.explorama.frontend.ui-base.overview.common.error-boundary
  (:require [de.explorama.frontend.ui-base.components.common.error-boundary :refer [error-boundary default-parameters parameter-definition]]
            [reagent.core :as reagent]
            [de.explorama.frontend.ui-base.overview.page :refer-macros [defcomponent defexample]]))

(defcomponent
  {:name "Error Boundary"
   :desc "Handles component crash"
   :require-statement "[de.explorama.frontend.ui-base.components.common.core :refer [error-boundary]]"
   :default-parameters default-parameters
   :parameters parameter-definition})

(defn test-crash []
  (throw "Crash me"))

(defexample
  [error-boundary
   [test-crash]]
  {:title "Simple crash"
   :code-before
   "(defn test-crash []
  (throw \"Crash me\"))"})