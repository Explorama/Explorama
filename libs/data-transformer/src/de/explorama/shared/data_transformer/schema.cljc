(ns de.explorama.shared.data-transformer.schema
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]))

(def ^:private schema
  [:schema {:registry {"field" [:tuple [:enum :field] string?]
                       "field-default" [:cat [:enum :field] string? :any]
                       "convert" [:tuple {:doc "string: split-by char"}
                                  [:enum :convert]
                                  [:or
                                   fn?
                                   [:or
                                    [:tuple string? string?]
                                    [:tuple string? string? :any]]]]
                       "fields" [:tuple
                                 [:enum :fields]
                                 [:vector string?]]
                       "fields-default" [:tuple
                                         [:enum :fields]
                                         [:vector string?]
                                         :any]
                       "default" [:tuple [:enum :value] :any]
                       "global-id" [:or
                                    "field"
                                    [:cat
                                     [:enum :id-generate]
                                     [:tuple string? [:enum :hash :text]] ;string? -> prefix
                                     [:or "field" "fields" "default"]]]
                       "global-id-context" [:or
                                            "field"
                                            [:cat
                                             [:enum :id-generate]
                                             [:tuple string? [:enum :hash :text]] ;string? -> prefix
                                             [:or "field" [:enum :name]]]]
                       "global-id-rand" [:tuple [:enum :id-rand] [:enum :uuid]]
                       "date-schema" [:cat [:enum :date-schema] string? "field"]
                       "value-multi" [:or
                                      "fields"
                                      "fields-default"
                                      "field"
                                      "field-default"
                                      "convert"
                                      "default"]
                       "value-single" [:or
                                       "field"
                                       "field-default"
                                       "convert"
                                       "default"]
                       "template" [:map
                                   [:suggestions {:optional true} :any]
                                   [:transformations {:optional true}
                                    [:map {:closed true}
                                     [:post :any]
                                     [:pre :any]]]
                                   [:meta-data [:map {:closed true}
                                                [:file-format [:enum :csv :xlsx]]
                                                [:csv {:optional true}
                                                 [:map
                                                  [:separator string?]
                                                  [:quote string?]]]
                                                [:xlsx {:optional true}
                                                 [:map
                                                  [:sheet string?]]]]]
                                   [:mapping [:map {:closed true}
                                              [:datasource [:map {:closed true}
                                                            [:name "value-single"]
                                                            [:global-id [:or
                                                                         "global-id"
                                                                         "default"]]
                                                            [:opts {:optional true} :any]]]
                                              [:items [:vector [:map {:closed true}
                                                                [:global-id [:or
                                                                             "global-id"
                                                                             "global-id-rand"]]
                                                                [:type {:optional true} "value-single"] ;currently not supported
                                                                [:features [:vector [:map {:closed true}
                                                                                     [:global-id {:optional true
                                                                                                  :doc "Currently not supported"}
                                                                                      "global-id"]
                                                                                     [:facts {:optional true}
                                                                                      [:vector
                                                                                       [:map {:closed true}
                                                                                        [:name "value-multi"]
                                                                                        [:type {:doc "Current types are: \"decimal\" \"integer\" \"string\""}
                                                                                         "value-single"]
                                                                                        [:value "value-multi"]]]]
                                                                                     [:locations {:optional true}
                                                                                      [:vector [:map {:closed true}
                                                                                                [:point [:or
                                                                                                         "convert"
                                                                                                         [:tuple [:enum :position] "value-single"]
                                                                                                         [:cat [:enum :lat-lon] "value-single" "value-single"]]]]]]
                                                                                     [:contexts
                                                                                      [:vector [:map {:closed true}
                                                                                                [:name "value-single"]
                                                                                                [:global-id "global-id-context"] ;TODO r1/mapping make global-id optional with a preprocessing step
                                                                                                [:type "value-single"]
                                                                                                [:rel-type {:optional true} "value-single"]
                                                                                                [:rel-name {:optional true} "value-single"]]]]
                                                                                     [:dates
                                                                                      [:vector
                                                                                       [:map {:closed true}
                                                                                        [:value [:or
                                                                                                 "value-single"
                                                                                                 "date-schema"]]
                                                                                        [:type {:doc "Current types are: \"occured-at\" \"start-at\" \"end-at\""}
                                                                                         "value-single"]]]]
                                                                                     [:texts {:optional true}
                                                                                      [:vector "value-multi"]]]]]]]]]]]}}
   "template"])

(defn- schema-validator []
  (m/schema schema))

(def import-schema
  (schema-validator))

(defn validate [data]
  (m/validate import-schema data))

(defn explain [data]
  (-> (m/explain import-schema data)
      (me/humanize)))

(def ^:private decoder (m/decoder import-schema mt/json-transformer))
(defn decode [data]
  (decoder data))
