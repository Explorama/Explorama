(ns de.explorama.backend.rights-roles.http-util
  (:require [jsonista.core :as json]
            [org.httpkit.client :as http-client]
            [taoensso.timbre :refer [error]]))

(defn safe-http-post [url params]
  (try
    (let [{:keys [status body]} @(http-client/post url params)
          parsed-body (json/read-value body json/keyword-keys-object-mapper)]
      (if (= status 200)
        parsed-body
        (do
          (error "Unexpected response from http/post" {:url url
                                                       :status status
                                                       :body body})
          nil)))
    (catch Throwable e
      (error e "Error while making http/post call" {:url url
                                                    :params params}))))

(defn safe-http-get [url params]
  (try
    (let [{:keys [status body]} @(http-client/get url params)
          parsed-body (json/read-value body json/keyword-keys-object-mapper)]
      (if (= status 200)
        parsed-body
        (do
          (error "Unexpected response from http/get" {:url url
                                                      :status status
                                                      :body body})
          nil)))
    (catch Throwable e
      (error e "Error while making http/get call" {:url url
                                                   :params params}))))
