(ns de.explorama.frontend.common.validation
  (:require [clojure.set :as set]
            [malli.core :as m]
            [taoensso.timbre :refer [error]]))

(def ^:private debug? ^boolean goog.DEBUG)

(def ^:private validates (atom #{}))

(defn enable-validation [namespaces-]
  (swap! validates set/union (into #{} (set namespaces-))))

(defmacro validate? [event spec desc]
  (when debug?
    `(when (and (or (@validates (namespace ~event))
                    (@validates ~event))
                (not (m/validate ~spec ~desc)))
       (error ~event
              (-> (m/explain ~spec ~desc)
                  (me/humanize))))))
