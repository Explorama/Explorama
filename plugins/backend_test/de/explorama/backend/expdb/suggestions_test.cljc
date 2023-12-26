(ns de.explorama.backend.expdb.suggestions-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.shared.data-transformer.suggestions :as sut]))

(def test-data
  (let [header ["id","pos","fact1","fact2","fact3","country","context1","context2","text","date"]
        rows [["9e98b89c-56fe-4fb4-8338-89d8a16b3c74","1.5,1.3","417517882","2.5015747848643652E38","in enim reprehenderit sunt commodo enim enim tempor","country-124","TNqkyAGU","WTmxCZiUEBWhmThCuzOaT","in enim reprehenderit sunt commodo enim enim tempor commodo Excepteur id ut magna enim ut laboris aute elit, ea ut, commodo Excepteur id ut magna enim ut laboris aute elit, ea ut","2018-09-24"]
              ["e5853083-fbd6-4196-a055-63e441c93a91","1.3,1.5","254097719","1.015822346965349E38","dolore deserunt qui dolore laboris laborum. minim Duis Lorem","country-26","bSpYGbjl","QImbiXBnMlKq","tempor magna exercitation dolore deserunt qui dolore laboris laborum. minim Duis Lorem consectetur Lorem","2013-03-16"]
              ["643ec73f-9b31-439d-9a79-31cd60658cca","1.3,1.5","-2118492375","2.9041681368069585E38","labore dolore ullamco laboris nostrud eu","country-170","ZnXMHh","TFRhbfDGdjiAsheKW","labore dolore ullamco laboris nostrud eu eu culpa Lorem in Ut amet, irure ad eu","2021-09-30"]]]
    (vec (map-indexed #(assoc (zipmap header %2) :row-number %1)
                      rows))))

(def test-desc
  {:meta-data {:file-format :csv
               :csv {:separator ","
                     :quote "\""}}})

(def test-mapping
  {:meta-data {:file-format :csv,
               :csv {:separator ",", :quote "\""}},
   :mapping {:datasource {:name [:value "Placeholder"],
                          :global-id [:value "source-placeholder"]},
             :items [{:global-id [:field "id"],
                      :features [{:facts [{:value [:field "fact1"],
                                           :name [:value "fact1"],
                                           :type [:value "integer"]}
                                          {:value [:field "fact2"],
                                           :name [:value "fact2"],
                                           :type [:value "decimal"]}
                                          {:name [:value "fact3"],
                                           :type [:value "string"]
                                           :value [:field "fact3"]}],
                                  :locations [{:point [:position [:field "pos"]]}],
                                  :contexts [{:name [:field "country"],
                                              :global-id [:id-generate ["country" :text] :name],
                                              :type [:value "country"]}
                                             {:name [:field "context2"],
                                              :global-id [:id-generate ["context2" :text] :name],
                                              :type [:value "context2"]}
                                             {:name [:field "context1"],
                                              :global-id [:id-generate ["context1" :text] :name],
                                              :type [:value "context1"]}],
                                  :dates [{:value [:date-schema
                                                   "YYYY-MM-dd"
                                                   [:field "date"]],
                                           :type [:value "occured-at"]}],
                                  :texts [[:field "text" ""]]}]}]}})

(deftest test-end-to-end
  (testing "end-to-end"
    (is (= (sut/create test-desc test-data)
           test-mapping))))

(deftest test-numbers
  (testing "Decimals happy path"
    (is (= (@#'sut/check-numbers nil "-1.5")
           (@#'sut/check-numbers nil "-1,5")
           (@#'sut/check-numbers nil "1.5")
           (@#'sut/check-numbers nil "1,5")
           (@#'sut/check-numbers nil "1.5e1")
           (@#'sut/check-numbers nil "-1.5e1")
           (@#'sut/check-numbers nil "-1.5e-1")
           (@#'sut/check-numbers nil "1,5e1")
           (@#'sut/check-numbers nil "-1,5e1")
           (@#'sut/check-numbers nil "-1,5e-1")
           (@#'sut/check-numbers nil "12,310,101.5")
           (@#'sut/check-numbers nil "1.238.711,5")
           (@#'sut/check-numbers nil "-12,310,101.5")
           (@#'sut/check-numbers nil "-1.238.711,5")
           {:type :decimal})))
  (testing "integer happy path"
    (is (= (@#'sut/check-numbers nil "-1.000.123")
           (@#'sut/check-numbers nil "-44,123,421")
           (@#'sut/check-numbers nil "32.312.213")
           (@#'sut/check-numbers nil "-32,312,213")
           (@#'sut/check-numbers nil "32312213")
           (@#'sut/check-numbers nil "-32312213")
           {:type :integer}))))

(deftest test-dates
  (testing "Dates happy path"
    (is (= (@#'sut/check-dates nil "2018-09-24")
           {:type :date
            :date-schema "YYYY-MM-dd"}))
    (is (= (@#'sut/check-dates nil "2018/09/24")
           {:type :date
            :date-schema "YYYY/MM/dd"}))
    (is (= (@#'sut/check-dates nil "2018.09.24")
           {:type :date
            :date-schema "YYYY.MM.dd"}))
    (is (= (@#'sut/check-dates nil "24-09-2018")
           {:type :date
            :date-schema "dd-MM-YYYY"}))
    (is (= (@#'sut/check-dates nil "24/09/2018")
           {:type :date
            :date-schema "dd/MM/YYYY"}))
    (is (= (@#'sut/check-dates nil "24.09.2018")
           {:type :date
            :date-schema "dd.MM.YYYY"}))
    (is (= (@#'sut/check-dates nil "24 SEP. 2018")
           {:type :date
            :date-schema "dd MMM. YYYY"}))
    (is (= (@#'sut/check-dates nil "SEP. 24, 2018")
           {:type :date
            :date-schema "MMM. dd, YYYY"}))
    (is (= (@#'sut/check-dates nil "2018")
           {:type :date
            :date-schema "YYYY"}))
    (is (= (@#'sut/check-dates nil "09.2018")
           {:type :date
            :date-schema "MM.YYYY"}))
    (is (= (@#'sut/check-dates nil "2018-09")
           {:type :date
            :date-schema "YYYY-MM"}))))
