(ns de.explorama.shared.charts.ws-api)

(def update-user-info ::update-user-info)

(def set-backend-canceled ::set-backend-canceled)

(def retrieve-external-ref ::retrieve-external-ref)
(def retrieve-external-ref-result ::retrieve-external-ref-result)

;; -----  charts  -----

(def default-x-attribute "month")
(def default-selected-x {:label default-x-attribute
                         :value default-x-attribute})
(def default-aggregation-method :sum)
(def default-sum-by "all")
(def default-sum-filter [])
(def default-stopping-attrs [{:label "notes"
                              :value "notes"}])
(def default-stemming-attrs [])

(def chart-datasets ::chart-datasets)
(def chart-datasets-result ::chart-datasets-result)
(def chart-error ::chart-error)
