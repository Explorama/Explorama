(ns de.explorama.frontend.map.impl.openlayers.util-test
  (:require [cljs.test :refer-macros [deftest testing is use-fixtures]]
            [de.explorama.frontend.map.map.impl.openlayers.util :as util]))

(def popup-result-marker-default
  (str "<div class=\"popup-content\" style=\"width: 350px;\"> <dl class=\"colored-bg\" style=\"background-color: #000000; color: #FFFFFF;\"><dt>test</dt><dd>test-title</dd></dl> <dl><dt>attr-1</dt><dd>0</dd><dt>attr-2</dt><dd>fooo</dd><dt>attr-3</dt><dd>bar</dd></dl> </div>"))

(def popup-result-marker-with-vector
  (str "<div class=\"popup-content\" style=\"width: 350px;\"> <dl class=\"colored-bg\" style=\"background-color: #000000; color: #FFFFFF;\"><dt>test</dt><dd>test-title</dd></dl> <dl><dt>attr-1</dt><dd>0</dd><dt>attr-2</dt><dd>fooo, bar, ha</dd><dt>attr-3</dt><dd>bar</dd></dl> </div>"))

(def popup-result-marker-missing-attribute
  (str "<div class=\"popup-content\" style=\"width: 350px;\"> <dl class=\"colored-bg\" style=\"background-color: #ffffff; color: #000000;\"><dt>test</dt><dd>test-title</dd></dl> <dl><dt>attr-1</dt><dd>0</dd><dt>attr-3</dt><dd>bar</dd></dl> </div>"))

(def popup-result-no-title
  (str "<div class=\"popup-content\" style=\"width: 350px;\">  <dl><dt>attr-1</dt><dd>0</dd><dt>attr-2</dt><dd>fooo</dd><dt>attr-3</dt><dd>bar</dd><dt>test</dt><dd>test-title</dd></dl> </div>"))

(def popup-result-no-title-all-attrs-flag
  (str "<div class=\"popup-content\" style=\"width: 350px;\">  <dl><dt>attr-1</dt><dd>0</dd><dt>attr-2</dt><dd>fooo</dd><dt>attr-3</dt><dd>bar</dd><dt>attr-4</dt><dd>20</dd><dt>test</dt><dd>test-title</dd></dl> </div>"))

(def default-marker {:color "#000000"
                     :event {"test" "test-title"
                             "attr-1" 0
                             "attr-2" "fooo"
                             "attr-3" "bar"
                             "attr-4" "blub"}
                     :title-attributes ["test"]
                     :display-attributes ["attr-1" "attr-2" "attr-3"]})

(def marker-with-vector {:color "#000000"
                         :event {"test" "test-title"
                                 "attr-1" 0
                                 "attr-2" ["fooo" "bar" "ha"]
                                 "attr-3" "bar"}
                         :title-attributes ["test"]
                         :display-attributes ["attr-1" "attr-2" "attr-3"]})

(def marker-with-missing-attr {:color "#ffffff"
                               :event {"test" "test-title"
                                       "attr-1" 0
                                       "attr-3" "bar"}
                               :title-attributes ["test"]
                               :display-attributes ["attr-1" "attr-2" "attr-3"]})

(def content-with-no-title {:event {"test" "test-title"
                                    "attr-1" 0
                                    "attr-2" "fooo"
                                    "attr-3" "bar"}
                            :display-attributes ["attr-1" "attr-2" "attr-3" "test"]})

(def content-with-no-title-all-attrs {:event {"test" "test-title"
                                              "attr-1" 0
                                              "attr-2" "fooo"
                                              "attr-3" "bar"
                                              "attr-4" 20}
                                      :display-attributes :all})

(defn localize-num-fn [num]
  (let [lang "en-GB"]
    (.toLocaleString num lang)))

(defn attribute-label-fn [attr]
  attr)

(deftest gen-popup-content
  (testing "Simple event-marker"
    (let [{:keys [color event 
                  title-attributes
                  display-attributes]} default-marker]
      (is (= popup-result-marker-default
             (util/gen-popup-content localize-num-fn
                                     attribute-label-fn
                                     color
                                     event 
                                     title-attributes
                                     display-attributes)))))
  (testing "event-marker with vector"
    (let [{:keys [color event
                  title-attributes
                  display-attributes]} marker-with-vector]
      (is (= popup-result-marker-with-vector
             (util/gen-popup-content localize-num-fn
                                     attribute-label-fn
                                     color
                                     event
                                     title-attributes
                                     display-attributes)))))
  (testing "event-marker with missing attribute"
    (let [{:keys [color event
                  title-attributes
                  display-attributes]} marker-with-missing-attr]
      (is (= popup-result-marker-missing-attribute
             (util/gen-popup-content localize-num-fn
                                     attribute-label-fn
                                     color
                                     event
                                     title-attributes
                                     display-attributes)))))
  (testing "Popup with no title"
    (let [{:keys [color event
                  title-attributes
                  display-attributes]} content-with-no-title]
      (is (= popup-result-no-title
             (util/gen-popup-content localize-num-fn
                                     attribute-label-fn
                                     color
                                     event
                                     title-attributes
                                     display-attributes)))))
  (testing "Popup with no title and all attributes"
    (let [{:keys [color event
                  title-attributes
                  display-attributes]} content-with-no-title-all-attrs]
      (is (= popup-result-no-title-all-attrs-flag
             (util/gen-popup-content localize-num-fn
                                     attribute-label-fn
                                     color
                                     event
                                     title-attributes
                                     display-attributes))))))