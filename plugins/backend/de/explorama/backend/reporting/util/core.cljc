(ns de.explorama.backend.reporting.util.core
  (:require [clojure.string :as str]))

(defn name-with-ns
  [obj]
  (if obj
    (cond
      (str/starts-with? obj ":") (subs obj 1)
      :else obj)
    obj))

(defn username [user-info]
  (cond
    (map? user-info)
    (:username user-info)
    :else
    user-info))

(defn apply-dissoc [col keys]
  (apply dissoc col keys))
