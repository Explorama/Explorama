(ns de.explorama.backend.projects.util.core
  (:require [clojure.string :as str]
            [de.explorama.shared.common.unification.misc :refer [cljc-uuid]]))

(defn request-user-roles [& [http-options]]
  {})
  ;; (group-by
  ;;  :role
  ;;  (:body
  ;;   (discovery-client-reqw VERTICALS
  ;;                          (fn call-fn [services]
  ;;                            (http/get (str (get-in services [:rechte-rollen :url])
  ;;                                           "/api/all-users")
  ;;                                      (merge (or http-options {})
  ;;                                             {:as :json})))))))

(defn name-with-ns
  [obj]
  (if obj
    (cond
      (str/starts-with? obj ":") (subs obj 1)
      :else obj)
    obj))

(defn gen-project-id []
  (cljc-uuid))

(defn username [user-info]
  (if (map? user-info)
    (:username user-info)
    user-info))

(defn apply-dissoc [col keys]
  (apply dissoc col keys))