(ns de.explorama.profiling-tool.verticals.expdb-acs
  (:require [de.explorama.backend.expdb.middleware.ac :as mwac]
            [de.explorama.profiling-tool.benchmark :refer [bench bench-report
                                                           benchmark-all
                                                           report->save]]
            [de.explorama.profiling-tool.data.search :refer [country-attr
                                                             data-a-formdata
                                                             empty-formdata fact1-formdata location-formdata notes-formdata organisation-attr]]
            [de.explorama.shared.common.data.attributes :as attrs]
            [taoensso.tufte :as tufte]))

(defn- attribute-types []
  (tufte/p
   :middleware-attribute-types
   (mwac/attribute-types {:blacklist #{}})))

(defn benchmark-attribute-types []
  (let [bench-name "attribute-types"
        num-of-attributes (count (attribute-types))]
    (assoc (bench bench-name [] attribute-types {:create-report? true})
           :name bench-name
           :service "ac-service"
           (str bench-name " -> Result count") num-of-attributes)))

(defn- datasource-values [bucket]
  (tufte/p
   :datasources
   (mwac/attribute-values {:formdata []
                           :bucket bucket
                           :attributes [[attrs/datasource-attr
                                         attrs/datasource-node]]})))

(defn benchmark-datasource-values []
  (let [bench-name-default "default-datasources"
        bench-name-temp "temp-datasources"
        num-of-default-sources (count (get (datasource-values :default)
                                           [attrs/datasource-attr
                                            attrs/datasource-node]))
        num-of-temp-sources (count (get (datasource-values :temp)
                                        [attrs/datasource-attr
                                         attrs/datasource-node]))]
    (bench bench-name-default [] (partial datasource-values :default))
    (bench bench-name-temp [] (partial datasource-values :temp))
    (assoc (bench-report)
           :name "datasources"
           :service "ac-service"
           (str bench-name-default " -> Result count") num-of-default-sources
           (str bench-name-temp " -> Result count") num-of-temp-sources)))

(defn- search-values [formdata attribute search-term]
  (mwac/attribute-values
   {:formdata formdata
    :search-term search-term
    :attributes [attribute]}))

(defn benchmark-serach-values []
  (let [bench-empty-form-no-term-country "No Formdata, no search-term, country"
        bench-empty-form-ben-organisation "No Formdata, ben ,organisation"
        bench-data-a-ben-organisation "Data-A, country-5, organisation"
        bench-notes-ben-organisation "Notes, country-5, organisation"
        bench-location-no-term-organisation "Location, no search-term, organisation"
        bench-fact1-no-term-organisation "fact1, no search-term, organisation"

        number-no-term-country (count (get (search-values empty-formdata country-attr "")
                                           country-attr))
        number-ben-organisation (count (get (search-values empty-formdata organisation-attr "country-5")
                                            organisation-attr))
        number-data-a-ben-organisation (count (get (search-values data-a-formdata organisation-attr "country-5")
                                                  organisation-attr))
        number-notes-ben-organisation (count (get (search-values notes-formdata organisation-attr "country-5")
                                                  organisation-attr))
        number-location-no-term-organisation (count (get (search-values location-formdata organisation-attr "")
                                                         organisation-attr))
        number-fact1-no-term-organisation (count (get (search-values fact1-formdata organisation-attr "")
                                                           organisation-attr))]
    (bench bench-empty-form-no-term-country
           []
           (partial search-values empty-formdata country-attr ""))
    (bench bench-empty-form-ben-organisation
           []
           (partial search-values empty-formdata organisation-attr "country-5"))
    (bench bench-data-a-ben-organisation
           []
           (partial search-values data-a-formdata organisation-attr "country-5"))
    (bench bench-notes-ben-organisation
           []
           (partial search-values notes-formdata organisation-attr "country-5"))
    (bench bench-location-no-term-organisation
           []
           (partial search-values location-formdata organisation-attr ""))
    (bench bench-fact1-no-term-organisation
           []
           (partial search-values fact1-formdata organisation-attr ""))
    (assoc (bench-report)
           :name "search-values"
           :service "ac-service"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-no-term-country " -> Result Count") number-no-term-country
           (str bench-empty-form-ben-organisation " -> Result Count") number-ben-organisation
           (str bench-data-a-ben-organisation " -> Result Count") number-data-a-ben-organisation
           (str bench-notes-ben-organisation " -> Result Count") number-notes-ben-organisation
           (str bench-location-no-term-organisation " -> Result Count") number-location-no-term-organisation
           (str bench-fact1-no-term-organisation " -> Result Count") number-fact1-no-term-organisation)))

(defn- search-options
  "Same as search-values but without a search-term"
  [attributes formdata]
  (mwac/attribute-values {:formdata formdata
                          :attributes attributes}))

(defn benchmark-search-options []
  (let [bench-empty-form-country "No Formdata,country search-options"
        bench-empty-form-organisation "No Formdata,organisation search-options"
        bench-data-a-organisation "Data-A, organisation search-options"
        bench-notes-organisation "Notes, organisation search-options"
        bench-location-organisation "Location, organisation search-options"
        bench-fact1-organisation "fact1, organisation search-options"

        number-country-options (count (get (search-options [country-attr] empty-formdata)
                                           country-attr))
        number-organisation-options (count (get (search-options [organisation-attr] empty-formdata)
                                                organisation-attr))
        number-data-a-organisation-options (count (get (search-options [organisation-attr] data-a-formdata)
                                                      organisation-attr))
        number-notes-organisation-options (count (get (search-options [organisation-attr] notes-formdata)
                                                      organisation-attr))
        number-location-organisation-options (count (get (search-options [organisation-attr] location-formdata)
                                                         organisation-attr))
        number-fact1-organisation-options (count (get (search-options [organisation-attr] fact1-formdata)
                                                           organisation-attr))]
    (bench bench-empty-form-country
           []
           (partial search-options [country-attr] empty-formdata))
    (bench bench-empty-form-organisation
           []
           (partial search-options [organisation-attr] empty-formdata))
    (bench bench-data-a-organisation
           []
           (partial search-options [organisation-attr] data-a-formdata))
    (bench bench-notes-organisation
           []
           (partial search-options [organisation-attr] notes-formdata))
    (bench bench-location-organisation
           []
           (partial search-options [organisation-attr] location-formdata))
    (bench bench-fact1-organisation
           []
           (partial search-options [organisation-attr] fact1-formdata))
    (assoc (bench-report)
           :name "search-options"
           :service "ac-service"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-country " -> Result Count") number-country-options
           (str bench-empty-form-organisation " -> Result Count") number-organisation-options
           (str bench-data-a-organisation " -> Result Count") number-data-a-organisation-options
           (str bench-notes-organisation " -> Result Count") number-notes-organisation-options
           (str bench-location-organisation " -> Result Count") number-location-organisation-options
           (str bench-fact1-organisation " -> Result Count") number-fact1-organisation-options)))

(defn- possible-attributes [formdata]
  (mwac/attribute-values {:formdata formdata
                          :blacklist #{}}))

(defn benchmark-possible-attributes []
  (let [bench-empty-form-possible-attributes "No Formdata, possible attributes"
        bench-data-a-possible-attributes "Data-A, possible attributes"
        bench-notes-possible-attributes "Notes, possible attributes"
        bench-location-possible-attributes "Location, possible attributes"
        bench-fact1-possible-attributes "fact1, possible attributes"

        number-empty-possible-attributes (count (possible-attributes empty-formdata))
        number-data-a-possible-attributes (count (possible-attributes data-a-formdata))
        number-notes-possible-attributes (count (possible-attributes notes-formdata))
        number-location-possible-attributes (count (possible-attributes location-formdata))
        number-fact1-possible-attributes (count (possible-attributes fact1-formdata))]

    (bench bench-empty-form-possible-attributes
           []
           (partial possible-attributes empty-formdata))
    (bench bench-data-a-possible-attributes
           []
           (partial possible-attributes data-a-formdata))
    (bench bench-notes-possible-attributes
           []
           (partial possible-attributes notes-formdata))
    (bench bench-location-possible-attributes
           []
           (partial possible-attributes location-formdata))
    (bench bench-fact1-possible-attributes
           []
           (partial possible-attributes fact1-formdata))
    (assoc (bench-report)
           :name "possible attributes"
           :service "ac-service"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-empty-form-possible-attributes " -> Result Count") number-empty-possible-attributes
           (str bench-data-a-possible-attributes " -> Result Count") number-data-a-possible-attributes
           (str bench-notes-possible-attributes " -> Result Count") number-notes-possible-attributes
           (str bench-location-possible-attributes " -> Result Count") number-location-possible-attributes
           (str bench-fact1-possible-attributes " -> Result Count") number-fact1-possible-attributes)))

(defn- data-tiles [formdata]
  (mwac/data-tiles-ref-api {:formdata (pr-str formdata)}))

(defn benchmark-data-tiles []
  (let [bench-data-a-data-tiles "Data-A, data-tiles"
        bench-notes-data-tiles "Notes, data-tiles"
        bench-location-data-tiles "Location, data-tiles"
        bench-fact1-data-tiles "fact1, data-tiles"

        number-data-a-data-tiles (count (data-tiles data-a-formdata))
        number-notes-data-tiles (count (data-tiles notes-formdata))
        number-location-data-tiles (count (data-tiles location-formdata))
        number-fact1-data-tiles (count (data-tiles fact1-formdata))]

    (bench bench-data-a-data-tiles
           []
           (partial data-tiles data-a-formdata))
    (bench bench-notes-data-tiles
           []
           (partial data-tiles notes-formdata))
    (bench bench-location-data-tiles
           []
           (partial data-tiles location-formdata))
    (bench bench-fact1-data-tiles
           []
           (partial data-tiles fact1-formdata))
    (assoc (bench-report)
           :name "possible attributes"
           :service "ac-service"
           :no-formdata []
           :data-a-formdata data-a-formdata
           :notes-formdata notes-formdata
           :location-formdata location-formdata
           :fact1-formdata fact1-formdata
           (str bench-data-a-data-tiles " -> Result Count") number-data-a-data-tiles
           (str bench-notes-data-tiles " -> Result Count") number-notes-data-tiles
           (str bench-location-data-tiles " -> Result Count") number-location-data-tiles
           (str bench-fact1-data-tiles " -> Result Count") number-fact1-data-tiles)))

(defn- datasource->attributes
  "Similar to possible-attributes but uses allowed-types and returns all attributes-values for one datasource."
  [datasource-attr datasource-val]
  (tufte/p
   (keyword (str datasource-val "-datasource->attributes"))
   (mwac/attributes {:allowed-types #{"Feature" "Date" "Context" "Datasource" "Fact"}
                     :formdata [[datasource-attr {:values [datasource-val] :timestamp 1}]]})))

(defn- datasource-acs []
  (let [datasources (get (datasource-values nil) [attrs/datasource-attr
                                                  attrs/datasource-node])
        datasources-acs (reduce (fn [acc datasource]
                                  (assoc acc
                                         datasource
                                         (datasource->attributes [attrs/datasource-attr attrs/datasource-node]
                                                                 datasource)))
                                {}
                                datasources)]
    datasources-acs))

(defn benchmark-datasource-acs []
  (let [benchmark-name "all datasources acs"
        all-datasources (get (datasource-values nil) [attrs/datasource-attr
                                                      attrs/datasource-node])
        number-of-acs-per-datasource (reduce (fn [acc [d acs]]
                                               (assoc acc d (count acs)))
                                             {}
                                             (datasource-acs))]
    (assoc (bench benchmark-name [] datasource-acs {:create-report? true})
           :name "datasources acs"
           :service "ac-service"
           :datasources all-datasources
           (str benchmark-name " -> Result Count") number-of-acs-per-datasource)))

(defn vertical-benchmark-all
  ([]
   (vertical-benchmark-all false))
  ([single-reports?]
   (if single-reports?
     (do (report->save benchmark-attribute-types)
         (report->save benchmark-datasource-values)
         (report->save benchmark-serach-values)
         (report->save benchmark-search-options)
         (report->save benchmark-possible-attributes)
         (report->save benchmark-data-tiles)
         (report->save benchmark-datasource-acs))
     (report->save
      (benchmark-all benchmark-attribute-types
                     benchmark-datasource-values
                     benchmark-serach-values
                     benchmark-search-options
                     benchmark-possible-attributes
                     benchmark-data-tiles
                     benchmark-datasource-acs)))))
