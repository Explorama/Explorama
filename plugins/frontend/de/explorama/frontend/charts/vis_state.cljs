(ns de.explorama.frontend.charts.vis-state
  (:require [de.explorama.frontend.charts.charts.backend-interface :as charts-backend-interface]
            [de.explorama.frontend.charts.charts.settings :as chart-settings]
            [de.explorama.frontend.charts.config :as vconfig]
            [de.explorama.frontend.charts.path :as path]
            [de.explorama.frontend.charts.util.queue :as queue-util]
            [de.explorama.frontend.common.frontend-interface :as fi]
            [de.explorama.shared.charts.ws-api :as ws-api]
            [re-frame.core :as re-frame]))

(def chart-options-keys
  [path/chart-desc-id-key
   path/aggregate-method-key
   path/y-option-key
   path/r-option-key
   path/sum-by-option-key
   path/sum-by-values-key
   path/sum-remaining-key

   path/min-occurence-key
   path/use-nlp?-key
   path/use-nlp-attributes-key
   path/attributes-key
   path/search-selection-key])

(defn current-charts-options-from-db [db frame-id]
  (let [{x-default :x
         y-default :y} (chart-settings/default-options frame-id)]
    {path/x-option-key (get-in db (path/x-option frame-id) x-default)
     path/charts-key (mapv (fn [chart-index]
                             [(get-in db (path/desc-id frame-id chart-index) (get chart-settings/default-chart-desc path/chart-desc-id-key))
                              (get-in db (path/aggregate-method frame-id chart-index) ws-api/default-aggregation-method)
                              (get-in db (path/y-option frame-id chart-index) {:value y-default
                                                                               :label y-default})
                              (get-in db (path/r-option frame-id chart-index))
                              (get-in db (path/sum-by-option frame-id chart-index) {:value "all"})
                              (vec (get-in db (path/sum-by-values frame-id chart-index) []))
                              (get-in db (path/sum-remaining frame-id chart-index))

                              (get-in db (path/min-occurence frame-id chart-index) 1)
                              (get-in db (path/use-nlp? frame-id chart-index) true)
                              (get-in db (path/use-nlp-attributes frame-id chart-index) [{:value "notes"}])
                              (get-in db (path/attributes frame-id chart-index) [])
                              (get-in db (path/search-selection frame-id) {:value :all})])
                           (range (count (get-in db (path/charts frame-id)))))}))

(re-frame/reg-event-fx
 ::restore-vis-desc
 (fn [{db :db} [_ frame-id {:keys [chart-desc di local-filter]}]]
   (let [chart-desc-map (update chart-desc
                                :charts
                                #(mapv (partial zipmap chart-options-keys)
                                       %))]
     {:fx (cond-> []
            chart-desc
            (conj [:dispatch [:de.explorama.frontend.charts.charts.settings/use-preexisting-settings frame-id chart-desc-map]])
            di
            (conj [:dispatch [::queue-util/queue-wrapper frame-id
                              [::charts-backend-interface/connect-to-datainstance
                               {:frame-target-id frame-id
                                :di di
                                :keep-selection? true}]
                              local-filter]]))})))

(defn vis-desc [db frame-id]
  {:di (get-in db (path/frame-di frame-id))
   :vertical vconfig/default-vertical-str
   :tool vconfig/tool-name-charts
   :chart-desc (current-charts-options-from-db db frame-id)
   :title (fi/call-api :full-frame-title-raw frame-id)
   :local-filter (fi/call-api :frame-filter-db-get db frame-id)})
