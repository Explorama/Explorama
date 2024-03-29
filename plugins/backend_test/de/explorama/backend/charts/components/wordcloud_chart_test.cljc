(ns de.explorama.backend.charts.components.wordcloud-chart-test
  (:require #_[clojure.test :refer [deftest is testing]]
            [de.explorama.shared.common.test-data :as td]))

(def note-attributes
  ["notes"])
(def category-1-attributes
  [td/category-1])
(def stopping-attributes
  #{"notes"})
(def advanced-stopping-attributes
  #{td/category-1 "notes"})
(def stemming-attributes
  #{})
(def advanced-stemming-attributes
  #{td/category-1})
(def min-occurence
  1)
(def advanced-min-occurence
  2)

(def only-notes
  [1 3 []]) ;;TODO r1/charts fix this test

(def specific-attributes-category-1 ;;TODO r1/charts fix this test
  [1 5 [[(td/category-val "A" 1) 1]
        [(td/category-val "A" 2) 5]]])

(def advanced-settings ;;TODO r1/charts fix this test
  [5 5 []])

;;TODO r1/charts fix this test
#_(deftest wordcloud-chart-test
    (testing "only-notes"
      (is (= only-notes
             (println (charts/wordcloud-dataset td/all-events note-attributes stopping-attributes stemming-attributes min-occurence)))))
    (testing "specific-attributes-event-type"
      (is (= specific-attributes-event-type
             (charts/wordcloud-dataset td/all-events event-type-attributes stopping-attributes stemming-attributes min-occurence))))
    (testing "advanced-settings"
      (is (= advanced-settings
             (charts/wordcloud-dataset td/all-events event-type-attributes advanced-stopping-attributes advanced-stemming-attributes advanced-min-occurence)))))