(ns de.explorama.backend.expdb.legacy.search.data-tile-ref
  (:require [clojure.edn :as edn]
            [de.explorama.backend.expdb.legacy.search.attribute-characteristics.api :as ac-api]
            [de.explorama.backend.expdb.legacy.search.data-tile :as dt-api]
            [de.explorama.shared.cache.api :as cache-api]
            [de.explorama.shared.cache.core :refer [put-cache]]
            [de.explorama.shared.cache.util :refer [single-destructuring
                                                    single-return-type]]
            [taoensso.tufte :as tufte]))

(defn- new-cache []
  (put-cache
   {:strategy :lru
    :size 2000}

   (atom {})
   (fn [_ _])))

(defonce ^:private request-cache (atom nil))

(defn reset-cache []
  (reset! request-cache (new-cache)))

(defn- clean-entry [attr-d]
  (reduce (fn [acc [key value]]
            (assoc acc key (if (map? value)
                             (dissoc value :i :type :label)
                             value)))
          {}
          (dissoc attr-d :timestamp :valid?)))

(def ^:private type-strip-list #{"decimal" "integer" "notes" "boolean"})

(defn- data-tile-reduce-formdata [attribute-types formdata]
  (tufte/p ::calc-reduce-formdata
           (reduce (fn [acc [[attr node-type :as row] attr-d]]
                     (if (type-strip-list (attribute-types row))
                       (assoc acc [attr node-type] {})
                       (assoc acc [attr node-type] (clean-entry attr-d))))
                   {}
                   formdata)))

(defn- get-data-tiles [formdata]
  (let [attribute-types (ac-api/attribute-types {})
        reduced-formdata (data-tile-reduce-formdata attribute-types formdata)]
    (single-destructuring
     (cache-api/lookup @request-cache
                       [reduced-formdata]
                       {:miss
                        (fn [key _]
                          (single-return-type
                           key
                           (dt-api/get-data-tiles reduced-formdata)))}))))

(defn get-data-tiles-api [tile-ref]
  (-> (get tile-ref :formdata "")
      edn/read-string
      get-data-tiles))