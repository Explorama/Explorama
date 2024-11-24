(ns de.explorama.shared.data-format.operations-mosaic-test
  (:require [de.explorama.shared.data-format.operations :as of]
            [de.explorama.shared.common.test-data :as td]
            [taoensso.tufte :as tufte]
            #?(:clj  [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;   Apply-Layout
(def input-data [{td/country td/country-a
                  "date" "1997-01-01"
                  td/fact-1 12
                  td/org ["A" "B"]
                  "org2" "A"}
                 {td/country td/country-a
                  "date" "1997-05-01"
                  td/fact-1 -6
                  td/org ["B" "C"]
                  "org2" ["A" "C"]}
                 {td/country td/country-a
                  "date" "1997-05-04"
                  td/fact-1 18
                  td/org "C"
                  "org2" ["A" "D"]}
                 {td/country td/country-c
                  "date" "1997-05-04"
                  "keine_fact-1" 16
                  td/org "CC"
                  "org2" "B"}
                 {td/country td/country-c
                  "date" "1998-01-04"
                  td/fact-1 100
                  td/org "A"
                  "org2" "D"}
                 {td/country td/country-c
                  "date" "1998-01-04"
                  td/fact-1 0
                  td/org ["A" "B"]
                  "org2" "C"}])

(def input-data-d [{td/country td/country-a
                    "date" "1997-01-01"
                    td/fact-1 12.76
                    td/org ["A" "B"]
                    "org2" "A"}
                   {td/country td/country-a
                    "date" "1997-05-01"
                    td/fact-1 -6.3
                    td/org ["B" "C"]
                    "org2" ["A" "C"]}
                   {td/country td/country-a
                    "date" "1997-05-04"
                    td/fact-1 18.2
                    td/org "C"
                    "org2" ["A" "D"]}
                   {td/country td/country-c
                    "date" "1997-05-04"
                    "keine_fact-1" 16
                    td/org "CC"
                    "org2" "B"}
                   {td/country td/country-c
                    "date" "1998-01-04"
                    td/fact-1 100.1
                    td/org "A"
                    "org2" "D"}
                   {td/country td/country-c
                    "date" "1998-01-04"
                    td/fact-1 0.8
                    td/org ["A" "B"]
                    "org2" "C"}])

(def input-data-2 [{td/country td/country-a
                    "date" "1997-01-01"
                    td/fact-1 12
                    td/org ["A" "B"]
                    "org2" "A"}
                   {td/country td/country-a
                    "date" "1997-05-01"
                    td/fact-1 -6
                    td/org ["B" "C"]
                    "org2" ["A" "C"]}
                   {td/country td/country-a
                    "date" "1997-05-04"
                    td/fact-1 18
                    td/org "C"
                    "org2" ["A" "D"]}
                   {td/country td/country-c
                    "date" "1997-05-04"
                    "keine_fact-1" 16
                    td/org "CC"
                    "org2" "B"}
                   {td/country td/country-c
                    "date" "1998-12-04"
                    td/fact-1 100
                    td/org "A"
                    "org2" "D"}
                   {td/country td/country-c
                    "date" "1998-01-04"
                    td/fact-1 0
                    td/org ["A" "B"]
                    "org2" "C"}
                   {td/country td/country-a
                    "date" "1997-11-02"
                    td/fact-1 12
                    td/org ["A" "B"]
                    "org2" "A"}
                   {td/country td/country-a
                    "date" "1997-05-02"
                    td/fact-1 -6
                    td/org ["B" "C"]
                    "org2" ["A" "C"]}
                   {td/country td/country-a
                    "date" "1997-05-05"
                    td/fact-1 18
                    td/org "C"
                    "org2" ["A" "D"]}
                   {td/country td/country-c
                    "date" "1997-05-05"
                    "keine_fact-1" 16
                    td/org "CC"
                    "org2" "B"}
                   {td/country td/country-c
                    "date" "1998-01-05"
                    td/fact-1 100
                    td/org "A"
                    "org2" "D"}
                   {td/country td/country-c
                    "date" "1998-09-05"
                    td/fact-1 0
                    td/org ["A" "B"]
                    "org2" "C"}])
(def ez-input-data [{td/country td/country-a
                     "date" "1997-01-01"
                     td/fact-1 12
                     td/org ["A" "B"]}
                    {td/country td/country-a
                     "date" "1997-05-01"
                     td/fact-1 6
                     td/org ["B" "C"]}
                    {td/country td/country-a
                     "date" "1997-05-04"
                     td/fact-1 18
                     td/org "C"}
                    {td/country td/country-c
                     "date" "1997-05-04"
                     td/fact-1 16
                     td/org "CC"}
                    {td/country td/country-c
                     "date" "1998-01-04"
                     td/fact-1 100
                     td/org "A"}
                    {td/country td/country-c
                     "date" "1998-01-04"
                     td/fact-1 0
                     td/org ["A" "B"]}])
(def color-scheme-1
  {:name "Scale-1-5"
   :id "colorscale2"
   :color-scale-numbers 5
   :colors {:0 "#e33b3b"
            :1 "#fb8d02"
            :2 "#4fb34f"
            :3 "#0292b5"
            :4 "#033579"}})

(def color-scheme-2
  {:name "Scale-1-5"
   :id "colorscale2"
   :color-scale-numbers 5
   :colors {:0 "#000000"
            :1 "#111111"
            :2 "#222222"
            :3 "#333333"
            :4 "#444444"}})

(def base-layout-3
  {:id  2
   :name "Base Layout 3"
   :timestamp 0
   :default? true
   :color-scheme color-scheme-2
   :attributes [td/country]
   :attribute-type "string"
   :value-assigned [[td/country-a] ["*"] [] []]
   :card-scheme "scheme-3"
   :field-assignments [["else" "datasource"]
                       ["else" "date"]]})

(def base-layout-1
  {:id 001
   :name "Base Layout 1"
   :timestamp 0
   :default? true
   :color-scheme color-scheme-1
   :attributes [td/fact-1 td/fact-1]
   :attribute-type "integer"
   :value-assigned [[51 1000000]
                    [20 51]
                    [6 20]
                    [1 6]
                    [0 1]]
   :card-scheme "scheme-1"
   :field-assignments [["else" "date"]
                       ["else" "datasource"]
                       ["else" td/category-1]
                       ["else" td/fact-1]
                       ["notes" "notes"]
                       ["else" td/country]
                       [td/org td/org]
                       ["location" "location"]]})

(def base-layout-1-d
  {:id 001
   :name "Base Layout 1"
   :timestamp 0
   :default? true
   :color-scheme color-scheme-1
   :attributes [td/fact-1 td/fact-1]
   :attribute-type "decimal"
   :value-assigned [[51 1000000]
                    [20 51]
                    [6 20]
                    [1 6]
                    [0 1]]
   :card-scheme "scheme-1"
   :field-assignments [["else" "date"]
                       ["else" "datasource"]
                       ["else" td/category-1]
                       ["else" td/fact-1]
                       ["notes" "notes"]
                       ["else" td/country]
                       [td/org td/org]
                       ["location" "location"]]})

(t/deftest apply-layout-test
  (t/testing "Apply a basic layout to a list of events"
    (t/is (= (mapv #(get-in % ["layout" "color"])
                   (of/perform-operation {"di-1" ez-input-data} {}
                                         [:apply-layout {:layouts [base-layout-1]} "di-1"]))
             ["#4fb34f"
              "#4fb34f"
              "#4fb34f"
              "#4fb34f"
              "#e33b3b"
              "#033579"])))
  (t/testing "Apply a basic layout to a list of events"
    (t/is (= (mapv #(get-in % ["layout" "color"])
                   (of/perform-operation {"di-1" ez-input-data} {}
                                         [:apply-layout {:layouts []} "di-1"]))
             [nil nil nil nil nil nil])))
  (t/testing "Apply a layout to a list of events with non-matching values and attributes"
    (t/is (= (mapv #(get-in % ["layout" "color"])
                   (of/perform-operation {"di-1" input-data} {}
                                         [:apply-layout {:layouts [base-layout-1]} "di-1"]))
             ["#4fb34f"
              nil
              "#4fb34f"
              nil
              "#e33b3b"
              "#033579"])))
  (t/testing "Apply a layout to a list of events with non-matching values and attributes - decimal"
    (t/is (= (mapv #(get-in % ["layout" "color"])
                   (of/perform-operation {"di-1" input-data-d} {}
                                         [:apply-layout {:layouts [base-layout-1-d]} "di-1"]))
             ["#4fb34f"
              nil
              "#4fb34f"
              nil
              "#e33b3b"
              "#033579"])))
  (t/testing "Applying two layouts, the second one should be used if the first one cannot colorize an event."
    (t/is (= (mapv #(get-in % ["layout" "color"])
                   (of/perform-operation {"di-1" input-data} {}
                                         [:apply-layout {:layouts [base-layout-1 base-layout-3]} "di-1"]))
             ["#4fb34f"
              "#000000"
              "#4fb34f"
              "#111111"
              "#e33b3b"
              "#033579"])))
  (t/testing "Applying layouts to grouped data is expected to change events in the same way applying a layout to ungrouped data does"
    (t/is (= (->> (of/perform-operation {"di-1" ez-input-data}
                                        {}
                                        [:apply-layout {:layouts [base-layout-1 base-layout-3]}
                                         [:group-by
                                          {:attributes [td/country "year"]
                                           :reduce-date? false}
                                          "di-1"]])
                  vals
                  (apply concat)
                  set)
             (->> (of/perform-operation {"di-1" ez-input-data}
                                        {}
                                        [:apply-layout {:layouts [base-layout-1 base-layout-3]}
                                         "di-1"])
                  set)))))


(comment

  (mapv #(get-in % ["layout" "color"])
        (of/perform-operation {"di-1" input-data-2} {}
                              [:apply-layout {:layouts [base-layout-1 base-layout-3]} "di-1"]))

  (tufte/add-basic-println-handler! {})

  (tufte/profile {:when false}
                 (time
                  (def a
                    (mapv
                     (fn [_]
                       (= (mapv #(get-in % ["layout" "color"])
                                (of/perform-operation {"di-1" input-data-2} {}
                                                      [:apply-layout {:layouts [base-layout-1 base-layout-3]} "di-1"]))
                          ["#4fb34f"
                           "#000000"
                           "#4fb34f"
                           "#111111"
                           "#e33b3b"
                           "#033579"
                           "#4fb34f"
                           "#000000"
                           "#4fb34f"
                           "#111111"
                           "#e33b3b"
                           "#033579"]))
                     (range 100000))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;   Sub-group-by

(def keep-group-by-result
  {{td/org "B"}
   [{td/country      td/country-a
     "date"         "1997-01-01"
     td/fact-1   12
     td/org ["A" "B"]
     "layout"       {"color" "#4fb34f"
                     "id"    1}}
    {td/country      td/country-a
     "date"         "1997-05-01"
     td/fact-1   6
     td/org ["B" "C"]
     "layout"       {"color" "#4fb34f"
                     "id"    1}}
    {td/country      td/country-c
     "date"         "1998-01-04"
     td/fact-1   0
     td/org ["A" "B"]
     "layout"       {"color" "#033579"
                     "id"    1}}]
   {td/org "A"}
   [{td/country      td/country-a
     "date"         "1997-01-01"
     td/fact-1   12
     td/org ["A" "B"]
     "layout"       {"color" "#4fb34f"
                     "id"    1}}
    {td/country      td/country-c
     "date"         "1998-01-04"
     td/fact-1   100
     td/org "A"
     "layout"       {"color" "#e33b3b"
                     "id"    1}}
    {td/country      td/country-c
     "date"         "1998-01-04"
     td/fact-1   0
     td/org ["A" "B"]
     "layout"       {"color" "#033579"
                     "id"    1}}]
   {td/org "C"}
   [{td/country      td/country-a
     "date"         "1997-05-01"
     td/fact-1   6
     td/org ["B" "C"]
     "layout"       {"color" "#4fb34f"
                     "id"    1}}
    {td/country      td/country-a
     "date"         "1997-05-04"
     td/fact-1   18
     td/org "C"
     "layout"      {"color" "#4fb34f"
                    "id"    1}}]
   {td/org "CC"}
   [{td/country      td/country-c
     "date"         "1997-05-04"
     td/fact-1   16
     td/org "CC"
     "layout"       {"color" "#4fb34f"
                     "id"    1}}]})

(def keep-sub-group-by-result
  {{td/org "B"} {{"org2" "A"} [{td/country       td/country-a
                                "date"          "1997-01-01"
                                td/fact-1    12
                                td/org  ["A" "B"]
                                "org2" "A"
                                "layout"        {"color" "#4fb34f"
                                                 "id"    1}}
                               {td/country       td/country-a
                                "date"          "1997-05-01"
                                td/fact-1    -6
                                td/org  ["B" "C"]
                                "org2" ["A" "C"]
                                "layout"        {"color" nil
                                                 "id"    nil}}]
                 {"org2" "C"} [{td/country       td/country-a
                                "date"          "1997-05-01"
                                td/fact-1    -6
                                td/org  ["B" "C"]
                                "org2" ["A" "C"]
                                "layout"        {"color" nil
                                                 "id"    nil}}
                               {td/country       td/country-c
                                "date"          "1998-01-04"
                                td/fact-1    0
                                td/org  ["A" "B"]
                                "org2" "C"
                                "layout"        {"color" "#033579"
                                                 "id"    1}}]}
   {td/org "A"}  {{"org2" "A"} [{td/country       td/country-a
                                 "date"          "1997-01-01"
                                 td/fact-1    12
                                 td/org  ["A" "B"]
                                 "org2" "A"
                                 "layout"        {"color" "#4fb34f"
                                                  "id"    1}}]
                  {"org2" "D"} [{td/country       td/country-c
                                 "date"          "1998-01-04"
                                 td/fact-1    100
                                 td/org  "A"
                                 "org2" "D"
                                 "layout"        {"color" "#e33b3b"
                                                  "id"    1}}],
                  {"org2" "C"} [{td/country       td/country-c
                                 "date"          "1998-01-04"
                                 td/fact-1    0
                                 td/org  ["A" "B"]
                                 "org2" "C"
                                 "layout"        {"color" "#033579"
                                                  "id"    1}}]}
   {td/org "C"}  {{"org2" "C"} [{td/country       td/country-a
                                 "date"          "1997-05-01"
                                 td/fact-1    -6
                                 td/org  ["B" "C"]
                                 "org2" ["A" "C"]
                                 "layout"        {"color" nil
                                                  "id"    nil}}]
                  {"org2" "A"} [{td/country       td/country-a
                                 "date"          "1997-05-01"
                                 td/fact-1    -6
                                 td/org  ["B" "C"]
                                 "org2" ["A" "C"]
                                 "layout"        {"color" nil
                                                  "id"    nil}}
                                {td/country       td/country-a
                                 "date"          "1997-05-04"
                                 td/fact-1    18
                                 td/org  "C"
                                 "org2" ["A" "D"]
                                 "layout"        {"color" "#4fb34f"
                                                  "id"    1}}]
                  {"org2" "D"} [{td/country       td/country-a
                                 "date"          "1997-05-04"
                                 td/fact-1    18
                                 td/org  "C"
                                 "org2" ["A" "D"]
                                 "layout"        {"color" "#4fb34f"
                                                  "id"    1}}]}
   {td/org "CC"} {{"org2" "B"} [{td/country          td/country-c
                                 "date"             "1997-05-04"
                                 "keine_fact-1" 16
                                 td/org     "CC"
                                 "org2"    "B"
                                 "layout"           {"color" nil
                                                     "id"   nil}}]}})

(def sort-events-by-date-group
  [[{td/org "CC"} [{td/country          td/country-c
                    "date"             "1997-05-04"
                    "keine_fact-1" 16
                    td/org     "CC"
                    "org2"    "B"
                    "layout"           {"color" nil
                                        "id"    nil}}]]
   [{td/org "C"}  [{td/country       td/country-a
                    "date"          "1997-05-04"
                    td/fact-1    18
                    td/org  "C"
                    "org2" ["A" "D"]
                    "layout"        {"color" "#4fb34f"
                                     "id"    1}}
                   {td/country       td/country-a
                    "date"          "1997-05-01"
                    td/fact-1    -6
                    td/org  ["B" "C"]
                    "org2" ["A" "C"]
                    "layout"        {"color" nil
                                     "id"    nil}}]]
   [{td/org "B"}  [{td/country       td/country-c
                    "date"          "1998-01-04"
                    td/fact-1    0
                    td/org  ["A" "B"]
                    "org2" "C"
                    "layout"        {"color" "#033579"
                                     "id"    1}}
                   {td/country       td/country-a
                    "date"          "1997-05-01"
                    td/fact-1    -6
                    td/org  ["B" "C"]
                    "org2" ["A" "C"]
                    "layout"        {"color" nil
                                     "id"    nil}}
                   {td/country       td/country-a
                    "date"          "1997-01-01"
                    td/fact-1    12
                    td/org  ["A" "B"]
                    "org2" "A"
                    "layout"        {"color" "#4fb34f"
                                     "id"    1}}]]
   [{td/org "A"}  [{td/country       td/country-c
                    "date"          "1998-01-04"
                    td/fact-1    100
                    td/org  "A"
                    "org2" "D"
                    "layout"        {"color" "#e33b3b"
                                     "id"    1}}
                   {td/country       td/country-c
                    "date"          "1998-01-04"
                    td/fact-1    0
                    td/org  ["A" "B"]
                    "org2" "C"
                    "layout"        {"color" "#033579"
                                     "id"    1}}
                   {td/country       td/country-a
                    "date"          "1997-01-01"
                    td/fact-1    12
                    td/org  ["A" "B"]
                    "org2" "A"
                    "layout"        {"color" "#4fb34f"
                                     "id"    1}}]]])

(def sort-events-by-date-sub-group
  [[{td/org "CC"} [[{"org2" "B"} [{td/country          td/country-c
                                   "date"             "1997-05-04"
                                   "keine_fact-1" 16
                                   td/org     "CC"
                                   "org2"    "B"
                                   "layout"           {"color" nil
                                                       "id"   nil}}]]]]
   [{td/org "C"}  [[{"org2" "D"} [{td/country       td/country-a
                                   "date"          "1997-05-04"
                                   td/fact-1    18
                                   td/org  "C"
                                   "org2" ["A" "D"]
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}]]
                   [{"org2" "C"} [{td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}]]
                   [{"org2" "A"} [{td/country       td/country-a
                                   "date"          "1997-05-04"
                                   td/fact-1    18
                                   td/org  "C"
                                   "org2" ["A" "D"]
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}
                                  {td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}]]]]
   [{td/org "B"}  [[{"org2" "C"} [{td/country       td/country-c
                                   "date"          "1998-01-04"
                                   td/fact-1    0
                                   td/org  ["A" "B"]
                                   "org2" "C"
                                   "layout"        {"color" "#033579"
                                                    "id"    1}}
                                  {td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}]]
                   [{"org2" "A"} [{td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}
                                  {td/country       td/country-a
                                   "date"          "1997-01-01"
                                   td/fact-1    12
                                   td/org  ["A" "B"]
                                   "org2" "A"
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}]]]]
   [{td/org "A"}  [[{"org2" "D"} [{td/country       td/country-c
                                   "date"          "1998-01-04"
                                   td/fact-1    100
                                   td/org  "A"
                                   "org2" "D"
                                   "layout"        {"color" "#e33b3b"
                                                    "id"    1}}]]
                   [{"org2" "C"} [{td/country       td/country-c
                                   "date"          "1998-01-04"
                                   td/fact-1    0
                                   td/org  ["A" "B"]
                                   "org2" "C"
                                   "layout"        {"color" "#033579"
                                                    "id"    1}}]]
                   [{"org2" "A"} [{td/country       td/country-a
                                   "date"          "1997-01-01"
                                   td/fact-1    12
                                   td/org  ["A" "B"]
                                   "org2" "A"
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}]]]]])

(def sort-groups-by-org-group
  [[{td/org "CC"} [{td/country          td/country-c
                    "date"             "1997-05-04"
                    "keine_fact-1" 16
                    td/org     "CC"
                    "org2"    "B"
                    "layout"           {"color" nil
                                        "id"    nil}}]]
   [{td/org "C"} [{td/country       td/country-a
                   "date"          "1997-05-01"
                   td/fact-1    -6
                   td/org  ["B" "C"]
                   "org2" ["A" "C"]
                   "layout"        {"color" nil
                                    "id"    nil}}
                  {td/country       td/country-a
                   "date"          "1997-05-04"
                   td/fact-1    18
                   td/org  "C"
                   "org2" ["A" "D"]
                   "layout"        {"color" "#4fb34f"
                                    "id"    1}}]]
   [{td/org "B"} [{td/country       td/country-a
                   "date"          "1997-01-01"
                   td/fact-1    12
                   td/org  ["A" "B"]
                   "org2" "A"
                   "layout"        {"color" "#4fb34f"
                                    "id"    1}}
                  {td/country       td/country-a
                   "date"          "1997-05-01"
                   td/fact-1    -6
                   td/org  ["B" "C"]
                   "org2" ["A" "C"]
                   "layout"        {"color" nil
                                    "id"    nil}}
                  {td/country       td/country-c
                   "date"          "1998-01-04"
                   td/fact-1    0
                   td/org  ["A" "B"]
                   "org2" "C"
                   "layout"        {"color" "#033579"
                                    "id"    1}}]]
   [{td/org "A"} [{td/country       td/country-a
                   "date"          "1997-01-01"
                   td/fact-1   12
                   td/org  ["A" "B"]
                   "org2" "A"
                   "layout"        {"color" "#4fb34f"
                                    "id"    1}}
                  {td/country       td/country-c
                   "date"          "1998-01-04"
                   td/fact-1    100
                   td/org  "A"
                   "org2" "D"
                   "layout"        {"color" "#e33b3b"
                                    "id"    1}}
                  {td/country       td/country-c
                   "date"          "1998-01-04"
                   td/fact-1    0
                   td/org  ["A" "B"]
                   "org2" "C"
                   "layout"        {"color" "#033579"
                                    "id"    1}}]]])

(def sort-groups-by-average-fact-1
  [[{td/org "A" "aggregated-value" (float 37.333333333333336)}
    [{td/country       td/country-a
      "date"          "1997-01-01"
      td/fact-1   12
      td/org  ["A" "B"]
      "org2" "A"
      "layout"        {"color" "#4fb34f"
                       "id"    1}}
     {td/country       td/country-c
      "date"          "1998-01-04"
      td/fact-1    100
      td/org  "A"
      "org2" "D"
      "layout"        {"color" "#e33b3b"
                       "id"    1}}
     {td/country       td/country-c
      "date"          "1998-01-04"
      td/fact-1    0
      td/org  ["A" "B"]
      "org2" "C"
      "layout"        {"color" "#033579"
                       "id"    1}}]]
   [{td/org "C" "aggregated-value" (float 6.0)} [{td/country       td/country-a
                                                  "date"          "1997-05-01"
                                                  td/fact-1    -6
                                                  td/org  ["B" "C"]
                                                  "org2" ["A" "C"]
                                                  "layout"        {"color" nil
                                                                   "id"    nil}}
                                                 {td/country       td/country-a
                                                  "date"          "1997-05-04"
                                                  td/fact-1    18
                                                  td/org  "C"
                                                  "org2" ["A" "D"]
                                                  "layout"        {"color" "#4fb34f"
                                                                   "id"    1}}]]
   [{td/org "B" "aggregated-value" (float 2.0)} [{td/country       td/country-a
                                                  "date"          "1997-01-01"
                                                  td/fact-1    12
                                                  td/org  ["A" "B"]
                                                  "org2" "A"
                                                  "layout"        {"color" "#4fb34f"
                                                                   "id"    1}}
                                                 {td/country       td/country-a
                                                  "date"          "1997-05-01"
                                                  td/fact-1    -6
                                                  td/org  ["B" "C"]
                                                  "org2" ["A" "C"]
                                                  "layout"        {"color" nil
                                                                   "id"    nil}}
                                                 {td/country       td/country-c
                                                  "date"          "1998-01-04"
                                                  td/fact-1    0
                                                  td/org  ["A" "B"]
                                                  "org2" "C"
                                                  "layout"        {"color" "#033579"
                                                                   "id"    1}}]]
   [{td/org "CC"} [{td/country          td/country-c
                    "date"             "1997-05-04"
                    "keine_fact-1" 16
                    td/org     "CC"
                    "org2"    "B"
                    "layout"           {"color" nil
                                        "id"    nil}}]]])

(def sort-groups-by-org-sub-group
  [[{td/org "CC"} {{"org2" "B"} [{td/country          td/country-c
                                  "date"             "1997-05-04"
                                  "keine_fact-1" 16
                                  td/org     "CC"
                                  "org2"    "B"
                                  "layout"           {"color" nil
                                                      "id"    nil}}]}]
   [{td/org "C"} {{"org2" "C"} [{td/country       td/country-a
                                 "date"          "1997-05-01"
                                 td/fact-1    -6
                                 td/org  ["B" "C"]
                                 "org2" ["A" "C"]
                                 "layout"        {"color" nil
                                                  "id"    nil}}]
                  {"org2" "A"} [{td/country       td/country-a
                                 "date"          "1997-05-01"
                                 td/fact-1    -6
                                 td/org  ["B" "C"]
                                 "org2" ["A" "C"]
                                 "layout"        {"color" nil
                                                  "id"    nil}}
                                {td/country       td/country-a
                                 "date"          "1997-05-04"
                                 td/fact-1    18
                                 td/org  "C"
                                 "org2" ["A" "D"]
                                 "layout"        {"color" "#4fb34f"
                                                  "id"    1}}]
                  {"org2" "D"} [{td/country       td/country-a
                                 "date"          "1997-05-04"
                                 td/fact-1    18
                                 td/org  "C"
                                 "org2" ["A" "D"]
                                 "layout"        {"color" "#4fb34f"
                                                  "id"    1}}]}]
   [{td/org "B"} {{"org2" "A"} [{td/country        td/country-a
                                 "date"           "1997-01-01"
                                 td/fact-1     12
                                 td/org   ["A" "B"]
                                 "org2" "A"
                                 "layout"         {"color" "#4fb34f"
                                                   "id"    1}}
                                {td/country       td/country-a
                                 "date"          "1997-05-01"
                                 td/fact-1    -6
                                 td/org  ["B" "C"]
                                 "org2" ["A" "C"]
                                 "layout"        {"color" nil
                                                  "id"    nil}}]
                  {"org2" "C"} [{td/country       td/country-a
                                 "date"          "1997-05-01"
                                 td/fact-1    -6
                                 td/org  ["B" "C"]
                                 "org2" ["A" "C"]
                                 "layout"        {"color" nil
                                                  "id"    nil}}
                                {td/country       td/country-c
                                 "date"          "1998-01-04"
                                 td/fact-1    0
                                 td/org  ["A" "B"]
                                 "org2" "C"
                                 "layout"        {"color" "#033579"
                                                  "id"    1}}]}]
   [{td/org "A"} {{"org2" "A"} [{td/country       td/country-a
                                 "date"          "1997-01-01"
                                 td/fact-1    12
                                 td/org  ["A" "B"]
                                 "org2" "A"
                                 "layout"        {"color" "#4fb34f"
                                                  "id"    1}}]
                  {"org2" "D"} [{td/country       td/country-c
                                 "date"          "1998-01-04"
                                 td/fact-1    100
                                 td/org  "A"
                                 "org2" "D"
                                 "layout"        {"color" "#e33b3b"
                                                  "id"    1}}]
                  {"org2" "C"} [{td/country       td/country-c
                                 "date"          "1998-01-04"
                                 td/fact-1    0
                                 td/org  ["A" "B"]
                                 "org2" "C"
                                 "layout"        {"color" "#033579"
                                                  "id"    1}}]}]])

(def sort-sub-groups-by-org-sub-group
  [[{td/org "CC"} [[{"org2" "B"} [{td/country          td/country-c
                                   "date"             "1997-05-04"
                                   "keine_fact-1" 16
                                   td/org     "CC"
                                   "org2"    "B"
                                   "layout"           {"color" nil
                                                       "id"     nil}}]]]]
   [{td/org "C"}  [[{"org2" "D"} [{td/country       td/country-a
                                   "date"          "1997-05-04"
                                   td/fact-1    18
                                   td/org  "C"
                                   "org2" ["A" "D"]
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}]]
                   [{"org2" "C"} [{td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}]]
                   [{"org2" "A"} [{td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}
                                  {td/country       td/country-a
                                   "date"          "1997-05-04"
                                   td/fact-1    18
                                   td/org  "C"
                                   "org2" ["A" "D"]
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}]]]]
   [{td/org "B"}  [[{"org2" "C"} [{td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}
                                  {td/country       td/country-c
                                   "date"          "1998-01-04"
                                   td/fact-1    0
                                   td/org  ["A" "B"]
                                   "org2" "C"
                                   "layout"        {"color" "#033579"
                                                    "id"    1}}]]
                   [{"org2" "A"} [{td/country       td/country-a
                                   "date"          "1997-01-01"
                                   td/fact-1    12
                                   td/org  ["A" "B"]
                                   "org2" "A"
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}
                                  {td/country       td/country-a
                                   "date"          "1997-05-01"
                                   td/fact-1    -6
                                   td/org  ["B" "C"]
                                   "org2" ["A" "C"]
                                   "layout"        {"color" nil
                                                    "id"    nil}}]]]]
   [{td/org "A"}  [[{"org2" "D"} [{td/country       td/country-c
                                   "date"          "1998-01-04"
                                   td/fact-1    100
                                   td/org  "A"
                                   "org2" "D"
                                   "layout"        {"color" "#e33b3b"
                                                    "id"    1}}]]
                   [{"org2" "C"} [{td/country       td/country-c
                                   "date"          "1998-01-04"
                                   td/fact-1    0
                                   td/org  ["A" "B"]
                                   "org2" "C"
                                   "layout"        {"color" "#033579"
                                                    "id"    1}}]]
                   [{"org2" "A"} [{td/country       td/country-a
                                   "date"          "1997-01-01"
                                   td/fact-1    12
                                   td/org  ["A" "B"]
                                   "org2" "A"
                                   "layout"        {"color" "#4fb34f"
                                                    "id"    1}}]]]]])

(def sort-both-by-org-sub-group
  [[{td/org "CC"} [[{"org2" "B"} [{td/country          td/country-c
                                   "date"             "1997-05-04"
                                   "keine_fact-1" 16
                                   td/org     "CC"
                                   "org2"    "B"
                                   "layout"           {"color" nil
                                                       "id"    nil}}]]]]
   [{td/org "C"} [[{"org2" "D"} [{td/country       td/country-a
                                  "date"          "1997-05-04"
                                  td/fact-1    18
                                  td/org  "C"
                                  "org2" ["A" "D"]
                                  "layout"        {"color" "#4fb34f"
                                                   "id"    1}}]]
                  [{"org2" "C"} [{td/country       td/country-a
                                  "date"          "1997-05-01"
                                  td/fact-1    -6
                                  td/org  ["B" "C"]
                                  "org2" ["A" "C"]
                                  "layout"        {"color" nil
                                                   "id"    nil}}]]
                  [{"org2" "A"} [{td/country       td/country-a
                                  "date"          "1997-05-01"
                                  td/fact-1    -6
                                  td/org  ["B" "C"]
                                  "org2" ["A" "C"]
                                  "layout"        {"color" nil
                                                   "id"    nil}}
                                 {td/country       td/country-a
                                  "date"          "1997-05-04"
                                  td/fact-1    18
                                  td/org  "C"
                                  "org2" ["A" "D"]
                                  "layout"        {"color" "#4fb34f"
                                                   "id"    1}}]]]]
   [{td/org "B"} [[{"org2" "C"} [{td/country        td/country-a
                                  "date"           "1997-05-01"
                                  td/fact-1     -6
                                  td/org   ["B" "C"]
                                  "org2" ["A" "C"]
                                  "layout"         {"color" nil
                                                    "id"    nil}}
                                 {td/country       td/country-c
                                  "date"          "1998-01-04"
                                  td/fact-1    0
                                  td/org  ["A" "B"]
                                  "org2" "C"
                                  "layout"        {"color" "#033579"
                                                   "id"    1}}]]
                  [{"org2" "A"} [{td/country       td/country-a
                                  "date"          "1997-01-01"
                                  td/fact-1    12
                                  td/org  ["A" "B"]
                                  "org2" "A"
                                  "layout"        {"color" "#4fb34f"
                                                   "id"    1}}
                                 {td/country       td/country-a
                                  "date"          "1997-05-01"
                                  td/fact-1    -6
                                  td/org  ["B" "C"]
                                  "org2" ["A" "C"]
                                  "layout"        {"color" nil
                                                   "id"    nil}}]]]]
   [{td/org "A"} [[{"org2" "D"} [{td/country       td/country-c
                                  "date"          "1998-01-04"
                                  td/fact-1    100
                                  td/org  "A"
                                  "org2" "D"
                                  "layout"        {"color" "#e33b3b"
                                                   "id"    1}}]]
                  [{"org2" "C"} [{td/country       td/country-c
                                  "date"          "1998-01-04"
                                  td/fact-1    0
                                  td/org  ["A" "B"]
                                  "org2" "C"
                                  "layout"        {"color" "#033579"
                                                   "id"    1}}]]
                  [{"org2" "A"} [{td/country       td/country-a
                                  "date"          "1997-01-01"
                                  td/fact-1    12
                                  td/org  ["A" "B"]
                                  "org2" "A"
                                  "layout"        {"color" "#4fb34f"
                                                   "id"     1}}]]]]])

(t/deftest sub-group-by-test
  (t/testing "simple keep group by"
    (t/is (= (of/perform-operation {"di-1" ez-input-data} {}
                                   [:group-by {:attributes [td/org] :mode :keep}
                                    [:apply-layout {:layouts [base-layout-1]} "di-1"]])
             keep-group-by-result)))
  (t/testing "simple keep sub group by"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:group-by {:attributes ["org2"] :mode :keep}
                                    [:group-by {:attributes [td/org] :mode :keep}
                                     [:apply-layout {:layouts [base-layout-1]} "di-1"]]])
             keep-sub-group-by-result)))
  (t/testing "simple keep sub group by - error"
    (t/is (thrown-with-msg?
           #?(:clj clojure.lang.ExceptionInfo
              :cljs js/Error)
           #"Group-by works only on meta-sub-group-list-events and meta-group-list-events"
           (of/perform-operation {"di-1" input-data} {}
                                 [:group-by {:attributes ["org2"] :mode :keep}
                                  [:sum {:attribute td/fact-1}
                                   [:group-by {:attributes [td/org] :mode :keep}
                                    [:apply-layout {:layouts [base-layout-1]} "di-1"]]]])))))

(t/deftest sort-by-simple-test
  (t/testing "sort events by date for grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute "date"
                                              :attribute-types {"date" :date}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :events}
                                    [:sort-by {:attribute td/org
                                               :attribute-types {td/org :number}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 0}
                                     [:group-by {:attributes [td/org] :mode :keep}
                                      [:apply-layout {:layouts [base-layout-1]} "di-1"]]]])
             sort-events-by-date-group)))
  (t/testing "sort events by date for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute "date"
                                              :attribute-types {"date" :date}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :events}
                                    [:sort-by {:attribute td/org
                                               :attribute-types {td/org :string}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 0}
                                     [:sort-by {:attribute "org2"
                                                :attribute-types {"org2" :string}
                                                :direction :desc
                                                :return-map? false
                                                :apply-to :group
                                                :level 1}
                                      [:group-by {:attributes ["org2"] :mode :keep}
                                       [:group-by {:attributes [td/org] :mode :keep}
                                        [:apply-layout {:layouts [base-layout-1]} "di-1"]]]]]])
             sort-events-by-date-sub-group)))
  (t/testing "sort groups by org for grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute td/org
                                              :attribute-types {td/org :string}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group}
                                    [:group-by {:attributes [td/org] :mode :keep}
                                     [:apply-layout {:layouts [base-layout-1]} "di-1"]]])
             sort-groups-by-org-group)))
  (t/testing "sort groups by org for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute td/org
                                              :attribute-types {td/org :string}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0}
                                    [:group-by {:attributes ["org2"] :mode :keep}
                                     [:group-by {:attributes [td/org] :mode :keep}
                                      [:apply-layout {:layouts [base-layout-1]} "di-1"]]]])
             sort-groups-by-org-sub-group)))
  (t/testing "sort sub groups by org for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute td/org
                                              :attribute-types {td/org :string}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0}
                                    [:sort-by {:attribute "org2"
                                               :attribute-types {"org2" :string}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 1}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:group-by {:attributes [td/org] :mode :keep}
                                       [:apply-layout {:layouts [base-layout-1]} "di-1"]]]]])
             sort-sub-groups-by-org-sub-group)))
  (t/testing "sort both groups and sub groups by org for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute td/org
                                              :attribute-types {td/org :string}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0}
                                    [:sort-by {:attribute "org2"
                                               :attribute-types {"org2" :string}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 1}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:group-by {:attributes [td/org] :mode :keep}
                                       [:apply-layout {:layouts [base-layout-1]} "di-1"]]]]])
             sort-both-by-org-sub-group))))

(def sub-group-by-with-aggregations-outer
  [[{td/org "A"
     "aggregated-value" 112} {{"org2" "A"} [{td/country       td/country-a
                                             "date"          "1997-01-01"
                                             td/fact-1    12
                                             td/org  ["A" "B"]
                                             "org2" "A"
                                             "layout"        {"color" "#4fb34f"
                                                              "id"    1}}]
                              {"org2" "D"} [{td/country       td/country-c
                                             "date"          "1998-01-04"
                                             td/fact-1    100
                                             td/org  "A"
                                             "org2" "D"
                                             "layout"        {"color" "#e33b3b"
                                                              "id"    1}}]
                              {"org2" "C"} [{td/country       td/country-c
                                             "date"          "1998-01-04"
                                             td/fact-1    0
                                             td/org  ["A" "B"]
                                             "org2" "C"
                                             "layout"        {"color" "#033579"
                                                              "id"    1}}]}]
   [{td/org "C"
     "aggregated-value" 24} {{"org2" "C"} [{td/country       td/country-a
                                            "date"          "1997-05-01"
                                            td/fact-1    -6
                                            td/org  ["B" "C"]
                                            "org2" ["A" "C"]
                                            "layout"        {"color" nil
                                                             "id"    nil}}]
                             {"org2" "A"} [{td/country       td/country-a
                                            "date"          "1997-05-01"
                                            td/fact-1    -6
                                            td/org  ["B" "C"]
                                            "org2" ["A" "C"]
                                            "layout"        {"color" nil
                                                             "id"    nil}}
                                           {td/country       td/country-a
                                            "date"          "1997-05-04"
                                            td/fact-1    18
                                            td/org  "C"
                                            "org2" ["A" "D"]
                                            "layout"        {"color" "#4fb34f"
                                                             "id"     1}}]
                             {"org2" "D"} [{td/country       td/country-a
                                            "date"          "1997-05-04"
                                            td/fact-1    18
                                            td/org  "C"
                                            "org2" ["A" "D"]
                                            "layout"        {"color" "#4fb34f"
                                                             "id"    1}}]}]
   [{td/org "B"
     "aggregated-value" 0} {{"org2" "A"} [{td/country       td/country-a
                                           "date"          "1997-01-01"
                                           td/fact-1    12
                                           td/org  ["A" "B"]
                                           "org2" "A"
                                           "layout"        {"color" "#4fb34f"
                                                            "id"    1}}
                                          {td/country       td/country-a
                                           "date"          "1997-05-01"
                                           td/fact-1    -6
                                           td/org  ["B" "C"]
                                           "org2" ["A" "C"]
                                           "layout"        {"color" nil
                                                            "id"    nil}}]
                            {"org2" "C"} [{td/country       td/country-a
                                           "date"          "1997-05-01"
                                           td/fact-1    -6
                                           td/org  ["B" "C"]
                                           "org2" ["A" "C"]
                                           "layout"        {"color" nil
                                                            "id"    nil}}
                                          {td/country       td/country-c
                                           "date"          "1998-01-04"
                                           td/fact-1    0
                                           td/org  ["A" "B"]
                                           "org2" "C"
                                           "layout"        {"color" "#033579"
                                                            "id"    1}}]}]
   [{td/org "CC"} {{"org2" "B"} [{td/country          td/country-c
                                  "date"             "1997-05-04"
                                  "keine_fact-1" 16
                                  td/org     "CC"
                                  "org2"    "B"
                                  "layout"           {"color" nil,
                                                      "id"    nil}}]}]])

(def sub-group-by-with-aggregations-inner
  [[{td/org "CC"} [[{"org2" "B"} [{td/country          td/country-c
                                   "date"             "1997-05-04"
                                   "keine_fact-1" 16
                                   td/org     "CC"
                                   "org2"    "B"
                                   "layout"           {"color" nil
                                                       "id"     nil}}]]]]
   [{td/org "C"}  [[{"org2" "D"
                     "aggregated-value" 18} [{td/country       td/country-a
                                              "date"          "1997-05-04"
                                              td/fact-1    18
                                              td/org  "C"
                                              "org2" ["A" "D"]
                                              "layout"        {"color" "#4fb34f"
                                                               "id"    1}}]]
                   [{"org2" "A"
                     "aggregated-value" 12} [{td/country       td/country-a
                                              "date"          "1997-05-01"
                                              td/fact-1    -6
                                              td/org  ["B" "C"]
                                              "org2" ["A" "C"]
                                              "layout"        {"color" nil
                                                               "id"    nil}}
                                             {td/country       td/country-a
                                              "date"          "1997-05-04"
                                              td/fact-1    18
                                              td/org  "C"
                                              "org2" ["A" "D"]
                                              "layout"        {"color" "#4fb34f"
                                                               "id"    1}}]]
                   [{"org2" "C"
                     "aggregated-value" -6} [{td/country       td/country-a
                                              "date"          "1997-05-01"
                                              td/fact-1    -6
                                              td/org  ["B" "C"]
                                              "org2" ["A" "C"]
                                              "layout"        {"color" nil
                                                               "id"    nil}}]]]]
   [{td/org "B"}  [[{"org2" "A"
                     "aggregated-value" 6} [{td/country       td/country-a
                                             "date"          "1997-01-01"
                                             td/fact-1    12
                                             td/org  ["A" "B"]
                                             "org2" "A"
                                             "layout"        {"color" "#4fb34f"
                                                              "id"    1}}
                                            {td/country       td/country-a
                                             "date"          "1997-05-01"
                                             td/fact-1    -6
                                             td/org  ["B" "C"]
                                             "org2" ["A" "C"]
                                             "layout"        {"color" nil
                                                              "id"    nil}}]]
                   [{"org2" "C"
                     "aggregated-value" -6} [{td/country       td/country-a
                                              "date"          "1997-05-01"
                                              td/fact-1    -6
                                              td/org  ["B" "C"]
                                              "org2" ["A" "C"]
                                              "layout"        {"color" nil
                                                               "id"    nil}}
                                             {td/country       td/country-c
                                              "date"          "1998-01-04"
                                              td/fact-1    0
                                              td/org  ["A" "B"]
                                              "org2" "C"
                                              "layout"        {"color" "#033579"
                                                               "id"    1}}]]]]
   [{td/org "A"}  [[{"org2" "D"
                     "aggregated-value" 100} [{td/country       td/country-c
                                               "date"          "1998-01-04"
                                               td/fact-1    100
                                               td/org  "A"
                                               "org2" "D"
                                               "layout"        {"color" "#e33b3b"
                                                                "id"    1}}]]
                   [{"org2" "A"
                     "aggregated-value" 12} [{td/country       td/country-a
                                              "date"          "1997-01-01"
                                              td/fact-1    12
                                              td/org  ["A" "B"]
                                              "org2" "A"
                                              "layout"        {"color" "#4fb34f"
                                                               "id"    1}}]]
                   [{"org2" "C"
                     "aggregated-value" 0} [{td/country       td/country-c
                                             "date"          "1998-01-04"
                                             td/fact-1    0
                                             td/org  ["A" "B"]
                                             "org2" "C"
                                             "layout"        {"color" "#033579"
                                                              "id"    1}}]]]]])

(def sub-group-by-with-aggregations-both
  [[{td/org "C"
     "aggregated-value" 24} [[{"org2" "D"
                               "aggregated-value" 18} [{td/country       td/country-a
                                                        "date"          "1997-05-04"
                                                        td/fact-1    18
                                                        td/org  "C"
                                                        "org2" ["A" "D"]
                                                        "layout"        {"color" "#4fb34f"
                                                                         "id"    1}}]]
                             [{"org2" "A"
                               "aggregated-value" 12} [{td/country       td/country-a
                                                        "date"          "1997-05-01"
                                                        td/fact-1    -6
                                                        td/org  ["B" "C"]
                                                        "org2" ["A" "C"]
                                                        "layout"        {"color" nil
                                                                         "id"    nil}}
                                                       {td/country       td/country-a
                                                        "date"          "1997-05-04"
                                                        td/fact-1    18
                                                        td/org  "C"
                                                        "org2" ["A" "D"]
                                                        "layout"        {"color" "#4fb34f"
                                                                         "id"    1}}]]
                             [{"org2" "C"
                               "aggregated-value" -6} [{td/country       td/country-a
                                                        "date"          "1997-05-01"
                                                        td/fact-1    -6
                                                        td/org  ["B" "C"]
                                                        "org2" ["A" "C"]
                                                        "layout"        {"color" nil
                                                                         "id"    nil}}]]]]
   [{td/org "A"
     "aggregated-value" 12} [[{"org2" "A"
                               "aggregated-value" 12} [{td/country       td/country-a
                                                        "date"          "1997-01-01"
                                                        td/fact-1    12
                                                        td/org  ["A" "B"]
                                                        "org2" "A"
                                                        "layout"        {"color" "#4fb34f"
                                                                         "id"    1}}]]]]
   [{td/org "B"
     "aggregated-value" 0} [[{"org2" "A"
                              "aggregated-value" 6} [{td/country       td/country-a
                                                      "date"          "1997-01-01"
                                                      td/fact-1    12
                                                      td/org  ["A" "B"]
                                                      "org2" "A"
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-01"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}]]
                            [{"org2" "C"
                              "aggregated-value" -6} [{td/country       td/country-a
                                                       "date"          "1997-05-01"
                                                       td/fact-1    -6
                                                       td/org  ["B" "C"]
                                                       "org2" ["A" "C"]
                                                       "layout"        {"color" nil
                                                                        "id"    nil}}]]]]])
(def sub-group-sorted-by-average-fact-1
  [[{td/org "CC"} [[{"org2" "B"}
                    [{td/country td/country-c,
                      "date" "1997-05-04",
                      "keine_fact-1" 16,
                      td/org "CC",
                      "org2" "B",
                      "layout" {"color" nil,
                                "id" nil}}
                     {td/country td/country-c,
                      "date" "1997-05-05",
                      "keine_fact-1" 16,
                      td/org "CC",
                      "org2" "B",
                      "layout" {"color" nil,
                                "id" nil}}]]]]
   [{td/org "C"} [[{"org2" "D",
                    "aggregated-value" 18.0}
                   [{td/country td/country-a,
                     "date" "1997-05-04",
                     td/fact-1 18,
                     td/org "C",
                     "org2" ["A" "D"],
                     "layout" {"color" "#4fb34f",
                               "id" 1}}
                    {td/country td/country-a,
                     "date" "1997-05-05",
                     td/fact-1 18,
                     td/org "C",
                     "org2" ["A" "D"],
                     "layout" {"color" "#4fb34f",
                               "id" 1}}]]
                  [{"org2" "A",
                    "aggregated-value" 6.0}
                   [{td/country td/country-a,
                     "date" "1997-05-01",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}
                    {td/country td/country-a,
                     "date" "1997-05-04",
                     td/fact-1 18,
                     td/org "C",
                     "org2" ["A" "D"],
                     "layout" {"color" "#4fb34f",
                               "id" 1}}
                    {td/country td/country-a,
                     "date" "1997-05-02",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}
                    {td/country td/country-a,
                     "date" "1997-05-05",
                     td/fact-1 18,
                     td/org "C",
                     "org2" ["A" "D"],
                     "layout" {"color" "#4fb34f",
                               "id" 1}}]]
                  [{"org2" "C",
                    "aggregated-value" -6.0}
                   [{td/country td/country-a,
                     "date" "1997-05-01",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}
                    {td/country td/country-a,
                     "date" "1997-05-02",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}]]]]
   [{td/org "B"} [[{"org2" "A",
                    "aggregated-value" 3.0}
                   [{td/country td/country-a,
                     "date" "1997-01-01",
                     td/fact-1 12,
                     td/org ["A" "B"],
                     "org2" "A",
                     "layout" {"color" "#4fb34f",
                               "id" 1}}
                    {td/country td/country-a,
                     "date" "1997-05-01",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}
                    {td/country td/country-a,
                     "date" "1997-11-02",
                     td/fact-1 12,
                     td/org ["A" "B"],
                     "org2" "A",
                     "layout" {"color" "#4fb34f",
                               "id" 1}}
                    {td/country td/country-a,
                     "date" "1997-05-02",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}]]
                  [{"org2" "C",
                    "aggregated-value" -3.0}
                   [{td/country td/country-a,
                     "date" "1997-05-01",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}
                    {td/country td/country-c,
                     "date" "1998-01-04",
                     td/fact-1 0,
                     td/org ["A" "B"],
                     "org2" "C",
                     "layout" {"color" "#033579",
                               "id" 1}}
                    {td/country td/country-a,
                     "date" "1997-05-02",
                     td/fact-1 -6,
                     td/org ["B" "C"],
                     "org2" ["A" "C"],
                     "layout" {"color" nil,
                               "id" nil}}
                    {td/country td/country-c,
                     "date" "1998-09-05",
                     td/fact-1 0,
                     td/org ["A" "B"],
                     "org2" "C",
                     "layout" {"color" "#033579",
                               "id" 1}}]]]]
   [{td/org "A"} [[{"org2" "D",
                    "aggregated-value" 100.0}
                   [{td/country td/country-c,
                     "date" "1998-12-04",
                     td/fact-1 100,
                     td/org "A",
                     "org2" "D",
                     "layout" {"color" "#e33b3b",
                               "id" 1}}
                    {td/country td/country-c,
                     "date" "1998-01-05",
                     td/fact-1 100,
                     td/org "A",
                     "org2" "D",
                     "layout" {"color" "#e33b3b",
                               "id" 1}}]]
                  [{"org2" "A",
                    "aggregated-value" 12.0}
                   [{td/country td/country-a,
                     "date" "1997-01-01",
                     td/fact-1 12,
                     td/org ["A" "B"],
                     "org2" "A",
                     "layout" {"color" "#4fb34f",
                               "id" 1}}
                    {td/country td/country-a,
                     "date" "1997-11-02",
                     td/fact-1 12,
                     td/org ["A" "B"],
                     "org2" "A",
                     "layout" {"color" "#4fb34f",
                               "id" 1}}]]
                  [{"org2" "C",
                    "aggregated-value" 0.0}
                   [{td/country td/country-c,
                     "date" "1998-01-04",
                     td/fact-1 0,
                     td/org ["A" "B"],
                     "org2" "C",
                     "layout" {"color" "#033579",
                               "id" 1}}
                    {td/country td/country-c,
                     "date" "1998-09-05",
                     td/fact-1 0,
                     td/org ["A" "B"],
                     "org2" "C",
                     "layout" {"color" "#033579",
                               "id" 1}}]]]]])

(def group-by-with-sort-events
  [[{"org2"    "D"
     "aggregated-value" 236} [{td/country       td/country-c
                               "date"          "1998-12-04"
                               td/fact-1    100
                               td/org  "A"
                               "org2" "D"
                               "layout"        {"color" "#e33b3b"
                                                "id"    1}}
                              {td/country       td/country-c
                               "date"          "1998-01-05"
                               td/fact-1    100
                               td/org  "A"
                               "org2" "D"
                               "layout"        {"color" "#e33b3b"
                                                "id"    1}}
                              {td/country       td/country-a
                               "date"          "1997-05-05"
                               td/fact-1    18
                               td/org  "C"
                               "org2" ["A" "D"]
                               "layout"        {"color" "#4fb34f"
                                                "id"    1}}
                              {td/country       td/country-a
                               "date"          "1997-05-04"
                               td/fact-1    18
                               td/org  "C"
                               "org2" ["A" "D"]
                               "layout"        {"color" "#4fb34f"
                                                "id"    1}}]]
   [{"org2"    "A"
     "aggregated-value" 48} [{td/country       td/country-a
                              "date"          "1997-11-02"
                              td/fact-1    12
                              td/org  ["A" "B"]
                              "org2" "A"
                              "layout"        {"color" "#4fb34f"
                                               "id"    1}}
                             {td/country       td/country-a
                              "date"          "1997-05-05"
                              td/fact-1    18
                              td/org  "C"
                              "org2" ["A" "D"]
                              "layout"        {"color" "#4fb34f"
                                               "id"    1}}
                             {td/country       td/country-a
                              "date"          "1997-05-04"
                              td/fact-1    18
                              td/org  "C"
                              "org2" ["A" "D"]
                              "layout"        {"color" "#4fb34f"
                                               "id"    1}}
                             {td/country       td/country-a
                              "date"          "1997-05-02"
                              td/fact-1    -6
                              td/org  ["B" "C"]
                              "org2" ["A" "C"]
                              "layout"        {"color" nil
                                               "id"    nil}}
                             {td/country       td/country-a
                              "date"          "1997-05-01"
                              td/fact-1    -6
                              td/org  ["B" "C"]
                              "org2" ["A" "C"]
                              "layout"        {"color" nil
                                               "id"    nil}}
                             {td/country       td/country-a
                              "date"          "1997-01-01"
                              td/fact-1    12
                              td/org  ["A" "B"]
                              "org2" "A"
                              "layout"        {"color" "#4fb34f"
                                               "id"    1}}]]
   [{"org2"    "C"
     "aggregated-value" -12} [{td/country       td/country-c
                               "date"          "1998-09-05"
                               td/fact-1    0
                               td/org  ["A" "B"]
                               "org2" "C"
                               "layout"        {"color" "#033579"
                                                "id"    1}}
                              {td/country       td/country-c
                               "date"          "1998-01-04"
                               td/fact-1    0
                               td/org  ["A" "B"]
                               "org2" "C"
                               "layout"        {"color" "#033579"
                                                "id"    1}}
                              {td/country       td/country-a
                               "date"          "1997-05-02"
                               td/fact-1    -6
                               td/org  ["B" "C"]
                               "org2" ["A" "C"]
                               "layout"        {"color" nil
                                                "id"    nil}}
                              {td/country       td/country-a
                               "date"          "1997-05-01"
                               td/fact-1    -6
                               td/org  ["B" "C"]
                               "org2" ["A" "C"]
                               "layout"        {"color" nil
                                                "id"    nil}}]]
   [{"org2"    "B"} [{td/country          td/country-c
                      "date"             "1997-05-05"
                      "keine_fact-1" 16
                      td/org     "CC"
                      "org2"    "B"
                      "layout"           {"color" nil
                                          "id"    nil}}
                     {td/country          td/country-c
                      "date"             "1997-05-04"
                      "keine_fact-1" 16
                      td/org     "CC"
                      "org2"    "B"
                      "layout"           {"color" nil
                                          "id"    nil}}]]])

(def group-by-month-with-sort-events
  [[{"month" "12"} [{td/country       td/country-c
                     "date"          "1998-12-04"
                     td/fact-1    100
                     td/org  "A"
                     "org2" "D"
                     "layout"        {"color" "#e33b3b"
                                      "id"    1}}]]
   [{"month" "11"} [{td/country       td/country-a
                     "date"          "1997-11-02"
                     td/fact-1    12
                     td/org  ["A" "B"]
                     "org2" "A"
                     "layout"        {"color" "#4fb34f"
                                      "id"    1}}]]
   [{"month" "09"} [{td/country       td/country-c
                     "date"          "1998-09-05"
                     td/fact-1    0
                     td/org  ["A" "B"]
                     "org2" "C"
                     "layout"        {"color" "#033579"
                                      "id"    1}}]]
   [{"month" "05"} [{td/country       td/country-a
                     "date"          "1997-05-05"
                     td/fact-1    18
                     td/org  "C"
                     "org2" ["A" "D"]
                     "layout"        {"color" "#4fb34f"
                                      "id"    1}}
                    {td/country          td/country-c
                     "date"             "1997-05-05"
                     "keine_fact-1" 16
                     td/org     "CC"
                     "org2"    "B"
                     "layout"           {"color" nil
                                         "id"    nil}}
                    {td/country       td/country-a
                     "date"          "1997-05-04"
                     td/fact-1    18
                     td/org  "C"
                     "org2" ["A" "D"]
                     "layout"        {"color" "#4fb34f"
                                      "id"    1}}
                    {td/country          td/country-c
                     "date"             "1997-05-04"
                     "keine_fact-1" 16
                     td/org     "CC"
                     "org2"    "B"
                     "layout"           {"color" nil
                                         "id"    nil}}
                    {td/country       td/country-a
                     "date"          "1997-05-02"
                     td/fact-1    -6
                     td/org  ["B" "C"]
                     "org2" ["A" "C"]
                     "layout"        {"color" nil
                                      "id"    nil}}
                    {td/country       td/country-a
                     "date"          "1997-05-01"
                     td/fact-1    -6
                     td/org  ["B" "C"]
                     "org2" ["A" "C"]
                     "layout"        {"color" nil
                                      "id"    nil}}]]
   [{"month" "01"} [{td/country       td/country-c
                     "date"          "1998-01-05"
                     td/fact-1    100
                     td/org  "A"
                     "org2" "D"
                     "layout"        {"color" "#e33b3b"
                                      "id"    1}}
                    {td/country       td/country-c
                     "date"          "1998-01-04"
                     td/fact-1    0
                     td/org  ["A" "B"]
                     "org2" "C"
                     "layout"        {"color" "#033579"
                                      "id"    1}}
                    {td/country       td/country-a
                     "date"          "1997-01-01"
                     td/fact-1    12
                     td/org  ["A" "B"]
                     "org2" "A"
                     "layout"        {"color" "#4fb34f"
                                      "id"    1}}]]])

(def group-by-weeks-with-sort-events
  [[{"week" "49"}
    [{td/country td/country-c,
      "date" "1998-12-04",
      td/fact-1 100,
      "layout" {"color" "#e33b3b", "id" 1},
      td/org "A",
      "org2" "D"}]]
   [{"week" "44"}
    [{td/country td/country-a,
      "date" "1997-11-02",
      td/fact-1 12,
      "layout" {"color" "#4fb34f", "id" 1},
      td/org ["A" "B"],
      "org2" "A"}]]
   [{"week" "36"}
    [{td/country td/country-c,
      "date" "1998-09-05",
      td/fact-1 0,
      "layout" {"color" "#033579", "id" 1},
      td/org ["A" "B"],
      "org2" "C"}]]
   [{"week" "2"}
    [{td/country td/country-c,
      "date" "1998-01-05",
      td/fact-1 100,
      "layout" {"color" "#e33b3b", "id" 1},
      td/org "A",
      "org2" "D"}]]
   [{"week" "19"}
    [{td/country td/country-a,
      "date" "1997-05-05",
      td/fact-1 18,
      "layout" {"color" "#4fb34f", "id" 1},
      td/org "C",
      "org2" ["A" "D"]}
     {td/country td/country-c,
      "date" "1997-05-05",
      "keine_fact-1" 16,
      "layout" {"color" nil, "id" nil},
      td/org "CC",
      "org2" "B"}]]
   [{"week" "18"}
    [{td/country td/country-a,
      "date" "1997-05-04",
      td/fact-1 18,
      "layout" {"color" "#4fb34f", "id" 1},
      td/org "C",
      "org2" ["A" "D"]}
     {td/country td/country-c,
      "date" "1997-05-04",
      "keine_fact-1" 16,
      "layout" {"color" nil, "id" nil},
      td/org "CC",
      "org2" "B"}
     {td/country td/country-a,
      "date" "1997-05-02",
      td/fact-1 -6,
      "layout" {"color" nil, "id" nil},
      td/org ["B" "C"],
      "org2" ["A" "C"]}
     {td/country td/country-a,
      "date" "1997-05-01",
      td/fact-1 -6,
      "layout" {"color" nil, "id" nil},
      td/org ["B" "C"],
      "org2" ["A" "C"]}]]
   [{"week" "1"}
    [{td/country td/country-c,
      "date" "1998-01-04",
      td/fact-1 0,
      "layout" {"color" "#033579", "id" 1},
      td/org ["A" "B"],
      "org2" "C"}
     {td/country td/country-a,
      "date" "1997-01-01",
      td/fact-1 12,
      "layout" {"color" "#4fb34f", "id" 1},
      td/org ["A" "B"],
      "org2" "A"}]]])

(def group-by-weekday-with-sort-events
  [[{"weekday" "7"}
    [{td/country td/country-c,
      "date" "1998-01-04",
      td/fact-1 0,
      td/org ["A" "B"],
      "org2" "C",
      "layout" {"color" "#033579", "id" 1}}
     {td/country td/country-a,
      "date" "1997-11-02",
      td/fact-1 12,
      td/org ["A" "B"],
      "org2" "A",
      "layout" {"color" "#4fb34f", "id" 1}}
     {td/country td/country-a,
      "date" "1997-05-04",
      td/fact-1 18,
      td/org "C",
      "org2" ["A" "D"],
      "layout" {"color" "#4fb34f", "id" 1}}
     {td/country td/country-c,
      "date" "1997-05-04",
      "keine_fact-1" 16,
      td/org "CC",
      "org2" "B",
      "layout" {"color" nil, "id" nil}}]]
   [{"weekday" "6"}
    [{td/country td/country-c,
      "date" "1998-09-05",
      td/fact-1 0,
      td/org ["A" "B"],
      "org2" "C",
      "layout" {"color" "#033579", "id" 1}}]]
   [{"weekday" "5"}
    [{td/country td/country-c,
      "date" "1998-12-04",
      td/fact-1 100,
      td/org "A",
      "org2" "D",
      "layout" {"color" "#e33b3b", "id" 1}}
     {td/country td/country-a,
      "date" "1997-05-02",
      td/fact-1 -6,
      td/org ["B" "C"],
      "org2" ["A" "C"],
      "layout" {"color" nil, "id" nil}}]]
   [{"weekday" "4"}
    [{td/country td/country-a,
      "date" "1997-05-01",
      td/fact-1 -6,
      td/org ["B" "C"],
      "org2" ["A" "C"],
      "layout" {"color" nil, "id" nil}}]]
   [{"weekday" "3"}
    [{td/country td/country-a,
      "date" "1997-01-01",
      td/fact-1 12,
      td/org ["A" "B"],
      "org2" "A",
      "layout" {"color" "#4fb34f", "id" 1}}]]
   [{"weekday" "1"}
    [{td/country td/country-c,
      "date" "1998-01-05",
      td/fact-1 100,
      td/org "A",
      "org2" "D",
      "layout" {"color" "#e33b3b", "id" 1}}
     {td/country td/country-a,
      "date" "1997-05-05",
      td/fact-1 18,
      td/org "C",
      "org2" ["A" "D"],
      "layout" {"color" "#4fb34f", "id" 1}}
     {td/country td/country-c,
      "date" "1997-05-05",
      "keine_fact-1" 16,
      td/org "CC",
      "org2" "B",
      "layout" {"color" nil, "id" nil}}]]])

(def sub-group-sorted-by-number
  [[{td/org     "B"
     "aggregated-value" 8} [[{"org2"    "A"
                              "aggregated-value" 4} [{td/country       td/country-a
                                                      "date"          "1997-01-01"
                                                      td/fact-1    12
                                                      td/org  ["A" "B"]
                                                      "org2" "A"
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-01"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-11-02"
                                                      td/fact-1    12
                                                      td/org  ["A" "B"]
                                                      "org2" "A"
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-02"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}]]
                            [{"org2"    "C"
                              "aggregated-value" 4} [{td/country       td/country-a
                                                      "date"          "1997-05-01"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}
                                                     {td/country       td/country-c
                                                      "date"          "1998-01-04"
                                                      td/fact-1    0
                                                      td/org  ["A" "B"]
                                                      "org2" "C"
                                                      "layout"        {"color" "#033579"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-02"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}
                                                     {td/country       td/country-c
                                                      "date"          "1998-09-05"
                                                      td/fact-1    0
                                                      td/org  ["A" "B"]
                                                      "org2" "C"
                                                      "layout"        {"color" "#033579"
                                                                       "id"    1}}]]]]
   [{td/org     "C"
     "aggregated-value" 8} [[{"org2"    "C"
                              "aggregated-value" 2} [{td/country       td/country-a
                                                      "date"          "1997-05-01"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-02"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}]]
                            [{"org2"    "D"
                              "aggregated-value" 2} [{td/country       td/country-a
                                                      "date"          "1997-05-04"
                                                      td/fact-1    18
                                                      td/org  "C"
                                                      "org2" ["A" "D"]
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-05"
                                                      td/fact-1    18
                                                      td/org  "C"
                                                      "org2" ["A" "D"]
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}]]
                            [{"org2"    "A"
                              "aggregated-value" 4} [{td/country       td/country-a
                                                      "date"          "1997-05-01"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-04"
                                                      td/fact-1    18
                                                      td/org  "C"
                                                      "org2" ["A" "D"]
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-02"
                                                      td/fact-1    -6
                                                      td/org  ["B" "C"]
                                                      "org2" ["A" "C"]
                                                      "layout"        {"color" nil
                                                                       "id"    nil}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-05-05"
                                                      td/fact-1    18
                                                      td/org  "C"
                                                      "org2" ["A" "D"]
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}]]]]
   [{td/org     "A"
     "aggregated-value" 6} [[{"org2"    "A"
                              "aggregated-value" 2} [{td/country       td/country-a
                                                      "date"          "1997-01-01"
                                                      td/fact-1    12
                                                      td/org  ["A" "B"]
                                                      "org2" "A"
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}
                                                     {td/country       td/country-a
                                                      "date"          "1997-11-02"
                                                      td/fact-1    12
                                                      td/org  ["A" "B"]
                                                      "org2" "A"
                                                      "layout"        {"color" "#4fb34f"
                                                                       "id"    1}}]]
                            [{"org2"    "D"
                              "aggregated-value" 2} [{td/country       td/country-c
                                                      "date"          "1998-12-04"
                                                      td/fact-1    100
                                                      td/org  "A"
                                                      "org2" "D"
                                                      "layout"        {"color" "#e33b3b"
                                                                       "id"    1}}
                                                     {td/country       td/country-c
                                                      "date"          "1998-01-05"
                                                      td/fact-1    100
                                                      td/org  "A"
                                                      "org2" "D"
                                                      "layout"        {"color" "#e33b3b"
                                                                       "id"    1}}]]
                            [{"org2"    "C"
                              "aggregated-value" 2} [{td/country       td/country-c
                                                      "date"          "1998-01-04"
                                                      td/fact-1    0
                                                      td/org  ["A" "B"]
                                                      "org2" "C"
                                                      "layout"        {"color" "#033579"
                                                                       "id"    1}}
                                                     {td/country       td/country-c
                                                      "date"          "1998-09-05"
                                                      td/fact-1    0
                                                      td/org  ["A" "B"]
                                                      "org2" "C"
                                                      "layout"        {"color" "#033579"
                                                                       "id"    1}}]]]]
   [{td/org     "CC"
     "aggregated-value" 2} [[{"org2"    "B"
                              "aggregated-value" 2} [{td/country          td/country-c
                                                      "date"             "1997-05-04"
                                                      "keine_fact-1" 16
                                                      td/org     "CC"
                                                      "org2"    "B"
                                                      "layout"           {"color" nil
                                                                          "id"    nil}}
                                                     {td/country          td/country-c
                                                      "date"             "1997-05-05"
                                                      "keine_fact-1" 16
                                                      td/org     "CC"
                                                      "org2"    "B"
                                                      "layout"           {"color" nil
                                                                          "id"    nil}}]]]]])

(def group-sorted-by-number
  [[{td/org     "A"
     "aggregated-value" 6} [{td/country       td/country-a
                             "date"          "1997-01-01"
                             td/fact-1    12
                             td/org  ["A" "B"]
                             "org2" "A"
                             "layout"        {"color" "#4fb34f"
                                              "id"    1}}
                            {td/country       td/country-a
                             "date"          "1997-11-02"
                             td/fact-1    12
                             td/org  ["A" "B"]
                             "org2" "A"
                             "layout"        {"color" "#4fb34f"
                                              "id"    1}}
                            {td/country       td/country-c
                             "date"          "1998-01-04"
                             td/fact-1    0
                             td/org  ["A" "B"]
                             "org2" "C"
                             "layout"        {"color" "#033579"
                                              "id"    1}}
                            {td/country       td/country-c
                             "date"          "1998-01-05"
                             td/fact-1    100
                             td/org  "A"
                             "org2" "D"
                             "layout"        {"color" "#e33b3b"
                                              "id"    1}}
                            {td/country       td/country-c
                             "date"          "1998-09-05"
                             td/fact-1    0
                             td/org  ["A" "B"]
                             "org2" "C"
                             "layout"        {"color" "#033579"
                                              "id"    1}}
                            {td/country       td/country-c
                             "date"          "1998-12-04"
                             td/fact-1    100
                             td/org  "A"
                             "org2" "D"
                             "layout"        {"color" "#e33b3b"
                                              "id"    1}}]]
   [{td/org     "B"
     "aggregated-value" 6} [{td/country       td/country-a
                             "date"          "1997-01-01"
                             td/fact-1    12
                             td/org  ["A" "B"]
                             "org2" "A"
                             "layout"        {"color" "#4fb34f"
                                              "id"    1}}
                            {td/country       td/country-a
                             "date"          "1997-05-01"
                             td/fact-1    -6
                             td/org  ["B" "C"]
                             "org2" ["A" "C"]
                             "layout"        {"color" nil
                                              "id"    nil}}
                            {td/country       td/country-a
                             "date"          "1997-05-02"
                             td/fact-1    -6
                             td/org  ["B" "C"]
                             "org2" ["A" "C"]
                             "layout"        {"color" nil
                                              "id"    nil}}
                            {td/country       td/country-a
                             "date"          "1997-11-02"
                             td/fact-1    12
                             td/org  ["A" "B"]
                             "org2" "A"
                             "layout"        {"color" "#4fb34f"
                                              "id"    1}}
                            {td/country       td/country-c
                             "date"          "1998-01-04"
                             td/fact-1    0
                             td/org  ["A" "B"]
                             "org2" "C"
                             "layout"        {"color" "#033579"
                                              "id"    1}}
                            {td/country       td/country-c
                             "date"          "1998-09-05"
                             td/fact-1    0
                             td/org  ["A" "B"]
                             "org2" "C"
                             "layout"        {"color" "#033579"
                                              "id"    1}}]]
   [{td/org     "C"
     "aggregated-value" 4} [{td/country       td/country-a
                             "date"          "1997-05-01"
                             td/fact-1    -6
                             td/org  ["B" "C"]
                             "org2" ["A" "C"]
                             "layout"        {"color" nil
                                              "id"    nil}}
                            {td/country       td/country-a
                             "date"          "1997-05-02"
                             td/fact-1    -6
                             td/org  ["B" "C"]
                             "org2" ["A" "C"]
                             "layout"        {"color" nil
                                              "id"    nil}}
                            {td/country       td/country-a
                             "date"          "1997-05-04"
                             td/fact-1    18
                             td/org  "C"
                             "org2" ["A" "D"]
                             "layout"        {"color" "#4fb34f"
                                              "id"    1}}
                            {td/country       td/country-a
                             "date"          "1997-05-05"
                             td/fact-1    18
                             td/org  "C"
                             "org2" ["A" "D"]
                             "layout"        {"color" "#4fb34f"
                                              "id"    1}}]]
   [{td/org     "CC"
     "aggregated-value" 2} [{td/country          td/country-c
                             "date"             "1997-05-04"
                             "keine_fact-1" 16
                             td/org     "CC"
                             "org2"    "B"
                             "layout"           {"color" nil
                                                 "id"    nil}}
                            {td/country          td/country-c
                             "date"             "1997-05-05"
                             "keine_fact-1" 16
                             td/org     "CC"
                             "org2"    "B"
                             "layout"           {"color" nil
                                                 "id"    nil}}]]])

(t/deftest sub-group-by-with-aggregations-test
  (t/testing "sort groups by fact-1 (sum) for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute td/fact-1
                                              :attribute-types {td/fact-1 :number}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0
                                              :aggregate [:sum {:attribute td/fact-1
                                                                :join? true}]}
                                    [:group-by {:attributes ["org2"] :mode :keep}
                                     [:group-by {:attributes [td/org] :mode :keep}
                                      [:apply-layout {:layouts [base-layout-1]} "di-1"]]]])
             sub-group-by-with-aggregations-outer)))
  (t/testing "sort sub groups by fact-1 (sum) for sub grouped data and sort events by date"
    (t/is (= (of/perform-operation {"di-1" input-data} {}
                                   [:sort-by {:attribute td/org
                                              :attribute-types {td/org :string}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0}
                                    [:sort-by {:attribute td/fact-1
                                               :attribute-types {td/fact-1 :number}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 1
                                               :aggregate [:sum {:attribute td/fact-1}]}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:group-by {:attributes [td/org] :mode :keep}
                                       [:apply-layout {:layouts [base-layout-1]} "di-1"]]]]])
             sub-group-by-with-aggregations-inner)))
  (t/testing "sort sub groups by org for sub grouped data with filter"
    (t/is (= (of/perform-operation {"di-1" input-data}
                                   {"f1" [:and [:or {:de.explorama.shared.data-format.filter/op :=,
                                                     :de.explorama.shared.data-format.filter/prop td/country,
                                                     :de.explorama.shared.data-format.filter/value td/country-a}]]}
                                   [:sort-by {:attribute td/fact-1
                                              :attribute-types {td/fact-1 :number}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0
                                              :aggregate [:sum {:attribute td/fact-1
                                                                :join? true}]}
                                    [:sort-by {:attribute td/fact-1
                                               :attribute-types {td/fact-1 :number}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 1
                                               :aggregate [:sum {:attribute td/fact-1}]}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:group-by {:attributes [td/org] :mode :keep}
                                       [:apply-layout {:layouts [base-layout-1]}
                                        [:filter "f1" "di-1"]]]]]])
             sub-group-by-with-aggregations-both)))
  (t/testing "group by org2 and sort events"
    (t/is (= (of/perform-operation {"di-1" input-data-2}
                                   {}
                                   [:sort-by {:attribute "date"
                                              :attribute-types {"date" :date}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :events}
                                    [:sort-by {:attribute td/fact-1
                                               :attribute-types {td/fact-1 :number}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 0
                                               :aggregate [:sum {:attribute td/fact-1}]}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:apply-layout {:layouts [base-layout-1]}
                                       "di-1"]]]])
             group-by-with-sort-events)))
  (t/testing "group by month and sort events"
    (t/is (= (of/perform-operation {"di-1" input-data-2}
                                   {}
                                   [:sort-by {:attribute "date"
                                              :attribute-types {"date" :date}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :events}
                                    [:sort-by {:attribute "month"
                                               :attribute-types {"month" :date}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 0}
                                     [:group-by {:attributes ["month"]
                                                 :mode :keep
                                                 :ignore-hierarchy? true}
                                      [:apply-layout {:layouts [base-layout-1]}
                                       "di-1"]]]])
             group-by-month-with-sort-events)))
  (t/testing "group by week and sort events"
    (t/is (= (of/perform-operation {"di-1" input-data-2}
                                   {}
                                   [:sort-by {:attribute "date"
                                              :attribute-types {"date" :date}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :events}
                                    [:sort-by {:attribute "week"
                                               :attribute-types {"week" :date}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 0}
                                     [:group-by {:attributes ["week"]
                                                 :mode :keep
                                                 :ignore-hierarchy? true}
                                      [:apply-layout {:layouts [base-layout-1]}
                                       "di-1"]]]])
             group-by-weeks-with-sort-events)))
  (t/testing "group by weekday and sort events"
    (t/is (= (of/perform-operation {"di-1" input-data-2}
                                   {}
                                   [:sort-by {:attribute "date"
                                              :attribute-types {"date" :date}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :events}
                                    [:sort-by {:attribute "weekday"
                                               :attribute-types {"weekday" :date}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 0}
                                     [:group-by {:attributes ["weekday"]
                                                 :mode :keep
                                                 :ignore-hierarchy? true}
                                      [:apply-layout {:layouts [base-layout-1]}
                                       "di-1"]]]])
             group-by-weekday-with-sort-events)))
  (t/testing "group by org with sort groups by average fact-1 for grouped data"
    (t/is (=
           (of/perform-operation {"di-1" input-data} {}
                                 [:sort-by {:attribute td/fact-1
                                            :attribute-types {td/fact-1 :number}
                                            :direction :desc
                                            :return-map? false
                                            :apply-to :group
                                            :aggregate [:average {:attribute td/fact-1}]}
                                  [:group-by {:attributes [td/org] :mode :keep}
                                   [:apply-layout {:layouts [base-layout-1]} "di-1"]]])
           sort-groups-by-average-fact-1)))
  (t/testing "sort groups by fact-1 (average) for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data-2} {}
                                   [:sort-by {:attribute td/org
                                              :attribute-types {td/org :string}
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :level 0}
                                    [:sort-by {:attribute td/fact-1
                                               :attribute-types {td/fact-1 :number}
                                               :direction :desc
                                               :return-map? false
                                               :apply-to :group
                                               :level 1
                                               :aggregate [:average {:attribute td/fact-1}]}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:group-by {:attributes [td/org] :mode :keep}
                                       [:apply-layout {:layouts [base-layout-1]} "di-1"]]]]])
             sub-group-sorted-by-average-fact-1)))
  (t/testing "sort groups by number of events for sub grouped data"
    (t/is (= (of/perform-operation {"di-1" input-data-2} {}
                                   [:sort-by {:attribute nil
                                              :direction :desc
                                              :return-map? false
                                              :apply-to :group
                                              :aggregate [:count-events {:join? true}]
                                              :level 0}
                                    [:sort-by {:attribute nil
                                               :direction :asc
                                               :return-map? false
                                               :apply-to :group
                                               :level 1
                                               :aggregate [:count-events {}]}
                                     [:group-by {:attributes ["org2"] :mode :keep}
                                      [:group-by {:attributes [td/org] :mode :keep}
                                       [:apply-layout {:layouts [base-layout-1]} "di-1"]]]]])
             sub-group-sorted-by-number)))
  (t/testing "sort groups by number of events for grouped data"
    (t/is (= (->> (of/perform-operation {"di-1" input-data-2} {}
                                        [:sort-by {:attribute "date"
                                                   :attribute-types {"date" :date}
                                                   :direction :asc
                                                   :return-map? false
                                                   :apply-to :events}
                                         [:sort-by {:attribute nil
                                                    :direction :desc
                                                    :return-map? false
                                                    :apply-to :group
                                                    :aggregate [:count-events {}]
                                                    :level 0}
                                          [:group-by {:attributes [td/org] :mode :keep}
                                           [:apply-layout {:layouts [base-layout-1]} "di-1"]]]])
                  (sort-by (fn [[key]]
                             (get key td/org)))
                  vec)
             group-sorted-by-number))))
