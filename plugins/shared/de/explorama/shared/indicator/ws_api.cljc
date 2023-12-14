(ns de.explorama.shared.indicator.ws-api)

(def update-user-info ::update-user-info)

(def load-indicator-ui-descs ::load-ui-descriptions)
(def loaded-indicator-ui-descs :indicator/loaded-ui-descriptions)

(def connect-to-di ::connect-to-datainstance)
(def connect-to-di-result :indicator/connected-to-datainstance)

(def create-new-indicator ::create-new-indicator)
(def create-new-indicator-result :indicator/created-new-indicator)

(def update-indicator-infos ::update-indicator-infos)
(def update-indicator-infos-result :indicator/updated-indicators)

(def all-indicators ::all-indicators)
(def all-indicators-result :indicator/all-indicators-result)

(def share-indicator ::share-indicator)
(def share-indicator-result :indicator/shared-result)

(def delete-indicator ::delete-indicator)
(def delete-indicator-result :indicator/indicator-deleted)

(def broadcast-indicator-updated :indicator/indicator-updated)

(def data-sample ::data-sample)
(def data-sample-result :indicator/data-sample-result)

(def create-and-publish-di ::create-and-publish-di)
(def publish-di-success :indicator/di-created)