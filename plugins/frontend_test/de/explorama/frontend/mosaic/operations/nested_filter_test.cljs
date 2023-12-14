(ns de.explorama.frontend.mosaic.operations.nested-filter-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [de.explorama.frontend.mosaic.operations.util :as gou]))

(def test-data-grp-1
  #js [#js {:group-key? true
            :key        "root"}
       #js [#js [#js {:event-type "Category 2"
                      :key        "Category 2"
                      :attr       "category"
                      :group-key? true}
                 #js [#js ["#babab9" "DS-A-10815" "default" "baselayout1"]
                      #js ["#babab9" "DS-A-10746" "default" "baselayout1"]]]
            #js [#js {:event-type "Category 1"
                      :key        "Category 1"
                      :attr       "category"
                      :group-key? true}
                 #js [#js ["#babab9" "DS-A-10750" "default" "baselayout1"]
                      #js ["#babab9" "DS-A-10723" "default" "baselayout1"]]]]])

(def test-data-grp-2
  #js [#js {:group-key? true
            :key        "root"}
       #js [#js [#js {:event-type "Category 2"
                      :key        "Category 2"
                      :attr       "category"
                      :group-key? true}
                 #js [#js [#js {:tag        "Tag 1"
                                :key        "Tag 1"
                                :attr       "tag"
                                :group-key? true}
                           #js [#js ["#babab9" "DS-A-10821" "default" "baselayout1"]
                                #js ["#babab9" "DS-A-10746" "default" "baselayout1"]]]]]
            #js [#js {:event-type "Category 1"
                      :key        "Category 1"
                      :attr       "category"
                      :group-key? true}
                 #js [#js [#js {:tag        "Protest with intervention"
                                :key        "Protest with intervention"
                                :attr       "tag"
                                :group-key? true}
                           #js [#js ["#babab9" "DS-A-10816" "default" "baselayout1"]
                                #js ["#babab9" "DS-A-10737" "default" "baselayout1"]]]
                      #js [#js {:tag        "Tag 2"
                                :key        "Tag 2"
                                :attr       "tag"
                                :group-key? true}
                           #js [#js ["#babab9" "DS-A-10820" "default" "baselayout1"]
                                #js ["#babab9" "DS-A-10722" "default" "baselayout1"]]]]]]])

(deftest copy-group-filter-1
  (testing "Generates Filter for simple group-by"
    (is (= (@#'gou/nested-filter-copy [1 0] test-data-grp-1 nil)
           [:or {:data-format-lib.filter/op :=
                 :data-format-lib.filter/prop "category"
                 :data-format-lib.filter/value "Category 2"}]))
    (is (= (@#'gou/nested-filter-copy [1 1] test-data-grp-1 nil)
           [:or {:data-format-lib.filter/op :=
                 :data-format-lib.filter/prop "category"
                 :data-format-lib.filter/value "Category 1"}]))))

(deftest copy-group-filter-2
  (testing "Generates Filter for simple group-by"
    (is (= (@#'gou/nested-filter-copy [1 1 1 0] test-data-grp-2 nil)
           [:and
            [:or {:data-format-lib.filter/op :=
                  :data-format-lib.filter/prop "category"
                  :data-format-lib.filter/value "Category 1"}]
            [:or {:data-format-lib.filter/op :=
                  :data-format-lib.filter/prop "tag"
                  :data-format-lib.filter/value "Protest with intervention"}]]))
    (is (= (@#'gou/nested-filter-copy [1 0 1 0] test-data-grp-2 nil)
           [:and
            [:or {:data-format-lib.filter/op :=
                  :data-format-lib.filter/prop "category"
                  :data-format-lib.filter/value "Category 2"}]
            [:or {:data-format-lib.filter/op :=
                  :data-format-lib.filter/prop "tag"
                  :data-format-lib.filter/value "Tag 1"}]]))))


(def layouts-1
  [{:attribute-type "number",
    :attributes ["fact-1" "fact-1"],
    :card-scheme "scheme-1",
    :color-scheme {:color-scale-numbers 5,
                   :colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a", :3 "#fb8d02", :4 "#e33b3b"},
                   :id "colorscale3",
                   :name "Scale-2-5"},
    :field-assignments [["else" "date"] ["else" "datasource"] ["else" "category"] ["else" "fact-1"]
                        ["notes" "notes"] ["else" "country"] ["org" "org"] ["location" "location"]],
    :id "7c7b8c8f-b35f-4b83-8a57-47414f0a3e43",
    :name "colored",
    :timestamp 1660808682867,
    :value-assigned [[0 1] [1 2] [2 3] [3 4] [4 1000000]]}])


(def layouts-2
  [{:attribute-type "string",
    :attributes ["category"],
    :card-scheme "scheme-3",
    :color-scheme {:colors {:0 "#babab9", :1 "#5c5b5b", :2 "#50678a"}},
    :field-assignments [["else" "datasource"] ["else" "date"]],
    :id "3efd6006-e0db-4cc3-aea0-d21a76fd933e",
    :name "simple et",
    :timestamp 1660812104338,
    :value-assigned [["circus"] ["race"] ["theater"]]}])

(def layouts-3
   [{:attribute-type "string",
     :attributes ["category"],
     :card-scheme "scheme-3",
     :color-scheme {:colors {:0 "#5c5b5b"}},
     :field-assignments [["else" "datasource"] ["else" "date"]],
     :id "e8db826d-e3f7-42e1-8202-800dd29f78ba",
     :name "simple et (temp)",
     :temporary? true,
     :timestamp 1660812104338,
     :value-assigned [["race"]]}
    {:attribute-type "string",
     :attributes ["category"],
     :card-scheme "scheme-3",
     :color-scheme {:colors {:0 "#babab9"}},
     :field-assignments [["else" "datasource"] ["else" "date"]],
     :id "abf52861-aa74-4f44-bb9b-890656227336",
     :name "simple et (temp)",
     :temporary? true,
     :timestamp 1660812104338,
     :value-assigned [["circus"]]}])

(def layouts-4
  [{:attribute-type "number",
    :attributes ["fact-1" "fact-1"],
    :card-scheme "scheme-1",
    :color-scheme {:colors {:0 "#babab9", :1 "#5c5b5b"}},
    :field-assignments [["else" "date"] ["else" "datasource"] ["else" "category"] ["else" "fact-1"]
                        ["notes" "notes"] ["else" "country"] ["org" "org"] ["location" "location"]],
    :id "4152930c-206c-426c-8cea-c0c8960247c6",
    :name "colored (temp)",
    :temporary? true,
    :timestamp 1660808682867,
    :value-assigned [[0 1] [1 2]]}
   {:attribute-type "number",
    :attributes ["fact-1" "fact-1"],
    :card-scheme "scheme-1",
    :color-scheme {:colors {:0 "#fb8d02", :1 "#e33b3b"}},
    :field-assignments [["else" "date"] ["else" "datasource"] ["else" "category"] ["else" "fact-1"]
                        ["notes" "notes"] ["else" "country"] ["org" "org"] ["location" "location"]],
    :id "6d1cbf9e-bb32-499f-a71d-e353f8bc4958",
    :name "colored (temp)",
    :temporary? true,
    :timestamp 1660808682867,
    :value-assigned [[3 4] [4 1000000]]}])

(deftest generate-layout-based-filter-test
  (testing "Generate filters based on layout color"
    (is (= (gou/build-filter-entry
            #js {:color "#fb8d02", :id "7c7b8c8f-b35f-4b83-8a57-47414f0a3e43"} "layout"
            layouts-1 :=)
           [:or
            [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 3}
             #:data-format-lib.filter{:op :<, :prop "fact-1", :value 4}]
            [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 3}
             #:data-format-lib.filter{:op :<, :prop "fact-1", :value 4}]]))
    (is (=  (gou/build-filter-entry
             #js {:color "#fb8d02", :id "7c7b8c8f-b35f-4b83-8a57-47414f0a3e43"} "layout"
             layouts-1 :not=)
            [:or
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 3}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 4}]
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 3}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 4}]]))
    (is (= (gou/build-filter-entry
            "race" "category"  layouts-2 :=)
           [:or #:data-format-lib.filter{:op :=, :prop "category", :value "race"}]))
    (is (= (gou/build-filter-entry
            "race" "category"
            layouts-2 :not=)
           [:or #:data-format-lib.filter{:op :not=, :prop "category", :value "race"}]))
    (is (= (gou/build-filter-entry
            #js {:color nil, :id nil} "layout"
            layouts-2 :=)
           [:and
            [:and #:data-format-lib.filter{:op :not=, :prop "category", :value "circus"}]
            [:and #:data-format-lib.filter{:op :not=, :prop "category", :value "race"}]
            [:and #:data-format-lib.filter{:op :not=, :prop "category", :value "theater"}]]))
    (is (= (gou/build-filter-entry
            #js {:color nil, :id nil} "layout"
            layouts-2 :not=)
           [:or
            [:or #:data-format-lib.filter{:op :=, :prop "category", :value "circus"}]
            [:or #:data-format-lib.filter{:op :=, :prop "category", :value "race"}]
            [:or #:data-format-lib.filter{:op :=, :prop "category", :value "theater"}]]))

    (is (= (gou/build-filter-entry
            #js {:color nil, :id nil} "layout"
            layouts-3 :not=)
           [:or
            [:or #:data-format-lib.filter{:op :=, :prop "category", :value "race"}]
            [:or #:data-format-lib.filter{:op :=, :prop "category", :value "circus"}]]))

    (is (= (gou/build-filter-entry
            #js {:color nil, :id nil} "layout"
            layouts-4 :=)
           [:and
            [:or
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 0}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 1}]
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 0}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 1}]]
            [:or
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 1}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 2}]
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 1}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 2}]]
            [:or
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 3}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 4}]
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 3}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 4}]]
            [:or
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 4}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 1000000}]
             [:or #:data-format-lib.filter{:op :<, :prop "fact-1", :value 4}
              #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 1000000}]]]))
    (is (= (gou/build-filter-entry
            #js {:color nil, :id nil} "layout"
            layouts-4 :not=)
           [:or
            [:or
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 0}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 1}]
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 0}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 1}]]
            [:or
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 1}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 2}]
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 1}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 2}]]
            [:or
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 3}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 4}]
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 3}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 4}]]
            [:or
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 4}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 1000000}]
             [:and #:data-format-lib.filter{:op :>=, :prop "fact-1", :value 4}
              #:data-format-lib.filter{:op :<, :prop "fact-1", :value 1000000}]]]))))