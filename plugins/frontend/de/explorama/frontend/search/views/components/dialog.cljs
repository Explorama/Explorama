(ns de.explorama.frontend.search.views.components.dialog
  (:require [re-frame.core :as re-frame]
            [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.frontend.ui-base.components.frames.core :refer [dialog]]))

(re-frame/reg-event-db
 ::dialog-data
 (fn [db [_ frame-id dialog-data]]
   (assoc-in db (spath/frame-dialog-data frame-id) dialog-data)))

(re-frame/reg-sub
 ::dialog-data
 (fn [db [_ frame-id]]
   (get-in db (spath/frame-dialog-data frame-id))))

(defn frame-dialog [frame-id]
  (let [{:keys [dialog-type message title on-success no-label yes-label icon]
         :as dialog-data}
        @(re-frame/subscribe [::dialog-data frame-id])]
    (when dialog-data
      [dialog (cond-> {:show? true
                       :hide-fn #(re-frame/dispatch [::dialog-data frame-id nil])
                       :message message
                       :title title
                       :no {:label (or no-label [::i18n/translate :ok])
                            :variant :secondary}}
                dialog-type
                (assoc :type dialog-type)
                on-success
                (assoc :yes (cond-> {:label (or yes-label [::i18n/translate :yes])
                                     :on-click on-success}
                              (= :warning dialog-type) (assoc :type :warning)
                              icon (assoc :start-icon :trash))))])))