(ns de.explorama.shared.table.ws-api)

(def update-user-info ::update-user-info)

(def set-backend-canceled ::set-backend-canceled)

(def load-event-details ::load-event-details)
(def load-event-details-result ::load-event-details-result)
(def retrieve-external-ref ::retrieve-external-ref)
(def retrieve-external-ref-result ::retrieve-external-ref-result)

;; -----  table  -----

(def di-key :di)
(def filter-key :local-filter)
(def vis-type-key :vis-type)

(def last-page-key :last-page)
(def page-size-key :page-size)
(def current-page-key :current-page)
(def focus-event-id-key :focus-event-id)
(def focus-row-idx-key :focus-row-idx)
(def sorting-key :sorting)
(def freeze-key :freeze)
(def row-count-key :row-count)
(def data-count-key :data-count)
(def data-range-key :data-range)
(def data-key :data)

(def scroll-x-key :scroll-x)
(def scroll-y-key :scroll-y)
(def force-next-request-key :force-next-request)

(def requesting-keys [di-key
                      filter-key
                      vis-type-key
                      page-size-key
                      current-page-key
                      focus-event-id-key
                      sorting-key])

(def logging-keys (conj requesting-keys
                        scroll-x-key
                        scroll-y-key))

(def table-data ::get-table-data)
(def table-data-result ::get-table-data-result)
(def table-data-error ::table-data-error)
