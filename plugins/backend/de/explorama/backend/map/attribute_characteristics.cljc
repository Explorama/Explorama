(ns de.explorama.backend.map.attribute-characteristics
  (:require [clojure.string :as string]
            [de.explorama.backend.frontend-api :refer [broadcast]]
            [de.explorama.backend.map.ac-api :as ac-api]
            [de.explorama.shared.map.ws-api :refer [set-acs]]
            [taoensso.timbre :refer [debug]]))

; contains following keys:
#_[:color :full :geo :context :<attribute>]
(defonce ui-options (atom {}))
(def conj-empty (fnil conj #{}))
(def type-whitelist-numbers #{"integer" "decimal" "float" "double"})
(def type-whitelist #{"boolean"})

(defn- full-acs [acc node-label attribute]
  (if (= "Date" node-label)
    (update acc :full
            conj-empty
            {:name "date"} {:name attribute})
    (update acc :full
            conj-empty
            {:name attribute})))

(defn- geolocated-options [acc node-label attribute]
  (let [update-fn (partial update acc :geo conj-empty)]
    (cond
      (= attribute "origin") (update-fn {:name attribute
                                         :type "string"})
      (= attribute "country") (update-fn {:name attribute
                                          :type "string"})
      (= attribute "location") (update-fn {:name attribute
                                           :type "location"})
      :else acc)))

(defn- context-options [acc attribute]
  (update acc :context conj-empty {:name attribute}))

(defn- attribute-values-options [acc node-label attribute value]
  (if (and (or (and (= node-label "Context")
                    (not= attribute "location"))
               (= node-label "Feature")
               (and (= node-label "Fact")
                    (not (#{"integer" "decimal" "boolean"}
                          value))))
           value)
    (update acc attribute conj-empty value)
    acc))

(defn- color-options [acc node-label attribute value]
  (let [[name
         type] (cond (and (= "Fact" node-label)
                          (type-whitelist-numbers value))
                     [attribute "number"]
                     (and (= "Fact" node-label)
                          (type-whitelist value))
                     [attribute value]
                     (and (= "Fact" node-label)
                          (not (type-whitelist value)))
                     [attribute "string"]
                     :else [attribute "string"])]
    (update acc :color
            conj-empty
            {:display-name (if (= type "number")
                             (str name " (" type ")")
                             name)
             :name name
             :type type})))

(defn- workaround-indicator
  "Adding extra attributes for de.explorama.backend.indicator."
  [all-attributes]
  (conj all-attributes
        ["Context" "indicator-type" "average"]
        ["Context" "indicator-type" "division"]
        ["Context" "indicator-type" "sum"]
        ["Context" "indicator-type" "min"]
        ["Context" "indicator-type" "max"]
        ["Context" "indicator-type" "normalize"]
        ["Context" "indicator-type" "indicator-rank"]
        ["Fact" "indicator" "decimal"]))

(defn- create-ui-options []
  (reduce (fn [acc [node-label attribute value]] ; value => characteristic or in case of Fact the type
            (cond-> acc
              :always (assoc-in [:name-mapping attribute] node-label)
              :always (full-acs node-label attribute)
              (= node-label "Context") (-> (geolocated-options node-label attribute)
                                           (context-options attribute))
              (#{"Context" "Feature" "Fact"} node-label) (attribute-values-options node-label attribute value)
              (not (or (= "Date" node-label)
                       (= "Notes" node-label)
                       (= "Datasource" node-label)
                       (and (= "Fact" node-label)
                            (= "notes" (string/lower-case attribute))))) (color-options node-label attribute value)))
          {}
          (workaround-indicator
           (ac-api/attributes))))

;color
(defn extract-acs-with-hacks []
  (:color @ui-options))

;full
(defn extract-full-acs []
  (:full @ui-options))

;geo
(defn extract-geolocated-context []
  (get @ui-options :geo))

(defn ds-acs []
  (get @ui-options :ds-acs))

(defn populate-ui-options! []
  (debug "creating ui-options based on current acs")
  (reset! ui-options (create-ui-options))
  (debug "broadcast acs")
  (broadcast [set-acs (extract-geolocated-context)])
  (debug "ui-options created."))

(comment
  (populate-ui-options!))