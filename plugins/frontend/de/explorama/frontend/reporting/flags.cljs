(ns de.explorama.frontend.reporting.flags
  (:require [taoensso.timbre :refer-macros [debug error]]))

(def provider-flags
  {:fi-name "reporting"
   :activate-logging? false
   :data-interaction? false
   :legend-default-open? false
   :force-read-only? true})

(defn provide-flags [db details frame-id]
  ;; Maybe we want to support dashboard/report related flags later
  provider-flags)