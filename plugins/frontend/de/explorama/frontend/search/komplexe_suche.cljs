(ns de.explorama.frontend.search.komplexe-suche
  (:require [clojure.spec.alpha :as spec]
            [de.explorama.shared.data-format.data-instance :as di]
            [de.explorama.frontend.search.views.formdata]
            [expound.alpha :as expound]))

(set! spec/*explain-out* expound/printer)

(def ignore-di-keys [::di/id
                     ::di/data-reference-point
                     ::di/original-data-reference-point])
