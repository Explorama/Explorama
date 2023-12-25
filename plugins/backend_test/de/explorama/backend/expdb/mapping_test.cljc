(ns de.explorama.backend.expdb.mapping-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.backend.expdb.suggestions-test :refer [test-mapping test-data]]
            [de.explorama.shared.data-transformer.mapping :as sut]
            [de.explorama.shared.data-transformer.generator.edn-json :as gen]))

(def test-result
  {:contexts [{:global-id "context2-TFRhbfDGdjiAsheKW",
               :type "context2",
               :name "TFRhbfDGdjiAsheKW"}
              {:global-id "context1-ZnXMHh",
               :type "context1",
               :name "ZnXMHh"}
              {:global-id "context1-bSpYGbjl",
               :type "context1",
               :name "bSpYGbjl"}
              {:global-id "country-country-170",
               :type "country",
               :name "country-170"}
              {:global-id "country-country-124",
               :type "country",
               :name "country-124"}
              {:global-id "country-country-26",
               :type "country",
               :name "country-26"}
              {:global-id "context2-WTmxCZiUEBWhmThCuzOaT",
               :type "context2",
               :name "WTmxCZiUEBWhmThCuzOaT"}
              {:global-id "context1-TNqkyAGU",
               :type "context1",
               :name "TNqkyAGU"}
              {:global-id "context2-QImbiXBnMlKq",
               :type "context2",
               :name "QImbiXBnMlKq"}],
   :datasource {:global-id "source-placeholder", :name "Placeholder"},
   :items [{:global-id "9e98b89c-56fe-4fb4-8338-89d8a16b3c74",
            :features [{:facts [{:name "fact1",
                                 :type "integer",
                                 :value 417517882}
                                {:name "fact2",
                                 :type "decimal",
                                 :value 2.5015747848643652e+38}
                                {:name "fact3",
                                 :type "string",
                                 :value "in enim reprehenderit sunt commodo enim enim tempor"}],
                        :locations [{:lat 1.5, :lon 1.3}],
                        :context-refs [{:global-id "country-country-124"}
                                       {:global-id "context2-WTmxCZiUEBWhmThCuzOaT"}
                                       {:global-id "context1-TNqkyAGU"}],
                        :dates [{:type "occured-at",
                                 :value "2018-09-24"}],
                        :texts ["in enim reprehenderit sunt commodo enim enim tempor commodo Excepteur id ut magna enim ut laboris aute elit, ea ut, commodo Excepteur id ut magna enim ut laboris aute elit, ea ut"]}]}
           {:global-id "e5853083-fbd6-4196-a055-63e441c93a91",
            :features [{:facts [{:name "fact1",
                                 :type "integer",
                                 :value 254097719}
                                {:name "fact2",
                                 :type "decimal",
                                 :value 1.015822346965349e+38}
                                {:name "fact3",
                                 :type "string",
                                 :value "dolore deserunt qui dolore laboris laborum. minim Duis Lorem"}],
                        :locations [{:lat 1.3, :lon 1.5}],
                        :context-refs [{:global-id "country-country-26"}
                                       {:global-id "context2-QImbiXBnMlKq"}
                                       {:global-id "context1-bSpYGbjl"}],
                        :dates [{:type "occured-at",
                                 :value "2013-03-16"}],
                        :texts ["tempor magna exercitation dolore deserunt qui dolore laboris laborum. minim Duis Lorem consectetur Lorem"]}]}
           {:global-id "643ec73f-9b31-439d-9a79-31cd60658cca",
            :features [{:facts [{:name "fact1",
                                 :type "integer",
                                 :value -2118492375}
                                {:name "fact2",
                                 :type "decimal",
                                 :value 2.9041681368069585e+38}
                                {:name "fact3",
                                 :type "string",
                                 :value "labore dolore ullamco laboris nostrud eu"}],
                        :locations [{:lat 1.3, :lon 1.5}],
                        :context-refs [{:global-id "country-country-170"}
                                       {:global-id "context2-TFRhbfDGdjiAsheKW"}
                                       {:global-id "context1-ZnXMHh"}],
                        :dates [{:type "occured-at",
                                 :value "2021-09-30"}],
                        :texts ["labore dolore ullamco laboris nostrud eu eu culpa Lorem in Ut amet, irure ad eu"]}]}]})

(deftest test-end-to-end
  (testing "end-to-end"
    (is (= (sut/mapping (gen/new-instance) test-mapping test-data)
           test-result))))