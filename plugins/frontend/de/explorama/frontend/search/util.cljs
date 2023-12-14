(ns de.explorama.frontend.search.util
  (:require [de.explorama.frontend.search.path :as spath]
            [de.explorama.frontend.search.config :as config]))

(defn index-of
  ([in value accessor]
   (loop [in in
          i 0]
     (if (not-empty in)
       (if (= (accessor (first in)) value)
         i
         (recur (rest in)
                (inc i)))
       nil)))
  ([in value]
   (index-of in value identity)))

(defn clear-path [db path frames]
  (if (seq frames)
    (apply update-in db path dissoc frames)
    db))

(defn clean-frames [db frames]
  (-> (clear-path db config/search-pre-path frames)
      (clear-path [:search :acs] frames)
      (clear-path [:search] frames)
      (clear-path [:search :replay] frames)
      (clear-path [:search :minimize] frames)
      (clear-path [:search :data-instance] frames)
      (clear-path [:search :is-clicked?] frames)
      (clear-path [:search :request-acs] frames)
      (clear-path [:search :create-data-instance] frames)
      (clear-path [:search :search-changed?] frames)
      (clear-path spath/wait-callback frames)
      (clear-path spath/wait-callback-keys frames)
      (clear-path spath/search-query frames)))