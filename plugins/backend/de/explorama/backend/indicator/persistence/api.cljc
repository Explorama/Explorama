(ns de.explorama.backend.indicator.persistence.api
  "This api is mainly used by the tubes namespace.
   It wraps the usage of the persistence core so the core doesn't
   need infos how to propagate the result."
  (:require [de.explorama.backend.indicator.persistence.core :as persistence]))

(defn all-user-indicators [{:keys [client-callback failed-callback
                                   user-validation]}
                           [user-info]]
  (if (user-validation user-info)
    (client-callback (persistence/all-user-indicators user-info))
    (failed-callback)))

(defn create-new-indicator [{:keys [client-callback failed-callback
                                    user-validation]}
                            [user-info indicator]]
  (if (user-validation user-info)
    (let [creation-result (persistence/create-new-indicator user-info indicator)]
      (client-callback creation-result))
    (failed-callback)))

(defn share-indicator [{:keys [client-callback failed-callback
                               broadcast-callback
                               user-validation]}
                       [current-user-info share-with indicator]]
  (if (user-validation current-user-info)
    (let [{:keys [status data]
           :as share-result} (persistence/share-with-user current-user-info share-with indicator)]
      (when (= status :success)
        (broadcast-callback (:id data)))
      (client-callback share-result))
    (failed-callback)))

(defn update-indicator
  "Update indicator infos without the need to create a new version.
   Only used for changes in name/description/"
  [{:keys [client-callback failed-callback
           user-validation]}
   [user-info indicator]]
  (if (user-validation user-info)
    (let [update-result (persistence/update-indicator user-info indicator)]
      (client-callback update-result))
    (failed-callback)))

(defn delete-indicator [{:keys [client-callback failed-callback
                                user-validation]}
                        [user-info indicator]]
  (if (user-validation user-info)
    (let [deletion-result (persistence/delete-indicator user-info indicator)]
      (client-callback deletion-result))
    (failed-callback)))

(defn indicator-desc [indicator-id]
  (persistence/read-indicator indicator-id))
