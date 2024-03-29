(ns de.explorama.backend.expdb.middleware.indexed-db-test
  (:require [clojure.java.io :as io]
            [de.explorama.backend.common.middleware.cache :as idb-cache]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.cache :as cache]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.core :as legacy-ac-core]
            [de.explorama.backend.expdb.legacy.search.data-tile-ref :as dt-api]
            [de.explorama.backend.expdb.persistence.backend-indexed :as backend-indexed]
            [de.explorama.backend.expdb.persistence.indexed :as sut]
            [de.explorama.backend.expdb.persistence.shared :as imp]
            [de.explorama.backend.expdb.query.index :as index]))

(def ^:private config {:backend "browser"
                       :indexed? true
                       :bucket "default"
                       :schema "default"
                       :data-tile-keys {"year" {:field ["Date" "date" "value"]
                                                :date-part :year
                                                :type :string}
                                        "country" {:field ["Context" "country" "name"]
                                                   :type :string}
                                        "datasource" {:field ["Datasource" "datasource" "name"]}
                                        "bucket" {:field :bucket
                                                  :type :string}
                                        "identifier" {:value "search"}}})

(def db (atom (backend-indexed/new-instance config)))

(def ^:private db-key "de.explorama.backend.expdb.indexed-test.sqlite3")

(defn test-setup [imports test-fn]
  (with-redefs [de.explorama.backend.expdb.persistence.backend-indexed/db-key db-key]
    (reset! db (backend-indexed/new-instance config))
    (dt-api/reset-cache)
    (cache/new-ac-cache)
    (idb-cache/reset-states)
    (let [[new-index new-index-inv new-dt-key-index] (sut/get-index @db)]
      (swap! index/expdb-hash->dt-key assoc (sut/schema @db) new-dt-key-index)
      (swap! index/current
             assoc
             (sut/schema @db)
             new-index)
      (swap! index/current-inv
             assoc
             (sut/schema @db)
             new-index-inv)
      (doseq [data imports]
        (imp/transform->import data {} "default"))
      (legacy-ac-core/all-attributes)
      (test-fn)
      (io/delete-file db-key))))
