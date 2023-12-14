(ns de.explorama.shared.data-transformer.generator.edn-json
  (:require [de.explorama.shared.data-transformer.generator :as gen]
            [taoensso.timbre :refer [warn]]
            #?(:clj [jsonista.core :as json])))

(deftype EdnJsonGen [state]
  gen/Generator
  (gen/state [_]
    state)
  (gen/finalize [_ data format]
    (case format
      :edn data
      :json #?(:clj (json/write-value-as-string data)
               :cljs (js/JSON.stringify (clj->js data)))))
  (gen/fact [_ name type value]
    {:name name
     :type type
     :value value})
  (gen/context [_ global-id type name]
    {:global-id global-id
     :type type
     :name name})
  (gen/context-ref [_ global-id rel-type rel-name]
    (cond-> {:global-id global-id}
      rel-type
      (assoc :rel-type rel-type)
      rel-name
      (assoc :rel-name rel-name)))
  (gen/location [_ [lat lon]]
    {:lat lat
     :lon lon})
  (gen/date
    [_ type value]
    {:type type
     :value value})
  (gen/text [_ value]
    value)
  (gen/datasource [_ global-id name opts]
    (when opts
      (warn "Datasource opts are currently not implemented"))
    {:global-id global-id
     :name name})
  (gen/datasource [_ global-id name]
    {:global-id global-id
     :name name})
  (gen/item [_ global-id features]
    {:global-id global-id
     :features features})
  (gen/feature [_ global-id facts locations context-refs dates texts]
    (cond-> {:facts facts
             :locations locations
             :context-refs context-refs
             :dates dates
             :texts texts}
      global-id
      (assoc :global-id global-id)))
  (gen/data [_ contexts datasource items]
    {:contexts contexts
     :datasource datasource
     :items items}))

(defn new-instance []
  (EdnJsonGen. (atom {})))