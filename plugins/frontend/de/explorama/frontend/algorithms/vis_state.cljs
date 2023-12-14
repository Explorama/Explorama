(ns de.explorama.frontend.algorithms.vis-state
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.frontend.common.i18n :as i18n]
            [re-frame.core :as re-frame]
            [de.explorama.frontend.algorithms.config :as config]
            [de.explorama.frontend.algorithms.path.core :as path]))

(re-frame/reg-event-fx
 ::restore-vis-desc
 (fn [_ [_ frame-id {:keys [di task local-filter used-settings]}]]
   {:dispatch-n (cond-> []
                  (and task di)
                  (conj [:de.explorama.frontend.algorithms.view/connect-to-di frame-id di]
                        [:de.explorama.frontend.algorithms.view/set-filter di frame-id local-filter]
                        [:de.explorama.frontend.algorithms.components.reduced-result/update-settings frame-id used-settings]
                        [:de.explorama.frontend.algorithms.components.main/submit-task frame-id task]))}))

(defn vis-desc [db frame-id]
  (let [task (get-in db (path/last-prediction-task frame-id))
        {:keys [predictions]} (get-in db (path/result frame-id))
        r2-values (for [{:keys [algorithm prediction-statistics]} predictions]
                    (hash-map :algorithm algorithm
                              :value (or (-> (filter #(= "R2" (:name %)) prediction-statistics)
                                             first
                                             (get :value))
                                         0)))
        conetxt-menu-options @(re-frame/subscribe [:de.explorama.frontend.algorithms.components.reduced-result/available-options frame-id])
        {best-prediction :algorithm}
        (->> r2-values
             (sort-by :value >)
             first)]
    (cond-> {:di (get-in db (path/data-instance-consuming frame-id))
             :vertical config/default-vertical-str
             :tool config/tool-name
             :used-settings {:selection best-prediction}
             :task task
             :title (fi/call-api :full-frame-title-raw frame-id)
             :local-filter (get-in db (path/frame-filter frame-id))}
      (and conetxt-menu-options (seq conetxt-menu-options))
      (assoc :context-menu {;:legend? true
                            :options-title @(re-frame/subscribe [::i18n/translate :algorithm])
                            :options conetxt-menu-options}))))
