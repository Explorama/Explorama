(ns de.explorama.backend.expdb.spec
  (:require [malli.core :as m]
            [malli.transform :as mt]))

(defn- schema-validator []
  (m/schema [:map {:closed true}
             [:contexts [:vector [:map {:closed true}
                                  [:name :string]
                                  [:global-id :string]
                                  [:type :string]
                                  [:opts {:optional true} :any]]]]
             [:datasource [:map {:closed true}
                           [:name :string]
                           [:global-id :string]
                           [:opts {:optional true} :any]]]
             [:items [:vector [:map {:closed true}
                               [:global-id :string]
                               ;TODO r1/expdb currently only on feature per item
                               [:features [:vector [:map {:closed true}
                                                    [:global-id {:optional true} :string]
                                                    [:facts {:optional true}
                                                     [:vector
                                                      [:map
                                                       [:name :string]
                                                       [:type [:enum "decimal" "integer" "string"]]
                                                       [:value :any]]]]
                                                    [:locations {:optional true}
                                                     [:vector
                                                      [:map {:closed true}
                                                       [:lat number?]
                                                       [:lon number?]]]]
                                                    [:context-refs
                                                     [:vector
                                                      [:map {:closed true}
                                                       [:global-id :string]
                                                       [:rel-type {:optional true} :string]
                                                       [:rel-name {:optional true} :string]]]]
                                                    [:dates
                                                     [:vector
                                                      [:map {:closed true}
                                                       [:type [:enum "occured-at" "start-at" "end-at"]]
                                                       [:value :string]]]]
                                                    [:texts {:optional true}
                                                     [:vector :string]]]]]]]]]))

(def import-schema
  (schema-validator))

(defn validate [data]
  (m/validate import-schema data))

(defn explain [data]
  (m/explain import-schema data))

(def ^:private decoder (m/decoder import-schema mt/json-transformer))
(defn decode [data]
  (decoder data))
