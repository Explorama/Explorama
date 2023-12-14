(ns de.explorama.backend.map.overlayers-test
  (:require [clojure.test :refer [deftest is testing]]
            [de.explorama.shared.map.config :as config-shared-map]
            [de.explorama.backend.map.overlayers :refer [layers-data
                                                         renderable-layers]]
            [de.explorama.shared.common.data.attributes :as attrs]))

(def B-attr (attrs/access-key "B"))
(def country-attr (attrs/access-key "country"))
(def location-attr (attrs/access-key "location"))

(def empty-test-data [])
(def single-test-data [{country-attr "Germany" B-attr 2 location-attr [[0 0]]}])
(def test-data-without-attribute [{country-attr "Germany" B-attr 2 location-attr [[0 0]]}
                                  {country-attr "Germany" location-attr [[0 0]]}])
(def out-of-range-test-data [{country-attr "Germany" B-attr 5 location-attr [[0 0]]}])

(def color-coding-1
  ;; This is created from (mosaic.config-shared-map/color-coding "Scale-1-5" "integer" (vec (reverse [[0 1] [1 6] [6 20] [20 51] [51 100000]])))
  ;; then translated using code from map.views.config (see re-frame/reg-event-fx :map.views.config-shared-map/change-design)
  ;; TODO: extract cljc part from mosaic.config to be used in tests.
  {:0 {:from 0 :to "1"}, :1 {:from "1" :to "2"}, :2 {:from "2" :to "3"}, :3 {:from "3" :to "4"}, :4 {:from "4" :to "5"}})

(def color-map-1
  ;; this is created from (map.views.config-shared-map/lookup-color-scheme "Scale-1-5")
  ;; TODO: extract cljc part from map.views.config to be used in tests.
  ;; This would be lookup-color-scheme or the the color scheme directly.
  {:0 "#e33b3b", :1 "#fb8d02", :2 "#4fb34f", :3 "#0292b5", :4 "#033579"})

(def avg-layer {:name             "Average-Layer"
                :id "average"
                :type             :feature
                :feature-layer-id "test-layer"
                :color-assignment color-coding-1
                :attributes       [B-attr]
                :aggregate-method :average
                :color-map        color-map-1
                :color-scheme {:colors color-map-1,
                               :id "test-color"}
                :value-assigned [[0 1] [1 2] [2 3] [3 4] [4 5]]})

(def heatmap-layer {:name "Heatmap-Layer"
                    :id "heatmap"
                    :type :heatmap
                    :attributes [B-attr]
                    :extrema :local})
(def whitelist ["date" "location"])

(def extern-config-test (fn []
                          {:overlayers [{:as-base-layer? true
                                         :id "test-layer"
                                         :feature-properties {"COUNTRY" [:str "#country"]}
                                         :main-feature-property "COUNTRY"
                                         :grouping-attributes ["country"]}
                                        {:as-base-layer? true
                                         :id "test-layer-multi"
                                         :feature-properties {"COUNTRY" [:str "#country"]
                                                              "PROVINCE" [:str "#country" "_" "#province"]}
                                         :main-feature-property "COUNTRY"
                                         :grouping-attributes ["country" "province"]}]}))

(deftest renderable-layers-test
  (with-redefs [config-shared-map/extern-config extern-config-test]
    (testing "no renderable-layers"
      (is (= #{}
             (renderable-layers [] empty-test-data)))
      (is (= #{}
             (renderable-layers [avg-layer] empty-test-data)))
      (is (= #{}
             (renderable-layers [] single-test-data))))
    (testing "heatmap renderable-layer"
      (is (= #{}
             (renderable-layers [heatmap-layer] empty-test-data))
          (= #{"heatmap"}
             (renderable-layers [heatmap-layer] single-test-data))))
    (testing "feature renderable-layers"
      (is (= #{}
             (renderable-layers [avg-layer] empty-test-data)))
      (is (= #{"average"}
             (renderable-layers [avg-layer] single-test-data)))
      (is (= #{}
             (renderable-layers [(assoc avg-layer
                                        :feature-layer-id "test-layer-multi")] single-test-data)))
      (is (= #{"average"}
             (renderable-layers [(assoc avg-layer
                                        :feature-layer-id "test-layer-multi")]
                                (assoc-in single-test-data [0 "province"] "Hamburg")))))))

(deftest feature-values-test
  (with-redefs [config-shared-map/extern-config extern-config-test]
    (let [feature-properties->feature-values #'map.overlayers/feature-properties->feature-values]
      (testing "basic attr to feature-prop mapping"
        (is (= {"COUNTRY" "Germany"}
               (feature-properties->feature-values "test-layer"
                                                   {"country" "Germany"}))))
      (testing "multi feature-props mapping"
        (is (= {"COUNTRY" "Germany"
                "PROVINCE" "Germany_Hamburg"}
               (feature-properties->feature-values "test-layer-multi"
                                                   {"country" "Germany"
                                                    "province" "Hamburg"})))))))

(deftest layers-data-test
  (with-redefs [config-shared-map/extern-config extern-config-test]
    (testing :no-layers
      (is (= {} (layers-data [] empty-test-data true))))
    (testing :avg-layer
      (is (= {}
             (layers-data [avg-layer] empty-test-data true)))
      (is (= {"average" {:layer-id "average"
                         :feature-layer-id "test-layer"
                         :name "Average-Layer"
                         :data-set {{"COUNTRY" "Germany"} {:color "#4fb34f", :value 2.0, :feature-names {"country" "Germany"}, :attribute B-attr, :opacity config-shared-map/explorama-overlayer-opacity}}
                         :type :feature}}
             (layers-data [avg-layer] single-test-data true)))
      (is (= {"average" {:layer-id "average"
                         :feature-layer-id "test-layer"
                         :name "Average-Layer"
                         :data-set {{"COUNTRY" "Germany"} {:color "#4fb34f", :value 2.0, :feature-names {"country" "Germany"}, :attribute B-attr, :opacity config-shared-map/explorama-overlayer-opacity}}
                         :type :feature}}
             (layers-data [avg-layer] test-data-without-attribute true))))
    (testing :out-of-range
      (is (= {"average" {:layer-id "average"
                         :feature-layer-id "test-layer"
                         :name "Average-Layer"
                         :data-set {{"COUNTRY" "Germany"} {:color nil, :value 5.0, :feature-names {"country" "Germany"}, :attribute B-attr, :opacity config-shared-map/explorama-overlayer-opacity}}
                         :type :feature}}
             (layers-data [avg-layer] out-of-range-test-data true))))
    (testing :heatmap-layer
      (is (= {"heatmap" {:layer-id "heatmap"
                         :name "Heatmap-Layer"
                         :data-set {:data [{B-attr 2
                                            :lat 0
                                            :lng 0}]}
                         :type :heatmap
                         :config {:value-field B-attr
                                  :use-local-extrema true}}}
             (layers-data [heatmap-layer] single-test-data true)))
      (is (= {}
             (layers-data [heatmap-layer] empty-test-data true))))))

;;; Marker layer tests

(def marker-layer-category-wildcard
  {:id "fc4cdb53be49c7e410bae59336714a75"
   :name "Marker2"
   :type :marker
   :attribute-type "string"
   :attributes ["category"]
   :value-assigned [[] ["*"] [] [] []]
   :color-scheme {:name "Test Color"
                  :id "color"
                  :color-scale-numbers 5
                  :colors {:0 "#e33b3b"
                           :1 "#fb8d02"
                           :2 "#50678a"
                           :3 "#5c5b5b"
                           :4 "#babab9"}}})
(def marker-layer-category-specific
  {:id "fc4cdb53be49c7e410bae59336714a75"
   :name "Marker2"
   :type :marker
   :attribute-type "string"
   :attributes ["category"]
   :value-assigned [["category-1"] ["*"] [] [] ["category-2"]]
   :color-scheme {:name "Test Color"
                  :id "color"
                  :colors {:0 "#e33b3b"
                           :1 "#fb8d02"
                           :2 "#50678a"
                           :3 "#5c5b5b"
                           :4 "#babab9"}}})

(def marker-layer-category-no-wildcard
  {:id "fc4cdb53be49c7e410bae59336714a75"
   :name "Marker2"
   :type :marker
   :attribute-type "string"
   :attributes ["category"]
   :value-assigned [["category-1"] [] [] [] ["category-2"]]
   :color-scheme {:name "Test color"
                  :id "color"
                  :colors {:0 "#e33b3b"
                           :1 "#fb8d02"
                           :2 "#50678a"
                           :3 "#5c5b5b"
                           :4 "#babab9"}}})

(def range-marker-layer
  {:id "26641e55f42658917c525aedebd98ad3"
   :name "Marker1"
   :type :marker
   :attribute-type "number"
   :attributes ["Fact-1"]
   :value-assigned [[51 100000] [20 51] [6 20] [1 6] [0 1]]
   :color-scheme {:name "Test color"
                  :id "color"
                  :colors {:0 "#e33b3b"
                           :1 "#fb8d02"
                           :2 "#50678a"
                           :3 "#5c5b5b"
                           :4 "#babab9"}}})

(def marker-definition-org
  {:id "fc4cdb53be49c7e410bae59336714a75"
   :name "Marker2"
   :type :marker
   :attribute-type "string"
   :attributes ["org"]
   :value-assigned [["Random1"] [] [] [] ["Random2"]]
   :color-scheme {:name "Test Color"
                  :id "color"
                  :colors {:0 "#e33b3b"
                           :1 "#fb8d02"
                           :2 "#50678a"
                           :3 "#5c5b5b"
                           :4 "#babab9"}}})

(def simple-data-points [{"Fact-1" 0
                          "date" "2011-03-02"
                          "category" "category-1"
                          "id" "DS-A-ID38"
                          "bucket" "default"
                          "location" [[6.35 2.433]]}
                         {"Fact-1" 0
                          "date" "2011-09-13"
                          "category" "category-2"
                          "id" "DS-A-ID45"
                          "bucket" "default"
                          "location" [[6.35 2.433]]}
                         {"Fact-1" 5
                          "date" "2011-03-02"
                          "category" "category-1"
                          "id" "DS-A-ID381"
                          "bucket" "default"
                          "location" [[6.35 2.433]]}
                         {"Fact-1" 200
                          "date" "2011-09-13"
                          "category" "foo"
                          "id" "DS-A-ID452"
                          "bucket" "default"
                          "location" [[6.35 2.433]]}])

#_{:clj-kondo/ignore [:type-mismatch]}
(def default-opacity (double (/ config-shared-map/explorama-marker-opacity 100)))

(def not-found-style {:style {:color "#5c5b5b",
                              :fill true,
                              :fillColor "#dddddd",
                              :fillOpacity 0.7,
                              :opacity 0.8,
                              :radius 5,
                              :stroke true,
                              :weight 3}
                      :location [[6.35 2.433]]})

(def marker-layer-category-wildcard-data-result
  {"DS-A-ID38"
   {:style {:color "#fb8d02", :fill true, :fillColor "#fb8d02", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID38"]}
   "DS-A-ID381"
   {:style {:color "#fb8d02", :fill true, :fillColor "#fb8d02", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID381"]}
   "DS-A-ID45"
   {:style {:color "#fb8d02", :fill true, :fillColor "#fb8d02", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID45"]}
   "DS-A-ID452"
   {:style {:color "#fb8d02", :fill true, :fillColor "#fb8d02", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID452"]}})

(def marker-layer-category-specific-data-result
  {"DS-A-ID38"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID38"]}
   "DS-A-ID381"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID381"]}
   "DS-A-ID45"
   {:style {:color "#babab9", :fill true, :fillColor "#babab9", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID45"]}
   "DS-A-ID452"
   {:style {:color "#fb8d02", :fill true, :fillColor "#fb8d02", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID452"]}})

(def marker-layer-category-no-wildcard-data-result
  {"DS-A-ID38"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID38"]}
   "DS-A-ID381"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID381"]}
   "DS-A-ID45"
   {:style {:color "#babab9", :fill true, :fillColor "#babab9", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID45"]}
   "DS-A-ID452"
   (assoc not-found-style
          :event-id ["default" "DS-A-ID452"])})

(def marker-layer-range-data-result
  {"DS-A-ID38"
   {:style {:color "#babab9", :fill true, :fillColor "#babab9", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID38"]}
   "DS-A-ID381"
   {:style {:color "#5c5b5b", :fill true, :fillColor "#5c5b5b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID381"]}
   "DS-A-ID45"
   {:style {:color "#babab9", :fill true, :fillColor "#babab9", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID45"]}
   "DS-A-ID452"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "DS-A-ID452"]}})

(def marker-data-multi-attrs [{"Fact-1" 0
                               "date" "2011-03-02"
                               "org" ["Random1" "Random2"]
                               "id" "Rand1-Rand2"
                               "bucket" "default"
                               "location" [[6.35 2.433]]}
                              {"Fact-1" 0
                               "date" "2011-09-13"
                               "org" "Random1"
                               "id" "Rand1"
                               "bucket" "default"
                               "location" [[6.35 2.433]]}
                              {"Fact-1" 5
                               "date" "2011-03-02"
                               "org" ["Random2" "foo"]
                               "id" "Rand2-foo"
                               "bucket" "default"
                               "location" [[6.35 2.433]]}
                              {"Fact-1" 200
                               "date" "2011-09-13"
                               "org" "foo"
                               "id" "foo"
                               "bucket" "default"
                               "location" [[6.35 2.433]]}])
(def marker-layer-org-result
  {"Rand1"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "Rand1"]}
   "Rand1-Rand2"
   {:style {:color "#e33b3b", :fill true, :fillColor "#e33b3b", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "Rand1-Rand2"]}
   "Rand2-foo"
   {:style {:color "#babab9", :fill true, :fillColor "#babab9", :fillOpacity default-opacity, :radius config-shared-map/explorama-marker-radius, :stroke false}
    :location [[6.35 2.433]]
    :event-id ["default" "Rand2-foo"]}
   "foo"
   (assoc not-found-style
          :event-id ["default" "foo"])})

(deftest marker-layer-coloring
  (testing "only wildcard match"
    (is (= marker-layer-category-wildcard-data-result
           (get-in (layers-data [marker-layer-category-wildcard]
                                simple-data-points
                                true)
                   ["fc4cdb53be49c7e410bae59336714a75" :data-set]))))
  (testing "specific match and wildcard"
    (is (= marker-layer-category-specific-data-result
           (get-in (layers-data [marker-layer-category-specific]
                                simple-data-points
                                true)
                   ["fc4cdb53be49c7e410bae59336714a75" :data-set]))))
  (testing "specific with no wildcard"
    (is (= marker-layer-category-no-wildcard-data-result
           (get-in (layers-data [marker-layer-category-no-wildcard]
                                simple-data-points
                                true)
                   ["fc4cdb53be49c7e410bae59336714a75" :data-set]))))
  (testing "range attribute (number)"
    (is (= marker-layer-range-data-result
           (get-in (layers-data [range-marker-layer]
                                simple-data-points
                                true)
                   ["26641e55f42658917c525aedebd98ad3" :data-set]))))
  (testing "multi value attributes"
    (is (= marker-layer-org-result
           (get-in (layers-data [marker-definition-org]
                                marker-data-multi-attrs
                                true)
                   ["fc4cdb53be49c7e410bae59336714a75" :data-set])))))