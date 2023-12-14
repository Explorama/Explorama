(ns de.explorama.frontend.map.configs.overlayer.core
  (:require [de.explorama.frontend.common.frontend-interface :as fi]
            [data-format-lib.aggregations :as dfl-aggr]
            [de.explorama.frontend.map.configs.overlayer.view :as overlayer]
            [de.explorama.frontend.common.i18n :as i18n]
            [de.explorama.shared.map.ws-api :as ws-api]
            [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::aggregate-methods
 (fn [db [_ type]]
   (if (= type "string")
     [{:label (i18n/translate db
                              :designer-layouter-method-first-color
                              true)
       :value         :first-matching-color}
      {:label (i18n/translate db
                              :designer-layouter-method-last-color
                              true)
       :value        :last-matching-color}
      {:label (i18n/translate db
                              :designer-layouter-method-max-color
                              true)
       :value         :max-matching-color}
      {:label (i18n/translate db
                              :designer-layouter-method-min-color
                              true)
       :value        :min-matching-color}]
     (->> (dissoc dfl-aggr/descs :number-of-events)
          (filter (fn [[_ {type :result-type}]]
                    (= type "number")))
          (mapv (fn [[aggr-key {:keys [label]}]]
                  {:label (i18n/translate db
                                          label
                                          true)
                   :value aggr-key}))))))

(re-frame/reg-event-fx
 ::get-acs
 (fn [_ _]
   {:dispatch (fi/call-api :load-users-roles-event-vec)
    :backend-tube-n [[ws-api/load-acs {:client-callback [ws-api/set-acs]}]]}))

(def view overlayer/view)
