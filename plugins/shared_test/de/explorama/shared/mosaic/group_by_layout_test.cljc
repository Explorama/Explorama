(ns de.explorama.shared.mosaic.group-by-layout-test
  (:require [clojure.test :as t]
            [de.explorama.shared.common.test-data :as td]
            [de.explorama.shared.mosaic.group-by-layout :as gbl]
            [taoensso.timbre :refer [error]])
  #?(:clj (:import (java.lang String)
                   (java.util Locale))))

#?(:cljs (defn localized-number [num lang]
           (let [lang (if (keyword? lang)
                        (name lang)
                        lang)]
             (if (and lang num)
               (try (.toLocaleString num lang)
                    (catch :default e
                      (error "failed to create localstring:" num "; lang" lang "; Exception:" e)
                      (str num)))
               (str num)))))

(t/deftest transform-single-layout-test
  (t/testing "transforming a single layout test"
    (t/is (= (gbl/transform-single-layout {:attribute-type "number",
                                           :attributes [td/fact-5],
                                           :card-scheme "scheme-3",
                                           :color-scheme {:color-scale-numbers 5,
                                                          :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                                                          :id "colorscale3",
                                                          :name "Scale-2-5"},
                                           :field-assignments [["else" "datasource"] ["else" "date"]],
                                           :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                                           :name "Layoutname",
                                           :timestamp 1660553228260,
                                           :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]} 0)
             [0 {:colors {"#babab9" [[0 [0 2]]],
                          "#5c5b5b" [[1 [2 3]]],
                          "#50678a" [[2 [3 4]]],
                          "#fb8d02" [[3 [4 5]]],
                          "#e33b3b" [[4 [5 100]]]},
                 :name "Layoutname",
                 :attribute-type "number",
                 :attributes [td/fact-5]}]))
    (t/is (= (gbl/transform-single-layout {:attribute-type "number",
                                           :attributes [td/fact-5],
                                           :card-scheme "scheme-3",
                                           :color-scheme {:color-scale-numbers 5,
                                                          :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                                                          :id "colorscale3",
                                                          :name "Scale-2-5"},
                                           :field-assignments [["else" "datasource"] ["else" "date"]],
                                           :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                                           :name "Layoutname",
                                           :timestamp 1660553228260,
                                           :value-assigned [[] [2 3] [3 4] [4 5] [5 100]]} 0)
             [0 {:colors {"#5c5b5b" [[0 [2 3]]],
                          "#50678a" [[1 [3 4]]],
                          "#fb8d02" [[2 [4 5]]],
                          "#e33b3b" [[3 [5 100]]]},
                 :name "Layoutname",
                 :attribute-type "number",
                 :attributes [td/fact-5]}]))
    (t/is (= (gbl/transform-single-layout {:attribute-type "number",
                                           :attributes [td/fact-5],
                                           :card-scheme "scheme-3",
                                           :color-scheme {:color-scale-numbers 5,
                                                          :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#babab9"},
                                                          :id "colorscale3",
                                                          :name "Scale-2-5"},
                                           :field-assignments [["else" "datasource"] ["else" "date"]],
                                           :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                                           :name "Layoutname",
                                           :timestamp 1660553228260,
                                           :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]} 0)
             [0 {:colors {"#babab9" [[0 [0 2]] [4 [5 100]]],
                          "#5c5b5b" [[1 [2 3]]],
                          "#50678a" [[2 [3 4]]],
                          "#fb8d02" [[3 [4 5]]]},
                 :name "Layoutname",
                 :attribute-type "number",
                 :attributes [td/fact-5]}]))
    (t/is (= (gbl/transform-single-layout {:attribute-type "number",
                                           :attributes [td/fact-5],
                                           :card-scheme "scheme-3",
                                           :color-scheme {:color-scale-numbers 5,
                                                          :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                                                          :id "colorscale3",
                                                          :name "Scale-2-5"},
                                           :field-assignments [["else" "datasource"] ["else" "date"]],
                                           :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                                           :name "Layoutname",
                                           :timestamp 1660553228260,
                                           :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]} 0)
             [0 {:colors {"#babab9" [[0 [0 2]]],
                          "#5c5b5b" [[1 [2 3]]],
                          "#50678a" [[2 [3 4]]],
                          "#fb8d02" [[3 [4 5]]],
                          "#e33b3b" [[4 [5 100]]]},
                 :name "Layoutname",
                 :attribute-type "number",
                 :attributes [td/fact-5]}]))
    (t/is (= (gbl/transform-single-layout {:attribute-type "string",
                                           :attributes [td/country],
                                           :card-scheme "scheme-2",
                                           :color-scheme {:colors {:0 "#0093dd", :1 "#28166f", :2 "#801d77", :3 "#dd137b"}},
                                           :field-assignments [["else" "date"] [td/org td/org] ["notes" "notes"]
                                                               ["else" td/country]],
                                           :id "80135a98-2d53-4c0e-a142-72950f521120",
                                           :name "simple2",
                                           :timestamp 1657029352782,
                                           :value-assigned [[td/country-l] [td/country-m] [td/country-n] [td/country-o]]} 1)
             [1 {:colors {"#0093dd" [[0 [td/country-l]]],
                          "#28166f" [[1 [td/country-m]]],
                          "#801d77" [[2 [td/country-n]]],
                          "#dd137b" [[3 [td/country-o]]]},
                 :name "simple2",
                 :attribute-type "string",
                 :attributes [td/country]}]))
    (t/is (= (gbl/transform-single-layout {:attribute-type "string",
                                           :attributes [td/country],
                                           :card-scheme "scheme-2",
                                           :color-scheme {:colors {:0 "#0093dd", :1 "#28166f", :2 "#801d77", :3 "#0093dd"}},
                                           :field-assignments [["else" "date"] [td/org td/org] ["notes" "notes"]
                                                               ["else" td/country]],
                                           :id "80135a98-2d53-4c0e-a142-72950f521120",
                                           :name "simple2",
                                           :timestamp 1657029352782,
                                           :value-assigned [[td/country-l] [td/country-m] [td/country-n] [td/country-o]]} 1)
             [1 {:colors {"#0093dd" [[0 [td/country-l]] [3 [td/country-o]]],
                          "#28166f" [[1 [td/country-m]]],
                          "#801d77" [[2 [td/country-n]]]},
                 :name "simple2",
                 :attribute-type "string",
                 :attributes [td/country]}]))
    (t/is (= (gbl/transform-single-layout
              {:attribute-type "number",
               :attributes [td/fact-1],
               :card-scheme "scheme-1"
               :color-scheme {:colors {:10 "#276585", :0 "#babab9", :4 "#e33b3b", :7 "#83bc32", :1 "#5c5b5b", :8 "#24d432", :9 "#c22882", :2 "#50678a", :5 "#8ad04a", :3 "#fb8d02", :6 "#661f5c"}},
               :field-assignments [["else" "date"] ["else" "datasource"] ["else" td/category-1] ["else" td/fact-1] ["notes" "notes"] ["else" td/country] [td/org td/org] ["location" "location"]],
               :value-assigned [[0 1] [1 6] [6 20] [20 51] [51 1000000] [101 200] [201 300] [301 400] [401 500] [501 1000] [1001 10000]], :temporary? true, :name "Base Layout 1 (temp)",
               :id "9c23a7f0-b487-480a-bd55-d8bd6ef087d9",
               :timestamp 1627662367930} 0)
             [0 {:colors {"#babab9" [[0 [0 1]]],
                          "#5c5b5b" [[1 [1 6]]],
                          "#50678a" [[2 [6 20]]],
                          "#fb8d02" [[3 [20 51]]],
                          "#e33b3b" [[4 [51 1000000]]],
                          "#8ad04a" [[5 [101 200]]],
                          "#661f5c" [[6 [201 300]]],
                          "#83bc32" [[7 [301 400]]],
                          "#24d432" [[8 [401 500]]],
                          "#c22882" [[9 [501 1000]]],
                          "#276585" [[10 [1001 10000]]]}
                 :name "Base Layout 1 (temp)",
                 :attribute-type "number",
                 :attributes [td/fact-1]}]))))

(t/deftest build-layout-lookup-table-test
  (t/testing "build layout lookup table test"
    (t/is (=
           (gbl/build-layout-lookup-table
            [{:attribute-type "number",
              :attributes [td/fact-5],
              :card-scheme "scheme-3",
              :color-scheme {:color-scale-numbers 5,
                             :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                             :id "colorscale3",
                             :name "Scale-2-5"},
              :field-assignments [["else" "datasource"] ["else" "date"]],
              :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
              :name "Layoutname",
              :timestamp 1660553228260,
              :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]}
             {:attribute-type "number",
              :attributes [td/fact-5],
              :card-scheme "scheme-3",
              :color-scheme {:color-scale-numbers 5,
                             :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#babab9"},
                             :id "colorscale3",
                             :name "Scale-2-5"},
              :field-assignments [["else" "datasource"] ["else" "date"]],
              :id "7b726262-7a9a-4d2c-a80b-6449aad6ceab",
              :name "Layoutname",
              :timestamp 1660553228260,
              :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]}
             {:attribute-type "string",
              :attributes [td/country],
              :card-scheme "scheme-2",
              :color-scheme {:colors {:0 "#0093dd", :1 "#28166f", :2 "#801d77", :3 "#dd137b"}},
              :field-assignments [["else" "date"] [td/org td/org] ["notes" "notes"]
                                  ["else" td/country]],
              :id "80135a98-2d53-4c0e-a142-72950f521120",
              :name "simple2",
              :timestamp 1657029352782,
              :value-assigned [[td/country-l] [td/country-m] [td/country-n] [td/country-o]]}
             {:attribute-type "string",
              :attributes [td/country],
              :card-scheme "scheme-2",
              :color-scheme {:colors {:0 "#0093dd", :1 "#28166f", :2 "#801d77", :3 "#0093dd"}},
              :field-assignments [["else" "date"] [td/org td/org] ["notes" "notes"]
                                  ["else" td/country]],
              :id "80135a98-2d53-4c0e-a142-72950f52112a",
              :name "simple2",
              :timestamp 1657029352782,
              :value-assigned [[td/country-l] [td/country-m] [td/country-n] [td/country-o]]}])
           {"7b726262-7a9a-4d2c-a80b-6449aad6ceaa"
            [0
             {:attribute-type "number",
              :attributes [td/fact-5],
              :colors {"#50678a" [[2 [3 4]]],
                       "#5c5b5b" [[1 [2 3]]],
                       "#babab9" [[0 [0 2]]],
                       "#e33b3b" [[4 [5 100]]],
                       "#fb8d02" [[3 [4 5]]]},
              :name "Layoutname"}],
            "7b726262-7a9a-4d2c-a80b-6449aad6ceab"
            [1
             {:attribute-type "number",
              :attributes [td/fact-5],
              :colors {"#50678a" [[2 [3 4]]], "#5c5b5b" [[1 [2 3]]], "#babab9" [[0 [0 2]] [4 [5 100]]], "#fb8d02" [[3 [4 5]]]},
              :name "Layoutname"}],
            "80135a98-2d53-4c0e-a142-72950f521120"
            [2
             {:attribute-type "string",
              :attributes [td/country],
              :colors {"#0093dd" [[0 [td/country-l]]],
                       "#28166f" [[1 [td/country-m]]],
                       "#801d77" [[2 [td/country-n]]],
                       "#dd137b" [[3 [td/country-o]]]},
              :name "simple2"}],
            "80135a98-2d53-4c0e-a142-72950f52112a"
            [3
             {:attribute-type "string",
              :attributes [td/country],
              :colors {"#0093dd" [[0 [td/country-l]] [3 [td/country-o]]], "#28166f" [[1 [td/country-m]]], "#801d77" [[2 [td/country-n]]]},
              :name "simple2"}]}))))

(t/deftest get-layout-and-color-idx-test
  (t/testing "build layout lookup table test"
    (let [lu
          (gbl/build-layout-lookup-table
           [{:attribute-type "number",
             :attributes [td/fact-5],
             :card-scheme "scheme-3",
             :color-scheme {:color-scale-numbers 5,
                            :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                            :id "colorscale3",
                            :name "Scale-2-5"},
             :field-assignments [["else" "datasource"] ["else" "date"]],
             :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
             :name "Layoutname",
             :timestamp 1660553228260,
             :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]}
            {:attribute-type "number",
             :attributes [td/fact-5],
             :card-scheme "scheme-3",
             :color-scheme {:color-scale-numbers 5,
                            :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#babab9"},
                            :id "colorscale3",
                            :name "Scale-2-5"},
             :field-assignments [["else" "datasource"] ["else" "date"]],
             :id "7b726262-7a9a-4d2c-a80b-6449aad6ceab",
             :name "Layoutname",
             :timestamp 1660553228260,
             :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]}])]
      (t/is (= (gbl/get-layout-and-color-idx lu "7b726262-7a9a-4d2c-a80b-6449aad6ceaa" "#babab9")
               [0 0]))
      (t/is (= (gbl/get-layout-and-color-idx lu "7b726262-7a9a-4d2c-a80b-6449aad6ceab" "#babab9")
               [1 0])))))

(t/deftest get-group-text-test
  (t/testing "get-group-text test"
    (let [lu (gbl/build-layout-lookup-table
              [{:attribute-type "number",
                :attributes [td/fact-5],
                :card-scheme "scheme-3",
                :color-scheme {:color-scale-numbers 5,
                               :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                               :id "colorscale3",
                               :name "Scale-2-5"},
                :field-assignments [["else" "datasource"] ["else" "date"]],
                :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                :name "Layoutname",
                :timestamp 1660553228260,
                :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 1000000]]}
               {:attribute-type "number",
                :attributes [td/fact-5],
                :card-scheme "scheme-3",
                :color-scheme {:color-scale-numbers 5,
                               :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#babab9"},
                               :id "colorscale3",
                               :name "Scale-2-5"},
                :field-assignments [["else" "datasource"] ["else" "date"]],
                :id "7b726262-7a9a-4d2c-a80b-6449aad6ceab",
                :name "Layoutname",
                :timestamp 1660553228260,
                :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 1000000]]}
               {:attribute-type "string",
                :attributes [td/country],
                :card-scheme "scheme-2",
                :color-scheme {:colors {:0 "#0093dd", :1 "#28166f", :2 "#801d77", :3 "#dd137b"}},
                :field-assignments [["else" "date"] [td/org td/org] ["notes" "notes"]
                                    ["else" td/country]],
                :id "80135a98-2d53-4c0e-a142-72950f521120",
                :name "simple2",
                :timestamp 1657029352782,
                :value-assigned [[td/country-l] [td/country-m] [td/country-n] [td/country-o]]}
               {:attribute-type "string",
                :attributes [td/country],
                :card-scheme "scheme-2",
                :color-scheme {:colors {:0 "#0093dd", :1 "#28166f", :2 "#801d77", :3 "#0093dd"}},
                :field-assignments [["else" "date"] [td/org td/org] ["notes" "notes"]
                                    ["else" td/country]],
                :id "80135a98-2d53-4c0e-a142-72950f52112a",
                :name "simple2",
                :timestamp 1657029352782,
                :value-assigned [[td/country-l] [td/country-m] [td/country-n] [td/country-o]]}])

          lu1 (gbl/build-layout-lookup-table
               [{:attribute-type "number",
                 :attributes [td/fact-5],
                 :card-scheme "scheme-3",
                 :color-scheme {:color-scale-numbers 5,
                                :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                                :id "colorscale3",
                                :name "Scale-2-5"},
                 :field-assignments [["else" "datasource"] ["else" "date"]],
                 :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                 :name "Layoutname",
                 :timestamp 1660553228260,
                 :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]}])
          lu2 (gbl/build-layout-lookup-table
               [{:attribute-type "number",
                 :attributes [td/fact-5 "whatever"],
                 :card-scheme "scheme-3",
                 :color-scheme {:color-scale-numbers 5,
                                :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                                :id "colorscale3",
                                :name "Scale-2-5"},
                 :field-assignments [["else" "datasource"] ["else" "date"]],
                 :id "7b726262-7a9a-4d2c-a80b-6449aad6ceaa",
                 :name "Layoutname",
                 :timestamp 1660553228260,
                 :value-assigned [[0 2] [2 3] [3 4] [4 5] [5 100]]}])
          lu3 (gbl/build-layout-lookup-table
               [{:attribute-type "number",
                 :attributes [td/fact-1],
                 :card-scheme "scheme-1"
                 :color-scheme {:colors {:10 "#276585", :0 "#babab9", :4 "#e33b3b", :7 "#83bc32", :1 "#5c5b5b", :8 "#24d432", :9 "#c22882", :2 "#50678a", :5 "#8ad04a", :3 "#fb8d02", :6 "#661f5c"}},
                 :field-assignments [["else" "date"] ["else" "datasource"] ["else" td/category-1] ["else" td/fact-1] ["notes" "notes"] ["else" td/country] [td/org td/org] ["location" "location"]],
                 :value-assigned [[0 1] [1 6] [6 20] [20 51] [51 1000000] [101 200] [201 300] [301 400] [401 500] [501 1000] [1001 10000]], :temporary? true, :name "Base Layout 1 (temp)",
                 :id "9c23a7f0-b487-480a-bd55-d8bd6ef087d9",
                 :timestamp 1627662367930}])]
      (t/is (= (gbl/get-group-text lu "7b726262-7a9a-4d2c-a80b-6449aad6ceab" "#babab9" #(get %2 %1 %1) {} #?(:cljs (fn [n] (localized-number n :en-GB))
                                                                                                             :clj (fn [n] (String/format
                                                                                                                           (Locale. "en-GB") "%,d"
                                                                                                                           (to-array [n])))))
               "0 - 2, 5 - 1,000,000 (fact-5)"))
      (t/is (= (gbl/get-group-text lu "7b726262-7a9a-4d2c-a80b-6449aad6ceaa" "#babab9" #(get %2 %1 %1) {} identity)
               "0 - 2 (fact-5)"))
      (t/is (= (gbl/get-group-text lu1 "7b726262-7a9a-4d2c-a80b-6449aad6ceaa" "#babab9" #(get %2 %1 %1) {} identity)
               "0 - 2 (fact-5)"))
      (t/is (= (gbl/get-group-text lu "80135a98-2d53-4c0e-a142-72950f521120" "#0093dd" #(get %2 %1 %1) {} identity)
               "Country L (country)"))
      (t/is (= (gbl/get-group-text lu "80135a98-2d53-4c0e-a142-72950f521120" "#0093dd" #(get %2 %1 %1) {td/country "kingdom"} identity)
               "Country L (kingdom)"))
      (t/is (= (gbl/get-group-text lu "80135a98-2d53-4c0e-a142-72950f52112a" "#0093dd" #(get %2 %1 %1) {} identity)
               "Country L, Country O (country)"))
      (t/is (= (gbl/get-group-text lu1 "7b726262-7a9a-4d2c-a80b-6449aad6ceaa" "#babab9" #(get %2 %1 %1) {} identity)
               "0 - 2 (fact-5)"))
      (t/is (= (gbl/get-group-text lu2 "7b726262-7a9a-4d2c-a80b-6449aad6ceaa" "#babab9" #(get %2 %1 %1) {} identity)
               "0 - 2 (fact-5, whatever)"))
      (t/is (= (gbl/get-group-text lu3 "9c23a7f0-b487-480a-bd55-d8bd6ef087d9" "#babab9" #(get %2 %1 %1) {} identity)
               "0 - 1 (fact-1)")))))