(ns de.explorama.frontend.woco.notes.plugin-impl
  (:require [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :refer [reg-sub subscribe]]
            [de.explorama.frontend.woco.notes.states :refer [instance-style]]
            [de.explorama.frontend.woco.notes.toolbar :refer [toolbar-impl]]))

(reg-sub
 ::title-prefix
 (fn [db [_ _frame-id vertical-count-number]]
   (let [note-label (i18n/translate db :notes)]
     (str note-label " " vertical-count-number))))

(def frame-header-impl
  {:frame-icon :note
   :frame-title-prefix-sub
   (fn [frame-id vertical-count-number]
     (subscribe [::title-prefix frame-id vertical-count-number]))})

(def desc
  {:toolbar toolbar-impl
   :frame {:extra-style instance-style}
   :frame-header frame-header-impl
   :product-tour nil})
