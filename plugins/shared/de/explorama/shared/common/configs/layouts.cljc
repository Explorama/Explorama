(ns de.explorama.shared.common.configs.layouts
  (:require #?(:clj [taoensso.timbre :refer [error]]
               :cljs [taoensso.timbre :refer-macros [error]])
            [de.explorama.shared.common.configs.color-scheme]
            [clojure.spec.alpha :as spec]))
;; Example
;; (def base-layout-1
;;     {:id (uuid)
;;      :name "Base Layout 1"
;;      :timestamp (System/currentTimeMillis)
;;      :default? true
;;      :color-scheme color-scheme-2
;;      :attributes [td/fact-1]
;;      :attribute-type "integer"
;;      :value-assigned [[51 1000000]
;;                       [20 51]
;;                       [6 20]
;;                       [1 6]
;;                       [0 1]]
;;      :card-scheme "scheme-1"
;;      :field-assignments [["else" "date"]
;;                          ["else" "datasource"]
;;                          ["else" td/category-1]
;;                          ["else" td/fact-1]
;;                          ["notes" "notes"]
;;                          ["else" td/country]
;;                          [td/org td/org]
;;                          ["location" "location"]]})

(spec/def :layout/id (spec/and string?
                               #(seq %)))
(spec/def :layout/name (spec/and string?
                                 #(seq %)))
(spec/def :layout/timestamp number?)
(spec/def :layout/default? boolean?)
(spec/def :layout/color-scheme :color-scheme/desc)
(spec/def :layout/attributes (spec/and vector?
                                       #(and (seq %)
                                             (every? string? %))))
(spec/def :layout/attribute-type #{"integer" "decimal" "number" "string"})
(spec/def :layout/card-scheme #{"scheme-1" "scheme-2" "scheme-3"})
(spec/def :layout/value-assigned (spec/and vector?
                                           #(seq %)))
(spec/def :layout/field-assignments (spec/and vector?
                                              #(seq %)))

(spec/def :layout/desc
  (spec/keys
   :req-un [:layout/id
            :layout/name
            :layout/timestamp
            :layout/color-scheme
            :layout/attributes
            :layout/attribute-type
            :layout/value-assigned
            :layout/card-scheme
            :layout/field-assignments]
   :opt-un [:layout/default?]))

(defonce relevant-layout-keys-cache (atom nil))

(defn- relevant-layout-keys []
  (if-let [layout-keys @relevant-layout-keys-cache]
    layout-keys
    (reset! relevant-layout-keys-cache
            (reduce (fn [acc keys]
                      (apply conj acc (map #(keyword (name %))
                                           keys)))
                    #{}
                    (filter vector? (spec/form :layout/desc))))))

(defn reduce-layout-desc
  "Ensures that only valid layout keys are used"
  [layout-desc]
  (select-keys layout-desc (relevant-layout-keys)))

(defn is-layout-valid?
  "Checks if a layout-desc is valid
   - layout-desc - the layout map
   - explain? - if map is invalid then show more informations why"
  ([layout-desc]
   (is-layout-valid? layout-desc false))
  ([layout-desc explain?]
   (let [valid? (spec/valid? :layout/desc layout-desc)]
     (if (and explain? (not valid?))
       (do (error "layout-desc not conform with spec"
                  {:desc layout-desc
                   :explain (spec/explain-str :layout/desc layout-desc)})
           valid?)
       valid?))))
