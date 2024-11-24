(ns de.explorama.shared.data-transformer.generator.operations
  (:require [clojure.set :as set]
            [de.explorama.shared.data-transformer.generator :as gen]
            [taoensso.timbre :refer [warn]]
            #?(:clj [jsonista.core :as json])))

(deftype OperationsGen [state]
  gen/Generator
  (gen/state [_]
    state)
  (gen/finalize [_ data format]
    (case format
      :edn data
      :json #?(:clj (json/write-value-as-string data)
               :cljs (js/JSON.stringify (clj->js data)))))
  (gen/fact [_ name type _]
    [name type])
  (gen/context [_ global-id type _]
    [global-id type])
  (gen/context-ref [_ global-id _ _]
    global-id)
  (gen/location [_ [lat lon]]
    (when (and lat lon)
      true))
  (gen/date
    [_ type _]
    type)
  (gen/text [_ _]
    true)
  (gen/datasource [_ _ name _opts]
    (warn "Datasource opts are currently not implemented")
    name)
  (gen/datasource [_ _ name]
    name)
  (gen/item [_ _ features]
    features)
  (gen/feature [_ _ facts locations context-refs dates texts]
    {:facts (reduce set/union #{} facts)
     :locations (some true? locations)
     :context-refs (reduce set/union #{} context-refs)
     :dates (reduce set/union #{} dates)
     :texts (some true? texts)})
  (gen/data [_ contexts datasource {features :features}]
    (-> (assoc features
               :datasource (some? datasource))
        (dissoc :context-refs)
        (assoc :contexts (->> (select-keys contexts (:context-refs features))
                              vals set)))))

(defn new-instance []
  (OperationsGen. (atom {})))